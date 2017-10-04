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

package com.trilogy.app.crm.web.acctmenu;

import com.trilogy.framework.xhome.beans.XBeans;
import com.trilogy.framework.xhome.context.ContextLocator;
import com.trilogy.framework.xhome.home.Home;

import com.trilogy.app.crm.bean.Subscriber;

/**
 * SubControllerBorder for use with any WebController for Beans which
 * implement the Child interface with Subscriber being their parent.
 *
 * @author kevin.greer@redknee.com
 */
public class SubscriberSubControllerBorder extends SubControllerBorder
{
    public static final String DEFAULT_FIELD_NAME = "SUBSCRIBERID";


    public SubscriberSubControllerBorder(final Class childCls)
    {
        this(childCls, DEFAULT_FIELD_NAME);
    }

    public SubscriberSubControllerBorder(final Class childCls, final String fieldName)
    {
        this(childCls, fieldName, XBeans.getClass(ContextLocator.locate(), childCls, Home.class));
    }

    public SubscriberSubControllerBorder(final Class childCls, final String fieldName, final Object homeKey)
    {
        super(Subscriber.class, childCls, homeKey, fieldName);
    }

}


