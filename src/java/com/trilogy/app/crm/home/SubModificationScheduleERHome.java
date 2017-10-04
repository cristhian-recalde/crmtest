package com.trilogy.app.crm.home;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.app.crm.agent.CronConstant;
import com.trilogy.app.crm.bean.*;
import com.trilogy.app.crm.bundle.*;
import com.trilogy.app.crm.xhome.home.*;
import com.trilogy.framework.xlog.log.*;
import com.trilogy.framework.xhome.holder.StringHolder;
import com.trilogy.app.crm.calculation.support.*;
import com.trilogy.app.crm.log.ERLogger;
import com.trilogy.app.crm.support.HomeSupportHelper;

public class SubModificationScheduleERHome extends HomeProxy 
{
	private String className = "com.redknee.app.crm.home.SubModificationScheduleERHome";
	private int ER_ID = 1192;
	private int ER_Class = 700;
	private String ER_Name = "Subscriber Priceplan Schedule Modification";
	
	// Action description: 0 = CREATE, 1 = UPDATE, 2 = DELETE
	
	public SubModificationScheduleERHome(Context ctx, Home delegate) 
	{	
		super(delegate);
	}
	
	public Object create(Context ctx, Object obj) throws HomeException,	HomeInternalException 
	{
		
		SubModificationSchedule subModificationSchedule=null; 
        try {
        	subModificationSchedule = (SubModificationSchedule) super.create(ctx, obj); 
        } catch (HomeException e)
        {
        	LogSupport.major(ctx, className, "Error in subModificationSchedule pipeline " + obj, e);
            throw e; 
        }
		
		try
		{
			// Get the MSISDN
			Subscriber sub = (Subscriber)HomeSupportHelper.get(ctx).findBean(ctx, Subscriber.class, subModificationSchedule.getSubscriptionId());
			
			if ( sub != null )
			{
				String msisdn = sub.getMSISDN();
			
				String snapShot = getSnapshot(subModificationSchedule);
				String supportingInfo = getSupportingInfo(subModificationSchedule);
				String newServices = getServices(subModificationSchedule);
				String oldServices = getRemoveServices(subModificationSchedule);	
				String agentId = getAgentId(subModificationSchedule);
				
				new ERLogMsg(ER_ID, ER_Class, ER_Name, subModificationSchedule.getSpid(),
				new String[] { 	String.valueOf(subModificationSchedule.getID()),
								subModificationSchedule.getSubscriptionId(),
								msisdn,
								"0", // Action description
								String.valueOf(subModificationSchedule.getType()),
								ERLogger.formatERDateDayOnly(subModificationSchedule.getScheduledTime()),
								String.valueOf(subModificationSchedule.getStatus()),
								"",//String.valueOf(bean.getStartDate()),
								"",//String.valueOf(bean.getEndDate()),
								ERLogger.formatERDateDayOnly(subModificationSchedule.getCreatedDate()),
								"",//String.valueOf(bean.getLastModifiedDate()),
								String.valueOf(subModificationSchedule.getChannel()),
								"\"[" + oldServices + "]\"",
								"\"[" + newServices + "]\"",
								"\"[" + snapShot + "]\"",
								"\"[" + supportingInfo + "]\"",
								"\"[" + subModificationSchedule.getNewPricePlanDetails() + "]\"",
								"\"[" + subModificationSchedule.getOldPricePlanDetails() + "]\"",
								agentId
							}).log(ctx);
			}
			else
			{
				LogSupport.major(ctx, className, "Unable to write 1192 ER for schedule because subscriber doesnt exist: " + obj);
			}
		}
		catch (Exception ex)
		{
			LogSupport.major(ctx, className, "Unable to write 1192 ER for schedule: " + obj, ex);
		}
		
		return subModificationSchedule;
	}
	
