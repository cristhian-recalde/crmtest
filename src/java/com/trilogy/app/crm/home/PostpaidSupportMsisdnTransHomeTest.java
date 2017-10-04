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
package com.trilogy.app.crm.home;

import java.util.Date;

import junit.framework.TestCase;

import com.trilogy.app.crm.bean.core.AdjustmentType;
import com.trilogy.app.crm.bean.AdjustmentTypeEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.support.AdjustmentTypeSupportHelper;
import com.trilogy.app.crm.support.SubscriberSupport;
import com.trilogy.app.crm.support.TransactionSupport;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;


/**
 * @author jchen
 *
 * TODO add javadoc
 */
public class PostpaidSupportMsisdnTransHomeTest extends TestCase {

    /**
     * 
     * @param ctx
     */
    public static void testCreate(Context ctx)
    {
        try
        {
	        String prepaidMsisdn = "3988222243";
	        //String postpaidMsisn = "3977222412";
	        
	        Subscriber sub = SubscriberSupport.lookupSubscriberForMSISDN(ctx, prepaidMsisdn);
	        AdjustmentTypeEnum typeIdentifier = AdjustmentTypeEnum.RecurringCharges;
	        long amount = 111;
	         
	        final AdjustmentType type =
	                AdjustmentTypeSupportHelper.get(ctx).getAdjustmentType(ctx, typeIdentifier);
	
	       TransactionSupport.createTransaction(ctx, sub, amount, type, true, new Date());
        }
        catch(HomeException e)
        {
            e.printStackTrace();
        }
    } 
    
   
}
