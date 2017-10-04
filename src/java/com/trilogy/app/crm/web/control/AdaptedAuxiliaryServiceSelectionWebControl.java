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
package com.trilogy.app.crm.web.control;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;

import javax.servlet.ServletRequest;

import com.trilogy.app.crm.amsisdn.AdditionalMsisdnAuxiliaryServiceSupport;
import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AdditionalMsisdnBean;
import com.trilogy.app.crm.bean.AuxiliaryServiceSelection;
import com.trilogy.app.crm.bean.AuxiliaryServiceSelectionXInfo;
import com.trilogy.app.crm.bean.AuxiliaryServiceStateEnum;
import com.trilogy.app.crm.bean.AuxiliaryServiceTypeEnum;
import com.trilogy.app.crm.bean.FixedIntervalTypeEnum;
import com.trilogy.app.crm.bean.HomeZoneFieldsBean;
import com.trilogy.app.crm.bean.Msisdn;
import com.trilogy.app.crm.bean.OneTimeTypeEnum;
import com.trilogy.app.crm.bean.SctAuxiliaryService;
import com.trilogy.app.crm.bean.SctAuxiliaryServiceXInfo;
import com.trilogy.app.crm.bean.ServicePeriodEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberAuxiliaryService;
import com.trilogy.app.crm.bean.SubscriberAuxiliaryServiceXInfo;
import com.trilogy.app.crm.bean.SubscriberHomezone;
import com.trilogy.app.crm.bean.VoicemailFieldsBean;
import com.trilogy.app.crm.bean.VpnBean;
import com.trilogy.app.crm.bean.core.custom.AuxiliaryService;
import com.trilogy.app.crm.extension.auxiliaryservice.core.AddMsisdnAuxSvcExtension;
import com.trilogy.app.crm.extension.auxiliaryservice.core.HomeZoneAuxSvcExtension;
import com.trilogy.app.crm.extension.auxiliaryservice.core.SPGAuxSvcExtension;
import com.trilogy.app.crm.homezone.ExtraFieldsPresent;
import com.trilogy.app.crm.homezone.HomezoneInfoValidator;
import com.trilogy.app.crm.support.AccountSupport;
import com.trilogy.app.crm.support.AuxiliaryServiceSupport;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.app.crm.support.EnumStateSupportHelper;
import com.trilogy.app.crm.support.ExtensionSupportHelper;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.app.crm.support.SubscriberAuxiliaryServiceSupport;
import com.trilogy.app.crm.support.SubscriberCreateTemplateSupport;
import com.trilogy.app.crm.support.SubscriberSupport;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.NEQ;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.webcontrol.AbstractWebControl;
import com.trilogy.framework.xhome.webcontrol.NoActionsWebControl;
import com.trilogy.framework.xhome.webcontrol.ProxyWebControl;
import com.trilogy.framework.xhome.webcontrol.WebControl;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MajorLogMsg;
import com.trilogy.framework.xlog.log.MinorLogMsg;


/**
 * Provides a web control for presenting AuxiliaryService selection options on the
 * subscriber pane. The intent is that this web control present for the subscriber all
 * available PLPs and only those CUGs to which the subscriber belongs.
 *
 * @author gary.anderson@redknee.com
 * @author prasanna.kulkarni@redknee.com
 * @author aaron.gourley@redknee.com
 */
public class AdaptedAuxiliaryServiceSelectionWebControl extends ProxyWebControl
{
    /**
     * Creates a new AdaptedAuxiliaryServiceSelectionWebControl.
     */
    public AdaptedAuxiliaryServiceSelectionWebControl()
    {
        super(new NoActionsWebControl(createTableWebControl()));
    }


    /**
     * Creates the specialized TableWebControl that modifies the way the default
     * TableWebControl handles the accept/reject checkboxes that appear in the first
     * column of the table.
     *
     * @return A specialized TableWebControl.
     */
    private static WebControl createTableWebControl()
    {
        final WebControl control = new SctAuxiliaryServiceSelectionTableWebControl()
        {
            @Override
            protected boolean isSelected(final Context context, final AuxiliaryServiceSelection selection)
            {
                 return selection.isChecked() || vpnChecked(context, selection);
            }

            
            /**
             * Checks if VPN is checked or not.
             *
             * @param context
             *            Context object
             * @param selection
             *            AuxiliaryServiceSelection object
             * @return boolean value representing whether VPN is checked or not.
             */
            private boolean vpnChecked(final Context context, final AuxiliaryServiceSelection selection)
            {
                boolean vpnChecked = false;
                
                final Object bean = context.get(AbstractWebControl.BEAN);
                if (bean instanceof Subscriber)
                {
                    final Subscriber subscriber = (Subscriber) bean;
                    try
                    {
                        if (selection.getType().equals(AuxiliaryServiceTypeEnum.Vpn))
                        {
                            final VpnBean vpnBean = (VpnBean) selection.getExtraFields();
                            if (vpnBean != null && !vpnBean.getVpnMsisdn().equals(subscriber.getMSISDN()))
                            {
                                final Subscriber groupVpnSubscriber = SubscriberSupport.lookupSubscriberForMSISDN(
                                        context,
                                        vpnBean.getVpnMsisdn());
                                if (groupVpnSubscriber != null)
                                {
                                    final SubscriberAuxiliaryService vpnSubAux = 
                                        SubscriberAuxiliaryServiceSupport.getSubscriberAuxiliaryServicesBySubIdAndSvcId(
                                                context, 
                                                groupVpnSubscriber.getId(),
                                                selection.getSelectionIdentifier());

                                    if (vpnSubAux != null)
                                    {
                                        vpnChecked = true;
                                    }
                                }
                            }
                        }
                    }
                    catch (final HomeException e)
                    {
                        LogSupport.debug(context, AdaptedAuxiliaryServiceSelectionWebControl.class.getName(),
                                "Home Exception : " + e.getMessage(), e);
                    }
                }
                
                return vpnChecked;
            }
        };
        return control;
    }


    /**
     * Inherit Method overrided to achieve custom functionality.
     *
     * @param context
     *            Context object
     * @param out
     *            java.io.PrintWriter object
     * @param name
     *            String object
     * @param obj
     *            current object coming from the pipeline
     * @Override
     */
    
    
    
    
    @Override
    public void toWeb(final Context context, final PrintWriter out, final String name, final Object obj)
    {
        final Context subContext = context.createSubContext();

        final Subscriber subscriber = (Subscriber) subContext.get(AbstractWebControl.BEAN);
        Collection associations = (Collection) obj;

        final Collection<AuxiliaryServiceSelection> services;
        if (subscriber != null && !subscriber.getId().equals("-1"))
        {
            if (associations == null)
            {
                associations = subscriber.getAuxiliaryServices(context);
                if (associations == null)
                {
                    associations = new ArrayList();
                }
            }
            services = getSelectableAuxiliaryServices(subContext, subscriber, associations);   
        }
        else
        {
            if (LogSupport.isDebugEnabled(context))
            {
                new DebugLogMsg(this, "Subscriber is null.  No selectable auxiliary services!", null).log(context);
            }
            services = new ArrayList<AuxiliaryServiceSelection>();
        }

        if (LogSupport.isDebugEnabled(context))
        {
            new DebugLogMsg(this, "Selectable auxiliary services for subscriber " + subscriber.getId() + " are " + services, null).log(context);
        }

        super.toWeb(subContext, out, name, services);
    }

    
    private void setSpecialFields(final Context ctx, 
    		final AuxiliaryServiceSelection selection, 
    		final AuxiliaryService service)
    {
 
    	if (service.isPrivateCUG(ctx))
        {
     	   	   selection.getHiddenFields().add(AuxiliaryServiceSelectionXInfo.CHARGE);
        	   selection.getReadOnlyFields().add(AuxiliaryServiceSelectionXInfo.START_DATE);
        	   selection.getReadOnlyFields().add(AuxiliaryServiceSelectionXInfo.END_DATE);
        	   selection.getReadOnlyFields().add(AuxiliaryServiceSelectionXInfo.PAYMENT_NUM);
        } 

    }
    
    


