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

import com.trilogy.framework.xhome.beans.ExceptionListener;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.home.HomeOperationEnum;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;

import com.trilogy.app.crm.Common;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.client.ProvisioningHomeException;
import com.trilogy.app.crm.home.cmd.CreateSubscriberCmd;
import com.trilogy.app.crm.log.ERLogger;
import com.trilogy.app.crm.subscriber.provision.SubscriberProvisionEndHome;
import com.trilogy.app.crm.subscriber.provision.SubscriberProvisionResultCode;
/**
 * This class performs the CREATE command to the database, prior to continuing
 * to the provision external applications.  The create-in-advance stragety was 
 * chosen to save the Subscriber in "pending" state before the services were 
 * provisioned, while another save after the services are successfully provisioned
 * will save the "active" subscriber state.
 *
 * Original execution of the pre-create was in SubscriberProvisionUpsHome.create(Context, Object).
 * 
 * Original author: @author joe.chen@redknee.com
 * Refactored by: @author angie.li@redknee.com
 *
 */
public class SubscriberPreCreateHome extends HomeProxy 
{
	public SubscriberPreCreateHome(Home delegate)
	{
		super(delegate);
	}

	@Override
    public Object create(Context ctx, Object obj)
	    throws HomeException, HomeInternalException
	{
		Subscriber newSub = (Subscriber)obj;
		
		// this is a different logic than the fs. according to the fs, provisioning will continue even
        // if we fail to add subscriber to the database. this is silly because you will end up with
        // a subscriber with everything provisioned but no way for you to access that profile
		//SubscriberStateEnum oldState = newSub.getState();
		//newSub.setState(SubscriberStateEnum.ERROR);
		newSub = preCreateSubscriber(ctx, newSub);
		//newSub.setState(oldState);
		
		return super.create(ctx, newSub);
	}
	

    Subscriber preCreateSubscriber(Context ctx, Subscriber subscriber) throws HomeException
    {
    	Subscriber retSub = subscriber;
      // [CW] have to fill in something for the context so subscriber.toString works correctly
      if (subscriber.getContextInternal() == null)
      {
         subscriber.setContext(ctx);
      }
        try
        {
            if (LogSupport.isDebugEnabled(ctx))
            {
                new DebugLogMsg(
                    this,
                    "with cleaned services override " + subscriber.toString(),
                    null).log(
                        ctx);
            }
            retSub = (Subscriber) getDelegate().cmd(ctx, new CreateSubscriberCmd(subscriber, HomeOperationEnum.CREATE));
            // mark it, so when the pipe line goes to create again, we redirect to store()
			ctx.put(SubscriberProvisionEndHome.getSubscriberCreatedKey(subscriber), true);

            //call cmd instead of getDelegate().create(....);
        }
        catch (Exception hex)
        {
            ExceptionListener exptListener = (ExceptionListener) ctx.get(ExceptionListener.class);
            if (exptListener != null)
            {
                exptListener.thrown(hex);
            }

            SubscriberProvisionResultCode.setProvisionCrmResultCode(ctx, 3009);
            SubscriberProvisionResultCode.setProvisionLastResultCode(ctx, 3009);

            // generate subscriber activate failed - out of sync alarm
            ERLogger.logOutOfSync10339(ctx, subscriber, hex, this, 3009);
            //new OMLogMsg(Common.OM_MODULE, Common.OM_CRM_PROV_ERROR).log(ctx);

            throw new ProvisioningHomeException(
                "provisioning result 3009: failed to provision bas subscriber profile with pending state ["
                + hex.getMessage()
                + "]", 3009,  Common.OM_CRM_PROV_ERROR, hex);
        }
        return retSub;
    }
}
