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

import com.trilogy.framework.xhome.beans.MetaBeanException;
import com.trilogy.framework.xhome.beans.SafetyUtil;
import com.trilogy.framework.xhome.beans.xi.PropertyInfo;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.webcontrol.AbstractWebControl;
import com.trilogy.framework.xhome.webcontrol.OutputWebControl;
import com.trilogy.framework.xlog.log.DebugLogMsg;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.support.AccountSupport;


/**
 * Web control for subscriber MSISDN selection.
 *
 * @author cindy.wong@redknee.com
 * @since Aug 22, 2007
 */
public class SubscriberAvailableMsisdnWebControl extends AvailMsisdnWebControl
{

    /**
     * Create a new instance of <code>SubscriberAvailableMsisdnWebControl</code>.
     *
     * @param groupProperty
     *            The field containing the MSISDN group.
     * @param msisdnProperty
     *            The field containing the MSISDN.
     */
    public SubscriberAvailableMsisdnWebControl(final PropertyInfo groupProperty, final PropertyInfo msisdnProperty)
    {
        super();
        setMsisdnProperty(msisdnProperty);
        setGroupProperty(groupProperty);
    }


    /**
     * Create a new instance of <code>SubscriberAvailableMsisdnWebControl</code>.
     *
     * @param groupProperty
     *            The field containing the MSISDN group.
     * @param msisdnProperty
     *            The field containing the MSISDN.
     * @param isOptional
     *            Whether the MSISDN is optional.
     */
    public SubscriberAvailableMsisdnWebControl(final PropertyInfo groupProperty, final PropertyInfo msisdnProperty,
        final boolean isOptional)
    {
        super(isOptional);
        setMsisdnProperty(msisdnProperty);
        setGroupProperty(groupProperty);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void toWeb(final Context ctx, final PrintWriter out, final String name, final Object obj)
    {
        final Context subContext = ctx.createSubContext();
        subContext.setName(this.getClass().getSimpleName());
        final Subscriber subscriber = super.lookupSubscriber(subContext);

        Integer group = null;
        String msisdn = null;
        try
        {
            group = (Integer) subscriber.get(ctx, this.groupProperty_.getName());
            msisdn = (String) subscriber.get(ctx, this.msisdnProperty_.getName());
        }
        catch (final MetaBeanException exception)
        {
            new DebugLogMsg(this, MetaBeanException.class.getSimpleName()
                + " occurred in SubscriberAvailableMsisdnWebControl.toWeb(): " + exception.getMessage(), exception)
                .log(ctx);
        }
        if (group != null)
        {
            subContext.put(AvailMsisdnWebControl.MSISDN_GROUP_KEY, group);
        }
        if (msisdn != null)
        {
            subContext.put(AvailMsisdnWebControl.MSISDN_KEY, msisdn);
            subContext.put(AvailMsisdnWebControl.ORIGINAL_MSISDN_KEY, msisdn);
        }

        final boolean readOnly = isMsisdnReadOnly(ctx, subscriber, msisdn);
        if (readOnly)
        {
            subContext.put("MODE", OutputWebControl.DISPLAY_MODE);
        }
        super.toWeb(subContext, out, name, obj);
    }


    /**
     * Whether the MSISDN is read-only.
     *
     * @param ctx
     *            The operating context.
     * @param subscriber
     *            Subscriber being displayed.
     * @param msisdn
     *            Currently selected MSISDN.
     * @return Returns <code>true</code> if the field should be read-only (i.e., not
     *         editable), <code>false</code> otherwise.
     */
    protected boolean isMsisdnReadOnly(final Context ctx, final Subscriber subscriber, final String msisdn)
    {
        boolean result = false;
        if(subscriber.isPooled(ctx))
        {
            String groupMsisdn = subscriber.getGroupMSISDN(ctx);
            if (groupMsisdn != null)
            {
                groupMsisdn = groupMsisdn.trim();
                if (groupMsisdn.length() > 0 && SafetyUtil.safeEquals(groupMsisdn, msisdn))
                {
                    result = true;
                }
            }
        }
        return result;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized Object fromWeb(final Context ctx, final ServletRequest req, final String name)
    {
        final Context subContext = ctx.createSubContext();
        subContext.setName(this.getClass().getSimpleName());
        final String msisdn = (String) super.fromWeb(subContext, req, name);
        final Subscriber subscriber = (Subscriber) ctx.get(AbstractWebControl.BEAN);

        final Integer msisdnGroup = (Integer) subContext.get(AvailMsisdnWebControl.MSISDN_GROUP_KEY, Integer.valueOf(
            AvailMsisdnWebControl.CUSTOM_GROUP));
        this.groupProperty_.set(subscriber, msisdnGroup);
        this.msisdnProperty_.set(subscriber, subContext.get(AvailMsisdnWebControl.MSISDN_KEY, msisdn));
        return msisdn;
    }


    /**
     * Retrieves the value of <code>msisdnProperty</code>.
     *
     * @return The value of <code>msisdnProperty</code>.
     */
    protected PropertyInfo getMsisdnProperty()
    {
        return this.msisdnProperty_;
    }


    /**
     * Sets the value of <code>msisdnProperty</code>.
     *
     * @param msisdnProperty
     *            The value of <code>msisdnProperty</code> to set.
     */
    protected void setMsisdnProperty(final PropertyInfo msisdnProperty)
    {
        this.msisdnProperty_ = msisdnProperty;
    }


    /**
     * Retrieves the value of <code>groupProperty</code>.
     *
     * @return The value of <code>groupProperty</code>.
     */
    protected PropertyInfo getGroupProperty()
    {
        return this.groupProperty_;
    }


    /**
     * Sets the value of <code>groupProperty</code>.
     *
     * @param groupProperty
     *            The value of <code>groupProperty</code> to set.
     */
    protected void setGroupProperty(final PropertyInfo groupProperty)
    {
        this.groupProperty_ = groupProperty;
    }

    /**
     * Field containing MSISDN.
     */
    private PropertyInfo msisdnProperty_;
    /**
     * Field containing MSISDN group.
     */
    private PropertyInfo groupProperty_;
}
