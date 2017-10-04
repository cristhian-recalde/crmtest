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
package com.trilogy.app.crm.web.acctmenu;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.support.AccountSupport;
import com.trilogy.app.crm.support.Lookup;
import com.trilogy.app.crm.support.SubscriberSupport;
import com.trilogy.framework.xhome.beans.ExceptionListener;
import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.language.MessageMgr;
import com.trilogy.framework.xhome.session.Session;
import com.trilogy.framework.xhome.web.border.Border;
import com.trilogy.framework.xhome.webcontrol.HTMLExceptionListener;
import com.trilogy.framework.xhome.webcontrol.RequestServicer;
import com.trilogy.framework.xhome.webcontrol.WebController;
import com.trilogy.framework.xlog.log.LogSupport;


/**
 * Border to be used as WebController Detail Border to save selected Subscriber is
 * Session.
 *
 * @author kevin.greer@redknee.com
 */
public class CustomSubscriberToSessionBorder implements Border
{

	/**
	 * {@inheritDoc}
	 */
	public void service(final Context ctx, final HttpServletRequest req, final HttpServletResponse res,
			final RequestServicer delegate) throws ServletException, IOException
			{
		final Subscriber sub = getSubscriber(ctx);
		final Context session = Session.getSession(ctx);


		// Add 'increment key' to context here, for GUI updates
		session.put(Lookup.PRICEPLAN_SWITCH_COUNTER_INCREMENT, true);

		if (sub != null)
		{
			try
			{
				final Account account = AccountSupport.getAccount(ctx, sub.getBAN());
				if (account != null)
				{
					session.put(Account.class, account);
				}
			}
			catch (final HomeException exception)
			{
				final StringBuilder sb = new StringBuilder();
				sb.append(exception.getClass().getSimpleName());
				sb.append(" caught in ");
				sb.append("CustomSubscriberToSessionBorder.service(): ");
				if (exception.getMessage() != null)
				{
					sb.append(exception.getMessage());
				}
				LogSupport.minor(ctx, this, sb.toString(), exception);

			}
			session.put(Subscriber.class, sub);
			session.put(com.redknee.app.crm.bean.SubscriberXInfo.ID, sub.getId());
			delegate.service(ctx, req, res);
		}  else  {
			final HTMLExceptionListener listener = new HTMLExceptionListener(new MessageMgr(ctx, getClass()));
			listener.thrown(new IllegalPropertyArgumentException("", "Subscriber details not found for your search criteria. Transaction details cannot be retrieved."));
			ctx.put(HTMLExceptionListener.class, listener);
			ctx.put(ExceptionListener.class, listener);
			listener.toWeb(ctx, res.getWriter(), null, null);
		}
			}


	/**
	 * Retrieves the subscriber to put in the context.
	 *
	 * @param ctx
	 *            The operating context.
	 * @return Subscriber to be put in the context.
	 */
	public Subscriber getSubscriber(final Context ctx)
	{
		final Object subscriber = WebController.getBean(ctx);
		if (subscriber != null && subscriber instanceof Subscriber && ((Subscriber) subscriber).isSubscriberIdSet())
		{
			return (Subscriber) subscriber;
		}

		// attempt to find the subscriber from the session
		Subscriber sub = (Subscriber) Session.getSession(ctx).get(Subscriber.class);
		if (sub != null && sub.isSubscriberIdSet())
		{
			/*
			 * [Cindy] 2008-02-29 TT# 8020600010: forcefully update the subscriber in the
			 * context. Under some circumstances, the subscriber stored in session when a
			 * transaction is created may not be up-to-date. As a result,
			 * SubscriberUpdateUpsAgent mistakenly uses the stale subscriber to determine
			 * ABM balance needs to be updated.
			 */
			try
			{
				sub = SubscriberSupport.lookupSubscriberForSubId(ctx, sub.getId());
			}
			catch (final HomeException exception)
			{
				final StringBuilder sb = new StringBuilder();
				sb.append(exception.getClass().getSimpleName());
				sb.append(" caught in ");
				sb.append("CustomSubscriberToSessionBorder.getSubscriber(): ");
				if (exception.getMessage() != null)
				{
					sb.append(exception.getMessage());
				}
				LogSupport.minor(ctx, this, sb.toString(), exception);
				sub = null;
			}
		}
		return sub;
	}

}
