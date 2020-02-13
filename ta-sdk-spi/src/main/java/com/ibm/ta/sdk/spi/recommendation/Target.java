/*
 * (C) Copyright IBM Corp. 2019,2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.ta.sdk.spi.recommendation;

public interface Target {
  enum PlatformType {
    Docker,
    VM;
  }

  enum LocationType {
    Private,
    Public;
  }

  String getProductName();  // Liberty, WAS, MQ, IIB

  String getProductVersion();

  String getRuntime();  // Optional - ACE

  PlatformType getPlatform();

  LocationType getLocation();

}