    /**
     * Gets a Collection of AuxiliaryServiceSelections for the given subscriber.
     * Selections are based on type and existing associations. An AuxiliaryServices is
     * selectable if it is a PLP type with the same SPID as the subscriber, or if it is a
     * CUG type that is already associated with the subscriber TODO: or basic type with
     * the same SPID with the subscriber, or provisionable type with the same SPID with
     * the subscriber .
     *
     * @param context
     *            The operating context.
     * @param subscriber
     *            The subscriber for which to get selectable AuxiliaryServices.
     * @param associations
     *            The SubscriberAuxiliaryServices for the currently associated
     *            AuxiliaryServices.
     * @return A Collection of AuxiliaryServiceSelections.
     */
    protected Collection<AuxiliaryServiceSelection> getSelectableAuxiliaryServices(final Context context,
            final Subscriber subscriber, final Collection associations)
    {
        // Notes about associations:
        // this passed-in associations contains the all the
        // SubscriberAuxiliaryService
        // entries stored for this subscriber profile, therefore including
        // waiting-to-unprovisioned(currently
        // active),waiting-to-provisioned-in-the-future,
        /*
         * Collection futureProvisionAuxSvc = null; try { futureProvisionAuxSvc =
         * SubscriberAuxiliaryServiceSupport.getSubscriberFutureProvisionAuxSvcs(context,
         * subscriber, new Date()); } catch(HomeException e) { //TODO: resolve it in
         * getSubscriberFutureProvisionAuxSvcs method } if ( futureProvisionAuxSvc != null
         * ) { associations.addAll(futureProvisionAuxSvc); }
         */
        if (LogSupport.isDebugEnabled(context))
        {
            new DebugLogMsg(this, "Associations inside getSelectableAuxiliaryServices = " + associations, null)
                .log(context);
        }
        Collection<SctAuxiliaryService> sctAuxColl = null;
        Long satId = null;
        int spid = 0;
        SortedSet selectionSet = null;
        Collection<AuxiliaryService> services = null;

        final Date runningDate = (Date) context.get("RunningDate") == null ? new Date() : (Date) context
            .get("RunningDate");
        // this services contains all PLP, CUG, Basic, Provisionable types
        // with the same spid as the subscriber
        if (subscriber != null)
        {
            services = getAllAvailableAuxiliaryServices(context, subscriber);
            // checked entries from home
            selectionSet = SubscriberAuxiliaryServiceSupport.getSelectionSet(subscriber.getAuxiliaryServices(context));
            satId = Long.valueOf(subscriber.getSatId());
            spid = subscriber.getSpid();
        }
        else
        {
            final Object obj = context.get(AbstractWebControl.BEAN);
            if (obj instanceof Subscriber)
            {
                satId = Long.valueOf(((Subscriber) obj).getSatId());
                spid = ((Subscriber) obj).getSpid();
            }
            if (associations != null && associations.iterator().hasNext()
                && associations.iterator().next() instanceof AuxiliaryService)
            {
                services = associations;
            }
            else
            {
                services = getAllAvailableAuxiliaryServices(context, obj != null ? (Subscriber) obj : null);
            }

            try
            {
                // MANDA - Get all the sct specific auxiliary services.
                sctAuxColl = getSctAuxiliaryServicesForSat(context, satId);
            }
            catch (final HomeException e)
            {
                new MajorLogMsg(this, "Failed to look-up available sct-auxiliary services.", e).log(context);
            }
            selectionSet = SubscriberAuxiliaryServiceSupport.getSelectionSetFromSat(selectionSet, sctAuxColl);
        }

        // checked entries from home
        // selectionSet =
        // SubscriberAuxiliaryServiceSupport.getSelectionSet(associations);
        final ArrayList<AuxiliaryServiceSelection> selectableServices = new ArrayList<AuxiliaryServiceSelection>();

        final Collection<AuxiliaryService> services_bkp = new ArrayList<AuxiliaryService>(services);

        // all availble Aux.Svc
        Iterator serviceIterator = services.iterator();

        if (serviceIterator.hasNext())
        {
            final Object obj = serviceIterator.next();
            if (!(obj instanceof AuxiliaryService))
            {
                if (((SubscriberAuxiliaryService) obj).getSubscriberIdentifier().equals("-1"))
                {
                    services = AuxiliaryServiceSupport.getAllAvailableAuxiliaryServices(context, (subscriber != null
                        ? subscriber.getSpid()
                        : spid));
                    serviceIterator = services.iterator();
                }
            }
            else
            {
                services = services_bkp;
                serviceIterator = services.iterator();
            }
        }

        while (serviceIterator.hasNext())
        {
            final AuxiliaryService service = (AuxiliaryService) serviceIterator.next();

            if (selectionSet != null && selectionSet.contains(Long.valueOf(service.getIdentifier())))
            {
                Collection<SubscriberAuxiliaryService> existingServices = null;
                if (subscriber != null)
                {
                    existingServices = SubscriberAuxiliaryServiceSupport.getSelectedSubscriberAuxiliaryServices(
                        subscriber.getAuxiliaryServices(context), service.getIdentifier());
                }
                
                if( existingServices == null || existingServices.size() == 0 )
                {
                    Collection<SctAuxiliaryService> sctAuxExitingServices = SubscriberCreateTemplateSupport.getSelectedSctAuxiliaryServices(sctAuxColl, service.getIdentifier());
                    if( sctAuxExitingServices == null || sctAuxExitingServices.size() == 0 )
                    {
                        AuxiliaryServiceSelection selection = adapt(context, service);
                        selection.setChecked(true);
                        selection.setExtraFields(getExtraFields(context, selection, subscriber, service, null, true));
                        selectableServices.add(selection); 
                    }
                    for( SctAuxiliaryService sctAuxExitingService : sctAuxExitingServices )
                    {
                        AuxiliaryServiceSelection selection = adapt(context, service);
                        selection.setChecked(true);
                        setStartAndEndDates(context, runningDate, selection, sctAuxExitingService, service);
                        selection.setExtraFields(getExtraFields(context, selection, subscriber, service, null, true));
                        selectableServices.add(selection); 
                    }
                }
                else // To avoid NullPointerException reported in TT9011200003
                {
                    for( SubscriberAuxiliaryService existingService : existingServices )
                    {
                        if (AuxiliaryServiceTypeEnum.MultiSIM.equals(service.getType())
                                && existingService.getSecondaryIdentifier() != SubscriberAuxiliaryService.DEFAULT_SECONDARYIDENTIFIER)
                        {
                            // Don't show SIM specific auxiliary services in the Rating tab
                            if (LogSupport.isDebugEnabled(context))
                            {
                                new DebugLogMsg(this, 
                                        "Hiding SIM-specifc Multi-SIM auxiliary service association [AuxSvcId="
                                        + existingService.getAuxiliaryServiceIdentifier()
                                        + ",SecondaryId=" + existingService.getSecondaryIdentifier()
                                        + ",SubId=" + existingService.getSubscriberIdentifier() + "] from GUI.", null).log(context);
                            }
                            continue;
                        }
                        
                        AuxiliaryServiceSelection selection = adapt(context, service);
                        selection.setChecked(true);
                        selection.setEnabled(existingService.getProvisioned());
                        selection.setStartDate(existingService.getStartDate());
                        selection.setEndDate(existingService.getEndDate());
                        selection.setPaymentNum(existingService.getPaymentNum());
                        selection.setSecondaryId(existingService.getSecondaryIdentifier());
                        selection.setIsfeePersonalizationApplied(existingService.getIsfeePersonalizationApplied());
                        selection.setPersonalizedFee(existingService.getPersonalizedFee());
                        selection.setExtraFields(getExtraFields(context, selection, subscriber, service, existingService, true));
                        selectableServices.add(selection); 
                        this.setSpecialFields(context, selection, service);
                        selection.setNextRecurringChargeDate(existingService.getNextRecurringChargeDate());
                        selection.setRecurrenceInterval(service.getRecurrenceInterval());
                    }
                }
            }
            else if (service.getState() != AuxiliaryServiceStateEnum.DEPRECATED)
            {
                if (service.getType() == AuxiliaryServiceTypeEnum.Vpn)
                {
                    final AuxiliaryServiceSelection selection = adapt(context, service);
                    final boolean displayExtraFields = isMom2(context, subscriber, selection);
                    if (displayExtraFields)
                    {
                        selection.setExtraFields(getExtraFields(context, selection, subscriber, service));
                        selectableServices.add(selection);
                    }
                }
                else if (!service.isCUG(context) && !service.isPrivateCUG(context))
                {
                    final AuxiliaryServiceSelection selection = adapt(context, service);
                    selection.setExtraFields(getExtraFields(context, selection, subscriber, service));
                    selectableServices.add(selection);
                }
            }
        }
        return selectableServices;
    }


