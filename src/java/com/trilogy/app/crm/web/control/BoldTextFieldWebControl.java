package com.trilogy.app.crm.web.control;

import java.io.PrintWriter;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.webcontrol.TextFieldWebControl;


public class BoldTextFieldWebControl extends TextFieldWebControl
{
    public BoldTextFieldWebControl(int size)
    {
       super(size);
    }

    public void toWeb(Context ctx, PrintWriter out, String name, Object obj)
    {
        int mode = ctx.getInt("MODE", DISPLAY_MODE);
        
        if (mode == DISPLAY_MODE && obj!=null)
        {
            super.toWeb(ctx, out, name, "<b>" + obj.toString() + "</b>");
        }
        else
        {
            super.toWeb(ctx, out, name, obj);
        }
    }
}
