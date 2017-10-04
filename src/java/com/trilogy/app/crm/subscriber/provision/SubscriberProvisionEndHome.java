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
package com.trilogy.app.crm.subscriber.provision;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeOperationEnum;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.home.cmd.CreateSubscriberCmd;
import com.trilogy.app.crm.home.sub.SubscriberPipeLineContextPrepareHome;
import com.trilogy.app.crm.support.Lookup;

/**
 * @author jchen
 *
 * Mark error state for checking provisioning result,
 * It also acts as HomeOperation redirect control. see CreateSubscriberCmd.java
 */
public class SubscriberProvisionEndHome extends HomeProxy 
{
	/**
	 * @param delegate
	 */
	public SubscriberProvisionEndHome(Home delegate) 
	{
		super(delegate);
		
	}
	
	/**
	 * @see com.redknee.framework.xhome.home.HomeSPI#create(com.redknee.framework.xhome.context.Context, java.lang.Object)
	 */
	@Override
    public Object create(Context ctx, Object obj) throws HomeException
	{
		LogSupport.debug(ctx, this, "SubscriberPipeline[create].....");
		Subscriber sub = (Subscriber)obj;
		
		markErrorStateForProvisioningFailure(ctx, null, sub);
		if (ctx.getBoolean(getSubscriberCreatedKey(sub), false))
		{
		    // Don't screw over downstream homes.  Give them the same pre-loaded context that it's store methods
		    // would expect had they been anywhere else in the pipeline ...
			return new SubscriberPipeLineContextPrepareHome(getDelegate()).store(ctx, obj);
		}
		
		
		return super.create(ctx, obj);
	}

	/**
	 * @param ctx
	 * @param newSub
	 */
	private void markErrorStateForProvisioningFailure(Context ctx, Subscriber oldSub, Subscriber newSub) 
	{
//		int lastResult = lastResult = SubscriberProvisionResultCode.getProvisionLastResultCode(ctx);
//		if (lastResult != 0)
//		{	
//			if (oldSub == null) // for creating, we set it to pending
//				newSub.setState(SubscriberStateEnum.PENDING);	
//			else
//			{//for storing method, we revert back to the starting state.
//				newSub.setState(oldSub.getState());
//			}
//		}
	}

	/**
	 * @see com.redknee.framework.xhome.home.HomeSPI#store(com.redknee.framework.xhome.context.Context, java.lang.Object)
	 */
	@Override
    public Object store(Context ctx, Object obj) throws HomeException
	{
		LogSupport.debug(ctx, this, "SubscriberPipeline[store].....");
		Subscriber newSub = (Subscriber)obj;
		Subscriber oldSub = (Subscriber) ctx.get(Lookup.OLDSUBSCRIBER);
		
		markErrorStateForProvisioningFailure(ctx, oldSub, newSub);
		return super.store(ctx, newSub);
	}
	
	   /**
     * @see com.redknee.framework.xhome.home.HomeSPI#removee(com.redknee.framework.xhome.context.Context, java.lang.Object)
     */
    @Override
    public void remove(Context ctx, Object obj) throws HomeException
    {
    	LogSupport.debug(ctx, this, "SubscriberPipeline[remove].....");
    	Subscriber newSub = (Subscriber)obj;
        
        super.remove(ctx, newSub);
    }
	
	/** 
	 * see comments on StoreSubscriberCmd
	 * 
	 * @see com.redknee.framework.xhome.home.HomeSPI#cmd(com.redknee.framework.xhome.context.Context, java.lang.Object)
	 */
	@Override
    public Object cmd(Context ctx, Object arg) throws HomeException
	{
		LogSupport.debug(ctx, this, "SubscriberPipeline[cmd].....");
		// TODO: move somewhere else... why are these here? was it too hard for one to call home.create or home.store? [psperneac]
		if (arg instanceof CreateSubscriberCmd)
		{
			CreateSubscriberCmd cmd = (CreateSubscriberCmd)arg;
			Subscriber sub = cmd.getSubscriber();
			HomeOperationEnum op = cmd.getHomeOperation();
			
			Object obj = null;
			if (op.equals(HomeOperationEnum.CREATE))
            {
                obj =  getDelegate().create(ctx, sub);
            }
            else if (op.equals(HomeOperationEnum.STORE))
            {
                obj =  store(ctx, sub);
            }
            else if (op.equals(HomeOperationEnum.REMOVE))
			{
			    remove(ctx,sub);
			    obj = sub;
			}
			//mark it, so when the pipe line goes to create again, we redirect to store()
			//ctx.put(getSubscriberCreatedKey(sub), true);
			return obj;
			
		}
        else
        {
            return super.cmd(ctx, arg);
        }
	}
	
	public static String getSubscriberCreatedKey(Subscriber sub)
	{
		return CONTEXT_KEY_SUBSCRBER_CREATED + sub.getId();
	}
	final static String CONTEXT_KEY_SUBSCRBER_CREATED = "com.redknee.app.crm.subscriber.provision.subCreated.";
}
