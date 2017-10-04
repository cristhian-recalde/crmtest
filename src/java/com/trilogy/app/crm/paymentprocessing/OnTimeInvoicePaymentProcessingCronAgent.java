package com.trilogy.app.crm.paymentprocessing;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;

import com.trilogy.app.crm.bean.BillCycle;
import com.trilogy.app.crm.support.CronTaskSupport;
import com.trilogy.framework.core.cron.AgentEntry;
import com.trilogy.framework.core.cron.agent.CronContextAgent;
import com.trilogy.framework.core.cron.agent.CronContextAgentException;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAwareSupport;
import com.trilogy.framework.xlog.log.LogSupport;

public abstract class OnTimeInvoicePaymentProcessingCronAgent extends
        ContextAwareSupport implements CronContextAgent

{

    /**
     * The date format used for specifying the "current date" in parameter 1.
     * This format is currently consistent with other CronAgents.
     */
    private static final String DATE_FORMAT_STRING = "yyyyMMdd";

    /**
     * @see com.redknee.framework.xhome.context.ContextAgent#execute(com.redknee.framework.xhome.context.Context)
     */
    public void execute(Context ctx) throws AgentException
    {
    	LogSupport.debug(ctx, getClass().getName(),
                "Invoice payment processing cron task initiated.");

        final Date billingDate = getCurrentDate(ctx);

        final Collection<BillCycle> billingCycles = getBillingCyclesToProcess(
                ctx, billingDate);
        for (BillCycle cycle : billingCycles)
        {
            final int identifier = cycle.getBillCycleID();

            /*ReportUtilities
            	.debug(
                    ctx,
                    getClass().getName(),
                    "Invoice payment processing cron task processing billing cycle {0}.",
                    identifier);*/

            try
            {
               InvoicePaymentProcessingSupport.processBillCycle(ctx, cycle,
                        OnTimePaymentProcessor.instance(), billingDate, false);
            }
            catch (final Exception exception)
            {
            	/*ReportUtilities.debug(ctx,
                                getClass().getName(),
                                "Invoice payment processing cron task encounterred exception processing identifer {0}.",
                                new String[]
                                    { Integer.toString(identifier) }, exception);*/
            	
                throw new CronContextAgentException(
                        "Invoice payment processing cron task encounterred exception.",
                        exception);
                        
            }
        }

        /*ReportUtilities.debug(ctx, getClass().getName(),
                "Invoice payment processing cron task complete."); */
    }

    public void stop()
    {
        // TODO - 2003-11-17 - Is this necessary?
    }

    public static String getParameter1(final Context context)
    {
		final AgentEntry entry = (AgentEntry)context.get(AgentEntry.class);
		final String value = entry.getTask().getParam0();
        return value;
    }
    
    /**
     * Gets the "current date" for the Invoice Generation. Returns the DAY
     * BEFORE, either: - Param1 in the CRON task Configuration, or if Param1 is
     * null - today
     * 
     * @param context
     *            The operating context.
     * @return The "current date" for the invoice run.
     */
    private Date getCurrentDate(final Context context) throws AgentException
    {
        final String dateParameter = getParameter1(context);

        final Calendar calendar = Calendar.getInstance();
        Date currentDate;
        if (dateParameter == null || dateParameter.trim().length() == 0)
        {
            calendar.add(Calendar.DAY_OF_MONTH, -1);
            currentDate = calendar.getTime();
        }
        else
        {
            try
            {
                final SimpleDateFormat dateFormat = new SimpleDateFormat(
                        DATE_FORMAT_STRING);
                currentDate = dateFormat.parse(dateParameter.trim());
                calendar.setTime(currentDate);
                // Day before the date set in Param1
                calendar.add(Calendar.DAY_OF_MONTH, -1);
                currentDate = calendar.getTime();
            }
            catch (final Throwable throwable)
            {
                throw new AgentException(
                        "Unable to determine date from parameter: \""
                                + dateParameter + "\""
                                + " Date  must be in format : yyyyMMdd.",
                        throwable);
            }
        }

        return currentDate;
    }

    /**
     * Gets the billing cycles which should be processed by this invoice payment
     * processor.
     * 
     * @param ctx
     *            The operating context.
     * @param billingDate
     *            has the bill cycle date.
     * @return A list of billing cycles.
     */
    protected abstract Collection<BillCycle> getBillingCyclesToProcess(
            final Context ctx, final Date billingDate);

 
}
