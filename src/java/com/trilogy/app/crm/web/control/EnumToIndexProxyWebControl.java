package com.trilogy.app.crm.web.control;

import java.io.PrintWriter;
import java.util.Iterator;

import javax.servlet.ServletRequest;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.webcontrol.ProxyWebControl;
import com.trilogy.framework.xhome.webcontrol.WebControl;
import com.trilogy.framework.xhome.xenum.EnumCollection;

public class EnumToIndexProxyWebControl extends ProxyWebControl
{
    public EnumToIndexProxyWebControl(final WebControl delegate)
    {
        super(delegate);
    }

    public Object fromWeb(Context ctx, ServletRequest req, String name) throws NullPointerException
    {
        Object val = getDelegate().fromWeb(ctx, req, name);
        com.redknee.framework.xhome.xenum.Enum e = (com.redknee.framework.xhome.xenum.Enum) val;
        return Short.valueOf(e.getIndex());
    }

    public void toWeb(Context ctx, PrintWriter out, String name, Object obj)
    {
        PredicateAwareRadioButtonWebControl control = (PredicateAwareRadioButtonWebControl) getDelegate();
        EnumCollection col = control.getEnumCollection(ctx);
        final short idx = ((Number) obj).shortValue();
        com.redknee.framework.xhome.xenum.Enum e = col.getByIndex(idx);

        if (e == null || e.getIndex() != idx)
        {
            final Iterator iter = col.iterator();
            if (iter.hasNext())
            {
                e = (com.redknee.framework.xhome.xenum.Enum) iter.next();
            }
            else
            {
                e = null;
            }
        }
        getDelegate().toWeb(ctx, out, name, e);
    }
}