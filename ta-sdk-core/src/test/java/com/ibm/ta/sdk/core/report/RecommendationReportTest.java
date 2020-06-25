/*
 * (C) Copyright IBM Corp. 2019,2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.ta.sdk.core.report;

import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import com.ibm.ta.sdk.core.assessment.GenericRecommendation;
import com.ibm.ta.sdk.core.collect.GenericAssessmentUnit;
import com.ibm.ta.sdk.core.detector.JsonDetectorTest;
import com.ibm.ta.sdk.core.util.GenericUtil;
import com.ibm.ta.sdk.spi.assess.ComplexityContributionJson;
import com.ibm.ta.sdk.spi.assess.RecommendationJson;
import com.ibm.ta.sdk.spi.plugin.TAException;
import com.ibm.ta.sdk.spi.recommendation.Issue;
import com.ibm.ta.sdk.spi.recommendation.Occurrence;
import com.ibm.ta.sdk.spi.recommendation.Severity;
import com.ibm.ta.sdk.spi.recommendation.Target;
import com.ibm.ta.sdk.spi.report.Report;
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
import static org.junit.jupiter.api.Assertions.assertEquals;

public class RecommendationReportTest {
    /*
     * Test recommendations report
     */
    @Test
    public void reportTest() {
        try {
            Path recommendationsJsonFile = new File(JsonDetectorTest.TEST_RESOURCES_DIR, "assess" + File.separator + "recommendations.json").toPath();
            JsonElement recJson = GenericUtil.getJson(recommendationsJsonFile);

            RecommendationReporter recReporter = new RecommendationReporter("London", recJson.getAsJsonObject());
            List<Report> reports = recReporter.generateHTMLReports();
        } catch (IOException | TAException e) {
            throw new AssertionFailedError("Error generating report", e);
        }
    }
}
