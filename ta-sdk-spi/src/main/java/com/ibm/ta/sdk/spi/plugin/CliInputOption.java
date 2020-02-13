/*
 * (C) Copyright IBM Corp. 2019,2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.ta.sdk.spi.plugin;

public class CliInputOption {

  private String shortArg;
  private String longArg;
  private String description;
  private boolean acceptsValue;
  private boolean valueRequired;
  private String valueDisplayName;
  private String value;


  public CliInputOption(String shortArg, String longArg, String description) {
    this(shortArg, longArg, description, false, false, "", null);
  }

  public CliInputOption(String shortArg, String longArg, String description, boolean acceptsValue, boolean valueRequired, String valueDisplayName, String defaultValue) {
    this.shortArg = shortArg==null ? "" : shortArg;
    this.longArg = longArg==null ? "" : longArg;
    this.description = description;
    this.acceptsValue = acceptsValue;
    this.valueRequired = valueRequired;
    this.valueDisplayName = valueDisplayName == null ? "VALUE" : valueDisplayName.toUpperCase();
    this.value = defaultValue;
  }

  protected CliInputOption(CliInputOption option) {
    this.shortArg = option.shortArg;
    this.longArg = option.longArg;
    this.description = option.description;
    this.acceptsValue = option.acceptsValue;
    this.valueRequired = option.valueRequired;
    this.valueDisplayName = option.valueDisplayName;
    this.value = option.value;
  }

  public String getShortArg() {
    return shortArg;
  }

  public String getLongArg() {
    return longArg;
  }

  public String getDescription() {
    return description;
  }

  public boolean acceptsValue() {
    return acceptsValue;
  }

  public boolean requiresValue() {
    return valueRequired;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  public String getUsageHelp() {
    return String.format("%1$-" + 4 + "s", "".equals(shortArg) ? "" : "-" + shortArg + ", ")  +
            String.format("%1$-" + 25 + "s", ("".equals(longArg) ? "" : "--" + longArg) + " " + (acceptsValue ? valueDisplayName : "")) +
            description + (value == null ? "" : "(Default:" + value +")");
  }

  @Override
  public String toString() {
    return "shortArg=" + shortArg +
            ", longArg=" + longArg +
            ", description=" + description +
            ", acceptsValue=" + acceptsValue +
            ", valueRequired=" + valueRequired +
            ", value=" + value;
  }
}
