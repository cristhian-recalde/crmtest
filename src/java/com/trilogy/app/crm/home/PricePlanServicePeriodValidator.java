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
package com.trilogy.app.crm.home;

import java.util.Iterator;
import java.util.Map;

import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.beans.Validator;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;

import com.trilogy.app.crm.bean.ServicePeriodEnum;
import com.trilogy.app.crm.bean.PricePlanVersion;
import com.trilogy.app.crm.bean.Service;
import com.trilogy.app.crm.bean.ServiceFee2ID;
import com.trilogy.app.crm.bean.ServicePackageVersionXInfo;
import com.trilogy.app.crm.bean.ServicePeriodEnum;
import com.trilogy.app.crm.bean.ServicePreferenceEnum;
import com.trilogy.app.crm.bean.core.ServiceFee2;
import com.trilogy.app.crm.exception.RethrowExceptionListener;
import com.trilogy.app.crm.support.ServiceSupport;
import com.trilogy.util.snippet.log.Logger;

/**
 * Validates that the right service fees corresponds to the righ service period.
 * For example:
 * If the service for this particular fee is "one time" the service period should be only "One time"
 * If the service is variable the fee would never be one time and could be monthly or weekly
 *
 * @author arturo.medina@redknee.com
 */
public class PricePlanServicePeriodValidator implements Validator
{
    /**
     * {@inheritDoc}
     */
    public void validate(final Context ctx, final Object obj)
    {
        final PricePlanVersion ppv = (PricePlanVersion)obj;
        final Map serviceFess = ppv.getServicePackageVersion().getServiceFees();

        if (serviceFess.size() == 0)
        {
            // shortcut for price plan versions with no services
            return;
        }

        final Iterator feeEntries = serviceFess.entrySet().iterator();
        final RethrowExceptionListener exceptions = new RethrowExceptionListener();

        while (feeEntries.hasNext())
        {
            final Map.Entry entry = (Map.Entry) feeEntries.next();
//            final Long key = (Long) entry.getKey();
            ServiceFee2ID SF2IDKey = (ServiceFee2ID) entry.getKey();
            final Long key = SF2IDKey.getServiceId();
            final ServiceFee2 fee = (ServiceFee2) entry.getValue();
            final Service service;

            try
            {
                service = ServiceSupport.getService(ctx, key);
            }
            catch (HomeException e)
            {
                final String msg = "Unable to validate presence of service " + key;
                final IllegalPropertyArgumentException ex = new IllegalPropertyArgumentException(
                        ServicePackageVersionXInfo.SERVICE_FEES, msg);
                ex.initCause(e);
                Logger.minor(ctx, this, msg, e);
                exceptions.thrown(ex);
                continue;
            }

            if (service.getChargeScheme() == ServicePeriodEnum.ONE_TIME)
            {
                if (fee.getServicePeriod() != ServicePeriodEnum.ONE_TIME)
                {
                    final String msg = HEADER_MSG + service.getID() + " " + service.getName()
                            + " should be One Time";
                    final IllegalPropertyArgumentException ex = new IllegalPropertyArgumentException(
                            ServicePackageVersionXInfo.SERVICE_FEES, msg);
                    exceptions.thrown(ex);
                }
       
            }
            else if (service.getChargeScheme() == ServicePeriodEnum.MONTHLY
                        && fee.getServicePeriod() != ServicePeriodEnum.MONTHLY)
            {
                    final String msg = HEADER_MSG + service.getID() + " " + service.getName()
                            + " should be Monthly";
                    final IllegalPropertyArgumentException ex = new IllegalPropertyArgumentException(
                            ServicePackageVersionXInfo.SERVICE_FEES, msg);
                    exceptions.thrown(ex);
                
            }
            else if (service.getChargeScheme() == ServicePeriodEnum.WEEKLY
                    && fee.getServicePeriod() != ServicePeriodEnum.WEEKLY)
        {
                final String msg = HEADER_MSG + service.getID() + " " + service.getName()
                        + " should be Weekly";
                final IllegalPropertyArgumentException ex = new IllegalPropertyArgumentException(
                        ServicePackageVersionXInfo.SERVICE_FEES, msg);
                exceptions.thrown(ex);
            
        }
            else if (service.getChargeScheme() == ServicePeriodEnum.MULTIMONTHLY
                    && fee.getServicePeriod() != ServicePeriodEnum.MULTIMONTHLY)
        {
                final String msg = HEADER_MSG + service.getID() + " " + service.getName()
                        + " should be Multi-Monthly";
                final IllegalPropertyArgumentException ex = new IllegalPropertyArgumentException(
                        ServicePackageVersionXInfo.SERVICE_FEES, msg);
                exceptions.thrown(ex);
            
        }
        }

        exceptions.throwAllAsCompoundException();
    }

    /**
     * Exception message header
     */
    private static final String HEADER_MSG = "Service Period for service ";

}
