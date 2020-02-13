/*
 * (C) Copyright IBM Corp. 2019,2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.ta.sdk.spi.plugin;

public class TAException extends Exception {

  public TAException(String message) {
    super(message);
  }

  public TAException(String message, Throwable cause) {
    super(message, cause);
  }

  public TAException(Throwable cause) {
    super(cause);
  }

}
