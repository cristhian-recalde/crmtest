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

import java.util.Map;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;

import com.trilogy.app.crm.bean.duplicatedetection.DuplicateAccountDetectionCriteria;

public abstract class DuplicateAccountDetectionCommand
{
	DuplicateAccountDetectionCommand(
	    final DuplicateAccountDetectionCriteria criteria)
	{
		this.criteria_ = criteria;
	}

	public DuplicateAccountDetectionCriteria getCriteria()
	{
		return criteria_;
	}

	/**
	 * Find all the duplicates associated with the preset criteria.
	 * 
	 * @param context
	 *            Operating context.
	 * @return A list of all duplicates.
	 * @throws HomeException
	 * @throws HomeInternalException
	 */
	public abstract Map findDuplicates(Context context)
	    throws HomeInternalException, HomeException;

	protected DuplicateAccountDetectionCriteria criteria_;
}
