/*
 * (C) Copyright IBM Corp. 2019,2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.ta.sdk.spi.assess;

import com.google.gson.annotations.Expose;
import com.ibm.ta.sdk.spi.recommendation.ComplexityRating;
import com.ibm.ta.sdk.spi.recommendation.ComplexityContribution;

import java.util.ArrayList;
import java.util.List;

public class ComplexityContributionJson {
  @Expose
  private String id;

  @Expose
  private String name;

  @Expose
  private String description;

  @Expose
  private String complexityContribution;

  @Expose
  private List<String> issues;

  @Expose
  private List<String> issuesCategory;

  public ComplexityContributionJson(ComplexityContribution cc) {
    id = cc.getId();
    name = cc.getName();
    description = cc.getDescription();
    complexityContribution = cc.getComplexity().name();
    issues = cc.getIssues();
    issuesCategory = cc.getIssuesCategory();
  }

  public ComplexityContribution getComplexityContribution() {
    return new ComplexityContribution() {
      @Override
      public String getId() {
        return id;
      }

      @Override
      public String getName() {
        return name;
      }

      @Override
      public String getDescription() {
        return description;
      }

      @Override
      public ComplexityRating getComplexity() {
        return ComplexityRating.valueOf(complexityContribution);
      }

      @Override
      public List<String> getIssues() {
        return issues;
      }

      @Override
      public List<String> getIssuesCategory() {
        return issuesCategory;
      }

      @Override
      public String toString() {
        return "id=" + id + ", " +
                "name=" + name + ", " +
                "description=" + description + ", " +
                "complexity=" + getComplexity() + ", " +
                "issues=" + issues + ", " +
                "issuecatories=" + issuesCategory;
      }

    };
  }

  public static List<ComplexityContributionJson> getComplexityContributionJsonList(List<ComplexityContribution> ccList) {
    List<ComplexityContributionJson> ccJsonList = new ArrayList<ComplexityContributionJson>();
    for (ComplexityContribution cc : ccList) {
      ccJsonList.add(new ComplexityContributionJson(cc));
    }
    return ccJsonList;
  }

  public static List<ComplexityContribution> getComplexityContributionList(List<ComplexityContributionJson> ccJsonList) {
    List<ComplexityContribution> ccList = new ArrayList<ComplexityContribution>();
    for (ComplexityContributionJson ccJson : ccJsonList) {
      ccList.add(ccJson.getComplexityContribution());
    }
    return ccList;
  }
}
