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
package com.trilogy.app.crm.provision;

import java.util.Iterator;
import java.util.Set;

import com.trilogy.app.crm.LicenseConstants;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.core.Service;
import com.trilogy.app.crm.bean.externalapp.ExternalAppEnum;
import com.trilogy.app.crm.support.ExternalAppSupportHelper;
import com.trilogy.app.crm.support.LicensingSupportHelper;
import com.trilogy.app.crm.voicemail.VoiceMailConstants;
import com.trilogy.app.crm.voicemail.VoiceMailServer;
import com.trilogy.app.crm.voicemail.VoiceMailService;
import com.trilogy.app.crm.voicemail.client.ExternalProvisionResult;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xlog.log.MajorLogMsg;


/**
 * @author Prasanna.Kulkarni
 * @time Oct 19, 2005
 * 
 * Voicemail provisioning agent: This takes care of provisioning the voicemail service to
 * the voicemail server This agent does not deal with any fo the state/msisdn change
 * subscriber conversion change related part It simply ignore all that and provisions the
 * supplied service only if its a fresh new service added to the subscriber becasue its a
 * mandatory voicemail service or subscriber has selected that service later
 */
public class VoicemailProvisionAgent extends CommonProvisionAgent
{

    public void execute(Context ctx) throws AgentException
    {
        if (!LicensingSupportHelper.get(ctx).isLicensed(ctx, LicenseConstants.LICENSE_APP_CRM_VOICE_MAIL))
        {
            throw new AgentException("No Voice mail license, can not provision"); 
        }
        
        SubscriberForServiceVO subSvcVO = (SubscriberForServiceVO) ctx.get(SubscriberForServiceVO.class);
        // actually subSvcVO.oldSubscriber should not ever happen
        if (subSvcVO == null || subSvcVO.oldSubscriber == null)
        {
            // Clean provisioning, must not be coming from create, or service provisioning
            // cron task
            provision(ctx, subSvcVO);
            return;
        }
        // This is particulary from subscriber->store, This block will get called if we
        // remove
        // any voicemail service after creating the subscriber
        int mode = subSvcVO.callerID;
        switch (mode)
        {
        case SubscriberForServiceVO.CALLED_FROM_STORE:
            storeHandler(ctx, subSvcVO);
            break;
        default:
            // nothing special currently for default.
            provision(ctx, subSvcVO);
            break;
        }
    }


    public void storeHandler(Context ctx, SubscriberForServiceVO subSvcVO) throws AgentException
    {
        if (isCleanProvision(ctx, subSvcVO))
            provision(ctx, subSvcVO);
        /*
         * Currently ignore all the other cases unprovision agent is supposed to take care
         * of all this as it's the one which always gets called first in case we are
         * unprovsioning the same service just for state/msisdn change
         */
        /*
         * Subscriber oldSub = subSvcVO.oldSubscriber; Subscriber newSub =
         * subSvcVO.newSubscriber; if (!SafetyUtil.safeEquals(oldSub.getMSISDN(),
         * newSub.getMSISDN())) { // onChangeMsisdn(ctx, oldSub, newSub); } if
         * (!oldSub.getSubscriberType().equals(newSub.getSubscriberType())) { //
         * onConversion(ctx, oldSub, newSub); } if
         * (!oldSub.getState().equals(newSub.getState())) { //
         * onSubscriberStateChange(ctx, oldSub, newSub); }
         */
    }


    /*
     * Only handle new service provisioning is handled over here, all the other things are
     * handled in the unprovision agent
     */
    private boolean isCleanProvision(Context ctx, SubscriberForServiceVO subVO) throws AgentException
    {
        Service currentService = getRelatedService(ctx);
       
        if (currentService == null)
        {
            new MajorLogMsg(this, "Could not find the service to provision", null).log(ctx);
            throw new AgentException("Could not find the service to provision");
            // why will one like to continue if he doesnt know what to provsion?
        }
        
        Set provServices = subVO.provisionedServiceIdSet;
        for (Iterator itr = provServices.iterator(); itr.hasNext();)
        {
            Number svcID = (Number) itr.next();
            if (svcID.longValue() == currentService.getID())
                return false;// this means its not a new service provisioning something
            // like msisdn,state change invoked provisioned agent
        }
        return true;
    }


    public Service getRelatedService(Context ctx) 
    {
        Service service = (Service) ctx.get(Service.class, null);
         return service;
    }


    public Subscriber getRelatedSubscriber(Context ctx) throws AgentException
    {
        Subscriber subscriber = (Subscriber) ctx.get(Subscriber.class, null);
        if (subscriber == null)
        {
            throw new AgentException("System error: No subscriber to provision");
        }
        return subscriber;
    }

 

    public void provision(Context ctx, SubscriberForServiceVO subSvcVO) throws AgentException
    {        
        Subscriber subscriber = null;
        if (subSvcVO != null)
            subscriber = subSvcVO.newSubscriber;
        else
            subscriber = getRelatedSubscriber(ctx);
        
        
        Service currentService = getRelatedService(ctx);
        
        if (currentService == null)
        {
            new MajorLogMsg(this, "Could not find the service to provision", null).log(ctx);
            throw new AgentException("Could not find the service to provision");
        }

        if (LicensingSupportHelper.get(ctx).isLicensed(ctx, LicenseConstants.TELUS_GATEWAY_LICENSE_KEY)) 
        {   
            this.callHlr(ctx, true, subscriber,  currentService, null, null);
        } 
        else 
        {
            VoiceMailService vmServer = (VoiceMailService)ctx.get(VoiceMailServer.class); 
            final ExternalProvisionResult ret = vmServer.provision(ctx, subscriber, currentService); 
            if ( ret.getCrmVMResultCode()!= VoiceMailConstants.RESULT_SUCCESS)
            {
                throw new ProvisionAgentException(ctx, ExternalAppSupportHelper.get(ctx)
                    .getProvisionErrorMessage(ctx, ExternalAppEnum.VOICEMAIL, ret.getCrmVMResultCode(), currentService), ret.getCrmVMResultCode(),
                    ExternalAppEnum.VOICEMAIL);
            }
        }   
         
    }
    
 }
