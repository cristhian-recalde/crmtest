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
package com.trilogy.app.crm.filter;

import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.filter.Predicate;
import com.trilogy.framework.xhome.visitor.AbortVisitException;

/**
 * Calls the evaluators to verify if this enum can be displayed or not 
 * @author arturo.medina@redknee.com
 *
 */
public class SubscriberTypeEnumPredicate implements Predicate
{
    /**
     * Sets the evaluators to delegate if the enum can be displayed
     */
    public SubscriberTypeEnumPredicate()
    {
        delegate_ = new SubscriptionTypeEnumPredicateEvaluator(
                        new SubscriberTypeInPooledAccountEnumPredicateEvaluator(
                                new AccountSystemTypeEnumPredicateEvaluator(null)));
    }
    
    /**
     * {@inheritDoc}
     */
    public boolean f(Context ctx, Object obj) throws AbortVisitException
    {
        if (obj == null || !(obj instanceof SubscriberTypeEnum))
        {
            return false;
        }
        
        SubscriberTypeEnum sType = (SubscriberTypeEnum)obj;
        
        if (sType == SubscriberTypeEnum.HYBRID)
        {
            return false;
        }

        return delegate_.evaluate(ctx, sType);
    }

    /**
     * 
     */
    private static final long serialVersionUID = -1227031200205791772L;
    private EnumPredicateEvaluator delegate_;
}
