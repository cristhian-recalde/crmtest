package com.trilogy.app.crm.home;

import java.util.Collection;
import java.util.Date;

import com.trilogy.app.crm.bean.PricePlan;
import com.trilogy.app.crm.bean.PricePlanHome;
import com.trilogy.app.crm.bean.PricePlanSubTypeEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.TopUpSchedule;
import com.trilogy.app.crm.bean.Transaction;
import com.trilogy.app.crm.bean.paymentgatewayintegration.SubscriptionSupport;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.app.crm.support.Lookup;
import com.trilogy.app.crm.support.SubscriberSupport;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xlog.log.InfoLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;

/**
 * It executes super.create() first. This class is responsible for updating top up schedule of a subscriber after successful PP change.
 * 
 * @author vijay.gote
 * @since 9.9
 * 
 */
public class TopUpScheduleUpdatingOnPlanChangeHome extends HomeProxy 
{

    private static final long serialVersionUID = 1L;
    public static final String SKIP_PROVISIONING_AND_CHARGING_CALCULATIONS = "SKIP_PROVISIONING_AND_CHARGING_CALCULATIONS";
    
    public TopUpScheduleUpdatingOnPlanChangeHome(Home delegate) 
    {
        super(delegate);
    }

    @Override
    public Object create(Context ctx, Object obj) throws HomeException, HomeInternalException 
    {
    	Object return_obj = super.create(ctx, obj);
		try
		{
			updateSubscriptionTopUpSchedule(ctx, (Transaction) obj);
		}catch (Exception e) {
			LogSupport.major(ctx, this, "Exception occurred while updating services next-recurring-charge-date!!",e);
		}
		
		return return_obj;
    }
    

    private PricePlan getPricePlan(Context ctx, Subscriber subscriber) throws HomeException
    {
        try
        {
            Home pricePlanHome = (Home) ctx.get(PricePlanHome.class);
            if (pricePlanHome != null)
            {
                PricePlan pp = (PricePlan) pricePlanHome.find(ctx, subscriber.getPricePlan());
                if (pp != null)
                {
                    return pp;
                }
                else
                {
                    throw new HomeException("Could not find price-plan for ID :" + subscriber.getPricePlan());
                }
            }
            else
            {
                throw new HomeException("Could not find PricePlanHome in context!!");
            }
        }
        catch (HomeException e)
        {
            throw e;
        }
    }
    
    
    private void updateSubscriptionTopUpSchedule(Context ctx, Transaction txn) throws Exception
    {
    	Subscriber sub = (Subscriber) ctx.get(Subscriber.class);
    	if (sub == null)
    	{
    		sub = SubscriberSupport.getSubscriber(ctx, txn.getSubscriberID());
    	}

    	PricePlan newPricePlan = getPricePlan(ctx, sub);
    	final Subscriber oldSub = (Subscriber) ctx.get(Lookup.OLDSUBSCRIBER);
    	PricePlan oldPricePlan = null;
    	if(oldSub != null)
    	{
    		oldPricePlan = getPricePlan(ctx, oldSub);
    	}
    	// TT: CANTLS-1522 : [Regression] Koodo - TopupSchedule Data Incorrect When Activating With Add-ons
    	// making sure that this is happening on priceplan change only, not options change 
    	if(newPricePlan != null && oldPricePlan!=null && newPricePlan.getId() != oldPricePlan.getId())
    	{
    		updateTopupSchedule(ctx, sub, oldSub, sub.getExpiryDate(), oldPricePlan, newPricePlan);
    	}
    }
    
    /**
     * 
     * @param ctx
     * @param sub
     * @param oldSub
     * @param newExpiryDate
     * @param oldPricePlan
     * @param newPricePlan
     * @throws Exception
     */
    private void updateTopupSchedule(Context ctx, Subscriber sub, Subscriber oldSub, Date newExpiryDate, PricePlan oldPricePlan, PricePlan newPricePlan) 
    		throws Exception
    {

    	if(PricePlanSubTypeEnum.LIFETIME.equals(newPricePlan.getPricePlanSubType()))
    	{
    		updateLifetimeSubscriberATU(ctx, oldPricePlan, newPricePlan, sub);
    	}
    	else if(PricePlanSubTypeEnum.MRC.equals(newPricePlan.getPricePlanSubType()) || PricePlanSubTypeEnum.PICKNPAY.equals(newPricePlan.getPricePlanSubType()) )
    	{
    		updateMRCSubscriberATU(ctx, oldPricePlan, newPricePlan, newExpiryDate, sub);
    	}
    	else if(PricePlanSubTypeEnum.PAYGO.equals(newPricePlan.getPricePlanSubType()))
    	{
    		updatePayGoSubscriberATU(ctx, oldPricePlan, newPricePlan, newExpiryDate, sub);
    	}
    }
    
