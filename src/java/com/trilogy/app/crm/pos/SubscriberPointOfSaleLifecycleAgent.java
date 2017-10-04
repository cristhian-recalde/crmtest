package com.trilogy.app.crm.pos;

import java.util.Date;

import com.trilogy.app.crm.bean.AccountHome;
import com.trilogy.app.crm.bean.AccountXInfo;
import com.trilogy.app.crm.bean.MsisdnHome;
import com.trilogy.app.crm.bean.MsisdnStateEnum;
import com.trilogy.app.crm.bean.MsisdnXInfo;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.lifecycle.LifecycleAgentScheduledTask;
import com.trilogy.app.crm.log.CoreERLogger;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.app.crm.support.PointOfSaleConfigurationSupport;
import com.trilogy.framework.lifecycle.LifecycleException;
import com.trilogy.framework.lifecycle.LifecycleStateEnum;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.Or;
import com.trilogy.framework.xhome.filter.Predicate;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.visitor.AbortVisitException;
import com.trilogy.framework.xlog.log.LogSupport;

public class SubscriberPointOfSaleLifecycleAgent extends LifecycleAgentScheduledTask
{
    private static final long serialVersionUID = 1L;

    public SubscriberPointOfSaleLifecycleAgent(Context ctx, final String agentId) throws AgentException
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
                sb.append("Running Subscriber Point of Sale extrator for date '");
                sb.append(CoreERLogger.formatERDateDayOnly(date));
                sb.append("'.");
                LogSupport.debug(ctx, this, sb.toString());
            }

            Home home = (Home) ctx.get(MsisdnHome.class);

            Or state = new Or()
                    .add(new EQ(MsisdnXInfo.STATE, MsisdnStateEnum.HELD))
                    .add(new EQ(MsisdnXInfo.STATE, MsisdnStateEnum.IN_USE));
            
            And predicate = new And()
                    .add(state)
                    .add(new EQ(MsisdnXInfo.SUBSCRIBER_TYPE, SubscriberTypeEnum.POSTPAID));
            AccumulatorProcessor accProcessor = new AccumulatorProcessor(ctx, predicate, "MSISDNAccumulatorProcessor");
            SubscriberAccumulatorVisitor visitor = new SubscriberAccumulatorVisitor(ctx, date, accProcessor.getLogger());
            accProcessor.update(ctx, date, home, visitor);
        
            /* Call to PointOfSaleProducer to: 
             * create IVR Extract File
             */
            PointOfSaleConfiguration config = PointOfSaleConfigurationSupport.getPOSConfig(ctx);
            PointOfSaleProducer producer = new PointOfSaleProducer(ctx,
                    new PointOfSaleConsumer(ctx), "Point of Sale Process", config.getThreadCount(), config.getConcurrentCount());
            
            producer.createPOSIVRExtractFile(ctx, this);    
        }
        catch (final Throwable exception)
        {
            final String message = exception.getMessage();
            LogSupport.minor(ctx, getClass().getName(), message, exception);
        }
    }
    
    private void verifyIfLifeCycleAgentIsRunning(Context ctx) throws AbortVisitException
    {
    	if (!LifecycleStateEnum.RUNNING.equals(this.getState()) && !LifecycleStateEnum.RUN.equals(this.getState()))
        {
            String msg = "Lifecycle agent " + this.getAgentId() + " no longer running.  Remaining accounts will be processed next time.";
            LogSupport.info(ctx, this, msg);
            throw new AbortVisitException(msg);
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
