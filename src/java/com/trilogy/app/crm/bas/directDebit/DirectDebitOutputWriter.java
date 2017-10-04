package com.trilogy.app.crm.bas.directDebit;

import com.trilogy.app.crm.bean.DirectDebitRecord;
import com.trilogy.framework.xhome.context.Context;

public interface DirectDebitOutputWriter 
{
	public void init(Context ctx, String path, String bankCode, String extension, int spid) throws Exception; 
	public void printLine(Context ctx, DirectDebitRecord record); 
	public void close(); 
}
