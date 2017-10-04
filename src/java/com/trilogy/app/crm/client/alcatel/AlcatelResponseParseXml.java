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
package com.trilogy.app.crm.client.alcatel;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.xml.sax.SAXException;

import com.trilogy.app.crm.client.xmlhttp.Response;
import com.trilogy.app.crm.client.xmlhttp.XmlParsedXpathQueryDocument;


/**
 * 
 * A class that prepares Alcatel XML response for querying and reponse extration
 * 
 * @author simar.singh@redknee.com
 * 
 */
public class AlcatelResponseParseXml extends XmlParsedXpathQueryDocument
{

    public final String xpathStringErrorCode = "/response/target/error/code";
    public final String xpathStringErrorMessage = "/response/target/error/message";
    public final String xpathStringAccountId = "/response/target/result/account/id";


    public AlcatelResponseParseXml(String xmlContent) throws XPathExpressionException, ParserConfigurationException,
            SAXException, IOException
    {
        super(xmlContent);
        xpathExprErrorCode_ = xpathFactory_.newXPath().compile(xpathStringErrorCode);
        xpathExprErrorMessage_ = xpathFactory_.newXPath().compile(xpathStringErrorMessage);
        xpathExprAccountId_ = xpathFactory_.newXPath().compile(xpathStringAccountId);
    }


    /**
     * Returns the XML Response object containing Error-Code and-or Error-Message
     * 
     * @return - Response(error-code,error-message) contining error or Response("","") for
     *         no error
     * @throws XPathExpressionException
     */
    public Response getResponse() throws XPathExpressionException
    {
        Response response = new Response();
        final String errorCode = getTextElement(xpathExprErrorCode_);
        final String errorMessage = getTextElement(xpathExprErrorMessage_);
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


    /**
     * Extracts the account-ID from XML using the appropriate XPATH expression
     * 
     * @return
     * @throws XPathExpressionException
     */
    public String getAccountID() throws XPathExpressionException
    {
        return getTextElement(xpathExprAccountId_);
    }

    private final XPathExpression xpathExprErrorCode_;
    private final XPathExpression xpathExprErrorMessage_;
    private final XPathExpression xpathExprAccountId_;

    /**
     * This class tests the containing class.
     * 
     * @author simar.singh
     * 
     */
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
         * Ensure that an Error Response is handled properly
         * 
         * @author simar.singh@redknee.com
         */
        @org.junit.Test
        public void testErrorResponse()
        {
            final AlcatelResponseParseXml parser;
            final Response response;
            try
            {
                parser = new AlcatelResponseParseXml(xmlError_);
                response = parser.getResponse();
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
         * Ensure that a a successful reponse containing account-id as one of return
         * values is parsed properly
         * 
         * @author simar.singh@redknee.com
         */
        @org.junit.Test
        public void testResponseWithAccountID()
        {
            final AlcatelResponseParseXml parser;
            final Response response;
            try
            {
                parser = new AlcatelResponseParseXml(xmlAccountID_);
                response = parser.getResponse();
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
                accountID = parser.getAccountID();
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
