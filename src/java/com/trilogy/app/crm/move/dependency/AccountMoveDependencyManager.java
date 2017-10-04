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
package com.trilogy.app.crm.move.dependency;

import java.util.ArrayList;
import java.util.Collection;

import com.trilogy.framework.xhome.beans.SafetyUtil;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.NEQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountStateEnum;
import com.trilogy.app.crm.bean.AccountXInfo;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.bean.SubscriberXInfo;
import com.trilogy.app.crm.bean.account.SubscriptionType;
import com.trilogy.app.crm.move.MoveException;
import com.trilogy.app.crm.move.MoveRequest;
import com.trilogy.app.crm.move.request.AccountExtensionMoveRequest;
import com.trilogy.app.crm.move.request.AccountMoveRequest;
import com.trilogy.app.crm.move.request.ConvertSubscriptionBillingTypeRequest;
import com.trilogy.app.crm.move.request.PostpaidServiceBasedSubscriberAccountMoveRequest;
import com.trilogy.app.crm.move.request.PrepaidPooledSubscriberAccountMoveRequest;
import com.trilogy.app.crm.move.request.ServiceBasedSubscriptionMoveRequest;
import com.trilogy.app.crm.move.request.SubscriptionMoveRequest;
import com.trilogy.app.crm.move.support.MoveRequestSupport;
import com.trilogy.app.crm.support.AccountSupport;
import com.trilogy.app.crm.support.HomeSupportHelper;


/**
 * Given an account move request, this class calculates its dependencies.
 * These dependencies include child accounts and subscriptions.
 *
 * @author Aaron Gourley
 * @since 8.1
 */
public class AccountMoveDependencyManager<AMR extends AccountMoveRequest> extends AbstractMoveDependencyManager<AMR>
{
    public AccountMoveDependencyManager(Context ctx, AMR srcRequest)
    {
        super(ctx, srcRequest);
    }

