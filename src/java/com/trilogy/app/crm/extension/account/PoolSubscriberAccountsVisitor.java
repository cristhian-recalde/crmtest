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
package com.trilogy.app.crm.extension.account;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.trilogy.framework.core.locale.Currency;
import com.trilogy.framework.xhome.beans.ExceptionListener;
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
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.InfoLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountHome;
import com.trilogy.app.crm.bean.AccountStateEnum;
import com.trilogy.app.crm.bean.AccountXInfo;
import com.trilogy.app.crm.bean.GroupTypeEnum;
import com.trilogy.app.crm.bean.QuotaTypeEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberHome;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.bean.SubscriberXInfo;
import com.trilogy.app.crm.client.bm.BalanceManagementSupport;
import com.trilogy.app.crm.client.bm.Parameters;
import com.trilogy.app.crm.client.bm.SubscriberProfileProvisionClient;
import com.trilogy.app.crm.home.sub.SubscriptionDeactivateValidator;
import com.trilogy.app.crm.support.AccountSupport;
import com.trilogy.app.crm.support.AccountTypeSupportHelper;

/**
 * Visit accounts, make sure these are Subscriber Accounts. Move all Subscriptions in this account to the pool.
 *
 * @author victor.stratan@redknee.com
 */
public class PoolSubscriberAccountsVisitor implements Visitor
{
    long balanceAcumulator_ = 0;
    private final Long subscriptionType_;
    private final SubscriberProfileProvisionClient bmClient_;
    private final String poolID_;
    ExceptionListener exceptionListener_;

    PoolSubscriberAccountsVisitor(final SubscriberProfileProvisionClient bmClient, final Long subscriptionTypes,
            final String poolID, ExceptionListener exceptionListener)
    {
        subscriptionType_ = subscriptionTypes;
        bmClient_ = bmClient;
        poolID_ = poolID;
        exceptionListener_ = exceptionListener;
    }

    public void visit(final Context ctx, final Object obj) throws AgentException, AbortVisitException
    {
        final Account account = (Account) obj;

        if (!account.isIndividual(ctx))
        {
            final Home accountHome = (Home) ctx.get(AccountHome.class);
            final And condition = new And();
            condition.add(new EQ(AccountXInfo.PARENT_BAN, account.getBAN()));
            condition.add(new EQ(AccountXInfo.RESPONSIBLE, Boolean.FALSE));
            condition.add(new Not(new In(AccountXInfo.STATE, PoolSubscriberAccountsVisitor.DISABLED_STATES)));
            try
            {
                condition.add(new EQ(AccountXInfo.GROUP_TYPE, GroupTypeEnum.SUBSCRIBER));
                accountHome.forEach(ctx, this, condition);
            }
            catch (HomeException e)
            {
                LogSupport.minor(ctx, this, "Error while pooling subaccounts in Account " + account.getBAN(), e);
            }
            return;
        }

        final Home subHome = (Home) ctx.get(SubscriberHome.class);
        final And condition = new And();
        condition.add(new EQ(SubscriberXInfo.BAN, account.getBAN()));
        condition.add(new EQ(SubscriberXInfo.SUBSCRIPTION_TYPE, subscriptionType_));
        condition.add(new Not(new In(SubscriberXInfo.STATE, PoolSubscriptionsVisitor.DISABLED_STATES)));

        PoolSubscriptionsVisitor visitor = new AddSubscriptionToPoolVisitor(subHome, bmClient_, poolID_, exceptionListener_);

        try
        {
            visitor = (PoolSubscriptionsVisitor) subHome.forEach(ctx, visitor, condition);
        }
        catch (HomeException e)
        {
            LogSupport.minor(ctx, this, "Error while pooling subscriptions in Account " + account.getBAN(), e);
        }

        balanceAcumulator_ += visitor.getAcumulatedBalance();
    }

    public long getAcumulatedBalance()
    {
        return balanceAcumulator_;
    }

    public static final Set DISABLED_STATES = new HashSet();

    static
    {
        DISABLED_STATES.add(AccountStateEnum.INACTIVE);
        DISABLED_STATES.add(AccountStateEnum.IN_COLLECTION);
    }
}

abstract class PoolSubscriptionsVisitor implements Visitor
{
    long balanceAcumulator_ = 0;
    SubscriberProfileProvisionClient bmClient_;
    Home subHome_;
    ExceptionListener exceptionListener_;

    public PoolSubscriptionsVisitor(final Home subHome, final SubscriberProfileProvisionClient bmClient, ExceptionListener  exceptionListener)
    {
        subHome_ = subHome;
        bmClient_ = bmClient;
        exceptionListener_ = exceptionListener;
    }

    public abstract void action(final Context ctx, final Subscriber subscriber, final Parameters subscription);

    public void visit(final Context ctx, final Object obj) throws AgentException, AbortVisitException
    {
        Subscriber subscriber = (Subscriber) obj;

        Parameters subscription = BalanceManagementSupport.getSubscription(ctx, this, bmClient_, subscriber);
        if (subscription == null)
        {
            return;
        }

        action(ctx, subscriber, subscription);
    }

