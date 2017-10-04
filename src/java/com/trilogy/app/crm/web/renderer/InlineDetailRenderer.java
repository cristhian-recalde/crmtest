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
 * Copyright � Redknee Inc. and its subsidiaries. All Rights Reserved. 
 */
package com.trilogy.app.crm.web.renderer;

import java.io.PrintWriter;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.web.renderer.DetailRenderer;
import com.trilogy.framework.xhome.web.renderer.DetailRendererProxy;


/**
 * This detail renderer output inner-beans in such a way as they appear to be part of the
 * container bean (i.e. everything goes in the same table instead of a table within a table)
 * 
 * @author Aaron Gourley
 * @since 7.5
 */
public class InlineDetailRenderer extends DetailRendererProxy
{
    
    public InlineDetailRenderer()
    {
        super();
    }

    public InlineDetailRenderer(DetailRenderer delegate)
    {
        super(delegate);
    }

    /*
     * (non-Javadoc)
     * @see com.redknee.framework.xhome.web.renderer.DetailRendererProxy#Table(java.io.PrintWriter, java.lang.String)
     */
    @Override
    public void Table(final Context ctx, PrintWriter out, String title)
    {
        // Output nothing for the table start to keep it inline
    }

    /*
     * (non-Javadoc)
     * @see com.redknee.framework.xhome.web.renderer.DetailRendererProxy#TableEnd(java.io.PrintWriter)
     */
    @Override
    public void TableEnd(final Context ctx, PrintWriter out)
    {
        // Output nothing for the table end to keep it inline
    }
}
