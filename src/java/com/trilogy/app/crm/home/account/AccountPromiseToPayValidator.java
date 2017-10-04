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
package com.trilogy.app.crm.home.account;

import java.util.Calendar;
import java.util.Date;

import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.beans.Validator;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.language.MessageMgr;

import com.trilogy.app.crm.account.AccountConstants;
import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountXInfo;
import com.trilogy.app.crm.support.CalendarSupportHelper;

public class AccountPromiseToPayValidator implements Validator
{
    /**
     * make sure the Promise-To-Pay Date is after today.
     */
    public void validate(final Context ctx, final Object obj) throws IllegalStateException
    {
        final CompoundIllegalStateException el = new CompoundIllegalStateException();
        final Account account = (Account) obj;

        final Date newPtpExpiryDate = account.getPromiseToPayDate();
        Date oldPtpExpiryDate = null;

        final Account oldAccount = (Account) ctx.get(AccountConstants.OLD_ACCOUNT);

        if (oldAccount != null)
        {
            oldPtpExpiryDate = oldAccount.getPromiseToPayDate();
        }

        if (oldPtpExpiryDate == null && newPtpExpiryDate != null
                && !newPtpExpiryDate.after(CalendarSupportHelper.get(ctx).getDateWithNoTimeOfDay(Calendar.getInstance().getTime())))
        {
            final MessageMgr msgMgr = new MessageMgr(ctx, this);

            el.thrown(new IllegalPropertyArgumentException(
                    AccountXInfo.PROMISE_TO_PAY_DATE,
                    "Must be after today."));

        }
        else if (oldPtpExpiryDate != null && newPtpExpiryDate != null && !newPtpExpiryDate.equals(oldPtpExpiryDate)
                && !newPtpExpiryDate.after(CalendarSupportHelper.get(ctx).getDateWithNoTimeOfDay(Calendar.getInstance().getTime())))
        {
            final MessageMgr msgMgr = new MessageMgr(ctx, this);

            el.thrown(new IllegalPropertyArgumentException(
                    AccountXInfo.PROMISE_TO_PAY_DATE,
                    "Must be after today."));
        }

        el.throwAll();
    }
}
