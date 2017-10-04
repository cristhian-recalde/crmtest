/*
 * Created on Nov 10, 2004
 */
/* 
 * This code is a protected work and subject to domestic and international
 * copyright law(s). A complete listing of authors of this work is readily 
 * available. Additionally, source code is, by its very nature, confidential 
 * information and inextricably contains trade secrets and other information 
 * proprietary, valuable and sensitive to Redknee, no unauthorised use, 
 * disclosure, manipulation or otherwise is permitted, and may only be used 
 * in accordance with the terms of the licence agreement entered into with 
 * Redknee Inc. and/or its subsidiaries. 
 * 
 * Copyright ? Redknee Inc. and its subsidiaries. All Rights Reserved. 
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
 * @author lzou
 *
 *  Provides a where clause that can be used to search for
 *  SubscriberAuxiliaryServices to be activated on a given date .
 */
public class SubscriberAuxiliaryServiceByStartDate 
	extends ContextAwareSupport implements Predicate, XStatement
{
	protected Date startDate;

	/**
	 * @param startDate the Date on which this Aux.Svc gets activated
	 */
	public SubscriberAuxiliaryServiceByStartDate(final Context ctx, final Date startDate)
	{
		setStartDate(startDate);
		setContext(ctx);
	}

	/**
	 * @see com.redknee.framework.xhome.filter.Predicate#f(com.redknee.framework.xhome.context.Context, java.lang.Object)
	 */
	public boolean f(Context _ctx, final Object obj)
	{
		final SubscriberAuxiliaryService association = (SubscriberAuxiliaryService) obj;

		return (CalendarSupportHelper.get(_ctx).getDateWithNoTimeOfDay(association.getStartDate()).before(
			CalendarSupportHelper.get(_ctx).getDateWithNoTimeOfDay(startDate)) || CalendarSupportHelper.get(_ctx).getDateWithNoTimeOfDay(
			association.getStartDate()).equals((CalendarSupportHelper.get(_ctx).getDateWithNoTimeOfDay(startDate))))
			&& CalendarSupportHelper.get(_ctx).getDateWithNoTimeOfDay(association.getEndDate()).after(
				CalendarSupportHelper.get(_ctx).getDateWithNoTimeOfDay(startDate)) && !association.isProvisioned();
	}

	/**
	 * Creates the SQL constraint used to find the SubscriberAuxiliaryServices.
	 *
	 * @param startDate the Date on which this Aux.Svc gets activated
	 * @return The SQL constraint used to find the SubscriberAuxiliaryServices.
	 */
    public String createStatement(Context ctx)
	{
		String expectedProvisionDateString = Long.toString(CalendarSupportHelper.get().getDateWithNoTimeOfDay(startDate).getTime());

		final String sql = 
			SubscriberAuxiliaryService.STARTDATE_PROPERTY + " <= " + expectedProvisionDateString
			+" and "
			+ SubscriberAuxiliaryService.ENDDATE_PROPERTY + " > " + expectedProvisionDateString 
			+ " and " 
			+ " provisioned='n'";

		return sql;
	}

    /**
     * Set a PreparedStatement with the supplied Object.
     */
    public void set(Context ctx, XPreparedStatement ps)
        throws SQLException
        {
        
        }
	/**
	 * @return Returns the startDate.
	 */
	public Date getStartDate()
	{
		return startDate;
	}

	/**
	 * @param startDate The startDate to set.
	 */
	public void setStartDate(Date startDate)
	{
		this.startDate = startDate;
	}
}
