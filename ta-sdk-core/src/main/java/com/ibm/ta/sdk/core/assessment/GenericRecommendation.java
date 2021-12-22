/*
 * (C) Copyright IBM Corp. 2019,2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.ta.sdk.core.assessment;

import com.google.gson.reflect.TypeToken;
import com.ibm.ta.sdk.core.assessment.json.ComplexitiesJson;
import com.ibm.ta.sdk.core.assessment.json.TargetsJson;
import com.ibm.ta.sdk.core.util.GenericUtil;
import com.ibm.ta.sdk.spi.plugin.TAException;
import com.ibm.ta.sdk.spi.collect.AssessmentUnit;
import com.ibm.ta.sdk.spi.assess.ComplexityContributionJson;
import com.ibm.ta.sdk.spi.assess.IssueCategoryJson;
import com.ibm.ta.sdk.spi.recommendation.*;
import com.ibm.ta.sdk.spi.validation.TaJsonFileValidator;
import org.tinylog.Logger;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.ibm.ta.sdk.core.util.Constants.*;
import static com.ibm.ta.sdk.core.util.Constants.FILE_TARGETS_JSON;

public class GenericRecommendation implements Recommendation {
  private IssueRuleProcessor rcm;

  private String assessmentName;
  private List<ComplexityContribution> complexityRules = new ArrayList<ComplexityContribution>();
  private List<IssueCategory> issueCategories = new ArrayList<IssueCategory>();
  private List<Target> targets = new ArrayList<Target>();

  public GenericRecommendation(String assessmentName, Path issuesFile, Path issuesCatFile, Path complexityFile,
                               Path targetsFile) throws IOException, TAException {
    this.assessmentName = assessmentName;

    // Complexity
    ComplexitiesJson ccList = GenericUtil.getJsonObj(new TypeToken<ComplexitiesJson>(){}, complexityFile);
    complexityRules.addAll(ComplexityContributionJson.getComplexityContributionList(ccList.getComplexities()));

    // Targets
    TaJsonFileValidator.validateTarget(Files.newInputStream(targetsFile));
    TargetsJson _targets = GenericUtil.getJsonObj(new TypeToken<TargetsJson>(){}, targetsFile);
    targets.addAll(_targets.getTargets());

    // Issue Categories
    Map<String, IssueCategoryJson> icMap = GenericUtil.getJsonObj(new TypeToken<Map<String, IssueCategoryJson>>(){}, issuesCatFile);
    List<IssueCategory> ics = IssueCategoryJson.getIssueCategoryList(icMap);
    issueCategories.addAll(ics);
    Map<String, IssueCategory> issueCategoryMap = new HashMap<String, IssueCategory>();
    for (IssueCategory ic : ics) {
      issueCategoryMap.put(ic.getId(), ic);
    }

    // Issues
    String issueRulesJson = GenericUtil.readFileToString(issuesFile);
    rcm = new IssueRuleProcessor(issueRulesJson, issueCategoryMap);

  }

  public static GenericRecommendation createGenericRecommemndation(String assessmentName, String middlewareName) throws TAException, IOException {
    String middlewareDir = "/" + middlewareName + "/";
    try {
        return new GenericRecommendation(assessmentName,
                Paths.get(GenericRecommendation.class.getResource(middlewareDir + FILE_ISSUES_JSON).toURI()),
                Paths.get(GenericRecommendation.class.getResource(middlewareDir + FILE_ISSUECATS_JSON).toURI()),
                Paths.get(GenericRecommendation.class.getResource(middlewareDir + FILE_COMPLEXITIES_JSON).toURI()),
                Paths.get(GenericRecommendation.class.getResource(middlewareDir + FILE_TARGETS_JSON).toURI()));
      } catch (URISyntaxException e) {
        throw new TAException(e);
      }
  }

  @Override
  public String getCollectionUnitName() {
    return assessmentName;
  }

  @Override
  public List<ComplexityContribution> getComplexityContributions() {
    return complexityRules;
  }

  @Override
  public List<IssueCategory> getIssueCategories() {
    return issueCategories;
  }

  @Override
  public List<Target> getTargets() {
    return targets;
  }

  @Override
  public List<Issue> getIssues(Target target, AssessmentUnit assessmentUnit) throws TAException {

    return rcm.processIssues((GenericTarget) target, assessmentUnit);
  }
}
