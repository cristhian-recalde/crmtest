/*
 * This code is a protected work and subject to domestic and international
 * copyright law(s).  A complete listing of authors of this work is readily
 * available.  Additionally, source code is, by its very nature, confidential
 * information and inextricably contains trade secrets and other information
 * proprietary, valuable and sensitive to Redknee.  No unauthorized use,
 * disclosure, manipulation or otherwise is permitted, and may only be used
 * in accordance with the terms of the license agreement entered into with
 * Redknee Inc. and/or its subsidiaries.
 *
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.home.sub;

import java.util.Set;

import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberServices;
import com.trilogy.app.crm.bean.SubscriberServicesXInfo;
import com.trilogy.app.crm.bean.service.ServiceStateEnum;
import com.trilogy.app.crm.support.SubscriberServicesSupport;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.filter.Predicate;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xlog.log.LogSupport;

/**
 * This home will be in charge of cleaning up the unwanted Subscriber Services records 
 * that remain after provisioning and charging are completed.
 * 
 * The Subscriber Services that should be deleted are:
 * 	+ Subscriber Service records in the UNPROVISIONED STATE
 * @author ali
 *
 */
public class SubscriberServicesCleanupHome extends HomeProxy 
{

	private static final long serialVersionUID = 1L;

	SubscriberServicesCleanupHome(Context context, Home delegate)
	{
		super(context, delegate);
	}

	public Object create(Context context, Object obj)
	throws HomeException, HomeInternalException
	{
		// Proceed to Provisioning and Charging
		Subscriber sub = (Subscriber) super.create(context, obj);
		// Clean up after processing 
		cleanup(context, sub);
		return sub;
	}

	public Object store(Context context, Object obj)
	throws HomeException, HomeInternalException
	{
		// Proceed to Provisioning and Charging
		Subscriber sub = (Subscriber) super.store(context, obj);
		// Clean up after processing 
		cleanup(context, sub);
		return sub;
	}

	public void remove(Context context, Object obj)
	throws HomeException, HomeInternalException
	{
		getDelegate().remove(obj);
		// Clean up after processing 
		cleanup(context, (Subscriber) obj);
	}
	
	private void cleanup(Context context, Subscriber sub)
	{
		/* Once the state has been set to unprovisioned and all the refunding has been done, 
         * delete the Subscriber Service Record.
         * TODO: should this be done for provisioned with error state? hmm...
         */
    	final String subscriberId = sub.getId();
    	/* Or filter = new Or();
    	filter.add(new EQ(SubscriberServicesXInfo.PROVISIONED_STATE, ServiceStateEnum.UNPROVISIONED));
    	filter.add(new EQ(SubscriberServicesXInfo.PROVISIONED_STATE, ServiceStateEnum.PROVISIONEDWITHERRORS));
    	*/
    	Predicate filter = new EQ(SubscriberServicesXInfo.PROVISIONED_STATE, ServiceStateEnum.UNPROVISIONED);
		Set<SubscriberServices> cleanupServices = SubscriberServicesSupport.getSubscriberServicesByFilter(context, subscriberId, filter);

		if (cleanupServices.size() > 0)
		{
			if (LogSupport.isDebugEnabled(context))
			{
				LogSupport.debug(context, this, 
						"Starting Subscriber Services clean up after provisioning and charging.  Will " +
						"delete any Subscriber Services in the UNPROVISIONED states.");
			}
	        
	        // Remove all remaining services in the list of unselected UNPROVISIONED services.
	        for(SubscriberServices serviceRecord : cleanupServices)
	        {
	            SubscriberServicesSupport.deleteSubscriberServiceRecord(context,serviceRecord);
	        }
		}
	}

}
