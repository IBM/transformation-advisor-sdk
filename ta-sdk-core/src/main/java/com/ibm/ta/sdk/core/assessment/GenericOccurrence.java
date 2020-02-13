/*
 * (C) Copyright IBM Corp. 2019,2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.ta.sdk.core.assessment;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.ibm.ta.sdk.spi.recommendation.Occurrence;

import java.util.*;

public class GenericOccurrence implements Occurrence {
  private Map<String, String> fields;

  private List<Map<String, String>> occurrenceValues = new ArrayList<Map<String, String>>();

  private IssueRule issueRule;

  private String uniqueCountKey;

  private List<String> uniqueKeyValues = new ArrayList<String>();

  private int occurrencesCount;

  public GenericOccurrence(IssueRule issueRule) {
    this.issueRule = issueRule;
  }

  @Override
  public Map<String, String> getFieldKeys() {
    if (fields == null) {
      fields = new LinkedHashMap<String, String>();
      JsonObject occurrenceAttr = issueRule.getMatchCriteria().getOccurrenceAttr();
      Iterator<String> itKeys = occurrenceAttr.keySet().iterator();
      while (itKeys.hasNext()) {
        String attrKey = itKeys.next();
        String attrTitle = occurrenceAttr.getAsJsonObject(attrKey).get(IssueRule.OCCURRENCE_TITLE_ATTR).getAsString();
        fields.put(attrKey, attrTitle);
      }
    }

    return fields;
  }

  @Override
  public List<Map<String, String>> getOccurrencesInstances() {
    return occurrenceValues;
  }

  @Override
  public String getUniqueCountKey() {
    if (uniqueCountKey == null) {
      JsonObject occurrenceAttr = issueRule.getMatchCriteria().getOccurrenceAttr();
      if (occurrenceAttr != null) {
        Iterator<String> itKeys = occurrenceAttr.keySet().iterator();
        while (itKeys.hasNext()) {
          String attrKey = itKeys.next();
          JsonElement attrCountUnique = occurrenceAttr.getAsJsonObject(attrKey).get(IssueRule.OCCURRENCE_COUNT_UNIQUE_ATTR);
          if (attrCountUnique != null && !attrCountUnique.isJsonNull()) {
            uniqueCountKey = attrKey;
            break;
          }
        }
      }
    }
    return uniqueCountKey;
  }

  @Override
  public Integer getOccurrencesCount() {
    return occurrencesCount;
  }

  public void addOccurence(Map<String, String> occurence) {
    occurrenceValues.add(occurence);

    // Sort
    Collections.sort(occurrenceValues, new Comparator<Map<String, String>>() {
      public int compare(Map<String, String> o1, Map<String, String> o2) {
        JsonObject occurrenceAttr = issueRule.getMatchCriteria().getOccurrenceAttr();
        Iterator<String> itAttrKeys = occurrenceAttr.keySet().iterator();
        while (itAttrKeys.hasNext()) {
          String attrKey = itAttrKeys.next();
          String o1Value = o1.get(attrKey);
          String o2Value = o2.get(attrKey);
          if (o1Value != o2Value) {
            if (o1Value == null) {
              return -1;
            }
            if (o2Value == null) {
              return 1;
            }
            if (!o1Value.equals(o2Value)) {
              return o1Value.compareTo(o2Value);
            }
          }
        }
        return 0;
      }
    });

    // Increment occurrencesCount
    String uniqueKey = getUniqueCountKey();
    if (uniqueKey != null) {
      String ocUniqueKeyValue = occurence.get(uniqueKey);
      if (ocUniqueKeyValue != null && !"".equals(ocUniqueKeyValue)) {
        if (!uniqueKeyValues.contains(ocUniqueKeyValue)) {
          uniqueKeyValues.add(ocUniqueKeyValue);
          occurrencesCount++;
        }
      }
    } else {
      occurrencesCount++;
    }
  }

  public void addOccurences(List<Map<String, String>> occurences) {
    for (Map<String, String> occurence : occurences) {
      addOccurence(occurence);
    }
  }

  @Override
  public String toString() {
    return "uniqueCountKey=" + uniqueCountKey + ", " +
            "uniqueKeyValues=" + uniqueKeyValues + ", " +
            "occurrenceValues=" + occurrenceValues + ", " +
            "occurrencesCount=" + occurrencesCount;
  }

}