	public Object store(Context ctx, Object obj) throws HomeException,	HomeInternalException 
	{
		SubModificationSchedule subModificationSchedule=null; 
        try {
        	subModificationSchedule = (SubModificationSchedule) super.store(ctx, obj); 
        } catch (HomeException e)
        {
        	LogSupport.major(ctx, className, "Error in subModificationSchedule pipeline " + obj, e);
            throw e; 
        }
        
		try
		{
			// Get the MSISDN
			Subscriber sub = (Subscriber)HomeSupportHelper.get(ctx).findBean(ctx, Subscriber.class, subModificationSchedule.getSubscriptionId());
			if ( sub != null )
			{
				String msisdn = sub.getMSISDN();
				
				String snapShot = getSnapshot(subModificationSchedule);
				String supportingInfo = getSupportingInfo(subModificationSchedule);
				String newServices = getServices(subModificationSchedule);
				String oldServices = getRemoveServices(subModificationSchedule);
				String agentId = getAgentId(subModificationSchedule);
				
				new ERLogMsg(ER_ID, ER_Class, ER_Name, subModificationSchedule.getSpid(),
				new String[] { 	String.valueOf(subModificationSchedule.getID()),
								subModificationSchedule.getSubscriptionId(),
								msisdn,
								"1", // Action description
								String.valueOf(subModificationSchedule.getType()),
								ERLogger.formatERDateDayOnly(subModificationSchedule.getScheduledTime()),
								String.valueOf(subModificationSchedule.getStatus()),
								"",//String.valueOf(bean.getStartDate()),
								"",//String.valueOf(bean.getEndDate()),
								ERLogger.formatERDateDayOnly(subModificationSchedule.getCreatedDate()),
								"",//String.valueOf(bean.getLastModifiedDate()),
								String.valueOf(subModificationSchedule.getChannel()),
								"\"[" + oldServices + "]\"",
								"\"[" + newServices + "]\"",
								"\"[" + snapShot + "]\"",
								"\"[" + supportingInfo + "]\"",
								"\"[" + subModificationSchedule.getNewPricePlanDetails() + "]\"",
								"\"[" + subModificationSchedule.getOldPricePlanDetails() + "]\"",
								agentId
							}).log(ctx);
			}
			else
			{
				LogSupport.major(ctx, className, "Unable to write 1192 ER for schedule because subscriber doesnt exist: " + obj);
			}						
		}
		catch (Exception ex)
		{
			LogSupport.major(ctx, className, "Unable to write 1192 ER for schedule: " + obj, ex);
		}
		
		return subModificationSchedule;
	}
	
	public void remove(Context ctx, Object obj)
    throws HomeException, HomeInternalException, UnsupportedOperationException
	{
		SubModificationSchedule subModificationSchedule=null; 
		// Get the SubModificationSchedule from the database
    	subModificationSchedule = HomeSupportHelper.get(ctx).findBean(ctx, com.redknee.app.crm.bean.SubModificationSchedule.class, obj);
    	
        try {	
        		super.remove(ctx, obj); 
        } catch (HomeException e)
        {
        	LogSupport.major(ctx, className, "Error in subModificationSchedule pipeline " + obj, e);
            throw e; 
        }
        
		try
		{
			
			// Get the MSISDN
			Subscriber sub = (Subscriber)HomeSupportHelper.get(ctx).findBean(ctx, Subscriber.class, subModificationSchedule.getSubscriptionId());
			if ( sub != null )
			{
				String msisdn = sub.getMSISDN();
				
				String snapShot = getSnapshot(subModificationSchedule);
				String supportingInfo = getSupportingInfo(subModificationSchedule);
				String newServices = getServices(subModificationSchedule);
				String oldServices = getRemoveServices(subModificationSchedule);
				String agentId = getAgentId(subModificationSchedule);
				
				new ERLogMsg(ER_ID, ER_Class, ER_Name, subModificationSchedule.getSpid(),
				new String[] { 	String.valueOf(subModificationSchedule.getID()),
					subModificationSchedule.getSubscriptionId(),
								msisdn,
								"2", // Action description
								String.valueOf(subModificationSchedule.getType()),
								ERLogger.formatERDateDayOnly(subModificationSchedule.getScheduledTime()),
								String.valueOf(subModificationSchedule.getStatus()),
								"",//String.valueOf(bean.getStartDate()),
								"",//String.valueOf(bean.getEndDate()),
								"",//String.valueOf(bean.getCreatedDate()),
								"",//String.valueOf(bean.getLastModifiedDate()),
								String.valueOf(subModificationSchedule.getChannel()),
								"\"[" + newServices + "]\"",
								"\"[" + oldServices + "]\"", // Swap the old and new services as we need to revert back to the old plan services.
								"\"[" + snapShot + "]\"",
								"\"[" + supportingInfo + "]\"",
								"\"[" + subModificationSchedule.getNewPricePlanDetails() + "]\"",
								"\"[" + subModificationSchedule.getOldPricePlanDetails() + "]\"",
								agentId
							}).log(ctx);
			}
			else
			{
				LogSupport.major(ctx, className, "Unable to write 1192 ER for schedule because subscriber doesnt exist: " + obj);
			}						
		}
		catch (Exception ex)
		{
			LogSupport.major(ctx, className, "Unable to write 1192 ER for schedule: " + obj, ex);
		}

	}
	
