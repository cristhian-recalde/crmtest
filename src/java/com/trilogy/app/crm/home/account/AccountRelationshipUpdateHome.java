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

package com.trilogy.app.crm.home.account;

import java.util.Collection;
import java.util.Date;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.account.AccountRelationship;
import com.trilogy.app.crm.bean.account.AccountRelationshipHome;
import com.trilogy.app.crm.bean.account.AccountRelationshipTypeEnum;
import com.trilogy.app.crm.bean.DiscountStrategyEnum;
import com.trilogy.app.crm.support.AccountSupport;

import com.trilogy.framework.xhome.auth.bean.User;
import com.trilogy.framework.xhome.beans.XBeans;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xhome.session.SessionInfo;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MinorLogMsg;

/**
 * @author vikash.kumar@redknee.com
 * @since 2016-10-10 
 * Purpose: To add entry into AccountRelationship table once a
 *        account is created. To have a mapping between ResponsibleBAN with BAN.
 */

public class AccountRelationshipUpdateHome extends HomeProxy {

	public static String MODULE = AccountRelationshipUpdateHome.class.getName();

	public AccountRelationshipUpdateHome(Context ctx, Home delegate) {
		super(ctx, delegate);
	}

	
	@Override
	public Object store(final Context ctx, final Object obj) throws HomeException {
        Account account = (Account)obj;
        account = (Account) super.store(ctx, obj);
        
        return account;
	}
	
	@Override
	public Object create(final Context ctx, final Object obj) throws HomeException {
		Account acct = (Account)obj;
        acct = (Account)super.create(ctx, obj);

		AccountRelationship acctRelationship = new AccountRelationship();
		
		Home home = (Home) ctx.get(AccountRelationshipHome.class);

		Home crmSpidHome = (Home) ctx.get(com.redknee.app.crm.bean.CRMSpidHome.class);
		CRMSpid spid = (CRMSpid) crmSpidHome.find(acct.getSpid());

		acctRelationship.setSpid(acct.getSpid());
		acctRelationship.setBAN(acct.getBAN());
		acctRelationship.setRelationshipType(AccountRelationshipTypeEnum.DISCOUNT);
		acctRelationship.setCreatedDate(new Date());
		
		SessionInfo session = (SessionInfo) ctx.get(SessionInfo.class);        
        if (session != null)
        {
        	acctRelationship.setUserId(session.getPrincipal());
        }

		DiscountStrategyEnum discountStrategy = spid.getDiscountStrategy();

		// For DiscountStrategyEnum.ROOT, setting ResponsibleBAN in AccountRelationshipHome as ROOT's BAN
		if (spid.getDiscountStrategy().equals(DiscountStrategyEnum.ROOT)) {
			try {
				Account rootAccount = acct.getRootAccount(ctx);
				if (rootAccount != null) {
					acctRelationship.setTargetBAN(rootAccount.getBAN());
				}
			} catch (HomeException e) {
				new MinorLogMsg(MODULE, "Error retrieving root account for " + acct.getBAN(), e)
						.log(ctx);
			}

		}
		// For DiscountStrategyEnum.SELF, setting ResponsibleBAN in AccountRelationshipHome as SELF
		else 
		{
			acctRelationship.setTargetBAN(acct.getBAN());
		}

		if (home != null) {
			try {
				home.create(ctx, acctRelationship);
			} catch (HomeInternalException e) {
				LogSupport.info(ctx, MODULE,
						"createRecord : Unable to add entry to AccountRelationship for BAN: " + acct.getBAN());
			} catch (HomeException e) {
				LogSupport.info(ctx, MODULE,
						"createRecord : Got HomeException, Unable to insert to AccountRelationship for BAN: "
								+ acct.getBAN());
			}
		}

		return acct;
	}
	
}
