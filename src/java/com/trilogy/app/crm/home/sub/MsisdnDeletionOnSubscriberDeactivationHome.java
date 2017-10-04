package com.trilogy.app.crm.home.sub;

import com.trilogy.app.crm.api.queryexecutor.subscription.SubscriptionQueryExecutors;
import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.PortingTypeEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.bean.core.Msisdn;
import com.trilogy.app.crm.support.EnumStateSupportHelper;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.app.crm.support.Lookup;
import com.trilogy.app.crm.support.SpidSupport;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xlog.log.LogSupport;

public class MsisdnDeletionOnSubscriberDeactivationHome extends HomeProxy implements Home{

	private static final long serialVersionUID = 1L;

	public MsisdnDeletionOnSubscriberDeactivationHome(final Home delegate)
    {
        super(delegate);
    }

	@Override
	public Object store(Context ctx, Object obj) throws HomeException,
			HomeInternalException {
		
		Subscriber sub = (Subscriber) obj;
        Subscriber oldSub = (Subscriber) ctx.get(Lookup.OLDSUBSCRIBER);
        boolean deleteMsisdn = false;
        CRMSpid spid = SpidSupport.getCRMSpid(ctx, oldSub.getSpid());
    	Msisdn oldMsisdn = null;
    	if(LogSupport.isDebugEnabled(ctx))
		{
    	LogSupport.debug(ctx, this, "Enter to delete MSISDN on Subscriber Deactivation for SubId : "+ oldSub.getId() + " and MSISDN : " + oldSub.getMsisdn());
		}
        // This check is only to delete the External Msisdn
		if(EnumStateSupportHelper.get(ctx).isEnteringState(oldSub, sub, SubscriberStateEnum.INACTIVE) 
				&& spid != null
				&& spid.getDeleteExternalMsisdnOnSubDeactivation())
        {
				oldMsisdn = HomeSupportHelper.get(ctx).findBean(ctx, Msisdn.class, oldSub.getMsisdn());
		        
				if(oldMsisdn != null && oldMsisdn.isExternal() && ((Boolean)ctx.get(SubscriptionQueryExecutors.SubscriptionUpdateWithStateTransitionQueryExecutor.PORTOUT_AND_INACTIVE_STATE,false)))
				{
					deleteMsisdn = true;
					if(LogSupport.isDebugEnabled(ctx))
					{
			    	LogSupport.debug(ctx, this, "Confirm to delete MSISDN on Subscriber Deactivation for SubId : "+ oldSub.getId() + " and MSISDN : " + oldSub.getMsisdn());
					}
				}
        }
		if(EnumStateSupportHelper.get(ctx).isEnteringState(oldSub, sub, SubscriberStateEnum.INACTIVE) 
				&& spid != null
				&& spid.getDeleteMsisdnOnSubDeactivation())
        {

			oldMsisdn = HomeSupportHelper.get(ctx).findBean(ctx, Msisdn.class, oldSub.getMsisdn());
			if(oldMsisdn != null && !oldMsisdn.isExternal())
			{
				deleteMsisdn = true;
			}
        }
		
		
		final Object returnSub =  super.store(ctx, obj);
		
		
		if(deleteMsisdn == true)
		{
			try
			{
				Msisdn newMsisdn = HomeSupportHelper.get(ctx).findBean(ctx, Msisdn.class, oldSub.getMsisdn());
				HomeSupportHelper.get(ctx).removeBean(ctx, newMsisdn);
		    	LogSupport.debug(ctx, this, " Confirmed delete MSISDN on Subscriber Deactivation for SubId : "+ oldSub.getId() + " and MSISDN : " + oldSub.getMsisdn());

			}
			catch(HomeException he)
			{
				LogSupport.major(ctx, this, "Unable to delete MSISDN on Subscriber Deactivation for SubId : "+ oldSub.getId() + " and MSISDN : " + oldSub.getMsisdn(),he);
			}
		}
		else
		{
			if(LogSupport.isDebugEnabled(ctx))
			{
			LogSupport.debug(ctx, this, "Not deleting the MSISDN as deleteMsisdn = false: "+ oldSub.getId() + " and MSISDN : " + oldSub.getMsisdn());
			}

		}
		
		return returnSub;
	}
}
