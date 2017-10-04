/*
 * Created on May 17, 2005
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
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.home;

import java.util.Collection;
import java.util.Iterator;

import com.trilogy.app.crm.bean.UsageType;
import com.trilogy.app.crm.bean.UsageTypeStateEnum;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xhome.home.Home;

/**
 * Home that doesn't let you delete/deprecate the item with id 1
 * 
 * @author psperneac
 */
public class UsageTypePreventDefaultItemDeleteHome extends HomeProxy
{
	public static final long DEFAULT=0;
	
	public UsageTypePreventDefaultItemDeleteHome(Home delegate)
	{
		super(delegate);
	}

	/**
	 * @see com.redknee.framework.xhome.home.HomeProxy#create(com.redknee.framework.xhome.context.Context, java.lang.Object)
	 */
	public Object create(Context ctx, Object obj) throws HomeException
	{
		UsageType u=(UsageType) obj;
		
		if(u.getId()==DEFAULT && u.getState()==UsageTypeStateEnum.DEPRECATED_INDEX)
		{
			throw new HomeException("Cannot delete item with id: "+DEFAULT);
		}

		return super.create(ctx, obj);
	}

	/**
	 * @see com.redknee.framework.xhome.home.HomeProxy#store(com.redknee.framework.xhome.context.Context, java.lang.Object)
	 */
	public Object store(Context ctx, Object obj) throws HomeException
	{
		UsageType u=(UsageType) obj;
		
		if(u.getId()==DEFAULT && u.getState()==UsageTypeStateEnum.DEPRECATED_INDEX)
		{
			throw new HomeException("Cannot delete item with id: "+DEFAULT);
		}

		return super.store(ctx, obj);
	}

	/**
	 * @see com.redknee.framework.xhome.home.HomeProxy#remove(com.redknee.framework.xhome.context.Context, java.lang.Object)
	 */
	public void remove(Context ctx, Object obj) throws HomeException
	{
		if(obj==null)
		{
			return;
		}
		
		UsageType u=(UsageType) obj;
		
		if(u.getId()==DEFAULT)
		{
			throw new HomeException("Cannot delete item with id: "+DEFAULT);
		}
		
		super.remove(ctx, obj);
	}

	/**
	 * @see com.redknee.framework.xhome.home.HomeProxy#removeAll(com.redknee.framework.xhome.context.Context, java.lang.Object)
	 */
	public void removeAll(Context ctx, Object where) throws HomeException, UnsupportedOperationException
	{
		Collection coll=select(ctx,where);
		
		for(Iterator i=coll.iterator();i.hasNext();)
		{
			UsageType u=(UsageType) i.next();
			
			if(u.getId()==DEFAULT)
			{
				throw new HomeException("Cannot delete item with id: "+DEFAULT);
			}
		}
		
		super.removeAll(ctx,where);
	}
	
	
}