    /**
     * Sets the start and end date to the screen subscriber auxiliary services
     *
     * @param context
     *            the operating context
     * @param runningDate
     *            the start date to be running
     * @param selection
     *            the bean to be set to
     * @param sctAuxExitingService
     *            the service to calculate the start and end date base don the nmumber of
     *            payments
     * @param service
     *            the service to calulate the start and end date based on the one time
     *            charge
     */
    private void setStartAndEndDates(final Context context, final Date runningDate,
        final AuxiliaryServiceSelection selection, final SctAuxiliaryService sctAuxExitingService,
        final AuxiliaryService service)
    {
        if (ServicePeriodEnum.ONE_TIME.equals(service.getChargingModeType()))
        {
            if (service.getRecurrenceType() == OneTimeTypeEnum.ONE_OFF_FIXED_DATE_RANGE)
            {
                final Date today = new Date();
                selection.setStartDate(service.getStartDate());
                if (service.getStartDate().before(today))
                {
                    selection.setStartDate(today);
                }
                selection.setEndDate(service.getEndDate());
            }
            else
            {
                selection.setStartDate(runningDate);

                if (service.getFixedInterval() == FixedIntervalTypeEnum.DAYS)
                {
                    selection.setEndDate(CalendarSupportHelper.get(context).findDateDaysAfter(service.getValidity(), runningDate));
                }
                else
                {
                    selection.setEndDate(CalendarSupportHelper.get(context).findDateMonthsAfter(service.getValidity(), runningDate));

                }
            }
        }
        else
        {
            // Only if it has something to do with the SctAuxiliaryService selection
            if (sctAuxExitingService != null)
            {
                // Manda- code change to set the start date to running
                // date.

                selection.setStartDate(runningDate);
                // TODO - Probably after weekly recurring charges need
                // to check for charging mode type
                Date endDate = CalendarSupportHelper.get(context).findDateMonthsAfter(sctAuxExitingService.getPaymentNum(), runningDate);
                endDate = CalendarSupportHelper.get(context).findDateDaysAfter(-1, endDate);
                selection.setEndDate(endDate);
                selection.setPaymentNum(sctAuxExitingService.getPaymentNum());
            }
        }
    }



    /**
     * Gets all the available AuxiliaryServices for the given subscriber. An
     * AuxiliaryService is available if it has the same SPID as the subscriber.
     *
     * @param context
     *            The operating context.
     * @param subscriber
     *            The subscriber for which to get all the available AuxiliaryServices.
     * @return All the available AuxiliaryServices for the given subscriber.
     */
    protected Collection<AuxiliaryService> getAllAvailableAuxiliaryServices(final Context context, final Subscriber subscriber)
    {
        return AuxiliaryServiceSupport.getAllAvailableAuxiliaryServices(context, subscriber.getSpid());
    }


    /**
     * Returns true if the auxiliary service is allowed to be selected from the subscriber screen.
     * 
     * @param service Auxiliary Service
     * @return true if the auxiliary service is allowed to be selected from the subscriber screen.
     */
    protected boolean isSelectable(Context ctx, AuxiliaryService service)
    {
        boolean result = true;

        if (!EnumStateSupportHelper.get(ctx).stateEquals(service, AuxiliaryServiceStateEnum.ACTIVE))
        {
            result = false;
        }
        else if (service.isCUG(ctx))
        {
            // CUG is not selectable. It can only be provisioned through CUG screen.
            result = false;
        }
        
        return result;
    }


