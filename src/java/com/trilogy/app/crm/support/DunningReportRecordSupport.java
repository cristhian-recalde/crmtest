package com.trilogy.app.crm.support;

import com.trilogy.app.crm.dunning.DunningReport;
import com.trilogy.app.crm.dunning.DunningReportRecordHome;
import com.trilogy.app.crm.dunning.DunningReportRecordXInfo;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.xdb.XDB;
import com.trilogy.framework.xlog.log.LogSupport;

public class DunningReportRecordSupport
{

    private static final String CLASS_NAME = DunningReportRecordSupport.class.getName();
    
    public static boolean updateDunningReportRecordMatureState(Context ctx, DunningReport dunningReport, int recordMaturityState, int currentRecordMaturityState)
    {
        final String updateDML =  " set " + DunningReportRecordXInfo.RECORD_MATURITY.getSQLName() + " = " + recordMaturityState + " where "
        + DunningReportRecordXInfo.SPID.getSQLName() + "=" + dunningReport.getSpid()  + " and "
        + DunningReportRecordXInfo.FORECASTED_LEVEL.getSQLName() + "=" + dunningReport.getNextLevel() + " and "
        + DunningReportRecordXInfo.REPORT_DATE.getSQLName() + "=" + dunningReport.getReportDate().getTime() + " and "
        + DunningReportRecordXInfo.RECORD_MATURITY.getSQLName() + "=" + currentRecordMaturityState;
        

        if(LogSupport.isDebugEnabled(ctx))
        {
            LogSupport.debug(ctx, CLASS_NAME, updateDML);
        }
        final XDB xdb = (XDB) ctx.get(XDB.class);
        try
        {
            xdb.execute(
                    ctx,
                    "UPDATE "
                            + MultiDbSupportHelper.get(ctx)
                                    .getTableName(
                                            ctx, (Home)ctx.get(DunningReportRecordHome.class)) + updateDML);
            return true;
        }
        catch (Exception t)
        {
            t.printStackTrace();
            return false;
        }
        
    }
    
    public static DunningReport cloneDunningReport(DunningReport dunningReportToClone)
    {
        DunningReport newDunningReportToCreate = new DunningReport();
        newDunningReportToCreate.setSpid(dunningReportToClone.getSpid());
        newDunningReportToCreate.setReportDate(dunningReportToClone.getReportDate());
        newDunningReportToCreate.setNextLevel(dunningReportToClone.getNextLevel());
        newDunningReportToCreate.setFailedToProcessRecords(dunningReportToClone.getFailedToProcessRecords());
        newDunningReportToCreate.setStatus(dunningReportToClone.getStatus());
        newDunningReportToCreate.setSuccessfullyProcessedRecords(dunningReportToClone.getSuccessfullyProcessedRecords());
        newDunningReportToCreate.setUpToDate(dunningReportToClone.getUpToDate());
        newDunningReportToCreate.setNumberOfRecords(dunningReportToClone.getNumberOfRecords());

        return newDunningReportToCreate;
    }
}
