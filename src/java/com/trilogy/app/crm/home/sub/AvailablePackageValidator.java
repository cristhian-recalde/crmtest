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

import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.beans.SafetyUtil;
import com.trilogy.framework.xhome.beans.Validator;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.language.MessageMgr;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.PackageStateEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberXInfo;
import com.trilogy.app.crm.bean.UpdateReasonEnum;
import com.trilogy.app.crm.bean.core.SubscriptionType;
import com.trilogy.app.crm.numbermgn.GenericPackage;
import com.trilogy.app.crm.support.Lookup;
import com.trilogy.app.crm.support.PackageSupportHelper;
import com.trilogy.app.crm.technology.TechnologyEnum;


/**
 * Validates that if the subscriber is not new, then that any change of Package
 * is to one that is Available.
 *
 * @author gary.anderson@redknee.com
 */
public class AvailablePackageValidator implements Validator
{
    /**
     * Key used by the Message Manager to lookup the appropriate message for
     * indicating that a card package is not available. The only variable
     * allowed is for the package identifier.
     */
    public static final String NOT_AVAILABLE_KEY =
        "AvailablePackageValidator.NOT_AVAILABLE_KEY";

    /**
     * Key used by the Message Manager to lookup the appropriate message for
     * indicating that a card package lookup failure occurred. The only variable
     * allowed is for the package identifier.
     */
    public static final String FAILURE_LOOKUP_KEY =
        "AvailablePackageValidator.FAILURE_LOOKUP_KEY";

    /**
     * Key used by the Message Manager to lookup the appropriate message for
     * indicating that a card package lookup failure occurred. The only variable
     * allowed is for the package identifier.
     */
    public static final String WRONG_SPID_KEY =
        "AvailablePackageValidator.WRONG_SPID_KEY";

    /**
     * {@inheritDoc}
     */
    @Override
    public void validate(final Context context, final Object object)
    {
        final Subscriber newSubscriber = (Subscriber) object;
        final Subscriber oldSubscriber = (Subscriber) context.get(Lookup.OLDSUBSCRIBER);

        SubscriptionType subscriptionType = newSubscriber.getSubscriptionType(context);
        
        TechnologyEnum technology = newSubscriber.getTechnology();
        
        if (technology!=null && !technology.isPackageAware())
        {
            // validate only for package-aware subscriptions
            return;
        }
        else 
        {
            final Account account =(Account) context.get(Account.class);
            
			if (account != null && account.isPooled(context))
            {
                // ignoring fake subscription that is created for Pooled acounts
                return;
            }           
           
        }
            

        // Cindy Wong: Make sure the package is in the correct technology. 

        GenericPackage card = null;
        try
        {
            card = PackageSupportHelper.get(context).getPackage(
                context,
                newSubscriber.getTechnology(),
                newSubscriber.getPackageId(), newSubscriber.getSpid());
        }
        catch (final HomeException exception)
        {
            final IllegalPropertyArgumentException newException =
                new IllegalPropertyArgumentException(
                    SubscriberXInfo.PACKAGE_ID,
                    getFailureLookupMessage(context, newSubscriber.getPackageId()));

            newException.initCause(exception);

            final CompoundIllegalStateException compound = new CompoundIllegalStateException();
            compound.thrown(newException);
            compound.throwAll();
        }

        if (card == null)
        {
            final CompoundIllegalStateException compound = new CompoundIllegalStateException();
            compound.thrown(
                new IllegalPropertyArgumentException(
                    SubscriberXInfo.PACKAGE_ID,
                    getFailureLookupMessage(context, newSubscriber.getPackageId())));
            compound.throwAll();
        }

        // If the package identifiers are not different, then there is nothing
        // further to validate.
        if ((oldSubscriber != null && SafetyUtil.safeEquals(
            oldSubscriber.getPackageId(),
            newSubscriber.getPackageId())) ||
            newSubscriber.getUpdateReason() == UpdateReasonEnum.CONVERSION)
        {
            return;
        }

        if (card.getSpid() != newSubscriber.getSpid())
        {
            final CompoundIllegalStateException compound = new CompoundIllegalStateException();
            final String msg = getWrongSpidMessage(context, card.getPackId(), card.getSpid(), newSubscriber.getSpid());
            compound.thrown(new IllegalPropertyArgumentException(SubscriberXInfo.PACKAGE_ID, msg));
            compound.throwAll();
        }

        if (PackageStateEnum.AVAILABLE != card.getState())
        {
            final CompoundIllegalStateException compound = new CompoundIllegalStateException();
            compound.thrown(
                new IllegalPropertyArgumentException(
                    SubscriberXInfo.PACKAGE_ID,
                    getNotAvailableMessage(context, card.getPackId())));
            compound.throwAll();
        }
    }


    /**
     * Gets the failure lookup message from the message manager.
     *
     * @param context The application context.
     * @param packageIdentifier The identifier of the card package.
     * @return The failure lookup message.
     */
    private String getFailureLookupMessage(
        final Context context,
        final String packageIdentifier)
    {
        final MessageMgr manager = new MessageMgr(context, this);
        return manager.get(
            FAILURE_LOOKUP_KEY,
            FAILURE_LOOKUP_DEFAULT,
            new String[]
            {
                packageIdentifier,
            });
    }


    /**
     * Gets the card-in-use message from the message manager.
     *
     * @param context The operating context.
     * @param packageIdentifier The identifier of the card package.
     * @return The card-in-use message from the message manager.
     */
    private String getNotAvailableMessage(
        final Context context,
        final String packageIdentifier)
    {
        final MessageMgr manager = new MessageMgr(context, this);
        return manager.get(
            NOT_AVAILABLE_KEY,
            NOT_AVAILABLE_DEFAULT,
            new String[]
            {
                packageIdentifier,
            });
    }


    /**
     * Gets the card-in-use message from the message manager.
     *
     * @param context The operating context.
     * @param packageIdentifier The identifier of the card package.
     * @return The card-in-use message from the message manager.
     */
    private String getWrongSpidMessage(final Context context, final String packageIdentifier, final int packageSpid,
            final int subscriberSpid)
    {
        final MessageMgr manager = new MessageMgr(context, this);

        final Object[] params = new Object[]{packageIdentifier, Integer.valueOf(packageSpid), Integer.valueOf(subscriberSpid)};
        return manager.get(WRONG_SPID_KEY, WRONG_SPID_DEFAULT, params);
    }

    /**
     * Provides the default value of the message to display when the card is not
     * available.
     */
    private static final String NOT_AVAILABLE_DEFAULT =
        "Card [{0}] is not available. Check the state of the Package.";

    /**
     * Provides the default value of the message to display when the card is not
     * found due to failure.
     */
    private static final String FAILURE_LOOKUP_DEFAULT =
        "Failed to locate card [{0}].";

    /**
     * Provides the default value of the message to display when the card is on a different SPID then Subscriber.
     */
    private static final String WRONG_SPID_DEFAULT =
        "Card [{0}] has Service Provider [{1}] while Subscriber is on Service Provider {2}.";

} // class
