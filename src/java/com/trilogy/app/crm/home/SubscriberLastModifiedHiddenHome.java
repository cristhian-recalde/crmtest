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

import java.util.Date;

import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.framework.xhome.beans.LastModifiedAware;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xlog.log.LogSupport;

/**
 * 
 * Updates/sets lastModifiedHidden to lastModified. there is a need to have lastModifiedHidden
 * as lastModified is read-only and not received from the GUI.
 * 
 * @author ameya.bhurke@redknee.com
 *
 */
public class SubscriberLastModifiedHiddenHome extends HomeProxy 
{

    public SubscriberLastModifiedHiddenHome(Home delegate)
    {
        setDelegate(delegate);
    }

    public Object create(Context ctx, Object obj)
        throws HomeException, HomeInternalException
    {
    	LogSupport.debug(ctx, this, "SubscriberPipeline[create].....");
    	setLastModifiedHidden(obj);
        return getDelegate().create(ctx, obj);
    }

    public Object store(Context ctx, Object obj)
        throws HomeException
    {
    	LogSupport.debug(ctx, this, "SubscriberPipeline[store].....");
    	setLastModifiedHidden(obj);
        return getDelegate().store(ctx, obj);
    }
	
	private void setLastModifiedHidden(Object obj) 
	{
    	Subscriber subscriber = (Subscriber)obj;
    	
    	subscriber.setLastModifiedHidden(subscriber.getLastModified());
	}
}
