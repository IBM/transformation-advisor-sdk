
/*
 * (C) Copyright IBM Corp. 2019,2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.ta.sdk.sample.test;

import com.google.gson.*;
import com.ibm.ta.sdk.sample.SamplePluginProvider;
import com.ibm.ta.sdk.spi.plugin.TAException;
import com.ibm.ta.sdk.spi.collect.AssessmentUnit;
import com.ibm.ta.sdk.spi.collect.DataCollection;
import com.ibm.ta.sdk.spi.collect.Environment;
import com.ibm.ta.sdk.spi.assess.RecommendationJson;
import com.ibm.ta.sdk.spi.recommendation.Recommendation;
import com.ibm.ta.sdk.spi.plugin.CliInputCommand;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class PluginProvisorTest {

    public static void main(String[] argu) throws Exception {

        System.out.println("start");
        CliInputCommand collectCmd = new CliInputCommand(CliInputCommand.CMD_COLLECT,
              "Performs data collection",
              null, null, null);
        SamplePluginProvider spp = new SamplePluginProvider();
        List<DataCollection> dataCollection = spp.getCollection(collectCmd);
        Environment environment = dataCollection.get(0).getEnvironment();
        List<? extends AssessmentUnit> assessUnits = dataCollection.get(0).getAssessmentUnits();


        List<Recommendation> rec = spp.getRecommendation(collectCmd);
        RecommendationJson recJson = new RecommendationJson(rec.get(0), environment, assessUnits);

        // Write files to output dir
        String cwd = System.getProperty("user.dir");
        File outputDir = new File(cwd, "output");
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }
        writeRecommendationsJson(recJson, outputDir);

        System.out.println("complete");
    }

    private static void writeRecommendationsJson(RecommendationJson recJson, File outputDir) throws Exception {
        File rjFile = new File(outputDir, "recommendations.json");
        if (rjFile.exists()) {
            rjFile.delete();
        }

        GsonBuilder builder = new GsonBuilder();
        builder.excludeFieldsWithoutExposeAnnotation();
        builder.setPrettyPrinting();

        Gson gson = builder.create();
        String recJsonStr = gson.toJson(recJson);
        System.out.println("zzzzzzz");
        //System.out.println(recJsonStr);
        writeFile(rjFile, recJsonStr);
    }

    private static void writeFile(File file, String content) throws TAException {
        FileWriter writer = null;
        try {
            writer = new FileWriter(file);
            writer.write(content);
        } catch (IOException e) {
            throw new TAException("Error writing recommendations.json", e);
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {

                }
            }
        }
    }

}