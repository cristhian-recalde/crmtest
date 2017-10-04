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

import com.trilogy.framework.xhome.auth.bean.User;
import com.trilogy.framework.xhome.beans.XBeans;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.web.border.Border;
import com.trilogy.framework.xhome.webcontrol.RequestServicer;
import com.trilogy.framework.xlog.log.LogSupport;

import com.trilogy.app.crm.CoreCrmConstants;
import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.TestInvoice;
import com.trilogy.app.crm.bean.TestInvoiceHome;
import com.trilogy.app.crm.bean.TestInvoiceID;
import com.trilogy.app.crm.bean.TestInvoiceIdentitySupport;
import com.trilogy.app.crm.bean.SystemNoteSubTypeEnum;
import com.trilogy.app.crm.bean.SystemNoteTypeEnum;
import com.trilogy.app.crm.support.AccountSupport;
import com.trilogy.app.crm.support.NoteSupport;
import com.trilogy.app.crm.support.NoteSupportHelper;

/**
 * @author amedina
 *
 * Writes an Account note every time there is a noteMessage attribute
 */
public class TestInvoiceAccountNoteWebBorder implements Border
{


	/* (non-Javadoc)
	 * @see com.redknee.framework.xhome.web.border.Border#service(com.redknee.framework.xhome.context.Context, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, com.redknee.framework.xhome.webcontrol.RequestServicer)
	 */
	public void service(Context ctx, HttpServletRequest req,
			HttpServletResponse res, RequestServicer delegate)
			throws ServletException, IOException 
	{
		String message = req.getParameter(MESSAGE);
		
        if ((message != null))
        {
        	String primaryKey = req.getParameter("key");
        	
			try 
			{
				TestInvoiceID id = (TestInvoiceID)TestInvoiceIdentitySupport.instance().fromStringID(primaryKey);
				Home invoiceHome = (Home) ctx.get(TestInvoiceHome.class);
				if (invoiceHome != null)
				{
					User user = (User) ctx.get(Principal.class);
					String principal = SYSTEM;
					
					if (user != null)
					{
						principal = user.getId();
					}
					
					TestInvoice invoice = (TestInvoice) invoiceHome.find(ctx, id);
					if (invoice != null)
					{
		        		addNote(ctx, invoice, principal, message);
					}
				}
			}
			catch (HomeException e)
			{
				LogSupport.crit(ctx,this,"HomeException: " + e.getMessage(), e);
			}

        }
        delegate.service(ctx, req, res);
	}

	public void addNote(Context ctx, TestInvoice inv, String principal, String message) 
	{
    	Account acct;
		try 
		{
			acct = AccountSupport.getAccount(ctx, inv.getBAN());
	    	
	    	NoteSupportHelper.get(ctx).addAccountNote(ctx, acct.getBAN(), principal + message + inv.getInvoiceId() + " - " + inv.getInvoiceDate(), SystemNoteTypeEnum.EVENTS, SystemNoteSubTypeEnum.INVCTL);
		}
		catch (HomeException e) 
		{
			LogSupport.major(ctx, this, "Error when trying to add a note for invoice " + XBeans.getIdentifier(inv) + " : " + e.getMessage(), e);
		}
	}

 
	public static final String MESSAGE = "noteMessage";
	private static final String SYSTEM = "system";

}
