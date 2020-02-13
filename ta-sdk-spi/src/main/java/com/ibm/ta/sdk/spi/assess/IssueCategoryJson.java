/*
 * (C) Copyright IBM Corp. 2019,2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.ta.sdk.spi.assess;

import com.google.gson.annotations.Expose;
import com.ibm.ta.sdk.spi.recommendation.IssueCategory;

import java.util.*;
import java.util.stream.Collectors;

public class IssueCategoryJson {
  private String id;

  @Expose
  private String title;

  public IssueCategoryJson(IssueCategory issueCat) {
    id = issueCat.getId();
    title = issueCat.getTitle();
  }

  public IssueCategory getIssueCategory() {
    return new IssueCategory() {
      @Override
      public String getId() {
        return id;
      }

      @Override
      public String getTitle() {
        return title;
      }

      @Override
      public String toString() {
        return "id=" + getId() + ", " +
                "title=" + getTitle();
      }
    };
  }

  public static Map<String, IssueCategoryJson> getIssueCategoryJsonMap(List<IssueCategory> issueCatList) {
    Map<String, IssueCategoryJson> issueCatMap = new HashMap<String, IssueCategoryJson>();
    for (IssueCategory ic : issueCatList) {
      issueCatMap.put(ic.getId(), new IssueCategoryJson(ic));
    }
    return issueCatMap;
  }

  public static List<IssueCategory> getIssueCategoryList(Map<String, IssueCategoryJson> issueCatJsonMap) {
    issueCatJsonMap.keySet().stream()
            .forEach(k -> issueCatJsonMap.get(k).id = k);
    return issueCatJsonMap.keySet().stream()
            .map(k -> issueCatJsonMap.get(k).getIssueCategory())
            .collect(Collectors.toList());
  }

  @Override
  public String toString() {
    return "id=" + id + ", " +
            "title=" + title;
  }
}
