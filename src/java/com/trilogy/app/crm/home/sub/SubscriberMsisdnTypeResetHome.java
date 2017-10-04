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
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.home.HomeProxy;

import com.trilogy.app.crm.bean.AbstractSubscriber;
import com.trilogy.app.crm.bean.Subscriber;

/**
 * Clears the values in the transient fields so that the propper value will be recalculated
 * after a successfull save.
 *
 * @author victor.stratan@redknee.com
 */
public class SubscriberMsisdnTypeResetHome extends HomeProxy
{
    public SubscriberMsisdnTypeResetHome(final Home delegate)
    {
        super(delegate);
    }

    /**
     * {@inheritDoc}
     */
    public Object store(Context ctx,final Object object)
        throws HomeException
    {
        final Subscriber sub = (Subscriber) super.store(ctx,object);
        sub.setMsisdnEntryType(AbstractSubscriber.DEFAULT_MSISDNENTRYTYPE);
        sub.setFaxMsisdnEntryType(AbstractSubscriber.DEFAULT_FAXMSISDNENTRYTYPE);
        sub.setDataMsisdnEntryType(AbstractSubscriber.DEFAULT_DATAMSISDNENTRYTYPE);
        return sub;
    }

    public Object create(final Context ctx, final Object obj) throws HomeException, HomeInternalException
    {
        final Subscriber sub = (Subscriber) super.create(ctx, obj);
        sub.setMsisdnEntryType(AbstractSubscriber.DEFAULT_MSISDNENTRYTYPE);
        sub.setFaxMsisdnEntryType(AbstractSubscriber.DEFAULT_FAXMSISDNENTRYTYPE);
        sub.setDataMsisdnEntryType(AbstractSubscriber.DEFAULT_DATAMSISDNENTRYTYPE);
        return sub;
    }
} // class