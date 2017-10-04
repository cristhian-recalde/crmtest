/*
 * This code is a protected work and subject to domestic and international
 * copyright law(s). A complete listing of authors of this work is readily
 * available. Additionally, source code is, by its very nature, confidential
 * information and inextricably contains trade secrets and other information
 * proprietary, valuable and sensitive to Redknee. No unauthorized use,
 * disclosure, manipulation or otherwise is permitted, and may only be used in
 * accordance with the terms of the license agreement entered into with Redknee
 * Inc. and/or its subsidiaries.
 * 
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */

package com.trilogy.app.crm.filter;

import com.trilogy.app.crm.bean.KeyConfiguration;
import com.trilogy.app.crm.bean.KeyValueFeatureEnum;
import com.trilogy.app.crm.calculator.SubscriberStateValueCalculator;
import com.trilogy.app.crm.calculator.TransferDisputeSubscriberValueCalculator;
import com.trilogy.app.crm.support.BeanLoaderSupportHelper;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.entity.EntityInfo;
import com.trilogy.framework.xhome.filter.Predicate;
import com.trilogy.framework.xhome.visitor.AbortVisitException;
import com.trilogy.framework.xlog.log.MinorLogMsg;

/**
 * Predicate to filter ValueCalculator by feature.
 * 
 * @author cindy.wong@redknee.com
 * @since 8.3
 */
public class ValueCalculatorByFeaturePredicate implements Predicate
{

    private static final long serialVersionUID = 1L;

    /**
     * @see com.redknee.framework.xhome.filter.Predicate#f(com.redknee.framework.xhome.context.Context,
     *      java.lang.Object)
     */
    @Override
    public boolean f(Context ctx, Object obj) throws AbortVisitException
    {
        KeyConfiguration keyConfig = BeanLoaderSupportHelper.get(ctx).getBean(ctx, KeyConfiguration.class);
        
        EntityInfo entity = (EntityInfo) obj;
        Class cls = null;
        try
        {
            cls = Class.forName(entity.getClassName());
        }
        catch (ClassNotFoundException exception)
        {
            new MinorLogMsg(this, "Cannot find class " + entity.getClassName(),
                    exception).log(ctx);
        }
        if (SubscriberStateValueCalculator.class.equals(cls))
        {
            return KeyValueFeatureEnum.STATE_CHANGE_EMAIL.equals(keyConfig
                    .getFeature());
        }

        if (TransferDisputeSubscriberValueCalculator.class.equals(cls))
        {
            return KeyValueFeatureEnum.TRANSFER_DISPUTE_EMAIL.equals(keyConfig
                    .getFeature());
        }
        return true;
    }

}
