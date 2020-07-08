/*
 * (C) Copyright IBM Corp. 2019,2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.ta.sdk.core.report;

import com.google.gson.annotations.Expose;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RecommendationReport {

    private String domain;
    private String collectionUnitType;
    private String collectionUnitName;
    private String middleware;
    private String version;
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


    public String getCollectionUnitType() {
        return collectionUnitType;
    }

    public void setCollectionUnitType(String collectionUnitType) {
        this.collectionUnitType = collectionUnitType;
    }

    public String getCollectionUnitName() {
        return collectionUnitName;
    }

    public void setCollectionUnitName(String collectionUnitName) {
        this.collectionUnitName = collectionUnitName;
    }

    public String getMiddleware() {
        return middleware;
    }

    public void setMiddleware(String middleware) {
        this.middleware = middleware;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}
