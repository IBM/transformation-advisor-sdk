/*
 * (C) Copyright IBM Corp. 2019,2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.ta.sdk.core.report;

import com.google.gson.JsonArray;

import java.util.ArrayList;
import java.util.List;

public class TargetReport {

    private String target;
    private JsonArray dimensions;
    private String overallComplexityScore;
    private int numberOfRedIssues = 0;
    private int numberOfYellowIssues = 0;
    private int numberOfGreenIssues = 0;

    private List<IssuesSameCategory> issuesSameCategories = new ArrayList<>();

    public TargetReport(String targetId, JsonArray dimensions, String overallComplexityScore, int numberOfRedIssues,
                        int numberOfYellowIssues, int numberOfGreenIssues){
        this.target = targetId;
        this.dimensions = dimensions;
        this.numberOfRedIssues = numberOfRedIssues;
        this.numberOfYellowIssues = numberOfYellowIssues;
        this.numberOfGreenIssues = numberOfGreenIssues;
        this.overallComplexityScore = overallComplexityScore;
    }

    public void IssuesSameCategory(IssuesSameCategory issueSameCategory){
        issuesSameCategories.add(issueSameCategory);
    }

    public String getTargetId() {
        return target;
    }

    public JsonArray getDimensions() {
        return dimensions;
    }

    public int getNumberOfRedIssues() {
        return numberOfRedIssues;
    }

    public int getNumberOfYellowIssues() {
        return numberOfYellowIssues;
    }

    public int getNumberOfGreenIssues() {
        return numberOfGreenIssues;
    }

    public String getOverallComplexityScore() {
        return overallComplexityScore;
    }

    public List<IssuesSameCategory> getIssuesSameCategory() {

        return this.issuesSameCategories;
    }

    public void setIssuesSameCategories(List<IssuesSameCategory> issuesSameCategories) {
        this.issuesSameCategories = issuesSameCategories;
    }

    public void addIssuesSameCategory(IssuesSameCategory issuesSameCategory) {
        this.issuesSameCategories.add(issuesSameCategory);
    }

}
