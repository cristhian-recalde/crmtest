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
package com.trilogy.app.crm.bean.urcs;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.trilogy.app.crm.bean.AuxiliaryServiceHome;
import com.trilogy.app.crm.bean.AuxiliaryServiceStateEnum;
import com.trilogy.app.crm.bean.AuxiliaryServiceXInfo;
import com.trilogy.app.crm.bean.core.custom.AuxiliaryService;
import com.trilogy.app.crm.extension.auxiliaryservice.URCSPromotionAuxSvcExtensionXInfo;
import com.trilogy.app.crm.extension.auxiliaryservice.core.URCSPromotionAuxSvcExtension;
import com.trilogy.app.crm.support.EnumStateSupportHelper;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.In;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.InfoLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;


/**
 * This class should contain all functionality associated with a ServiceOption
 *
 * @author Aaron Gourley
 * @since 8.2
 */
public class ServiceOption extends AbstractServiceOption
{
    public static ServiceOption getServiceOptionBean(final Context ctx, final long serviceOption)
    {
        ServiceOption servOptObj;
        try
        {
            servOptObj = getServiceOptionBeanEx(ctx, serviceOption);
        }
        catch (HomeException e)
        {
            LogSupport.minor(ctx, ServiceOption.class,
                    "Unable to retreive ServiceOption ID=" + serviceOption, e);
            servOptObj = null;
        }

        return servOptObj;
    }

    public static ServiceOption getServiceOptionBeanEx(final Context ctx, final long serviceOption)
        throws HomeException
    {
        final Home home = (Home) ctx.get(ServiceOptionHome.class);
        return (ServiceOption) home.find(ctx, Long.valueOf(serviceOption));
    }
    
    private Collection<AuxiliaryService> getAuxiliaryServices(Context ctx) throws HomeException
    {
        Collection<AuxiliaryService> services = new ArrayList<AuxiliaryService>();
        And filter = new And();
        filter.add(new EQ(URCSPromotionAuxSvcExtensionXInfo.SERVICE_OPTION, this.getIdentifier()));
        
       
        Collection<URCSPromotionAuxSvcExtension> extensions = HomeSupportHelper.get(ctx).getBeans(ctx,  URCSPromotionAuxSvcExtension.class, filter);
        
        if (extensions!=null && extensions.size()>0)
        {
            Set<Long> identifiers = new HashSet<Long>();
            for (URCSPromotionAuxSvcExtension extension : extensions)
            {
                identifiers.add(extension.getAuxiliaryServiceId());
            }
            And filter1 = new And();
            filter1.add(new EQ(AuxiliaryServiceXInfo.SPID,this.getSpid()));
            filter1.add(new In(AuxiliaryServiceXInfo.IDENTIFIER, identifiers));
            
            
            services = HomeSupportHelper.get(ctx).getBeans(ctx, AuxiliaryService.class, filter1);
        }
        
        return services;
        
    }

    public void retire(Context ctx) throws HomeException
    {   
        if (LogSupport.isDebugEnabled(ctx))
        {
            new DebugLogMsg(this, "Attempting to retire service option " + this.getName()
                    + " (ID=" + this.getIdentifier() + ")...", null).log(ctx);   
        }

        Home auxSvcHome = (Home) ctx.get(AuxiliaryServiceHome.class);

        HomeException he = null;
        
        
        Collection<AuxiliaryService> services = getAuxiliaryServices(ctx);
        for (AuxiliaryService service : services)
        {
            if (EnumStateSupportHelper.get(ctx).stateEquals(service, AuxiliaryServiceStateEnum.ACTIVE))
            {
                if (LogSupport.isDebugEnabled(ctx))
                {
                    new DebugLogMsg(this, "Attempting to update state of auxiliary service " + service.getName()
                            + " (ID=" + service.getIdentifier() + ") to " + AuxiliaryServiceStateEnum.DEPRECATED + "...", null).log(ctx);
                }
                
                service.setState(AuxiliaryServiceStateEnum.DEPRECATED);
                auxSvcHome.store(ctx, service);
                
                new InfoLogMsg(this, "Updated state of auxiliary service " + service.getName()
                        + " (ID=" + service.getIdentifier() + ") to " + AuxiliaryServiceStateEnum.DEPRECATED
                        + " while retiring service option " + this.getName() + " (ID=" + this.getIdentifier() + ")", null).log(ctx);
            }
            else if (service.isInFinalState())
            {
                he = new HomeException("Can't deprecate " + service.getType() + " Auxiliary Service (Name=" + service.getName() + "/ID=" + service.ID() + ") because it is in a final state.");
            }
        }
        
        if (he != null)
        {
            throw he;
        }
        else
        {
            new InfoLogMsg(this, "Service option " + this.getName() + " (ID=" + this.getIdentifier() + ") retired successfully.", null).log(ctx);   
        }
    }
    
