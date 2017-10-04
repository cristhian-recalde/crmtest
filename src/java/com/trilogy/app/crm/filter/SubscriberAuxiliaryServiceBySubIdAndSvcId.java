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
 * SubscriberAuxiliaryServices for a given Subscriber identifier.
 *
 * @author lzou
 */
public
class SubscriberAuxiliaryServiceBySubIdAndSvcId
    implements Predicate,XStatement
{
	protected String subscriberIdentifier;
	protected long auxServiceIdentifier;

    /**
     * Creates a new FindAuxiliaryServiceForSubscriberPredicate.
     *
     * @param subscriberIdentifier The Subscriber identifier used to find
     * corresponding SubscriberAuxiliaryServices.
     */
    public SubscriberAuxiliaryServiceBySubIdAndSvcId(final String subscriberIdentifier, final long auxSvcIdentifier)
    {
    	setSubscriberIdentifier(subscriberIdentifier);
    	setAuxServiceIdentifier(auxSvcIdentifier);
    }

    /**
     * @see com.redknee.framework.xhome.filter.Predicate#f(com.redknee.framework.xhome.context.Context, java.lang.Object)
     */
    public boolean f(Context _ctx,final Object obj)
    {
        final SubscriberAuxiliaryService association =
            (SubscriberAuxiliaryService)obj;

        if (association.getSubscriberIdentifier() == null)
        {
            return false;
        }

        if ( association.getAuxiliaryServiceIdentifier() == -1 )
        {
            return false;
        }

        return association.getSubscriberIdentifier().equals(subscriberIdentifier) &&
        	association.getAuxiliaryServiceIdentifier() == getAuxServiceIdentifier();
    }

    /**
     * Creates the SQL constraint used to find the SubscriberAuxiliaryServices.
     *
     * @param subscriberIdentifier The Subscriber identifier used to find
     * corresponding SubscriberAuxiliaryServices.
     * @return The SQL constraint used to find the SubscriberAuxiliaryServices.
     */
    public String createStatement(Context ctx)
    {
        // TODO - 2004-08-11 - Should ensure that the identifier does not
        // contain invalid characters.
        final String sql =
            SubscriberAuxiliaryService.SUBSCRIBERIDENTIFIER_PROPERTY
            + " = '"
            + getSubscriberIdentifier()
            + "' and "
            + SubscriberAuxiliaryService.AUXILIARYSERVICEIDENTIFIER_PROPERTY
            + " = "
            + getAuxServiceIdentifier();

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
	 * @return Returns the auxServiceIdentifier.
	 */
	public long getAuxServiceIdentifier()
	{
		return auxServiceIdentifier;
	}
	/**
	 * @param auxServiceIdentifier The auxServiceIdentifier to set.
	 */
	public void setAuxServiceIdentifier(long auxServiceIdentifier)
	{
		this.auxServiceIdentifier = auxServiceIdentifier;
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
