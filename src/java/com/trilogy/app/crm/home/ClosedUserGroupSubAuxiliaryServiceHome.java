/*
 * This code is a protected work and subject to domestic and international
 * copyright law(s). A complete listing of authors of this work is readily
 * available. Additionally, source code is, by its very nature, confidential
 * information and inextricably contains trade secrets and other information
 * proprietary, valuable and sensitive to Redknee, no unauthorised use,
 * disclosure, manipulation or otherwise is permitted, and may only be used
 * in accordance with the terms of the licence agreement entered into with
 * Redknee Inc. and/or its subsidiaries.
 *
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.home;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AuxiliaryService;
import com.trilogy.app.crm.bean.AuxiliaryServiceTypeEnum;
import com.trilogy.app.crm.bean.ClosedUserGroup;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberAuxiliaryService;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.bean.SuspendedEntity;
import com.trilogy.app.crm.bean.SuspendedEntityHome;
import com.trilogy.app.crm.bean.SystemNoteSubTypeEnum;
import com.trilogy.app.crm.bean.SystemNoteTypeEnum;
import com.trilogy.app.crm.bean.core.Msisdn;
import com.trilogy.app.crm.extension.auxiliaryservice.core.custom.CallingGroupAuxSvcExtension;
import com.trilogy.app.crm.subscriber.charge.CrmCharger;
import com.trilogy.app.crm.support.CallingGroupSupport;
import com.trilogy.app.crm.support.ClosedUserGroupSupport;
import com.trilogy.app.crm.support.ClosedUserGroupSupport73;
import com.trilogy.app.crm.support.Lookup;
import com.trilogy.app.crm.support.NoteSupportHelper;
import com.trilogy.app.crm.support.SubscriberAuxiliaryServiceSupport;
import com.trilogy.app.crm.support.SubscriberSupport;
import com.trilogy.app.crm.support.SuspendedEntitySupport;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MinorLogMsg;


/**
 * This class provides functionality for creating/removing sub
 * auxiliary services associated with the corresponding Closed User Group.
 *
 * @author ltse
 */
