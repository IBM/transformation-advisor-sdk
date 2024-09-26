/*
 * (C) Copyright IBM Corp. 2019,2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.ta.sdk.core.assessment;

import com.google.gson.annotations.Expose;
import com.ibm.ta.sdk.spi.recommendation.*;

import java.util.*;

public class GenericIssue implements Issue {
    @Expose
    protected String id;

    @Expose
    protected String title;

    @Expose
    protected float cost;

    @Expose
    protected float issueOverhead;

    @Expose
    protected float occurrencesCost;

    @Expose
    protected String complexityRule;

    @Expose
    protected List<String> solutionText;

    @Expose
    protected String severity;

    @Expose
    protected Map<String, String> occurrencesFields;

    @Expose
    protected int occurrencesCount;

    @Expose
    protected List<Map<String, String>> occurrences;

    @Expose
    protected float costCappingThreshold;

    private IssueRule issueRule;

    private Occurrence occurrence;

    private ComplexityContribution complexityContribution;

    private IssueCategory issueCategory;

    private String targetName;

    public GenericIssue(IssueRule issueRule, String targetName) {
        this.issueRule = issueRule;
        this.targetName = targetName;

        occurrence = new GenericOccurrence(this.issueRule);

        // init gson fields
        init();
    }

    @Override
    public String getId() {
        return issueRule.getId();
    }

    @Override
    public String getTitle() {
        return issueRule.getTitle();
    }

    @Override
    public IssueCategory getCategory() {
        return issueCategory;
    }

    public void setCategory(IssueCategory issueCategory) {
        this.issueCategory = issueCategory;
    }

    @Override
    public List<String> getSolutionText() {
        return issueRule.getTargetedSolutionText(targetName);
    }

    @Override
    public Float getCost() {
        cost = issueOverhead + (occurrence.getOccurrencesCount() * occurrencesCost);
        if (Float.compare(cost, this.costCappingThreshold) > 0) {
            cost = costCappingThreshold;
        }
        return cost;
    }

    @Override
    public Float getOverheadCost() {
        return issueRule.getIssueOverhead();
    }

    @Override
    public Float getOccurrenceCost() {
        return issueRule.getOccurrencesCost();
    }

    @Override
    public Severity getSeverity() {
        return Severity.valueOf(issueRule.getSeverity());
    }

    @Override
    public ComplexityContribution getComplexityContribution() {
        return complexityContribution;
    }

    @Override
    public void setComplexityContribution(ComplexityContribution complexityContribution) {
        this.complexityContribution = complexityContribution;
        complexityRule = complexityContribution.getId();
    }

    protected void setCostCappingThreshold(float newCappingThreshold) {
        this.costCappingThreshold = newCappingThreshold;
    }

    @Override
    public Occurrence getOccurrence() {
        return occurrence;
    }

    @Override
    public Integer getOccurrencesCount() {
        return occurrencesCount;
    }

    public void addOccurences(List<Map<String, String>> occurences) {
        occurrence.addOccurences(occurences);
        occurrencesCount = occurrence.getOccurrencesCount();
    }

    public void init() {
        id = getId();
        title = getTitle();
        cost = getCost();
        issueOverhead = getOverheadCost();
        occurrencesCost = getOccurrenceCost();
        solutionText = getSolutionText();
        severity = getSeverity().name();
        occurrencesFields = getOccurrence().getFieldKeys();
        occurrences = getOccurrence().getOccurrencesInstances();
        occurrencesCount = getOccurrencesCount();
    }

    @Override
    public String toString() {
        return "id=" + getId() + ", " +
                "title=" + getTitle() + ", " +
                "category=" + getCategory().getId() + ", " +
                "severity=" + getSeverity() + ", " +
                "cost=" + getCost() + ", " +
                "overheadCost=" + getOverheadCost() + ", " +
                "occurrenceCost=" + getOccurrenceCost() + ", " +
                "occurrence=" + getOccurrence();
    }

}