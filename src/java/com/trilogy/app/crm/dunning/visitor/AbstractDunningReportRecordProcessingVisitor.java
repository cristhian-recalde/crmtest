package com.trilogy.app.crm.dunning.visitor;

import java.util.Date;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountSuspensionReasonEnum;
import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.dunning.DunningProcessException;
import com.trilogy.app.crm.dunning.DunningProcessHelper;
import com.trilogy.app.crm.dunning.DunningReport;
import com.trilogy.app.crm.dunning.processor.DunningProcessor;
import com.trilogy.app.crm.dunning.processor.IgnoreSubscriberAgedDebtDunningProcessor;
import com.trilogy.app.crm.dunning.processor.PolicyBasedDunningProcessor;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.visitor.AbortVisitException;
import com.trilogy.framework.xlog.log.LogSupport;


import com.trilogy.app.crm.dunning.DunningReportRecord;
import com.trilogy.app.crm.dunning.DunningReportRecordStatusEnum;
import com.trilogy.app.crm.log.CoreERLogger;
import com.trilogy.app.crm.support.AccountSupport;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.app.crm.support.SpidSupport;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.visitor.Visitor;
import com.trilogy.framework.xlog.log.MinorLogMsg;


/**
 * 
 * @author odeshpande
 * since 10.2
 */
public abstract class AbstractDunningReportRecordProcessingVisitor implements Visitor
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public AbstractDunningReportRecordProcessingVisitor(DunningReport report)
	{
       	report_ = report;
		runningDate_ = report.getReportDate();		    
	}
	
		
	@Override
	public void visit(Context ctx, Object obj) throws AgentException,
			AbortVisitException {

		final DunningReportRecord reportRecord = (DunningReportRecord)obj;
		try

        {
        	processAccount(ctx, reportRecord);
        	HomeSupportHelper.get(ctx).storeBean(ctx, reportRecord);
                                       
        }catch (DunningProcessException e)
        {
        	markRecordFail(ctx,reportRecord,e.getMessage());
        	throw new AgentException(e);
        }catch (HomeException e)
        {
            StringBuilder sb = new StringBuilder();
            sb.append("Unnable to mark record as processed for the account '");
            sb.append(reportRecord.getBAN());
            sb.append("' in the report on date '");
            sb.append(CoreERLogger.formatERDateDayOnly(reportRecord.getReportDate()));
            sb.append("'.");
            LogSupport.minor(ctx, this, sb.toString());
            throw new AgentException(e);
        }

    }

   
	
	protected void processAccount(Context context,DunningReportRecord reportRecord) throws DunningProcessException
	{
		
        final Account account = getAccount(context,reportRecord);
        
        //skiping dunning if account is manually suspended
        if(account.getSuspensionReason()==AccountSuspensionReasonEnum.Other_INDEX){
        	return;
        }
        //Do we really require to check or find responsible account?No?
        final Account responsibleAccount = getResponsibleAccout(context,account);
        DunningProcessor dunningProcessor=null;
        boolean movedToPtp = checkAndMoveToPtp(context,responsibleAccount,reportRecord);
        if(movedToPtp)
        {
        	reportRecord.setStatus(DunningReportRecordStatusEnum.PROCESSED_INDEX);
        }else{
	        //TODO status of dunning exempted accounts
        	if(DunningProcessHelper.ignoreSubscriberAgedDebt(context,account))
        	dunningProcessor = new IgnoreSubscriberAgedDebtDunningProcessor(getRunningDate());
        	else
        	dunningProcessor = new PolicyBasedDunningProcessor(getRunningDate());
	        
	        if (LogSupport.isDebugEnabled(context))
	        {
	            StringBuilder sb = new StringBuilder();
	            sb.append("Executing ");
	            //sb.append(processName);
	            sb.append(" for account '");
	            sb.append(responsibleAccount.getBAN());
	            sb.append("'");
	            if (!responsibleAccount.getBAN().equals(account.getBAN()))
	            {
	                sb.append(", which is the responsible account for account '");
	                sb.append(account.getBAN());
	                sb.append("'");
	            }
	            LogSupport.debug(context, this, sb.toString());
	        }
	        
	        dunningProcessor.processAccount(context, responsibleAccount);
	        reportRecord.setStatus(DunningReportRecordStatusEnum.PROCESSED_INDEX);
        }
    
	}

	protected abstract boolean checkAndMoveToPtp(Context context,Account account , DunningReportRecord drr) ;
	
	protected Account getResponsibleAccout(Context context,Account account)
	{
		return DunningProcessHelper.getResponsibleAccout(context, account);
	}
	
	   	
	private Account getAccount(Context context,DunningReportRecord drr) throws DunningProcessException
	{
		Account account =null;
        try
        {
        	account = AccountSupport.getAccount(context, drr.getBAN());
        	
        }catch (HomeException e) {
			new MinorLogMsg(this, "Unable to retrieve account for dunning processing: "+ e.getMessage(), e).log(context);
			throw new DunningProcessException(e);
		}
        return account;
	}
	
	private void markRecordFail(final Context ctx, DunningReportRecord record, final String additionalInfo)
    {
        record.setStatus(DunningReportRecordStatusEnum.FAILED_INDEX);
        record.setAdditionalInfo(additionalInfo);
        try{
        	HomeSupportHelper.get(ctx).storeBean(ctx, record);
        }catch(HomeException e)
        {
        	LogSupport.minor(ctx, this, "Exception while saving state of failed report record",e);
        }
        
    }
	
	protected DunningReport getDunningReport()
    {
        return report_;
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

}
