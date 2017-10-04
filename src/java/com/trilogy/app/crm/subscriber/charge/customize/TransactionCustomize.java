package com.trilogy.app.crm.subscriber.charge.customize;

import com.trilogy.app.crm.bean.core.Transaction;
import com.trilogy.framework.xhome.context.Context;

public interface TransactionCustomize 
{
	public Transaction customize(Context ctx, Transaction trans); 
    public void setDelegate(TransactionCustomize delegate);

}
