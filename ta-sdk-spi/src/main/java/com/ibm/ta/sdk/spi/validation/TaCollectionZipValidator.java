/*
 * (C) Copyright IBM Corp. 2019,2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.ta.sdk.spi.validation;

import com.google.gson.Gson;
import com.ibm.ta.sdk.spi.collect.EnvironmentJson;
import com.ibm.ta.sdk.spi.plugin.TADataCollector;
import com.ibm.ta.sdk.spi.plugin.TAException;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class TaCollectionZipValidator {
    private static final String FILE_ZIP = ".zip";
    private static final String TEMPLATES_DIR = "templates";

    /**
     * Validates a zip collection archive created by the SDK.
     *
     * The validation checks:
     * 1. The archive is a zip file
     * 2. Checks that environment.json exists and validate that the schema is correct
     * 3. Checks that recommendations.json exists and validate that the schema is correct
     * 4. Checks the folder structure for assessment units and it matches the assessment units in the environment.json
     *
     * @param zipFilePath Collection archive file to validate
     * @throws TAException TAException is thrown when the validation fails
     */
    public static void validateCollectionArchive(String zipFilePath) throws TAException{
        if (!zipFilePath.endsWith(FILE_ZIP)) {
            throw new TAException("Input collection archive file is not a zip file");
        }
        File collectionFile = new File(zipFilePath);
        if (!collectionFile.exists()) {
            throw new TAException("Input collection archive file does not exist");
        }
        try {
            ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(collectionFile));
            validateArchive(zipInputStream);
        } catch (IOException e) {
            throw new TAException(e);
        }
    }

    /**
     * Validates a zip collection archive created by the SDK.
     *
     * The validation checks:
     * 1. The archive is a zip file
     * 2. Checks that environment.json exists and validate that the schema is correct
     * 3. Checks that recommendations.json exists and validate that the schema is correct
     * 4. Checks the folder structure for assessment units and it matches the assessment units in the environment.json
     *
     * @param zipInputStream InputStream of the collection archive to validate
     * @throws TAException TAException is thrown when the validation fails
     * @throws IOException IOException is thrown if there is an error with the zipInputStream
     */
    public static void validateArchive(ZipInputStream zipInputStream) throws IOException, TAException {
        String recJsonStr = null;
        String envJsonStr = null;
        String targetsJsonStr = null;
        Map<String, String> auMetadataMap = new HashMap<>();
        Set<String> assessmentUnits = new LinkedHashSet<>(); // list of assessment unit names based on the root dir names
        List<String> templateFiles = new ArrayList();

        for (ZipEntry nextEntry; Objects.nonNull(nextEntry = zipInputStream.getNextEntry()); ) {
            String entryName = nextEntry.getName();

            if (entryName.endsWith(TADataCollector.ENVIRONMENT_JSON_FILE) && isValidLocation(entryName, new int[] {1})) {
                envJsonStr = getStringFromZipInputStream(zipInputStream);
            } else if (entryName.endsWith(TADataCollector.RECOMMENDATIONS_JSON_FILE) && isValidLocation(entryName, new int[] {1})) {
                recJsonStr = getStringFromZipInputStream(zipInputStream);
            } else if (entryName.endsWith(TADataCollector.ASSESSMENTUNIT_META_JSON_FILE) && isValidLocation(entryName, new int[] {2})) {
                String auMetadataStr = getStringFromZipInputStream(zipInputStream);
                String auDirName = getAssessmentUnitDir(entryName);
                auMetadataMap.put(auDirName, auMetadataStr);
            } else if (entryName.endsWith(TADataCollector.TARGETS_JSON_FILE) && isValidLocation(entryName, new int[] {2})) {
                targetsJsonStr = getStringFromZipInputStream(zipInputStream);
            } else if (entryName.contains(TEMPLATES_DIR)) {
                templateFiles.add(entryName);
            }

            // Get assessment unit dir names from files 3 level deep (installation1/assessunitunit1/file1)
            if (isValidLocation(entryName, new int[] {2})) {
                // root directory name is the name of the assessment unit
                String auDirName = getAssessmentUnitDir(entryName);
                if (auDirName != null) {
                    assessmentUnits.add(auDirName);
                }
            }
        }

        try {
            // Validate environment json file
            if (envJsonStr != null) {
                TaJsonFileValidator.validateEnvironment(new ByteArrayInputStream(envJsonStr.getBytes()));
            } else {
                throw new TAException("Anomaly found. Environment JSON not found.");
            }

            // Validate recommendation json file
            if (recJsonStr != null) {
                TaJsonFileValidator.validateRecommendation(new ByteArrayInputStream(recJsonStr.getBytes()));
            } else {
                throw new TAException("Anomaly found. Recommendation JSON not found.");
            }

            // Validate assessment units
            EnvironmentJson envJson = new Gson().fromJson(envJsonStr, EnvironmentJson.class);

            List<String> envAuNamesList = envJson.getAssessmentUnits();

            // remove the middleware name dir which contains the templates files for 0.6.1
            // middleware directory will copied to collection directory, but it is not an assessment unit
            assessmentUnits.remove(envJson.getMiddlewareName());

            if (assessmentUnits.size() != envAuNamesList.size()) {
                throw new TAException("Anomaly found. Number of asessment unit directories in archive does not match data in environment JSON.");
            } else {
                if (!assessmentUnits.containsAll(envAuNamesList)) {
                    throw new TAException("Anomaly found. Asessment unit directories in archive does not match environment JSON.");
                }
            }

            // Check assessment unit metadata json exists in each assessment unit directory
            // In future, may want to validation the schema of the file as well
            Set<String> auMetaKeys = auMetadataMap.keySet();
            boolean isValid = assessmentUnits.size() == auMetaKeys.size() && assessmentUnits.containsAll(auMetaKeys);
            if (!isValid) {
                throw new TAException("Anomaly found. Missing assessment unit metadata JSON file in assessment unit directory.");
            }

            // validate the targets.json file
            // validate all template files start with middleware name
            if(envJson.containsTemplateFiles()) {
                if (targetsJsonStr != null) {
                    TaJsonFileValidator.validateTarget(new ByteArrayInputStream(targetsJsonStr.getBytes()));
                } else {
                    throw new TAException("Anomaly found. targets.json file not found.");
                }
                if (templateFiles.size()==0) {
                    throw new TAException("Anomaly found. No plugin template files find.");
                }
                for (String templateFilePath: templateFiles) {
                    if (!templateFilePath.startsWith(envJson.getCollectionUnitName()+"/"+envJson.getMiddlewareName())){
                        throw new TAException("Anomaly found. Plugin template file is not under the middleware name directory.");
                    }
                }
            }
        } catch (TAException ex) {
            throw new TAException("Invalid collection archive.\n" + ex.getMessage(), ex);
        }
    }

    private static String getStringFromZipInputStream(ZipInputStream zipInputStream) throws IOException {
        int byteRead;
        int maxNumberByteRead = 2048;
        StringBuilder jsonSb = new StringBuilder();
        byte[] buffer = new byte[maxNumberByteRead];

        while ((byteRead = zipInputStream.read(buffer, 0, maxNumberByteRead)) >= 0) {
            jsonSb.append(new String(buffer, 0, byteRead));
        }
        return jsonSb.toString();
    }

    private static boolean isValidLocation(String entryName, int[] positions) {
        String[] filePathParts = entryName.split("/");
        if (positions.length == 0) {
            return false;
        }
        // the file at top level:
        return Arrays.stream(positions).anyMatch(position -> position + 1 == filePathParts.length);
    }

    private static String getAssessmentUnitDir(String entryName) {
        String[] filePathParts = entryName.split("/");
        if (filePathParts.length < 3 ) {
            return null;
        }
        return filePathParts[1];
    }
}
