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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.trilogy.app.crm.Common;
import com.trilogy.app.crm.amsisdn.AdditionalMsisdnAuxiliaryServiceSupport;
import com.trilogy.app.crm.api.rmi.support.ApiOptionsUpdateResultSupport;
import com.trilogy.app.crm.bean.AuxiliaryService;
import com.trilogy.app.crm.bean.AuxiliaryServiceTypeEnum;
import com.trilogy.app.crm.bean.Msisdn;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberAuxiliaryService;
import com.trilogy.app.crm.bean.SubscriberAuxiliaryServiceHome;
import com.trilogy.app.crm.bean.SubscriberAuxiliaryServiceXInfo;
import com.trilogy.app.crm.bean.SubscriberHomezone;
import com.trilogy.app.crm.bean.SubscriberHomezoneHome;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.bean.service.ServiceStateEnum;
import com.trilogy.app.crm.clean.CronConstants;
import com.trilogy.app.crm.client.ProvisioningHomeException;
import com.trilogy.app.crm.extension.auxiliaryservice.core.TFAAuxSvcExtension;
import com.trilogy.app.crm.notification.NotificationTypeEnum;
import com.trilogy.app.crm.subscriber.provision.TFAAuxServiceSupport;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.app.crm.support.EnumStateSupportHelper;
import com.trilogy.app.crm.support.Lookup;
import com.trilogy.app.crm.support.SubscriberAuxiliaryServiceSupport;
import com.trilogy.app.crm.support.SubscriptionNotificationSupport;
import com.trilogy.framework.xhome.beans.ExceptionListener;
import com.trilogy.framework.xhome.beans.SafetyUtil;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xhome.language.MessageMgr;
import com.trilogy.framework.xhome.webcontrol.HTMLExceptionListener;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MajorLogMsg;
import com.trilogy.framework.xlog.log.MinorLogMsg;
import com.trilogy.util.crmapi.wsdl.v2_1.types.serviceandbundle.PricePlanOptionTypeEnum;



/**
 * Provides a mechanism to react to changes in the Subscriber's selected
 * AuxiliaryServices.
 *
 * @author gary.anderson@redknee.com
 */
public class SubscriberAuxiliaryServiceCreationHome extends HomeProxy
{

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 1L;

    private static final Collection<SubscriberStateEnum> UNPROVISION_STATES =
        new HashSet<SubscriberStateEnum>(Arrays.asList(SubscriberStateEnum.PENDING, SubscriberStateEnum.INACTIVE));

