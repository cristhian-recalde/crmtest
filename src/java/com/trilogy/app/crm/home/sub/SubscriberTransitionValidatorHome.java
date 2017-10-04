/*
 * Created on May 20, 2005
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
package com.trilogy.app.crm.home.sub;

import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.support.Lookup;
import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.beans.CompoundValidator;
import com.trilogy.framework.xhome.beans.Validator;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.*;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;

/**
 * 
 * Does transition specific validation
 * 
 * @author psperneac
 *
 */
public class SubscriberTransitionValidatorHome extends HomeProxy
{
	protected final Validator[][] validators=new Validator[20][20];
	
	public SubscriberTransitionValidatorHome(Home delegate)
	{
		super(delegate);
	}

	/**
	 * @see com.redknee.framework.xhome.home.HomeProxy#store(com.redknee.framework.xhome.context.Context, java.lang.Object)
	 */
	public Object store(Context ctx, Object obj) throws HomeException, HomeInternalException
	{
		SubscriberStateEnum newState=((Subscriber)obj).getState();
		SubscriberStateEnum oldState=((Subscriber)ctx.get(Lookup.OLDSUBSCRIBER)).getState();
		
		try
		{
			validate(ctx,oldState,newState,obj);
	    }
	    catch (IllegalStateException e)
	    {
	       if ( e instanceof CompoundIllegalStateException )
	       {
	    	   throw new HomeException("Cannot validate transition from "+oldState.toString()+" to "+
	    		   newState.toString()+" - root cause:"+e.getMessage(), e);
	       }
	       else
	       {
	    	   throw new HomeException("Cannot validate transition from "+oldState.toString()+" to "+
	    		   newState.toString()+" - root cause:"+e.getMessage(), new CompoundIllegalStateException(e));  
	       }
	    }
		
		return super.store(ctx, obj);
	}

	/**
	 * Adds a specific validation for a state transition
	 * @param ctx
	 * @param oldState
	 * @param newState
	 * @param v
	 */
	public void add(Context ctx,SubscriberStateEnum oldState, SubscriberStateEnum newState,Validator v)
	{
		Object obj=validators[oldState.getIndex()][newState.getIndex()];
		if(obj==null)
		{
			validators[oldState.getIndex()][newState.getIndex()]=v;
			return;
		}
		
		if(obj instanceof CompoundValidator)
		{
			((CompoundValidator)obj).add(v);
		}
		else
		{
			validators[oldState.getIndex()][newState.getIndex()]=new CompoundValidator().add((Validator)obj).add(v);
		}
	}
	
	/**
	 * Checks if a transition from a state to another is valid
	 * @param ctx
	 * @param oldState
	 * @param newState
	 * @param sub
	 * @return
	 */
	public void validate(Context ctx, SubscriberStateEnum oldState, SubscriberStateEnum newState,Object sub)
	{
		Object obj=validators[oldState.getIndex()][newState.getIndex()];
		if(obj==null)
		{
			return;
		}
		
		((Validator)obj).validate(ctx,sub);
	}
	
	
}
