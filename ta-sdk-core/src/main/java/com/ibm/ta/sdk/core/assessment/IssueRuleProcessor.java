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
import com.ibm.ta.sdk.spi.recommendation.Target;
import com.ibm.ta.sdk.core.util.GenericUtil;
import com.ibm.ta.sdk.core.detector.IssueRuleTypeProvider;

import java.util.*;

import org.apache.log4j.Logger;

public class IssueRuleProcessor {

  private static final String ISSUERULE_MATCH_CRITERIA = "matchCriteria";
  private static final String ISSUERULE_PROVIDER = "ruleType";

  private String issuesJson;
  private Map<String, IssueCategory> issueCategories;

  private ServiceLoader<IssueRuleTypeProvider> serviceLoader = ServiceLoader.load(IssueRuleTypeProvider.class);
  private Map<String, IssueRuleTypeProvider> ruleProviderMap = new HashMap<String, IssueRuleTypeProvider>();

  private static Logger logger = Logger.getLogger(IssueRuleProcessor.class.getName());

  public IssueRuleProcessor(String issuesJson, Map<String, IssueCategory> issueCategories) {
    this.issuesJson = issuesJson;
    this.issueCategories = issueCategories;

    // Build map of issue rule providers
    Iterator<IssueRuleTypeProvider> itIRprovider = serviceLoader.iterator();
    while (itIRprovider.hasNext()) {
      IssueRuleTypeProvider irProvider = itIRprovider.next();
      logger.debug("Adding issue rule provider:" + irProvider.getName());
      ruleProviderMap.put(irProvider.getName(), irProvider);
    }
  }

  public List<Issue> processIssues(Target target, AssessmentUnit assessmentUnit) throws TAException {
    List<Issue> issueList = new ArrayList<Issue>();

    JsonArray issueRulesJson = new JsonParser().parse(issuesJson).getAsJsonArray();
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
