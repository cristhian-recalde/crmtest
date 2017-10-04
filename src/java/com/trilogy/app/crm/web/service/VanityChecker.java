/*
 * This code is a protected work and subject to domestic and international
 * copyright law(s).  A complete listing of authors of this work is readily
 * available.  Additionally, source code is, by its very nature, confidential
 * information and inextricably contains trade secrets and other information
 * proprietary, valuable and sensitive to Redknee.  No unauthorized use,
 * disclosure, manipulation or otherwise is permitted, and may only be used
 * in accordance with the terms of the license agreement entered into with
 * Redknee Inc. and/or its subsidiaries.
 *
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.web.service;

/**
 * Utility class to check vanity MSISDN.
 *
 * @author larry.xia@redknee.com
 */
public final class VanityChecker
{

    /**
     * Creates a new <code>VanityChecker</code> instance. This method is made private to
     * prevent instantiation of utility class.
     */
    private VanityChecker()
    {
        // empty
    }


    /**
     * Whether the MSISDN is a full-house in the suffix
     *
     * @param msisdn
     *            MSISDN being checked.
     * @return Returns <code>true</code> if the MSISDN is a full-house,
     *         <code>false</code> otherwise.
     */
    public static boolean isFullhouse(final String msisdn)
    {
    	boolean result = true;
        int suffixLength = 5;
        String suffix = msisdn.substring(msisdn.length() - suffixLength);
        
        // A "set" is 3-of-a-kind
        /* The set and the pair cannot be the same number.  
         * If we check for a set of three like "x333x", and it exists in the suffix, 
         * then we know this isn't a legitimate full house. We want to prevent the following 
         * from being considered as a full house, 33333.
         */ 
        boolean hasDistinctDigits = !isSame(suffix.substring(1,4).toCharArray(), 3);
        if (hasDistinctDigits)
        {
        	boolean setThenPair = isSame(suffix.substring(0,3).toCharArray(), 3) 
        	&& isSame(suffix.substring(3).toCharArray(), 2);

        	if (!setThenPair)
        	{
        		boolean pairThenSet = isSame(suffix.substring(0,2).toCharArray(), 2) 
        		&& isSame(suffix.substring(2).toCharArray(), 3);
        		if (!pairThenSet)
        		{
        			result = false;
        		}
        	}
        }
        else 
        {
        	// Validated to being like x333x.
        	result = false;
        }
        return result;
    }


    /**
     * Whether the MSISDN contains the specified number of pairs in the suffix
     *
     * @param msisdn
     *            MSISDN being checked.
     * @param pair
     *            Number of pairs required.
     * @return Returns <code>true</code> if the MSISDN has at least the requested number
     *         of straight pairs, <code>false</code> otherwise.
     */
    public static boolean isStraightPairs(final String msisdn, final int pair)
    {
        int suffixLength = pair * 2;
        String suffix = msisdn.substring(msisdn.length() - suffixLength);
        final char[] num = suffix.toCharArray();

        for (int i = 0; i < num.length - 1; i=i+2)
        {
            if (num[i] != num[i + 1])
            {
            	return false;
            }
            /* Peek ahead and make sure that the digits ahead are distinct from the current digit.
             * We want to avoid matching 777777 as multiple pairs (they are not).  
             * We only have to check one number out of every pair in the suffix. If they aren't pairs 
             * they will be caught by the other check. */
            int peekAhead = i+2;
            while (peekAhead < num.length - 1)
            {
            	if (num[i] == num[peekAhead])
            	{
            		return false;
            	}
            	peekAhead += 2;
            }
        }
        
        return true;
    }


