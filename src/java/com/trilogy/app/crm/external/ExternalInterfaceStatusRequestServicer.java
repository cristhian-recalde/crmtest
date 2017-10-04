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

package com.trilogy.app.crm.external;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opensaml.xacml.ctx.ResponseType;


import com.trilogy.app.crm.aptilo.AptiloSubscriberWebControl;
import com.trilogy.app.crm.aptilo.AptiloSupport;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.blackberry.BlackberrySupport;
import com.trilogy.app.crm.external.ecp.EcpSubscriberHome;
import com.trilogy.app.crm.external.ecp.EcpSubscriberWebControl;
import com.trilogy.app.crm.external.smsb.SmsbSubscriberHome;
import com.trilogy.app.crm.external.smsb.SmsbSubscriberWebControl;
import com.trilogy.app.crm.external.ups.UpsSubscriberHome;
import com.trilogy.app.crm.external.ups.UpsSubscriberWebControl;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.webcontrol.RequestServicer;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.client.bm.BalanceManagementSupport;
import com.trilogy.app.crm.client.bm.Parameters;
import com.trilogy.app.crm.client.bm.SubscriberProfileProvisionClient;
import com.trilogy.app.crm.client.bm.SubscriberProfileProvisionException;
import com.trilogy.app.crm.external.ecp.EcpSubscriberHome;
import com.trilogy.app.crm.external.ecp.EcpSubscriberWebControl;
import com.trilogy.app.crm.external.smsb.SmsbSubscriberHome;
import com.trilogy.app.crm.external.smsb.SmsbSubscriberWebControl;
import com.trilogy.app.crm.external.ups.UpsSubscriber;
import com.trilogy.app.crm.external.ups.UpsSubscriberWebControl;
import com.trilogy.app.crm.support.EnumStateSupportHelper;
import com.trilogy.framework.xhome.msp.MSP;
import com.trilogy.app.crm.esbconnect.DefaultEsbMessage;


/**
 * Queries external interfaces and display the respective profiles on the same page.
 *
 * @author paul.sperneac@redknee.com
 */
public class ExternalInterfaceStatusRequestServicer implements RequestServicer
{

    /**
     * States which should be ignored.
     */
    public static final Collection<SubscriberStateEnum> IGNORED_STATES = 
        Collections.unmodifiableCollection(Arrays.asList(
                SubscriberStateEnum.INACTIVE));


    /**
     * {@inheritDoc}
     */
    public void service(final Context ctx, final HttpServletRequest req, final HttpServletResponse res)
        throws ServletException, IOException
    {
        final PrintWriter out = res.getWriter();
        if (!ctx.has(Subscriber.class))
        {
            out.println("Cannot find Subscriber in context");
            return;
        }

        out.println("<table border=\"0\" cellpadding=\"4\" cellspacing=\"4\">");
        out.println("<tr><td valign=\"top\">");

        final Subscriber sub = (Subscriber) ctx.get(Subscriber.class);
        final String msisdn = sub.getMSISDN();
        final String div = "</td><td valign=\"top\">";

        /*
         * [Cindy] 2008-02-29 TT8020600010: Don't query if the state should be ignored --
         * subscriber's MSISDN may have been reused by other subscribers already.
         */
        if (EnumStateSupportHelper.get(ctx).isOneOfStates(sub, IGNORED_STATES))
        {
            final String msg = "Subscriber " + sub.getId() + " has been deactivated";
            out.println(msg);
        }
        else
        {
            Context subCtx = ctx.createSubContext();
            
            MSP.setBeanSpid(subCtx, sub.getSpid());            

            displayEcpProfile(subCtx, out, msisdn);

            out.println(div);

            displaySmsbProfile(subCtx, out, msisdn);

            out.println(div);

            displayBMProfile(subCtx, out, sub);
        }
        
       try
        {
    	   
            if (AptiloSupport.isAptiloSupportEnabled(ctx,sub) && AptiloSupport.subscriberHasValidAptiloService(ctx, sub.getId()))
            {
                out.println("</td><td valign=\"top\">");
  
                Object aptiloSubscriber = AptiloSupport.getSubscriber(ctx, sub);
    
                if(aptiloSubscriber!=null)
                {
                    new AptiloSubscriberWebControl().toWeb(ctx,out,"AptiloSubscriber",aptiloSubscriber);
                }
                else
                {
                    out.println("Cannot find subscriber "+msisdn+" in Aptilo server");
                }
            }
        }
        catch (Exception e)
        {
            if(LogSupport.isDebugEnabled(ctx))
            {
                new DebugLogMsg(this,e.getMessage(),e).log(ctx);
            }
        }
        out.println("</td></tr>");
        out.println("</table>");
    }


