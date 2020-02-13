package com.ibm.ta.sdk.core.util;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import java.io.File;
import java.util.List;

public class XmlUtilsTest {

    public static void main(String[] argu) throws Exception{
        System.out.println("start");
        File xmlFile = new File(XmlUtilsTest.class.getClassLoader().getResource("src/test/resources/sample/ejb.xml").getFile());
        Document sampleDoc = XmlUtils.getXmlDoc(xmlFile);
        System.out.println(XmlUtils.getDocumentTypeSystemId(sampleDoc));
        List<Node> dtcNodes = XmlUtils.getDoctypeDTDReference(sampleDoc, xmlFile.getName(), new String[]{"ejb.xml"}, "http://java.sun.com/dtd/ejb-jar_2_0.dtd", false);
        System.out.println(dtcNodes.size());
        List<Node> tagNodes = XmlUtils.getTagDeclarations(sampleDoc, "", new String[]{"ejb-jar"});
        System.out.println(tagNodes);
        List<Node> findNodes = XmlUtils.getTagDeclarationsByAttributeValue(tagNodes, "", "version", "2.1");
        System.out.println(findNodes);
        findNodes = XmlUtils.getTagDeclarationsByAttributeValue(sampleDoc, xmlFile.getName(), new String[]{"(.*/)?ejb\\.xml"}, "", new String[]{"ejb-jar"}, "", "version", "2.1");
        System.out.println(findNodes);
        List<Node> ejbNodes = XmlUtils.getTagDeclarations(sampleDoc, "", new String[]{"enterprise-beans"});
        String ejbName = XmlUtils.getNodeValue(XmlUtils.getFirstChildNode(ejbNodes.get(0), "message-driven"), "ejb-class");
        //System.out.println(ejbName);
        List<Node> ejbClassNodes = XmlUtils.getTagDeclarations(sampleDoc, "", new String[]{"ejb-class"});
        String ejb_class = ejbClassNodes.get(0).getTextContent();
        System.out.println(ejb_class);

    }
}
