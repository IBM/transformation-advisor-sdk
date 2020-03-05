/*
 * (C) Copyright IBM Corp. 2019,2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.ta.sdk.spi.collect;

import java.util.List;

/**
 * {@code DataCollection} contains all the data that is persisted to <i>output</i> directory on the filesystem. This
 * data is used later during assessment to identify issues and create a <i>recommendations.json</i>.
 *
 * <p>{@link #getEnvironment()} contains information about the middleware, and the Operating System where it is
 * running on. {@code Environment} is persisted to JSON format, and written to a file called environment.json
 * in the root of the <i>output</i> directory.
 *
 * <p>@see AssessmentUnitMetadataJson for an overview of an assessment unit. Generally, an assessment unit is a running instance
 * of the middleware installation. Each assessment unit contains configuration information in <i>JSON</i> format. In
 * addition, it may include other configuration files that is relevant to the assessment of this unit. The artifacts
 * for each assessment unit are persisted in a separate sub-directory in the <i>output</i> directory. The name
 * of this sub-directory is the assessment unit name.
 *
 */
public interface DataCollection {

  /**
   * Name of the assessment that this data collection is for.
   * @return Assessment name
   */
  String getAssessmentName();


  /**
   * {@link Environment} contains information about the middleware and the Operating System where this middleware
   * is running on.
   *
   * @return Information about the environment where the middleware is running on
   * @see Environment
   */
  Environment getEnvironment();

  /**
   * Each assessment unit is assessed separately. For this same reason, the configuration data and files are each
   * assessment unit are stored separately as well. {@link AssessmentUnit} contains all the information for each
   * assessment unit, which will be persisted in its own sub-directory.
   *
   * @return {@code List} of {@code AssessmentUnitMetadataJson}, with configuration data and files for each assessment unit
   * @see AssessmentUnit
   */
  List<? extends AssessmentUnit> getAssessmentUnits();
}