    public void unretire(Context ctx) throws HomeException
    {
        if (LogSupport.isDebugEnabled(ctx))
        {
            new DebugLogMsg(this, "Attempting to unretire service option " + this.getName()
                    + " (ID=" + this.getIdentifier() + ")...", null).log(ctx);   
        }
        
        Home auxSvcHome = (Home) ctx.get(AuxiliaryServiceHome.class);

        HomeException he = null;
        
        Collection<AuxiliaryService> services = getAuxiliaryServices(ctx);
        for (AuxiliaryService service : services)
        {
            if (EnumStateSupportHelper.get(ctx).stateEquals(service, AuxiliaryServiceStateEnum.DEPRECATED))
            {
                if (LogSupport.isDebugEnabled(ctx))
                {
                    new DebugLogMsg(this, "Attempting to update state of auxiliary service " + service.getName()
                            + " (ID=" + service.getIdentifier() + ") to " + AuxiliaryServiceStateEnum.ACTIVE + "...", null).log(ctx);
                }
                
                service.setState(AuxiliaryServiceStateEnum.ACTIVE);
                auxSvcHome.store(ctx, service);
                
                new InfoLogMsg(this, "Updated state of auxiliary service " + service.getName()
                        + " (ID=" + service.getIdentifier() + ") to " + AuxiliaryServiceStateEnum.ACTIVE
                        + " while unretiring service option " + this.getName() + " (ID=" + this.getIdentifier() + ")", null).log(ctx);
            }
            else if (service.isInFinalState())
            {
                he = new HomeException("Can't activate " + service.getType() + " Auxiliary Service (Name=" + service.getName() + "/ID=" + service.ID() + ") because it is in a final state.");
            }
        }
        
        if (he != null)
        {
            throw he;
        }
        else
        {
            new InfoLogMsg(this, "Service option " + this.getName() + " (ID=" + this.getIdentifier() + ") unretired successfully.", null).log(ctx);   
        }
    }
    
    public void deactivate(Context ctx) throws HomeException
    {
        if (LogSupport.isDebugEnabled(ctx))
        {
            new DebugLogMsg(this, "Attempting to deactivate service option " + this.getName()
                    + " (ID=" + this.getIdentifier() + ")...", null).log(ctx);   
        }
        
        Home auxSvcHome = (Home) ctx.get(AuxiliaryServiceHome.class);

        Collection<AuxiliaryService> services = getAuxiliaryServices(ctx);
        for (AuxiliaryService service : services)
        {
            if (!EnumStateSupportHelper.get(ctx).stateEquals(service, AuxiliaryServiceStateEnum.CLOSED))
            {
                if (LogSupport.isDebugEnabled(ctx))
                {
                    new DebugLogMsg(this, "Attempting to update state of auxiliary service " + service.getName()
                            + " (ID=" + service.getIdentifier() + ") to " + AuxiliaryServiceStateEnum.CLOSED + "...", null).log(ctx);
                }
                
                
                service.setState(AuxiliaryServiceStateEnum.CLOSED);
                auxSvcHome.store(ctx, service);
                
                new InfoLogMsg(this, "Updated state of auxiliary service " + service.getName()
                        + " (ID=" + service.getIdentifier() + ") to " + AuxiliaryServiceStateEnum.CLOSED
                        + " while deactivating service option " + this.getName() + " (ID=" + this.getIdentifier() + ")", null).log(ctx);
            }
        }
        
        new InfoLogMsg(this, "Service option " + this.getName() + " (ID=" + this.getIdentifier() + ") deactivated successfully.", null).log(ctx);
    }
}
