/*
 * (C) Copyright IBM Corp. 2019,2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.ta.sdk.spi.test;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.ibm.ta.sdk.spi.collect.EnvironmentJson;
import com.ibm.ta.sdk.spi.plugin.PluginProvider;
import com.ibm.ta.sdk.spi.plugin.TADataCollector;
import com.ibm.ta.sdk.spi.plugin.TAException;
import com.ibm.ta.sdk.spi.assess.UTRecommendation;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public class TestUtils {
    public static final String TEST_RESOURCES_DIR = "src" + File.separator + "test" + File.separator + "resources";
    public static final String TEST_OUTPUT_DIR = "output";
    public static final String ENVIRONMENT_JSON = "environment.json";
    public static final String RECOMMENDATIONS_JSON = "recommendations.json";


    public static void runPluginCommand(PluginProvider plugin, List<String> cliArguments) throws TAException, IOException {
        new TADataCollector() {
            @Override
            public Iterator<PluginProvider> getPluginProviders() {
                return Arrays.asList(plugin).iterator();
            }
        }.runCommand(plugin.getMiddleware(), cliArguments);
    }

    public static EnvironmentJson buildEnvironmentJsonObj(Path envFile) throws  IOException {
        return getJsonObj(new TypeToken<EnvironmentJson>(){}, envFile);
    }

    public static UTRecommendation buildRecommendationsJsonObj(Path recommendationFile) throws IOException {
        return getJsonObj(new TypeToken<UTRecommendation>(){}, recommendationFile);
    }

    private static String readFileToString(Path path) throws IOException {
        List<String> lines = Files.readAllLines(path);
        String content = lines.stream()
                .collect(Collectors.joining(System.lineSeparator()));
        return content;
    }

    public static <T> T getJsonObj(TypeToken<T> typeToken, Path path) throws IOException {
        String jsonStr = readFileToString(path);
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        return gson.fromJson(jsonStr, typeToken.getType());
    }

    public static JsonElement getJson(Path resourcePath) throws IOException {
        String jsonStr = readFileToString(resourcePath);
        return new JsonParser().parse(jsonStr);
    }

    public static JsonElement getJsonTree(Object object) {
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        return gson.toJsonTree(object);
    }

    public static void deleteDir(File dir) {
        if (dir.exists()) {
            if (dir.isFile()) {
                dir.delete();
            } else {
                for (File file : dir.listFiles()) {
                    deleteDir(file);
                }
                dir.delete();
            }
        }
    }
}
