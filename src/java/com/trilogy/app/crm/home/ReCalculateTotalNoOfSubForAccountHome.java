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
package com.trilogy.app.crm.home;

import com.trilogy.app.crm.account.AccountConstants;
import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.GroupTypeEnum;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xlog.log.LogSupport;

/*
 * This class has been added after we got a problem in setting TotalNumOfSubscriptions (TT#13011420022) for group account
 * after deactivating accounts via Cron Task named "Deactivates all IN_COLLECTION accounts after a configurable period of time".
 * Since the call flows through Account pipeline to Subscriber pipeline (Interlinked Pipelines!!)  where it updates the
 * group account for correct count but then it reverts to its original value while second update . We have set the counter in 
 * context in DeactivateInCollectionAccount and updated the value in AccountSubscriptionCountUpdateHome .
 * We will use the context value of count in this home and set it while account is updated  
 * 
 */
public class ReCalculateTotalNoOfSubForAccountHome extends HomeProxy
{

     
    /**
     * Create a new instance of <code>ReCalculateTotalNoOfSubForAccountHome</code>.
     *
     * @param ctx
     *            The operating context.
     * @param delegate
     *            Delegate of this home.
     */
    public ReCalculateTotalNoOfSubForAccountHome(final Context ctx, final Home delegate)
    {
        super(delegate);
        setContext(ctx);
    }



    /**
     * {@inheritDoc}
     */
    @Override
    public Object store(final Context ctx, final Object obj) throws HomeException
    {
        final Account newAccount = (Account) obj;
        if(!newAccount.isPrepaid())
        {
            if(newAccount.getGroupType().getIndex() ==  GroupTypeEnum.GROUP_INDEX || newAccount.getGroupType().getIndex() ==  GroupTypeEnum.GROUP_POOLED_INDEX)
            {
                Boolean recalculate = (Boolean) ctx.get(AccountConstants.DEACTIVATE_ACCOUNT_CRON_AGENT+newAccount.getBAN());
                if(recalculate != null && recalculate)
                {
                    int subCount = newAccount.getNonDeActiveSubscribers(ctx).size();
                    if(subCount >= 0)
                    {
                        newAccount.setTotalNumOfSubscriptions(subCount);
                    }
                    if(LogSupport.isDebugEnabled(ctx))
                    {
                        LogSupport.debug(ctx, this, "Setting Total Number Of Subscriptions for Group Account to "+subCount);
                    }
                }
            }
        }
        
        Object ret = null;
        ret = super.store(ctx, newAccount);
         
        return ret;
    }

}
