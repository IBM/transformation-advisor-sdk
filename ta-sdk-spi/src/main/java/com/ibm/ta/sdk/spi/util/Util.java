/*
 * (C) Copyright IBM Corp. 2019,2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.ta.sdk.spi.util;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.file.Path;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class Util {

  private static final String ZIP_FILE_SEPARATOR = "/"; // File.separator should not be used in zips
  private static Logger logger = LogManager.getLogger(Util.class.getName());

  public static String getSDKVersion() {
    String version = null;
    final Properties properties = new Properties();
    try {
      properties.load(Util.class.getClassLoader().getResourceAsStream("version.properties"));
      version = properties.getProperty("version");
    } catch (IOException ioe) {
      logger.error("Cannot get TA SDK version.", ioe);
    }
    return version;
  }
  public static void zipDir(Path zipOutFile, File zipInDir) throws IOException {
    ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipOutFile.toFile()));
    addZipEntry(zipInDir, null, zos);
    zos.finish();
    zos.close();
  }

  private static void addZipEntry(File file, String parentDir, ZipOutputStream zos) throws IOException {
    // Add current file/dir
    zos.putNextEntry(new ZipEntry(
            (parentDir != null ? parentDir + ZIP_FILE_SEPARATOR : "")  // Do not start with leading /
                    + file.getName()
                    + (file.isDirectory() ? ZIP_FILE_SEPARATOR : "")  // Add trailing / to directories
    ));
    if (file.isFile()) {
      IOUtils.copy(new FileInputStream(file), zos);
    }
    zos.closeEntry();

    // Add subdir files
    if (file.isDirectory()) {
      parentDir = (parentDir != null ? parentDir + ZIP_FILE_SEPARATOR : "") + file.getName();
      for (File dirFile : file.listFiles()) {
        addZipEntry(dirFile, parentDir, zos);
      }
    }
  }

  public static File getOutputDir() {
    String cwd = System.getProperty("user.dir");
    return new File(cwd, "output");
  }

  public static File getAssessmentOutputDir(String assessmentName) {
    File outputDir = getOutputDir();
    return new File(outputDir, assessmentName);
  }

  /**
   * Reads the recommendations json from the <i>output</i> directory for an assessment name and assessmentUnit name.
   *
   * @param assessmentName Name of the assessment
   * @return JsonObject for the recommendations.json
   * @throws FileNotFoundException Exception is thrown if recommendations artifacts are not found in the output directory
   */
  public static JsonObject getRecommendationsJson(String assessmentName) throws FileNotFoundException {
    String cwd = System.getProperty("user.dir");
    File outputDir = new File(cwd, "output");
    outputDir = new File(outputDir, assessmentName);
    if (!outputDir.exists()) {
      throw new FileNotFoundException("No directory found in output for the assessment name:" + assessmentName);
    }

    File recFile = new File(outputDir, "recommendations.json");
    if (!recFile.exists()) {
      throw new FileNotFoundException("No recommendations.json found in output for assessment '" + assessmentName + "'");
    }

    return new JsonParser().parse(new FileReader(recFile)).getAsJsonObject();
  }
}
