package com.trilogy.app.crm.subscriber.charge.customize;

import com.trilogy.app.crm.bean.core.Transaction;
import com.trilogy.framework.xhome.context.Context;

public class OneTimeChargeCustomize 
implements TransactionCustomize
{

	public OneTimeChargeCustomize()
	{
	}


	
	public Transaction customize(Context ctx, Transaction trans)
	{		
        trans.setAmount(trans.getFullCharge());
		return trans; 
	}

	public void setDelegate(TransactionCustomize delegate)
    {
    	
    }


}
