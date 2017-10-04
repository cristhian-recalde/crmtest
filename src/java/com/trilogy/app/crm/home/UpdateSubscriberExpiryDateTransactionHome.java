/*
 * This code is a protected work and subject to domestic and international
 * copyright law(s). A complete listing of authors of this work is readily
 * available. Additionally, source code is, by its very nature, confidential
 * information and inextricably contains trade secrets and other information
 * proprietary, valuable and sensitive to Redknee, no unauthorised use,
 * disclosure, manipulation or otherwise is permitted, and may only be used in
 * accordance with the terms of the licence agreement entered into with Redknee
 * Inc. and/or its subsidiaries.
 * 
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.

 * Created on 8-Dec-2005
 *
 */
package com.trilogy.app.crm.home;
import java.util.Date;

import com.trilogy.app.crm.bean.AdjustmentTypeActionEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberHome;
import com.trilogy.app.crm.bean.Transaction;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.app.crm.support.SubscriberSupport;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xlog.log.MajorLogMsg;

/**
 * @author jke
 * 
 * Update the Expiry Date for Prepaid subscriber.
 */
public class UpdateSubscriberExpiryDateTransactionHome
extends HomeProxy
{

    public UpdateSubscriberExpiryDateTransactionHome(Home _delegate)
	{
		super(_delegate);
	}
    
    /**
     * @param ctx The operating context.
     * @param obj The transaction.
     */

    public Object create(Context ctx, final Object obj)
        throws HomeException
    {
		if (obj != null)
		{
			if (obj instanceof Transaction)
			{
				Transaction t = (Transaction) obj;
				
				if(AdjustmentTypeActionEnum.CREDIT == t.getAction())
				{
					Subscriber sub = SubscriberSupport.lookupSubscriberForMSISDN(ctx,t.getMSISDN(), t.getTransDate());
					if (sub != null)
					{
					 // Update the Expiry Date
						if( sub.isPrepaid() && t.getExpiryDaysExt() > 0)
					    {
					        Date expiryDate = CalendarSupportHelper.get(ctx).findDateDaysAfter(t.getExpiryDaysExt(), sub.getExpiryDate(), sub.getTimeZone(ctx));
					        sub.setExpiryDate(expiryDate);
					        updateSubscriber(ctx, sub);
					    }
					}
				}
			}
		}

        return super.create(ctx,obj);
    }

   /**
    * @param ctx The operating context.
    * @param sub The Subscriber to be updated.
    */

    private void updateSubscriber(Context ctx, Subscriber sub)
    {
   		try
		{
   		    SubscriberSupport.updateExpiryOnCrmAbmBM(ctx, sub);
		}
   		catch(Exception e)
   		{
	    	new MajorLogMsg("UpdateSubscriberExpiryDateTransactionHome.updateSubscriberExpiryDate()",
	        		e.getMessage(), e).log(ctx);
   		}
    }

}