    /**
     * Returns TRUE if the account is a MOM account (VPN or ICM) and 1) if the subscriber
     * is the VPN Msisdn, or 2) if there are auxiliary services chosen by the VPNMSISDN
     * subscriber. Return FALSE otherwise. the auxiliary service is of MOM type too.
     *
     * @param ctx
     *            the operating context
     * @param subscriber
     *            Subscriber object
     * @param selection
     *            AuxiliaryServiceSelection object
     * @return boolean value representing where account of the subscriber is eligible for
     *         showing VPN Auxiliary Service
     */
    protected boolean isMom(final Context ctx, final Subscriber subscriber, final AuxiliaryServiceSelection selection)
    {
        boolean result = false;
        try
        {
            Account account = null;
            Account rootAccount = null;
            
            if (subscriber.getBAN() != null
                    && subscriber.getBAN().trim().length() != 0)
            {
                account = AccountSupport.getAccount(ctx, subscriber.getBAN());
            }
            else
            {
                account = (Account) ctx.get(Account.class);
            }
            
            boolean isMom = account.isMom(ctx);
            if (!isMom)
            {
                rootAccount = account.getRootAccount(ctx);
                isMom = rootAccount.isMom(ctx);
            }
            
            // 2006-02-24: Changed to isMom (includes VPN and ICM), since both
            // VPN and ICM share the same functionality
            if (isMom)
            {
                final String vpnMsisdn = account.getVpnMSISDN();
                if (vpnMsisdn != null && vpnMsisdn.trim().length() > 0)
                {
                    Subscriber vpnSubscriber = SubscriberSupport.lookupSubscriberForMSISDN(ctx, vpnMsisdn);
                    
                    String msisdn = subscriber.getMSISDN();
                    if (msisdn.equals(vpnMsisdn))
                    {
                        result = true;
                    }
                    else
                    {
                        if (rootAccount == null)
                        {
                            rootAccount = account.getRootAccount(ctx);
                        }
                        if (msisdn.equals(rootAccount.getVpnMSISDN()))
                        {
                            result = true;
                        }
                    }
                    
                    if (!result)
                    {
                        SubscriberAuxiliaryService subAux = null;
                        if (vpnSubscriber != null)
                        {
                            subAux = SubscriberAuxiliaryServiceSupport.getSubscriberAuxiliaryServicesBySubIdAndSvcId(ctx,
                                vpnSubscriber.getId(), selection.getSelectionIdentifier());
                        }

                        if (subAux != null)
                        {
                            result = true;
                        }
                    }
                }
                else
                {
                    result = true;
                }
            }
        }
        catch (final HomeException e)
        {
            new MajorLogMsg(this, "Error determining whether or not subscriber " + subscriber.getId() + " is a MOM subscriber.", e).log(ctx);
            result = false;
        }
        return result;
    }


    /**
     * Creates an AuxiliaryServiceSelection corresponding to the given AuxiliaryService.
     *
     * @param ctx
     *            the operating context
     * @param service
     *            The AuxiliaryService to adapt.
     * @return An AuxiliaryServiceSelection corresponding to the given AuxiliaryService.
     */
    protected AuxiliaryServiceSelection adapt(final Context ctx, final AuxiliaryService service)
    {
        final AuxiliaryServiceSelection selection = new AuxiliaryServiceSelection();
        selection.setSelectionIdentifier(service.getIdentifier());
        selection.setName(service.getName());
        selection.setChargingModeType(service.getChargingModeType());
        selection.setRecurrenceInterval(service.getRecurrenceInterval());
        selection.setCharge(service.getCharge());
        selection.setType(service.getType());
        setStartAndEndDates(ctx, selection.getStartDate(), selection, service);
        return selection;
    }
    
    /**
     * Sets the start and end date to the screen subscriber auxiliary services
     *
     * @param context
     *            the operating context
     * @param runningDate
     *            the start date to be running
     * @param selection
     *            the bean to be set to
     * @param service
     *            the service to calulate the start and end date based on the one time
     *            charge
     */
    private Date getEndDate(final Context context,
        final Date startDate, final Date endDate, final com.redknee.app.crm.bean.core.AuxiliaryService service)
    {
    	Date resultEndDate = endDate!= null ? endDate:new Date();
    	
        if (ServicePeriodEnum.ONE_TIME.equals(service.getChargingModeType()))
        {
            if (service.getRecurrenceType() == OneTimeTypeEnum.ONE_OFF_FIXED_INTERVAL)
            {
                if (service.getFixedInterval() == FixedIntervalTypeEnum.DAYS)
                {
                	resultEndDate= CalendarSupportHelper.get(context).findDateDaysAfter(service.getValidity(), startDate);
                }
                else
                {
                	resultEndDate = CalendarSupportHelper.get(context).findDateMonthsAfter(service.getValidity(), startDate);
                	// Why is end date not set as last second of previous day (for fixed interval)
                	// Temporary fix has been done for correct end date at transaction record in com.redknee.app.crm.service.OneTimePeriodHandler
                	//  for TT#13041756009. Please remove that if endDate is fixed in this class
                }
            }
        }
        return resultEndDate;
    }


    /**
     * Sets the start and end date to the screen subscriber auxiliary services
     *
     * @param context
     *            the operating context
     * @param runningDate
     *            the start date to be running
     * @param selection
     *            the bean to be set to
     * @param service
     *            the service to calulate the start and end date based on the one time
     *            charge
     */
    private void setStartAndEndDates(final Context context, final Date runningDate,
        final AuxiliaryServiceSelection selection, final AuxiliaryService service)
    {
        if (ServicePeriodEnum.ONE_TIME.equals(service.getChargingModeType()))
        {
            if (service.getRecurrenceType() == OneTimeTypeEnum.ONE_OFF_FIXED_DATE_RANGE)
            {
                final Date today = new Date();
                selection.setStartDate(service.getStartDate());
                if (service.getStartDate() == null || service.getStartDate().before(today))
                {
                    selection.setStartDate(today);
                }
                selection.setEndDate(service.getEndDate());
            }
            else
            {
                selection.setStartDate(runningDate);

                if (service.getFixedInterval() == FixedIntervalTypeEnum.DAYS)
                {
                    selection.setEndDate(CalendarSupportHelper.get(context).findDateDaysAfter(service.getValidity(), runningDate));
                }
                else
                {
                    selection.setEndDate(CalendarSupportHelper.get(context).findDateMonthsAfter(service.getValidity(), runningDate));

                }
            }
        }
    }


    /**
     * This method returns the bean representing the extra fields of the aux service.
     *
     * @param ctx
     *            The operating context.
     * @param selection
     *            Auxiliary service selection.
     * @param sub
     *            Subscriber being processed..
     * @param service
     *            Auxiliary service.
     * @param associationId
     *            Identifier of the subscriber-auxiliary service association.
     * @param isPresent
     *            Whether the extra fields currently exist.
     * @return ExtraFieldsPresent The bean representing the extra fields.
     */
    protected ExtraFieldsPresent getExtraFields(final Context ctx, final AuxiliaryServiceSelection selection,
        final Subscriber sub, final AuxiliaryService service, final SubscriberAuxiliaryService subAuxService, final boolean isPresent)
    {
        ExtraFieldsPresent bean = null;
        long associationId = -1;
        
        if (subAuxService != null)
        {
            associationId = subAuxService.getIdentifier();
        }

        switch (service.getType().getIndex())
        {
            case AuxiliaryServiceTypeEnum.AdditionalMsisdn_INDEX:
            {
                bean = getAdditionalMsisdnExtraFields(ctx, selection, sub, service, subAuxService, isPresent);
                break;
            }
            case AuxiliaryServiceTypeEnum.HomeZone_INDEX:
            {
                bean = getHomeZoneExtraFields(ctx, selection, service, associationId, isPresent);
                break;
            }

            case AuxiliaryServiceTypeEnum.Vpn_INDEX:
            {
                final Account account = (Account) ctx.get(Account.class);
                if (account.getVpnMSISDN() != null && account.getVpnMSISDN().trim().length() != 0)
                {
                    if (!account.getVpnMSISDN().equals(sub.getMSISDN()))
                    {
                        selection.setVpnMode(1);
                    }
                }
                bean = getVpnExtraFields(ctx, associationId, sub, selection, isPresent);
                break;
            }
            case AuxiliaryServiceTypeEnum.Voicemail_INDEX:
            {
                bean = getVoicemailExtraFields(ctx, sub, service);
                break;
            }
            default:
                // do nothing
        }

        return bean;
    }




