package com.trilogy.app.crm.bas.directDebit;

import java.util.Date;

import com.trilogy.app.crm.CoreCrmLicenseConstants;
import com.trilogy.app.crm.bas.SpidAwareAgent;
import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.DirectDebitConstants;
import com.trilogy.app.crm.bean.DirectDebitRecordHome;
import com.trilogy.app.crm.bean.DirectDebitRecordXInfo;
import com.trilogy.app.crm.bean.GeneralConfig;
import com.trilogy.app.crm.bean.SpidDirectDebitConfig;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.app.crm.support.LicensingSupportHelper;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.LTE;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MajorLogMsg;

public class DirectDebitOvertimeUpdateAgent 
extends SpidAwareAgent
{

	
	public static final String AGENT_NAME = "DirectDebitOverTimeUpdate";
	public static final String AGENT_DISCRIPTION = "Clean up the direct debit request that are timed out";
	
	public DirectDebitOvertimeUpdateAgent()
	{
		this.setTaskName(AGENT_NAME);
	}
	
	public void processSpid(final Context ctx, final CRMSpid spid)
	{
	        final Home home = (Home) ctx.get(DirectDebitRecordHome.class);

	 
	        int day = 10; 
	        try 
	        {
	        	day = HomeSupportHelper.get(ctx).findBean(ctx,  SpidDirectDebitConfig.class, Integer.valueOf(spid.getId())).getDayTimeout();  
	        } catch (Throwable e)
	        {
	        	new MajorLogMsg(this, "fail to find spid direct debit config for spid" + spid.getId() + " will skip this spid", e).log(ctx); 
	        	return; 
	        }
	        
	        Date dueDate = CalendarSupportHelper.get(ctx).getDateWithNoTimeOfDay(CalendarSupportHelper.get(ctx).getDaysBefore(new Date(), day));	
	        And and = new And(); 
	        
	        and.add(new EQ(DirectDebitRecordXInfo.SPID, Integer.valueOf(spid.getId()))); 
	        and.add(new EQ(DirectDebitRecordXInfo.STATE, Long.valueOf(DirectDebitConstants.DD_STATE_PENDING)));        
	        and.add(new LTE(DirectDebitRecordXInfo.POST_DATE, dueDate)); 
	        
	        
	        GeneralConfig config = (GeneralConfig) ctx.get(GeneralConfig.class);  
	        int threadCount = 5; 
	        
	        if (config != null)
	        {
	        	threadCount = config.getDDRExpiryThreads();
	        }
	        
	        EnhancedParallVisitor visitor = new EnhancedParallVisitor(threadCount, new DirectDebitOvertimeVisitor() ); 
	        try
	        {
	            home.where(ctx, and).forEach(ctx, visitor);
	        }
	        catch (final HomeException e)
	        {
	         
	        }finally
	        {
	        	 try
	             {
	        		 visitor.shutdown(EnhancedParallVisitor.TIME_OUT_FOR_SHUTTING_DOWN);
	             }
	             catch (final Exception e)
	             {
	                 LogSupport.minor(ctx, this, "exception catched during wait for completion of all Direct debit threads", e);
	             }
	        }
	}
	
	
	   public boolean isLicensed(Context ctx)
	   {
	        return LicensingSupportHelper.get(ctx).isLicensed(ctx, CoreCrmLicenseConstants.DIRECTDEBIT_LICENSE); 
	   }

}
