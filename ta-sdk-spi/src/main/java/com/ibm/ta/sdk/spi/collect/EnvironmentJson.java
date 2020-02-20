/*
 * (C) Copyright IBM Corp. 2019,2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.ta.sdk.spi.collect;

import com.google.gson.JsonObject;
import com.google.gson.JsonElement;
import com.google.gson.annotations.Expose;
import com.ibm.ta.sdk.spi.collect.Environment;

public class EnvironmentJson {
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
  private String assessmentName;

  @Expose
  private String assessmentType;

  @Expose
  private JsonElement assessmentMetadata;

  public EnvironmentJson() {
    // For read json from file
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
    assessmentName = environment.getAssessmentName();
    assessmentType = environment.getAssessmentType();
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
      public String getAssessmentName() {
        return assessmentName;
      }

      @Override
      public String getAssessmentType() {
        return assessmentType;
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
}
