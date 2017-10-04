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

import com.trilogy.app.crm.bean.ServiceBase;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.filter.Predicate;
import com.trilogy.framework.xhome.xdb.XPreparedStatement;
import com.trilogy.framework.xhome.xdb.XStatement;


/**
 * A Predicate used to find the Service associated with the given Adjustment Type.
 *
 * @author jimmy.ng@redknee.com
 */
public class ServiceBaseByAdjustmentType
	implements Predicate,XStatement
{
    /**
     * The Adjustment Type code to watch for.
     */
	protected int code_;

	/**
     * Creates a new predicate with the given criteria.
     *
     * @param code The Adjustment Type code to match.
     */ 
	public ServiceBaseByAdjustmentType(final int code)
	{
        code_ = code;
	}

    /**
     * INHERIT
     */
	public boolean f(Context _ctx,final Object obj)
	{
		final ServiceBase service = (ServiceBase) obj;

		return service.getAdjustmentType() == getCode();
	}

	/**
	 * @see com.redknee.framework.xhome.filter.SQLClause#getSQLClause()
	 */
	public String createStatement(Context ctx)
	{
		return "adjustmentType="+getCode();
	}
	

    /**
     * Set a PreparedStatement with the supplied Object.
     */
    public void set(Context ctx, XPreparedStatement ps)
        throws SQLException
    {
        
    }
	
	public int getCode()
	{
		return code_;
	}
	
	public void setcode(int code)
	{
		this.code_=code;
	}
}
