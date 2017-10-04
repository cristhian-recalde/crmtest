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
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.webcontrol.ProxyWebControl;
import com.trilogy.framework.xhome.webcontrol.WebControl;

import com.trilogy.util.snippet.log.Logger;

/**
 * Provides a web-control decorator that looks-up a bean based on the identifier
 * passed in.  During construction, a delegate WebControl and key is provided.
 * The key is used to look-up the appropriate Home in the context.  The delegate
 * is used to render the bean looked-up in the home.  When the toWeb() method is
 * invoked, the bean is looked-up using the Home's find() method.
 *
 * @author gary.anderson@redknee.com
 */
public class PrimaryKeyWebControl extends ProxyWebControl
{
    /**
     * Creates a new PrimaryKeyWebControl.
     *
     * @param delegate The web-control to which this proxy delegates.
     * @param homeKey The key used to look-up the home in the context.
     *
     * @exception IllegalArgumentException Thrown if the homeKey parameter is null.
     */
    public PrimaryKeyWebControl(final WebControl delegate, final Object homeKey)
    {
        super(delegate);

        if (homeKey == null)
        {
            throw new IllegalArgumentException("The homeKey parameter is null.");
        }

        homeKey_ = homeKey;
    }


    // INHERIT
    public void toWeb(
        final Context context,
        final PrintWriter out,
        final String name,
        final Object obj)
    {
        Home home = (Home) context.get(homeKey_);

        if (home == null)
        {
            throw new IllegalStateException("No home found in context for " + homeKey_);
        }

        try
        {
            Object bean = home.find(context, obj);
            if (bean == null)
            {
                out.print(LinkedWebControl.MISSING_BEAN_MESSAGE);
            }
            else
            {
                super.toWeb(context, out, name, bean);
            }
        }
        catch (final HomeException exception)
        {
            Logger.minor(context, this, "Unable to retreive bean using condition " + obj);
        }
    }


    /**
     * The key used to look-up the home in the context.
     */
    private final Object homeKey_;

} // class
