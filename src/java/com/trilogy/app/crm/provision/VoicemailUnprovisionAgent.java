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

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;

import com.trilogy.app.crm.LicenseConstants;
import com.trilogy.app.crm.bean.Service;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.bean.externalapp.ExternalAppEnum;
import com.trilogy.app.crm.support.EnumStateSupportHelper;
import com.trilogy.app.crm.support.ExternalAppSupportHelper;
import com.trilogy.app.crm.support.LicensingSupportHelper;
import com.trilogy.app.crm.support.SubscriberServicesSupport;
import com.trilogy.app.crm.voicemail.VoiceMailConstants;
import com.trilogy.app.crm.voicemail.VoiceMailServer;
import com.trilogy.app.crm.voicemail.VoiceMailService;
import com.trilogy.app.crm.voicemail.client.ExternalProvisionResult;
import com.trilogy.framework.xhome.beans.SafetyUtil;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xlog.log.MajorLogMsg;


/**
 * @author Prasanna.Kulkarni
 * @time Oct 19, 2005
 * 
 * 
 * Voicemail unprovisioning agent: This takes care of unprovisioning the voicemail service
 * from the voicemail server. Any voicemail service csr has unchecked or should be removed
 * because of priceplan change is removed from voicemail server by this agent This agent
 * also deals with any of the state/msisdn change and subscriber conversion change related
 * part because in all those cases we normally unprovision the service then provision the
 * servcie again. But for voicemail this is not so because upon reprovisioning the
 * subscriber will lose his/her voicemails and greetings For msisdn change we modify the
 * current user by sending the new user ID i.e. new MSISDN For state change, if the state
 * change is from active->suspended we deactivate the account; if its from suspend to
 * active we activate the account For subscriber conversion we dont do anything as
 * subscriber id changes during both post->pre and pre->post conversion but voicemail ahs
 * nothing to do with that id and msisdn changes during post->pre conversion but that will
 * be detected by msisdn change
 * 
 */
