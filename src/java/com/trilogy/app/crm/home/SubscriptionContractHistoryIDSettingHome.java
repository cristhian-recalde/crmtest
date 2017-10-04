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

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xlog.log.LogSupport;

import com.trilogy.app.crm.bean.IdentifierEnum;
import com.trilogy.app.crm.contract.SubscriptionContractHistory;
import com.trilogy.app.crm.support.IdentifierSequenceSupportHelper;

/**
 * 
 * Sets primary key for this SubscriptionContractHistory using an
 * IdentifierSequence.
 * 
 * @author alekurwale
 *
 */
public class SubscriptionContractHistoryIDSettingHome extends HomeProxy 
{
    private static final long startValue = 100000;
    
    public SubscriptionContractHistoryIDSettingHome(Context ctx, Home delegate)
    {
        super(ctx, delegate);
    }

    @Override
    public Object create(Context ctx, Object obj) throws HomeException,
            HomeInternalException 
    {
        
    	SubscriptionContractHistory subscriptionContractHistory = (SubscriptionContractHistory)obj;
        
        IdentifierSequenceSupportHelper.get(ctx).ensureSequenceExists(ctx, IdentifierEnum.SUBSCRIPTION_CONTRACT_HISTORY_ID,
                startValue, Long.MAX_VALUE);
        
        long recordID = IdentifierSequenceSupportHelper.get(ctx).getNextIdentifier(
                ctx,
                IdentifierEnum.SUBSCRIPTION_CONTRACT_HISTORY_ID,
                null);
        
        if(subscriptionContractHistory.getIdentifier() == 0)
        {
        	subscriptionContractHistory.setIdentifier(recordID);
        }
        
        LogSupport.info(ctx, this, "SubscriptionContractHistory ID set to: " + subscriptionContractHistory.getIdentifier());
        
        return super.create(ctx, obj);
    }

    
}
