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
package com.trilogy.app.crm.client.bm;

import com.trilogy.framework.xhome.context.Context;

import com.trilogy.service.corba.CorbaClientProperty;


//TODO -- 2008-08-08 -- If this class is actually useful, it should be moved to a utility project.


/**
 * Provides an alternate version of the ConnectionProperties that explicitly
 * checks for available updates less often. This is useful for reducing overly
 * frequent checks on a high-traffic connection.
 *
 * @author gary.anderson@redknee.com
 */
public class TimerCachedConnectionProperties
    extends ConnectionProperties
{

    /**
     * Creates a new ConnectionProperties for the given properties key.
     *
     * @param propertiesKey The key used to look up properties in the
     * CorbaClientProperty home.
     */
    public TimerCachedConnectionProperties(final String propertiesKey)
    {
        this(propertiesKey, DEFAULT_MINIMUM_CHECK_PERIOD);
    }


    /**
     * Creates a new ConnectionProperties for the given properties key.
     *
     * @param propertiesKey The key used to look up properties in the
     * CorbaClientProperty home.
     * @param minimumCheckPeriod The minimum amount of time that must elapse
     * before an actual check can be performed.
     */
    public TimerCachedConnectionProperties(final String propertiesKey, final long minimumCheckPeriod)
    {
        super(propertiesKey);
        lastTimeUpdated_ = 0L;
        minimumCheckPeriod_ = minimumCheckPeriod;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    protected CorbaClientProperty lookupProperties(final Context context)
    {
        setLastTimeUpdated(System.currentTimeMillis());
        return super.lookupProperties(context);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public boolean updateAvailable(final Context context)
    {
        final long now = System.currentTimeMillis();
        final long nextAllowedCheck = getLastTimeUpdated() + getMinimumCheckPeriod();

        final boolean updateAvailable;

        if (now >= nextAllowedCheck)
        {
            updateAvailable = super.updateAvailable(context);
        }
        else
        {
            updateAvailable = false;
        }

        return updateAvailable;
    }


    /**
     * Gets the minimum amount of time that must elapse before an actual check
     * can be performed.
     *
     * @return The minimum amount of time that must elapse before an actual
     * check can be performed.
     */
    public long getMinimumCheckPeriod()
    {
        return minimumCheckPeriod_;
    }


    /**
     * Sets the minimum amount of time that must elapse before an actual check
     * can be performed.
     *
     * @param minimumCheckPeriod The minimum amount of time that must elapse
     * before an actual check can be performed.
     */
    public void setMinimumCheckPeriod(final long minimumCheckPeriod)
    {
        minimumCheckPeriod_ = minimumCheckPeriod;
    }


    /**
     * Gets the last time at which the properties were updated.
     *
     * @return The last time at which the properties were updated.
     */
    protected long getLastTimeUpdated()
    {
        return lastTimeUpdated_;
    }


    /**
     * Sets the last time at which the properties were updated.
     *
     * @param lastTimeUpdated The last time at which the properties were
     * updated.
     */
    protected void setLastTimeUpdated(final long lastTimeUpdated)
    {
        lastTimeUpdated_ = lastTimeUpdated;
    }


    /**
     * The last time at which the properties were updated.
     */
    private long lastTimeUpdated_;

    /**
     * The minimum amount of time that must elapse before an actual check can be
     * performed.
     */
    private long minimumCheckPeriod_;

    /**
     * The default, minimum amount of time that must elapse before an actual
     * check can be performed.
     */
    private static final long DEFAULT_MINIMUM_CHECK_PERIOD = 5000L;
}
