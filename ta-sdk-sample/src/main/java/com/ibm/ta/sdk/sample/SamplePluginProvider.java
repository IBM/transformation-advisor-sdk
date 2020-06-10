/*
 * (C) Copyright IBM Corp. 2019,2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.ta.sdk.sample;

import com.ibm.ta.sdk.spi.collect.EnvironmentJson;
import com.ibm.ta.sdk.spi.plugin.CliInputCommand;
import com.ibm.ta.sdk.spi.plugin.CliInputOption;
import com.ibm.ta.sdk.spi.plugin.TAException;
import com.ibm.ta.sdk.core.plugin.GenericPluginProvider;
import com.ibm.ta.sdk.core.collect.GenericAssessmentUnit;
import com.ibm.ta.sdk.core.collect.GenericDataCollection;
import com.ibm.ta.sdk.spi.collect.DataCollection;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class SamplePluginProvider extends GenericPluginProvider {
  private static Logger logger = LogManager.getLogger(SamplePluginProvider.class.getName());
  private static final String SAMPLE_DOMAIN = "sampleDomain";
  private static final String SAMPLE_MIDDLEWARE= "sample";

  // JSON files
  private static final String FILE_ASSESS_DATA_JSON = "/sampleData/AssessmentUnit1.json";
  private static final String FILE_ASSESS_CONFIG_FILE_JSON = "/sampleData/SampleConfigFile.json";
  private static final String FILE_ASSESS_CONFIG_FILE2_JSON = "/sampleData/SampleConfigFile2.json";
  private static final String FILE_ASSESS_CONFIG_FILE_XML= "/sampleData/sampleData.xml";
  private static final String FILE_ASSESS_CONFIG_FILE_XML2= "/sampleData/server.xml";

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

      // put your logic here to detect the middleware runtime environment and construct the environmentJson object
      String instanceName = "Installation1";
      EnvironmentJson envJson = new EnvironmentJson(SAMPLE_DOMAIN, SAMPLE_MIDDLEWARE, "1.0.0");
      envJson.setMiddlewareInstallPath(cliInputCommand.getArguments().get(0));
      envJson.setMiddlewareDataPath(cliInputCommand.getArguments().get(1));
      envJson.setCollectionUnitType("Instance");
      envJson.setCollectionUnitName(instanceName);
      envJson.setCollectionUnitTypeLabel("Instance");
      envJson.setAssessmentUnitSingleLabel("Assessment unit");
      envJson.setAssessmentUnitMultipleLabel("Assessment units");

      // use the middleware specific technology to generate the assesment unit data json file
      // in this sample plug-in we assume the /sampleData/AssessmentUnit1.json is the generated file
      Path assessDataJsonFile = getFileFromUri(SamplePluginProvider.class.getResource(FILE_ASSESS_DATA_JSON).toURI());

      // You can add more middlewware specific configuration files here.
      Path assessConfigJsonFile = Paths.get(SamplePluginProvider.class.getResource(FILE_ASSESS_CONFIG_FILE_JSON).toURI());
      Path assessConfigJsonFile2 = Paths.get(SamplePluginProvider.class.getResource(FILE_ASSESS_CONFIG_FILE2_JSON).toURI());
      Path assessConfigXmlFile = Paths.get(SamplePluginProvider.class.getResource(FILE_ASSESS_CONFIG_FILE_XML).toURI());
      Path assessConfigXmlFile2 = Paths.get(SamplePluginProvider.class.getResource(FILE_ASSESS_CONFIG_FILE_XML2).toURI());

      List<Path> assessmentConfigFiles = new ArrayList<Path>();
      assessmentConfigFiles.add(assessConfigJsonFile);
      assessmentConfigFiles.add(assessConfigJsonFile2);
      assessmentConfigFiles.add(assessConfigXmlFile);
      assessmentConfigFiles.add(assessConfigXmlFile2);

      // here is the example to add a specific file on file system to the config file list
      File assessConfigXmlFile3 = new File("/tmp/sample/sampleConfig/sampleConfigFile.json");
      if (assessConfigXmlFile3.exists()) {
        assessmentConfigFiles.add(assessConfigXmlFile3.toPath());
      }

      // all these data file and config files for an assessment unit will be copied to out/<assessmentName>/ directory
      // If the assessment unit name isnot specified,  it will use filename of the data file as the assessment unit name
      // these filess will be used in the next assess() command to generate the recommandtion
      // for plug-in developers,  you also need to provide a set of issue json file under <middlewareName> dir
      // in runtime,  TA SDK will load these issue json files from classpath to detect the issues from the asseesment data file or configure files
      List<GenericAssessmentUnit> auList = new ArrayList<>();
      auList.add(super.getAssessmentUnit("Plants.ear", assessDataJsonFile, assessmentConfigFiles));

      GenericDataCollection coll = new GenericDataCollection(instanceName, envJson, auList);
      List<DataCollection> colls = new ArrayList<>();
      colls.add(coll);
      return colls;
    } catch (URISyntaxException e) {
      throw new TAException(e);
    } catch (IOException e) {
      throw new TAException(e);
    }
  }
}
