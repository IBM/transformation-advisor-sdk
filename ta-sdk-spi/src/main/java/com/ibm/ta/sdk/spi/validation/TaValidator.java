/*
 * (C) Copyright IBM Corp. 2019,2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.ta.sdk.spi.validation;

import com.ibm.ta.sdk.spi.plugin.TAException;
import com.ibm.ta.sdk.spi.util.Util;
import org.apache.commons.cli.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TaValidator {

    private static Logger logger = LogManager.getLogger(TaValidator.class.getName());

    private static final String sdk_version = Util.getSDKVersion();

    private static void getHelpPrintOut(Options options) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("ta-validation-" + sdk_version + ".jar [OPTION] [ARGUMENT]", options);
    }

    public static void main(String[] args) {
        CommandLineParser parser = new DefaultParser();
        Options options = CommandLineWrapper.getOptions();

        try {
            CommandLine commandLine = parser.parse(options, args);
            if (commandLine.hasOption("h")) {
                getHelpPrintOut(options);
            } else if (commandLine.hasOption("v")) {
                System.out.println("Current TA Validator version: " + sdk_version);
            } else if (commandLine.hasOption("c")) {
                String jsonFile = commandLine.getOptionValue("c");
                TaJsonFileValidator.validateComplexity(jsonFile);
                System.out.println("The resource " + jsonFile + " is a valid complexity json file. No anomaly were found.");
            } else if (commandLine.hasOption("i")) {
                String jsonFile = commandLine.getOptionValue("i");
                TaJsonFileValidator.validateIssue(jsonFile);
                System.out.println("The resource " + jsonFile + " is a valid issue rule json file. No anomaly were found.");
            } else if (commandLine.hasOption("t")) {
                String jsonFile = commandLine.getOptionValue("t");
                TaJsonFileValidator.validateTarget(jsonFile);
                System.out.println("The resource " + jsonFile + " is a valid target json file. No anomaly were found.");
            } else if (commandLine.hasOption("e")) {
                String jsonFile = commandLine.getOptionValue("e");
                TaJsonFileValidator.validateEnvironment(jsonFile);
                System.out.println("The resource " + jsonFile + " is a valid environment json file. No anomaly were found.");
            } else if (commandLine.hasOption("r")) {
                String jsonFile = commandLine.getOptionValue("r");
                TaJsonFileValidator.validateRecommendation(jsonFile);
                System.out.println("The resource " + jsonFile + " is a valid recommendation json file. No anomaly were found.");
            } else if (commandLine.hasOption("z")) {
                String zipFile = commandLine.getOptionValue("z");
                TaCollectionZipValidator.validateCollectionArchive(zipFile);
                System.out.println("The resource " + zipFile + " is a valid collection archive file. No anomaly were found.");
            } else {
                getHelpPrintOut(options);
            }
        } catch (TAException e) {
            System.err.println(e.getMessage());
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            getHelpPrintOut(options);
        }

    }

}
