package com.trilogy.app.crm.support;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.CreditCategory;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.home.sub.UserAdjustmentLimitValidator;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;

public class SubscriberCreditLimitSupport {
	
	
	public static void updateCreditLimit(final Context ctx,Account acct,Subscriber subs,long depositAmount) throws Exception
    {
        if (LogSupport.isDebugEnabled(ctx))
        {
            new DebugLogMsg(MODULE, "Subscriber credit limit updating", null).log(ctx);
        }
        try
        {
            final CreditCategory creditCategory = HomeSupportHelper.get(ctx).findBean(ctx, CreditCategory.class, Integer.valueOf(acct.getCreditCategory()));
            /*
             * [Cindy] 2007-11-12: Credit limit should not be updated on deposit release
             * if the option is not set in SPID.
             */
            if (depositAmount > 0 || SpidSupport.isCreditLimitUpdatedOnDepositRelease(ctx, subs.getSpid()))
            {
                subs.setCreditLimit((long) (subs.getCreditLimit(ctx) + depositAmount * creditCategory.getFactor()));
                /*
                 * [Cindy] 2007-11-27 TT7102900043: Skip credit limit check when the
                 * change is caused by deposit made.
                 */
                ctx.put(UserAdjustmentLimitValidator.SKIP_USER_ADJUSTMENT_LIMIT_VALIDATION, true);
                updateSubscriber(ctx,subs);
            }
            LogSupport.info(ctx, MODULE, "credit limit changed to " + subs.getCreditLimit());
        }
        catch (final Exception e)
        {
        	LogSupport.major(ctx, MODULE, "Fail to Change Subscriber Credit limit");
        	throw new Exception("Fail to Change Subscriber Credit limit");
        }
    }
	
	
	private static Subscriber updateSubscriber(Context ctx, Subscriber subscriber)
			throws DepositReleaseException {
		if (subscriber != null) {
			try {
				subscriber = (Subscriber) HomeSupportHelper.get(ctx).storeBean(ctx,
						subscriber);
			} catch (HomeException e) {
				LogSupport.minor(ctx, MODULE, "Fail to update Subscriber" + e, e);
				throw new DepositReleaseException("Fail to update Subscriber"
						+ subscriber.getId());
			}
		}
		return subscriber;
	}
	
	private static final String MODULE = SubscriberCreditLimitSupport.class.getName();
}
