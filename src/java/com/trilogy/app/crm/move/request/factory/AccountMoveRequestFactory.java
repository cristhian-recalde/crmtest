/*
 * This code is a protected work and subject to domestic and international
 * copyright law(s). A complete listing of authors of this work is readily 
 * available. Additionally, source code is, by its very nature, confidential 
 * information and inextricably contains trade secrets and other information 
 * proprietary, valuable and sensitive to Redknee, no unauthorised use, 
 * disclosure, manipulation or otherwise is permitted, and may only be used 
 * in accordance with the terms of the licence agreement entered into with 
 * Redknee Inc. and/or its subsidiaries.
 *
 * Copyright � Redknee Inc. and its subsidiaries. All Rights Reserved. 
 */
package com.trilogy.app.crm.move.request.factory;

import java.util.Collection;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.InfoLogMsg;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.core.SubscriptionType;
import com.trilogy.app.crm.move.MoveConstants;
import com.trilogy.app.crm.move.request.AccountMoveRequest;
import com.trilogy.app.crm.move.request.PostpaidServiceBasedSubscriberAccountMoveRequest;
import com.trilogy.app.crm.move.request.PrepaidPooledSubscriberAccountMoveRequest;


/**
 * Creates an appropriate instance of an AccountMoveRequest from a given Account.
 * 
 * If given account is null, then a default AccountMoveRequest is created.
 *
 * @author Aaron Gourley
 * @since 8.1
 */
class AccountMoveRequestFactory
{

    static AccountMoveRequest getInstance(Context ctx, Account account)
    {
        AccountMoveRequest request = null;
        
        if (account != null)
        {
            if (account.isIndividual(ctx))
            {
                if (account.isPrepaid())
                {
                    boolean isPooledMember = isPooledMember(ctx, account);
                    if (isPooledMember)
                    {
                        request = new PrepaidPooledSubscriberAccountMoveRequest(); 
                    }
                }
                else
                {
                    try
                    {
                        Collection<Subscriber> subscriptions = account.getSubscribers(ctx);
                        for(Subscriber subscription : subscriptions)
                        {
                            SubscriptionType subscriptionType = subscription.getSubscriptionType(ctx);
                            if (subscriptionType != null
                                    && subscriptionType.isService())
                            {
                                request = new PostpaidServiceBasedSubscriberAccountMoveRequest();
                                ((PostpaidServiceBasedSubscriberAccountMoveRequest) request)
                                        .setNewDepositAmount(subscription.getDeposit(ctx));
                                break;
                            }
                        }
                    }
                    catch (HomeException e)
                    {
                        new InfoLogMsg(AccountMoveRequestFactory.class, "Error looking up service-based subscriptions for account " + account.getBAN() + ".", e).log(ctx);
                    }
                }
            }
        }
        
        if (request == null)
        {
            request = new AccountMoveRequest();
        }
        
        if (account != null)
        {
            request.setExistingBAN(account);            
            
        }

        /*
         * Set the BAN to a unique temporary 'unset' value so that it will be set during AccountHome.create()
         * Hash code is used because it is unique and DEFAULT_MOVE_PREFIX + hashCode < Account.BAN_WIDTH
         */
        request.setNewBAN(MoveConstants.DEFAULT_MOVE_PREFIX + request.hashCode());
        request.setNewResponsible(account.isResponsible());
        
        return request;
    }

    
    private static boolean isPooledMember(Context ctx, Account account)
    {
        Account parent = null;
        try
        {
            parent = account.getParentAccount(ctx);
        }
        catch (HomeException e)
        {
        }
        return parent != null && parent.isPooled(ctx);
    }
}
