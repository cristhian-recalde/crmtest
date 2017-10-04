package com.trilogy.app.crm.dunning;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import com.trilogy.app.crm.bean.AgedDebt;
import com.trilogy.app.crm.log.CoreERLogger;
import com.trilogy.app.crm.support.SystemSupport;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xlog.log.LogSupport;

public class DunningReportGenerationHelper 
{
	 
	public static List<DunningReportRecordAgedDebt> createDunningReportRecordAgedDebtList(final Context context, final int spid)
    {
        int agedDebtBreakdown = SystemSupport.getDunningReportAgedDebtBreakdown(context, spid);
        List<DunningReportRecordAgedDebt> agedDebts = new ArrayList<DunningReportRecordAgedDebt>();
        for (int i=0;i<agedDebtBreakdown;i++)
        {
            StringBuilder sb = new StringBuilder();
            sb.append(i==0?"0":String.valueOf((i*30)+1));
            sb.append("-");
            sb.append(String.valueOf((i+1)*30));
            DunningReportRecordAgedDebt agedDebt = new DunningReportRecordAgedDebt();
            agedDebt.setPeriod(sb.toString());
            agedDebt.setValue(0);
            agedDebts.add(agedDebt);
        }

        StringBuilder sb = new StringBuilder();
        sb.append(String.valueOf((agedDebtBreakdown*30)+1));
        sb.append("+");
        DunningReportRecordAgedDebt agedDebt = new DunningReportRecordAgedDebt();
        agedDebt.setPeriod(sb.toString());
        agedDebt.setValue(0);
        agedDebts.add(agedDebt);

        return agedDebts;
    }
	
	public static List<Date> createAgedDebtDatesList(final Context context, final int spid,Date runningDate)
    {
        int agedDebtBreakdown = SystemSupport.getDunningReportAgedDebtBreakdown(context, spid);
        List<Date> agedDebtDate = new ArrayList<Date>();
        for (int i=0;i<agedDebtBreakdown;i++)
        {
            agedDebtDate.add(getAgedDebtDate(context, i+1,runningDate));
        }
        return agedDebtDate;
    }
	
	public static void populateRecordAgedDebt(final Context context, final AgedDebt agedDebt, final List<DunningReportRecordAgedDebt> recordAgedDebts, final List<Date> agedDebtDates, final boolean current)
    {
        long debt = agedDebt.getDebt();
        long accumulatedDebt = agedDebt.getAccumulatedDebt();

        if (current)
        {
            debt = agedDebt.getCurrentDebt();
            accumulatedDebt = agedDebt.getCurrentAccumulatedDebt();
        }

        Iterator<DunningReportRecordAgedDebt> recordAgedDebtsIterator = recordAgedDebts.iterator();
        Iterator<Date> agedDebtDatesIterator = agedDebtDates.iterator();
        DunningReportRecordAgedDebt lastRecordAgedDebt = recordAgedDebts.get(recordAgedDebts.size()-1);
        boolean processed = false;

        while (agedDebtDatesIterator.hasNext() && recordAgedDebtsIterator.hasNext())
        {
            DunningReportRecordAgedDebt recordAgedDebt = recordAgedDebtsIterator.next();
            Date cutDate = agedDebtDatesIterator.next();
            if (agedDebt.getDebtDate().after(cutDate))
            {
                processed = true;
                recordAgedDebt.setValue(recordAgedDebt.getValue() + debt);
                lastRecordAgedDebt.setValue(accumulatedDebt - debt);
                if (LogSupport.isDebugEnabled(context))
                {
                    StringBuilder sb = new StringBuilder();
                    sb.append("Account :");
                    sb.append(agedDebt.getBAN());
                    sb.append(", agedDebt date : ");
                    sb.append(CoreERLogger.formatERDateDayOnly(agedDebt.getDebtDate()));
                    sb.append(" is after cutDate : ");
                    sb.append(CoreERLogger.formatERDateDayOnly(cutDate));
                    sb.append(". Set RecordAgedDebt : ");
                    sb.append(recordAgedDebt);
                    
                    LogSupport.debug(context, DunningReportGenerationHelper.class, sb.toString());
                }
                break;
            }
            else
            {
                if (LogSupport.isDebugEnabled(context))
                {
                    StringBuilder sb = new StringBuilder();
                    sb.append("Ignoring. Account :");
                    sb.append(agedDebt.getBAN());
                    sb.append(", agedDebt date : ");
                    sb.append(CoreERLogger.formatERDateDayOnly(agedDebt.getDebtDate()));
                    sb.append(" is before cutDate : ");
                    sb.append(CoreERLogger.formatERDateDayOnly(cutDate));
                    sb.append(". Set RecordAgedDebt : ");
                    sb.append(recordAgedDebt);
                   
                    LogSupport.debug(context, DunningReportGenerationHelper.class, sb.toString());
                }
            }
        }

        if (!processed)
        {
            lastRecordAgedDebt.setValue(accumulatedDebt);
        }

        if (LogSupport.isDebugEnabled(context))
        {
            StringBuilder sb = new StringBuilder();
            sb.append("Populated last record Aged debt. Account :");
            sb.append(agedDebt.getBAN());
            sb.append(", lastRecordAgedDebt : ");
            sb.append(lastRecordAgedDebt);
            
            LogSupport.debug(context, DunningReportGenerationHelper.class, sb.toString());
        }
    }
	
	private static Date getAgedDebtDate(final Context context, final int month,Date runningDate)
    {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(runningDate);
        calendar.add(Calendar.MONTH, -month);
        return calendar.getTime();
    }
}
