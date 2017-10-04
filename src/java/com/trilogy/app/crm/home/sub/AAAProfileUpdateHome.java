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
package com.trilogy.app.crm.home.sub;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xhome.support.IdentitySupport;
import com.trilogy.framework.xlog.log.EntryLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.app.crm.bean.ServiceTypeEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberHome;
import com.trilogy.app.crm.bean.SubscriberIdentitySupport;
import com.trilogy.app.crm.client.aaa.AAAClient;
import com.trilogy.app.crm.client.aaa.AAAClientException;
import com.trilogy.app.crm.client.aaa.AAAClientFactory;
import com.trilogy.app.crm.support.Lookup;
import com.trilogy.app.crm.support.ServiceSupport;


/**
 * Provides a home that watches for and updates the AAA profile of subscribers.
 *
 * @author gary.anderson@redknee.com
 */
class AAAProfileUpdateHome
    extends HomeProxy
{
    /**
     * Creates a new AAAProfileUpdateHome for the given delegate.
     *
     * @param delegate The Home to which this proxy delegates.
     */
    public AAAProfileUpdateHome(final Home delegate)
    {
        super(delegate);
    }


    /**
     * {@inheritDoc}
     */
    public Object store(final Context context, final Object object)
        throws HomeException
    {
    	LogSupport.debug(context, this, "SubscriberPipeline[store].....");
    	final Subscriber oldSubscriber = getOldSubscriber(context, (Subscriber)object);

        final Subscriber newSubscriber = storeCRMProfile(context, oldSubscriber, object);

        storeAAAProfile(context, oldSubscriber, newSubscriber);

        return newSubscriber;
    }


    /**
     * Gets the old Subscriber profile given the new Subscriber profile.
     *
     * @param context The operating context.
     * @param newSubscriber The new Subscriber profile.
     *
     * @return The old version of the Subscriber profile.
     *
     * @exception HomeException Thrown if there are problems accessing the
     * profile data in the Context.
     */
    private Subscriber getOldSubscriber(
        final Context context,
        final Subscriber newSubscriber)
        throws HomeException
    {
        Subscriber oldSubscriber = (Subscriber)context.get(Lookup.OLDSUBSCRIBER);

        if (oldSubscriber == null)
        {
            final Home home = (Home)context.get(SubscriberHome.class);

            final IdentitySupport idHelp = SubscriberIdentitySupport.instance();

            oldSubscriber = (Subscriber)home.find(context, idHelp.ID(newSubscriber));
        }

        if (oldSubscriber == null)
        {
            throw new HomeException(
                "Failed to find old profile for " + debugSubscriber(newSubscriber)
                + " during check for AAA profile update.");
        }

        return oldSubscriber;
    }


    /**
     * Provides a controlled update of the CRM profile.  If any exception is
     * thrown, the attempt to update the AAA profile should be aborted because
     * this method will have generated an Alarm indicating so.
     *
     * @param context The operating context.
     * @param oldSubscriber The old version of the CRM Subscriber profile.
     * @param object The Subscriber bean.
     * @return The new Subscriber profile returned from this proxy's delegate.
     *
     * @exception HomeException Thrown if there are problems accessing Home data
     * in the context.
     */
    private Subscriber storeCRMProfile(
        final Context context,
        final Subscriber oldSubscriber,
        final Object object)
        throws HomeException
    {
        final Subscriber newSubscriber;

        Throwable subscriberUpdateException = null;

        try
        {
            newSubscriber = (Subscriber)super.store(context, object);
        }
        catch (final HomeException exception)
        {
            if (hasAAAProfile(context, oldSubscriber, (Subscriber)object))
            {
                subscriberUpdateException = exception;
            }

            throw exception;
        }
        catch (final RuntimeException exception)
        {
            if (hasAAAProfile(context, oldSubscriber, (Subscriber)object))
            {
                subscriberUpdateException = exception;
            }

            throw exception;
        }
        finally
        {
            if (subscriberUpdateException != null)
            {
                final Subscriber subscriber = (Subscriber)object;

                final String[] parameters =
                    new String[]
                    {
                        subscriber.getId(),
                        subscriber.getMSISDN()
                    };

                new EntryLogMsg(12924, this,
                    "", "", parameters, subscriberUpdateException).log(context);
           }
        }

        return newSubscriber;
    }


    /**
     * Provides a controlled update of the AAA profile.
     *
     * @param context The operating context.
     * @param oldSubscriber The old version of the CRM Subscriber profile.
     * @param newSubscriber The old version of the CRM Subscriber profile.
     *
     * @exception HomeException Thrown if there are any problems accessing Home
     * data or services in the context.
     */
    private void storeAAAProfile(
        final Context context,
        final Subscriber oldSubscriber,
        final Subscriber newSubscriber)
        throws HomeException
    {
        if (hasAAAProfile(context, oldSubscriber, newSubscriber))
        {
            Throwable subscriberUpdateException = null;

            try
            {
                updateAAAProfile(context, oldSubscriber, newSubscriber);
            }
            catch (final AAAClientException exception)
            {
                subscriberUpdateException = exception;
            }
            catch (final RuntimeException exception)
            {
                subscriberUpdateException = exception;
            }

            if (subscriberUpdateException != null)
            {
                final String[] parameters =
                    new String[]
                    {
                        newSubscriber.getId(),
                        newSubscriber.getMSISDN()
                    };

                new EntryLogMsg(12925, this,
                     "", "", parameters, subscriberUpdateException).log(context);

                throw new HomeException(
                    "Failed to update AAA profile for " + debugSubscriber(newSubscriber),
                    subscriberUpdateException);
            }
        }
    }


    /**
     * Determines whether or not the given subscriber has an AAA (EVDO) profile.
     *
     * @param context The operating context.
     * @param oldSubscriber The old CRM subscriber profile.
     * @param newSubscriber The new CRM subscriber profile.
     * @return True if the given subscriber has an AAA profile; false otherwise.
     *
     * @exception HomeException Thrown if there is a problem accessing the Price
     * Plan or Service Home data in the context.
     */
    protected boolean hasAAAProfile(
        final Context context,
        final Subscriber oldSubscriber,
        final Subscriber newSubscriber)
        throws HomeException
    {
        // Both the old and new subscriber must have the EVDO service, otherwise
        // this update is already handled by the provision/unprovision agent, or
        // the susbcriber hasn't had any EVDO service enabled.
        return ServiceSupport.isServiceSelected(context, oldSubscriber, ServiceTypeEnum.EVDO)
            && ServiceSupport.isServiceSelected(context, newSubscriber, ServiceTypeEnum.EVDO);
    }


    /**
     * Updates the AAA profile of the subscriber using the information in the
     * old and new versions of the CRM subscriber profile.
     *
     * @param context The operating context.
     * @param oldSubscriber The old CRM subscriber profile.
     * @param newSubscriber The new CRM subscriber profile.
     *
     * @exception AAAClientException Thrown if there is a problem communicating
     * with the AAA service.
     */
    protected void updateAAAProfile(
        final Context context,
        final Subscriber oldSubscriber,
        final Subscriber newSubscriber)
        throws AAAClientException
    {
        final AAAClient client = AAAClientFactory.locateClient(context);
        client.updateProfile(context, oldSubscriber, newSubscriber);
    }


    /**
     * Provides a convenient method of generating a log-friendly identifier for
     * a subscriber.
     *
     * @param subscriber The subscriber for which to generate an identifier.
     * @return A log-friendly identifier for a subscriber.
     */
    private String debugSubscriber(final Subscriber subscriber)
    {
        return "Subscriber[ID=" + subscriber.getId()
            + ", MSISDN=" + subscriber.getMSISDN()
            + ", Package=" + subscriber.getPackageId() + "]";
    }

} // class
