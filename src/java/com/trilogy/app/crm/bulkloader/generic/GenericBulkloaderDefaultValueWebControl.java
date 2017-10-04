package com.trilogy.app.crm.bulkloader.generic;

import java.io.PrintWriter;

import javax.servlet.ServletRequest;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.webcontrol.AbstractWebControl;
import com.trilogy.framework.xhome.webcontrol.ProxyWebControl;
import com.trilogy.framework.xhome.webcontrol.WebControl;

/**
 * @author sbanerjee
 *
 */
public class GenericBulkloaderDefaultValueWebControl extends ProxyWebControl
        implements WebControl
{
    
    
    /**
     * 
     */
    public GenericBulkloaderDefaultValueWebControl()
    {
        super();
    }


    /**
     * @param delegate
     */
    public GenericBulkloaderDefaultValueWebControl(WebControl delegate)
    {
        super(delegate);
    }


    @Override
    public void toWeb(Context ctx, PrintWriter p1, String p2, Object p3)
    {
        
        BulkloadPropertyInfo bean = (BulkloadPropertyInfo) ctx.get(AbstractWebControl.BEAN);
        
        if(!bean.getUseDefaultOnFailure())
        {
            final int mode = ctx.getInt("MODE", DISPLAY_MODE);
    
            if ( mode != DISPLAY_MODE )
            {
               ctx = ctx.createSubContext();
               ctx.put("MODE", Integer.valueOf(DISPLAY_MODE));
            }
        }
        
        super.toWeb(ctx, p1, p2, p3);
    }
    
    
    @Override
    public Object fromWeb(Context ctx, ServletRequest p1, String p2)
    {
        BulkloadPropertyInfo bean = (BulkloadPropertyInfo) ctx.get(AbstractWebControl.BEAN);
        
        // just as though the parameter didn't exist
        if(!bean.getUseDefaultOnFailure())
            throw new NullPointerException();
        
        
        return super.fromWeb(ctx, p1, p2);
    }
}
