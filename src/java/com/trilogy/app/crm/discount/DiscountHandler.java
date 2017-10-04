/*
 * This code is a protected work and subject to domestic and international copyright
 * law(s). A complete listing of authors of this work is readily available. Additionally,
 * source code is, by its very nature, confidential information and inextricably contains
 * trade secrets and other information proprietary, valuable and sensitive to Redknee. No
 * unauthorized use, disclosure, manipulation or otherwise is permitted, and may only be
 * used in accordance with the terms of the license agreement entered into with Redknee
 * Inc. and/or its subsidiaries.
 * 
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.discount;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.DiscountEventActivity;
import com.trilogy.app.crm.core.bean.ifc.SubscriberIfc;
import com.trilogy.framework.xhome.context.Context;

/**
 * The Handler interface for discount engine. 
 * The classes implementing this interface defines the behaviour of specific discount type
 * @author abhijit.mokashi
 *
 */
public interface DiscountHandler {
	
	/**
	 * This method is used to initialize the input data required for evaluating the discount
	 * @param context
	 * @param discountEvents
	 * @return
	 */

	public boolean init(Context context,Account account,List<SubscriberIfc> subscriberList);

	
	/**
	 * This method hits the discount rule engine to check if current account is eligible for discount.
	 * If eligible updates the discount event activity
	 * @param context
	 * @param account
	 * @param subscriberList
	 * @param discountEvents
	 * @param trackDiscount
	 * @return
	 */
	boolean evaluate(Context context, Account account,
			List<SubscriberIfc> subscriberList,
			Collection<DiscountEventActivity> existingDiscountActivities,
			Map<String, Boolean> trackDiscount,
			Collection<DiscountEventActivity> discountEventActivityForCreation,
			Collection<DiscountEventActivity> discountEventActivityForUpdate,
			Collection<DiscountEventActivity> discountEventActivityContinued);
	
	/**
	 * 
	 * @return
	 */
	public boolean generateTransactions(final Context context, final Account account, 
			 List<DiscountEventActivity> discountEventActivity,
    		 List<DiscountEventActivity> discountEventToBeUpdated
    		);
}