    @Override
    protected Collection<? extends MoveRequest> getDependencyRequests(Context ctx, AMR request) throws MoveException
    {
        Collection<MoveRequest> dependencies = new ArrayList<MoveRequest>();

        Account newAccount = request.getNewAccount(ctx);
        if (newAccount != null)
        {
            /*
             * We can only recursively validate or move if the new BAN is set
             * because the dependencies need to know where they are moving!
             */
            Account oldAccount = request.getOldAccount(ctx);
            if(oldAccount != null)
            {
                dependencies.addAll(
                        getExtensionDependencies(
                                ctx,
                                request));

                dependencies.addAll(
                        getSubscriptionDependencies(
                                ctx,
                                request));

                dependencies.addAll(
                        getChildAccountDependencies(
                                ctx, 
                                request));
            }
        }
        
        return dependencies;
    }

    
    protected MoveRequest getMoveRequestForChildAccount(Context ctx, AMR srcRequest, Account childAccount)
    {
        return MoveRequestSupport.getMoveRequest(ctx, childAccount);
    }

    
    protected MoveRequest getMoveRequestForSubscription(Context ctx, AMR srcRequest, Subscriber subscription)
    {
        return MoveRequestSupport.getMoveRequest(ctx, subscription);
    }

    
    protected MoveRequest getMoveRequestForExtensions(Context ctx, AMR srcRequest)
    {
        // Don't pass the frozen original account to the move request factory.
        // It may want to store it as the non-frozen "old" account and create
        // its own frozen original copy.
        return MoveRequestSupport.getMoveRequest(ctx, srcRequest.getOldAccount(ctx), AccountExtensionMoveRequest.class);
    }

    
    private Collection<? extends MoveRequest> getChildAccountDependencies(Context ctx, AMR srcRequest) throws MoveException
    {
        Collection<MoveRequest> childAccountDependencies = new ArrayList<MoveRequest>();

        Collection<Account> childAccounts = null;
        try
        {
            Home childAccountHome = AccountSupport.getImmediateChildrenAccountHome(ctx, srcRequest.getExistingBAN());
            childAccountHome = childAccountHome.where(ctx, new NEQ(AccountXInfo.STATE, AccountStateEnum.INACTIVE));
            childAccounts = childAccountHome.selectAll(ctx);
        }
        catch (HomeException e)
        {
            throw new MoveException(getSourceRequest(), "Error occurred retrieving accounts with parent BAN " + srcRequest.getExistingBAN() + ".  Unable to calculate move dependencies.", e);
        }
        
        for(Account childAccount : childAccounts)
        {            
            MoveRequest dependency = getMoveRequestForChildAccount(ctx, srcRequest, childAccount);

            if (dependency instanceof AccountMoveRequest)
            {
                AccountMoveRequest accountRequest = (AccountMoveRequest) dependency;
                accountRequest.setNewParentBAN(srcRequest.getNewAccount(ctx));
            }
            
            if (dependency != null)
            {
                childAccountDependencies.add(dependency);   
            }
        }
        
        return childAccountDependencies;
    }

    
    private Collection<? extends MoveRequest> getExtensionDependencies(Context ctx, AMR srcRequest)
    {
        Collection<MoveRequest> extensionDependencies = new ArrayList<MoveRequest>();
        
        MoveRequest dependency = getMoveRequestForExtensions(ctx, srcRequest);

        if (dependency instanceof AccountExtensionMoveRequest)
        {
            AccountExtensionMoveRequest extRequest = (AccountExtensionMoveRequest) dependency;
            
            extRequest.setNewBAN(srcRequest.getNewAccount(ctx));
        }
        
        if (dependency != null)
        {
            extensionDependencies.add(dependency);   
        }
        
        return extensionDependencies;
    }
    
    
    private Collection<? extends MoveRequest> getSubscriptionDependencies(Context ctx, AMR srcRequest) throws MoveException
    {
        Collection<MoveRequest> subscriptionDependencies = new ArrayList<MoveRequest>();

        Collection<Subscriber> subscriptions = null;
        try
        {
            And filter = new And();
            filter.add(new EQ(SubscriberXInfo.BAN, srcRequest.getExistingBAN()));
            filter.add(new NEQ(SubscriberXInfo.STATE, SubscriberStateEnum.INACTIVE));
            subscriptions = HomeSupportHelper.get(ctx).getBeans(ctx, Subscriber.class, filter);
        }
        catch (HomeException e)
        {
            throw new MoveException(getSourceRequest(), "Error occurred retrieving subscriptions with BAN " + srcRequest.getExistingBAN() + ".  Unable to calculate move dependencies.", e);
        }
        
        for(Subscriber subscription : subscriptions)
        {
            MoveRequest dependency = getMoveRequestForSubscription(ctx, srcRequest, subscription);
            
            if (dependency instanceof SubscriptionMoveRequest)
            {
                SubscriptionMoveRequest subRequest = (SubscriptionMoveRequest) dependency;
                subRequest.setNewBAN(srcRequest.getNewAccount(ctx));

                if (SafetyUtil.safeEquals(srcRequest.getExistingBAN(), srcRequest.getNewBAN()))
                {
                    // Account is not changing, so update the subscription in place.
                    subRequest.setNewSubscriptionId(subscription);
                }
                
                SubscriptionType subscriptionType = subscription.getSubscriptionType(ctx);
                if (subscriptionType == null)
                {
                    throw new MoveException(getSourceRequest(), "Error occurred retrieving subscription type for subscription " + subscription.getId() + ".  Unable to calculate move dependencies.", null);
                }
                
                // If subRequest is a convert subscription billing type request, don't overwrite the expiryExtension nor newDepositAmount values, as they 
                // have alrady been set by the getMoveRequestForSubscription() method.
                if (subRequest instanceof ServiceBasedSubscriptionMoveRequest && !(subRequest instanceof ConvertSubscriptionBillingTypeRequest))
                {
                    ServiceBasedSubscriptionMoveRequest serviceBasedRequest = (ServiceBasedSubscriptionMoveRequest)subRequest;

                    int expiryExtension = PrepaidPooledSubscriberAccountMoveRequest.DEFAULT_EXPIRYEXTENSION;
                    long newDepositAmount = PostpaidServiceBasedSubscriberAccountMoveRequest.DEFAULT_NEWDEPOSITAMOUNT;
                    if (srcRequest instanceof PrepaidPooledSubscriberAccountMoveRequest)
                    {
                        expiryExtension = ((PrepaidPooledSubscriberAccountMoveRequest)srcRequest).getExpiryExtension();
                    }
                    if (srcRequest instanceof PostpaidServiceBasedSubscriberAccountMoveRequest)
                    {
                        newDepositAmount = ((PostpaidServiceBasedSubscriberAccountMoveRequest)srcRequest).getNewDepositAmount();
                    }
                    
                    serviceBasedRequest.setExpiryExtension(expiryExtension);   
                    serviceBasedRequest.setNewDepositAmount(newDepositAmount);
                }
            }
            
            if (dependency != null)
            {
                subscriptionDependencies.add(dependency);   
            }
        }
        
        return subscriptionDependencies;
    }
}