    /**
     * Whether the MSISDN is contains at least the provided number of pairs.
     *
     * @param msisdn
     *            MSISDN being checked.
     * @param pair
     *            The minimum number of pairs in the MSISDN.
     * @return Returns <code>true</code> if the MSISDN contains the minimum number of
     *         pairs, <code>false</code> otherwise.
     * @deprecated
     * 		Deprecated since the definitions of Pairs have changed to mean "Contiguous Pairs"
     */
    private static boolean isPairs(final String msisdn, final int pair)
    {
        int count = 0;
        final char[] num = msisdn.toCharArray();

        for (int i = 0; i < num.length - 1; ++i)
        {
            for (int j = i + 1; j < num.length; ++j)
            {

                if (num[i] == num[j] && num[i] != 'a')
                {
                    ++count;
                    num[j] = 'a';
                    break;
                }
            }
        }

        if (count < pair)
        {
            return false;
        }

        return true;
    }


    /**
     * Whether the MSISDN has a straight of the given length in the suffix
     *
     * @param msisdn
     *            MSISDN being checked.
     * @param row
     *            The length of the straight.
     * @return Returns <code>true</code> if the MSISDN contains a straight of the
     *         given length, <code>false</code> otherwise.
     */
    public static boolean isStraight(final String msisdn, final int row)
    {
    	String suffix = msisdn.substring(msisdn.length() - row);
        final char[] num = suffix.toCharArray();

        for (int i = 0; i < num.length - 1; ++i)
        {
        	if (num[i] + 1 != num[i+1])
        	{
        		return false;
        	}
        }

        return true;
    }

    /**
     * Whether the MSISDN contains a the given pattern 
     *
     * @param msisdn
     *            MSISDN being checked.
     * @return Returns <code>true</code> if the MSISDN contains the given pattern, <code>false</code> otherwise.
     */
    public static boolean isValidFourDigitPattern(final String msisdn, final int pattern)
    {
        
        if ( msisdn.length() < 4)
        {
            return false;
        }
        
        final char[] num = msisdn.toCharArray();
        
        char pos1 = num[msisdn.length()-4];
        char pos2 = num[msisdn.length()-3];
        char pos3 = num[msisdn.length()-2];
        char pos4 = num[msisdn.length()-1];
        
        boolean result = false;
        if ( pattern == FOUR_DIGIT_PATTERN_AAAB)
        {
            result = (pos1 == pos2) && (pos2 == pos3) && (pos1 != pos4) ? true : false;
        }
        else if (pattern == FOUR_DIGIT_PATTERN_ABAB)
        {
            result = (pos1 == pos3) && (pos2 == pos4) && (pos1 != pos2) ? true : false;            
        }
        else if (pattern == FOUR_DIGIT_PATTERN_ABBA)
        {
            result = (pos1 == pos4) && (pos2 == pos3) && (pos1 != pos2) ? true : false;            
        }
            
        return result;
    }
    

    /**
     * Whether the MSISDN contains a given number of same digit in a row.
     *
     * @param msisdn
     *            MSISDN being checked.
     * @param row
     *            number of the same digit in a row.
     * @return Returns <code>true</code> if the MSISDN contains the number of
     *         the same digits in a row, <code>false</code> otherwise.
     */
    public static boolean isSame(final String msisdn, final int row)
    {
        String suffix = msisdn.substring(msisdn.length() - row);
        return isSame(suffix.toCharArray(), row);
    }

    
    
    /**
     * Whether the String contains the given number of same digit in a row.
     *
     * @param sequence
     *            sequence of numbers being checked.
     * @param row
     *            number of the same digit in a row.
     * @return Returns <code>true</code> if the MSISDN contains the number of
     *         the same digits in a row, <code>false</code> otherwise.
     */
    private static boolean isSame(final char[] sequence, final int row)
    {
    	for (int i = 0; i < sequence.length - 1; ++i)
        {
    		//check ahead in the sequence
        	if (sequence[i] != sequence[i+1])
        	{
        		return false;
        	}
        }
        return true;
    }
    
    public static final int FOUR_DIGIT_PATTERN_ABBA = 1;
    public static final int FOUR_DIGIT_PATTERN_ABAB = 2;
    public static final int FOUR_DIGIT_PATTERN_AAAB = 3;
    

}
