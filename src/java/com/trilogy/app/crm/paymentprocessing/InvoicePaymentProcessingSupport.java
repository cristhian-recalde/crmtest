package com.trilogy.app.crm.paymentprocessing;

import java.util.Calendar;
import java.util.Date;

import com.trilogy.app.crm.bean.BillCycle;
import com.trilogy.app.crm.bean.InvoiceHome;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.visitor.ParallelVisitor;
import com.trilogy.framework.xlog.log.MinorLogMsg;

public class InvoicePaymentProcessingSupport
{
    /**
     * Number of invoice payment processing threads.
     */
    public static final int NUM_POSTPROCESSING_THREADS = 5;

    private InvoicePaymentProcessingSupport()
    {
        // empty
    }

    /**
     * Process all invoices past due in a bill cycle.
     * 
     * @param context
     *            Operating context.
     * @param processor
     *            Invoice payment processor.
     * @param billingDate
     *            The date to do the processing on.
     * @param forceReprocess
     *            Whether reprocess invoice if it has already been processed.
     *            Highly recommended to leave this as <code>false</code>!
     * @throws AgentException
     *             Thrown if there are problems with processing.
     */
    public static void processBillCycle(Context context, BillCycle cycle,
            PaymentPromotionProcessor processor, Date billingDate,
            boolean forceReprocess) throws AgentException
    {
        Home home = (Home) context.get(InvoiceHome.class);

        final String recordTableName = "InvoicePaymentRecord";
        final StringBuffer sql = new StringBuffer();

        sql.append(" dueDate < ");
        sql.append(billingDate.getTime());
        

        if (!forceReprocess) 
        {
            /*
             * [Cindy Wong] TT#10052519007 -- only process the latest month's invoice unless 
             * forceReprocess is set.
             */
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(billingDate);
            calendar.add(Calendar.MONTH, -1);
            sql.append(" AND dueDate >= ");
            sql.append(calendar.getTimeInMillis());

            sql.append(" AND invoiceId NOT IN ");
            sql.append("(SELECT DISTINCT invoiceId FROM ");
            sql.append(recordTableName);
            sql.append(")");
        }
        try
        {
            home.forEach(context, new ParallelVisitor(
                    NUM_POSTPROCESSING_THREADS,
                    new InvoicePaymentProcessingVisitor(processor, billingDate,
                            forceReprocess)), sql.toString());
        }
        catch (HomeException exception)
        {
            new MinorLogMsg(InvoicePaymentProcessingSupport.class,
                    "Exception caught during invoice postprocessing", exception)
                    .log(context);
        }
    }
}
