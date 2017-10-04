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
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.TransactionHome;
import com.trilogy.app.crm.bean.TransactionXInfo;
import com.trilogy.app.crm.support.AccountSupport;
import com.trilogy.framework.xhome.beans.ExceptionListener;
import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.In;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.language.MessageMgr;
import com.trilogy.framework.xhome.session.Session;
import com.trilogy.framework.xhome.web.border.Border;
import com.trilogy.framework.xhome.webcontrol.HTMLExceptionListener;
import com.trilogy.framework.xhome.webcontrol.RequestServicer;
import com.trilogy.framework.xlog.log.MinorLogMsg;


/**
 * A Border for the Transactions screen that presets the the BAN selection.
 *
 * @author simar.singh@redknee.com
 */
public class CustomAccountTransactionsSelectBorder implements Border
{

	public void service(Context ctx, final HttpServletRequest req, final HttpServletResponse res,
			final RequestServicer delegate) throws ServletException, IOException
			{
		ctx = ctx.createSubContext();
		Context session = Session.getSession(ctx);
		Account acct = (Account) session.get(Account.class);
		if (ctx.has(Account.class))
		{
			acct = (Account) ctx.get(Account.class);
		}
		else
		{
			ctx.put(Account.class, acct);
		}
		Home home = (Home) ctx.get(TransactionHome.class);
		if (acct != null)
		{
			if (acct.isResponsible())
			{
				/*
				 * if the account is responsible it shares ownership (ex payments) of
				 * transactions made by it non-responsible children
				 */
				final HashSet<String> childNonRespAcctIds = new HashSet<String>();
				try
				{
					final Collection<Account> childAccounts = AccountSupport.getNonResponsibleAccounts(ctx, acct);
					for (final Account childAcct : childAccounts)
					{
						final Account nonResponsibleAccount = childAcct;
						childNonRespAcctIds.add(nonResponsibleAccount.getBAN());
					}
					res.getWriter().write(
							"<br/><center> Responsible Account [BAN: " + acct.getBAN()
							+ "] is responsible for Accounts [BAN(s): (" + getBANSetString(childNonRespAcctIds)
							+ ")].</center><br/>");
					home = home.where(ctx, new In(TransactionXInfo.BAN, childNonRespAcctIds));
				}
				catch (HomeException e)
				{
					new MinorLogMsg(this, "Error fetching child accounts for Responsible Account BAN [" + acct.getBAN()
							+ "]", e).log(ctx);
					res.getWriter().write(
							"<font color=\"red\"><br/><center>Error fetching child accounts for Responsible Account [BAN: "
									+ acct.getBAN() + "]. (" + e.getMessage() + ")</center><br/>");
				}
			}
			else
			{
				/*
				 * Commented out to handle dunning issue for group account.
				 * Issue was: If a grp acct has multiple child NR accounts and anyone of them is credited then with x amount,
				 * then that credit should happen against grp accnt and all the child accnts should be able to use that credit,
				 * not the only one that was credited.
				 */
				//home = home.where(ctx, new EQ(TransactionXInfo.BAN, acct.getBAN()));
			}
			ctx.put(TransactionHome.class, home);
			delegate.service(ctx, req, res);
		} else {
			final HTMLExceptionListener listener = new HTMLExceptionListener(new MessageMgr(ctx, getClass()));
			listener.thrown(new IllegalPropertyArgumentException("", "Account details not found for your search criteria. Transaction details cannot be retrieved."));
			ctx.put(HTMLExceptionListener.class, listener);
			ctx.put(ExceptionListener.class, listener);
			listener.toWeb(ctx, res.getWriter(), null, null);
		}
			}


	/**
	 * 
	 * @param banSet
	 *            - Set of Account IDs or say any strings
	 * @return A represntation of the set in strings [<csv-fromat]
	 */
	private String getBANSetString(final Set<String> banSet)
	{
		final StringBuilder bansBuilder = new StringBuilder();
		for (final Iterator<String> banSIterator = banSet.iterator(); banSIterator.hasNext();)
		{
			bansBuilder.append(banSIterator.next());
			if (banSIterator.hasNext())
			{
				bansBuilder.append(", ");
			}
			else
			{
				break;
			}
		}
		return bansBuilder.toString();
	}
}