public class ClosedUserGroupSubAuxiliaryServiceHome
    extends HomeProxy
{
    /**
     * Creates a new ClosedUserGroupAuxiliaryServiceCreationHome.
     *
     * @param ctx The operating context.
     * @param delegate The home to delegate to.
     */
    public ClosedUserGroupSubAuxiliaryServiceHome(final Context ctx, final Home delegate)
    {
        super(ctx, delegate);
    }


    /**
     * INHERIT
     */
    @Override
    public Object create(Context ctx, Object obj)
        throws HomeException
    {
        final ClosedUserGroup cug = (ClosedUserGroup) super.create(ctx,obj);
        
        try
        {
            final AuxiliaryService auxSvc = 
                CallingGroupSupport.getAuxiliaryServiceForCUGTemplate(
                        ctx, 
                        cug.getCugTemplateID());
            
            addAssociations(ctx, 
                    auxSvc.getIdentifier(), 
                    cug.getID(),
                    cug.getSubscribers().keySet(),
                    false, cug); 
            
			CrmCharger charger = ClosedUserGroupSupport73.getCUGCharger(ctx,
					cug, cug);
				charger.charge(ctx, null);

            return cug;
        }
        catch (final HomeException e)
        {

            CallingGroupERLogMsg.generateCUGCreationER(
                    cug, 
                    CallingGroupERLogMsg.ERROR_PROVISIONING_AUXILIARY_SERVICE, ctx);


            new MinorLogMsg(this, "Problem encounterred while trying to creat the CUG instance "
                + cug.getID(), e).log(ctx); 
            
            throw e;
        }
    }


    /**
     * INHERIT
     */
    @Override
    public void remove(Context ctx, Object obj)
        throws HomeException
    {
    	HashSet<String> toBeRemoved = new HashSet(); 
    	toBeRemoved.addAll(((ClosedUserGroup) obj).getSubscribers().keySet()); 
    	
        super.remove(ctx,obj);
        final ClosedUserGroup cug = (ClosedUserGroup) obj;
        
        try
        {
            final AuxiliaryService auxSvc = 
                CallingGroupSupport.getAuxiliaryServiceForCUGTemplate(
                        ctx, 
                        cug.getCugTemplateID());
            // Create charger before removing associations, as we will need it.
            CrmCharger charger = ClosedUserGroupSupport73.getCUGCharger(ctx,
                    cug, cug);
            
            removeAssociations(ctx, 
                    auxSvc.getIdentifier(), 
                    cug.getID(),
                    toBeRemoved,
                    false, cug); 
            
            removeSuspendedEntities(
                    ctx, 
                    auxSvc.getIdentifier(), 
                    cug,
                    toBeRemoved,
                    false);
            
            charger.refund(ctx, null);

        }
        catch (final HomeException e)
        {

            new MinorLogMsg(this, "Problem encounterred while trying to remove the CUG instance "
                + cug.getID(), e).log(ctx);
            
            CallingGroupERLogMsg.generateCUGDeletionER((ClosedUserGroup)obj,
                    CallingGroupERLogMsg.ERROR_PROVISIONING_AUXILIARY_SERVICE, ctx);
            
            // cannot rollback because we cannot create the same instance of
            // CUG
            
            throw e;
        }
            
    }
    
    
    /**
     * INHERIT
     */
    @Override
    public Object store(Context ctx, Object obj)
        throws HomeException
    {
            
        final ClosedUserGroup cug = (ClosedUserGroup) obj;
        
        
        ClosedUserGroup oldCug = (ClosedUserGroup) ctx.get(ClosedUserGroupServiceHome.OLD_CUG);
        
        obj = super.store(ctx,obj);

        try
        {
            // new aux service
            final AuxiliaryService auxSvc = cug.getAuxiliaryService(ctx);
            
            // Determine new subscribers added.
            Collection<String> newSubs = new HashSet(); 
            newSubs.addAll(cug.getSubscribers().keySet());


            // update the subscriber aux service due to the cug template ID change
            if (oldCug.getCugTemplateID() != cug.getCugTemplateID())
            {
                // get the old aux service msisdn list
                final AuxiliaryService oldAuxSvc = 
                    CallingGroupSupport.getAuxiliaryServiceForCUGTemplate(
                            ctx, 
                            oldCug.getCugTemplateID());
                
                Collection<String> oldCugMsisdnList = new HashSet(); 
                oldCugMsisdnList.addAll(oldCug.getSubscribers().keySet()); 
                
                // if the cug template is changed, remove all the sub aux service
                // to get the refund on the old cug template subscription
                removeAssociations(ctx, 
                        oldAuxSvc.getIdentifier(), 
                        cug.getID(),
                        oldCugMsisdnList,
                        false, oldCug); 
                
                updateSuspendedEntities(ctx, 
                        oldAuxSvc.getIdentifier(), 
                        auxSvc.getIdentifier(), 
                        oldCug,
                        oldCugMsisdnList,
                        false);
            } else {
            	   // Determine the removed subs 
                Collection<String> removedSubs = new HashSet();
                removedSubs.addAll(oldCug.getSubscribers().keySet());
                removedSubs.removeAll(cug.getSubscribers().keySet());
                
                removeAssociations(ctx, 
                        auxSvc.getIdentifier(), 
                        cug.getID(),
                        removedSubs,
                        false, oldCug); 
                
                updateSuspendedEntities(ctx, 
                        auxSvc.getIdentifier(), 
                        auxSvc.getIdentifier(), 
                        oldCug,
                        removedSubs,
                        false);

                newSubs.removeAll(oldCug.getSubscribers().keySet());
            }
            
            
            addAssociations(ctx, 
                    auxSvc.getIdentifier(), 
                    cug.getID(),
                    newSubs,
                    false, cug); 
            
			CrmCharger charger = ClosedUserGroupSupport73.getCUGCharger(ctx,
				cug, oldCug);
			charger.chargeAndRefund(ctx, null);

        }
        catch (final HomeException e)
        {

            new MinorLogMsg(this, "Problem encounterred while trying to store the CUG instance "
                + cug.getID(), e).log(ctx);
                        
            CallingGroupERLogMsg.generateCUGModificationER(oldCug,cug,
                    CallingGroupERLogMsg.ERROR_PROVISIONING_TO_FF_RESULT_CODE, ctx);
            
            // will not try to rollback because the database table is likely to
            // be down. It is hard to sync up the current sub aux service
            // with the FnF sub list.
            
            throw e;
        }
            
        return obj;
    }
    

    /**
     * query the sub aux service table and return the
     * msisdns that are subscribed to the CUG
     *
     * @param ctx
     * @param serviceIdentifier - the aux service id
     * @param cugId 
     * @return the set that contains the MSISDNS that are subscribed to the cug
     */
    private HashSet<String> querySubAuxServiceForMsisdnList(Context ctx, 
            final long serviceIdentifier, 
            final long cugId)throws HomeException
    {
        
        HashSet<String> subscribedSet = new HashSet<String>();
        
        Collection auxServices = SubscriberAuxiliaryServiceSupport.getSubscriberAuxiliaryServices(
                ctx, serviceIdentifier, cugId);
        
        for (Iterator ite = auxServices.iterator(); ite.hasNext(); )
        {
            SubscriberAuxiliaryService subAuxService = (SubscriberAuxiliaryService)ite.next();
            String subId = subAuxService.getSubscriberIdentifier();
            
            Subscriber sub = SubscriberSupport.lookupSubscriberForSubId(ctx, subId);

            /* TT#9031200086: Inactive subscribers should not be taken in consideration when
               returning the msisdns that have the auxiliary service. */
            if (!SubscriberStateEnum.INACTIVE.equals(sub.getState()))
            {
                subscribedSet.add(sub.getMSISDN());
            }
        }
        
        return subscribedSet;
    }
    
        

    /**
     * Adds the associations between the auxiliary service and the
     * subscribers referred to in the cug.
     *
     * @param serviceIdentifier The identifier of the AuxiliaryService.
     * @param secondaryIdentifier. The secondary id of the subscriber aux service.
     * The cug instance ID is used for the secondary identifier for subscriber
     * aux service.
     * @param msisdn collections
     * @param reportErrorImmediately
     * @param cug The cug instance.
     */
    private void addAssociations(Context ctx, 
            final long auxServiceIdentifier, 
            final long secondaryIdentifier,
            final Collection msisdns,
            boolean reportErrorImmediately, 
            final ClosedUserGroup cug) 
        
    throws HomeException
    {
        // no need to add associations if there is no new subs
        if (msisdns == null)
        {
            return;
        }
        
        Object[] msisdnArray = msisdns.toArray();
        
        for (int n = 0; n < msisdnArray.length; ++n)
        {
            final String msisdn = (String) msisdnArray[n];
			cug.getNewMsisdns().add(msisdn);

            try
            {
                final Msisdn msisdnObject = SubscriberSupport.lookupMsisdnObjectForMSISDN(ctx, msisdn);
                if (msisdnObject == null || msisdnObject.getSubscriberID(ctx) == null)
                {
                    new DebugLogMsg(this, "Fail to create the subscirber aux service for MSISDN "
                            + msisdn + 
                            " , CUG ID (" + secondaryIdentifier + ") because the msisdn does not exist", null).log(ctx);
                    continue;
                }
                
                Subscriber sub = SubscriberSupport.lookupSubscriberForSubId(ctx, msisdnObject.getSubscriberID(ctx));
                
                /* TT#9031200086: The association should only be added to non-inactive subscribers and subscribers in the same spid as the cug. */
                if (sub != null && !SubscriberStateEnum.INACTIVE.equals(sub.getState()))
                {
                    SubscriberAuxiliaryService auxService;
                    if (sub.getSpid() == cug.getSpid())
                    {
                        auxService = SubscriberAuxiliaryServiceSupport.createAssociationForSubscriber(
                                ctx, 
                                sub, 
                                auxServiceIdentifier,
                                secondaryIdentifier, 
                                AuxiliaryServiceTypeEnum.CallingGroup);

                     // removing this method as subscriber aux service association has already been added in above method call.
                        // On any update done to subscriber after this method call will make subscriber pipeline to add this association once again.
                        addAuxServiceToGUISubProfile(ctx,
                                sub.getId(),
                                auxService);

                        try
                        {
                            NoteSupportHelper.get(ctx).addSubscriberNote(
                                    ctx,
                                    sub.getId(),
                                    "Subscriber updating succeeded\nSubscriber Auxiliary Service "
                                            + auxService.getAuxiliaryServiceIdentifier() + " (CugID = " + secondaryIdentifier + ") provisioned.",
                                    SystemNoteTypeEnum.EVENTS, SystemNoteSubTypeEnum.SUBUPDATE);
                        }
                        catch (HomeException e)
                        {
                            LogSupport.minor(ctx,  this, "Unable to log note: " + e.getMessage(), e);
                        }

                    }
                    else
                    {
                        // Only populate the Subscriber Auxiliary Service but do not save it.
                        auxService = SubscriberAuxiliaryServiceSupport.populateAssociationForSubscriber(
                                ctx, 
                                sub, 
                                auxServiceIdentifier,
                                secondaryIdentifier, 
                                AuxiliaryServiceTypeEnum.CallingGroup);
                    }
                    
					cug.getSubAuxServices().put(msisdn, auxService);
                }
                else
                {
                    if (sub == null)
                    {
                        new MinorLogMsg(this, "Fail to create the subscriber aux service for MSISDN "
                                + msisdn + 
                                " , CUG ID (" + secondaryIdentifier + ") because the sub is null!", null).log(ctx);
                    }
                    else
                    {
                        new MinorLogMsg(this, "Fail to create the subscriber aux service for MSISDN "
                                + msisdn + 
                                " , CUG ID (" + secondaryIdentifier + ") because the msisdn is inactive (SubscriberId='" + sub.getId() + "')", null).log(ctx);
                    }
                    continue;
                }
            }
            catch (final HomeException exception)
            {
                new MinorLogMsg(this, "Problem encounterred while trying to create an association between MSISDN "
                    + msisdn + 
                    " ,AuxiliaryService (" + auxServiceIdentifier + ")" +
                    " , CUG ID (" + secondaryIdentifier + ")", exception).log(ctx);
                if (reportErrorImmediately)
                {
                    throw exception;
                }
                
            }
        }
    }

    /**
     * Removes the associations between the auxiliary service and the
     * subscribers referred to in the cug.
     *
     * @param serviceIdentifier The identifier of the AuxiliaryService.
     * @param secondaryIdentifier. The secondary id of the subscriber aux service.
     * The cug instance ID is used for the secondary identifier for subscriber
     * aux service.
     * @param msisdn collections
     * @param reportErrorImmediately

     */
    private void removeAssociations(Context ctx, 
            final long auxServiceIdentifier, 
            final long secondaryIdentifier,
            final Collection msisdns,
            boolean reportErrorImmediately, 
            final ClosedUserGroup cug) throws HomeException
    {
        final Context context = ctx.createSubContext();
        context.put(CallingGroupAuxSvcExtension.ENABLED, false);

        Object[] msisdnArray = msisdns.toArray();

        for (int n = 0; n < msisdnArray.length; ++n)
        {
            final String msisdn = (String)msisdnArray[n];
			cug.getRemoveddMsisdns().add(msisdn);


            try
            {
                final Msisdn msisdnObject = SubscriberSupport.lookupMsisdnObjectForMSISDN(ctx, msisdn);
                if (msisdnObject == null || msisdnObject.getSubscriberID(ctx) == null)
                {
                    new MinorLogMsg(this, "Fail to remove the subscirber aux service for MSISDN "
                            + msisdn + 
                            " , CUG ID (" + secondaryIdentifier + ") because the msisdn does not exist", null).log(ctx);
                    continue;
                }
                
                
                Subscriber sub = SubscriberSupport.lookupSubscriberForSubId(ctx, 
                        msisdnObject.getSubscriberID(ctx));
                
                SubscriberAuxiliaryService auxService = null;
                
                if (sub!=null && sub.getSpid() == cug.getSpid())
                {
                    auxService = SubscriberAuxiliaryServiceSupport.removeAssociationForSubscriber(context, 
                            sub, 
                            auxServiceIdentifier,
                            secondaryIdentifier);

                 // removing this method as subscriber aux service association has already been removed in above method call.
                    // On any update done to subscriber after this method call will make subscriber pipeline to try to remove this association once again.
                    removeAuxServiceFromGUISubProfile(ctx, sub.getId(), 
                            auxServiceIdentifier, secondaryIdentifier);
                    
                    try
                    {
                        NoteSupportHelper.get(ctx).addSubscriberNote(
                                ctx,
                                sub.getId(),
                                "Subscriber updating succeeded\nSubscriber Auxiliary Service "
                                        + auxServiceIdentifier + " (CugID = " + secondaryIdentifier + ") unprovisioned.",
                                SystemNoteTypeEnum.EVENTS, SystemNoteSubTypeEnum.SUBUPDATE);
                    }
                    catch (HomeException e)
                    {
                        LogSupport.minor(ctx,  this, "Unable to log note: " + e.getMessage(), e);
                    }
                    
                }
                else if (sub!=null)
                {
                    auxService =  SubscriberAuxiliaryServiceSupport.populateAssociationForSubscriber(
                            ctx, 
                            sub, 
                            auxServiceIdentifier,
                            secondaryIdentifier, 
                            AuxiliaryServiceTypeEnum.CallingGroup);
                }
                
                if (auxService != null)
                {	
                	cug.getSubAuxServices().put(msisdn, auxService);
                }
                
            }
            catch (final HomeException exception)
            {
                new MinorLogMsg(this, "Problem encounterred while trying to remove an association between MSISDN "
                        + msisdn + 
                        " ,AuxiliaryService (" + auxServiceIdentifier + ")" +
                        " , CUG ID (" + secondaryIdentifier + ")", exception).log(ctx);
                if (reportErrorImmediately)
                {
                    throw exception;
                }
                    
            }
        }
    }
    
    /**
     * This method is needed for Family Plan feature.
     * When updating/creating the individual account, the FnF extension logic will
     * update/create/remove the subscriber auxiliary service. Those changes need 
     * to be applied to aux service list of the GUI sub profile because the subscriber will be stored
     * after the account is modified.
     * @param ctx
     * @param subId
     * @param auxId
     * @param secondaryId
     */
    private void removeAuxServiceFromGUISubProfile(Context ctx,
            String subId,
            long auxId,
            long secondaryId)
    {
        try
        {
            Account account = (Account) ctx.get(Account.class);
            
            if (account == null)
            {
                return;
            }
            
            Subscriber sub = account.getIndividualSubscriber(ctx);
            
            if (sub == null)
            {
                return;
            }
            if (!sub.getId().equals(subId))
            {
                return;    
            }
            if (sub.getAuxiliaryServices() == null)
            {
                return;
            }
            
            for (Iterator ite = sub.getAuxiliaryServices().iterator(); ite.hasNext();)
            {
                SubscriberAuxiliaryService auxService = (SubscriberAuxiliaryService) ite.next();
                if (auxService.getAuxiliaryServiceIdentifier() == auxId &&
                    auxService.getSecondaryIdentifier() == secondaryId)
                {
                    ite.remove();
                    if (LogSupport.isDebugEnabled(ctx))
                    {
                        new DebugLogMsg(this, "Remove subAuxService from GUI sub profile"
                                + subId + 
                                " ,AuxiliaryService (" + auxId + ")" +
                                " , CUG ID (" + secondaryId + ")", null).log(ctx);
                    }

                }
            }
        }
        catch (Exception e)
        {
            new MinorLogMsg(this, "Problem encountered while trying to remove auxService from GUI sub profile "
                    + subId + 
                    " ,AuxiliaryService (" + auxId + ")" +
                    " , CUG ID (" + secondaryId + ")", e).log(ctx);
        }
    }

    /**
     * This method is needed for Family Plan feature.
     * When updating/creating the individual account, the FnF extension logic will
     * update/create/remove the subscriber auxiliary service. Those changes need 
     * to be applied to aux service list of the GUI sub profile because the subscriber will be stored
     * after the account is modified.
     * @param ctx
     * @param subId
     * @param subAuxService
     */

    private void addAuxServiceToGUISubProfile(Context ctx,
            String subId,
            SubscriberAuxiliaryService subAuxService)
    {
        try
        {
            Account account = (Account) ctx.get(Account.class);
            Subscriber sub = (Subscriber) ctx.get(Subscriber.class);            
            if (sub!=null && sub.getAuxiliaryServices()!=null && sub.getId().equals(subId))
            {
                sub.getAuxiliaryServices().add(subAuxService);                
                if (LogSupport.isDebugEnabled(ctx))
                {
                    new DebugLogMsg(this, "Add subAuxService to context sub profile"
                            + subId + 
                            " ,AuxiliaryService (" + subAuxService.getAuxiliaryServiceIdentifier() + ")" +
                            " , CUG ID (" + subAuxService.getSecondaryIdentifier() + ")", null).log(ctx);
                }
            }
            
            // Adding auxiliary service to old subscriber, to avoid duplicate update on
            // cug service association when cug provisioning in invoked by
            // callinggroupprovision service
            Subscriber oldSub = (Subscriber) ctx.get(Lookup.OLDSUBSCRIBER);
            boolean addToOld = ctx.has(ClosedUserGroupValidator.SKIP_CUG_ACCOUNT_VALIDATION) && oldSub != null;
            if (addToOld && oldSub.getAuxiliaryServices() != null && oldSub.getId().equals(subId))
            {
                oldSub.getAuxiliaryServices().add(subAuxService);
            }
            
            if (account == null)
            {
                return;
            }
            
            Subscriber individualAccountSub = account.getIndividualSubscriber(ctx);

            if (individualAccountSub == null)
            {
                return;
            }
            if (!individualAccountSub.getId().equals(subId))
            {
                return;    
            }

            if (individualAccountSub.getAuxiliaryServices() == null)
            {
                return;
            }
            
            individualAccountSub.getAuxiliaryServices().add(subAuxService);
            if (LogSupport.isDebugEnabled(ctx))
            {
                new DebugLogMsg(this, "Add subAuxService to GUI sub profile"
                        + subId + 
                        " ,AuxiliaryService (" + subAuxService.getAuxiliaryServiceIdentifier() + ")" +
                        " , CUG ID (" + subAuxService.getSecondaryIdentifier() + ")", null).log(ctx);
            }

            
        }
        catch (Exception e)
        {
            new MinorLogMsg(this, "Problem encountered while trying add auxService to GUI sub profile "
                    + subId + 
                    " ,AuxiliaryService (" + subAuxService.getAuxiliaryServiceIdentifier() + ")" +
                    " , CUG ID (" + subAuxService.getSecondaryIdentifier() + ")", e).log(ctx);
        }
    }

    
    
    /**
     * Update the suspended entities 
     *
     * @param ctx
     * @param oldAuxServiceIdentifier The identifier of the AuxiliaryService.
     * @param newAuxServiceIdentifier. The identifier of the new aux service (corresponding
     * to the new cug template).
     * @param cug
     * @param msisdn collections
     * @param reportErrorImmediately

     */
    private void updateSuspendedEntities(Context ctx, 
            final long oldAuxServiceIdentifier, 
            final long newAuxServiceIdentifier, 
            final ClosedUserGroup cug,
            final Collection msisdns,
            boolean reportErrorImmediately) throws HomeException
    {
        Collection col = SuspendedEntitySupport.findSuspendedEntity(ctx, 
                oldAuxServiceIdentifier,
                cug.getID(), 
                AuxiliaryService.class);
        
        if (col == null)
        {
            return;
        }

        for (Iterator ite = col.iterator(); ite.hasNext();)
        {
            SuspendedEntity entity = (SuspendedEntity) ite.next();

            try
            {
                if (entity != null)
                {
                    final Home suspEntityHome = (Home) ctx.get(SuspendedEntityHome.class);

                    entity.setIdentifier(newAuxServiceIdentifier);
                    suspEntityHome.store(entity);
                }
                
            }
            catch (final HomeException exception)
            {
                new MinorLogMsg(this, "Problem encounterred while trying to update the supended entities " +
                        " , oldAuxServiceId (" + oldAuxServiceIdentifier + ")" +
                        " , newAuxServiceId (" + newAuxServiceIdentifier + ")" +
                        " , CUG ID (" + cug.getID() + ")", exception).log(ctx);
                if (reportErrorImmediately)
                {
                    throw exception;
                }
                    
            }
        }
    }
    
    /**
     * Remove the suspended entities 
     *
     * @param ctx
     * @param oldAuxServiceIdentifier The identifier of the AuxiliaryService.
     * @param newAuxServiceIdentifier. The identifier of the new aux service (corresponding
     * to the new cug template).
     * @param cug
     * @param msisdn collections
     * @param reportErrorImmediately

     */
    private void removeSuspendedEntities(Context ctx, 
            final long auxServiceIdentifier, 
            final ClosedUserGroup cug,
            final Collection msisdns,
            boolean reportErrorImmediately) throws HomeException
    {
        Object[] msisdnArray = msisdns.toArray();

        for (int n = 0; n < msisdnArray.length; ++n)
        {
            final String msisdn = (String)msisdnArray[n];

            try
            {
                final Msisdn msisdnObject = SubscriberSupport.lookupMsisdnObjectForMSISDN(ctx, msisdn);
                if (msisdnObject == null || msisdnObject.getSubscriberID(ctx) == null)
                {
                    new MinorLogMsg(this, "Fail to remove supend entities for MSISDN "
                            + msisdn + 
                            " , auxServiceId (" + auxServiceIdentifier + ")" +
                            " , CUG ID (" + cug.getID() + ") because the msisdn does not exist", null).log(ctx);
                    continue;
                }

                Subscriber sub = SubscriberSupport.lookupSubscriberForSubId(ctx, 
                        msisdnObject.getSubscriberID(ctx));
                
                if (sub != null)
                {
                    SuspendedEntitySupport.removeSuspendedEntity(
                            ctx, 
                            sub.getId(), 
                            auxServiceIdentifier, 
                            cug.getID(), 
                            AuxiliaryService.class);
                }
            }
            catch (final HomeException exception)
            {
                new MinorLogMsg(this, "Problem encounterred while trying to remove the supended entities "
                        + msisdn + 
                        " , auxServiceId (" + auxServiceIdentifier + ")" +
                        " , CUG ID (" + cug.getID() + ")", exception).log(ctx);
                if (reportErrorImmediately)
                {
                    throw exception;
                }
                    
            }
        }
    }


}
