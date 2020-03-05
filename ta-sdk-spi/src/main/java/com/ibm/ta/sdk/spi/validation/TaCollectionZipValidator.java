/*
 * (C) Copyright IBM Corp. 2019,2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.ta.sdk.spi.validation;

import com.google.gson.Gson;
import com.ibm.ta.sdk.spi.collect.EnvironmentJson;
import com.ibm.ta.sdk.spi.plugin.TADataCollector;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class TaCollectionZipValidator {
    private static final String FILE_ZIP = ".zip";

    public static boolean validateCollectionArchive(String zipFilePath) {
        if (!zipFilePath.endsWith(FILE_ZIP)) {
            System.err.println("Input collection archive file is not a zip file");
            return false;
        }
        File collectionFile = new File(zipFilePath);
        if (!collectionFile.exists()) {
            System.err.println("Input collection archive file does not exist");
            return false;
        }
        try {
            return validateArchive(collectionFile);
        } catch (IOException e) {
            System.err.println("Error validating collection archive:" + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private static boolean validateArchive(File collectionArchiveZip) throws IOException {
        ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(collectionArchiveZip));

        String recJsonStr = null;
        String envJsonStr = null;
        Map<String, String> auMetadataMap = new HashMap<>();
        Set<String> assessmentUnits = new LinkedHashSet<>(); // list of assessment unit names based on the root dir names

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

        boolean isValid = true;

        // Validate environment json file
        if (envJsonStr != null) {
            isValid = TaJsonFileValidator.validateEnvironment(new ByteArrayInputStream(envJsonStr.getBytes()));
            if (!isValid) {
                System.err.println("Anomaly found. Environment JSON schema validation failed.");
            }
        } else {
            System.err.println("Anomaly found. Environment JSON not found.");
            isValid = false;
        }

        // Validate recommendation json file
        if (isValid) {
            if (recJsonStr != null) {
                isValid = TaJsonFileValidator.validateRecommendation(new ByteArrayInputStream(recJsonStr.getBytes()));
                if (!isValid) {
                    System.err.println("Anomaly found. Recommendation JSON schema validation failed.");
                }
            } else {
                System.err.println("Anomaly found. Recommendation JSON not found.");
                isValid = false;
            }
        }

        // Validate assessment units
        if (isValid) {
            EnvironmentJson envJson = new Gson().fromJson(envJsonStr, EnvironmentJson.class);

            List<String> envAuNamesList = envJson.getAssessmentUnits();
            if (assessmentUnits.size() != envAuNamesList.size()) {
                System.err.println("Anomaly found. Number of asessment unit directories in archive does not match data in environment JSON.");
                isValid = false;
            } else {
                isValid = assessmentUnits.containsAll(envAuNamesList);
                if (!isValid) {
                    System.err.println("Anomaly found. Asessment unit directories in archive does not match environment JSON.");
                }
            }
        }

        // Check assessment unit metadata json exists in each assessment unit directory
        if (isValid) {
            // In future, may want to validation the schema of the file as well
            Set<String> auMetaKeys = auMetadataMap.keySet();
            isValid = assessmentUnits.size() == auMetaKeys.size() && assessmentUnits.containsAll(auMetaKeys);
            if (!isValid) {
                System.err.println("Anomaly found. Missing assessment unit metadata JSON file in assessment unit directory.");
            }
        }

        return isValid;
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
