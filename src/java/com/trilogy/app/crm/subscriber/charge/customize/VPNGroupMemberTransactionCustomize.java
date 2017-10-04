package com.trilogy.app.crm.subscriber.charge.customize;

import com.trilogy.framework.xhome.context.Context;

import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.core.Transaction;

public class VPNGroupMemberTransactionCustomize implements TransactionCustomize
{

	public VPNGroupMemberTransactionCustomize(Subscriber leader)
	{
		 this.groupLeader = leader; 
	}

	public Transaction customize(Context ctx, Transaction trans)
	{		
        trans.setSupportedSubscriberID(trans.getSubscriberID());
        trans.setBAN(groupLeader.getBAN());
        trans.setSpid(groupLeader.getSpid());
        trans.setMSISDN(groupLeader.getMSISDN());
        trans.setSubscriberID(groupLeader.getId());
		return trans; 
	}

	public void setDelegate(TransactionCustomize delegate)
    {
    	
    }


	Subscriber groupLeader; 
}
