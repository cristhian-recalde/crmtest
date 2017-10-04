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
package com.trilogy.app.crm.web.control;

import com.trilogy.app.crm.bean.SubscriptionTypeAware;
import com.trilogy.app.crm.bean.core.SubscriptionType;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.filter.Predicate;
import com.trilogy.framework.xhome.visitor.AbortVisitException;
import com.trilogy.framework.xlog.log.LogSupport;


/**
 * Determines whether the SubscriptionTypeAware entity is of type Wallet
 * 
 * @author simar.singh@redknee.com
 */
public class IsSubscriptionTypeWallet implements Predicate
{

    /**
     * 
     */
    private static final long serialVersionUID = 1L;


    public boolean f(final Context ctx, final Object obj) throws AbortVisitException
    {
        if (!(obj instanceof SubscriptionTypeAware))
        {
            LogSupport.debug(ctx, this, "Only ServiceTypeEnum is supported", new RuntimeException(
                    "Only ServiceTypeEnum is supported"));
            return false;
        }
        final SubscriptionTypeAware typeAwareBean = (SubscriptionTypeAware) obj;
        final SubscriptionType subscriptionType = typeAwareBean.getSubscriptionType(ctx);
        if (null == subscriptionType)
        {
            LogSupport.debug(ctx, this, "Can not work on null Subscription Types", new RuntimeException(
                    "Can not work on null Subscription Types"));
            return false;
        }
        return subscriptionType.isWallet();
    }


    /**
     * Provides access to a singleton instance of this class.
     * 
     * @return A singleton instance of this class.
     */
    public static IsSubscriptionTypeWallet getInstance()
    {
        return instance_;
    }

    /**
     * A singleton instance of this class.
     */
    private static final IsSubscriptionTypeWallet instance_ = new IsSubscriptionTypeWallet();
} // class