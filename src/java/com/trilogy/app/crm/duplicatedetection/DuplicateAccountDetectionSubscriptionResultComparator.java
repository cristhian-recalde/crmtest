/*
 * This code is a protected work and subject to domestic and international
 * copyright law(s). A complete listing of authors of this work is readily
 * available. Additionally, source code is, by its very nature, confidential
 * information and inextricably contains trade secrets and other information
 * proprietary, valuable and sensitive to Redknee, no unauthorised use,
 * disclosure, manipulation or otherwise is permitted, and may only be used
 * in accordance with the terms of the licence agreement entered into with
 * Redknee Inc. and/or its subsidiaries.
 *
 * Copyright &copy; Redknee Inc. and its subsidiaries. All Rights Reserved.
 *
 */
package com.trilogy.app.crm.duplicatedetection;

import java.util.Comparator;

import com.trilogy.framework.xhome.beans.SafetyUtil;

import com.trilogy.app.crm.bean.duplicatedetection.DuplicateAccountDetectionSubscriptionResult;

/**
 * @author cindy.wong@redknee.com
 * @since 2010-09-09
 */
public class DuplicateAccountDetectionSubscriptionResultComparator implements
    Comparator<DuplicateAccountDetectionSubscriptionResult>
{

	private static final DuplicateAccountDetectionSubscriptionResultComparator instance =
	    new DuplicateAccountDetectionSubscriptionResultComparator();

	public static DuplicateAccountDetectionSubscriptionResultComparator
	    instance()
	{
		return instance;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int compare(DuplicateAccountDetectionSubscriptionResult o1,
	    DuplicateAccountDetectionSubscriptionResult o2)
	{
		int result =
		    SafetyUtil.safeCompare(o1.getSubscriptionType(),
		        o2.getSubscriptionType());
		if (result != 0)
		{
			return result;
		}
		result = SafetyUtil.safeCompare(o1.getState(), o2.getState());
		if (result != 0)
		{
			return result;
		}

		return SafetyUtil.safeCompare(o1.getPhoneNumber(), o2.getPhoneNumber());
	}

}
