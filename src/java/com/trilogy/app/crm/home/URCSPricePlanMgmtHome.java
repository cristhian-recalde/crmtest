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
package com.trilogy.app.crm.home;

import java.util.ArrayList;

import com.trilogy.app.crm.bean.PricePlan;
import com.trilogy.app.crm.bean.PricePlanXInfo;
import com.trilogy.app.crm.client.PricePlanMgmtClientV2;
import com.trilogy.app.crm.client.PricePlanMgmtCorbaClientV2;
import com.trilogy.app.crm.client.PricePlanMgmtException;
import com.trilogy.app.crm.support.PricePlanSupport;
import com.trilogy.app.urcs.ParamUtil;
import com.trilogy.app.urcs.param.Parameter;
import com.trilogy.app.urcs.param.ParameterSetHolder;
import com.trilogy.app.urcs.provision.PricePlanMgmtParamID;
import com.trilogy.app.urcs.provision.RatePlanType;
import com.trilogy.framework.xhome.beans.ExceptionListener;
import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.beans.XBeans;
import com.trilogy.framework.xhome.beans.xi.PropertyInfo;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MajorLogMsg;
import com.trilogy.framework.xlog.log.MinorLogMsg;

/**
 * This home pushes the Price Plan and Rate Plan mapping to URCS upon Price Plan Creation and 
 * Modification.
 * 
 * By default we will attempt to update the URCS mapping before updating CRM Price Plan.
 * Any errors encountered while updating the URCS mapping will abort the CRM Price Plan update.
 * 
 * @author angie.li@redknee.com
 *
 */
public class URCSPricePlanMgmtHome extends HomeProxy 
{
    
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public URCSPricePlanMgmtHome(Context ctx, Home delegate)
    {
        super(ctx, delegate);
    }

    public Object create(Context ctx, Object obj)
        throws HomeException, HomeInternalException
    {
        PricePlan pp = (PricePlan) getDelegate(ctx).create(ctx, obj);
        PricePlan originalPricePlan;
        //Create a Price Plan with all Default Values
        try
        {    
            originalPricePlan = (PricePlan) XBeans.instantiate(PricePlan.class, ctx);
        }
        catch (Throwable t)
        {
            new MinorLogMsg(this, "Failed to create Default Price Plan for comparison. ", t).log(ctx);
            originalPricePlan = new PricePlan();
        }
        service(ctx, pp, originalPricePlan);
        return pp;
    }
    
    public Object store(Context ctx, Object obj)
        throws HomeException, HomeInternalException
    {
        PricePlan newPricePlan = (PricePlan) obj;
        PricePlan originalPricePlan = PricePlanSupport.getPlan(ctx, newPricePlan.getId());
        service(ctx, newPricePlan, originalPricePlan);
        return getDelegate(ctx).store(ctx, obj);
    }
    
