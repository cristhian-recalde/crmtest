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
import java.text.SimpleDateFormat;
import java.util.Date;


import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.filter.Predicate;
import com.trilogy.framework.xhome.support.DateUtil;
import com.trilogy.framework.xhome.web.support.WebSupport;
import com.trilogy.framework.xhome.webcontrol.ProxyWebControl;
import com.trilogy.framework.xhome.webcontrol.WebControl;

/**
 * Do not display anything if the Predicate returns true.
 * @author victor.stratan@redknee.com
 */
public class HideOnConditionWebControl extends ProxyWebControl
{
    final Predicate predicate_;

    public HideOnConditionWebControl(final Predicate predicate, final WebControl delegate)
    {
        super(delegate);
        this.predicate_ = predicate;
    }

    /**
     * Do not display anything if the Predicate returns true.
     * {@inheritDoc}
     */
    public void toWeb(final Context ctx, final PrintWriter out, final String name, final Object p3)
    {
        if (this.predicate_.f(ctx, p3))
        {
            String value = String.valueOf(p3);
            if (p3 instanceof Date)
            {
               value = DateUtil.toString((Date) p3);
            }
            //This allows property to be passed in as hidden property,so that value doesn't get changed
            out.println("<input type=\"hidden\" name=\"" + name + "\" id=\"" + WebSupport.fieldToId(ctx, name )+"\"  value=\""+value.replaceAll("\"", "&quot;")+"\" />");            
            return;
        }
        super.toWeb(ctx, out, name, p3);
    }
}
