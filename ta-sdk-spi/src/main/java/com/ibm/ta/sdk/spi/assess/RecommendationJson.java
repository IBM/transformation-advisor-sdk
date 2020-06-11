/*
 * (C) Copyright IBM Corp. 2019,2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.ta.sdk.spi.assess;

import com.google.gson.annotations.Expose;
import com.ibm.ta.sdk.spi.plugin.TAException;
import com.ibm.ta.sdk.spi.collect.AssessmentUnit;
import com.ibm.ta.sdk.spi.collect.Environment;
import com.ibm.ta.sdk.spi.recommendation.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

public class RecommendationJson {
  private static final String REC_ATTR_QM_NAME = "name";
  private static final String ASS_ATTR_TARGET = "targets";
  private static final String ASS_ATTR_TARGET_VERSION = "version";
  private static final String ASS_ATTR_TARGET_PLATFORM = "platform";
  private static final String ASS_ATTR_TARGET_LOCATION = "location";
  private static final String ASS_ATTR_TARGET_RUNTIME  = "runtime";
  private static final String ASS_ATTR_TARGET_PRODUCTNAME = "productName";
  private static final String ASS_ATTR_TARGET_PRODUCTVERSION = "productVersion";
  private static final String ASS_ATTR_ISSUES = "issues";
  private static final String ASS_ATTR_SUMMARY = "summary";
  private static final String ASS_ATTR_VALID = "valid";
  private static final String ASS_SUMMARY_COMPLEXITY = "complexity";
  private static final String ASS_SUMMARY_ISSUES = "issues";
  private static final String ASS_SUMMARY_EFFORT = "effort";
  private static final String ASS_SUMMARY_COMPLEXITY_SCORE = "score";

  @Expose
  protected String domain;

  @Expose
  protected String collectionUnitType;

  @Expose
  protected String collectionUnitTypeLabel;

  @Expose
  protected String collectionUnitName;

  @Expose
  protected String assessmentUnitSingleLabel;

  @Expose
  protected String assessmentUnitMultipleLabel;

  @Expose
  protected String middleware;

  @Expose
  protected String version;

  @Expose
  protected List<ComplexityContributionJson> complexityRules;

  @Expose
  protected Map<String, IssueCategoryJson> issueCategories;

  @Expose
  protected List<Map<String, Object>> assessmentUnits = new ArrayList<Map<String, Object>>();
  private Recommendation recommendation;

  private static Logger logger = LogManager.getLogger(RecommendationJson.class.getName());

  public RecommendationJson() {
    // Read from Json file
  }

  public RecommendationJson(Recommendation recommendation, Environment environment, List<? extends AssessmentUnit> auList) throws TAException {
    this.recommendation = recommendation;
    domain = environment.getDomain();
    middleware = environment.getMiddlewareName();
    collectionUnitType = environment.getCollectionUnitType();
    collectionUnitTypeLabel = environment.getCollectionUnitTypeLabel();
    collectionUnitName = environment.getCollectionUnitName();
    version = environment.getMiddlewareVersion();
    complexityRules = ComplexityContributionJson.getComplexityContributionJsonList(recommendation.getComplexityContributions());
    issueCategories = IssueCategoryJson.getIssueCategoryJsonMap(recommendation.getIssueCategories());
    assessmentUnitSingleLabel = environment.getAssessmentUnitSingleLabel();
    assessmentUnitMultipleLabel = environment.getAssessmentUnitMultipleLabel();

    for (Target target : recommendation.getTargets()) {
      for (AssessmentUnit au : auList) {
        List<Issue> auIssues = recommendation.getIssues(target, au);

        // Build map of issues by category
        Map<String, List<Issue>> issuesMap = new LinkedHashMap<String, List<Issue>>();
        for (Issue issue : auIssues) {
          IssueCategory iCat = issue.getCategory();
          if (iCat == null) {
            throw new TAException("No matching issue category found for issue with ID:" + issue.getId());
          }
          String issueCat = iCat.getId();

          List<Issue> issuesList = issuesMap.get(issueCat);
          if (issuesList == null) {
            issuesList = new ArrayList<Issue>();
            issuesMap.put(issueCat, issuesList);
          }
          issuesList.add(issue);
        }

        Map<String, Object> auMap = getAssessmentUnit(target, au,issuesMap);
        assessmentUnits.add(auMap);
      }
    }
  }

  private Map<String, Object> getAssessmentUnit(Target target, AssessmentUnit au, Map<String, List<Issue>>  issuesMap) {
    Map<String, Object> auMap = new LinkedHashMap<String, Object>();
    auMap.put(REC_ATTR_QM_NAME, au.getName());
    List<Map<String, Object>> targetList = new LinkedList<Map<String, Object>>();
    auMap.put(ASS_ATTR_TARGET, targetList);
    Map<String, Object> targetMap = new LinkedHashMap<String, Object>();
    targetList.add(targetMap);

    targetMap.put(ASS_ATTR_TARGET_VERSION, target.getProductVersion());
    targetMap.put(ASS_ATTR_TARGET_PRODUCTNAME, target.getProductName());
    targetMap.put(ASS_ATTR_TARGET_PRODUCTVERSION, target.getProductVersion());
    String targetRuntime = target.getRuntime();
    if (targetRuntime != null && !targetRuntime.equals("")) {
      targetMap.put(ASS_ATTR_TARGET_RUNTIME, target.getRuntime());
    }
    targetMap.put(ASS_ATTR_TARGET_PLATFORM, target.getPlatform());
    targetMap.put(ASS_ATTR_TARGET_LOCATION, target.getLocation());

    targetMap.put(ASS_ATTR_VALID, "true");
    targetMap.put(ASS_ATTR_ISSUES, issuesMap);
    targetMap.put(ASS_ATTR_SUMMARY, getAssessmentSummary(issuesMap));
    return auMap;
  }

  private Map<String, Object> getAssessmentSummary(Map<String, List<Issue>> issues) {
    Map<String, Object> summaryMap = new LinkedHashMap<String, Object>();
    summaryMap.put(ASS_SUMMARY_COMPLEXITY, getSummaryComplexity(issues));
    summaryMap.put(ASS_SUMMARY_COMPLEXITY, getSummaryComplexity(issues));
    summaryMap.put(ASS_SUMMARY_ISSUES, getSummaryIssues(issues));
    summaryMap.put(ASS_SUMMARY_EFFORT, getSummaryDevEffort(issues));

    return summaryMap;
  }

  private Map<String, Object>  getSummaryComplexity(Map<String, List<Issue>> issues) {
    Map<String, Object> complexityMap = new LinkedHashMap<String, Object>();

    Iterator<String> itIssueCategory = issues.keySet().iterator();
    while (itIssueCategory.hasNext()) {
      String issueCategory = itIssueCategory.next();
      List<Issue> categoryIssues = issues.get(issueCategory);
      logger.trace("Get complexity for category:" + issueCategory + " issues:" + categoryIssues);
      for (Issue catIs : categoryIssues) {
        ComplexityContribution matchedCC = getMatchedComplexityContribution(catIs);
        if (matchedCC != null) {
          catIs.setComplexityContribution(matchedCC);
          String issueComplexity = matchedCC.getComplexity().name();
          Map<String, Integer> complexityScoreMap = (Map<String, Integer>) complexityMap.get(issueComplexity);
          if (complexityScoreMap == null) {
            complexityScoreMap = new LinkedHashMap<String, Integer>();
            complexityMap.put(issueComplexity, complexityScoreMap);
          }
          Integer ccount = complexityScoreMap.get(catIs.getCategory());
          if (ccount == null) {
            ccount = 0;
          }

          complexityScoreMap.put(catIs.getCategory().getId(), ++ccount);
        } else {
          logger.warn("No complexity rule found for issue:" + catIs.getId());
        }
      }
    }

    addComplexityScore(complexityMap);
    return complexityMap;
  }

  private ComplexityContribution getMatchedComplexityContribution(Issue issue) {
    logger.trace("Find matching complexity contribution for issue:" + issue);

    ComplexityContribution matchedCC = null;
    for (ComplexityContribution cc : recommendation.getComplexityContributions()) {
      List<String> ruleIssues = cc.getIssues();
      if (ruleIssues != null) {
        for (String ruleIssue : ruleIssues) {
          if (ruleIssue.equals(issue.getId())) {
            logger.debug("Matching complexity contribution found (matched by ID):" + cc);
            return cc;
          }
        }
      }

      List<String> ruleCategories = cc.getIssuesCategory();
      if (ruleCategories != null) {
        for (String ruleCategory : ruleCategories) {
          if (ruleCategory.equals(issue.getCategory().getId())) {
            logger.debug("Matching complexity contribution found (matched by category):" + cc);
            if (matchedCC == null) {
              matchedCC = cc;
            } else {
              if (cc.getComplexity().compareTo(matchedCC.getComplexity()) > 0) {
                matchedCC = cc;
              }
            }
            break; // Continue to the next rule
          }
        }
      }
    }
    return matchedCC;
  }

  private void addComplexityScore(Map<String, Object> complexityMap) {
    ComplexityRating maxComplexity = ComplexityRating.simple;

    Iterator<String> itComplexity = complexityMap.keySet().iterator();
    while (itComplexity.hasNext()) {
      ComplexityRating complexity = ComplexityRating.valueOf(itComplexity.next());
      if (maxComplexity == null || complexity.compareTo(maxComplexity) > 0) {
        maxComplexity = complexity;
      }
    }

    complexityMap.put(ASS_SUMMARY_COMPLEXITY_SCORE, maxComplexity.toString());
  }

  private Map<String, Integer>  getSummaryIssues(Map<String, List<Issue>>  issues) {
    Map<String, Integer> summaryIssuesMap = new LinkedHashMap<String, Integer>();

    Iterator<String> itIssueCategory = issues.keySet().iterator();
    while (itIssueCategory.hasNext()) {
      String issueCategory = itIssueCategory.next();
      List<Issue> categoryIssues = issues.get(issueCategory);
      for (Issue catIs : categoryIssues) {
        String severity = catIs.getSeverity().name();
        Integer sevCount = summaryIssuesMap.get(severity);
        if (sevCount == null) {
          sevCount = 0;
        }
        summaryIssuesMap.put(severity, ++sevCount);
      }
    }

    return summaryIssuesMap;
  }

  private Map<String, Float>  getSummaryDevEffort(Map<String, List<Issue>>  issues) {
    Map<String, Float> effortMap = new LinkedHashMap<String, Float>();
    float totalCost = 0f;

    Iterator<String> itIssueCategory = issues.keySet().iterator();
    while (itIssueCategory.hasNext()) {
      float categoryTotalCost = 0f;

      String issueCategory = itIssueCategory.next();
      List<Issue> categoryIssues = issues.get(issueCategory);
      for (Issue catIs : categoryIssues) {
        categoryTotalCost += catIs.getCost();
      }

      totalCost += categoryTotalCost;
      effortMap.put(issueCategory, categoryTotalCost);
    }
    effortMap.put("total", totalCost);

    return effortMap;
  }
}