	private String getSupportingInfo(SubModificationSchedule bean)
	{
		String supportingInfo = "";
		if ( bean.getSupportingInformation() != null  && bean.getSupportingInformation().size() > 0 )
		{
			StringHolder sh = (StringHolder)bean.getSupportingInformation().get(0);
			if ( sh != null )
			{
				supportingInfo = sh.getValue();
			}
		}
		return supportingInfo;
	}
	
	private String getSnapshot(SubModificationSchedule bean)
	{
		String snapShot = "";
		if ( bean.getSnapshot() != null && bean.getSnapshot().size() > 0 )
		{
			StringHolder sh = (StringHolder)bean.getSnapshot().get(0);
			if ( sh != null )
			{
				snapShot = sh.getValue();
			}
		}
		return snapShot;
	}
	
	private String getServices(SubModificationSchedule bean)
	{
		String services = "";
		String newPricePlanDetails = bean.getNewPricePlanDetails();
		if ( newPricePlanDetails != null && newPricePlanDetails.length() != 0 )
		{
			String[] newPricePlanDetailsArr = newPricePlanDetails.split(",");
			for (String newPricePlanDetail : newPricePlanDetailsArr)
			{
				if ( newPricePlanDetail.startsWith(CronConstant.SERVICES) )
				{
					services = newPricePlanDetail.split("=")[1].replaceAll("\\|", ",");
					break;
				}
			}
		}
		return services;
	}
	
	private String getRemoveServices(SubModificationSchedule bean)
	{
		String services = "";
		String newPricePlanDetails = bean.getNewPricePlanDetails();
		if ( newPricePlanDetails != null && newPricePlanDetails.length() != 0 )
		{
			String[] newPricePlanDetailsArr = newPricePlanDetails.split(",");
			for (String newPricePlanDetail : newPricePlanDetailsArr)
			{
				if ( newPricePlanDetail.startsWith(CronConstant.REMOVE_SERVICES) )
				{
					services = newPricePlanDetail.split("=")[1].replaceAll("\\|", ",");
					break;
				}
			}
		}
		return services;
	}	
	
	private String getAgentId(SubModificationSchedule bean)
	{
		String agentId = "";
		String newPricePlanDetails = bean.getNewPricePlanDetails();
		if ( newPricePlanDetails != null && newPricePlanDetails.length() != 0 )
		{
			String[] newPricePlanDetailsArr = newPricePlanDetails.split(",");
			for (String newPricePlanDetail : newPricePlanDetailsArr)
			{
				if ( newPricePlanDetail.startsWith(CronConstant.AGENT_ID) )
				{
					agentId = newPricePlanDetail.split("=")[1];
					break;
				}
			}
		}
		return agentId;
	}
	
}