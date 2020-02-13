/*
 * (C) Copyright IBM Corp. 2019,2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.ta.sdk.spi.recommendation;

import java.util.List;
import java.util.Map;

public interface Occurrence {

  // key/description name of each field to identify the identify the occurrence instance
  Map<String, String> getFieldKeys();

  // key of occurrence field where multiple occurences with the same value are counted only once
  String getUniqueCountKey();

  // number of instances of this issue occurrence
  Integer getOccurrencesCount();

  // value of each occurrence instance
  List<Map<String, String>> getOccurrencesInstances();

  // add an occurrence instance
  void addOccurence(Map<String, String> occurence);

  // add a list of occurrence instances
  void addOccurences(List<Map<String, String>> occurence);

}
