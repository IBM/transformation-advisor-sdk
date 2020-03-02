/*
 * (C) Copyright IBM Corp. 2019,2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.ta.sdk.core.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.ibm.ta.sdk.spi.collect.AssessmentUnit;
import com.ibm.ta.sdk.core.assessment.JavaClassTypeAdapterFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class GenericUtil {

  public static String readFileToString(Path path) throws IOException {
    List<String> lines = Files.readAllLines(path);
    String content = lines.stream()
            .collect(Collectors.joining(System.lineSeparator()));
    return content;
  }

  public static <T> T getJsonObj(TypeToken<T> typeToken, Path path) throws IOException {
    String jsonStr = readFileToString(path);
    return getJsonObj(typeToken, jsonStr);
  }

  public static <T> T getJsonObj(TypeToken<T> typeToken, String jsonStr) {
    Gson gson = getGson();
    return gson.fromJson(jsonStr, typeToken.getType());
  }

  public static <T> T getJsonObj(TypeToken<T> typeToken, JsonObject jsonObj) {
    Gson gson = getGson();
    return gson.fromJson(jsonObj, typeToken.getType());
  }

  private static Gson getGson() {
    GsonBuilder builder = new GsonBuilder();
    builder.registerTypeAdapterFactory(new JavaClassTypeAdapterFactory());
    Gson gson = builder.create();
    return gson;
  }

  /**
   * Finds matching config Files in assessment unit that matches the queryInputFiles in the 'MatchingCriteria' for an issue
   * @param au Assessment Unit containing the config files to be filtered
   * @param queryInputFilesMap Query input file names map from the 'MatchingCriteria' for an issue. Supports regex for the query input file names.
   * @return Unique list of config files in the assessment unit that matches the filtering criteria in the queryInputFilesMap
   */
  public static List<Path> getMatchingAssessmentUnitConfigFiles(AssessmentUnit au, Map<String, String> queryInputFilesMap) {
    List<Path> matchingAuConfigFiles = new ArrayList<>();
    if (!queryInputFilesMap.isEmpty()) {
      Collection<String> queryInputFileNamesList = queryInputFilesMap.values();

      if (au.getConfigFiles()!=null) {
        for (Path configFile : au.getConfigFiles()) {
          String configFilename = configFile.getFileName().toString();
          for (String queryInputFileName : queryInputFileNamesList) {
            if (configFilename.matches(queryInputFileName)) {
              // Found, continue with next config file
              matchingAuConfigFiles.add(configFile);
              break;
            }
          }
        }
      }
    }
    return matchingAuConfigFiles;
  }
}