    /**
     * Creates a new SubscriberAuxiliaryServiceCreationHome.
     *
     * @param delegate
     *            The Home to which we delegate.
     */
    public SubscriberAuxiliaryServiceCreationHome(final Home delegate)
    {
        super(delegate);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Object create(final Context ctx, final Object obj) throws HomeException
    {
        final Object createdObject = super.create(ctx, obj);
        // add homezone count = 0 here
        updateAssociations(ctx, createdObject);

        return createdObject;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Object store(final Context ctx, final Object obj) throws HomeException
    {
        final Object ret = super.store(ctx, obj);
        updateAssociations(ctx, ret);
        return ret;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void remove(final Context ctx, final Object obj) throws HomeException
    {
        final Subscriber subscriber = (Subscriber) obj;

        final Context subCtx = ctx.createSubContext();

        // when removing a subscriber we should not charge/refund anything
        // subscriber should be disabled, at that time the charging/refund was taken care of
        //subCtx.put(SubscriberAuxiliaryServiceChargingHome.BYPASS_CHARGING, true);

        // if we are removing a subscriber, then we should remove all associations
        removeAllAssociations(subCtx, subscriber);

        super.remove(ctx, subscriber);
    }


    /**
     * Removes all associations for the subscriber.
     *
     * @param ctx
     *            The operating context.
     * @param subscriber
     *            The subscriber to be removed.
     * @throws HomeException
     *             Thrown if there are problems removing all the associations related to
     *             the subscriber.
     */
    private static void removeAllAssociations(final Context ctx, final Subscriber subscriber) throws HomeException
    {
        final Home home = (Home) ctx.get(SubscriberAuxiliaryServiceHome.class);
        final EQ condition = new EQ(SubscriberAuxiliaryServiceXInfo.SUBSCRIBER_IDENTIFIER, subscriber.getId());
        home.removeAll(ctx, condition);
    }


    /**
     * Adds a set of wanted SubscriberAuxiliaryServices.
     *
     * @param ctx
     *            The operating context.
     * @param subscriber
     *            The owner of the new association
     * @param associations
     *            The associations to add.
     * @throws HomeException
     *             Thrown by home.
     */
    @SuppressWarnings("unchecked")
    public void addWantedAssociations(final Context ctx, final Subscriber subscriber, final Collection associations)
        throws HomeException
    {
        final Home home = (Home) ctx.get(SubscriberAuxiliaryServiceHome.class);

        final Iterator associationIterator = associations.iterator();
   
        Map<Long, Short> subAuxTFAIndexMap = new HashMap<Long, Short>();
        Map<Long, Short> subCurrTFAIndexMap = new HashMap<Long,Short>();
        List<com.redknee.app.crm.bean.core.custom.AuxiliaryService> subTFAAuxSvcList = null;
        boolean isProvisionedTFAType = false;
        ExceptionListener el= (ExceptionListener)ctx.get(ExceptionListener.class); 
        TFAAuxSvcExtension tfaExtension = null;
        while (associationIterator.hasNext())
        {
            final SubscriberAuxiliaryService association = (SubscriberAuxiliaryService) associationIterator.next();
            association.setContext(ctx);
            
            
            // Manda - Added code from lines
            Collection coll = null;
            try
            {
                coll = getExistingAssociations(ctx, association, home);
            }
            catch (final HomeException e1)
            {
                final String message = "Exception thrown while checking for an Existing Subscriber Auxiliary Service Association";
                new MajorLogMsg(this, message, e1).log(ctx);
                throw e1;
            }

            try
            {
                if (coll != null && coll.size() > 0)
                {
                    updateAssociation(ctx, coll, association, home);
                }
            }
            catch (final HomeException e)
            {
                final String message = "Exception thrown while updating an existing Subscriber Auxiliary Service Association with "
                    + " Identifier = "
                    + association.getIdentifier()
                    + "for Subscriber = "
                    + association.getSubscriberIdentifier();
                new MajorLogMsg(this, message, e).log(ctx);

                throw e;
            }

            try
            {
            	AuxiliaryService auxService = association.getAuxiliaryService(ctx);
            	
            	short tfaExtIndex = -1;
            	/*Validation for TFA Auxiliary Service Type. Two use cases are handled below:
            	*1. Subscriber adding two TFA auxiliary services of similar type at the same time. Only first one is provisioned and for second service appropriate message is displayed.
            	*2. Subscriber is adding TFA auxiliary service which is already provisioned for subscriber.
            	*/
            	if(auxService.getType().equals(AuxiliaryServiceTypeEnum.TFA)){
            		isProvisionedTFAType = false;
            		Subscriber oldSub = (Subscriber) ctx.get(Lookup.OLDSUBSCRIBER);
            		if(oldSub == null)
            			oldSub = subscriber;
            		
            		tfaExtension = TFAAuxSvcExtension.getTfaServiceExtention(ctx, auxService.getIdentifier());
            	
            		tfaExtIndex = tfaExtension.getTfaServiceExtention().getIndex() ;//Get TFA service type index.
            	
            		if(subCurrTFAIndexMap.containsValue(tfaExtIndex)){ // If service type is already added for subscriber.
            			isProvisionedTFAType = true;
            		}else{
            			
            			if(subTFAAuxSvcList == null) {//Fetch all TFA Auxiliary Services for Subscriber once.
            				
            				subTFAAuxSvcList = TFAAuxServiceSupport.getTFAAuxiliaryServices(ctx,oldSub);
            				Iterator<com.redknee.app.crm.bean.core.custom.AuxiliaryService> it = subTFAAuxSvcList.iterator();
            				while (it.hasNext()) {

            					AuxiliaryService service =  it.next();

            					tfaExtension= TFAAuxSvcExtension.getTfaServiceExtention(ctx, service.getIdentifier());

            					short tfaIndex1 = tfaExtension.getTfaServiceExtention().getIndex() ;

            					subAuxTFAIndexMap.put(service.getID(), tfaIndex1);

            				}
            			}
            			
            			//If the services are already provisioned for subscriber,do not create again.
            			if(!subAuxTFAIndexMap.containsKey(auxService.getID()) && subAuxTFAIndexMap.containsValue(tfaExtIndex)){
            				
            				isProvisionedTFAType = true;
            			
            			}else if(subAuxTFAIndexMap.containsKey(auxService.getID()) && subCurrTFAIndexMap.containsValue(tfaExtIndex)){
            				//If subscriber selects same type of 2 services to be provisioned, create the first one and reject the second one
            				isProvisionedTFAType = true;     				
            			}
            			 
            		}
            	}
            	if(isProvisionedTFAType){

            		new MinorLogMsg(this,"Failed to provision Auxiliary Service :"+auxService.getName()+". Subscriber is already provisioned for TFA Auxiliary Service of type: "+tfaExtension.getTfaServiceExtention(),null).log(ctx);


            		if (el == null)
            		{
            			MessageMgr manager = new MessageMgr(ctx, this); 
            			el = new HTMLExceptionListener(manager);
            			ctx.put(HTMLExceptionListener.class, el);
            			ctx.put(ExceptionListener.class, el);

            		}

            		String msg = "Failed to provision Auxiliary Service :"+auxService.getName()+". Subscriber is already provisioned for TFA Auxiliary Service of type: "+tfaExtension.getTfaServiceExtention();

            		//If Request comes from API the condition will be true;
            		if(ApiOptionsUpdateResultSupport.isInstalledApiResultSetInContext(ctx))
            		{
            			//Set Appropriate ErrorCode/Error Message for TFA similar type service.
            			long auxSvcId = auxService.getID();
            			int auxSvcType = ApiOptionsUpdateResultSupport.toChargableItemType(PricePlanOptionTypeEnum.AUXILIARY_SERVICE).intValue();

            			ApiOptionsUpdateResultSupport.setApiErrorMessage(ctx, auxSvcId,
            					auxSvcType, 
            					msg);
            			ApiOptionsUpdateResultSupport.setApiErrorCode(ctx, auxSvcId,
            					auxSvcType, TFAAuxServiceSupport.TFA_SAME_TYPE_AUX_SERVICE_EXCEPTION);

            			
            		}
            		el.thrown(new Exception(msg,new Exception()));					

            		continue;
            	}
                association.setSubscriber(subscriber);
                association.setProvisionAction(com.redknee.app.crm.bean.service.ServiceProvisionActionEnum.PROVISION);
                SubscriberAuxiliaryServiceSupport.createSubscriberAuxiliaryService(ctx, association);
                Subscriber oldSub = (Subscriber) ctx.get(Lookup.OLDSUBSCRIBER);
                if(oldSub != null){
                	 SubscriptionNotificationSupport.sendServiceStateChangeNotification(ctx, oldSub, subscriber, null, association.getAuxiliaryService(ctx), NotificationTypeEnum.SERVICE_STATE_CHANGE,ServiceStateEnum.PROVISIONED);
                }
                subCurrTFAIndexMap.put(auxService.getID(), tfaExtIndex);
            }
            catch (final Throwable t)
            {
                /*
                 * TT#9070600028: Refreshing the auxiliary services of the subscribers in the context.
                 */
                subscriber.setAuxiliaryServices(null);

                final String message = " Encounter problem when adding wanted auxiliary service "
                    + association.getAuxiliaryServiceIdentifier() + " to subscriber "
                    + association.getSubscriberIdentifier();

                new MajorLogMsg(this, message, t).log(ctx);

                if (t instanceof ProvisioningHomeException)
                {
                    throw (ProvisioningHomeException) t;
                }
                else
                {
                    throw new HomeException(t);
                }
            }
        }
        
    }


    /**
     * Returns a collection of subscriber-auxiliary service associations associated with
     * the same auxiliary service and subscriber referred to by the provided association.
     * exists.
     * @param ctx
     *            The operating context.
     * @param subAuxService
     *            Subscriber-auxiliary service association to check for.
     * @param home
     *            SubscriberAuxiliaryServiceHome.
     *
     * @return Collection of all existing associations with the same subscriber and
     *         auxiliary service as the provided association.
     * @throws HomeException
     *             Thrown if there are problems looking up the associations.
     */
    @SuppressWarnings("unchecked")
    private Collection getExistingAssociations(final Context ctx, final SubscriberAuxiliaryService subAuxService,
            final Home home) throws HomeException
    {
        final And condition = new And();

        condition.add(new EQ(SubscriberAuxiliaryServiceXInfo.SUBSCRIBER_IDENTIFIER,
                subAuxService.getSubscriberIdentifier()));

        final Long auxSrvID = Long.valueOf(subAuxService.getAuxiliaryServiceIdentifier());
        condition.add(new EQ(SubscriberAuxiliaryServiceXInfo.AUXILIARY_SERVICE_IDENTIFIER, auxSrvID));

        final Collection coll = home.select(ctx, condition);

        return coll;
    }


    /**
     * This method updates/stores the existing Subscriber Auxiliary Service association
     * with the values coming from GUI. This is done for the Buzzard Business Promo RFF.
     * @param ctx
     *            The operating context.
     * @param coll
     *            Collection of SubscriberAuxiliaryService to update.
     * @param association
     *            SubscriberAuxiliaryService object from the UI.
     * @param home
     *            SubscriberAuxiliaryServiceHome object
     *
     * @throws HomeException
     *             Thrown if there are problems updating the associations.
     */
    @SuppressWarnings("unchecked")
    private static void updateAssociation(final Context ctx, final Collection coll,
        final SubscriberAuxiliaryService association, final Home home) throws HomeException
    {
        if (coll == null || coll.size() <= 0)
        {
            return;
        }

        for (final Iterator servIter = coll.iterator(); servIter.hasNext();)
        {
            final SubscriberAuxiliaryService subAux = (SubscriberAuxiliaryService) servIter.next();

            subAux.setPaymentNum(association.getPaymentNum());
            subAux.setStartDate(association.getStartDate());
            subAux.setEndDate(association.getEndDate());
            association.setProvisionAction(com.redknee.app.crm.bean.service.ServiceProvisionActionEnum.UPDATE_ATTRIBUTES);
            home.store(ctx, subAux);
        }
    }


    /**
     * Gets collections of wanted and unwanted SubscriberAuxiliaryService associations
     * based on newly chosen associations and existing associations. The wanted
     * associations returned will be those in the chosen collection, but not in the
     * existing collection. The unwanted associations returned will be those in the
     * existing collection but not in the chosen collection. The contained type of all
     * collections is SubscriberAuxiliaryService.
     *
     * @param ctx
     *            The operating context.
     * @param chosenAssociations
     *            The chosen associations for the subscriber.
     * @param existingAssociations
     *            The existing associations for the subscriber.
     * @param wantedAssociations
     *            [OUT] The wanted associations the subscriber does not yet have.
     * @param unwantedAssociations
     *            [OUT] The existing associations the subscriber no longer wants.
     * @param refreshedAssociations
     *            [OUT] The existing associations to be refreshed.
     * @param subscriber
     *            The subscriber related to these associations.
     */
    @SuppressWarnings("unchecked")
    private void getAddAndRemoveSets(final Context ctx, final Collection chosenAssociations,
        final Collection existingAssociations, final Collection wantedAssociations,
        final Collection unwantedAssociations, final Collection refreshedAssociations, final Subscriber subscriber)
    {
        determineExistingAssociations(ctx, chosenAssociations, existingAssociations, unwantedAssociations,
            refreshedAssociations);

        determineChosenAssociations(ctx, chosenAssociations, existingAssociations, wantedAssociations, subscriber);
    }


    /**
     * Determines the status of the existing associations.
     *
     * @param ctx
     *            The operating context.
     * @param chosenAssociations
     *            The chosen associations for the subscriber.
     * @param existingAssociations
     *            The existing associations for the subscriber.
     * @param unwantedAssociations
     *            [OUT] The existing associations the subscriber no longer wants.
     * @param refreshedAssociations
     *            [OUT] The existing associations to be refreshed.
     */
    @SuppressWarnings("unchecked")
    private void determineExistingAssociations(final Context ctx, final Collection chosenAssociations,
        final Collection existingAssociations, final Collection unwantedAssociations,
        final Collection refreshedAssociations)
    {
        // Determine the unwanted associations.
        final Map<Long,Map<Long, SubscriberAuxiliaryService>> chosenIdentifiers = SubscriberAuxiliaryServiceSupport.getSelectionMap(ctx, chosenAssociations);

        final Iterator existingAssociationIterator = existingAssociations.iterator();
        while (existingAssociationIterator.hasNext())
        {
            final SubscriberAuxiliaryService existingService = (SubscriberAuxiliaryService) existingAssociationIterator
                .next();

            Map<Long, SubscriberAuxiliaryService> auxServiceMap = chosenIdentifiers.get(Long.valueOf(existingService.getAuxiliaryServiceIdentifier()));
            // entry exists now but not selected any more
            if (auxServiceMap==null || auxServiceMap.get(Long.valueOf(existingService.getSecondaryIdentifier())) == null)
            {
                unwantedAssociations.add(existingService);
            }
            else
            {
                // ----- entry exists and selected, check if it has been modified -----
                /*
                 * check if endDate has been modified, checking of startDate has been done
                 * in Validator, e.g. if it is existing now then startDate can not be
                 * modified
                 */

                final SubscriberAuxiliaryService chosenService = SubscriberAuxiliaryServiceSupport
                    .getSelectedSubscriberAuxiliaryService(chosenAssociations, existingService
                        .getAuxiliaryServiceIdentifier(), existingService.getSecondaryIdentifier());

                final Date existingEndDateDay = CalendarSupportHelper.get(ctx).getDateWithNoTimeOfDay(existingService.getEndDate());
                final Date chosenEndDateDay = CalendarSupportHelper.get(ctx).getDateWithNoTimeOfDay(chosenService.getEndDate());
                final boolean dateChanged = !existingEndDateDay.equals(chosenEndDateDay);

                final boolean fieldChanged = chosenService.getPaymentNum() != existingService.getPaymentNum() || chosenService.getPersonalizedFee() != existingService.getPersonalizedFee()
                        ||chosenService.getIsfeePersonalizationApplied()!=existingService.isIsfeePersonalizationApplied()
                    || isExtraFieldsChanged(ctx, chosenService, existingService.getIdentifier()) ;
                final boolean update = dateChanged || fieldChanged;

                if (update)
                {
                    // provisioned parameter isn't displayed on GUI on always
                    // return false from interface, need to reset with real value.
                    chosenService.setProvisioned(existingService.getProvisioned());

                    // all the non-valid endDate checking has been done in validator
                    // if it gets here, the endDate should be later than/equal to today
                    // which are all valid
                    refreshedAssociations.add(chosenService);
                }
            }
        }
    }


    /**
     * Determines the status of the chosen associations.
     *
     * @param ctx
     *            The operating context.
     * @param chosenAssociations
     *            The chosen associations for the subscriber.
     * @param existingAssociations
     *            The existing associations for the subscriber.
     * @param wantedAssociations
     *            [OUT] The wanted associations the subscriber does not yet have.
     * @param subscriber
     *            The subscriber related to these associations.
     */
    @SuppressWarnings("unchecked")
    private void determineChosenAssociations(final Context ctx, final Collection chosenAssociations,
        final Collection existingAssociations, final Collection wantedAssociations, final Subscriber subscriber)
    {
        // Determine the wanted associations.
        final Map<Long,Map<Long, SubscriberAuxiliaryService>> existingIdentifiers = SubscriberAuxiliaryServiceSupport.getSelectionMap(ctx, existingAssociations);
//        final Set<Long> existingIdentifiers = SubscriberAuxiliaryServiceSupport.getSelectionSet(ctx,
            //existingAssociations);

        if (chosenAssociations == null || chosenAssociations.size() <= 0)
        {
            return;
        }
        final Iterator chosenAssociationIterator = chosenAssociations.iterator();

        while (chosenAssociationIterator.hasNext())
        {
            final Object obj = chosenAssociationIterator.next();
            if (obj instanceof SubscriberAuxiliaryService)
            {
                final SubscriberAuxiliaryService chosenService = (SubscriberAuxiliaryService) obj;

                AuxiliaryService service = null;
                try
                {
                    service = chosenService.getAuxiliaryService(ctx);
                }
                catch (final HomeException he)
                {
                    new MinorLogMsg(this,
                        " Failed to get the AuxiliaryService for the Subscriber AuxiliaryService with "
                            + "Aux Service Id = " + chosenService.getAuxiliaryServiceIdentifier(), he)
                        .log(ctx);
                    continue;
                }
                determineChosenAssociation(wantedAssociations, subscriber, existingIdentifiers, chosenService, service);
            }
        }
    }


    /**
     * Determines the status of the chosen association.
     *
     * @param wantedAssociations
     *            [OUT] The wanted associations the subscriber does not yet have.
     * @param subscriber
     *            The subscriber related to these associations.
     * @param existingIdentifiers
     *            The set of identifiers of existing auxiliary services.
     * @param association
     *            The chosen association whose status is being determined.
     * @param service
     *            The auxiliary service related to the chosen association.
     */
    @SuppressWarnings("unchecked")
    private void determineChosenAssociation(final Collection wantedAssociations, final Subscriber subscriber,
        final Map<Long, Map<Long, SubscriberAuxiliaryService>> existingIdentifiers, final SubscriberAuxiliaryService association,
        final AuxiliaryService service)
    {

        Map<Long, SubscriberAuxiliaryService> auxServiceMap = existingIdentifiers.get(Long.valueOf(association.getAuxiliaryServiceIdentifier()));
        
        if (auxServiceMap==null || auxServiceMap.get(Long.valueOf(association.getSecondaryIdentifier())) == null)
        {
            final Date chosenStartDateDay = CalendarSupportHelper.get().getDateWithNoTimeOfDay(association.getStartDate());
            final Date chosenEndDateDay = CalendarSupportHelper.get().getDateWithNoTimeOfDay(association.getEndDate());

            association.setProvisioned(true);


            // manda - added this new line
            if (!association.getSubscriberIdentifier().equals(subscriber.getId()))
            {
                association.setSubscriberIdentifier(subscriber.getId());
            }
            wantedAssociations.add(association);
        }
    }


    /**
     * Removes a set of unwanted SubscriberAuxiliaryServices.
     *
     * @param ctx
     *            The operating context.
     * @param subscriber
     *            The owner of the removed association
     * @param associations
     *            The associations to remove.
     * @throws HomeInternalException
     *             Thrown if there are problems removing the unwanted associations.
     */
    @SuppressWarnings("unchecked")
    public void removeUnwantedAssociations(final Context ctx, final Subscriber subscriber,
            final Collection associations) throws HomeException
    {
        // Manda - added null check
        if (associations != null && associations.size() > 0)
        {
            final Home home = (Home) ctx.get(SubscriberAuxiliaryServiceHome.class);

            final Iterator associationIterator = associations.iterator();
            while (associationIterator.hasNext())
            {
                final SubscriberAuxiliaryService association = (SubscriberAuxiliaryService) associationIterator.next();
                association.setContext(ctx);

                try
                {
                    association.setSubscriber(subscriber);
                    association.setProvisionAction(com.redknee.app.crm.bean.service.ServiceProvisionActionEnum.UNPROVISION);
                    SubscriberAuxiliaryServiceSupport.removeSubscriberAuxiliaryService(ctx, association);
                    Subscriber oldSub = (Subscriber) ctx.get(Lookup.OLDSUBSCRIBER);
                    if(oldSub != null){
                    	 SubscriptionNotificationSupport.sendServiceStateChangeNotification(ctx, oldSub, subscriber,null, association.getAuxiliaryService(ctx), NotificationTypeEnum.SERVICE_STATE_CHANGE,ServiceStateEnum.UNPROVISIONED);
                    }
                }
                catch (final Throwable t)
                {
                    /*
                     * TT#9070600028: Refreshing the auxiliary services of the subscribers in the context.
                     */
                    subscriber.setAuxiliaryServices(null);

                    final String message = "Encountered problem when removing unwanted auxiliary service "
                        + association.getAuxiliaryServiceIdentifier() + " from subscriber "
                        + association.getSubscriberIdentifier();

                    new MajorLogMsg(this, message, t).log(ctx);

                    if (t instanceof ProvisioningHomeException)
                    {
                        throw (ProvisioningHomeException) t;
                    }
                    else
                    {
                        throw new HomeException(t);
                    }
                }
            }
        }
    }


    /**
     * Store set of modified SubscriberAuxiliaryServices.
     *
     * @param ctx
     *            The operating context.
     * @param subscriber
     *            The owner of the associations.
     * @param associations
     *            The associations to remove.
     * @throws HomeException
     *             Thrown if there are problems renewing the refreshed associations.
     */
    @SuppressWarnings("unchecked")
    public void renewRefreshedAssociations(final Context ctx, final Subscriber subscriber,
            final Collection associations) throws HomeException
    {
        final Home home = (Home) ctx.get(SubscriberAuxiliaryServiceHome.class);

        final Iterator associationIterator = associations.iterator();
        while (associationIterator.hasNext())
        {
            final SubscriberAuxiliaryService association = (SubscriberAuxiliaryService) associationIterator.next();
            final SubscriberAuxiliaryService oldAssociation = SubscriberAuxiliaryServiceSupport
                .getSubscriberAuxiliaryServicesWithIdentifiers(ctx, association.getSubscriberIdentifier(),
                        association.getAuxiliaryServiceIdentifier(),association.getSecondaryIdentifier());
            association.setIdentifier(oldAssociation.getIdentifier());
            association.setContext(ctx);

            if (LogSupport.isDebugEnabled(ctx))
            {
                new DebugLogMsg(this, "Storing the Refreshed/modified Subscriber Auxiliary Service "
                    + association.getIdentifier(), null).log(ctx);
            }
            try
            {
                association.setSubscriber(subscriber);
                association.setProvisionAction(com.redknee.app.crm.bean.service.ServiceProvisionActionEnum.UPDATE_ATTRIBUTES);
                home.store(ctx, association);
            }
            catch (final Throwable t)
            {
                final String message = "Encountered problem when storing modified auxiliary service "
                    + association.getAuxiliaryServiceIdentifier() + " from subscriber "
                    + association.getSubscriberIdentifier();

                new MajorLogMsg(this, message, t).log(ctx);

                if (t instanceof ProvisioningHomeException)
                {
                    throw (ProvisioningHomeException) t;
                }
                else
                {
                    throw new HomeException(t);
                }
            }
        }
    }


    /**
     * Save a set of waiting-to-provisioned-in-the-future SubscriberAuxiliaryServices.
     *
     * @param ctx
     *            The operating context.
     * @param chosenfutureAssociations
     *            Collection of newly chosen future associations.
     * @param existingFutureAssociations
     *            Collection of existing future associations.
     * @param newSubscriber
     *            Subscriber related to these associations.
     * @throws HomeException
     *             Thrown if there are problems modifying the future associations.
     */
    @SuppressWarnings("unchecked")
    public void modifyFutureWantedAssociations(final Context ctx, final Collection chosenfutureAssociations,
        final Collection existingFutureAssociations, final Subscriber newSubscriber) throws HomeException
    {
        Collection wantedFutureAssociations = new ArrayList();
        final Collection unwantedFutureAssociations = new ArrayList();
        final Collection refreshedFutureAssociations = new ArrayList();

        final Subscriber oldSubscriber = (Subscriber) ctx.get(Lookup.OLDSUBSCRIBER);

        if (oldSubscriber != null)
        {
            getAddAndRemoveFutureSets(ctx, chosenfutureAssociations, existingFutureAssociations,
                wantedFutureAssociations, unwantedFutureAssociations, refreshedFutureAssociations);
        }
        else
        {
            wantedFutureAssociations = chosenfutureAssociations;
        }

        removeUnwantedAssociations(ctx, newSubscriber, unwantedFutureAssociations);
        renewRefreshedAssociations(ctx, newSubscriber, refreshedFutureAssociations);
        addWantedAssociations(ctx, newSubscriber, wantedFutureAssociations);
    }


    /**
     * Gets collections of wanted and unwanted waiting-to-provisioned-in-the future
     * SubscriberAuxiliaryService associations based on newly chosen associations and
     * existing associations. The wanted associations returned will be those in the chosen
     * collection, but not in the existing collection or . The unwanted associations
     * returned will be those in the existing collection but not in the chosen collection
     * or those in both existing and chosen collection but with modified contents such as
     * start/end date. The contained type of all collections is
     * SubscriberAuxiliaryService.
     *
     * @param ctx
     *            The operating context.
     * @param chosenFutureAssociations
     *            The chosen associations for the subscriber in the future.
     * @param existingFutureAssociations
     *            The existing associations for the subscriber in the future.
     * @param wantedFutureAssociations
     *            [OUT] The wanted associations the subscriber does not yet have.
     * @param unwantedFutureAssociations
     *            [OUT] The existing associations the subscriber no longer wants in the
     *            future.
     * @param refreshedFutureAssociations
     *            [OUT] The set of future associations to be refreshed.
     */
    @SuppressWarnings("unchecked")
    private void getAddAndRemoveFutureSets(final Context ctx, final Collection chosenFutureAssociations,
        final Collection existingFutureAssociations, final Collection wantedFutureAssociations,
        final Collection unwantedFutureAssociations, final Collection refreshedFutureAssociations)
    {
        // check existing entries first
        final Map<Long,Map<Long, SubscriberAuxiliaryService>> chosenFutureIdentifiers = SubscriberAuxiliaryServiceSupport.getSelectionMap(ctx, chosenFutureAssociations);
        for (final Object element : existingFutureAssociations)
        {
            final SubscriberAuxiliaryService existingFutureService = (SubscriberAuxiliaryService) element;
            final long existingFutureServiceId = existingFutureService.getAuxiliaryServiceIdentifier();
            final long existingFutureServiceSecondaryId = existingFutureService.getSecondaryIdentifier();

            
            Map<Long, SubscriberAuxiliaryService> auxServiceMap = chosenFutureIdentifiers.get(Long.valueOf(existingFutureServiceId));
            // entry exists now but not selected any more
            if (auxServiceMap==null || auxServiceMap.get(Long.valueOf(existingFutureServiceSecondaryId)) == null)
            {
                unwantedFutureAssociations.add(existingFutureService);
            }
            else
            {
                // check if content has be modified
                final SubscriberAuxiliaryService chosenFutureService = SubscriberAuxiliaryServiceSupport
                    .getSelectedSubscriberAuxiliaryService(chosenFutureAssociations, existingFutureServiceId, existingFutureServiceSecondaryId);
                chosenFutureService.setIdentifier(existingFutureService.getIdentifier());

                // can't use equals to compare since createdDate, lastmodifiedDate
                // are
                // all different
                // if ( !chosenFutureService.equals(existingFutureService)) // has
                // changed
                final Date existingEndDay = CalendarSupportHelper.get(ctx).getDateWithNoTimeOfDay(existingFutureService.getEndDate());
                final Date chosenEndDay = CalendarSupportHelper.get(ctx).getDateWithNoTimeOfDay(chosenFutureService.getEndDate());
                final Date existingStartDay = CalendarSupportHelper.get(ctx).getDateWithNoTimeOfDay(existingFutureService.getStartDate());
                final Date chosenStartDay = CalendarSupportHelper.get(ctx).getDateWithNoTimeOfDay(chosenFutureService.getStartDate());
                final boolean dateChanged = !existingEndDay.equals(chosenEndDay)
                // TODO determine if startDate should be propagated.
                    || !existingStartDay.equals(chosenStartDay);

                // TODO determine if paymentNum change should be propagated.
                final boolean fieldChanged = chosenFutureService.getPaymentNum() != existingFutureService
                    .getPaymentNum()
                    || isExtraFieldsChanged(ctx, chosenFutureService, existingFutureService.getIdentifier());

                final boolean update = dateChanged || fieldChanged;
                if (update)
                {
                    final Date runningDate = CalendarSupportHelper.get(ctx).getRunningDate(ctx);
                    if (chosenStartDay.after(CalendarSupportHelper.get(ctx).getDateWithNoTimeOfDay(runningDate))
                        && chosenEndDay.after(chosenStartDay))
                    {
                        chosenFutureService.setProvisioned(existingFutureService.getProvisioned());
                        refreshedFutureAssociations.add(chosenFutureService);
                    }
                }
            }
        }

        // then check new entries.
        final Set existingFutureIdentifiers = SubscriberAuxiliaryServiceSupport.getSelectionSet(ctx,
            existingFutureAssociations);

        for (final Object element : chosenFutureAssociations)
        {
            final SubscriberAuxiliaryService chosenFutureService = (SubscriberAuxiliaryService) element;

            if (!existingFutureIdentifiers.contains(Long.valueOf(chosenFutureService.getAuxiliaryServiceIdentifier())))
            {
                wantedFutureAssociations.add(chosenFutureService);
            }
        }
    }


    /**
     * Removes all waiting-to-provisioned SubscriberAuxiliaryServices for a given Subscriber
     *
     * @param ctx
     *            The operating context.
     * @param subscriber
     *            The given subscriber
     * @throws HomeException
     *             Thrown if there are problems removing the future associations.
     */
    public void removeFutureAssociations(final Context ctx, final Subscriber subscriber) throws HomeException
    {
        // find all future Aux.Svc that has start date being today
        final Date runningDate = CalendarSupportHelper.get(ctx).getRunningDate(ctx);

        final List<SubscriberAuxiliaryService> allFutureAssociations = subscriber.getFutureAuxiliaryServices(ctx,
                runningDate);

        removeUnwantedAssociations(ctx, subscriber, allFutureAssociations);
    }


    /**
     * Echos the differences between the associated AuxiliaryServices to the debug logs.
     *
     * @param ctx
     *            The operating context.
     * @param wantedAssociations
     *            [OUT] The wanted associations the subscriber does not yet have.
     * @param unwantedAssociations
     *            [OUT] The existing associations the subscriber no longer wants.
     * @param chosenfutureAssociations
     *            [OUT] The newly chosen associations which are slated to start in the
     *            future.
     * @param existingAssociations
     *            [OUT] Existing associations the subscriber wants to keep.
     */
    @SuppressWarnings("unchecked")
    private void showDifferences(final Context ctx, final Collection wantedAssociations,
        final Collection unwantedAssociations, final Collection chosenfutureAssociations,
        final Collection existingAssociations)
    {
        if (LogSupport.isDebugEnabled(ctx))
        {
            try
            {
                showServices(ctx, unwantedAssociations, "Unwanted SubscriberAuxiliaryService");
                showServices(ctx, wantedAssociations, "Wanted SubscriberAuxiliaryService");
                showServices(ctx, chosenfutureAssociations, "Future SubscriberAuxiliaryService");
                showServices(ctx, existingAssociations, "Existing SubscriberAxuiliaryService");
            }
            catch (final Throwable t)
            {
                if (LogSupport.isDebugEnabled(ctx))
                {
                    new DebugLogMsg(this, "Failed to show differences.", t).log(ctx);
                }
            }
        }
    }


    /**
     * Echos the AuxiliaryServices to the debug logs. This method assumes that DebugLogMsg
     * guarding is already done.
     *
     * @param ctx
     *            The operating context.
     * @param services
     *            The services to echo to the log.
     * @param message
     *            Message for the debug log.
     */
    @SuppressWarnings("unchecked")
    private void showServices(final Context ctx, final Collection services, final String message)
    {
        if (services != null && services.size() > 0)
        {
            final Iterator serviceIterator = services.iterator();
            while (serviceIterator.hasNext())
            {
                final SubscriberAuxiliaryService service = (SubscriberAuxiliaryService) serviceIterator.next();

                if (LogSupport.isDebugEnabled(ctx))
                {
                    new DebugLogMsg(this, message + ": " + service, null).log(ctx);
                }
            }
        }
    }


    /**
     * Updates the given Subscriber's selection of Auxiliary services.
     *
     * @param ctx
     *            The operating context.
     * @param bean
     *            The Subscriber.
     * @throws HomeException
     *             Thrown if there associations update failed.
     */
    private void updateAssociations(final Context ctx, final Object bean) throws HomeException
    {
        final Subscriber newSubscriber = (Subscriber) bean;
        final Subscriber oldSubscriber = (Subscriber) ctx.get(Lookup.OLDSUBSCRIBER);
        final boolean fromCron = ctx.getBoolean(CronConstants.FROM_CRON_AGENT_CTX_KEY, false);
        if (EnumStateSupportHelper.get(ctx).isNotOneOfStates(newSubscriber, UNPROVISION_STATES))
        {
            if (fromCron)
            {
                updateAssociationsFromCron(ctx, newSubscriber, oldSubscriber);
            }
            else
            {
                updateAssociationsNotFromCron(ctx, newSubscriber, oldSubscriber);
            }
        }
        else if(oldSubscriber!=null && oldSubscriber.getState()!=newSubscriber.getState())
        {
            //OLD Subscriber is looked up from context; better check for null
            //if the subscriber is entering into an un-provisioned state from a provisioned state;
            //remove all associations
            final Context subCtx = ctx.createSubContext();
            // when removing a subscriber we should not charge/refund anything
            // subscriber should be disabled, at that time the charging/refund was taken
            // care
            // of
            //subCtx.put(SubscriberAuxiliaryServiceChargingHome.BYPASS_CHARGING, true);
            // if we are removing a subscriber, then we should remove all associations
            removeAllAssociations(subCtx, oldSubscriber);
        }
    }


    /**
     * Updates the associations when it is not a request from cron task.
     *
     * @param ctx
     *            The operating context.
     * @param newSubscriber
     *            The new subscriber.
     * @param oldSubscriber
     *            The existing subscriber.
     * @throws HomeException
     *             Thrown if there are problems updating the associations.
     */
    @SuppressWarnings("unchecked")
    private void updateAssociationsNotFromCron(final Context ctx, final Subscriber newSubscriber,
        final Subscriber oldSubscriber) throws HomeException
    {
        /*
         * TODO 2007-08-11 [Cindy]: This mess probably still needs further refactoring,
         * and possibly some merging with updateAssociationsFromCron.
         */
        Collection chosenFutureAssociations = newSubscriber.getFutureAuxiliaryServices();
        Collection chosenAssociations = new ArrayList(newSubscriber.getAuxiliaryServices(ctx));
        Collection wantedAssociations = new ArrayList();
        Collection existingAssociations = new ArrayList();
        Collection existingFutureAssociations = new ArrayList();

        Collection unwantedAssociations = new ArrayList();

        Collection cronToProvisionAssociations = new ArrayList();

        /*
         * this collection refers to those entries that have been modified ( most likely
         * endDate field) so we need to do store to avoid go through
         * SubscriberAuxiliaryServiceChargingHome and gets charged for service subscriber
         * fee again.
         */
        Collection refreshedAssociations = new ArrayList();

        if (newSubscriber.getState() == SubscriberStateEnum.INACTIVE
            && ctx.get(Common.POSTPAID_PREPAID_CONVERSION_SUBCRIBER) == null)
        {
            chosenAssociations = new ArrayList();
            chosenFutureAssociations = new ArrayList();
        }        
        

        if (oldSubscriber != null)
        {
            existingFutureAssociations = updateAssociationsNotFromCronSubscriberExists(ctx, oldSubscriber,
                chosenAssociations, wantedAssociations, existingAssociations, unwantedAssociations,
                refreshedAssociations);
        }
        else
        {
            wantedAssociations = updateAssociationsNewSubscriber(ctx, newSubscriber, chosenFutureAssociations,
                chosenAssociations);
        }
        
        chosenFutureAssociations = filterPrivateCUGAuxServiceAssociations(ctx, chosenFutureAssociations);
        chosenAssociations = filterPrivateCUGAuxServiceAssociations(ctx, chosenAssociations);
        wantedAssociations = filterPrivateCUGAuxServiceAssociations(ctx, wantedAssociations);
        unwantedAssociations = filterPrivateCUGAuxServiceAssociations(ctx, unwantedAssociations);
        existingAssociations = filterPrivateCUGAuxServiceAssociations(ctx, existingAssociations);
        refreshedAssociations = filterPrivateCUGAuxServiceAssociations(ctx, refreshedAssociations);

        showDifferences(ctx, wantedAssociations, unwantedAssociations, chosenFutureAssociations, existingAssociations);
        removeUnwantedAssociations(ctx, newSubscriber, unwantedAssociations);
        renewRefreshedAssociations(ctx, newSubscriber, refreshedAssociations);
        addWantedAssociations(ctx, newSubscriber, wantedAssociations);
        cronProvisionAssociations(ctx, newSubscriber, cronToProvisionAssociations);

        modifyFutureWantedAssociations(ctx, chosenFutureAssociations, existingFutureAssociations, newSubscriber);
    }


    /**
     * Update associations for a new subscriber.
     *
     * @param newSubscriber
     *            The new subscriber.
     * @param chosenFutureAssociations
     *            Subscriber-auxiliary service associations chosen by the subscriber and
     *            slated for future activation. No association will be added to or removed
     *            from the collection, but each association will be modified.
     * @param chosenAssociations
     *            Subscriber-auxiliary service associations chosen by the subscriber. No
     *            association will be added to or removed from the collection, but each
     *            association will be modified.
     * @return A collection of subscriber-auxiliary service associations wanted by the
     *         subscriber.
     */
    @SuppressWarnings("unchecked")
    private Collection updateAssociationsNewSubscriber(final Context ctx, final Subscriber newSubscriber,
        final Collection chosenFutureAssociations, final Collection chosenAssociations)
    {
        /*
         * from web, when creating a new sub, subscriberId has been set in web to be -1.
         * Its real value hasn't been set until ProvisionHome gets called.
         */
        final Date runningDate = CalendarSupportHelper.get(ctx).getRunningDate(ctx);
        for (final Iterator i = chosenAssociations.iterator(); i.hasNext();)
        {
            final SubscriberAuxiliaryService service = (SubscriberAuxiliaryService) i.next();
            service.setSubscriberIdentifier(newSubscriber.getId());
            
            // When subscriber is prepaid, allow pre-dating.
            if (service.getStartDate().before(runningDate) && newSubscriber.isPrepaid())
            {
                service.setStartDate(runningDate);
            }
            service.setProvisioned(true);
        }

        // set subscriber identifier for future subauxservcies also.
        for (final Iterator i = chosenFutureAssociations.iterator(); i.hasNext();)
        {
            final SubscriberAuxiliaryService service = (SubscriberAuxiliaryService) i.next();
            service.setSubscriberIdentifier(newSubscriber.getId());
        }
        return chosenAssociations;
    }


    /**
     * Update the associations for an existing subscriber when the request is not from a
     * cron task.
     *
     * @param ctx
     *            The operating context.
     * @param oldSubscriber
     *            The existing subscriber.
     * @param chosenAssociations
     *            Subscriber-auxiliary service associations chosen by the subscriber. No
     *            association will be added to or removed from the collection, but each
     *            association will be modified.
     * @param wantedAssociations
     *            [OUT] Subscriber-auxiliary service associations wanted by the
     *            subscriber.
     * @param existingAssociations
     *            [OUT] Existing subscriber-auxiliary service associations.
     * @param unwantedAssociations
     *            [OUT] Subscriber-auxiliary service associations no longer wanted by the
     *            subscriber.
     * @param refreshedAssociations
     *            [OUT] Existing subscriber-auxiliary service associations which need to
     *            be refreshed.
     * @return A collection of existing subscriber-auxiliary service associations which
     *         are slated for future activation.
     * @throws HomeException
     *             Thrown if there are problems updating the associations.
     */
    @SuppressWarnings("unchecked")
    private Collection updateAssociationsNotFromCronSubscriberExists(final Context ctx, final Subscriber oldSubscriber,
        final Collection chosenAssociations, final Collection wantedAssociations,
        final Collection existingAssociations, final Collection unwantedAssociations,
        final Collection refreshedAssociations) throws HomeException
    {
        final Collection allExistingAssociations = oldSubscriber.getAuxiliaryServices(ctx);

        final Date runningDate = CalendarSupportHelper.get(ctx).getRunningDate(ctx);

        final Collection existingFutureAssociations = oldSubscriber.getFutureAuxiliaryServices(ctx, runningDate);

        for (final Object element : allExistingAssociations)
        {
            final SubscriberAuxiliaryService service = (SubscriberAuxiliaryService) element;

            if (!existingFutureAssociations.contains(service))
            {
                existingAssociations.add(service);
            }
        }

        /*
         * only handle waiting-to-unprovision/need-to-provision-now auxiliary service
         */
        getAddAndRemoveSets(ctx, chosenAssociations, existingAssociations, wantedAssociations, unwantedAssociations,
            refreshedAssociations, oldSubscriber);

        return existingFutureAssociations;
    }


    /**
     * Updates the associations when it is a request from cron task.
     *
     * @param ctx
     *            The operating context.
     * @param newSubscriber
     *            The new subscriber.
     * @param oldSubscriber
     *            The existing subscriber.
     * @throws HomeException
     *             Thrown if there are problems updating the associations.
     */
    @SuppressWarnings("unchecked")
    private void updateAssociationsFromCron(final Context ctx, final Subscriber newSubscriber,
        final Subscriber oldSubscriber) throws HomeException
    {
        Collection chosenFutureAssociations = newSubscriber.getFutureAuxiliaryServices();
        Collection chosenAssociations = newSubscriber.getAuxiliaryServices(ctx);
        Collection wantedAssociations = new ArrayList();
        Collection existingAssociations = new ArrayList();
        Collection existingFutureAssociations = new ArrayList();

        Collection unwantedAssociations = new ArrayList();

        Collection cronToProvisionAssociations = new ArrayList();

        /*
         * this collection refers to those entries that have been modified ( most likely
         * endDate field) so we need to do store to avoid go through
         * SubscriberAuxiliaryServiceChargingHome and gets charged for service subscriber
         * fee again.
         */
        Collection refreshedAssociations = new ArrayList();

        /*
         * Prasanna: If subscriber moves from active/permitteable state to inactive state
         * i.e. If subscriber is deactivated then all the auxiliary services should be
         * removed from his/her profile hence making chosenAssociations empty. Also if
         * subscriber is deactivated because it is being converted from postpaid to
         * prepaid then don't remove its auxiliary services because for those services,
         * only subscriber Id will be changed in SubscriberAuxiliaryServiceXDBHome to
         * point to new SubscriberID. That functionality is in PostpaidPrepaidConversion
         * class.
         */
        if (newSubscriber.getState() == SubscriberStateEnum.INACTIVE
            && newSubscriber.getSubscriberType().equals(SubscriberTypeEnum.POSTPAID)
            && ctx.get(Common.POSTPAID_PREPAID_CONVERSION_SUBCRIBER) == null)
        {
            chosenAssociations = new ArrayList();
            chosenFutureAssociations = new ArrayList();
        }

        if (oldSubscriber != null)
        {
            final Collection allExistingAssociations = SubscriberAuxiliaryServiceSupport
                .getSubscriberAuxiliaryServices(ctx, oldSubscriber.getId());

            final Date runningDate = CalendarSupportHelper.get(ctx).getRunningDate(ctx);

            existingFutureAssociations = oldSubscriber.getFutureAuxiliaryServices(ctx, runningDate);

            boolean isFromUnProvisionAuxSvcCronAgent = ctx.getBoolean(CronConstants.FROM_UNPROV_CRON_AGENT_CTX_KEY,
                false);
            for (final Object existingAssociation : allExistingAssociations)
            {
                final SubscriberAuxiliaryService service = (SubscriberAuxiliaryService) existingAssociation;
                if (!existingFutureAssociations.contains(service))
                {
                    if (!isFromUnProvisionAuxSvcCronAgent)
                    {
                        if (service.isProvisioned())
                        {
                            existingAssociations.add(service);
                        }
                        else
                        {
                            // unprovisioned, start date is not in future
                            // refers entries that are passed by cron agent and have
                            // start date set to be
                            // runningDate so waiting for cron to activate.
                            if (!CalendarSupportHelper.get(ctx).getDateWithNoTimeOfDay(service.getStartDate()).after(
                                CalendarSupportHelper.get(ctx).getDateWithNoTimeOfDay(runningDate)))
                            {
                                unwantedAssociations.add(service);
                            }
                            else
                            {
                                // unprovisioned and also not future entry, not likely
                            }
                        }
                    }
                    else
                    {
                        if (service.isProvisioned())
                        {
                            // currently active, has today as endDate
                            if (CalendarSupportHelper.get(ctx).getDateWithNoTimeOfDay(service.getEndDate()).equals(
                                CalendarSupportHelper.get(ctx).getDateWithNoTimeOfDay(runningDate)))
                            {
                                unwantedAssociations.add(service);
                            }
                            else
                            {
                                existingAssociations.add(service);
                            }
                        }
                    }
                }
            }

            // only handle waiting-to-unprovision/need-to-provision-now auxiliary service
            getAddAndRemoveSets(ctx, chosenAssociations, existingAssociations, wantedAssociations,
                unwantedAssociations, refreshedAssociations, oldSubscriber);
        }
        else
        {
            wantedAssociations = updateAssociationsNewSubscriber(ctx, newSubscriber, chosenFutureAssociations,
                chosenAssociations);
        }
        
        chosenFutureAssociations = filterPrivateCUGAuxServiceAssociations(ctx, chosenFutureAssociations);
        chosenAssociations = filterPrivateCUGAuxServiceAssociations(ctx, chosenAssociations);
        wantedAssociations = filterPrivateCUGAuxServiceAssociations(ctx, wantedAssociations);
        unwantedAssociations = filterPrivateCUGAuxServiceAssociations(ctx, unwantedAssociations);
        existingAssociations = filterPrivateCUGAuxServiceAssociations(ctx, existingAssociations);
        refreshedAssociations = filterPrivateCUGAuxServiceAssociations(ctx, refreshedAssociations);

        showDifferences(ctx, wantedAssociations, unwantedAssociations, chosenFutureAssociations, existingAssociations);
        removeUnwantedAssociations(ctx, newSubscriber, unwantedAssociations);
        renewRefreshedAssociations(ctx, newSubscriber, refreshedAssociations);
        addWantedAssociations(ctx, newSubscriber, wantedAssociations);
        cronProvisionAssociations(ctx, newSubscriber, cronToProvisionAssociations);
        if (existingFutureAssociations == null)
        {
            existingFutureAssociations = new ArrayList();
        }

        modifyFutureWantedAssociations(ctx, chosenFutureAssociations, existingFutureAssociations, newSubscriber);
    }
    

    /**
     * Filter out Private cug aux service associations, these associations are handled by CUG pipeline.
     * @param col
     * @return
     */
    private Collection filterPrivateCUGAuxServiceAssociations(Context ctx, Collection col)
    {
        List<SubscriberAuxiliaryService> list = new ArrayList<SubscriberAuxiliaryService>();
        for (Object obj : col)
        {
            SubscriberAuxiliaryService subAuxService = (SubscriberAuxiliaryService) obj;            
            try
            {
                if (!subAuxService.getAuxiliaryService(ctx).isPrivateCUG(ctx))
                {
                    list.add(subAuxService);
                }
            }
            catch (HomeException e)
            {
                LogSupport.minor(ctx, this, "Failed to determine aux service type. Can not filter PCUG aux service.");
            }
        }
        return list;
    }


    /**
     * Adds a set of wanted SubscriberAuxiliaryServices ( passed in by nightly cron agent ).
     *
     * @param ctx
     *            The operating context.
     * @param subscriber
     *            The owner of the new association
     * @param cronToProvisionAssociations
     *            The associations to add.
     * @throws HomeException
     *             Thrown if the associations cannot be provisioned.
     */
    @SuppressWarnings("unchecked")
    private void cronProvisionAssociations(final Context ctx, final Subscriber subscriber,
            final Collection cronToProvisionAssociations) throws HomeException
    {
        addWantedAssociations(ctx, subscriber, cronToProvisionAssociations);
    }


    /**
     * Determines whether the homezone extra parameters have changed.
     *
     * @param ctx
     *            The operating context.
     * @param chosenService
     *            The homezone subscriber-auxiliary service association.
     * @param existingServiceId
     *            The ID of the existing association.
     * @return Whether Homezone extra parameters have changed.
     */
    private boolean isHomeZoneNisChanged(final Context ctx, final SubscriberAuxiliaryService chosenService,
        final long existingServiceId)
    {
        if (chosenService.getType(ctx) != null
            && chosenService.getType(ctx).getIndex() == AuxiliaryServiceTypeEnum.HomeZone_INDEX)
        {
            final Home subHomeZoneHome = (Home) ctx.get(SubscriberHomezoneHome.class);
            SubscriberHomezone subZone = null;
            try
            {
                subZone = (SubscriberHomezone) subHomeZoneHome.find(ctx, Long.valueOf(existingServiceId));
                if (subZone == null)
                {
                    new MajorLogMsg(
                        this,
                        "Can not get the corresponding entry for subscriber auxiliary service in Subscriberhomezone home, check whether both the tables are in sync or not,existingSubscriberAuxiliaryServiceId="
                            + existingServiceId + " the chosen subscriber auxiliary service:" + chosenService, null)
                        .log(ctx);
                    return false;
                }

            }
            catch (final HomeException he)
            {
                new MajorLogMsg(
                    this,
                    "Can not get the corresponding entry for subscriber auxiliary service in Subscriberhomezone home, check whether both the tables are in sync or not,existingSubscriberAuxiliaryServiceId="
                        + existingServiceId + " the chosen subscriber auxiliary service:" + chosenService, he).log(ctx);
                return false;
            }

            final Subscriber oldSub = (Subscriber) ctx.get(Lookup.OLDSUBSCRIBER);
            final Subscriber newSub = (Subscriber) ctx.get(Subscriber.class);

            if (subZone.getHzPriority() != chosenService.getHzPriority(ctx)
                || subZone.getHzX() != chosenService.getHzX(ctx)
                || subZone.getHzY() != chosenService.getHzY(ctx)
                || !SafetyUtil.safeEquals(oldSub.getMSISDN(), newSub.getMSISDN()))
            {
                // we will have to add the support for change MSISDN here also
                // i.e. if MSISDN is changed then even if nothing is changed from homezone
                // add the service to renew
                return true;
            }
        }
        return false;
    }


    /**
     * Determines whether the voicemail extra parameters have changed.
     *
     * @param ctx
     *            The operating context.
     * @param chosenService
     *            The voicemail subscriber-auxiliary service association.
     * @param existingServiceId
     *            The ID of the existing association.
     * @return Whether voicemail extra parameters have changed.
     */
    private boolean isVoicemailNisChanged(final Context ctx, final SubscriberAuxiliaryService chosenService,
        final long existingServiceId)
    {
        if (chosenService.getType(ctx) != null
            && chosenService.getType(ctx).getIndex() == AuxiliaryServiceTypeEnum.Voicemail_INDEX)
        {
            final Subscriber oldSub = (Subscriber) ctx.get(Lookup.OLDSUBSCRIBER);
            final Subscriber newSub = (Subscriber) ctx.get(Subscriber.class);

            if (!SafetyUtil.safeEquals(oldSub.getMSISDN(), newSub.getMSISDN())
                || oldSub.getState() != newSub.getState())
            {
                // we will have to add the support for changeMsisdn and subscriber state change
                return true;
            }
        }
        return false;
    }


    /**
     * Determines whether the VPN extra parameters have changed.
     *
     * @param ctx
     *            The operating context.
     * @param chosenFutureService
     *            The VPN subscriber-auxiliary service association.
     * @param identifier
     *            The ID of the existing association.
     * @return Whether VPN extra parameters have changed.
     */
    private boolean isVpnFieldsChanged(final Context ctx, final SubscriberAuxiliaryService chosenFutureService,
        final long identifier)
    {
        return false;
    }


    /**
     * Determines if any of the extra fields has changed.
     *
     * @param context
     *            The operating context.
     * @param newAssociation
     *            The new subscriber-auxiliary service association.
     * @param existingAssociationId
     *            The ID of the existing association.
     * @return Return <code>true</code> if any of the extra fields has been modified,
     *         <code>false</code> otherwise.
     */
    private boolean isAdditionalMsisdnFieldsChanged(final Context context,
        final SubscriberAuxiliaryService newAssociation, final long existingAssociationId)
    {
        boolean result = false;
        if (newAssociation.getType(context) == AuxiliaryServiceTypeEnum.AdditionalMsisdn
            && AdditionalMsisdnAuxiliaryServiceSupport.isAdditionalMsisdnEnabled(context))
        {
            final String newMsisdn = newAssociation.getAMsisdn(context);
            Msisdn msisdn = null;
            try
            {
                msisdn = AdditionalMsisdnAuxiliaryServiceSupport.getAMsisdn(context, existingAssociationId);
            }
            catch (final HomeException exception)
            {
                new DebugLogMsg(this, "Exception caught when looking up existing MSISDN; assuming null", exception)
                    .log(context);
            }
            final boolean isNewMsisdnEmpty = newMsisdn == null || newMsisdn.trim().length() == 0;
            final boolean isOldMsisdnEmpty = msisdn == null || msisdn.getMsisdn().length() == 0;
            if (isNewMsisdnEmpty != isOldMsisdnEmpty)
            {
                result = true;
            }
            else if (!newMsisdn.equals(msisdn.getMsisdn()))
            {
                result = true;
            }
            else
            {
                final Subscriber newSubscriber = (Subscriber) context.get(Subscriber.class);
                if (newSubscriber != null
                    && SafetyUtil.safeEquals(newSubscriber.getState(), SubscriberStateEnum.INACTIVE))
                {
                    result = true;
                }

            }
        }
        return result;
    }


    /**
     * Determines if any of the extra fields has changed.
     *
     * @param context
     *            The operating context.
     * @param newAssociation
     *            The new subscriber-auxiliary service association.
     * @param oldAssociationId
     *            The ID of the existing association.
     * @return Return <code>true</code> if any of the extra fields has been modified,
     *         <code>false</code> otherwise.
     */
    private boolean isExtraFieldsChanged(final Context context, final SubscriberAuxiliaryService newAssociation,
        final long oldAssociationId)
    {
        boolean updated = false;
        updated |= isHomeZoneNisChanged(context, newAssociation, oldAssociationId);
        updated |= isVoicemailNisChanged(context, newAssociation, oldAssociationId);
        updated |= isVpnFieldsChanged(context, newAssociation, oldAssociationId);
        updated |= isAdditionalMsisdnFieldsChanged(context, newAssociation, oldAssociationId);
        return updated;
    }

} // class
