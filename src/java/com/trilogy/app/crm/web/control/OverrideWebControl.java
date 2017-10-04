// INSPECTED: 06/10/03 LZOU

/*
 *  OverrideWebControl
 *
 *  Author : Kevin Greer
 *  Date   : Sept 30, 2003
 *
 *  Copyright (c) Redknee, 2003
 *    - all rights reserved
 */
 
package com.trilogy.app.crm.web.control;

import com.trilogy.framework.xhome.home.*;
import com.trilogy.app.crm.bean.*;
import com.trilogy.framework.xhome.context.*;
import com.trilogy.framework.xhome.webcontrol.*;
import java.io.PrintWriter;
import javax.servlet.ServletRequest;

/** Like a LongWebControl but displays the default value as blank. **/
public abstract class OverrideWebControl
    extends ProxyWebControl
{
    public OverrideWebControl()
    {
        this(new LongWebControl(20), DEFAULT_VALUE);
    }

    public OverrideWebControl(final WebControl delegate, final long defaultValue)
    {
        super(delegate);
        defaultValue_ = defaultValue;
    }

   
    public void toWeb(Context ctx, PrintWriter out, String name, Object obj)
    {
        try
        {
            long             value = ((Number) obj).longValue();
            Subscriber       sub   = (Subscriber)       ctx.get(AbstractWebControl.BEAN);
            PricePlanVersion plan  = sub.getRawPricePlanVersion(ctx);
            int              mode  = ctx.getInt("MODE", DISPLAY_MODE);
      
            if ( mode == DISPLAY_MODE )
            {
                // Only display one value (the one being used) in display mode
                /**  A Long object is expected by its Delegate : LongWebControl so....  **/
                //getDelegate().toWeb(ctx, out, name, String.valueOf(( value == defaultValue_ ) ? getValue(plan) : value));
                getDelegate().toWeb(ctx, out, name,  Long.valueOf(( value == defaultValue_ ) ? getValue(plan) : value));
            }
            else
            {
                // Use a TextFieldWebControl to output ""
                if ( value == defaultValue_ )
                {
                    empty_wc_.toWeb(ctx, out, name, "");
                }
                else
                {
                    getDelegate().toWeb(ctx, out, name, obj);
                }
            
                if ( plan != null )
                {
                    out.print(( value == defaultValue_ ) ? " -> " : " <- ");

                    final Context subContext = ctx.createSubContext();
                    subContext.put("MODE", DISPLAY_MODE);
                    getDelegate().toWeb(subContext, out, "", Long.valueOf(getValue(plan)));
                }
            }
        }
        catch (HomeException e)
        {
            // not important  
        }
    }
   
   
    public abstract long getValue(PricePlanVersion plan);


    public Object fromWeb(Context ctx, ServletRequest req, String name)
    {
        String str = req.getParameter(name);
      
        if ( str == null )
        {
            throw new NullPointerException("Null Value");  
        }
      
        if ( "".equals(str.trim()) ) return Long.valueOf(defaultValue_);
         
        return getDelegate().fromWeb(ctx, req, name);
    }

       
    protected WebControl empty_wc_ = new TextFieldWebControl();

    protected final long defaultValue_;

    protected static final long DEFAULT_VALUE = -1;
}


