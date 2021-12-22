/*
 * (C) Copyright IBM Corp. 2019,2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.ta.sdk.spi.validation;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.ibm.ta.sdk.spi.assess.UTRecommendation;
import com.ibm.ta.sdk.spi.collect.EnvironmentJson;
import com.ibm.ta.sdk.spi.collect.UTAssessmentUnit;
import com.ibm.ta.sdk.spi.collect.UTDataCollection;
import com.ibm.ta.sdk.spi.plugin.CliInputCommand;
import com.ibm.ta.sdk.spi.plugin.CliInputOption;
import com.ibm.ta.sdk.spi.plugin.TAException;
import com.ibm.ta.sdk.spi.plugin.UTPluginProvider;
import com.ibm.ta.sdk.spi.test.TestUtils;
import org.tinylog.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Path;
import java.util.*;
import java.util.zip.ZipInputStream;

import static com.ibm.ta.sdk.spi.test.TestUtils.TEST_OUTPUT_DIR;
import static com.ibm.ta.sdk.spi.test.TestUtils.getJson;
import static com.ibm.ta.sdk.spi.test.ValidationUtils.*;
import static org.junit.jupiter.api.Assertions.*;

public class ValidationTest {

    @Test
    public void environmentJsonTest() {
        Exception e = assertThrows(TAException.class, () -> {
                    TaJsonFileValidator.validateEnvironment(
                            new File(TestUtils.TEST_RESOURCES_DIR, "environment.json").getAbsolutePath());
                });
        assertTrue(e.getMessage().contains("Anomalies found."));

        try {
            JsonElement envJson = getJson(new File(TestUtils.TEST_RESOURCES_DIR, "environment.json").toPath());
            envJson.getAsJsonObject().addProperty("pluginVersion", "1.0");
            envJson.getAsJsonObject().add("assessmentUnits", new JsonArray());
            TaJsonFileValidator.validateEnvironment(new ByteArrayInputStream(envJson.toString().getBytes()));
        } catch (Exception ex) {
            throw new AssertionFailedError( "Error validating environment.json", ex);
        }
    }

    @Test
    public void recommendationJsonTest() {
        assertDoesNotThrow( () -> TaJsonFileValidator.validateRecommendation(
                new File(TestUtils.TEST_RESOURCES_DIR, "assessmentUnits/London/recommendations.json").getAbsolutePath()));

        Exception e = assertThrows(TAException.class, () -> {
            JsonElement recJson = getJson(new File(TestUtils.TEST_RESOURCES_DIR, "assessmentUnits/London/recommendations.json").toPath());
            recJson.getAsJsonObject().remove("collectionUnitType");

            TaJsonFileValidator.validateRecommendation(new ByteArrayInputStream(recJson.toString().getBytes()));
        });
        assertTrue(e.getMessage().contains("Anomalies found."));

    }
}
