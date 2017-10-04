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
import java.util.Comparator;
import java.util.List;

import com.trilogy.framework.xhome.beans.SafetyUtil;

import com.trilogy.app.crm.bean.duplicatedetection.DuplicateAccountDetectionIdentificationResult;
import com.trilogy.app.crm.bean.duplicatedetection.DuplicateAccountDetectionResult;
import com.trilogy.app.crm.bean.duplicatedetection.DuplicateAccountDetectionSubscriptionResult;
import com.trilogy.app.crm.util.ListComparator;

/**
 * @author cindy.wong@redknee.com
 * @since 8.5
 */
public class DuplicateAccountDetectionResultComparator implements
    Comparator<DuplicateAccountDetectionResult>
{
	private static final DuplicateAccountDetectionResultComparator instance_ =
	    new DuplicateAccountDetectionResultComparator();

	public static DuplicateAccountDetectionResultComparator instance()
	{
		return instance_;
	}

	/**
	 * Constructor for DuplicateAccountDetectionResultComparator.
	 */
	private DuplicateAccountDetectionResultComparator()
	{
		// empty
	}

	@Override
	public int compare(DuplicateAccountDetectionResult arg0,
	    DuplicateAccountDetectionResult arg1)
	{
		int result = SafetyUtil.safeCompare(arg0.getBan(), arg1.getBan());
		if (result != 0)
		{
			return result;
		}
		result = SafetyUtil.safeCompare(arg0.getLastName(), arg1.getLastName());
		if (result != 0)
		{
			return result;
		}

		result =
		    SafetyUtil.safeCompare(arg0.getFirstName(), arg1.getFirstName());
		if (result != 0)
		{
			return result;
		}

		result =
		    SafetyUtil
		        .safeCompare(arg0.getDateOfBirth(), arg1.getDateOfBirth());

		List idList1 = arg0.getIdentifications();
		List idList2 = arg1.getIdentifications();

		Collections.sort(idList1,
		    DuplicateAccountDetectionIdentificationResultComparator.instance());
		Collections.sort(idList2,
		    DuplicateAccountDetectionIdentificationResultComparator.instance());

		ListComparator<DuplicateAccountDetectionIdentificationResult> idListComparator =
		    new ListComparator<DuplicateAccountDetectionIdentificationResult>(
		        DuplicateAccountDetectionIdentificationResultComparator
		            .instance());
		result = idListComparator.compare(idList1, idList2);

		if (result != 0)
		{
			return result;
		}

		List subList1 = arg0.getSubscriptions();
		List subList2 = arg1.getSubscriptions();

		Collections.sort(subList1,
		    DuplicateAccountDetectionSubscriptionResultComparator.instance());
		Collections.sort(subList2,
		    DuplicateAccountDetectionSubscriptionResultComparator.instance());

		ListComparator<DuplicateAccountDetectionSubscriptionResult> subListComparator =
		    new ListComparator<DuplicateAccountDetectionSubscriptionResult>(
		        DuplicateAccountDetectionSubscriptionResultComparator
		            .instance());
		return subListComparator.compare(subList1, subList2);
	}

}
