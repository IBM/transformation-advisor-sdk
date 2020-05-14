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

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
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

  /**
   * List directory contents for a resource folder. Not recursive.
   * This is basically a brute-force implementation.
   * Works for regular files and also JARs.
   *
   * @param clazz Any java class that lives in the same place as the resources you want.
   * @param path Should end with "/", but not start with one.
   * @return Just the name of each member item, not the full paths.
   * @throws URISyntaxException
   * @throws IOException
   */
  public static String[] getResourceListing(Class clazz, String path) throws URISyntaxException, IOException {
    URL dirURL = clazz.getClassLoader().getResource(path);
    if (dirURL != null && dirURL.getProtocol().equals("file")) {
      /* A file path: easy enough */
      return new File(dirURL.toURI()).list();
    }

    if (dirURL == null) {
      /*
       * In case of a jar file, we can't actually find a directory.
       * Have to assume the same jar as clazz.
       */
      String me = clazz.getName().replace(".", "/")+".class";
      dirURL = clazz.getClassLoader().getResource(me);
    }

    if (dirURL.getProtocol().equals("jar")) {
      /* A JAR path */
      String jarPath = dirURL.getPath().substring(5, dirURL.getPath().indexOf("!")); //strip out only the JAR file
      JarFile jar = new JarFile(URLDecoder.decode(jarPath, "UTF-8"));
      Enumeration<JarEntry> entries = jar.entries(); //gives ALL entries in jar
      Set<String> result = new HashSet<String>(); //avoid duplicates in case it is a subdirectory
      while(entries.hasMoreElements()) {
        String name = entries.nextElement().getName();
        if (name.startsWith(path)) { //filter according to the path
          String entry = name.substring(path.length());
          int checkSubdir = entry.indexOf("/");
          if (checkSubdir >= 0) {
            // if it is a subdirectory, we just return the directory name
            entry = entry.substring(0, checkSubdir);
          }
          if (entry.length()>0) {
            result.add(entry);
          }
        }
      }
      return result.toArray(new String[result.size()]);
    }

    throw new UnsupportedOperationException("Cannot list files for URL "+dirURL);
  }
}
