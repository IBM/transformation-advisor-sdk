/*
 * (C) Copyright IBM Corp. 2019,2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.ta.sdk.spi.assess;

import com.google.gson.annotations.Expose;
import com.ibm.ta.sdk.spi.recommendation.*;

import java.util.List;
import java.util.Map;

public class UTIssue implements Issue {
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

    private Occurrence occurrence;

    private ComplexityContribution complexityContribution;

    private IssueCategory issueCategory;

    public UTIssue(String id, String title, float issueOverhead, float occurrencesCost, List<String> solutionText,
                   String severity, IssueCategory issueCategory, Occurrence occurrence,
                   ComplexityContribution complexityContribution) {
        this.id = id;
        this.title = title;
        // cost = getCost();
        this.issueOverhead = issueOverhead;
        this.occurrencesCost = occurrencesCost;
        this.solutionText = solutionText;
        this.severity = severity;
        this.complexityRule = complexityRule;
        this.issueCategory = issueCategory;
        this.occurrence = occurrence;
        this.occurrencesFields = occurrence.getFieldKeys();
        this.occurrences = occurrence.getOccurrencesInstances();
        this.occurrencesCount = occurrence.getOccurrencesCount();
        setComplexityContribution(complexityContribution);
        getCost();
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public IssueCategory getCategory() {
        return issueCategory;
    }

    @Override
    public List<String> getSolutionText() {
        return solutionText;
    }

    @Override
    public Float getCost() {
        cost = issueOverhead + (occurrence.getOccurrencesCount() * occurrencesCost);
        return cost;
    }

    @Override
    public Float getOverheadCost() {
        return issueOverhead;
    }

    @Override
    public Float getOccurrenceCost() {
        return occurrencesCost;
    }

    @Override
    public Severity getSeverity() {
        return Severity.valueOf(severity);
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

    @Override
    public Occurrence getOccurrence() {
        return occurrence;
    }

    @Override
    public Integer getOccurrencesCount() {
        return occurrencesCount;
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