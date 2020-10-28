/*
 * (C) Copyright IBM Corp. 2019,2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.ta.sdk.spi.util;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ibm.ta.sdk.spi.plugin.TADataCollector;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import static java.nio.file.Files.copy;

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

  public static void zipCollection(Path zipOutFile, File zipInDir, boolean excludeData) throws IOException  {
    ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipOutFile.toFile()));
    zos.putNextEntry(new ZipEntry(zipInDir.getName() + ZIP_FILE_SEPARATOR));
    zos.closeEntry();

    // Add subdir files
    String parentDir = zipInDir.getName();
    for (File dirFile : zipInDir.listFiles()) {
      if (excludeData) {
        // If plugin has sensitive data, include only the metadata.assessmentunit.json and reports HTML files in
        // each assessment unit dir
        addZipEntry(dirFile, parentDir, zos, Arrays.asList(new String[]{
                TADataCollector.ASSESSMENTUNIT_META_JSON_FILE,
                "recommendations_.*.html"}));
      } else {
        addZipEntry(dirFile, parentDir, zos, null);
      }
    }

    zos.finish();
    zos.close();
  }

  public static void zipDir(Path zipOutFile, File zipInDir) throws IOException {
    ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipOutFile.toFile()));
    addZipEntry(zipInDir, null, zos, null);
    zos.finish();
    zos.close();
  }

  private static void addZipEntry(File file, String parentDir, ZipOutputStream zos, List<String> includeFileList) throws IOException {
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
        if (includeFileList == null || hasMatchingFileName(includeFileList, dirFile.getName())) {
          addZipEntry(dirFile, parentDir, zos, includeFileList);
        }
      }
    }
  }

  private static boolean hasMatchingFileName(List<String> files, String fileName) {
    for (String file : files) {
      if (fileName.matches(file)) {
        return true;
      }
    }
    return false;
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

  public static void copyResourceToDir (String resource, File outputDir) throws IOException {
    logger.debug("resource="+resource);
    URL resourceURL = Util.class.getClassLoader().getResource(resource);
    logger.debug("resourceURL="+resourceURL);
    if (resourceURL.getProtocol().equals("jar")) {
      String jarPath = resourceURL.getPath().substring(5, resourceURL.getPath().indexOf("!")); //strip out only the JAR file
      logger.debug("jarPath="+jarPath);
      JarFile jar = new JarFile(URLDecoder.decode(jarPath, "UTF-8"));
      Enumeration<JarEntry> entries = jar.entries(); //gives ALL entries in jar
      while(entries.hasMoreElements()) {
        JarEntry entry = entries.nextElement();
        String name =entry.getName();
        if (name.startsWith(resource)) {
          logger.debug("template-name="+name);
          File targetFile = new File (outputDir.getCanonicalPath()+File.separator+name);
          logger.debug("targetFile="+targetFile.getPath());
          if (!entry.isDirectory()) {
            InputStream entryInputStream = null;
            try {
              entryInputStream = jar.getInputStream(entry);
              FileUtils.copyInputStreamToFile(entryInputStream, targetFile);
            } finally {
              entryInputStream.close();
            }
          } else {
            targetFile.mkdirs();
          }
        }
      }
    } else if (resourceURL.getProtocol().equals("file")) {
      Path source = null;
      try {
        source = Paths.get(resourceURL.toURI());
      } catch (URISyntaxException e) {
        logger.error("Failed to get template directory from path", e);
        return;
      }
      Path target = new File (outputDir.getCanonicalPath()+File.separator+resource).toPath();
      Path finalSource = source;
      Files.walkFileTree(finalSource, new SimpleFileVisitor<Path>() {
        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                throws IOException {
          Path dstFilePath = target.resolve(finalSource.relativize(file));
          if(!dstFilePath.toFile().getParentFile().exists()){
            dstFilePath.toFile().getParentFile().mkdirs();
          }
          copy(file, target.resolve(finalSource.relativize(file)), StandardCopyOption.REPLACE_EXISTING);
          return FileVisitResult.CONTINUE;
        }
      });
    }
  }
}
