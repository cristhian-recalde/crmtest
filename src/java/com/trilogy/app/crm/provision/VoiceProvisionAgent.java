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

import com.trilogy.app.crm.Common;
import com.trilogy.app.crm.amsisdn.AdditionalMsisdnAuxiliaryServiceSupport;
import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.CRMSpidHome;
import com.trilogy.app.crm.bean.core.Service;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.externalapp.ExternalAppEnum;
import com.trilogy.app.crm.client.AppEcpClient;
import com.trilogy.app.crm.client.AppEcpClientSupport;
import com.trilogy.app.crm.config.AppEcpClientConfig;
import com.trilogy.app.crm.support.CallingGroupSupport;
import com.trilogy.app.crm.support.ExternalAppSupportHelper;
import com.trilogy.app.crm.support.SpidSupport;
import com.trilogy.app.crm.support.SubscriberAuxiliaryServiceSupport;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.EntryLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MinorLogMsg;
import com.trilogy.framework.xlog.log.OMLogMsg;


/**
 * Voice provisioning agent.
 *
 * @author candy.wong@redknee.com
 */
public class VoiceProvisionAgent extends CommonProvisionAgent
{

    /**
     * <p>
     * Installs voice services to HLR and AppEcp.
     * </p>
     * <p>
     * Context must contain the subscriber to be installed keyed by Subscriber.class
     * </p>
     * <p>
     * Context must contain Service to retrieve additional params needed associated with
     * this service
     * </p>
     * <p>
     * Context must contain AppEcpClient to provision AppEcp using CORBA
     * </p>
     * <p>
     * Context must contain Account of the subscriber
     * </p>
     *
     * @param ctx
     *            The operating context.
     * @throws AgentException
     *             Thrown if there are problems installing the services.
     */
    public void execute(final Context ctx) throws AgentException
    {
        provisionVoice(ctx,PROVISION_AGENT);
    }


    /**
     * Reprovisions AMSISDN to ECP.
     *
     * @param context
     *            The operating context.
     * @param subscriber
     *            The subscriber being updated.
     */
    protected void updateAMsisdn(final Context context, final Subscriber subscriber)
    {
        if (AdditionalMsisdnAuxiliaryServiceSupport.isAdditionalMsisdnEnabled(context))
        {
            try
            {
                AdditionalMsisdnAuxiliaryServiceSupport.syncSubscriberAMsisdnWithEcp(context, subscriber);
            }
            catch (final HomeException exception)
            {
                if (LogSupport.isDebugEnabled(context))
                {
                    final StringBuilder sb = new StringBuilder();
                    sb.append(exception.getClass().getSimpleName());
                    sb.append(" caught when attempting to reprovision AMSISDNs to ECP: ");
                    if (exception.getMessage() != null)
                    {
                        sb.append(exception.getMessage());
                    }
                    LogSupport.info(context, this, sb.toString(), exception);
                }
            }
        }
    }


    /**
     * Set the FF Rating Flag for the given subscriber if any Calling Group service(s)
     * is/are associated with the subscriber.
     *
     * @param context
     *            The operating context.
     * @param subscriber
     *            The subscriber for which to determine if the FF Rating Flag needs to be
     *            set.
     */
    protected void setFFRatingFlagIfCallingGroupsExist(final Context context, final Subscriber subscriber)
    {
        try
        {
            if (CallingGroupSupport.hasProvisionedCallingGroupService(context, subscriber))
            {
                final AppEcpClient client = (AppEcpClient) context.get(AppEcpClient.class);

                final short result = client.setFriendsAndFamilyEnabled(subscriber.getMSISDN(), true);

                if (result != 0)
                {
                    throw new HomeException(
                        "Failed to update Friends and Family information in ECP for subscriber " + subscriber.getId());
                }
            }
        }
        catch (final HomeException e)
        {
            new MinorLogMsg(this, e.getMessage(), null).log(context);
        }
    }


    /**
     * Resets homezone flag on ECP.
     *
     * @param ctx
     *            The operating context.
     * @param sub
     *            The subscriber being updated.
     */
    protected void setHZFlagIfHomeZoneExists(final Context ctx, final Subscriber sub)
    {
        /*
         * Prasanna: This is weird, this stuff should go into subscriberhomezonehome's
         * store method where we check change msisdn, but this agent will anyhow overwrite
         * that change so no other option :(
         */

        SubscriberAuxiliaryServiceSupport.enableHZIfRequired(ctx, sub);
    }
    
    
    private int getEcpState(final Context ctx, final String agentType, final Subscriber sub)
    {
        // add subscriber to ECP
        int ecpState = -1;
        // This is avoid suspending the voice service on a resume
        if ( agentType.equals(RESUME_AGENT))
        {
            if ( sub.isPostpaid() )
            {
                ecpState = AppEcpClientSupport.mapPostpaidEcpState(ctx,sub.getState());
            }
            else
            {
                ecpState = AppEcpClientSupport.mapPrepaidEcpState(ctx, sub.getState());
            }
        }
        else
        {
            ecpState = AppEcpClientSupport.mapToEcpState(ctx, sub);   
        }
        return ecpState;
    }
    