public class VoicemailUnprovisionAgent extends CommonUnprovisionAgent
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
            // Clean unprovisioning, must not be coming from remove, or service
            // unprovisioning
            // cron task
            unprovision(ctx, subSvcVO);
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
            unprovision(ctx, subSvcVO);
            break;
        }
    }


    public void storeHandler(Context ctx, SubscriberForServiceVO subSvcVO) throws AgentException
    {
        Subscriber oldSub = subSvcVO.oldSubscriber;
        Subscriber newSub = subSvcVO.newSubscriber;
        if (!SafetyUtil.safeEquals(oldSub.getMSISDN(), newSub.getMSISDN()))
        {
            // This single flag works for voicemail because any subscriber can have only
            // one voicemail
            // service at any point of time, this wont work if in the future Subscriber is
            // allowed to
            // have two voicemail services. In that case we need to have the same kind of
            // flag stored
            // in some hashpam inside SubscriberForServiceVO, with service as a key.
            if (!subSvcVO.msisdnChanged)
            {
                subSvcVO.msisdnChanged = true;
                onChangeMsisdn(ctx, oldSub, newSub);
            }
        }
        if (!oldSub.getSubscriberType().equals(newSub.getSubscriberType()))
        {
            onConversion(ctx, oldSub, newSub);
        }
        if (!oldSub.getState().equals(newSub.getState()))
        {
            if (!subSvcVO.stateChanged)
            {
                subSvcVO.stateChanged = true;
                onSubscriberStateChange(ctx, oldSub, newSub, subSvcVO);

                //If subscriber is deactivated then the servcie should be unprovisioned from Mpathix.
                if(newSub.getState() == SubscriberStateEnum.INACTIVE)
                {
                    unprovision(ctx, subSvcVO);
                    return;
                }
            }
        }
        if (isCleanUnProvision(ctx, subSvcVO))
        {
            unprovision(ctx, subSvcVO);
        }
    }


    /*
     * Only handle new service provisioning is handled over here, all the other things are
     * handled in the unprovision agent
     */
    private boolean isCleanUnProvision(Context ctx, SubscriberForServiceVO subVO) throws AgentException
    {
        Service currentService = null;
        try
        {
            currentService = getRelatedService(ctx);
        }
        catch (AgentException age)
        {
            new MajorLogMsg(this, "Could not find the service to unprovision", age).log(ctx);
            throw age;// why will one like to continue if he doesnt know what to
            // unprovsion?
        }
        if (currentService == null)
        {
            new MajorLogMsg(this, "Could not find the service to provision", null).log(ctx);
            throw new AgentException("Could not find the service to provision");
            // why will one like to continue if he doesnt know what to unprovsion?
        }
        Subscriber newSub = subVO.newSubscriber;
        Set provServices = subVO.provisionedServiceIdSet;
        for (Iterator itr = provServices.iterator(); itr.hasNext();)
        {
            Number svcID = (Number) itr.next();
            if (svcID.longValue() == currentService.getID())
            {
                // The service was provisoned previously now check if the service is not eligeble for provisioning

                Collection toHave = SubscriberServicesSupport.getServicesEligibleForProvisioning(ctx, newSub.getId());
                return !toHave.contains(svcID);
            }
        }
        return false;
    }


    public Service getRelatedService(Context ctx) throws AgentException
    {
        Service service = (Service) ctx.get(Service.class, null);
        if (service == null)
        { 
            service = (Service)ctx.get(com.redknee.app.crm.bean.core.Service.class);
            if(service == null)
            {
                throw new AgentException("System error: No service to provision");
            }
        }
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


    public void onConversion(Context ctx, Subscriber oldSub, Subscriber newSub) throws AgentException
    {
        // NOP
        /*
         * On Prepaid->Postpaid: msisdn doesnt change, so do nothing On Postpaid->Prepaid:
         * msisdn changes but onChangeMsisdn takes care of it so do nothing
         */
    }


    public void onSubscriberStateChange(Context ctx, Subscriber oldSub, Subscriber newSub, SubscriberForServiceVO subSvcVO) throws AgentException
    {
        if (!LicensingSupportHelper.get(ctx).isLicensed(ctx, LicenseConstants.TELUS_GATEWAY_LICENSE_KEY)) 
        {
            
        
        if (EnumStateSupportHelper.get(ctx).isTransition(oldSub, newSub, DEACTIVATION_STATES, ACTIVATION_STATES))
        {
            activateAccount(ctx, oldSub, newSub);
        }
        else
        {
            if (EnumStateSupportHelper.get(ctx).isTransition(oldSub, newSub, ACTIVATION_STATES, DEACTIVATION_STATES))
            {
                deActivateAccount(ctx, oldSub, newSub);
            }
        }
        
        }
    }


    public void onChangeMsisdn(Context ctx, Subscriber oldSub, Subscriber newSub) throws AgentException
    {
 
        if (!LicensingSupportHelper.get(ctx).isLicensed(ctx, LicenseConstants.TELUS_GATEWAY_LICENSE_KEY)) 
        {
        VoiceMailService vmServer = (VoiceMailService)ctx.get(VoiceMailServer.class); 
        final ExternalProvisionResult ret = vmServer.changeMsisdn(ctx, oldSub, newSub.getMSISDN()); 
        
        if ( ret.getCrmVMResultCode() != VoiceMailConstants.RESULT_SUCCESS)
        {
             throw new AgentException("Fail to activate voice mail for subscriber, " +
                     newSub.getId() + " result = " + ret);
 
        }
        }

    }


    private void activateAccount(Context ctx, Subscriber oldSub, Subscriber newSub) throws AgentException
    {
        Service currentService = getRelatedService(ctx);
        
        if (currentService == null)
        {
            new MajorLogMsg(this, "Could not find the service to provision", null).log(ctx);
            throw new AgentException("Could not find the service to provision");
        }

        VoiceMailService vmServer = (VoiceMailService)ctx.get(VoiceMailServer.class); 
        final ExternalProvisionResult ret = vmServer.activate(ctx, oldSub, currentService); 
        
        if ( ret.getCrmVMResultCode() != VoiceMailConstants.RESULT_SUCCESS)
        {
             throw new AgentException("Fail to activate voice mail for subscriber, " + 
                     newSub.getId() + " result = " + ret.getCrmVMResultCode());
 
        }

    }


    private void deActivateAccount(Context ctx, Subscriber oldSub, Subscriber newSub) throws AgentException
    {
       Service currentService = getRelatedService(ctx);
        
        if (currentService == null)
        {
            new MajorLogMsg(this, "Could not find the service to deactivate", null).log(ctx);
            throw new AgentException("Could not find the service to deactivate");
        }

        VoiceMailService vmServer = (VoiceMailService)ctx.get(VoiceMailServer.class); 
        final ExternalProvisionResult ret = vmServer.deactivate(ctx, oldSub, currentService); 
        
        if ( ret.getCrmVMResultCode() != VoiceMailConstants.RESULT_SUCCESS)
        {
             throw new AgentException("Fail to deactivate voice mail for subscriber, " + 
                     newSub.getId() + " result = " + ret.getCrmVMResultCode());
 
        }

    }


    public void unprovision(Context ctx, SubscriberForServiceVO subSvcVO) throws AgentException
    {
        Subscriber subscriber = null;
        if (subSvcVO != null)
        {
            subscriber = subSvcVO.newSubscriber;
        }
        else
        {
            subscriber = getRelatedSubscriber(ctx);
        }
        
        
        Service currentService = getRelatedService(ctx);
        
        if (currentService == null)
        {
            new MajorLogMsg(this, "Could not find the service to provision", null).log(ctx);
            throw new AgentException("Could not find the service to provision");
        }

        
        if (LicensingSupportHelper.get(ctx).isLicensed(ctx, LicenseConstants.TELUS_GATEWAY_LICENSE_KEY)) 
        {   
            this.callHlr(ctx, false, subscriber,  currentService, null, null);
        } else 
        {
            VoiceMailService vmServer = (VoiceMailService)ctx.get(VoiceMailServer.class); 
            final ExternalProvisionResult ret = vmServer.unprovision(ctx, subscriber, currentService); 
            if ( ret.getCrmVMResultCode() != VoiceMailConstants.RESULT_SUCCESS)
            {
                throw new ProvisionAgentException(ctx, ExternalAppSupportHelper.get(ctx)
                    .getUnprovisionErrorMessage(ctx, ExternalAppEnum.VOICEMAIL, ret.getCrmVMResultCode(), currentService), ret.getCrmVMResultCode(),
                    ExternalAppEnum.VOICEMAIL);
            }
        }
    }

    protected static final Collection<SubscriberStateEnum> DEACTIVATION_STATES =
        Collections.unmodifiableCollection(Arrays.asList(SubscriberStateEnum.SUSPENDED, SubscriberStateEnum.LOCKED));
    
    protected static final Collection<SubscriberStateEnum> ACTIVATION_STATES =
        Collections.unmodifiableCollection(Arrays.asList(SubscriberStateEnum.ACTIVE, SubscriberStateEnum.PROMISE_TO_PAY));
}