    /**
     * Retrieves the extra fields related to a particular subscriber-additional MSISDN
     * auxiliary service.
     *
     * @param context
     *            The operating context.
     * @param selection
     *            The auxiliary service selection.
     * @param subscriber
     *            The subscriber selecting the auxiliary service.
     * @param service
     *            The auxiliary service being selected.
     * @param associationId
     *            Identifier of the subscriber-auxiliary service association.
     * @param infoExists
     *            Whether the information related to the extra fields already exists.
     * @return The bean representing the extra fields related to the selection.
     */
    private ExtraFieldsPresent getAdditionalMsisdnExtraFields(final Context context,
        final AuxiliaryServiceSelection selection, final Subscriber subscriber, final AuxiliaryService service,
        final SubscriberAuxiliaryService association, final boolean infoExists)
    {
        final AdditionalMsisdnBean bean = new AdditionalMsisdnBean();
        String bearerType = null;
        AddMsisdnAuxSvcExtension extension = ExtensionSupportHelper.get(context).getExtension(context , service, AddMsisdnAuxSvcExtension.class);
        if (extension!=null)
        {
            bearerType = extension.getBearerType();
        }
        else
        {
            LogSupport.minor(context, this,
                    "Unable to find required extension of type '" + AdditionalMsisdnBean.class.getSimpleName()
                            + "' for auxiliary service " + service.getIdentifier());
        }
        bean.setBearerType(bearerType);
        
        if (infoExists && association!=null)
        {
            Msisdn msisdn = null;
            if (association.getIdentifier() > 0)
            {
                try
                {
                    msisdn = AdditionalMsisdnAuxiliaryServiceSupport.getAMsisdn(context, association);
                }
                catch (final HomeException e)
                {
                    LogSupport.debug(context, this, "Unable to retreive MSISDN associated with Additional MSISDN service "
                        + service.getIdentifier(), e);
                }
            }
            try
            {
                if (msisdn == null && SubscriberAuxiliaryServiceSupport.getSubscriberAuxiliaryServicesBySubIdAndSvcId(context,
                        association.getSubscriberIdentifier(), association.getAuxiliaryServiceIdentifier()) != null)
                {
                    msisdn = AdditionalMsisdnAuxiliaryServiceSupport.getAMsisdn(context, association
                        .getAuxiliaryServiceIdentifier(), association.getSubscriberIdentifier());
                }
            }
            catch (final HomeException e)
            {
                LogSupport.debug(context, this, "Unable to retreive MSISDN associated with Additional MSISDN service "
                    + service.getIdentifier(), e);
            }
            
            bean.setAMsisdn(association.getAMsisdn(context));
            bean.setAMsisdnEntryType(association.getAMsisdnEntryType());
            bean.setBAN(subscriber.getBAN());
            bean.setSubscriber(subscriber);
            
            if (msisdn != null && msisdn.isAMsisdn())
            {
                bean.setOriginalMsisdn(msisdn.getMsisdn());
                if (bean.getOriginalMsisdn().equals(bean.getAMsisdn()))
                {
                    bean.setAMsisdnGroup(msisdn.getGroup());
                }
            }
            else
            {
                bean.setOriginalMsisdn(bean.getAMsisdn());
            }
            
            if (bean.getAMsisdnGroup()==AdditionalMsisdnBean.DEFAULT_AMSISDNGROUP)
            {
                bean.setAMsisdnGroup(association.getAMsisdnGroup(context));
            }
            
        }
        return bean;
    }





    /**
     * Retrieves the extra fields related to a particular subscriber-VPN auxiliary service
     * association. object
     *
     * @param ctx
     *            The operating context.
     * @param subAuxSvcId
     *            Identifier of the subscriber-auxiliary service association.
     * @param sub
     *            Subscriber owner of the assocaition.
     * @param selection
     *            Auxiliary service selection.
     * @param isPresent
     *            Whether the extra fields bean already exists.
     * @return The bean representing the extra fields associated with the particular VPN
     *         auxiliary service selection.
     */
    private ExtraFieldsPresent getVpnExtraFields(final Context ctx, final long subAuxSvcId, final Subscriber sub,
        final AuxiliaryServiceSelection selection, final boolean isPresent)
    {
        final VpnBean vpn = new VpnBean();

        Account account = (Account) ctx.get(Account.class);
        try
        {
            if (account.getBAN().length() != 0)
            {
                account = account.getRootAccount(ctx);
            }
        }
        catch (final HomeException exception)
        {
            new DebugLogMsg(this, "exception when trying to fill VpnInfo" + " in auxiliaryservices for subscriber",
                exception).log(ctx);
        }
        if (isPresent)
        {
            vpn.setVpnMsisdn(account.getVpnMSISDN());
        }
        else
        {
            vpn.setVpnMsisdn("");
        }

        return vpn;
    }


    /**
     * Fills up the voice mail information
     *
     * @param ctx
     *            The operating context.
     * @param sub
     *            Subscriber being filled.
     * @param service
     *            Auxiliary serivce.
     * @return Extra fields of the voice mail auxiliary service.
     */
    private VoicemailFieldsBean getVoicemailExtraFields(final Context ctx, final Subscriber sub,
        final AuxiliaryService service)
    {
        VoicemailFieldsBean vmBean = null;
        if (isVoicemailProvisioned(ctx, sub.getId(), service))
        {
            if (sub.getMSISDN() != null && sub.getMSISDN().length() > 0)
            {
                vmBean = new VoicemailFieldsBean();
                vmBean.setPassword(sub.getMSISDN());
            }
        }
        return vmBean;
    }


    /**
     * Method to check whether Voice Mail service is provisioned
     *
     * @param ctx
     *            context object
     * @param subId
     *            Subscriber ID.
     * @param service
     *            Auxiliary service ID.
     * @return Returns <code>true</code> if voicemail is provisioned, <code>false</code>
     *         otherwise.
     */
    private boolean isVoicemailProvisioned(final Context ctx, final String subId, final AuxiliaryService service)
    {
        Object association = null;
        if (subId != null && subId.length() > 0)
        {
            association = SubscriberAuxiliaryServiceSupport.getSubscriberAuxiliaryServicesBySubIdAndSvcId(ctx, subId,
                service.getIdentifier());
        }
        return association != null;

    }


