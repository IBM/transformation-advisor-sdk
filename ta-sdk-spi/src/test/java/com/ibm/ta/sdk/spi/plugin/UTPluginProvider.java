/*
 * (C) Copyright IBM Corp. 2019,2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.ta.sdk.spi.plugin;

import com.ibm.ta.sdk.spi.collect.DataCollection;
import com.ibm.ta.sdk.spi.recommendation.Recommendation;
import com.ibm.ta.sdk.spi.report.Report;

import java.util.List;

public class UTPluginProvider implements PluginProvider {
    private String version = "1.0.0";
    private String domain = "testDomain";
    private String middleware = "testMiddleware";

    private CliInputCommand collectCommand;
    private CliInputCommand assessCommand;
    private CliInputCommand reportCommand;

    private List<DataCollection> dataCollection;
    private List<Recommendation> recommendations;
    private List<Report> reports;

    @Override
    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    @Override
    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    @Override
    public String getMiddleware() {
        return middleware;
    }

    public void setMiddleware(String middleware) {
        this.middleware = middleware;
    }

    public void setCollectCommand(CliInputCommand collectCommand) {
        this.collectCommand = collectCommand;
    }

    @Override
    public CliInputCommand getCollectCommand() {
        return this.collectCommand;
    }

    public void setDataCollection(List<DataCollection>  dataCollection) {
        this.dataCollection = dataCollection;
    }

    @Override
    public List<DataCollection> getCollection(CliInputCommand collectCommand) throws TAException {
        return this.dataCollection;
    }

    public void setAssessCommand(CliInputCommand assessCommand) {
        this.assessCommand = assessCommand;
    }

    @Override
    public CliInputCommand getAssessCommand() {
        return this.assessCommand;
    }

    public void setRecommendations(List<Recommendation> recommendations) {
        this.recommendations = recommendations;
    }

    @Override
    public List<Recommendation> getRecommendation(CliInputCommand assessCommand) throws TAException {
        return this.recommendations;
    }

    public void setReportCommand(CliInputCommand reportCommand) {
        this.reportCommand = reportCommand;
    }

    @Override
    public CliInputCommand getReportCommand() {
        return this.reportCommand;
    }

    public void setReport(List<Report> reports) {
        this.reports = reports;
    }

    @Override
    public List<Report> getReport(String assessmentName, CliInputCommand reportCommand) throws TAException {
        return this.reports;
    }

    @Override
    public CliInputCommand getMigrateCommand() {
        return null;
    }

    @Override
    public void getMigrationBundle(CliInputCommand migrateCommand) throws TAException {

    }
}
