/*
 * (C) Copyright IBM Corp. 2019,2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.ta.sdk.core.detector.xml;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.ibm.ta.sdk.spi.collect.AssessmentUnit;
import com.ibm.ta.sdk.spi.recommendation.Target;
import com.ibm.ta.sdk.core.util.XmlUtils;
import com.ibm.ta.sdk.core.assessment.GenericIssue;
import com.ibm.ta.sdk.core.assessment.IssueMatchCriteria;
import com.ibm.ta.sdk.core.assessment.IssueRule;
import com.ibm.ta.sdk.core.detector.IssueRuleTypeProvider;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class XmlIssueRuleTypeProvider implements IssueRuleTypeProvider {

    public static final String XML_RULE_PROVIDER_NAME = "xml";
    private static final String CRETERIA_KEYNAME = "criteria";
    private static final String DETECT_DTD = "detectDTD";
    private static final String DETECT_ATTR = "detectAttribute";
    private static final String DETECT_ELEMENT = "detectElement";
    private static final String DTD_NAME = "dtdName";
    private static final String XML_FILE = "xmlFile";

    private static Logger logger = LogManager.getLogger(XmlIssueRuleTypeProvider.class.getName());

    @Override
    public String getName() {
        return XML_RULE_PROVIDER_NAME;
    }

    @Override
    public IssueMatchCriteria getIssueMatchCriteria(JsonObject matchCriteriaJson) {
        return new IssueMatchCriteria(matchCriteriaJson, CRETERIA_KEYNAME);
    }

    @Override
    public GenericIssue getIssue(Target target, AssessmentUnit assessmentUnit, IssueRule issueRule) {
        GenericIssue issue = new GenericIssue(issueRule);
        logger.debug("issueRule="+issueRule.getMatchCriteriaJson());
        logger.debug("assessmentUnit config file=" + assessmentUnit.getConfigFiles());

        if (assessmentUnit.getConfigFiles() != null) {
            List<Path> xmlFiles = assessmentUnit.getConfigFiles()
                    .stream()
                    .filter(path ->path.toString().endsWith(XML_RULE_PROVIDER_NAME))
                    .collect(Collectors.toList());
            for (Path file: xmlFiles) {
                issue.addOccurences(getOcurrence(file, issueRule));
            }
        }

        return issue;
    }

    private List<Map<String, String>> getOcurrence(Path xmlFilePath,  IssueRule issueRule){
        List<Map<String, String>> ocMapList = new ArrayList<Map<String, String>>();;
        Map<String, JsonElement> creteria = getIssueMatchCriteria(issueRule.getMatchCriteriaJson()).getQueryPaths();
        logger.debug("matching creteria: "+creteria);
        boolean matches = false;
        try {
            // find match
            Document xmlDoc = XmlUtils.getXmlDoc(xmlFilePath.toFile());
            for (String detectMethod : creteria.keySet()) {
                if (detectMethod.equals(DETECT_DTD)){
                    JsonObject detectDtd = creteria.get(DETECT_DTD).getAsJsonObject();
                    if (detectDTD(xmlDoc, xmlFilePath.toFile().getName(), detectDtd)){
                        matches = true;
                    }
                }
                if (detectMethod.equals(DETECT_ATTR)) {
                    JsonObject detectAttribute = creteria.get(DETECT_ATTR).getAsJsonObject();
                    List<Node> findNodes = detectAttribute(xmlDoc, xmlFilePath.toFile().getName(), detectAttribute);
                    if (findNodes != null && findNodes.size()>0) {
                        matches = true;
                    }
                }
                if (detectMethod.equals(DETECT_ELEMENT)) {
                    JsonObject detectElement = creteria.get(DETECT_ELEMENT).getAsJsonObject();
                    List<Node> findNodes = detectElement(xmlDoc, xmlFilePath.toFile().getName(), detectElement);
                    if (findNodes != null && findNodes.size()>0) {
                        matches = true;
                    }
                }
                if (matches) break;
            }

            // detect the occurence
            if (matches) {
                JsonObject occurrenceAttr = issueRule.getMatchCriteria().getOccurrenceAttr();
                logger.debug("occurrenceAttr="+occurrenceAttr);
                if (occurrenceAttr != null) {
                    for (String attrKey : occurrenceAttr.keySet()) {
                        JsonObject oneAttribute = occurrenceAttr.get(attrKey).getAsJsonObject();
                        JsonObject creteriaJO = oneAttribute.get(CRETERIA_KEYNAME).getAsJsonObject();
                        for (String detectMethod: creteriaJO.keySet()) {
                            logger.debug("detector=" + detectMethod);
                            if (detectMethod.equals(DETECT_ATTR)) {
                                JsonObject detectAttribute = creteriaJO.get(DETECT_ATTR).getAsJsonObject();
                                List<Node> findNodes = detectAttribute(xmlDoc, xmlFilePath.toFile().getName(), detectAttribute);
                                if (findNodes != null && findNodes.size() > 0) {
                                    for (Node findNode : findNodes) {
                                        Map<String, String> newOccurence = new HashMap<>();
                                        newOccurence.put(attrKey, findNode.getTextContent());
                                        ocMapList.add(newOccurence);
                                    }
                                }
                            }

                            if (detectMethod.equals(DETECT_ELEMENT)) {
                                JsonObject detectElement = creteriaJO.get(DETECT_ELEMENT).getAsJsonObject();
                                List<Node> findNodes = detectElement(xmlDoc, xmlFilePath.toFile().getName(), detectElement);
                                if (findNodes != null && findNodes.size() > 0) {
                                    for (Node findNode : findNodes) {
                                        Map<String, String> newOccurence = new HashMap<>();
                                        newOccurence.put(attrKey, findNode.getTextContent());
                                        ocMapList.add(newOccurence);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception ex){
            ex.printStackTrace();
        }
        return ocMapList;
    }

    private boolean detectDTD(Document xmlDoc, String xmlFileName, JsonObject detectDtd){
        JsonArray dtdNames = detectDtd.getAsJsonArray(DTD_NAME);
        logger.debug("dtdnames="+dtdNames);
        String[] xmlFiles = getMemberAsStrArray(detectDtd, XML_FILE);
        for (JsonElement dtdNameJE : dtdNames) {
            String dtdName = dtdNameJE.getAsString();
            List<Node> findNodes = XmlUtils.getDoctypeDTDReference(xmlDoc, xmlFileName, xmlFiles, dtdName, false);
            if (findNodes != null && findNodes.size()>0) {
                return true;
            }
        }
        return false;
    }

    private List<Node> detectAttribute(Document xmlDoc, String xmlFileName, JsonObject detectAttribute){
        String[] tags = getMemberAsStrArray(detectAttribute, "tags");
        String[] xmlFiles = getMemberAsStrArray(detectAttribute, XML_FILE);
        logger.debug("tags in detect attribute: "+tags);
        String attriValue = null;
        if (detectAttribute.get("attributeValue") != null) {
            attriValue = detectAttribute.get("attributeValue").getAsString();
        }
        return XmlUtils.getTagDeclarationsByAttributeValue(xmlDoc, xmlFileName, xmlFiles, "", tags, "", detectAttribute.get("attributeName").getAsString(), attriValue);
    }

    private List<Node> detectElement(Document xmlDoc, String xmlFileName, JsonObject detectElement){
        String[] tags = getMemberAsStrArray(detectElement, "tags");
        String[] xmlFiles = getMemberAsStrArray(detectElement, XML_FILE);
        logger.debug("tags in detect element: "+tags);
        return XmlUtils.getTagDeclarations(xmlDoc, xmlFileName, xmlFiles, "", tags);
    }

    private String[] getMemberAsStrArray(JsonObject json, String memberName){
        JsonArray memberArray = json.get(memberName).getAsJsonArray();
        String[] values = IntStream.range(0, memberArray.size())
                .mapToObj(i -> memberArray.get(i).getAsString())
                .toArray(String[]::new);
        return values;
    }
}
