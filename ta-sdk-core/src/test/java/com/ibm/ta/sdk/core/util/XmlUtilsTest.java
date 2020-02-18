package com.ibm.ta.sdk.core.util;

import org.junit.jupiter.api.BeforeEach;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import java.io.File;
import java.io.IOException;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;

import static org.junit.jupiter.api.Assertions.*;

public class XmlUtilsTest {

    private static File xmlFile = new File(XmlUtilsTest.class.getClassLoader().getResource("sample/ejb.xml").getFile());
    private Document sampleDoc = null;

    @BeforeEach
    void initialXmlDoc() throws IOException, SAXException, ParserConfigurationException  {
        sampleDoc = XmlUtils.getXmlDoc(xmlFile);
    }

    @Test
    public void getXmlDocTest() {
        assertNotNull(sampleDoc, "xml document get from a file is null");
        assertEquals(sampleDoc.getXmlVersion(), "1.0");
        assertEquals(sampleDoc.getXmlEncoding(), "UTF-8");
    }

    @Test
    public void getDoctypeDTDReferenceTest() {
        List<Node> dtcNodes = XmlUtils.getDoctypeDTDReference(sampleDoc, xmlFile.getName(), new String[]{"ejb.xml"}, "http://java.sun.com/dtd/ejb-jar_2_0.dtd", false);
        assertFalse(dtcNodes.isEmpty());
        assertEquals(dtcNodes.size(), 1);
    }

    @Test
    public void getTagDeclarationsTest() {
        List<Node> tagNodes = XmlUtils.getTagDeclarations(sampleDoc, "", new String[]{"ejb-jar"});
        assertFalse(tagNodes.isEmpty());
        assertEquals(tagNodes.size(), 1);
    }

    @Test
    public void getTagTextTest(){
        List<Node> ejbClassNodes = XmlUtils.getTagDeclarations(sampleDoc, "", new String[]{"ejb-class"});
        assertFalse(ejbClassNodes.isEmpty());
        assertEquals(ejbClassNodes.size(), 1);
        String ejb_class = ejbClassNodes.get(0).getTextContent();
        assertEquals(ejb_class, "samples.mdb.ejb.MessageBean");
    }

    @Test
    public void getTagDeclarationsByAttributeValueTest() {
        List<Node> tagNodes = XmlUtils.getTagDeclarations(sampleDoc, "", new String[]{"ejb-jar"});
        List<Node> findNodes = XmlUtils.getTagDeclarationsByAttributeValue(tagNodes, "", "version", "2.1");
        assertFalse(findNodes.isEmpty());
        assertEquals(findNodes.size(), 1);
    }

    @Test
    public void getNodeValueTest() {
        List<Node> ejbNodes = XmlUtils.getTagDeclarations(sampleDoc, "", new String[]{"enterprise-beans"});
        String ejbName = XmlUtils.getNodeValue(XmlUtils.getFirstChildNode(ejbNodes.get(0), "message-driven"), "ejb-name");
        assertEquals(ejbName, "MessageBean");
    }

    @Test
    public void getTagUsingRegExTest() {
        List<Node> tagNodes = XmlUtils.getTagDeclarationsByAttributeValue(sampleDoc, xmlFile.getName(), new String[]{"(.*/)?ejb\\.xml"}, "", new String[]{"ejb-jar"}, "", "version", "2.1");
        assertFalse(tagNodes.isEmpty());
        assertEquals(tagNodes.size(), 1);
    }
}
