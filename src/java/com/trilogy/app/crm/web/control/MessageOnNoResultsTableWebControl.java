package com.trilogy.app.crm.web.control;

import java.io.PrintWriter;
import java.util.Collection;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.webcontrol.ProxyWebControl;
import com.trilogy.framework.xhome.webcontrol.WebControl;

public class MessageOnNoResultsTableWebControl
    extends ProxyWebControl
{
    public MessageOnNoResultsTableWebControl(WebControl control)
    {
        super(control);
    }

    public MessageOnNoResultsTableWebControl(WebControl control, String message)
    {
        super(control);
        message_ = message;
    }

    public void toWeb(Context ctx, PrintWriter out, String name, Object obj)
    {
        if(obj instanceof Collection)
        {
            Collection beans = (Collection) obj;
            if(beans.size() > 0)
            {
                getDelegate().toWeb(ctx, out, name, obj);
            }
            else
            {
                out.println("<table width=\"70%\"><tr><td><center><b>" + message_ + "</b></center></td></tr></table>");
            }
        }
        else
        {
            getDelegate().toWeb(ctx, out, name, obj);
        }
    }

    private String message_ = "The result set is empty.";
}