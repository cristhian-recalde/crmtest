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

public class ContractDiscountHandler extends CrossSubscriptionDiscountHandler {
	
	@Override
	public boolean init(Context context,
         	Account account,List<SubscriberIfc> subscriberList) {
		return super.init(context,account,subscriberList);
		
	}

	@Override
	public boolean evaluate(Context context, Account account,
			List<SubscriberIfc> subscriberList,
			Collection<DiscountEventActivity> existingDiscountActivities,
			Map<String, Boolean> trackDiscount,
			Collection<DiscountEventActivity> discountEventActivityForCreation,
			Collection<DiscountEventActivity> discountEventActivityForUpdate,
			Collection<DiscountEventActivity> discountEventActivityContinued) {
		
		if(DiscountUtils.containsAnyContract(context, account)){
			return super.evaluate(context, account, subscriberList, 
					existingDiscountActivities, trackDiscount,
					discountEventActivityForCreation,
					discountEventActivityForUpdate, 
					discountEventActivityContinued);
		}
		return false;
	}
}
