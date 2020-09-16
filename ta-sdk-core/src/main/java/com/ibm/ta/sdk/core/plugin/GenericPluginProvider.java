/*
 * (C) Copyright IBM Corp. 2019,2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.ta.sdk.core.plugin;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.ibm.ta.sdk.core.assessment.GenericRecommendation;
import com.ibm.ta.sdk.core.collect.GenericAssessmentUnit;
import com.ibm.ta.sdk.core.collect.TextContextMask;
import com.ibm.ta.sdk.core.report.RecommendationReporter;
import com.ibm.ta.sdk.core.util.FreeMarkerTemplateResolver;
import com.ibm.ta.sdk.core.util.GenericUtil;
import com.ibm.ta.sdk.spi.collect.ContentMask;
import com.ibm.ta.sdk.spi.plugin.CliInputCommand;
import com.ibm.ta.sdk.spi.plugin.CliInputOption;
import com.ibm.ta.sdk.spi.plugin.PluginProvider;
import com.ibm.ta.sdk.spi.plugin.TAException;
import com.ibm.ta.sdk.spi.recommendation.Recommendation;
import com.ibm.ta.sdk.spi.report.Report;
import com.ibm.ta.sdk.spi.util.Util;
import com.ibm.ta.sdk.spi.validation.TaJsonFileValidator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.nio.file.*;
import java.util.*;

import static com.ibm.ta.sdk.core.util.Constants.*;

public abstract class GenericPluginProvider implements PluginProvider {
  private static Logger logger = LogManager.getLogger(GenericPluginProvider.class.getName());

  @Override
  public void validateJsonFiles() throws TAException {
    try {
       getFileFromUri(getClass().getClassLoader().getResource(getMiddleware()).toURI());
    } catch (Exception e) {
      logger.error("cannot find middleware in class path. exception is: ", e);
      throw new TAException("Command assess is not supported for plugin provider "+this.getClass()+
              "\n        No middleware specific issue json files found in plugin provider ");
    }
    TaJsonFileValidator.validateIssue(getMiddleware() + File.separator + FILE_ISSUES_JSON);
    TaJsonFileValidator.validateComplexity(getMiddleware() + File.separator + FILE_COMPLEXITIES_JSON);
    TaJsonFileValidator.validateTarget(getMiddleware() + File.separator + FILE_TARGETS_JSON);
  }

  @Override
  public String getVersion() {
    return Util.getSDKVersion();
  }

  @Override
  public List<Recommendation> getRecommendation(CliInputCommand cliInputCommand) throws TAException {
    try {
      File outDir = Util.getOutputDir();
      List<Recommendation> recs = new ArrayList<>();

      File[] fileList = outDir.listFiles();
      for (File file: fileList) {
        if (file.isDirectory()) {
          String dirName = file.getName();
          GenericRecommendation rec = GenericRecommendation.createGenericRecommemndation(dirName, getMiddleware());
          recs.add(rec);
        }
      }

      return recs;
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
      try {
        FileSystem zipfs = FileSystems.newFileSystem(uri, env);
      } catch (FileSystemAlreadyExistsException fsaee) {
        // do nothing here,  the file system for jar file is already created.
      }
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

  @Override
  public CliInputCommand getMigrateCommand() {
    // migrate command
    CliInputOption migrateCmdAssessNameOpt = new CliInputOption("n", "assessUnitName", "The name of assessment unit", true, true, null, null);
    List<CliInputOption> migrateCmdOpts = new LinkedList<>(Arrays.asList(migrateCmdAssessNameOpt));
    CliInputCommand migrateCmd = new CliInputCommand(CliInputCommand.CMD_MIGRATE,
            "Generate migration bundle for different target based on assessment unit files in the collection",
            migrateCmdOpts, null, Arrays.asList("COLLECTION_UNIT_DIR"));
    return migrateCmd;
  }

  @Override
  public void getMigrationBundle(CliInputCommand migrateCommand, List<String> targets) throws TAException {
    List<String> migrateArgu = migrateCommand.getArguments();

    // check the passin collection directory is a directory
    JsonObject envJson = null;
    File collectionDir = new File(migrateArgu.get(0));
    if (!collectionDir.isDirectory()) {
      throw new TAException("The value for collectionDir is not a directory.");
    }

    // check the environment.json file is in the collection directory.
    File envJsonFile = new File(collectionDir.getAbsolutePath()+File.separator+ ENVIRONMENT_JSON);
    if (!envJsonFile.exists()) {
      throw new TAException("The value for collectionDir is not valid collection directory,  cannot find the environment.json file in that directory");
    }

    // load the environment.json file from the collection directory.
    try {
      envJson = GenericUtil.getJsonObj(new TypeToken<JsonObject>(){}, envJsonFile.toPath());
    } catch (IOException e) {
      throw new TAException("Failed to parse environment.json file to JsonObject.",e);
    }

    JsonArray assessments = envJson.getAsJsonArray("assessmentUnits");
    List<String> assessNames = CliInputOption.getCliOptionValuesByShortName(migrateCommand.getOptions(), "n");

    for (JsonElement assess:assessments) {
      String assessName = assess.getAsString();
      if (assessNames.size()==0 || assessNames.contains(assessName)) {
          FreeMarkerTemplateResolver fmtr = new FreeMarkerTemplateResolver(this, new File(collectionDir.getAbsolutePath()+File.separator+assessName), envJson);
          fmtr.resolveTemplatesForTargets(targets);
      } else {
          logger.warn("skip generate migration bundle for assessment unit " + assessName);
      }
    }
  }
}
