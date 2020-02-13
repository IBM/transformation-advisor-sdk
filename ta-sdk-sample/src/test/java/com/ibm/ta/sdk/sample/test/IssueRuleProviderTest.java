/*
 * (C) Copyright IBM Corp. 2019,2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.ta.sdk.sample.test;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.ibm.ta.sdk.sample.SamplePluginProvider;
import com.ibm.ta.sdk.spi.collect.AssessmentUnit;
import com.ibm.ta.sdk.core.util.GenericUtil;
import com.ibm.ta.sdk.core.collect.GenericAssessmentUnit;
import com.ibm.ta.sdk.core.assessment.GenericIssue;
import com.ibm.ta.sdk.core.assessment.GenericTarget;
import com.ibm.ta.sdk.core.assessment.IssueRule;
import com.ibm.ta.sdk.core.detector.IssueRuleTypeProvider;


import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class IssueRuleProviderTest {

    private static final String FILE_ASSESS_DATA_JSON = "/sampleData/AssessmentUnit1.json";
    private static final String FILE_ISSUE_JSON = "/sample/issue.json";
    private static final String FILE_ASSESS_CONFIG_FILE_XML = "/sampleData/sampleData.xml";
    private static final String FILE_ASSESS_CONFIG_FILE_JSON = "/sampleData/SampleConfigFile.json";
    private static final String FILE_ASSESS_CONFIG_FILE2_JSON = "/sampleData/SampleConfigFile2.json";
    private static Map<String, IssueRuleTypeProvider> ruleProviderMap = new HashMap();

    static {
        ServiceLoader<IssueRuleTypeProvider> serviceLoader = ServiceLoader.load(IssueRuleTypeProvider.class);
        Iterator itIRprovider = serviceLoader.iterator();
        while(itIRprovider.hasNext()) {
            IssueRuleTypeProvider irtProvider = (IssueRuleTypeProvider)itIRprovider.next();
            ruleProviderMap.put(irtProvider.getName(), irtProvider);
        }
    }

    public static void main(String[] argu) throws Exception {
        System.out.println("start");
        Path issueJsonFile = Paths.get(SamplePluginProvider.class.getResource(FILE_ISSUE_JSON).toURI());
        Path assessDataJsonFile = Paths.get(SamplePluginProvider.class.getResource(FILE_ASSESS_DATA_JSON).toURI());
        Path assessConfigXmlFile = Paths.get(SamplePluginProvider.class.getResource(FILE_ASSESS_CONFIG_FILE_XML).toURI());
        Path assessConfigJsonFile = Paths.get(SamplePluginProvider.class.getResource(FILE_ASSESS_CONFIG_FILE_JSON).toURI());
        Path assessConfigJsonFile2 = Paths.get(SamplePluginProvider.class.getResource(FILE_ASSESS_CONFIG_FILE2_JSON).toURI());
        List<Path> assessmentConfigFiles = new ArrayList<Path>();
        assessmentConfigFiles.add(assessConfigJsonFile);
        assessmentConfigFiles.add(assessConfigXmlFile);
        assessmentConfigFiles.add(assessConfigJsonFile2);
        String issueRulesJson = GenericUtil.readFileToString(issueJsonFile);
        JsonArray issueRulesJsonArray = (new JsonParser()).parse(issueRulesJson).getAsJsonArray();
        AssessmentUnit assessmentUnit = new GenericAssessmentUnit(assessDataJsonFile, assessmentConfigFiles);
        JsonObject issueRuleJson = issueRulesJsonArray.get(7).getAsJsonObject();
        IssueRuleTypeProvider ruleProvider = ruleProviderMap.get("xml");
        IssueRule issueRule = (IssueRule)GenericUtil.getJsonObj(new TypeToken<IssueRule>() {}, issueRuleJson);
        issueRule.setMatchCriteria(ruleProvider.getIssueMatchCriteria(issueRule.getMatchCriteriaJson()));
        GenericIssue issue = ruleProvider.getIssue(new GenericTarget(), assessmentUnit, issueRule);
        System.out.println("complete");
    }
}
