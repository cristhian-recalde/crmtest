/*
 * Created on Jul 13, 2005
 *
 * This code is a protected work and subject to domestic and international
 * copyright law(s).  A complete listing of authors of this work is readily
 * available.  Additionally, source code is, by its very nature, confidential
 * information and inextricably contains trade secrets and other information
 * proprietary, valuable and sensitive to Redknee.  No unauthorized use,
 * disclosure, manipulation or otherwise is permitted, and may only be used
 * in accordance with the terms of the license agreement entered into with
 * Redknee Inc. and/or its subsidiaries.
 *
 * Copyright � Redknee Inc. and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.subscriber.agent;

import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgent;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.pipe.PipelineAgent;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;

import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.filter.SubscriberExpiredPredicate;
import com.trilogy.app.crm.support.SubscriptionNotificationSupport;
import com.trilogy.app.crm.support.SpidSupport;
import com.trilogy.app.crm.support.SystemSupport;

/**
 * Preexpires a subscriber passed in the context as Subscriber.class. Can be used as part of pipelines or
 * independently as PreExpireSubscriber.instance().preExpireSubscriber(ctx,sub);
 * 
 * @author psperneac
 *
 */
public class PreExpireSubscriber extends PipelineAgent
{
	protected static PreExpireSubscriber instance__;
	
	public static PreExpireSubscriber instance()
	{
		if(instance__==null)
		{
			instance__=new PreExpireSubscriber();
		}
		
		return instance__;
	}
	
	public PreExpireSubscriber()
	{
		super();
	}

	public PreExpireSubscriber(ContextAgent delegate)
	{
		super(delegate);
	}

	@Override
    public void execute(Context ctx) throws AgentException
	{
		try
		{
            if (!SystemSupport.supportsUnExpirablePrepaidSubscription(ctx))
            {
                Subscriber sub = (Subscriber) require(ctx, this, Subscriber.class);
                preExpireSubscriber(ctx, sub);
            }
		}
		catch(Throwable th)
		{
			if(LogSupport.isDebugEnabled(ctx))
			{
				new DebugLogMsg(this,th.getMessage(),th).log(ctx);
			}
		}
		
		pass(ctx,this);
	}

	public void preExpireSubscriber(Context ctx, Subscriber sub) throws HomeException
	{
        int preExpiryDays = getPreExpiryDays(ctx, sub.getSpid());
        if (SpidSupport.needsPreExpiryMsg(ctx, sub.getSpid()) )
        {
        	if(new SubscriberExpiredPredicate(preExpiryDays).f(ctx,sub) && !sub.getPreExpirySmsSent())
        	{
                SubscriptionNotificationSupport.sendPreExpiryNotification(ctx, sub);
            }
        }
    }
    
    /**
     * Returns the number of days before you have to notify a subscriber about his expiry.
     * 
     * @param ctx
     * @param spid the CRMSpid. Only used to get the spidid
     * @return
     * @throws HomeException
     */
    public static int getPreExpiryDays(final Context ctx, CRMSpid spid) throws HomeException 
    {
        if(spid==null)
        {
            return -1;
        }
        
        return getPreExpiryDays(ctx,spid.getSpid());
    }

    /**
     * Returns the number of days before you have to notify a subscriber about his expiry.
     * 
     * @param context
     * @param spid the spid 
     * @return
     * @throws HomeException
     */
    public static int getPreExpiryDays(final Context context, int spid) throws HomeException 
    {
        CRMSpid smsCfg = SpidSupport.getCRMSpid(context,spid);
        if (smsCfg != null)
        {
            return smsCfg.getPreExpirySmsDays();
        }
        
        return -1;
    }
}
