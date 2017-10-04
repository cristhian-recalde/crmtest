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
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletRequest;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.web.action.ActionMgr;
import com.trilogy.framework.xhome.web.action.EditAction;
import com.trilogy.framework.xhome.web.action.ViewAction;
import com.trilogy.framework.xhome.web.action.WebAction;
import com.trilogy.framework.xhome.web.util.Link;
import com.trilogy.framework.xhome.webcontrol.AbstractWebControl;


/**
 * Provides a web control that simply enumerates the actions (excepting Edit and
 * View) associated with the bean.
 *
 * @author gary.anderson@redknee.com
 */
public
class ActionEnumerationWebControl
    extends AbstractWebControl
{
    /**
     * Gets an instance of this class.
     *
     * @return An instance of this class.
     */
    public static ActionEnumerationWebControl instance()
    {
        return INSTANCE;
    }


    /**
     * {@inheritDoc}
     */
    public void toWeb(
        final Context context,
        final PrintWriter out,
        final String name,
        final Object object)
    {
        final Object bean = context.get(BEAN);
        final List actions = ActionMgr.getActions(context);

        for (final Iterator k = actions.iterator(); k.hasNext();)
        {
            final WebAction action = (WebAction)k.next();

            if (!(action instanceof ViewAction)
                && !(action instanceof EditAction))
            {
                final Link subLink = new Link(context);
                action.writeLink(context, out, bean, subLink);

                if (k.hasNext())
                {
                    out.print("&nbsp;&nbsp;");
                }
            }
        }
    }


    /**
     * {@inheritDoc}
     */
    public Object fromWeb(
        final Context context,
        final ServletRequest req,
        final String name)
    {
        return null;
    }


    /**
     * {@inheritDoc}
     */
    public void fromWeb(
        final Context context,
        final Object obj,
        final ServletRequest req,
        final String name)
    {
        // Empty.
    }


    /**
     * An instance of this class.
     */
    private static final ActionEnumerationWebControl INSTANCE = new ActionEnumerationWebControl();

} // class
