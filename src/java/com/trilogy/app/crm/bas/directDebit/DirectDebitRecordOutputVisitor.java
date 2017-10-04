package com.trilogy.app.crm.bas.directDebit;

import java.util.Date;

import com.trilogy.app.crm.bean.DirectDebitConstants;
import com.trilogy.app.crm.bean.DirectDebitRecord;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.visitor.AbortVisitException;
import com.trilogy.framework.xhome.visitor.Visitor;
import com.trilogy.framework.xlog.log.MajorLogMsg;

public class DirectDebitRecordOutputVisitor
implements Visitor
{

	public DirectDebitRecordOutputVisitor(DirectDebitOutputWriterFactory factory)
	{
		this.factory_ = factory; 
	}
	
	@Override
	public void visit(Context ctx, Object obj) throws AgentException,
			AbortVisitException 
	{
		DirectDebitRecord record = (DirectDebitRecord)obj; 
		DirectDebitOutputWriter writer = factory_.getWriter(ctx, record);
		if (writer != null)
		{	
			try 
			{
				writer.printLine(ctx, record);
				record.setState(DirectDebitConstants.DD_STATE_PENDING);
				record.setPostDate(new Date()); 
				HomeSupportHelper.get(ctx).storeBean(ctx, record);
			} catch (Throwable t)
			{
				new MajorLogMsg(this, "fail to update direct Debit record or generate direct debit output " + record.getId(), t).log(ctx); 
			}
		} 
	}

	
	DirectDebitOutputWriterFactory factory_; 
}
