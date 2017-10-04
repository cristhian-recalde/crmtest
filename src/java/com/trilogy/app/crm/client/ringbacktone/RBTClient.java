package com.trilogy.app.crm.client.ringbacktone;

import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.framework.xhome.context.Context;


/**
 * Interface for provisioning the Ring-Back Tone client.
 * 
 * @author Nick Landry
 *
 */
public interface RBTClient
{
	// TODO: determine parameters
	public void createSubscriber(Context context, Subscriber subscriber) throws RBTClientException;
	
	// TODO: determine parameters
	public void deleteSubscriber(Context context, String msisdn) throws RBTClientException;
	
	// TODO: determine parameters
	public void updateSubscriberMSISDN(Context context, String oldMsisdn, String newMsisdn) throws RBTClientException;
	
	// TODO: determine parameters
	public void updateSubscriberSuspend(Context context, String msisdn) throws RBTClientException;
	
	// TODO: determine parameters
	public void updateSubscriberReactivate(Context context, String msisdn) throws RBTClientException; 
	
	public Long getSubscriberNotFoundErrorCode();

	// TODO: Are we supporting deleting RBT content from the album?
	//public void deleteRBT() throws RBTClientException;
}
