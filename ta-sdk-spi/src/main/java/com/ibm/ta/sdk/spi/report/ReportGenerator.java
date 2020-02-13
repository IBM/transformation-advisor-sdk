package com.ibm.ta.sdk.spi.report;

import com.ibm.ta.sdk.spi.plugin.TAException;

import java.util.List;

public interface ReportGenerator {
    public List<Report> generateHTMLReports() throws TAException;
}
