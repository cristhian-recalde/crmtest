/*
 * Created on Sep 23, 2005
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
package com.trilogy.app.crm.bundle;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextFactory;

/**
 * 
 * @author psperneac
 *
 */
public class BundleAdjustmentFactory implements ContextFactory
{
	protected static BundleAdjustmentFactory instance__;
	
	/**
	 * Prevents instantiation
	 */
	private BundleAdjustmentFactory()
	{
		super();
	}
	
	public static BundleAdjustmentFactory instance()
	{
		if(instance__==null)
		{
			instance__=new BundleAdjustmentFactory();
		}
		
		return instance__;
	}

	public Object create(Context ctx)
	{
		return new BundleAdjustment();
	}

}
