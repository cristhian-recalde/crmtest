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

import com.trilogy.app.crm.bean.IdentifierAware;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.filter.Predicate;
import com.trilogy.framework.xhome.xdb.XPreparedStatement;
import com.trilogy.framework.xhome.xdb.XStatement;

/**
 * @author jchen
 */
public class IdentifierPredicate implements Predicate,XStatement
{
    /**
     * The Adjustment Type code to watch for.
     */
	final protected long identifier_;

	/**
     * Creates a new predicate with the given criteria.
     *
     * @param code The Adjustment Type code to match.
     */ 
	public IdentifierPredicate(final long id)
	{
        identifier_ = id;
	}

	/**
	 * @see com.redknee.framework.xhome.filter.Predicate#f(com.redknee.framework.xhome.context.Context, java.lang.Object)
	 */
	public boolean f(Context _ctx,final Object obj)
	{
		final IdentifierAware Spid = (IdentifierAware) obj;

		return Spid.getIdentifier()  == identifier_;
	}


    /**
     * Set a PreparedStatement with the supplied Object.
     */
    public void set(Context ctx, XPreparedStatement ps) throws SQLException
    {
    }


    public String createStatement(Context ctx)
    {
        return getIdentifierColumnName() + " = " + getIdentifier();
    }


	public long getIdentifier()
	{
		return identifier_;
	}
	
	
	String identifierColumnName = "IDIDENTIFIER";

	/**
	 * @return Returns the identifierColumnName.
	 */
	public String getIdentifierColumnName() {
		return identifierColumnName;
	}
	/**
	 * @param identifierColumnName The identifierColumnName to set.
	 */
	public void setIdentifierColumnName(String identifierColumnName) {
		this.identifierColumnName = identifierColumnName;
	}
}
