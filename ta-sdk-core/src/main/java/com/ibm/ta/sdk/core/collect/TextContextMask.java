/*
 * (C) Copyright IBM Corp. 2019,2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.ta.sdk.core.collect;

import com.ibm.ta.sdk.spi.collect.ContentMask;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class TextContextMask implements ContentMask {
  private List<String> files;
  private List<AbstractMap.SimpleEntry<String, String>> regexs;

  public TextContextMask(List<String> files, List<AbstractMap.SimpleEntry<String, String>> regexs) {
    if (files == null) {
      this.files = new ArrayList<>();
    } else {
      this.files = files;
    }

    if (regexs == null) {
      this.regexs = new ArrayList<>();
    } else {
      this.regexs = regexs;
    }
  }

  @Override
  public List<String> getFiles() {
    return files;
  }

  @Override
  public List<String> mask(List<String> content) {
    List<String> outContent = new LinkedList<>();

    for (String line : content) {
      for (AbstractMap.SimpleEntry<String, String> regex : regexs) {
        line = line.replaceAll(regex.getKey(), regex.getValue());
      }
      outContent.add(line);
    }

    return outContent;
  }
}
