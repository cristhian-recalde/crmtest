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
package com.trilogy.app.crm.hlr;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.trilogy.app.crm.bean.ProvisionCommand;
import com.trilogy.app.crm.bean.ProvisionCommandTypeEnum;
import com.trilogy.app.crm.bean.ServiceFee2ID;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.bean.core.PricePlanVersion;
import com.trilogy.app.crm.bean.ui.Service;
import com.trilogy.app.crm.home.sub.HLRCommandFindHelper;
import com.trilogy.app.crm.home.sub.HLRConstants;
import com.trilogy.app.crm.provision.CommonProvisionAgentBase;
import com.trilogy.app.crm.provision.ProvisionAgentException;
import com.trilogy.app.crm.provision.gateway.SPGParameter;
import com.trilogy.app.crm.provision.gateway.ServiceProvisioningGatewaySupport;
import com.trilogy.app.crm.provision.service.param.CommandID;
import com.trilogy.app.crm.provision.service.param.ParameterID;
import com.trilogy.app.crm.provision.service.param.ProvisionEntityType;
import com.trilogy.app.crm.support.Lookup;
import com.trilogy.app.crm.support.SystemSupport;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.LogSupport;

/**
 * @author joe.chen@redknee.com
 */
public class HlrSupport 
{

    public static boolean updateHlr(Context ctx, Subscriber subscriber,  final ProvisionCommand provCmd) 
    throws ProvisionAgentException
	{
        //HLR is disabled
        if (!SystemSupport.needsHlr(ctx))
        {
            return true;
        }
        Context subCtx = ctx.createSubContext(); 
        
        //subCtx.put(ServiceProvisioningGatewaySupport.HLR_SERVICE_SPG_SERVICE_ID, String.valueOf(ProvisionEntityType.PROVISION_ENTITY_TYPE_PRV_CMD)); 
        final Set<Long> ids = new HashSet<Long>();
        ids.add(Long.valueOf(ServiceProvisioningGatewaySupport.HLR_SERVICE_SPG_SERVICE_ID));
        Subscriber oldSub = (Subscriber)ctx.get(Lookup.OLD_FROZEN_SUBSCRIBER, subscriber);
        
        subCtx.put(HLRConstants.HLR_PARAMKEY_PROVISION_CMD_NAME, provCmd.getName());
     
        subCtx.put(CommonProvisionAgentBase.SPG_PROVISIONING_CUSTOM_PROVISION_ENTITY_TYPE, String.valueOf(ProvisionEntityType.PROVISION_ENTITY_TYPE_PRV_CMD));
        if (ctx.has(HLRConstants.HLR_PARAMKEY_OLD_IMSI_KEY))
        {
            subCtx.put(CommonProvisionAgentBase.SPG_PROVISIONING_CUSTOM_OLD_IMSI,
                ctx.get(HLRConstants.HLR_PARAMKEY_OLD_IMSI_KEY));
        }
        else 
        {
            subCtx.put(CommonProvisionAgentBase.SPG_PROVISIONING_CUSTOM_OLD_IMSI,oldSub.getIMSI()); 
        }
        subCtx.put(CommonProvisionAgentBase.SPG_PROVISIONING_CUSTOM_OLD_VOICE_MSISDN, oldSub.getMsisdn()); 
        subCtx.put(CommonProvisionAgentBase.SPG_PROVISIONING_CUSTOM_OLD_LANGUAGE, oldSub.getBillingLanguage());
 

        subCtx.put(CommonProvisionAgentBase.SPG_PROVISIONING_CUSTOM_OLD_PRICEPLANID, oldSub.getPricePlan());
        
        try
        {
            if(!ctx.getBoolean(HLRConstants.ACCOUNT_BILLCYCLE_CHANGE))
            {
                subCtx.put(CommonProvisionAgentBase.SPG_PROVISIONING_CUSTOM_OLD_BILLCYCLEDAY, oldSub.getAccount(subCtx).getBillCycleDay(subCtx));
                subCtx.put(CommonProvisionAgentBase.SPG_PROVISIONING_CUSTOM_OLD_BILLCYCLEID, oldSub.getAccount(subCtx).getBillCycleID());
            }
        } 
        catch (HomeException e)
        {
            LogSupport.minor(subCtx, HlrSupport.class.getName(), "Could not get Acconut bean for the old subscriber: "+ oldSub, e);
        }
        
        try
        {
            /*
             * Account derivatives
             */
            if(!ctx.getBoolean(HLRConstants.ACCOUNT_BILLCYCLE_CHANGE))
            {
                subCtx.put(CommonProvisionAgentBase.SPG_PROVISIONING_CUSTOM_BILLCYCLEID, subscriber.getAccount(ctx).getBillCycleID());
                subCtx.put(CommonProvisionAgentBase.SPG_PROVISIONING_CUSTOM_BILLCYCLEDAY, subscriber.getAccount(ctx).getBillCycleDay(ctx));
            }
        } 
        catch (HomeException e)
        {
            LogSupport.minor(subCtx, HlrSupport.class.getName(), "Could not get Acconut bean for the subscriber_to_update: "+ subscriber, e);
        }
        

        final Collection<SPGParameter>[] params = ServiceProvisioningGatewaySupport.collectParameterDefinitions(subCtx, ids);
        final Map<Integer, String> values = ServiceProvisioningGatewaySupport.collectParameters(subCtx, 
                params[ServiceProvisioningGatewaySupport.ALL], subscriber);   
        //values.put(ParameterID.PRICEPLAN_SERVICES, getPricePlanServiceIdString(ctx, subscriber)); 
        subCtx.put(CommonProvisionAgentBase.SPG_PROVISIONING_CLIENT_VERSION, ctx.get(CommonProvisionAgentBase.SPG_PROVISIONING_CLIENT_VERSION));
        return ServiceProvisioningGatewaySupport.execute(subCtx, CommandID.PROVISION_COMMMAND_EXECUTE, 
                provCmd.getId(), values, subscriber)==0l;
	}
	
 
    
    
	public static boolean updateHlr(Context ctx, Subscriber sub,  final String key) 
	throws HomeException, ProvisionAgentException
	{
		ProvisionCommand cmd = findCommand(ctx, sub, key );
		if (cmd != null)
		{
			return updateHlr(ctx, sub, cmd);
		}	
		
		return false; 
	}
	
	
	public static ProvisionCommand findCommand(final Context context,  Subscriber sub, final ProvisionCommandTypeEnum key) throws HomeException
    {
	    return findCommand(context, sub, key.getDescription());
    }
	
	public static ProvisionCommand findCommand(final Context context,  Subscriber sub, final String key) throws HomeException
    {
        final HLRCommandFindHelper hlrCmdFinder = new HLRCommandFindHelper(
        		context);

        ProvisionCommand command = hlrCmdFinder.findCommand(context, key, sub);
        return command;
    }
	

	public static String getPricePlanServiceIdString(Context ctx, Subscriber sub)
	{
        String ret = "";
        
	    try
	    {
	        PricePlanVersion ppv = sub.getRawPricePlanVersion(ctx); 
	        Collection<ServiceFee2ID> services = ppv.getServices(ctx);
	        
	        for( ServiceFee2ID serviceFee2ID : services)
	        {
	            if (!ret.isEmpty())
	            {
	                ret = ret.concat(","); 
	            }
	            
	            
	            ret = ret.concat(String.valueOf(serviceFee2ID.getServiceId())); 
	        }
	    } catch (Exception e)
	    {
	        
	    
	    }

	        

	    
	    return ret; 
	}
}
