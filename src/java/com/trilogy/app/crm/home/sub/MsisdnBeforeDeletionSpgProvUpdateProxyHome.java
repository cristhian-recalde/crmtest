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
package com.trilogy.app.crm.home.sub;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.trilogy.app.crm.bean.Msisdn;
import com.trilogy.app.crm.bean.MsisdnHome;
import com.trilogy.app.crm.bean.MsisdnXInfo;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.core.MsisdnMgmtHistory;
import com.trilogy.app.crm.grr.GrrGeneratorGeneralConfig;
import com.trilogy.app.crm.grr.GrrGeneratorGeneralConfigHome;
import com.trilogy.app.crm.numbermgn.MsisdnMgmtHistoryHome;
import com.trilogy.app.crm.numbermgn.MsisdnMgmtHistoryXInfo;
import com.trilogy.app.crm.provision.gateway.ServiceProvisioningGatewaySupport;
import com.trilogy.app.crm.provision.service.ErrorCode;
import com.trilogy.app.crm.provision.service.param.CommandID;
import com.trilogy.app.crm.provision.service.param.ParameterID;
import com.trilogy.app.crm.provision.service.param.ProvisionEntityType;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.app.crm.support.SubscriberSupport;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MajorLogMsg;
import com.trilogy.framework.xlog.log.MinorLogMsg;
import com.trilogy.util.snippet.log.Logger;
/**
 * 
 * @author rashmi Deogaonkar
 *
 */
public class MsisdnBeforeDeletionSpgProvUpdateProxyHome extends HomeProxy 
{
	private static final long serialVersionUID = 1L;
	
	public MsisdnBeforeDeletionSpgProvUpdateProxyHome(Context ctx, Home delegate) {
		super(ctx, delegate);
	}
	
	private static int GATEWAY_SERVICE_CONSTANT_FOR_MSISDN_DELETION_FOR_UPD_FNR =5;
	@Override
	public void remove(Context ctx, Object obj) throws HomeException,
			HomeInternalException 
	{
		Msisdn msisdn =  (Msisdn)obj;
		if(msisdn.isExternal())
		{
		
			Home home = (Home)ctx.get(MsisdnMgmtHistoryHome.class);
			Collection<MsisdnMgmtHistory> col =home.select(ctx,new EQ(MsisdnMgmtHistoryXInfo.TERMINAL_ID, msisdn.getMsisdn()));
			Subscriber subscriber = null;
			String subscriberId = null;
			for(MsisdnMgmtHistory msisdnMgmtHistory : col)
			{
				subscriberId =msisdnMgmtHistory.getSubscriberId()==null? subscriberId: msisdnMgmtHistory.getSubscriberId();
			} 
			if(subscriberId == null)
			{
				String log= " As updation at external network is not Sucessful because subscriber ID not found in MsisdnMgmtHistory for Msisdn "+msisdn.getMsisdn()+",So avoiding to remove the  external msisdn ["+msisdn.getMsisdn()+"] from the database with state held";
				Logger.major(ctx, this, log);
				throw new HomeException(log);
			}
			subscriber = SubscriberSupport.getSubscriber(ctx, subscriberId);
			if(subscriber == null)
			{
				String log=  " As updation at external network is not Sucessful because subscriber not found in Subscriber for Msisdn "+msisdn.getMsisdn()+",So avoiding to remove the  external msisdn ["+msisdn.getMsisdn()+"] from the database with state held";
				Logger.major(ctx, this, log);
				throw new HomeException(log);
			
			}
		
			int spid = msisdn.getSpid();
			String[] cmd = getExtMsisdnDeletionCmd(ctx, spid);
		
			for (String cmdi : cmd )
			{
				if(cmdi != null)
				{
					if(LogSupport.isDebugEnabled(ctx))
					{
						LogSupport.debug(ctx, this, "Preparing to send External MSISDN deletion command : "+ cmdi);
					}
					
					Map<Integer,String> values = new HashMap<Integer,String>() ;
					values.put(ParameterID.SPID, String.valueOf(spid));
					values.put(ParameterID.MSISDN, String.valueOf(msisdn.getMsisdn()));
					values.put(ParameterID.BAN, msisdn.getBAN());
					values.put(ParameterID.PROVISION_ENTITY_TYPE,String.valueOf(ProvisionEntityType.PROVISION_ENTITY_TYPE_SPG));
					values.put(ParameterID.MSISDN_DELETION_CMD, cmdi);
					
					if(ServiceProvisioningGatewaySupport.execute(ctx, CommandID.MSISDN_DELETION_EXECUTE, GATEWAY_SERVICE_CONSTANT_FOR_MSISDN_DELETION_FOR_UPD_FNR, values, subscriber)== ErrorCode.SUCCESS)
					{
						if(LogSupport.isDebugEnabled(ctx))
						{
							LogSupport.debug(ctx, this, "External MSISDN deletion command ("+ cmdi +") successful.");
						}
						super.remove(ctx, obj);
					}
					else
					{
						String log=  " As updation at external network is not Sucessful,So avoiding to remove the  external msisdn ["+msisdn.getMsisdn()+"] from the database with state held";
						Logger.major(ctx, this, log);
						throw new HomeException(log);
					}
				}
			}
		}
		else
		{
			super.remove(ctx, obj);
		}
		
		
	}
	
