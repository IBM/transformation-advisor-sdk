/*
 * (C) Copyright IBM Corp. 2019,2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.ta.sdk.spi.collect;

import com.google.gson.JsonObject;

import java.nio.file.Path;
import java.util.List;

/**
 * Most middleware installations support many running instances of the middleware. Each of these instance is
 * generically represent as an {@code AssessmentUnit}. Examples of assessment units include profiles in <b>WebSphere
 * Application Server</b>, or Queue Managers in <b>WebSphere MQ</b>.
 *
 * <p>All collected artifacts for an assessment unit exist in a separate subdirectory, by the asssessment unit name,
 * in the output directory.
 *
 */
public interface AssessmentUnit {
  /**
   * Name of the assessment unit.
   *
   * @return Assessment unit name
   */
  String getName();

  /**
   * {}@code JsonObject} containing information and configurations of the assessment unit. This information
   * could be used later in the assess stage to identify issues. It is a good practice to gather as much
   * configuration information as possible. In the future there may be support to re-run assessments on collected data,
   * without having to re-run collection again. New rules could be added to re-assess and generate new
   * recommendations on the existing data. This would only work if the original data collected is broad and
   * detailed.
   *
   * <p>In the output directory of the assessment unit, the {}@code JsonObject} returned from this method
   * {@link #getAssessmentData()} is written to a file with the same name as the assessment unit.
   *
   * @return Data for the assessment unit in a {@code JsonObject}
   */
  JsonObject getAssessmentData();

  /**
   * In addition to the assessment data, configuration files for an assessment unit could be collected and used later
   * during assessment to identify issues.
   *
   * <p>The configuration files returned from this method are copied to the output directory of the assessment unit.
   * The directory tree of configuration file is retained in the output directory. File <i>/tmp/config.json</i> would
   * exist in the output directory in this path <i>output/[assessment unit name]/tmp/config.json</i>.
   *
   * <p>The collected data could be exported to an external location. If it possible the all collected config files
   * are free of sensitive information. {@link #getContentMasks()} could be used to filter out sensitive information
   * from the config files.
   *
   * @return {@code List} of {@code Path} of files on the filesystem or jar archive that will be copied to
   * the assessment unit's output directory. Return an empty list if no config files are to be collected.
   */
  List<Path> getConfigFiles();

  /**
   * {@link ContentMask} could be used to filter out sensitive information, such as passwords, from the
   * {@link #getConfigFiles()} files collected.
   *
   * @return {@code List} of {@code ContentMask} that will be applied to the config files to remove sensitive
   * information. Return an empty list if no content masks exist.
   */
  List<ContentMask> getContentMasks();

}
