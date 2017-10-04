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
package com.trilogy.app.crm.home.account;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.home.HomeProxy;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.BlackTypeEnum;
import com.trilogy.app.crm.bean.SystemNoteSubTypeEnum;
import com.trilogy.app.crm.bean.SystemNoteTypeEnum;
import com.trilogy.app.crm.blacklist.BlackListSupport;
import com.trilogy.app.crm.support.NoteSupportHelper;

/**
 * @author cindy.wong@redknee.com
 * @since 2011-04-05
 */
public class BlackListOverrideNoteCreationHome extends HomeProxy
{

	private static final long serialVersionUID = 1L;

	

	/**
	 * Constructor for BlackListOverrideNoteCreationHome.
	 * @param delegate
	 */
	public BlackListOverrideNoteCreationHome(Home delegate)
	{
		super(delegate);
	}

	@Override
	public Object create(Context ctx, Object obj) throws HomeException,
	    HomeInternalException
	{
		Object result = super.create(ctx, obj);
		BlackTypeEnum colour =
		    (BlackTypeEnum) ctx.get(BlackListSupport.BLACKLIST_OVERRIDE_COLOUR);
		if (colour != null)
		{
			Account account = (Account) result;
			StringBuilder sb = new StringBuilder();
			sb.append("Account was created despite one or more identification(s) was on the black list with colour ");
			sb.append(colour);
			sb.append(" -- operation authorized");
			NoteSupportHelper.get(ctx).addAccountNote(ctx, account.getBAN(),
			    sb.toString(), SystemNoteTypeEnum.EVENTS,
			    SystemNoteSubTypeEnum.ACCACTIVE);
		}
		return result;
	}

}
