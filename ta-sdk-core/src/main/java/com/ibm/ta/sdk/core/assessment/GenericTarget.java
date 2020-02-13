/*
 * (C) Copyright IBM Corp. 2019,2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.ta.sdk.core.assessment;

import com.google.gson.annotations.Expose;
import com.ibm.ta.sdk.spi.recommendation.Target;

public class GenericTarget implements Target {
  @Expose(serialize = false)
  private String productName;

  @Expose(serialize = false)
  private String productVersion;

  @Expose(serialize = false)
  private String runtime;

  @Override
  public PlatformType getPlatform() {
    return PlatformType.Docker;
  }

  @Override
  public LocationType getLocation() {
    return LocationType.Private;
  }

  @Override
  public String getRuntime() {
    return runtime;
  }

  @Override
  public String getProductName() {
    return productName;
  }

  @Override
  public String getProductVersion() {
    return productVersion;
  }
}