    /**
     * Perform the Update of the Price Plan/Rate Plan mapping, if the Rate Plan values have 
     * changed
     * @param ctx
     * @param obj
     * @throws HomeException
     * @throws HomeInternalException
     */
    private void service(Context ctx, PricePlan newPricePlan, PricePlan originalPricePlan)
        throws HomeException, HomeInternalException
    {
        boolean rollback = false;
        ArrayList<RatePlanType> successfulUpdates = new ArrayList<RatePlanType>();
        
        //Update the mapping if the rate plan has changed
        int spid = newPricePlan.getSpid();
        long pricePlanId = newPricePlan.getId(); 
        try
        {
            updateURCSMapping(ctx, spid, pricePlanId, newPricePlan.getVoiceRatePlan(), 
                    originalPricePlan.getVoiceRatePlan(), RatePlanType.VOICE, rollback, newPricePlan);
            successfulUpdates.add(RatePlanType.VOICE);
        }
        catch(Throwable e)
        {
            handleError(ctx, e, PricePlanXInfo.VOICE_RATE_PLAN);
            rollback = true;
        }
        
        try
        {
            updateURCSMapping(ctx, spid, pricePlanId, newPricePlan.getSMSRatePlan(),
                originalPricePlan.getSMSRatePlan(), RatePlanType.SMS, rollback, newPricePlan);
            successfulUpdates.add(RatePlanType.SMS);
        }
        catch(Exception e)
        {
            handleError(ctx, e, PricePlanXInfo.SMSRATE_PLAN);
            rollback = true;
        }
        
        try
        {
            updateURCSMapping(ctx, spid, pricePlanId, String.valueOf(newPricePlan.getDataRatePlan()),
                String.valueOf(originalPricePlan.getDataRatePlan()), RatePlanType.DATA, rollback, newPricePlan);
            successfulUpdates.add(RatePlanType.DATA);
        }
        catch(Exception e)
        {
            handleError(ctx, e, PricePlanXInfo.DATA_RATE_PLAN);
            rollback = true;
        }
        
        
        /* TODO: URCS 1.x
         * When IPCG integrates fully with URCS, CRM will need to push the Rate Plan mapping to URCS.
         * For now, it will be disabled.  IPCG does not need the rate plan mapping.
        try
        {
            updateURCSMapping(ctx, spid, pricePlanId, newPricePlan.getDataRatePlan(),
                originalPricePlan.getDataRatePlan(), RatePlanType.DATA);
            successfulUpdates.add(RatePlanType.DATA);
        }
        catch(Exception e)
        {
            handleError(ctx, e, PricePlanXInfo.DATA_RATE_PLAN);
            rollback = true;
        }
        */
        
        if (rollback)
        {
            rollbackAllchanges(ctx, spid, pricePlanId, newPricePlan, originalPricePlan, successfulUpdates);
            
            //if we had to roll-back throw an exception to abort the rest of the Price Plan Creation.
            throw new HomeException("Update to the URCS Price Plan/Rate Plan mapping failed.  Abort saving Price Plan.");
        }
        
    }
    
    /**
     * Rollback all successful updates done by this class.
     * @param ctx
     * @param spid
     * @param pricePlanId
     * @param newPricePlan
     * @param originalPricePlan
     * @param successfulUpdates
     */
    private void rollbackAllchanges(Context ctx, final int spid, final long pricePlanId, 
            final PricePlan newPricePlan, final PricePlan originalPricePlan, 
            final ArrayList<RatePlanType> successfulUpdates) 
    {
        //Reset roll-back marker
        if(successfulUpdates.contains(RatePlanType.VOICE))
        {
            //roll-back Voice
            rollbackURCSMapping(ctx, spid, pricePlanId, newPricePlan.getVoiceRatePlan(),
                    originalPricePlan.getVoiceRatePlan(), RatePlanType.VOICE, PricePlanXInfo.VOICE_RATE_PLAN, newPricePlan);
            
        }
        if(successfulUpdates.contains(RatePlanType.SMS))
        {
            //roll-back SMS
            rollbackURCSMapping(ctx, spid, pricePlanId, newPricePlan.getSMSRatePlan(),
                    originalPricePlan.getSMSRatePlan(), RatePlanType.SMS, PricePlanXInfo.SMSRATE_PLAN, newPricePlan);
        }
      
        if(successfulUpdates.contains(RatePlanType.DATA))
        {
            //roll-back Data
            rollbackURCSMapping(ctx, spid, pricePlanId, String.valueOf(newPricePlan.getDataRatePlan()),
                    String.valueOf(originalPricePlan.getDataRatePlan()), RatePlanType.DATA, PricePlanXInfo.DATA_RATE_PLAN, newPricePlan);
        }
      
    }
    
    /**
     * If the Rate Plans given don't match, then perform an update of the PricePlan/RatePlan mapping
     * in URCS.
     * If the roll-back action has been triggered, then ignore the update request.
     * 
     * @param ctx
     * @param spid
     * @param pricePlanId
     * @param newRatePlan
     * @param originalRatePlan
     * @param type
     * @param property
     */
    private void updateURCSMapping(Context ctx, final int spid, final long pricePlanId, 
            final String newRatePlan, final String originalRatePlan, final RatePlanType type,
            final boolean rollback, PricePlan pricePlan)
        throws HomeException, PricePlanMgmtException
    {
        if (!rollback && !newRatePlan.equals(originalRatePlan))
        {
            mapPricePlan(ctx, spid, pricePlanId, newRatePlan, type, pricePlan);
        }
    }
    
