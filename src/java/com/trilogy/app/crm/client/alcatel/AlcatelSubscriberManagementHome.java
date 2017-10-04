/*
 * This code is a protected work and subject to domestic and international copyright
 * law(s). A complete listing of authors of this work is readily available. Additionally,
 * source code is, by its very nature, confidential information and inextricably contains
 * trade secrets and other information proprietary, valuable and sensitive to Redknee, no
 * unauthorised use, disclosure, manipulation or otherwise is permitted, and may only be
 * used in accordance with the terms of the licence agreement entered into with Redknee
 * Inc. and/or its subsidiaries.
 * 
 * Copyright � Redknee Inc. and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.client.alcatel;

import java.util.Collection;

import com.trilogy.app.crm.bean.Service;
import com.trilogy.app.crm.bean.ServiceTypeEnum;
import com.trilogy.app.crm.bean.ServiceXInfo;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.app.crm.support.Lookup;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.In;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;


/**
 * 
 * An extension of AlcatelUpdateHome to handle Subscriber Deactivation and Remove
 * 
 * @author simar.singh@redknee.com
 * 
 * @param <SUBSCRIBER>
 */
public class AlcatelSubscriberManagementHome extends AlcatelUpdateHome<Subscriber>
{

    /**
     * 
     */
    private static final long serialVersionUID = 1L;


    public AlcatelSubscriberManagementHome(Context ctx, AlcatelFunctions.AlcateBeanFunction<Subscriber> beanFunction,
            Home delegate)
    {
        super(ctx, beanFunction, delegate);
    }


    /**
     * Finalize-remove all Alcatel related services if they exist for this Subscriber
     */
    @Override
    public void remove(Context ctx, Object obj) throws HomeException
    {
    	LogSupport.debug(ctx, this, "SubscriberPipeline[remove].....");
    	// TODO Auto-generated method stub
        super.remove(ctx, obj);
        onFinalization(ctx, (Subscriber) obj);
    }


    /**
     * Try to get the old Subscriber from the context lookup first If not found, recourse
     * to square one.
     */
    @Override
    protected Subscriber getExistingBean(Context ctx, Subscriber newSub) throws HomeException
    {
        // TODO Auto-generated method stub
        if (ctx.has(Lookup.OLDSUBSCRIBER))
        {
            Subscriber oldSub = (Subscriber) ctx.get(Lookup.OLDSUBSCRIBER);
            newSub.getId().equals(oldSub.getId());
            return oldSub;
        }
        return super.getExistingBean(ctx, newSub);
    }


    /**
     * On finalization, remove all Alcatel services for the Subscriber whose record exists
     * on our system {@inheritDoc}
     */
    @Override
    protected void onFinalization(Context ctx, Subscriber sub)
    {
        try
        {
            alcatelRemove(ctx, sub);
        }
        catch (Throwable t)
        {
            // TODO Auto-generated catch block
            logException(ctx, new IllegalStateException("Error removing Alcatel-SSC services for Subscriber ID ["
                    + sub.getId() + "]. Error [ " + t.getMessage() + "]", t));
        }
    }


    /**
     * A simple function to remove all the Alcatel services for a Subscriber
     * 
     * @param ctx
     * @param sub
     */
    protected void alcatelRemove(Context ctx, Subscriber sub)
    {
        new DebugLogMsg(this, "Removing alcatel service", null).log(ctx);
        AlcatelProvisioning alcatelService = (AlcatelProvisioning) ctx.get(AlcatelProvisioning.class);
        try
        {
            // although there is suppose to be just one alcatel service for a Subscriber
            // but model does not ensure one to one mappig.
            Collection<Service> services = HomeSupportHelper.get(ctx).getBeans(ctx, Service.class, new In(ServiceXInfo.ID, sub
                    .getServices(ctx, ServiceTypeEnum.ALCATEL_SSC)));
            for (Service service : services)
            {
                try
                {
                    alcatelService.deleteAccount(ctx, service, sub);
                }
                catch (Throwable t)
                {
                    final String errorMessage = "Error processing Alcatel_SSC Remove for Subscriber with ID ["
                            + sub.getId() + "] with corresponding Service ID [" + service.getID() + "]";
                    logException(ctx, new IllegalStateException(errorMessage, t));
                }
            }
        }
        catch (Throwable t)
        {
            // TODO Auto-generated catch block
            logException(ctx, t);
        }
    }
}
