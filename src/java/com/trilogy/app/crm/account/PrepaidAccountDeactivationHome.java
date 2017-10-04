package com.trilogy.app.crm.account;

import java.util.Collection;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountHome;
import com.trilogy.app.crm.bean.AccountStateEnum;
import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.support.SpidSupport;
import com.trilogy.util.snippet.log.Logger;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xlog.log.LogSupport;

/**
 * 
 * <p>
 * Deactivates the Account under which this subscription lies, if:
 * 
 * <ul>
 * 	<li>Account is Prepaid</li>
 * 	<li>This Subscription is deactivated</li>
 * 	<li>All Subscription(s) under this Account are in Inactive state</li>
 * 	<li>This is an individual account</li>
 * </ul>
 * 
 * </p>
 * 
 * @author <a href='mailto:ameya.bhurke@redknee.com'>Ameya Bhurke</a>
 *
 */
@SuppressWarnings("serial")
public class PrepaidAccountDeactivationHome extends HomeProxy 
{

	public PrepaidAccountDeactivationHome() 
	{
		// TODO Auto-generated constructor stub
	}

	public PrepaidAccountDeactivationHome(Context ctx) 
	{
		super(ctx);
		// TODO Auto-generated constructor stub
	}

	public PrepaidAccountDeactivationHome(Home delegate) 
	{
		super(delegate);
		// TODO Auto-generated constructor stub
	}

	public PrepaidAccountDeactivationHome(Context ctx, Home delegate) 
	{
		super(ctx, delegate);
		// TODO Auto-generated constructor stub
	}

	@Override
	public Object store(Context ctx, Object obj) throws HomeException,
			HomeInternalException 
	{

		LogSupport.debug(ctx, this, "SubscriberPipeline[store].....");
		boolean deactivateAccount = Boolean.TRUE;
		
		Object returnObj = super.store(ctx, obj);
		
		if( !(obj instanceof Subscriber) )
		{
			return returnObj;
		}
		
		Subscriber subscriber = (Subscriber) returnObj;
		
		CRMSpid spid = SpidSupport.getCRMSpid(ctx, subscriber.getSpid());
		
		if(!spid.getDeactivateAccountOnSubscriberDeactivation())
		{
			deactivateAccount = Boolean.FALSE;
			if(Logger.isDebugEnabled() && subscriber.isPrepaid())
			{
				LogSupport.debug(ctx, this, "SPID level configuration for 'Deactivate Prepaid Individual Account on Subscriber Deactivation' is disabled. " +
						"Corresponding account for PREPAID subscriber "+subscriber.getBAN()+ " will not be deactivated.");
			}
		}
		else
		{
			if(subscriber.isPrepaid() && SubscriberStateEnum.INACTIVE.equals(subscriber.getState()) )
			{
				if(Logger.isDebugEnabled())
				{
					LogSupport.debug(ctx, this, "SPID level configuration for 'Deactivate Prepaid Individual Account on Subscriber Deactivation' is enabled.");
				}
				
				Account account = subscriber.getAccount(ctx);
				if(account.isIndividual(ctx) && account.isPrepaid())
				{
					Collection<Subscriber> subscriberCollection = account.getSubscribers(ctx);
					
					for(Subscriber subscription : subscriberCollection)
					{
						if( !SubscriberStateEnum.INACTIVE.equals(subscription.getState() ) )
						{
							deactivateAccount = Boolean.FALSE;
							break;
						}
					}
				}
				else
				{
					return returnObj;
				}
				
				if(deactivateAccount)
				{
					LogSupport.info(ctx, this, "Deactivating Account : " + account.getBAN() + " as all it's subscriptions are Inactive.");
					account.setState(AccountStateEnum.INACTIVE);
					Home accountHome = (Home)ctx.get(AccountHome.class);
					accountHome.store(account);
				}
			}
		}
		return returnObj;
	}

	
	
}
