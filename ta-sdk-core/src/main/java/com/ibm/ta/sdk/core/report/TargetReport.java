/*
 * (C) Copyright IBM Corp. 2019,2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.ta.sdk.core.report;

import java.util.ArrayList;
import java.util.List;

public class TargetReport {

    private String productName;
    private String productVersion;
    private String runtime;
    private String platform;
    private String location;
    private String overallComplexityScore;
    private int numberOfRedIssues = 0;
    private int numberOfYellowIssues = 0;
    private int numberOfGreenIssues = 0;

    private List<IssuesSameCategory> issuesSameCategories = new ArrayList<>();

    public TargetReport(String productName, String productVersion, String runtime, String platform, String location, String overallComplexityScore, int numberOfRedIssues, int numberOfYellowIssues, int numberOfGreenIssues){
        this.productName = productName;
        this.productVersion =productVersion;
        this.runtime = runtime;
        this.platform = platform;
        this.location = location;
        this.numberOfRedIssues = numberOfRedIssues;
        this.numberOfYellowIssues = numberOfYellowIssues;
        this.numberOfGreenIssues = numberOfGreenIssues;
        this.overallComplexityScore = overallComplexityScore;
    }

    public void IssuesSameCategory(IssuesSameCategory issueSameCategory){
        issuesSameCategories.add(issueSameCategory);
    }

    public String getProductName() {
        return productName;
    }

    public String getProductVersion() {
        return productVersion;
    }

    public String getRuntime() {
        return runtime;
    }

    public String getPlatform() {
            return platform;
        }

    public String getLocation() {
        return location;
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
