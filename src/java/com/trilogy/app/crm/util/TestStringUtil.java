package com.trilogy.app.crm.util;

import java.util.HashMap;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


/**
 * Tests the StringUtil utility functions
 * 
 * @author simar.singh@redknee.com
 * 
 */
public class TestStringUtil extends TestCase
{

    public static Test suite()
    {
        TestSuite suite = new TestSuite(TestStringUtil.class);
        return suite;
    }

    /**
     * @author simar.singh@redknee.com
     */
    @org.junit.Test
    public void testStringCount()
    {
        String token = "Redknee";
        int expectedTokenCount = 4;
        String input = " ";
        for (int count = 0; count < expectedTokenCount; count++)
        {
            input = input + token + " ";
        }
        assertEquals("Test - {General # Token Count}", expectedTokenCount, StringUtil.getMatchCount(input, token));
        assertEquals("Test - {Boundary # Token count on blank String}", 0, StringUtil.getMatchCount("", token));
        assertEquals("Test - {Boundary # Token count with blank Token}", 0, StringUtil.getMatchCount(input, ""));
        assertEquals("Test - {Boundary # Blank Token count with blank Token}", 0, StringUtil.getMatchCount("", ""));
    }

    /**
     * @author simar.singh@redknee.com
     */
    @org.junit.Test
    public void testStringReplacement()
    {
        String sourceString = "This test case tests {class}. {class} will be tested using {cases} written in {framework} framework.";
        String resultString = "This test case tests {TestStringUtil.class}. {TestStringUtil.class} will be tested using {unit test cases} written in {junit framwork} framework.";
        Map<String, String> key_value_pairs = new HashMap<String, String>();
        {
            // Test 1- {General # key-value replacement}
            key_value_pairs.put("{class}", "{TestStringUtil.class}");
            key_value_pairs.put("{framework}", "{junit framwork}");
            key_value_pairs.put("{cases}", "{unit test cases}");
            assertEquals("Test - {General # key-value replacement}", resultString, StringUtil.replaceAll(sourceString,
                    key_value_pairs));
        }
        {
            // Test 2- {Special # key-value replacement where key-string is a substring of
            // value-string}
            key_value_pairs.put("Name", "Name : Redknee");
            sourceString = "Company's Name";
            resultString = "Company's Name : Redknee";
            assertEquals("Test - {Special # key-value replacement where key-string is a substring of value-string}",
                    resultString, StringUtil.replaceAll(sourceString, key_value_pairs));
        }
        // Test 3- {Boundary # key-value replacement with empty key-value map}
        assertEquals("Test - {Boundary # key-value replacement with empty key-value map}", sourceString, StringUtil
                .replaceAll(sourceString, new HashMap<String, String>()));
        // Test 4- {Boundary # key-value replacement on blank String}
        assertEquals("Test - {Boundary # key-value replacement on blank String}", "", StringUtil.replaceAll("",
                new HashMap<String, String>()));
    }

    /**
     * @author simar.singh@redknee.com
     */
    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(suite());
    }
}