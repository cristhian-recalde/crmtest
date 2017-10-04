/*
 * Copyright (c) 2007, REDKNEE.com. All Rights Reserved.
 * 
 * This software is the confidential and proprietary information of REDKNEE.com.
 * ("Confidential Information"). You shall not disclose such Confidential Information and
 * shall use it only in accordance with the terms of the license agreement you entered
 * into with REDKNEE.com.
 * 
 * REDKNEE.COM MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF THE
 * SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, OR NON-INFRINGEMENT.
 * REDKNEE.COM SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF
 * USING, MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS DERIVATIVES.
 */



package com.trilogy.app.crm.home.sub;

import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.bean.SubscriberXInfo;
import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.beans.Validator;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.LogSupport;

/**
 * Validates Monthly Spend Limit.
 * 
 * @author piyush.shirke@redknee.com
 * 
 */

public class MonthlySpendLimitValidator implements Validator {

	public MonthlySpendLimitValidator()
	{
	}
	
	@Override
	public void validate(Context ctx, Object obj) throws IllegalStateException {

		 final Subscriber newSub = (Subscriber) obj;
		 if(newSub.isPrepaid() || newSub.isPooled(ctx))
	     {
	            // nothing to validate as monthly spend limit for a pre-paid or pooled is never used
	            return;
	     }
		
		 try
         {
		     /**
		      * Nothing to validate for subscriptions of Group pooled account
		      * TT Fix TT#13070248026
		      * Document referred for further analysis BSS Sgr ID: SgR.CRM.1391. 
		      */
		     
            if(newSub.getAccount(ctx).isPooled(ctx))
            {
               return;
            }
         }
		 catch (HomeException e)
         {
            LogSupport.minor(ctx, this, "Exception occured while fetching account" +
            		" group type for subscription with identifier : "+newSub.getId()+" "+e.getMessage());
         }
		 
		 if(newSub.getSubscriberType() == SubscriberTypeEnum.POSTPAID){
			
		   if(newSub.getMonthlySpendLimit() == -1){
				return;
		   }
		 
		   if (newSub.getMonthlySpendLimit() == 0)
		   {
	            final CompoundIllegalStateException el = new CompoundIllegalStateException();
	            el.thrown(new IllegalPropertyArgumentException(
	                    SubscriberXInfo.MONTHLY_SPEND_LIMIT,
	                    "POSTPAID Subscriber cannot have ZERO Monthly Spend Limit. Please refer help for appropriate values."));
	            el.throwAll();
	        }
	        else if (newSub.getMonthlySpendLimit() > newSub.getCreditLimit())
	        {
	            final CompoundIllegalStateException el = new CompoundIllegalStateException();
	            el.thrown(new IllegalPropertyArgumentException(
	                    SubscriberXInfo.MONTHLY_SPEND_LIMIT,
	                    "POSTPAID Subscriber cannot have Monthly Spend Limit more than Credit Limit. Please refer help for appropriate values."));
	            el.throwAll();
	        }
		 }

	}
	 public static MonthlySpendLimitValidator instance()
	    {
	        if (instance == null)
	        {
	            instance = new MonthlySpendLimitValidator();
	        }

	        return instance;
	    }

	private static MonthlySpendLimitValidator instance;
}
