/*
 * (C) Copyright IBM Corp. 2019,2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.ta.sdk.spi.recommendation;

import java.util.List;

/**
  * TODO Separate into Issue and IssueSolution and allow multiples for the same Issue
  * TODO allow IssueSolution to be Target specific
  * TODO add Issue.getIssueDetail method that returns markdown describing exactly the problem found
 */
public interface Issue {

  String getId();

 /**
   * Title should be written in a manner that tells the user what they need to do at a glance. 
   * 
   * For RED severity issues, it should be written like
   * Fire detected.  Use the Dry Powder fire extinguisher.
   * 
   * For YELLOW severity issues it should be written like
   * Smoke detected.  Please check in the kitchen to see if you need to replace the dishwasher.
   * 
   * For GREEN severity issues it should be written like
   * Smell detected. Consider giving the utility a clean.
   *
   * @return the title of the issue
   */
  String getTitle();

  IssueCategory getCategory();

 /**
   * Note: currently this field is used as the title in the UI.  It should use title instead.
   * and this field should be used for the longer detail.
   * 
   * TODO return markdown instead of plain text
   * 
   * @return the solution test of the issue
   */
  List<String> getSolutionText();

  /**
   * For GREEN severity issues, Cost should be zero as it does not need to count as part of the modernization effort
   * A Cost can be provided however in case
   * 
   * Cost = OverheadCost + (OccurenceCost x # occurences)
   * 
   * @return cost (in Days)
   */
  Float getCost();

  /**
   * Cost if at least one occurence of the issue is found
   * 
   * @return overheadCost (in Days)
   */
  Float getOverheadCost();

  /**
   * Cost to be added for each occurence found (including the first one)
   * 
   * @return occurenceCost (in Days)
   */
  Float getOccurrenceCost();

  /**
   * @see Severity
   * 
   * @return the severity of the issue
   */
  Severity getSeverity();

  ComplexityContribution getComplexityContribution();

  void setComplexityContribution(ComplexityContribution complexityContribution);

  Occurrence getOccurrence();

  Integer getOccurrencesCount();
}
