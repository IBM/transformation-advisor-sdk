/*
 * (C) Copyright IBM Corp. 2019,2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.ta.sdk.core.report;

import java.util.ArrayList;
import java.util.List;

public class IssuesSameCategory {

    private String issueCatKey = null;

    private String issueCategoryTitle = null;
    private List<IssueReport> issues = new ArrayList();

    public String getIssueCatKey() {
        return issueCatKey;
    }

    public void setIssues(List<IssueReport> issues) {
        this.issues = issues;
    }

    public List<IssueReport> getIssues() {
        return issues;
    }

    public IssuesSameCategory(String issueCatKey, String issueCategoryTitle){
        this.issueCatKey = issueCatKey;
        this.issueCategoryTitle = issueCategoryTitle;
    }


    public void addIssue(IssueReport issue) {
        this.issues.add(issue);
    }

//    public static List<IssuesSameCategory> updateIssuesSameCategoryList (List<IssuesSameCategory> issuesSameCategories, String issueCatKey, List<Issue> issues){
//        List<IssuesSameCategory> result = new ArrayList<IssuesSameCategory>();
//        for (IssuesSameCategory issuesSameCategory : issuesSameCategories){
//            if (issueCatKey.equals(issuesSameCategory.getIssueCatKey())){
//                issuesSameCategory.setIssues(issues);
//                result.add(issuesSameCategory);
//            }else{
//                result.add(issuesSameCategory);
//            }
//        }
//        return result;
//    }

//    public static List<IssuesSameCategory> updateIssuesSameCategoryList (List<IssuesSameCategory> issuesSameCategories, String issueCatKey, Issue issue){
//        List<IssuesSameCategory> result = new ArrayList<IssuesSameCategory>();
//        for (IssuesSameCategory issuesSameCategory : issuesSameCategories){
//            if (issueCatKey.equals(issuesSameCategory.getIssueCatKey())){
//                issuesSameCategory.addIssue(issue);
//                result.add(issuesSameCategory);
//            }else{
//                result.add(issuesSameCategory);
//            }
//        }
//        return result;
//    }


    public String getIssueCategoryTitle() {
        return issueCategoryTitle;
    }
}
