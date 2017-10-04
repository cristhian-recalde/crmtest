/*
 * This code is a protected work and subject to domestic and international copyright
 * law(s). A complete listing of authors of this work is readily available. Additionally,
 * source code is, by its very nature, confidential information and inextricably contains
 * trade secrets and other information proprietary, valuable and sensitive to Redknee. No
 * unauthorized use, disclosure, manipulation or otherwise is permitted, and may only be
 * used in accordance with the terms of the license agreement entered into with Redknee
 * Inc. and/or its subsidiaries.
 * 
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.subscriber.cron;

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;

import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.CRMSpidHome;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberHome;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.bean.SubscriberXInfo;
import com.trilogy.app.crm.filter.SubscriberExpiredPredicate;
import com.trilogy.app.crm.subscriber.agent.PreExpireSubscriber;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.app.crm.support.SubscriptionNotificationSupport;
import com.trilogy.app.crm.support.SpidSupport;
import com.trilogy.app.crm.support.SubscriberSupport;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgent;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.EQDay;
import com.trilogy.framework.xhome.elang.GT;
import com.trilogy.framework.xhome.elang.In;
import com.trilogy.framework.xhome.elang.LTE;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.visitor.AbortVisitException;
import com.trilogy.framework.xhome.visitor.PredicateVisitor;
import com.trilogy.framework.xhome.visitor.Visitor;
import com.trilogy.framework.xhome.visitor.Visitors;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MinorLogMsg;


/**
 * Process subscribers that pre-expired, sending sms notification.
 *
 * @author jchen
 */
public class SubsriberPreExpiryAgent implements ContextAgent
{
    public void execute(Context ctx) throws AgentException
    {
        Home spidHome = (Home) ctx.get(CRMSpidHome.class);
        Collection allSpids = null;
        try
        {
            allSpids = spidHome.selectAll();
        }
        catch (HomeException e)
        {
            new MinorLogMsg(this, "Error getting data from spid table", e).log(ctx);
            return;
        }
        final Home subHome = (Home) ctx.get(SubscriberHome.class);
        if (subHome == null)
        {
            new MinorLogMsg(this, "System error: no SubscriberHome found in context.", null).log(ctx);
            return;
        }
        for (Object spid : allSpids)
        {
            CRMSpid crmSp = (CRMSpid) spid;
            // try to get all pre expired sub for this spid, since pre-expiry is spid
            // aware
            try
            {
                int preExpiryDays = PreExpireSubscriber.getPreExpiryDays(ctx, crmSp);
                LogSupport.debug(ctx, this,
                        "needs Pre Expiry Msg : " + SpidSupport.needsPreExpiryMsg(ctx, crmSp.getId()));
                if (SpidSupport.needsPreExpiryMsg(ctx, crmSp.getId()))
                {
                    PredicateVisitor predicateVisitor = new PredicateVisitor(new SubscriberExpiredPredicate(
                            preExpiryDays), new Visitor()
                    {
                        private static final long serialVersionUID = 1L;

                        public void visit(Context context, Object obj) throws AgentException, AbortVisitException
                        {
                            Context vCtx = context.createSubContext();
                            if (obj instanceof Subscriber)
                            {
                                Subscriber sub = (Subscriber) obj;
                                if (!sub.getPreExpirySmsSent())
                                {
                                    SubscriptionNotificationSupport.sendPreExpiryNotification(vCtx, sub);
                                }
                            }
                        }
                    });
                    Date currentDate = CalendarSupportHelper.get(ctx).getRunningDate(ctx);
                    final Date adjustedTodayDate = CalendarSupportHelper.get(ctx).getDateWithNoTimeOfDay(
                            getAdjustedExpiry(currentDate, preExpiryDays));
                    And and = new And()
                            .add(new EQ(SubscriberXInfo.SUBSCRIBER_TYPE, SubscriberTypeEnum.PREPAID))
                            .add(new EQ(SubscriberXInfo.SPID, crmSp.getSpid()))
                            .add(new EQDay(SubscriberXInfo.EXPIRY_DATE, adjustedTodayDate))
                            .add(new GT(SubscriberXInfo.EXPIRY_DATE, Subscriber.NEVER_EXPIRE_CUTOFF_DATE))
                            .add(new In(SubscriberXInfo.STATE, new HashSet()).add(SubscriberStateEnum.ACTIVE).add(
                                    SubscriberStateEnum.SUSPENDED));
                    subHome.forEach(ctx, predicateVisitor, and);
                }
            }
            catch (HomeException e)
            {
                LogSupport.minor(ctx, this, "Unable to run pre expiry process", e);
            }
            catch (Exception e)
            {
                LogSupport.minor(ctx, this, "Unable to run pre expiry process, exception thrown", e);                
            }
        }
    }


    /**
     * returns a new adjusted expiry date (Used for pre-expiry stuff)
     * 
     * @param expiryDate
     * @return
     */
    public Date getAdjustedExpiry(Date expiryDate, int adjExpiryDays)
    {
        Date dat = expiryDate;
        if (adjExpiryDays != 0)
        {
            final Calendar calendar = Calendar.getInstance();
            calendar.setTime(expiryDate);
            /*TT: 12060705002 Fix : As Pre-Expiry SMS Notification is for sending notification to subscribers prior of getting Expired. 
            It should not be multiplied by -1.
            */
            calendar.add(Calendar.DATE, Math.abs(adjExpiryDays));
            dat = calendar.getTime();
        }
        return dat;
    }
}
