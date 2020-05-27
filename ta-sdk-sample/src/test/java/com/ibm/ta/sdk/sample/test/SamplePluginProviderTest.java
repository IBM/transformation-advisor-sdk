
/*
 * (C) Copyright IBM Corp. 2019,2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.ta.sdk.sample.test;

import com.ibm.ta.sdk.spi.plugin.TADataCollector;
import com.ibm.ta.sdk.spi.plugin.TAException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import com.ibm.ta.sdk.spi.util.Util;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class SamplePluginProviderTest {

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
        argus.add("test");
        TADataCollector tadc = new TADataCollector();
        tadc.runCommand("sample", argus);
        File outputDir = Util.getOutputDir();
        File[] directories = outputDir.listFiles(File::isDirectory);
        assertEquals(directories.length, 1);
        assertEquals(directories[0].getName(), "Installation1");
        File envFile = new File (directories[0]+ "/environment.json");
        assertTrue(envFile.exists());
        assertTrue(envFile.isFile());
        assertTrue(envFile.length() > 2);
        File recommFile = new File (directories[0]+"/recommendations.json");
        assertFalse(recommFile.exists());
    }

    @Test
    public void assessCommandTest() throws TAException, IOException {
        List<String> argus = new ArrayList<>();
        argus.add("assess");
        argus.add("test");
        argus.add("test");
        TADataCollector tadc = new TADataCollector();
        tadc.runCommand("sample", argus);
        File outputDir = Util.getOutputDir();
        File[] directories = outputDir.listFiles(File::isDirectory);
        assertEquals(directories.length, 1);
        assertEquals(directories[0].getName(), "Installation1");
        File envFile = new File (directories[0]+ "/environment.json");
        assertTrue(envFile.exists());
        assertTrue(envFile.isFile());
        assertTrue(envFile.length() > 2);
        File recommFile = new File (directories[0]+"/recommendations.json");
        assertTrue(recommFile.exists());
        assertTrue(recommFile.isFile());
        assertTrue(recommFile.length() > 2);
    }

    @Test
    public void runCommandTest() throws TAException, IOException {
        List<String> argus = new ArrayList<>();
        argus.add("run");
        argus.add("test");
        argus.add("test");
        TADataCollector tadc = new TADataCollector();
        tadc.runCommand("sample", argus);
        File outputDir = Util.getOutputDir();
        File[] directories = outputDir.listFiles(File::isDirectory);
        assertEquals(directories.length, 1);
        assertEquals(directories[0].getName(), "Installation1");
        File envFile = new File (directories[0]+ "/environment.json");
        assertTrue(envFile.exists());
        assertTrue(envFile.isFile());
        assertTrue(envFile.length() > 2);
        File recommFile = new File (directories[0]+"/recommendations.json");
        assertTrue(recommFile.exists());
        assertTrue(recommFile.isFile());
        assertTrue(recommFile.length() > 2);
        File reportFile = new File (directories[0]+"/AssessmentUnit1/recommendations_Private_Docker.html");
        assertTrue(reportFile.exists());
        assertTrue(reportFile.isFile());
        assertTrue(reportFile.length() > 2);
        File collectionFile = new File (outputDir+"/Installation1.zip");
        assertTrue(collectionFile.exists());
        assertTrue(collectionFile.isFile());
        assertTrue(collectionFile.length() > 2);
    }

}