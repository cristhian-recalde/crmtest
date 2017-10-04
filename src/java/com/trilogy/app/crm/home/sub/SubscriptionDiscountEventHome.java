package com.trilogy.app.crm.home.sub;

import com.trilogy.app.crm.bean.DiscountActivityTypeEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
//import com.trilogy.app.crm.bean.pipeline.PipelineAgentExtension;
import com.trilogy.app.crm.discount.DiscountActivityUtils;
import com.trilogy.app.crm.support.DiscountSupportImpl;
import com.trilogy.app.crm.support.EnumStateSupportHelper;
import com.trilogy.app.crm.support.Lookup;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xlog.log.LogSupport;

/**
 * @since 10.1.5
 * @author shailesh.makhijani
 *
 */
public class SubscriptionDiscountEventHome extends HomeProxy {

	private static final long serialVersionUID = 1L;
	private static final String MODULE = SubscriptionDiscountEventHome.class.getName();

	
	public SubscriptionDiscountEventHome(Home delegate)
    {
        super(delegate);
    }
	
	public SubscriptionDiscountEventHome(Context ctx, Home delegate)
    {
        super(ctx, delegate);
    }
	
	
	/*public SubscriptionDiscountEventHome(Context ctx, Home delegate, PipelineAgentExtension<Home> extension)
    {
        this(ctx, delegate);
    }*/
	
	
	 /**
     * @param ctx
     * @param obj
     * @return Object
     * @exception HomeException
     * @exception HomeInternalException
     */
	@Override
    public Object create(Context ctx, Object obj) throws HomeException
    {
         obj = super.create(ctx, obj);

         Subscriber sub = (Subscriber) obj;
         
         DiscountActivityUtils.createTrigger(
        		 DiscountActivityTypeEnum.SUBSCRIBER_CREATION_EVENT, ctx, sub.getSpid(),sub.getBAN());
		 return obj;
    }
    
    /**
     * @param ctx
     * @param obj
     * @return Object
     * @exception HomeException
     * @exception HomeInternalException
     */
	@Override
    public Object store(Context ctx, Object obj) throws HomeException, HomeInternalException
    {
         obj = super.store(ctx, obj);
         
         Subscriber sub = (Subscriber) obj;
         
         final Subscriber oldSubscriber = (Subscriber) ctx.get(Lookup.OLDSUBSCRIBER);
         
         /**
          * Subscriber dunning level change
          */
         if (oldSubscriber.getLastDunningLevel() != sub.getLastDunningLevel()) 
         {
        	 
        	 DiscountActivityUtils.createTrigger(
            		 DiscountActivityTypeEnum.SUBSCRIBER_DUNNING_LEVEL_CHANGE_EVENT, ctx, sub.getSpid(),sub.getBAN());
         }
         /**
          * Subscriber moving from pending to Active state
          */
         else if (EnumStateSupportHelper.get(ctx).isTransition(oldSubscriber, sub, SubscriberStateEnum.PENDING, SubscriberStateEnum.ACTIVE))
         {
        	
        	 DiscountActivityUtils.createTrigger(
            		 DiscountActivityTypeEnum.SUBSCRIBER_STATE_CHANGE_EVENT, ctx, sub.getSpid(),sub.getBAN()); 
         }
         else if(oldSubscriber.getState().equals(SubscriberStateEnum.SUSPENDED) && sub.getState().equals(SubscriberStateEnum.ACTIVE))
         {
        	 DiscountActivityUtils.createTrigger(
            		 DiscountActivityTypeEnum.SUBSCRIBER_RESUME_EVENT, ctx, sub.getSpid(),sub.getBAN()); 
         }
         else if(oldSubscriber.getState().equals(SubscriberStateEnum.ACTIVE) && sub.getState().equals(SubscriberStateEnum.SUSPENDED))
         {
        	 DiscountActivityUtils.createTrigger(
            		 DiscountActivityTypeEnum.SUBSCRIBER_SUSPENSION_EVENT, ctx, sub.getSpid(),sub.getBAN()); 
         }
         else if(!oldSubscriber.getState().equals(sub.getState()) && !sub.getState().equals(SubscriberStateEnum.INACTIVE))
         {
        	 DiscountActivityUtils.createTrigger(
            		 DiscountActivityTypeEnum.SUBSCRIBER_STATE_CHANGE_EVENT, ctx, sub.getSpid(),sub.getBAN()); 
         }
         /**
          * Subscription de-activation
          */
         else if (sub.getState().equals(SubscriberStateEnum.INACTIVE)) 
         {
        	
        	 DiscountActivityUtils.createTrigger(
            		 DiscountActivityTypeEnum.SUBSCRIBER_DEACTIVATION_EVENT, ctx, sub.getSpid(),sub.getBAN());
         } 
        
         /**
          * PP Change
          */
         else if (sub.getPricePlan() != oldSubscriber.getPricePlan())
         {
        	 
        	 DiscountActivityUtils.createTrigger(
            		 DiscountActivityTypeEnum.PRICE_PLAN_CHANGE_EVENT, ctx, sub.getSpid(),sub.getBAN());
         }
         /**
          * PP Version Change
          */
         else if (sub.getPricePlanVersion() != oldSubscriber.getPricePlanVersion())
         {
        	 
        	 DiscountActivityUtils.createTrigger(
            		 DiscountActivityTypeEnum.PRICE_PLAN_VERSION_CHANGE_EVENT, ctx, sub.getSpid(),sub.getBAN());
         }
         
		 return obj;
    }
    
}
