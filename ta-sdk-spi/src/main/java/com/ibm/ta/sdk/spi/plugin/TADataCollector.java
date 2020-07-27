/*
 * (C) Copyright IBM Corp. 2019,2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.ta.sdk.spi.plugin;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.ibm.ta.sdk.spi.collect.*;
import com.ibm.ta.sdk.spi.recommendation.Recommendation;
import com.ibm.ta.sdk.spi.assess.RecommendationJson;
import com.ibm.ta.sdk.spi.recommendation.Target;
import com.ibm.ta.sdk.spi.report.Report;
import com.ibm.ta.sdk.spi.util.Util;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.stream.Collectors;

public class TADataCollector {
  private static final String TADATACOLLECTOR_COMMAND_HELP = "Run 'TADataCollector MIDDLEWARE COMMAND --help' for more information on a command.";
  private static final String TADATACOLLECTOR_HELP_USAGE_PREFIX = "Usage: TADataCollector";
  private static final String TADATACOLLECTOR_BASE_HELP_USAGE = TADATACOLLECTOR_HELP_USAGE_PREFIX + " MIDDLEWARE COMMAND [OPTIONS]";
  public static final String ENVIRONMENT_JSON_FILE = "environment.json";
  public static final String RECOMMENDATIONS_JSON_FILE = "recommendations.json";
  public static final String ASSESSMENTUNIT_META_JSON_FILE = ".assessmentUnit.json";

  private static Logger logger = LogManager.getLogger(TADataCollector.class.getName());

  ServiceLoader<PluginProvider> loader = ServiceLoader.load(PluginProvider.class);

  public Iterator<PluginProvider> getPluginProviders() {
    return loader.iterator();
  }

  public void runCommand(String middleware, List<String> cliArguments) throws TAException, IOException {
    PluginProvider provider = getProvider(middleware);
    if (provider == null) {
      throw new IllegalArgumentException("No plug-in provider found for middleware:" + middleware + ".");
    }
    logger.debug("cliArguments:" + cliArguments);
    if (cliArguments.isEmpty()) {
      throw new IllegalArgumentException("No command was specified.");
    }

    List<CliInputCommand> providerCommands = new LinkedList<>();
    providerCommands.add(provider.getCollectCommand());
    CliInputCommand assessCommand = provider.getAssessCommand();
    providerCommands.add(assessCommand);
    providerCommands.add(provider.getReportCommand());

    // Add 'run' command which performs collect, assess, and report
    // The command does not be be provided by the provided, we could re-use the 'assess' command
    CliInputCommand runCommand = new CliInputCommand(CliInputCommand.CMD_RUN, CliInputCommand.CMD_RUN_DESC,
            assessCommand.getOptions(), assessCommand.getCommands(), assessCommand.getArgumentDisplayNames());
    providerCommands.add(runCommand);

    // Help command - lists all top level commands available for middleware
    if (cliArguments.get(0).equals("help")) {
      System.out.println("\n" + getBaseMiddlewareHelp(middleware, providerCommands) + "\n");
      return;
    }

    // Find command and display help for that command
    if (cliArguments.contains("--help") || cliArguments.contains("-h")) {
      CliInputCommand commandHelp = findMatchingCommandForUsageHelp(cliArguments, providerCommands);
      if (commandHelp == null) {
        throw new IllegalArgumentException("Cannot display help for command. Option is not supported for the command.");
      }

      // Display help and exit
      System.out.println("\n" + TADATACOLLECTOR_HELP_USAGE_PREFIX + " " + middleware + " " + commandHelp.getUsageHelp() + "\n\n" + TADATACOLLECTOR_COMMAND_HELP + "\n");
      return;
    }

    // Finding matching command
    CliInputCommand matchedCommand = findMatchingCommand(cliArguments, providerCommands);
    if (matchedCommand == null) {
      throw new IllegalArgumentException("Command is not supported for middleware: " + middleware + ".");
    }

    // Invoke supported commands
    if (CliInputCommand.CMD_COLLECT.equals(matchedCommand.getName())) {
      runCollect(provider, matchedCommand);
    } else if (CliInputCommand.CMD_ASSESS.equals(matchedCommand.getName())) {
      runAssess(provider, matchedCommand);
    } else if (CliInputCommand.CMD_REPORT.equals(matchedCommand.getName())) {
      runReport(provider, matchedCommand);
    } else if (CliInputCommand.CMD_RUN.equals(matchedCommand.getName())) {
      runRun(provider, matchedCommand);
    } else {
      throw new IllegalArgumentException("Command '" + matchedCommand.getName() + "' is not supported.");
    }

    System.out.println("Command '" + matchedCommand.getName() + "' completed successfully.\n");
  }

  public void runCollect(PluginProvider provider, CliInputCommand cliInputCommand) throws TAException, IOException {
    collectData(provider, cliInputCommand);
  }

  private List<DataCollection> collectData(PluginProvider provider, CliInputCommand cliInputCommand) throws TAException, IOException {
    List<DataCollection> dataCollections = provider.getCollection(cliInputCommand);
    for (DataCollection dataCollection : dataCollections) {
      // Get environment
      Environment environment = dataCollection.getEnvironment();

      // Create output dir
      String assessmentName = environment.getConnectionUnitName();
      File outputDir = Util.getAssessmentOutputDir(assessmentName);
      if (!outputDir.exists()) {
        outputDir.mkdirs();
      }

      // Get assessment units
      List<? extends AssessmentUnit> aus = getAssessmentUnits(dataCollection, outputDir);
      List<String> auNameList = aus.stream()
              .map(au -> ((AssessmentUnit) au).getName())
              .collect(Collectors.toList());

      // Write environment json to output dir
      writeEnvironmentJson(environment, provider.getVersion(), auNameList, outputDir);
    }
    return dataCollections;
  }

  private List<? extends AssessmentUnit> getAssessmentUnits(DataCollection dataCollection, File outputDir) throws TAException, IOException {
    List<? extends AssessmentUnit> assessUnits = dataCollection.getAssessmentUnits();
    for (AssessmentUnit au : assessUnits) {
      // Create sub dir for each assessment unit
      File auOutputDir = new File(outputDir, au.getName());
      if (!auOutputDir.exists()) {
        auOutputDir.mkdirs();
      }

      // Write assessment unit data file
      writeAssessmentDataJson(au, auOutputDir);

      // Write assessment unit metadata file
      writeAssessmentUnitMetaJson(au, dataCollection.getEnvironment(), auOutputDir);

      // Copy assessment files to make them available during recommendations
      List<Path> configFiles = au.getConfigFiles();
      if (configFiles != null) {
        List<Path> outputConfigFiles = new LinkedList<>();
        for (Path file : configFiles) {
          File destFile = new File(auOutputDir, file.getFileName().toString());
          Path destPath = destFile.toPath();
          if (!destFile.getParentFile().exists()) {
            destFile.getParentFile().mkdirs();
          }
          Files.copy(file, destPath, StandardCopyOption.REPLACE_EXISTING);

          outputConfigFiles.add(destPath);

          // Add files from directory
          File dirFile = destPath.toFile();
          if (dirFile.isDirectory()) {
            outputConfigFiles.addAll(getFilesFromDir(dirFile));
          }
        }

        // Apply mask to content
        logger.debug("Applying content masks");
        List<ContentMask> contentMasks = au.getContentMasks();
        if (contentMasks != null) {
          for (Path path : outputConfigFiles) {
            for (ContentMask mask : contentMasks) {
              for (String contentMaskFile : mask.getFiles()) {
                // Use the original path of the file, not the new path where the file is copied to
                String origPath = path.toAbsolutePath().toString().replace(auOutputDir.getAbsolutePath(), "");

                logger.debug("Comparing file:" + origPath + " to contentMaskFile:" + contentMaskFile);

                if (origPath.matches(contentMaskFile)) {
                  logger.info("Applying mask to file:" + path);
                  // Read lines from file
                  List<String> lines = Files.readAllLines(path);

                  // Mask file content
                  List<String> updatedLines = mask.mask(lines);

                  // Write updated context to the file
                  Files.write(path, updatedLines);

                  // Move on to the next mask
                  break;
                }
              }
            }
          }
        }

        // Update config files that point to output dir for use in recommendations
        configFiles.clear();
        configFiles.addAll(outputConfigFiles);
      }
    }

    return assessUnits;
  }

  private List<Path> getFilesFromDir(File dir) {
    List<Path> dirFiles = new LinkedList<>();
    if (!dir.isDirectory()) {
      return dirFiles;
    }

    File[] files = dir.listFiles();
    for (File file : files) {
      if (file.isDirectory()) {
        dirFiles.addAll(getFilesFromDir(file));
      } else {
        dirFiles.add(file.toPath());
      }
    }

    return dirFiles;
  }

  public void runAssess(PluginProvider provider, CliInputCommand cliInputCommand) throws TAException, IOException {
    // Run collections
    List<DataCollection> dataCollections = collectData(provider, cliInputCommand);

    // Generate and write recommendations
    List<Recommendation> recs = provider.getRecommendation(cliInputCommand);
    for (Recommendation rec : recs) {
      String assessmentName = rec.getAssessmentName();

      Optional<DataCollection> dcOp = dataCollections.stream()
              .filter(d -> d.getAssessmentName().equals(assessmentName))
              .findFirst();
      if (!dcOp.isPresent()) {
        throw new TAException("Collection not found for assessment:" + assessmentName);
      }

      DataCollection dc = dcOp.get();
      Environment environment =  dc.getEnvironment();
      List<? extends AssessmentUnit> assessUnits = dc.getAssessmentUnits();
      RecommendationJson recJson = new RecommendationJson(rec, environment, assessUnits);
      File outputDir = Util.getAssessmentOutputDir(assessmentName);
      writeRecommendationsJson(recJson, outputDir);

      // zip output dir
      // Only create zip if the data collection does not contain sensitive data
      if (!environment.hasSensitiveData()) {
        String zipFileName = environment.getConnectionUnitName() + ".zip";
        File zipFile = new File(outputDir.getParentFile(), zipFileName);
        Util.zipDir(zipFile.toPath(), outputDir);
      } else {
        logger.info("The enivrnoment.json file indicates that the collection contains sensitive data. No data collection zip archive will be created for collections with sensitive data.");
      }
    }
  }

  public void runReport(PluginProvider provider, CliInputCommand cliInputCommand) throws TAException, IOException {
    // Get a list of assessments by scanning directories in the output directory
    List<String> assessmentNames = new ArrayList<>();
    File outputDir = Util.getOutputDir();
    File[] outputDirFiles = outputDir.listFiles();
    for (File file : outputDirFiles) {
      if (file.isDirectory()) {
        assessmentNames.add(file.getName());
      }
    }
    logger.debug("Generating reports for assessment units:" + assessmentNames);

    // Get report for each assessment
    for (String assessmentName : assessmentNames) {
      List<Report> reports = provider.getReport(assessmentName, cliInputCommand);
      for (Report report : reports) {
        Target target = report.getTarget();
        String reportName = "recommendations_" + target.getLocation() + "_" + target.getPlatform() +
                "." + report.getReportType().toString().toLowerCase();

        // Report path
        File aOutputDir = Util.getAssessmentOutputDir(assessmentName);
        File auOutputDir = new File(aOutputDir, report.getAssessmentUnitName());
        File recFile = new File(auOutputDir, reportName);

        // Write report
        logger.info("Writing report:" + recFile.getAbsolutePath());
        writeFile(recFile, report.getReport());

        // Update assessment unit zip
        String zipFileName = assessmentName + ".zip";
        File zipFile = new File(aOutputDir.getParentFile(), zipFileName);
        if (zipFile.exists()) {
          logger.debug("Updating collection zip archive to include report: " + zipFile.getAbsolutePath());
          Util.zipDir(zipFile.toPath(), aOutputDir);
        }
      }
    }
  }

  public void runRun(PluginProvider provider, CliInputCommand cliInputCommand) throws TAException, IOException {
    runAssess(provider, cliInputCommand);
    runReport(provider, cliInputCommand);
  }

  private CliInputCommand findMatchingCommand(List<String> cliArguments, List<CliInputCommand> providerCommands) {
    logger.debug("cliArguments:" + cliArguments);
    if (cliArguments.size() < 1) {
      return null;
    }

    String cliArg = cliArguments.get(0);
    // stop looking for commands when first arg is found
    if (cliArg.startsWith("-")) {
      return null;
    }

    CliInputCommand matchedCommand = null;
    for (CliInputCommand command : providerCommands) {
      if (command.getName().equals(cliArg)) {
        matchedCommand = new CliInputCommand(command);
        cliArguments.remove(0); // Pop out the first command now that we found a match

        // Process subcommand
        List<CliInputCommand> subCommandsList = command.getCommands();
        if (subCommandsList.size() > 0) {

          CliInputCommand subCommand = findMatchingCommand (cliArguments, subCommandsList);
          logger.debug("Matched subCommand:" + subCommand);
          if (subCommand != null) {
            matchedCommand.setCommands(new ArrayList<>(Arrays.asList(subCommand)));
          }
        }

        if (matchedCommand.getCommands().isEmpty()) {
          logger.debug("No matching subcommands. Adding args and options to command:" + matchedCommand.getName());

          // No more subcommands, this is the last subcommand, add options and arguments to this command
          List<CliInputOption> matchedOptions = command.getMatchedOptions(cliArguments);
          logger.debug("Matched options:" + matchedOptions);

          matchedCommand.setOptions(matchedOptions);

          // Remaining cli arguments are added as arguments to command
          logger.debug("Set arguments:" + cliArguments);
          matchedCommand.setArguments(cliArguments);
        }

        break;
      }
    }
    return matchedCommand;
  }

  // Finds the last subcommand that matches the commands in the CLI arguments. This CliInputCommand is used to display help.
  private CliInputCommand findMatchingCommandForUsageHelp(List<String> cliArguments, List<CliInputCommand> providerCommands) {
    if (cliArguments.size() < 1) {
      return null;
    }

    String cliArg = cliArguments.get(0);
    logger.debug("Comparing cliArg:" + cliArg);
    // stop looking for commands when first arg is found
    if (cliArg.startsWith("-")) {
      return null;
    }

    for (CliInputCommand command : providerCommands) {
      if (command.getName().equals(cliArg)) {
        cliArguments.remove(0); // Pop out the first command now that we found a match

        // Process subcommand
        List<CliInputCommand> subCommandsList = command.getCommands();
        if (subCommandsList.size() > 0) {
          CliInputCommand subCommand = findMatchingCommandForUsageHelp (cliArguments, subCommandsList);
          logger.debug("subCommand:" + subCommand);

          if (subCommand != null) {
            return subCommand;
          }
        }

        return command;
      }
    }
    return null;
  }

  /*
   * Base help for a specific middleware. Lists all commands a available for that middleware.
   */
  private String getBaseMiddlewareHelp(String middleware, List<CliInputCommand> providerCommands) {
    String usage = TADATACOLLECTOR_HELP_USAGE_PREFIX + " " + middleware + " COMMAND [OPTIONS]\n\n";
    usage += "Commands:\n";

    for (CliInputCommand command : providerCommands) {
      usage += "  " +  String.format("%1$-" + 15 + "s", command.getName()) + command.getDescription() + "\n";
    }
    usage += "\n\n" + TADATACOLLECTOR_COMMAND_HELP;

    return usage;
  }

  /*
   * Base help for TADataCollector. Lists all middlewares available to help the user start.
   */
  private static String getBaseHelp() {
    Iterator<PluginProvider> itRecP = new TADataCollector().getPluginProviders();
    if (!itRecP.hasNext()) {
      throw new TARuntimeException("No plug-in available. Add a plug-in to the classpath and run TADataCollector again.");
    }

    String middleware = null;
    while (itRecP.hasNext()) {
      PluginProvider recProvider = itRecP.next();
      if (middleware == null) {
        middleware = recProvider.getMiddleware();
      } else {
        middleware += " | " + recProvider.getMiddleware();
      }
    }

    String usage = TADATACOLLECTOR_BASE_HELP_USAGE + "\n\n";
    usage += "Middleware:\n" + "  Plug-ins available for these middleware [ " + middleware + " ]\n\n";
    usage += "Commands:\n" + "  help      Get information on the commands and options available for a middleware";

    return usage;
  }

  // Check cli input args against list of args from plugin provider
  private static void validateCliArgs(List<CliInputOption> providerArgs, Properties cliInputProps) {
    for (CliInputOption providerArg : providerArgs) {

    }
  }

  private String getJsonStr(Object recJson) {
    GsonBuilder builder = new GsonBuilder();
    builder.excludeFieldsWithoutExposeAnnotation();
    builder.setPrettyPrinting();

    Gson gson = builder.create();
    String issuesJsonString = gson.toJson(recJson);
    return issuesJsonString;
  }

  private void writeRecommendationsJson(RecommendationJson recJson, File outputDir) throws TAException {
    File rjFile = new File(outputDir, RECOMMENDATIONS_JSON_FILE);
    if (rjFile.exists()) {
      rjFile.delete();
    }

    // Convert recommendation to JSON
    String recJsonStr = getJsonStr(recJson);
    writeFile(rjFile, recJsonStr);
  }

  private void writeAssessmentDataJson(AssessmentUnit au, File outputDir) throws TAException {
    File auFile = new File(outputDir, au.getName() + ".json");
    if (auFile.exists()) {
      auFile.delete();
    }
    logger.debug("Writing assessment unit data json file:" + auFile);

    // Convert recommendation to JSON
    String auJsonStr = getJsonStr(au.getAssessmentData());
    writeFile(auFile, auJsonStr);
  }


  private void writeEnvironmentJson(Environment environment, String version, List<String> auNameList, File outputDir) throws TAException {
    File envFile = new File(outputDir, ENVIRONMENT_JSON_FILE);
    if (envFile.exists()) {
      envFile.delete();
    }
    logger.debug("Writing env file:" + envFile);

    EnvironmentJson envJson = new EnvironmentJson(environment);
    envJson.setPluginVersion(version);
    envJson.setAssessmentUnits(auNameList);
    String envJsonStr = getJsonStr(envJson);
    writeFile(envFile, envJsonStr);
  }

  private void writeAssessmentUnitMetaJson(AssessmentUnit au, Environment environment, File outputDir) throws TAException {
    File auMetaFile = new File(outputDir, au.getName() + ASSESSMENTUNIT_META_JSON_FILE);
    if (auMetaFile.exists()) {
      auMetaFile.delete();
    }
    logger.debug("Writing assessment unit metadata json file:" + auMetaFile);

    AssessmentUnitMetadataJson auMeta = new AssessmentUnitMetadataJson(environment, au.getName());
    String auMetaJsonStr = getJsonStr(auMeta.toJsonObject());
    writeFile(auMetaFile, auMetaJsonStr);
  }

  private static void writeFile(File file, String content) throws TAException {
    writeFile(file, content.getBytes());
  }

  private static void writeFile(File file, byte[] content) throws TAException {
    try {
      Files.write(file.toPath(), content);
    } catch (IOException e) {
      throw new TAException("Error writing file:" + file.getAbsolutePath(), e);
    }
  }

  private PluginProvider getProvider(String middleware) {
    Iterator<PluginProvider> itRecP = getPluginProviders();
    while (itRecP.hasNext()) {
      PluginProvider recProvider = itRecP.next();

      // Find provider that matches domain
      if (recProvider.getMiddleware().equals(middleware)) {
        try {
          recProvider.validateJsonFiles();
        } catch (TAException e) {
          recProvider = null;
        }
        return recProvider;
      }
    }
    return null;
  }

  private static void processInput(String[] args) {
    Properties cliArgs = new Properties();
    List<String> cliCommands = new LinkedList<>();

    for (int i = 0; i < args.length; i++) {
      switch (args[i].charAt(0)) {
        case '-':
          String argKey = args[i];
          String argValue = "";
          if (i+1 < args.length) {
            if (!args[i + 1].startsWith("-")) {
              argValue = args[++i];
            }
          }

          if (argKey.startsWith("--")) {
            argKey = argKey.replaceFirst("--", "");
            if (argKey.length() <= 1) {
              throw new IllegalArgumentException("Invalid argument '--" + argKey + "'. Use the help command to get usage information.");
            }
          } else if (argKey.startsWith("-")) {
            argKey = argKey.replaceFirst("-", "");
            if (argKey.length() != 1) {
              throw new IllegalArgumentException("Invalid argument '-" + argKey + "'. Use the help command to get usage information.");
            }
          }

          cliArgs.setProperty(argKey, argValue);
          break;
        default:
          cliCommands.add(args[i]);
      }
    }
  }

  public static void main(String[] args) {
    List<String> cliCommands = new LinkedList<>();

    for (int i = 0; i < args.length; i++) {
      cliCommands.add(args[i]);
    }

    // No args at all - display help and exit
    if (cliCommands.isEmpty()) {
      System.out.println(getBaseHelp());
      return;
    }

    // First arg is not middleware
    String middleware = cliCommands.get(0);
    if (middleware.startsWith("-")) {
      System.out.println("\n" + getBaseHelp() + "\n");
      return;
    }
    cliCommands.remove(0); // Pop out middleware from CLI args

    try {
      new TADataCollector().runCommand(middleware, cliCommands);
    } catch (IllegalArgumentException e) {
      System.out.println(e.getMessage() + "\n\n" + getBaseHelp() + "\n");
    } catch (TAException tae) {
      logger.error("Fail to run the command:", tae);
      System.out.println("Fail to run the command, check log file for detail information.\n    " +tae.getMessage() +  "\n");
    } catch (IOException ioe) {
      logger.error("Fail to run the command:", ioe);
      System.out.println("Fail to run the command, check log file for detail information.\n    " +ioe.getMessage() +  "\n");
    }
  }
}