    /**
     * Display UPS profile.
     *
     * @param ctx
     *            The operating context.
     * @param out
     *            Output printer.
     * @param subscription
     *            Subscription.
     */
    private void displayBMProfile(final Context ctx, final PrintWriter out, final Subscriber subscription)
    {
        try
        {
            final SubscriberProfileProvisionClient client = BalanceManagementSupport.getSubscriberProfileProvisionClient(ctx);
            final Account subscriberAccount = subscription.getAccount(ctx);
            final Parameters subscriberAccountProfile = client.querySubscriberAccountProfile(ctx, subscriberAccount );
            final Parameters subscriptionProfile = client.querySubscriptionProfile(ctx, subscription);

            if (subscriberAccountProfile != null && subscriptionProfile != null)
            {
                UpsSubscriber sub=new UpsSubscriber();

                // TODO -- From where does activation extension come?
                //sub.setActivationExtension(subscriptionProfile.getActivationExtension());
                sub.setBalance(subscriptionProfile.getBalance());
                sub.setBillCycleDay(subscriberAccountProfile.getBillCycleDay());
                sub.setCreditLimit(subscriptionProfile.getCreditLimit());
                sub.setCurrency(subscriptionProfile.getCurrency());
                sub.setExpiryDate(subscriptionProfile.getExpiryDate());
                sub.setGroupQuota(subscriptionProfile.getGroupQuota());
                sub.setGroupUsage(subscriptionProfile.getGroupUsage());
                sub.setMsisdn(subscriptionProfile.getMsisdn());
                sub.setOverdraftBalanceLimit(subscriptionProfile.getOverdraftBalanceLimit());
                sub.setPoolGroupID(subscriptionProfile.getPoolGroupID());
                sub.setPoolGroupOwner(subscriptionProfile.getPoolGroupOwner());
                sub.setPricePlan(subscriptionProfile.getPricePlan());
                sub.setSpid(subscriberAccountProfile.getSpid());
                sub.setState(subscriptionProfile.getState());
                sub.setSubscriberID(subscriptionProfile.getSubscriberID());
                sub.setSubscriptionLevel(subscriptionProfile.getSubscriptionLevel());
                sub.setSubscriptionType(subscriptionProfile.getSubscriptionType());
                sub.setActivationDate(subscriptionProfile.getActivationDate());
                sub.setPpsmSupporter(subscriptionProfile.getPpsmSupporter());
                sub.setScreeningTemplate(subscriptionProfile.getPpsmScreeningTemplate());
                // TODO -- Need some way to get activation date.

                new UpsSubscriberWebControl().toWeb(ctx, out, "BmSubscriber", sub);
            }
            else
            {
                out.println("Cannot find subscriber " + subscription.getMSISDN() + " in BM");
            }
        }
        catch (final HomeException e)
        {
            out.println("Error during find subscriber " + subscription.getMSISDN() + " " + e.getMessage());
            if (LogSupport.isDebugEnabled(ctx))
            {
                new DebugLogMsg(this, e.getMessage(), e).log(ctx);
            }
        }
        catch (SubscriberProfileProvisionException e)
        {
            out.println("Error during find subscriber " + subscription.getMSISDN() + " in BM" + e.getMessage());
            if (LogSupport.isDebugEnabled(ctx))
            {
                new DebugLogMsg(this, e.getMessage(), e).log(ctx);
            }
        }
    }


    /**
     * Display SMSB profile.
     *
     * @param ctx
     *            The operating context.
     * @param out
     *            Output printer.
     * @param msisdn
     *            Subscriber MSISDN.
     */
    private void displaySmsbProfile(final Context ctx, final PrintWriter out, final String msisdn)
    {
        try
        {
            final Home smsbHome = (Home) ctx.get(SmsbSubscriberHome.class);
            final Object smsbBean = smsbHome.find(ctx, msisdn);

            if (smsbBean != null)
            {
                new SmsbSubscriberWebControl().toWeb(ctx, out, "SmsbSubscriber", smsbBean);
            }
            else
            {
                out.println("Cannot find subscriber " + msisdn + " in smsb");
            }

        }
        catch (final HomeException e)
        {
            out.println("Error during find subscriber " + msisdn + " " + e.getMessage());
            if (LogSupport.isDebugEnabled(ctx))
            {
                new DebugLogMsg(this, e.getMessage(), e).log(ctx);
            }
        }

    }

        
        

    /**
     * Display ECP profile.
     *
     * @param ctx
     *            The operating context.
     * @param out
     *            Output printer.
     * @param msisdn
     *            Subscriber MSISDN.
     */
    private void displayEcpProfile(final Context ctx, final PrintWriter out, final String msisdn)
    {
        try
        {
            final Home ecpHome = (Home) ctx.get(EcpSubscriberHome.class);
            final Object ecpBean = ecpHome.find(ctx, msisdn);

            if (ecpBean != null)
            {
                new EcpSubscriberWebControl().toWeb(ctx, out, "EcpSubscriber", ecpBean);
            }
            else
            {
                out.println("Cannot find subscriber " + msisdn + " in ecp");
            }
        }
        catch (final HomeException e)
        {
            out.println("Error during find subscriber " + msisdn + " " + e.getMessage());
            if (LogSupport.isDebugEnabled(ctx))
            {
                new DebugLogMsg(this, e.getMessage(), e).log(ctx);
            }
        }
    }	

               

	

}
