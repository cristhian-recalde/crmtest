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
package com.trilogy.app.crm.sat;

import com.trilogy.app.crm.bas.tps.ServiceActivationTemplate;
import com.trilogy.app.crm.bas.tps.ServiceActivationTemplateHome;
import com.trilogy.app.crm.bas.tps.ServiceActivationTemplateXInfo;
import com.trilogy.framework.xhome.beans.SafetyUtil;
import com.trilogy.framework.xhome.beans.Validator;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeOperationEnum;
import com.trilogy.framework.xlog.log.MajorLogMsg;

/**
 * 
 * @author Aaron Gourley
 * @since 
 *
 */
public class UniqueSATNameForSameSpidValidator implements Validator
{
    private static UniqueSATNameForSameSpidValidator instance_ = null;
    
    public static UniqueSATNameForSameSpidValidator instance()
    {
        if( instance_ == null )
        {
            instance_ = new UniqueSATNameForSameSpidValidator();
        }
        return instance_;
    }
    
    /* (non-Javadoc)
     * @see com.redknee.framework.xhome.beans.Validator#validate(com.redknee.framework.xhome.context.Context, java.lang.Object)
     */
    public void validate(Context ctx, Object bean) throws IllegalStateException
    {
        if( bean instanceof ServiceActivationTemplate )
        {
            ServiceActivationTemplate sat = (ServiceActivationTemplate)bean;
            if( !isNameUniqueForSameSpid(ctx, sat) )
            {
                throw new IllegalStateException(sat.getName() + " already exists for SPID=" + sat.getSpid() );   
            }
        }
    }

    private boolean isNameUniqueForSameSpid(Context ctx, ServiceActivationTemplate newSat)
    {
        try
        {
            Home satHome = (Home)ctx.get(ServiceActivationTemplateHome.class);
            ServiceActivationTemplate oldSat = (ServiceActivationTemplate) satHome.find(ctx,
                    new And()
                    .add(new EQ(ServiceActivationTemplateXInfo.NAME, newSat.getName()))
                    .add(new EQ(ServiceActivationTemplateXInfo.SPID, newSat.getSpid())));

            if (oldSat == null)
            {
                return true;
            }
            
            return SafetyUtil.safeEquals(HomeOperationEnum.STORE, ctx.get(HomeOperationEnum.class))
                    && newSat.getIdentifier() == oldSat.getIdentifier();
        }
        catch (HomeException he)
        {
            new MajorLogMsg(this, "Failed to look-up SCT for SPID " + newSat.getSpid() + " and Name "
                + newSat.getName(), he).log(ctx);

            return false;
        }

    }

}
