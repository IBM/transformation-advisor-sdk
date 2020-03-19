/*
 * (C) Copyright IBM Corp. 2019,2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.ta.sdk.spi.validation;

import com.ibm.ta.sdk.spi.plugin.TAException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.leadpony.justify.api.*;

import javax.json.JsonReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class TaJsonFileValidator {

    private static Logger logger = LogManager.getLogger(TaJsonFileValidator.class.getName());

    private static final String ISSUE_SCHEMA = "schema/issue.schema.json";
    private static final String TARGET_SCHEMA = "schema/target.schema.json";
    private static final String COMPLEXITY_SCHEMA = "schema/complexity.schema.json";
    private static final String ENVIRONMENT_SCHEMA = "schema/environment.schema.json";
    private static final String RECOMMENDATION_SCHEMA = "schema/recommendation.schema.json";


    // The only instance of JSON validation service.
    private static JsonValidationService service = JsonValidationService.newInstance();

    // The configured factory which will produce schema readers.
    private static JsonSchemaReaderFactory readerFactory;

    /**
     * Reads the JSON schema from the specified path.
     *
     * @param inputStream the path to the schema.
     * @return the read schema.
     */

    private static JsonSchema readSchema(InputStream inputStream) {
        try (JsonSchemaReader reader = readerFactory.createSchemaReader(inputStream)) {
            return reader.read();
        }
    }

    /**
     * Resolves the referenced JSON schema.
     *
     * @param id the identifier of the referenced JSON schema.
     * @return referenced JSON schema.
     */

    private static JsonSchema resolveSchema(URI id) {
        //System.out.println(id.getPath());
        // The schema is available in the local filesystem.
        InputStream schemaResources = TaJsonFileValidator.class.getClassLoader().getResourceAsStream(id.getPath());
        assert schemaResources != null;
        return readSchema(schemaResources);
    }

    private static ProblemHandler getProblemHandler(Consumer<String> handler) {
        return service.createProblemPrinter(handler);
    }


    /**
     * Run a test to a json file at jsonResourcePath against a schema at schemaPath
     *
     * @param schemaPath The path to the JSON schema file in resource.
     * @param jsonResourcePath The path to the JSON file to be validated in resource.
     */
    private static boolean validateJsonBySchema(String schemaPath, String jsonResourcePath) {
        boolean isValid = true;

        try {
            InputStream jrIs = getResource(jsonResourcePath);
            isValid = validateJsonBySchema(schemaPath, jrIs);
        } catch (FileNotFoundException e) {
            logger.error("We have problem to process the json files.");
            logger.error(e);
            isValid = false;
        }

        return isValid;
    }

    /**
     * Run a test to a json file at jsonResourcePath against a schema at schemaPath
     *
     * @param schemaPath The path to the JSON schema file in resource.
     * @param jrIs InputStream for JSON file to be validated in resource.
     */
    private static boolean validateJsonBySchema(String schemaPath, InputStream jrIs) {
        boolean isValid = true;

        List<String> problemList = new ArrayList<>();
        // Builds a factory of schema readers.
        readerFactory = service.createSchemaReaderFactoryBuilder()
                .withSchemaResolver(TaJsonFileValidator::resolveSchema)
                .build();

        try {
            //System.out.println("zzzzz = "+schemaPath);
            InputStream schemaResources = getResource(schemaPath);
            assert schemaResources != null;
            JsonSchema schema = readSchema(schemaResources);
            // Problem handler
            ProblemHandler handler = getProblemHandler(problemList::add);
            if (jrIs != null) {
                JsonReader reader = service.createReader(jrIs, schema, handler);
                reader.readValue();

                if (problemList.size() > 0) {
                    isValid = false;
                    logger.info("For resource " + jrIs + ", ");
                    logger.info("We have found problems below:");
                    problemList.forEach(System.out::println);
                }
            } else {
                logger.error("Cannot find file " + jrIs + " to validate");
            }
        } catch (Exception e) {
            logger.error("We have problem to process the json files.");
            logger.error(e);
            isValid = false;
        }

        return isValid;
    }

    public static void validateIssue(String issueJsonFile) throws TAException {
        if (!validateJsonBySchema(ISSUE_SCHEMA, issueJsonFile)) {
            throw new TAException("Anomalies found. The resource " + issueJsonFile + " is not a valid issue rule json file.");
        }
    }

    public static void validateTarget(String targetJsonFile) throws TAException {
        if (!validateJsonBySchema(TARGET_SCHEMA, targetJsonFile)) {
            throw new TAException("Anomalies found. The resource " + targetJsonFile + " is not a valid target json file.");
        }
    }

    public static void validateComplexity(String complexityJsonFile) throws TAException {
        if (!validateJsonBySchema(COMPLEXITY_SCHEMA, complexityJsonFile)) {
            throw new TAException("Anomalies found. The resource " + complexityJsonFile + " is not a valid complexity json file.");
        }
    }

    public static void validateEnvironment(String environmentJsonFile) throws TAException {
        if (!validateJsonBySchema(ENVIRONMENT_SCHEMA, environmentJsonFile)) {
            throw new TAException("Anomalies found. The resource " + environmentJsonFile + " is not a valid environment json file.");
        }
    }

    public static void validateEnvironment(InputStream envFileIs) throws TAException {
        if (!validateJsonBySchema(ENVIRONMENT_SCHEMA, envFileIs)) {
            throw new TAException("Anomalies found. The resource is not a valid environment json file.");
        }
    }

    public static void validateRecommendation(String recommendationJsonFile) throws TAException {
        if (!validateJsonBySchema(RECOMMENDATION_SCHEMA, recommendationJsonFile)) {
            throw new TAException("Anomalies found. The resource " + recommendationJsonFile + " is a valid recommendation json file.");
        }
    }

    public static void validateRecommendation(InputStream recFileIs) throws TAException {
        if (!validateJsonBySchema(RECOMMENDATION_SCHEMA, recFileIs)) {
            throw new TAException("Anomalies found. The resource is a valid recommendation json file.");
        }
    }

    private static InputStream getResource(String filePath) throws FileNotFoundException {
        File realFile = new File(filePath);
        if (realFile.exists()) {
            return new FileInputStream(realFile);
        } else {
            return TaJsonFileValidator.class.getClassLoader().getResourceAsStream(filePath);
        }
    }
}