    public long getAcumulatedBalance()
    {
        return balanceAcumulator_;
    }

    public static final Set DISABLED_STATES = new HashSet();

    static
    {
        DISABLED_STATES.add(SubscriberStateEnum.INACTIVE);
        DISABLED_STATES.add(SubscriberStateEnum.IN_COLLECTION);
        DISABLED_STATES.add(SubscriberStateEnum.DORMANT);
        DISABLED_STATES.add(SubscriberStateEnum.EXPIRED);
    }

    void handleException(Context ctx, Subscriber poolSub, String message)
    {
       handleException(ctx, poolSub, message, null);
    }

    void handleException(Context ctx, Subscriber poolSub, String message, Throwable t)
    {
        final String errorMessage = "Error Pooling of Subscription-Type [" + poolSub.getSubscriptionType()
                + "] under Account [" + poolSub.getBAN() + "] with Pool-MSISDN [" + poolSub.getMSISDN() + "]: Error ["
                + (((message == null) && t != null) ? t.getMessage() : message) + "]";
        new DebugLogMsg(this, errorMessage, t).log(ctx);
        if (exceptionListener_ != null)
        {
            exceptionListener_.thrown(new IllegalStateException(errorMessage, t));
        }
    }
}

class AddSubscriptionToPoolVisitor extends PoolSubscriptionsVisitor
{
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private final String poolID_;

    public AddSubscriptionToPoolVisitor(final Home subHome, final SubscriberProfileProvisionClient bmClient,
            final String poolID, ExceptionListener  exceptionListener)
    {
        super(subHome, bmClient,exceptionListener);
        poolID_ = poolID;
    }
    
    @Override
    public void action(final Context ctx, final Subscriber subscriber, final Parameters subscription)
    {
        final String poolGroupID = subscription.getPoolGroupID();
        if (poolGroupID == null || poolGroupID.length() == 0)
        {
            long balance = subscription.getBalance();
            boolean result = BalanceManagementSupport.updatePooledGroupID(ctx, this, bmClient_, subscriber, poolID_);
            if (result)
            {
                try
                {
                    // credit limit values become useless for members in a pooled setup
                    subscriber.setCreditLimit(0L);
                    if (subscriber.isPrepaid() && AccountSupport.getAccount(ctx, poolID_).isHybrid())
                    {
                        subscriber.setQuotaType(QuotaTypeEnum.LIMITED_QUOTA);
                        subscriber.setQuotaLimit(0);
                        // balance is not pooled for Pre-Paid in an hybrid pool
                    }
                    else
                    {
                        // Accumulate the balance no matter prepaid or postpaid...
                        // balance to be used (prepaid)
                        // or balance is already used (postpaid)
                        // Either way, the pool accumultes it
                        result = BalanceManagementSupport.zeroSubscriberBalance(ctx, this, bmClient_, subscriber);
                        if (result)
                        {
                            // make sure we accumulate the balance in the pool only when
                            // it has been wiped out from the member's kitty
                            balanceAcumulator_ += balance;
                        }
                        else
                        {
                            final Currency currency = (Currency) ctx.get(Currency.class, Currency.DEFAULT);
                            final String errorMessage = "Member Subscription [" + subscriber.getId()
                                    + "]'s balance of [" + currency.getCode() + " " + currency.formatValue(balance)
                                    + "  ] could not be be accumulated in the Pool with ID [" + poolID_ + "].";
                            handleException(ctx, subscriber, errorMessage);
                        }
                    }
                    ((Home) ctx.get(SubscriberHome.class)).store(ctx, subscriber);
                }
                catch (HomeException e)
                {
                    final String errorMessage = "Pool Member Subscription [" + subscriber.getId()
                            + "] could not be stored due to error " + "[" + e.getMessage() + "  ]";
                    handleException(ctx, subscriber, errorMessage, e);
                }
            }
            else
            {
                final String errorMessage = "Subscription [" + subscriber.getId()
                        + "] could not be added to the Pool on URCS.";
                handleException(ctx, subscriber, errorMessage);
            }
        }
    }
}

class RemoveSubscriptionFromPoolVisitor extends PoolSubscriptionsVisitor
{

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public RemoveSubscriptionFromPoolVisitor(final Home subHome, Subscriber poolSub,
            final SubscriberProfileProvisionClient bmClient,ExceptionListener  exceptionListener)
    {
        super(subHome, bmClient, exceptionListener);
        poolSub_ = poolSub;
    }


    @Override
    public void action(final Context ctx, final Subscriber subscriber, final Parameters subscription)
    {
        final String poolGroupID = subscription.getPoolGroupID();
        if (poolGroupID != null && poolGroupID.length() > 0 && poolGroupID.equals(poolSub_.getMSISDN()))
        {
            boolean result = BalanceManagementSupport.updatePooledGroupID(ctx, this, bmClient_, subscriber, "");
            if (result)
            {
                return;
            }
            LogSupport.minor(ctx, this, "Subscription move to Pool failed: " + subscriber.getId());
        }
    }

