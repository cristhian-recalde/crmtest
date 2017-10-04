/*
 * This code is a protected work and subject to domestic and international copyright
 * law(s). A complete listing of authors of this work is readily available. Additionally,
 * source code is, by its very nature, confidential information and inextricably contains
 * trade secrets and other information proprietary, valuable and sensitive to Redknee, no
 * unauthorised use, disclosure, manipulation or otherwise is permitted, and may only be
 * used in accordance with the terms of the licence agreement entered into with Redknee
 * Inc. and/or its subsidiaries.
 * 
 * Copyright &copy; Redknee Inc. and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.client.xmlhttp;

import java.io.IOException;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;


/**
 * A javax.xml.* based class to parse XML Document and support querying using XPATH
 * expressions
 * 
 * @author simar.singh@redknee.com
 * 
 */
public class XmlParsedXpathQueryDocument
{

    /**
     * Constructs the XML - XPATH Query Document using XML content passed as String XML
     * content need not be Name-Space aware and may be without any validating DTD Uses
     * Default XPATH-Factory of type javax.xml.xpath.XPathFactory to to evaluate XPATH
     * expressions/results on the document. Holds XML as an [org.w3c.dom.Document] built
     * with [javax.xml.parsers.DocumentBuilder.parse(xmlContent)] using default
     * javax.xml.parsers.DocumentBuilderFactory.newInstance()
     * 
     * @param xmlContent
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     */
    public XmlParsedXpathQueryDocument(String xmlContent) throws ParserConfigurationException, SAXException,
            IOException
    {
        this(xmlContent, XPathFactory.newInstance(), false, false);
    }


    /**
     * Constructs the XML - XPATH Query Document using XML content passed as String XML
     * content may be set Name-Space aware and-or validated one with it's DTD Uses Default
     * XPATH-Factory of type javax.xml.xpath.XPathFactory to to evaluate XPATH
     * expressions/results on the document. Holds XML as an [org.w3c.dom.Document] built
     * with [javax.xml.parsers.DocumentBuilder.parse(xmlContent)] using default
     * javax.xml.parsers.DocumentBuilderFactory.newInstance()
     * 
     * @param xmlContent
     * @param isNameSpaceAware
     * @param isValidating
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     */
    public XmlParsedXpathQueryDocument(String xmlContent, boolean isNameSpaceAware, boolean isValidating)
            throws ParserConfigurationException, SAXException, IOException
    {
        this(xmlContent, XPathFactory.newInstance(), isNameSpaceAware, isValidating);
    }


    /**
     * Constructs the XML - XPATH Query Document using XML content passed as String XML
     * content may be set Name-Space aware and-or validated one with it's DTD Uses default
     * XPATH-Factory [javax.xml.xpath.XPathFactory.newInstance()] to to evaluate XPATH
     * expressions/results on the document. Holds XML as an [org.w3c.dom.Document] built
     * with [javax.xml.parsers.DocumentBuilder.parse(xmlContent)] using default
     * [javax.xml.parsers.DocumentBuilderFactory.newInstance()]
     * 
     * @param xmlContent
     * @param factory
     * @param isNameSpaceAware
     * @param isValidating
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     */
    public XmlParsedXpathQueryDocument(String xmlContent, XPathFactory factory, boolean isNameSpaceAware,
            boolean isValidating) throws ParserConfigurationException, SAXException, IOException
    {
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        docFactory.setNamespaceAware(true);
        docFactory.setValidating(false);
        this.xmlContent_ = xmlContent;
        this.xpathFactory_ = factory;
        this.docBuilder_ = docFactory.newDocumentBuilder();
        this.xml_ = this.docBuilder_.parse(new InputSource(new StringReader(xmlContent)));
    }


    /**
     * Constructs the XML - XPATH Query Document using XML content passed as String XML
     * content may be set Name-Space aware and-or validated one with it's DTD Uses
     * XPATH-Factory from as passed [javax.xml.xpath.XPathFactory.newInstance()] to to
     * evaluate XPATH expressions/results on the document. Holds XML as an
     * [org.w3c.dom.Document] built with
     * [javax.xml.parsers.DocumentBuilder.parse(xmlContent)] using with passed
     * [javax.xml.parsers.DocumentBuilderFactory.newInstance()]
     * 
     * @param xmlContent
     * @param factory
     * @param isNameSpaceAware
     * @param isValidating
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     */
    public XmlParsedXpathQueryDocument(String xmlContent, DocumentBuilderFactory docFactory, XPathFactory factory,
            boolean isNameSpaceAware, boolean isValidating) throws ParserConfigurationException, SAXException,
            IOException
    {
        this.xmlContent_ = xmlContent;
        this.xpathFactory_ = factory;
        this.docBuilder_ = docFactory.newDocumentBuilder();
        this.xml_ = this.docBuilder_.parse(new InputSource(new StringReader(xmlContent)));
    }


    /**
     * Constructs the XML - XPATH Query Document using XML content passed as String XML
     * content may be set Name-Space aware and-or validated one with it's DTD Uses
     * XPATH-Factory from as passed [javax.xml.xpath.XPathFactory.newInstance()] to to
     * evaluate XPATH expressions/results on the document. Holds XML in the passed
     * [javax.xml.parsers.DocumentBuilder]
     * 
     * @param xmlContent
     * @param factory
     * @param isNameSpaceAware
     * @param isValidating
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     */
    public XmlParsedXpathQueryDocument(String xmlContent, DocumentBuilder docBuilder, XPathFactory factory,
            boolean isNameSpaceAware, boolean isValidating) throws ParserConfigurationException, SAXException,
            IOException
    {
        this.xmlContent_ = xmlContent;
        this.xpathFactory_ = factory;
        this.docBuilder_ = docBuilder;
        this.xml_ = this.docBuilder_.parse(new InputSource(new StringReader(xmlContent)));
    }


    /**
     * Get's the text value from the XML-element for a given XPathExpression
     * 
     * @param expr
     * @return
     * @throws XPathExpressionException
     */
    public String getTextElement(XPathExpression expr) throws XPathExpressionException
    {
        return (String) expr.evaluate(xml_, XPathConstants.STRING);
    }


    /**
     * Get's the text-value from the XML-element for a given String(X-Path-Expression)
     * 
     * @param xpathExpr
     * @return
     * @throws XPathExpressionException
     */
    public String getTextElement(String xpathExpr) throws XPathExpressionException
    {
        return (String) getXPathExpression(xpathExpr).evaluate(xml_, XPathConstants.STRING);
    }


    /**
     * Returns the XPathExpression expression from the String(X-Path-Expression)
     * 
     * @param xpathExpr
     * @return
     * @throws XPathExpressionException
     */
    public XPathExpression getXPathExpression(String xpath) throws XPathExpressionException
    {
        return xpathFactory_.newXPath().compile(xpath);
    }

    protected final String xmlContent_;
    protected final org.w3c.dom.Document xml_;
    protected XPathFactory xpathFactory_;
    protected DocumentBuilder docBuilder_;
}
