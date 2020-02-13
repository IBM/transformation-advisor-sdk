/*
 * (C) Copyright IBM Corp. 2019,2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.ta.sdk.spi.report;


import com.ibm.ta.sdk.spi.recommendation.Target;

public interface Report {
  String getAssessmentName();

  String getAssessmentUnitName();

  Target getTarget();

  ReportType getReportType();

  byte[] getReport();
}