    /**
     * Retrieves a copy of the bean representing the extra fields associated with a home
     * zone auxiliary service.
     *
     * @param ctx
     *            The operating context.
     * @param selection
     *            The auxiliary service selection.
     * @param service
     *            The home zone auxiliary service.
     * @param subAuxSvcId
     *            Identifier of the subscriber-auxiliary service association.
     * @param isPresent
     *            Whether the extra fields bean already exist.
     * @return The bean representing the extra fields associated with the home zone
     *         auxiliary service selection.
     */
    private HomeZoneFieldsBean getHomeZoneExtraFields(final Context ctx, final AuxiliaryServiceSelection selection,
        final AuxiliaryService service, final long subAuxSvcId, final boolean isPresent)
    {
        final HomeZoneAuxSvcExtension hzBean = getHomeZoneByAuxSvcID(ctx, service.getIdentifier());
        final HomeZoneFieldsBean hzFieldsBean = new HomeZoneFieldsBean();
        hzFieldsBean.setSelectionIdentifier(service.getIdentifier());
        hzFieldsBean.setSpid(service.getSpid());
        if (!isPresent)
        {
            if (hzBean != null)
            {
                // as service is not provisioned before for this subscriber,
                // display default values for x and y
                setHzBeanForDefaultValues(hzFieldsBean, hzBean);
            }
        }
        else
        {
            final SubscriberHomezone subZoneBean = getSubHomeZoneBysubAuxSvcID(ctx, subAuxSvcId);
            if (subZoneBean != null)
            {
                hzFieldsBean.setHzX(subZoneBean.getHzX());
                hzFieldsBean.setHzY(subZoneBean.getHzY());
                hzFieldsBean.setHzCellID(subZoneBean.getHzCellID());
                hzFieldsBean.setHzPriority(subZoneBean.getHzPriority());
                hzFieldsBean.setHzRadius(hzBean.getRadius());
                hzFieldsBean.setHzDiscount(hzBean.getDiscount());
            }
            else
            {
                // Some error has occurred while creating homezone auxiliary service
                // fill up bean with default data again
                if (hzBean != null)
                {
                    setHzBeanForDefaultValues(hzFieldsBean, hzBean);
                }
                selection.setChecked(false);
            }
        }
        return hzFieldsBean;
    }


    /**
     * Sets default values for any aux. service if it is a homezone
     *
     * @param hzFieldsBean
     *            the bean to adapt
     * @param hzBean
     *            the adapted bean
     */
    private void setHzBeanForDefaultValues(final HomeZoneFieldsBean hzFieldsBean, final HomeZoneAuxSvcExtension hzBean)
    {
        hzFieldsBean.setHzX(hzBean.getHzX());
        hzFieldsBean.setHzY(hzBean.getHzY());
        hzFieldsBean.setHzCellID(hzBean.getHzCellID());
        hzFieldsBean.setHzRadius(hzBean.getRadius());
        hzFieldsBean.setHzDiscount(hzBean.getDiscount());
    }


    /**
     * using the auxSvcId get the corresponding homezone(both homezone and aux. service
     * have the same Id)
     *
     * @param ctx
     *            the operating context
     * @param auxSvcId
     *            the service id
     * @return the home zone
     */
    private HomeZoneAuxSvcExtension getHomeZoneByAuxSvcID(final Context ctx, final long auxSvcId)
    {
        try
        {
            HomeZoneAuxSvcExtension hz = HomeSupportHelper.get(ctx).findBean(ctx, HomeZoneAuxSvcExtension.class, Long.valueOf(auxSvcId));
            return hz;
        }
        catch (final HomeException e)
        {
            new MinorLogMsg(this, "Error retrieving the corresponding homezone for Auxiliary Service ID:"
                + auxSvcId, e).log(ctx);
        }
        return null;
    }


    /**
     * using the subAuxSvcId to get the corresponding SubHomezone(both subHomezone and
     * SubscriberAuxService have the same Id)
     *
     * @param ctx
     *            the operating context
     * @param subAuxSvcId
     *            the service id
     * @return the subscriber home zone
     */
    private SubscriberHomezone getSubHomeZoneBysubAuxSvcID(final Context ctx, final long subAuxSvcId)
    {
        try
        {
            SubscriberHomezone subHz = HomeSupportHelper.get(ctx).findBean(ctx, SubscriberHomezone.class, Long.valueOf(subAuxSvcId));
            return subHz;
        }
        catch (final HomeException e)
        {
            new MinorLogMsg(this,
                "Error retrieving the corresponding subscriberhomezone for SubAuxServiceID:"
                    + subAuxSvcId, e).log(ctx);
        }
        return null;
    }


