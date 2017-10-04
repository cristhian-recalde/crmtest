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

import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.visitor.AbortVisitException;
import com.trilogy.framework.xhome.visitor.HashMapBuildingVisitor;
import com.trilogy.framework.xlog.log.LogSupport;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.account.Contact;
import com.trilogy.app.crm.bean.duplicatedetection.DuplicateAccountDetectionResult;
import com.trilogy.app.crm.support.AccountSupport;

/**
 * Visitor to build the duplicate account detection results based on ID matches.
 * This visitor is designed for Contact home. The map built has
 * entries in the form of (BAN, DuplicateAccountDetectionResult).
 * 
 * @author cindy.wong@redknee.com
 * @since 2010-09-09
 */
public class ContactResultMapBuildingVisitor extends HashMapBuildingVisitor
{

	/**
	 * Serial version UID.
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public void visit(Context ctx, Object obj) throws AgentException,
	    AbortVisitException
	{
		Contact contact = (Contact) obj;
		DuplicateAccountDetectionResult row = null;
		Account account = null;
		try
		{
			account = AccountSupport.getAccount(ctx, contact.getAccount());
		}
		catch (HomeException exception)
		{
			LogSupport.minor(ctx, this, "Exception caught", exception);
			return;
		}

		if (account == null)
		{
			LogSupport.minor(ctx, this, "Account " + contact.getAccount()
			    + " does not exist");
			return;
		}

		try
		{
			row =
			    (DuplicateAccountDetectionResult) AccountDetectionResultAdapter
			        .instance().adapt(ctx, account);
		}
		catch (HomeException exception)
		{
			LogSupport.minor(ctx, this, "Exception caught", exception);
			return;
		}

		if (row == null)
		{
			LogSupport
			    .minor(ctx, this,
			        "Fail to adapt the ID serach result into a duplicate detection result row");
			return;
		}

		put(contact.getAccount(), row);
	}

}
