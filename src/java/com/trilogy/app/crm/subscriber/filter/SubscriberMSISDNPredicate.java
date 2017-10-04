// INSPECTED: 29/09/2003 MLAM

/*
      SubscriberMSISDNPredicate

      Author: Lily Zou
      Date:   Sept 22, 2003
*/


package com.trilogy.app.crm.subscriber.filter;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.filter.Predicate;

import com.trilogy.app.crm.bean.Subscriber;

/** A Predicate used to retrieve subscriber with specific MSISDN from it Home 
 * 
 * @deprecated Use {@link #com.redknee.framework.xhome.elang.EQ}
 */
@Deprecated
public class SubscriberMSISDNPredicate
	implements Predicate
{
	// REVIEW(maintainability): private data members should be placed after public methods
	private String msisdn_;

	public SubscriberMSISDNPredicate(String msisdn)
	{
		msisdn_ = msisdn;
	}

	public boolean f(Context ctx,Object obj)
	{
		Subscriber sub = (Subscriber)obj;

		return (sub.getMSISDN().equals(msisdn_));
	}
}
