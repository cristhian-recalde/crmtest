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
package com.trilogy.app.crm.xhome;

import com.trilogy.framework.xhome.context.Context;

/**
 * @author jchen
 */
public interface HomeValidator 
{

	public void validateCreate(Context ctx, Object obj)
    			throws IllegalStateException;
	
	public void validateStore(Context ctx, Object obj)
				throws IllegalStateException;
	
	public void validateRemove(Context ctx, Object obj)
				throws IllegalStateException;

}
