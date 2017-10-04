package com.trilogy.app.crm.bean.paymentgatewayintegration;

import java.util.Date;

import com.trilogy.app.crm.bean.TopUpSchedule;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.visitor.AbortVisitException;
import com.trilogy.framework.xhome.visitor.Visitor;
import com.trilogy.framework.xlog.log.MinorLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;

public class PostpaidTopUpScheduleOutputVisitor
implements Visitor
{

	public PostpaidTopUpScheduleOutputVisitor(AdmerisDirectDebitOutputWriter writer)
	{
		this.writer = writer;
	}
	
	@Override
	public void visit(Context ctx, Object obj) throws AgentException,
			AbortVisitException 
	{
		final TopUpSchedule origTopUpSchedule = (TopUpSchedule) obj; 
		

		TopUpSchedule topUpSchedule = null;
	 		try 
	 		{
	 			topUpSchedule = (TopUpSchedule) origTopUpSchedule.clone();
	 		} 
	 		catch (Exception ex) 
	 		{
	             throw new AgentException("Could not clone the TopUpSchedule " +obj , ex);
	 		}
	 		
		
		final StringBuilder buf = new StringBuilder();
        buf.append("TopUpSchedule getting Processed : Top Up Schedule ID : [");
        buf.append(topUpSchedule.getId());
        buf.append("], BAN : [");
        buf.append(topUpSchedule.getBan());
        buf.append("], Subscription ID : [");
        buf.append(topUpSchedule.getSubscriptionId());
        buf.append("], MSISDN : [");
        buf.append(topUpSchedule.getMsisdn());
        buf.append("], Amount : [");
        buf.append(topUpSchedule.getAmount());
        buf.append("], Next Application Date : [");
        buf.append(topUpSchedule.getNextApplication());
        buf.append("], SystemType / Billing Type : [");
        buf.append(topUpSchedule.getSystemType());
        buf.append("], Invoice ID : [");
        buf.append(topUpSchedule.getInvoiceId());
        buf.append("], Invoice Due : [");
        buf.append(topUpSchedule.getInvoiceDueDate());
        buf.append("]");
        LogSupport.info(ctx, this, buf.toString());
		
		try 
		{
			writer.printLine(ctx, topUpSchedule);
		} 
		catch (Throwable t)
		{
			new MinorLogMsg(this, "Fail to generate Direct Debit record for Credit Card Top Up Schedule, ID : [" + topUpSchedule.getId() + "], "
									+ " Account : [" + topUpSchedule.getBan() + "]", t).log(ctx); 
		}
 
	}
	
	AdmerisDirectDebitOutputWriter writer;
	 
}
