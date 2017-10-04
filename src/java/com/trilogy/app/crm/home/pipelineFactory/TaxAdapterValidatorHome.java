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
package com.trilogy.app.crm.home.pipelineFactory;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.home.HomeProxy;

import com.trilogy.app.crm.bean.TaxAdapters;

/**
 * This class is used to validate Tax Adapter class name, which  
 * should be fully qualified valid class name. 
 * Default is com.redknee.app.crm.taxation.LocalTaxAdapter.
 * 
 * @author shailesh.makhijani
 * @since 9.6
 */
public class TaxAdapterValidatorHome extends HomeProxy{

	private static final long serialVersionUID = 1L;

	/**
	 * @param home
	 * @throws HomeException 
	 * @throws HomeInternalException 
	 */
	public TaxAdapterValidatorHome(Context ctx, Home delegate) throws HomeInternalException, HomeException {
		super(ctx, delegate);
	}

	@Override
	public Object create(Context ctx, Object obj) throws HomeException,HomeInternalException {
		validate(obj);
		return super.create(ctx, obj);
	}
	
	@Override
	public Object store(Context ctx, Object obj) throws HomeException,HomeInternalException {
		validate(obj);
		return super.store(ctx, obj);
	}
	
	public void validate(Object obj) throws HomeException {
		if(obj instanceof TaxAdapters){
			TaxAdapters taxAdapters = (TaxAdapters)obj;
			try {
				Class.forName(taxAdapters.getAdapter());
			} catch (ClassNotFoundException e) {
				throw new HomeException("Tax Adapter class doesn't exist ", e);
			}
		}
	}
}
