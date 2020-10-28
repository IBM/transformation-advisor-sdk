
/*
 * (C) Copyright IBM Corp. 2019,2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.ta.sdk.sample.test;

import com.ibm.ta.sdk.core.util.Constants;
import com.ibm.ta.sdk.core.util.GenericUtil;
import com.ibm.ta.sdk.spi.plugin.TADataCollector;
import com.ibm.ta.sdk.spi.plugin.TAException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import com.ibm.ta.sdk.spi.util.Util;
import com.ibm.ta.sdk.spi.validation.TaCollectionZipValidator;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class SamplePluginProviderTest {

    static final String MIDDLEWARE_NAME = "middleware";
    static final String COLLECTION_UNIT_NAME = "collection1";
    static final String ASSESS_UNIT_NAME = "assessmentUnit1";
    @BeforeEach
    void cleanOutpotDir() throws IOException {
        Path outputDir = Util.getOutputDir().toPath();
        if (outputDir.toFile().exists()) {
            Files.walk(outputDir)
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        }
    }

    @Test
    public void collectCommandTest() throws TAException, IOException {
        List<String> argus = new ArrayList<>();
        argus.add("collect");
        argus.add("test");
        //argus.add("test");
        TADataCollector tadc = new TADataCollector();
        tadc.runCommand(MIDDLEWARE_NAME, argus);
        File outputDir = Util.getOutputDir();
        File[] directories = outputDir.listFiles(File::isDirectory);
        assertEquals(directories.length, 2);
        //assertEquals(directories[0].getName(), COLLECTION_UNIT_NAME);
        File envFile = new File (directories[0]+File.separator+ Constants.ENVIRONMENT_JSON);
        assertTrue(envFile.exists());
        assertTrue(envFile.isFile());
        assertTrue(envFile.length() > 2);
        File recommFile = new File (directories[0]+File.separator+ Constants.RECOMMENDATIONS_JSON);
        assertFalse(recommFile.exists());
    }

    @Test
    public void assessCommandTest() throws TAException, IOException {
        List<String> argus = new ArrayList<>();
        argus.add("assess");
        argus.add("test");
        //argus.add("test");
        TADataCollector tadc = new TADataCollector();
        tadc.runCommand(MIDDLEWARE_NAME, argus);
        File outputDir = Util.getOutputDir();
        File[] directories = outputDir.listFiles(File::isDirectory);
        assertEquals(directories.length, 2);
        //assertEquals(directories[0].getName(), COLLECTION_UNIT_NAME);
        File envFile = new File (directories[0]+File.separator+ Constants.ENVIRONMENT_JSON);
        assertTrue(envFile.exists());
        assertTrue(envFile.isFile());
        assertTrue(envFile.length() > 2);
        File recommFile = new File (directories[0]+File.separator+ Constants.RECOMMENDATIONS_JSON);
        assertTrue(recommFile.exists());
        assertTrue(recommFile.isFile());
        assertTrue(recommFile.length() > 2);
        File targetsFile = new File (directories[0]+File.separator+MIDDLEWARE_NAME+File.separator+Constants.FILE_TARGETS_JSON);
        assertTrue(targetsFile.exists());
        assertTrue(targetsFile.isFile());
        assertTrue(targetsFile.length() > 2);
        File templateFile = new File (directories[0]+File.separator+MIDDLEWARE_NAME+File.separator+"templates"+File.separator+"targetA"+File.separator+"pom.xml.ftl");
        assertTrue(templateFile.exists());
        assertTrue(templateFile.isFile());
        assertTrue(templateFile.length()>2);
    }

    @Test
    public void runCommandTest() throws TAException, IOException {
        List<String> argus = new ArrayList<>();
        argus.add("run");
        argus.add("test");
        //argus.add("test");
        TADataCollector tadc = new TADataCollector();
        tadc.runCommand(MIDDLEWARE_NAME, argus);
        File outputDir = Util.getOutputDir();
        File[] directories = outputDir.listFiles(File::isDirectory);
        assertEquals(directories.length, 2);
        //assertEquals(directories[0].getName(), COLLECTION_UNIT_NAME);
        File envFile = new File (directories[0]+File.separator+ Constants.ENVIRONMENT_JSON);
        assertTrue(envFile.exists());
        assertTrue(envFile.isFile());
        assertTrue(envFile.length() > 2);
        File recommFile = new File (directories[0]+File.separator+ Constants.RECOMMENDATIONS_JSON);
        assertTrue(recommFile.exists());
        assertTrue(recommFile.isFile());
        assertTrue(recommFile.length() > 2);
        File reportFile = new File (outputDir+File.separator+COLLECTION_UNIT_NAME+File.separator+ASSESS_UNIT_NAME+"/recommendations_targetA.html");
        assertTrue(reportFile.exists());
        assertTrue(reportFile.isFile());
        assertTrue(reportFile.length() > 2);
        File collectionFile = new File (outputDir+File.separator+COLLECTION_UNIT_NAME+".zip");
        assertTrue(collectionFile.exists());
        assertTrue(collectionFile.isFile());
        assertTrue(collectionFile.length() > 2);
        File targetsFile = new File (directories[0]+File.separator+MIDDLEWARE_NAME+File.separator+Constants.FILE_TARGETS_JSON);
        assertTrue(targetsFile.exists());
        assertTrue(targetsFile.isFile());
        assertTrue(targetsFile.length() > 2);
        File templateFile = new File (directories[0]+File.separator+MIDDLEWARE_NAME+File.separator+"templates"+File.separator+"targetA"+File.separator+"pom.xml.ftl");
        assertTrue(templateFile.exists());
        assertTrue(templateFile.isFile());
        assertTrue(templateFile.length()>2);
    }

    @Test
    public void migrateCommandTest() throws TAException, IOException {
        List<String> argus = new ArrayList<>();
        argus.add("run");
        argus.add("test");
        //argus.add("test");
        TADataCollector tadc = new TADataCollector();
        tadc.runCommand(MIDDLEWARE_NAME, argus);
        List<String> migrateArgus = new ArrayList<>();
        migrateArgus.add("migrate");
        migrateArgus.add("./output/"+COLLECTION_UNIT_NAME);
        tadc.runCommand(MIDDLEWARE_NAME, migrateArgus);
        File outputDir = Util.getOutputDir();
        String migrationBundleDir = outputDir+File.separator+COLLECTION_UNIT_NAME+File.separator+ASSESS_UNIT_NAME+"/migrationBundle/";
        File bundleZipFile = new File (migrationBundleDir+ASSESS_UNIT_NAME+"_targetA.zip");
        assertTrue(bundleZipFile.exists());
        assertTrue(bundleZipFile.isFile());
        assertTrue(bundleZipFile.length() > 2);
        bundleZipFile = new File (migrationBundleDir+ASSESS_UNIT_NAME+"_targetB.zip");
        assertTrue(bundleZipFile.exists());
        assertTrue(bundleZipFile.isFile());
        assertTrue(bundleZipFile.length() > 2);
        File bundleDir = new File (migrationBundleDir+"targetA/");
        assertTrue(bundleDir.exists());
        assertTrue(bundleDir.isDirectory());
        File pomFile = new File(bundleDir.getAbsolutePath()+File.separator+"pom.xml");
        assertTrue(pomFile.exists());
        assertTrue(pomFile.isFile());
        assertTrue(pomFile.length() > 2);
        String pomFileContent = GenericUtil.readFileToString(pomFile.toPath());
        assertFalse(pomFileContent.contains("[="));
        assertTrue(pomFileContent.contains("project.artifactId"));
        assertTrue(pomFileContent.contains(ASSESS_UNIT_NAME));
        File dockerFile = new File(bundleDir.getAbsolutePath()+File.separator+"Dockerfile");
        assertTrue(dockerFile.exists());
        assertTrue(dockerFile.isFile());
        assertTrue(dockerFile.length() > 2);
        String dockerFileContent = GenericUtil.readFileToString(dockerFile.toPath());
        assertFalse(dockerFileContent.contains("[="));
        assertTrue(dockerFileContent.contains("2.1"));
        assertTrue(dockerFileContent.contains(ASSESS_UNIT_NAME));
        File jvmFile = new File(bundleDir.getAbsolutePath()+File.separator+"jvm.options");
        assertTrue(jvmFile.exists());
        assertTrue(jvmFile.isFile());
        assertTrue(jvmFile.length() > 2);
        String jvmFileContent = GenericUtil.readFileToString(jvmFile.toPath());
        assertFalse(jvmFileContent.contains("[="));
        assertTrue(jvmFileContent.contains("-Duser.country=CA"));
        File serverXmlFile = new File(bundleDir.getAbsolutePath()+File.separator+"server.xml");
        assertTrue(serverXmlFile.exists());
        assertTrue(serverXmlFile.isFile());
        assertTrue(serverXmlFile.length() > 2);
        File serverPyFile = new File(bundleDir.getAbsolutePath()+File.separator+"server_config.py");
        assertTrue(serverPyFile.exists());
        assertTrue(serverPyFile.isFile());
        assertTrue(serverPyFile.length() > 2);
    }

    @Test
    public void migrateCommandNoCollectionTest() throws TAException, IOException {
        TADataCollector tadc = new TADataCollector();
        List<String> nodirArgus = new ArrayList<>();
        nodirArgus.add("migrate");
        nodirArgus.add("./output/"+COLLECTION_UNIT_NAME);
        Exception exception = assertThrows(TAException.class, () -> {
            tadc.runCommand(MIDDLEWARE_NAME, nodirArgus);
        });
        assertTrue(exception.getMessage().contains("collectionDir is not a directory"));
        List<String> noEnvArgus = new ArrayList<>();
        noEnvArgus.add("migrate");
        noEnvArgus.add("./");
        exception = assertThrows(TAException.class, () -> {
            tadc.runCommand(MIDDLEWARE_NAME, noEnvArgus);
        });
        assertTrue(exception.getMessage().contains("cannot find the environment.json file"));
    }

    /**
     * Run migrate command with target option for 'targetA' only.
     * Verify only migration bundle for 'targetA' is created.
     */
    @Test
    public void migrateCommandTargetOptionTest() throws TAException, IOException {
        List<String> argus = new ArrayList<>();
        argus.add("run");
        argus.add("test");
        //argus.add("test");
        TADataCollector tadc = new TADataCollector();
        tadc.runCommand(MIDDLEWARE_NAME, argus);
        List<String> migrateArgus = new ArrayList<>();
        migrateArgus.add("migrate");
        migrateArgus.add("--target");
        migrateArgus.add("targetA");
        migrateArgus.add("./output/"+COLLECTION_UNIT_NAME);
        tadc.runCommand(MIDDLEWARE_NAME, migrateArgus);
        File outputDir = Util.getOutputDir();
        String migrationBundleDir = outputDir+File.separator+COLLECTION_UNIT_NAME+File.separator+ASSESS_UNIT_NAME+"/migrationBundle/";
        File bundleZipFile = new File (migrationBundleDir+ASSESS_UNIT_NAME+"_targetA.zip");
        assertTrue(bundleZipFile.exists());
        bundleZipFile = new File (migrationBundleDir+ASSESS_UNIT_NAME+"_targetB.zip");
        assertTrue(!bundleZipFile.exists());
    }

    @Test
    public void collectionZipFileValidationTest() throws TAException, IOException {
        List<String> argus = new ArrayList<>();
        argus.add("run");
        argus.add("test");
        //argus.add("test");
        TADataCollector tadc = new TADataCollector();
        tadc.runCommand(MIDDLEWARE_NAME, argus);
        try {
            TaCollectionZipValidator.validateCollectionArchive("./output/collection1.zip");
        } catch (TAException ex) {
            ex.printStackTrace();
            assertTrue(false, ex.getMessage());
        }
    }
}