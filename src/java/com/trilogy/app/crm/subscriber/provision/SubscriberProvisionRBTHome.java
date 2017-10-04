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

package com.trilogy.app.crm.subscriber.provision;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.trilogy.framework.xhome.beans.SafetyUtil;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xlog.log.InfoLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;

import com.trilogy.app.crm.bean.core.AuxiliaryService;
import com.trilogy.app.crm.bean.AuxiliaryServiceTypeEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberAuxiliaryService;
import com.trilogy.app.crm.client.ringbacktone.RBTClient;
import com.trilogy.app.crm.client.ringbacktone.RBTClientFactory;
import com.trilogy.app.crm.extension.auxiliaryservice.PRBTAuxSvcExtension;
import com.trilogy.app.crm.extension.auxiliaryservice.core.CallingGroupAuxSvcExtension;
import com.trilogy.app.crm.home.sub.StateChangeAuxiliaryServiceSupport;
import com.trilogy.app.crm.support.ClosedUserGroupSupport73;
import com.trilogy.app.crm.support.ExtensionSupportHelper;
import com.trilogy.app.crm.support.Lookup;


/**
 * @author jeff
 */
public class SubscriberProvisionRBTHome extends HomeProxy
{

    /**
     * @param ctx
     * @param delegate
     */
    public SubscriberProvisionRBTHome(final Context ctx, final Home delegate)
    {
        super(ctx, delegate);
    }

    /**
     * @see com.redknee.framework.xhome.home.HomeSPI#store(com.redknee.framework.xhome.context.Context, java.lang.Object)
     */
    @Override
    public Object store(final Context ctx, final Object obj) throws HomeException
    {
    	LogSupport.debug(ctx, this, "SubscriberPipeline[store].....");
    	final Subscriber newSub = (Subscriber)obj;
        final Subscriber oldSub = (Subscriber) ctx.get(Lookup.OLDSUBSCRIBER);

        try
        {
            boolean msisdnChanged = oldSub!=null && !SafetyUtil.safeEquals(oldSub.getMSISDN(), newSub.getMSISDN());
            
            // The code here is different to the original copy from RBT branch,  The reason being is that
            // Activate/Deactiavate will trigger create()/remove() in AuxService Pipeline in latest CRM (trunk)
            // but it wasn't in RBT branch (That's why we need to add watchdog in Subscriber Pipeline for activation/deactivation).
            // The code to handle activatation/deactivation is not required any more here.  -- JLI
            
            if (msisdnChanged)
            {
                List<AuxiliaryService> services = getRbtService(ctx, newSub);
                if (services != null && services.size()>0)
                {

                    
                    Context subCtx   = ctx.createSubContext();
                    
                    for (AuxiliaryService auxSvc : services)
                    {
                        subCtx.put(AuxiliaryService.class, auxSvc);
                        
                        long rbtId = PRBTAuxSvcExtension.DEFAULT_RBTID;
                        PRBTAuxSvcExtension prbtAuxSvcExtension = ExtensionSupportHelper.get(ctx).getExtension(ctx, auxSvc, PRBTAuxSvcExtension.class);
                        if (prbtAuxSvcExtension!=null)
                        {
                            rbtId = prbtAuxSvcExtension.getRbtId();
                        }
                        else
                        {
                            LogSupport.minor(ctx, this,
                                    "Unable to find required extension of type '" + PRBTAuxSvcExtension.class.getSimpleName()
                                            + "' for auxiliary service " + auxSvc.getIdentifier());
                        }
                        RBTClient client = RBTClientFactory.locateClient(rbtId);
                        if (client == null)
                        {
                            throw new NullPointerException("RBTClient is not available.");
                        }                        
                        if (msisdnChanged )
                        {
                            client.updateSubscriberMSISDN(subCtx, oldSub.getMSISDN(), newSub.getMSISDN());
                        }
                    }
                }
            }
        }
        catch(Exception e)
        {
            LogSupport.minor(ctx, this, "Failed to provision RingBackTone Service due to "+ e.getMessage());
            SubscriberProvisionResultCode.addException(ctx, "Store subscriber with RBT Aux Service error: " + e, e, null, newSub);
        }
        
        return super.store(ctx, newSub);
    }

    @SuppressWarnings("unchecked")
    private List<AuxiliaryService> getRbtService(Context ctx, Subscriber subscriber)
    {
        List<AuxiliaryService> list = new ArrayList<AuxiliaryService>();
        
        final Collection auxServiceCol = subscriber.getAuxiliaryServices(ctx);

        if (auxServiceCol != null)
        {
            for (Object obj : auxServiceCol)
            {
                SubscriberAuxiliaryService subService = (SubscriberAuxiliaryService) obj;
                try
                {
                    final AuxiliaryService service = subService.getAuxiliaryService(ctx);

                    if (service.getType() == AuxiliaryServiceTypeEnum.PRBT)
                    {
                        list.add(service);
                    }
                }
                catch (final HomeException e)
                {
                    new InfoLogMsg(StateChangeAuxiliaryServiceSupport.class, "AuxilaryService ["
                        + subService.getAuxiliaryServiceIdentifier() + "] for Subscriber [" + subscriber.getId()
                        + "] is not available", e).log(ctx);
                }

            }
        }
        
        return list;
    }
}
