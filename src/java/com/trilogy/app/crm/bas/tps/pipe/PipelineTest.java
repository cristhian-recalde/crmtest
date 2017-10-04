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
package com.trilogy.app.crm.bas.tps.pipe;

import com.trilogy.app.crm.bas.tps.TPSRecord;
import com.trilogy.framework.xhome.context.Context;

import junit.framework.TestCase;


/**
 * @author jchen
 */
public class PipelineTest extends TestCase {

    public static void testPump(Context ctx)
    {
        int amount = 9;
        
        TPSRecord tps = null;
        
        try
        {
	        //Test correct account, and msisdn, but the msisdn are under different account
	        tps = new TPSRecord();
	        tps.setAccountNum("0167");
	        tps.setTelephoneNum("3977222266");  //in account "0111"
	        tps.setAmount(amount);
	        Pipeline.pump( ctx, tps);
        } catch(Exception e)  {e.printStackTrace();}
        
        
        try
        {
        
        //Test correct account, invalid msisdn, 
        tps = new TPSRecord();
        tps.setAccountNum("0167");
        tps.setTelephoneNum("3977222266123");  //in account "0111"
        tps.setAmount(amount);
        Pipeline.pump( ctx, tps);
        } catch(Exception e)  {e.printStackTrace();}
        
        try
        {
        
        //test invalid account, correct msisnd
        tps = new TPSRecord();
        tps.setAccountNum("01671233");
        tps.setTelephoneNum("3977222412");  //in account "0111"
        tps.setAmount(amount);
        Pipeline.pump( ctx, tps);
        
        } catch(Exception e)  {e.printStackTrace();}
        
        try
        {
        //test invalid account, and invalid msisdn
        tps = new TPSRecord();
        tps.setAccountNum("01671233");
        tps.setTelephoneNum("39772224121233");  //in account "0111"
        tps.setAmount(amount);
        Pipeline.pump( ctx, tps);
        
        } catch(Exception e)  {e.printStackTrace();}
        
        
        try
        {
        //test invalid account, and invalid msisdn
        tps = new TPSRecord();
        tps.setAccountNum("");
        tps.setTelephoneNum("39772224121233");  //in account "0111"
        tps.setAmount(amount);
        Pipeline.pump( ctx, tps);
        
        } catch(Exception e)  {e.printStackTrace();}
    }
    
}
