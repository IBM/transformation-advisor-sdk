/*
 * (C) Copyright IBM Corp. 2019,2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.ta.sdk.spi.collect;

import com.google.gson.JsonObject;

/**
 * {@code Environment} contains information about the middleware, and the Operating System where it is
 * running on.
 *
 * The environment information is persisted to JSON format, and written to a file called environment.json
 * in the root of the <i>output</i> directory
 */
public interface Environment {
  /**
   * Name of the Operating System that the middleware is installed on.
   *
   * @return OS Name
   */
  String getOperatingSystem();

  /**
   * Hostname of the system where the middleware is installed on.
   *
   * @return Hostname of the system
   */
  String getHostname();

  /**
   * Domain name, or product family, is a container grouping that the middleware belongs to.
   *
   * @return Domain name
   */
  String getDomain();

  /**
   * Name of the middleware installed on the system that is being analyzed.
   *
   * @return Name of the middleware
   */
  String getMiddlewareName();

  /**
   * Version of the middleware installed on the system that is being analyzed.
   *
   * @return Version of the middleware
   */
  String getMiddlewareVersion();

  /**
   * Filesystem path on the system where the middleware is installed on.
   *
   * @return Installation path of the middleware
   */
  String getMiddlewareInstallPath();

  /**
   * Filesystem path on the system where the data for the middleware is store on.
   *
   * @return Data path of the middleware
   */
  String getMiddlewareDataPath();

  /**
   * Additional metadata about the middleware or the Operating System. This could contain additional information about
   * the OS or the middleware that could be used during assessment to help identify issues. The information is
   * stored in JSON format. It is expected that the plug-in provider who constructed this information also knows
   * how to parse and use it during assessment.
   *
   * @return Additional metadata information about the OS or middleware
   */
  JsonObject getMiddlewareMetadata();

  /**
   * An collection unit could be an installation of a middleware. In this case, the collection unit name would be the name
   * of the middleware installation. An collection unit could also be a particular node containing the middleware. The
   * model for collection unit will generally be different for different middleware, and is decided by the plug-in
   * developer.
   *
   * @return Name of the collection unit
   */
  String getCollectionUnitName();

  /**
   * As with the {@link #getCollectionUnitName()}, the collection unit type will generally be different for different
   * middleware. Examples of assessment type my be "Installation", or "Node".
   *
   * @return Type of the collection unit
   */
  String getCollectionUnitType();

  /**
   * As with the {@link #getCollectionUnitName()}, the collection unit type will generally be different for different
   * middleware. Examples of assessment type my be "Installation", or "Node".
   *
   * @return Type of the collection unit
   */
  String getCollectionUnitLabel();

  /**
   * Label to be used to describe the assessment unit (for a single assessment unit, e.g. MyAssessmentUnit: MyAssessmentUnitName)
   *
   * @return Assessment unit label for single
   */
  String getAssessmentUnitSingleLabel();

  /**
   * Label to be used to describe assessment units (for multiple assessment units, e.g. MyAssessmentUnits: MyAssessmentUnitName1, MyAssessmentUnitName2...)
   *
   * @return Assessment unit label for multiple
   */
  String getAssessmentUnitMultipleLabel();

  /**
   * Additional metadata about the assessment, in addition to the name and type. This information is available
   * during assessment to help identify issues. The information is stored in JSON format. It is expected that the
   * plug-in provider who constructed this information also knows how to parse and use it during assessment.
   *
   * @return Additional metadata information about the assessment
   */
  JsonObject getAssessmentMetadata();
}
