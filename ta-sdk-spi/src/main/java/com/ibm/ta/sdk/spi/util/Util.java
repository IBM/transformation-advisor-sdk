/*
 * (C) Copyright IBM Corp. 2019,2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.ta.sdk.spi.util;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveOutputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.apache.commons.compress.utils.IOUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

public class Util {
  public static void zipDir(Path zipOutFile, File zipInDir) throws IOException {
    try (OutputStream fo = Files.newOutputStream(zipOutFile);
         OutputStream gzo = new GzipCompressorOutputStream(fo);
         ArchiveOutputStream o = new TarArchiveOutputStream(gzo)) {
      for (File file : zipInDir.listFiles()) {
        ArchiveEntry entry = o.createArchiveEntry(file, file.getName());
        o.putArchiveEntry(entry);
        if (file.isFile()) {
          try (InputStream i = Files.newInputStream(file.toPath())) {
            IOUtils.copy(i, o);
          }
        }
        o.closeArchiveEntry();
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
