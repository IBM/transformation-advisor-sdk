/*
 * (C) Copyright IBM Corp. 2019,2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.ta.sdk.spi.test;

import com.google.gson.*;
import com.ibm.ta.sdk.spi.collect.EnvironmentJson;
import com.ibm.ta.sdk.spi.validation.TaJsonFileValidator;
import org.opentest4j.AssertionFailedError;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static com.ibm.ta.sdk.spi.test.TestUtils.*;

public class ValidationUtils {

    public static void assertCollectionUnit(String collectionUnitName) {
        // directory is created in output for the collection unit
        File cuDir = new File(TEST_OUTPUT_DIR, collectionUnitName);
        assertTrue(cuDir.exists());
    }

    public static void assertEnvironmentJson(String domain, String middlewareName, String middlewareVersion,
                                             String operatingSystem, String hostName, String middlewareInstallPath,
                                             String middlewareDataPath, JsonElement middlewareMetadata, String collectionUnitName,
                                             String collectionUnitType, JsonElement assessmentMetadata,
                                             List<String> assessmentUnits, String pluginVersion) {
        File envFile = new File(TEST_OUTPUT_DIR, collectionUnitName + File.separator + ENVIRONMENT_JSON);
        assertTrue(envFile.exists());

        // Validate schema
        assertDoesNotThrow( () -> TaJsonFileValidator.validateEnvironment(envFile.getPath()));

        // Add assessment units to expected environment
        EnvironmentJson expectedEnvJson = new EnvironmentJson(domain, middlewareName, middlewareVersion);
        expectedEnvJson.setOperatingSystem(operatingSystem);
        expectedEnvJson.setHostName(hostName);
        expectedEnvJson.setMiddlewareInstallPath(middlewareInstallPath);
        expectedEnvJson.setMiddlewareDataPath(middlewareDataPath);
        expectedEnvJson.setMiddlewareMetadata(middlewareMetadata);
        expectedEnvJson.setCollectionUnitName(collectionUnitName);
        expectedEnvJson.setCollectionUnitType(collectionUnitType);
        expectedEnvJson.setAssessmentMetadata(assessmentMetadata);
        expectedEnvJson.setAssessmentUnits(assessmentUnits);
        expectedEnvJson.setPluginVersion(pluginVersion);
        JsonElement expectedJson = getJsonTree(expectedEnvJson).getAsJsonObject();

        // Read env.json and verify attributes
        AtomicReference<JsonObject> envJsonAr = new AtomicReference<>();
        assertDoesNotThrow( () -> envJsonAr.set(TestUtils.getJson(envFile.toPath()).getAsJsonObject()));
        JsonObject envJson = envJsonAr.get();
        assertEquals(expectedJson, envJson);
    }

    public static void assertAssessmemntUnits(String collectionUnitName, List<String> expectedAssessmentUnits) {
        // Check assessment units in environment
        File envFile = new File(TEST_OUTPUT_DIR, collectionUnitName + File.separator + ENVIRONMENT_JSON);
        AtomicReference<JsonObject> envJsonAr = new AtomicReference<>();
        assertDoesNotThrow( () -> envJsonAr.set(TestUtils.getJson(envFile.toPath()).getAsJsonObject()));
        JsonArray auJson = envJsonAr.get().get("assessmentUnits").getAsJsonArray();
        JsonArray expectedAuJson = getJsonTree(expectedAssessmentUnits).getAsJsonArray();
        assertEquals(expectedAuJson, auJson);

        // Check directories in output
        File assessmentRootDir = new File(TEST_OUTPUT_DIR, collectionUnitName);
        for (String au : expectedAssessmentUnits) {
            // Assessment unit dir
            File auDir = new File(assessmentRootDir, au);
            assertTrue(auDir.exists());

            // assessmentUnit file and asessmentUnit data file
            assertTrue(new File(auDir, au + ".json").exists());
            assertTrue(new File(auDir, au + ".assessmentUnit.json").exists());
        }
    }

    public static void assertAssessmentUnitFiles(String collectionUnitName, String assessmentUnitName,
                                                 List<String> expectedAuConfigFiles) {

        // Check content of assessment unit files

        // Check for config files in the output
        File auDir = new File(TEST_OUTPUT_DIR, collectionUnitName + File.separator + assessmentUnitName);
        assertEquals(2 + expectedAuConfigFiles.size(), auDir.list().length);
        assertTrue(new File(auDir, assessmentUnitName + ".json").exists());
        assertTrue(new File(auDir, assessmentUnitName + ".assessmentUnit.json").exists());

        for (String configFileName : expectedAuConfigFiles) {
            File configFile = new File(auDir, configFileName);
            assertTrue(configFile.exists());
        }
    }

    public static void assertRecommendationsJson(String collectionUnitName, Path expectedRecommendationsJsonFile) {
        File recommendationsFile = new File(TEST_OUTPUT_DIR, collectionUnitName + File.separator + RECOMMENDATIONS_JSON);
        assertTrue(recommendationsFile.exists());

        // Validate schema
        assertDoesNotThrow( () -> TaJsonFileValidator.validateRecommendation(recommendationsFile.getPath()));

        // Assert recommendations file values
        try {
            JsonElement recJson = getJson(recommendationsFile.toPath());
            JsonElement expectedRecJson = getJson(expectedRecommendationsJsonFile);

            assertEquals(expectedRecJson, recJson);
        } catch (IOException e) {
            throw new AssertionFailedError("Error reading recommendations json:" + recommendationsFile.getAbsolutePath(), e);
        }
    }
}
