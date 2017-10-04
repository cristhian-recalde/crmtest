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

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgent;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.In;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.visitor.CountingVisitor;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;

import com.trilogy.app.crm.bean.ServiceHome;
import com.trilogy.app.crm.bean.ServiceTypeEnum;
import com.trilogy.app.crm.bean.ServiceXInfo;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.core.PricePlanVersion;


/**
 * Provides a ContextAgent for unprovisioning EVDO services.
 *
 * @author gary.anderson@redknee.com
 */
public
class EVDOUnprovisionAgent
    implements ContextAgent
{
    /**
     * {@inheritDoc}
     *
     * The EVDO provisioning is actually a combination of provisioning to AAA
     * and to IPCG.
     *
     * This execute depends upon a Subscriber in the context with the key
     * Subscriber.class, and a Service in the context with the key
     * Service.class.
     */
    public void execute(final Context context)
        throws AgentException
    {
        new AAAUnprovisionAgent().execute(context);

        // We should only unprovision IPCG if the subscriber does not have a
        // separate Data service in their price plan.
        if (!isSeparateDataServiceSelected(context))
        {
            new IPCUnprovisionAgent().execute(context);
        }
    }


    /**
     * Checks to see if the subscriber being processed has a separate Data
     * service in their PricePlan.
     *
     * @param context The operating context, which is expected to contain a
     * Subscriber keyed by the Subscriber class.
     *
     * @return True if the subscriber has a separate Data service in their
     * PricePlan.
     *
     * @exception AgentException Thrown if the there are any problems accessing
     * Home data in the context.
     */
    boolean isSeparateDataServiceSelected(final Context context)
        throws AgentException
    {
        try
        {
            final Subscriber subscriber = getSubscriber(context);

            debug(context, "isSeparateDataServiceSelected()", subscriber);
            
            final PricePlanVersion plan = subscriber.getPricePlan(context);
            final Map serviceFees = plan.getServiceFees(context);
            final Set serviceIdentifiers = serviceFees.keySet();

            debug(context, "Service identifiers: ", serviceIdentifiers);

            final And seachCriteria = new And();
            seachCriteria.add(new In(ServiceXInfo.ID, serviceIdentifiers));
            seachCriteria.add(new EQ(ServiceXInfo.TYPE, ServiceTypeEnum.DATA));

            Home serviceHome = (Home)context.get(ServiceHome.class);
            serviceHome = serviceHome.where(context, seachCriteria);

            final CountingVisitor counter =
                (CountingVisitor)serviceHome.forEach(context, new CountingVisitor());

            return counter.getCount() > 0;
        }
        catch (final HomeException exception)
        {
            throw new AgentException(
                "Unanticipated exception encounterred while examining subscriber's services.",
                exception);
        }
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
     * Provides a convenient method of generating DEBUG log messages for this
     * class.
     *
     * @param context The operating context.
     * @param message The message of the log.
     * @param subscriber The Subscriber to which this message applies.
     */
    private void debug(
        final Context context,
        final String message,
        final Subscriber subscriber)
    {
        if (LogSupport.isDebugEnabled(context))
        {
            final String fullMessage =
                message
                + " -- "
                + debugSubscriber(subscriber);

            debug(context, fullMessage);
        }
    }


    /**
     * Provides a convenient method of generating DEBUG log messages for this
     * class.
     *
     * @param context The operating context.
     * @param message The message of the log.
     * @param items A generic collection of items to include in the message.
     */
    private void debug(
        final Context context,
        final String message,
        final Collection items)
    {
        if (LogSupport.isDebugEnabled(context))
        {
            final StringBuilder fullMessage = new StringBuilder();
            fullMessage.append(message);
            fullMessage.append(" {");

            final Iterator itemIterator = items.iterator();
            while (itemIterator.hasNext())
            {
                final Object item = itemIterator.next();
                if (item instanceof Map.Entry)
                {
                    final Map.Entry entry = (Map.Entry)item;
                    fullMessage.append("[");
                    fullMessage.append(entry.getKey().toString());
                    fullMessage.append(" -> ");
                    fullMessage.append(entry.getValue().toString());
                    fullMessage.append("]");
                }
                else
                {
                    fullMessage.append(item.toString());
                }

                if (itemIterator.hasNext())
                {
                    fullMessage.append(", ");
                }
            }
            
            fullMessage.append("}");

            debug(context, fullMessage.toString());
        }
    }


    /**
     * Provides a convenient method of generating DEBUG log messages for this
     * class.
     *
     * @param context The operating context.
     * @param message The message of the log.
     */
    private void debug(final Context context, final String message)
    {
        if (LogSupport.isDebugEnabled(context))
        {
            new DebugLogMsg(this, message, null).log(context);
        }
    }
    

    /**
     * Provides a convenient method of creating a debug description of a
     * Subsciber.  Currently, the description includes at least the
     * Subscriber.id and the Subscriber.MSISDN.
     *
     * @param subscriber The subscriber for which to generate a debug
     * description.
     * @return A debug description of a Subsciber.
     */
    private String debugSubscriber(final Subscriber subscriber)
    {
        final String message =
            "Subscriber[ID: " + subscriber.getId()
            + ", MSISDN: " + subscriber.getMSISDN() + "]";

        return message;
    }


    
} // class
