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
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletRequest;

import com.trilogy.app.crm.bean.*;
import com.trilogy.app.crm.CommonFramework;
import com.trilogy.app.crm.CommonTime;
import com.trilogy.app.crm.bean.FixedIntervalTypeEnum;
import com.trilogy.app.crm.bean.OneTimeTypeEnum;
import com.trilogy.app.crm.bean.Service;
import com.trilogy.app.crm.bean.ServiceFee2ID;
import com.trilogy.app.crm.bean.ServicePeriodEnum;
import com.trilogy.app.crm.bean.ServicePreferenceEnum;
import com.trilogy.app.crm.bean.ServiceProvisionStatusEnum;
import com.trilogy.app.crm.bean.ServiceTypeEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberServiceDisplay;
import com.trilogy.app.crm.bean.SubscriberServiceDisplayTableWebControl;
import com.trilogy.app.crm.bean.SubscriberServiceDisplayXInfo;
import com.trilogy.app.crm.bean.SubscriberServices;
import com.trilogy.app.crm.bean.VoicemailFieldsBean;
import com.trilogy.app.crm.bean.core.ServiceFee2;
import com.trilogy.app.crm.bean.service.ServiceStateEnum;
import com.trilogy.app.crm.provision.xgen.SubscriberService;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.app.crm.support.ServiceSupport;
import com.trilogy.app.crm.support.SubscriberServicesSupport;
import com.trilogy.app.crm.util.SubscriberServicesUtil;
import com.trilogy.app.crm.web.renderer.GroupByTableRenderer;
import com.trilogy.framework.auth.AuthMgr;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.web.action.ActionMgr;
import com.trilogy.framework.xhome.web.renderer.TableRenderer;
import com.trilogy.framework.xhome.webcontrol.AbstractWebControl;
import com.trilogy.framework.xhome.webcontrol.ViewModeEnum;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;


/**
 * Support inner class thar displays the services of the subscriber.
 * @author rajith.attapattu@redknee.com
 */
public class DisplaySubscriberServicesWebControl extends SubscriberServiceDisplayTableWebControl
{

