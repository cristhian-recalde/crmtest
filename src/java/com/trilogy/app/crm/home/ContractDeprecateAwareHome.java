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
package com.trilogy.app.crm.home;

import com.trilogy.app.crm.bean.payment.Contract;
import com.trilogy.app.crm.bean.payment.ContractStateEnum;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.filter.Predicate;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.WhereHome;

/**
 * Filters the home to return only non-deprecated Contracts
 * TODO deprecated beans should not be completly filtered out on the Home level,
 * TODO because in this way it is no different from being deleted. 
 * @author amedina
 */
public class ContractDeprecateAwareHome extends WhereHome 
{
	/**
	 * Only constructor
	 * @param ctx
	 * @param delegate
	 */
	public ContractDeprecateAwareHome(Context ctx, Home delegate)
	{
		super(ctx,delegate);
	}

	
   
	public Object getWhere(Context ctx)
	{
		return new Predicate()
		{
			public boolean f(Context ctx, Object obj)
	        {
	        	Contract contract = (Contract) obj;
	        	return contract.getState() == ContractStateEnum.ACTIVE;
	        }

	      };
	}

}
