package com.trilogy.app.crm.client.ringbacktone;

import java.util.Arrays;

import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;

/**
 * Not intended for use outside of the RBT package.
 * Log debug messages for entry and exit of each client method. 
 * In order for the correct class name to show up in the logs this should be
 * directly before the client in the proxy chain.
 * 
 * @author Nick Landry
 *
 */
class DebugLogProxy extends ClientProxy
{
	public DebugLogProxy(RBTClient delegate)
	{
		super(delegate);
	}
	
	@Override
	public void createSubscriber(Context context, Subscriber subscriber) throws RBTClientException
	{
		if (LogSupport.isDebugEnabled(context))
		{
			debugEntry(context, "createSubscriber", "SubscriberId="+subscriber.getId(),"MSISDN="+subscriber.getMSISDN());
			
			try
			{
				super.createSubscriber(context, subscriber);
				debugReturn(context, "createSubscriber");
			}
			catch (RBTClientException e)
			{
				debugException(context, "createSubscriber", e);
				throw e;
			}
		}
		else
		{
			super.createSubscriber(context, subscriber);
		}
	}

	@Override
	public void deleteSubscriber(Context context, String msisdn) throws RBTClientException
	{
		if (LogSupport.isDebugEnabled(context))
		{
            debugEntry(context, "deleteSubscriber", "MSISDN="+msisdn);
			
			try
			{
				super.deleteSubscriber(context, msisdn);
				debugReturn(context, "deleteSubscriber");
			}
			catch (RBTClientException e)
			{
				debugException(context, "deleteSubscriber", e);
				throw e;
			}
		}
		else
		{
			super.deleteSubscriber(context, msisdn);
		}
	}
	
	public void updateSubscriberMSISDN(Context context, String oldMsisdn, String newMsisdn)
			throws RBTClientException
	{

		if (LogSupport.isDebugEnabled(context))
		{
            debugEntry(context, "updateSubscriberMSISDN", "OldMSISDN="+oldMsisdn, "NewMsisdn="+newMsisdn);
			
			try
			{
				super.updateSubscriberMSISDN(context, oldMsisdn, newMsisdn);
				debugReturn(context, "updateSubscriberMSISDN");
			}
			catch (RBTClientException e)
			{
				debugException(context, "updateSubscriberMSISDN", e);
				throw e;
			}
		}
		else
		{
			super.updateSubscriberMSISDN(context, oldMsisdn, newMsisdn);	
		}
	}

	@Override
	public void updateSubscriberReactivate(Context context, String msisdn)
			throws RBTClientException
	{

		if (LogSupport.isDebugEnabled(context))
		{
            debugEntry(context, "updateSubscriberReactivate", "MSISDN="+msisdn);
			
			try
			{
				super.updateSubscriberReactivate(context,msisdn);
				debugReturn(context, "updateSubscriberReactivate");
			}
			catch (RBTClientException e)
			{
				debugException(context, "updateSubscriberReactivate", e);
				throw e;
			}
		}
		else
		{
			super.updateSubscriberReactivate(context, msisdn);		
		}
	
	}

	@Override
	public void updateSubscriberSuspend(Context context, String msisdn)
			throws RBTClientException
	{
		if (LogSupport.isDebugEnabled(context))
		{
            debugEntry(context, "updateSubscriberSuspend", "MSISDN="+msisdn);
			
			try
			{
				super.updateSubscriberSuspend(context, msisdn);
				debugReturn(context, "updateSubscriberSuspend");
			}
			catch (RBTClientException e)
			{
				debugException(context, "updateSubscriberSuspend", e);
				throw e;
			}
		}
		else
		{
			super.updateSubscriberSuspend(context, msisdn);		
		}
	}

	private void debugEntry(Context context, String methodName, String... params)
	{
		new DebugLogMsg(getDelegate(context), methodName + ": BEGIN "+Arrays.toString(params), null).log(context);
	}
	
	private void debugReturn(Context context, String methodName)
	{
		new DebugLogMsg(getDelegate(context), methodName + ": RETURNED void", null).log(context);
	}
	
	private void debugReturn(Context context, String methodName, Object returnValue)
	{
		new DebugLogMsg(getDelegate(context), methodName + ": RETURNED " + returnValue, null).log(context);
	}

	private void debugException(Context context, String methodName, RBTClientException e)
	{
		new DebugLogMsg(getDelegate(context), methodName + ": THREW Exception ", e).log(context);
	}
}
