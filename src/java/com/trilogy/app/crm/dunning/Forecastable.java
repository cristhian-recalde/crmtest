/**
 * 
 */
package com.trilogy.app.crm.dunning;

import java.util.Date;
import java.util.List;

import com.trilogy.framework.core.locale.Currency;
import com.trilogy.framework.xhome.beans.AbstractBean;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.holder.LongHolder;
import com.trilogy.framework.xhome.holder.ObjectHolder;


/**
 * @author hmurumkar
 * @since 10.1.4
 */
public interface Forecastable
{
    public DunningLevel calculateForecastedLevel(final Context context, final AbstractBean bean,
            final List<? extends AbstractBean> agedDebtRecords, final Currency currency,
            final boolean paymentsConsidered, Date runningDate,DunningPolicy policy);
}