/*
 * (C) Copyright IBM Corp. 2019,2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.ta.sdk.core.assessment;

import com.ibm.ta.sdk.spi.plugin.TAException;
import com.ibm.ta.sdk.spi.recommendation.Target;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static com.ibm.ta.sdk.core.util.Constants.*;
import static com.ibm.ta.sdk.core.util.Constants.FILE_TARGET_JSON;
import static org.junit.jupiter.api.Assertions.*;

public class TargetTest {
    public static final String TEST_RESOURCES_DIR = "src" + File.separator + "test" + File.separator + "resources";

    @Test
    public void validTargetTest() {
        try {
            Path targetJsonFile = new File(TEST_RESOURCES_DIR, "target/target.json").toPath();

            GenericRecommendation rec = getRecommendation(targetJsonFile);
            List<Target> targets = rec.getTargets();
            assertEquals(1, targets.size());

            Target target = targets.get(0);
            assertEquals("testProduct", target.getProductName());
            assertEquals("10.0.0", target.getProductVersion());
            assertEquals("testRuntime", target.getRuntime());
            assertEquals(Target.LocationType.Private, target.getLocation());
            assertEquals(Target.PlatformType.Docker, target.getPlatform());
        } catch (IOException | TAException e) {
            throw new AssertionFailedError("Error generating recommendations", e);
        }
    }

    @Test
    public void nullTargetTest() {
        Path targetJsonFile = new File(TEST_RESOURCES_DIR, "target/target_null.json").toPath();

        Exception e = assertThrows(TAException.class, () -> {
            getRecommendation(targetJsonFile);
        });
        assertTrue(e.getMessage().contains("Anomalies found."));
    }


    @Test
    public void missingAttrTargetTest() {
        Path targetJsonFile = new File(TEST_RESOURCES_DIR, "target/target_missing_attr.json").toPath();

        Exception e = assertThrows(TAException.class, () -> {
            getRecommendation(targetJsonFile);
        });
        assertTrue(e.getMessage().contains("Anomalies found."));
    }

    private GenericRecommendation getRecommendation(Path targetJsonFile) throws IOException, TAException {
        Path complexityJsonFile = new File(TEST_RESOURCES_DIR, "assess" + File.separator + FILE_COMPLEXITY_JSON).toPath();
        Path issueCatJsonFile = new File(TEST_RESOURCES_DIR, "assess" + File.separator + FILE_ISSUECAT_JSON).toPath();
        Path issueJsonFile = new File(TEST_RESOURCES_DIR, "issue" + File.separator + "issue_attr_filter.json").toPath();

        return new GenericRecommendation("assessment1", issueJsonFile,
                    issueCatJsonFile, complexityJsonFile, targetJsonFile);
    }
}
