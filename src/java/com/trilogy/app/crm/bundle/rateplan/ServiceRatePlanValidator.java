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
package com.trilogy.app.crm.bundle.rateplan;

import java.util.Collection;
import java.util.Iterator;

import com.trilogy.framework.xhome.beans.ExceptionListener;
import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.beans.Validator;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.LogSupport;

import com.trilogy.app.crm.bean.AbstractPricePlan;
import com.trilogy.app.crm.bean.PPVModificationRequest;
import com.trilogy.app.crm.bean.PricePlanXInfo;
import com.trilogy.app.crm.bean.Service;
import com.trilogy.app.crm.bean.ServiceHome;
import com.trilogy.app.crm.bean.ServiceTypeEnum;
import com.trilogy.app.crm.bean.core.PricePlan;
import com.trilogy.app.crm.bean.core.PricePlanVersion;
import com.trilogy.app.crm.bean.core.ServiceFee2;
import com.trilogy.app.crm.blackberry.BlackberrySupport;
import com.trilogy.app.crm.exception.RethrowExceptionListener;
import com.trilogy.app.crm.support.PricePlanSupport;

/**
 * Ensure the selected service has a corresponding rate plan declared in Price Plan Version.
 *
 * @author victor.stratan@redknee.com
 */
public class ServiceRatePlanValidator implements Validator
{
    /**
     * {@inheritDoc}
     */
    public void validate(final Context ctx, final Object obj) throws IllegalStateException
    {
        if (obj instanceof PPVModificationRequest)
        {
            validate(ctx, (PPVModificationRequest) obj);
            
        }
        else if (obj instanceof PricePlanVersion)
        {
            validate(ctx, (PricePlanVersion) obj);
        }
        else
        {
            throw new UnsupportedOperationException("Unsupported bean for this home");
        }
    }
    
    private PricePlan getPricePlan(Context ctx, long id)
    {
        PricePlan pricePlan = null;
        try
        {
            pricePlan = PricePlanSupport.getPlan(ctx, id);
        }
        catch (HomeException e)
        {
            final String msg = "Exception occured when trying to retrieve the Price Plan for Price Plan Version: "
                    + id + ". " + e.getMessage();
            LogSupport.minor(ctx, this, msg, e);
            throw new IllegalStateException(msg);
        }
        
        if (pricePlan == null)
        {
            final String msg = "Unable to retrieve the Price Plan for Price Plan Version: " + id + ".";
            LogSupport.minor(ctx, this, msg, null);
            throw new IllegalStateException(msg);
        }

        return pricePlan;
    }


    public void validate(final Context ctx, final PricePlanVersion ppv) throws IllegalStateException
    {

        PricePlan pricePlan = getPricePlan(ctx, ppv.getId());
        
        final RethrowExceptionListener el = new RethrowExceptionListener();
        final Collection serviceFees = PricePlanSupport.getAllServicesInPPV(ctx, ppv);

        validateRatePlans(ctx, pricePlan, serviceFees, el);
        el.throwAllAsCompoundException();
    }

    public void validate(final Context ctx, final PPVModificationRequest request) throws IllegalStateException
    {
        PricePlan pricePlan = getPricePlan(ctx, request.getPricePlanIdentifier());
        
        final RethrowExceptionListener el = new RethrowExceptionListener();
        validateRatePlans(ctx, pricePlan, request.getServicePackageVersion().getServiceFees(ctx).values(), el);
        validateRatePlans(ctx, pricePlan, request.getServicePackageVersion().getNewServiceFees(ctx).values(), el);
        el.throwAllAsCompoundException();
    }

    public static void validateRatePlans(final Context ctx, final PricePlan pricePlan, final Collection<ServiceFee2> serviceFees,
            final ExceptionListener el)
        throws IllegalStateException
    {
        final Home serviceHome = (Home) ctx.get(ServiceHome.class);

        boolean hasVoice = false;
        boolean hasSMS = false;
        boolean hasData = false;

        for (final Iterator<ServiceFee2> iter = serviceFees.iterator(); iter.hasNext();)
        {
            final ServiceFee2 serviceFee = iter.next();
            Service service = null;
            try
            {
                service = (Service) serviceHome.find(ctx, Long.valueOf(serviceFee.getServiceId()));
            }
            catch (HomeException hEx)
            {
                final IllegalStateException exception;
                exception = new IllegalStateException("Unable to find service " + serviceFee.getServiceId());
                exception.initCause(hEx);
                el.thrown(exception);
                continue;
            }

            if (service == null)
            {
                el.thrown(new IllegalStateException("Service " + serviceFee.getServiceId() + " does not exist"));
                continue;
            }

            if (service.getType() == ServiceTypeEnum.VOICE)
            {
                hasVoice = true;
                if (pricePlan.getVoiceRatePlan().equals(AbstractPricePlan.DEFAULT_VOICERATEPLAN))
                {
                    // no voice rate plan selected
                    el.thrown(new IllegalPropertyArgumentException(PricePlanXInfo.VOICE_RATE_PLAN,
                            "value required by Voice service '" + service.getID() + " - " + service.getName() + "'"));
                }
            }
            else if (service.getType() == ServiceTypeEnum.SMS)
            {
                hasSMS = true;
                if (pricePlan.getSMSRatePlan().equals(AbstractPricePlan.DEFAULT_SMSRATEPLAN))
                {
                    // no SMS rate plan selected
                    el.thrown(new IllegalPropertyArgumentException(PricePlanXInfo.SMSRATE_PLAN,
                            "value required by SMS service '" + service.getID() + " - " + service.getName() + "'"));
                }
            }
            else if (service.getType() == ServiceTypeEnum.DATA)
            {
                hasData = true;
                if (pricePlan.getDataRatePlan() == AbstractPricePlan.DEFAULT_DATARATEPLAN)
                {
                    // no data rate plan selected
                    el.thrown(new IllegalPropertyArgumentException(PricePlanXInfo.DATA_RATE_PLAN,
                            "value required by data service '" + service.getID() + " - " + service.getName() + "'"));
                }
            }
            else if (service.getType() == ServiceTypeEnum.BLACKBERRY && BlackberrySupport.areBlackberryServicesProvisionedToIPC(ctx))
            {
                hasData = true;
                if (pricePlan.getDataRatePlan() == AbstractPricePlan.DEFAULT_DATARATEPLAN)
                {
                    // no data rate plan selected
                    el.thrown(new IllegalPropertyArgumentException(PricePlanXInfo.DATA_RATE_PLAN,
                            "value required by BlackBerry service '" + service.getID() + " - " + service.getName() + "'"));
                }
            }
        }

    }
}
