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
package com.trilogy.app.crm.web.border;

import java.io.IOException;
import java.security.Principal;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberHome;
import com.trilogy.app.crm.bean.Transaction;
import com.trilogy.app.crm.bean.TransactionHome;
import com.trilogy.framework.xhome.auth.bean.User;
import com.trilogy.framework.xhome.beans.Validator;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.ValidatingHome;
import com.trilogy.framework.xhome.web.border.Border;
import com.trilogy.framework.xhome.webcontrol.RequestServicer;
/**
 * @author atse
 *
 * Decorates the Validation at GUI side 
 */
public class SetTransactionUserBorder implements Border, Validator
{	
	/* (non-Javadoc)
	 * @see com.redknee.framework.xhome.web.border.Border#service(com.redknee.framework.xhome.context.Context, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, com.redknee.framework.xhome.webcontrol.RequestServicer)
	 */
	public void service(Context ctx, HttpServletRequest req,
			HttpServletResponse res, RequestServicer delegate)
			throws ServletException, IOException 
	{
		Context subCtx = ctx.createSubContext();
		subCtx.put(TransactionHome.class, new ValidatingHome(this, (Home) subCtx.get(TransactionHome.class)));
		delegate.service(subCtx,req,res);
	}
	
	public void validate(Context ctx, Object obj)
			throws IllegalStateException
	{
	    Transaction tran = (Transaction)obj;
		final User principal = (User)ctx.get(Principal.class, new User());
		if (tran ==null)
		{
			throw new IllegalStateException ("Unable to find Transaction through the border");
		}
		tran.setAgent(principal.getId());
	}	
}