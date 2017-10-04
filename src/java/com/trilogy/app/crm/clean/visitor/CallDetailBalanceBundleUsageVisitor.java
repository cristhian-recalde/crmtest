package com.trilogy.app.crm.clean.visitor;

import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.CallDetailProcessConfig;
import com.trilogy.app.crm.bean.calldetail.CallDetail;
import com.trilogy.app.crm.support.BalanceBundleUsageSummarySupport;
import com.trilogy.app.crm.support.SpidSupport;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.visitor.AbortVisitException;
import com.trilogy.framework.xhome.visitor.Visitor;
import com.trilogy.framework.xlog.log.ERLogMsg;
import com.trilogy.framework.xlog.log.EntryLogMsg;
import com.trilogy.framework.xlog.log.MajorLogMsg;

public class CallDetailBalanceBundleUsageVisitor implements Visitor {

	private static Visitor instance_ = null;
	private long counter = 0l;
	public static final int SUCCESS = 0;
	public static final int FAILURE = 1;
	public static final int BALANCE_BUNDLE_USAGE_SUMMARY_ERID = 1393;
    public long getCounter() {
		return counter;
	}
	public static Visitor instance()
    {
        if (instance_ == null)
        {
            instance_ = new CallDetailBalanceBundleUsageVisitor();
        }
        return instance_;
    }
	@Override
	public void visit(Context ctx, Object obj) throws AgentException,
			AbortVisitException
	{
		
		CallDetail callDetail	=	(CallDetail)obj;
		CallDetailProcessConfig processConfig = (CallDetailProcessConfig)ctx.get(CallDetailProcessConfig.class);
		
		try 
		{
			CRMSpid spid   = SpidSupport.getCRMSpid(ctx, callDetail.getSpid());
			BalanceBundleUsageSummarySupport balanceBundleUsageSummarySupport = new BalanceBundleUsageSummarySupport();
			balanceBundleUsageSummarySupport.prepareBalanceBundleUsageSummary(ctx,callDetail,spid);
			counter++;
		} 
		catch (HomeException homeException)
		{
			String msg = "CallDetail failed CalldetailId :"+ callDetail.getId() + " SessionId : "+ callDetail.getCallID();
			new MajorLogMsg(this, msg + " Exception Msg :" + homeException.getMessage(), homeException).log(ctx);
			logEr(ctx,callDetail.getSpid(),processConfig.getCallDetailId(),homeException.getMessage(),FAILURE);
			generateAlarm(ctx,callDetail, homeException.getMessage(), homeException);
		}
		catch (Throwable exception)
		{
			String msg = "CallDetail failed CalldetailId :"+ callDetail.getId() + " SessionId : "+ callDetail.getCallID();
			new MajorLogMsg(this, msg + " Exception Msg :" + exception.getMessage(), exception).log(ctx);
			logEr(ctx,callDetail.getSpid(),processConfig.getCallDetailId(),exception.getMessage(),FAILURE);
			generateAlarm(ctx,callDetail, exception.getMessage(), exception);
		}
		
	}
	
	public void logEr(Context ctx,int spid,long callId,String msg, int result)
	{
		final String[] fields = new String[3];
		fields[0] = String.valueOf(callId);
		fields[1] = msg;
		fields[2] = String.valueOf(result);
		new ERLogMsg(BALANCE_BUNDLE_USAGE_SUMMARY_ERID, 700, "Balance Bundle Usage Summary Task ER", spid, fields)
        .log(ctx);
		
	}
	
	 /**
     * Generates Entry-Log (Alarm) for an un-toward event.
     * 
     * @param ctx
     * @param message
     * @param subscriber
     * @param service
     * @param t
     */
    protected void generateAlarm(Context ctx,CallDetail callDetail, String message,Throwable t)
    {
        new EntryLogMsg(15187l, this,"BalanceBundleUsageSummary" , message, (new String[]
            {String.valueOf(callDetail.getId()), message}), t).log(ctx);
    }
	
}