	public static boolean testThisCode(String msisdn1, Context ctx)
	{
		Home msisdnHome =  (Home)ctx.get(MsisdnHome.class);
		try
		{
			Msisdn msisdn =  (Msisdn)msisdnHome.find(ctx, new EQ(MsisdnXInfo.MSISDN,msisdn1));
			Home home = (Home)ctx.get(MsisdnMgmtHistoryHome.class);
			Collection<MsisdnMgmtHistory> col =home.select(ctx,new EQ(MsisdnMgmtHistoryXInfo.TERMINAL_ID, msisdn.getMsisdn()));
			Subscriber subscriber = null;
			String subscriberId = null;
			for(MsisdnMgmtHistory msisdnMgmtHistory : col)
			{
				subscriberId =msisdnMgmtHistory.getSubscriberId()==null? subscriberId: msisdnMgmtHistory.getSubscriberId();
			} 
			if(subscriberId == null)
			{
				String log= " As updation at external network is not Sucessful because subscriber ID not found in MsisdnMgmtHistory for Msisdn "+msisdn.getMsisdn()+",So avoiding to remove the  external msisdn ["+msisdn.getMsisdn()+"] from the database with state held";
				Logger.major(ctx, MsisdnBeforeDeletionSpgProvUpdateProxyHome.class, log);
				throw new HomeException(log);
			}
			subscriber = SubscriberSupport.getSubscriber(ctx, subscriberId);
			if(subscriber == null)
			{
				String log=  " As updation at external network is not Sucessful because subscriber not found in Subscriber for Msisdn "+msisdn.getMsisdn()+",So avoiding to remove the  external msisdn ["+msisdn.getMsisdn()+"] from the database with state held";
				Logger.major(ctx, MsisdnBeforeDeletionSpgProvUpdateProxyHome.class, log);
				throw new HomeException(log);
			
			}
		
			int spid = msisdn.getSpid();
			String[] cmd = getExtMsisdnDeletionCmd(ctx, spid);
		
			for (String cmdi : cmd )
			{
				if(LogSupport.isDebugEnabled(ctx))
				{
					LogSupport.debug(ctx, MsisdnBeforeDeletionSpgProvUpdateProxyHome.class, "Preparing to send External MSISDN deletion command : "+ cmdi);
				}
				
				Map<Integer,String> values = new HashMap<Integer,String>() ;
				values.put(ParameterID.SPID, String.valueOf(spid));
				values.put(ParameterID.MSISDN, String.valueOf(msisdn.getMsisdn()));
				values.put(ParameterID.BAN, msisdn.getBAN());
				values.put(ParameterID.PROVISION_ENTITY_TYPE,String.valueOf(ProvisionEntityType.PROVISION_ENTITY_TYPE_SPG));
				values.put(ParameterID.MSISDN_DELETION_CMD, cmdi);
				
				if(ServiceProvisioningGatewaySupport.execute(ctx, CommandID.MSISDN_DELETION_EXECUTE, GATEWAY_SERVICE_CONSTANT_FOR_MSISDN_DELETION_FOR_UPD_FNR, values, subscriber)== ErrorCode.SUCCESS)
				{
				//super.remove(ctx, obj);
					Logger.major(ctx, MsisdnBeforeDeletionSpgProvUpdateProxyHome.class, " As updation at external network Sucessful for "+cmdi+", Avoiding to remove the  external msisdn ["+msisdn.getMsisdn()+"] from the database with state held");
					return true;
				}
				else
				{
					//com.redknee.app.crm.home.sub.MsisdnBeforeDeletionSpgProvUpdateProxyHome.testThisCode("",ctx)
					Logger.major(ctx, MsisdnBeforeDeletionSpgProvUpdateProxyHome.class, " As updation at external network is not Sucessful,So Avoiding to remove the  external msisdn ["+msisdn.getMsisdn()+"] from the database with state held");
					return false;
				}
			}	
		}
		catch(Exception e)
		{
			Logger.major(ctx, MsisdnBeforeDeletionSpgProvUpdateProxyHome.class,"",e);
		}
		return false;

	}
	
    private static String[] getExtMsisdnDeletionCmd(Context ctx, int spid) throws HomeInternalException, HomeException 
    {
    	GrrGeneratorGeneralConfig grrGeneratorGeneralConfig = null;
    	String[] msisdnDeletionCmd = new String[2];
    	
		grrGeneratorGeneralConfig = HomeSupportHelper.get(ctx).findBean(ctx, GrrGeneratorGeneralConfig.class, spid);
		
		if(grrGeneratorGeneralConfig !=null)
		{
			if(grrGeneratorGeneralConfig.isUpdateFNR())
				msisdnDeletionCmd[0] = grrGeneratorGeneralConfig.getExtMsisdnDeletionCmd();
			
			if (grrGeneratorGeneralConfig.isUpdateNPGW())
				msisdnDeletionCmd[1] = grrGeneratorGeneralConfig.getNpGwMsisdnDeletionCmd();
		}
		else
		{
			String msg = "Error retrieving Grr General configuration for spid" + spid;
			new MajorLogMsg(ServiceProvisioningGatewaySupport.class, msg, null).log(ctx);
			
			throw new HomeException(msg);
		}
    	
    	return msisdnDeletionCmd;
    }   
    
    
	
	
}
