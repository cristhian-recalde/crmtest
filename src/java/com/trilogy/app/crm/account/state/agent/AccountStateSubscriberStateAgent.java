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

package com.trilogy.app.crm.account.state.agent;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Date;

import javax.management.RuntimeErrorException;

import com.trilogy.framework.xhome.beans.ExceptionListener;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.EQIC;
import com.trilogy.framework.xhome.elang.In;
import com.trilogy.framework.xhome.elang.Or;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.webcontrol.HTMLExceptionListener;
import com.trilogy.framework.xhome.xdb.XStatement;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.InfoLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MinorLogMsg;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountStateEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.bean.SubscriberXInfo;
import com.trilogy.app.crm.subscriber.state.AbstractSubscriberState;
import com.trilogy.app.crm.support.EnumStateSupportHelper;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.app.crm.dunning.DunningConstants;
import com.trilogy.app.crm.bean.SubscriptionSuspensionReason;
import com.trilogy.app.crm.bean.SubscriptionSuspensionReasonHome;
import com.trilogy.app.crm.bean.SubscriptionSuspensionReasonXInfo;
import com.trilogy.app.crm.home.TransactionRedirectionHome;

/**
 * Propagate account states to immediate subscriber' state.
 *
 * @author joe.chen@redknee.com
 */
public class AccountStateSubscriberStateAgent extends AccountStateAgentHome
{

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * States considered to be dunned already.
     */
    private static final Collection<AccountStateEnum> ACCOUNT_DUNNED_STATES = 
        Collections.unmodifiableCollection(Arrays.asList(
                AccountStateEnum.IN_ARREARS, 
                AccountStateEnum.NON_PAYMENT_WARN,
                AccountStateEnum.IN_COLLECTION, 
                AccountStateEnum.PROMISE_TO_PAY,
                AccountStateEnum.NON_PAYMENT_SUSPENDED));

    private static final Collection<SubscriberStateEnum> SUBSCRIBER_DUNNED_STATES = 
        Collections.unmodifiableCollection(Arrays.asList(
                SubscriberStateEnum.IN_ARREARS, 
                SubscriberStateEnum.NON_PAYMENT_WARN,
                SubscriberStateEnum.IN_COLLECTION, 
                SubscriberStateEnum.PROMISE_TO_PAY,
                SubscriberStateEnum.NON_PAYMENT_SUSPENDED));

    /**
     * Set of states considered to be dunned already.
     */
    private static final Set<SubscriberStateEnum> DUNNED_STATE_SET;

    static
    {
        final Set<SubscriberStateEnum> set = new HashSet<SubscriberStateEnum>();
        for (final SubscriberStateEnum element : SUBSCRIBER_DUNNED_STATES)
        {
            set.add(element);
        }
        DUNNED_STATE_SET = Collections.unmodifiableSet(set);
    }


