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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberAuxiliaryService;
import com.trilogy.app.crm.bean.core.AuxiliaryService;
import com.trilogy.app.crm.support.ClosedUserGroupSupport73;
import com.trilogy.app.crm.support.Lookup;
import com.trilogy.app.crm.support.SubscriberAuxiliaryServiceSupport;
import com.trilogy.framework.xhome.beans.SafetyUtil;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;

/**
 * Update the CUGs containing this subscriber when the MSISDN is updated.
 *
 * @author cindy.wong@redknee.com
 */
public class SubscriberClosedUserGroupMsisdnUpdateHome extends HomeProxy
{

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Create a new instance of <code>SubscriberClosedUserGroupMsisdnUpdateHome</code>.
     *
     * @param delegate
     *            The home to which we delegate.
     */
    public SubscriberClosedUserGroupMsisdnUpdateHome(final Home delegate)
    {
        super(delegate);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object store(final Context context, final Object object) throws HomeException
    {
        final Subscriber newSubscriber = (Subscriber) object;
        final Subscriber oldSubscriber = (Subscriber) context.get(Lookup.OLDSUBSCRIBER);

        // run the delegate first so the msisdn update can be done first before updating the CUG        
        Object returnSubscriber =  super.store(context, newSubscriber);
        
        // determine if the msisdn has changed
        if ((newSubscriber != null && oldSubscriber != null)
                && (!SafetyUtil.safeEquals(newSubscriber.getMSISDN(), oldSubscriber.getMSISDN())))
        {
            final List<SubscriberAuxiliaryService> commonAssociations = findCommonAssociations(context, newSubscriber,
                oldSubscriber);
            for (SubscriberAuxiliaryService association : commonAssociations)
            {
                updateClosedUserGroup(context, association, newSubscriber.getMSISDN(), oldSubscriber.getMSISDN());
            }
        }

        return returnSubscriber;
    }

    /**
     * Find the set of auxiliary services common to both the old and new subscriber.
     *
     * @param newSubscriber
     *            The new subscriber.
     * @param oldSubscriber
     *            The old subscriber.
     * @return The set of auxiliary services common to both the old and new subscriber.
     */
    private List<SubscriberAuxiliaryService> findCommonAssociations(final Context ctx, final Subscriber newSubscriber,
        final Subscriber oldSubscriber)
    {
        final Set<Long> set = new HashSet<Long>();
        final List<SubscriberAuxiliaryService> commonServices = new ArrayList<SubscriberAuxiliaryService>();

        for (final Iterator iterator = newSubscriber.getAuxiliaryServices(ctx).iterator(); iterator.hasNext();)
        {
            final SubscriberAuxiliaryService association = (SubscriberAuxiliaryService) iterator.next();
            set.add(Long.valueOf(association.getAuxiliaryServiceIdentifier()));
        }

        for (final Iterator iterator = oldSubscriber.getAuxiliaryServices(ctx).iterator(); iterator.hasNext();)
        {
            final SubscriberAuxiliaryService association = (SubscriberAuxiliaryService) iterator.next();
            if (set.contains(association.getAuxiliaryServiceIdentifier()))
            {
                commonServices.add(association);
            }
        }
        return commonServices;
    }

    /**
     * Updates the MSISDN in CUG.
     *
     * @param context
     *            The operating context.
     * @param service_
     *            The auxiliary service associated with the CUG.
     * @param newMsisdn
     *            The new MSISDN.
     * @param oldMsisdn
     *            The old MSISDN.
     * @throws HomeException
     *             Thrown if there are problems with updating the subscriber.
     */
    private void updateClosedUserGroup(final Context context, final SubscriberAuxiliaryService association, final String newMsisdn,
        final String oldMsisdn) throws HomeException
    {
        final AuxiliaryService service = SubscriberAuxiliaryServiceSupport.getAuxiliaryService(context, association);
        if (service.isPrivateCUG(context))
        {
            ClosedUserGroupSupport73.switchPrivateCUGMsisdn(context, association, oldMsisdn, newMsisdn);
            ClosedUserGroupSupport73.switchCUGMsisdn(context, association, oldMsisdn, newMsisdn, service.getSpid());
            ClosedUserGroupSupport73.updateCUGNotifyMsisdn(context, association, oldMsisdn, newMsisdn, service.getSpid());
        }
        else if (service.isCUG(context))
        {
            ClosedUserGroupSupport73.switchCUGMsisdn(context, association, oldMsisdn, newMsisdn, service.getSpid());
            ClosedUserGroupSupport73.updateCUGNotifyMsisdn(context, association, oldMsisdn, newMsisdn, service.getSpid());
        }
        
    }
}
