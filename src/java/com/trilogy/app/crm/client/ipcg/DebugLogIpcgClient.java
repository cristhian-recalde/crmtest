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

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;

import com.trilogy.app.crm.bean.Subscriber;

// TODO - 2006-05-12 - Remove the dependency on CORBA code.
import com.trilogy.product.s5600.ipcg.rating.provisioning.RatePlan;


/**
 * Provides a simple proxy class for the IpcgClient that creates DebugLog
 * messages for all calls.
 *
 * @author gary.anderson@redknee.com
 */
public
class DebugLogIpcgClient
    extends IpcgClientProxy
{
    /**
     * Creates a new DebugLogIpcgClient.
     *
     * @param delegate The IpcgClient to which this proxy delegates.
     */
    public DebugLogIpcgClient(final IpcgClient delegate)
    {
        super(delegate);
    }


    /**
     * {@inheritDoc}
     */
    public RatePlan[] getAllRatePlans(final Context context)
        throws IpcgRatingProvException
    {
        if (!LogSupport.isDebugEnabled(context))
        {
            return super.getAllRatePlans(context);
        }

        debugIn(context, "getAllRatePlans", "");

        try
        {
            final RatePlan[] result = super.getAllRatePlans(context);

            final StringBuilder buffer = new StringBuilder();
            buffer.append("Results: {");

            for (int n = 0; n < result.length; ++n)
            {
                if (n != 0)
                {
                    buffer.append(", ");
                }

                final RatePlan plan = result[n];

                buffer.append("[spId: ");
                buffer.append(plan.spId);
                buffer.append(", rpId: ");
                buffer.append(plan.rpId);
                buffer.append(", description: ");
                buffer.append(plan.description);
                buffer.append("]");
            }

            buffer.append("}");

            debugResult(context, "getAllRatePlans", buffer.toString());

            return result;
        }
        catch (final IpcgRatingProvException exception)
        {
            debugException(context, "getAllRatePlans", exception);
            throw exception;
        }
        catch (final RuntimeException exception)
        {
            debugException(context, "getAllRatePlans", exception);
            throw exception;
        }
    }


    /**
     * {@inheritDoc}
     */
    public int addSub(
        final Context context,
        final Subscriber subscriber,
        final short billingCycleDate,
        final String timeZone,
        final int ratePlan,
        final int scpId,
        final boolean subBasedRatingEnabled,
        final int serviceGrade)
        throws IpcgSubProvException
    {
        if (!LogSupport.isDebugEnabled(context))
        {
            return super.addSub(
                context,
                subscriber,
                billingCycleDate,
                timeZone,
                ratePlan,
                scpId,
                subBasedRatingEnabled,
                serviceGrade);
        }

        final String message =
            debugSubscriber(subscriber)
            + ", billingCycleDate: " + billingCycleDate
            + ", timeZone: " + timeZone
            + ", ratePlan: " + ratePlan
            + ", scpId: " + scpId
            + ", subBasedRatingEnabled: " + subBasedRatingEnabled
            + ", serviceGrade: " + serviceGrade;

        debugIn(context, "addSub", message);

        try
        {
            final int result = super.addSub(
                context,
                subscriber,
                billingCycleDate,
                timeZone,
                ratePlan,
                scpId,
                subBasedRatingEnabled,
                serviceGrade);

            debugResult(context, "addSub", "Result: " + result);

            return result;
        }
        catch (final IpcgSubProvException exception)
        {
            debugException(context, "addSub", exception);
            throw exception;
        }
        catch (final RuntimeException exception)
        {
            debugException(context, "addSub", exception);
            throw exception;
        }
    }


    /**
     * {@inheritDoc}
     */
    public int addChangeSub(
        final Context context,
        final Subscriber subscriber,
        final short billingCycleDate,
        final int ratePlan,
        final int serviceGrade)
        throws IpcgSubProvException
    {
        if (!LogSupport.isDebugEnabled(context))
        {
            return super.addChangeSub(context, subscriber, billingCycleDate, ratePlan, serviceGrade);
        }

        final String message =
            debugSubscriber(subscriber)
            + ", ratePlan: " + ratePlan
            + ", serviceGrade: " + serviceGrade;

        debugIn(context, "addChangeSub", message);

        try
        {
            final int result = super.addChangeSub(context, subscriber, billingCycleDate, ratePlan, serviceGrade);

            debugResult(context, "", "Result: " + result);

            return result;
        }
        catch (final IpcgSubProvException exception)
        {
            debugException(context, "addChangeSub", exception);
            throw exception;
        }
        catch (final RuntimeException exception)
        {
            debugException(context, "addChangeSub", exception);
            throw exception;
        }
    }


    /**
     * {@inheritDoc}
     */
    public int addChangeSubBillCycleDate(
        final Context context,
        final Subscriber subscriber,
        final short billCycleDate)
        throws IpcgSubProvException
    {
        if (!LogSupport.isDebugEnabled(context))
        {
            return super.addChangeSubBillCycleDate(
                context,
                subscriber,
                billCycleDate);
        }

        final String message =
            debugSubscriber(subscriber)
            + ", billCycleDate: " + billCycleDate;

        debugIn(context, "addChangeSubBillCycleDate", message);

        try
        {
            final int result = super.addChangeSubBillCycleDate(
                context,
                subscriber,
                billCycleDate);

            debugResult(context, "addChangeSubBillCycleDate", "Result: " + result);

            return result;
        }
        catch (final IpcgSubProvException exception)
        {
            debugException(context, "addChangeSubBillCycleDate", exception);
            throw exception;
        }
        catch (final RuntimeException exception)
        {
            debugException(context, "addChangeSubBillCycleDate", exception);
            throw exception;
        }
    }


    /**
     * {@inheritDoc}
     */
    public int setSubscriberEnabled(
        final Context context,
        final Subscriber subscriber,
        final boolean enabled)
        throws IpcgSubProvException
    {
        if (!LogSupport.isDebugEnabled(context))
        {
            return super.setSubscriberEnabled(context, subscriber, enabled);
        }

        final String message =
            debugSubscriber(subscriber)
            + ", enabled: " + enabled;

        debugIn(context, "setSubscriberEnabled", message);

        try
        {
            final int result = super.setSubscriberEnabled(context, subscriber, enabled);

            debugResult(context, "setSubscriberEnabled", "Result: " + result);

            return result;
        }
        catch (final IpcgSubProvException exception)
        {
            debugException(context, "setSubscriberEnabled", exception);
            throw exception;
        }
        catch (final RuntimeException exception)
        {
            debugException(context, "setSubscriberEnabled", exception);
            throw exception;
        }
    }


    /**
     * {@inheritDoc}
     */
    public boolean isSubscriberProfileAvailable(
        final Context context,
        final Subscriber subscriber)
        throws IpcgSubProvException
    {
        if (!LogSupport.isDebugEnabled(context))
        {
            return super.isSubscriberProfileAvailable(context, subscriber);
        }

        final String message = debugSubscriber(subscriber);

        debugIn(context, "isSubscriberProfileAvailable", message);

        try
        {
            final boolean result = super.isSubscriberProfileAvailable(context, subscriber);

            debugResult(context, "isSubscriberProfileAvailable", "Result: " + result);

            return result;
        }
        catch (final IpcgSubProvException exception)
        {
            debugException(context, "isSubscriberProfileAvailable", exception);
            throw exception;
        }
        catch (final RuntimeException exception)
        {
            debugException(context, "isSubscriberProfileAvailable", exception);
            throw exception;
        }
    }


    /**
     * {@inheritDoc}
     */
    public int removeSubscriber(final Context context, final Subscriber subscriber)
        throws IpcgSubProvException
    {
        if (!LogSupport.isDebugEnabled(context))
        {
            return super.removeSubscriber(context, subscriber);
        }

        debugIn(context, "removeSubscriber", debugSubscriber(subscriber));

        try
        {
            final int result = super.removeSubscriber(context, subscriber);

            debugResult(context, "removeSubscriber", "Result: " + result);

            return result;
        }
        catch (final IpcgSubProvException exception)
        {
            debugException(context, "removeSubscriber", exception);
            throw exception;
        }
        catch (final RuntimeException exception)
        {
            debugException(context, "removeSubscriber", exception);
            throw exception;
        }
    }


    /**
     * Provides a convenient method of creating a log message for entry into w
     * method.
     *
     * @param context The operating context.
     * @param method The name of the method.
     * @param message A simple message desribing the input to the method
     * (excluding the context).
     */
    private void debugIn(
        final Context context,
        final String method,
        final String message)
    {
        final String fullMessage =
            method + "("
            + "Context: " + context.getName()
            + message
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
     */
    private String debugSubscriber(final Subscriber subscriber)
    {
        final String message =
            "Subscriber[ID: " + subscriber.getId()
            + ", MSISDN: " + subscriber.getMSISDN() + "]";

        return message;
    }


} // class
