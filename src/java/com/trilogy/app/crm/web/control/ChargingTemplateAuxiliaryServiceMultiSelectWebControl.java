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
package com.trilogy.app.crm.web.control;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.trilogy.app.crm.bean.AuxiliaryServiceTypeEnum;
import com.trilogy.app.crm.bean.AuxiliaryServiceXInfo;
import com.trilogy.app.crm.bean.CallingGroupTypeEnum;
import com.trilogy.app.crm.extension.auxiliaryservice.CallingGroupAuxSvcExtensionXInfo;
import com.trilogy.app.crm.extension.auxiliaryservice.core.CallingGroupAuxSvcExtension;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.In;
import com.trilogy.framework.xhome.elang.Not;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.LogSupport;

/**
 * Multi select web control used to display auxiliary services filtering PCUGs.
 * @author Marcio Marques
 * @since 8.5
 * 
 */
public class ChargingTemplateAuxiliaryServiceMultiSelectWebControl extends AuxiliaryServiceMultiSelectWebControl
{
    public ChargingTemplateAuxiliaryServiceMultiSelectWebControl()
    {
        super();
    }

    @Override
    public Home getHome(Context ctx)
    {
        return filterPCUG(ctx, super.getHome(ctx));
    }
    
    private Home filterPCUG(Context ctx, Home home)
    {
        try
        {
            Collection<CallingGroupAuxSvcExtension> extensions = HomeSupportHelper.get(ctx).getBeans(ctx, CallingGroupAuxSvcExtension.class, new EQ(CallingGroupAuxSvcExtensionXInfo.CALLING_GROUP_TYPE, CallingGroupTypeEnum.PCUG));
            
            if (extensions!=null && extensions.size()>0)
            {
                Set<Long> identifiers = new HashSet<Long>();
                for (CallingGroupAuxSvcExtension extension : extensions)
                {
                    identifiers.add(extension.getAuxiliaryServiceId());
                }
                
                And and = new And();
                and.add(new EQ(AuxiliaryServiceXInfo.TYPE, AuxiliaryServiceTypeEnum.CallingGroup));
                and.add(new In(AuxiliaryServiceXInfo.IDENTIFIER, identifiers));
                Not filter = new Not(and);
                return home.where(ctx, filter);
            }
        }
        catch (HomeException e)
        {
            LogSupport.minor(ctx, this, "Unable to filter PCUGs out of Charging Template multi select web control: " + e.getMessage(), e);
        }
        
        return home;
    }
    
  
}
