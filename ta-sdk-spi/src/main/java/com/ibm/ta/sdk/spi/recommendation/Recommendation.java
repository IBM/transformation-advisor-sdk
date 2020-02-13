/*
 * (C) Copyright IBM Corp. 2019,2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.ta.sdk.spi.recommendation;

import com.ibm.ta.sdk.spi.plugin.TAException;
import com.ibm.ta.sdk.spi.collect.AssessmentUnit;

import java.util.List;

public interface Recommendation {

  String getAssessmentName();

  List<ComplexityContribution> getComplexityContributions();

  List<IssueCategory> getIssueCategories();

  List<Target> getTargets();

  List<Issue> getIssues(Target target, AssessmentUnit assessmentUnit) throws TAException;
}
