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

import java.sql.SQLException;
import java.util.Date;

import com.trilogy.app.crm.bean.SubscriberAuxiliaryService;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAwareSupport;
import com.trilogy.framework.xhome.filter.Predicate;
import com.trilogy.framework.xhome.xdb.XPreparedStatement;
import com.trilogy.framework.xhome.xdb.XStatement;

/**
 * Provides a where clause that can be used to search for
 * waint-to-provisioned-in-the-future SubscriberAuxiliaryServices 
 * for a given Subscriber identifier and a given date.
 *
 * @author lily.zou
 */
public class SubscriberByFutureAuxiliaryServices
	extends ContextAwareSupport
    implements Predicate,XStatement 
{
	protected String subscriberIdentifier;
	protected Date queryDate;
	
    /**
     * Creates a new FindSubscribersFutureAuxiliaryServicesPredicate.
     *
     * @param _subscriberIdentifier The Subscriber identifier used to find
     * @param _queryDate the date after which this Aux.Svc will be provisioned for this Subscriber
     */
    public SubscriberByFutureAuxiliaryServices(
    		final Context ctx,
            final String _subscriberIdentifier,
            final Date   _queryDate )
    {
    	setContext(ctx);
    	
    	setSubscriberIdentifier(_subscriberIdentifier);
    	setQueryDate(_queryDate);
    }

    /**
     * @see com.redknee.framework.xhome.filter.Predicate#f(com.redknee.framework.xhome.context.Context, java.lang.Object)
     */
    public boolean f(Context _ctx,final Object obj)
    {
        final SubscriberAuxiliaryService association =(SubscriberAuxiliaryService)obj;

        if (association.getSubscriberIdentifier() == null)
        {
            return false;
        }

        return association.getSubscriberIdentifier().equals(subscriberIdentifier) && 
        	CalendarSupportHelper.get(_ctx).getDateWithNoTimeOfDay(association.getStartDate()).after(CalendarSupportHelper.get(_ctx).getDateWithNoTimeOfDay(queryDate)) ;
    }


    public String createStatement(Context ctx)
    {
        final String sql =
            SubscriberAuxiliaryService.SUBSCRIBERIDENTIFIER_PROPERTY
            + " = '"
            + subscriberIdentifier
            + "' and startDate > "
            + Long.toString(CalendarSupportHelper.get().getDateWithNoTimeOfDay( queryDate ).getTime());

        return sql;
    }

    public void set(Context ctx, XPreparedStatement ps)
    throws SQLException
    {
        
    }
    
	/**
	 * @return Returns the queryDate.
	 */
	public Date getQueryDate()
	{
		return queryDate;
	}
	/**
	 * @param queryDate The queryDate to set.
	 */
	public void setQueryDate(Date queryDate)
	{
		this.queryDate = queryDate;
	}
	/**
	 * @return Returns the subscriberIdentifier.
	 */
	public String getSubscriberIdentifier()
	{
		return subscriberIdentifier;
	}
	/**
	 * @param subscriberIdentifier The subscriberIdentifier to set.
	 */
	public void setSubscriberIdentifier(String subscriberIdentifier)
	{
		this.subscriberIdentifier = subscriberIdentifier;
	}
} // class
