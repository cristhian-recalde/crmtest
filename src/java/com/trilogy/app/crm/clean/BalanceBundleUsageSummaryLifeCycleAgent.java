package com.trilogy.app.crm.clean;

import java.util.Collection;
import java.util.Date;

import com.trilogy.app.crm.bean.CallDetailProcessConfig;
import com.trilogy.app.crm.bean.calldetail.CallDetail;
import com.trilogy.app.crm.bean.calldetail.CallDetailHome;
import com.trilogy.app.crm.bean.calldetail.CallDetailXDBHome;
import com.trilogy.app.crm.bean.calldetail.CallDetailXInfo;
import com.trilogy.app.crm.clean.visitor.CallDetailBalanceBundleUsageVisitor;
import com.trilogy.app.crm.lifecycle.LifecycleAgentScheduledTask;
import com.trilogy.app.crm.support.AccountSupport;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.app.crm.support.MultiDbSupportHelper;
import com.trilogy.framework.lifecycle.LifecycleException;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MajorLogMsg;
/**
 * 
 *
 * @author atul.mundra@redknee.com
 * @since 9.9.1
 */
public class BalanceBundleUsageSummaryLifeCycleAgent extends LifecycleAgentScheduledTask{

	public static final long CONSTANT_PRICEPLAN = 999999999999l;
	public BalanceBundleUsageSummaryLifeCycleAgent(Context ctx, String agentId)
			throws AgentException {
		super(ctx, agentId);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	protected void start(Context ctx) throws LifecycleException, HomeException  {

		 final Date runningDate = getRunningDate(ctx);
		 Context subContext = ctx.createSubContext();
		 
		 processCallDetails(subContext,runningDate);		 
	}

	
	/**
     * Gets the running date.
     * 
     * @param context
     *            The operating context.
     * @return The "current date" for the dunning report generation run.
     * @throws AgentException
     *             thrown if any Exception is thrown during date parsing. Original
     *             Exception is linked.
     */
    private Date getRunningDate(final Context context)
    {
        Date reportDate = getParameter1(context, Date.class);
        if (reportDate==null)
        {
            reportDate = new Date();
        }
        return reportDate;

    }
    private void processCallDetails(Context ctx, Date runningDate) throws HomeException
    {
    	CallDetailProcessConfig processConfig = (CallDetailProcessConfig)ctx.get(CallDetailProcessConfig.class);
    	CallDetailBalanceBundleUsageVisitor visitor 	=	new CallDetailBalanceBundleUsageVisitor();
        String sql = getSqlQueryForCallDetail(ctx, processConfig);
        
        if(sql != null)
        {
        	Collection<Object> callIdList = AccountSupport.getQueryDataList(ctx, sql);
        	for (Object callId : callIdList)
        	{
        		And and = new And();
        		and.add(new EQ(CallDetailXInfo.ID,Long.parseLong(callId.toString())));
        		CallDetail callDetail = HomeSupportHelper.get(ctx).findBean(ctx, CallDetail.class, and );

        		if(callDetail != null)
        		{
        			try
        			{
        				visitor.visit(ctx,callDetail);
        			}
        			catch(Throwable e)
        			{
        				String msg = "CallDetail failed CalldetailId :"+ callDetail.getId() + " SessionId : "+ callDetail.getCallID();
        				new MajorLogMsg(this, msg + " Exception Msg :" + e.getMessage(), e).log(ctx);
        			}
        		}
        	}
        }
    }
    
    private String getSqlQueryForCallDetail(Context ctx, CallDetailProcessConfig processConfig)
    {
       
        final String callDetailTableName = MultiDbSupportHelper.get(ctx).getTableName(ctx, CallDetailHome.class,
        CallDetailXInfo.DEFAULT_TABLE_NAME);
        
        if(processConfig != null)
        {
        	long lastProcessedCallId = processConfig.getCallDetailId();
            Date lastProcessedRecordDate= processConfig.getPostedDate();
            LogSupport.info(ctx, this, "retrieved last processed call Id : "+lastProcessedCallId +" and lastProcessedRecordDate : " +lastProcessedRecordDate + " from CallDetailProcessConfig");
        			
            	StringBuilder query = new StringBuilder();
            	query.append("select ID from ").append(callDetailTableName);
        		if(processConfig != null)
        		{
        			query.append(" where ");
        			if(processConfig.isInProcess())
        			{
        				query.append(" ID >= ").append(lastProcessedCallId );
        			}
        			else
        			{
        				query.append(" ID > ").append(lastProcessedCallId);
        			}
        			if(lastProcessedRecordDate!= null)
        			{
        				query.append(" and postedDate >= ").append(lastProcessedRecordDate.getTime() );
        			}
        		}
            	query.append( " order by postedDate");  	
            	return query.toString();
        }
        return null;
        
    }
}
