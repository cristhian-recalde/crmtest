/*
 * This code is a protected work and subject to domestic and international copyright
 * law(s). A complete listing of authors of this work is readily available. Additionally,
 * source code is, by its very nature, confidential information and inextricably contains
 * trade secrets and other information proprietary, valuable and sensitive to Redknee. No
 * unauthorized use, disclosure, manipulation or otherwise is permitted, and may only be
 * used in accordance with the terms of the license agreement entered into with Redknee
 * Inc. and/or its subsidiaries.
 * 
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.dunning.visitor;

import java.util.Date;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountSuspensionReasonEnum;
import com.trilogy.app.crm.dunning.DunningProcessException;
import com.trilogy.app.crm.dunning.DunningProcessHelper;
import com.trilogy.app.crm.dunning.DunningReport;
import com.trilogy.app.crm.dunning.DunningReportRecord;
import com.trilogy.app.crm.dunning.processor.DunningProcessor;
import com.trilogy.app.crm.dunning.processor.IgnoreSubscriberAgedDebtDunningProcessor;
import com.trilogy.app.crm.dunning.processor.PolicyBasedDunningProcessor;
import com.trilogy.app.crm.log.CoreERLogger;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.visitor.AbortVisitException;
import com.trilogy.framework.xhome.visitor.Visitor;
import com.trilogy.framework.xlog.log.LogSupport;


/**
 * Visitor responsible to process accounts during dunning report generation and
 * processing.
 * 
 * @author Marcio Marques
 * @since 9.0
 */
public abstract class AbstractDunningReportRecordAccountVisitor implements Visitor
{

    /**
     * 
     */
    private static final long serialVersionUID = 1L;


    /**
     * Create a new AbstractDunningReportRecordAccountVisitor visitor.
     * 
     * @param report
     */
    public AbstractDunningReportRecordAccountVisitor(final DunningReport report)
    {
        report_ = report;
        runningDate_ = report.getReportDate();
    }
    
    @Override
	public void visit(Context ctx, Object obj) throws AgentException,
			AbortVisitException {

        try
        {
            processAccount(ctx, obj);
        }
        catch (DunningProcessException e)
        {
            throw new AgentException(e);
        }
    
		
	}
    
    protected void processAccount(Context context,Object obj) throws DunningProcessException
    {


	    final Account account = (Account) obj;
	    DunningProcessor dunningProcessor = null; 
	    //skiping dunning if account is manually suspended
	    if(account.getSuspensionReason()==AccountSuspensionReasonEnum.Other_INDEX){
        	return;
        }
	    //String processName = getProcessName();
	    //boolean notificationProcess = processName.equals("Pre-Dunning Notification Processing");
	    final Account responsibleAccount = DunningProcessHelper.getResponsibleAccout(context,account);
	    if(DunningProcessHelper.ignoreSubscriberAgedDebt(context,account))
        	dunningProcessor = new IgnoreSubscriberAgedDebtDunningProcessor(getDunningReport().getReportDate());
        else
        	dunningProcessor = new PolicyBasedDunningProcessor(getDunningReport().getReportDate());
	    
	    if (LogSupport.isDebugEnabled(context))
	    {
	        StringBuilder sb = new StringBuilder();
	        sb.append("Executing ");
	        //sb.append(processName);
	        sb.append(" for account '");
	        sb.append(responsibleAccount.getBAN());
	        sb.append("'");
	        
	        LogSupport.debug(context, this, sb.toString());
	    }
	    
	    DunningReportRecord drr = dunningProcessor.generateReportRecord(context, responsibleAccount);
	    if(drr != null)
	    	executeOnActionRequired(context,responsibleAccount,drr);
    
	
    }
    
    protected abstract void executeOnActionRequired(final Context context, final Account account,
            final DunningReportRecord dunningReportRecord)
            throws DunningProcessException;

    protected DunningReport getDunningReport()
    {
    	return report_;
    }
    
	/**
     * Remove a record from the Dunning Report after processing.
     * 
     * @param ctx
     * @param record
     */
    protected void removeRecord(final Context ctx, final DunningReportRecord record)
    {
        if (record != null)
        {
            if (LogSupport.isDebugEnabled(ctx))
            {
                StringBuilder sb = new StringBuilder();
                sb.append("Removing record for account '");
                sb.append(record.getBAN());
                sb.append("' and report date '");
                sb.append(CoreERLogger.formatERDateDayOnly(record.getReportDate()));
                sb.append("'");
                LogSupport.debug(ctx, this, sb.toString());
            }
            try
            {
                HomeSupportHelper.get(ctx).removeBean(ctx, record);
            }
            catch (HomeException e)
            {
                StringBuilder sb = new StringBuilder();
                sb.append("Unable to remove report record for account '");
                sb.append(record.getBAN());
                sb.append("': ");
                sb.append(e.getMessage());
                LogSupport.minor(ctx, this, sb.toString(), e);
            }
        }
    }


    /**
     * Retrieves a record from the report
     * 
     * @param ctx
     * @param BAN
     * @return
     */
    protected DunningReportRecord retrieveRecordFromReport(Context ctx, String BAN)
    {
        DunningReportRecord result = null;
        for (DunningReportRecord record : report_.getRecords(ctx))
        {
            if (record.getBAN().equals(BAN))
            {
                result = record;
                break;
            }
        }
        return result;
    }
    
    
    
    protected Date getReportDate()
    {
    	return report_.getReportDate();
    }
    
    protected Date getRunningDate()
    {
    	return runningDate_;
    }

    private DunningReport report_;
    private Date runningDate_;

    /**
     * Remove a record from the Dunning Report after processing.
     * 
     * @param ctx
     * @param record
     */
    protected void removeRecord(final Context ctx, final String BAN)
    {
        DunningReportRecord record = retrieveRecordFromReport(ctx, BAN);
        if (record != null)
        {
            removeRecord(ctx, record);
        }
    }
    
    
   
}
