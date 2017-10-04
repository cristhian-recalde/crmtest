package com.trilogy.app.crm.transaction.task;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.CopyOnWriteArrayList;

import com.trilogy.app.crm.bean.AccountOverPaymentHistoryHome;
import com.trilogy.app.crm.bean.AccountOverPaymentHistoryXInfo;
import com.trilogy.app.crm.bean.OverPaymentRun;
import com.trilogy.app.crm.bean.OverPaymentRunHome;
import com.trilogy.app.crm.bean.OverPaymentRunStatusEnum;
import com.trilogy.app.crm.bean.OverPaymentRunXInfo;
import com.trilogy.app.crm.lifecycle.LifecycleAgentScheduledTask;
import com.trilogy.app.crm.support.OverPaymentRunSupport;
import com.trilogy.framework.lifecycle.LifecycleException;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.LT;
import com.trilogy.framework.xhome.elang.LTE;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.visitor.Visitor;
import com.trilogy.framework.xlog.log.EntryLogMsg;
import com.trilogy.framework.xlog.log.MajorLogMsg;
import com.trilogy.framework.xlog.log.MinorLogMsg;
import com.trilogy.framework.xlog.log.PMLogMsg;
import com.trilogy.framework.xlog.log.OMLogMsg;


/**
 * 
 */
public class OverPaymentProcessingLifecycleAgent extends LifecycleAgentScheduledTask {

