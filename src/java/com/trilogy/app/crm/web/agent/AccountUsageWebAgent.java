/*
 *  AccountUsageWebAgent.java
 *
 *  Author : kgreer
 *  Date   : Apr 01, 2005
 *
 * This code is a protected work and subject to domestic and international
 * copyright law(s). A complete listing of authors of this work is readily
 * available. Additionally, source code is, by its very nature, confidential
 * information and inextricably contains trade secrets and other information
 * proprietary, valuable and sensitive to Redknee, no unauthorised use,
 * disclosure, manipulation or otherwise is permitted, and may only be used
 * in accordance with the terms of the licence agreement entered into with
 * Redknee Inc. and/or its subsidiaries.
 *
 * Copyright � Redknee Inc. and its subsidiaries. All Rights Reserved.
 */

package com.trilogy.app.crm.web.agent;

import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.language.HomeMessageMgrSPI;
import com.trilogy.framework.xhome.web.action.EditHelpAction;
import com.trilogy.framework.xhome.web.action.WebActionBean;
import com.trilogy.framework.xhome.web.agent.WebAgent;
import com.trilogy.framework.xhome.web.agent.WebAgents;
import com.trilogy.framework.xhome.web.renderer.ButtonRenderer;
import com.trilogy.framework.xhome.web.renderer.DefaultButtonRenderer;
import com.trilogy.framework.xhome.web.util.Link;
import com.trilogy.framework.xhome.webcontrol.WebControl;
import com.trilogy.framework.xlog.log.LogSupport;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountUsage;
import com.trilogy.app.crm.bean.CreditCategory;
import com.trilogy.app.crm.bean.GroupTypeEnum;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.app.crm.web.control.CustomAccountUsageWebControl;

/**
 * WebAgent for displaying Account usage information.
 * 
 * @author kgreer
 **/
public class AccountUsageWebAgent extends WebAgents implements WebAgent
{

	protected static WebControl wc_ = new CustomAccountUsageWebControl();

	public AccountUsageWebAgent()
	{
	}

	@Override
	public void execute(Context ctx) throws AgentException
	{
		PrintWriter out = WebAgents.getWriter(ctx);

		Account account = (Account) ctx.get(Account.class);

		if (account == null)
		{
			out.println("<font color=red>No Account selected! </font>");

			return;
		}

		if (account.getSystemType().equals(SubscriberTypeEnum.PREPAID))
		{
			out.println("<font color=red>This function is not available for Prepaid Account! </font>");

			return;
		}

		AccountUsage usage = account.getAccountUsage(ctx);
		
		/*
		 * Note - Below condition added for TT#14021050007
		 */
		if(!account.isIndividual(ctx))
		{
			try{
				CreditCategory creditCategory = HomeSupportHelper.get(ctx).findBean(ctx, CreditCategory.class, account.getCreditCategory());
				if(creditCategory != null && !creditCategory.isOverPaymentDistributed())
				{
					usage.setOverPaymentDistributed(false);
				}
			}
			catch(HomeException e)
			{
				LogSupport.minor(ctx, this, "Exception in determining Credit Category for Account",e);
				throw new AgentException("Home Exception encountered while determining Credit Category For Account");
			}
		}

		// Add table so that it doesn't expand to 100%
		out.println("<table><tr><td>");
		wc_.toWeb(ctx, out, "", usage);

		HttpServletRequest req =
		    (HttpServletRequest) ctx.get(HttpServletRequest.class);
		HttpServletResponse res =
		    (HttpServletResponse) ctx.get(HttpServletResponse.class);

		if (req == null)
		{
			out.println("<p>Request is null</p>");
		}
		if (res == null)
		{
			out.println("<p>Response is null</p>");
		}

		outputHelpLink(ctx, out, req);

		out.println("</td></tr></table>");
	}

	/**
	 * @param ctx
	 * @param out
	 * @param req
	 */
	public static void outputHelpLink(Context ctx, PrintWriter out,
	    HttpServletRequest req)
	{
		String redirectURL = getHelpURL(ctx, req);
		ButtonRenderer br =
		    (ButtonRenderer) ctx.get(ButtonRenderer.class,
		        DefaultButtonRenderer.instance());
		Link link = new Link(ctx);

		link.remove("cmd");
		link.setURI(redirectURL);
		br.linkButton(out, ctx, "Help", "Help", link, "showHelpMenu('"
		    + redirectURL + "'); return false;");
		if (ctx.getBoolean(HomeMessageMgrSPI.ENABLE_XMESSAGE_CAPTURE))
		{
			link.remove("mode");
			WebActionBean action1 = new EditHelpAction();
			action1.writeLinkDetail(ctx, out, null, link);
		}
	}

	public static String getHelpURL(Context ctx, HttpServletRequest req)
	{
		Context subCtx = ctx.createSubContext();
		subCtx.put(Link.class, null);
		Link link = new Link(subCtx);
		link.copy(req, "cmd");

		link.addRaw("mode", "help");
		link.addRaw("border", "hide");
		link.addRaw("menu", "hide");

		link.remove("mode");
		link.addRaw("mode", "help");
		link.addRaw("border", "hide");
		link.addRaw("menu", "hide");

		return link.write();
	}
}
