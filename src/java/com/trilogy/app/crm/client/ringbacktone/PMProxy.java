package com.trilogy.app.crm.client.ringbacktone;

import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xlog.log.PMLogMsg;

/**
 * Not intended for use outside of the RBT package.
 * Take a performance measurement of each client call.
 * 
 * @author Nick Landry
 *
 */
class PMProxy extends ClientProxy
{
	public PMProxy(RBTClient delegate)
	{
		super(delegate);
	}
	
	public void createSubscriber(Context context, Subscriber subscriber) throws RBTClientException
	{
		PMLogMsg pm = new PMLogMsg(PM_MODULE_NAME, "RBT_addSub");
		super.createSubscriber(context, subscriber);
		pm.log(context);
	}

	public void deleteSubscriber(Context context, String msisdn) throws RBTClientException
	{
		PMLogMsg pm = new PMLogMsg(PM_MODULE_NAME, "RBT_removeSub");
		super.deleteSubscriber(context, msisdn);
		pm.log(context);
	}

	public void updateSubscriberMSISDN(Context context, String oldMsisdn, String newMsisdn)
			throws RBTClientException
	{
		PMLogMsg pm = new PMLogMsg(PM_MODULE_NAME, "RBT_switchMSISDN");
		super.updateSubscriberMSISDN(context, oldMsisdn, newMsisdn);
		pm.log(context);
	}

	public void updateSubscriberReactivate(Context context, String msisdn)
			throws RBTClientException
	{
		PMLogMsg pm = new PMLogMsg(PM_MODULE_NAME, "RBT_unSuspendSub");
		super.updateSubscriberReactivate(context, msisdn);
		pm.log(context);
	}

	public void updateSubscriberSuspend(Context context, String msisdn)
			throws RBTClientException
	{
		PMLogMsg pm = new PMLogMsg(PM_MODULE_NAME, "RBT_suspendSub");
		super.updateSubscriberSuspend(context, msisdn);
		pm.log(context);
	}
	
	public final static String PM_MODULE_NAME = "Ring Back Tone Client"; 
}
