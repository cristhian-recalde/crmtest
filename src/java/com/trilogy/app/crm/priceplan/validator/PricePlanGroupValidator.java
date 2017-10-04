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
package com.trilogy.app.crm.priceplan.validator;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.trilogy.framework.xhome.beans.Validator;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;

import com.trilogy.app.crm.bean.PricePlan;
import com.trilogy.app.crm.bean.ServiceFee2ID;
import com.trilogy.app.crm.bean.ServicePreferenceEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberAuxiliaryService;
import com.trilogy.app.crm.bean.core.BundleFee;
import com.trilogy.app.crm.bean.core.PricePlanVersion;
import com.trilogy.app.crm.bean.core.ServiceFee2;
import com.trilogy.app.crm.support.HomeSupportHelper;

/**
 * @author senthooran.kularajasingham@redknee.com
 */
public class PricePlanGroupValidator implements Validator
{

    /**
     *
     */
    public PricePlanGroupValidator()
    {
    }

    private void validate(final Context ctx, final Subscriber sub)
    {
        final Set<String> ppServices = new HashSet<String>();

        List<SubscriberAuxiliaryService> auxSvcs = sub.getAuxiliaryServices(ctx);
        for (SubscriberAuxiliaryService sas : auxSvcs)
        {
            final StringBuilder buf = new StringBuilder("a");
            buf.append(sas.getAuxiliaryServiceIdentifier());
            ppServices.add(buf.toString());
        }

        Map bundles = new HashMap(sub.getBundles());
        for (Object obj : bundles.keySet())
        {
            final StringBuilder buf = new StringBuilder("b");
            buf.append(obj);
            ppServices.add(buf.toString());
        }

        Set services = sub.getServices();
        for (Object obj : services)
        {
            final StringBuilder buf = new StringBuilder();
            buf.append(obj);
            ppServices.add(buf.toString());
        }

        try
        {
            final PricePlan pp = HomeSupportHelper.get(ctx).findBean(ctx, PricePlan.class, sub.getPricePlan());
            if (pp == null)
            {
                // can't do validation, Price Plan is missing. Price Plan validation should throw validation exception
                return;
            }
            final long ppglID = pp.getPricePlanGroup();
            final PricePlanGroupList ppgl = new PricePlanGroupList(ctx, Long.valueOf(ppglID), null, null);

            ppgl.validate(ppServices, ppServices, PricePlanGroupList.SUBSCRIBER_VALIDATION);
        }
        catch (HomeException e)
        {
            if (LogSupport.isDebugEnabled(ctx))
            {
                new DebugLogMsg(this,
                        "Error Occurred while validating Price Plan Grouping for subscriber=" + sub.getId()
                                + " due to: " + e.getMessage(),
                        e).log(ctx);
            }
        }
    }

    public void validate(final Context ctx, final Object obj) throws IllegalStateException
    {
        PricePlanVersion ppv = null;

        final Set<String> ppServices = new HashSet<String>();
        final Set<String> ppMandatoryServices = new HashSet<String>();

        if (obj instanceof PricePlanVersion)
        {
            ppv = (PricePlanVersion) obj;
        }
        else if (obj instanceof Subscriber)
        {
            validate(ctx, (Subscriber) obj);
            return;
        }
        else
        {
            return;
        }

        //Getting Services
        Map<ServiceFee2ID, ServiceFee2> svcFees = ppv.getServiceFees(ctx);
        for (Map.Entry<ServiceFee2ID, ServiceFee2> entry : svcFees.entrySet())
        {
        	ServiceFee2ID serviceFee2ID = entry.getKey();
            final ServiceFee2 servicefee2 = entry.getValue();

            if (servicefee2.getServicePreference().equals(ServicePreferenceEnum.MANDATORY))
            {
                ppMandatoryServices.add(String.valueOf(serviceFee2ID.getServiceId()));
            }

            ppServices.add(String.valueOf(serviceFee2ID.getServiceId()));
        }

        //Getting Bundles
        Map<Object, Object> bundleFees = ppv.getServicePackageVersion().getBundleFees();
        for (Map.Entry entry : bundleFees.entrySet())
        {
            Object key = entry.getKey();
            final BundleFee bundlefee = (BundleFee) entry.getValue();

            if (bundlefee.getServicePreference().equals(ServicePreferenceEnum.MANDATORY))
            {
                ppMandatoryServices.add("b" + String.valueOf(key));
            }

            //May have performance since using "b"+str(senthooran), check back later
            ppServices.add("b" + String.valueOf(key));
        }

        try
        {
            final PricePlan pp = HomeSupportHelper.get(ctx).findBean(ctx, PricePlan.class, ppv.getId());
            if(pp == null)
            {
                return;
            }
            final long ppglID = pp.getPricePlanGroup();
            final PricePlanGroupList ppgl = new PricePlanGroupList(ctx, Long.valueOf(ppglID), null, null);

            ppgl.validate(ppServices, ppMandatoryServices, PricePlanGroupList.PRICEPLAN_VALIDATION);
        }
        catch (HomeException e)
        {
            if (LogSupport.isDebugEnabled(ctx))
            {
                new DebugLogMsg(this,
                        "Error Occurred while validating Price Plan Grouping. " + e.getMessage(),
                        e).log(ctx);
            }
        }
    }
}
