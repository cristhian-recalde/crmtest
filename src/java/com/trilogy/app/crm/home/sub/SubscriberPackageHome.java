/*
 * This code is a protected work and subject to domestic and international
 * copyright law(s).  A complete listing of authors of this work is readily
 * available.  Additionally, source code is, by its very nature, confidential
 * information and inextricably contains trade secrets and other information
 * proprietary, valuable and sensitive to Redknee.  No unauthorized use,
 * disclosure, manipulation or otherwise is permitted, and may only be used
 * in accordance with the terms of the license agreement entered into with
 * Redknee Inc. and/or its subsidiaries.
 *
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.home.sub;

import com.trilogy.framework.xhome.beans.SafetyUtil;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;

import com.trilogy.util.snippet.log.Logger;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.PackageStateEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.numbermgn.AppendNumberMgmtHistoryHome;
import com.trilogy.app.crm.numbermgn.NumberMgmtHistory;
import com.trilogy.app.crm.numbermgn.PackageMgmtHistoryHome;
import com.trilogy.app.crm.support.EnumStateSupportHelper;
import com.trilogy.app.crm.support.FrameworkSupportHelper;
import com.trilogy.app.crm.support.Lookup;
import com.trilogy.app.crm.support.PackageSupportHelper;
import com.trilogy.app.crm.support.SubscriberSupport;
import com.trilogy.app.crm.technology.TechnologyEnum;

/**
 * @author joe.chen@redknee.com
 */
public class SubscriberPackageHome extends AppendNumberMgmtHistoryHome
{
	
	/**
	 * @param ctx
	 * @param delegate
	 */
	public SubscriberPackageHome(Home delegate) 
	{
		
		super(delegate, PackageMgmtHistoryHome.class);
	}
	
	/* (non-Javadoc)
	 * @see com.redknee.framework.xhome.home.HomeSPI#create(com.redknee.framework.xhome.context.Context, java.lang.Object)
	 */
	@Override
    public Object create(Context ctx, Object obj) throws HomeException 
	{
		Subscriber newSub = (Subscriber)obj;
		
		//moved to validator
		//Claim.validatePackageTypeAndAvailable(ctx, newSub);

        final TechnologyEnum technology = newSub.getTechnology();
        if (technology == null)
        {
            final String msg = "Missing technology " + newSub.getTechnology()
                    + " while adapting Subscriber " + newSub.getId();
            Logger.major(ctx, this, msg);
            final HomeException ex = new HomeException(msg);
            FrameworkSupportHelper.get(ctx).notifyExceptionListener(ctx, ex);
            return getDelegate().create(ctx, obj);
        }
        else if (!technology.isPackageAware())
        {
            // package and IMSI logic only for package-aware subscriptions
            return getDelegate().create(ctx, obj);
        }
        else 
        {
            final Account account =(Account) ctx.get(Account.class);
            
			if (account != null && account.isPooled(ctx))
            {
                // ignoring fake subscription that is created for Pooled acounts
                return getDelegate().create(ctx, obj);
                
            }           
           
        }
		SubscriberSupport.setIMSI(ctx, newSub, newSub.getPackageId());
		syncPackageStateWithSubscriber(ctx, null, (Subscriber)obj);
		
		return super.create(ctx, obj);
	}
	
	/**
	 * @see com.redknee.framework.xhome.home.HomeSPI#store(com.redknee.framework.xhome.context.Context, java.lang.Object)
	 */
	@Override
    public Object store(Context ctx, Object obj) throws HomeException 
	{
		Subscriber newSub = (Subscriber)obj;
		Subscriber oldSub = (Subscriber) ctx.get(Lookup.OLDSUBSCRIBER);

        final TechnologyEnum technology = newSub.getTechnology();
        if (technology == null)
        {
            final String msg = "Missing technology " + newSub.getTechnology()
                    + " while adapting Subscriber " + newSub.getId();
            Logger.major(ctx, this, msg);
            final HomeException ex = new HomeException(msg);
            FrameworkSupportHelper.get(ctx).notifyExceptionListener(ctx, ex);
            return getDelegate().store(ctx, obj);
        }
        else if (!technology.isPackageAware())
        {
            // package and IMSI logic only for package-aware subscriptions
            return getDelegate().store(ctx, obj);
        }
        else 
        {
            final Account account =(Account) ctx.get(Account.class);
            
			if (account != null && account.isPooled(ctx))
            {
                // ignoring fake subscription that is created for Pooled acounts
                return getDelegate().store(ctx, obj);
                
            }           
           
        }
		//Subscriber change
		//subscriber state change
		if (!EnumStateSupportHelper.get(ctx).stateEquals(oldSub, newSub))
		{
			onSubscriberStateChange(ctx, oldSub, newSub);
		}
		
		
		//change this source msisdn
		if (!SafetyUtil.safeEquals(getResourceId(oldSub), getResourceId(newSub)))
		{
			onChangePackage(ctx, oldSub, newSub);
		}
		
		//do conversion
		if (!oldSub.getSubscriberType().equals(newSub.getSubscriberType()))
		{
			onConversion(ctx, oldSub, newSub);
		}
		
		return super.store(ctx, obj);
	}

