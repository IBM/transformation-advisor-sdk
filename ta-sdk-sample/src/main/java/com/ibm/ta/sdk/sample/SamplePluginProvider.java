/*
 * (C) Copyright IBM Corp. 2019,2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.ta.sdk.sample;

import com.ibm.ta.sdk.spi.plugin.CliInputCommand;
import com.ibm.ta.sdk.spi.plugin.CliInputOption;
import com.ibm.ta.sdk.spi.plugin.TAException;
import com.ibm.ta.sdk.spi.collect.ContentMask;
import com.ibm.ta.sdk.spi.report.Report;
import com.ibm.ta.sdk.core.plugin.GenericPluginProvider;
import com.ibm.ta.sdk.core.collect.GenericAssessmentUnit;
import com.ibm.ta.sdk.core.collect.GenericDataCollection;
import com.ibm.ta.sdk.core.collect.TextContextMask;
import com.ibm.ta.sdk.core.assessment.GenericRecommendation;
import com.ibm.ta.sdk.spi.collect.DataCollection;
import com.ibm.ta.sdk.spi.recommendation.Recommendation;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class SamplePluginProvider extends GenericPluginProvider {
  private static Logger logger = Logger.getLogger(SamplePluginProvider.class.getName());
  private static final String SAMPLE_DOMAIN = "sampleDomain";
  private static final String SAMPLE_MIDDLEWARE= "sample";

  // JSON files
  private static final String FILE_ENVIRONMENT_JSON = "/sampleData/environment.json";
  private static final String FILE_ASSESS_DATA_JSON = "/sampleData/AssessmentUnit1.json";
  private static final String FILE_ASSESS_CONFIG_FILE_JSON = "/sampleData/SampleConfigFile.json";
  private static final String FILE_ASSESS_CONFIG_FILE2_JSON = "/sampleData/SampleConfigFile2.json";
  private static final String FILE_ASSESS_CONFIG_FILE_XML= "/sampleData/sampleData.xml";
  private static final String FILE_ASSESS_CONFIG_FILE_XML2= "/sampleData/Plants.ear_server.xml";
  private static final String FILE_COMPLEXITY_JSON = "/sample/complexity.json";
  private static final String FILE_ISSUECAT_JSON = "/sample/issue-category.json";
  private static final String FILE_ISSUE_JSON = "/sample/issue.json";
  private static final String FILE_TARGET_JSON = "/sample/target.json";

  @Override
  public String getDomain() {
    return SAMPLE_DOMAIN;
  }

  @Override
  public String getMiddleware() {
    return SAMPLE_MIDDLEWARE;
  }

  @Override
  public CliInputCommand getCollectCommand() {
    // Collect command
    CliInputOption collectCmdAllOpt = new CliInputOption("a", "all", "Collect everything");
    CliInputOption collectCmdDataOpt = new CliInputOption("x", "extdata", "Directory containing the external data", true, true, "DIR_NAME", "/opt/test");
    List<CliInputOption> collectionCmdOpts = new LinkedList<>(Arrays.asList(collectCmdAllOpt, collectCmdDataOpt));
    CliInputCommand collectCmd = new CliInputCommand(CliInputCommand.CMD_COLLECT,
            "Performs data collection",
            collectionCmdOpts, null, Arrays.asList("INSTALL_PATH", "DATA_DIR"));
    return collectCmd;
  }

  @Override
  public List<DataCollection> getCollection(CliInputCommand cliInputCommand) throws TAException {
    logger.info("CliInputCommandOptions:" + cliInputCommand.getOptions());
    logger.info("CliInputCommandArguments:" + cliInputCommand.getArguments());

    try {
      Path envJsonFile = getFileFromUri(SamplePluginProvider.class.getResource(FILE_ENVIRONMENT_JSON).toURI());

      Path assessDataJsonFile = Paths.get(SamplePluginProvider.class.getResource(FILE_ASSESS_DATA_JSON).toURI());
      Path assessConfigJsonFile = Paths.get(SamplePluginProvider.class.getResource(FILE_ASSESS_CONFIG_FILE_JSON).toURI());
      Path assessConfigJsonFile2 = Paths.get(SamplePluginProvider.class.getResource(FILE_ASSESS_CONFIG_FILE2_JSON).toURI());
      Path assessConfigXmlFile = Paths.get(SamplePluginProvider.class.getResource(FILE_ASSESS_CONFIG_FILE_XML).toURI());
      Path assessConfigXmlFile2 = Paths.get(SamplePluginProvider.class.getResource(FILE_ASSESS_CONFIG_FILE_XML2).toURI());

      List<Path> assessmentConfigFiles = new ArrayList<Path>();
      assessmentConfigFiles.add(assessConfigJsonFile);
      assessmentConfigFiles.add(assessConfigJsonFile2);
      assessmentConfigFiles.add(assessConfigXmlFile);
      assessmentConfigFiles.add(assessConfigXmlFile2);


      File fsJsonFile = new File("/tmp/tacore/test1/test.json");
      if (fsJsonFile.exists()) {
        assessmentConfigFiles.add(fsJsonFile.toPath());
      }

      GenericAssessmentUnit au = new GenericAssessmentUnit(assessDataJsonFile, assessmentConfigFiles);

      // Add content mask to filter out values for 'password' fields
      au.setContentMasks(getContentMasks());

      List<GenericAssessmentUnit> auList = new ArrayList<>();
      auList.add(au);


      GenericDataCollection coll = new GenericDataCollection("Installation1", envJsonFile, auList);
      List<DataCollection> colls = new ArrayList<>();
      colls.add(coll);
      return colls;
    } catch (URISyntaxException e) {
      throw new TAException(e);
    } catch (IOException e) {
      throw new TAException(e);
    }
  }

  @Override
  public CliInputCommand getAssessCommand() {
    // Assess command
    CliInputOption assessCmdSkipCollectOpt = new CliInputOption("s", "skipcollect", "Skip collection, perform assessment only");
    List<CliInputOption> assessCmdOpts = new LinkedList<>(Arrays.asList(assessCmdSkipCollectOpt));
    CliInputCommand assessCmd = new CliInputCommand(CliInputCommand.CMD_ASSESS,
            "Performs data collection and assessment",
            assessCmdOpts, null, Arrays.asList("INSTALL_PATH", "DATA_DIR"));

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

  private List<ContentMask> getContentMasks() {
    List<ContentMask> contentMasks = new ArrayList<>();

    ContentMask jsonPsswdMask = new TextContextMask(
            Arrays.asList("(.*).json"),
            Arrays.asList(new AbstractMap.SimpleEntry("(\"password\"\\s?:\\s?\").*(\")", "$1" + ContentMask.MASK + "$2")));
    contentMasks.add(jsonPsswdMask);

    return contentMasks;
  }

  @Override
  public List<Recommendation> getRecommendation(CliInputCommand cliInputCommand) throws TAException {
    try {
      Path complexityJsonFile = Paths.get(SamplePluginProvider.class.getResource(FILE_COMPLEXITY_JSON).toURI());
      Path issueCatJsonFile = Paths.get(SamplePluginProvider.class.getResource(FILE_ISSUECAT_JSON).toURI());
      Path issueJsonFile = Paths.get(SamplePluginProvider.class.getResource(FILE_ISSUE_JSON).toURI());
      Path targetJsonFile = Paths.get(SamplePluginProvider.class.getResource(FILE_TARGET_JSON).toURI());

      GenericRecommendation rec = new GenericRecommendation("Installation1", issueJsonFile, issueCatJsonFile, complexityJsonFile, targetJsonFile);
      List<Recommendation> recs = new ArrayList<>();
      recs.add(rec);
      return recs;
    } catch (URISyntaxException e) {
      throw new TAException(e);
    } catch (IOException e) {
      throw new TAException(e);
    }
  }

  @Override
  public CliInputCommand getReportCommand() {
    // Collect command
    CliInputOption reportCmdAllOpt = new CliInputOption("", "html", "HTML report format");
    List<CliInputOption> reportCmdOpts = new LinkedList<>(Arrays.asList(reportCmdAllOpt));
    CliInputCommand reportCmd = new CliInputCommand(CliInputCommand.CMD_REPORT,
            "Generate reports",
            reportCmdOpts, null, null);
    return reportCmd;
  }

  @Override
  public List<Report> getReport(String assessmentName, CliInputCommand reportCommand) throws TAException {
    logger.info("CliInputCommandOptions:" + reportCommand.getOptions());
    logger.info("CliInputCommandArguments:" + reportCommand.getArguments());

    List<Report> reports = getHtmlReport(assessmentName); // Use html report generator from base class
    return reports;
  }

  private Path getFileFromUri(URI uri) throws IOException {
    Map<String, String> env = new HashMap<>();
    env.put("create", "true");
    FileSystem zipfs = FileSystems.newFileSystem(uri, env);
    return Paths.get(uri);
  }
}
