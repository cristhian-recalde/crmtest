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
package com.trilogy.app.crm.support;

import java.util.Collection;
import java.util.Set;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.In;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.LogSupport;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountXInfo;
import com.trilogy.app.crm.bean.GroupTypeEnum;
import com.trilogy.app.crm.bean.Subscriber;

/**
 * Support methods to navigate the Account topology newly introduced with the implementation of
 * Mobile Money (Group Accounts, Subscriber Accounts and Subscriptions).  
 * 
 * In previous versions of CRM we could find the directly topology with respect to a given Group Account.
 * As of CRM 8.0, this task requires us to sift through the Account information and determine if the 
 * Account is a Group or a Subscriber Account.
 * 
 * The methods in this class heavily rely on the following rules over Account Hierarchy (restrictions
 * over Account creation):
 *   + Individual Accounts cannot be assigned as Parent Accounts.  Only Group Accounts and 
 *   Group Pooled Accounts can be named as Parent Accounts.
 *   + Group Accounts cannot have Subscriptions
 * 
 * This means that:
 * 1) given a Subscription we can know directly the Individual Subscriber Account
 * associated with the Subscription (Subscriber.getBAN()).  Consequently, the Group Account Id is the 
 * parent Id of the Subscriber Account.
 * 2) given a Subscriber Account Identifier, we can find the Subscriber Accounts within the same
 * Group Account (by gathering all Subscriber accounts with the same Parent ID).
 *  
 * @author angie.li
 *
 */
public class TopologySupport 
{

    /**
     * Returns the Group Account Identifier for the given Subscription.
     * Read the Javadoc comments for this class for the assumptions made for this search.
     * @param ctx
     * @param subscription
     * @return  Could return Null if errors while searching for Group Account 
     */
    public static String findGroupAccountId(Context ctx, Subscriber subscription)
    {
        String result = null;
        try
        {
            Account subscriberAccount = AccountSupport.getAccount(ctx, subscription.getBAN());
            Account groupAccount = AccountSupport.getAccount(ctx, subscriberAccount.getParentBAN());
            if (groupAccount.isIndividual(ctx))
            {
                if (LogSupport.isDebugEnabled(ctx))
                {
                    StringBuilder msg = new StringBuilder();
                    msg.append("findGroupAccountId found BAN ");
                    msg.append(groupAccount.getBAN());
                    msg.append(" as the Group Account of Subscriber Account ");
                    msg.append(subscriberAccount.getBAN());
                    msg.append(", but this Account is an Individual Account!  Returning NULL as a result.");
                    LogSupport.debug(ctx, TopologySupport.class.getName(), msg.toString());
                }
            }
        }
        catch (Exception e)
        {
            if (LogSupport.isDebugEnabled(ctx))
            {
                StringBuilder msg = new StringBuilder();
                msg.append("Errors encountered while trying to seach for the Group Account of Subscriber Account ");
                msg.append(subscription.getBAN());
                msg.append(". ");
                msg.append(e.getMessage());
                LogSupport.debug(ctx, TopologySupport.class.getName(), msg.toString());
            }
        }
        return result;
    }
    
    
    /**
     * Although Responsible Subscriber Account (A) has the same Parent ID as Non-Responsible Subscriber
     * Account (B), they are not in the same Billable Group Account.  The reason is the Responsible 
     * Subscriber Account (A) is responsible for its own balances while Subscriber Account (B) is not.
     * 
     * @param ctx
     * @param leafAccount
     * @return
     */
    public static Collection<Account> getSubscriberAccountsInSameBillableGroupAccount(Context ctx, Account leafAccount)
        throws HomeException
    {
        String groupAccountId = leafAccount.getParentBAN();
        //Get all Subscriber Account Types
        //filter the home with IN (Account Types)
        Home home = AccountSupport.getImmediateNonResponsibleChildrenAccountHome(ctx, groupAccountId);
        home = home.where(ctx, new EQ(AccountXInfo.GROUP_TYPE, GroupTypeEnum.SUBSCRIBER));
        Collection<Account> col = home.selectAll();
        return col;
    }
}
