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
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;

import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.TDMAPackage;


/**
 * Provides a simple test client for the AAAClient interface.
 *
 * @author gary.anderson@redknee.com
 */
public
class AAATestClient
    implements AAAClient
{
    /**
     * {@inheritDoc}
     */
    public void createProfile(final Context context, final Subscriber subscriber)
        throws AAAClientException
    {
        debug(context, "createProfile()");
    }


    /**
     * {@inheritDoc}
     */
    public void deleteProfile(final Context context, final Subscriber subscriber)
        throws AAAClientException
    {
        debug(context, "deleteProfile()");
    }


    /**
     * {@inheritDoc}
     */
    public boolean isProfileEnabled(
        final Context context,
        final Subscriber subscriber)
        throws AAAClientException
    {
        debug(context, "isProfileEnabled()");
        return true;
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
        debug(context, "setProfileEnabled()");
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
        debug(context, "updateProfile(Subscriber)");
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
        debug(context, "updateProfile(TDMAPackage)");
    }


    /**
     * Provices a simple method of creating debug log messages.
     *
     * @param context The operating context.
     * @param method The name of the AAAClient method being called.
     */
    private void debug(final Context context, final String method)
    {
        if (LogSupport.isDebugEnabled(context))
        {
            new DebugLogMsg(this, "AAA TEST CLIENT -- " + method, null).log(context);
        }
    }

} // class
