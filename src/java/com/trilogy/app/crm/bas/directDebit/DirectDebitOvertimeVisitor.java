package com.trilogy.app.crm.bas.directDebit;

import java.util.Date;

import com.trilogy.app.crm.bean.DirectDebitConstants;
import com.trilogy.app.crm.bean.DirectDebitRecord;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.visitor.AbortVisitException;
import com.trilogy.framework.xhome.visitor.Visitor;

public class DirectDebitOvertimeVisitor 
implements Visitor
{

	@Override
	public void visit(Context ctx, Object obj) throws AgentException,
			AbortVisitException 
	{
		DirectDebitRecord record = (DirectDebitRecord) obj; 
		record.setState(DirectDebitConstants.DD_STATE_FAIL);
		record.setReceiveDate(new Date()); 
		record.setReasonCode(DirectDebitConstants.DD_REASON_CODE_TIMEOUT);
		try
		{
			HomeSupportHelper.get(ctx).storeBean(ctx, record);
		
			record.createTimeOutMessage(ctx);
		} catch (Exception e)
		{
			
		}

	}


}
