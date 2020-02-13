/*
 * (C) Copyright IBM Corp. 2019,2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.ta.sdk.core.report;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RecommendationReport {

    private String domain;
    private List<Map<String, String>> issueCategories;
    private List<AssessmentUnitReport> assessmentUnits = new ArrayList<AssessmentUnitReport>();

    public void setIssueCategories(List<Map<String, String>> issueCategories) {
        this.issueCategories = issueCategories;
    }

    public void addAssessmentUnit(AssessmentUnitReport assessmentUnit) {
        this.assessmentUnits.add(assessmentUnit);
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public List<Map<String, String>> getIssueCategories() {
        return issueCategories;
    }

    public List<AssessmentUnitReport> getAssessmentUnits() {
        return assessmentUnits;
    }

    public String getDomain() {
        return domain;
    }


    // getters , setters, some boring stuff
}
