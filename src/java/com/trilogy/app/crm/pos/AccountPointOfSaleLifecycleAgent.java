package com.trilogy.app.crm.pos;

import java.util.Date;

import com.trilogy.app.crm.bean.AccountHome;
import com.trilogy.app.crm.bean.AccountXInfo;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.lifecycle.LifecycleAgentScheduledTask;
import com.trilogy.app.crm.log.CoreERLogger;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.app.crm.support.PointOfSaleConfigurationSupport;
import com.trilogy.framework.lifecycle.LifecycleException;
import com.trilogy.framework.lifecycle.LifecycleStateEnum;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.Or;
import com.trilogy.framework.xhome.filter.Predicate;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.visitor.AbortVisitException;
import com.trilogy.framework.xlog.log.LogSupport;

public class AccountPointOfSaleLifecycleAgent extends LifecycleAgentScheduledTask
{
    private static final long serialVersionUID = 1L;

    public AccountPointOfSaleLifecycleAgent(Context ctx, final String agentId) throws AgentException
    {
        super(ctx, agentId);
    }

    /**
     * {@inheritDoc}
     */
    protected void start(Context ctx) throws LifecycleException
    {
        try
        {
            final Date date = getDate(ctx);
            if (LogSupport.isDebugEnabled(ctx))
            {
                StringBuilder sb = new StringBuilder();
                sb.append("Running Account Point of Sale extrator for date '");
                sb.append(CoreERLogger.formatERDateDayOnly(date));
                sb.append("'.");
                LogSupport.debug(ctx, this, sb.toString());
            }

            Home home = (Home) ctx.get(AccountHome.class);
            

            Predicate predicate = new Or()
                    .add(new EQ(AccountXInfo.SYSTEM_TYPE, SubscriberTypeEnum.HYBRID))
                    .add(new EQ(AccountXInfo.SYSTEM_TYPE, SubscriberTypeEnum.POSTPAID));
            AccumulatorProcessor accProcessor = new AccumulatorProcessor(ctx, predicate, "AccountAccumulatorProcessor");
            AccountAccumulatorVisitor visitor = new AccountAccumulatorVisitor(ctx, date, accProcessor.getLogger());
            accProcessor.update(ctx, date, home, visitor);
        
            
            /* Call to PointOfSaleProducer to: 
             * 1) create External Agents File
             * 2) create Cashier File
             * 3) create Conciliation File
             * 4) create Payment Exceptions File 
             * 5) create IVR Extract File
             */
            PointOfSaleConfiguration config = PointOfSaleConfigurationSupport.getPOSConfig(ctx);
            PointOfSaleProducer producer = new PointOfSaleProducer(ctx,
                    new PointOfSaleConsumer(ctx), "Point of Sale Process", config.getThreadCount(), config.getConcurrentCount());
            
            producer.createExternalAgentsFile(ctx, this);
            
            producer.createCashierFile(ctx, this);
            
            producer.createConciliationFile(ctx, date, this);
            
            producer.createPOSPaymentExceptionFile(ctx, date, this);
            
            producer.createPOSIVRExtractFile(ctx, this);        
        }
        catch (final Throwable exception)
        {
            final String message = exception.getMessage();
            LogSupport.minor(ctx, getClass().getName(), message, exception);
        }
    }
    

    /**
     * {@inheritDoc}
     */
    public boolean isEnabled(Context ctx)
    {
        return PointOfSale.isEnabled(ctx);
    }

    private Date getDate(final Context context)
    {
        Date date = getParameter1(context, Date.class);
        if (date==null)
        {
            date = CalendarSupportHelper.get(context).getDateWithNoTimeOfDay(CalendarSupportHelper.get(context).getRunningDate(context));
        }
        return date;
    }

}
