package com.trilogy.app.crm.home;

import java.util.Calendar;
import java.util.Date;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;

import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.PricePlan;
import com.trilogy.app.crm.bean.PricePlanSubTypeEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.bean.TopUpSchedule;
import com.trilogy.app.crm.bean.TopUpScheduleXInfo;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.app.crm.support.PricePlanSupport;
import com.trilogy.app.crm.support.SpidSupport;

/**
 * @author chandrachud.ingale
 * @since
 */
public class UpdateTopUpScheduleOnATUHome extends HomeProxy
{

    private static final long serialVersionUID = 2400450393669285719L;


    public UpdateTopUpScheduleOnATUHome(Home delegate)
    {
        super(delegate);
    }


    @Override
    public Object store(Context ctx, Object obj) throws HomeException, HomeInternalException
    {
    	LogSupport.debug(ctx, this, "SubscriberPipeline[store].....");
    	obj = super.store(ctx, obj);

        Subscriber sub = (Subscriber) obj;
        if (sub.getSubscriberType().equals(SubscriberTypeEnum.PREPAID))
        {

            PricePlan pricePlan = PricePlanSupport.getPlan(ctx, sub.getPricePlan());
            switch (pricePlan.getPricePlanSubType().getIndex())
            {
                case PricePlanSubTypeEnum.PAYGO_INDEX:
                    updateTopUpSchedule(ctx, sub);
                    break;
            }
        }

        return obj;
    }


    /**
     * @param ctx
     * @param sub
     * @throws HomeException
     * @throws HomeInternalException
     */
    private void updateTopUpSchedule(Context ctx, Subscriber sub) throws HomeInternalException, HomeException
    {
        new DebugLogMsg(this, "Updating nextApplicationDate for subscriber : " + sub.getId()).log(ctx);
        CRMSpid crmSpid = SpidSupport.getCRMSpid(ctx, sub.getSpid());
        int oneDayPriorSchedule = crmSpid.isScheduleCCAtuOneDayPriorExpiry() ? -1 : 0;

        Calendar expiryCalendar = Calendar.getInstance();
        expiryCalendar.setTime(sub.getExpiryDate());
        expiryCalendar.add(Calendar.DAY_OF_YEAR, oneDayPriorSchedule);

        Date nextApplicationDate = expiryCalendar.getTime();

        TopUpSchedule existingSchedule = HomeSupportHelper.get(ctx).findBean(ctx, TopUpSchedule.class,
                new EQ(TopUpScheduleXInfo.BAN, sub.getBAN()));

        if (existingSchedule != null)
        {
            Date existingNextApplicationDate = existingSchedule.getNextApplication();
            existingSchedule.setNextApplication(nextApplicationDate);
            new DebugLogMsg(this, "Updating TopUpSchedule nextApplicationDate for subscriber : " + sub.getId()
                    + " from : " + existingNextApplicationDate + " to : " + nextApplicationDate).log(ctx);
            HomeSupportHelper.get(ctx).storeBean(ctx, existingSchedule);
        }
        else
        {
        	if(LogSupport.isDebugEnabled(ctx))
        	{	
        		new DebugLogMsg(this, "NO TopUpSchedule present cannot update nextApplicationDate for subscriber : "
                    + sub.getId()).log(ctx);
        	}
        }
    }

}
