/*
 * (C) Copyright IBM Corp. 2019,2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.ta.sdk.core.detector;

import com.ibm.ta.sdk.core.assessment.GenericRecommendation;
import com.ibm.ta.sdk.core.collect.GenericAssessmentUnit;
import com.ibm.ta.sdk.spi.plugin.TAException;
import com.ibm.ta.sdk.spi.recommendation.Issue;
import com.ibm.ta.sdk.spi.recommendation.Occurrence;
import com.ibm.ta.sdk.spi.recommendation.Severity;
import com.ibm.ta.sdk.spi.recommendation.Target;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.ibm.ta.sdk.core.util.Constants.*;
import static org.junit.jupiter.api.Assertions.*;

public class JsonDetectorTest {
    public static final String TEST_RESOURCES_DIR = "src" + File.separator + "test" + File.separator + "resources";

    /*
     * Simple filter by attribute and checks the values for all the issue attributes are correct.
     */
    @Test
    public void validIssueAttributesTest() {
        try {
            Path assessmentUnitFile = new File(TEST_RESOURCES_DIR, "collect/AssessmentUnit.json").toPath();
            GenericAssessmentUnit au = new GenericAssessmentUnit(assessmentUnitFile, null);

            Path issueJsonFile = new File(TEST_RESOURCES_DIR, "issue/issue_attr_filter.json").toPath();
            GenericRecommendation rec = getRecommendation(issueJsonFile);
            List<Target> targets = rec.getTargets();
            assertEquals(1, targets.size());

            Target target = targets.get(0);
            List<Issue> issues = rec.getIssues(target, au);
            assertEquals(1, issues.size());

            Issue issue = issues.get(0);
            assertEquals("MQCL03", issue.getId());
            assertEquals("Contains a Full Repository.  Assess impact to other cluster members.", issue.getTitle());
            assertEquals(1.0f, issue.getCost());
            assertEquals(0.5f, issue.getOverheadCost());
            assertEquals(0.5f, issue.getOccurrenceCost());
            assertEquals(Arrays.asList("Update other Cluster members using IPAddresses to use the new IPAddress after migrating"),
                    issue.getSolutionText());
            assertEquals(Severity.potential, issue.getSeverity());
            assertEquals("cluster", issue.getCategory().getId());
            assertEquals(1, issue.getOccurrencesCount());

            // Check occurrences
            Occurrence occurrence = issue.getOccurrence();
            Map<String, String> expectedFieldKeys = new HashMap<>();
            expectedFieldKeys.put("cluster", "Cluster");
            assertEquals(expectedFieldKeys, occurrence.getFieldKeys());
            Map<String, String> expectedOcInstance = new HashMap<>();
            expectedOcInstance.put("cluster", "INVENTORY");
            assertEquals(Arrays.asList(expectedOcInstance), occurrence.getOccurrencesInstances());
        } catch (IOException | TAException e) {
            throw new AssertionFailedError("Error generating recommendations", e);
        }
    }

    /*
     * Simple filter by attribute and checks the values for all the issue attributes are correct.
     */
    @Test
    public void issueKeyNameWithSlashTest() {
        try {
            Path assessmentUnitFile = new File(TEST_RESOURCES_DIR, "collect/AssessmentUnit.json").toPath();
            GenericAssessmentUnit au = new GenericAssessmentUnit(assessmentUnitFile, null);

            Path issueJsonFile = new File(TEST_RESOURCES_DIR, "issue/issue_keyname_with_slash.json").toPath();
            GenericRecommendation rec = getRecommendation(issueJsonFile);
            List<Target> targets = rec.getTargets();
            assertEquals(1, targets.size());

            Target target = targets.get(0);
            List<Issue> issues = rec.getIssues(target, au);
            assertEquals(1, issues.size());

            Issue issue = issues.get(0);
            assertEquals("MQCL03", issue.getId());
            assertEquals("Contains a Full Repository.  Assess impact to other cluster members.", issue.getTitle());
            assertEquals(1.5f, issue.getCost());
            assertEquals(0.5f, issue.getOverheadCost());
            assertEquals(0.5f, issue.getOccurrenceCost());
            assertEquals(Arrays.asList("Update other Cluster members using IPAddresses to use the new IPAddress after migrating"),
                    issue.getSolutionText());
            assertEquals(Severity.potential, issue.getSeverity());
            assertEquals("cluster", issue.getCategory().getId());
            assertEquals(2, issue.getOccurrencesCount());

            // Check occurrences
            Occurrence occurrence = issue.getOccurrence();
            Map<String, String> expectedFieldKeys = new HashMap<>();
            expectedFieldKeys.put("mainCluster", "Main Cluster");
            assertEquals(expectedFieldKeys, occurrence.getFieldKeys());
            Map<String, String> expectedOcInstance = new HashMap<>();
            expectedOcInstance.put("mainCluster", "INVENTORY");
            Map<String, String> expectedOcInstance2 = new HashMap<>();
            expectedOcInstance2.put("mainCluster", "PAYABLE");
            assertEquals(Arrays.asList(expectedOcInstance, expectedOcInstance2), occurrence.getOccurrencesInstances());
        } catch (IOException | TAException e) {
            throw new AssertionFailedError("Error generating recommendations", e);
        }
    }

    /*
     * Simple filter by attribute and checks the values for all the issue attributes are correct.
     */
    @Test
    public void issueTargetedSolutions() {
        try {
            Path assessmentUnitFile = new File(TEST_RESOURCES_DIR, "collect/AssessmentUnit.json").toPath();
            GenericAssessmentUnit au = new GenericAssessmentUnit(assessmentUnitFile, null);

            Path issueJsonFile = new File(TEST_RESOURCES_DIR, "issue/issue_targeted_solutions.json").toPath();
            Path complexityJsonFile = new File(TEST_RESOURCES_DIR, "assess" + File.separator + FILE_COMPLEXITIES_JSON).toPath();
            Path issueCatJsonFile = new File(TEST_RESOURCES_DIR, "assess" + File.separator + FILE_ISSUECATS_JSON).toPath();
            Path targetJsonFile = new File(TEST_RESOURCES_DIR, "target" + File.separator + "targets_three.json").toPath();
    
            GenericRecommendation rec = new GenericRecommendation("assessment1", issueJsonFile,
                    issueCatJsonFile, complexityJsonFile, targetJsonFile);
            List<Target> targets = rec.getTargets();
            assertEquals(3, targets.size());

            Boolean foundTargetA = false, foundTargetB = false, foundTargetC = false;

            // we can't _guarantee_ the order of the targets, so we iterate through the list and check which of the 3 targets we've found.
            for (int i = 0; i < targets.size(); i++ ) {
                Target target = targets.get(i);
                if (target.getTargetId().equals("targetA")) {
                    if (foundTargetA) fail("found targetA twice in targets");
                    foundTargetA = true;
                    List<Issue> issues = rec.getIssues(target, au);
                    assertEquals(1, issues.size());

                    Issue issue = issues.get(0);
                    assertEquals(Arrays.asList("targetA solution"),
                            issue.getSolutionText());

                } else if (target.getTargetId().equals("targetB")){ 
                    if (foundTargetB) fail("found targetB twice in targets");
                    foundTargetB = true;
                    List<Issue> issues = rec.getIssues(target, au);
                    assertEquals(1, issues.size());

                    Issue issue = issues.get(0);
                    assertEquals(Arrays.asList("targetB solution"),
                            issue.getSolutionText());

                } else if (target.getTargetId().equals("targetC")){ 
                    if (foundTargetC) fail("found targetC twice in targets");
                    foundTargetC = true;
                    List<Issue> issues = rec.getIssues(target, au);
                    assertEquals(1, issues.size());

                    Issue issue = issues.get(0);
                    assertEquals(Arrays.asList("Untargeted solution"),
                            issue.getSolutionText());

                } else {
                    fail("found unexpected target: " + target.getTargetId());
                }
            }
        } catch (IOException | TAException e) {
            throw new AssertionFailedError("Error generating recommendations", e);
        }
    }


    /*
     * Test no matching issue found
     */
    @Test
    public void issueNotFoundTest() {
        try {
            Path assessmentUnitFile = new File(TEST_RESOURCES_DIR, "collect/AssessmentUnit.json").toPath();
            GenericAssessmentUnit au = new GenericAssessmentUnit(assessmentUnitFile, null);

            Path issueJsonFile = new File(TEST_RESOURCES_DIR, "issue/issue_not_found.json").toPath();
            GenericRecommendation rec = getRecommendation(issueJsonFile);
            List<Target> targets = rec.getTargets();
            assertEquals(1, targets.size());

            Target target = targets.get(0);
            List<Issue> issues = rec.getIssues(target, au);
            assertEquals(0, issues.size());
       } catch (IOException | TAException e) {
            throw new AssertionFailedError("Error generating recommendations", e);
        }
    }

    /*
     * Test retrieval of parent attribute using @parent
     */
    @Test
    public void parentAttributesTest() {
        try {
            Path assessmentUnitFile = new File(TEST_RESOURCES_DIR, "collect/AssessmentUnit.json").toPath();
            GenericAssessmentUnit au = new GenericAssessmentUnit(assessmentUnitFile, null);

            Path issueJsonFile = new File(TEST_RESOURCES_DIR, "issue/issue_parent_attr.json").toPath();
            GenericRecommendation rec = getRecommendation(issueJsonFile);
            List<Target> targets = rec.getTargets();
            assertEquals(1, targets.size());

            Target target = targets.get(0);
            List<Issue> issues = rec.getIssues(target, au);
            assertEquals(1, issues.size());

            Issue issue = issues.get(0);

            // Check occurrences
            Occurrence occurrence = issue.getOccurrence();
            Map<String, String> expectedFieldKeys = new HashMap<>();
            expectedFieldKeys.put("queueName", "Queue name");
            assertEquals(expectedFieldKeys, occurrence.getFieldKeys());
            Map<String, String> expectedOcInstance = new HashMap<>();
            expectedOcInstance.put("queueName", "NewYork");
            assertEquals(Arrays.asList(expectedOcInstance), occurrence.getOccurrencesInstances());
        } catch (IOException | TAException e) {
            throw new AssertionFailedError("Error generating recommendations", e);
        }
    }


    /*
     * Test retrieval of nr attribute using @parent.
     * This returns a static value for the attribute (everything after '@nr.').
     * Another approach is not to use 'path' and introduce 'value'
     */
    @Test
    public void nrAttributesTest() {
        try {
            Path assessmentUnitFile = new File(TEST_RESOURCES_DIR, "collect/AssessmentUnit.json").toPath();
            GenericAssessmentUnit au = new GenericAssessmentUnit(assessmentUnitFile, null);

            Path issueJsonFile = new File(TEST_RESOURCES_DIR, "issue/issue_nr_attr.json").toPath();
            GenericRecommendation rec = getRecommendation(issueJsonFile);
            List<Target> targets = rec.getTargets();
            assertEquals(1, targets.size());

            Target target = targets.get(0);
            List<Issue> issues = rec.getIssues(target, au);
            assertEquals(1, issues.size());

            Issue issue = issues.get(0);

            // Check occurrences
            Occurrence occurrence = issue.getOccurrence();
            Map<String, String> expectedFieldKeys = new HashMap<>();
            expectedFieldKeys.put("cluster", "Cluster");
            assertEquals(expectedFieldKeys, occurrence.getFieldKeys());
            Map<String, String> expectedOcInstance = new HashMap<>();
            expectedOcInstance.put("cluster", "MY_CLUSTER");
            assertEquals(Arrays.asList(expectedOcInstance), occurrence.getOccurrencesInstances());
        } catch (IOException | TAException e) {
            throw new AssertionFailedError("Error generating recommendations", e);
        }
    }


    /*
     * Test attribute @resolvedFilterPath, which returns the JSON Path of the matching JSON object found
     */
    @Test
    public void resolvedFilterPathtAttributesTest() {
        try {
            Path assessmentUnitFile = new File(TEST_RESOURCES_DIR, "collect/AssessmentUnit.json").toPath();
            GenericAssessmentUnit au = new GenericAssessmentUnit(assessmentUnitFile, null);

            Path issueJsonFile = new File(TEST_RESOURCES_DIR, "issue/issue_resolvedFilterPath_attr.json").toPath();
            GenericRecommendation rec = getRecommendation(issueJsonFile);
            List<Target> targets = rec.getTargets();
            assertEquals(1, targets.size());

            Target target = targets.get(0);
            List<Issue> issues = rec.getIssues(target, au);
            assertEquals(1, issues.size());

            Issue issue = issues.get(0);

            // Check occurrences
            Occurrence occurrence = issue.getOccurrence();
            Map<String, String> expectedFieldKeys = new HashMap<>();
            expectedFieldKeys.put("cluster", "Cluster");
            assertEquals(expectedFieldKeys, occurrence.getFieldKeys());
            Map<String, String> expectedOcInstance = new HashMap<>();
            expectedOcInstance.put("cluster", "[clusters][0]");
            assertEquals(Arrays.asList(expectedOcInstance), occurrence.getOccurrencesInstances());
        } catch (IOException | TAException e) {
            throw new AssertionFailedError("Error generating recommendations", e);
        }
    }

    /*
     * Test filtering by @filterPathKey
     */
    @Test
    public void filterPathKeyTest() {
        try {
            Path assessmentUnitFile = new File(TEST_RESOURCES_DIR, "collect/AssessmentUnit.json").toPath();
            GenericAssessmentUnit au = new GenericAssessmentUnit(assessmentUnitFile, null);

            Path issueJsonFile = new File(TEST_RESOURCES_DIR, "issue/issue_filterPathKey.json").toPath();
            GenericRecommendation rec = getRecommendation(issueJsonFile);
            List<Target> targets = rec.getTargets();
            assertEquals(1, targets.size());

            Target target = targets.get(0);
            List<Issue> issues = rec.getIssues(target, au);
            assertEquals(1, issues.size());

            Issue issue = issues.get(0);

            // Check occurrences
            Occurrence occurrence = issue.getOccurrence();
            Map<String, String> expectedFieldKeys = new HashMap<>();
            expectedFieldKeys.put("channel", "Channel");
            expectedFieldKeys.put("channelType", "Channel Type");
            expectedFieldKeys.put("channelTypeName", "Channel Type Name");
            assertEquals(expectedFieldKeys, occurrence.getFieldKeys());
            Map<String, String> expectedOcInstance1 = new HashMap<>();
            expectedOcInstance1.put("channel", "INVENTORY.LONDON");
            expectedOcInstance1.put("channelType", "sendch");
            expectedOcInstance1.put("channelTypeName", "Inventory");
            Map<String, String> expectedOcInstance2 = new HashMap<>();
            expectedOcInstance2.put("channel", "SYSTEM.AUTO.SVRCONN");
            expectedOcInstance2.put("channelType", "rcvch");
            expectedOcInstance2.put("channelTypeName", "Svr Conn");
            Map<String, String> expectedOcInstance3 = new HashMap<>();
            expectedOcInstance3.put("channel", "SYSTEM.DEF.CLUSSDR");
            expectedOcInstance3.put("channelType", "sendch");
            expectedOcInstance3.put("channelTypeName", "Cluster DR");
            Map<String, String> expectedOcInstance4 = new HashMap<>();
            expectedOcInstance4.put("channel", "SYSTEM.DEF.SVRCONN");
            expectedOcInstance4.put("channelType", "rcvch");
            expectedOcInstance4.put("channelTypeName", "Svr Conn 2");
            assertEquals(Arrays.asList(expectedOcInstance1, expectedOcInstance2, expectedOcInstance3, expectedOcInstance4),
                    occurrence.getOccurrencesInstances());
        } catch (IOException | TAException e) {
            throw new AssertionFailedError("Error generating recommendations", e);
        }
    }


    /*
     * Test query multiple config files using queryInputFile attribute
     */
    @Test
    public void queryMultipleInputFileTest() {
        try {
            Path assessmentUnitFile = new File(TEST_RESOURCES_DIR, "collect/AssessmentUnit.json").toPath();
            Path configFile1 = new File(TEST_RESOURCES_DIR,
                    "configFiles" + File.separator + "configFile.json").toPath();
            Path configFile2 = new File(TEST_RESOURCES_DIR,
                    "configFiles" + File.separator + "configFile2.json").toPath();
            GenericAssessmentUnit au = new GenericAssessmentUnit(assessmentUnitFile, Arrays.asList(configFile1, configFile2));

            Path issueJsonFile = new File(TEST_RESOURCES_DIR, "issue/issue_queryMultipleInputFile.json").toPath();
            GenericRecommendation rec = getRecommendation(issueJsonFile);
            List<Target> targets = rec.getTargets();
            assertEquals(1, targets.size());

            Target target = targets.get(0);
            List<Issue> issues = rec.getIssues(target, au);
            assertEquals(1, issues.size());

            Issue issue = issues.get(0);

            // Check occurrences
            Occurrence occurrence = issue.getOccurrence();
            Map<String, String> expectedFieldKeys = new HashMap<>();
            expectedFieldKeys.put("cluster", "Cluster");
            assertEquals(expectedFieldKeys, occurrence.getFieldKeys());
            Map<String, String> expectedOcInstance = new HashMap<>();
            expectedOcInstance.put("cluster", "CORK");
            Map<String, String> expectedOcInstance2 = new HashMap<>();
            expectedOcInstance2.put("cluster", "TORONTO");
            assertEquals(Arrays.asList(expectedOcInstance, expectedOcInstance2), occurrence.getOccurrencesInstances());
        } catch (IOException | TAException e) {
            throw new AssertionFailedError("Error generating recommendations", e);
        }
    }

    /*
     * Test query a single config files using queryInputFile attribute
     */
    @Test
    public void querySingleInputFileTest() {
        try {
            Path assessmentUnitFile = new File(TEST_RESOURCES_DIR, "collect/AssessmentUnit.json").toPath();
            Path configFile1 = new File(TEST_RESOURCES_DIR,
                    "configFiles" + File.separator + "configFile.json").toPath();
            Path configFile2 = new File(TEST_RESOURCES_DIR,
                    "configFiles" + File.separator + "configFile2.json").toPath();
            GenericAssessmentUnit au = new GenericAssessmentUnit(assessmentUnitFile, Arrays.asList(configFile1, configFile2));

            Path issueJsonFile = new File(TEST_RESOURCES_DIR, "issue/issue_querySingleInputFile.json").toPath();
            GenericRecommendation rec = getRecommendation(issueJsonFile);
            List<Target> targets = rec.getTargets();
            assertEquals(1, targets.size());

            Target target = targets.get(0);
            List<Issue> issues = rec.getIssues(target, au);
            assertEquals(1, issues.size());

            Issue issue = issues.get(0);

            // Check occurrences
            Occurrence occurrence = issue.getOccurrence();
            Map<String, String> expectedFieldKeys = new HashMap<>();
            expectedFieldKeys.put("cluster", "Cluster");
            assertEquals(expectedFieldKeys, occurrence.getFieldKeys());
            Map<String, String> expectedOcInstance = new HashMap<>();
            expectedOcInstance.put("cluster", "TORONTO");
            assertEquals(Arrays.asList(expectedOcInstance), occurrence.getOccurrencesInstances());
        } catch (IOException | TAException e) {
            throw new AssertionFailedError("Error generating recommendations", e);
        }
    }

    private GenericRecommendation getRecommendation(Path issueJsonFile) throws IOException, TAException {
        Path complexityJsonFile = new File(TEST_RESOURCES_DIR, "assess" + File.separator + FILE_COMPLEXITIES_JSON).toPath();
        Path issueCatJsonFile = new File(TEST_RESOURCES_DIR, "assess" + File.separator + FILE_ISSUECATS_JSON).toPath();
        Path targetJsonFile = new File(TEST_RESOURCES_DIR, "target" + File.separator + "targets_one.json").toPath();

        return new GenericRecommendation("assessment1", issueJsonFile,
                issueCatJsonFile, complexityJsonFile, targetJsonFile);
    }

}
