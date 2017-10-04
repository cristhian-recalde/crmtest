/*
 * Created on Sep 22, 2003
 *
 * Copyright (c) 1999-2003 REDKNEE.com. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of
 * REDKNEE.com. ("Confidential Information"). You shall not disclose such
 * Confidential Information and shall use it only in accordance with the
 * terms of the license agreement you entered into with REDKNEE.com.
 *
 * REDKNEE.COM MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE
 * SUITABILITY OF THE SOFTWARE, EITHCDR EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE IMPLIED WARRANTIES OF MCDRCHANTABILITY, FITNESS FOR
 * A PARTICULAR PURPOSE, OR NON-INFRINGEMENT. REDKNEE.COM SHALL NOT BE
 * LIABLE FOR ANY DAMAGES SUFFCDRED BY LICENSEE AS A RESULT OF USING,
 * MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS DCDRIVATIVES.
 */
package com.trilogy.app.crm.home;

import java.security.Principal;

import com.trilogy.framework.xhome.auth.bean.User;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.OMLogMsg;

import com.trilogy.app.crm.Common;
import com.trilogy.app.crm.bean.core.Transaction;
import com.trilogy.app.crm.support.CoreTransactionSupportHelper;
import com.trilogy.app.crm.support.SystemSupport;
import com.trilogy.app.crm.support.UserGroupSupport;

/**
 * This is a home that only allows transaction operations for adjustments
 * that are below a certain limit. The limit is set per user group.
 
 * @author psperneac
 */
public class AdjustmentLimitTransactionHome 
	extends    HomeProxy
{
	public AdjustmentLimitTransactionHome(Home _delegate)
	{
		super(_delegate);
	}
	/** 
	 * @see com.redknee.framework.xhome.home.Home#create(java.lang.Object)
	 */
	@Override
    public Object create(Context ctx, Object obj) throws HomeException
	{
		//Larry: why hide account adjustment attempt om here?
		new OMLogMsg(Common.OM_MODULE, Common.OM_ACCT_ADJUST_ATTEMPT).log(ctx);

		Transaction trans=(Transaction)obj;

		//Larry: for payment, there is not such ajustment limit restriction.
        if (!CoreTransactionSupportHelper.get(ctx).isPayment(ctx, trans))
		{
			if(!validTransaction(ctx,trans))
			{               
				throw new HomeException("The transaction of Msisdn="+ trans.getMSISDN()+" exceeds the maximum allowed transaction limit of "+getAdjustmentLimit(ctx) + ", adjustmentType=" + trans.getAdjustmentType() + ", amount=" + trans.getAmount()+", exemptCreditLimitChecking="+trans.getExemptCreditLimitChecking());
			}
		}
        if(CoreTransactionSupportHelper.get(ctx).isInitialBalanceCredit(ctx, trans)){
        	
        	LogSupport.debug(ctx, this, "Increasing Daily Adjustment Limit by "+trans.getAmount());
        	UserDailyAdjustmentLimitTransactionIncreaseHome.increaseDailyLimitUsage(ctx, SystemSupport.getAgent(ctx), trans.getAmount());
        	
        }
		return super.create(ctx,obj);
	}

	/**
	 * checks a transaction for validity
	 * @param trans The transaction
	 * @return true if the transaction is valid given the adjustment limit or this
     *          transaction is exempted credit limit checking.
	 */
	private boolean validTransaction(Context ctx,Transaction trans)
	{
 		//adjustment 		
		if ( trans == null || trans.getExemptCreditLimitChecking() )
        {
            return true;            
        }
        else {
            return Math.abs(trans.getAmount())<=Math.abs(getAdjustmentLimit(ctx));
        }
	}

	/**
	 * Returns the adjustment limit for the user represented by the Principal in the context.
	 * The adjustment limit is stored in the group from which the user belongs, so the group
	 * respective is looked up.
	 * 
	 * @param ctx The current context
	 * @return integer, the current user's adjustment limit. if the user does
	 * not exist, return the DEFAULT_ADJUST_LIMIT
	 */
	private long getAdjustmentLimit(Context ctx)
	{
		
		User user=(User)ctx.get(Principal.class);
		if(user==null)
		{
			return Common.DEFAULT_ADJUST_LIMIT;
		}
		
		
		try
		{
			return UserGroupSupport.getAdjustmentLimit(ctx);
		}
		catch (HomeException e)
		{
			if (LogSupport.isDebugEnabled(ctx))
			{
				new DebugLogMsg(this,e.getMessage(),e).log(ctx);
			}
		}

		return Common.DEFAULT_ADJUST_LIMIT;
	}
}

