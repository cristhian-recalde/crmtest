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

import com.trilogy.app.crm.bean.SubscriberAuxiliaryService;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.filter.Predicate;
import com.trilogy.framework.xhome.xdb.XPreparedStatement;
import com.trilogy.framework.xhome.xdb.XStatement;


/**
 * Provides a where clause that can be used to search for
 * SubscriberAuxiliaryServices for a given provisioning status.
 *
 * @author gary.anderson@redknee.com
 */
public class SubscriberAuxiliaryServiceByProvisioned
    implements Predicate,XStatement
{
	protected boolean provisioned;
	
    /**
     * Creates a new FindProvisionedForAuxiliaryServicePredicate.
     *
     * @param provisioned True if successfully provisioned
     * SubscriberAuxiliaryServices are to be looked-up; false otherwise.
     */
    public SubscriberAuxiliaryServiceByProvisioned(final boolean provisioned)
    {
    	setProvisioned(provisioned);
    }

    /**
     * @see com.redknee.framework.xhome.filter.Predicate#f(com.redknee.framework.xhome.context.Context, java.lang.Object)
     */
    public boolean f(Context _ctx,final Object obj)
    {
        final SubscriberAuxiliaryService association =
            (SubscriberAuxiliaryService)obj;

        return association.getProvisioned() == isProvisioned();
    }


    /**
     * Set a PreparedStatement with the supplied Object.
     */
    public void set(Context ctx, XPreparedStatement ps) throws SQLException
    {
    }


    /**
     * Creates the SQL constraint used to find the SubscriberAuxiliaryServices.
     * 
     * @param provisioned
     *            True if successfully provisioned SubscriberAuxiliaryServices are to be
     *            looked-up; false otherwise.
     * @return The SQL constraint used to find the SubscriberAuxiliaryServices.
     */
    public String createStatement(Context ctx)
    {
        // TODO - 2004-08-11 - See if there is a way to get the true/false
        // values from the framework.
        final String provisionedString;
        if (isProvisioned())
        {
            provisionedString = "y";
        }
        else
        {
            provisionedString = "n";
        }
        final String sql = SubscriberAuxiliaryService.PROVISIONED_PROPERTY + " = '" + provisionedString + "'";
        return sql;
    }

	/**
	 * @return Returns the provisioned.
	 */
	public boolean isProvisioned()
	{
		return provisioned;
	}
	/**
	 * @param provisioned The provisioned to set.
	 */
	public void setProvisioned(boolean provisioned)
	{
		this.provisioned = provisioned;
	}
} // class
