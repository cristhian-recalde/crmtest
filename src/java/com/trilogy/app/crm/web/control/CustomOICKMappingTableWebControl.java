/*
 * This code is a protected work and subject to domestic and international
 * copyright law(s). A complete listing of authors of this work is readily 
 * available. Additionally, source code is, by its very nature, confidential 
 * information and inextricably contains trade secrets and other information 
 * proprietary, valuable and sensitive to Redknee, no unauthorised use, 
 * disclosure, manipulation or otherwise is permitted, and may only be used 
 * in accordance with the terms of the licence agreement entered into with 
 * Redknee Inc. and/or its subsidiaries.
 *
 * Copyright &copy; Redknee Inc. and its subsidiaries. All Rights Reserved. 
 *
 */
package com.trilogy.app.crm.web.control;

import java.io.PrintWriter;
import java.util.Collection;
import java.util.Iterator;

import com.trilogy.framework.xhome.context.Context;

import com.trilogy.app.crm.bean.OICKMapping;
import com.trilogy.app.crm.bean.OICKMappingTableWebControl;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;


/**
 * Provides a web control decorator that causes specific values to be
 * displayed in the OICK Mapping table.
 *
 * @author jimmy.ng@redknee.com
 */
public class CustomOICKMappingTableWebControl
    extends OICKMappingTableWebControl
{
    // INHERIT
    public void toWeb(
        final Context ctx,
        final PrintWriter out,
        final String name,
        final Object obj)
    {
        final Context subContext = ctx.createSubContext();
        
        final Collection mappings = (Collection) obj;
        final Iterator itr = mappings.iterator();
        while (itr.hasNext())
        {
            final OICKMapping mapping = (OICKMapping) itr.next();
            if (mapping.getSubscriberType() == SubscriberTypeEnum.POSTPAID)
            {
                mapping.setUnprovisionOICK("n/a");
            }
        }
        
        super.toWeb(subContext, out, name, mappings);
    }
} // class
