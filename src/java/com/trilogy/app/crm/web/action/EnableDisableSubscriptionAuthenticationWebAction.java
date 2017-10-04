/*
 * Author : Kumaran Sivasubramaniam
 * Date   : June 12, 2009
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

package com.trilogy.app.crm.web.action;


import java.io.PrintWriter;

import com.trilogy.framework.xhome.beans.XBeans;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgents;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.support.IdentitySupport;
import com.trilogy.framework.xhome.web.action.SimpleWebAction;
import com.trilogy.framework.xhome.web.agent.WebAgents;
import com.trilogy.framework.xlog.log.InfoLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MinorLogMsg;

import com.trilogy.app.crm.bean.AcquiredMsisdnPINManagement;
import com.trilogy.app.crm.bean.account.SubscriptionTypeEnum;
import com.trilogy.app.crm.bean.core.SubscriptionType;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.app.crm.support.PinManagerSupport;
import com.trilogy.app.pin.manager.ErrorCode;


public class EnableDisableSubscriptionAuthenticationWebAction extends SimpleWebAction
{

    public EnableDisableSubscriptionAuthenticationWebAction()
    {
        super();
    }

    public EnableDisableSubscriptionAuthenticationWebAction(String key, String label)
    {
        super(key, label);
    }

    @Override
    public void execute(Context ctx) throws AgentException
    {
        LogSupport.debug(ctx, this, "Change Subscription Authentication status");
        String key = WebAgents.getParameter(ctx, "key");
        PrintWriter out = WebAgents.getWriter(ctx);
        String message = "";
        String errorMsg = "";
        short result = -1;
        
        Context subCtx = ctx.createSubContext();
        if (key != null)
        {
            try
            {
                IdentitySupport id = (IdentitySupport) XBeans.getInstanceOf(ctx, AcquiredMsisdnPINManagement.class, IdentitySupport.class);
                AcquiredMsisdnPINManagement bean = HomeSupportHelper.get(subCtx).findBean(subCtx, AcquiredMsisdnPINManagement.class, id.fromStringID(key));
                if (bean != null)
                {
                    try
                    {
                        SubscriptionType subscriptionType = SubscriptionType.getSubscriptionType(ctx, SubscriptionTypeEnum.WIRE_LINE);
                        if (subscriptionType != null)
                        {
                            result = PinManagerSupport.setAuthenticatedFlag(ctx, key, Long.valueOf(subscriptionType.getId()).intValue(),
                                    !bean.getAuthenticated(), "");
                            if (result != 0)
                            {
                                new InfoLogMsg(this,
                                        " Unsuccessful attempt at changing the authentication status for msisdn ["
                                                + bean.getMsisdn() + " ]", null).log(ctx);
                            }
                        }
                    }
                    catch (HomeException homeEx)
                    {
                        new MinorLogMsg(this, " Unable update the authentication status for msisdn ["
                                + bean.getMsisdn() + " ]", homeEx).log(ctx);
                    }
                }
                else
                {
                    out.println("<font color=red>Unable to change the subscription authentication key=" + key
                            + ".</font><br/><br/>");
                }
            }
            catch (HomeException e)
            {
                throw new AgentException(e);
            }
        }
        if (result == ErrorCode.SUCCESS)
        {
            message = "Subscription Authentication change was successful for MSISDN [" + key + "]. ";
            message = "<table width=\"70%\"><tr><td><center><b style=\"color:green;\">" + message + "</b></center></td></tr></table>";
        }
        else
        {
            message = "Problem occured in trying change subscription authentication for MSISDN [" + key + "]. ";
            message = "<table width=\"70%\"><tr><td><center><b style=\"color:red;\">" + message + "</b></center></td></tr></table>";
            errorMsg = PinManagerSupport.pinManagerResultToMessageMapping(ctx, result);
        }
        out.println(message);
        if (!"".equals(errorMsg))
        {
            out.println("<table width=\"70%\"><tr><td><center><b style=\"color:red;\">" + errorMsg + "</b></center></td></tr></table>");
        }
        out.println("<table width=\"70%\"><tr><td><center>");
        ContextAgents.doReturn(ctx);
        out.println("</center></td></tr></table>");
    }
}
