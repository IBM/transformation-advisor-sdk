package com.ibm.ta.sdk.spi.collect;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.annotations.Expose;

import java.util.Properties;

/**
 * Assessment unit metadata file required by Transformation Advisor.
 */
public class AssessmentUnitMetadataJson {

  @Expose
  private String domain;

  @Expose
  private String middleware;

  @Expose
  private String dcVersion = "2.0.4";

  @Expose
  private String host;

  @Expose
  private String assessmentUnitName;

  @Expose
  private String collectionUnitType;

  @Expose
  private String collectionUnitName;

  @Expose
  private Properties identifier;

  public AssessmentUnitMetadataJson(String domain, String middleWare, String hostname, String assessmentUnitName,
                                    String collectionUnitType, String collectionUnitName) {
    this.domain = domain;
    this.middleware = middleWare;
    this.host = hostname;
    this.assessmentUnitName = assessmentUnitName;
    this.collectionUnitType = collectionUnitType;
    this.collectionUnitName = collectionUnitName;
    addIdentifier("assessmentUnitName", assessmentUnitName);
  }

  public AssessmentUnitMetadataJson(Environment env, String assessmentUnitName) {
    this(env.getDomain(), env.getMiddlewareName(), env.getHostname(), assessmentUnitName,
            env.getCollectionUnitType(), env.getCollectionUnitName());
  }

  public AssessmentUnitMetadataJson(Environment env, AssessmentUnit assessmentUnit) {
    this(env, assessmentUnit.getName());
    Properties auIdentifier = assessmentUnit.getIdentifier();
    if (auIdentifier!=null && !auIdentifier.isEmpty()) {
      for (String propName : auIdentifier.stringPropertyNames()) {
        addIdentifier(propName.toString(), auIdentifier.get(propName));
      }
    }
  }

  public void addIdentifier(String key, Object value) {
    if (identifier == null) {
      identifier = new Properties();
    }
    identifier.put(key, value);
  }

  public JsonElement toJsonObject() {
    return new Gson().toJsonTree(this);
  }
}
