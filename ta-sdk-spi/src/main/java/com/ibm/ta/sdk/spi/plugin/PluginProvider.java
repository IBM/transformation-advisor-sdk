/*
 * (C) Copyright IBM Corp. 2019,2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.ta.sdk.spi.plugin;

import com.ibm.ta.sdk.spi.collect.DataCollection;
import com.ibm.ta.sdk.spi.recommendation.Recommendation;
import com.ibm.ta.sdk.spi.report.Report;
import com.ibm.ta.sdk.spi.validation.TaJsonFileValidator;

import java.util.List;


/**
 * The interface for TA data collection plug-ins. Each plug-in supports the commands to collect and assess
 * for a <i>middleware</i>. <i>Collect</i> is ran first to gather configuration information about the
 * middleware. The output from the collection may include configuration files from the middleware, and additional
 * processed configuration information in JSON format. After the data collection is completed, <i>assess</i>
 * is ran to analyze the collected configuration data. The final output of <i>assess</i> is a recommendations file
 * containing a list of issues, including details on how to resolve it.
 *
 * <p>TA's datacollector uses the Java Service Provider Interface (SPI) to get all available plug-ins in the
 * classpath.
 */
public interface PluginProvider {
  /**
   * Version number of the Plug-in.
   * @return Plug-in version
   */
  String getVersion();

  /**
   * Domain is a container to group a set of middleware together. These middleware may belong to the same
   * product family.
   * @return Name of the domain
   */
  String getDomain();

  /**
   * Name of the middleware this plug-in supports.
   *
   * @return Name of the middleware
   */
  String getMiddleware();

  /**
   * The collect command is a command with the name {@link CliInputCommand#CMD_COLLECT}. It contains the options and
   * arguments that is required by the plug-in to run the collect command. The user input values for the options
   * and arguments is passed to the {@link #getCollection(CliInputCommand)} method when it is invoked.
   *
   * @return Collect command including required options and arguments.
   */
  CliInputCommand getCollectCommand();

  /**
   * This is the entry point to perform collection for this middleware.
   *
   * <p>The {@code collectCommand} parameter contains the user provided options and arguments from the CLI for the
   * collect command. Use
   * {@link CliInputCommand#getOptions()} to get the values of the options provided by the user, and
   * {@link CliInputCommand#getArguments()} for the values of the arguments provided by the user.
   *
   * <p>The {@link DataCollection} object returned from this method contains data about the environment,
   * and configuration and files for each assessment unit. This data is persisted in an <i>output</i> directory,
   * with a separate subdirectory for each assessment unit.
   *
   * <p>The collected data is used later during assessment to identify issues and generate a recommendations.json.
   *
   * @param collectCommand Collect command containing user input options and arguments
   * @return List of collected configuration data and files
   * @throws TAException If an error occurs during data collection
   */
  List<DataCollection> getCollection(CliInputCommand collectCommand) throws TAException;


  /**
   * The assess command is a command with the name {@link CliInputCommand#CMD_ASSESS}. It contains the options and
   * arguments that is required by the plug-in to run the assess command. The user input values for the options
   * and arguments is passed to the {@link #getRecommendation(CliInputCommand)} method when it is invoked.
   *
   * @return Assess command including required options and arguments.
   */
  CliInputCommand getAssessCommand();

  /**
   * This is the entry point to perform assessment for this middleware. The assessment uses the collected
   * configuration information and files as input. The data is parsed to identify issues. The output from the
   * assessment is a {@link Recommendation} object that contains a list of issues by
   * {@link com.ibm.ta.sdk.spi.recommendation.Target} platform.
   *
   * <p>The {@code assessCommand} parameter contains the user provided options and arguments from the CLI for the
   * assess command. Use
   * {@link CliInputCommand#getOptions()} to get the values of the options provided by the user, and
   * {@link CliInputCommand#getArguments()} for the values of the arguments provided by the user.
   *
   * <p>The {@link Recommendation} is converted to a JSON object that is written to a file called
   * recommendations.json in the <i>output</i> directory. Presentation processors could be applied to the
   * recommendations.json to generate reports in other formats, such as HTML or PDF.
   *
   * @param assessCommand Assess command containing user input options and arguments
   * @return List of recommendations containing a list of issues by {@link com.ibm.ta.sdk.spi.recommendation.Target}
   * platform
   * @throws TAException If an error occurs when generating recommendations
   */
  List<Recommendation> getRecommendation(CliInputCommand assessCommand) throws TAException;

  /**
   * The Report command is a command with the name {@link CliInputCommand#CMD_REPORT}. It contains the options and
   * arguments that is required by the plug-in to run the report command. The user input values for the options
   * and arguments is passed to the {@link #getReport(String, CliInputCommand)} method when it is invoked.
   *
   * @return Report command including required options and arguments.
   */
  CliInputCommand getReportCommand();

  /**
   * This method is invoked from the {@link CliInputCommand#CMD_REPORT} command. It reads the recommendations.json
   * for an assessment and generates a report from it. Currently only <i>HTML</i> reports are supported. A report
   * is created for each assessment unit in the assessment, and is written to the subdirectory containing all
   * assessment unit artifacts.
   *
   * @param assessmentName Name of the assessment the reports will be generated for
   * @param reportCommand Report command containing user input options and arguments
   * @return List of report generated for the assessment, from the recommendations.json
   * @throws TAException TAException If an error occurs when generating the reports
   */
  List<Report> getReport(String assessmentName, CliInputCommand reportCommand) throws TAException;

  default void validateJsonFiles() throws TAException {
    TaJsonFileValidator.validateIssue(getMiddleware()+"/issue.json");
    TaJsonFileValidator.validateComplexity(getMiddleware()+"/complexity.json");
    TaJsonFileValidator.validateTarget(getMiddleware()+"/target.json");
  }
}
