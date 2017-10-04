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
import java.util.Set;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountsDiscount;
import com.trilogy.app.crm.bean.AccountsDiscountXInfo;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.home.HomeProxy;

/**
 * This home is responsible to create list of service level discount classes if specified in GUI or in API [discountClassInfo Generic para]
 * 
 * @author shailesh.makhijani
 * @since 9.7.2
 *
 */

public class AccountDiscountUpdateHome extends HomeProxy {

	/**
	 * default serial version id
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * -1 indicates there is no discount applicable.
	 * -2 indicates service level discount and account to discount class mapping entry will be
	 *  created in AccountsDiscount table.
	 */
	private static final int SERVICE_LEVEL_DISCOUNT = -2;



	public AccountDiscountUpdateHome(Context ctx, Home delegate) {
		super(ctx, delegate);
	}
	
	
	
	@Override
	 public Object create(Context ctx, Object obj)
			 throws HomeException, HomeInternalException {
		
		Account account = (Account)obj;
		Set <Integer>set = account.getDiscountsClassHolder();

		if (set.size()!= 0) {
			
			account.setDiscountClass(SERVICE_LEVEL_DISCOUNT);
			super.create(ctx, account);
			for (Integer discount : set) {
				AccountsDiscount accountsDiscount = new AccountsDiscount();
				accountsDiscount.setBAN(account.getBAN());
				accountsDiscount.setDiscountClass(discount);
				accountsDiscount.setSpid(account.getSpid());

				HomeSupportHelper.get(ctx).createBean(ctx, accountsDiscount);
			}
		}
		else
		{
			super.create(ctx, account);
		}
		return obj;
	}
	


	@Override
	public Object store(Context ctx, Object obj)
			throws HomeException, HomeInternalException {

		Account account = (Account)obj;

		Set <Integer>newSet = account.getDiscountsClassHolder();
		

		And and = new And ();
		and.add(new EQ(AccountsDiscountXInfo.BAN, account.getBAN()));
		and.add(new EQ(AccountsDiscountXInfo.SPID, account.getSpid()));
		
		Collection<AccountsDiscount> coll = HomeSupportHelper.get(ctx).getBeans(ctx, AccountsDiscount.class, and);
		
		if (newSet.size() != 0 || coll.size() != 0) {
			
			/**
			 * remove all previously stored bean for this ban and spid
			 */
			for (AccountsDiscount acc : coll){
				HomeSupportHelper.get(ctx).removeBean(ctx, acc);
			}
			
			/**
			 *  Create new entries
			 */
			for (Integer discount : newSet) {
				AccountsDiscount accountsDiscount = new AccountsDiscount();
				accountsDiscount.setBAN(account.getBAN());
				accountsDiscount.setDiscountClass(discount);
				accountsDiscount.setSpid(account.getSpid());

				HomeSupportHelper.get(ctx).createBean(ctx, accountsDiscount);
			}
			if (newSet.size() != 0){
				account.setDiscountClass(SERVICE_LEVEL_DISCOUNT);
			}
		}
		return getDelegate(ctx).store(ctx, obj);
	}
	
}
