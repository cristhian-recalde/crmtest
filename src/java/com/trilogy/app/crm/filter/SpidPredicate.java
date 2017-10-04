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
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.filter;

import java.sql.SQLException;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.filter.Predicate;
import com.trilogy.framework.xhome.msp.SpidAware;
import com.trilogy.framework.xhome.xdb.XPreparedStatement;
import com.trilogy.framework.xhome.xdb.XStatement;


/**
 * A Predicate used to filter out spid aware beans in home
 *
 * @author jchen
 */
public class SpidPredicate
	implements Predicate,XStatement
{
    /**
     * The Adjustment Type code to watch for.
     */
	protected int spid_;

	/**
     * Creates a new predicate with the given criteria.
     *
     * @param code The Adjustment Type code to match.
     */ 
	public SpidPredicate(final int spid)
	{
        spid_ = spid;
	}

	/**
	 * @see com.redknee.framework.xhome.filter.Predicate#f(com.redknee.framework.xhome.context.Context, java.lang.Object)
	 */
	public boolean f(Context _ctx,final Object obj)
	{
		final SpidAware Spid = (SpidAware) obj;

		return Spid.getSpid() == spid_;
	}

	/**
	 * @see com.redknee.framework.xhome.filter.SQLClause#getSQLClause()
	 */
    public String createStatement(Context ctx)
	{
		return "spid="+getSpid();
	}


    /**
     * Set a PreparedStatement with the supplied Object.
     */
    public void set(Context ctx, XPreparedStatement ps)
        throws SQLException
        {
        
        }

	public int getSpid()
	{
		return spid_;
	}
	
	public void setSpid(int spid)
	{
		this.spid_=spid;
	}
}
