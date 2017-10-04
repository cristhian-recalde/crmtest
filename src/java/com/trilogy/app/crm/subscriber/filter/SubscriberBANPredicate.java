// INSPECTED: 29/09/2003 MLAM

/*
      Author: Paul Sperneac
      Date:   Sept 22, 2003
*/


package com.trilogy.app.crm.subscriber.filter;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.filter.Predicate;

import com.trilogy.app.crm.bean.Subscriber;

/** A Predicate used to retrieve subscriber with specific BAN from it Home 
 * 
 * @deprecated Use {@link #com.redknee.framework.xhome.elang.EQ}
 */
@Deprecated
public class SubscriberBANPredicate
	implements Predicate
{
	// REVIEW(maintainability): private data members should be put after public methods
	private String ban;

	public SubscriberBANPredicate(String _ban)
	{
		ban = _ban;
	}

	public boolean f(Context _ctx,Object obj)
	{
		Subscriber sub = (Subscriber)obj;

		return (sub.getBAN().equals(ban));
	}
}
