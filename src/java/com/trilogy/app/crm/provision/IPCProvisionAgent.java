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
package com.trilogy.app.crm.provision;

import java.util.HashSet;
import java.util.Set;

import com.trilogy.app.crm.Common;
import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.CRMSpidHome;
import com.trilogy.app.crm.bean.PricePlan;
import com.trilogy.app.crm.bean.ServiceTypeEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.bean.core.Service;
import com.trilogy.app.crm.bean.externalapp.ExternalAppEnum;
import com.trilogy.app.crm.bean.ipc.IpcProvConfig;
import com.trilogy.app.crm.client.ipcg.IpcgClient;
import com.trilogy.app.crm.client.ipcg.IpcgClientFactory;
import com.trilogy.app.crm.client.ipcg.IpcgClientProxy;
import com.trilogy.app.crm.support.BillCycleSupport;
import com.trilogy.app.crm.support.ExternalAppSupportHelper;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.EntryLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.OMLogMsg;
import com.trilogy.product.s5600.ipcg.provisioning.ResponseCode;


/**
 * @author rattapattu
 */
public class IPCProvisionAgent extends CommonProvisionAgent
{

    public final static short DEFAULT_BILLCYCLEDATE = 1;
    
    public final static int ERRORCODE_EXISTS = 2;   //NGRC rc
    
    public static final String SKIP_DATA_SVC_CHECK = "SKIP_DATA_SVC_CHECK";
    /*
     * (non-Javadoc)
     * 
     * @see com.redknee.framework.xhome.context.ContextAgent#execute(com.redknee.framework.xhome.context.Context)
     */
    public void execute(Context ctx) throws AgentException
    {
        Subscriber subscriber;
        IpcgClient ipcgClient;
        PricePlan pricePlan;
        Account account;
        Service service;
        boolean isBlackberryService = false;

        subscriber = (Subscriber) ctx.get(Subscriber.class);
        if (subscriber == null)
        {
            throw new AgentException(
                    "System error: No subscriber to provision");
        }
        Set svcSet = subscriber.getServices(ctx);
        // TT#13112159022 - when call comes from MOVE pipeline, new subscriber doesn't have any services coming from ctx 
        // HLR command should still be sent for these data services        
        if (ctx.getBoolean(SKIP_DATA_SVC_CHECK, Boolean.FALSE)== Boolean.TRUE || IpcgClientProxy.hasDataService(ctx, svcSet))
        {
        //  since it has come to IPCG agent via IPCG handler it IS data service, so legacy check of hasDataService may not be required
            ipcgClient = IpcgClientFactory.locateClient(ctx, subscriber.getTechnology());
            if (ipcgClient == null)
            {
                throw new AgentException("System error: IpcgClient not found in context");
            }
        }
        else
        {   //If sub. not have the data service than no need to provison it on IPCG and HLR 
            return;
        }   

        try
        {
            //As of CRM 8.2, the Rate Plan information is stored in Price Plan
            pricePlan = subscriber.getRawPricePlanVersion(ctx).getPricePlan(ctx);
        }
        catch (HomeException e)
        {
            throw new AgentException(e.getMessage(), e);
        }
        if (pricePlan == null)
        {
            throw new AgentException(
                    "System error: No price plan associated with subscriber");
        }
        account = (Account) ctx.get(Account.class);
        if (account == null)
        {
            throw new AgentException(
                    "System error: subscriber's account not found");
        }

        service = (Service) ctx.get(Service.class);
        if (service == null)
        {
            throw new AgentException(
                    "System error: Service for IPC provisioning not found in context");
        }
        else if (ServiceTypeEnum.BLACKBERRY.equals(service.getType()))
        {
            isBlackberryService = true;
        }
        

        // Set BillCycleDate
        short billCycleDate = DEFAULT_BILLCYCLEDATE;
       
        try
        {
            billCycleDate = (short)(BillCycleSupport.getBillCycleForBan(ctx,subscriber.getBAN())).getDayOfMonth();
        }
        catch (HomeException e1)
        {
            // will use the default
        }
        
        IpcProvConfig config = (IpcProvConfig)ctx.get(IpcProvConfig.class); 
        if (config == null)
        {
           throw new AgentException("System error: IpcProvConfig not found in context");
        }
        
        String timeZone = config.getTimezone();
        String ratePlan;
        
        // UMP-3348: Allow Data service for broadband and voice for wireline subscriptions
        // hack fix to support Multiplay capability, as legacy interfaces did not consider subscription type other than AIRTIME
        if(allowMultiSubPerAccount(ctx, account))
        {
        	ratePlan = String.valueOf(subscriber.getSubscriptionType());
        }
        else if(config.isSupportsPriceToRatePlanMapping())
        {
            // TODO - Bad we do this lossy cast...it would go away when we move Data to URCS
            ratePlan = String.valueOf(pricePlan.getId());
        } else
        {
            new DebugLogMsg(this, "Support of Price Plan to Rate Plan mapping is disabled. Using Rate Plan ID for Subscriber Data rate plan association.", null).log(ctx);
            ratePlan = pricePlan.getDataRatePlan();
        }
        
        
        int scpId = config.getScpId();
        boolean subBasedRatingEnabled = config.getSubBasedRatingEnabled();
        int serviceGrade = (subscriber.getSubscriberType() == SubscriberTypeEnum.POSTPAID)? config.getPostpaidServiceGrade() : config.getPrepaidServiceGrade();
        int result = -1;
        
        try
        {
            result = ipcgClient.addSub(ctx, subscriber, billCycleDate, timeZone, Integer.parseInt(ratePlan), scpId,
                    subBasedRatingEnabled, serviceGrade);
        }
        catch(Exception e)
        {
            result = -1;
            LogSupport.major(ctx,this,"Error provisioning to IPC",e);
        }
        
        //case: Where susbcriber profile already exists on ipc
        if(requiresUpdate(config, result) )
        {
            try
            {
                result = ipcgClient.addChangeSub(ctx, subscriber, billCycleDate, Integer.valueOf(ratePlan),serviceGrade);
            }
            catch(Exception e)
            {
                result = -1;
                LogSupport.major(ctx,this,"Error provisioning to IPC",e);
            }
        }
        
        if (result != ResponseCode.SUCCESS)
        {
            final String msg = "Data service " + getServiceDescription(service) + " provisioning failed: " + result;
            new OMLogMsg(Common.OM_MODULE, Common.OM_IPC_ERROR).log(ctx);
            //SNMP Trap: IPCProvisionAgent - fail to add subcriber - result code = {0}
            new EntryLogMsg(13779, this, "", subscriber.toString(), new java.lang.String[]
                {String.valueOf(result)}, null).log(ctx);
            throw new ProvisionAgentException(ctx, ExternalAppSupportHelper.get(ctx).getProvisionErrorMessage(ctx,
                    ExternalAppEnum.DATA, result, service), result, ExternalAppEnum.DATA);
        }

        // Only send HLR commands if this is not a blackberry service. Otherwise it will have been sent earlier.
        if (!isBlackberryService)
        {
            String hlrCmds = null;
            short hlrId = subscriber.getHlrId();
            //hlr
            if (subscriber.getSubscriberType() == SubscriberTypeEnum.POSTPAID)
            {
    
                hlrCmds = service.getProvisionConfigs();
            }
            else if (subscriber.getSubscriberType() == SubscriberTypeEnum.PREPAID)
            {
                hlrCmds = service.getPrepaidProvisionConfigs();
            }
    
            if ((hlrCmds == null) || (hlrCmds.length() == 0))
            {
                // no HLR commands configured means nothing to do
                return;
            }
            
            // execute HLR commands using HLR client
            callHlr(ctx,true,subscriber,service,null,null);
        }

    }
    
    
    private boolean allowMultiSubPerAccount(final Context context, final Account subscriberAccount)
    throws AgentException
	{
	    final int spid = subscriberAccount.getSpid();
	
	    final Home home = (Home)context.get(CRMSpidHome.class);
	    try
	    {
	    	final CRMSpid serviceProvider = (CRMSpid)home.find(context, Integer.valueOf(spid));
	    	if (serviceProvider == null)
		    {
		        throw new AgentException(
		            "Failed to locate service provider profile " + spid + " for account " + subscriberAccount.getBAN());
		    }
	    	return serviceProvider.isAllowMultiSubForOneAccount();
	    }
	    catch(HomeException he)
	    {
	    	throw new AgentException(
		            "Exception while looking for spid " + spid + " for account " + subscriberAccount.getBAN() +" "+ he.getMessage());
	    }
	}

