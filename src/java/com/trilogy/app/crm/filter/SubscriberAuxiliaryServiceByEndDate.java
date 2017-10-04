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
import java.text.SimpleDateFormat;
import java.util.Date;

import com.trilogy.app.crm.bean.SubscriberAuxiliaryService;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.app.crm.support.MultiDbSupportHelper;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAwareSupport;
import com.trilogy.framework.xhome.filter.Predicate;
import com.trilogy.framework.xhome.xdb.XStatement;
import com.trilogy.framework.xhome.xdb.XPreparedStatement;

/**
 * @author lzou
 *
 *  Provides a where clause that can be used to search for
 *  SubscriberAuxiliaryServices to be unactivated on a given date .
 */
public class SubscriberAuxiliaryServiceByEndDate
extends ContextAwareSupport
	implements Predicate,XStatement
{
	public Date endDate;

	/**
	 * @param startDate the Date on which this Aux.Svc gets activated
	 */
	public SubscriberAuxiliaryServiceByEndDate(final Context ctx, final Date endDate)
	{
		setContext(ctx);
		setEndDate(endDate);
	}

	/**
	 * @see com.redknee.framework.xhome.filter.Predicate#f(com.redknee.framework.xhome.context.Context, java.lang.Object)
	 */
    public boolean f(Context _ctx,final Object obj)
    {
        final SubscriberAuxiliaryService association =
            (SubscriberAuxiliaryService)obj;

        return CalendarSupportHelper.get(_ctx).getDateWithNoTimeOfDay(association.getEndDate()).before(CalendarSupportHelper.get(_ctx).getDateWithNoTimeOfDay(getEndDate())) &&
		      association.isProvisioned();
    }

    /**
     * Creates the SQL constraint used to find the SubscriberAuxiliaryServices.
     *
     * @param startDate the Date on which this Aux.Svc gets activated
     * @return The SQL constraint used to find the SubscriberAuxiliaryServices.
     */
    public String getSQLClause()
    {
        final String sql = 
			 SubscriberAuxiliaryService.ENDDATE_PROPERTY
			 + " <= "
			 + Long.toString(CalendarSupportHelper.get().getDateWithNoTimeOfDay(getEndDate()).getTime()+1)
			 + " and provisioned='y'";
            
        return sql;
    }
    
    
    public String createStatement(Context ctx)
    {
        final String sql = 
            SubscriberAuxiliaryService.ENDDATE_PROPERTY
            + " <= "
            + Long.toString(CalendarSupportHelper.get().getDateWithNoTimeOfDay(getEndDate()).getTime()+1)
            + " and provisioned='y'";
           
       return sql;
    }

    /**
     * Set a PreparedStatement with the supplied Object.
     */
    public void set(Context ctx, XPreparedStatement ps) throws SQLException
    {
            
    }
    
	/**
	 * @return Returns the endDate.
	 */
	public Date getEndDate()
	{
		return endDate;
	}
	/**
	 * @param endDate The endDate to set.
	 */
	public void setEndDate(Date endDate)
	{
		this.endDate = endDate;
	}
}
