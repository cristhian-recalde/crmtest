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
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.MajorLogMsg;

import com.trilogy.service.corba.CorbaClientProperty;
import com.trilogy.service.corba.CorbaClientPropertyHome;
import com.trilogy.service.corba.CorbaClientPropertyXInfo;


//  TODO -- 2008-08-08 -- If this class is actually useful, it should be moved to a utility project.


/**
 * Provides a convenient method of caching the look-up of CORBA connection
 * properties, and of determining when these properties have been updated.
 * <p>
 * See {@link TimerCachedConnectionProperties} for an extension of this class
 * that reduces the cost of calling
 * {@link ConnectionProperties#updateAvailable(Context) updateAvailable()} on high-traffic
 * connections.
 *
 * @see TimerCachedConnectionProperties
 * @author gary.anderson@redknee.com
 */
public class ConnectionProperties
{
    /**
     * Creates a new ConnectionProperties for the given properties key.
     *
     * @param propertiesKey The key used to look up properties in the
     * CorbaClientProperty home.
     */
    public ConnectionProperties(final String propertiesKey)
    {
        if (propertiesKey == null || propertiesKey.trim().length() == 0)
        {
            throw new IllegalArgumentException("The propertiesKey value is empty.");
        }

        propertiesKey_ = propertiesKey;
    }


    /**
     * Gets the currently cached set of connection properties. Properties are
     * lazy-loaded.
     *
     * @param context The operating context.
     * @return The CORBA connection properties.
     */
    public CorbaClientProperty getProperties(final Context context)
    {
        synchronized (this)
        {
            if (cachedProperties_ == null)
            {
                cachedProperties_ = lookupProperties(context);
            }
        }

        return cachedProperties_;
    }


    /**
     * Determines whether or not an updated set of connection properties are
     * available.
     *
     * @param context The operating context.
     * @return True if an updated set of connection properties are available;
     * false otherwise.
     */
    public boolean updateAvailable(final Context context)
    {
        final CorbaClientProperty cachedProperties = cachedProperties_;

        if (cachedProperties == null)
        {
            return true;
        }

        final CorbaClientProperty currentProperties = lookupProperties(context);

        return !currentProperties.equals(cachedProperties);
    }


    /**
     * Discards any currently cached connection properties and retrieves the
     * current properties from the CorbaClientProperty Home.
     *
     * @param context The operating context.
     * @return The CORBA connection properties.
     */
    public CorbaClientProperty refreshProperties(final Context context)
    {
        synchronized (this)
        {
            cachedProperties_ = null;
        }

        return getProperties(context);
    }


    /**
     * Looks-up the current CORBA connection properties in the
     * CorbaClientProperty Home.
     *
     * @param context The operating context.
     * @return The CORBA connection properties.
     */
    protected CorbaClientProperty lookupProperties(final Context context)
    {
        final Home home = (Home)context.get(CorbaClientPropertyHome.class);
        final EQ key = new EQ(CorbaClientPropertyXInfo.KEY, propertiesKey_);

        CorbaClientProperty properties = null;

        if (home == null)
        {
            new MajorLogMsg(this, "Failed to locate CORBA properties Home ", null).log(context);
        }
        else
        {
            try
            {
                properties = (CorbaClientProperty)home.find(context, key);
            }
            catch (final HomeException exception)
            {
                new MajorLogMsg(this, "Failed to locate CORBA properties for " + propertiesKey_,
                        exception).log(context);
            }
        }

        return properties;
    }


    /**
     * The name of the key used to look-up the connection properties in the
     * CorbaClientProperty Home.
     */
    private final String propertiesKey_;

    /**
     * The currently cached connection properties.
     */
    private CorbaClientProperty cachedProperties_;
}
