package com.trilogy.app.crm.client.ringbacktone;

import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.framework.xhome.context.Context;

/**
 * Not intended for use outside of the RBT package
 * 
 * @author Nick Landry
 *
 */
class ClientProxy implements RBTClient
{

	public ClientProxy(RBTClient delegate)
	{
		delegate_ = delegate;
	}
	
	public RBTClient getDelegate(Context context)
	{
		return delegate_;
	}
	
	public void createSubscriber(Context context, Subscriber subscriber) throws RBTClientException
	{
		getDelegate(context).createSubscriber(context, subscriber);
	}

	public void deleteSubscriber(Context context, String msisdn) throws RBTClientException
	{
		getDelegate(context).deleteSubscriber(context, msisdn);
	}
	
	public void updateSubscriberMSISDN(Context context, String oldMsisdn, String newMsisdn) throws RBTClientException
	{
		getDelegate(context).updateSubscriberMSISDN(context, oldMsisdn, newMsisdn);
	}

	public void updateSubscriberReactivate(Context context, String msisdn) throws RBTClientException
	{
		getDelegate(context).updateSubscriberReactivate(context, msisdn);
	}

	public void updateSubscriberSuspend(Context context, String msisdn) throws RBTClientException
	{
		getDelegate(context).updateSubscriberSuspend(context, msisdn);
	}
	
    public Long getSubscriberNotFoundErrorCode()
    {
        return getDelegate(null).getSubscriberNotFoundErrorCode();
    }

	
	private RBTClient delegate_ = null;
}