    /**
     * <p>
     * Install/Resume voice services to HLR and AppEcp.
     * </p>
     * <p>
     * Context must contain the subscriber to be installed keyed by Subscriber.class
     * </p>
     * <p>
     * Context must contain Service to retrieve additional params needed associated with
     * this service
     * </p>
     * <p>
     * Context must contain AppEcpClient to provision AppEcp using CORBA
     * </p>
     * <p>
     * Context must contain Account of the subscriber
     * </p>
     *
     * @param ctx
     *            The operating context.
     * @param agentType
     *            If the agentType is provision or resume            
     * @throws AgentException
     *             Thrown if there are problems installing the services.
     */
    protected void provisionVoice(final Context ctx, final String agentType) throws AgentException
    {
        AppEcpClient appEcpClient = null;
        Subscriber subscriber = getSubscriber(ctx);
        Service service = getService(ctx);
        Account account = getAccount(ctx);
        String hlrCmds = getHlrCommand(ctx,subscriber,service);
        CRMSpid spid = null;
        int result;

        if (LogSupport.isDebugEnabled(ctx))
        {
            new DebugLogMsg(this, "Provisioning Voice Services ( " + service.getName() + " ) for " + 
                    subscriber.getMSISDN() , null).log(ctx);
        }

        AppEcpClientConfig config = null;
        config = (AppEcpClientConfig) ctx.get(AppEcpClientConfig.class);
        if (config == null)
        {
            throw new AgentException("System error: AppEcpClientConfig not found in context");
        }
        appEcpClient = (AppEcpClient) ctx.get(AppEcpClient.class);
        if (appEcpClient == null)
        {
            throw new AgentException("System error: AppEcpClient not found in context");
        }

        // Determine the ECP Class of Service for this subscriber.
        int class_of_service = 0;
        try
        {
            class_of_service = config.getClassOfService(ctx, account.getSpid(), account.getType(), subscriber
                .getSubscriberType());
            spid = SpidSupport.getCRMSpid(ctx, account.getSpid());
        }
        catch (final HomeException e)
        {
            throw new AgentException(e.getMessage());
        }

        int ecpState = getEcpState(ctx, agentType, subscriber);

        String groupMSISDN = subscriber.getGroupMSISDN(ctx);
        if( "".equals(groupMSISDN) )
        {
            // This check is required because as of CRM 7.5, the group MSISDN field in CRM and ABM is blank
            // for non-pooled subscribers.  Previous versions provisioned ECP with group MSISDN = MSISDN.
            groupMSISDN = subscriber.getMSISDN();
        }
        
        // UMP-3348: Allow Data service for broadband and voice for wireline subscriptions
        // hack fix to support Multiplay capability, as legacy interfaces did not consider subscription type other than AIRTIME
        // Rate plan info will now be treated as subscription type by the URCS ECP interface
        int ratePlan = 0; 
        if(allowMultiSubPerAccount(ctx, account))
        {
        	Long subType = subscriber.getSubscriptionType();
        	ratePlan = subType.intValue();
        }        
        
        result = appEcpClient.addSubscriber(
                subscriber.getMSISDN(), 
                account.getSpid(), 
                subscriber.getIMSI(), 
                account.getCurrency(), 
                ratePlan, //As of CRM 8.2, Rate Plan information is in Price Plan and it is ignored when pushed to the subscriber through this interface 
                config.getExpiry(), 
                class_of_service, 
                ecpState, 
                config.getPin(), 
                config.getLanguage(), 
                spid.getTimezone(), 
                groupMSISDN);

        if (result != 0)
        {
            // Try to overwrite the ECP profile.
            final int ec = appEcpClient.updateSubscriber(
                    subscriber.getMSISDN(), 
                    account.getSpid(), 
                    subscriber.getIMSI(), 
                    account.getCurrency(), 
                    ratePlan, //As of CRM 8.2, Rate Plan information is in Price Plan and it is ignored when pushed to the subscriber through this interface 
                    config.getExpiry(), 
                    class_of_service, 
                    ecpState, 
                    config.getPin(), 
                    config.getLanguage(), 
                    spid.getTimezone(), 
                    groupMSISDN);
            if (ec != 0)
            {
                new OMLogMsg(Common.OM_MODULE, Common.OM_ECP_ERROR).log(ctx);
                new EntryLogMsg(10361, this, "", subscriber.toString(), new java.lang.String[]
                {
                    String.valueOf(result)
                }, null).log(ctx);
                throw new ProvisionAgentException(ctx, ExternalAppSupportHelper.get(ctx).getProvisionErrorMessage(ctx,
                        ExternalAppEnum.VOICE, result, service), result, ExternalAppEnum.VOICE);
            }
        }

        /*
         * TT 7082700027: Update AMSISDNs on ECP.
         */
        updateAMsisdn(ctx, subscriber);

        // Set the Friends & Family Rating flag if any Calling Group service(s)
        // is/are associated with the subscriber.
        setFFRatingFlagIfCallingGroupsExist(ctx, subscriber);
        setHZFlagIfHomeZoneExists(ctx, subscriber);

        callHlr(ctx, true, subscriber, service,null, null);
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
    
    
    public static String PROVISION_AGENT = "Provision";
    public static String RESUME_AGENT = "Resume";
}
