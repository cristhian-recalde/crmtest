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
 */
package com.trilogy.app.crm.web.control;

import java.io.PrintWriter;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.webcontrol.TextFieldWebControl;


/**
 * A webcontrol based on the TextField web control, but adds hidden fields to
 * the HTML so that transient input is carred through.  This is useful for forms
 * that have parameters passed in through the URL for fields that should be
 * read-only.  Making the fields read-only in the current Framework (pre 1.5)
 * prevents their values from being passed when the screen is reloaded.
 *
 * @author gary.anderson@redknee.com
 */
public
class TransientPassTextFieldWebControl
    extends TextFieldWebControl
{
    private boolean isEditable_;
    
    /**
     * Creates a new TransientPassTextFieldWebControl of the given size.
     *
     * @param size The size of the text field.
     */
    public TransientPassTextFieldWebControl(final int size)
    {
        super(size);
        isEditable_ = false;
    }

    public TransientPassTextFieldWebControl(final int size, boolean isEditable)
    {
        super(size);
        isEditable_ = isEditable;
    }
    
    // INHERIT
    public void toWeb(
        final Context context,
        final PrintWriter out,
        final String name,
        final Object obj)
    {
        final Context subContext = context.createSubContext();
        if (!isEditable_)
        {
            subContext.put("MODE", DISPLAY_MODE);
        }
        if (!isEditable_ || subContext.getInt("MODE", DISPLAY_MODE) == DISPLAY_MODE)
        {         
            out.print("<input type=\"hidden\" name=\"");
            out.print(name);
            out.print("\" ");
    
            out.print("value=\"");
            out.print((obj == null) ? "" : addSequence(obj.toString() ) );
            out.println("\" />");
        }
        
        super.toWeb(subContext, out, name, obj);
    }
    private String addSequence(String str)
    {
        return str.replaceAll("\"", "&quot;");
    }
} // class
