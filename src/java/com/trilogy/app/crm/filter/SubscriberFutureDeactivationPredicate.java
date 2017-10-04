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

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.filter.Predicate;
import com.trilogy.framework.xhome.visitor.AbortVisitException;

import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.support.EnumStateSupportHelper;

/**
 * @author lxia
 */
public class SubscriberFutureDeactivationPredicate implements Predicate
{

    /**
     * Creates a new predicate with the given criteria.
     *
     * @param serviceProvider The service provider to compare with.
     * @param expirationDate
     */ 
	public SubscriberFutureDeactivationPredicate(final CRMSpid serviceProvider, final Date expirationDate)
	{
        serviceProvider_ = serviceProvider;
        date_ = expirationDate;

	}
	/* (non-Javadoc)
	 * @see com.redknee.framework.xhome.filter.Predicate#f(com.redknee.framework.xhome.context.Context, java.lang.Object)
	 */
	public boolean f(Context ctx, Object obj) throws AbortVisitException 
	{
		final Subscriber subscriber = (Subscriber) obj;
        
        if (subscriber.getSpid() == serviceProvider_.getId() && 
        	!EnumStateSupportHelper.get(ctx).isOneOfStates(subscriber, validStatesEnum)&&	
    			(subscriber.getEndDate() != null && subscriber.getEndDate().getTime() <= date_.getTime()))
            {
                return true;
            }
 
		return false;
	}

	
	
	static public String getValidateStates()
	 {
	 	StringBuilder sb = new StringBuilder(" (");
	 	for (SubscriberStateEnum state : validStatesEnum)
	 	{
	 		sb.append(state.getIndex());
	 		sb.append(",");
	 	}
	 	sb.deleteCharAt(sb.length()-1);
	 	sb.append(")");
	 	return sb.toString();
	 }
	
	static public Set<SubscriberStateEnum> getValidateStatesAsSet()
	 {
	 	return new HashSet<SubscriberStateEnum>(validStatesEnum);
	 }
 
    /**
     * The servie provider to compare with.
     */
    protected final CRMSpid serviceProvider_;
    protected final Date date_;
    
    static private Collection<SubscriberStateEnum> validStatesEnum = 
        Collections.unmodifiableCollection(Arrays.asList(
                SubscriberStateEnum.PENDING, 
                SubscriberStateEnum.INACTIVE,
                SubscriberStateEnum.MOVED));
    
    
    

}
