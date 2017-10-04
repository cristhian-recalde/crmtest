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
package com.trilogy.app.crm.contract;

import com.trilogy.app.crm.bean.IdentifierEnum;
import com.trilogy.app.crm.support.IdentifierSequenceSupportHelper;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;


/**
 * Sets the unique identifiers used by subscription contract term.
 * 
 * @author
 */
public class SubscriptionContractTermIdentifierSettingHome extends HomeProxy
{

    /**
     * Creates a new SubscriptionContractTermIdentifierSettingHome proxy.
     * 
     * @param delegate
     *            The Home to which we delegate.
     */
    public SubscriptionContractTermIdentifierSettingHome(final Home delegate)
    {
        super(delegate);
    }


    // INHERIT
    public Object create(final Context ctx, final Object bean) throws HomeException
    {
        final SubscriptionContractTerm contract = (SubscriptionContractTerm) bean;
        // Throws HomeException.
        final long identifier = getNextIdentifier(ctx);
        contract.setId(identifier);
        return super.create(ctx, contract);
    }


    /**
     * Gets the next available identifier.
     * 
     * @return The next available identifier.
     */
    private long getNextIdentifier(final Context ctx)
        throws HomeException
    {
        IdentifierSequenceSupportHelper.get(ctx).ensureSequenceExists(
            ctx,
            IdentifierEnum.SUBSCRIPTION_CONTRACT_TERM_ID,
            1,
            Long.MAX_VALUE);

        
        return IdentifierSequenceSupportHelper.get(ctx).getNextIdentifier(
            ctx,
            IdentifierEnum.SUBSCRIPTION_CONTRACT_TERM_ID,
            null);
    }
} // class
