/*
 * (C) Copyright IBM Corp. 2019,2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.ta.sdk.spi.recommendation;

import java.util.List;

public interface Target {
  String getId();

  String getRuntime();

  List<ModDimension> getDimensions();
}
