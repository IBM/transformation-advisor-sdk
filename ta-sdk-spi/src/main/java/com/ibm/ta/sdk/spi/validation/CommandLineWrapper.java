/*
 * (C) Copyright IBM Corp. 2019,2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.ta.sdk.spi.validation;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

public class CommandLineWrapper {

    private static Options options = new Options();

    static {
        Option issueOption = Option
                .builder("i")
                .longOpt("issue")
                .hasArg()
                .desc("Validate issue rule json file.")
                .build();

        Option versionOption = Option
                .builder("v")
                .longOpt("version")
                .desc("Get the version of the TA validator")
                .build();

        Option helpOption = Option
                .builder("h")
                .longOpt("help")
                .desc("Get to start to use the TA validator command line tool")
                .build();

        Option targetOption = Option
                .builder("t")
                .longOpt("target")
                .hasArg()
                .desc("Validate target json file")
                .build();

        Option complexOption = Option
                .builder("c")
                .longOpt("complexity")
                .hasArg()
                .desc("Validate complexity json file")
                .build();

        Option recommOption = Option
                .builder("r")
                .longOpt("recommendation")
                .hasArg()
                .desc("Validate recommendation json file")
                .build();

        Option zipOption = Option
                .builder("z")
                .longOpt("collection")
                .hasArg()
                .desc("Validate collection zip file")
                .build();
        Option envOption = Option
                .builder("e")
                .longOpt("environment")
                .hasArg()
                .desc("Validate environment json file")
                .build();

        Option recOption = Option
                .builder("r")
                .longOpt("recommendation")
                .hasArg()
                .desc("Validate recommendation json file")
                .build();


        options
                .addOption(issueOption)
                .addOption(versionOption)
                .addOption(helpOption)
                .addOption(complexOption)
                .addOption(recommOption)
                .addOption(targetOption)
                .addOption(zipOption)
                .addOption(envOption)
                .addOption(recommOption);
    }

    private CommandLineWrapper() {
    }

    static Options getOptions() {
        // Required options
        return options;
    }
}
