/*
 * (C) Copyright IBM Corp. 2019,2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.ta.sdk.core.assessment;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.ibm.ta.sdk.spi.plugin.TAException;
import com.ibm.ta.sdk.spi.collect.AssessmentUnit;
import com.ibm.ta.sdk.spi.recommendation.Issue;
import com.ibm.ta.sdk.spi.recommendation.IssueCategory;
import com.ibm.ta.sdk.core.util.GenericUtil;
import com.ibm.ta.sdk.core.detector.IssueRuleTypeProvider;

import java.util.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class IssueRuleProcessor {

  private static final String ISSUERULE_MATCH_CRITERIA = "matchCriteria";
  private static final String ISSUERULE_PROVIDER = "ruleType";

  private Map<String, JsonObject> issueRulesMap;
  private Map<String, List<String>> issueCatIssueRulesMap;
  private Map<String, IssueCategory> issueCategories;

  private ServiceLoader<IssueRuleTypeProvider> serviceLoader = ServiceLoader.load(IssueRuleTypeProvider.class);
  private Map<String, IssueRuleTypeProvider> ruleProviderMap = new HashMap<String, IssueRuleTypeProvider>();

  private static Logger logger = LogManager.getLogger(IssueRuleProcessor.class.getName());

  public IssueRuleProcessor(String issuesJson, Map<String, IssueCategory> issueCategories) {
    this.issueCategories = issueCategories;

    // Build map with issue rules
    // Build a map of the list of issues in each issue category
    issueRulesMap = new HashMap<>();
    issueCatIssueRulesMap = new HashMap<>();
    JsonObject issueRulesJson = new JsonParser().parse(issuesJson).getAsJsonObject();
    for (Iterator it = issueRulesJson.get("issues").getAsJsonArray().iterator(); it.hasNext(); ) {
      JsonObject issueRule = (JsonObject) it.next();
      String issueRuleId = issueRule.get("id").getAsString();
      String issueRuleCat = issueRule.get("category").getAsString();
      issueRulesMap.put(issueRuleId, issueRule);

      List<String> issueRulesInCat = issueCatIssueRulesMap.get(issueRuleCat);
      if (issueRulesInCat == null) {
        issueRulesInCat = new ArrayList<>();
        issueCatIssueRulesMap.put(issueRuleCat, issueRulesInCat);
      }
      issueRulesInCat.add(issueRuleId);
    }

    // Build map of issue rule providers
    Iterator<IssueRuleTypeProvider> itIRprovider = serviceLoader.iterator();
    while (itIRprovider.hasNext()) {
      IssueRuleTypeProvider irProvider = itIRprovider.next();
      logger.debug("Adding issue rule provider:" + irProvider.getName());
      ruleProviderMap.put(irProvider.getName(), irProvider);
    }
  }

  public List<Issue> processIssues(GenericTarget target, AssessmentUnit assessmentUnit) throws TAException {
    List<Issue> issueList = new ArrayList<Issue>();

    // Get list of issues applicable to this target,
    // Add issues from issue categories
    Set<String> issueRulesSet = new TreeSet<>();
    issueRulesSet.addAll(target.getIssues());
    for (String issueCats : target.getIssueCategories()) {
      List<String> issuesInCat = issueCatIssueRulesMap.get(issueCats);
      if (issuesInCat != null) {
        issueRulesSet.addAll(issuesInCat);
      }
    }

    // If no issues or issue categories in target, add all issues to target
    if (target.getIssues().isEmpty() && target.getIssueCategories().isEmpty()) {
      issueRulesSet.addAll(issueRulesMap.keySet());
    }

    // Create issue rule array with only issues listed in the target
    JsonArray issueRulesJson = new JsonArray();
    for (String targetIssue : issueRulesSet.toArray(new String[]{})) {
      JsonObject issueRuleJson = issueRulesMap.get(targetIssue);
      if (issueRuleJson != null) {
        issueRulesJson.add(issueRuleJson);
      } else {
        logger.warn("Issue " + issueRuleJson + " in target " + target.getTargetId() + " not found.");
      }
    }

    for (int i = 0; i < issueRulesJson.size(); i++) {
      JsonObject issueRuleJson = issueRulesJson.get(i).getAsJsonObject();
      logger.info("Process recommendation rule:" + issueRuleJson.toString());

      IssueRuleTypeProvider ruleProvider = getIssueRuleProvider(issueRuleJson);
      if (ruleProvider == null) {
        logger.error("Rule cannot be processed, no provider found for rule:" + issueRuleJson);
        continue;
      }

      IssueRule issueRule = getIssueRule(ruleProvider, issueRuleJson);
      GenericIssue issue = ruleProvider.getIssue(target, assessmentUnit, issueRule);
      issue.setCategory(issueCategories.get(issueRule.getCategory()));

      // Merge the issues, by category, then ID
      List<Map<String, String>> occurrences = issue.getOccurrence().getOccurrencesInstances();
      if (occurrences != null && occurrences.size() != 0) {
        // Get recommendation with same ID if it already exists and merge the occurrences
        Issue matchingIssue = getIssueById(issueList, issue);
        if (matchingIssue == null) {
          issueList.add(issue);
        } else {
          ((GenericIssue) matchingIssue).addOccurences(occurrences);
        }
      }
    }

    return issueList;
  }

  public IssueRule getIssueRule(IssueRuleTypeProvider ruleProvider, JsonObject issueRuleJson) {
    IssueRule issueRule = GenericUtil.getJsonObj(new TypeToken<IssueRule>(){}, issueRuleJson);
    issueRule.setMatchCriteria(ruleProvider.getIssueMatchCriteria(issueRule.getMatchCriteriaJson()));
    return issueRule;
  }

  private IssueRuleTypeProvider getIssueRuleProvider(JsonObject issueRuleJson) {
    JsonElement mcJsonE = issueRuleJson.get(ISSUERULE_MATCH_CRITERIA);
    if (mcJsonE != null && !mcJsonE.isJsonNull()) {
      JsonElement providerJsonE = mcJsonE.getAsJsonObject().get(ISSUERULE_PROVIDER);
      if (providerJsonE != null && !providerJsonE.isJsonNull()) {
        return ruleProviderMap.get(providerJsonE.getAsString());
      }
    }
    return null;
  }

  private Issue getIssueById(List<Issue> issueList, Issue targetIssue) {
    for (Issue iss : issueList) {
      if (iss.getId().equals(targetIssue.getId())) {
        return iss;
      }
    }
    return null;
  }
}
