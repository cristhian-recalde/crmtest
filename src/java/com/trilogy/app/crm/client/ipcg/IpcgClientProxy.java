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
package com.trilogy.app.crm.client.ipcg;

import java.util.Iterator;
import java.util.Set;

import com.trilogy.app.crm.bean.Service;
import com.trilogy.app.crm.bean.ServiceFee2ID;
import com.trilogy.app.crm.bean.ServiceTypeEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.blackberry.BlackberrySupport;
import com.trilogy.app.crm.support.ServiceSupport;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.product.s5600.ipcg.rating.provisioning.RatePlan;
import com.trilogy.product.s5600.iprc.rating.provisioning.IprcRatePlan;


/**
 * Provides a simple proxy class for the IpcgClient.
 *
 * @author gary.anderson@redknee.com
 */
public
class IpcgClientProxy
    implements IpcgClient
{
    /**
     * Creates a new IpcgClientProxy.
     *
     * @param delegate The IpcgClient to which this proxy delegates.
     */
    public IpcgClientProxy(final IpcgClient delegate)
    {
        delegate_ = delegate;
    }


    /**
     * Gets the IpcgClient to which this proxy delegates.
     *
     * @return The IpcgClient to which this proxy delegates.
     */
    public IpcgClient getDelegate(final Context context)
    {
        return delegate_;
    }


    /**
     * {@inheritDoc}
     */
    public RatePlan[] getAllRatePlans(final Context context)
        throws IpcgRatingProvException
    {
        return getDelegate(context).getAllRatePlans(context);
    }
    
    /**
     * {@inheritDoc}
     */
    public IprcRatePlan[] getAllRatePlans(final Context context, final int spid)
        throws IpcgRatingProvException
    {
        return getDelegate(context).getAllRatePlans(context, spid);
    }


    /**
     * {@inheritDoc}
     */
    public int addSub(
        final Context context,
        final Subscriber subscriber,
        final short billingCycleDate,
        final String timeZone,
        final int ratePlan,
        final int scpId,
        final boolean subBasedRatingEnabled,
        final int serviceGrade)
        throws IpcgSubProvException
    {
        return getDelegate(context).addSub(
            context,
            subscriber,
            billingCycleDate,
            timeZone,
            ratePlan,
            scpId,
            subBasedRatingEnabled,
            serviceGrade);
    }


    /**
     * {@inheritDoc}
     */
    public int addChangeSub(
        final Context context,
        final Subscriber subscriber,
        final short billingCycleDate,
        final int ratePlan,
        final int serviceGrade)
        throws IpcgSubProvException
    {
        return getDelegate(context).addChangeSub(context,  subscriber, billingCycleDate, ratePlan, serviceGrade);
    }


    /**
     * {@inheritDoc}
     */
    public int addChangeSubBillCycleDate(
        final Context context,
        final Subscriber subscriber,
        final short billCycleDate)
        throws IpcgSubProvException
    {
        return getDelegate(context).addChangeSubBillCycleDate(
            context,
            subscriber,
            billCycleDate);
    }


    /**
     * {@inheritDoc}
     */
    public int setSubscriberEnabled(
        final Context context,
        final Subscriber subscriber,
        final boolean enabled)
        throws IpcgSubProvException
    {
        return getDelegate(context).setSubscriberEnabled(context, subscriber, enabled);
    }


    /**
     * {@inheritDoc}
     */
    public boolean isSubscriberProfileAvailable(
        final Context context,
        final Subscriber subscriber)
        throws IpcgSubProvException
    {
        return getDelegate(context).isSubscriberProfileAvailable(context, subscriber);
    }


    /**
     * {@inheritDoc}
     */
    public int removeSubscriber(final Context context, final Subscriber subscriber)
        throws IpcgSubProvException
    {
        return getDelegate(context).removeSubscriber(context, subscriber);
    }
    
    
    /**
     * @param ctx
     * @param svcSet
     * @return true if the service of type DATA present
     */
    public static boolean hasDataService(Context ctx ,Set svcSet)
    {
        Iterator svcSetItr = svcSet.iterator();
        boolean hasDataSvc = false;
        while (svcSetItr.hasNext())
        {
            ServiceFee2ID serviceFee2ID = (ServiceFee2ID)svcSetItr.next();
            Service svc = null;
            try
            {
                svc = ServiceSupport.getService(ctx,serviceFee2ID.getServiceId());
                if (svc != null && 
                        (svc.getType().equals(ServiceTypeEnum.DATA) || 
                                (svc.getType().equals(ServiceTypeEnum.BLACKBERRY) && 
                                        BlackberrySupport.areBlackberryServicesProvisionedToIPC(ctx)
                                )
                        )
                   )
                {
                    return true;
                }
            }
            catch (HomeException he)
            {
                LogSupport.minor(ctx , IpcgClientProxy.class, "Error while retrieving service for ServiceID= " +serviceFee2ID.getServiceId(), he);
            }
        }
        return hasDataSvc;
    }



    /**
     * The IpcgClient to which this proxy delegates.
     */
    private final IpcgClient delegate_;


} // class
