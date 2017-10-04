package com.trilogy.app.crm.dunning.processor;

import java.util.Date;
import java.util.List;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AgedDebt;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberAgedDebt;
import com.trilogy.app.crm.dunning.DunningPolicy;
import com.trilogy.app.crm.dunning.DunningProcessException;
import com.trilogy.app.crm.dunning.DunningProcessHelper;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.framework.core.locale.Currency;
import com.trilogy.framework.core.locale.CurrencyXInfo;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.LogSupport;

public abstract class AbstractDunningProcessor implements DunningProcessor
{
	public abstract Date getRunningDate();
	
	protected List<AgedDebt> getAgedDebts(final Context context, final Account account, final DunningPolicy policy)
    {
        Date oldestAgedDebtToLook = DunningProcessHelper.getOldestAgedDebtToLook(context, account, policy, getRunningDate());

        return account.getInvoicedAgedDebt(context, oldestAgedDebtToLook, true);
    }
	
	protected List<SubscriberAgedDebt> getSubscriberAgedDebts(final Context context, final Subscriber subscriber, final DunningPolicy policy)
    {
        Date oldestSubscriberAgedDebtToLook = DunningProcessHelper.getOldestSubscriberAgedDebtToLook(context, subscriber, policy, getRunningDate());

        return subscriber.getSubscriberInvoicedAgedDebt(context, oldestSubscriberAgedDebtToLook, true);
    }
	
	protected DunningPolicy getDunningPolicyOfAccount(Context ctx,Account account) throws DunningProcessException
	{
		try{
			return account.getDunningPolicy(ctx);
		}catch(HomeException he)
		{
			LogSupport.info(ctx, this, "Exception while determining DunningPolicy for "+account.getBAN(),he);
			throw new DunningProcessException(he);
		}
	}
	
	protected Currency retrieveCurrency(final Context context, final Account account) throws DunningProcessException
    {
        Currency currency = (Currency) context.get(Currency.class);
        if (currency==null || !currency.getCode().equals(account.getCurrency()))
        {
            try
            {
                currency = HomeSupportHelper.get(context).findBean(context, Currency.class,
                        new EQ(CurrencyXInfo.CODE, account.getCurrency()));
            }
            catch (final Exception exception)
            {
                StringBuilder cause = new StringBuilder();
                cause.append("Unable to retrieve currency '");
                cause.append(account.getCurrency());
                cause.append("'");
                StringBuilder sb = new StringBuilder();
                sb.append(cause);
                sb.append(" for account '");
                sb.append(account.getBAN());
                sb.append("': ");
                sb.append(exception.getMessage());
                LogSupport.minor(context, this, sb.toString(), exception);
                throw new DunningProcessException(cause.toString(), exception);
            }
        }

        if (currency == null)
        {
            StringBuilder cause = new StringBuilder();
            cause.append("Currency '");
            cause.append(account.getCurrency());
            cause.append("' not found");
            StringBuilder sb = new StringBuilder();
            sb.append(cause);
            sb.append(" for account '");
            sb.append(account.getBAN());
            sb.append("'");
            LogSupport.minor(context, this, sb.toString());
            throw new DunningProcessException(cause.toString());
        }
        return currency;
    }
}
