/*
 * (C) Copyright IBM Corp. 2019,2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.ta.sdk.core.assessment;

import com.ibm.ta.sdk.core.collect.GenericAssessmentUnit;
import com.ibm.ta.sdk.spi.plugin.TAException;
import com.ibm.ta.sdk.spi.recommendation.Issue;
import com.ibm.ta.sdk.spi.recommendation.Target;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static com.ibm.ta.sdk.core.util.Constants.*;
import static org.junit.jupiter.api.Assertions.*;

public class TargetTest {
    public static final String TEST_RESOURCES_DIR = "src" + File.separator + "test" + File.separator + "resources";

    /*
     * Test with targets.json containing only issues and no issue category
     */
    @Test
    public void issueNoCategoryTest() {
        try {
            Path targetJsonFile = new File(TEST_RESOURCES_DIR, "target/targets_one.json").toPath();
            List<Issue> issues = getIssue(targetJsonFile);
            assertEquals(1, issues.size());
            assertEquals("MQCL03", issues.get(0).getId());
        } catch (IOException | TAException e) {
            throw new AssertionFailedError("Error generating recommendations", e);
        }
    }

    /*
     * Test with targets.json containing two targets
     */
    @Test
    public void multipleTargetTest() {
        try {
            Path targetJsonFile = new File(TEST_RESOURCES_DIR, "target/targets_two.json").toPath();
            GenericRecommendation rec = getRecommendation(targetJsonFile);
            List<Target> targets = rec.getTargets();
            assertEquals(2, targets.size());

            Target target1 = targets.get(0);
            assertEquals("OPEN_LIBERTY", target1.getTargetId());

            Target target2 = targets.get(1);
            assertEquals("WAS_LIBERTY", target2.getTargetId());
        } catch (IOException | TAException e) {
            throw new AssertionFailedError("Error generating recommendations", e);
        }
    }


    /*
     * Target with no issue and no issue category
     */
    @Test
    public void  noIssueTest() {
        try {
            // No issue and no issue category
            Path targetJsonFile = new File(TEST_RESOURCES_DIR, "target/targets_no_issue_no_cat.json").toPath();
            List<Issue> issues = getIssue(targetJsonFile);
            assertEquals(0, issues.size());
        } catch (IOException | TAException e) {
            throw new AssertionFailedError("Error generating recommendations", e);
        }
    }

    /*
     * Test for targets.json containing a single matching issue category
     */
    @Test
    public void targetIssueCategoryTest() {
        try {
            // Category for issue found
            Path targetJsonFile = new File(TEST_RESOURCES_DIR, "target/targets_cat_cluster.json").toPath();
            List<Issue> issues = getIssue(targetJsonFile);
            assertEquals(1, issues.size());
            assertEquals("MQCL03", issues.get(0).getId());
        } catch (IOException | TAException e) {
            throw new AssertionFailedError("Error generating recommendations", e);
        }
    }

    /*
     * Test for targets.json containing no matching issue category
     */
    @Test
    public void targetIssueCategoryNotMatchTest() {
        try {
            // Category for issue not found
            Path targetJsonFile = new File(TEST_RESOURCES_DIR, "target/targets_cat_exit.json").toPath();
            List<Issue> issues = getIssue(targetJsonFile);
            assertEquals(0, issues.size());
        } catch (IOException | TAException e) {
            throw new AssertionFailedError("Error generating recommendations", e);
        }
    }

    /*
     * Test for targets.json containing more than one issue categories
     */
    @Test
    public void targetIssueCategoriesTest() {
        try {
            // Category for issue found
            Path targetJsonFile = new File(TEST_RESOURCES_DIR, "target/targets_cat_cluster_exit.json").toPath();
            List<Issue> issues = getIssue(targetJsonFile);
            assertEquals(1, issues.size());
            assertEquals("MQCL03", issues.get(0).getId());
        } catch (IOException | TAException e) {
            throw new AssertionFailedError("Error generating recommendations", e);
        }
    }

    /*
     * Test for targets.json containing an overlap of the same issue in issues and issueCategories
     */
    @Test
    public void targetOverlapIssueAndCatTest() {
        try {
            // Category for issue found
            Path targetJsonFile = new File(TEST_RESOURCES_DIR, "target/targets_issue_cat_overlap.json").toPath();
            List<Issue> issues = getIssue(targetJsonFile);
            assertEquals(1, issues.size());
            assertEquals("MQCL03", issues.get(0).getId());
        } catch (IOException | TAException e) {
            throw new AssertionFailedError("Error generating recommendations", e);
        }
    }

    /*
     * Test for targets.json containing no targets
     */
    @Test
    public void nullTargetTest() {
        Path targetJsonFile = new File(TEST_RESOURCES_DIR, "target/targets_null.json").toPath();

        Exception e = assertThrows(TAException.class, () -> {
            getRecommendation(targetJsonFile);
        });
        assertTrue(e.getMessage().contains("Anomalies found."));
    }


    /*
     * Test for targets.json missing required attributes
     */
    // @Test
    public void missingAttrTargetTest() {
        Path targetJsonFile = new File(TEST_RESOURCES_DIR, "target/targets_missing_attr.json").toPath();

        Exception e = assertThrows(TAException.class, () -> {
            getRecommendation(targetJsonFile);
        });
        assertTrue(e.getMessage().contains("Anomalies found."));
    }

    private GenericRecommendation getRecommendation(Path targetJsonFile) throws IOException, TAException {
        Path complexityJsonFile = new File(TEST_RESOURCES_DIR, "assess" + File.separator + FILE_COMPLEXITIES_JSON).toPath();
        Path issueCatJsonFile = new File(TEST_RESOURCES_DIR, "assess" + File.separator + FILE_ISSUECATS_JSON).toPath();
        Path issueJsonFile = new File(TEST_RESOURCES_DIR, "issue" + File.separator + "issue_attr_filter.json").toPath();

        return new GenericRecommendation("assessment1", issueJsonFile,
                    issueCatJsonFile, complexityJsonFile, targetJsonFile);
    }

    private List<Issue> getIssue(Path targetJsonFile) throws TAException, IOException {
        GenericRecommendation rec = getRecommendation(targetJsonFile);
        List<Target> targets = rec.getTargets();
        assertEquals(1, targets.size());

        Target target = targets.get(0);
        Path assessmentUnitFile = new File(TEST_RESOURCES_DIR, "collect/AssessmentUnit.json").toPath();
        GenericAssessmentUnit au = new GenericAssessmentUnit(assessmentUnitFile, null);
        return rec.getIssues(target, au);
    }

}
