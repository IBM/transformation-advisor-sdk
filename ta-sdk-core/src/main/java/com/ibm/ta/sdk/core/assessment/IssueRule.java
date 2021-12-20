/*
 * (C) Copyright IBM Corp. 2019,2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.ta.sdk.core.assessment;

import com.google.gson.JsonObject;
import com.google.gson.annotations.Expose;
import com.jayway.jsonpath.DocumentContext;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.List;

public class IssueRule {

  public static final String OCCURRENCE_TITLE_ATTR = "title";
  public static final String OCCURRENCE_COUNT_UNIQUE_ATTR = "countUnique";

  @Expose(serialize = false)
  protected String id;

  @Expose(serialize = false)
  protected String title;

  @Expose(serialize = false)
  protected String category;

  @Expose(serialize = false)
  protected float issueOverhead;

  @Expose(serialize = false)
  protected float occurrencesCost;

  @Expose(serialize = false)
  protected List<String> solutionText;

  @Expose(serialize = false)
  protected String severity;

  @Expose(serialize = false)
  protected String javaIssue;

  @Expose(serialize = false)
  protected JsonObject matchCriteria;
  private IssueMatchCriteria issueMatchCriteria;

  private String uniqueCountKey; // Currently only support 1 key that is marked as countUnique

  private List<String> uniqueKeyValues = new ArrayList<String>();

  public String getId() {
    return id;
  }

  public String getProvider() {
    return getMatchCriteria().getProvider();
  }

  public String getTitle() {
    return title;
  }

  public String getCategory() {
    return category;
  }

  public float getIssueOverhead() {
    return issueOverhead;
  }

  public float getOccurrencesCost() {
    return occurrencesCost;
  }

  public List<String> getSolutionText() {
    return solutionText;
  }

  public String getSeverity() {
    return severity;
  }

  public JsonObject getMatchCriteriaJson() {
    return matchCriteria;
  }

  public IssueMatchCriteria getMatchCriteria() {
    return issueMatchCriteria;
  }

  public void setMatchCriteria(IssueMatchCriteria issueMatchCriteria) {
    this.issueMatchCriteria = issueMatchCriteria;
  }

  /*
      Override to perform additional filtering, for a path that already matches the getFilter() criteria.
      Return false to skip data from the path to be included in the occurrences.
  */
  public boolean customFilter(DocumentContext doc, String path) {
    return true;
  }

  @Override
  public String toString() {
    return "id=" + id + ", " +
            "provider=" + getProvider() + ", " +
            "title=" + title + ", " +
            "category=" + category + ", " +
            "issueOverhead=" + issueOverhead + ", " +
            "occurrencesCost=" + occurrencesCost + ", " +
            "solutionText=" + solutionText + ", " +
            "severity=" + severity + ", " +
            "javaIssue=" + javaIssue + ", " +
            "matchCriteria=" + matchCriteria;
  }
}


