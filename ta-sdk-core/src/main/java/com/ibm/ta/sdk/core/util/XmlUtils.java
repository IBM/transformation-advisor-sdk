/*
 * (C) Copyright IBM Corp. 2019,2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.ta.sdk.core.util;


import org.apache.log4j.Logger;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class XmlUtils {
    private final String CLASS_NAME = getClass().getName();
    private static Logger logger = Logger.getLogger(XmlUtils.class.getName());

    /**
     *
     * Helper method to return the parent node of the specified name space and node name.
     *
     * @param child The starting node to search up the node tree.
     * @param namespaceURI Name space URI for parent.
     * @param targetParentNodeName Node name of the parent to find.
     * @return The parent node or <code>null</code> if not found.
     */
    public static Node getParentNode(Node child, String namespaceURI, String targetParentNodeName) {
        boolean loop = true;
        Node node = child;
        do {
            if (node != null) {
                Node parent = node.getParentNode();
                if (parent != null) {
                    if (namespaceURI == null || namespaceURI.trim().length() == 0 || namespaceURI.trim().equals("*")) {
                        // any name space
                        if (targetParentNodeName.equals(parent.getLocalName())) {
                            loop = false;
                            return parent;
                        }
                    } else {
                        if (namespaceURI.equals(parent.getNamespaceURI()) && targetParentNodeName.equals(parent.getLocalName())) {
                            return parent;
                        }
                    }
                    // reset and look up further
                    node = parent;
                } else {
                    loop = false;
                }
            } else {
                loop = false;
            }
        } while (loop);

        // did not find it
        return null;
    }

    /**
     *
     * Returns a list of tag declarations found in a XML file
     *
     * @param document represents a XML file
     * @param fileName the name of the XML file loaded in the Document
     * @param xmlFiles name of XML files to scan
     * @param tagNamespace XML tag namespace. Can be specified as a regular expression.
     * @param tags to look for in XML
     * @return nodes with instances that match the specified criteria
     */
    public static List<Node> getTagDeclarations(Document document, String fileName, String[] xmlFiles, String tagNamespace, String[] tags) {
        if ((document == null) || !isMatchingFile(fileName, xmlFiles)) {
            return Collections.emptyList();
        }

        return getTagDeclarations(document, tagNamespace, tags);
    }


    public static List<Node> getTagDeclarations(Document document, String tagNamespace, String[] tags) {
        List<Node> result = new ArrayList<Node>();
        for (String currentTag : tags) {
            NodeList nodeList = null;
            if ("".equals(tagNamespace)) { //$NON-NLS-1$
                nodeList = document.getElementsByTagName(currentTag.trim());
            } else {
                nodeList = document.getElementsByTagNameNS("*", currentTag.trim()); //$NON-NLS-1$
            }
            if (nodeList == null) {
                continue;
            }
            for (int i = 0; i < nodeList.getLength(); ++i) {
                Node currentNode = nodeList.item(i);
                // check namespace
                if ("".equals(tagNamespace) || tagNamespace.equals("*")) { //$NON-NLS-1$ //$NON-NLS-2$
                    // no namespace matching
                } else {
                    String namespace = currentNode.getNamespaceURI();
                    if (namespace == null || !namespace.matches(tagNamespace)) {
                        continue;
                    }
                }
                result.add(currentNode);
            }
        }
        return result;
    }

    /**
     *
     * Returns a list of tag declarations found in an XML file
     *
     * @param document represents a XML file
     * @param fileName the name of the XML file loaded in the Document
     * @param xmlFiles XML files to look for instances in
     * @param tagNamespace XML tag namespace. Can be specified as a regular expression.
     * @param tags XML tag to detect
     * @param attributeNamespace XML attribute namespace. Can be specified as a regular expression.
     * @param attributeName XML attribute. Can be specified as a regular expression.
     * @param attributeValue XML attribute value. Can be specified as a regular expression.
     * @return list of nodes containing the specified criteria
     */
    public static List<Node> getTagDeclarationsByAttributeValue(Document document,
                                                                String fileName,
                                                                String[] xmlFiles,
                                                                String tagNamespace,
                                                                String[] tags,
                                                                String attributeNamespace,
                                                                String attributeName,
                                                                String attributeValue) {
        List<Node> tagNodes = getTagDeclarations(document, fileName, xmlFiles, tagNamespace, tags);
        return getTagDeclarationsByAttributeValue(tagNodes, attributeNamespace, attributeName, attributeValue);
    }

    public static Node getAttributeNode(NamedNodeMap attributes, String attributeNamespace, String attributeName) {

        int numAttrs = attributes.getLength();
        for (int i = 0; i < numAttrs; i++) {
            Node node = attributes.item(i);
            // check namespace
            if ("".equals(attributeNamespace) || attributeNamespace.equals("*")) { //$NON-NLS-1$ //$NON-NLS-2$
                // no namespace matching
            } else {
                String namespace = node.getNamespaceURI();
                if (namespace == null || !namespace.matches(attributeNamespace)) {
                    continue;
                }
            }
            // check node name
            if (!node.getLocalName().matches(attributeName)) {
                continue;
            }
            return node;
        }

        return null;
    }

    public static String getAttributeValue(NamedNodeMap attributes, String attributeNamespace, String attributeName) {

        int numAttrs = attributes.getLength();
        for (int i = 0; i < numAttrs; i++) {
            Node node = attributes.item(i);
            // check namespace
            if ("".equals(attributeNamespace) || attributeNamespace.equals("*")) { //$NON-NLS-1$ //$NON-NLS-2$
                // no namespace matching
            } else {
                String namespace = node.getNamespaceURI();
                if (namespace == null || !namespace.matches(attributeNamespace)) {
                    continue;
                }
            }
            // check node name
            if (!node.getLocalName().matches(attributeName)) {
                continue;
            }
            return node.getNodeValue();
        }

        return null;
    }

    public static List<Node> getTagDeclarationsByAttributeValue(List<Node> tagNodes,
                                                                String attributeNamespace,
                                                                String attributeName,
                                                                String attributeValue) {
        List<Node> result = new ArrayList<Node>();
        boolean exactMatch = attributeName.matches("[a-zA-Z]*") && "".equals(attributeNamespace); //$NON-NLS-1$ //$NON-NLS-2$
        if (exactMatch) {
            for (Node tagNode : tagNodes) {
                Node node = tagNode.getAttributes().getNamedItem(attributeName);
                if (node != null) {
                    // check node value if necessary
                    if ((attributeValue == null) || (node.getNodeValue() != null && node.getNodeValue().matches(attributeValue))) {
                        result.add(node);
                    }
                }
            }
        } else {
            for (Node tagNode : tagNodes) {
                NamedNodeMap attrs = tagNode.getAttributes();
                int numAttrs = attrs.getLength();
                for (int i = 0; i < numAttrs; i++) {
                    Node node = attrs.item(i);
                    // check namespace
                    if ("".equals(attributeNamespace) || attributeNamespace.equals("*")) { //$NON-NLS-1$ //$NON-NLS-2$
                        // no namespace matching
                    } else {
                        String namespace = node.getNamespaceURI();
                        if (namespace == null || !namespace.matches(attributeNamespace)) {
                            continue;
                        }
                    }
                    // check node name
                    if (!node.getLocalName().matches(attributeName)) {
                        continue;
                    }
                    // check node value if necessary
                    if ((attributeValue == null) || (node.getNodeValue() != null && node.getNodeValue().matches(attributeValue))) {
                        result.add(node);
                    }
                }
            }
        }
        return result;
    }

    /**
     *
     * Returns a list of tag declarations found in an XML file
     *
     * @param document represents a XML file
     * @param fileName the name of the XML file loaded in the Document
     * @param xmlFiles XML files to look for instances in
     * @param tagNamespace XML tag namespace
     * @param value of detected tag. Can be specified as a regular expression.
     * @param preserveWhiteSpace true if whitespace is to be preserved and taking into consideration while doing a comparison. False, if whitespace is to be removed for node value
     *            before comparing.
     * @param skipRootElementAttributes true if root element attributes should not be scanned
     * @return list of nodes containing the specified criteria
     */
    public static List<Node> getTagDeclarationsByNodeOrAttributeValue(Document document,
                                                                      String fileName,
                                                                      String[] xmlFiles,
                                                                      String tagNamespace,
                                                                      String value,
                                                                      boolean preserveWhiteSpace,
                                                                      boolean skipRootElementAttributes) {

        boolean checkNamespace = !(tagNamespace == null || tagNamespace.equals("") || tagNamespace.equals("*"));
        List<Node> result = new ArrayList<Node>();
        return getTagDeclarationsByNodeOrAttributeValue(document.getChildNodes(), tagNamespace, value, preserveWhiteSpace, skipRootElementAttributes, checkNamespace, result);
    }

    /**
     *
     * Returns a list of tag declarations found in an XML file based on nodes or attribute value
     *
     * @param children List of Nodes need to be checked
     * @param namespace XML node namespace
     * @param value of detected tag. Can be specified as a regular expression.
     * @param value of detected tag. Can be specified as a regular expression.
     * @param preserveWhiteSpace true if whitespace is to be preserved and taking into consideration while doing a comparison. False, if whitespace is to be removed for node value
     *            before comparing.
     * @param skipRootElementAttributes true if root element attributes should not be scanned
     * @param checkNamespace true to check the namespace during detect
     * @param result the list of the nodes which will be added to the detected nodes
     * @return list of nodes containing the specified criteria
     */
    public static List<Node> getTagDeclarationsByNodeOrAttributeValue(NodeList children, String namespace, String value, boolean preserveWhiteSpace,
                                                                      boolean skipRootElementAttributes, boolean checkNamespace, List<Node> result) {
        int numChildren = children.getLength();
        for (int i = 0; i < numChildren; i++) {
            Node nextNode = children.item(i);
            if (checkNamespace) {
                String nodeNamespace = nextNode.getNamespaceURI();
                if (nodeNamespace == null || !nodeNamespace.matches(namespace)) {
                    continue;
                }
            }

            String text = getElmentNodeTextContent(nextNode);
            if (text != null) {
                if (!preserveWhiteSpace) {
                    text = text.replaceAll("\\s+", "");
                }
                if (text.matches(value)) {
                    result.add(nextNode);
                }
            }

            if (!skipRootElementAttributes) {
                NamedNodeMap attrs = nextNode.getAttributes();
                if (attrs != null) {
                    int numAttrs = attrs.getLength();
                    for (int j = 0; j < numAttrs; j++) {
                        Node nextAttrNode = attrs.item(j);
                        if (checkNamespace) {
                            String nextAttrNodeNS = nextNode.getNamespaceURI();
                            if (nextAttrNodeNS == null || !nextAttrNodeNS.matches(namespace)) {
                                continue;
                            }
                        }
                        // check node value
                        String nodeAttrValue = nextAttrNode.getNodeValue();
                        if (nodeAttrValue != null && nodeAttrValue.matches(value)) {
                            result.add(nextAttrNode);
                        }
                    }
                }
            }

            if (nextNode.hasChildNodes()) {
                getTagDeclarationsByNodeOrAttributeValue(nextNode.getChildNodes(), namespace, value, preserveWhiteSpace, false, checkNamespace, result);
            }

        }

        return result;
    }

    /**
     *
     * Returns a list of tag declarations found in an XML file
     *
     * @param document represents a XML file
     * @param fileName the name of the XML file loaded in the Document
     * @param xmlFiles XML files to look for instances in
     * @param tagNamespace XML tag namespace
     * @param tags XML tag to detect
     * @param value of detected tag. Can be specified as a regular expression.
     * @param preserveWhiteSpace true if whitespace is to be preserved and taking into consideration while doing a comparison. False, if whitespace is to be removed for node value
     *            before comparing.
     * @return list of nodes containing the specified criteria
     */
    public static List<Node> getTagDeclarationsByNodeValue(Document document,
                                                           String fileName,
                                                           String[] xmlFiles,
                                                           String tagNamespace,
                                                           String[] tags,
                                                           String value,
                                                           boolean preserveWhiteSpace) {
        List<Node> tagNodes = getTagDeclarations(document, fileName, xmlFiles, tagNamespace, tags);
        return getTagDeclarationsByNodeValue(tagNodes, value, preserveWhiteSpace);
    }

    public static List<Node> getTagDeclarationsByNodeValue(List<Node> tagNodes, String value, boolean preserveWhiteSpace) {
        List<Node> result = new ArrayList<Node>();
        for (Node tagNode : tagNodes) {
            String text = getElmentNodeTextContent(tagNode);
            if (text != null) {
                if (!preserveWhiteSpace) {
                    text = text.replaceAll("\\s+", "");
                }
                if (text.matches(value)) {
                    result.add(tagNode);
                }
            }
        }
        return result;
    }

    public static String getElmentNodeTextContent(Node n) {
        Node textChild = n.getFirstChild();
        if (textChild != null && textChild.getNodeType() != Node.COMMENT_NODE) {
            return textChild.getNodeValue();
        }
        return null;
    }

    /**
     *
     * Returns a list of tag declarations found in an XML file
     *
     * @param document represents a XML file
     * @param fileName the name of the XML file loaded in the Document
     * @param xmlFiles XML files to look for instances in
     * @param dtdName DTD names to detect
     * @param flagNullDocTypeWhenNoVersionAttrExists Need to detect when no DocType and no version
     * @return list of nodes containing the specified criteria
     */
    public static List<Node> getDoctypeDTDReference(Document document, String fileName, String[] xmlFiles, String dtdName, boolean flagNullDocTypeWhenNoVersionAttrExists) {
        return getDoctypeDTDReference(document, fileName, xmlFiles, null, dtdName, flagNullDocTypeWhenNoVersionAttrExists);
    }

    /**
     *
     * Returns a list of tag declarations found in an XML file
     *
     * @param document represents a XML file
     * @param fileName the name of the XML file loaded in the Document
     * @param xmlFiles XML files to look for instances in
     * @param rootTag root element name to verify
     * @param dtdName DTD names to detect
     * @param flagNullDocTypeWhenNoVersionAttrExists Need to detect when no DocType and no version
     * @return list of nodes containing the specified criteria
     */
    public static List<Node> getDoctypeDTDReference(Document document, String fileName, String[] xmlFiles, String rootTag, String dtdName,
                                                    boolean flagNullDocTypeWhenNoVersionAttrExists) {
        List<Node> result = new ArrayList<Node>();
        if ((document == null) || !isMatchingFile(fileName, xmlFiles)) {
            return result;
        }

        DocumentType docType = document.getDoctype();
        Element root = document.getDocumentElement();

        if (rootTag != null && !root.getNodeName().equals(rootTag)) {
            return result;
        }

        if (flagNullDocTypeWhenNoVersionAttrExists) {
            String version = getAttributeValue(root.getAttributes(), "*", "version");

            if ((docType == null && (version == null || version.equals(""))) ||
                    (docType != null && docType.getSystemId().matches(dtdName))) {
                // SMA Fix for @DetectDTD. The rule instance will be marked on the root element of the XML document
                result.add(root);
            }
        } else {
            if (docType != null && docType.getSystemId().matches(dtdName)) {
                // SMA Fix for @DetectDTD. The rule instance will be marked on the root element of the XML document
                result.add(root);
            }
        }

        return result;
    }

    /*
     * Returns the DTD for the passed Document, or null if no document type declaration.
     */
    public static String getDocumentTypeSystemId(Document document) {
        String result = null;

        DocumentType docType = document.getDoctype();
        if (docType != null) {
            return docType.getSystemId();
        }

        return result;
    }

    public static boolean isMatchingFile(String fileName, String[] xmlFiles) {
        if (xmlFiles == null) return true;
        for (String xmlFile : xmlFiles) {
            if (isMatchingFile(fileName, xmlFile)) {
                return true;
            }
        }
        return false;
    }

    /*
     * Also fixed logic to handle regular expressions correctly.
     * Since I am taking a String file name, had to replace back slashes with forward slashes in order for
     * matching to work on Windows.
     */
    public static boolean isMatchingFile(String fileName, String xmlFile) {
        // replace all back slashes with forward slashes before comparing
        String normalizedFileName = fileName.replace("\\", "/"); //$NON-NLS-1$ //$NON-NLS-2$

        boolean bool;
        if (xmlFile.contains("*") || xmlFile.contains("+") ||
                xmlFile.contains("$") || xmlFile.contains("|") || xmlFile.contains("(")) { //$NON-NLS-1$ //$NON-NLS-2$
            bool = normalizedFileName.matches(xmlFile);
        } else if (xmlFile.startsWith(".")) { //$NON-NLS-1$
            bool = normalizedFileName.endsWith(xmlFile);
        } else if (xmlFile.indexOf('/') >= 0) {
            bool = normalizedFileName.endsWith(xmlFile);
        } else {
            bool = normalizedFileName.equalsIgnoreCase(xmlFile);
        }

        return bool;
    }

    /**
     * Helper method to strip off white space from a node's text content.
     * Blanks, tabs, and carriage returns will be removed.
     *
     * @param node Node to get the text content with whitespace removed.
     * @return The text content string without white space.
     */
    public static String getTextWithoutWhitespace(Node node) {
        if (node == null)
            return null;
        String text = node.getTextContent();
        if (text != null) {
            text = text.replaceAll("\\s+", "");
        }
        return text;
    }

    /**
     *
     * Helper method to find the child elements that match the provided name space URI and
     * target name.
     *
     * @param parent Starting element for the search.
     * @param namespaceURI Name space URI for the element.
     * @param targetName Tag name to search.
     * @return List of elements that match the name space and tag.
     */
    public static List<Element> getChildElements(Element parent, String namespaceURI, String targetName) {
        List<Element> list = new ArrayList<Element>();
        NodeList children = parent.getElementsByTagNameNS(namespaceURI, targetName);
        for (int i = 0; i < children.getLength(); i++) {
            Element childElement = (Element) children.item(i);
            if (namespaceURI == null || namespaceURI.trim().length() == 0 || namespaceURI.trim().equals("*")) {
                // any name space
                if (targetName.equals(childElement.getLocalName())) {
                    list.add(childElement);
                }
            } else {
                if (namespaceURI.equals(childElement.getNamespaceURI()) && targetName.equals(childElement.getLocalName())) {
                    list.add(childElement);
                }
            }

        }
        return list;
    }

    /**
     * Expanded on getChildElements method above. This method will return a list of element objects that match the specified
     * tag name, namespace (optional), attribute name and attribute value.
     *
     * @param parent parent element that will be used
     * @param namespaceURI The namespace URI of the elements to match on. The special value "*" matches all namespaces
     * @param tagName tag name to search for
     * @param attributeName attribute name to search for
     * @param attributeValuesRegex Regex string of attribute value(s) to search for.
     * @return List of elements that match the name space and tag and attribute name
     */
    public static List<Element> getChildElementsByAttributeValue(Element parent, String namespaceURI, String tagName, String attributeName, String attributeValuesRegex) {
        List<Element> list = new ArrayList<Element>();
        NodeList children = null;
        if (namespaceURI != null) {
            children = parent.getElementsByTagNameNS(namespaceURI, tagName);
        } else {
            children = parent.getElementsByTagName(tagName);
        }

        for (int i = 0; i < children.getLength(); i++) {
            Element childElement = (Element) children.item(i);
            if (namespaceURI == null || namespaceURI.trim().length() == 0 || namespaceURI.trim().equals("*")) {
                // any name space
                if (tagName.equals(childElement.getLocalName())) {
                    NamedNodeMap nodeAttributes = childElement.getAttributes();
                    if (nodeAttributes != null) {
                        Node nameAttr = nodeAttributes.getNamedItem(attributeName);
                        if (nameAttr != null) {
                            String nameAttrValue = nameAttr.getNodeValue();
                            if (nameAttrValue.matches(attributeValuesRegex)) {
                                list.add(childElement);
                            }
                        }
                    }
                }
            } else {
                if (namespaceURI.equals(childElement.getNamespaceURI()) && tagName.equals(childElement.getLocalName())) {
                    list.add(childElement);
                }
            }

        }
        return list;
    }

    /**
     *
     * Helper method to find the first child element that matches the provided name space URI and
     * target name.
     *
     * @param parent Starting element for the search.
     * @param namespaceURI Name space URI for the element.
     * @param targetName Tag name to search.
     * @return First element that matches or <code>null</code> if not found.
     *
     */
    public static Element getFirstChildElement(Element parent, String namespaceURI, String targetName) {
        NodeList children = parent.getElementsByTagNameNS(namespaceURI, targetName);
        for (int i = 0; i < children.getLength(); i++) {
            Element childElement = (Element) children.item(i);
            if (namespaceURI == null || namespaceURI.trim().length() == 0 || namespaceURI.trim().equals("*")) {
                // any name space
                if (targetName.equals(childElement.getLocalName())) {
                    return childElement;
                }
            } else {
                if (namespaceURI.equals(childElement.getNamespaceURI()) && targetName.equals(childElement.getLocalName())) {
                    return childElement;
                }
            }
        }
        return null;
    }

    /**
     *
     * Helper method to return the value of the first child element that matches the provided
     * name space URI and tag name.
     *
     * @param parent Starting element for the search.
     * @param namespaceURI Name space URI for the element.
     * @param targetName Tag name to search.
     * @return The string value of the matching element or <code>null</code> if not found.
     */
    public static String getFirstChildElementValue(Element parent, String namespaceURI, String targetName) {
        Element childElement = getFirstChildElement(parent, namespaceURI, targetName);
        if (childElement != null) {
            return getTextWithoutWhitespace(childElement);
        }
        return null;
    }

    public static String getNodeValue(Node parent, String childNodeName) {
        String nodeValue = null;
        Node node = getFirstChildNode(parent, childNodeName);
        if (node != null) {
            Node firstChild = node.getFirstChild();
            if (firstChild != null) {
                nodeValue = firstChild.getNodeValue();
                if (nodeValue != null) {
                    nodeValue = nodeValue.trim();
                }
            }
        }
        return nodeValue;
    }

    public static Node getFirstChildNode(Node parent, String childNodeName) {
        NodeList children = parent.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node childNode = children.item(i);
            if (childNodeName.equals(childNode.getNodeName())) {
                return childNode;
            }
        }
        return null;
    }

    /**
     * This method will return the value of a node specified by parameter
     * nodeNameToFind. The node can either be an attribute of theNode
     * or it can be a child of theNode.
     *
     * @param theNode Starting node in the DOM tree to search.
     * @param nodeNameToFind Name of the XML node element to find.
     * @return The node value of the matching node or <code>null</code> if no
     *         match was found.
     */
    public static String getAttributeOrChildNodeValue(Node theNode, String nodeNameToFind) {
        String value = null;

        Node node = getAttributeOrChildNode(theNode, nodeNameToFind);
        if (node != null) {
            value = node.getTextContent();
            if (value != null) {
                value = value.replaceAll("\\s+", "");
            }
        }

        return value;
    }

    /**
     * This method will return the value of a node specified by parameter
     * nodeNameToFind. The node can either be an attribute of theNode
     * or it can be a child of theNode.
     *
     * @param theNode Starting node in the DOM tree to search.
     * @param nodeNameToFind Name of the XML node element to find.
     * @return The XML node that matches <code>nodeNameToFind</code>
     */
    public static Node getAttributeOrChildNode(Node theNode, String nodeNameToFind) {

        if (theNode == null) {
            return null;
        }
        Node node = theNode.getAttributes().getNamedItem(nodeNameToFind);

        // if we don't find it as an attribute, look for child.
        if (node == null) {
            //get child value from a child node like below
            //<property name="name" >
            //   <value>value</value>
            //</property>
            NodeList childNodes = theNode.getChildNodes();
            for (int i = 0; i < childNodes.getLength(); i++) {
                Node possibleChild = childNodes.item(i);
                if (nodeNameToFind.equals(possibleChild.getLocalName())) {
                    node = possibleChild;
                }
            }
        }
        return node;
    }

    private static boolean isNodeNameValid(Node node, String name) {
        String nodeName = node.getNodeName();
        if (nodeName != null) {
            nodeName = nodeName.trim();
            int index = nodeName.indexOf(':');
            if (index > -1) {
                nodeName = nodeName.substring(index + 1);
            }
            return nodeName.equals(name);
        }
        return false;
    }

    public static Document getXmlDoc(File xmlFile) throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilder dBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document doc = dBuilder.parse(xmlFile);
        return doc;
    }

}
