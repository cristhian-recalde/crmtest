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

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.support.IdentitySupport;
import com.trilogy.framework.xhome.webcontrol.ProxyWebControl;
import com.trilogy.framework.xhome.webcontrol.WebControl;


/**
 * Provides a web control that uses IdentitySupport to generate a String version
 * of the bean and passes that to the delegate WebControl.
 *
 * @author gary.anderson@redknee.com
 */
public
class IdentitySupportWebControl
    extends ProxyWebControl
{
    /**
     * Creates a new IdentitySupportWebControl.
     *
     * @param delegate The web-control to which this proxy delegates.
     * @param support Used to convert the bean to a String.
     */
    public IdentitySupportWebControl(final WebControl delegate, final IdentitySupport support)
    {
        super(delegate);

        identitySupport_ = support;
    }


    // INHERIT
    public void toWeb(
        final Context context,
        final PrintWriter out,
        final String name,
        final Object obj)
    {
        super.toWeb(context, out, name, identitySupport_.toStringID(obj));
    }


    /**
     * Used to convert the bean to a String.
     */
    private final IdentitySupport identitySupport_;

} // class
