package com.ibm.ta.sdk.core.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class CommandInvoker {
    public static CommandReturn execude (String command) throws IOException, InterruptedException {
        if (System.getProperty("local.test") == null) {
            Process proc = Runtime.getRuntime().exec(command);
            ReadStream inputStream = new ReadStream("stdin", proc.getInputStream());
            ReadStream errorStream = new ReadStream("stdin", proc.getErrorStream());
            /* start the stream threads */
            inputStream.start();
            errorStream.start();
            proc.waitFor();
            return new CommandReturn(proc.exitValue(), inputStream.getMsg(), errorStream.getMsg());
        } else {
            InputStream inputS = CommandInvoker.class.getClassLoader().getResourceAsStream("command_output.properties");
            Properties prop = new Properties();
            prop.load(inputS);
            String propName = command.split(" ")[0];
            if (propName.contains(File.separator)) {
                propName = propName.substring(propName.lastIndexOf(File.separator) + 1);
            }
            String outPut = prop.getProperty(propName);
            return new CommandReturn(0, outPut.substring(1, outPut.length() - 1), "");
        }
    }
}
