/*
 * (C) Copyright IBM Corp. 2019,2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.ta.sdk.spi.collect;

import com.google.gson.JsonObject;
import com.google.gson.JsonElement;
import com.google.gson.annotations.Expose;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;


public class EnvironmentJson {

  private static Logger logger = LogManager.getLogger(EnvironmentJson.class.getName());
  @Expose
  private String pluginVersion;

  @Expose
  private String domain;

  @Expose
  private String operatingSystem;

  @Expose
  private String hostName;

  @Expose
  private String middlewareName;

  @Expose
  private String middlewareVersion;

  @Expose
  private String middlewareInstallPath;

  @Expose
  private String middlewareDataPath;

  @Expose
  private JsonElement middlewareMetadata;

  @Expose
  private String collectionUnitName;

  @Expose
  private String collectionUnitType;

  @Expose
  private JsonElement assessmentMetadata;

  @Expose
  private List<String> assessmentUnits;

  public EnvironmentJson() {
    // For read json from file
  }

  public EnvironmentJson(String domain, String middlewareName, String middlewareVersion) {
    this.domain = domain;
    this.middlewareName = middlewareName;
    this.middlewareVersion = middlewareVersion;
    this.operatingSystem = System.getProperty("os.name");
    try {
      this.hostName = InetAddress.getLocalHost().getCanonicalHostName();
    } catch (UnknownHostException e) {
      logger.error("cannot detect the hostname and set it to environment", e);
    }
  }

  public EnvironmentJson(Environment environment) {
    domain = environment.getDomain();
    operatingSystem = environment.getOperatingSystem();
    hostName = environment.getHostname();
    middlewareName = environment.getMiddlewareName();
    middlewareVersion = environment.getMiddlewareVersion();
    middlewareInstallPath = environment.getMiddlewareInstallPath();
    middlewareDataPath = environment.getMiddlewareDataPath();
    if (environment.getMiddlewareMetadata() != null) {
      middlewareMetadata = environment.getMiddlewareMetadata();
    }
    collectionUnitName = environment.getConnectionUnitName();
    collectionUnitType = environment.getConnectionUnitType();
    if (environment.getAssessmentMetadata() != null) {
      assessmentMetadata = environment.getAssessmentMetadata();
    }
  }

  public Environment getEnvironment() {
    Environment environment = new Environment() {
      @Override
      public String getOperatingSystem() {
        return operatingSystem;
      }

      @Override
      public String getHostname() {
        return hostName;
      }

      @Override
      public String getDomain() {
        return domain;
      }

      @Override
      public String getMiddlewareName() {
        return middlewareName;
      }

      @Override
      public String getMiddlewareVersion() {
        return middlewareVersion;
      }

      @Override
      public String getMiddlewareInstallPath() {
        return middlewareInstallPath;
      }

      @Override
      public String getMiddlewareDataPath() {
        return middlewareDataPath;
      }

      @Override
      public JsonObject getMiddlewareMetadata() {
        if (middlewareMetadata != null && middlewareMetadata.isJsonObject()) {
          return middlewareMetadata.getAsJsonObject();
        }
        return null;
      }

      @Override
      public String getConnectionUnitName() {
        return collectionUnitName;
      }

      @Override
      public String getConnectionUnitType() {
        return collectionUnitType;
      }

      @Override
      public JsonObject getAssessmentMetadata() {
        if (assessmentMetadata != null && assessmentMetadata.isJsonObject()) {
          return assessmentMetadata.getAsJsonObject();
        }
        return null;
      }
    };
    return environment;
  }

  public void setOperatingSystem(String operatingSystem) {
    this.operatingSystem = operatingSystem;
  }

  public void setMiddlewareMetadata(JsonElement middlewareMetadata) {
    this.middlewareMetadata = middlewareMetadata;
  }

  public void setHostName(String hostName) {
    this.hostName = hostName;
  }

  public void setAssessmentMetadata(JsonElement assessmentMetadata) {
    this.assessmentMetadata = assessmentMetadata;
  }

  public void setMiddlewareInstallPath(String middlewareInstallPath) {
    this.middlewareInstallPath = middlewareInstallPath;
  }

  public void setMiddlewareDataPath(String middlewareDataPath) {
    this.middlewareDataPath = middlewareDataPath;
  }

  public void setCollectionUnitName(String collectionUnitName) {
    this.collectionUnitName = collectionUnitName;
  }

  public void setCollectionUnitType(String collectionUnitType) {
    this.collectionUnitType = collectionUnitType;
  }

  public void setPluginVersion(String version) {
    this.pluginVersion = version;
  }

  public List<String> getAssessmentUnits() {
    return assessmentUnits;
  }

  public void setAssessmentUnits(List<String> auNamesList) {
    this.assessmentUnits = auNamesList;
  }
}
