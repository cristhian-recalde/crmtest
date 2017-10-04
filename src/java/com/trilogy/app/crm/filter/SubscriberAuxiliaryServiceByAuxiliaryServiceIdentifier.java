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
 * SubscriberAuxiliaryServices for a given AuxiliaryService identifier.
 *
 * @author gary.anderson@redknee.com
 */
public
class SubscriberAuxiliaryServiceByAuxiliaryServiceIdentifier
    implements Predicate,XStatement
{
	protected long serviceIdentifier;
	
    /**
     * Creates a new FindSubscriberForAuxiliaryServicePredicate.
     *
     * @param serviceIdentifier The AuxiliaryService identifier used to find
     * corresponding SubscriberAuxiliaryServices.
     */
    public SubscriberAuxiliaryServiceByAuxiliaryServiceIdentifier(final long serviceIdentifier)
    {
    	setServiceIdentifier(serviceIdentifier);
    }

    public boolean f(Context _ctx,final Object obj)
    {
        final SubscriberAuxiliaryService association =
            (SubscriberAuxiliaryService)obj;

        return association.getAuxiliaryServiceIdentifier() == getServiceIdentifier();
    }


    /**
     * Creates the SQL constraint used to find the SubscriberAuxiliaryServices.
     * 
     * @param serviceIdentifier
     *            The AuxiliaryService identifier used to find corresponding
     *            SubscriberAuxiliaryServices.
     * @return The SQL constraint used to find the SubscriberAuxiliaryServices.
     */
    public String createStatement(Context ctx)
    {
        final String sql = SubscriberAuxiliaryService.AUXILIARYSERVICEIDENTIFIER_PROPERTY + " = "
                + getServiceIdentifier();
        return sql;
    }


    /**
     * Set a PreparedStatement with the supplied Object.
     */
    public void set(Context ctx, XPreparedStatement ps) throws SQLException
    {
    }

	/**
	 * @return Returns the serviceIdentifier.
	 */
	public long getServiceIdentifier()
	{
		return serviceIdentifier;
	}
	/**
	 * @param serviceIdentifier The serviceIdentifier to set.
	 */
	public void setServiceIdentifier(long serviceIdentifier)
	{
		this.serviceIdentifier = serviceIdentifier;
	}
} // class
