/*
 * (C) Copyright IBM Corp. 2019,2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.ta.sdk.spi.plugin;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CliInputCommand {
  private static Logger logger = LogManager.getLogger(CliInputCommand.class.getName());

  // Names of supported Commands
  public static final String CMD_COLLECT      = "collect";
  public static final String CMD_COLLECT_DESC = "Performs data collection";
  public static final String CMD_ASSESS       = "assess";
  public static final String CMD_ASSESS_DESC  = "Performs data collection and assessment";
  public static final String CMD_REPORT       = "report";
  public static final String CMD_REPORT_DESC  = "Generates reports";
  public static final String CMD_RUN          = "run";
  public static final String CMD_RUN_DESC     = "Performs data collection, assessment, and generate reports";

  private String name;
  private String description;
  private List<CliInputOption> options;
  private List<CliInputCommand> commands;
  private List<String> argumentDisplayNames; // display name for the arguments, used for usage help
  private List<String> arguments;
  private CliInputCommand parentCommand; // reference to parent command for reverse navigation


  public CliInputCommand(String name, String description, List<CliInputOption> options, List<CliInputCommand> commands, List<String> argumentDisplayNames) {
    this.name = name;
    this.description = description;
    if (options == null) {
      options = new ArrayList<>();
    }
    this.options = options;
    setCommands(commands);
    if (argumentDisplayNames ==null) {
      argumentDisplayNames = new ArrayList<>();
    }
    this.argumentDisplayNames = argumentDisplayNames;
  }

  protected CliInputCommand(CliInputCommand cmd) {
    this(cmd.name, cmd.description, null, null, cmd.argumentDisplayNames);
  }

  public String getName() {
    return name;
  }

  public String getDescription() {
    return description;
  }

  public List<CliInputOption> getOptions() {
    return options;
  }

  public void setOptions(List<CliInputOption> options) {
    this.options = options;
  }

  public List<CliInputCommand> getCommands() {
    return commands;
  }

  public void setCommands(List<CliInputCommand> commands) {
    if (commands == null) {
      commands = new ArrayList<>();
    }
    for (CliInputCommand command : commands) {
      command.parentCommand = this;
    }

    this.commands = commands;
  }

  public List<String> getArgumentDisplayNames() {
    return argumentDisplayNames;
  }

  public List<String> getArguments() {
    return arguments;
  }

  public void setArguments(List<String> arguments) {
    logger.debug("arguments:" + arguments + ", argumentDisplayNames:" + argumentDisplayNames);
    if (arguments.size() != argumentDisplayNames.size()) {
      throw new IllegalArgumentException("Invalid arguments '" + arguments + "' for command '" + name + "'. Expects arguments '" + argumentDisplayNames + "'.");
    }
    this.arguments = arguments;
  }

  public CliInputCommand getParentCommand() {
    return parentCommand;
  }

  public String getUsageHelp() {
    String usage = "";
    for (CliInputCommand c = this; c != null; c = c.parentCommand) {
      usage = c.getName() + (usage.length()==0 ? "" : " ") + usage;
    }

    String usageCommands = null;
    if (!commands.isEmpty()) {
      usage += " COMMMAND";

      usageCommands = "\nCommands:\n";
      for (CliInputCommand c : commands) {
        usageCommands += "  " +  String.format("%1$-" + 15 + "s", c.getName()) + c.getDescription() + "\n";
      }
    }

    String usageOptions = null;
    if (!options.isEmpty()) {
      usage += " [OPTIONS]";

      usageOptions = "\nOptions:\n";
      for (CliInputOption o : options) {
        usageOptions += "  " + o.getUsageHelp() + "\n";
      }
    }

    if (!argumentDisplayNames.isEmpty()) {
      for (String arg : argumentDisplayNames) {
        usage += " " + arg;
      }
    }

    if (usageCommands != null) {
      usage += "\n" + usageCommands;
    }
    if (usageOptions != null) {
      usage += "\n" + usageOptions;
    }

    return usage;
  }

  /**
   * Performing matching of input arguments with the list of supported options for a command.
   * If a match is found, create a new instance of the CliInputOption and initialize the value.
   * @param inputArgs Input arguments provided by the user in the CLI
   * @return List of CliInputOption that matches the input arguments
   */
  public List<CliInputOption> getMatchedOptions(List<String> inputArgs) {
    logger.debug("Find matching options for inputArgs:" + inputArgs);
    List<CliInputOption> matchedOptions = new ArrayList<>();
    if (options.isEmpty()) {
      return matchedOptions;
    }

    // track options that matched and remove them at the end
    List<Integer> matchedArgIndexes = new ArrayList<>();

    for (int i=0; i < inputArgs.size(); i++) {
      String arg = inputArgs.get(i);
      if (!arg.startsWith("-")) {
        continue;
      }

      CliInputOption matchedOption = null;
      for (CliInputOption option : options) {
        String argName, optionName;
        if (arg.startsWith("--")) {
          // long args
          argName = arg.substring(2);
          optionName = option.getLongArg();
        } else {
          // short args
          argName = arg.substring(1);
          optionName = option.getShortArg();
        }

        if (argName.equals(optionName)) {
          logger.debug("Option match found, argName=" + argName + ", optionName=" + optionName);
          matchedOption = new CliInputOption(option);
          matchedArgIndexes.add(i);
          break;
        }
      }

      // Found matching option
      if (matchedOption != null) {
        // Initial value if required
        if (matchedOption.acceptsValue()) {
          String argValue = null;
          if (i+1 >= inputArgs.size()) {
            if (matchedOption.requiresValue() && matchedOption.getValue() == null) {
              throw new IllegalArgumentException("Value required for option '" + arg + "'.");
            }
          } else {
            argValue = inputArgs.get(i+1);
          }

          if (argValue.startsWith("-")) {
            if (matchedOption.requiresValue() && matchedOption.getValue() == null) {
              throw new IllegalArgumentException("Invalid value specified for option '" + arg + "', value= '" + argValue + "'.");
            }
            argValue = null;
          }

          // Add value to option
          if (argValue != null) {
            i++; // Increment i to skip processing of next cli arg since it is a value for this option
            matchedArgIndexes.add(i);
            matchedOption.setValue(argValue);
          }
        }

        matchedOptions.add(matchedOption);
      } else {
        throw new IllegalArgumentException("Invalid option '" + arg + "'. Option is not supported.");
      }
    }

    // Remove from inputArgs list the args that matched options
    Collections.reverse(matchedArgIndexes);
    logger.debug("Removing matched indexes from input args:" + matchedArgIndexes);
    for (Integer i : matchedArgIndexes) {
      inputArgs.remove(i.intValue());
    }

    return matchedOptions;
  }

  @Override
  public String toString() {
    return "name=" + name + ", description=" + description + "\nCommands:\n" + commands.toString() + "\nOptions:\n" + options.toString();
  }
}
