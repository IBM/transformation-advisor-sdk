/*
 * (C) Copyright IBM Corp. 2019,2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.ta.sdk.core.plugin;

import com.google.gson.JsonObject;
import com.ibm.ta.sdk.core.assessment.GenericRecommendation;
import com.ibm.ta.sdk.core.collect.GenericAssessmentUnit;
import com.ibm.ta.sdk.core.collect.TextContextMask;
import com.ibm.ta.sdk.core.report.RecommendationReporter;
import com.ibm.ta.sdk.spi.collect.ContentMask;
import com.ibm.ta.sdk.spi.plugin.CliInputCommand;
import com.ibm.ta.sdk.spi.plugin.CliInputOption;
import com.ibm.ta.sdk.spi.plugin.PluginProvider;
import com.ibm.ta.sdk.spi.plugin.TAException;
import com.ibm.ta.sdk.spi.recommendation.Recommendation;
import com.ibm.ta.sdk.spi.report.Report;
import com.ibm.ta.sdk.spi.util.Util;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static com.ibm.ta.sdk.core.util.Constants.*;

public abstract class GenericPluginProvider implements PluginProvider {
  private static Logger logger = LogManager.getLogger(GenericPluginProvider.class.getName());

  @Override
  public List<Recommendation> getRecommendation(CliInputCommand cliInputCommand) throws TAException {
    try {

      String middlewareDir = "/" + getMiddleware() + "/";
      Path complexityJsonFile = Paths.get(GenericPluginProvider.class.getResource(middlewareDir + FILE_COMPLEXITY_JSON).toURI());
      Path issueCatJsonFile = Paths.get(GenericPluginProvider.class.getResource(middlewareDir + FILE_ISSUECAT_JSON).toURI());
      Path issueJsonFile = Paths.get(GenericPluginProvider.class.getResource(middlewareDir + FILE_ISSUE_JSON).toURI());
      Path targetJsonFile = Paths.get(GenericPluginProvider.class.getResource(middlewareDir + FILE_TARGET_JSON).toURI());
      File outDir = Util.getOutputDir();
      List<Recommendation> recs = new ArrayList<>();

      File[] fileList = outDir.listFiles();
      for (File file: fileList) {
        if (file.isDirectory()) {
          String dirName = file.getName();
          GenericRecommendation rec = new GenericRecommendation(dirName, issueJsonFile, issueCatJsonFile, complexityJsonFile, targetJsonFile);
          recs.add(rec);
        }
      }

      return recs;
    } catch (URISyntaxException e) {
      throw new TAException(e);
    } catch (IOException e) {
      throw new TAException(e);
    }
  }

  @Override
  public List<Report> getReport(String assessmentName, CliInputCommand reportCommand) throws TAException {
    logger.info("CliInputCommandOptions:" + reportCommand.getOptions());
    logger.info("CliInputCommandArguments:" + reportCommand.getArguments());

    List<Report> reports = getHtmlReport(assessmentName); // Use html report generator from base class
    return reports;
  }

  @Override
  public CliInputCommand getAssessCommand() {
    // Assess command
    CliInputOption assessCmdSkipCollectOpt = new CliInputOption("s", "skipcollect", "Skip collection, perform assessment only");
    List<CliInputOption> assessCmdOpts = new LinkedList<>(Arrays.asList(assessCmdSkipCollectOpt));
    CliInputCommand assessCmd = new CliInputCommand(CliInputCommand.CMD_ASSESS,
            "Performs data collection and assessment",
            assessCmdOpts, null, getCollectCommand().getArgumentDisplayNames());

    // Assess subcommand get get only costs in days/weeks
    CliInputOption costCmdDayOpt = new CliInputOption(null, "days", "Display cost in days");
    CliInputOption costCmdWeekOpt = new CliInputOption(null, "weeks", "Display cost in weeks");
    List<CliInputOption> costCmdOpts = new LinkedList<>(Arrays.asList(costCmdDayOpt, costCmdWeekOpt));
    CliInputCommand analyzeCostCmd = new CliInputCommand("cost",
            "Generate cost summary only",
            costCmdOpts, null, null);
    List<CliInputCommand> analyzeCmds = new LinkedList<>(Arrays.asList(analyzeCostCmd));
    assessCmd.setCommands(analyzeCmds);

    return assessCmd;
  }

  @Override
  public CliInputCommand getReportCommand() {
    // Report command
    CliInputOption reportCmdAllOpt = new CliInputOption("", "html", "HTML report format");
    List<CliInputOption> reportCmdOpts = new LinkedList<>(Arrays.asList(reportCmdAllOpt));
    CliInputCommand reportCmd = new CliInputCommand(CliInputCommand.CMD_REPORT,
            "Generate reports",
            reportCmdOpts, null, null);
    return reportCmd;
  }

  protected Path getFileFromUri(URI uri) throws IOException {
    if (!uri.toString().startsWith("file")){
      Map<String, String> env = new HashMap<>();
      env.put("create", "true");
      FileSystem zipfs = FileSystems.newFileSystem(uri, env);
    }
    return Paths.get(uri);
  }

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

  protected List<ContentMask> getContentMasks() {
    List<ContentMask> contentMasks = new ArrayList<>();

    ContentMask jsonPsswdMask = new TextContextMask(
            Arrays.asList("(.*).json"),
            Arrays.asList(new AbstractMap.SimpleEntry("(\"password\"\\s?:\\s?\").*(\")", "$1" + ContentMask.MASK + "$2")));
    contentMasks.add(jsonPsswdMask);

    return contentMasks;
  }

  protected GenericAssessmentUnit getAssessmentUnit(String assessUnitName, Path assessmentUnitJsonDataFile, List<Path> assessmentUnitConfigFiles) throws IOException {
    GenericAssessmentUnit au = new GenericAssessmentUnit(assessUnitName, assessmentUnitJsonDataFile, assessmentUnitConfigFiles);

    // Add content mask to filter out values for 'password' fields
    au.setContentMasks(getContentMasks());

    return au;
  }
}
