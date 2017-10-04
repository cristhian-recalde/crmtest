/*
 * This code is a protected work and subject to domestic and international copyright
 * law(s). A complete listing of authors of this work is readily available. Additionally,
 * source code is, by its very nature, confidential information and inextricably contains
 * trade secrets and other information proprietary, valuable and sensitive to Redknee. No
 * unauthorized use, disclosure, manipulation or otherwise is permitted, and may only be
 * used in accordance with the terms of the license agreement entered into with Redknee
 * Inc. and/or its subsidiaries.
 * 
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.filter;

/*
 * CallDetailpostedDateAfterPredicate.java
 * 
 * Author : Simar Singh, Date : 2009-03-18
 * 
 * Copyright (c) Redknee, 2009 - all rights reserved
 */
import com.trilogy.app.crm.transfer.TransferDispute;
import com.trilogy.app.troubleticket.Common;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.filter.Predicate;


/**
 * A Predicate used to find CallDetails with specific posdtedDate after the given date.
 */
public class TransferDisputeExtensionTicket implements Predicate
{

    /**
     * 
     */
    private static final long serialVersionUID = 1L;


    /**
     * @return True if the context has a trouble-ticket extension as Transfer Dispute
     */
    public boolean f(Context ctx, final Object obj)
    {
        if (ctx.has(Common.TT_ENTITY_CREATE_TYPE))
        {
            if (TransferDispute.class.equals(ctx.get(Common.TT_ENTITY_CREATE_TYPE)))
            {
                return true;
            }
        }
        return false;
    }
}
