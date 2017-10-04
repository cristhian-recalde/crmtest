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

package com.trilogy.app.crm.util;

import java.io.IOException;
import java.net.InetAddress;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xlog.log.MinorLogMsg;

/**
 * Provides some utilities for dealing with the network.
 *
 * @author gary.anderson@redknee.com
 */
public final
class NetworkUtil
{
    /**
     * Prevent accidental instantiation.
     */
    private NetworkUtil()
    {
        // Empty
    }


    /**
     * Convers an InetAddress to a string for use in log messages.
     *
     * @param context The operating context.
     * @param address The address to convert to a string.
     * @return The addess as a string.
     */
    public static String toString(final Context context, final InetAddress address)
    {
        final StringBuilder builder = new StringBuilder();

        try
        {
            toString(context, address, builder);
        }
        catch (final IOException exception)
        {
            builder.append(" -- Failure while generating description: ");
            builder.append(exception.getMessage());

            new MinorLogMsg(
                NetworkUtil.class.getName(),
                "Failure while generating InetAddress description.",
                exception).log(context);
        }

        return builder.toString();
    }


    /**
     * Convers an InetAddress to a string for use in log messages.
     *
     * @param context The operating context.
     * @param address The address to convert to a string.
     * @param message The message to which the address information is appended.
     *
     * @throws IOException Thrown if there are problems writing to the message.
     */
    public static void toString(
        final Context context,
        final InetAddress address,
        final Appendable message)
        throws IOException
    {
        message.append("[Host Name: ");
        message.append(address.getHostName());
        message.append(", Canonical Host Name: ");
        message.append(address.getCanonicalHostName());
        message.append(", Host Address: ");
        message.append(address.getHostAddress());
        message.append("]");
    }
}
