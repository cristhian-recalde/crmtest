/*
 * SubscriberPinManagerUpdateHome.java
 * 
 * Author : danny.ng@redknee.com Date : Mar 10, 2006
 * 
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
package com.trilogy.app.crm.home.sub;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.CRMSpidHome;
import com.trilogy.app.crm.bean.PinProvisioningStatusEnum;
import com.trilogy.app.crm.bean.Service;
import com.trilogy.app.crm.bean.ServiceHome;
import com.trilogy.app.crm.bean.ServiceProvisionStatusEnum;
import com.trilogy.app.crm.bean.ServiceTypeEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.provision.gateway.ServiceProvisioningGatewaySupport;
import com.trilogy.app.crm.support.Lookup;
import com.trilogy.app.crm.support.PinManagerSupport;
import com.trilogy.app.crm.support.SubscriberServicesSupport;
import com.trilogy.app.crm.support.SubscriberSupport;
import com.trilogy.framework.xhome.beans.ExceptionListener;
import com.trilogy.framework.xhome.beans.SafetyUtil;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MajorLogMsg;


/**
 * Provision the subscriber to PIN Manager
 * 
 * This home takes care of Generating a PIN for the claimed Subscription MSISDN
 * (if it has not already been generated), when the Subscription changes from
 * Available/Pending states to Active state.
 * 
 * This home decorates the Subscriber Home Pipeline.
 * 
 * All exceptions while trying to Generate the Pin are quietly suppressed on the GUI
 * but are included in the Appplication logs.
 * 
 * @author danny.ng@redknee.com
 *         deepak.mishra@redknee.com 
 *         angie.li@redknee.com
 */
public class SubscriberPinManagerUpdateHome extends HomeProxy
{

    /**
     * Generated UID
     */
    private static final long serialVersionUID = 6814808804895056406L;


    public SubscriberPinManagerUpdateHome(Context ctx, Home home)
    {
        super(ctx, home);
    }


    /* (non-Javadoc)
     * @see com.redknee.framework.xhome.home.HomeSPI#create(com.redknee.framework.xhome.context.Context, java.lang.Object)
     */
    public Object create(Context ctx, Object obj) throws HomeException, HomeInternalException
    {
    	LogSupport.debug(ctx, this, "SubscriberPipeline[create].....");
    	Subscriber sub = (Subscriber) super.create(ctx, obj);
        if (isApplicalble(ctx, sub))
        {
            new DebugLogMsg(this, "Skipping PIN GENERATION Pool-Subscription [" + sub.getId() + "]", null).log(ctx);
            // Only generate the PIN if the subscription is activating.
            if (sub.getState().equals(SubscriberStateEnum.ACTIVE))
            {
                logDebugMsg(ctx,
                        "Subscription is being created in the Active state, CRM will attempt to create a PIN for this subscription.");
                generatePin(ctx, sub);
            }
        }
        return sub;
        
    }


    /* (non-Javadoc)
     * @see com.redknee.framework.xhome.home.HomeSPI#store(com.redknee.framework.xhome.context.Context, java.lang.Object)
     */
    public Object store(Context ctx, Object obj) throws HomeException, HomeInternalException
    {
    	LogSupport.debug(ctx, this, "SubscriberPipeline[store].....");
    	Subscriber newSub = (Subscriber) obj;
        if (isApplicalble(ctx, newSub))
        {
            new DebugLogMsg(this, "Skipping PIN UPDATE Pool-Subscription [" + newSub.getId() + "]", null).log(ctx);
            Subscriber oldSub = (Subscriber) ctx.get(Lookup.OLDSUBSCRIBER);
            if (!SafetyUtil.safeEquals(oldSub.getMSISDN(), newSub.getMSISDN()))
            {
                onMsisdnChange(ctx, oldSub, newSub);
            }
            if (!SafetyUtil.safeEquals(oldSub.getState(), newSub.getState()))
            {
                onStateChange(ctx, oldSub, newSub);
            }else if(syncPinToVMProvider(ctx,newSub.getSpid()) && isVoiceMailServiceProvisioned(ctx,oldSub,newSub))
            {
            	regeneratePin(ctx,newSub);
            }
        }
        return super.store(ctx, obj);
    }


