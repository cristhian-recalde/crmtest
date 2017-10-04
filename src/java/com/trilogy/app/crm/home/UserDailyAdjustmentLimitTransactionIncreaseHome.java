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
package com.trilogy.app.crm.home;

import java.util.Date;

import com.trilogy.app.crm.bean.UserDailyAdjustmentLimit;
import com.trilogy.app.crm.bean.UserDailyAdjustmentLimitXInfo;
import com.trilogy.app.crm.bean.core.Transaction;
import com.trilogy.app.crm.log.CoreERLogger;
import com.trilogy.app.crm.support.AuthSupport;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.app.crm.support.CurrencyPrecisionSupportHelper;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.app.crm.support.SystemSupport;
import com.trilogy.framework.core.locale.Currency;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.EQDay;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xlog.log.LogSupport;

/**
 * Home responsible to increase the user daily adjustment limit.
 * @author Marcio Marques
 * @since 9.1.1
 *
 */
public class UserDailyAdjustmentLimitTransactionIncreaseHome extends HomeProxy
{

    private static final long serialVersionUID = 1L;


    public UserDailyAdjustmentLimitTransactionIncreaseHome(Context ctx, Home delegate)
    {
        super(delegate);
    }


    public static void increaseDailyLimitUsage(Context ctx, String userID, long transactionAmount)
    {
        transactionAmount = Math.abs(transactionAmount);
        Date date = CalendarSupportHelper.get(ctx).getDateWithNoTimeOfDay(
                CalendarSupportHelper.get(ctx).getRunningDate(ctx));
        try
        {
            long configuredLimit = AuthSupport.getUserLimitFromGroup(ctx, userID);

            if (configuredLimit!=UserDailyAdjustmentLimit.DEFAULT_CONFIGUREDLIMIT)
            {
                And filter = new And();
                filter.add(new EQDay(UserDailyAdjustmentLimitXInfo.LIMIT_DATE, date));
                filter.add(new EQ(UserDailyAdjustmentLimitXInfo.USER_ID, SystemSupport.getAgent(ctx)));
                
                UserDailyAdjustmentLimit limit = HomeSupportHelper.get(ctx).findBean(ctx, UserDailyAdjustmentLimit.class,
                        filter);
    
                if (limit == null)
                {
                    limit = new UserDailyAdjustmentLimit();
                    limit.setLimitDate(date);
                    limit.setUserID(userID);
                    limit.setConfiguredLimit(configuredLimit);
                    limit.setLimitIncrease(0);
                    limit.setTotalAmount(transactionAmount);
                    HomeSupportHelper.get(ctx).createBean(ctx, limit);
                }
                else
                {
                    limit.setConfiguredLimit(configuredLimit);
                    limit.setTotalAmount(limit.getTotalAmount() + transactionAmount);
                    HomeSupportHelper.get(ctx).storeBean(ctx, limit);
                }
            }
        }
        catch (HomeException e)
        {
            String usage = CurrencyPrecisionSupportHelper.get(ctx).formatStorageCurrencyValue(ctx,
                    (Currency) ctx.get(Currency.class, Currency.DEFAULT), transactionAmount);
            LogSupport.minor(
                    ctx,
                    UserDailyAdjustmentLimitTransactionIncreaseHome.class.getName(),
                    "Unable to increase usage for Agent '" + userID + "' on date '"
                            + CoreERLogger.formatERDateDayOnly(date) + "' by " + usage + ": " + e.getMessage(), e);
        }
    }



    @Override
    public Object create(Context ctx, Object obj) throws HomeException, HomeInternalException
    {
        Transaction transaction = (Transaction) obj;
        String userID = SystemSupport.getAgent(ctx);

        Object result = super.create(ctx, transaction);
        increaseDailyLimitUsage(ctx, userID, transaction.getAmount());
        
        return result;
    }
}
