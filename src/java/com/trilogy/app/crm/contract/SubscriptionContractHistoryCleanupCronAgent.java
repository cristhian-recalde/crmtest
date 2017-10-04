package com.trilogy.app.crm.contract;

import java.util.Date;

import com.trilogy.framework.core.cron.AgentEntry;
import com.trilogy.framework.core.cron.agent.CronContextAgent;
import com.trilogy.framework.core.cron.agent.CronContextAgentException;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAwareSupport;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.GT;
import com.trilogy.framework.xhome.elang.LT;
import com.trilogy.framework.xhome.elang.LTE;
import com.trilogy.framework.xhome.elang.Or;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.visitor.Visitor;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.SeverityEnum;

import com.trilogy.app.crm.api.Constants;
import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.app.crm.support.HomeSupportHelper;


public class SubscriptionContractHistoryCleanupCronAgent extends ContextAwareSupport implements CronContextAgent
{

    public SubscriptionContractHistoryCleanupCronAgent()
    {
    }


    /**
     * @see com.redknee.framework.xhome.context.ContextAgent#execute(com.redknee.framework.xhome.context.Context)
     */
    @Override
    public void execute(final Context ctx) throws AgentException
    {
        final AgentEntry entry = (AgentEntry) ctx.get(AgentEntry.class);
        String spid = "";
        Date contractHistoryLastModifyDate = null;
        if(entry == null || entry.getTask().getParam0() == null || entry.getTask().getParam0().trim().equals(""))
        	spid = "1";
        else
        	spid = entry.getTask().getParam0();
        
        final Context subCtx = ctx.createSubContext();
        if (LogSupport.isEnabled(ctx, SeverityEnum.INFO))
        {
            LogSupport.info(ctx, this, "Subscription contract history cleanup cron task initiated.");
        }
        try
        {
            final CRMSpid crmspid = HomeSupportHelper.get(ctx).findBean(ctx, CRMSpid.class, Integer.parseInt(spid));//TODO: SPID
            if(!crmspid.isEnableSubscriberContractHistory()){
            	return;
            }
            
            Or predicate = new Or();
            And predicate1 = new And();
            And predicate2 = new And();
            Visitor visitor = new SubscriptionContractHistoryCleanupVisitor();
            Home home = (Home) subCtx.get(SubscriptionContractHistoryHome.class);
            
            int cleanupDays = crmspid.getSubscriberContractHistoryCleanUpDays();
            contractHistoryLastModifyDate = CalendarSupportHelper.get(ctx).getDateWithNoTimeOfDay(
            		CalendarSupportHelper.get(ctx).getDaysBefore(new Date(), cleanupDays));
            Date currentDate = CalendarSupportHelper.get(ctx).getDateWithNoTimeOfDay(new Date());
            
            predicate1.add(new EQ(SubscriptionContractHistoryXInfo.CONTRACT_STATUS, Constants.CONTRACT_ACTIVE));//currently active contracts
            predicate1.add(new LT(SubscriptionContractHistoryXInfo.CONTRACT_END_DATE, currentDate));//end date already passed
            
            predicate2.add(new EQ(SubscriptionContractHistoryXInfo.CONTRACT_STATUS, Constants.CONTRACT_INACTIVE));
            predicate2.add(new LTE(SubscriptionContractHistoryXInfo.RECORD_MODIFY_DATE, contractHistoryLastModifyDate));
            
            predicate.add(predicate1);
            predicate.add(predicate2);
            home.forEach(subCtx, visitor, predicate);
        }
        catch (final Exception exception)
        {
        	LogSupport.minor(ctx,this,
                    "Subscription contract history Cleanup cron task exception.",
                    exception);
            throw new CronContextAgentException(
                    "Subscription contract history Cleanup cron task exception.", exception);
        }
        if (LogSupport.isEnabled(ctx, SeverityEnum.INFO))
        {
            LogSupport.info(ctx, this, "Subscription contract history Cleanup cron task completed for:" + contractHistoryLastModifyDate + " .");
        }
    }


    @Override
    public void stop()
    {

    }
}
