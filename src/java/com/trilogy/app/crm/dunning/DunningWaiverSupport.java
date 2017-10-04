package com.trilogy.app.crm.dunning;

import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountHome;
import com.trilogy.app.crm.bean.AccountXInfo;
import com.trilogy.app.crm.bean.AgedDebt;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.Or;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.LogSupport;

public class DunningWaiverSupport {
	
	private static Account account;
	private static DunningPolicy policy;
	public static Date calCulateWaiverEndDate(Context ctx,
			DunningWaiver bean) 
	
	{
		Home home=(Home)ctx.get(AccountHome.class);
		
		List<AgedDebt> agedDebt=null;
		DunningLevel nextLevel=null;
		Date waiverEndDate=null;
		try {
			account = (Account)home.find(ctx, new EQ(AccountXInfo.BAN,bean.getBan()));
		
		
		Home dunningHome=(Home)ctx.get(DunningPolicyHome.class);
		policy=(DunningPolicy) dunningHome.find(ctx,new EQ(DunningPolicyXInfo.DUNNING_POLICY_ID,account.getDunningPolicyId())); 
		Date oldestAgedDebtToLook = DunningProcessHelper.getOldestAgedDebtToLook(ctx, account, policy, CalendarSupportHelper.get(ctx).getRunningDate(ctx));
		agedDebt=account.getInvoicedAgedDebt(ctx, oldestAgedDebtToLook, true);
		Collections.reverse(agedDebt);
		
		int currentLevelId=account.getLastDunningLevel();
		List<DunningLevel> levels = policy.getAllLevels(ctx);
		if(levels.size()==currentLevelId)
		{
			throw new RuntimeException("Acccount is already at it max level. can not add exempted days");
		}
		nextLevel=policy.getLevelAt(ctx,account.getLastDunningLevel());
		int currentLevelGraceDays=nextLevel.getGraceDays();
		
		waiverEndDate=calculateFinalDate(ctx,agedDebt.get(0).getDueDate(),account.getDunningGracePeriod(),bean.getExemptedDays(),currentLevelGraceDays);
		
		 if (LogSupport.isDebugEnabled(ctx))
         {
             StringBuilder sb = new StringBuilder();
             sb.append("Calculated  waiver end date for BAN - ");
             sb.append(account.getBAN());
             sb.append("--");
             sb.append(waiverEndDate);
             LogSupport.debug(ctx, DunningWaiverSupport.class.getName(), sb.toString());
         }
		 
		}catch (HomeException e) {
			
		}
		return waiverEndDate;
	
		
		
	}

	private static Date calculateFinalDate(Context ctx, Date dueDate,
			int dunningGracePeriod, int exemptedDays, int currentLevelGraceDays) 
	{
		int totalExemptedDays=dunningGracePeriod+exemptedDays+currentLevelGraceDays;
		Date oldestDueDate = CalendarSupportHelper.get(ctx).getDateWithNoTimeOfDay(dueDate);
        final Calendar calendar = Calendar.getInstance();
        calendar.setTime(oldestDueDate);
        calendar.add(Calendar.DAY_OF_YEAR, totalExemptedDays);
        
        
        if (LogSupport.isDebugEnabled(ctx))
        {
            StringBuilder sb = new StringBuilder();
            sb.append("Oldest due date - ");
            sb.append(oldestDueDate);
            sb.append(", Account dunning grace period  - ");
            sb.append(dunningGracePeriod);
            sb.append(", Exempted days -");
            sb.append(exemptedDays);
            sb.append(", Current level grace days -");
            sb.append(currentLevelGraceDays);
            LogSupport.debug(ctx, DunningWaiverSupport.class.getName(), sb.toString());
        }
        return calendar.getTime();          
	
	
	}

	public static void rejectExistingDunningReport(Context ctx, String ban) throws DunningProcessException 
	{

		Home home=(Home)ctx.get(DunningReportRecordHome.class);
		And where=new And();
		where.add(new EQ(DunningReportRecordXInfo.BAN,ban));
		Or or= new Or();
		or.add(new EQ(DunningReportRecordXInfo.RECORD_MATURITY,DunningReportRecordMatureStateEnum.PENDING_INDEX));
		or.add(new EQ(DunningReportRecordXInfo.RECORD_MATURITY,DunningReportRecordMatureStateEnum.APPROVED_INDEX));
		where.add(or);
		
		try 
		{
			DunningReportRecord record=(DunningReportRecord) home.find(ctx, where);
			if(record!=null)
			{
				 if (LogSupport.isDebugEnabled(ctx))
	                {
	                    StringBuilder sb = new StringBuilder();
	                    sb.append("Found Existing sDunning Report record for BAN - ");
	                    sb.append(ban);
	                    sb.append("   -Marking it as DISCARDED");
	                    LogSupport.debug(ctx, DunningWaiverSupport.class.getName(), sb.toString());
	                }
				 
				record.setRecordMaturity(DunningReportRecordMatureStateEnum.DISCARDED_INDEX);
				home.store(ctx, record);
			}
		}  catch (HomeException e) {
			throw new DunningProcessException(e);
		}
		
	
		
	}

	public static int fetchCurrentLevelGraceDays(Context ctx,
			DunningWaiver dunningWaiverBean) {
		
		DunningLevel nextLevel=null;
		nextLevel=policy.getLevelAt(ctx,account.getLastDunningLevel());
		if (LogSupport.isDebugEnabled(ctx))
        {
            StringBuilder sb = new StringBuilder();
            sb.append("Fetching current level grace days - ");
            sb.append("Current level is - ");
            sb.append(nextLevel.getId());
            sb.append(", Grace days - ");
            sb.append(nextLevel.getGraceDays());
            LogSupport.debug(ctx, DunningWaiverSupport.class.getName(), sb.toString());
        }
		
		if(nextLevel!=null)
		{
			return nextLevel.getGraceDays();
		}
		return 0;
	}

}