    /**
     * {@inheritDoc}
     */
    @Override
    public void toWeb(final Context ctx,
            final PrintWriter out,
            final String name,
            final Object obj)
    {
        final Context subCtx = ctx.createSubContext();

        final Subscriber sub = (Subscriber) subCtx.get(AbstractWebControl.BEAN);

        final List beanList = new ArrayList();
        final Map<ServiceFee2ID, SubscriberServices> services = SubscriberServicesSupport.getAllSubscribersServicesRecords(subCtx, sub.getId(),true);

        final Map<ServiceFee2ID, ServiceFee2> serviceFees = SubscriberServicesSupport.getServiceFeesWithSource(subCtx, sub, true);

        for (final Iterator iter = serviceFees.entrySet().iterator(); iter.hasNext();)
        {
            final Map.Entry entry = (Map.Entry) iter.next();
            ServiceFee2ID sobj = (ServiceFee2ID)entry.getKey();
//            final Long srvID = (Long) sobj.getServiceId();
            final ServiceFee2 fee = (ServiceFee2) entry.getValue();
            try
            {
                final SubscriberServiceDisplay bean = adapt(subCtx, sub, sobj, services, fee);
                if (bean != null)
                {
                    beanList.add(bean);
                }
            }
            catch (final Exception e)
            {
                new DebugLogMsg(this, "Unable to adapt ", e).log(subCtx);
            }
        }

        Collections.sort(beanList, new Comparator()
        {
            public int compare(final Object o1, final Object o2)
            {
                final SubscriberServiceDisplay f1 = (SubscriberServiceDisplay) o1;
                final SubscriberServiceDisplay f2 = (SubscriberServiceDisplay) o2;

                int result = f1.getSource().compareTo(f2.getSource());
                if (result == 0)
                {
                    // if the source is the same sort by ID
                    result = (int) Math.signum(f1.getServiceId() - f2.getServiceId());
                }

                return result;
            }
        });

        subCtx.put(NUM_OF_BLANKS, -1);
        disableActions(subCtx);
        setPropertyReadOnly(subCtx, "SubscriberServiceDisplay.serviceId");
        setPropertyReadOnly(subCtx, "SubscriberServiceDisplay.fee");
        setPropertyReadOnly(subCtx, "SubscriberServiceDisplay.servicePeriod");
        setPropertyReadOnly(subCtx, "SubscriberServiceDisplay.cltcDisabled");
        setPropertyReadOnly(subCtx, "SubscriberServiceDisplay.nextRecurringChargeDate");

        /*
         * This was copied from DefaultServiceEditorWebControl
         *
         *  // MAALP: 05/03/04 - TT 402262116 // create a subcontext which
         * overrides DISPLAY_MODE // in order to make ServiceFee2tableWebControl //
         * call outputCheckBox method // and saves the real mode in order to
         * properly display check box
         */
        final int mode = subCtx.getInt(MODE, DISPLAY_MODE);
        subCtx.put(CommonFramework.REAL_MODE, mode);
        if (mode == DISPLAY_MODE)
        {
            subCtx.put(MODE, EDIT_MODE);
        }

        super.toWeb(subCtx, out, name, beanList);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object fromWeb(final Context ctx, final ServletRequest req, final String name)
    {
        final Set set = new HashSet();
        final List list = new ArrayList();
        final Set<ServiceFee2ID> serviceFee2IDList = new HashSet();
        final Subscriber sub = (Subscriber) ctx.get(AbstractWebControl.BEAN);

        super.fromWeb(ctx, list, req, name);

        final Map serviceFeesMap = SubscriberServicesSupport.getServiceFees(ctx, sub);
        final Map selectedServices = SubscriberServicesSupport.getSubscribersServices(ctx, sub.getId());

        final Date today = CalendarSupportHelper.get(ctx).getDateWithNoTimeOfDay(new Date());

        for (final Iterator i = list.iterator(); i.hasNext();)
        {
            final SubscriberServiceDisplay fee = (SubscriberServiceDisplay) i.next();
            /*
             * hidden fields are not persistent across toWeb & fromWeb methods
             * therefore I have to querry again to figure out whether it's a
             * mandatory field or not
             */
            // bean.setMandatory(fee.getMandatory());
            final Long key = Long.valueOf(fee.getServiceId());
            // the key should be there for sure, but just in case
            final ServiceFee2 fees = (ServiceFee2) serviceFeesMap.get(new ServiceFee2ID(fee.getServiceId(), fee.getPath()));

            if (((!fees.getServicePreference().equals(ServicePreferenceEnum.MANDATORY)) && (!hasAccessRights(ctx, fee))))
            {
                continue;
            }
            	
            final SubscriberServices bean = new SubscriberServices();
            bean.setSubscriberId(sub.getId());
            bean.setServiceId(fee.getServiceId());
            bean.setStartDate(CalendarSupportHelper.get(ctx).getDateWithNoTimeOfDay(fee.getStartDate()));
            bean.setEndDate(CalendarSupportHelper.get(ctx).getDateWithNoTimeOfDay(fee.getEndDate()));
            bean.setNextRecurringChargeDate(fee.getNextRecurringChargeDate());
            bean.setIsfeePersonalizationApplied(fee.getIsfeePersonalizationApplied());
            bean.setPath(fee.getPath());
            
            if(fee.getIsfeePersonalizationApplied())
            {
            	bean.setPersonalizedFee(fee.getPersonalizedFee());
            }
            
            SubscriberServices   subservice =  null;
            
            if(bean != null && bean.getServiceId() > 0)
            {
               subservice = (SubscriberServices) selectedServices.get(new ServiceFee2ID(bean.getServiceId(), SubscriberServicesUtil.DEFAULT_PATH));
            }
            
            if(subservice != null)
            {
            	bean.setProvisionAction(subservice.getProvisionAction());
            }

            if (fees != null)
            {
                if (fees.getServicePreference().equals(ServicePreferenceEnum.MANDATORY))
                {
                    bean.setMandatory(true);
                }
                else
                {
                    bean.setMandatory(false);
                }
                
                // Service Period is a redundant field in SubscriberServices table 
                // to facilitate weekly recurring charge
                bean.setServicePeriod(fees.getServicePeriod());
            }
            final Long serviceID = Long.valueOf(fee.getServiceId());
            final String path = fee.getPath();
            final SubscriberServices subService = (SubscriberServices) selectedServices.get(new ServiceFee2ID(fee.getServiceId(), SubscriberServicesUtil.DEFAULT_PATH));
            
            if( subService != null)
            {
                bean.setProvisionedState(subService.getProvisionedState());
                // check if the service quantity changed and if changed then set the diff in the changedServiceQntatity
                if(fee.getServiceQuantity() != subService.getServiceQuantity())
                	bean.setChangedServiceQuantity(Math.abs(fee.getServiceQuantity()-subService.getServiceQuantity()));
            }
            else if (bean.getMandatory())
            {              
                bean.setProvisionedState(ServiceStateEnum.PROVISIONED);
            }
            else
            {
                if (!bean.getStartDate().after(today))
                {
                    // This will make sure the optional service starting today
                    // is provisoned immediately
                    bean.setProvisionedState(ServiceStateEnum.PROVISIONED);
                }
            }
            set.add(bean);
            serviceFee2IDList.add(new ServiceFee2ID(serviceID, path));
            bean.setServiceQuantity(fee.getServiceQuantity());
        }
        // this function is very important.
        syncServicesField(sub, serviceFee2IDList);

        return set;
    }

    /**
     * Synchronizes the service field.
     * @param sub the subscriber to synchronize
     * @param serviceIdList the list of service to synchronize
     */
    private void syncServicesField(final Subscriber sub, final Set<ServiceFee2ID> serviceFee2IDSet)
    {
        /*
         * this is done so that the existing getServices method will work properly
         * in the code this was done after talking with joe and disscussiing for a
         * while.
         * The fact that we sync and save the service id's within the subscriber
         * table helps us later on in SubscriberServicesSaveHome It relies on
         * the sub.getServices() to work properly
         */
        sub.setServices(serviceFee2IDSet);
    }

    /**
     * Disables the actions from the services
     * @param ctx the operating context
     */
    private void disableActions(final Context ctx)
    {
        ActionMgr.disableActions(ctx);
    }

    /**
     * Adapts the Service Fee to the services to be displayed
     * @param ctx the operating context
     * @param sub the subscriber on the screen
     * @param selectedServices the subscriber services currently has
     * @param fee the subscriber fee for the service
     * @return the service to be displayed on the screen
     * @throws HomeException in case something wrong happens on the system
     */
    protected SubscriberServiceDisplay adapt(final Context ctx,
            final Subscriber sub,
            final ServiceFee2ID serviceFee2ID,
            final Map selectedServices,
            final ServiceFee2 fee)
        throws HomeException
    {
        final SubscriberServiceDisplay bean = new SubscriberServiceDisplay();
        bean.setServiceId(fee.getServiceId());
        bean.setFee(fee.getFee());
        bean.setPath(fee.getPath());
        bean.setServicePeriod(fee.getServicePeriod());
        bean.setRecurrenceInterval(fee.getRecurrenceInterval());
        bean.setChargeFailureAction(fee.getChargeFailureAction());
        if (fee.getServicePreference().equals(ServicePreferenceEnum.MANDATORY))
        {
            bean.setMandatory(true);
        }
        else
        {
            bean.setMandatory(false);
        }

        bean.setSource(fee.getSource());

        final SubscriberServices subService = (SubscriberServices) selectedServices.get(serviceFee2ID);
        Service service;
        if (subService != null)
        {
            // previously picked service
            bean.setStartDate(subService.getStartDate());
            bean.setEndDate(subService.getEndDate());
            bean.setIsfeePersonalizationApplied(subService.getIsfeePersonalizationApplied());
            bean.setPersonalizedFee(subService.getPersonalizedFee());
            //if (SubscriberServicesSupport.UNPROVISIONED_STATES.contains(subService.getProvisionedState()))
            if(ServiceStateEnum.UNPROVISIONED.equals(subService.getProvisionedState()))
            {
                bean.setChecked(false);
            }
            else
            {
                bean.setChecked(true);
            }
            bean.setServiceStatus(sub.getServiceStatus(ctx, subService));
            // fill in Subscriber services display extra fields
            getBeanBytype(ctx, bean, sub, subService);
            service = subService.getService(ctx);
            
            bean.setNextRecurringChargeDate(subService.getNextRecurringChargeDate());
            bean.setServiceQuantity(subService.getServiceQuantity());
        }
        else if (sub.getServices(ctx).contains(new ServiceFee2ID(fee.getServiceId(), fee.getPath())))
        {
            bean.setChecked(true);
            bean.setEndDate(CalendarSupportHelper.get(ctx).findDateYearsAfter(CommonTime.YEARS_IN_FUTURE, bean.getStartDate()));
            for (SubscriberServices intentService : (Set<SubscriberServices>) sub.getIntentToProvisionServices(ctx))
            {
                if (intentService.getServiceId() == serviceFee2ID.getServiceId())
                {
                    bean.setStartDate(intentService.getStartDate());
                    bean.setEndDate(intentService.getEndDate());
                    bean.setNextRecurringChargeDate(intentService.getNextRecurringChargeDate());
                    bean.setServiceQuantity(intentService.getServiceQuantity());
                    bean.setIsfeePersonalizationApplied(intentService.getIsfeePersonalizationApplied());
                    bean.setPersonalizedFee(intentService.getPersonalizedFee());
                    break;
                }
            }
            service = ServiceSupport.getService(ctx,serviceFee2ID.getServiceId());
        }
        else
        {
            bean.setEndDate(CalendarSupportHelper.get(ctx).findDateYearsAfter(CommonTime.YEARS_IN_FUTURE, bean.getStartDate())); 
            service = ServiceSupport.getService(ctx, serviceFee2ID.getServiceId());            
        }
        

        /*
         * whatever it is, the end date for mandatory services should be null.
         * since the date is stored as long and null cannot be saved to the
         * database. therefore override it with null just before display. Even
         * if there is a value in database it's not used in the code for
         * anything.
         *
         * madatory services are selected by the mandatory flag not by the start
         * date for provisioning purposes.
         */
        if (fee.getServicePreference().equals(ServicePreferenceEnum.MANDATORY))
        {
            bean.setEndDate(null);
        }
        else
        {
            // During a priceplan change if mandatory service is changed to
            // optional
            setOneTimeStartAndEndDate(ctx, bean, fee,service, subService);
        }

       
        return bean;
    }

    /**
     * If the subscriber service is one time it sets the start and end date
     * based on the configured one time configurations
     *
     * @param ctx
     *            the operating context
     * @param bean
     *            the bean to set start and end dates
     * @param fee
     *            the subscriber service for this subscriber
     * @throws HomeException
     *             if something is wrong with the application and can't find the
     *             service
     */
    private void setOneTimeStartAndEndDate(final Context ctx,
            final SubscriberServiceDisplay bean,
            final ServiceFee2 fee,
            final Service service,
            final SubscriberServices subService) throws HomeException
    {

        if (service.getChargeScheme() == ServicePeriodEnum.ONE_TIME)
        {
            if (service.getRecurrenceType() == OneTimeTypeEnum.ONE_OFF_FIXED_DATE_RANGE)
            {
                final Date today = new Date();
                if (subService != null)
                {
                    bean.setStartDate(subService.getStartDate());
                }
                else
                {
                    bean.setStartDate(service.getStartDate());
                    if (service.getStartDate().before(today))
                    {
                        bean.setStartDate(today);
                    }
                }

                bean.setEndDate(service.getEndDate());
            }
            else
            {
                if (service.getFixedInterval() == FixedIntervalTypeEnum.DAYS)
                {
                    bean.setEndDate(CalendarSupportHelper.get(ctx).findDateDaysAfter(service.getValidity(), bean.getStartDate()));
                }
                else
                {
                    bean.setEndDate(CalendarSupportHelper.get(ctx).findDateMonthsAfter(service.getValidity(), bean.getStartDate()));
                }

            }
        }
        else if (bean.getStartDate().equals(bean.getEndDate()))
        {
            // During a priceplan change if mandatory service is changed to
            // optional
            // it will display the same day for both start and end date.
            // this is done to avoid that.
            bean.setEndDate(CalendarSupportHelper.get(ctx).findDateYearsAfter(CommonTime.YEARS_IN_FUTURE, bean.getStartDate()));
        }

    }


    /**
     * Set a particular property to read only
     * @param ctx the operating context
     * @param property the property to set to read only
     */
    void setPropertyReadOnly(final Context ctx, final String property)
    {
        final ViewModeEnum mode = getMode(ctx, property);
        if (mode != ViewModeEnum.NONE)
        {
            setMode(ctx, property, ViewModeEnum.READ_ONLY);
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TableRenderer tableRenderer(final Context ctx)
    {
        final TableRenderer renderer = super.tableRenderer(ctx);
        return new GroupByTableRenderer(ctx, SubscriberServiceDisplayXInfo.SOURCE, renderer);
    }

    private boolean hasAccessRights(Context ctx, SubscriberServiceDisplay fee)
    {
        boolean result = false;
        try
        {
            Service service = ServiceSupport.getService(ctx,  fee.getServiceId());
            if (service != null)
            {
                result = hasPermission(ctx, service.getPermission());
            }
        }
        catch (HomeException e)
        {
            LogSupport.minor(ctx, this, "Unable to retrieve service " + fee.getServiceId() + ": " + e.getMessage());
            
        }
        return result;
    }

    static boolean hasPermission(Context ctx, String permission)
    {
        AuthMgr authMgr = new AuthMgr(ctx);
        return authMgr.check(permission);
    }
    
    // copied from DefaultserivceEditorWebControl
    /**
     * {@inheritDoc}
     */
    @Override
    public void outputCheckBox(final Context ctx, final PrintWriter out, final String name,
            final Object bean, final boolean isChecked)
    {
        final SubscriberServiceDisplay fee = (SubscriberServiceDisplay) bean;

        out.print("<input type=\"hidden\" name=\"");
        out.print(name);
        out.print(SEPERATOR);
        out.print("serviceId\" value=\"");
        out.print(fee.getServiceId());
        out.println("\" />");

        // MAALP: 05/03/04 - TT 402262116
        // Restore the real mode and display check box based on it
        // When view mode is selected show "x" by the checked items,
        // otherwise display a regular check box
        final int mode = ctx.getInt(CommonFramework.REAL_MODE, DISPLAY_MODE);
        if (fee.isMandatory() || mode == DISPLAY_MODE || !hasAccessRights(ctx, fee))
        {
            // Modified the name so it will not interfere with Framework fields.
            out.print("<td><input type=\"checkbox\" name=\"X");
            out.print(name);
            out.print("X\" disabled value=\"X\" ");
            if (fee.isChecked())
            {
                out.print("checked=\"checked\"");
            }
            out.print(" /> <input type=\"hidden\" name=\"");
            out.print(name);
            out.print(SEPERATOR);
            out.print("_enabled\" value=\"X\" /> </td>");
        }
        else
        {
            super.outputCheckBox(ctx, out, name, bean, fee.isChecked());
        }
    }

    /**
     * If the service is VoiceMail, gets al the extra fields from it
     * @param ctx the operating context
     * @param serviceId the service id
     * @param bean the bean to display
     * @param sub the subscriber on the screen
     */
    private void getBeanBytype(final Context ctx, 
            final SubscriberServiceDisplay bean, final Subscriber sub, final SubscriberServices service)
    {
        if (isVMService(ctx, service)
                && bean.getServiceStatus() == ServiceProvisionStatusEnum.PROVISIONED)
        {
            bean.setExtraFields(fillUpVoicemailInfo(ctx, sub));
        }
    }

    /**
     * Checks if the service ID sent is a VoiceMail
     * @param ctx the operating context
     * @param serviceId the service Id
     * @return true if it's a voice mail
     */
    private boolean isVMService(final Context ctx, final SubscriberServices svc)
    {
        boolean isVMService = false;

       
        if (svc.getService(ctx) != null && svc.getService(ctx).getType()== ServiceTypeEnum.VOICEMAIL)
        {
            isVMService = true;
        }

        return isVMService;
    }

    /**
     * Creates a VoicemailFieldsBean with the password as MSISDN
     * @param ctx the operating context
     * @param sub the subscriber on the screen.
     * @return the bean with the filled password or null if the subscriber's MSISDN is empty
     */
    private VoicemailFieldsBean fillUpVoicemailInfo(final Context ctx, final Subscriber sub)
    {
        final VoicemailFieldsBean vmBean = new VoicemailFieldsBean();
        if (sub != null && sub.getMSISDN() != null
                && sub.getMSISDN().trim().length() >= 0)
        {
            vmBean.setPassword(sub.getMSISDN());
            return vmBean;
        }
        return null;
    }

    // TODO 2009-01-28 refactor out these constants 
    /**
     * Context MODE key.
     */
    protected static final Object MODE = "MODE";

}
