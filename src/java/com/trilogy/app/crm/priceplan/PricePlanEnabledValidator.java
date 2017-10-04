/*
 * This code is a protected work and subject to domestic and international
 * copyright law(s). A complete listing of authors of this work is readily 
 * available. Additionally, source code is, by its very nature, confidential 
 * information and inextricably contains trade secrets and other information 
 * proprietary, valuable and sensitive to Redknee, no unauthorised use, 
 * disclosure, manipulation or otherwise is permitted, and may only be used 
 * in accordance with the terms of the licence agreement entered into with 
 * Redknee Inc. and/or its subsidiaries.
 *
 * Copyright � Redknee Inc. and its subsidiaries. All Rights Reserved. 
 */
package com.trilogy.app.crm.priceplan;

import com.trilogy.app.crm.bean.PricePlan;
import com.trilogy.app.crm.bean.PricePlanHome;
import com.trilogy.app.crm.bean.PricePlanIdentitySupport;
import com.trilogy.app.crm.bean.PricePlanXInfo;
import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.beans.Validator;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;

/**
 * 
 * @author Aaron Gourley
 * @since 
 *
 */
public class PricePlanEnabledValidator implements Validator
{
    private static PricePlanEnabledValidator instance_ = null;
    public static PricePlanEnabledValidator instance()
    {
        if( instance_ == null )
        {
            instance_ = new PricePlanEnabledValidator();
        }
        return instance_;
    }

    /* (non-Javadoc)
     * @see com.redknee.framework.xhome.beans.Validator#validate(com.redknee.framework.xhome.context.Context, java.lang.Object)
     */
    public void validate(Context ctx, Object bean) throws IllegalStateException
    {
        Object pricePlan = bean;
        if( PricePlanIdentitySupport.instance().isKey(bean) )
        {
            Home ppHome = (Home)ctx.get(PricePlanHome.class);
            if( ppHome != null )
            {
                try
                {
                    pricePlan = ppHome.find(ctx, bean);
                }
                catch (HomeException e)
                {}
            }
            if( pricePlan == null )
            {
                throw new IllegalStateException("Price plan not found with ID=" + bean);
            }
        }
        if( pricePlan instanceof PricePlan && !((PricePlan)pricePlan).isEnabled() )
        {
            throw new IllegalStateException("Price plan can not be used because it is disabled.");
        }
    }

}
