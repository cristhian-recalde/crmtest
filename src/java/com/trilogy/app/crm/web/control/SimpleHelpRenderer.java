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
package com.trilogy.app.crm.web.control;

import java.io.PrintWriter;

import com.trilogy.framework.xhome.web.renderer.DefaultHelpRenderer;
import com.trilogy.framework.xhome.web.renderer.HelpRenderer;


/**
 * A simple help renderer that can be used when nesting help text.  It uses HTML paragraphs and line breaks instead
 * of HTML tables, which can't be used in our current FW help text (it screws up the main GUI).
 *
 * @author Aaron Gourley
 * @since 7.4.16
 */
public class SimpleHelpRenderer extends DefaultHelpRenderer
{
    private static HelpRenderer instance_ = null;
    public static HelpRenderer instance()
    {
        if( instance_ == null )
        {
            instance_ = new SimpleHelpRenderer();
        }
        return instance_;
    }

    protected SimpleHelpRenderer()
    {
    }

    @Override
    public void Help(PrintWriter out, String title)
    {
        out.print("<p><b>");
        out.print(title);
        out.print("</b><br/>");
    }

    @Override
    public void HelpEnd(PrintWriter out)
    {
        out.print("</p>");
    }

    @Override
    public void FieldTitle(PrintWriter out, String title)
    {
        out.print("&nbsp;&nbsp;&nbsp;");
        out.print(title);
        out.print("&nbsp;-&nbsp;");
    }
    
    @Override
    public void FieldEnd(PrintWriter out)
    {
        out.print("<br/>");
    }

    @Override
    public void FieldList(PrintWriter out)
    {
        out.print("<font>");
    }

    @Override
    public void FieldListEnd(PrintWriter out)
    {
        out.print("</font>");
    }

}
