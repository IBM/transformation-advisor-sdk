/*
 * (C) Copyright IBM Corp. 2019,2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.ta.sdk.spi.plugin;

import java.util.ArrayList;
import java.util.List;

public class CliInputOption {

  // Built in options
  public static final String OPT_TARGET      = "target";
  public static final String OPT_TARGET_DESC = "Select by Target ID";

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

  public static CliInputOption buildTargetOption() {
    return new CliInputOption(null, OPT_TARGET, OPT_TARGET_DESC,
            true, false, null, null);
  }

  public String getShortArg() {
    return shortArg;
  }

  public String getLongArg() {
    return longArg;
  }

  public void setShortArg(String shortArg) {
    this.shortArg = shortArg;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public void setValueDisplayName(String valueDisplayName) {
    this.valueDisplayName = valueDisplayName;
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

  /**
   * Iterates through all CliInputOptions and retrieves values for options with the matching short name
   * @param options List of CliInputOption to retrieve values from
   * @param optionShotName Short name of the option to retrieve values for
   * @return List of values for the option
   */
  public static List<String> getCliOptionValuesByShortName(List<CliInputOption> options, String optionShotName) {
    List<String> values = new ArrayList<>();
    for (CliInputOption cliOption : options) {
      if (cliOption.getShortArg().equals(optionShotName)) {
        values.add(cliOption.getValue());
      }
    }
    return values;
  }

  /**
   * terates through all CliInputOptions and retrieves values for options with the matching long name
   * @param options List of CliInputOption to retrieve values from
   * @param optionLongName Long name of the option to retrieve values for
   * @return List of values for the option
   */
  public static List<String> getCliOptionValuesByLongName(List<CliInputOption> options, String optionLongName) {
    List<String> values = new ArrayList<>();
    for (CliInputOption cliOption : options) {
      if (cliOption.getLongArg().equals(optionLongName)) {
        values.add(cliOption.getValue());
      }
    }
    return values;
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
