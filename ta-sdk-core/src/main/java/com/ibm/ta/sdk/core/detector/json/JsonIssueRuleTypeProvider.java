/*
 * (C) Copyright IBM Corp. 2019,2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.ta.sdk.core.detector.json;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.ibm.ta.sdk.spi.collect.AssessmentUnit;
import com.ibm.ta.sdk.spi.plugin.TARuntimeException;
import com.ibm.ta.sdk.spi.recommendation.Target;
import com.ibm.ta.sdk.core.util.GenericUtil;
import com.ibm.ta.sdk.core.assessment.GenericIssue;
import com.ibm.ta.sdk.core.assessment.IssueMatchCriteria;
import com.ibm.ta.sdk.core.assessment.IssueRuleProcessor;
import com.ibm.ta.sdk.core.assessment.IssueRule;
import com.ibm.ta.sdk.core.detector.IssueRuleTypeProvider;
import com.jayway.jsonpath.*;
import net.minidev.json.JSONArray;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.jayway.jsonpath.JsonPath.using;

public class JsonIssueRuleTypeProvider implements IssueRuleTypeProvider {
  private static final String JSON_RULE_PROVIDER_NAME = "json";
  private static Logger logger = LogManager.getLogger(JsonIssueRuleTypeProvider.class.getName());

  public static final String QUERYPATHS_KEYNAME = "jsonQueryPath";
  public static final String PATHVAR_NOT_RESOLVE = "@nr.";
  public static final String PATHVAR_PARENT = "@parent.";
  public static final String PATHVAR_FILTER_KEY = "@filterPathKey";
  public static final String PATHVAR_RESOLVED_FILTER_PATH = "@resolvedFilterPath";
  public static final String PATHVAR_FILTER_KEY_VALUE = "@filterPathKeyValue";
  public static final String OCCURRENCE_PATH_ATTR = "path";


  @Override
  public String getName() {
    return JSON_RULE_PROVIDER_NAME;
  }

  @Override
  public IssueMatchCriteria getIssueMatchCriteria(JsonObject matchCriteriaJson) {
    return new IssueMatchCriteria(matchCriteriaJson, QUERYPATHS_KEYNAME);
  }

  @Override
  public GenericIssue getIssue(Target target, AssessmentUnit assessmentUnit, IssueRule issueRule) {
    GenericIssue issue = new GenericIssue(issueRule);
    List<String> queryInputJsonList = new ArrayList<>();

    // Get query input files and convert to json
    Map<String, String> queryInputFilesMap = issueRule.getMatchCriteria().getQueryInputFiles();
    if (!queryInputFilesMap.isEmpty()) {
      List<Path> matchingConfigFiles = GenericUtil.getMatchingAssessmentUnitConfigFiles(assessmentUnit, queryInputFilesMap);
      for (Path configFile : matchingConfigFiles) {
        try {
          logger.debug("Reading config file:" + configFile);
          String configFileJsonStr = new String(Files.readAllBytes(configFile));
          queryInputJsonList.add(configFileJsonStr);
        } catch (IOException e) {
          throw new TARuntimeException(e);
        }
      }
    } else {
      JsonObject assessUnitDataJson = assessmentUnit.getAssessmentData();
      queryInputJsonList.add(assessUnitDataJson.toString());
    }

    for (String querytInputJsonStr : queryInputJsonList) {
      Map<String, List<String>> pathListMap = new LinkedHashMap<>();
      Map<String, JsonElement> issueQueryPaths = issueRule.getMatchCriteria().getQueryPaths();
      for (String pathKey : issueQueryPaths.keySet()) {
        String pathValue = issueQueryPaths.get(pathKey).getAsString();

        List<String> pathList = null;

        //logger.info("queryInput:" + querytInputJsonStr);
        //logger.info("path:" + pathValue);

        Configuration conf = Configuration.builder()
                .options(Option.AS_PATH_LIST).build();
        try {
          pathList = using(conf).parse(querytInputJsonStr).read(pathValue);
        } catch (PathNotFoundException e) {
          logger.error("No issues found in path:" + pathValue);
        }

        if (pathList != null && !pathList.isEmpty()) {
          pathListMap.put(pathKey, pathList);
        }
      }

      for (String pathKey : pathListMap.keySet()) {
        List<String> pathList = pathListMap.get(pathKey);

        for (String path : pathList) {
          logger.trace("recommendation path:" + path);

          DocumentContext doc = JsonPath.parse(querytInputJsonStr);
          if (!issueRule.customFilter(doc, path)) {
            continue;
          }
          List<Map<String, String>> occurrences = getOccurrence(doc, pathKey, path, issueRule.getMatchCriteria().getOccurrenceAttr());
          logger.trace("occurrence:" + occurrences);
          issue.addOccurences(occurrences);
        }
      }
    }

    return issue;
  }


  protected List<Map<String, String>> getOccurrence(DocumentContext doc, String filterPathKey, String filterPath, JsonObject occurrenceAttr) {
    List<Map<String, String>> ocMapList = new ArrayList<>();
    ocMapList.add(new LinkedHashMap<>());

    if (occurrenceAttr != null) {
      for (String attrKey : occurrenceAttr.keySet()) {
        String pathKey = occurrenceAttr.getAsJsonObject(attrKey).get(OCCURRENCE_PATH_ATTR).getAsString();
        List<String> pathValues = new ArrayList<>();
        try {
          if (pathKey.startsWith(PATHVAR_NOT_RESOLVE)) {
            pathValues.add(pathKey.substring(4));
          } else if (pathKey.equals(PATHVAR_FILTER_KEY)) {
            pathValues.add(filterPathKey);
          } else if (pathKey.equals(PATHVAR_RESOLVED_FILTER_PATH)) {
            pathValues.add(filterPath.replaceAll("'", "").replace("$", ""));
          } else if (pathKey.startsWith(PATHVAR_PARENT)) {
            pathKey = pathKey.substring(8);
            String parentPath = getParentPath(filterPath);
            pathValues.add(doc.read(parentPath + "['" + pathKey + "']"));
          } else {
            if (pathKey.startsWith(PATHVAR_FILTER_KEY_VALUE)) {
              pathKey = filterPathKey;
            }

            if (filterPath.contains("\\")) {
              filterPath = filterPath.replace("\\", "\\\\");
            }
            Object pathObj =  doc.read(filterPath + "['" + pathKey + "']");
            if (pathObj instanceof JSONArray) {
              String[] strValues = new String[((JSONArray) pathObj).size()];
              strValues = ((JSONArray) pathObj).toArray(strValues);
              pathValues.addAll(Arrays.asList(strValues));
            } else {
              pathValues.add(pathObj.toString());
            }
          }
        } catch (PathNotFoundException e) {
          logger.error("Attribute not found in JSON, filterPath:" + filterPath + " key:" + pathKey);
        }

        // Clone ocMap for each value in pathValues
        if (pathValues.size() > 1) {
          List<Map<String, String>> newOcMapList = new ArrayList<>();
          for (int i=1; i<pathValues.size(); i++) {
            for (Map<String, String> ocMap : ocMapList) {
              newOcMapList.add((HashMap)((HashMap) ocMap).clone());
            }
          }

          if (newOcMapList.size() > 0) {
            ocMapList.addAll(newOcMapList);
          }
        }

        for (int i=0; i < pathValues.size(); i++) {
          for (int m=0; m < ocMapList.size(); m++) {
            if (m % pathValues.size() == i) {
              String pathValue = pathValues.get(i);
              Map<String, String> ocMap = ocMapList.get(m);
              ocMap.put(attrKey, pathValue);
            }
          }
        }
      }
    }

    return ocMapList;
  }

  // Input path: $['clusters'][0]['channels'][0]
  // Tokenizes it into List of '[]'
  protected static List<String> parsePath(String path) {
    Pattern pt = Pattern.compile("\\[.*?\\]");
    Matcher m = pt.matcher(path);

    List<String> pathList = new ArrayList<String>();
    while (m.find()) {
      pathList.add(m.group(0));
    }
    return pathList;
  }

  // Given a path, returns the parent, takes arrays into consideration
  // Input: $['clusters'][0]['channels'][0]
  // Output: $['clusters'][0]
  protected static String getParentPath(String path) {
    List<String> pathList = parsePath(path);
    String lastPath = pathList.remove(pathList.size() -1);
    Pattern pt = Pattern.compile("\\[\'.*?\'\\]");
    Matcher m = pt.matcher(lastPath);
    while (!m.matches()) {
      lastPath = pathList.remove(pathList.size() -1);
      m = pt.matcher(lastPath);
    }

    String result = "$";
    for (String p : pathList) {
      result += p;
    }
    return result;
  }


}
