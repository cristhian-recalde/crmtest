/*
 * Created on Dec 08, 2004
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
* Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
*/ 
package com.trilogy.app.crm.filter;

import java.sql.SQLException;

import com.trilogy.app.crm.bean.AbstractAdjustmentType;
import com.trilogy.app.crm.bean.AdjustmentType;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.filter.Predicate;
import com.trilogy.framework.xhome.xdb.XStatement;
import com.trilogy.framework.xhome.xdb.XPreparedStatement;

/**
 * @author lzou
 *
 *  Provides a where clause to look for specific AdjustmentType for VRA ER handling
 */
public class AdjustmentTypeByName
	implements Predicate,XStatement
{
	protected String name;
	
	/**
	 * @param name the AdjustmentType name 
     */
	public AdjustmentTypeByName(final String name)
	{
		setName(name);
	}
	
    public boolean f(Context _ctx,final Object obj)
    {
        final  AdjustmentType  type =  (AdjustmentType)obj;

        return type.getName().equals(getName());
    }

    /**
     * Creates the SQL constraint used to find the AdjustmentType for VRA ER handling.
     *
     * @param name the AdjustmentType name
     * @return The SQL constraint used to find the  AdjustmentType for VRA ER handling.
     */
    public String createStatement(Context ctx)
    {
        return AbstractAdjustmentType.NAME_PROPERTY + " = " + getName(); 
    }
    

    /**
     *  Set a PreparedStatement with the supplied Object.
     **/
    public void set(Context ctx, XPreparedStatement ps)
       throws SQLException
       {
        
       }
	/**
	 * @return Returns the name.
	 */
	public String getName()
	{
		return name;
	}
	/**
	 * @param name The name to set.
	 */
	public void setName(String name)
	{
		this.name = name;
	}
}
