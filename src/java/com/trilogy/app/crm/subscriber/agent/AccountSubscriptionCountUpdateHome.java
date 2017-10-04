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
package com.trilogy.app.crm.subscriber.agent;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.CreditCategory;
import com.trilogy.app.crm.bean.GroupTypeEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.app.crm.support.Lookup;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MinorLogMsg;


/**
 * This home updates the number of total non-deactive postpaid subscription a group account
 * has under it
 * 
 * @author bdhavalshankh
 * @since 9.5.1
 */
public class AccountSubscriptionCountUpdateHome extends HomeProxy
{

    public AccountSubscriptionCountUpdateHome(Context ctx, Home delegate)
    {
        super(ctx, delegate);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Object create(Context ctx, Object obj) throws HomeException, HomeInternalException
    {
        Subscriber newSub = (Subscriber) super.create(ctx, obj);
        Account newAccount = newSub.getAccount(ctx);
        if(!newAccount.isResponsible() && !(newSub.getState().getIndex() == SubscriberStateEnum.INACTIVE_INDEX))
        {
            int cnt = getCounterValue(newSub, null);
            if(cnt != 0)
            {
                updateTotalSubscriptionCounter(ctx, newSub, cnt);
            }
        }
        return newSub;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Object store(Context ctx, Object obj) throws HomeException, HomeInternalException
    {
        Subscriber newSub = (Subscriber) super.store(ctx, obj);
        Subscriber oldSub = (Subscriber) ctx.get(Lookup.OLDSUBSCRIBER);
        Account account = oldSub.getAccount(ctx);
        
        if(oldSub.getBAN().equals(newSub.getBAN()) && oldSub.getState() != newSub.getState())
        {
            if(!account.isResponsible())
            {
                int cnt = getCounterValue(newSub, oldSub);
                if(cnt != 0)
                {
                    updateTotalSubscriptionCounter(ctx, oldSub, cnt);
                }
            }
        }
        return newSub;
    }


    private int getCounterValue(Subscriber sub, Subscriber oldSub)
    {
        if(oldSub != null)
        {
            if(sub.getState().getIndex() == SubscriberStateEnum.INACTIVE_INDEX)
            {
                return -1;
            }
            else
            {
                return 0;
            }
        }
        else
        {
            return 1;
        }
    }

    /*
     * This methods Updates the counter of group account which keeps a track of number of subscriptions added in the group till now.
     * If a subscriptions is deactivated, we will decrement the counter by 1
     * Adding a subscription will increment the counter by 1
     * Moving a subscription from account1 to account2 will decrement and increment the counter respectively . 
     *@param ctx Context
     *@param sub Subscription reference
     *@counterValue It may be 1 or -1
     *   
     */
    
    public void updateTotalSubscriptionCounter(Context ctx, Subscriber sub, int counterValue)
    {
        Account responsibleParentAcct = null;
        try
        {
            Account parentAccount = sub.getAccount(ctx);
            responsibleParentAcct = parentAccount.getResponsibleParentAccount(ctx);
        }
        catch (HomeException e)
        {
            new MinorLogMsg(this, "Failed to load Responsible parent account for " + sub.getBAN(), e).log(ctx);
        }
        
        if (responsibleParentAcct != null && responsibleParentAcct.isResponsible() && (responsibleParentAcct.getGroupType().getIndex() == GroupTypeEnum.GROUP_INDEX || responsibleParentAcct.getGroupType().getIndex() == GroupTypeEnum.GROUP_POOLED_INDEX))
        {
            if(responsibleParentAcct.isPostpaid() || responsibleParentAcct.isHybrid())
            {
                try
                {
                    CreditCategory groupCc = HomeSupportHelper.get(ctx).findBean(ctx, CreditCategory.class,
                            responsibleParentAcct.getCreditCategory());
                
                    if(groupCc != null)
                    {
                        int maxSubscriptionAllowed = groupCc.getMaxSubscriptionsAllowed();
                        if(maxSubscriptionAllowed > 0)
                        {
                            int subscriptionCnt = 0, tmp = 0;
                            tmp = responsibleParentAcct.getTotalNumOfSubscriptions();
                            
                            subscriptionCnt =  tmp + (counterValue);
                            if(subscriptionCnt >= 0)
                            {
                                responsibleParentAcct.setTotalNumOfSubscriptions(subscriptionCnt);
                                Account account = null;
                                    account = HomeSupportHelper.get(ctx).storeBean(ctx, responsibleParentAcct);
                                    LogSupport.info(ctx, this, "Total number of Non Deactivated subscriptions for Account with BAN : "+account.getBAN()+" has been updated to : "+subscriptionCnt);
                                
                            }
                        }
                    }
                }
                catch (HomeException e)
                {
                    LogSupport.minor(ctx, this, "Could not update account due to : "+e.getMessage());
                }
            }
        }
    }
    
}