    /**
     * Use the Price Plan Management Client to update the Price Plan/Rate Plan mapping.
     * @param ctx
     * @param spid
     * @param pricePlanId
     * @param newRatePlan
     * @param type
     * @param property
     */
    private void mapPricePlan(Context ctx, final int spid, final long pricePlanId, 
            final String newRatePlan, final RatePlanType type, PricePlan pricePlan)
        throws HomeException, PricePlanMgmtException
    {
        if (LogSupport.isDebugEnabled(ctx))
        {
            new DebugLogMsg(this, "Updating URCS Price Plan/Rate Plan Mapping for SPID=" + spid + 
                    ", PricePlanId=" + pricePlanId + ", RatePlanType=" + 
                    PricePlanMgmtCorbaClientV2.formatRatePlanTypeName(type) + 
                    " with NewRatePlanId=" + newRatePlan, null).log(ctx);
        }
        final Parameter[] inParams = new Parameter[1];
        inParams[0] = new Parameter(PricePlanMgmtParamID.PRICE_PLAN_SUB_TYPE, ParamUtil.createValue((short)pricePlan.getPricePlanSubType().getIndex()));
		final ParameterSetHolder outParams = new ParameterSetHolder();
        getClient(ctx).mapPricePlan(ctx, spid, String.valueOf(pricePlanId), 
                type, newRatePlan, inParams, outParams);
    }
    
    /**
     * If the given Rate Plan Ids don't match, then update the Price Plan/Rate Plan mapping on 
     * URCS with the original Rate Plan.
     * 
     * @param ctx
     * @param spid
     * @param pricePlanId
     * @param newRatePlan
     * @param originalRatePlan
     * @param type
     * @param property
     */
    private void rollbackURCSMapping(Context ctx, final int spid, final long pricePlanId, 
            final String newRatePlan, final String originalRatePlan, final RatePlanType type,
            final PropertyInfo property, PricePlan pricePlan)
    {
        ExceptionListener el = (ExceptionListener) ctx.get(ExceptionListener.class);

        try
        {
            if (!newRatePlan.equals(originalRatePlan))
            {
                if (LogSupport.isDebugEnabled(ctx))
                {
                    new DebugLogMsg(this, "Rolling back URCS Mapping for Rate Plan Type=" + PricePlanMgmtCorbaClientV2.formatRatePlanTypeName(type), null).log(ctx);
                }
                //Pass in the originalRatePlan as the newly intended Rate Plan update during the roll-back
                updateURCSMapping(ctx, spid, pricePlanId, originalRatePlan, newRatePlan, type, false, pricePlan);
                
                //If Roll-back was attempted, then report it to the screen.
                el.thrown(new IllegalPropertyArgumentException(property, "Rolled-back the update of this Price Plan/Rate Plan Mapping on URCS"));
            }
        }
        catch (Throwable t)
        {
            String msg = "Failed to rolled-back the update of this Price Plan/Rate Plan Mapping on URCS";
            new MinorLogMsg(this, msg, t).log(ctx);
            el.thrown(new IllegalPropertyArgumentException(property, msg));
        }
    }
    
    /**
     * Retrieve the URCS PricePlanMgmtClient
     * @param ctx
     * @return
     * @throws HomeException
     */
    private PricePlanMgmtClientV2 getClient(final Context ctx) throws HomeException
    {
        final PricePlanMgmtClientV2 client = (PricePlanMgmtClientV2) ctx.get(PricePlanMgmtClientV2.class);
        if (client == null)
        {
            HomeException exception = new HomeException("URCS PricePlanMgmtClientV2 is not found in context.");
            new MajorLogMsg(this, exception.getMessage(), exception).log(ctx);
            throw exception;
        }
        return client;
    }

    /**
     * Report the Error to the GUI and trigger roll-back action
     * @param ctx
     * @param e
     * @param type
     * @param property
     */
    private void handleError(Context ctx, final Throwable e, final PropertyInfo property) 
    {
        ExceptionListener el = (ExceptionListener) ctx.get(ExceptionListener.class);
        el.thrown(new IllegalPropertyArgumentException(property, "Failed to update Price Plan/Rate Plan mapping on URCS due to " + e.getMessage()));       
    }

}
