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

import org.junit.Test;

import com.trilogy.app.crm.unit_test.ContextAwareTestCase;

/**
 * @author cindy.wong@redknee.com
 * @since 2010-06-30
 */
public class NullDuplicateDetectionCommandTest extends ContextAwareTestCase
{

	/**
	 * Test method for
	 * {@link com.redknee.app.crm.duplicatedetection.NullDuplicateDetectionCommand#findDuplicates(com.redknee.framework.xhome.context.Context)}
	 * .
	 */
	@Test
	public void testFindDuplicates()
	{
		final Map result =
		    NullDuplicateDetectionCommand.instance().findDuplicates(
		        getContext());
		assert result != null;
		assert result.isEmpty();
	}

}
