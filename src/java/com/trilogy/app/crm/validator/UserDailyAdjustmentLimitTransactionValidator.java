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
package com.trilogy.app.crm.validator;

import java.util.Date;

import com.trilogy.app.crm.bean.TransactionXInfo;
import com.trilogy.app.crm.bean.UserDailyAdjustmentLimit;
import com.trilogy.app.crm.bean.UserDailyAdjustmentLimitXInfo;
import com.trilogy.app.crm.bean.core.Transaction;
import com.trilogy.app.crm.support.AuthSupport;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.app.crm.support.CurrencyPrecisionSupportHelper;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.app.crm.support.SystemSupport;
import com.trilogy.framework.core.locale.Currency;
import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.beans.Validator;
import com.trilogy.framework.xhome.beans.XBeans;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.EQDay;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.LogSupport;

/**
 * Home responsible to validate the user daily adjustment limit.
 * @author Marcio Marques
 * @since 9.1.1
 *
 */
public class UserDailyAdjustmentLimitTransactionValidator implements Validator
{

    @Override
    public void validate(Context ctx, Object obj) throws IllegalStateException
    {
        Transaction transaction = (Transaction) obj;
        CompoundIllegalStateException cise = new CompoundIllegalStateException();

        String userID = SystemSupport.getAgent(ctx);
        try
        {
            long transactionLimit = getUserTransactionLimit(ctx, userID, transaction.getAmount());
            if (Math.abs(transaction.getAmount()) > transactionLimit)
            {
                String transactionLimitFormatted = CurrencyPrecisionSupportHelper.get(ctx).formatStorageCurrencyValue(ctx,
                        (Currency) ctx.get(Currency.class, Currency.DEFAULT), transactionLimit);
                cise.thrown(new IllegalPropertyArgumentException(TransactionXInfo.AMOUNT, "Agent '" + userID + 
                        "' not allowed to create transaction because this would exceed his/her daily adjustment limit left for the day of "
                        + transactionLimitFormatted));
            }
        }
        catch (HomeException e)
        {
            LogSupport.minor(ctx, this, "Unable to validate the daily adjustment limit left for agent '" + userID
                    + "': " + e.getMessage(), e);
            cise.thrown(new IllegalPropertyArgumentException(TransactionXInfo.AMOUNT,
                    "Unable to validate the daily adjustment limit lefr for agent '" + userID + "'"));
        }
        
        cise.throwAll();
    }

    public static long getUserTransactionLimit(Context ctx, String userID, long transactionAmount) throws HomeException
    {
        transactionAmount = Math.abs(transactionAmount);
        long configuredLimit = AuthSupport.getUserLimitFromGroup(ctx, userID);
        final long transactionLimit;

        if (configuredLimit != UserDailyAdjustmentLimit.DEFAULT_CONFIGUREDLIMIT)
        {
            And filter = new And();
            Date date = CalendarSupportHelper.get(ctx).getDateWithNoTimeOfDay(
                    CalendarSupportHelper.get(ctx).getRunningDate(ctx));
            filter.add(new EQDay(UserDailyAdjustmentLimitXInfo.LIMIT_DATE, date));
            filter.add(new EQ(UserDailyAdjustmentLimitXInfo.USER_ID, SystemSupport.getAgent(ctx)));
    
            UserDailyAdjustmentLimit limit = HomeSupportHelper.get(ctx).findBean(ctx, UserDailyAdjustmentLimit.class,
                    filter);
            if (limit == null)
            {
                try
                {
                    limit = (UserDailyAdjustmentLimit) XBeans.instantiate(UserDailyAdjustmentLimit.class, ctx);
                }
                catch (Throwable t)
                {
                    LogSupport.minor(ctx, UserDailyAdjustmentLimitTransactionValidator.class.getName(), "Unable to instantiate UserDailyAdjustmentLimit with XBeans. Using new UserDailyAdjustmentLimit() instead: " + t.getMessage(), t);
                    limit = new UserDailyAdjustmentLimit();
                }
                limit.setLimitDate(date);
                limit.setUserID(userID);
                limit.setConfiguredLimit(configuredLimit);
                limit.setLimitIncrease(0);
                limit.setTotalAmount(0);
                HomeSupportHelper.get(ctx).createBean(ctx, limit);
            }
            else
            {
                limit.setConfiguredLimit(configuredLimit);
            }

            if (limit.getLimit() - limit.getTotalAmount() < 0)
            {
                transactionLimit = 0;
            }
            else
            {
                transactionLimit = limit.getLimit() - limit.getTotalAmount();
            }
        }
        else
        {
            transactionLimit = transactionAmount;
        }

        return transactionLimit;
    }

}
