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
package com.trilogy.app.crm.home.sub;

import com.trilogy.framework.xhome.beans.XBeans;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xlog.log.MajorLogMsg;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountHome;
import com.trilogy.app.crm.bean.BillCycle;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.bean.TaxAuthority;
import com.trilogy.app.crm.support.SpidSupport;
import com.trilogy.app.crm.support.SubscriberSupport;
import com.trilogy.app.crm.support.SystemSupport;

/**
 * @author jchen
 *
 * Auto create account objects in the background, if account is not visible
 * in the system, like buzzard.
 * 
 * We also need to sync account & subscriber merge view
 */
public class AutoCreateAccountHome extends HomeProxy
{
    // TODO review if this class is needed
	public AutoCreateAccountHome(Home delegate)
	{
		super(delegate);
	}

	/**
	 * Auto create account object if ban is not set
	 * @see com.redknee.framework.xhome.home.Home#create(java.lang.Object)
	 */
	@Override
    public Object create(Context ctx, Object obj) throws HomeException
	{
		Subscriber sub = (Subscriber) obj;
		if(sub.getSubscriberType() == SubscriberTypeEnum.PREPAID )
		{
			//No ban
			if (sub.getBAN() == null || sub.getBAN().trim().length() == 0)
			{
				if (SystemSupport.autoCreatesAccount(ctx))
				{
					try
					{
						//defaut should 
						Account account = (Account) XBeans.instantiate(Account.class, ctx);
	
						account = syncAccountFromSub(ctx, sub, account);
						
						final BillCycle bc = SpidSupport.getDefBillingCycle(ctx, account.getSpid());
				        if (bc != null)
				        {
				            account.setBillCycleID(bc.getBillCycleID());
				        }

				        final TaxAuthority ta = SpidSupport.getDefTaxAuthority(ctx, account.getSpid());
				        if (ta != null)
				        {
				            account.setTaxAuthority(ta.getTaxId());
				        }
	
						Home accHome = (Home) ctx.get(AccountHome.class);
						account = (Account) accHome.create(ctx,account);
						sub.setBAN(account.getBAN());
					}
					catch (Exception e)
					{
						String msg = "Failed to create subscriber's account";
						new MajorLogMsg(this, msg, e).log(ctx);
	
						//todo, clean up account record if failed
						throw new HomeException(msg, e);
					}
				}
				
			}	
		   
		}

		return super.create(ctx,obj);
	}

	/**
	 * copy values that shared between sub and account, copy from sub to account
	 * @param sub
	 * @param account
	 * @return
	 */
	public static Account syncAccountFromSub(Context ctx, Subscriber sub, Account account)
	{
		//      transfer some value from account to subscriber
		account.setDealerCode(sub.getDealerCode());
		account.setCurrency(sub.getCurrency(ctx));
        account.setLanguage(sub.getBillingLanguage());
        account.setSpid(sub.getSpid());

		// because on autocreate you would really like the name to be tranferred into the account [PaulSperneac]
        // TODO 2008-08-21 name no longer part of subscriber
		//account.setFirstName(sub.getFirstName());
		//account.setLastName(sub.getLastName());
		
         //set the BillingCycleId --- when the billcycleid is changed from the special Billcycle ID. 
       
		return account;
	}

	@Override
    public Object store(Context ctx,Object obj) throws HomeException
	{

		Subscriber sub = (Subscriber) obj;
		if (SystemSupport.autoCreatesAccount(ctx))
		{
			//to do, update only setting changed
			Account account = SubscriberSupport.lookupAccount(ctx, sub);
           
			account = syncAccountFromSub(ctx, sub, account);
			Home accHome = (Home) ctx.get(AccountHome.class);
			accHome.store(ctx,account);
		}

		return super.store(ctx,obj);
	}
}
