/*
 * This code is a protected work and subject to domestic and international
 * copyright law(s). A complete listing of authors of this work is readily
 * available. Additionally, source code is, by its very nature, confidential
 * information and inextricably contains trade secrets and other information
 * proprietary, valuable and sensitive to Redknee, no unauthorised use,
 * disclosure, manipulation or otherwise is permitted, and may only be used
 * in accordance with the terms of the licence agreement entered into with
 * Redknee Inc. and/or its subsidiaries.
 *
 * Copyright &copy; Redknee Inc. and its subsidiaries. All Rights Reserved.
 *
 */
package com.trilogy.app.crm.duplicatedetection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Adapter;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.visitor.FunctionVisitor;
import com.trilogy.framework.xhome.visitor.ListBuildingVisitor;
import com.trilogy.framework.xlog.log.LogSupport;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.SubscriberHome;
import com.trilogy.app.crm.bean.SubscriberXInfo;

/**
 * Adapts an account into a list of DuplicateAccountDetectionSubscriptionResult.
 * 
 * @author cindy.wong@redknee.com
 * @since 2010-09-09
 */
public class AccountDetectionSubscriptionResultListAdapter implements Adapter
{

	/**
	 * Serial version UID.
	 */
	private static final long serialVersionUID = 1L;

	private static final AccountDetectionSubscriptionResultListAdapter instance =
	    new AccountDetectionSubscriptionResultListAdapter();

	public static AccountDetectionSubscriptionResultListAdapter instance()
	{
		return instance;
	}

	@Override
	public Object adapt(Context ctx, Object obj) throws HomeException
	{
		List list = new ArrayList();
		Account account = (Account) obj;
		final Home home = (Home) ctx.get(SubscriberHome.class);
		final And and = new And();
		and.add(new EQ(SubscriberXInfo.BAN, account.getBAN()));

		FunctionVisitor visitor =
		    new FunctionVisitor(SubscriberDetectionResultAdapter.instance(),
		        new ListBuildingVisitor());
		try
		{
			visitor = (FunctionVisitor) home.forEach(ctx, visitor, and);
		}
		catch (final HomeException exception)
		{
			LogSupport.minor(ctx, this,
			    "Exception caught while building subscriber list", exception);
			throw new HomeException("Fail to add subscribers of account "
			    + account.getBAN() + " into result list", exception);
		}

		final ListBuildingVisitor subscribers =
		    (ListBuildingVisitor) visitor.getDelegate();
		if (subscribers != null)
		{
			list.addAll(subscribers);
		}

		Collections.sort(list,
		    DuplicateAccountDetectionSubscriptionResultComparator.instance());

		return list;
	}

	@Override
	public Object unAdapt(Context ctx, Object obj) throws HomeException
	{
		throw new UnsupportedOperationException();
	}

}
