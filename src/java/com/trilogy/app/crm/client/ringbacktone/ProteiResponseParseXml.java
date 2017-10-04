package com.trilogy.app.crm.client.ringbacktone;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;

import org.xml.sax.SAXException;

import com.trilogy.app.crm.client.xmlhttp.XmlParsedXpathQueryDocument;

public class ProteiResponseParseXml
extends XmlParsedXpathQueryDocument
{

    public ProteiResponseParseXml(String xmlContent, String codePath) throws XPathExpressionException, ParserConfigurationException,
    SAXException, IOException
    {   
        super(xmlContent, true, false);
        XPath xpath = xpathFactory_.newXPath(); 
        
       /*
        * handle multiple namespace in xml, no need after protei team
        * fixed the format of response xml. 
         NamespaceContext ctx = new NamespaceContext() {
        
            
            public String getNamespaceURI(String prefix) {
                String uri;
                if (prefix.equals("soap"))
                    uri = "http://schemas.xmlsoap.org/soap/envelope/";
                else
                    uri = null;
                return uri;
            }
            
            // Dummy implementation - not used!
            public Iterator getPrefixes(String val) {
                return null;
            }
            
            // Dummy implemenation - not used!
            public String getPrefix(String uri) {
                return null;
            }
        };

        xpath.setNamespaceContext(ctx);
        */ 
        xpathExprResultCode_ = xpath.compile(codePath);

    }
    
    public String getResultCode() throws XPathExpressionException
    {
        
       return getTextElement(xpathExprResultCode_);
    }

    private final XPathExpression xpathExprResultCode_;

}
