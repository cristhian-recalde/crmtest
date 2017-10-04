/*
 *  TransactionMSISDNPredicate.java
 *
 *  Author : Gary Anderson
 *  Date   : 2003-12-05
 *
 *  Copyright (c) Redknee, 2003
 *  - all rights reserved
 */
package com.trilogy.app.crm.filter;

import com.trilogy.app.crm.bean.Transaction;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.filter.Predicate;


/**
 * A Predicate used to find Transactions with specific MSISDN.
 *
 * @author gary.anderson@redknee.com
 */
public class TransactionMSISDNPredicate
	implements Predicate
{
    /**
     * Creates a new predicate with the given criteria.
     *
     * @param msisdn The MSISDN to match.
     *
     * @exception IllegalArgumentException Thrown if the given MSISDN is
     * null. 
     */ 
	public TransactionMSISDNPredicate(final String msisdn)
	{
        if (msisdn == null)
        {
            throw new IllegalArgumentException(
                "Could not initialize predicate.  "
                + "The given MSISDN parameter is null.");
        }
        
		msisdn_ = msisdn;
	}

    
    // INHERIT
	public boolean f(Context ctx,final Object obj)
	{
		final Transaction detail = (Transaction)obj;

		return msisdn_.equals(detail.getMSISDN());
	}


    /**
     * The MSISDN to match.
     */
	protected final String msisdn_;
}
