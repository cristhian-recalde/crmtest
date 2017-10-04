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
package com.trilogy.app.crm.home;

import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Adapter;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.app.crm.bean.PricePlanOptionTypeEnum;
import com.trilogy.app.crm.bean.ui.DiscountRule;

/**
 * 
 * @author ishan.batra
 * @since 9.9
 * 
 * This calls is used to store set of services and bundles "," seperated in a string and to distinguish between them an indicator
 * is added to identify services and bundles in the list.
 */

public class DiscountRuleEngineAdapter implements Adapter {

	/**
	 * {@inheritDoc}
	 */
	public Object adapt(Context ctx, Object obj) throws HomeException {

		DiscountRule discountRule = (DiscountRule) obj;


		//Adapt Bundle 
		String str = discountRule.getServiceBundleSet(); 

		Set<String> serviceSet = new HashSet<String>();
		Set<String> bundleSet = new HashSet<String>();

		StringTokenizer st = new StringTokenizer(str,",");
		while (st.hasMoreTokens()) {
			String token = st.nextToken();
			String id = token.substring(0, token.indexOf("-")); 
			if(token.endsWith(String.valueOf(PricePlanOptionTypeEnum.SERVICE_INDEX))) 
			{
				serviceSet.add(id);
			}else if(token.endsWith(String.valueOf(PricePlanOptionTypeEnum.BUNDLE_INDEX))) 
			{
				bundleSet.add(id);
			}

		}  	
		discountRule.setServices(serviceSet);
		discountRule.setBundles(bundleSet);


		//Adapt Discounts
		str = discountRule.getDiscountSet();        
		Set<String> discountSet = new HashSet<String>();
		st = new StringTokenizer(str,",");
		while (st.hasMoreTokens()) {
			// System.out.println(st.nextToken());
			discountSet.add(st.nextToken());

		}  	
		discountRule.setDiscounts(discountSet);


		return discountRule;
	}

	/**
	 * {@inheritDoc}
	 */
	public Object unAdapt(Context ctx, Object obj) throws HomeException {

		DiscountRule discountRule = (DiscountRule) obj;


		//UnAdapt Services and Bundles
		TreeSet serviceSet = new TreeSet(discountRule.getServices());
		TreeSet bundleSet = new TreeSet(discountRule.getBundles());	
		

		Object[] serviceArr = serviceSet.toArray();
		Object[] bundleArr = bundleSet.toArray();

		StringBuilder buff = new StringBuilder();

		for (int x=0; x<serviceArr.length; x++)
		{
			if (x!=0)
			{
				buff.append(",");
			}
			buff.append(serviceArr[x] +"-" + PricePlanOptionTypeEnum.SERVICE_INDEX); // "-0" to denote services in service bundle set
		}

		for (int x=0; x<bundleArr.length; x++)
		{
			buff.append(",");
			buff.append(bundleArr[x] + "-" + PricePlanOptionTypeEnum.BUNDLE_INDEX); // "-2" to denote bundle profile in service bundle set
		}

		/*
		 * To make a string which will contain all service and bundle id with tokens attached "-0" for services and "-2" for bundle profiles
		 */
		discountRule.setServiceBundleSet(buff.toString());    	



		//UnAdapt Services
		TreeSet set = new TreeSet(discountRule.getDiscounts());
		Object[] arr = set.toArray();
		buff = new StringBuilder();

		for (int x=0; x<arr.length; x++)
		{
			if (x!=0)
			{
				buff.append(",");
			}
			buff.append(arr[x]);
		}
		discountRule.setDiscountSet(buff.toString());

		return obj;
	}

}

