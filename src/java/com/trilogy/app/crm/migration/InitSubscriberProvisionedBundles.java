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
package com.trilogy.app.crm.migration;

import java.util.Collection;
import java.util.HashSet;

import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.In;
import com.trilogy.framework.xhome.elang.Not;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.visitor.ParallelVisitor;
import com.trilogy.framework.xhome.visitor.Visitor;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.InfoLogMsg;
import com.trilogy.framework.xlog.log.MinorLogMsg;

import com.trilogy.app.crm.bas.directDebit.EnhancedParallVisitor;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberHome;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.bean.SubscriberXDBHome;
import com.trilogy.app.crm.bean.SubscriberXInfo;
import com.trilogy.app.crm.bundle.SubscriberBundleSupport;
import com.trilogy.app.crm.support.ServiceSupport;
import com.trilogy.framework.xlog.log.LogSupport;
/**
 * This class is for TT6031531983, to populate new field provisionedBundles in subscriber table
 * neeed to run via bean shell when deployed.
 *
 * @author jchen
 */

public class InitSubscriberProvisionedBundles
{
    public void initProvisionedBundles(final Context context, final int[] pricePlanWithBundles) throws HomeException
    {
        final Home home = (Home) context.get(SubscriberHome.class);

        //filtering subscriber state
        final HashSet setBundleProvisioned = new HashSet();

        setBundleProvisioned.add(SubscriberStateEnum.PENDING);
        setBundleProvisioned.add(SubscriberStateEnum.INACTIVE);
        setBundleProvisioned.add(SubscriberStateEnum.MOVED);
        setBundleProvisioned.add(SubscriberStateEnum.AVAILABLE);

        Home subHome = home.where(context, new Not(new In(SubscriberXInfo.STATE, setBundleProvisioned)));

        //filtering price plan with bundles

        if (pricePlanWithBundles != null && pricePlanWithBundles.length > 0)
        {
            final HashSet pricePlans = new HashSet();
            for (int i = 0; i < pricePlanWithBundles.length; i++)
            {
                pricePlans.add(Long.valueOf(pricePlanWithBundles[i]));
            }
            subHome = subHome.where(context, new In(SubscriberXInfo.PRICE_PLAN, pricePlans));
        }
        updateSelectedHome(context, subHome);
        myPrint(context, "Total subscribers updated=" + (incrementSuccessCnt(context) - 1));
        myPrint(context, "Total subscribers failed=" + (incrementFailedCnt(context) - 1));
    }

    void myPrint(final Context ctx, final String msg)
    {
        System.out.println(msg);
        new InfoLogMsg(this, msg, null).log(ctx);
    }

    String PROVISIONED_BUNDLE_SUCCESS_CNT = "provisionedSubscriberSuccessCnt";

    int incrementSuccessCnt(final Context context)
    {
        int cnt = 0;
        synchronized (PROVISIONED_BUNDLE_SUCCESS_CNT)
        {
            cnt = context.getInt(PROVISIONED_BUNDLE_SUCCESS_CNT, 0);
            cnt++;
            context.put(PROVISIONED_BUNDLE_SUCCESS_CNT, Integer.valueOf(cnt));
        }
        return cnt;
    }

    String PROVISIONED_BUNDLE_FAILED_CNT = "provisionedSubscriberFailedCnt";

    int incrementFailedCnt(final Context context)
    {
        int cnt = 0;
        synchronized (PROVISIONED_BUNDLE_FAILED_CNT)
        {
            cnt = context.getInt(PROVISIONED_BUNDLE_FAILED_CNT, 0);
            cnt++;
            context.put(PROVISIONED_BUNDLE_FAILED_CNT, Integer.valueOf(cnt));
        }
        return cnt;
    }

    void updateSelectedHome(final Context context, final Home home) throws HomeException
    {
        final Home xdbHome = new SubscriberXDBHome(context, "SUBSCRIBER");
        new DebugLogMsg(this, "Init Subscriber Provisioned Bundles Starting...", null).log(context);
        EnhancedParallVisitor pv = null;
        try{
        pv =  new EnhancedParallVisitor(30, new Visitor()
        {
            public void visit(final Context context, final Object obj) throws AgentException
            {
                final Context subCtx = context.createSubContext();
                Subscriber sub = (Subscriber) obj;
                final Collection existingProvsioned = sub.getProvisionedBundles();
                if (existingProvsioned.size() == 0)
                {
                    Collection newProvisioned = SubscriberBundleSupport.getSubscribedBundles(subCtx, sub).keySet();
                    if (newProvisioned.size() != 0)
                    {
                        newProvisioned = ServiceSupport.transformServiceObjectToIds(newProvisioned);

                        //Home home = (Home)subCtx.get(SubscriberHome.class);
                        try
                        {
                            sub = (Subscriber) xdbHome.find(subCtx, sub.getId());
                            sub.getProvisionedBundles().clear();
                            sub.getProvisionedBundles().addAll(newProvisioned);
                            xdbHome.store(subCtx, sub);

                            incrementSuccessCnt(context);
                            new DebugLogMsg(this, "sub provisionedBundles init, sub=" + sub.getId()
                                    + ",provisionedBundles=" + ServiceSupport.getServiceIdString(
                                    newProvisioned), null).log(subCtx);
                        }
                        catch (Exception he)
                        {
                            incrementFailedCnt(subCtx);
                            new MinorLogMsg(this, "bean shell update sub error," + he, he).log(subCtx);
                        }
                    }
                }
            }
        });
       home.forEach(context,pv);
        }
       finally
       {
	        	 try
	             {
   	        		pv.shutdown(EnhancedParallVisitor.TIME_OUT_FOR_SHUTTING_DOWN);
	             }
	             catch (final Exception e)
	             {
	                 LogSupport.major(context, this, "Exception caught during wait for completion of all Init Subscriber Provisioned Bundles Threads", e);
	             }
       }
       
        
    }

    //int[] pricePlanWithBundles = new int[] {12,13,14,15,16,22,24,25,27,35,41,42,43,44,45,46,47,49};
    //initProvisionedBundles(ctx, pricePlanWithBundles);

}