    /* (non-Javadoc)
     * @see com.redknee.framework.xhome.home.HomeSPI#remove(com.redknee.framework.xhome.context.Context, java.lang.Object)
     */
    public void remove(Context ctx, Object obj) throws HomeException, HomeInternalException
    {
    	LogSupport.debug(ctx, this, "SubscriberPipeline[remove].....");
    	Subscriber sub = (Subscriber) obj;
        if (isApplicalble(ctx, sub))
        {
            new DebugLogMsg(this, "Skipping PIN DELETE Pool-Subscription [" + sub.getId() + "]", null).log(ctx);
            if (LogSupport.isDebugEnabled(ctx))
            {
                logDebugMsg(ctx,
                        "Due to Subscription delete, CRM will attempt to delete the PIN for the Subscription's MSISDN.");
            }
            deletePin(ctx, sub);
        }
        super.remove(ctx, obj);
    }


    /**
     * On a MSISDN change for Subscription, 
     * 1) generate a new PIN for the new MSISDN (if it is not yet provisioned
     * on PIN Manager), and 
     * 2) delete the PIN for the original MSISDN, if the MSISDN is no longer associated
     * to any other active Subscriptions.  If the MSISDN has existing associations, then 
     * leave the existing PIN as it is.
     * 
     * Change of MSISDN by MsisdnOwnereship (GUI: MSISDN Management) will perform a Subscriber.store for all
     * Subscriptions associated with the ORIGINAL MSISDN.  So this code will also be run in that case.
     * @param ctx
     * @param oldSub
     * @param newSub
     * @throws HomeException
     */
    public void onMsisdnChange(Context ctx, Subscriber oldSub, Subscriber newSub) throws HomeException
    {
        SubscriberStateEnum state = newSub.getState();
        if (state != SubscriberStateEnum.INACTIVE && state != SubscriberStateEnum.AVAILABLE
                && state != SubscriberStateEnum.PENDING)
        {
            changePinMsisdn(ctx, oldSub, newSub);
        }
    }


    /**
     * Handles any PIN Manager profile related updates on subscriber
     * state change.
     * 
     * @param ctx
     * @param oldSub
     * @param newSub
     * @throws HomeException
     */
    public void onStateChange(Context ctx, Subscriber oldSub, Subscriber newSub) throws HomeException
    {
        /*
         * Deactivating sub, we remove PIN profile.
         */
        if (oldSub.getState() != SubscriberStateEnum.INACTIVE && newSub.getState() == SubscriberStateEnum.INACTIVE)
        {
            logDebugMsg(ctx, 
                    "Due to Subscription state change to INACTIVE state, the PIN will be deleted from Pin Manager for MSISDN=" 
                    + newSub.getMSISDN());
            deletePin(ctx, newSub);
        }
        /*
         * Check if profile exists in PIN Manager when transitioning
         * from pending (postpaid) or available (prepaid) to active state 
         * if it doesn't exist, create it.
         * We don't want to support the subscription reactivation case.
         */
        else if ((oldSub.getState() == SubscriberStateEnum.PENDING || oldSub.getState() == SubscriberStateEnum.AVAILABLE)
                && newSub.getState() == SubscriberStateEnum.ACTIVE )
        {
            try
            {
                logDebugMsg(ctx, "Subscription [" + newSub.getId() + "] with MSISDN [" + newSub.getMSISDN()
                        + "] is Activating for the first time, CRM will generate a PIN in Pin Manager for the MSISDN="
                        + newSub.getMSISDN());
                generatePin(ctx, newSub);
            }
            catch (HomeException e)
            {
                LogSupport.info(ctx, this, "For Subscription [" + newSub.getId() + "] with MSISDN ["
                        + newSub.getMSISDN()
                        + "], problem occured in PIN generation during state transition from Pending State.", e);
                throw e;
            }
        }
        /* 
         * Since CRM 8.0, we no longer support Subscription reactivation.
         */
    }
    
    /**
     * Reset Pin in case VM service is provisioned for existing subscriber (added as optional service 
     * or PP change).This will be done only when TCB pin is used in VM system.
     * @param ctx
     * @param msisdn
     */
    public void regeneratePin(Context ctx,Subscriber sub) throws HomeException
    {
    	String msisdn = sub.getMsisdn();
    	if (PinManagerSupport.getStateOnPinManager(ctx, msisdn).equals(PinProvisioningStatusEnum.PROVISIONED))
        {
    		try
            {
                PinManagerSupport.resetPin(ctx, msisdn, PinManagerSupport.ER_REFERENCE);
            }
            catch (HomeException e)
            {
                // Log the error to the application log and to the screen.
                if (LogSupport.isDebugEnabled(ctx))
                {
                    LogSupport.debug(ctx, SubscriberPinManagerUpdateHome.class,
                            "Encountered a error while trying to resetPIN for MSISDN [" + msisdn + "] in PIN "
                                    + "Manager.  Subscription update will continue.", e);
                }
                String msg = "Encountered a error while trying to resetPIN in PIN Manager.  <br/>"
                        + "VM PIN for this Mobile Number [" + msisdn
                        + "] could not be set automatically. Attempt resetting the PIN manually.";
                ExceptionListener el = (ExceptionListener) ctx.get(ExceptionListener.class);
                if (el != null)
                {
                    el.thrown(new HomeException(msg, e));
                }
            }

        }
    	else if(sub.getState() == SubscriberStateEnum.ACTIVE)
        {
            new DebugLogMsg(this, "No pin exists for subscriber , Generating new pin").log(ctx);
            generatePin(ctx,sub);
        }
    }
    
