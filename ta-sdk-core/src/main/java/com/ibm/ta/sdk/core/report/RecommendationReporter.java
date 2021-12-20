/*
 * (C) Copyright IBM Corp. 2019,2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.ta.sdk.core.report;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.ibm.ta.sdk.spi.recommendation.ModDimension;
import com.ibm.ta.sdk.spi.report.ReportGenerator;
import com.google.gson.JsonObject;
import com.ibm.ta.sdk.spi.plugin.TAException;
import com.ibm.ta.sdk.spi.report.Report;
import com.ibm.ta.sdk.spi.report.ReportType;
import org.tinylog.Logger;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class RecommendationReporter implements ReportGenerator {

    // HTML templates
    private static final String TEMPLATE = "/templates/recommendations.template";
    private static final String TEMPLATE_ISSUES_TABLE = "/templates/issuesBelongToSameCategoryTable.template";
    private static final String TEMPLATE_ISSUES_TABLE_ROW = "/templates/issueTableRow.template";

    // variables to be replaced in the recommendation template file
    private static final String ASSESSMENT_UNIT_NAME_VAR = "{ASSESSMENT_UNIT_NAME}";
    private static final String PRODUCT_NAME_VAR = "{PRODUCT_NAME}";
    private static final String PRODUCT_VERSION_VAR = "{PRODUCT_VERSION}";
    private static final String TARGET_COLUMN_VAR = "{TARGET_COLUMN}";
    private static final String OVERALL_COMPLEXITY_SCORE_VAR = "{OVERALL_COMPLEXITY_SCORE}";
    private static final String NUM_OF_RED_ISSUES_VAR = "{NUM_OF_RED_ISSUES}";
    private static final String NUM_OF_YELLOW_ISSUES_VAR = "{NUM_OF_YELLOW_ISSUES}";
    private static final String NUM_OF_GREEN_ISSUES_VAR = "{NUM_OF_GREEN_ISSUES}";
    private static final String ISSUE_TABLES_VAR = "{ISSUE_TABLES}";

    // variables to be replaced in the issues table template file
    private static final String ISSUE_CATEGORY_TITLE_VAR = "{ISSUE_CATEGORY_TITLE}";
    private static final String ISSUES_TABLE_BODY_VAR = "{ISSUES_TABLE_BODY}";

    // variables to be replaced in the issues table row template file
    private static final String ISSUE_OCCURANCE_COUNT_VAR = "{ISSUE_OCCURANCE_COUNT}";
    private static final String ISSUE_ID_VAR = "{ISSUE_ID}";
    private static final String ISSUE_TITLE_VAR = "{ISSUE_TITLE}";
    private static final String ISSUE_COST_VAR = "{ISSUE_COST}";
    private static final String ISSUE_OVERHEAD_VAR = "{ISSUE_OVERHEAD}";
    private static final String ISSUE_OCCURANCES_COST_VAR = "{ISSUE_OCCURANCES_COST}";
    private static final String ISSUE_SEVERITY_VAR = "{ISSUE_SEVERITY}";
    private static final String ISSUE_SOLUTION_TEXT_VAR = "{ISSUE_SOLUTION_TEXT}";


    // template String  used in the issues table
    // exmple of solution text: <ul><li>Update other Cluster members to use the correct IP Addresses after migrating</li></ul>
    private static final String TEMPLATE_ISSUES_SOLUTION_TEXT_ITEM_VAR = "{TEMPLATE_ISSUES_SOLUTION_TEXT_ITEM}";
    private static final String TEMPLATE_ISSUES_SOLUTION_TEXT_VAR = "{TEMPLATE_ISSUES_SOLUTION_TEXT}";
    private static final String TEMPLATE_ISSUES_SOLUTION_TEXT_TEMPLATE = "<ul>" + TEMPLATE_ISSUES_SOLUTION_TEXT_VAR + "</ul>";
    private static final String TEMPLATE_ISSUES_SOLUTION_TEXT_ITEM_TEMPLATE = "<li>" + TEMPLATE_ISSUES_SOLUTION_TEXT_ITEM_VAR + "</li>";

    //exmplae of occurancesFields: <table><tr><th>cluster</th></tr><tr><td>INVENTORY</td></tr></table>
    private static final String ISSUE_OCCURANCES_FIELDS_VAR = "{ISSUE_OCCURANCES_FIELDS}";

    //exmplae of occurances: <table><tr><th width="33%">type</th><th width="30%">object</th><th width="30%">osGroup</th></tr>
    //                          <tr><td>channel</td><td>CHANNELNAME</td><td>jc</td></tr>
    //                          <tr><td>queue</td><td>LONDON</td><td>mqm</td></tr>
    //                          <tr><td>queue</td><td>LONDON</td><td>root</td></tr>
    //                      </table>
    private static final String ISSUE_OCCURANCES_VAR = "{ISSUE_OCCURANCES}";
    private static final String HIDDEN_ROW_COUNT_VAR = "{HIDDEN_ROW_COUNT}";
    private static final String ISSUE_OCCURANCES_TABLE_HEADER_VAR = "{ISSUE_OCCURANCES_TABLE_HEADER}";
    private static final String ISSUE_OCCURANCES_TABLE_BODY_VAR = "{ISSUE_OCCURANCES_TABLE_BODY}";
    private static final String TEMPLATE_OCCURANCES = "<table>" + ISSUE_OCCURANCES_TABLE_HEADER_VAR + ISSUE_OCCURANCES_TABLE_BODY_VAR + "</table>";


    private JsonObject recommendationJson = null;
    private String assessmentName = null;

    public RecommendationReporter(String assessmentName, JsonObject recommendationJson){
        this.recommendationJson = recommendationJson;
        this.assessmentName = assessmentName;
    }

    @Override
    public List<Report> generateHTMLReports() throws TAException {

        List generatedHTMLFiles = new ArrayList();

        String templateStr = loadTemplate(TEMPLATE);

        RecommendationReport recommendation = null;
        try {
            recommendation = parseJSON();
        } catch (Exception e) {
            Logger.error("Failed to generate objects from JSON", e);
            throw new TAException(e);
        }

        List<Map<String, String>> issueCategories = recommendation.getIssueCategories();
        String middleware = recommendation.getMiddleware();
        String version = recommendation.getVersion();

        List<AssessmentUnitReport> assessmentUnits = recommendation.getAssessmentUnits();
        for (AssessmentUnitReport assessmentUnit: assessmentUnits){
            List<Report> htmlFiles = null;

            htmlFiles = generateHTMLForOneAssessmentUnit(middleware, version, assessmentUnit, templateStr, issueCategories);

            generatedHTMLFiles.addAll(htmlFiles);
        }

        return generatedHTMLFiles;
    }

    private List<Report> generateHTMLForOneAssessmentUnit(String middleware, String version,
                AssessmentUnitReport assessmentUnit, String templateStr, List<Map<String, String>> issueCategories) throws TAException {
        List<Report> generatedHTMLs = new ArrayList<Report>();
        List<TargetReport> targets = assessmentUnit.getTargets();
        String assessmentUnitName = assessmentUnit.getName();
        for (TargetReport target : targets){
            Report reportHTML = generateHTMLForOneTarget(middleware, version, assessmentUnitName, target, templateStr, issueCategories);
            generatedHTMLs.add(reportHTML);
        }
        return generatedHTMLs;
    }

    private Report generateHTMLForOneTarget(String middleware, String version, String assessmentUnitName,
                TargetReport target, String templateStr, List<Map<String, String>> issueCategories) throws TAException {
        String id = target.getTargetId();
        String targetId = assessmentUnitName + "-" + id;

        String resultStr = templateStr.replace(ASSESSMENT_UNIT_NAME_VAR, assessmentUnitName);
        resultStr = resultStr.replace(PRODUCT_NAME_VAR, middleware);
        resultStr = resultStr.replace(PRODUCT_VERSION_VAR, version);
        resultStr = resultStr.replace(TARGET_COLUMN_VAR, target.getTargetId());
        resultStr = resultStr.replace(OVERALL_COMPLEXITY_SCORE_VAR, target.getOverallComplexityScore());
        resultStr = resultStr.replace(NUM_OF_RED_ISSUES_VAR, new Integer(target.getNumberOfRedIssues()).toString());
        resultStr = resultStr.replace(NUM_OF_YELLOW_ISSUES_VAR, new Integer(target.getNumberOfYellowIssues()).toString());
        resultStr = resultStr.replace(NUM_OF_GREEN_ISSUES_VAR, new Integer(target.getNumberOfGreenIssues()).toString());

        Logger.debug("\n**************Gen HTML for " + targetId + "******************\n");
        String issueTablesStr = "";
        List<IssuesSameCategory> issues = target.getIssuesSameCategory();
        Logger.debug("issues is " + issues);
        int nextTableRowIndex = 1;
        for (IssuesSameCategory issuesSameCategory : issues){
            GenerationResult oneIssueCategoryTableGenResult = genIssueTableForOneCategory(issuesSameCategory, nextTableRowIndex);
            if (oneIssueCategoryTableGenResult != null) {
                String oneIssueCategoryTableStr = oneIssueCategoryTableGenResult.getStringResult();
                issueTablesStr += oneIssueCategoryTableStr;
                nextTableRowIndex += oneIssueCategoryTableGenResult.getNumberOfIssuesGenerated();
            }
        }

        resultStr = resultStr.replace(ISSUE_TABLES_VAR, issueTablesStr);

        com.ibm.ta.sdk.spi.recommendation.Target targetResult = new com.ibm.ta.sdk.spi.recommendation.Target(){
            @Override
            public String getTargetId() {
                return target.getTargetId();
            }

            @Override
            public List<ModDimension> getDimensions() {
                List<ModDimension> dimensions = new ArrayList<>();
                for (JsonElement dimJsonE : target.getDimensions()) {
                    JsonObject dimJson = dimJsonE.getAsJsonObject();
                    List<JsonElement> values = new ArrayList<>();
                    dimJson.get("values").getAsJsonArray().forEach( v -> values.add(v));
                    dimensions.add(new ModDimension(
                                dimJson.get("name").getAsString(),
                                values,
                                dimJson.get("defaultValue")));

                }
                return dimensions;
            }
        };

        String finalResultStr = resultStr;
        Report result = new Report() {
            @Override
            public String getAssessmentName() {
                return assessmentName;
            }

            @Override
            public String getAssessmentUnitName() {
                return assessmentUnitName;
            }

            @Override
            public com.ibm.ta.sdk.spi.recommendation.Target getTarget() {
                return targetResult;
            }

            @Override
            public ReportType getReportType() {
                return ReportType.HTML;
            }

            @Override
            public byte[] getReport() {
                return finalResultStr.getBytes();
            }
        };
        return result;
    }


    private GenerationResult genIssueTableForOneCategory(IssuesSameCategory issuesSameCategory, int nextTableRowIndex) throws TAException {
        String issueCategoryTitle = issuesSameCategory.getIssueCategoryTitle();
        String templateStr = loadTemplate(TEMPLATE_ISSUES_TABLE);

        String resultStr = templateStr.replace(ISSUE_CATEGORY_TITLE_VAR, issueCategoryTitle);

        String issueTableBodyStr = "";
        List<IssueReport> issues = issuesSameCategory.getIssues();
        int issueGenerated = 0;
        for (IssueReport issue : issues) {
            GenerationResult issueTableResult = genIssueTableRow(issue, nextTableRowIndex);
            if (issueTableResult != null) {
                String oneIssueTableRowStr = issueTableResult.getStringResult();
                issueTableBodyStr += oneIssueTableRowStr;
                nextTableRowIndex++;
            }
        }
        resultStr =  resultStr.replace(ISSUES_TABLE_BODY_VAR, issueTableBodyStr);
        return new GenerationResult(resultStr, issues.size());

    }

    private GenerationResult genIssueTableRow(IssueReport issue, int nextTableRowIndex) throws TAException {
        String templateStr = loadTemplate(TEMPLATE_ISSUES_TABLE_ROW);

        String issueOccuranceCount = issue.getOccurrencesCount();
        String resultStr = templateStr.replace(ISSUE_OCCURANCE_COUNT_VAR, issueOccuranceCount);
        resultStr = resultStr.replace(ISSUE_ID_VAR, issue.getId());
        resultStr = resultStr.replace(ISSUE_TITLE_VAR, issue.getTitle());
        resultStr = resultStr.replace(ISSUE_COST_VAR, issue.getCost());
        resultStr = resultStr.replace(ISSUE_OVERHEAD_VAR, issue.getIssueOverhead());
        resultStr = resultStr.replace(ISSUE_OCCURANCES_COST_VAR, issue.getOccurancesCost());
        resultStr = resultStr.replace(ISSUE_SEVERITY_VAR, issue.getSeverity());

        List<String> solutionTextList = issue.getSolutionTextList();
        String solutionTextItems = "";
        for (String oneSolutionText : solutionTextList) {
            solutionTextItems += TEMPLATE_ISSUES_SOLUTION_TEXT_ITEM_TEMPLATE.replace(TEMPLATE_ISSUES_SOLUTION_TEXT_ITEM_VAR, oneSolutionText);
        }
        String solutionText = TEMPLATE_ISSUES_SOLUTION_TEXT_TEMPLATE.replace(TEMPLATE_ISSUES_SOLUTION_TEXT_VAR, solutionTextItems);
        resultStr = resultStr.replace(TEMPLATE_ISSUES_SOLUTION_TEXT_VAR, solutionText);

            // Nov 7, remove occurancesFields from HTML table
//            Map<String, String> occurancesFields = issue.getOccurrencesFields();
//            String occurancesFieldsStr = "";
//            if (occurancesFields != null && occurancesFields.size() > 0) {
//                occurancesFieldsStr += "<table>";
//                Set<String> occurancesFieldKeys = occurancesFields.keySet();
//                String tableRowStr = "";
//                for (String occurancesFiledKey : occurancesFieldKeys) {
//                    occurancesFieldsStr += "<tr><th>" + occurancesFiledKey + "</th></tr>";
//                    String value = occurancesFields.get(occurancesFiledKey);
//                    occurancesFieldsStr += "<tr><td>" + value + "</td></tr>";
//                }
//                occurancesFieldsStr += "</table>";
//            }
//            Logger.debug("occurancesFieldsStr is " + occurancesFieldsStr);
//            resultStr = resultStr.replace(ISSUE_OCCURANCES_FIELDS_VAR, occurancesFieldsStr);

        List<Map<String, String>> occurancesList = issue.getOccurances();
        String occurancesStr = "";
        if (occurancesList != null && occurancesList.size() > 0) {
            // header
            Map<String, String> firstOccuranceMap = occurancesList.get(0);
            Set<String> headerSet = firstOccuranceMap.keySet();
            String headerStr = "<tr><th>index</th>";
            for (String header : headerSet) {
                headerStr += "<th>" + header + "</th>";
            }
            headerStr += "</tr>";
            occurancesStr = TEMPLATE_OCCURANCES.replace(ISSUE_OCCURANCES_TABLE_HEADER_VAR, headerStr);

            // body: each occurance take one table row
            String occuranceRowsStr = "";
            int index = 0;
            for (Map<String, String> oneOccuranceMap : occurancesList) {
                index ++;
                String oneRow = "<tr><td>" + new Integer(index).toString() + "</td>";
                Set<String> oneOccuranceKeys = oneOccuranceMap.keySet();
                for (String oneOccuranceKey : oneOccuranceKeys) {
                    Object oneOccuranceValue = oneOccuranceMap.get(oneOccuranceKey);
                    oneRow += "<td>" + oneOccuranceValue.toString() + "</td>";
                }
                oneRow += "</tr>";
                occuranceRowsStr += oneRow;
            }
            occurancesStr = occurancesStr.replace(ISSUE_OCCURANCES_TABLE_BODY_VAR, occuranceRowsStr);
        }

        resultStr = resultStr.replace(ISSUE_OCCURANCES_VAR, occurancesStr);
        resultStr = resultStr.replace(HIDDEN_ROW_COUNT_VAR, new Integer(nextTableRowIndex).toString());
        return new GenerationResult(resultStr, nextTableRowIndex);

    }

    private String loadTemplate(String templateFilePath) throws TAException{
        //Read the template and load it
        InputStream is = null;
        try {
            BufferedReader buf = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream(templateFilePath)));

            String line = buf.readLine();
            StringBuilder sb = new StringBuilder();

            while(line != null){
                sb.append(line).append("\n");
                line = buf.readLine();
            }
            return sb.toString();
        } catch(Exception e) {
            Logger.error("Not able to read template: " + templateFilePath, e);
            throw new TAException(e);
        }finally{
            try {
                if (is != null) {
                    is.close();
                    is = null;
                }
            } catch(Exception e2) {
                Logger.warn("Not able to close template file: " + templateFilePath, e2);
            }
        }
    }

    private RecommendationReport parseJSON(){
        RecommendationReport recommendation = new RecommendationReport();
        
        String domain = this.recommendationJson.get("domain").getAsString();
        Logger.debug("domain is " + domain);
        recommendation.setDomain(domain);
        recommendation.setMiddleware(recommendationJson.get("middleware").getAsString());
        recommendation.setVersion(recommendationJson.get("version").getAsString());
        recommendation.setCollectionUnitName(recommendationJson.get("collectionUnitName").getAsString());
        recommendation.setCollectionUnitType(recommendationJson.get("collectionUnitType").getAsString());

        // issueCategories
        JsonObject issueCategories = this.recommendationJson.getAsJsonObject("issueCategories");

        Map<String, String> issueCategoryMap = new HashMap<String, String>();
        Set<String> issueCatKeys = issueCategories.keySet();
        for (String issueCatKey : issueCatKeys) {
            JsonObject issueCatTitleJO = issueCategories.getAsJsonObject(issueCatKey);
            String issueCatTitle = issueCatTitleJO.get("title").getAsString();
            Logger.debug("issueCatKey is " + issueCatKey + "; issueCatTitle is " + issueCatTitle);
            issueCategoryMap.put(issueCatKey, issueCatTitle);
        }

        //assessmentUnits
        JsonArray assessmentUnits = this.recommendationJson.getAsJsonArray("assessmentUnits");
        Logger.debug("assessmentUnits is " + assessmentUnits);

        for (Object assessmentUnit : assessmentUnits) {
            JsonObject assessmentUnitsJO = (JsonObject) assessmentUnit;
            Logger.debug("assessmentUnitsJO is " + assessmentUnitsJO);
            String name = assessmentUnitsJO.get("name").getAsString();
            AssessmentUnitReport aU = new AssessmentUnitReport(name);

            JsonArray targetsJA = (JsonArray) assessmentUnitsJO.get("targets");
            for (Object targetObj : targetsJA) {
                JsonObject targetJO = (JsonObject) targetObj;
                String id = targetJO.get("target")== null? "": targetJO.get("target").getAsString();
                JsonObject summary = (JsonObject) targetJO.get("summary");
                JsonObject issuesInSummary = (JsonObject) summary.get("issues");
                int numOfRedIssues = issuesInSummary.get("severe") == null ? 0 : issuesInSummary.get("severe").getAsInt();
                if (numOfRedIssues == 0){
                    numOfRedIssues = issuesInSummary.get("critical") == null ? 0 : issuesInSummary.get("critical").getAsInt();
                }
                int numOfYellowIssues = issuesInSummary.get("warning") == null ? 0 : issuesInSummary.get("warning").getAsInt();
                if (numOfYellowIssues == 0){
                    numOfYellowIssues = issuesInSummary.get("potential") == null ? 0 : issuesInSummary.get("potential").getAsInt();
                }
                int numOfGreenIssues = issuesInSummary.get("info") == null ? 0 : issuesInSummary.get("simple").getAsInt();
                if (numOfGreenIssues == 0){
                    numOfGreenIssues = issuesInSummary.get("suggested") == null ? 0 : issuesInSummary.get("suggested").getAsInt();
                }

                JsonObject complexity = (JsonObject) summary.get("complexity");
                String overallComplexityScore = complexity.get("score").getAsString();
                TargetReport target = new TargetReport(id, new JsonArray(), overallComplexityScore, numOfRedIssues, numOfYellowIssues, numOfGreenIssues);

                JsonObject issuesJO = (JsonObject) targetJO.get("issues");
                Set<String> issuesKeySet = issuesJO.keySet();
                for (String issuesKey : issuesKeySet) {
                    Logger.debug("--issuesKey is " + issuesKey);
                    IssuesSameCategory issuesSameCategory = new IssuesSameCategory(issuesKey, issueCategoryMap.get(issuesKey));
                    JsonArray issuesJA = (JsonArray) issuesJO.get(issuesKey);

                    for (Object issueOb : issuesJA) {
                        JsonObject issueJO = (JsonObject) issueOb;
                        Logger.debug("----issueJO is " + issueJO);
                        IssueReport issue = new IssueReport(issueJO);
                        issuesSameCategory.addIssue(issue);
                    }
                    target.addIssuesSameCategory(issuesSameCategory);
                }
                aU.addTarget(target);
            }
            recommendation.addAssessmentUnit(aU);
        }
        return recommendation;
    }

    class GenerationResult {
        private String stringResult;
        private int numberOfIssuesGenerated;

        public GenerationResult(String stringResult, int numberOfIssuesGenerated){
            this.stringResult = stringResult;
            this.numberOfIssuesGenerated = numberOfIssuesGenerated;
        }

        public String getStringResult(){
            return this.stringResult;
        }

        public int getNumberOfIssuesGenerated(){
            return this.numberOfIssuesGenerated;
        }
    }

    /***********************************************************
     *
     *                   Unit testing methods
     *
     ***********************************************************/
    public static void main (String[] args)  {
        try {
            JsonObject recJO = new JsonParser().parse(new FileReader("/Users/jgao/ta/features/RecommendationConvertingTool/Installation4/recommendations.json")).getAsJsonObject();
            RecommendationReporter reporter = new RecommendationReporter("Installation1", recJO);
            List<Report> generatedHTMLs = reporter.generateHTMLReports();
            System.out.print("generated HTML files are: ");
            for (Report generatedHTML : generatedHTMLs){
                String htmlFileName = "/Users/jgao/ta/features/RecommendationConvertingTool/Installation4/unitTestingOutput/" + generatedHTML.getAssessmentUnitName() + "-" + generatedHTML.getAssessmentUnitName() + "-" + generatedHTML.getTarget().getTargetId() + ".html";
                System.out.print(htmlFileName + " ");
                writeUsingOutputStream(generatedHTML.getReport(), htmlFileName);
            }
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }

    private static void writeUsingOutputStream(byte[] htmlString, String htmlFileName) {
        OutputStream os = null;
        try {
            File htmlFile = new File(htmlFileName);
            if (htmlFile.exists()){
                htmlFile.delete();
            }
            if (!htmlFile.getParentFile().exists()){
                htmlFile.getParentFile().mkdirs();
            }
            os = new FileOutputStream(new File(htmlFileName));
            os.write(htmlString, 0, htmlString.length);
        } catch (IOException e) {
            Logger.error("Not able to write to " + htmlFileName, e);
        }finally{
            try {
                os.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