    private boolean requiresUpdate(IpcProvConfig config, int result)
    {
        return config.getUseLegacyResponseCodes() && retryResponseCode_.contains(result) ||
            !config.getUseLegacyResponseCodes()&& retryLegacyResponseCode_.contains(result);
    }
    
    /*
    public void callHlr(Context ctx, short hlrId, String hlrCmds, Subscriber subscriber) throws AgentException
    {
        if (!hasMsisdnJustRemovedFromHlr(ctx, subscriber))
        {
            final CrmHlrServicePipelineImpl hlrService = CrmHlrServicePipelineImpl.instance();
    

            
            try 
            {
                // execute HLR commands using HLR client
            	String request = CommonProvisionAgentBase.replaceHLRCommand(ctx,  hlrCmds, subscriber, null, null);
                hlrService.process(ctx, hlrId,request);

            }
            catch (Exception e)
            {
                throw new AgentException(e);
            }
        }

    }
    */
    

    
    protected static Set<Integer> retryLegacyResponseCode_ = new HashSet<Integer>();
    static 
    {
        retryLegacyResponseCode_.add(ResponseCode.MSISDN_ALREADY_EXISTS);
    }
    
    protected static Set<Integer> retryResponseCode_ = new HashSet<Integer>();
    static 
    {
        retryResponseCode_.add(ERRORCODE_EXISTS);
    }
    
}
