/*
 * (C) Copyright IBM Corp. 2019,2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.ta.sdk.spi.collect;

import com.google.gson.JsonObject;
import com.ibm.ta.sdk.spi.collect.AssessmentUnit;
import com.ibm.ta.sdk.spi.collect.ContentMask;
import com.ibm.ta.sdk.spi.test.TestUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class UTAssessmentUnit implements AssessmentUnit {
    private String name;
    private JsonObject assessmentData;
    private List<Path> configFiles;
    private List<ContentMask> contentMasks;
    private Properties identifier;

    public UTAssessmentUnit(String name, String assessmentDataFile) throws IOException {
        this.name = name;
        this.assessmentData = TestUtils.getJson(new File(TestUtils.TEST_RESOURCES_DIR, assessmentDataFile).toPath()).getAsJsonObject();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public JsonObject getAssessmentData() {
        return assessmentData;
    }

    public void setConfigFiles(List<String> configFiles) {
        if (this.configFiles == null) {
            this.configFiles = new ArrayList<>();
        }

        for (String configFile : configFiles) {
            if (configFile.startsWith("/")) {
                this.configFiles.add(new File(configFile).toPath());
            } else {
                this.configFiles.add(new File(TestUtils.TEST_RESOURCES_DIR, configFile).toPath());
            }
        }
    }

    @Override
    public List<Path> getConfigFiles() {
        if (configFiles == null) {
            configFiles = new ArrayList<>();
        }
        return configFiles;
    }

    public void setContentMasks(List<ContentMask>  contentMasks) {
        this.contentMasks = contentMasks;
    }

    @Override
    public List<ContentMask> getContentMasks() {
        if (contentMasks == null) {
            contentMasks = new ArrayList<>();
        }
        return contentMasks;
    }

    @Override
    public Properties getIdentifier() {
        return identifier;
    }


}
