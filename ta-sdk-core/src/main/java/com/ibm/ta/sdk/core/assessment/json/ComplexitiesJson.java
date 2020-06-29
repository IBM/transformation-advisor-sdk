/*
 * (C) Copyright IBM Corp. 2019,2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.ta.sdk.core.assessment.json;

import com.google.gson.annotations.Expose;
import com.ibm.ta.sdk.spi.assess.ComplexityContributionJson;
import com.ibm.ta.sdk.spi.recommendation.ComplexityContribution;
import com.ibm.ta.sdk.spi.recommendation.ComplexityRating;

import java.util.ArrayList;
import java.util.List;

public class ComplexitiesJson {
  @Expose
  private List<ComplexityContributionJson> complexities;

  public List<ComplexityContributionJson> getComplexities() {
    return complexities;
  }
}
