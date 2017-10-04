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
package com.trilogy.app.crm.subscriber.provision.ipc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.True;
import com.trilogy.framework.xhome.home.AbstractClassAwareHome;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.visitor.Visitor;
import com.trilogy.framework.xhome.webcontrol.AbstractWebControl;

import com.trilogy.product.s5600.ipcg.rating.provisioning.RatePlan;
import com.trilogy.product.s5600.iprc.rating.provisioning.IprcRatePlan;

import com.trilogy.app.crm.bean.PricePlan;
import com.trilogy.app.crm.bean.ipc.IpcRatePlan;
import com.trilogy.app.crm.bean.ipc.IpcRatePlanID;
import com.trilogy.app.crm.client.ipcg.IpcgClient;
import com.trilogy.app.crm.client.ipcg.IpcgClientFactory;
import com.trilogy.app.crm.client.ipcg.IpcgRatingProvException;
import com.trilogy.app.crm.support.IpcgClientSupport;
import com.trilogy.app.crm.technology.TechnologyEnum;

/**
 * @author rajith.attapattu@redknee.com
 */
public class IpcRatePlanCorbaHome extends AbstractClassAwareHome
{
    public IpcRatePlanCorbaHome(final Context ctx, final TechnologyEnum technology)
    {
        super(ctx, IpcRatePlan.class);
        technology_ = technology;
    }

    @Override
    public Object create(final Context ctx, final Object obj)
    {
        throw new UnsupportedOperationException("NOP");
    }

    @Override
    public Object store(final Context ctx, final Object obj)
    {
        throw new UnsupportedOperationException("NOP");
    }

    @Override
    public Object find(final Context ctx, final Object obj) throws HomeException
    {
        final IpcRatePlanID key = (IpcRatePlanID)obj;
        if (key.getRatePlanId() == IpcRatePlan.DEFAULT_RATEPLANID)
        {
            return new IpcRatePlan();
        }

        final Collection col = select(ctx, True.instance());
        for (final Iterator it = col.iterator(); it.hasNext();)
        {
            final IpcRatePlan plan = (IpcRatePlan) it.next();
            if (plan.getRatePlanId() == key.getRatePlanId() && plan.getSpid() == key.getSpid())
            {
                return plan;
            }
        }

        throw new HomeException("Rate Plan id " + key + " not found!");
    }

    @Override
    public Collection select(final Context ctx, final Object obj) throws HomeException
    {
        final IpcgClient appIpcClient = IpcgClientFactory.locateClient(ctx, technology_);

        final List ratePlans = new ArrayList();
        
        if (IpcgClientSupport.supportsRetrieveAllRatePlans(ctx))
        {
        	getAllRatePlans(ctx, appIpcClient, ratePlans);
        }
        else // if (IpcgClientSupport.supportsQueryRatePlans(ctx))
        {
        	/* Select is only performed for PricePlanVersion creation.  When this changes,
        	 * we have to think of another way to retrieve the Service Provider information. */
        	final Context parentContext = (Context) ctx.get("..");
            final PricePlan pPlan = (PricePlan) parentContext.get(AbstractWebControl.BEAN);
        	getAllRatePlans(ctx, appIpcClient, ratePlans, pPlan.getSpid());
        }

        return ratePlans;
    }
    
    private List getAllRatePlans(Context ctx, IpcgClient appIpcClient, List ratePlans)
    throws HomeException
    {
    	try
    	{
			if (appIpcClient == null)
			{
				throw new IpcgRatingProvException(
				    "Internal Error: IPC client does not exist in context.");
			}
    		final RatePlan[] plans = appIpcClient.getAllRatePlans(ctx);
    		for (int i = 0; i < plans.length; i++)
    		{
                addDistinctRatePlan(ratePlans, plans[i]);
    		}
    	}
    	catch (IpcgRatingProvException e)
    	{
    		throw new HomeException("Unable to retrive rating plans from IpcProductS5600 using getAllRatePlans(). Error: " + e.getMessage(), e);
    	}
        return ratePlans;
    }

    private void addDistinctRatePlan(List plans, RatePlan toAdd)
    {
        Iterator it = plans.iterator();
        while (it.hasNext())
        {
            IpcRatePlan rp = (IpcRatePlan) it.next();
            if (rp.getRatePlanId() == toAdd.rpId && rp.getSpid() == toAdd.spId)
            {
                return;
            }
        }

        final IpcRatePlan irp = new IpcRatePlan();
        irp.setRatePlanId(toAdd.rpId);
        irp.setSpid(toAdd.spId);
        irp.setDescription(toAdd.description);

        plans.add(irp);
    }

    /**
     * Retrieve all URCS Data Rate Plans for the given Service Provider.
     * @param ctx
     * @param appIpcClient
     * @param ratePlans
     * @param spid
     * @return
     */
    private List getAllRatePlans(Context ctx, IpcgClient appIpcClient, 
    		List ratePlans, int spid)
    throws HomeException
    {
    	try
    	{
    		final IprcRatePlan[] plans = appIpcClient.getAllRatePlans(ctx, spid);
    		for (int i = 0; i < plans.length; i++)
    		{
    			final IprcRatePlan plan = plans[i];

    			final IpcRatePlan rp = new IpcRatePlan();
    			rp.setRatePlanId(plan.rpId);
    			rp.setSpid(plan.spId);
    			rp.setDescription(plan.description);

    			ratePlans.add(rp);
    		}
    	}
    	catch (IpcgRatingProvException e)
    	{
    		throw new HomeException("Unable to retrive rating plans from IpcProductS5600 using queryRatePlans(). Error: " + e.getMessage(), e);
    	}
        return ratePlans;
    }

    @Override
    public void remove(final Context ctx, final Object obj)
    {
        throw new UnsupportedOperationException("NOP");
    }

    @Override
    public void removeAll(final Context ctx, final Object obj)
    {
        throw new UnsupportedOperationException("NOP");
    }

    @Override
    public Visitor forEach(final Context ctx, final Visitor visitor, final Object obj)
    {
        throw new UnsupportedOperationException("NOP");
    }

    /**
     * The Technology type associated with this home.  This is required for
     * locating the proper IpcgClient in the context.
     */
    private final TechnologyEnum technology_;

}