    /**
     * Generates a new PIN in the Pin Manager Client for the given Subscription MSISDN, if 
     * the MSISDN doesn't have an existing PIN.
     * 
     * If there exist a PIN, log debug message and continue. 
     * @param ctx
     * @param msisdn Mobile Number for which the Pin will be generated on Pin Manager.
     * @throws HomeException
     */
    private void generatePin(Context ctx, Subscriber sub)
        throws HomeException
    {
        // check if the MSISDN is currently used by some other subscription
        final String msisdn = sub.getMSISDN();
        Account account = (Account) ctx.get(Account.class);
        if (account==null || !account.getBAN().equals(sub.getBAN()))
        {
            account = sub.getAccount(ctx);
        }
        
        if (PinManagerSupport.getStateOnPinManager(ctx, msisdn).equals(PinProvisioningStatusEnum.UNPROVISIONED))
        {
            try
            {
                PinManagerSupport.generatePin(ctx, msisdn, account, PinManagerSupport.ER_REFERENCE);
            }
            catch (HomeException e)
            {
                // Log the error to the application log and to the screen.
                if (LogSupport.isDebugEnabled(ctx))
                {
                    LogSupport.debug(ctx, SubscriberPinManagerUpdateHome.class,
                            "Encountered a error while trying to provision PIN for MSISDN [" + msisdn + "] in PIN "
                                    + "Manager.  Subscription update will continue.", e);
                }
                String msg = "Encountered a error while trying to provision PIN in PIN Manager.  <br/>"
                        + "The PIN for this Mobile Number [" + msisdn
                        + "] could not be set automatically. Attempt setting the PIN manually.";
                ExceptionListener el = (ExceptionListener) ctx.get(ExceptionListener.class);
                if (el != null)
                {
                    el.thrown(new HomeException(msg, e));
                }
            }
        }
        else
        {
            logDebugMsg(ctx, "Since the same Msisdn [" + msisdn
                    + "] is used by some other subscription we don't have to generate the PIN again.");
        }
    }
    
    /**
     * Delete the PIN from Pin Manager for the given Subscription MSISDN, if the 
     * MSISDN is no longer associated with any other active Subscriptions.
     * @param ctx
     * @param sub
     * @throws HomeException
     */
    public void deletePin(Context ctx, Subscriber sub)
        throws HomeException
    {
        /* Get the current MSISDN Associations, and if the associations are empty then we can delete the original
         * MSISDN's PIN.*/
        Collection<Subscriber> subscriptions = SubscriberSupport.getSubscriptionsByMSISDN(ctx, sub.getMSISDN());
        boolean hasAssociations = false;
        for(Subscriber association: subscriptions)
        {
            if (!association.getId().equals(sub.getId()))
            {
                hasAssociations = true;
            }
        }
        
        if (hasAssociations)
        {
            // there are still subscriptions attached to this MSISDN we will not allow the MSISDN's PIN to be deleted
            logDebugMsg(ctx, 
                    "Currently there exists other associations to the original MSISDN, so the PIN will not be deleted for MSISDN=" + sub.getMSISDN());
        }
        else
        {
            logDebugMsg(ctx,
                    "No subscriptions are associated with the original MSISDN so the PIN will be deleted, MSISDN="
                            + sub.getMSISDN());
            try
            {
                PinManagerSupport.deletePin(ctx, sub.getMSISDN(), PinManagerSupport.ER_REFERENCE);
                PinManagerSupport.removePinProvisoningRecord(ctx, sub.getMSISDN());
            }
            catch (HomeException e)
            {
                // Log the error to the application log and to the screen.
                if (LogSupport.isDebugEnabled(ctx))
                {
                    LogSupport.debug(ctx, SubscriberPinManagerUpdateHome.class,
                            "Encountered a error while trying to delete PIN from PIN " + "Manager for MSISDN ["
                                    + sub.getMSISDN() + "].  Subscription update will continue.", e);
                }
                String msg = "Encountered a error while trying to delete PIN from PIN Manager.  <br/>"
                        + "The PIN for for MSISDN [" + sub.getMSISDN()
                        + "] could not be deleted automatically. Attempt deleting the PIN manually.";
                ExceptionListener el = (ExceptionListener) ctx.get(ExceptionListener.class);
                if (el != null)
                {
                    el.thrown(new HomeException(msg, e));
                }
            }
        }
    }
    
