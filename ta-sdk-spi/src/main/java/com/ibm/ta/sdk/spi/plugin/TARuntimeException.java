/*
 * (C) Copyright IBM Corp. 2019,2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.ta.sdk.spi.plugin;

public class TARuntimeException extends RuntimeException {

  public TARuntimeException(String message) {
    super(message);
  }

  public TARuntimeException(String message, Throwable cause) {
    super(message, cause);
  }

  public TARuntimeException(Throwable cause) {
    super(cause);
  }
}
