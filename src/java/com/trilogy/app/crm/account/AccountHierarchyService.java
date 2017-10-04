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
package com.trilogy.app.crm.account;

import com.trilogy.app.crm.bean.Account;

/**
 * @author Angie Li
 *
 * Given the BAN of the root account, the methods defined in this interface will
 * return results on queries involving this account's the entire topology (a.k.a. 
 * heirarchy -- its non-responsible sub-accounts).
 * 
 * There is a need for this, since searches started at the subscriber level (via the GUI)
 * have the in the context filtered SubscriberHome according to the BAN the subscriber 
 * belongs to.
 * 
 * To avoid filtered Homes in the context. We install this service that contains its own
 * context.
 *   
 */
public interface AccountHierarchyService 
{

    /**
     * Returns the number of active postpaid subscribers in the topoloty of the given account.
     * The check includes the given account's immediate subscribers, and the immediate subscribers
     * of the given account's non-responsible sub-accounts.
     * 
     * @param parentAccount root account of the heirarchy
     * @return int >= 0
     */
    public int getNumberOfActivePostpaidSubscribersInTopology(Account parentAccount);
}
