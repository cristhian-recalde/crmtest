/*
 * This code is a protected work and subject to domestic and international copyright
 * law(s). A complete listing of authors of this work is readily available. Additionally,
 * source code is, by its very nature, confidential information and inextricably contains
 * trade secrets and other information proprietary, valuable and sensitive to Redknee. No
 * unauthorized use, disclosure, manipulation or otherwise is permitted, and may only be
 * used in accordance with the terms of the license agreement entered into with Redknee
 * Inc. and/or its subsidiaries.
 * 
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */
/*
 * @author jchen Created on Dec 16, 2005
 * 
 * @author simar.singh@redknee.com; re-implemented
 */
package com.trilogy.app.crm.util;

import java.util.Map;


/**
 * @author simar.singh@redknee.com
 * @author jchen
 * 
 *         Utility functions for String manipulation and analysis. Avoid modification to
 *         existing methods Keep TestStringUtil updated with new methods if added.
 */
public class StringUtil
{

    /**
     * Get the total count of String(token)'s occurrence in a String(input)
     * @author jchen
     * @param str
     * @param token
     * @return
     */
    public static int getMatchCount(String str, String token)
    {
        if (str == null || str.length() == 0)
            return 0;
        if (token == null || token.length() == 0)
            return 0;
        int count = 0;
        int lastIndex = 0;
        while ((lastIndex = str.indexOf(token, lastIndex)) != -1)
        {
            lastIndex += token.length();
            count++;
        }
        return count;
    }


    /**
     * Takes a string, replaces every occurrence of String(keys) with their corresponding
     * String(values). String replacement method
     * 
     * @author simar.singh@redknee.com
     * @param sourceString
     *            - Source String
     * @param keyValuePairs
     *            - Map of key-value pairs
     * @return - resulting a String
     */
    public static String replaceAll(String sourceString, Map<?, ?> keyValuePairs)
    {
        final StringBuilder buff = new StringBuilder(sourceString);
        for (Map.Entry<?, ?> key_value : keyValuePairs.entrySet())
        {
            replaceAll(buff, String.valueOf(key_value.getKey()), String.valueOf(key_value.getValue()));
        }
        return buff.toString();
    }


    /**
     * Takes a string, replaces all occurrence of String(key) with String(value)
     * 
     * @author simar.singh@redknee.com
     * @param sourceString
     *            - Source String
     * @param key
     *            - key to be replaced
     * @param value
     *            value that replaces the key
     * @return - resulting a String
     */
    public static String replaceAll(String sourceString, String key, String value)
    {
        final StringBuilder buff = new StringBuilder(sourceString);
        replaceAll(buff, key, value);
        return buff.toString();
    }


    /**
     * Internal method used in String Replacement This method is tricky, avoid
     * modification else be cautious It takes a StringBuffer input and modifies it state
     * as result.
     * 
     * @author simar.singh@redknee.com
     * @param buff
     * @param key
     * @param value
     */
    private static void replaceAll(StringBuilder buff, String key, String value)
    {
        int currentIndex = buff.indexOf(key);
        while (currentIndex >= 0)
        {
            buff.replace(currentIndex, currentIndex + key.length(), value);
            currentIndex = buff.indexOf(key, currentIndex + value.length());
        }
    }
    
    public static boolean checkIfStringIsEmpty(String sourceString)
    {
        if(sourceString == null || sourceString.trim().equals(""))
        	return true;
        return false;
    }
    
    public static final String NEW_LINE = System.getProperty("line.separator");
    public static final String[] EMPTY_STRING_ARRAY = new String[0];
}
