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
package com.trilogy.app.crm.resource;

import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.beans.Validator;
import com.trilogy.framework.xhome.context.Context;

import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberXInfo;
import com.trilogy.app.crm.bean.core.ResourceDevice;
import com.trilogy.app.crm.exception.RethrowExceptionListener;
import com.trilogy.app.crm.support.Lookup;
import com.trilogy.app.crm.technology.TechnologyEnum;

/**
 * Check that the Subscription Types that have Resource Device configured as mandatory have the Resource device set.
 * Check that selected Resource Device is in the right state.
 *
 * @author victor.stratan@redknee.com
 */
public class SubscriptionResourceDeviceValidator implements Validator
{
    /**
     * {@inheritDoc}
     */
    public void validate(final Context ctx, final Object obj) throws IllegalStateException
    {
        final Subscriber sub = (Subscriber) obj;
        final RethrowExceptionListener exceptions = new RethrowExceptionListener();

        final String resourceID = sub.getResourceID(ctx);
        boolean isMandatory = ResourceDeviceConfig.isMandatoryForSubscriptionType(ctx, sub.getSubscriptionType());
        if (isMandatory)
        {
            if (resourceID == null ||  resourceID.length() == 0)
            {
                exceptions.thrown(new IllegalPropertyArgumentException(SubscriberXInfo.RESOURCE_ID,
                        "Resource Device is mandatory for Subscription Type [" + sub.getSubscriptionType() + "]"));
            }
        }

        if (resourceID != null &&  resourceID.length() > 0)
        {
            // if oldSub == null it's Create otherwise it's store
            final Subscriber oldSub = (Subscriber) ctx.get(Lookup.OLDSUBSCRIBER);
            if (oldSub == null || !resourceID.equals(oldSub.getResourceID(ctx)))
            {
                final ResourceDevice resource = ResourceDevice.getResourceDevice(ctx, resourceID);
                if (resource == null)
                {
                    exceptions.thrown(new IllegalPropertyArgumentException(SubscriberXInfo.RESOURCE_ID,
                            "Unable to locate Resource Device [" + resourceID + "]"));
                }
                else if (resource.getState() == ResourceDeviceStateEnum.IN_USE_INDEX)
                {
                    exceptions.thrown(new IllegalPropertyArgumentException(SubscriberXInfo.RESOURCE_ID,
                            "Resource Device [" + resourceID + "] is "
                            + ResourceDeviceStateEnum.IN_USE.getDescription(ctx)));
                }
                else if (resource.getState() == ResourceDeviceStateEnum.DEFECTIVE_INDEX)
                {
                    exceptions.thrown(new IllegalPropertyArgumentException(SubscriberXInfo.RESOURCE_ID,
                            "Resource Device [" + resourceID + "] is "
                            + ResourceDeviceStateEnum.DEFECTIVE.getDescription(ctx)));
                }

                if (resource != null && resource.getTechnology() != sub.getTechnology().getIndex())
                {
                    exceptions.thrown(new IllegalPropertyArgumentException(SubscriberXInfo.RESOURCE_ID,
                            "Resource Device [" + resourceID + "] is of different Technology ["
                            + TechnologyEnum.get((short) resource.getTechnology()).getDescription(ctx)
                            + "] then the Subscription [" + sub.getTechnology().getDescription(ctx) + "]"));
                }
            }
        }

        exceptions.throwAllAsCompoundException();
    }

}
