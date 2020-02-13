/*
 * (C) Copyright IBM Corp. 2019,2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.ta.sdk.core.detector;

import com.google.gson.JsonObject;
import com.ibm.ta.sdk.spi.collect.AssessmentUnit;
import com.ibm.ta.sdk.spi.recommendation.Target;
import com.ibm.ta.sdk.core.assessment.GenericIssue;
import com.ibm.ta.sdk.core.assessment.IssueMatchCriteria;
import com.ibm.ta.sdk.core.assessment.IssueRule;


public interface IssueRuleTypeProvider {

  public String getName();

  public IssueMatchCriteria getIssueMatchCriteria(JsonObject matchCriteriaJson);

  public GenericIssue getIssue(Target target, AssessmentUnit assessmentUnit, IssueRule issueRule);

}
