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
import java.io.StringWriter;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.web.util.Link;
import com.trilogy.framework.xhome.webcontrol.ProxyWebControl;
import com.trilogy.framework.xhome.webcontrol.WebControl;


/**
 * Provides a web control that turns the output of a delegate web control
 * (presumably text) into a link that loads a primary screen for the subject.
 *
 * @author gary.anderson@redknee.com
 */
public class LinkedWebControl extends ProxyWebControl
{
    public static final String MISSING_BEAN_MESSAGE = "--missing--";

    /**
     * Creates a new LinkedWebControl.
     *
     * @param delegate The WebControl to which we delegate.
     * @param screenName The name of the screen to which the link points.
     *
     * @exception IllegalArgumentException Thrown if the screenName parameter is null or empty.
     */
    public LinkedWebControl(final WebControl delegate, final String screenName)
    {
        super(delegate);

        if (screenName == null || screenName.trim().length() == 0)
        {
            throw new IllegalArgumentException(
                "The screenName parameter is null or empty.");
        }

        screenName_ = screenName;
    }


    // INHERIT
    public void toWeb(
        Context context,
        final PrintWriter out,
        final String name,
        final Object object)
    {
        context = context.createSubContext();
        context.setName(this.getClass().getName());
        final int originalMode = context.getInt("MODE", DISPLAY_MODE);
        context.put("MODE", DISPLAY_MODE);

        final StringWriter buffer = new StringWriter();
        final PrintWriter printBuffer = new PrintWriter(buffer);

        super.toWeb(context, printBuffer, name, object);

        printBuffer.flush();
        final String idString = buffer.toString();

        if (MISSING_BEAN_MESSAGE.equals(idString))
        {
            out.print("<font color=\"red\"><b>");
            out.print(MISSING_BEAN_MESSAGE);
            out.print("</b></font>");
        }
        else
        {
            final Link link = new Link(context);
            link.addRaw("cmd", screenName_);
            link.addRaw("key", object.toString());

            if (originalMode == DISPLAY_MODE)
            {
                link.addRaw("mode", "display");
            }
            else
            {
                link.addRaw("action", "edit");
            }

            link.writeLink(out, idString);
        }
    }


    /**
     * The name of the screen to use for the item search.
     */
    private final String screenName_;


} // class
