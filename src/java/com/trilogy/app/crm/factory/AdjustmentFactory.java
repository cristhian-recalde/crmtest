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
package com.trilogy.app.crm.factory;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextFactory;
import com.trilogy.framework.xhome.context.ContextFactoryProxy;

import com.trilogy.app.crm.bean.Adjustment;
import com.trilogy.app.crm.home.TransactionToAdjustmentAdapter;
import com.trilogy.app.crm.support.CalendarSupportHelper;


/**
 * Creates default Adjustment beans.
 *
 * @author gary.anderson@redknee.com
 */
public
class AdjustmentFactory
    extends ContextFactoryProxy
{
    /**
     * Creates a new factory for cloning the prototype referred to with the
     * given key.
     *
     * @param key The key used to locate the prototype in the context.
     */
    public AdjustmentFactory(final Object key)
    {
        this(key instanceof ContextFactory ? (ContextFactory) key : new PrototypeContextFactory(key));
    }
    /**
     * Creates a new factory for cloning the prototype referred to with the
     * given key.
     *
     * @param key The key used to locate the prototype in the context.
     */
    public AdjustmentFactory(final ContextFactory delegate)
    {
        super(delegate);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Object create(final Context context)
    {
        final Adjustment adjustment = (Adjustment)super.create(context);

        final Date now = CalendarSupportHelper.get(context).getRunningDate(context);
		SimpleDateFormat format =
		    new SimpleDateFormat(
		        TransactionToAdjustmentAdapter.DATE_FORMAT_STRING);
		adjustment.setTransDate(format.format(now));

        return adjustment;
    }

} // class
