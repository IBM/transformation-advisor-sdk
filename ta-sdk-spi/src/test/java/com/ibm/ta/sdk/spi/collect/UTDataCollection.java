/*
 * (C) Copyright IBM Corp. 2019,2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.ta.sdk.spi.collect;

import com.ibm.ta.sdk.spi.collect.*;
import com.ibm.ta.sdk.spi.test.TestUtils;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class UTDataCollection implements DataCollection {
    private String assessmentName;
    private EnvironmentJson environmentJson;
    private List<AssessmentUnit> assessmentUnits;

    public UTDataCollection(String assessmentName, String envFile, List<String> assessmentUnitFiles) throws IOException, URISyntaxException {
        this.assessmentName = assessmentName;
        this.environmentJson = TestUtils.buildEnvironmentJsonObj(new File(TestUtils.TEST_RESOURCES_DIR, envFile).toPath());
        assessmentUnits = new ArrayList<>();
        for (String auFile : assessmentUnitFiles) {
            String auName = new File(auFile).getName().split("\\.")[0];
            assessmentUnits.add(new UTAssessmentUnit(auName, auFile));
        }
    }

    @Override
    public String getAssessmentName() {
        return assessmentName;
    }

    public EnvironmentJson getEnvironmentJson() {
        return environmentJson;
    }

    @Override
    public Environment getEnvironment() {
        return environmentJson.getEnvironment();
    }

    @Override
    public List<? extends AssessmentUnit> getAssessmentUnits() {
        return assessmentUnits;
    }
}
