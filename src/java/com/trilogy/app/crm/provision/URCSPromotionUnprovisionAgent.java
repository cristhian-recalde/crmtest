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
package com.trilogy.app.crm.provision;

import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.LogSupport;

import com.trilogy.app.crm.bean.Service;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.blackberry.BlackberrySupport;
import com.trilogy.app.crm.blackberry.error.ErrorHandler;
import com.trilogy.app.crm.extension.ExtensionAssociationException;
import com.trilogy.app.crm.extension.auxiliaryservice.core.custom.URCSPromotionAuxSvcExtension;
import com.trilogy.app.crm.extension.service.URCSPromotionServiceExtensionHome;
import com.trilogy.app.crm.extension.service.URCSPromotionServiceExtensionXInfo;
import com.trilogy.app.crm.extension.service.core.URCSPromotionServiceExtension;

public class URCSPromotionUnprovisionAgent extends CommonProvisionAgent {

	public URCSPromotionUnprovisionAgent() {

	}

	public void execute(Context ctx) throws AgentException {
		Subscriber subscriber = getSubscriber(ctx);
		Service service = getService(ctx);

		long serviceOption = getURCSPromotionServiceOption(ctx, service.getID());
		
		if (serviceOption == -1)
		{
			throw new AgentException (" Unable to find service option for the Bolt on service");
		}
		try 
		{
			URCSPromotionAuxSvcExtension.provisionUrcsPromotion(ctx,
					subscriber, serviceOption, false);
		} 
		catch (ExtensionAssociationException ex) 
		{
			throw new AgentException(ex.getMessage(), ex);
		}

	}

	public static long getURCSPromotionServiceOption(Context ctx, long serviceId) 
	{
		Home h = (Home) ctx.get(URCSPromotionServiceExtensionHome.class);
		try 
		{
			URCSPromotionServiceExtension serviceExtension = (URCSPromotionServiceExtension) h
					.find(ctx,
							new EQ(
									URCSPromotionServiceExtensionXInfo.SERVICE_ID,
									Long.valueOf(serviceId)));
			if (serviceExtension != null)
			{
				return serviceExtension.getServiceOption();
			}
		} 
		catch (HomeException e) 
		{
			LogSupport.minor(ctx, BlackberrySupport.class,
					"Unable to retrieve BlackBerry services for service "
							+ serviceId + ": " + e.getMessage(), e);
		}
		return -1;
	}


}
