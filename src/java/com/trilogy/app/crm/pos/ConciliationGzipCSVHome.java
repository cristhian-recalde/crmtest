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
package com.trilogy.app.crm.pos;

import com.trilogy.app.crm.util.GzipCSVHome;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;

/**
 * Conciliation GZIP CSV Home.
 * 
 * The class com.redknee.app.crm.util.GzipCSVHome doesn't support loading from 
 * a CSV file.  It is important that this file be empty before initializing  
 * ConciliationGzipCSVHome.
 * 
 * @author Angie Li
 */
public class ConciliationGzipCSVHome extends GzipCSVHome 
{
	public ConciliationGzipCSVHome(Context ctx, String filename)
	throws HomeException
	{
		this(ctx, filename, com.redknee.framework.xhome.csv.Constants.DEFAULT_SEPERATOR);
	}
	
	public ConciliationGzipCSVHome(Context ctx, String filename, char seperator)
	throws HomeException
	{
		super(
				ctx,
				new ConciliationTransientHome(ctx),
				ConciliationCustomizedCSVSupport.getInstance(),
				filename,
				seperator);
	}
}
