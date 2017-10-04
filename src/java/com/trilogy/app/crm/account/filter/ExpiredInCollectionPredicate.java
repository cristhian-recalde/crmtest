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

import java.util.Date;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountStateEnum;
import com.trilogy.app.crm.support.CalendarSupportHelper;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.filter.Predicate;

/**
 * @author jchen
 *
 * Checking if in collection account pass grace days or not 
 */
public class ExpiredInCollectionPredicate implements Predicate
{
    public ExpiredInCollectionPredicate(int spid, int graceDays)
    {
    	spid_ = spid;
       expiryDate_ = CalendarSupportHelper.get().findDatesAfter(-graceDays);
    }

    public boolean f(Context ctx, Object obj)
    {
       Account account = (Account)obj;
       
       return account!=null && 
	       account.getSpid() == spid_ &&
	       account.getState()!=null &&
	       account.getState().equals(AccountStateEnum.IN_COLLECTION) &&
	       account.getInCollectionDate()!=null && 
	       account.getInCollectionDate().after(expiryDate_);
    }

    private Date expiryDate_;
    private int spid_;
    

}
