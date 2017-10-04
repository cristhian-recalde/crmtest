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
package com.trilogy.app.crm.provision.agent.suspend;

import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;

import com.trilogy.app.crm.bean.Service;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.extension.ExtensionAssociationException;
import com.trilogy.app.crm.extension.auxiliaryservice.core.custom.URCSPromotionAuxSvcExtension;
import com.trilogy.app.crm.provision.URCSPromotionUnprovisionAgent;

public class URCSPromotionSuspendAgent extends CommonSuspendAgent
{

    /**
     * @param ctx
     * 
     */
    public void execute(Context ctx) throws AgentException
    {
        if (LogSupport.isDebugEnabled(ctx))
        {
        	new DebugLogMsg(this, " URCS promotion suspend ", null).log(ctx);
        }
    
		Subscriber subscriber = getSubscriber(ctx);
		Service service = getService(ctx);

		long serviceOption = URCSPromotionUnprovisionAgent.getURCSPromotionServiceOption(ctx, service.getID());
		
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
}