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
package com.trilogy.app.crm.filter;

import java.util.Collection;

import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberXInfo;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.filter.Predicate;
import com.trilogy.framework.xhome.visitor.AbortVisitException;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.app.crm.bean.SubscriptionSuspensionReason;
/**
 * 
 * @author monami.pakira@redknee.com
 * @since 11.0.4
 * 
 */

public class SubSuspensionReasonEditButtonPredicate implements Predicate{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	/**
	 *  This method hide edit button for subscription suspension reason : Other and Unpaid
	 *  else show edit button
	 */
	public boolean f(Context ctx, Object object)
			throws AbortVisitException {
		if(object instanceof SubscriptionSuspensionReason)
		{
		 try {
			 SubscriptionSuspensionReason subSuspResn = (SubscriptionSuspensionReason)object;
			 if(LogSupport.isDebugEnabled(ctx))
				{
					LogSupport.debug(ctx, this,"SubscriptionSuspensionReason object : "+subSuspResn);
				}
			 if (subSuspResn.getName().equals("Other") || subSuspResn.getName().equals("Unpaid"))
			 {
			   return false;
			 } 
		 }
		 catch (Exception e) 
		  {
			 
			 if(LogSupport.isDebugEnabled(ctx))
				{
					LogSupport.debug(ctx, this, "Getting error while fetching SubscriptionSuspensionReason");
				}
			} 
		  
		  }
		 
		 return true;
	
  }
}
