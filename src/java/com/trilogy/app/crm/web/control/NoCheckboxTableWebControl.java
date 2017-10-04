/*
 * This code is a protected work and subject to domestic and international copyright
 * law(s). A complete listing of authors of this work is readily available. Additionally,
 * source code is, by its very nature, confidential information and inextricably contains
 * trade secrets and other information proprietary, valuable and sensitive to Redknee. No
 * unauthorized use, disclosure, manipulation or otherwise is permitted, and may only be
 * used in accordance with the terms of the license agreement entered into with Redknee
 * Inc. and/or its subsidiaries.
 * 
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.web.control;

import java.io.PrintWriter;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.webcontrol.AbstractTableWebControl;
import com.trilogy.framework.xhome.webcontrol.AbstractWebControl;
import com.trilogy.framework.xhome.webcontrol.ProxyWebControl;
import com.trilogy.framework.xhome.webcontrol.WebControl;


/**
 * Wrapper for the TableWebControl that removes the checkbox.
 * 
 * @author Marcio Marques
 * @since 9.0
 * 
 */
public class NoCheckboxTableWebControl extends ProxyWebControl
{

    /**
     * Creates a new NoCheckboxTableWebControl object.
     * 
     * @param delegate
     */
    public NoCheckboxTableWebControl(WebControl delegate)
    {
        super(delegate);
    }


    /**
     * {@inheritDoc}
     */
    public void toWeb(Context ctx, PrintWriter out, String name, Object obj)
    {
        Context subCtx = ctx.createSubContext();
        subCtx.put(AbstractTableWebControl.HIDE_CHECKBOX, Boolean.TRUE);
        subCtx.put(AbstractTableWebControl.DISABLE_NEW, Boolean.TRUE);
        subCtx.put(AbstractWebControl.NUM_OF_BLANKS, 0);
        getDelegate().toWeb(subCtx, out, name, obj);
    }
}
