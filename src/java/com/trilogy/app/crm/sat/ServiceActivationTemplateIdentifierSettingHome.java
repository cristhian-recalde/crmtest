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
package com.trilogy.app.crm.sat;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;

import com.trilogy.app.crm.bas.tps.ServiceActivationTemplate;
import com.trilogy.app.crm.bean.IdentifierEnum;
import com.trilogy.app.crm.support.IdentifierSequenceSupportHelper;


/**
 * Sets the unique identifiers used by Service Activation Template(SAT).
 *
 * @author lily.zou@redknee.com
 */
public class ServiceActivationTemplateIdentifierSettingHome extends HomeProxy
{
    /**
     * Creates a new SubscriberAuxiliaryServiceIdentiferHome proxy.
     *
     * @param context The operating context
     * @param delegate The Home to which we delegate.
     */
    public ServiceActivationTemplateIdentifierSettingHome(final Context context, final Home delegate)
    {
        super(context, delegate);
    }

    // INHERIT
    public Object create(final Context ctx, final Object bean) throws HomeException
    {
        final ServiceActivationTemplate srvTemp = (ServiceActivationTemplate) bean;

        // Throws HomeException.
        final long identifier = getNextIdentifier(ctx);

        srvTemp.setIdentifier(identifier);

        return super.create(ctx, srvTemp);
    }

    /**
     * Gets the next available identifier.
     *
     * @return The next available identifier.
     */
    private long getNextIdentifier(final Context ctx) throws HomeException
    {
        IdentifierSequenceSupportHelper.get(ctx).ensureSequenceExists(ctx,
                IdentifierEnum.SERVICE_ACTIVATION_TEMPLATE_ID, 1, Long.MAX_VALUE);

        // TODO - 2004-08-04 - Should provide roll-over function.  The defaults
        // should not require roll over for a very long time, but there is
        // nothing to prevent an admin from changing the next or end values.
        return IdentifierSequenceSupportHelper.get(ctx).getNextIdentifier(ctx,
                IdentifierEnum.SERVICE_ACTIVATION_TEMPLATE_ID, null);
    }

} // class
