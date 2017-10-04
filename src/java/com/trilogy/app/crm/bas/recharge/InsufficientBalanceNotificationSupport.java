package com.trilogy.app.crm.bas.recharge;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import bsh.ParseException;
import com.trilogy.app.crm.support.BillCycleSupport;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.app.crm.support.CronTaskSupportHelper;
import com.trilogy.framework.core.cron.AgentEntry;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xlog.log.LogSupport;

public class InsufficientBalanceNotificationSupport {
	
	// ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// METHOD RETURN A DATE
	//CASE 1 -- IF PARAMETER1 IS NULL OR BLANK ----THEN -- IT TAKES CURRENT DATE AS BILLING DATE
	//CASE 2 -- IF PARAMETER1 IS NOT NULL ---------THEN -- IT TAKES PROVIDED DATE... DATE FORMAT SHOULD ME YYYYMMDD
//	          THIS METHOD TAKE A DATE AND CONVERT IT INTO SPECIFIC REQUIRED FORMAT AND CONCIDERED AS A BILLING DATE.
	// ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	    
	    public static Date insufficientBalanceNotificationDate(final Context ctx) throws ParseException  
	    {
	        final String billingDateString = CronTaskSupportHelper.get(ctx).getParameter1(ctx);
	        Calendar calendar = new GregorianCalendar();
	        Date parameterDate = null;
	       
	      //IF PARAMETER 1 IS NOT NULL THEN ...FORMAT THE INPUT DATE INTO REQUIRED DATE FORMAT.
	        if (billingDateString != null && billingDateString.trim().length() > 0)
	        {	
	        	
	        	DateFormat paramDateFormat = new SimpleDateFormat("yyyyMMdd");
				try {
					parameterDate = (Date)paramDateFormat.parse(billingDateString.toString());
				} catch (java.text.ParseException e) {
					if (LogSupport.isDebugEnabled(ctx))
			        {
						LogSupport.debug(ctx, WeeklyRecurChargeCronAgentSupport.class.getName(),e);
			        }
					throw new ParseException();
				}	
	        }else
	        {
	        	// TAKES CURRENT DATE
	        	calendar = new GregorianCalendar();
	        }
	      
	        Date currentDate = CalendarSupportHelper.get(ctx).getDateWithNoTimeOfDay(calendar.getTime());
	        
	        //IF PARAMETER 1 DATE IS GIVEN THEN CURRENT is parameterDate DATE OTHER WISE IT WILL BE currentDate
	        if (billingDateString != null && billingDateString.trim().length() > 0)
	        {
	        	currentDate = parameterDate; 
	        }
	     
	  
	        if (LogSupport.isDebugEnabled(ctx))
	        {
	            LogSupport.debug(ctx, WeeklyRecurChargeCronAgentSupport.class.getName(), "Date considered is " + currentDate);
	        }
	        
	        return currentDate;
	    }

}
