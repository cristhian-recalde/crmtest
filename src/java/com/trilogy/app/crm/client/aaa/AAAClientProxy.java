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
package com.trilogy.app.crm.client.aaa;

import com.trilogy.framework.xhome.context.Context;

import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.TDMAPackage;


/**
 * Provides a proxy for the AAAClient interface.
 *
 * @author gary.anderson@redknee.com
 */
public
class AAAClientProxy
    implements AAAClient
{
    /**
     * Creates a new proxy for the given delegate.
     *
     * @param delegate The AAAClient to which this proxy delegates.
     */
    public AAAClientProxy(final AAAClient delegate)
    {
        delegate_ = delegate;
    }


    /**
     * Gets the AAAClient to which this proxy delegates.
     *
     * @param context The operating context.
     * @return The AAAClient to which this proxy delegates.
     */
    public AAAClient getDelegate(final Context context)
    {
        return delegate_;
    }


    /**
     * {@inheritDoc}
     */
    public void createProfile(final Context context, final Subscriber subscriber)
        throws AAAClientException
    {
        getDelegate(context).createProfile(context, subscriber);
    }


    /**
     * {@inheritDoc}
     */
    public void deleteProfile(final Context context, final Subscriber subscriber)
        throws AAAClientException
    {
        getDelegate(context).deleteProfile(context, subscriber);
    }


    /**
     * {@inheritDoc}
     */
    public boolean isProfileEnabled(
        final Context context,
        final Subscriber subscriber)
        throws AAAClientException
    {
        return getDelegate(context).isProfileEnabled(context, subscriber);
    }


    /**
     * {@inheritDoc}
     */
    public void setProfileEnabled(
        final Context context,
        final Subscriber subscriber,
        final boolean enabled)
        throws AAAClientException
    {
        getDelegate(context).setProfileEnabled(context, subscriber, enabled);
    }


    /**
     * {@inheritDoc}
     */
    public void updateProfile(
        final Context context,
        final Subscriber oldSubscriber,
        final Subscriber newSubscriber)
        throws AAAClientException
    {
        getDelegate(context).updateProfile(
            context,
            oldSubscriber,
            newSubscriber);
    }


    /**
     * {@inheritDoc}
     */
    public void updateProfile(
        final Context context,
        final TDMAPackage oldPackage,
        final TDMAPackage newPackage)
        throws AAAClientException
    {
        getDelegate(context).updateProfile(
            context,
            oldPackage,
            newPackage);
    }


    /**
     * The AAAClient to which this proxy delegates.
     */
    private final AAAClient delegate_;


} // class
