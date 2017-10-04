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
package com.trilogy.app.crm.move.support;

import java.util.Collection;

import com.trilogy.framework.xhome.beans.ExceptionListener;
import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.MinorLogMsg;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountCategory;
import com.trilogy.app.crm.bean.AccountCategoryHome;
import com.trilogy.app.crm.bean.AccountCategoryXInfo;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.extension.account.FriendsAndFamilyExtension;
import com.trilogy.app.crm.extension.account.FriendsAndFamilyExtensionHome;
import com.trilogy.app.crm.move.MoveConstants;
import com.trilogy.app.crm.move.request.SubscriptionMoveRequest;
import com.trilogy.app.crm.move.request.SubscriptionMoveRequestXInfo;
import com.trilogy.app.crm.support.SubscriberSupport;


/**
 * 
 *
 * @author aaron.gourley@redknee.com
 * @since 8.2
 */
public class SubscriptionMoveValidationSupport
{
    public static Subscriber validateOldSubscriptionExists(Context ctx, SubscriptionMoveRequest request, ExceptionListener el)
    {
        Subscriber oldSubscription = request.getOldSubscription(ctx);
        Subscriber originalSubscription = request.getOriginalSubscription(ctx);
        if (oldSubscription == null || originalSubscription == null)
        {
            el.thrown(new IllegalPropertyArgumentException(
                    SubscriptionMoveRequestXInfo.OLD_SUBSCRIPTION_ID, 
                    "Subscription (ID=" + request.getOldSubscriptionId() + ") does not exist."));
        }
        return originalSubscription;
    }

    public static Subscriber validateNewSubscriptionExists(Context ctx, SubscriptionMoveRequest request, ExceptionListener el)
    {
        Subscriber newSubscription = request.getNewSubscription(ctx);
        if (newSubscription == null)
        {
            final String infoString;
            if (request.getNewSubscriptionId() == null || request.getNewSubscriptionId().startsWith(MoveConstants.DEFAULT_MOVE_PREFIX))
            {
                infoString = "copy of " + request.getOldSubscriptionId(); 
            }
            else
            {
                infoString = "ID=" + request.getNewSubscriptionId();
            }
            el.thrown(new IllegalPropertyArgumentException(
                    SubscriptionMoveRequestXInfo.NEW_SUBSCRIPTION_ID, 
                    "New subscription (" + infoString + ") does not exist."));
        }
        return newSubscription;
    }

    public static Account validateOldAccountExists(Context ctx, SubscriptionMoveRequest request, ExceptionListener el)
    {
        Account oldAccount = request.getOldAccount(ctx);
        if (oldAccount == null)
        {
            Subscriber originalSubscription = request.getOriginalSubscription(ctx);
            if (originalSubscription != null)
            {
                el.thrown(new IllegalPropertyArgumentException(
                        SubscriptionMoveRequestXInfo.OLD_SUBSCRIPTION_ID, 
                        "Subscription's (ID=" + request.getOldSubscriptionId() + ") account (BAN=" + originalSubscription.getBAN() + ") does not exist.")); 
            }
            else
            {
                el.thrown(new IllegalPropertyArgumentException(
                        SubscriptionMoveRequestXInfo.OLD_SUBSCRIPTION_ID, 
                        "Old account could not be found for subscription " + request.getOldSubscriptionId() + "."));
            }
            
        }
        return oldAccount;
    }
    
    public static Account validateNewAccountExists(Context ctx, SubscriptionMoveRequest request, ExceptionListener el)
    {
        Account newAccount = request.getNewAccount(ctx);
        if (newAccount == null)
        {
            final String infoString;
            if (request.getNewBAN() == null || request.getNewBAN().startsWith(MoveConstants.DEFAULT_MOVE_PREFIX))
            {
                Subscriber originalSubscription = request.getOriginalSubscription(ctx);
                if (originalSubscription != null)
                {
                    infoString = "copy of " + originalSubscription.getBAN();
                }
                else
                {
                    infoString = "BAN=<N/A>";
                }
            }
            else
            {
                infoString = "BAN=" + request.getNewBAN();
            }
            el.thrown(new IllegalPropertyArgumentException(
                    SubscriptionMoveRequestXInfo.NEW_BAN, 
                    "New account (" + infoString + ") does not exist."));
        }
        return newAccount;
    }
    
    public static Subscriber validateAllowMoveGroupLeader(Context ctx, SubscriptionMoveRequest request, ExceptionListener el)
    {
        Subscriber oldSubscription = request.getOldSubscription(ctx);
        Account oldAccount = request.getOldAccount(ctx);
        Collection<Subscriber> collSub = null;
        Home home = (Home) ctx.get(AccountCategoryHome.class);
        AccountCategory accountCategory = null;
        Account oldParentAccount = null;
        String cugOwner = null;
        try
        {
            oldParentAccount = oldAccount.getParentAccount(ctx);
            if (oldParentAccount != null)
            {
                accountCategory = (AccountCategory) home.find(ctx, new EQ(AccountCategoryXInfo.IDENTIFIER,
                        oldParentAccount.getType()));
            }
            if (accountCategory != null && !accountCategory.isMoveGroupLeader())
            {
                final Home friendsAndFamilyExtensionHome = (Home) ctx.get(FriendsAndFamilyExtensionHome.class);
                FriendsAndFamilyExtension friendsAndFamilyExtension = (FriendsAndFamilyExtension) friendsAndFamilyExtensionHome
                        .find(ctx, oldParentAccount.getBAN());
                if(friendsAndFamilyExtension != null)
                {
                    cugOwner = friendsAndFamilyExtension.getCugOwnerMsisdn();
                }
                else
                {
                    cugOwner = oldParentAccount.getOwnerMSISDN();
                }
                if (cugOwner != null)
                {
                    collSub = SubscriberSupport.getSubscriptionsByMSISDN(ctx, cugOwner);
                }
                if (collSub != null && collSub.size() > 0)
                {
                    for (Subscriber sub : collSub)
                    {
                        if (sub.getBAN().equals(oldSubscription.getBAN()))
                        {
                            el.thrown(new IllegalPropertyArgumentException(
                                    SubscriptionMoveRequestXInfo.OLD_SUBSCRIPTION_ID,
                                    "Cannot move subscriber as it is group leader."));
                        }
                    }
                }
            }
        }
        catch (HomeException e)
        {
            new MinorLogMsg(SubscriptionMoveValidationSupport.class, "Error occurred validating moving of pooled group leader " + oldSubscription + ": " + e.getMessage(), e).log(ctx);
        }
        return oldSubscription;
    }
}
