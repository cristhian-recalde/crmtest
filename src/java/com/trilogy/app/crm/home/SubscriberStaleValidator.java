/*
 * This code is a protected work and subject to domestic and international
 * copyright law(s). A complete listing of authors of this work is readily
 * available. Additionally, source code is, by its very nature, confidential
 * information and inextricably contains trade secrets and other information
 * proprietary, valuable and sensitive to Redknee, no unauthorised use,
 * disclosure, manipulation or otherwise is permitted, and may only be used
 * in accordance with the terms of the licence agreement entered into with
 * Redknee Inc. and/or its subsidiaries.
 *
 * Copyright � Redknee Inc. and its subsidiaries. All Rights Reserved.
 */

package com.trilogy.app.crm.home;

import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.support.Lookup;
import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.beans.Validator;
import com.trilogy.framework.xhome.context.Context;

/**
 * 
 * Verifies is subscriber object state is stale based on lastModifiedHidden property.
 * 
 * @author ameya.bhurke@redknee.com
 *
 */
public class SubscriberStaleValidator implements Validator 
{

	public static final String MESSAGE = "This Entity state is stale and is modified externally. Please refresh entity and retry.";
	
	private SubscriberStaleValidator() 
	{
		super();
	}
	
	public static final SubscriberStaleValidator getInstance() 
	{
		return instance;
	}

	
	private long getLastModifiedTimestampFromDS(Context ctx) 
	{
		Subscriber oldSubscriber = (Subscriber)ctx.get(Lookup.OLDSUBSCRIBER);
		
		if(oldSubscriber != null) 
		{
			return oldSubscriber.getLastModifiedHidden().getTime();
		}
		else
		{
			throw new IllegalArgumentException("The Subscriber entity in not found in DB. Cannot proceed further.");
		}
	}
	
	@Override
    public void validate(final Context ctx, final Object obj)
    {
        final CompoundIllegalStateException el = new CompoundIllegalStateException();
        
        if(getLastModifiedTimestampFromDS(ctx) > ((Subscriber)obj).getLastModifiedHidden().getTime()) 
        {
        	el.thrown(new IllegalArgumentException(MESSAGE));
        }
        
        el.throwAll();
    }
	
	private static final SubscriberStaleValidator instance = new SubscriberStaleValidator();

}