    /**
     * 
     * @param ctx
     * @param oldPricePlan
     * @param newPricePlan
     * @param nxtApplication
     * @param sub
     * @throws HomeException
     */
    private void updateMRCSubscriberATU(Context ctx, PricePlan oldPricePlan, PricePlan newPricePlan, Date nxtApplication, Subscriber sub) 
    		throws HomeException
    {
    	Collection<TopUpSchedule> collTopUpSchedule = null;
        collTopUpSchedule = SubscriberSupport.getSubscriberTopUpSchedules(ctx, sub.getId());
        for (TopUpSchedule topUpSchedule : collTopUpSchedule)
        {
        	if(!topUpSchedule.isUsePlanFees())
        	{
        		topUpSchedule.setUsePlanFees(true);
        		topUpSchedule.setAmount(TopUpSchedule.DEFAULT_AMOUNT);
        	}
        	topUpSchedule.setNextApplication(nxtApplication);
        	HomeSupportHelper.get(ctx).storeBean(ctx, topUpSchedule);
        	new InfoLogMsg(this, "Updated top up schedule : " + topUpSchedule, null).log(ctx);
        }
    }
    
    /**
     * 
     * @param ctx
     * @param oldPricePlan
     * @param newPricePlan
     * @param nxtApplication
     * @param sub
     * @throws HomeException
     */
    private void updateLifetimeSubscriberATU(Context ctx, PricePlan oldPricePlan, PricePlan newPricePlan, Subscriber sub) 
    		throws HomeException
    {
    	Collection<TopUpSchedule> collTopUpSchedule = null;
        collTopUpSchedule = SubscriberSupport.getSubscriberTopUpSchedules(ctx, sub.getId());
        if(oldPricePlan != null)
        {
        	for (TopUpSchedule topUpSchedule : collTopUpSchedule)
        	{
        		if (topUpSchedule != null)
                {
                    if(topUpSchedule.isScheduleUserDefined())
                    {
                    	topUpSchedule.setNextApplication(SubscriptionSupport.determineNextTopupScheduleDate(ctx, topUpSchedule.getNextApplication()));
                    }
                    else
                    {
                    	topUpSchedule.setNextApplication(SubscriptionSupport.determineNextTopUpDate(ctx, sub));
                    }
                }
        		if(PricePlanSubTypeEnum.MRC.equals(oldPricePlan.getPricePlanSubType()) && topUpSchedule.isUsePlanFees())
        		{
        			topUpSchedule.setUsePlanFees(false);
        			topUpSchedule.setAmount(newPricePlan.getMinimumATUAmount());
        		}
        		HomeSupportHelper.get(ctx).storeBean(ctx, topUpSchedule);
        		new InfoLogMsg(this, "Updated top up schedule : " + topUpSchedule, null).log(ctx);
        	}
        }
        else
        {
        	for (TopUpSchedule topUpSchedule : collTopUpSchedule)
        	{
        		if (topUpSchedule != null)
                {
                    if(topUpSchedule.isScheduleUserDefined())
                    {
                    	topUpSchedule.setNextApplication(SubscriptionSupport.determineNextTopupScheduleDate(ctx, topUpSchedule.getNextApplication()));
                    }
                    else
                    {
                    	topUpSchedule.setNextApplication(SubscriptionSupport.determineNextTopUpDate(ctx, sub));
                    }
                }
        		if(topUpSchedule.isUsePlanFees())
        		{
        			topUpSchedule.setUsePlanFees(false);
        			topUpSchedule.setAmount(newPricePlan.getMinimumATUAmount());
        		}
        		HomeSupportHelper.get(ctx).storeBean(ctx, topUpSchedule);
        		new InfoLogMsg(this, "Updated top up schedule : " + topUpSchedule, null).log(ctx);
        	}
        }
    }
    
    /**
     * 
     * 
     * @param ctx
     * @param oldPricePlan
     * @param newPricePlan
     * @param nxtApplication
     * @param sub
     * @throws HomeException
     */
    private void updatePayGoSubscriberATU(Context ctx, PricePlan oldPricePlan, PricePlan newPricePlan, Date nxtApplication, Subscriber sub) 
    		throws HomeException
    {
    	Collection<TopUpSchedule> collTopUpSchedule = null;
        collTopUpSchedule = SubscriberSupport.getSubscriberTopUpSchedules(ctx, sub.getId());
        if(oldPricePlan != null)
        {
        	for (TopUpSchedule topUpSchedule : collTopUpSchedule)
        	{
        		//topUpSchedule.setNextApplication(nxtApplication); // This has been addressed in com.redknee.app.crm.home.sub.CreditCardTopupScheduleUpdatingHome
        		if(PricePlanSubTypeEnum.MRC.equals(oldPricePlan.getPricePlanSubType()) && topUpSchedule.isUsePlanFees())
        		{
        			topUpSchedule.setUsePlanFees(false);
        			topUpSchedule.setAmount(newPricePlan.getMinimumATUAmount());
        			HomeSupportHelper.get(ctx).storeBean(ctx, topUpSchedule);
        			new InfoLogMsg(this, "Updated top up schedule : " + topUpSchedule, null).log(ctx);
        		}
        	}
        }
        else
        {
        	for (TopUpSchedule topUpSchedule : collTopUpSchedule)
        	{
        		//topUpSchedule.setNextApplication(nxtApplication); // This has been addressed in com.redknee.app.crm.home.sub.CreditCardTopupScheduleUpdatingHome
        		if(topUpSchedule.isUsePlanFees())
        		{
        			topUpSchedule.setUsePlanFees(false);
        			topUpSchedule.setAmount(newPricePlan.getMinimumATUAmount());
        			HomeSupportHelper.get(ctx).storeBean(ctx, topUpSchedule);
        			new InfoLogMsg(this, "Updated top up schedule : " + topUpSchedule, null).log(ctx);
        		}
        	}
        }
    }
}
