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

import com.trilogy.app.crm.client.urcs.UrcsClientInstall;
import com.trilogy.app.crm.support.IpcgClientSupport;
import com.trilogy.app.crm.support.SystemStatusSupportHelper;
import com.trilogy.app.crm.technology.TechnologyEnum;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.InfoLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MajorLogMsg;
import com.trilogy.product.s5600.ipcg.provisioning.SubscriberProv;


/**
 * Provides a factory for creating IpcgClients.
 *
 * @author gary.anderson@redknee.com
 */
public final
class IpcgClientFactory
{
    /**
     * Discourage instantiation of this class.
     */
    private IpcgClientFactory()
    {
        // Empty
    }


    /**
     * Installs an IpcgClient into the context with IpcgClient.class as the key.
     *
     * @param context The operating context into which the client is installed.
     */
    public static void installIpcgClient(final Context context)
    {
        info(context, "Installing IPCG clients.");

        try
        {
            installIpcgClient(context, null);
            installIpcgClient(context, CDMA_SUFFIX);

            info(context, "Installation of IPCG clients completed without exception.");
        }
        catch (final RuntimeException exception)
        {
            major(context, "Installtion of IPCG caused an exception to be thrown.", exception);
            throw exception;
        }
    }


    /**
     * Locates the appropriate IPCG client for the given technology type.
     *
     * @param context The operating context.
     * @param technology The type of technology being used.
     *
     * @return The appropriate IPCG client for the given technology type.
     */
    public static IpcgClient locateClient(
        final Context ctx,
        final TechnologyEnum technology)
    {
        Context subContext = ctx.createSubContext();
        debug(subContext, "Locating client for technology: " + technology);

        final Object key;

        if (TechnologyEnum.CDMA == technology)
        {
            key = generateContextKey(IpcgClient.class, CDMA_SUFFIX);
        }
        else
        {
            key = generateContextKey(IpcgClient.class, null);
        }

        debug(subContext, "Key for look-up: " + key);

        final IpcgClient client = (IpcgClient)subContext.get(key);

        debug(subContext, "Client obtained from the context: " + client);

        return client;
    }


    /**
     * Installs an IpcgClient into the context with IpcgClient.class as the key.
     *
     * @param context The operating context into which the client is installed.
     * @param suffix An optional suffix for distinguishing between multiple
     * clients.
     */
    private static void installIpcgClient(final Context context, final String suffix)
    {
        info(context, "Creating IPCG client with suffix: " + suffix);

        try
        {
            final Class productS5600Key;
            
            if (suffix != null && CDMA_SUFFIX.equals(suffix))
            {
                productS5600Key = UrcsClientInstall.PRODUCT_S5600_IPCG_CDMA_CLIENT_KEY;
            }
            else
            {
                productS5600Key = UrcsClientInstall.PRODUCT_S5600_IPCG_CLIENT_KEY;
            }
            
            final UrcsDataRatingProvClient ratePlanClient = 
            	createAndInstallUrcsDataRatingProvClient(context, suffix);

            info(context, "Creating client facade.");
            IpcgClient client =
                new IpcgClientFacade(
                    (Class<ProductS5600IpcgClient>) productS5600Key,
                    ratePlanClient);

            client = new IpcgTestClientSwitch(client);
            client = new DebugLogIpcgClient(client);

            info(context, "Installing client facade into the operating context: " + context.getName());
            final Object key = generateContextKey(IpcgClient.class, suffix);
            context.put(key, client);

            info(context, "Completed creation of IPCG client with suffix " + suffix + " without exception.");
        }
        catch (final RuntimeException exception)
        {
            major(context, "", exception);
            throw exception;
        }
    }



    /**
     * Creates, installs in to the context, and returns a
     * URCSDataRatingProvClient.
     *
     * @param context The operating context.
     * @param suffix An optional suffix for distinguishing between multiple
     * clients.
     * @return A URCSDataRatingProvClient.
     */
    private static UrcsDataRatingProvClient createAndInstallUrcsDataRatingProvClient(
        final Context context,
        final String suffix)
    {
        info(context, "Creating URCS Data rate plan client.");

        final String propertiesKey =
            generatePropertiesKey(UrcsDataRatingProvClient.class, suffix);

        final UrcsDataRatingProvClient profileClient;
        	
        if(IpcgClientSupport.supportsRetrieveAllRatePlans(context))
        {
        	profileClient = new ProductS5600IpcgRatingProvClient(context, propertiesKey);
        }
        else //if (supportsQueryRatePlans(context))
        {
        	profileClient = new ProductS5600IprcRatingProvClient(context, propertiesKey);
        }
            
        final Object contextKey =
            generateContextKey(UrcsDataRatingProvClient.class, suffix);

        context.put(contextKey, profileClient);

        SystemStatusSupportHelper.get(context).registerExternalService(context, profileClient);

        return profileClient;
    }




    /**
     * Generates a Context key for the given object Class.  If the suffix is blank
     * then the key is simply the Class of the object.  If the suffix is
     * non-blank, then the key is a String of the class name with the suffix
     * appended.
     *
     * @param clientClass The Class of the object for which to create a key.
     * @param suffix The optional suffix to append to the key.
     * @return A context key for the given object.
     */
    static Object generateContextKey(
        final Class clientClass,
        final String suffix)
    {
        final Object key;

        if (suffix == null || suffix.trim().length() == 0)
        {
            key = clientClass;
        }
        else
        {
            key = clientClass.getName() + suffix.trim();
        }

        return key;
    }


    /**
     * Generates a Properties key for the given Class.  If the suffix is blank
     * then the key is simply the unqualified Class name.  This is meant to
     * provide backwards compatability with the old naming conventions.  If the
     * suffix is non-blank, then the key is a String of the same name but with
     * the suffix appended.
     *
     * @param clientClass The object for which to create a key.
     * @param suffix The optional suffix to append to the key.
     * @return A properties key for the given object.
     */
    static String generatePropertiesKey(
        final Class clientClass,
        final String suffix)
    {
        final String qualifiedClassName = clientClass.getName();
        final int lastDot = qualifiedClassName.lastIndexOf('.');
        final String unqualifedClassName = qualifiedClassName.substring(lastDot + 1);

        final String key;

        if (suffix == null || suffix.trim().length() == 0)
        {
            key = unqualifedClassName;
        }
        else
        {
            key = unqualifedClassName + suffix;
        }

        return key;
    }


    /**
     * Utility method for generating Major-level messages.
     *
     * @param context The operating context.
     * @param message The message to include in the major message.
     * @param throwable An optional Throwable that was caught.
     */
    private static void major(
        final Context context,
        final String message,
        final Throwable throwable)
    {
        new MajorLogMsg(IpcgClientFactory.class.getName(), message, throwable).log(context);
    }


    /**
     * Utility method for generating Info-level messages.
     *
     * @param context The operating context.
     * @param message The message to include in the info message.
     */
    private static void info(
        final Context context,
        final String message)
    {
        new InfoLogMsg(IpcgClientFactory.class.getName(), message, null).log(context);
    }


    /**
     * Utility method for generating Debug-level messages.
     *
     * @param context The operating context.
     * @param message The message to include in the debug message.
     */
    private static void debug(
        final Context context,
        final String message)
    {
        if (LogSupport.isDebugEnabled(context))
        {
            new DebugLogMsg(IpcgClientFactory.class.getName(), message, null).log(context);
        }
    }


    /**
     * Provides a suffix for use in distinguishing between the CDMA specific
     * IPCG client and any other IPCG.
     */
    static final String CDMA_SUFFIX = ".CDMA";


} // class
