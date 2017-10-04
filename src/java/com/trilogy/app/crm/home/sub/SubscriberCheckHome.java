/*
 * Created on Dec 12, 2003
 * 
 * Copyright (c) 1999-2003 REDKNEE.com. All Rights Reserved.
 * 
 * This software is the confidential and proprietary information of REDKNEE.com. ("Confidential Information"). You
 * shall not disclose such Confidential Information and shall use it only in accordance with the terms of the license
 * agreement you entered into with REDKNEE.com.
 * 
 * REDKNEE.COM MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF THE SOFTWARE, EITHCDR EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED WARRANTIES OF MCDRCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE,
 * OR NON-INFRINGEMENT. REDKNEE.COM SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFCDRED BY LICENSEE AS A RESULT OF USING,
 * MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS DERIVATIVES.
 */
package com.trilogy.app.crm.home.sub;

import com.trilogy.app.crm.bean.*;
import com.trilogy.app.crm.support.Lookup;
import com.trilogy.framework.xhome.beans.Validator;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.*;

/**
 * Perform validations and context preparation for home methods
 * TODO 2007-09-18 convert to validator and correct validation exception style
 * 
 * @author psperneac, jchen
 */
public class SubscriberCheckHome extends HomeProxy implements HomeValidator
{
	/**
	 * @param delegate
	 */
	public SubscriberCheckHome(Home delegate)
	{
		super(delegate);
	}

	/**
	 * @see com.redknee.framework.xhome.home.HomeValidator#getCreateValidator()
	 */
	public Validator getCreateValidator()
	{
		return new Validator()
		{
			public void validate(Context ctx, Object obj) throws IllegalStateException
			{
				Subscriber newSub = (Subscriber) obj;
				try
				{
					onValidatePrepaidPriceplan(ctx, null, newSub);
					onCreateValidateAccountState(ctx, newSub);
				}
				catch (Exception e)
				{
					throw new IllegalStateException("Illegal State for provisioning." + e);
				}
			}
		};
	}

	/**
	 * @see com.redknee.framework.xhome.home.HomeValidator#getStoreValidator()
	 */
	public Validator getStoreValidator()
	{
		return new Validator()
		{
			public void validate(Context ctx, Object obj) throws IllegalStateException
			{
				Subscriber newSub = (Subscriber) obj;
				Subscriber oldSub = (Subscriber) ctx.get(Lookup.OLDSUBSCRIBER);

				try
				{
					onValidatePrepaidPriceplan(ctx, oldSub, newSub);
					//onStoreValidateConversion(ctx, oldSub, newSub); we do not validate converson here, we do it inside SubscriberConversionHome
					onStoreValidateExclusiveFeatureUpdate(ctx, oldSub, newSub);
				}
				catch (Exception e)
				{
					throw new IllegalStateException("Illegal State for provisioning." + e);
				}
			}
		};
	}

	/**
	 * @param obj
	 * @throws HomeException
	 */
	void onCreateValidateAccountState(Context ctx, Subscriber subscriber) throws HomeException
	{
		Account account = (Account) ctx.get(Lookup.ACCOUNT);
		// validate account state
		if (!(AccountStateEnum.ACTIVE.equals(account.getState())))
		{
			//migration from provisionhome.java
			//log3013(subscriber, account, pricePlan);

			throw new HomeException("Provisioning Error 3013: Account " + account.getBAN() + " is not active ");
		}
	}

	void onValidatePrepaidPriceplan(Context ctx, Subscriber oldSub, Subscriber newSub) throws HomeException
	{
		//TODO, Ibis prepaid price plan might have deposit???
		//I don't see any code base in 5.0 has deposit checking
		if (newSub != null && newSub.isPrepaid())
		{
			PricePlanVersion ppl = (PricePlanVersion) ctx.get(PricePlanVersion.class) ;
				
				//newSub.getPricePlan(ctx);
			// validate account state
			if (ppl != null && ppl.getDeposit() != 0)
			{
				//migration from provisionhome.java
				//log3013(subscriber, account, pricePlan);

				throw new HomeException("Please make a deposit release for " + ppl.getDeposit() + " or change price plan before doing this transaction");
			}
		}
	}

	//We prehibit some propertyies to be changed at the same time to simplify
	//bussiness logic
	void onStoreValidateExclusiveFeatureUpdate(Context ctx, Subscriber oldSub, Subscriber newSub)
	{
		if (!oldSub.getSubscriberType().equals(newSub.getSubscriberType())
			&& !oldSub.getState().equals(newSub.getState()))
		{
			if (oldSub.getState() != SubscriberStateEnum.SUSPENDED && oldSub.getState() != SubscriberStateEnum.EXPIRED)
			{
				throw new IllegalStateException("Not allowed to change subscriber type and state and the same time.");
			}
		}
	}
	
	void onStoreValidateConversion(Context ctx, Subscriber oldSub, Subscriber newSub)
	{
		if (!oldSub.getSubscriberType().equals(newSub.getSubscriberType()))
		{
			//only allow conversion at active state
			int oldState = oldSub.getState().getIndex();
			if (oldState != SubscriberStateEnum.ACTIVE_INDEX)
			{
				throw new IllegalStateException("Not allowed to converge subscriber in non active state.");
			}

			//when doing conversion, we need to force price plan change
			if (oldSub.getPricePlan() == newSub.getPricePlan())
			{
				throw new IllegalStateException("Price Plan needs to change when converting.");
			}

			//No amount owing.
			long amountOwing = oldSub.getAmountOwing();
			if (amountOwing != 0)
			{
				throw new IllegalStateException("Subscriber Amount Owing is not zero, " + amountOwing + ".");
			}

			//TODO, group msisdn validation

		}

		//From original ProvisioningHOme.java
		//		 validate account state
		//        if (!(AccountStateEnum.ACTIVE.equals(account.getState())))
		//        {
		//            log3013(subscriber, account, pricePlan);
		//            throw new HomeException(
		//                "Provisioning Error 3013: Account " + account.getBAN() + " is not active ");
		//        }
	}

}
