/*
 * (C) Copyright IBM Corp. 2019,2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.ta.sdk.core.plugin;

import com.google.gson.JsonObject;
import com.ibm.ta.sdk.core.report.RecommendationReporter;
import com.ibm.ta.sdk.spi.plugin.PluginProvider;
import com.ibm.ta.sdk.spi.plugin.TAException;
import com.ibm.ta.sdk.spi.report.Report;
import com.ibm.ta.sdk.spi.util.Util;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;

public abstract class GenericPluginProvider implements PluginProvider {
  private static Logger logger = LogManager.getLogger(GenericPluginProvider.class.getName());

  protected List<Report> getHtmlReport(String assessmentName) throws TAException {
    logger.info("Get HTML report for assessment:" + assessmentName);

    File assessOutputDir = Util.getAssessmentOutputDir(assessmentName);
    if (!assessOutputDir.exists()) {
      throw new TAException("Output directory not found for assessment:" + assessOutputDir.getAbsolutePath());
    }

    JsonObject recJson;
    RecommendationReporter reportGenerator = null;
    try {
      recJson = Util.getRecommendationsJson(assessmentName);
      logger.info("recommendations.json:" + recJson);
      reportGenerator = new RecommendationReporter(assessmentName, recJson);
    } catch (FileNotFoundException e) {
      logger.error("Recommendation.json not found for assessment:" + assessmentName);
      throw new TAException(e);
    }

    try {
        return reportGenerator.generateHTMLReports();
    } catch (Exception e) {
        logger.error("Failed to generate HTML files for assessment: " + assessmentName, e);
        throw new TAException(e);
    }
  }
}
