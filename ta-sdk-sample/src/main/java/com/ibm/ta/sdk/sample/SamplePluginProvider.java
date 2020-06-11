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
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SamplePluginProvider extends GenericPluginProvider {
  private static Logger logger = LogManager.getLogger(SamplePluginProvider.class.getName());
  private static final String SAMPLE_DOMAIN = "<Domain>";
  private static final String SAMPLE_MIDDLEWARE= "sample";

  private static Map<Object, Object> collectionSets = Stream.of(new Object[][] {
    { "collection1", Arrays.asList("AssessmentUnit1") },
    { "collection2", Arrays.asList("AssessmentUnit2", "AssessmentUnit3") }
  }).collect(Collectors.toMap(data -> (String) data[0], data -> (List<String>) data[1]));

  // JSON files
  private static final String FILE_ASSESS_DATA_DIR = "/sampleData/";
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
    CliInputOption collectCmdCollectionOpt = new CliInputOption("c", "collectionUnit", "The name of the collection unit to perform the collection");
    CliInputOption collectCmdAssessmentOpt = new CliInputOption("a", "assessmentUnit", "The list of the assessment unit", true, false, null, null);
    List<CliInputOption> collectionCmdOpts = new LinkedList<>(Arrays.asList(collectCmdCollectionOpt, collectCmdAssessmentOpt));
    CliInputCommand collectCmd = new CliInputCommand(CliInputCommand.CMD_COLLECT,
            "Performs data collection",
            collectionCmdOpts, null, Arrays.asList("INSTALL_PATH"));
    return collectCmd;
  }

  @Override
  public List<DataCollection> getCollection(CliInputCommand cliInputCommand) throws TAException {
    logger.info("CliInputCommandOptions:" + cliInputCommand.getOptions());
    logger.info("CliInputCommandArguments:" + cliInputCommand.getArguments());

    try {
      List<DataCollection> colls = new ArrayList<>();
      for (Object collectionUnit: collectionSets.keySet()) {
        String collectionName = (String)collectionUnit;
        DataCollection oneCollection = getDataCollection(cliInputCommand.getArguments().get(0), collectionName);
        colls.add(oneCollection);
      }
      return colls;
    } catch (URISyntaxException e) {
      throw new TAException(e);
    } catch (IOException e) {
      throw new TAException(e);
    }
  }

  private DataCollection getDataCollection(String installPath, String collectionUnitName) throws IOException, URISyntaxException {
      // put your logic here to detect the middleware runtime environment and construct the environmentJson object
      EnvironmentJson envJson = new EnvironmentJson(SAMPLE_DOMAIN, SAMPLE_MIDDLEWARE, "1.0.0");
      envJson.setMiddlewareInstallPath(installPath);
      envJson.setMiddlewareDataPath(null);
      envJson.setCollectionUnitType("Instance");
      envJson.setCollectionUnitName(collectionUnitName);

      List<GenericAssessmentUnit> auList = new ArrayList<>();
      for (String assessmentName: (List<String>)collectionSets.get(collectionUnitName)) {
        auList.add(getAssessmentUnit(assessmentName));
      }
      return new GenericDataCollection(collectionUnitName, envJson, auList);
  }

  private GenericAssessmentUnit getAssessmentUnit(String assessmentUnitName) throws URISyntaxException, IOException {
    // use the middleware specific technology to generate the assesment unit data json file
    // in this sample plug-in we assume the /sampleData/AssessmentUnit1.json is the generated file
    Path assessDataJsonFile = Paths.get(SamplePluginProvider.class.getResource(FILE_ASSESS_DATA_DIR+assessmentUnitName+".json").toURI());
    List<Path> assessmentConfigFiles = new ArrayList<Path>();

    if (assessmentUnitName.equals("AssessmentUnit1")) {
      // You can add more middlewware specific configuration files here.
      Path assessConfigJsonFile = Paths.get(SamplePluginProvider.class.getResource(FILE_ASSESS_CONFIG_FILE_JSON).toURI());
      Path assessConfigJsonFile2 = Paths.get(SamplePluginProvider.class.getResource(FILE_ASSESS_CONFIG_FILE2_JSON).toURI());
      Path assessConfigXmlFile = Paths.get(SamplePluginProvider.class.getResource(FILE_ASSESS_CONFIG_FILE_XML).toURI());
      Path assessConfigXmlFile2 = Paths.get(SamplePluginProvider.class.getResource(FILE_ASSESS_CONFIG_FILE_XML2).toURI());

      assessmentConfigFiles.add(assessConfigJsonFile);
      assessmentConfigFiles.add(assessConfigJsonFile2);
      assessmentConfigFiles.add(assessConfigXmlFile);
      assessmentConfigFiles.add(assessConfigXmlFile2);

      // here is the example to add a specific file on file system to the config file list
      File assessConfigXmlFile3 = new File("/tmp/sample/sampleConfig/sampleConfigFile.json");
      if (assessConfigXmlFile3.exists()) {
        assessmentConfigFiles.add(assessConfigXmlFile3.toPath());
      }
    }

    return super.getAssessmentUnit(assessmentUnitName, assessDataJsonFile, assessmentConfigFiles);
  }
}
