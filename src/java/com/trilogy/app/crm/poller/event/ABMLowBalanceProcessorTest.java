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
package com.trilogy.app.crm.poller.event;

import com.trilogy.framework.xhome.context.ContextSupport;
import junit.framework.TestCase;

/**
 * @author jchen
 */
public class ABMLowBalanceProcessorTest extends TestCase {

	public void testProcess()
	{
		//String erRecord = "2004/11/09,16:04:42,447,700,Synaxis2200,0,3977222412,5,6,7,0,1";
		String erRecord = "700, Synaxis2200,0,559052223507,5,6,7,0,1";
		
		ABMLowBalanceProcessor pr = new ABMLowBalanceProcessor(new ContextSupport(), 5, 5);
		char[] prChars = erRecord.toCharArray();
		int startIndex = 0;
		pr.process(0, "447", prChars, startIndex);
		
	}
}
