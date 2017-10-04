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
public interface ReportForecastable
{
    public DunningLevel calculateForecastedLevel(final Context context, final AbstractBean bean,
            final List<? extends AbstractBean> agedDebts, final Currency currency,
            final List<? extends AbstractBean> dunningReportRecordAgedDebt,
            final boolean current, final LongHolder dunningAmount, 
            final ObjectHolder dunnedAgedDebt,Date runningDate,DunningPolicy policy);
}


