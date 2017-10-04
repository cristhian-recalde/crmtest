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

package com.trilogy.app.crm.duplicatedetection;

import java.util.Collections;
import java.util.Map;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xlog.log.LogSupport;

import com.trilogy.app.crm.bean.duplicatedetection.DuplicateAccountDetectionCriteria;

/**
 * Null command for duplicate account detection.
 * 
 * @author cindy.wong@redknee.com
 * @since 8.5
 */
class NullDuplicateDetectionCommand extends DuplicateAccountDetectionCommand
{

	/**
	 * Constructor for NullDuplicateDetectionCommand.
	 * 
	 * @param criteria
	 */
	NullDuplicateDetectionCommand(
	    final DuplicateAccountDetectionCriteria criteria)
	{
		super(criteria);
	}

	/**
	 * Always returns an empty list.
	 * 
	 * @see com.redknee.app.crm.duplicatedetection.DuplicateAccountDetectionCommand#findDuplicates(com.redknee.framework.xhome.context.Context)
	 */
	@Override
	public Map findDuplicates(final Context context)
	{
		if (LogSupport.isDebugEnabled(context))
		{
			LogSupport.debug(context, this, "No-op triggered");
		}
		return Collections.EMPTY_MAP;
	}

	public static NullDuplicateDetectionCommand instance()
	{
		return instance;
	}

	private static NullDuplicateDetectionCommand instance =
	    new NullDuplicateDetectionCommand(null);
}