    /**
     * Delete the PIN from Pin Manager for the given Subscription MSISDN, if the 
     * MSISDN is no longer associated with any other active Subscriptions.
     * @param ctx
     * @param sub
     * @throws HomeException
     */
    public void changePinMsisdn(Context ctx, Subscriber oldSub, Subscriber newSub)
        throws HomeException
    {
        final String newMsisdn = newSub.getMSISDN();
        try
        {
            if (PinManagerSupport.getStateOnPinManager(ctx, newSub.getMSISDN()) != PinProvisioningStatusEnum.PROVISIONED)
            {
                if (PinManagerSupport.getStateOnPinManager(ctx, oldSub.getMSISDN()) != PinProvisioningStatusEnum.PROVISIONED)
                {
                    logDebugMsg(ctx,
                            "The MSISDN is changing for this Subscription, CRM will generate a new PIN for the new MSISDN"
                                    + newSub.getMSISDN());
                    generatePin(ctx, newSub);
                }
                else
                {
                    Account account = (Account) ctx.get(Account.class);
                    if (account==null || !account.getBAN().equals(newSub.getBAN()))
                    {
                        account = newSub.getAccount(ctx);
                    }
                    PinManagerSupport.changeMsisdn(ctx, account, oldSub.getMSISDN(), newSub.getMSISDN(),
                            PinManagerSupport.ER_REFERENCE);
                }
            }
        }
        catch (HomeException e)
        {
            String errorMString = "Encountered a Error  [" + e.getMessage()
                    + "] while trying to provision PIN for MSISDN change for Subscription [" + oldSub.getId()
                    + "] from [" + newMsisdn + "] to [ " + oldSub.getMSISDN()
                    + "].  Subscription update will continue.";
            // Log the error to the application log and to the screen.
            new DebugLogMsg(this, errorMString, e);
            ExceptionListener el = (ExceptionListener) ctx.get(ExceptionListener.class);
            if (el != null)
            {
                el.thrown(new HomeException(errorMString, e));
            }
        }
    }
    
    /**
     * Log given String as a Debug Msg in the logs. 
     * @param ctx
     * @param msg
     */
    private void logDebugMsg(Context ctx, String msg)
    {
        if (LogSupport.isDebugEnabled(ctx))
        {
            LogSupport.debug(ctx, SubscriberPinManagerUpdateHome.class, msg);
        }
    }
    
    
    private  boolean isApplicalble(Context ctx, Subscriber sub)
    {
        return !(sub.isPooledGroupLeader(ctx));
    }
    
    private boolean isVoiceMailServiceProvisioned(Context ctx, Subscriber oldSub, Subscriber newSub)
    {
    	boolean isVMServicePresentInOldSub = VoiceMailServiceProvisionSupport.isVMServiceAlreadyPresent(ctx,oldSub);
    	new DebugLogMsg(SubscriberPinManagerUpdateHome.class, "VM service present for old - "+isVMServicePresentInOldSub).log(ctx);
    	if(isVMServicePresentInOldSub)
    		return false;
    	boolean isVMServiceProvForNewSub = VoiceMailServiceProvisionSupport.isVMServiceProvisionedNow(ctx,newSub);
    	new DebugLogMsg(SubscriberPinManagerUpdateHome.class, "VM service present for new - "+isVMServiceProvForNewSub).log(ctx);
    	if(isVMServiceProvForNewSub)
    		return true;
    	else
    		return false;
    }    
    
    private boolean syncPinToVMProvider(Context ctx, int spid) throws HomeException
    {
        boolean syncPinToVMProvider = false;
        Home spHome = (Home) ctx.get(CRMSpidHome.class);
        if(spHome != null){
	        CRMSpid sp = (CRMSpid) spHome.find(ctx, Integer.valueOf(spid));
	        if(sp != null)
	        {
	        	syncPinToVMProvider = sp.isSyncPinToVMServer();
	        }
        }
        return syncPinToVMProvider;
    }

}

