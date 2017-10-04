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

package com.trilogy.app.crm.subscriber.validator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.beans.Validator;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.MinorLogMsg;

import com.trilogy.app.crm.CoreCrmLicenseConstants;
import com.trilogy.app.crm.bean.AuxiliaryServiceTypeEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberAuxiliaryService;
import com.trilogy.app.crm.bean.SubscriberXInfo;
import com.trilogy.app.crm.bean.core.custom.AuxiliaryService;
import com.trilogy.app.crm.extension.subscriber.MultiSimSubExtension;
import com.trilogy.app.crm.support.LicensingSupportHelper;
import com.trilogy.app.crm.support.Lookup;


/**
 * Validator class for Multi-SIM auxiliary service.
 *
 * @author aaron.gourley@gmail.com
 * @since 8.8/9.0
 */
public class SubscriberMultiSimValidator implements Validator
{

    /**
     * Create a new instance of <code>SubscriberMultiSimValidator</code>.
     */
    protected SubscriberMultiSimValidator()
    {
        // do nothing
    }


    /**
     * Returns an instance of <code>SubscriberMultiSimValidator</code>.
     *
     * @return An instance of <code>SubscriberMultiSimValidator</code>.
     */
    public static SubscriberMultiSimValidator instance()
    {
        if (instance == null)
        {
            instance = new SubscriberMultiSimValidator();
        }
        return instance;
    }


    /**
     * {@inheritDoc}
     */
    public void validate(final Context context, final Object object)
    {
        final CompoundIllegalStateException exceptions = new CompoundIllegalStateException();
        
        if (LicensingSupportHelper.get(context).isLicensed(context, CoreCrmLicenseConstants.MULTI_SIM_LICENSE))
        {
            Subscriber subscriber = null;
            AuxiliaryService newAuxSvc = null;
            if (object instanceof Subscriber)
            {
                subscriber = (Subscriber) object;
            }
            else if (object instanceof MultiSimSubExtension)
            {
                MultiSimSubExtension ext = (MultiSimSubExtension) object;
                subscriber = ext.getSubscriber(context);
                newAuxSvc = ext.getAuxiliaryService(context);
            }
            
            if (subscriber != null)
            {
                final Collection<SubscriberAuxiliaryService> serviceRefs = new ArrayList<SubscriberAuxiliaryService>(subscriber.getAuxiliaryServices(context));
                if (serviceRefs != null && serviceRefs.size() > 0)
                {
                    Set<Long> oldSvcIds = Collections.emptySet();
                    Subscriber oldSub = (Subscriber) context.get(Lookup.OLD_FROZEN_SUBSCRIBER);
                    if (oldSub != null)
                    {
                        oldSvcIds = new HashSet<Long>(oldSub.getAuxiliaryServiceIds(context));
                    }
                    
                    if (newAuxSvc != null)
                    {
                        SubscriberAuxiliaryService temp = new SubscriberAuxiliaryService();
                        temp.setSubscriberIdentifier(subscriber.getId());
                        temp.setAuxiliaryService(newAuxSvc);
                        temp.setAuxiliaryServiceIdentifier(newAuxSvc.getIdentifier());
                        temp.setType(newAuxSvc.getType());
                        serviceRefs.add(temp);   
                    }

                    for (final SubscriberAuxiliaryService serviceRef : serviceRefs)
                    {
                        // Only allowed one primary multi-SIM auxiliary service
                        // Primary service has default secondary identifier.  Per-SIM services also exist, but they
                        // will have secondary idenifier set.
                        if (serviceRef.getSecondaryIdentifier() == SubscriberAuxiliaryService.DEFAULT_SECONDARYIDENTIFIER
                                && !oldSvcIds.contains(serviceRef))
                        {
                            try
                            {
                                AuxiliaryService auxSvc = serviceRef.getAuxiliaryService(context);
                                if (auxSvc != null && auxSvc.getType() == AuxiliaryServiceTypeEnum.MultiSIM)
                                {
                                    for (final SubscriberAuxiliaryService otherServiceRef : serviceRefs)
                                    {
                                        if (otherServiceRef.getSecondaryIdentifier() == SubscriberAuxiliaryService.DEFAULT_SECONDARYIDENTIFIER
                                                && otherServiceRef != serviceRef)
                                        {
                                            AuxiliaryService otherAuxSvc = otherServiceRef.getAuxiliaryService(context);
                                            if (otherAuxSvc != null && otherAuxSvc.getType() == AuxiliaryServiceTypeEnum.MultiSIM)
                                            {
                                                exceptions.thrown(new IllegalPropertyArgumentException(
                                                        SubscriberXInfo.AUXILIARY_SERVICES, 
                                                        "Only one Multi-SIM auxiliary service may be assigned to a subscription."));
                                            }
                                        }
                                    }
                                    break;
                                }
                            }
                            catch (HomeException e)
                            {
                                String msg = "Error retrieving auxiliary service: " + serviceRef;
                                new MinorLogMsg(this, msg, e).log(context);
                                exceptions.thrown(new IllegalPropertyArgumentException(
                                        SubscriberXInfo.AUXILIARY_SERVICES, msg));
                                break;
                            }
                        }
                    }
                }                
            }
        }

        exceptions.throwAll();
    }

    /**
     * Singleton instance.
     */
    private static SubscriberMultiSimValidator instance;
}
