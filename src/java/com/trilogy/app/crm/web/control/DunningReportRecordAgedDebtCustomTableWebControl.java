package com.trilogy.app.crm.web.control;

import java.io.PrintWriter;
import java.util.Collection;

import com.trilogy.app.crm.dunning.DunningReportRecordAgedDebtTableWebControl;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.webcontrol.AbstractTableWebControl;


public class DunningReportRecordAgedDebtCustomTableWebControl extends DunningReportRecordAgedDebtTableWebControl
{
    public void toWeb(Context ctx, PrintWriter out, String name, Object obj)
    {
        Collection         beans    = (Collection) obj;
        int mode = ctx.getInt("MODE", DISPLAY_MODE);
        int                blanks   = ctx.getInt(NUM_OF_BLANKS, DEFAULT_BLANKS);

        if (mode != EDIT_MODE && mode != CREATE_MODE )
        {
           // The Math.max() bit is so that if blanks is set to 0 that you can still add a row
           out.print("<input type=\"hidden\" name=\"" + name + SEPERATOR + "_count\" value=\"" + (beans.size() + blanks) + "\" />");
        }
        
        super.toWeb(ctx, out, name, obj);
    }
    
    public void fromWeb(Context ctx, Object p1, javax.servlet.ServletRequest p2, String p3)
    {
        Context subCtx = ctx.createSubContext();
        subCtx.put(AbstractTableWebControl.DISABLE_NEW, Boolean.TRUE);
        super.fromWeb(subCtx, p1, p2, p3);
    }
    
    public Object fromWeb(Context ctx, javax.servlet.ServletRequest p1, String p2)
    {
        Context subCtx = ctx.createSubContext();
        subCtx.put(AbstractTableWebControl.DISABLE_NEW, Boolean.TRUE);
        return super.fromWeb(subCtx, p1, p2);
    }
}
