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
package com.trilogy.app.crm.subscriber.state;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountHome;
import com.trilogy.app.crm.bean.AccountStateEnum;
import com.trilogy.app.crm.bean.AccountXInfo;
import com.trilogy.app.crm.bean.GroupTypeEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberHome;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.bean.SubscriberXInfo;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.In;
import com.trilogy.framework.xhome.elang.Not;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.visitor.AbortVisitException;
import com.trilogy.framework.xhome.visitor.Visitor;
import com.trilogy.framework.xlog.log.MinorLogMsg;


/**
 * This visitor mutates the state of a Subscriber including its pool members if any.
 * 
 * @author simar.singh@redknee.com
 * 
 */
public class PoolHandlingSubscriberStateMutator implements Visitor
{

    private static final long serialVersionUID = 1L;


    public PoolHandlingSubscriberStateMutator(SubscriberStateEnum mutateToState)
    {
        this.mutateToState_ = mutateToState;
    }


    @Override
    public void visit(Context ctx, Object obj) throws AgentException, AbortVisitException
    {
        final Subscriber subscriber = (Subscriber) obj;
        mutate(ctx, subscriber);
    }


    public void mutate(Context ctx, Subscriber subscriber)
    {
        try
        {
            subscriber.setState(mutateToState_);
            HomeSupportHelper.get(ctx).storeBean(ctx, subscriber);
            if (subscriber.isPooledGroupLeader(ctx))
            {
                processPoolSubscription(ctx, subscriber);
            }   
        }
        catch (Exception e)
        {
            // catching Exception to try to process all subscriptions
            final String errorMessage = "Dormant-State-Agent: Error processing Subscriber ID [" + subscriber.getId()
                    + "]. Error [" + e.getMessage() + "]";
            new MinorLogMsg(this, errorMessage, e).log(ctx);
        }
    }


    private void processPoolSubscription(Context ctx, final Subscriber subscriber) throws HomeException
    {
        final Home subHome = (Home) ctx.get(SubscriberHome.class);
        final And poolMemAcctCond = new And();
        poolMemAcctCond.add(new EQ(AccountXInfo.PARENT_BAN, subscriber.getBAN()));
        poolMemAcctCond.add(new EQ(AccountXInfo.RESPONSIBLE, Boolean.FALSE));
        poolMemAcctCond.add(new Not(new In(AccountXInfo.STATE, DISABLED_ACCOUNT_STATES)));
        poolMemAcctCond.add(new Not(new EQ(AccountXInfo.GROUP_TYPE, GroupTypeEnum.SUBSCRIBER)));
        final Home acctHome = (Home) ctx.get(AccountHome.class);
        acctHome.forEach(ctx, new Visitor()
        {

            private static final long serialVersionUID = 1L;


            @Override
            public void visit(Context ctx, Object obj) throws AgentException, AbortVisitException
            {
                if (obj instanceof Account)
                {
                    final Account pooledMemAcct = (Account) obj;
                    final And poolMemSubCond = new And();
                    poolMemSubCond.add(new EQ(SubscriberXInfo.BAN, pooledMemAcct.getBAN()));
                    poolMemSubCond.add(new EQ(SubscriberXInfo.SUBSCRIPTION_TYPE, subscriber.getSubscriptionType()));
                    // only mutate the Subscriber Members which are same type
                    // (post-paid/pre-paid) as their leader
                    // this is necessary to handle the family-plan / hybrid pools where a
                    // pre-paid member under a post-paid leader has its own balance but
                    // shares the bundles.
                    poolMemAcctCond.add(new EQ(SubscriberXInfo.SUBSCRIBER_TYPE, subscriber.getSubscriberType()));
                    poolMemAcctCond.add(new Not(new In(SubscriberXInfo.STATE, DISABLED_SUBSCRIBER_STATES)));
                    try
                    {
                        subHome.forEach(ctx, new Visitor()
                        {
                            private static final long serialVersionUID = 1L;

                            @Override
                            public void visit(Context ctx, Object obj) throws AgentException, AbortVisitException
                            {
                                final Subscriber memberSub = (Subscriber) obj;
                                try
                                {
                                    memberSub.setState(mutateToState_);
                                    HomeSupportHelper.get(ctx).storeBean(ctx, memberSub);
                                }
                                catch (HomeException e)
                                {
                                    final String errorMessage = "Dormant-State-Agent: Error processing Member Subscriber ID ["
                                            + memberSub.getId() + "]. Error [" + e.getMessage() + "]";
                                    new MinorLogMsg(this, errorMessage, e).log(ctx);
                                }
                            }
                        });
                    }
                    catch (HomeException e)
                    {
                        final String errorMessage = "Dormant-State-Agent: Error processing Member Subscriber ID ["
                                + subscriber.getId() + "]. Error [" + e.getMessage() + "]";
                        new MinorLogMsg(this, errorMessage, e).log(ctx);
                    }
                }
            }
        }, poolMemAcctCond);
    }

    final SubscriberStateEnum mutateToState_;


    public static final Set<SubscriberStateEnum> DISABLED_SUBSCRIBER_STATES = Collections
            .unmodifiableSet(new HashSet<SubscriberStateEnum>(Arrays.asList(SubscriberStateEnum.INACTIVE,
                    SubscriberStateEnum.PENDING, SubscriberStateEnum.DORMANT)));
    public static final Set<AccountStateEnum> DISABLED_ACCOUNT_STATES = Collections
            .unmodifiableSet(new HashSet<AccountStateEnum>(Arrays.asList(AccountStateEnum.INACTIVE,
                    AccountStateEnum.IN_COLLECTION, AccountStateEnum.IN_ARREARS)));
}