	/**
	 * @param ctx
	 * @param oldSub
	 * @param newSub
	 * @throws HomeException
	 */
	private void onConversion(Context ctx, Subscriber oldSub, Subscriber newSub) throws HomeException 
	{
		if (isResourceOptional())
        {
            validateResourceType(ctx, newSub);
        }
        else
		{
			setResourceType(ctx, newSub);
			syncPackageStateWithSubscriber(ctx, null, newSub);// force stae update
		}
	}
	
	/**
	 * 
	 * validate any other properties except state
	 * spid, subscribertype
	 * @param ctx
	 * @param newSub
	 * @throws HomeException
	 */
	protected void validateResourceType(Context ctx, Subscriber newSub) throws HomeException 
	{
		Claim.validatePackageType(ctx, newSub, newSub.getPackageId());
	}

	/**
	 * @param ctx
	 * @param oldSub
	 * @param newSub
	 * @throws HomeInternalException
	 * @throws HomeException
	 */
	private void onChangePackage(Context ctx, Subscriber oldSub, Subscriber newSub) throws HomeException 
	{
		//implemented in validating home
		//Claim.validatePackageAvailable(ctx, newSub);
		
		Claim.validatePackageType(ctx, newSub,   newSub.getPackageId());
		//			 force stae update
		syncPackageStateWithSubscriber(ctx, null, newSub);
		SubscriberSupport.setIMSI(ctx, newSub, newSub.getPackageId());
		
		// when changing the package the old package goes into HELD state for 
		//  IMSI no of held days. this value is in the CRMSpid class.
		// a cron task will reclaim these packages.
		PackageSupportHelper.get(ctx).setPackageState(ctx, getResourceId(oldSub),oldSub.getTechnology(), PackageStateEnum.HELD_INDEX, oldSub.getSpid());

      NumberMgmtHistory history = (NumberMgmtHistory)appendHistory(
            ctx,
            oldSub.getPackageId(),
            getHistoryEventSupport(ctx).getPackageMigrationEvent(ctx),
            "migrate package to ["+newSub.getPackageId()+"]");
      history = (NumberMgmtHistory)appendHistory(
            ctx,
            newSub.getPackageId(),
            getHistoryEventSupport(ctx).getPackageMigrationEvent(ctx),
            "migrate package from ["+oldSub.getPackageId()+"]");
	}

	/**
	 * @param ctx
	 * @param oldSub
	 * @param newSub
	 * @throws HomeException
	 */
	protected void onSubscriberStateChange(Context ctx, Subscriber oldSub, Subscriber newSub) throws HomeException
	{
		if(EnumStateSupportHelper.get(ctx).isLeavingState(oldSub, newSub, SubscriberStateEnum.INACTIVE))
		{
			Claim.validatePackageNotInUse(ctx, newSub);  // Verify SIM State != IN_USE
		}
		syncPackageStateWithSubscriber(ctx, oldSub, newSub);
	}
	
	void syncPackageStateWithSubscriber(Context ctx, Subscriber oldSub, Subscriber newSub) throws HomeException
	{
		if (!EnumStateSupportHelper.get(ctx).stateEquals(oldSub, newSub))
		{
			int newPackageState = mapSubscriberState(ctx, newSub);

			if(newSub!=null)
			{
				//write it the state for safety, error protection
				PackageSupportHelper.get(ctx).setPackageState(ctx, getResourceId(newSub),newSub.getTechnology(),newPackageState, newSub.getSpid());
			}
			else if(oldSub!=null)
			{
				// this is in case we delete (newSub=null)
				PackageSupportHelper.get(ctx).setPackageState(ctx, getResourceId(oldSub), oldSub.getTechnology(),newPackageState, oldSub.getSpid());
			}
		}
	}
	
	
	protected String getResourceId(Subscriber sub)
	{
		return sub.getPackageId();
	}
	protected String getResouceRef()
	{
		return "package";
	}
	protected boolean isResourceOptional()
	{
		return false;
	}
	
	
	 /**
     * 
     * Gets targeting msisdn state for the new subscriber 
     * @param ctx
     * @return
     */
    protected int mapSubscriberState(Context ctx, Subscriber newSub)
    {
    	int packageState = PackageStateEnum.IN_USE_INDEX;    	
    	if (newSub == null)
        {
            packageState = PackageStateEnum.AVAILABLE_INDEX;
        }
        else
    	{
    		switch (newSub.getState().getIndex())
	    	{
//	    		case SubscriberStateEnum.ACTIVE_INDEX:
//	    			packageState = PackageStateEnum;
//	    		break;
	    		
    		
	    		case SubscriberStateEnum.INACTIVE_INDEX:
//	    			pack.reset();// DZ: change to reset so available for reuse 
	    			//pack.release();
	    			packageState = PackageStateEnum.HELD_INDEX;
	    		break;
	    			
//	    		case SubscriberStateEnum.AVAILABLE_INDEX:
//	    			packageState = PackageStateEnum;
//	    		break;	    				
	    	}
    	}

    	return packageState;
    }
    
    /**
	 * @param ctx
	 * @param newSub
	 * @throws HomeException
	 */
	protected void setResourceType(Context ctx, Subscriber newSub) throws HomeException 
	{
		//package does not associate with subscriber type
		//MsisdnSupport.setMsisdnType(ctx, getSourcePackage(newSub), newSub);
	}

}
