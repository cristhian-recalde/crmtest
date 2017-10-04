/*
 * Created on Mar 15, 2005
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
package com.trilogy.app.crm.home.calldetail.range;

import java.util.Date;

import com.trilogy.framework.xhome.context.Context;

/**
 * Interface for a class that defines rules for intervals when data is saved in a home.
 * 
 * @author psperneac
 */
public interface RangeManager
{
	/**
	 * Returns the start of an interval that contains the date passed in.
	 * @param ctx
	 * @param currentDate
	 * @return
	 */
	public Date getStartOfInterval(Context ctx,Date currentDate);

	/**
	 * Returns the end of an interval that contains the date passed in.
	 * @param ctx
	 * @param currentDate
	 * @return
	 */
	public Date getEndOfInterval(Context ctx,Date currentDate);

	/**
	 * Returns the name of a table based on the dates passed in.
	 * @param ctx
	 * @param start
     * @param end
	 * @return
	 */
	public String getName(Context ctx,Date start,Date end);
}
