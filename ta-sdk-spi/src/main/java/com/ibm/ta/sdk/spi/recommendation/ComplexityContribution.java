/*
 * (C) Copyright IBM Corp. 2019,2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.ta.sdk.spi.recommendation;

import java.util.List;

/**
 * TODO rename to ComplexityRule
 * 
 * TODO add a "getSolution" and "getIgnoreCondition" method?
 * 
 * Issue(Solution?)s can only be in one ComplexityRule
 */
public interface ComplexityContribution {

  String getId();

  /**
   * Example: Incompatible technology
   * @return the rule name
   */
  String getName();

  /**
   * Example: This application uses technologies that are not available in Liberty....
   * @return description
   */
  String getDescription();

  /**
   * The Complexity Rating that we level up to if any of the issues in this rule are found
   * @return complexityRating
   */
  ComplexityRating getComplexity();

  /**
   * A list of specific issues that apply to this rule.
   * If another ComplexityRule specifies the Category of any of these Issues then this specific reference will take precedence
   * @return a list of issueSolution ids
   */

  List<String> getIssues();
  
  /**
   * A category of issues that apply to this rule.
   * @return a list of issueCategory ids
   */
  List<String> getIssuesCategory();
}
