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
package com.trilogy.app.crm.web.control;

import java.io.PrintWriter;

import javax.servlet.ServletRequest;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.webcontrol.AbstractWebControl;
import com.trilogy.framework.xhome.webcontrol.OutputWebControl;
import com.trilogy.framework.xlog.log.MinorLogMsg;

import com.trilogy.app.crm.bean.GroupCallTimeUsageView;
import com.trilogy.app.crm.bean.GroupCallTimeUsageViewWebControl;
import com.trilogy.app.crm.bean.IndividualCallTimeUsageView;
import com.trilogy.app.crm.bean.IndividualCallTimeUsageViewWebControl;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberUsage;


/**
 * Provides a custom web control for the presentation of the subscriber's voice
 * usage information.
 *
 * @author gary.anderson@redknee.com
 */
public
class SubscriberUsageCustomWebControl
    extends AbstractWebControl
{
    /**
     * Prevent external instantiation.  Use the {@link #getInstance} method to
     * get an instance of this class.
     */
    protected SubscriberUsageCustomWebControl()
    {
        // Empty.
    }


    /**
     * Gets the singleton instance of this class.
     *
     * @return The singleton instance of this class.
     */
    public static SubscriberUsageCustomWebControl getInstance()
    {
        return INSTANCE;
    }


    /**
     * {@inheritDoc}
     */
    public Object fromWeb(
        final Context context,
        final ServletRequest request,
        final String name)
    {
        // This web control is only ever used as a read-only control, and
        // therefore does not need to read values back in from the web.
        return null;
    }


    /**
     * {@inheritDoc}
     */
    public void fromWeb(
        final Context context,
        final Object obj,
        final ServletRequest request,
        final String name)
    {
        // This web control is only ever used as a read-only control, and
        // therefore does not need to read values back in from the web.
    }


    /**
     * {@inheritDoc}
     */
    public void toWeb(
        final Context context,
        final PrintWriter out,
        final String name,
        final Object obj)
    {
        final Context readOnlyContext = getReadOnlyContext(context);
        final Subscriber subscriber = (Subscriber)readOnlyContext.get(BEAN);
        final SubscriberUsage usage = (SubscriberUsage)obj;

        showSubscriberView(readOnlyContext, out, name, usage);
        showGroupView(readOnlyContext, out, name, subscriber);
    }


    /**
     * Returns a context that indicates web controls should be drawn in
     * read-only mode.
     *
     * @param context The operating context.
     * @return A context that indicates web controls should be drawn in
     * read-only mode.
     */
    private Context getReadOnlyContext(final Context context)
    {
        final Context readOnlyContext;

        if (context.getInt("MODE", EDIT_MODE) != DISPLAY_MODE)
        {
            final Context subContext = context.createSubContext();
            subContext.put("MODE", DISPLAY_MODE);
            readOnlyContext = subContext;
        }
        else
        {
            readOnlyContext = context;
        }

        return readOnlyContext;
    }


    /**
     * Shows the subscriber's group usage information if the subscriber is in a
     * group-pooled account.
     *
     * @param context The operating context.
     * @param out The PrintWriter required by the web control.
     * @param name The name of the property.
     * @param subscriber The subscriber for which to show any group usage
     * information.
     */
    private void showGroupView(
        final Context context,
        final PrintWriter out,
        final String name,
        final Subscriber subscriber)
    {
        if (subscriber.isPooled(context))
        {
            out.println("<br/>");

            try
            {
                final Subscriber groupSubscriber = subscriber.getPoolSubscription(context); 
                if (groupSubscriber == null)
                {
                    return;
                }

                // FCT no longer supported
                final SubscriberUsage usage = new SubscriberUsage();

                final GroupCallTimeUsageView view = new GroupCallTimeUsageView();
                view.setSubscriberUsage(usage);
                view.setFreeCallTime(null);

                GROUP_VIEW.toWeb(context, out, name, view);
            }
            catch (final HomeException exception)
            {
                new MinorLogMsg(
                        this,
                        "Failed to display realtime group-usage information.",
                        exception).log(context);
            }
        }
    }


    /**
     * Shows the subscriber's specific usage information.
     *
     * @param context The operating context.
     * @param out The PrintWriter required by the web control.
     * @param name The name of the property.
     * @param usage The subscriber usage information to display.
     */
    private void showSubscriberView(
        final Context context,
        final PrintWriter out,
        final String name,
        final SubscriberUsage usage)
    {
        final IndividualCallTimeUsageView view = new IndividualCallTimeUsageView();
        view.setSubscriberUsage(usage);

        SUBSCRIBER_VIEW.toWeb(context, out, name, view);
    }


    /**
     * The singleton instance of this class.
     */
    private static final SubscriberUsageCustomWebControl INSTANCE =
        new SubscriberUsageCustomWebControl();

    /**
     * A reusable web control for displaying subscriber specific usage.
     */
    private static final OutputWebControl SUBSCRIBER_VIEW =
        new IndividualCallTimeUsageViewWebControl();

    /**
     * A reusable web control for displaying subscriber group usage.
     */
    private static final OutputWebControl GROUP_VIEW =
        new GroupCallTimeUsageViewWebControl();

} // class
