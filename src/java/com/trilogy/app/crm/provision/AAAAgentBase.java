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
package com.trilogy.app.crm.provision;

import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgent;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;

import com.trilogy.app.crm.bean.core.Service;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.client.aaa.AAAClient;
import com.trilogy.app.crm.client.aaa.AAAClientFactory;


/**
 * Provides a ContextAgent base class for provisioning and unprovisioning AAA
 * services.
 *
 * @author gary.anderson@redknee.com
 */
public abstract
class AAAAgentBase
    implements ContextAgent
{
    /**
     * {@inheritDoc}
     *
     * This execute depends upon a Subscriber in the context with the key
     * Subscriber.class, and a Service in the context with the key
     * Service.class.
     */
    public void execute(final Context context)
        throws AgentException
    {
        final AAAClient client = getClient(context);
        final Service service = getService(context);
        final Subscriber subscriber = getSubscriber(context);

        processSubscriber(context, client, service, subscriber);
    }


    /**
     * Processes the Subscriber on the given Service.
     *
     * @param context The operating context.
     * @param client The AAAClient used for provisioning.
     * @param service The service that prompted this provisioning.
     * @param subscriber The subscriber to provision.
     *
     * @exception AgentException Thrown if any problems are encounterred durring
     * provisioning or unprovisioning.
     */
    protected abstract void processSubscriber(
        final Context context,
        final AAAClient client,
        final Service service,
        final Subscriber subscriber)
        throws AgentException;


    /**
     * Gets the AAAClient provided in the given context.
     *
     * @param context The operating context.
     * @return The AAAClient provided in the given context.
     *
     * @exception AgentException Thrown if the AAAClient cannot be found.
     */
    private AAAClient getClient(final Context context)
        throws AgentException
    {
        final AAAClient client = AAAClientFactory.locateClient(context);
        if (client == null)
        {
            throw new AgentException("Failed to locate the AAAClient for provisioning.");
        }

        return client;
    }


    /**
     * Gets the Service provided in the given context.
     *
     * @param context The operating context.
     * @return The Service provided in the given context.
     *
     * @exception AgentException Thrown if the Service cannot be found.
     */
    private Service getService(final Context context)
        throws AgentException
    {
        final Service service = (Service)context.get(Service.class);
        if (service == null)
        {
            throw new AgentException("Failed to locate the Service to be provisioned.");
        }

        return service;
    }


    /**
     * Gets the Subscriber provided in the given context.
     *
     * @param context The operating context.
     * @return The Subscriber provided in the given context.
     *
     * @exception AgentException Thrown if the Subscriber cannot be found.
     */
    private Subscriber getSubscriber(final Context context)
        throws AgentException
    {
        final Subscriber subscriber = (Subscriber)context.get(Subscriber.class);
        if (subscriber == null)
        {
            throw new AgentException("Failed to locate the Subscriber to be provisioned.");
        }

        return subscriber;
    }


    /**
     * Provides a convenient method of creating a debug description of a
     * Susbciber.  Currently, the description includes at least the
     * Subscriber.id and the Subscriber.MSISDN.
     *
     * @param subscriber The subscriber for which to generate a debug
     * description.
     * @return A debug description of a Subscriber.
     */
    protected String debugSubscriber(final Subscriber subscriber)
    {
        final String message =
            "Subscriber[ID: " + subscriber.getId()
            + ", MSISDN: " + subscriber.getMSISDN() + "]";

        return message;
    }


    /**
     * Provides a convenient method of creating a debug description of a
     * Service.  Currently, the description includes at least the
     * Service.id and the Service.MSISDN.
     *
     * @param service The service for which to generate a debug
     * description.
     * @return A debug description of a Service.
     */
    protected String debugService(final Service service)
    {
        final String message =
            "Service '" + service.getID()
            + " - " + service.getName() + "'";

        return message;
    }


    /**
     * Provides a convenient method of creating a DEBUG log messages.
     *
     * @param context The operating context.
     * @param message The message to display.
     */
    protected void debugLog(final Context context, final String message)
    {
        if (LogSupport.isDebugEnabled(context))
        {
            new DebugLogMsg(this, message, null).log(context);
        }
    }


    /**
     * Provides a convenient method of creating a DEBUG log messages.
     *
     * @param context The operating context.
     * @param message The message to display.
     * @param service The service being provisioned.
     * @param subscriber The subscriber having a service provisioned.
     */
    protected void debugLog(
        final Context context,
        final String message,
        final Service service,
        final Subscriber subscriber)
    {
        if (LogSupport.isDebugEnabled(context))
        {
            final String fullMessage =
                message
                + " with "
                + debugService(service)
                + " and "
                + debugSubscriber(subscriber) + ".";

            debugLog(context, fullMessage);
        }
    }


} // class
