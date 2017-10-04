package com.trilogy.app.crm.web.control;

import java.io.PrintWriter;
import java.util.Date;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.filter.Predicate;
import com.trilogy.framework.xhome.support.DateUtil;
import com.trilogy.framework.xhome.web.support.WebSupport;
import com.trilogy.framework.xhome.webcontrol.ProxyWebControl;
import com.trilogy.framework.xhome.webcontrol.ReadOnlyWebControl;
import com.trilogy.framework.xhome.webcontrol.WebControl;


public class PersistentReadOnlyOnPredicateWebControl extends ProxyWebControl
{
    final Predicate predicate_;

    public PersistentReadOnlyOnPredicateWebControl(final Predicate predicate, final WebControl delegate)
    {
        super(delegate);
        this.predicate_ = predicate;
        webControl_ = new ReadOnlyWebControl(delegate);
    }

    /**
     * Do not display anything if the Predicate returns true.
     * {@inheritDoc}
     */
    public void toWeb(final Context ctx, final PrintWriter out, final String name, final Object obj)
    {
        if (this.predicate_.f(ctx, obj))
        {
            String value = String.valueOf(obj);
            if (obj instanceof Date)
            {
               value = DateUtil.toString((Date) obj);
            }
            //This allows property to be passed in as hidden property,so that value doesn't get changed
            out.println("<input type=\"hidden\" name=\"" + name + "\" id=\"" + WebSupport.fieldToId(ctx, name )+"\"  value=\""+value.replaceAll("\"", "&quot;")+"\" />");            
            
            webControl_.toWeb(ctx, out, name, obj);
        }
        else
        {
            super.toWeb(ctx, out, name, obj);
        }
    }
    
    ReadOnlyWebControl webControl_;
}