    /**
     * Sets the extra fields according to the type of the service, only applicable for
     * services which HAVE extra fields associated with them currently used for homezone
     *
     * @param context
     *            The operating context.
     * @param extraFieldBean
     *            The extra fields of this auxiliary service selection bean.
     * @param service
     *            The subscriber auxiliary service association.
     */
    private void setHomezoneExtraFields(final Context context, final Object extraFieldBean,
        final SubscriberAuxiliaryService service)
    {
        final HomeZoneFieldsBean hzBean = (HomeZoneFieldsBean) extraFieldBean;
        service.setHzPriority(hzBean.getHzPriority());
        service.setHzCellID(hzBean.getHzCellID());
        service.setHzX(hzBean.getHzX());
        service.setHzY(hzBean.getHzY());
        service.setHzRadius(hzBean.getHzRadius());
        service.setHzDiscount(hzBean.getHzDiscount());
        service.setType(AuxiliaryServiceTypeEnum.HomeZone);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Object fromWeb(final Context context, final ServletRequest request, final String name)
    {
        // The web control will only return to us a collection of the
        // AuxiliaryServiceSelections that are checked on the subscriber
        // profile. From that list, we need to derive a list of
        // SubscriberAuxiliaryServices to return to the subscriber.
        //
        // From above list, we need to derive three lists of
        // SubscriberAuxiliaryServices
        // 1. currently active ( waiting-to-unprovisioned ) existing aux.svc
        // 2. currently inactive ( waiting-to-provisioned right away ) new
        // aux.svc
        // 3. currently inactive ( waiting-to-provisioned in the future ) new acx. svc
        //
        // list 1 needs to return to the subscriber and save in db later in
        // AuxSvcCreatingHome
        // list 2 needs to return to the subscriber and save in db later in
        // AuxSvcCreatingHome
        // list 3 needs to save to db later in AuxSvcCreatingHome in order to
        // provision by daily cron job.

        final List<AuxiliaryServiceSelection> selections = new ArrayList<AuxiliaryServiceSelection>();

        super.fromWeb(context, selections, request, name);
        final Subscriber subscriber = (Subscriber) context.get(AbstractWebControl.BEAN);

        final boolean isNewSub = (subscriber.getId() == null || subscriber.getId().length() == 0);

        final List<SubscriberAuxiliaryService> services = new ArrayList<SubscriberAuxiliaryService>(selections.size());
        final List<SubscriberAuxiliaryService> homzoneServices = new ArrayList<SubscriberAuxiliaryService>(selections.size());

        final HomezoneInfoValidator hzValidator = new HomezoneInfoValidator();

        boolean vmFlag = false;
        boolean vpnFlag = false;
        for (AuxiliaryServiceSelection selection : selections)
        {
            // if 2 voicemail services are chosen then no need to continue throw an
            // exception right away
            assignTypeToSelection(context, selection);

            if (selection.getType() == AuxiliaryServiceTypeEnum.Voicemail)
            {
                if (vmFlag)
                {
                    throw new IllegalStateException("You can not select more than one voicemail "
                        + "auxiliary services at a time, please select only one");
                }
                vmFlag = true;
            }
            else if (selection.getType() == AuxiliaryServiceTypeEnum.Vpn)
            {
                if (vpnFlag)
                {
                    throw new IllegalStateException("You can not select more than one vpn"
                        + " auxiliary services at a time, please select only one");
                }
                vpnFlag = true;
            }
            else if (selection.getType() == AuxiliaryServiceTypeEnum.AdditionalMsisdn)
            {
                // assign bearer type
                final AuxiliaryService auxiliaryService = getAuxiliaryService(context, selection
                    .getSelectionIdentifier());
                final AdditionalMsisdnBean bean = (AdditionalMsisdnBean) selection.getExtraFields();
                if (bean != null && auxiliaryService != null)
                {
                    String bearerType = null;
                    AddMsisdnAuxSvcExtension extension = ExtensionSupportHelper.get(context).getExtension(context , auxiliaryService, AddMsisdnAuxSvcExtension.class);
                    if (extension!=null)
                    {
                        bearerType = extension.getBearerType();
                    }
                    else
                    {
                        LogSupport.minor(context, this,
                                "Unable to find required extension of type '" + AdditionalMsisdnBean.class.getSimpleName()
                                        + "' for auxiliary service " + auxiliaryService.getIdentifier());
                    }
                    bean.setBearerType(bearerType);
                }
            }
            else if (selection.getType() == AuxiliaryServiceTypeEnum.MultiSIM)
            {
                // We do not show secondary Multi-SIM services in the rating tab.  Secondary services (i.e. individual SIMs)
                // are configured in the Multi-SIM extension.
                And filter = new And();
                filter.add(new EQ(SubscriberAuxiliaryServiceXInfo.SUBSCRIBER_IDENTIFIER, subscriber.getId()));
                filter.add(new EQ(SubscriberAuxiliaryServiceXInfo.AUXILIARY_SERVICE_IDENTIFIER, selection.getSelectionIdentifier()));
                filter.add(new NEQ(SubscriberAuxiliaryServiceXInfo.SECONDARY_IDENTIFIER, SubscriberAuxiliaryService.DEFAULT_SECONDARYIDENTIFIER));
                Collection<SubscriberAuxiliaryService> hiddenSimServices = new ArrayList<SubscriberAuxiliaryService>();
                try
                {
                    hiddenSimServices = HomeSupportHelper.get(context).getBeans(context, SubscriberAuxiliaryService.class, filter);
                }
                catch (HomeException e)
                {
                    throw new IllegalStateException("Not allowed to remove SIM specific auxiliary services.  This message is due to a processing error.  See your system administrator or try again later.");
                }
                services.addAll(hiddenSimServices);
            }

           	// the hidden and realy only web control are not properly implemented, as a result these information is lost.
        	// so the only way to query DB. the GUI object should keep all data. But FW doesn't support it, if we use customized 
            // webcontrol, then it could be too much work, which raise the doubt what benefit we really get by using FW. 
            SubscriberAuxiliaryService service = SubscriberAuxiliaryServiceSupport.getSubscriberAuxiliaryService(context, subscriber.getId(), 
            		selection.getSelectionIdentifier(), selection.getSecondaryId());
            
            
            if ( service != null )
            {
             	try
            	{
            		if( service.getAuxiliaryService(context).isPrivateCUG(context) || service.getType(context) == AuxiliaryServiceTypeEnum.MultiSIM)
            		{	
            	     	services.add(service);
            	     	continue; 
            		}	
            	} 	catch (HomeException e )
            	{
            		
            	}

            }       		

            service = new SubscriberAuxiliaryService();
            
            
            service.setContext(context);
            service.setIdentifier(-1);
            // service.setSubscriberIdentifier(subscriber.getId());
            if (isNewSub)
            {
                service.setSubscriberIdentifier("-1");

                // don't allow to select PLP/CUG aux. services while creating the subscriber
                // pcValidator.validate(context, selection);
            }
            else
            {
                service.setSubscriberIdentifier(subscriber.getId());
            }
            service.setAuxiliaryServiceIdentifier(selection.getSelectionIdentifier());
            service.setSecondaryIdentifier(selection.getSecondaryId());
            service.setIsfeePersonalizationApplied(selection.getIsfeePersonalizationApplied());
            if(selection.getIsfeePersonalizationApplied())
            {
            	service.setPersonalizedFee(selection.getPersonalizedFee());
            }
            
            
            
            final Date startDate = CalendarSupportHelper.get(context).getDateWithNoTimeOfDay(selection.getStartDate()!=null ? selection.getStartDate():new Date());
            
            service.setStartDate(startDate);
            
            // End Date calculated from the value coming from GUI
            Date endDate = getEndDate(context, startDate, selection.getEndDate(), selection.getAuxiliarService(context));
            final Date endDateFrmGui = CalendarSupportHelper.get(context).getDateWithNoTimeOfDay(endDate);
            service.setEndDate(endDateFrmGui);
            /*
             * Handling the case when the Payment number is becoming zero for CSR Login.
             */
            if (selection.getPaymentNum() <= 0)
            {
                final Collection<SctAuxiliaryService> sctColl =
                    SubscriberCreateTemplateSupport.getSctAuxiliaryServices(context, subscriber.getSatId());
                final int noOfPayments = getPaymentNumFromSct(context, sctColl, selection.getSelectionIdentifier());
                selection.setPaymentNum(noOfPayments);
            }

            service.setPaymentNum(selection.getPaymentNum());
            service.setType(selection.getType());

            final Context subContext = context.createSubContext();
            subContext.put("fromCronAgent", Boolean.FALSE);

            // get the extra fields bean if it is null check for its type:
            // whether those are homezone extra fields or VPN extra fields...

            ExtraFieldsPresent extraFieldBean = (ExtraFieldsPresent) selection.getExtraFields();
            if (extraFieldBean instanceof HomeZoneFieldsBean)
            {
                /*
                 * TODO [Cindy] 2007-09-17: Move validation to subscriber pipeline;
                 * otherwise homezone service added through API will not be validated.
                 */

                // setting the extra fields and validating them immediately
                setHomezoneExtraFields(subContext, extraFieldBean, service);
                hzValidator.validate(subContext, service);
                homzoneServices.add(service);
            }
            else if (extraFieldBean instanceof VpnBean)
            {
                setVpnExtraFields((VpnBean) extraFieldBean, service);
            }
            else if (extraFieldBean instanceof AdditionalMsisdnBean)
            {
                setAdditionalMsisdnExtraFields((AdditionalMsisdnBean) extraFieldBean, service);
            }

            /*
             * [Cindy] 2007-09-17: Future services are moved to SubscriberAuxiliaryServicePreparationHome.
             */
            services.add(service);
        }
        hzValidator.validatePriorities(context, homzoneServices, subscriber.getSpid());
        
        new DebugLogMsg(this, "Auxiliary Services for subscriber " + subscriber.getId() + " are " + services, null).log(context);
        
        return services;
    }


    /**
     * This method gets the Auxiliary service for the given Aux Service identifier
     *
     * @param ctx
     *            Context object
     * @param svcIdentifier
     *            Auxiliary Service Identifier
     * @return com.redkne.app.crm.bean.AuxiliaryService object
     */
    private AuxiliaryService getAuxiliaryService(final Context ctx, final long svcIdentifier)
    {
        AuxiliaryService auxSvcBean = null;
        try
        {
            auxSvcBean = AuxiliaryServiceSupport.getAuxiliaryService(ctx, Long.valueOf(svcIdentifier));
        }
        catch (final HomeException he)
        {
            new MinorLogMsg(this, "Error retrieving the auxiliary service with servcie id:" + svcIdentifier, null).log(ctx);
        }
        return auxSvcBean;
    }


    /**
     * Sets the optional values of an additional MSISDN association base on the extra
     * fields.
     *
     * @param bean
     *            The bean containing the extra fields to be set.
     * @param association
     *            The association to be set.
     */
    private void setAdditionalMsisdnExtraFields(final AdditionalMsisdnBean bean,
        final SubscriberAuxiliaryService association)
    {
        association.setAMsisdn(bean.getAMsisdn());
        association.setAMsisdnGroup(bean.getAMsisdnGroup());
        association.setBearerType(bean.getBearerType());
        association.setAMsisdnEntryType(bean.getAMsisdnEntryType());
    }


    /**
     * Set the extra fields on the subscriber auxiliary service if the auxiliary service is VPN.
     *
     * @param vpn
     *            the bean to set such fields
     * @param service
     *            the subscriber auxiliary service
     */
    private void setVpnExtraFields(final VpnBean vpn, final SubscriberAuxiliaryService service)
    {
        // nothing to do currently. Leaving in place to be reused if short code selection ever happens
    }


    
    
    /**
     * Assigns the Auxiliary Service type to the AuxiliaryServiceSelection object
     *
     * @param ctx
     *            Context
     * @param selection
     *            AuxiliaryServiceSelection object.
     */
    private void assignTypeToSelection(final Context ctx, final AuxiliaryServiceSelection selection)
    {
        if (selection != null)
        {
            final AuxiliaryService auxSvcBean = getAuxiliaryService(ctx, selection.getSelectionIdentifier());
            if (auxSvcBean != null)
            {
                selection.setType(auxSvcBean.getType());
            }
        }
    }


    /**
     * Manda - This method returns the No of payments field from the selected SCT
     *
     * @param ctx
     *            Context Object
     * @param coll
     *            Collection of SCT Auxiliary service objects.
     * @param auxServiceIdentifier
     *            Auxiliary Service Identifer
     * @return int the number of payments
     */
    private int getPaymentNumFromSct(final Context ctx, final Collection<SctAuxiliaryService> coll,
            final long auxServiceIdentifier)
    {
        int paymentNo = 0;
        if (coll != null)
        {
            for (SctAuxiliaryService sctAux : coll)
            {
                if (sctAux.getAuxiliaryServiceIdentifier() == auxServiceIdentifier)
                {
                    paymentNo = sctAux.getPaymentNum();
                    break;
                }
            }
        }
        return paymentNo;
    }
    
    /**
     * Manda - this method returns the SCTAuxiliaryService objects that are associated
     * with the current Service Activation Template Id selected on the Subscriber profile.
     *
     * @param ctx
     *            Context object
     * @param satId
     *            ServiceActivationTemplate Id, selected on Subscriber profile UI
     * @return Collection of SctAuxiliaryServices
     * @throws com.redknee.framework.xhome.home.HomeException
     *             exception thrown while finding these objects
     */
    private Collection<SctAuxiliaryService> getSctAuxiliaryServicesForSat(final Context ctx, final Long satId)
        throws HomeException
    {
        final Collection<SctAuxiliaryService> services = 
            HomeSupportHelper.get(ctx).getBeans(
                    ctx, SctAuxiliaryService.class, 
                    new EQ(SctAuxiliaryServiceXInfo.SCT_IDENTIFIER, satId));
        
        return services;
    }

    protected ExtraFieldsPresent getExtraFields(final Context context, final AuxiliaryServiceSelection selection, 
            final Subscriber subscriber, final AuxiliaryService service)
    {
        return getExtraFields(context, selection, subscriber, service, null, false);
    }


    /**
     * Returns TRUE if the account is a MOM account (VPN or ICM) and 1) if the subscriber
     * is the VPN Msisdn, or 2) if there are auxiliary services chosen by the VPNMSISDN
     * subscriber. Return FALSE otherwise. the auxiliary service is of MOM type too.
     *
     * @param ctx
     *            the operating context
     * @param subscriber
     *            Subscriber object
     * @param selection
     *            AuxiliaryServiceSelection object
     * @return boolean value representing where account of the subscriber is eligible for
     *         showing VPN Auxiliary Service
     */
    protected boolean isMom2(final Context ctx, final Subscriber subscriber, final AuxiliaryServiceSelection selection)
    {
        Account account = null;
        Account rootAccount = null;
        try
        {
            if (subscriber.getBAN().length() != 0)
            {
                account = AccountSupport.getAccount(ctx, subscriber.getBAN());
                rootAccount = account.getRootAccount(ctx);
            }
            else
            {
                account = (Account) ctx.get(Account.class);
                rootAccount = account.getRootAccount(ctx);
            }
            // 2006-02-24: Changed to isMom (includes VPN and ICM), since both
            // VPN and ICM share the same functionality

            if (account.isMom(ctx) || rootAccount.isMom(ctx))
            {
                Subscriber vpnSubscriber = null;
                final String vpnMsisdn = account.getVpnMSISDN();

                if (vpnMsisdn != null && vpnMsisdn.trim().length() > 0)
                {
                    vpnSubscriber = SubscriberSupport.lookupSubscriberForMSISDN(ctx, vpnMsisdn);
                }
                else
                {
                    return true;
                }
                if (subscriber.getMSISDN().equals(vpnMsisdn))
                {
                    return true;
                }
                if (subscriber.getMSISDN().equals(rootAccount.getVpnMSISDN()))
                {
                    return true;
                }

                SubscriberAuxiliaryService subAux = null;
                if (vpnSubscriber != null)
                {
                    subAux = SubscriberAuxiliaryServiceSupport.getSubscriberAuxiliaryServicesBySubIdAndSvcId(ctx,
                        vpnSubscriber.getId(), selection.getSelectionIdentifier());
                }

                if (subAux != null)
                {
                    return true;
                }
            }
        }
        catch (final Exception e)
        {
            return false;
        }
        return false;
    }


} // class
