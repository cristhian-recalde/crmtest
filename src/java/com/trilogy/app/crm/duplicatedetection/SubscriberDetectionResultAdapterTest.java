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

import org.junit.BeforeClass;
import org.junit.Test;

import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberHome;
import com.trilogy.app.crm.bean.SubscriberXInfo;
import com.trilogy.app.crm.bean.duplicatedetection.DuplicateAccountDetectionResult;
import com.trilogy.app.crm.bean.duplicatedetection.DuplicateAccountDetectionSubscriptionResult;
import com.trilogy.app.crm.unit_test.ContextAwareTestCase;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;

/**
 * @author cindy.wong@redknee.com
 * @since 2010-06-30
 */
public class SubscriberDetectionResultAdapterTest extends ContextAwareTestCase
{
	@Override
	@BeforeClass
	public void setUp()
	{
		super.setUp();

		final Home home = (Home) getContext().get(SubscriberHome.class);

		try
		{
			// find a random subscriber
			subscriber_ =
			    (Subscriber) home.find(getContext(), new EQ(
			        SubscriberXInfo.SPID, 1));
			if (subscriber_ == null)
			{
				fail("No subscriber!");
			}
		}
		catch (final HomeException exception)
		{
			fail("Exception caught: " + exception);
		}
	}

	/**
	 * Test method for
	 * {@link com.redknee.app.crm.duplicatedetection.SubscriberDetectionResultAdapter#f(com.redknee.framework.xhome.context.Context, java.lang.Object)}
	 * .
	 */
	@Test
	public void testF()
	{
		final DuplicateAccountDetectionSubscriptionResult result =
		    (DuplicateAccountDetectionSubscriptionResult) SubscriberDetectionResultAdapter
		        .instance().f(getContext(), subscriber_);
		assert result != null;
		assert result.getSubscriptionType() == subscriber_
		    .getSubscriptionType();
		assert result.getPhoneNumber() == subscriber_.getMSISDN();
		assert result.getState() == subscriber_.getState();
	}

	/**
	 * Test method for
	 * {@link com.redknee.app.crm.duplicatedetection.SubscriberDetectionResultAdapter#unAdapt(com.redknee.framework.xhome.context.Context, java.lang.Object)}
	 * .
	 */
	@Test
	public void testUnAdapt()
	{
		fail("Not yet implemented");
	}

	/**
	 * Test method for
	 * {@link com.redknee.app.crm.duplicatedetection.SubscriberDetectionResultAdapter#adapt(com.redknee.framework.xhome.context.Context, java.lang.Object)}
	 * .
	 */
	@Test
	public void testAdapt()
	{
		final DuplicateAccountDetectionSubscriptionResult result =
		    (DuplicateAccountDetectionSubscriptionResult) SubscriberDetectionResultAdapter
		        .instance().adapt(getContext(), subscriber_);
		assert result != null;
		assert result.getSubscriptionType() == subscriber_
		    .getSubscriptionType();
		assert result.getPhoneNumber() == subscriber_.getMSISDN();
		assert result.getState() == subscriber_.getState();
	}

	private Subscriber subscriber_ = null;
}
