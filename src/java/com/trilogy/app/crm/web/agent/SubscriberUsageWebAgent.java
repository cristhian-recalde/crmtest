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

package com.trilogy.app.crm.web.agent;

import java.io.PrintWriter;

import com.trilogy.framework.xhome.beans.SafetyUtil;
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
import com.trilogy.framework.xlog.log.MinorLogMsg;
import com.trilogy.framework.xlog.log.PMLogMsg;

import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.bean.usage.BalanceUsageWebControl;
import com.trilogy.app.crm.blackberry.BlackberrySupport;
import com.trilogy.app.crm.bundle.web.BundleUsageWebAgent;
import com.trilogy.app.crm.support.FrameworkSupportHelper;
import com.trilogy.app.crm.support.SpidSupport;
import com.trilogy.app.crm.support.SubscriberSupport;
import com.trilogy.app.crm.web.border.ResetSubscriptionUsageBorder;
import com.trilogy.app.crm.web.control.CurrencyContextSetupWebControl;


/**
 * WebAgent for displaying Subscriber usage information.
 *
 * @author kevin.greer@redknee.com
 */
public class SubscriberUsageWebAgent extends WebAgents implements WebAgent
{

    /**
     * PM log module name.
     */
    public static String PM_MODULE = "SubscriberUsageWebAgent";


    /**
     * {@inheritDoc}
     */
    @Override
    public void execute(final Context ctx) throws AgentException
    {
        final PrintWriter out = WebAgents.getWriter(ctx);
        Subscriber sub = (Subscriber) ctx.get(Subscriber.class);

        if (sub == null)
        {
            out.println("<font color=red>No Subscriber selected!</font>");

            return;
        }

        final PMLogMsg pmLogMsg = new PMLogMsg(PM_MODULE, "execute", sub.getId());

        try
        {
            sub = SubscriberSupport.getSubscriber(ctx, sub.getId());
        }
        catch (final HomeException exception)
        {
            final StringBuilder sb = new StringBuilder();
            sb.append(exception.getClass().getSimpleName());
            sb.append(" caught in ");
            sb.append("SubscriberUsageWebAgent.execute(): ");
            if (exception.getMessage() != null)
            {
                sb.append(exception.getMessage());
            }
            LogSupport.minor(ctx, this, sb.toString(), exception);
        }

        // Set it to the current context so that it will not have any problems
        // with using old cached contexts which may have invalidated objects
        sub.setContext(ctx);
                
        try 
		{
			final CRMSpid crmSpid = SpidSupport.getCRMSpid(ctx, sub.getSpid());
			if (crmSpid != null) 
			{
				ctx.put("useSIunit", !crmSpid.isUseIECunits());
			}
		} 
		catch (Throwable e) 
		{
			new MinorLogMsg(this, "CRMSpid home error", e).log(ctx);
		}
        
        // Add table so that it doesn't expand to 100%
        out.println("<table halgin=\"left\"><tr><td>");

        final PMLogMsg pmLogMsg2 = new PMLogMsg(PM_MODULE, "BundleAgent", sub.getId());
        bundleAgent_.execute(ctx);
        pmLogMsg2.log(ctx);

        out.println("<br/>");

        out.println("<table width=\"300\" ><tr><td>");
        final PMLogMsg pmLogMsg3 = new PMLogMsg(PM_MODULE, "BalanceUsage", sub.getId());
 		if (SafetyUtil.safeEquals(sub.getState(), SubscriberStateEnum.INACTIVE))
        {
            out.println("Can not show balance as subscriber has been deactivated.");
        }
        else
        {
            balanceUsageView_wc_.toWeb(ctx, out, "", sub.getBalanceUsage(ctx));
		    outputHelpLink(ctx, out);
		    pmLogMsg3.log(ctx);
        }        
       
        
        if (BlackberrySupport.isBlackberryEnabled(ctx))
        {
            out.println("<br/>");
            final PMLogMsg pmLogMsg4 = new PMLogMsg(PM_MODULE, "BlackberryStatus", sub.getId());
            blackberryStatusAgent_.execute(ctx);
            pmLogMsg4.log(ctx);
        }

        out.println("</tr></td></table>");
        
        boolean isPooled = sub.isPooled(ctx);
        
        out.println("</tr></td>");

        if (isPooled)
        {
            if (FrameworkSupportHelper.get(ctx).hasPermission(ctx, ResetSubscriptionUsageBorder.RESET_Group_Usage_Permission))
            {
                final Link link = new Link(ctx);
                out.print("<tr><td><form action=\"");
                link.write(out);
                out.println("\" method=\"POST\">");
                
                out.println("<input type=\"submit\" name=\"cc_action\" value=\"" + ResetSubscriptionUsageBorder.RESET_Group_Usage + "\" \\>");
                out.println("</form></td></tr>");
            }

        }
        else if (sub.isPostpaid())
        {
            if (FrameworkSupportHelper.get(ctx).hasPermission(ctx, ResetSubscriptionUsageBorder.RESET_Monthly_Spend_Limit_Permission))
            {
                final Link link = new Link(ctx);
                out.print("<tr><td><form action=\"");
                link.write(out);
                out.println("\" method=\"POST\">");
                out.println("<input type=\"submit\" name=\"cc_action\" value=\"" + ResetSubscriptionUsageBorder.RESET_Monthly_Spend_Limit
                                + "\" \\>");
                out.println("</form></td></tr>");
            }
        }
        
        out.println("</table>");

        out.println("</td></tr></table>");

        pmLogMsg.log(ctx);
    }

	public static void outputHelpLink(Context ctx, PrintWriter out)
	{
		String redirectURL = getHelpURL(ctx);
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

	public static String getHelpURL(Context ctx)
	{
		Context subCtx = ctx.createSubContext();
		subCtx.put(Link.class, null);
		Link link = new Link(subCtx);
		link.addRaw("cmd", "SubMenuSubUsageHelp");

		link.addRaw("mode", "help");
		link.addRaw("border", "hide");
		link.addRaw("menu", "hide");

		link.remove("mode");
		link.addRaw("mode", "help");
		link.addRaw("border", "hide");
		link.addRaw("menu", "hide");

		return link.write();
	}
    /**
     * Balance usage web control.
     */
    protected static WebControl balanceUsageView_wc_ = new CurrencyContextSetupWebControl(new BalanceUsageWebControl());

    /**
     * Bundle usage web agent.
     */
    protected static WebAgent bundleAgent_ = new BundleUsageWebAgent();
    
    protected WebAgent  blackberryStatusAgent_ = new BlackberryStatusWebAgent();


}
