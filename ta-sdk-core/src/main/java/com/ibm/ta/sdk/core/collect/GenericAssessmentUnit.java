/*
 * (C) Copyright IBM Corp. 2019,2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.ta.sdk.core.collect;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ibm.ta.sdk.spi.collect.ContentMask;
import com.ibm.ta.sdk.spi.collect.DataCollection;
import com.ibm.ta.sdk.core.util.GenericUtil;
import com.ibm.ta.sdk.spi.collect.AssessmentUnit;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class GenericAssessmentUnit implements AssessmentUnit {

  private String assessmentUnitName;
  private JsonObject assessmentInfo;
  private List<Path> configFileList;
  private List<ContentMask> contentMasks;
  private DataCollection dataCollection;

  public GenericAssessmentUnit(Path assessmentUnitJsonDataFile, List<Path> assessmentUnitConfigFiles) throws IOException {
    this(null, assessmentUnitJsonDataFile, assessmentUnitConfigFiles);
  }

  public GenericAssessmentUnit(String assessUnitName, Path assessmentUnitJsonDataFile, List<Path> assessmentUnitConfigFiles) throws IOException {
    if (assessUnitName == null) {
      assessUnitName = assessmentUnitJsonDataFile.getFileName().toString();
      int extIndex = assessUnitName.lastIndexOf(".");
      if (extIndex > 0) {
        assessUnitName = assessUnitName.substring(0, extIndex);
      }
    }
    this.assessmentUnitName = assessUnitName;

    assessmentInfo = (JsonObject) new JsonParser().parse(GenericUtil.readFileToString(assessmentUnitJsonDataFile));

    configFileList = assessmentUnitConfigFiles;
  }

  @Override
  public String getName() {
    return assessmentUnitName;
  }

  public DataCollection getDataCollection() {
    return dataCollection;
  }

  public void setDataCollection(DataCollection dataCollection) {
    this.dataCollection = dataCollection;
  }

  @Override
  public JsonObject getAssessmentData() {
    return assessmentInfo;
  }

  @Override
  public List<Path> getConfigFiles() {
    return configFileList;
  }

  @Override
  public List<ContentMask> getContentMasks() {
    if (contentMasks == null) {
      contentMasks = new ArrayList<>();
    }
    return contentMasks;
  }

  public void setContentMasks(List<ContentMask> contentMasks) {
    this.contentMasks = contentMasks;
  }

}
