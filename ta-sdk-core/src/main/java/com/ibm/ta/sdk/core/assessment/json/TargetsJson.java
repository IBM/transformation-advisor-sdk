/*
 * (C) Copyright IBM Corp. 2019,2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.ta.sdk.core.assessment.json;

import com.google.gson.annotations.Expose;
import com.ibm.ta.sdk.core.assessment.GenericTarget;
import com.ibm.ta.sdk.spi.assess.ComplexityContributionJson;

import java.util.List;

public class TargetsJson {
  @Expose
  private List<GenericTarget> targets;

  public List<GenericTarget> getTargets() {
    return targets;
  }
}
