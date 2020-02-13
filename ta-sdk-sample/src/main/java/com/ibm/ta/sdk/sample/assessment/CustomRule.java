/*
 * (C) Copyright IBM Corp. 2019,2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.ta.sdk.sample.assessment;

import com.ibm.ta.sdk.core.assessment.IssueRule;

import java.util.List;

public class CustomRule extends IssueRule {

  @Override
  public List<String> getSolutionText() {
    List<String> solution = super.getSolutionText();
    solution.add(0, "Try sleeping first, then:");
    return solution;
  }

}
