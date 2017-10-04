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

package com.trilogy.app.crm.provision.soap;

import java.util.Date;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.LogSupport;

import com.trilogy.app.crm.bean.PricePlan;
import com.trilogy.app.crm.bean.PricePlanVersionHome;
import com.trilogy.app.crm.bean.PricePlanVersionID;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberHome;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.app.crm.support.PricePlanSupport;
import com.trilogy.app.crm.support.SubscriberSupport;

import electric.util.holder.intOut;

/**
 * Provides the functionality of provisioning a SecondaryPrice Plan for the given Msisdn,PricePlan ID with given
 * start & end date.
 *
 * @author manda.subramanyam@redknee.com
 * @author amit.baid@redknee.com
 */
public class PricePlanProvisionImpl implements PricePlanProvisionInterface
{

    /**
     * The operating context
     */
    private Context context_;

    /**
     * @param ctx The operating context
     */
    public PricePlanProvisionImpl(final Context ctx)
    {
        super();
        context_ = ctx;
    }

    /**
     * This method sets the Secondary PricePlan, its start and end dates for the
     * given Subscriber Msisdn.
     *
     * @param msisdn String MSISDN of the Subscriber
     * @param pricePlanId int Secondary Price Plan Id
     * @param startDate java.util.Date Secondary Price Plan Start Date
     * @param endDate java.util.Date Secondary Price Plan End Date
     * @param retCode intOut holder object
     * @throws SoapServiceException
     */
    public void provisionSecondaryPricePlan(final String msisdn, final int pricePlanId,
            final Date startDate, final Date endDate, final intOut retCode)
        throws SoapServiceException
    {
        Subscriber sub = null;
        PricePlan pricePlan = null;

        if (msisdn == null || pricePlanId <= 0 || startDate == null || endDate == null)
        {
            retCode.value = INVALID_PARAMETERS;
            LogSupport.info(getContext(), this, "Invalid Parameters Provided ==> msisdn --" + msisdn
                    + "--pricePlanId--" + pricePlanId + "--startDate--"
                    + startDate + "--endDate--" + endDate);
            return;
        }
        else if (msisdn != null && msisdn.length() <= 0)
        {
            retCode.value = INVALID_MSISDN;
            return;
        }

        if (LogSupport.isDebugEnabled(context_))
        {
            LogSupport.debug(context_, this, "msisdn --" + msisdn
                    + "--pricePlanId--" + pricePlanId + "--startDate--"
                    + startDate + "--endDate--" + endDate);
        }

        final Date today = new Date();

        if (startDate.before(today))
        {
            retCode.value = STARTDATE_LESS_THAN_CURRENTDATE;
            return;
        }
        if (endDate.before(startDate))
        {
            retCode.value = ENDDATE_LESS_THAN_STARTDATE;
            return;
        }

        try
        {
            sub = SubscriberSupport.lookupSubscriberForMSISDN(context_, msisdn);
        }
        catch (HomeException e)
        {
            retCode.value = INVALID_MSISDN;
            LogSupport.major(getContext(), this, "Unable to Find Subscriber for the given Msisdn = "
                    + msisdn, e);
            return;
        }

        if (sub == null)
        {
            retCode.value = INVALID_MSISDN;
            LogSupport.info(getContext(), this, "Unable to Find Subscriber for the given Msisdn = "
                    + msisdn);
            return;
        }

        //Check for deactivated Subscriber
        if (sub != null && sub.getState().getIndex() == SubscriberStateEnum.INACTIVE_INDEX)
        {
            retCode.value = INVALID_SUBSCRIBER_STATE;
            LogSupport.info(getContext(), this, "Subscriber Not in Active State ==>" + msisdn);
            return;
        }

        try
        {
            pricePlan = PricePlanSupport.getPlan(context_, pricePlanId);
        }
        catch (HomeException e)
        {
            retCode.value = INVALID_PRICE_PLAN;
            LogSupport.major(getContext(), this, "Unable to Find Price Plan for the given Price Plan Id = "
                    + pricePlanId, e);
            return;
        }

        if (pricePlan == null)
        {
            retCode.value = INVALID_PRICE_PLAN;
            LogSupport.info(getContext(), this, "Unable to Find Price Plan for the given Price Plan Id = "
                    + pricePlanId);
            return;
        }
        else if (pricePlan.getSpid() != sub.getSpid())
        {
            retCode.value = INVALID_PRICE_PLAN;
            LogSupport.info(getContext(), this, "Price Plan SPID:" + pricePlan.getSpid()
                    + " of PricePlan ID." + pricePlanId + " does not match with  subscriber spid : " + sub.getSpid());
            return;
        }
        else if (!pricePlan.getEnabled())
        {
            retCode.value = INVALID_PRICE_PLAN;
            LogSupport.info(getContext(), this, "Price Plan for the given Price Plan Id = "
                    + pricePlanId + " is disabled.");
            return;
        }
        else if (pricePlan.getTechnology().getIndex() != sub.getTechnology().getIndex())
        {
            retCode.value = INVALID_PRICE_PLAN;
            LogSupport.info(getContext(), this, "Price Plan for the given Price Plan Id = "
                    + pricePlanId + " is non " + sub.getTechnology().getDescription());
            return;
        }

        try
        {
            final Home ppvHome = (Home) getContext().get(PricePlanVersionHome.class);
            final Object ppv = ppvHome.find(new PricePlanVersionID(pricePlan.getId(), pricePlan.getCurrentVersion()));
            if (ppv == null)
            {
                retCode.value = INVALID_PRICE_PLAN;
                LogSupport.info(getContext(), this, "Price Plan " + pricePlanId
                        + " does not have a valid Price Plan Version ");
                return;
            }
        }
        catch (HomeException he)
        {
            retCode.value = INVALID_PRICE_PLAN;
            LogSupport.info(getContext(), this, "Price Plan " + pricePlanId
                    + " does not have a valid Price Plan Version ");
            return;
        }

        sub.setSecondaryPricePlan(pricePlanId);
        sub.setSecondaryPricePlanStartDate(CalendarSupportHelper.get(getContext()).getDateWithNoTimeOfDay(startDate));
        sub.setSecondaryPricePlanEndDate(CalendarSupportHelper.get(getContext()).getDateWithNoTimeOfDay(endDate));

        final Home subHome = (Home) context_.get(SubscriberHome.class);

        try
        {
            subHome.store(sub);
        }
        catch (HomeException e1)
        {
            LogSupport.major(context_, this, "Failed to update the Subscriber = " + msisdn
                    + " with Secondary Price Plan Information", e1);
            retCode.value = FAILED_PROVISIONING;
            return;
        }

        retCode.value = SUCCESSFUL;
    }

    public Context getContext()
    {
        return context_;
    }

    public void setContext(final Context context)
    {
        this.context_ = context;
    }
}
