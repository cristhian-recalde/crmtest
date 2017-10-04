package com.trilogy.app.crm.bas.directDebit;

import com.trilogy.app.crm.bean.DirectDebitRecord;
import com.trilogy.framework.xhome.context.Context;

public interface DirectDebitOutputWriterFactory 
{
	public DirectDebitOutputWriter getWriter(Context ctx, DirectDebitRecord record);  
}
