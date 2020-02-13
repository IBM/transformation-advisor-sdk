/*
 * (C) Copyright IBM Corp. 2019,2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.ta.sdk.core.assessment;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;


public class IssueMatchCriteria {
  protected static String ATTR_RULETYPE = "ruleType";
  protected static String ATTR_FILTER_INPUT_FILES = "queryInputFile";
  protected static String ATTR_OCCURRENCE_ATTR = "occurrenceAttr";

  private JsonObject matchCriteriaJsonObj;
  private String provider;
  private Map<String, String> queryInputFiles = new HashMap<String, String>();
  private Map<String, JsonElement> queryPaths = new HashMap<String, JsonElement>();
  protected JsonObject occurenceAttrs;

  private static Logger logger = Logger.getLogger(IssueMatchCriteria.class.getName());

  public IssueMatchCriteria(JsonObject matchCriteriaJsonObj, String queryPathsKeyName) {
    this.matchCriteriaJsonObj = matchCriteriaJsonObj;

    if (matchCriteriaJsonObj == null) {
      logger.error("No matchingCriteria object in issue rule");
      return;
    }

    provider = matchCriteriaJsonObj.get(ATTR_RULETYPE).getAsString();

    // Add query input files
    JsonElement qifJsonE = matchCriteriaJsonObj.get(ATTR_FILTER_INPUT_FILES);
    if (qifJsonE != null && !qifJsonE.isJsonNull()) {
      JsonObject qifJson = qifJsonE.getAsJsonObject();
      qifJson.keySet().stream()
              .forEach(k -> queryInputFiles.put(k, qifJson.get(k).getAsString()));
    } else {
      logger.info("matchingCriteria does not contain object:" + ATTR_FILTER_INPUT_FILES);
    }

    // Add query paths
    if (queryPathsKeyName != null && !"".equals(queryPathsKeyName)) {
      JsonElement qpJsonE = matchCriteriaJsonObj.get(queryPathsKeyName);
      if (qpJsonE != null && !qpJsonE.isJsonNull()) {
        JsonObject qpJson = qpJsonE.getAsJsonObject();
        qpJson.keySet().stream()
                .forEach(k -> queryPaths.put(k, qpJson.get(k)));
      } else {
        logger.info("Query paths key name is not null");
      }
    } else {
      logger.error("matchingCriteria does not contain object:" + queryPathsKeyName);
    }

    // Occurrence attributes
    JsonElement oaJsonE = matchCriteriaJsonObj.get(ATTR_OCCURRENCE_ATTR);
    if (oaJsonE != null && !oaJsonE.isJsonNull()) {
      occurenceAttrs = oaJsonE.getAsJsonObject();
    } else {
      logger.error("matchingCriteria does not contain object:" + ATTR_OCCURRENCE_ATTR);
    }
  }

  public String getProvider() {
    return provider;
  }

  public Map<String, String> getQueryInputFiles() {
    return queryInputFiles;

  }

  public Map<String, JsonElement> getQueryPaths() {
    return queryPaths;
  }


  public JsonObject getOccurrenceAttr() {
    return occurenceAttrs;
  }

  public JsonObject getMatchCriteriaJson() {
    return matchCriteriaJsonObj;
  }
}
