package com.trilogy.app.crm.client.ringbacktone;

import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xlog.log.OMLogMsg;

/**
 * Not intended for use outside of the RBT package.
 * Count each client's operation's attempts, success, and fail.
 * 
 * @author Nick Landry
 *
 */
class OMProxy extends ClientProxy
{

	public OMProxy(RBTClient delegate)
	{
		super(delegate);
	}

	public void createSubscriber(Context context, Subscriber subscriber) throws RBTClientException
	{
		new OMLogMsg(OM_MODULE_NAME, "RBT_addSub_attempt").log(context);
		
		try
		{
			super.createSubscriber(context, subscriber);
			new OMLogMsg(OM_MODULE_NAME, "RBT_addSub_success").log(context);
		}
		catch (RBTClientException e)
		{
			new OMLogMsg(OM_MODULE_NAME, "RBT_addSub_fail").log(context);
			throw e;
		}
	}

	public void deleteSubscriber(Context context, String msisdn) throws RBTClientException
	{
		new OMLogMsg(OM_MODULE_NAME, "RBT_removeSub_attempt").log(context);
		
		try
		{
			super.deleteSubscriber(context, msisdn);
			new OMLogMsg(OM_MODULE_NAME, "RBT_removeSub_success").log(context);
		}
		catch (RBTClientException e)
		{
			new OMLogMsg(OM_MODULE_NAME, "RBT_removeSub_fail").log(context);
			throw e;
		}
	}

	public void updateSubscriberMSISDN(Context context, String oldMsisdn, String newMsisdn)
			throws RBTClientException
	{
		new OMLogMsg(OM_MODULE_NAME, "RBT_switchMSISDN_attempt").log(context);
		
		try
		{
			super.updateSubscriberMSISDN(context, oldMsisdn, newMsisdn);
			new OMLogMsg(OM_MODULE_NAME, "RBT_switchMSISDN_success").log(context);
		}
		catch (RBTClientException e)
		{
			new OMLogMsg(OM_MODULE_NAME, "RBT_switchMSISDN_fail").log(context);
			throw e;
		}
	}

	public void updateSubscriberReactivate(Context context, String msisdn)
			throws RBTClientException
	{
		new OMLogMsg(OM_MODULE_NAME, "RBT_unSuspendSub_attempt").log(context);
		
		try
		{
			super.updateSubscriberReactivate(context, msisdn);
			new OMLogMsg(OM_MODULE_NAME, "RBT_unSuspendSub_success").log(context);
		}
		catch (RBTClientException e)
		{
			new OMLogMsg(OM_MODULE_NAME, "RBT_unSuspendSub_fail").log(context);
			throw e;
		}
		
	}

	public void updateSubscriberSuspend(Context context, String msisdn)
			throws RBTClientException
	{
		new OMLogMsg(OM_MODULE_NAME, "RBT_suspendSub_attempt").log(context);
		
		try
		{
			super.updateSubscriberSuspend(context, msisdn);
			new OMLogMsg(OM_MODULE_NAME, "RBT_suspendSub_success").log(context);
		}
		catch (RBTClientException e)
		{
			new OMLogMsg(OM_MODULE_NAME, "RBT_suspendSub_fail").log(context);
			throw e;
		}
	}
	
	public final String OM_MODULE_NAME = "Ring Back Tone Client";
}
