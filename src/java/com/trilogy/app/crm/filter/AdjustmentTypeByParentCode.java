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

import com.trilogy.app.crm.bean.AdjustmentType;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.filter.Predicate;
import com.trilogy.framework.xhome.xdb.XPreparedStatement;
import com.trilogy.framework.xhome.xdb.XStatement;


/**
 * A Predicate used to find AdjustmentType(s) with a specific Parent Code.
 *
 * @author jimmy.ng@redknee.com
 */
public class AdjustmentTypeByParentCode
	implements Predicate,XStatement
{
    /**
     * The Parent Code to watch for.
     */
	protected int code_;

	/**
     * Creates a new predicate with the given criteria.
     *
     * @param code The AdjustmentType code to match.
     */ 
	public AdjustmentTypeByParentCode(final int code)
	{
        code_ = code;
	}

    
    /**
     * INHERIT
     */
	public boolean f(Context ctx,final Object obj)
	{
		final AdjustmentType adjustmentType = (AdjustmentType) obj;

		return adjustmentType.getParentCode() == code_;
	}



    public String createStatement(Context ctx)
    {
        return "parentcode=" + code_;
    }


    /**
     * Set a PreparedStatement with the supplied Object.
     */
    public void set(Context ctx, XPreparedStatement ps) throws SQLException
    {
    }


    public int getCode()
    {
		return code_;
	}
	
	public void setCode(int code)
	{
		this.code_=code;
	}
}
