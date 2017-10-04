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
package com.trilogy.app.crm.home.validator;

import com.trilogy.framework.xhome.beans.AbstractBean;
import com.trilogy.framework.xhome.beans.Identifiable;
import com.trilogy.framework.xhome.beans.Validator;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeOperationEnum;
import com.trilogy.framework.xlog.log.DebugLogMsg;

import com.trilogy.app.crm.state.FinalStateAware;
import com.trilogy.app.crm.support.HomeSupportHelper;


/**
 * This validator prevents beans from being updated when they
 * are in a final state.
 *
 * @author aaron.gourley@redknee.com
 * @since 8.2
 */
public class FinalStateBeanUpdateValidator implements Validator
{
    private static Validator instance_ = null;
    public static Validator instance()
    {
        if (instance_ == null)
        {
            instance_ = new FinalStateBeanUpdateValidator();
        }
        return instance_;
    }
    
    protected FinalStateBeanUpdateValidator()
    {
    }

    /**
     * @{inheritDoc}
     */
    @Override
    public void validate(Context ctx, Object obj) throws IllegalStateException
    {
        if (obj instanceof FinalStateAware
                && obj instanceof Identifiable
                && obj instanceof AbstractBean
                && HomeOperationEnum.STORE.equals(ctx.get(HomeOperationEnum.class)))
        {
            Object oldObj = null;

            Object id = ((Identifiable)obj).ID();
            try
            {
                oldObj = HomeSupportHelper.get(ctx).findBean(ctx, ((AbstractBean)obj).getClass(), id);
            }
            catch (HomeException he)
            {
                new DebugLogMsg(this, "Failed to retrieve old copy of " + obj.getClass().getName() + " with key=" + id, he).log(ctx);
            }

            if (oldObj instanceof FinalStateAware)
            {
                FinalStateAware oldBean = (FinalStateAware) oldObj;

                if(oldBean.isInFinalState())
                {
                    throw new IllegalStateException("Illegal update attempt to entity that is in a final state (" + oldBean.getAbstractState() + ")");
                }
            }
        }
    }

}
