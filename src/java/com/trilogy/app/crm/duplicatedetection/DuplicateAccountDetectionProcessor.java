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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;

/**
 * Factory class for DuplicateAccountDetectionCommand.
 * 
 * @author cindy.wong@redknee.com
 * @since 8.5
 */
public class DuplicateAccountDetectionProcessor
{
	private static final DuplicateAccountDetectionProcessor instance_ =
	    new DuplicateAccountDetectionProcessor();

	private DuplicateAccountDetectionProcessor()
	{
		// empty
	}

	public static DuplicateAccountDetectionProcessor instance()
	{
		return instance_;
	}

	public List execute(final Context context,
	    final DuplicateAccountDetectionCommand command)
	    throws IllegalArgumentException, HomeException
	{
		if (context == null)
		{
			throw new IllegalArgumentException("Context cannot be null");
		}

		if (command == null)
		{
			throw new IllegalArgumentException("Command cannot be null");
		}

		Map duplicates = command.findDuplicates(context);
		List results = new ArrayList(duplicates.size());
		results.addAll(duplicates.values());
		Collections.sort(results,
		    DuplicateAccountDetectionResultComparator.instance());

		return results;
	}
}
