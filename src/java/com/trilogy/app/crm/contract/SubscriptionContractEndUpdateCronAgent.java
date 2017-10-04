package com.trilogy.app.crm.contract;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.framework.core.cron.AgentEntry;
import com.trilogy.framework.core.cron.agent.CronContextAgent;
import com.trilogy.framework.core.cron.agent.CronContextAgentException;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAwareSupport;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.LTE;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.visitor.Visitor;
import com.trilogy.framework.xlog.log.InfoLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MajorLogMsg;
import com.trilogy.framework.xlog.log.MinorLogMsg;
import com.trilogy.framework.xlog.log.SeverityEnum;


public class SubscriptionContractEndUpdateCronAgent extends ContextAwareSupport implements CronContextAgent
{

    private static final String DATE_FORMAT_STRING = "yyyyMMdd";


    public SubscriptionContractEndUpdateCronAgent()
    {
    }


    /**
     * @see com.redknee.framework.xhome.context.ContextAgent#execute(com.redknee.framework.xhome.context.Context)
     */
    @Override
    public void execute(final Context ctx) throws AgentException
    {
        final AgentEntry entry = (AgentEntry) ctx.get(AgentEntry.class);
        String endDateString = entry.getTask().getParam0();
        Date endDate = CalendarSupportHelper.get(ctx).getDateWithNoTimeOfDay(new Date());
        if (endDateString != null)
        {
            if (endDateString.trim().length() != 0)
            {
                try
                {
                    endDate = new SimpleDateFormat(DATE_FORMAT_STRING).parse(endDateString);
                }
                catch (ParseException e)
                {
                    new MajorLogMsg(this, "Fails to parse the input billing date", e).log(ctx);
                }
            }
            else
            {
                endDate = CalendarSupportHelper.get(ctx).getDateWithNoTimeOfDay(new Date());
            }
        }
        String subId = entry.getTask().getParam1();
        
        final Context subCtx = ctx.createSubContext();
        if (LogSupport.isEnabled(ctx, SeverityEnum.INFO))
        {
            new InfoLogMsg(this, "Subscription contract end processing cron task initiated for " + endDate + " .", null)
                    .log(ctx);
        }
        try
        {
            And predicate = new And();
            Visitor visitor = new SubscriptionContractEndUpdateProcessingVisitor(endDate);
            Home home = (Home) subCtx.get(SubscriptionContractHome.class);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(endDate);
            predicate.add(new LTE(SubscriptionContractXInfo.BONUS_END_DATE, endDate));
            if (subId != null && subId.trim().length() > 0)
            {
                predicate.add(new EQ(SubscriptionContractXInfo.SUBSCRIPTION_ID, subId));
            }
            home.forEach(subCtx, visitor, predicate);
        }
        catch (final Exception exception)
        {
            new MinorLogMsg(ctx,
                    "Subscription contract bonus end update cron task encounterred exception processing end date " + endDate,
                    exception).log(ctx);
            throw new CronContextAgentException(
                    "Subscription contract end update processing cron task encountered exception.", exception);
        }
        if (LogSupport.isEnabled(ctx, SeverityEnum.INFO))
        {
            new InfoLogMsg(this, "Subscription contract end processing cron task is completed for " + endDate + " .",
                    null).log(ctx);
        }
    }


    @Override
    public void stop()
    {

    }
}
