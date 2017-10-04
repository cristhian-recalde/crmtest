package com.trilogy.app.crm.home.sub;

import com.trilogy.app.crm.bean.DiscountActivityTypeEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberAuxiliaryService;
import com.trilogy.app.crm.discount.DiscountActivityUtils;
import com.trilogy.app.crm.support.SubscriberSupport;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.home.HomeProxy;

public class SubscriptionAuxiliaryServiceDiscountEventHome extends HomeProxy {
	
	private static final long serialVersionUID = 1L;

	public SubscriptionAuxiliaryServiceDiscountEventHome(Home delegate)
    {
        super(delegate);
    }
	
	public SubscriptionAuxiliaryServiceDiscountEventHome(Context ctx, Home delegate)
    {
        super(ctx, delegate);
    }
	
	

	
	@Override
	public Object create(Context ctx, Object obj) throws HomeException,
			HomeInternalException {
        if (obj instanceof SubscriberAuxiliaryService)
        {
            SubscriberAuxiliaryService association = (SubscriberAuxiliaryService) obj;
            Subscriber subscriber = SubscriberSupport.getSubscriberForAuxiliaryService(ctx, association);
            if(null != association && null != subscriber){
            	
            	 DiscountActivityUtils.createTrigger(
                		 DiscountActivityTypeEnum.AUX_SERVICE_PROVISIONING_EVENT, ctx, subscriber.getSpid(),subscriber.getBAN());
            }
        }
        
        return super.create(ctx, obj);
	}

	@Override
	public void remove(Context ctx, Object obj) throws HomeException,
			HomeInternalException {
		
		if (obj instanceof SubscriberAuxiliaryService)
		{
			SubscriberAuxiliaryService association = (SubscriberAuxiliaryService) obj;
			Subscriber subscriber = SubscriberSupport.getSubscriberForAuxiliaryService(ctx, association);
			if(null != association && null != subscriber){
				//DiscountSupportImpl.cancelDiscountForAuxiliaryService(ctx, subscriber, association);
				DiscountActivityUtils.createTrigger(
               		 DiscountActivityTypeEnum.AUX_SERVICE_DEPROVISIONING_EVENT, ctx, subscriber.getSpid(),subscriber.getBAN());
			}
		}

		super.remove(ctx, obj);
	}

	@Override
	public Object store(Context ctx, Object obj) throws HomeException,
			HomeInternalException {
		return super.store(ctx, obj);
	}

}
