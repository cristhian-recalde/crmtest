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

import java.util.Collection;

import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.filter.Predicate;
import com.trilogy.framework.xhome.visitor.ListBuildingVisitor;
import com.trilogy.framework.xhome.visitor.PredicateVisitor;
import com.trilogy.framework.xhome.visitor.Visitors;
import com.trilogy.framework.xhome.webcontrol.ProxyWebControl;
import com.trilogy.framework.xhome.webcontrol.TableWebControl;
import com.trilogy.framework.xhome.webcontrol.WebControl;

import com.trilogy.util.snippet.log.Logger;

/**
 * Filters collection of beans used by the WebControl delegate based Predicate provided.
 *
 * @author victor.stratan@redknee.com
 */
public class FilterTableOnPredicateWebControl extends ProxyWebControl implements TableWebControl
{
    /**
     * @param delegate
     * @param predicate
     */
    public FilterTableOnPredicateWebControl(final WebControl delegate, final Predicate predicate)
    {
        setDelegate(delegate);
        this.predicate_ = predicate;
    }

    public void toWeb(final Context ctx, final java.io.PrintWriter out, final String name, Object beans)
    {
        if (beans instanceof Collection)
        {
            final Collection collection = (Collection) beans;
            final ListBuildingVisitor listVisitor = new ListBuildingVisitor();
            final PredicateVisitor visitor = new PredicateVisitor(predicate_, listVisitor);
            try
            {
                Visitors.forEach(ctx, collection, visitor);
                beans = listVisitor;
            }
            catch (AgentException e)
            {
                Logger.minor(ctx, this, "Unable to filter the result collection", e);
            }
        }
        else
        {
            Logger.debug(ctx, this, "parameter 4 is not a Collection. Check instantiation."
                    + " Can be used only for TableWebControls", null);
        }

        delegate_.toWeb(wrapContext(ctx), out, name, beans);
    }

    final Predicate predicate_;
}