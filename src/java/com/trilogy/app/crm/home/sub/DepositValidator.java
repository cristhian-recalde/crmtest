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
package com.trilogy.app.crm.home.sub;

import com.trilogy.framework.core.locale.Currency;
import com.trilogy.framework.xhome.auth.bean.User;
import com.trilogy.framework.xhome.auth.bean.UserHome;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeOperationEnum;
import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xlog.log.MajorLogMsg;

import com.trilogy.app.crm.bean.CRMGroup;
import com.trilogy.app.crm.bean.CRMGroupHome;
import com.trilogy.app.crm.bean.PricePlanVersion;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.bean.SubscriberXInfo;
import com.trilogy.app.crm.support.Lookup;

/**
 * Validates the deposit is above the minimum deposit threshold. The minimum deposit is defined as the agent's group
 * minimum deposit percentage multiplied by the default deposit in the the raw priceplan.
 *
 * @author danny.ng@redknee.com
 */
public class DepositValidator extends AbstractSubscriberValidator
{

    /**
     * Create a new DepositValidator.
     */
    protected DepositValidator()
    {
        // empty
    }

    /**
     * Returns an instance of DepositValidator.
     *
     * @return An instance of DepositValidator.
     */
    public static DepositValidator instance()
    {
        if (instance == null)
        {
            instance = new DepositValidator();
        }

        return instance;
    }

    /**
     * Validates the deposit is below the minimum deposit threshold.
     *
     * @param ctx
     *            The operating context.
     * @param obj
     *            The subscriber being validated.
     */
    @Override
    public void validate(final Context ctx, final Object obj)
    {
        final Subscriber newSub = (Subscriber) obj;
        if(newSub.isPrepaid() || newSub.isPooled(ctx))
        {
            // nothing to validate as credit limit for a pre-paid or pooled is never used
            return;
        }
            
        final Subscriber oldSub = (Subscriber) ctx.get(Lookup.OLDSUBSCRIBER);
        
        final long deposit = newSub.getDeposit(ctx);
        if (newSub.getSubscriberType() == SubscriberTypeEnum.POSTPAID
                && newSub.getDeposit(ctx) < 0)
        {
            final CompoundIllegalStateException el = new CompoundIllegalStateException();
            el.thrown(new IllegalPropertyArgumentException(
                    SubscriberXInfo.DEPOSIT,
                    "POSTPAID Subscriber cannot have negative deposit value!"));
            el.throwAll();
        }
        else if (deposit == Subscriber.DEFAULT_DEPOSIT)
        {
            // this check is to prevent implementaion errors, when Subscriber beans are created automatically
            final CompoundIllegalStateException el = new CompoundIllegalStateException();
            el.thrown(new IllegalPropertyArgumentException(SubscriberXInfo.DEPOSIT,
                    "Deposit not initialized!"));
            el.throwAll();
        }
        final HomeOperationEnum op = (HomeOperationEnum) ctx.get(HomeOperationEnum.class);

        // Note that on the HomeOperaion Create, the oldSub would be null
        // and if the HomeOperation is STORE, then the oldSub will not be null
        if ((newSub.getSubscriberType() != SubscriberTypeEnum.PREPAID)
            && ((op == HomeOperationEnum.CREATE) || ((oldSub.getState() == SubscriberStateEnum.PENDING)
                && (newSub.getState() == SubscriberStateEnum.ACTIVE))))
        {
            PricePlanVersion ppv = null;
            // Fetch minimum deposit based on price plan default deposit
            try
            {
                ppv = newSub.getRawPricePlanVersion(ctx);
            }
            catch (HomeException e)
            {
                final CompoundIllegalStateException el = new CompoundIllegalStateException();
                el.thrown(new IllegalPropertyArgumentException(SubscriberXInfo.PRICE_PLAN,
                        "Could not get selected price plan version."));
                el.throwAll();
            }

            if (ppv != null)
            {
                final long ppDeposit = ppv.getDeposit();
                final int minDepoPercent = fetchUserMinimumDepositPercent(ctx, newSub);
                final long minDeposit = (ppDeposit * minDepoPercent) / 100;

                // Check deposit isin't below minimum deposit
                // 0 is special case that we do not need to check
                if ((deposit != 0) && (deposit < minDeposit))
                {
                    final Currency c = (Currency) ctx.get(Currency.class);
                    final String msg = "Deposit is below minimum deposit.  Minimum deposit is set at "
                            + minDepoPercent + "% of the price plan's deposit value.  The price plan's deposit is "
                            + c.formatValue(ppDeposit);

                    final CompoundIllegalStateException el = new CompoundIllegalStateException();
                    el.thrown(new IllegalPropertyArgumentException(SubscriberXInfo.DEPOSIT, msg));
                    el.throwAll();
                }
            }
        }
    }

    /**
     * Retrives the minimum deposit percent set in the user's group.
     *
     * @param ctx
     *            The operating context.
     * @param sub
     *            The subscriber being validated.
     * @return The minimum deposit percent for the user.
     */
    private int fetchUserMinimumDepositPercent(final Context ctx, final Subscriber sub)
    {
        // Fetch user's minimum deposit
        final Home userHome = (Home) ctx.get(UserHome.class);
        final Home groupHome = (Home) ctx.get(CRMGroupHome.class);
        int minDeposit = 0;

        if (userHome == null)
        {
            throw new IllegalStateException(
                "Unable to fetch User Home from context.  Cannot look up user's minimum deposit.");
        }

        if (groupHome == null)
        {
            throw new IllegalStateException(
                "Unable to fetch Group Home from context.  Cannot look up user's minimum deposit.");
        }

        try
        {
            // If the user string is empty, its likely a bulk load
            // bypassing the GUI so we set the minimum to be 0
            if (sub.getUser() != null && sub.getUser().trim().length() > 0)
            {
                final User agent = (User) userHome.find(sub.getUser());

                if (agent == null)
                {
                    final IllegalStateException exception = new IllegalStateException(
                        "Error when retrieving principal.");
                    new MajorLogMsg(this, "Cannot retrieve user group of user = " + sub.getUser() + ".", exception)
                        .log(ctx);
                    throw exception;
                }

                final CRMGroup agentGroup = (CRMGroup) groupHome.find(agent.getGroup());

                if (agentGroup == null)
                {
                    throw new IllegalStateException("Error looking up user group.");
                }
                minDeposit = agentGroup.getMinimumDeposit();
            }
        }
        catch (HomeException exception)
        {
            throw new IllegalStateException("Error looking up user's minimum deposit.", exception);
        }
        return minDeposit;
    }

    /**
     * Singleton instance.
     */
    private static DepositValidator instance;
}
