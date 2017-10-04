package com.trilogy.app.crm.service;

import java.util.Date;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;


public class AnnualPeriodHandler implements ServicePeriodHandler, ChargingCycleHandler
{
    public double calculateRate(final Context context, final Date billingDate, final int billingCycleDay, final int spid)
    {
        throw new IllegalArgumentException("Annual period not implemented.");
    }

    public Date calculateCycleStartDate(final Context context, final Date billingDate, final int billingCycleDay, final int spid)
    {
        throw new IllegalArgumentException("Annual period not implemented.");
    }    
    
    public Date calculateCycleEndDate(final Context context, final Date billingDate, final int billingCycleDay, final int spid)
    {
        throw new IllegalArgumentException("Annual period not implemented.");
    } 

    public double calculateRate(final Context context, final Date startDate, final Date billingDate, final int billingCycleDay, final int spid, final String subscriberId, final Object item)
    {
        throw new IllegalArgumentException("Annual period not implemented.");
    }

    public double calculateRate(final Context context, final Date billingDate, final int billingCycleDay, final int spid, final String subscriberId, final Object item) throws HomeException
    {
        throw new IllegalArgumentException("Annual period not implemented.");
    }
    
    public double calculateRefundRate(final Context context, final Date billingDate, final int billingCycleDay, final int spid, final String subscriberId, final Object item) throws HomeException
    {
        return -1.0 * calculateRate(context, billingDate, billingCycleDay, spid, subscriberId, item);
    }

    public Date calculateCycleStartDate(final Context context, final Date billingDate, final int billingCycleDay, final int spid, final String subscriberId, final Object item) throws HomeException
    {
        throw new IllegalArgumentException("Annual period not implemented.");
    }
    
    public Date calculateCycleEndDate(final Context context, final Date billingDate, final int billingCycleDay, final int spid, final String subscriberId, final Object item) throws HomeException
    {
        throw new IllegalArgumentException("Annual period not implemented.");
    }
    
    public static AnnualPeriodHandler instance()
    {
        throw new IllegalArgumentException("Annual period not implemented.");
    }

    /**
	 * {@inheritDoc}
	 */
	public double calculateRefundRateBasedOnUsage(Context context,
			Date billingDate, int billingCycleDay, Date startDate, int spid,
			String subscriberId, Object item, int unbilledDays) throws HomeException {
		throw new UnsupportedOperationException("Operation Not Supported.");
	}
    
}
