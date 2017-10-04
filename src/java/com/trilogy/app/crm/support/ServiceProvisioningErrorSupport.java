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
package com.trilogy.app.crm.support;

import com.trilogy.app.crm.bean.service.ServiceProvisioningError;
import com.trilogy.app.crm.bean.service.ServiceProvisioningErrorHome;
import com.trilogy.app.crm.bean.service.ServiceProvisioningErrorID;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.LogSupport;

/**
 * Utility class to manipulate ServiceProvisioningError.
 * @author Angie Li
 *
 */
public class ServiceProvisioningErrorSupport 
{
    /**
     * Returns the ServiceProvisioningError record identified by the given parameters
     * @param ctx
     * @param serviceId
     * @param subscriberId
     * @return
     * @throws HomeException
     */
    public static ServiceProvisioningError getErrorRecord(
            final Context ctx,
            final long serviceId,
            final String subscriberId)
    throws HomeException
    {
        Home errorHome = (Home) ctx.get(ServiceProvisioningErrorHome.class);

        ServiceProvisioningError error = 
            (ServiceProvisioningError) errorHome.find(new ServiceProvisioningErrorID(serviceId,subscriberId));
        return error;
    }
    
    
    /**
     * Set the wasChargeSucessful flag to the given value, if and only if the requested
     * ServiceProvisioningError record exists. 
     * @param ctx
     * @param serviceId
     * @param subscriberId
     * @param isSuccess
     * @throws HomeException
     */
    public static void changeWasTransactionSuccessful(
            final Context ctx,
            final long serviceId,
            final String subscriberId,
            final boolean isSuccess)
    {
    	try
    	{
    		ServiceProvisioningError record = getErrorRecord(ctx, serviceId, subscriberId);
    		if (record != null)
    		{
    			record.setWasChargeSucessful(isSuccess);
    			Home errorHome = (Home) ctx.get(ServiceProvisioningErrorHome.class);
    			errorHome.store(ctx,record);
    			if (LogSupport.isDebugEnabled(ctx))
    			{
    				LogSupport.debug(ctx, "ServiceProvisioningErrorSupport.changeWasChargeSuccessful", 
    						"Setting the ServiceProvisioningError.wasChargeSuccessful=" + isSuccess);
    			}
    		}
    	}
    	catch (HomeException e)
    	{
    		if (LogSupport.isDebugEnabled(ctx))
			{
				LogSupport.debug(ctx, "ServiceProvisioningErrorSupport.changeWasChargeSuccessful", 
						"Attempt to update ServiceProvisioningError.wasChargeSuccessful was aborted due to error. " 
						+ e.getMessage(), e);
			}
    	}
    }
}
