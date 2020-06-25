/*
 * (C) Copyright IBM Corp. 2019,2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.ta.sdk.core.assessment;

import com.google.gson.annotations.Expose;
import com.ibm.ta.sdk.spi.recommendation.ModDimension;
import com.ibm.ta.sdk.spi.recommendation.Target;

import java.util.ArrayList;
import java.util.List;

public class GenericTarget implements Target {
  @Expose
  private String id;

  @Expose
  private String runtime;

  @Expose
  private List<ModDimension> dimensions;

  @Expose
  private List<String> issues;

  @Expose
  private List<String> issueCategories;

  @Override
  public String getId() {
    return id;
  }

  @Override
  public String getRuntime() {
    return runtime;
  }

  @Override
  public List<ModDimension> getDimensions() {
    return dimensions;
  }

  public List<String> getIssues() {
    if (issues == null) {
      issues = new ArrayList<>();
    }
    return issues;
  }

  public List<String> getIssueCategories() {
    if (issueCategories == null) {
      issueCategories = new ArrayList<>();
    }
    return issueCategories;
  }
}
