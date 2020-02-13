/*
 * (C) Copyright IBM Corp. 2019,2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.ta.sdk.core.collect;

import com.google.gson.reflect.TypeToken;
import com.ibm.ta.sdk.core.util.GenericUtil;
import com.ibm.ta.sdk.spi.collect.AssessmentUnit;
import com.ibm.ta.sdk.spi.collect.DataCollection;
import com.ibm.ta.sdk.spi.collect.Environment;
import com.ibm.ta.sdk.spi.collect.EnvironmentJson;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class GenericDataCollection implements DataCollection {

  private String assessmentName;
  private EnvironmentJson envJson;
  private List<GenericAssessmentUnit> assessmentUnits;

  public GenericDataCollection(String assessmentName, Path envFile, List<GenericAssessmentUnit> assessmentUnits) throws IOException {
    this.assessmentName = assessmentName;
    envJson = GenericUtil.getJsonObj(new TypeToken<EnvironmentJson>(){}, envFile);

    if (assessmentUnits == null) {
      this.assessmentUnits = new ArrayList<>();
    } else {
      this.assessmentUnits = assessmentUnits;

      for (GenericAssessmentUnit au : assessmentUnits) {
        au.setDataCollection(this);
      }
    }
  }


  @Override
  public String getAssessmentName() {
    return assessmentName;
  }

  @Override
  public Environment getEnvironment() {
    return envJson.getEnvironment();
  }

  @Override
  public List<? extends AssessmentUnit> getAssessmentUnits() {
    return assessmentUnits;
  }

  public void addAssessmentUnit(GenericAssessmentUnit assessmentUnit) {
    assessmentUnit.setDataCollection(this);
    assessmentUnits.add(assessmentUnit);
  }
}
