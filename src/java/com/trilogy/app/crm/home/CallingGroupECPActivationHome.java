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

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;

import com.trilogy.app.crm.bean.AuxiliaryService;
import com.trilogy.app.crm.bean.AuxiliaryServiceTypeEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberAuxiliaryService;
import com.trilogy.app.crm.bean.SubscriberHome;
import com.trilogy.app.crm.client.AppEcpClient;
import com.trilogy.app.crm.support.CallingGroupSupport;


/**
 * Provides a mechanism to react to changes in the Subscriber's selected
 * AuxiliaryServices and enable or disable CallingGroup (PLP/CUG) functions in
 * ECP.
 *
 * @author gary.anderson@redknee.com
 */
public
class CallingGroupECPActivationHome
    extends HomeProxy
{
    /**
     * Creates a new CallingGroupECPActivationHome.
     *
     * @param context The operating context.
     * @param delegate The Home to which we delegate.
     */
    public CallingGroupECPActivationHome(final Context context, final Home delegate)
    {
        super(context, delegate);
    }

    // INHERIT
    @Override
    public Object create(Context ctx,final Object obj)
        throws HomeException
    {
        final SubscriberAuxiliaryService association = (SubscriberAuxiliaryService)obj;
        final String subscriberIdentifier = association.getSubscriberIdentifier();

        final boolean previouslyHadCallgingGroups =
            CallingGroupSupport.hasProvisionedCallingGroupService(ctx, subscriberIdentifier);

        final Object createdObject = super.create(ctx,obj);

        if (!previouslyHadCallgingGroups)
        {
            final AuxiliaryService service = association.getAuxiliaryService(ctx);

            if (service.getType() == AuxiliaryServiceTypeEnum.CallingGroup
                    && association.isProvisioned())
            {
                CallingGroupSupport.setFriendsAndFamilyEnabled(ctx,subscriberIdentifier, true);
            }
        }
        // Larry: thrown exception doesn't make any sense, we should capture home exception and change the subscriberauxiliaryservice state to provision fail. 
        return createdObject;
    }


    // INHERIT
    @Override
    public void remove(Context ctx, final Object obj)
        throws HomeException
     // Larry: thrown exception doesn't make any sense, we should capture home exception and change the subscriberauxiliaryservice state to provision fail. 
    {
        final SubscriberAuxiliaryService association = (SubscriberAuxiliaryService)obj;
        final String subscriberIdentifier = association.getSubscriberIdentifier();

        final boolean previouslyHadCallgingGroups =
            CallingGroupSupport.hasProvisionedCallingGroupService(ctx, subscriberIdentifier);

        // Larry: thrown exception doesn't make any sense, we should capture home exception and change the subscriberauxiliaryservice state to provision fail. 
        super.remove(ctx,association);

        final boolean nowHasCallgingGroups =
            CallingGroupSupport.hasProvisionedCallingGroupService(ctx, subscriberIdentifier);

        if (previouslyHadCallgingGroups && !nowHasCallgingGroups)
        {
            CallingGroupSupport.setFriendsAndFamilyEnabled(ctx,subscriberIdentifier, false);
        }
        // Larry: should not thrown out the exception.  
    }

} // class
