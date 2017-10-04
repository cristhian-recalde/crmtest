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
package com.trilogy.app.crm.account.filter;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.filter.Predicate;
import com.trilogy.framework.xhome.visitor.AbortVisitException;

/**
 * Returns if the account is of Billing Type POSTPAID or CONVERGE.
 * @author ali
 */
public class PostpaidAccountPredicate implements Predicate 
{

    public PostpaidAccountPredicate()
    {
    }
    
    
    public boolean f(Context arg0, Object obj) throws AbortVisitException 
    {
        Account account = (Account)obj;
        SubscriberTypeEnum sysType = account.getSystemType();

        return (sysType == SubscriberTypeEnum.HYBRID || sysType == SubscriberTypeEnum.POSTPAID);
    }

}