	private List<OverPaymentRun> records_;
    private static final String MODULE = OverPaymentProcessingLifecycleAgent.class.getName();
    private static final long ALARM_ID = 15178l;
    private static final String COMPONENT = "Over Payment Run";
    OMLogMsg overPaymentAttempt = new OMLogMsg(MODULE, "Over_Payment_Transaction_Attempt");
    OMLogMsg overPaymentAttemptSucess = new OMLogMsg(MODULE, "Over_Payment_Transaction_Attempt_Success");
    OMLogMsg overPaymentAttemptFailed = new OMLogMsg(MODULE, "Over_Payment_Transaction_Attempt_Failed");
    public OverPaymentProcessingLifecycleAgent(Context ctx, final String agentId) throws AgentException
    {
        super(ctx, agentId);

            visitorOverPaymentRun_ = new OverPaymentRunTransactionProcessingVisitor(this);
			visitor_ = new OverPaymentTransactionProcessingVisitor(this);
			
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void start(Context ctx) throws LifecycleException
    {
    	
    	Home home = (Home) ctx.get(AccountOverPaymentHistoryHome.class);
    	Home homeOverPaymentRun = (Home) ctx.get(OverPaymentRunHome.class);
    	
    	records_ = new CopyOnWriteArrayList<OverPaymentRun>();
    	PMLogMsg pmLogMsg = new PMLogMsg(MODULE, "OverPaymentProcessingLifecycleAgent Starts To Run Over Payment Distribuition Task");
    	
        try
        {
            And filterOverPaymentRun = new And();
            filterOverPaymentRun.add(new EQ( OverPaymentRunXInfo.STATE, OverPaymentRunStatusEnum.PENDING) );
            filterOverPaymentRun.add(new LTE( OverPaymentRunXInfo.SCHEDULED_DATE_TIME,new Date()));
            homeOverPaymentRun.forEach(ctx, visitorOverPaymentRun_, filterOverPaymentRun);
            resetCounters();
            if(!records_.isEmpty()) 
            {
                for(OverPaymentRun currentRun : records_)
                {
                	OverPaymentRun overPaymentRun = null;          	                         
                    int spid = currentRun.getSpid();
                    int billCycleID = currentRun.getBillCycleId();
                    And filter = new And();
                    filter.add(new LT( AccountOverPaymentHistoryXInfo.NEW_OVERPAYMENT_BALANCE, 0) );
                    filter.add(new EQ( AccountOverPaymentHistoryXInfo.SPID, spid) );
                    filter.add(new EQ( AccountOverPaymentHistoryXInfo.BILL_CYCLE_ID, billCycleID) );
                    filter.add(new EQ( AccountOverPaymentHistoryXInfo.LATEST,true));
                    try{
                    	overPaymentRun = (OverPaymentRun)currentRun.deepClone();
                    	markPaymentRunInProgress(ctx,overPaymentRun);
                    }catch(Exception e)
                    {
                    	new EntryLogMsg(ALARM_ID,this,COMPONENT,"Exception while changing state of OverPaymentRun.",new String[]
                    			{String.valueOf(currentRun.getId())},e).log(ctx);
                    	
                    	continue;
                    }
                    try{
                    	home.forEach(ctx, visitor_, filter);
                    }catch(HomeException e)
                    {
                    	
                    	new EntryLogMsg(ALARM_ID,this,COMPONENT,"Exception while Distribution.",new String[]
                    			{String.valueOf(currentRun.getId())},e).log(ctx);
                    }finally{
                    
                    	updatePaymentRunStatistics(ctx,overPaymentRun);
                    	if(hasRunFailed())
                    	{
                    		OverPaymentRunSupport.createOverPaymentRunRecord(ctx, spid, billCycleID);
                    	}
                    	resetCounters();
                    }
                }
            
            }
            
            
            
        }
        catch (Exception e)
        {
           new EntryLogMsg(ALARM_ID,this,COMPONENT,"",new String[]
        			{String.valueOf("All Eligible Runs")},e).log(ctx);
           
		}finally{
        	records_.clear();
			pmLogMsg.log(ctx);
        }
    }
    
    public void addRecord(OverPaymentRun record)
    {
    	if(records_ != null)
    	{
    		records_.add(record);
    	}
    }
    
    private void markPaymentRunInProgress(Context context,OverPaymentRun overPaymentRun) throws HomeException
    {
    	overPaymentRun.setState(OverPaymentRunStatusEnum.IN_PROGRESS);
    	overPaymentRun.setStartDateTime(new Date());
    	OverPaymentRunSupport.updateOverPaymentRunRecord(context,overPaymentRun);
    }
    
    private void updatePaymentRunStatistics(Context context,OverPaymentRun currentRun) throws HomeException
    {
    	long noOfSuccessfulTransactions = ((OverPaymentTransactionProcessingVisitor)visitor_).getNoOfSuccessfulTransactions();
    	long noOfFailedTransations = ((OverPaymentTransactionProcessingVisitor)visitor_).getNoOfFailedTransactions();
    	long totalNoOfTransactionsProcessed = noOfSuccessfulTransactions + noOfFailedTransations;
    	
    	generateOMLogs(context,totalNoOfTransactionsProcessed,noOfSuccessfulTransactions,noOfFailedTransations);
    	currentRun.setState(OverPaymentRunSupport.getStatusBasedOnProcessedResult( noOfFailedTransations));
    	currentRun.setNumProcessed(totalNoOfTransactionsProcessed);
    	currentRun.setNumSuccessful(noOfSuccessfulTransactions);
    	currentRun.setNumFailed(noOfFailedTransations);
    	currentRun.setEndDateTime(new Date());
    	
    	OverPaymentRunSupport.updateOverPaymentRunRecord(context,currentRun);
    }
    
    private boolean hasRunFailed()
    {
    	long noOfFailedTransations = ((OverPaymentTransactionProcessingVisitor)visitor_).getNoOfFailedTransactions();
    	OverPaymentRunStatusEnum status = OverPaymentRunSupport.getStatusBasedOnProcessedResult( noOfFailedTransations);
    	if(status == OverPaymentRunStatusEnum.FAILED)
    		return true;
    	else
    		return false;
    }
    
    private void resetCounters()
    {
    	((OverPaymentTransactionProcessingVisitor)visitor_).resetTransactionCounters();
    }
    
    private void generateOMLogs(Context ctx,long recordsProcessed,long sucess,long failed)
    {
    	overPaymentAttemptSucess.setCount(sucess);
    	overPaymentAttemptSucess.log(ctx);
    	overPaymentAttemptFailed.setCount(failed);
    	overPaymentAttemptFailed.log(ctx);
    	overPaymentAttempt.setCount(recordsProcessed);
    	overPaymentAttempt.log(ctx);
    }

    private Visitor visitor_;
    private Visitor visitorOverPaymentRun_;
    
}
