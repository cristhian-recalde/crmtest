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
import com.trilogy.framework.xlog.log.DebugLogMsg;

import com.trilogy.app.crm.state.FinalStateAware;
import com.trilogy.app.crm.support.EnumStateSupportHelper;
import com.trilogy.app.crm.support.HomeSupportHelper;


/**
 * This validator prevents beans from being updated such that they
 * being transitioned out of a final state.
 *
 * @author aaron.gourley@redknee.com
 * @since 8.2
 */
public class FinalStateChangeBlockingValidator implements Validator
{
    private static Validator instance_ = null;
    public static Validator instance()
    {
        if (instance_ == null)
        {
            instance_ = new FinalStateChangeBlockingValidator();
        }
        return instance_;
    }
    
    protected FinalStateChangeBlockingValidator()
    {
    }

    /**
     * @{inheritDoc}
     */
    @Override
    public void validate(Context ctx, Object obj) throws IllegalStateException
    {
        if (obj instanceof FinalStateAware
                && !((FinalStateAware) obj).isInFinalState())
        {
            if (obj instanceof Identifiable
                && obj instanceof AbstractBean)
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
                    FinalStateAware bean = (FinalStateAware) obj;
                    FinalStateAware oldBean = (FinalStateAware) oldObj;
                    
                    if(EnumStateSupportHelper.get(ctx).isFinalStateViolation(oldBean, bean))
                    {
                        throw new IllegalStateException("Illegal state change from final state " + oldBean.getAbstractState() + " to " + bean.getAbstractState());
                    }
                }
            }
        }
    }

}
