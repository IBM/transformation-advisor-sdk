/*
 * (C) Copyright IBM Corp. 2019,2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.ta.sdk.spi.validation;

import org.apache.commons.cli.*;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.Properties;

public class TaValidator {

    private static Logger logger = Logger.getLogger(TaValidator.class.getName());

    private static String getTaVersion() {
        String version = null;
        final Properties properties = new Properties();
        try {
            properties.load(TaValidator.class.getClassLoader().getResourceAsStream("version.properties"));
            version = properties.getProperty("version");
        } catch (IOException ioe) {
            logger.error("Cannot get TA SDK version.", ioe);
        }
        return version;
    }

    private static void getHelpPrintOut(Options options) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("ta-validation-" + getTaVersion() + ".jar [OPTION] [ARGUMENT]", options);
    }

    public static void main(String[] args) {
        CommandLineParser parser = new DefaultParser();
        Options options = CommandLineWrapper.getOptions();

        try {
            CommandLine commandLine = parser.parse(options, args);
            if (commandLine.hasOption("h")) {
                getHelpPrintOut(options);
            } else if (commandLine.hasOption("v")) {
                System.out.println("Current TA Validator version: " + getTaVersion());
            } else if (commandLine.hasOption("c")) {
                String jsonFile = commandLine.getOptionValue("c");
                if (TaJsonFileValidator.validateComplexity(jsonFile)) {
                    System.out.println("The resource " + jsonFile + " is a valid complexity json file. No anomaly were found.");
                }
            } else if (commandLine.hasOption("i")) {
                String jsonFile = commandLine.getOptionValue("i");
                if (TaJsonFileValidator.validateIssue(jsonFile)) {
                    System.out.println("The resource " + jsonFile + " is a vlid issue rule json file. No anomaly were found.");
                }
            } else if (commandLine.hasOption("t")) {
                String jsonFile = commandLine.getOptionValue("t");
                if (TaJsonFileValidator.validateTarget(jsonFile)) {
                    System.out.println("The resource " + jsonFile + " is a valid target json file. No anomaly were found.");
                }
            } else if (commandLine.hasOption("z")) {
                String zipFile = commandLine.getOptionValue("z");
            } else {
                getHelpPrintOut(options);
            }
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            getHelpPrintOut(options);
        }

    }

}
