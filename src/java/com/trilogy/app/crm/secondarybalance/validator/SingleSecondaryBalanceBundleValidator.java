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

package com.trilogy.app.crm.secondarybalance.validator;

import java.util.Collection;
import java.util.Map;

import com.trilogy.app.crm.bean.core.BundleFee;
import com.trilogy.app.crm.bean.core.PricePlan;
import com.trilogy.app.crm.bean.core.PricePlanVersion;
import com.trilogy.app.crm.bean.PricePlanVersionXInfo;
import com.trilogy.app.crm.bean.ServicePackageVersionXInfo;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberXInfo;
import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.beans.Validator;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;

/**
 * 
 * Validate if objects have a single Bundle of Bundle Category unit type
 * as Secondary Balance.
 * 
 * Currently supported objects:-
 * 
 * <ul>
 * 	<li>{@link Subscriber}
 * 	<li>{@link PricePlan}
 * </ul>
 * 
 * @author <a href='mailto:ameya.bhurke@redknee.com'>Ameya Bhurke</a>
 *
 */
public class SingleSecondaryBalanceBundleValidator implements Validator 
{
	private static SingleSecondaryBalanceBundleValidator instance_ = null;
	
	static
	{
		instance_ = new SingleSecondaryBalanceBundleValidator();
	}
	
	public static final String PRICE_PLAN_MESSAGE = "A specific Bundle ID that is a Secondary Balance, can only appear once in the Price Plan.";
	
	public static final String SUBSCRIBER_MESSAGE = "A specific Bundle ID that is a Secondary Balance, can only be subscribed once.";

	
	private SingleSecondaryBalanceBundleValidator()
	{
		
	}
	
	public static SingleSecondaryBalanceBundleValidator instance()
	{
		return instance_;
	}
	
	
	@Override
	public void validate(Context ctx, Object obj) throws IllegalStateException 
	{
		CompoundIllegalStateException validationException = new CompoundIllegalStateException();
		try
		{
			if(obj instanceof PricePlanVersion)
			{
				PricePlanVersion pricePlanVersion = (PricePlanVersion)obj;
				
				@SuppressWarnings("unchecked")
				Map<Long,BundleFee> bundleFeeMap = (Map<Long,BundleFee>)pricePlanVersion.getServicePackageVersion(ctx).getBundleFees();
				
				if(hasMoreThanOneSecondaryBalanceBundle(ctx, bundleFeeMap.values(), pricePlanVersion.getPricePlan(ctx).getSpid()))
				{
					validationException.thrown(new IllegalPropertyArgumentException(ServicePackageVersionXInfo.BUNDLE_FEES, PRICE_PLAN_MESSAGE));
				}
			}
			else if(obj instanceof Subscriber)
			{
				Subscriber subscriber = (Subscriber)obj;
				
				@SuppressWarnings("unchecked")
				Map<Long,BundleFee> bundleFeeMap = (Map<Long,BundleFee>)subscriber.getBundles(); 
				
				if(hasMoreThanOneSecondaryBalanceBundle(ctx, bundleFeeMap.values(), subscriber.getSpid()))
				{
					validationException.thrown(new IllegalPropertyArgumentException(SubscriberXInfo.PRICE_PLAN, SUBSCRIBER_MESSAGE));
				}
			}
		}
		catch(HomeException e)
		{
			validationException.thrown(new IllegalPropertyArgumentException(PricePlanVersionXInfo.SERVICE_PACKAGE_VERSION, e));
		}
		finally
		{
			validationException.throwAll();
		}
		
		
	}
	
	/**
	 * 
	 * Finds out if a there are more than one SecondaryBalance bundles in the {@link BundleFee} {@link Collection}.
	 * 
	 * @param ctx
	 * @param bundleFeeCollection
	 * @param spid
	 * @return
	 * @throws IllegalStateException
	 */
	public static boolean hasMoreThanOneSecondaryBalanceBundle(Context ctx, Collection<BundleFee> bundleFeeCollection, int spid)
		throws IllegalStateException
	{
		short secondaryBundleCount = 0;
		
		for(BundleFee bundleFee : bundleFeeCollection)
		{
			try
			{
				if( bundleFee.getBundleProfile(ctx, spid).isSecondaryBalance(ctx) )
				{
					secondaryBundleCount++;
				}
			}
			catch(Exception e)
			{
				throw new IllegalStateException("Internal Error while trying to query Bundle Profile: ", e);
			}
		}
		
		return (secondaryBundleCount > 1) ;
	}

}
