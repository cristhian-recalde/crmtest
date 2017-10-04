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
import com.trilogy.framework.xlog.log.InfoLogMsg;
import com.trilogy.framework.xlog.log.MajorLogMsg;

import com.trilogy.app.crm.support.SystemStatusSupportHelper;


/**
 * Provides a central mechanism for installing and looking-up of AAA clients.
 *
 * @author gary.anderson@redknee.com
 */
public final
class AAAClientFactory
{
    /**
     * Private constructor discourages instantiation.
     */
    private AAAClientFactory()
    {
        // Empty
    }


    /**
     * Installs the AAA client into the given context for future use.
     *
     * @param context The application context.
     */
    public static void installClient(final Context context)
    {
        infoLog(context, "Installing AAA clients.");

        // Currently, there is only one AAA client to install.  This method will
        // become more complex in the future when additional AAA clients are
        // configurable.

        try
        {
            AAAGatewayRMIClient aaaRmiclient = new AAAGatewayRMIClient(context);
            context.put(AAAGatewayRMIClient.class, aaaRmiclient);
            SystemStatusSupportHelper.get(context).registerExternalService(context, aaaRmiclient);

            AAAClient client = new AAATestClientSwitch(aaaRmiclient);
            client = new DebugLogAAAClient(client);

            context.put(DEFAULT_AAA_CLIENT_CONTEXT_KEY, client);

            infoLog(context, "Installation of AAA clients completed without exception.");
        }
        catch (final Throwable throwable)
        {
            majorLog(
                context,
                "Encounterred unanticipated exception during AAA client installation.",
                throwable);
        }
    }


    /**
     * Locates and returns the AAA client.
     *
     * @param context The operating context.
     * @return The AAA client.
     */
    public static AAAClient locateClient(final Context context)
    {
        return (AAAClient)context.get(DEFAULT_AAA_CLIENT_CONTEXT_KEY);
    }


    /**
     * Provides a convenient method of generating an INFO log message.
     *
     * @param context The operating context.
     * @param message The message to include in the log message.
     */
    private static void infoLog(final Context context, final String message)
    {
        new InfoLogMsg(AAAClientFactory.class.getName(), message, null).log(context);
    }


    /**
     * Provides a convenient method of generating an INFO log message.
     *
     * @param context The operating context.
     * @param message The message to include in the log message.
     * @param throwable The exception that prompted this log message.  May be
     * null.
     */
    private static void majorLog(
        final Context context,
        final String message,
        final Throwable throwable)
    {
        new MajorLogMsg(AAAClientFactory.class.getName(), message, throwable).log(context);
    }


    /**
     * Provides the default key to use when getting the AAA client from the
     * context.
     */
    private static final Object DEFAULT_AAA_CLIENT_CONTEXT_KEY = AAAClient.class;


} // class
