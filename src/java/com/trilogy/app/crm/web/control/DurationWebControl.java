/*
 * Created on Jul 26, 2005
 */
package com.trilogy.app.crm.web.control;

import java.io.PrintWriter;

import javax.servlet.ServletRequest;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.webcontrol.PrimitiveWebControl;

/**
 * @author rattapattu
 */
public class DurationWebControl extends PrimitiveWebControl
{
    private static long SECOND = 1000;
    private static long MINUTE = SECOND*60;
    private static long HOUR = MINUTE*60;
    
    /* (non-Javadoc)
     * @see com.redknee.framework.xhome.webcontrol.InputWebControl#fromWeb(com.redknee.framework.xhome.context.Context, javax.servlet.ServletRequest, java.lang.String)
     */
    public Object fromWeb(Context ctx, ServletRequest req, String name)
            throws NullPointerException
    {
        return null;
    }

    /* (non-Javadoc)
     * @see com.redknee.framework.xhome.webcontrol.OutputWebControl#toWeb(com.redknee.framework.xhome.context.Context, java.io.PrintWriter, java.lang.String, java.lang.Object)
     */
    public void toWeb(Context ctx, PrintWriter out, String name, Object obj)
    {
        long timeTaken = ((Long)obj).longValue();
        
        out.print(formatTimeTaken(timeTaken));

    }
    
    private String formatTimeTaken(long timeTaken)
    {
        if(timeTaken == 0)
        {
            return "";
        }
        else if(timeTaken > HOUR)
        {
            return formatHours(timeTaken);
        }
        else if(timeTaken > MINUTE)
        {
            return formatMinute(timeTaken);
        }
        else if(timeTaken > SECOND)
        {
            return formatSeconds(timeTaken);
        }
        else
        {
            return formatMiliSeconds(timeTaken);
        }
    }

    private String formatMiliSeconds(long timeTaken)
    {
       return "<b>" + timeTaken + "mills </b>";        
    }

    private String formatHours(long timeTaken)
    {
       StringBuilder buf = new StringBuilder();  
        
       long hours = timeTaken/HOUR;
       long remainder = timeTaken%HOUR;
       
       buf.append("<font color=red><b>").append(hours).append("hrs </b></font>");
       
       buf.append(formatTimeTaken(remainder));
       
       return buf.toString();
       
    }

    private String formatSeconds(long timeTaken)
    {
       StringBuilder buf = new StringBuilder();  
        
       long mins = timeTaken/SECOND;
       long remainder = timeTaken%SECOND;
       
       buf.append("<font color=blue><b>").append(mins).append("sec </b></font>");
       
       buf.append(formatTimeTaken(remainder));
       
       return buf.toString();
    }

    private String formatMinute(long timeTaken)
    {
       StringBuilder buf = new StringBuilder();
        
       long mins = timeTaken/MINUTE;
       long remainder = timeTaken%MINUTE;
       
       buf.append("<font color=green><b>").append(mins).append("mins </b></font>");
       
       buf.append(formatTimeTaken(remainder));
        
       return buf.toString();
    }

}
