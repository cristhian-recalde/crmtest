package com.trilogy.app.crm.paymentprocessing;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import com.trilogy.app.crm.support.CronTaskSupportHelper;
import com.trilogy.framework.core.cron.agent.CronContextAgent;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAwareSupport;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;

public class InvoicePaymentProcessingCronAgent extends ContextAwareSupport implements CronContextAgent
{
    /**
     * The date format used for specifying the "current date" in parameter 1.
     * This format is currently consistent with other CronAgents.
     */
    private static final String DATE_FORMAT_STRING = "yyyyMMdd";

    public InvoicePaymentProcessingCronAgent(final InvoicePaymentProcessingAgent agent)
    {
        this.agent_ = agent;
    }

    /**
     * @see com.redknee.framework.xhome.context.ContextAgent#execute(com.redknee.framework.xhome.context.Context)
     */
    @Override
    public void execute(final Context ctx) throws AgentException
    {
        if (LogSupport.isDebugEnabled(ctx))
        {
            new DebugLogMsg(this,
                    "Invoice payment processing cron task initiated.",
                    null).log(ctx);
        }

        final Date billingDate = getCurrentDate(ctx);
        final Context subCtx = ctx.createSubContext();
        subCtx.put(InvoicePaymentProcessingAgent.BILLING_DATE, billingDate);
        agent_.execute(subCtx);

        if (LogSupport.isDebugEnabled(ctx))
        {
            new DebugLogMsg(this,
                    "Invoice payment processing cron task complete.",
                    null).log(ctx);
        }
    }

    @Override
    public void stop()
    {
        // TODO - 2003-11-17 - Is this necessary?
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
        final String dateParameter = CronTaskSupportHelper.get(context).getParameter1(context);

        final Calendar calendar = Calendar.getInstance();
        Date currentDate;
        if (dateParameter != null && !dateParameter.trim().isEmpty())
        {
            try
            {
                final SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT_STRING);
                currentDate = dateFormat.parse(dateParameter.trim());
                calendar.setTime(currentDate);
            }
            catch (final Throwable throwable)
            {
                throw new AgentException("Unable to determine date from parameter: \"" + dateParameter + "\""
                        + " Date  must be in format : yyyyMMdd.", throwable);
            }
        }

        return calendar.getTime();
    }

    private final InvoicePaymentProcessingAgent agent_;
}
