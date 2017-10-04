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

package com.trilogy.app.crm.service.home;

import com.trilogy.app.crm.bean.IdentifierEnum;
import com.trilogy.app.crm.bean.Service;
import com.trilogy.app.crm.support.IdentifierSequenceSupportHelper;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xlog.log.LogSupport;

/**
 * 
 * Sets ID and identifier for this service using an
 * IdentifierSequence. ID is used in SQLServer.
 * 
 * IdentifierSettingHome is for IdentifierAware interface and
 * does not set ID. Hence IdentifierSettingHome is not sufficient.
 * 
 * @author ameya.bhurke@redknee.com
 *
 */
public class ServiceIDSettingHome extends HomeProxy 
{
	private static final long startValue = 100000;
	
	public ServiceIDSettingHome(Context ctx, Home delegate)
	{
		super(ctx, delegate);
	}

	@Override
	public Object create(Context ctx, Object obj) throws HomeException,
			HomeInternalException 
	{
		
		Service service = (Service)obj;
		
		IdentifierSequenceSupportHelper.get(ctx).ensureSequenceExists(ctx, IdentifierEnum.SERVICE_ID,
				startValue, Long.MAX_VALUE);
		
		long serviceID = IdentifierSequenceSupportHelper.get(ctx).getNextIdentifier(
	            ctx,
	            IdentifierEnum.SERVICE_ID,
	            null);
		
		if(service.getID() == 0)
		{
			service.setID(serviceID);
			service.setIdentifier(serviceID);
		}
		
		LogSupport.info(ctx, this, "Service ID set to: " + service.getID());
		
		return super.create(ctx, obj);
	}

	
}
