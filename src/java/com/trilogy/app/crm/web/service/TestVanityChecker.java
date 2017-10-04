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

import junit.framework.TestCase;

/**
 * Unit test for Vanity Checker Pattern matching Options.
 * 
 * @author ganderson
 */
public class TestVanityChecker
    extends TestCase
{
	/*
	 * Vanity Pattern Options 
	 */
    //The Zero (0) bit is reserved for failure to match the options below
    private static final int NOT_VANITY = 1;
    private static final int FULL_HOUSE = 2;
    private static final int TWO_PAIRS = 4;
    private static final int THREE_PAIRS = 8;
    private static final int STRAIGHT_FOUR = 16;
    private static final int STRAIGHT_FIVE = 32;
    private static final int STRAIGHT_SIX = 64;
    private static final int STRAIGHT_SEVEN = 128;
    private static final int SAME_THREE = 256;
    private static final int SAME_FOUR = 512;
    private static final int SAME_FIVE = 1024;
    private static final int SAME_SIX = 2048;
    private static final int SAME_SEVEN = 4096;

    class VanityTestCase
    {
        public VanityTestCase(String msisdn, int type)
        {
            this.msisdn = msisdn;
            this.type = type;
        }

        /**
         * Returns True if the given Vanity type matches this 
         * VanityTestCase's expected Vanity Pattern options. 
         * 
         * @param type
         * @return
         */
        public boolean is(final int type)
        {
            return (this.type & type) > 0;
        }

        /**
         * The String (Mobile Number) against which we'll check the Vanity Patterns  
         */
        public String msisdn;
        
        /**
         * Expected matching Vanity Pattern options
         */ 
        public int type;
    }

    /**
     * Unit test for Vanity Number Pattern Matching
     */
    public void testVanityNumbers()
    {
        final VanityTestCase[] cases =
        {
        	// Random number
            new VanityTestCase("9056278134", NOT_VANITY),
            
            //Pairs
            // Only one pair in the suffix
            new VanityTestCase("9056278244", NOT_VANITY),
            // Only one pair in the suffix
            new VanityTestCase("9056278442", NOT_VANITY),
            // Only one pair in the suffix
            new VanityTestCase("9056274482", NOT_VANITY),
            // Pairs are not in the suffix (off by 1)
            new VanityTestCase("9053377884", NOT_VANITY),
            // Pairs are not in the suffix (off by 2)
            new VanityTestCase("9433778834", NOT_VANITY),
            // Pairs are not in the suffix (off by 3)
            new VanityTestCase("9337788434", NOT_VANITY),
            // Pairs are not in the suffix (at the top)
            new VanityTestCase("3377884349", NOT_VANITY),
            // Pairs are not in the suffix
            new VanityTestCase("3377884349", NOT_VANITY),
            // Pairs are not contiguous in the suffix
            new VanityTestCase("3493377988", NOT_VANITY),
            // Pairs are not contiguous in the suffix
            new VanityTestCase("3493377988", NOT_VANITY),
            // Pairs are in the suffix
            new VanityTestCase("3493397788", TWO_PAIRS),
            new VanityTestCase("3399337788", TWO_PAIRS | THREE_PAIRS),
            // Pairs in suffix are the same number, NOT considered Vanity Pairs or a Full House
            new VanityTestCase("9077777777", SAME_THREE | SAME_FOUR | SAME_FIVE | SAME_SIX | SAME_SEVEN),
            
            // Straight
            // Straight is in descending order in the suffix
            new VanityTestCase("9029876543", NOT_VANITY),
            // 9 does not connect to 0 for a Straight in suffix
            new VanityTestCase("9021567890", NOT_VANITY),
            // Straight is not in the suffix (off by 1)
            new VanityTestCase("9023456786", NOT_VANITY),
            // Straight is not in the suffix (off by 2)
            new VanityTestCase("5234567869", NOT_VANITY),
            // Straight is not in the suffix (off by 3)
            new VanityTestCase("3456789286", NOT_VANITY),
            // Straight is in suffix
            new VanityTestCase("1324850123", STRAIGHT_FOUR),
            new VanityTestCase("1324834567", STRAIGHT_FOUR | STRAIGHT_FIVE),
            new VanityTestCase("1320456789", STRAIGHT_FOUR | STRAIGHT_FIVE | STRAIGHT_SIX),
            new VanityTestCase("1302345678", STRAIGHT_FOUR | STRAIGHT_FIVE | STRAIGHT_SIX | STRAIGHT_SEVEN),
            
            // Full House 
            // pair of 4's with no set
            new VanityTestCase("9056244123", NOT_VANITY),
            // set of 4's with no pair
        	new VanityTestCase("9056244423", NOT_VANITY),
        	// set of 5's followed by pair 2's not contiguous in suffix
        	new VanityTestCase("9056255123", NOT_VANITY),
        	// set of 5's followed by pair 4's not in suffix (off by 1)
        	new VanityTestCase("9012555443", NOT_VANITY),
        	// set of 5's followed by pair 4's not in suffix (off by 2)
        	new VanityTestCase("9055554423", NOT_VANITY),
        	// set of 5's followed by pair 4's not in suffix (off by 3)
        	new VanityTestCase("9055544123", NOT_VANITY),
        	// set of 5's followed by pair 4's not in suffix (off by 4)
        	new VanityTestCase("9555440129", NOT_VANITY),
        	// set of 5's followed by pair 4's not in suffix (at top)
        	new VanityTestCase("5554494012", NOT_VANITY),
        	// pair 4's followed by set of 5's not in suffix (at top)
        	new VanityTestCase("4455594012", NOT_VANITY),
        	// pair 4's followed by set of 5's not in suffix (off by 1)
        	new VanityTestCase("9012445553", NOT_VANITY),
        	// pair 4's followed by set of 5's not in suffix (off by 2)
        	new VanityTestCase("9014455523", NOT_VANITY),
        	// pair 4's followed by set of 5's not in suffix (off by 3)
        	new VanityTestCase("9044555123", NOT_VANITY),
        	// pair 4's followed by set of 5's not in suffix (off by 4)
        	new VanityTestCase("9445550129", NOT_VANITY),
            // Full house is in the suffix: A pair followed by a set  
            new VanityTestCase("9900111222", FULL_HOUSE | SAME_THREE),
            // Full house is in the suffix: A set followed by a pair  
            new VanityTestCase("1234199922", FULL_HOUSE | TWO_PAIRS),
            
            // Repetition is not in suffix (off by 1)
            new VanityTestCase("1299999993", NOT_VANITY),
            // Repetition is not in suffix (off by 2)
            new VanityTestCase("1999999923", NOT_VANITY),
            // Repetition at the top of String
            new VanityTestCase("9999999123", NOT_VANITY),
            // Repetition
            new VanityTestCase("9056278222", SAME_THREE),
            new VanityTestCase("9056272222", SAME_THREE | SAME_FOUR),
            new VanityTestCase("9256722222", SAME_THREE | SAME_FOUR | SAME_FIVE),
            new VanityTestCase("9256222222", SAME_THREE | SAME_FOUR | SAME_FIVE | SAME_SIX),
            new VanityTestCase("9052222222", SAME_THREE | SAME_FOUR | SAME_FIVE | SAME_SIX | SAME_SEVEN),
            
        };

        /*
         * This testing method not only verifies positive pattern matching, 
         * but also indicates all pattern matches where there shouldn't be any.
         * 
         * Any time there is a failure, the rest of the test case will abort, 
         * but we will have notice of which case failed in the Failure message.
         */
        for (final VanityTestCase testcase : cases)
        {
        	boolean isTwoPairs = VanityChecker.isStraightPairs(testcase.msisdn, 2);
            validateTestResult("Two Pairs: " + testcase.msisdn, testcase.is(TWO_PAIRS), isTwoPairs);
            
            boolean isThreePairs = VanityChecker.isStraightPairs(testcase.msisdn, 3);
            validateTestResult("Three Pairs: " + testcase.msisdn, testcase.is(THREE_PAIRS), isThreePairs);

            boolean isFullHouse = VanityChecker.isFullhouse(testcase.msisdn);
            validateTestResult("Full House: " + testcase.msisdn, testcase.is(FULL_HOUSE), isFullHouse);
            
            boolean isFourStraight = VanityChecker.isStraight(testcase.msisdn, 4);
            validateTestResult("Four-Digit Straight: " + testcase.msisdn, testcase.is(STRAIGHT_FOUR), isFourStraight);
            
            boolean isFiveStraight = VanityChecker.isStraight(testcase.msisdn, 5);
            validateTestResult("Five-Digit Straight: " + testcase.msisdn, testcase.is(STRAIGHT_FIVE), isFiveStraight);
            
            boolean isSixStraight = VanityChecker.isStraight(testcase.msisdn, 6);
            validateTestResult("Six-Digit Straight: " + testcase.msisdn, testcase.is(STRAIGHT_SIX), isSixStraight);
            
            boolean isSevenStraight = VanityChecker.isStraight(testcase.msisdn, 7);
            validateTestResult("Seven-Digit Straight: " + testcase.msisdn, testcase.is(STRAIGHT_SEVEN), isSevenStraight);
            
            boolean isSameThree = VanityChecker.isSame(testcase.msisdn, 3);
            validateTestResult("Three-of-a-Kind: " + testcase.msisdn, testcase.is(SAME_THREE), isSameThree);
            
            boolean isSameFour = VanityChecker.isSame(testcase.msisdn, 4);
            validateTestResult("Four-of-a-Kind: " + testcase.msisdn, testcase.is(SAME_FOUR), isSameFour);
            
            boolean isSameFive = VanityChecker.isSame(testcase.msisdn, 5);
            validateTestResult("Five-of-a-Kind: " + testcase.msisdn, testcase.is(SAME_FIVE), isSameFive);
            
            boolean isSameSix = VanityChecker.isSame(testcase.msisdn, 6);
            validateTestResult("Six-of-a-Kind: " + testcase.msisdn, testcase.is(SAME_SIX), isSameSix);
            
            boolean isSameSeven = VanityChecker.isSame(testcase.msisdn, 7);
            validateTestResult("Seven-of-a-Kind: " + testcase.msisdn, testcase.is(SAME_SEVEN), isSameSeven);
            
            boolean isNotVanity = (false == (isTwoPairs | isThreePairs | isFullHouse | 
            						isFourStraight | isFiveStraight | isSixStraight | isSevenStraight | 
            						isSameThree | isSameFour | isSameFive | isSameSix | isSameSeven) );
            validateTestResult("Not Vanity: " + testcase.msisdn, testcase.is(NOT_VANITY), isNotVanity);
            
        }
        
        assertTrue("There were failures in the TestVanityChecker.  See report in console.", overallTestPass);
    }
    
    /**
     * Depending on the local reporting flag either:
     *  1) Log the list of pattern validation results, (completes entire Test suite) or 
     *  2) validate Unit Test Case with assertEquals (potentially aborting the remainder of the Test)  
     * @param msg
     * @param expectedResult  the expected result of the pattern matching
     * @param checkerResult  the actual result of the pattern matching
     */
    private void validateTestResult(final String msg, final boolean expectedResult, final boolean checkerResult)
    {
    	if (reportToLog)
    	{
    		String result = "Pass";
    		if (expectedResult != checkerResult)
    		{
    			result = "FAIL";
    			setOverAllTestFailed();
    		}
    		
    		StringBuilder output = new StringBuilder();
    		output.append(msg);
    		if (printExpected)
    		{
    			output.append(" (Expected: " + expectedResult + ")");
    		}
    		output.append(" [RESULT: " + result + "]");
    		if (expectedResult != checkerResult)
    		{
    			System.out.println(output);
    		}
    	}
    	else
    	{
    		assertEquals(msg, expectedResult, checkerResult);
    	}
    }
    
    private void setOverAllTestFailed()
    {
    	if (overallTestPass)
    	{
    		overallTestPass = false;
    	}
    }
    
    /**
     * Depending on this flag either:
     *  1) Log the list of pattern validation results, (completes entire Test suite) or 
     *  2) validate Unit Test Case with assertEquals (potentially aborting the remainder of the Test) 
     */
    private boolean reportToLog = true;
    
    /**
     * Depending on the local reporting flag, add expected result to report messages.
     */
    private boolean printExpected = true;
    
    /**
     * Flag marks the failure of the test if running in reporting mode.
     */
    private boolean overallTestPass = true;
}
