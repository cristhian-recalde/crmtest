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
 * Provides a proxy for the AAAClient interface.
 *
 * @author gary.anderson@redknee.com
 */
public
class DebugLogAAAClient
    extends AAAClientProxy
{
    /**
     * Creates a new proxy for the given delegate.
     *
     * @param delegate The AAAClient to which this proxy delegates.
     */
    public DebugLogAAAClient(final AAAClient delegate)
    {
        super(delegate);
    }


    /**
     * {@inheritDoc}
     */
    public void createProfile(final Context context, final Subscriber subscriber)
        throws AAAClientException
    {
        if (!LogSupport.isDebugEnabled(context))
        {
            super.createProfile(context, subscriber);
        }
        else
        {
            debugIn(context, "createProfile", debugSubscriber(subscriber));
            try
            {
                super.createProfile(context, subscriber);
                debugResult(context, "createProfile", "void");
            }
            catch (final AAAClientException exception)
            {
                debugException(context, "createProfile", exception);
                throw exception;
            }
            catch (final RuntimeException exception)
            {
                debugException(context, "createProfile", exception);
                throw exception;
            }
        }
    }


    /**
     * {@inheritDoc}
     */
    public void deleteProfile(final Context context, final Subscriber subscriber)
        throws AAAClientException
    {
        if (!LogSupport.isDebugEnabled(context))
        {
            super.deleteProfile(context, subscriber);
        }
        else
        {
            debugIn(context, "deleteProfile", debugSubscriber(subscriber));
            try
            {
                super.deleteProfile(context, subscriber);
                debugResult(context, "deleteProfile", "void");
            }
            catch (final AAAClientException exception)
            {
                debugException(context, "deleteProfile", exception);
                throw exception;
            }
            catch (final RuntimeException exception)
            {
                debugException(context, "deleteProfile", exception);
                throw exception;
            }
        }
    }


    /**
     * {@inheritDoc}
     */
    public boolean isProfileEnabled(
        final Context context,
        final Subscriber subscriber)
        throws AAAClientException
    {
        if (!LogSupport.isDebugEnabled(context))
        {
            return super.isProfileEnabled(context, subscriber);
        }
        else
        {
            debugIn(context, "isProfileEnabled", debugSubscriber(subscriber));
            try
            {
                final boolean result = super.isProfileEnabled(context, subscriber);
                debugResult(context, "isProfileEnabled", "Result: " + result);
                return result;
            }
            catch (final AAAClientException exception)
            {
                debugException(context, "isProfileEnabled", exception);
                throw exception;
            }
            catch (final RuntimeException exception)
            {
                debugException(context, "isProfileEnabled", exception);
                throw exception;
            }
        }
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
        if (!LogSupport.isDebugEnabled(context))
        {
            super.setProfileEnabled(context, subscriber, enabled);
        }
        else
        {
            debugIn(context, "setProfileEnabled", debugSubscriber(subscriber) + ", Enabled: " + enabled);
            try
            {
                super.setProfileEnabled(context, subscriber, enabled);
                debugResult(context, "setProfileEnabled", "void");
            }
            catch (final AAAClientException exception)
            {
                debugException(context, "setProfileEnabled", exception);
                throw exception;
            }
            catch (final RuntimeException exception)
            {
                debugException(context, "setProfileEnabled", exception);
                throw exception;
            }
        }
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
        if (!LogSupport.isDebugEnabled(context))
        {
            super.updateProfile(
                context,
                oldSubscriber,
                newSubscriber);
        }
        else
        {
            final String message = "Old" + debugSubscriber(oldSubscriber) + ", New" + debugSubscriber(newSubscriber);
            debugIn(context, "updateProfile", message);
            try
            {
                super.updateProfile(context, oldSubscriber, newSubscriber);
                debugResult(context, "updateProfile", "void");
            }
            catch (final AAAClientException exception)
            {
                debugException(context, "updateProfile", exception);
                throw exception;
            }
            catch (final RuntimeException exception)
            {
                debugException(context, "updateProfile", exception);
                throw exception;
            }
        }
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
        if (!LogSupport.isDebugEnabled(context))
        {
            super.updateProfile(
                context,
                oldPackage,
                newPackage);
        }
        else
        {
            final String message = "Old" + debugPackage(oldPackage) + ", New" + debugPackage(newPackage);
            debugIn(context, "updateProfile", message);
            try
            {
                super.updateProfile(context, oldPackage, newPackage);
                debugResult(context, "updateProfile", "void");
            }
            catch (final AAAClientException exception)
            {
                debugException(context, "updateProfile", exception);
                throw exception;
            }
            catch (final RuntimeException exception)
            {
                debugException(context, "updateProfile", exception);
                throw exception;
            }
        }
    }


    /**
     * Provides a convenient method of creating a log message for entry into a method.
     * 
     * @param context
     *            The operating context.
     * @param method
     *            The name of the method.
     * @param message
     *            A simple message desribing the input to the method (excluding the
     *            context).
     */
    private void debugIn(
        final Context context,
        final String method,
        final String message)
    {
        final String fullMessage =
            method + "("
            + "Context: " + context.getName()
            + ", " + message
            + ")";

        new DebugLogMsg(getDelegate(context), fullMessage, null).log(context);
    }


    /**
     * Provides a convenient method of creating a log message for the result of
     * executing a method.
     *
     * @param context The operating context.
     * @param method The name of the method.
     * @param message A simple message describing the result.
     */
    private void debugResult(
        final Context context,
        final String method,
        final String message)
    {
        final String fullMessage = method + "() returned " + message;

        new DebugLogMsg(getDelegate(context), fullMessage, null).log(context);
     }


    /**
     * Provides a convenient method of creating a log message for an exception
     * thrown from a method.
     *
     * @param context The operating context.
     * @param method The name of the nethod.
     * @param throwable The exception thrown.
     */
    private void debugException(
        final Context context,
        final String method,
        final Throwable throwable)
    {
        final String fullMessage = method + "() threw exception.";

        new DebugLogMsg(getDelegate(context), fullMessage, throwable).log(context);
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


    /**
     * Provides a convenient method of creating a debug description of a
     * Subsciber.  Currently, the description includes at least the
     * Package.id and the Package.MSISDN.
     *
     * @param cardPackage The package for which to generate a debug
     * description.
     * @return A debug description of a Subsciber.
     */
    private String debugPackage(final TDMAPackage cardPackage)
    {
        final String message =
            "TDMAPackage[PackID: " + cardPackage.getPackId()
            + ", MDN: " + cardPackage.getMin()
            + ", ESN: " + cardPackage.getESN() + "]";

        return message;
    }


} // class
