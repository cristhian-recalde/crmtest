/*
 * This code is a protected work and subject to domestic and international copyright
 * law(s). A complete listing of authors of this work is readily available. Additionally,
 * source code is, by its very nature, confidential information and inextricably contains
 * trade secrets and other information proprietary, valuable and sensitive to Redknee. No
 * unauthorized use, disclosure, manipulation or otherwise is permitted, and may only be
 * used in accordance with the terms of the license agreement entered into with Redknee
 * Inc. and/or its subsidiaries.
 * 
 * Copyright � Redknee Inc. and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.home.sub;

import java.util.Collection;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountCategory;
import com.trilogy.app.crm.bean.AccountCategoryHome;
import com.trilogy.app.crm.bean.AccountCategoryXInfo;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberXInfo;
import com.trilogy.app.crm.exception.RethrowExceptionListener;
import com.trilogy.app.crm.extension.account.FriendsAndFamilyExtension;
import com.trilogy.app.crm.extension.account.FriendsAndFamilyExtensionHome;
import com.trilogy.app.crm.support.Lookup;
import com.trilogy.app.crm.support.SubscriberSupport;
import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.beans.Validator;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeOperationEnum;
import com.trilogy.framework.xlog.log.MinorLogMsg;


/**
 * Ensure that a subscription getting de-activatd or put in dormant state does not have
 * any disputes
 * 
 * @author simar.singh@redknee.com
 */
public final class DeactivateGroupLeaderValidator implements Validator
{

    /**
     * Create a new instance of <code>DeactivateGroupLeaderValidator</code>.
     */
    protected DeactivateGroupLeaderValidator()
    {
        // empty
    }


    /**
     * Returns an instance of <code>DeactivateGroupLeaderValidator</code>.
     * 
     * @return An instance of <code>DeactivateGroupLeaderValidator</code>.
     */
    public static DeactivateGroupLeaderValidator instance()
    {
        if (instance == null)
        {
            instance = new DeactivateGroupLeaderValidator();
        }
        return instance;
    }


    /**
     * {@inheritDoc}
     */
    public void validate(final Context ctx, final Object obj)
    {
        if (!HomeOperationEnum.STORE.equals(ctx.get(HomeOperationEnum.class)))
        {
            // validating only store() operations
            return;
        }
        final Subscriber sub = (Subscriber) obj;
        final Subscriber oldSub = (Subscriber) ctx.get(Lookup.OLDSUBSCRIBER);
        
        final RethrowExceptionListener exceptions = new RethrowExceptionListener();
        if (sub.isInFinalState() && oldSub != null && !oldSub.isInFinalState() && oldSub.isPostpaid())
        {

            Collection<Subscriber> collSub = null;
            Home home = (Home) ctx.get(AccountCategoryHome.class);
            AccountCategory accountCategory = null;
            Account oldParentAccount = null;
            String cugOwner = null;
            try
            {
                Account oldAccount = oldSub.getAccount(ctx);
                
                if (oldAccount != null)
                {
                    oldParentAccount = oldAccount.getParentAccount(ctx);
                }
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
                    if (collSub.size() > 0)
                    {
                        for (Subscriber subscriber : collSub)
                        {
                            if (subscriber.getBAN().equals(oldSub.getBAN()))
                            {
                                final IllegalStateException ex;
                                ex = new IllegalStateException("Cannot deactivate subscriber as it is group leader for account : " + oldParentAccount.getBAN());
                                exceptions.thrown(ex);
                            }
                        }
                    }
                }
            }
            catch (HomeException e)
            {
                new MinorLogMsg(this, "Error occurred validating moving of pooled group leader " + oldSub + ": " + e.getMessage(), e).log(ctx);
            }
        
        }
        exceptions.throwAllAsCompoundException();
    }
    
    /**
     * Singleton instance.
     */ 
    private static DeactivateGroupLeaderValidator instance;
}