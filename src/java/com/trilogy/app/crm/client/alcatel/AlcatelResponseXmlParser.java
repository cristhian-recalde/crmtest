package com.trilogy.app.crm.client.alcatel;

import java.io.IOException;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.trilogy.app.crm.client.xmlhttp.Response;


public class AlcatelResponseXmlParser
{

    public AlcatelResponseXmlParser() throws XPathExpressionException
    {
        XPathFactory xpathFactory = XPathFactory.newInstance();
        exprErrorCode_ = xpathFactory.newXPath().compile("/response/target/error/code");
        exprErrorMessage_ = xpathFactory.newXPath().compile("/response/target/error/message");
        exprAccountID_ = xpathFactory.newXPath().compile("/response/target/result/account/id");
    }


    public org.w3c.dom.Document getXmlDocument(String xml) throws ParserConfigurationException, SAXException,
            IOException
    {
        DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
        domFactory.setNamespaceAware(true);
        domFactory.setValidating(false);
        javax.xml.parsers.DocumentBuilder builder = domFactory.newDocumentBuilder();
        org.w3c.dom.Document doc = builder.parse(new InputSource(new StringReader(xml)));
        return doc;
    }


    public Response getResponse(org.w3c.dom.Document xml) throws XPathExpressionException
    {
        Response response = new Response();
        final String errorCode = getTextElement(xml, exprErrorCode_);
        final String errorMessage = getTextElement(xml, exprErrorMessage_);
        if (null != errorCode && errorCode.length() > 0)
        {
            response.setResultCode(errorCode);
            response.setResultMessage(errorMessage);
        }
        else
        {
            response.setResultCode("");
            response.setResultMessage("");
        }
        return response;
    }


    public String getAccountID(org.w3c.dom.Document xml) throws XPathExpressionException
    {
        return getTextElement(xml, exprAccountID_);
    }


    public String getTextElement(org.w3c.dom.Document xml, XPathExpression expr) throws XPathExpressionException
    {
        return (String) expr.evaluate(xml, XPathConstants.STRING);
    }

    public final XPathExpression exprErrorCode_;
    public final XPathExpression exprErrorMessage_;
    public final XPathExpression exprAccountID_;

    public static class TestAlcatelResponseXmlParser extends TestCase
    {

        public static Test suite()
        {
            TestSuite suite = new TestSuite(TestAlcatelResponseXmlParser.class);
            return suite;
        }


        public TestAlcatelResponseXmlParser()
        {
        }


        /**
         * @author simar.singh@redknee.com
         */
        @org.junit.Test
        public void testErrorResponse()
        {
            final AlcatelResponseXmlParser parser;
            final Response response;
            try
            {
                parser = new AlcatelResponseXmlParser();
                response = parser.getResponse(parser.getXmlDocument(xmlError_));
            }
            catch (Throwable t)
            {
                // TODO Auto-generated catch block
                throw new RuntimeException("Error exceuting test.", t);
            }
            assertEquals("Test - Error-Code", errorCode_, response.getResultCode());
            assertEquals("Test - Error-Message", errorMessage_, response.getResultMessage());
        }


        /**
         * @author simar.singh@redknee.com
         */
        @org.junit.Test
        public void testResponseWithAccountID()
        {
            final AlcatelResponseXmlParser parser;
            final Response response;
            final Document xml;
            try
            {
                parser = new AlcatelResponseXmlParser();
                xml = parser.getXmlDocument(xmlAccountID_);
                response = parser.getResponse(xml);
            }
            catch (Throwable t)
            {
                // TODO Auto-generated catch block
                throw new RuntimeException("Error exceuting test.", t);
            }
            assertEquals("Test - Error-Code", "", response.getResultCode());
            assertEquals("Test - Error-Message", "", response.getResultMessage());
            final String accountID;
            try
            {
                accountID = parser.getAccountID(xml);
            }
            catch (Throwable t)
            {
                // TODO Auto-generated catch block
                throw new RuntimeException("Error exceuting test.", t);
            }
            assertEquals("Test - Account-ID", accountID_, accountID);
        }


        /**
         * @author simar.singh@redknee.com
         */
        public static void main(String[] args)
        {
            junit.textui.TestRunner.run(suite());
        }

        private final String errorCode_ = "USR-00001";
        private final String errorMessage_ = "User Mr T not found";
        private final String accountID_ = "3";
        private final String xmlAccountID_ = "<response version=\"1.2\">"
                + "<target name=\"AccountAPI\" operation=\"createAccount\">" + "<result> " + "<account> " + "<id>"
                + accountID_ + "</id> " + "</account> " + "</result>" + "</target>" + "</response>";
        private final String xmlError_ = "<response version=\"1.2\"> <target name=\"AccountAPI\" operation=\"getAccount\">"
                + "<error> <code>"
                + errorCode_
                + "</code><message>"
                + errorMessage_
                + "</message></error></target></response>";
    }
}
