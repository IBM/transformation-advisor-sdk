/*
 * (C) Copyright IBM Corp. 2019,2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.ta.sdk.core.report;

import java.util.ArrayList;
import java.util.List;

public class AssessmentUnitReport {

    private String name;
    private List<TargetReport> targets = new ArrayList<>();

    public AssessmentUnitReport(String name){
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public List<TargetReport> getTargets() {
        return this.targets;
    }


    public void addTarget(TargetReport target) {
        this.targets.add(target);
    }

}