    /**
     * Create a new instance of <code>AccountStateSubscriberStateAgent</code>.
     *
     * @param ctx
     *            The operating context.
     * @param delegate
     *            The delegate of this agent.
     */
    public AccountStateSubscriberStateAgent(final Context ctx, final Home delegate)
    {
        super(ctx, delegate);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void onStateChange(final Context parentCtx, final Account oldAccount, final Account newAccount)
        throws HomeException
    {
        final Context ctx = HomeSupportHelper.get(parentCtx).getWhereContext(
                parentCtx, 
                Subscriber.class, 
                new EQ(SubscriberXInfo.BAN, oldAccount.getBAN()));
        
        if (EnumStateSupportHelper.get(ctx).isEnteringState(oldAccount, newAccount, ACCOUNT_DUNNED_STATES))
        {
            onEnteringDunnedState(ctx, newAccount);
        }
        else if (EnumStateSupportHelper.get(ctx).isEnteringState(oldAccount, newAccount, AccountStateEnum.ACTIVE))
        {
            if (EnumStateSupportHelper.get(ctx).isTransition(oldAccount, newAccount, AccountStateEnum.SUSPENDED,
                    AccountStateEnum.ACTIVE))
            {
                onSuspendToActive(ctx, newAccount);
            }
            else if (EnumStateSupportHelper.get(ctx).isTransition(oldAccount, newAccount, ACCOUNT_DUNNED_STATES,
                    AccountStateEnum.ACTIVE))
            {
                onDunnedToActive(ctx, newAccount);
            }
        }
        else if (EnumStateSupportHelper.get(ctx).isEnteringState(oldAccount, newAccount, AccountStateEnum.SUSPENDED))
        {
            onEnteringSuspendedState(ctx, newAccount);
        }
        else if (EnumStateSupportHelper.get(ctx).isEnteringState(oldAccount, newAccount, AccountStateEnum.INACTIVE))
        {
            onDeactivation(ctx, newAccount);
        }
        // Promise to Pay
        else if (EnumStateSupportHelper.get(ctx).isEnteringState(oldAccount, newAccount, AccountStateEnum.PROMISE_TO_PAY))
        {
            onEnteringPromiseToPayState(ctx, newAccount);
        }
    }


    /**
     * Update the appropriate subscribers when entering "promise to pay" state.
     *
     * @param ctx
     *            The operating context.
     * @param newAccount
     *            The new account object.
     * @throws HomeException
     *             Thrown if there are problems selecting the subscribers.
     */
    private void onEnteringPromiseToPayState(final Context ctx, final Account newAccount) 
        throws HomeException
    {
        final Set<SubscriberStateEnum> existingStates = new HashSet<SubscriberStateEnum>();
        existingStates.add(SubscriberStateEnum.IN_ARREARS);
        existingStates.add(SubscriberStateEnum.NON_PAYMENT_WARN);
        existingStates.add(SubscriberStateEnum.PROMISE_TO_PAY);
        existingStates.add(SubscriberStateEnum.NON_PAYMENT_SUSPENDED);
        existingStates.add(SubscriberStateEnum.ACTIVE);

        final And filter = new And();
        filter.add(new In(SubscriberXInfo.STATE, existingStates));
        filter.add(new EQ(SubscriberXInfo.SUBSCRIBER_TYPE, SubscriberTypeEnum.POSTPAID));
        
        Collection<Subscriber> subs = HomeSupportHelper.get(ctx).getBeans(ctx, Subscriber.class, filter);
        
        updateSubscribers(ctx, subs, SubscriberStateEnum.PROMISE_TO_PAY, newAccount);
    }


    /**
     * Update the appropriate subscribers when entering inactive state.
     *
     * @param ctx
     *            The operating context.
     * @param newAccount
     *            The new account object.
     * @throws HomeException
     *             Thrown if there are problems selecting the subscribers.
     */
    private void onDeactivation(final Context ctx, final Account newAccount) 
        throws HomeException
    {
        final Set<SubscriberStateEnum> existingStates = new HashSet<SubscriberStateEnum>(DUNNED_STATE_SET);
        existingStates.add(SubscriberStateEnum.ACTIVE);
        existingStates.add(SubscriberStateEnum.SUSPENDED);
        existingStates.add(SubscriberStateEnum.AVAILABLE);
        existingStates.add(SubscriberStateEnum.PENDING);

        final XStatement filter = new In(SubscriberXInfo.STATE, existingStates);
        
        Collection<Subscriber> subs = HomeSupportHelper.get(ctx).getBeans(ctx, Subscriber.class, filter);

        updateSubscribers(ctx, subs, SubscriberStateEnum.INACTIVE, newAccount);
    }


    /**
     * Update the appropriate subscribers when entering suspended state.
     *
     * @param ctx
     *            The operating context.
     * @param newAccount
     *            The new account object.
     * @throws HomeException
     *             Thrown if there are problems selecting the subscribers.
     */
    private void onEnteringSuspendedState(final Context ctx,  final Account newAccount) 
        throws HomeException
    {
        /*
         * do not bar or suspend available/pending subscriber. Let the cron job take care
         * of it.
         */
        final Set<SubscriberStateEnum> existingStates = new HashSet<SubscriberStateEnum>(DUNNED_STATE_SET);
        existingStates.add(SubscriberStateEnum.ACTIVE);
        existingStates.add(SubscriberStateEnum.LOCKED);
        final XStatement filter = new In(SubscriberXInfo.STATE, existingStates);

        // prepaid subs should state change to BARRED
        SubscriberStateEnum prepaidState = SubscriberStateEnum.LOCKED;
        final SubscriberStateEnum postpaidState = SubscriberStateEnum.SUSPENDED;

        Collection<Subscriber> subs = HomeSupportHelper.get(ctx).getBeans(ctx, Subscriber.class, filter);
        
        updateSubscribers(ctx, subs, prepaidState, postpaidState, newAccount);
    }


    /**
     * Update the appropriate subscribers when transitioning from one of the dunned states
     * to active state.
     *
     * @param ctx
     *            The operating context.
     * @param newAccount
     *            The new account object.
     * @throws HomeException
     *             Thrown if there are problems selecting the subscribers.
     */
    private void onDunnedToActive(final Context ctx,  final Account newAccount) 
        throws HomeException
    {
        final And filter = new And();
        filter.add(new EQ(SubscriberXInfo.SUBSCRIBER_TYPE, SubscriberTypeEnum.POSTPAID));
        filter.add(new In(SubscriberXInfo.STATE, DUNNED_STATE_SET));

        Collection<Subscriber> subs = HomeSupportHelper.get(ctx).getBeans(ctx, Subscriber.class, filter);
        
        updateSubscribers(ctx, subs, SubscriberStateEnum.ACTIVE, newAccount);
    }


    /**
     * Update the appropriate subscribers when transitioning from suspended state to
     * active state.
     *
     * @param ctx
     *            The operating context.
     * @param newAccount
     *            The new account object.
     * @throws HomeException
     *             Thrown if there are problems selecting the subscribers.
     */
    private void onSuspendToActive(final Context ctx, final Account newAccount) 
        throws HomeException
    {
        new InfoLogMsg(this, "finding all dunned sub or suspended postpaid or barred prepaid", null).log(ctx);

        SubscriberStateEnum prepaidState = SubscriberStateEnum.LOCKED;
 
        final And postpaidSuspend = new And();
        postpaidSuspend.add(new EQ(SubscriberXInfo.SUBSCRIBER_TYPE, SubscriberTypeEnum.POSTPAID));
        postpaidSuspend.add(new EQ(SubscriberXInfo.STATE, SubscriberStateEnum.SUSPENDED));

        final And prepaid = new And();
        prepaid.add(new EQ(SubscriberXInfo.SUBSCRIBER_TYPE, SubscriberTypeEnum.PREPAID));
        prepaid.add(new EQ(SubscriberXInfo.STATE, prepaidState));

        final Or filter = new Or();
        filter.add(new In(SubscriberXInfo.STATE, DUNNED_STATE_SET));
        filter.add(postpaidSuspend);
        filter.add(prepaid);

        Collection<Subscriber> subs = HomeSupportHelper.get(ctx).getBeans(ctx, Subscriber.class, filter);
        updateSubscribers(ctx, subs, SubscriberStateEnum.ACTIVE, newAccount);
    }


    /**
     * Update the appropriate subscribers when entering one of the dunned states.
     *
     * @param ctx
     *            The operating context.
     * @param newAccount
     *            The new account.


     * @throws HomeException
     *             Thrown if there are problems selecting the subscribers.
     */
    private void onEnteringDunnedState(final Context ctx, final Account newAccount)
        throws HomeException
    {
        final Set<SubscriberStateEnum> existingStates = new HashSet<SubscriberStateEnum>(DUNNED_STATE_SET);
        existingStates.add(SubscriberStateEnum.ACTIVE);

        final And filter = new And();
        filter.add(new EQ(SubscriberXInfo.SUBSCRIBER_TYPE, SubscriberTypeEnum.POSTPAID));
        filter.add(new In(SubscriberXInfo.STATE, existingStates));

        final SubscriberStateEnum subState = AbstractSubscriberState.translateAccountState(newAccount);

        Collection<Subscriber> subs = HomeSupportHelper.get(ctx).getBeans(ctx, Subscriber.class, filter);
        
        updateSubscribers(ctx, subs, subState, newAccount);
    }


    /**
     * Update the state of all subscribers.
     *
     * @param ctx
     *            The operating context.
     * @param allSub
     *            The subscribers to be updated.
     * @param newState
     *            The new state of the subscribers.
    * @param newAccount
     *            The new account object.
     */
    private void updateSubscribers(final Context ctx, final Collection allSub, 
            final SubscriberStateEnum newState,
            final Account newAccount)
    {
        updateSubscribers(ctx, allSub, newState, newState, newAccount);
    }


    /**
     * Update the state of all subscribers.
     *
     * @param ctx
     *            The operating context.
     * @param allSub
     *            The subscribers to be updated.
     * @param newPrepaidSubState
     *            The new state for prepaid subscribers.
     * @param newPostpaidSubState
     *            The new state for postpaid subscribers.
     * @param newAccount
     *            The new account object.
     */
    private void updateSubscribers(final Context ctx, final Collection allSub,
        final SubscriberStateEnum newPrepaidSubState, final SubscriberStateEnum newPostpaidSubState,
        final Account newAccount)
    {
        if (LogSupport.isDebugEnabled(ctx))
        {
            new DebugLogMsg(this, "updating a collection of prepaid subscriber to state " + newPrepaidSubState
                + " and postpaid subscriber to state " + newPostpaidSubState, null).log(ctx);
        }

        for (final Iterator i = allSub.iterator(); i.hasNext();)
        {
            final Subscriber sub = (Subscriber) i.next();
            // go to suspend this subscriber

            if (sub.isPrepaid() && !EnumStateSupportHelper.get(ctx).stateEquals(sub, newPrepaidSubState))
            {
               sub.setState(newPrepaidSubState);

                if (LogSupport.isDebugEnabled(ctx))
                {
                    new DebugLogMsg(this, "updating prepaid subscriber " + sub.getId() + " to state " + sub.getState(),
                        null).log(ctx);
                }
                updateSubscriber(ctx, sub, newAccount);
            }
            else if (sub.isPostpaid() && !EnumStateSupportHelper.get(ctx).stateEquals(sub, newPostpaidSubState))
            {
            	SubscriberStateEnum oldSubState = sub.getState();
				String subsuspensionReason = sub.getSuspensionReason();
				sub.setState(newPostpaidSubState);
				
				// when account is suspended manually subscription is also
				// suspended automatically.

				if (newAccount.getState()
						.equals(AccountStateEnum.SUSPENDED)
						&& newAccount.getSuspensionReason() == -1) {
					// set suspension reason as other for manual suspension
					// for all subscription
					String subReason = getSusbcriberSuspensionReason(ctx,
							sub);
					sub.setSuspensionReason(subReason);
					updateSubscriber(ctx, sub, newAccount);
					continue;
				}
				// if dunning suspended subscriber is going to active state
				// after payment then set suspensionreason as default(-1)
				if (sub.getState().equals(SubscriberStateEnum.ACTIVE)
						&& sub.getSuspensionReason() == getSubscriberSuspensionReasonDueToDunning(
								ctx, sub) && newAccount.getState()
								.equals(AccountStateEnum.ACTIVE)) {
					String reason = null;
					sub.setSuspensionReason(reason);
					sub.setResumedDate(new Date());
				}
				// manually suspended subscriber is going to active state
				// manually.Set suspensionReason as default(-1).
				if (sub.getSuspensionReason() == getSusbcriberSuspensionReason(
						ctx, sub)) {

					if (sub.getState().equals(SubscriberStateEnum.ACTIVE)) {
						String subReason = null;
						sub.setSuspensionReason(subReason);
					}

					// check whether state transition initiated by Payments
					if (ctx.has(TransactionRedirectionHome.IS_DEBT_CLEARED_BY_TRANSACTION)) {
						// manually suspended subscriber should not come to
						// active after full/partial payments.
						sub.setState(oldSubState);
						sub.setSuspensionReason(subsuspensionReason);
					}
				}
                if (LogSupport.isDebugEnabled(ctx))
                {
                    new DebugLogMsg(this,
                        "updating postpaid subscriber " + sub.getId() + " to state " + sub.getState(), null).log(ctx);
                }
                updateSubscriber(ctx, sub, newAccount);
            }
        }
    }
	private String getSusbcriberSuspensionReason(Context ctx, Subscriber sub) {

		int spid = sub.getSpid();

		And where = new And();
		where.add(new EQ(SubscriptionSuspensionReasonXInfo.SPID, spid));
		where.add(new EQIC(SubscriptionSuspensionReasonXInfo.NAME,
				DunningConstants.DUNNING_SUSPENSION_REASON_OTHER));
		SubscriptionSuspensionReason bean = null;
		Home home = (Home) ctx.get(SubscriptionSuspensionReasonHome.class);

		try {
			bean = (SubscriptionSuspensionReason) home.find(ctx, where);
		} catch (HomeException e) {
			throw new RuntimeException(
					"Exception occured while finding Subscriber suspension reason");
		}
		if (bean==null) {
			throw new RuntimeException(
					"Subscriber Suspension reason not found");
		}
		return bean.getReasoncode();
	}

	private String getSubscriberSuspensionReasonDueToDunning(Context context,
			Subscriber sub) {

		int spid = sub.getSpid();

		And where = new And();
		where.add(new EQ(SubscriptionSuspensionReasonXInfo.SPID, spid));
		where.add(new EQIC(SubscriptionSuspensionReasonXInfo.NAME,
				DunningConstants.DUNNING_SUSPENSION_REASON_UNPAID));
		SubscriptionSuspensionReason bean = null;
		Home home = (Home) context.get(SubscriptionSuspensionReasonHome.class);
		
		try {
			bean = (SubscriptionSuspensionReason) home.find(context, where);
		} catch (HomeException e) {
			throw new RuntimeException(
					"Exception occured while finding Subscriber suspension reason");
		}
			
			if (bean == null) {
				throw new RuntimeException(
						"Subscriber Suspension reason not found");
			}
		return bean.getReasoncode();
	}

    /**
     * Update a subscriber.
     *
     * @param ctx
     *            The operating context.
     * @param sub
     *            The subscriber to be updated.
     * @param newAccount
     *            The new account object.
     */
    private void updateSubscriber(final Context ctx, final Subscriber sub, final Account newAccount)
    {
        try
        {
            HomeSupportHelper.get(ctx).storeBean(ctx, sub);
            if (newAccount.isIndividual(ctx))
            {
                newAccount.setSubscriber(sub);
            }
        }
        catch (final HomeException e)
        {
            final ExceptionListener el = (ExceptionListener) ctx.get(HTMLExceptionListener.class);
            if (el != null)
            {
                el.thrown(e);
            }

            // generate ER with result code 3009

            // generate Account_Modify failure OM

            new MinorLogMsg(this, " update subsciber state " + sub.getMSISDN() + " failed", e).log(ctx);
        }
    }
}
