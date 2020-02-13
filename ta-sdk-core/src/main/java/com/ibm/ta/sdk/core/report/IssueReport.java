/*
 * (C) Copyright IBM Corp. 2019,2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.ta.sdk.core.report;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.*;

public class IssueReport {

    private JsonObject issueJO;

    private String severity = null;
    private String cost;
    private String issueOverhead;
    private String id;
    private String title;
    private String occurrencesCount;
    private String occurrencesCost;
    private List<String> solutionTextList = new ArrayList<>();
    private List<Map<String, String>> occurances = new ArrayList<>();
    private Map<String, String> occurrencesFields = new HashMap<>();

    private int occurancesCount = 0;   // do not need to display, should be used to determine # of rows of occurrences table

    public IssueReport(JsonObject issueJO){
        this.issueJO = issueJO;
        this.severity = issueJO.get("severity").getAsString();
        this.cost = issueJO.get("cost").getAsString();
        this.issueOverhead = issueJO.get("issueOverhead").getAsString();
        this.id = issueJO.get("id").getAsString();
        this.title = issueJO.get("title").getAsString();
        this.occurrencesCount = issueJO.get("occurrencesCount").getAsString();
        this.occurrencesCost = issueJO.get("occurrencesCost").getAsString();

        JsonArray solutionTextJA = issueJO.getAsJsonArray("solutionText");
        for (JsonElement solutionTextObj : solutionTextJA){
            String solutionText = solutionTextObj.getAsString();
            this.solutionTextList.add(solutionText);
        }

        JsonObject occurrencesFields = (JsonObject) issueJO.get("occurrencesFields");
        Set<String> occurrencesFieldsKeySet = occurrencesFields.keySet();
        for (String occurrencesFieldsKey : occurrencesFieldsKeySet){
            this.occurrencesFields.put(occurrencesFieldsKey, occurrencesFields.get(occurrencesFieldsKey).getAsString());
        }

        JsonArray occurancesJA = (JsonArray)issueJO.get("occurrences");
        for (Object occurancesObj : occurancesJA) {
            JsonObject occurancesJO = (JsonObject) occurancesObj;
            Set<String> occurancesJOKeySet = occurancesJO.keySet();
            Map occurance = new HashMap();
            for (String occurancesJOKey : occurancesJOKeySet){
                occurance.put(occurancesJOKey, occurancesJO.get(occurancesJOKey));
            }
            this.occurances.add(occurance);
        }

    }


    public JsonObject getIssueJO() {
        return issueJO;
    }

    public String getSeverity() {
        return severity;
    }

    public String getCost() {
        return cost;
    }

    public String getIssueOverhead() {
        return issueOverhead;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getOccurrencesCount() {
        return occurrencesCount;
    }

    public List<String> getSolutionTextList() {
        return solutionTextList;
    }

    public List<Map<String, String>> getOccurances() {
        return occurances;
    }

    public Map<String, String> getOccurrencesFields() {
        return occurrencesFields;
    }

    public String getOccurancesCost() {
        return occurrencesCost;
    }
}