    final Subscriber poolSub_;
}

/**
 * 
 * @author ssimar
 * This class safely removes a poll and disassociates its members if any
 * 
 */
class RemoveSubscriptionPoolVisitor implements Visitor
{

    public RemoveSubscriptionPoolVisitor(Home subHome, Home accountHome, SubscriberProfileProvisionClient bmClient,
            ExceptionListener exceptionListner)
    {
        subHome_ = subHome;
        exceptionListner_ = exceptionListner;
        accountHome_ = accountHome;
        bmClient_ = bmClient;
        closedPools_ = new ArrayList<Subscriber>();
    }

    /**
     * 
     */
    private static final long serialVersionUID = 1L;


    public void visit(final Context ctx, final Object obj)
    {
        final Subscriber poolSub = (Subscriber) obj;
        if (poolSub.isPooledGroupLeader(ctx))
        {
            try
            {
                /*
                 * ensure the pool being removed has no existing memberships all
                 * non-responsible subscriber accounts under a pool are members set
                 */
                final And condition = new And();
                condition.add(new EQ(AccountXInfo.PARENT_BAN, poolSub.getBAN()));
                condition.add(new EQ(AccountXInfo.RESPONSIBLE, Boolean.FALSE));
                condition.add(new Not(new In(AccountXInfo.STATE, PoolSubscriberAccountsVisitor.DISABLED_STATES)));
                condition.add(new EQ(AccountXInfo.GROUP_TYPE, GroupTypeEnum.SUBSCRIBER));
                accountHome_.forEach(ctx, new Visitor()
                {

                    /**
                     * As of today, we do not allow pools to be closed unless pool account
                     * is being closed. As we can only close an account if it's
                     * sub-accounts are closed, we should not hit this forEach()
                     */
                    public void visit(Context ctx, Object obj) throws AgentException, AbortVisitException
                    {
                        final Account account = (Account) obj;
                        final And condition = new And();
                        condition.add(new EQ(SubscriberXInfo.BAN, account.getBAN()));
                        condition.add(new EQ(SubscriberXInfo.SUBSCRIPTION_TYPE, poolSub.getSubscriptionType()));
                        condition.add(new Not(new In(SubscriberXInfo.STATE, PoolSubscriptionsVisitor.DISABLED_STATES)));
                        try
                        {
                            subHome_.forEach(ctx, new RemoveSubscriptionFromPoolVisitor(subHome_, poolSub, bmClient_, exceptionListner_),
                                    condition);
                        }
                        catch (HomeException e)
                        {
                            final String errorMessage = "Unable to remove pool memebetrship of Subscriber Account ["
                                    + account.getBAN() + "] under Pooled Account[" + poolSub.getBAN()
                                    + "] for Subscription-Type [" + poolSub.getSubscriptionType() + "] .Error ["
                                    + e.getMessage() + "]";
                            handleException(ctx, poolSub, errorMessage, e);
                        }
                    }
                }, condition);
            }
            catch (HomeException e)
            {
                final String errorMessage = "Unable to remove all memebetrships of pools under " + poolSub.getBAN()
                        + " for SubscriptionT-ype [" + poolSub.getSubscriptionType() + "] .Error [" + e.getMessage()
                        + "]";
                handleException(ctx, poolSub, errorMessage, e);
            }
            new InfoLogMsg(this, "Removing Pool of Subscription-Type [" + poolSub.getSubscriptionType()
                    + "] under Account [" + poolSub.getBAN() + "] with Pool-MSISDN [" + poolSub.getMSISDN(), null)
                    .log(ctx);
            poolSub.setState(SubscriberStateEnum.INACTIVE);
            try
            {
                Context subCtx = ctx.createSubContext();
                subCtx.put(SubscriptionDeactivateValidator.WRITE_OFF, true);
                subHome_.store(subCtx, poolSub);
                closedPools_.add(poolSub);
            }
            catch (HomeException e)
            {
                final String errorMessage = "Unable to deactivate Subscription Pool under " + poolSub.getBAN()
                        + " for SubscriptionT-ype [" + poolSub.getSubscriptionType() + "] .Error [" + e.getMessage()
                        + "]";
                handleException(ctx, poolSub, errorMessage, e);
            }
        }
        
        
    }

    private void handleException(Context ctx, Subscriber poolSub, String message, Throwable t)
    {
        new DebugLogMsg(this, message, t).log(ctx);
        LogSupport.minor(ctx, this, message, null);
        if (exceptionListner_ != null)
        {
            exceptionListner_.thrown(new IllegalStateException(message, t));
        }
    }
    
    
    public Collection<Subscriber> getClosedPools()
    {
        return closedPools_;
    }
    
    final List <Subscriber> closedPools_;
    final ExceptionListener exceptionListner_;
    final Home subHome_;
    final Home accountHome_;
    final SubscriberProfileProvisionClient bmClient_;
}