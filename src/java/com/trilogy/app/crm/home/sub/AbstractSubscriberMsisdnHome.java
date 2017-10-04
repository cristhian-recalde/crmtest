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
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MinorLogMsg;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.MsisdnStateEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.numbermgn.AppendNumberMgmtHistoryHome;
import com.trilogy.app.crm.numbermgn.MsisdnAlreadyAcquiredException;
import com.trilogy.app.crm.numbermgn.MsisdnManagement;
import com.trilogy.app.crm.numbermgn.MsisdnMgmtHistoryHome;
import com.trilogy.app.crm.support.EnumStateSupportHelper;
import com.trilogy.app.crm.support.Lookup;
import com.trilogy.app.crm.support.MsisdnSupport;
import com.trilogy.app.crm.technology.Technology;

/**
 * @author jchen
 *
 * Msisdn table in response to subscriber events
 */
public abstract class AbstractSubscriberMsisdnHome 
   extends AppendNumberMgmtHistoryHome
{
	
	/**
	 * @param ctx
	 * @param delegate
	 */
	public AbstractSubscriberMsisdnHome(Home delegate) 
	{
		
		super(delegate, MsisdnMgmtHistoryHome.class);
	}
	
	/* (non-Javadoc)
	 * @see com.redknee.framework.xhome.home.HomeSPI#create(com.redknee.framework.xhome.context.Context, java.lang.Object)
	 */
	@Override
    public Object create(Context ctx, Object obj) throws HomeException,
			HomeInternalException 
	{
        // resource availablility check already handled by AbstractMsisdnHomeValidator
        Subscriber sub = (Subscriber) obj;
        if (isSet(getResourceId(sub)))
        {
            safeAttemptToClaimMsisdn(ctx, sub);
            MsisdnManagement.associateMsisdnWithSubscription(ctx, getResourceId(sub), sub, getResourceRef());
        }
        else if (!isSet(getResourceId(sub)) && !isResourceOptional())
        {
            throw new HomeException("Required mobile number is unset: " + getResourceRef());
        }
        return super.create(ctx, obj);
    }

	/* (non-Javadoc)
	 * @see com.redknee.framework.xhome.home.HomeSPI#remove(com.redknee.framework.xhome.context.Context, java.lang.Object)
	 */
	@Override
    public void remove(Context ctx, Object obj) throws HomeException,
			HomeInternalException 
	{
		Subscriber sub = (Subscriber)obj;
		
		if (isSet(getResourceId(sub)))
		{
		    MsisdnManagement.deassociateMsisdnWithSubscription(ctx, getResourceId(sub), sub.getId(), getResourceRef());

		    // TODO technically if they are actually REMOVING the subscriber we should remove all the child entities assoicated with it (eg. by SubscriberId)
		    // which would include all the MSISDN history.  Stale entries can cause problems.
		}

		super.remove(ctx, obj);
	}
	
	/* (non-Javadoc)
	 * @see com.redknee.framework.xhome.home.HomeSPI#store(com.redknee.framework.xhome.context.Context, java.lang.Object)
	 */
	@Override
    public Object store(Context ctx, Object obj) throws HomeException,
			HomeInternalException 
	{
		Subscriber newSub = (Subscriber)obj;
		Subscriber oldSub = (Subscriber) ctx.get(Lookup.OLDSUBSCRIBER);
		
		if (!isSet(getResourceId(newSub)) && !isResourceOptional())
        {
		    // We have a mandatory msisdn that is unset.
            throw new HomeException("Required mobile number is unset: " + getResourceRef());
        }
		else if (!isSet(getResourceId(oldSub)) && !isSet(getResourceId(newSub)))
		{
		    // Pass-through: if the resource was not and is not set, then there are 
	        // no changes to be made, so simply delegate.
		    return super.store(ctx, obj);
		}
		
		boolean changingMsisdn = false;
		//Subscriber change 

		boolean changeState = false;
        if (!EnumStateSupportHelper.get(ctx).stateEquals(oldSub, newSub))
        {
            changeState = true;
        }
		
		//change this source msisdn
		if (!SafetyUtil.safeEquals(getResourceId(oldSub), getResourceId(newSub)))
		{
			changingMsisdn = true;
			onChangeMsisdn(ctx, oldSub, newSub);
		}
		
		//do conversion
		if (!oldSub.getSubscriberType().equals(newSub.getSubscriberType()))
		{
			onConversion(ctx, oldSub, newSub);
		}
		
		boolean Success = false;
		Object newObj = null;
		try
		{
			newObj = super.store(ctx, obj);
			Success = true;
			 
            // subscriber state change
			// deassociate the msisdn at the end of pipeline because msisdn needs to be used for charging and has other usage for it
			if (changeState)
			{
                onSubscriberStateChange(ctx, oldSub, newSub);
            }
            if (changingMsisdn && isSet(getResourceId(oldSub)))
            {
                MsisdnManagement.deassociateMsisdnWithSubscription(ctx, getResourceId(oldSub), oldSub.getId(),
                        getResourceRef());
            }
		}
		finally
		{

			if (changingMsisdn)
            {
                logChangeMsisdn(ctx, Success);
            }
		}
		return newObj;
	}

	/**
	 * @param ctx
	 * @param oldSub
	 * @param newSub
	 * @throws HomeException
	 */
	private void onConversion(Context ctx, Subscriber oldSub, Subscriber newSub) throws HomeException 
	{
		validateResourceType(ctx, newSub);
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
		Claim.validateMsisdnType(ctx, newSub, getResourceId(newSub), getResourceRef(), isResourceOptional());
	}

	/**
	 * @param ctx
	 * @param newSub
	 * @throws HomeException
	 */
	protected void setResourceType(Context ctx, Subscriber newSub) throws HomeException {
		MsisdnSupport.setMsisdnType(ctx, getResourceId(newSub), newSub);
	}

	/**
	 * @param ctx
	 * @param oldSub
	 * @param newSub
	 * @throws HomeInternalException
	 * @throws HomeException
	 */
	private void onChangeMsisdn(Context ctx, Subscriber oldSub, Subscriber newSub) throws HomeInternalException, HomeException 
	{
        if (!isSet(getResourceId(newSub)))
        {
            return;
        }
        if (LogSupport.isDebugEnabled(ctx))
        {
            new DebugLogMsg(this, "Attempting to change " + getResourceRef() + " number of subcriber \""
                    + oldSub.getId() + "\" from \"" + getResourceId(oldSub) + "\" to \"" + getResourceId(newSub)
                    + "\".", null).log(ctx);
        }
        // resource availablility check already handled by SubscriberMsisdnValidator
        // Try to claim first -- 
        safeAttemptToClaimMsisdn(ctx, newSub);
        // force update association. If msisdn could not claimed by above attemot.....it
        // may have been already claimed earlier
        MsisdnManagement.associateMsisdnWithSubscription(ctx, getResourceId(newSub), newSub, getResourceRef());
        if (isSet(getResourceId(oldSub)))
        {
            MsisdnManagement.deassociateMsisdnWithSubscription(ctx, getResourceId(oldSub), newSub, getResourceRef());    
    }
        else
        {
            new DebugLogMsg(this, "Old MSISDN is not set, skipping attempt to deassociate the MSISDN for subscriber " 
                    + oldSub.getId(),null).log(ctx);
        }
        
    }

	/**
     * @param ctx
     * @param oldSub
     * @param newSub
     * @throws HomeException
     */
    protected void onSubscriberStateChange(Context ctx, Subscriber oldSub, Subscriber newSub) throws HomeException
    {
        if (EnumStateSupportHelper.get(ctx).isEnteringState(oldSub, newSub, SubscriberStateEnum.INACTIVE))
        {
            // Deactivated subscribers always get their MSISDN deassociated
            MsisdnManagement.deassociateMsisdnWithSubscription(ctx, getResourceId(newSub), newSub.getId(),
                    getResourceRef());
        }
    }


    /**
     * Determines if the given value is set (i.e., non-null and contains more
     * than blanks).
     *
     * @param value The value to check as being set.
     * @return True if the value is set; false otherwise.
     */
    private boolean isSet(final String value)
    {
        return value != null && value.trim().length() > 0;
    }

	
	protected abstract String getResourceId(Subscriber sub);
	protected abstract String getResourceRef();
	protected abstract boolean isResourceOptional();
	protected abstract boolean isResourceExternal(Subscriber sub);

	/**
	     * 
	     * Gets targeting msisdn state for the new subscriber 
	     * @param ctx
	     * @return
	     */
	    protected int mapSubscriberState(Context ctx, Subscriber newSub)
	    {
	    	int msisdnState = MsisdnStateEnum.IN_USE_INDEX;    	
	    	if (newSub == null)
            {
                msisdnState = MsisdnStateEnum.AVAILABLE_INDEX;
            }
            else
	    	{
	    		switch (newSub.getState().getIndex())
		    	{
	//	    		case SubscriberStateEnum.ACTIVE_INDEX:
	//	    			msisdnState = MsisdnStateEnum.IN_USE_INDEX;
	//	    		break;
		    		
		    		case SubscriberStateEnum.INACTIVE_INDEX:
		    			msisdnState = MsisdnStateEnum.HELD_INDEX;
		    		break;
		    			
	//	    		case SubscriberStateEnum.AVAILABLE_INDEX:
	//	    			msisdnState = MsisdnStateEnum.IN_USE_INDEX;
	//	    		break;	    				
		    	}
	    	}
	
	    	return msisdnState;
	    }

	void logChangeMsisdn(Context ctx, boolean success)
	{
		if (LogSupport.isDebugEnabled(ctx))
        {
            final String message;
            if (success)
            {
                message = "Successfully changed "+getResourceRef();
            }
            else
            {
                message = "Failed to change "+getResourceRef();
            }

            new DebugLogMsg(this, message, null).log(ctx);
        }
	}
	
	
	private void safeAttemptToClaimMsisdn(Context ctx, Subscriber sub)
	{
        try
        {
            Account account = (Account) ctx.get(Account.class);
            if (account == null || !account.getBAN().equals(sub.getBAN()))
            {
                account = sub.getAccount(ctx);
            }
            Technology.setBeanTechnology(ctx, sub.getTechnology());
            MsisdnManagement
                    .claimMsisdn(ctx, getResourceId(sub), account, isResourceExternal(sub), getUserId(ctx));
        }
        catch (MsisdnAlreadyAcquiredException e)
        {
            new DebugLogMsg(this, "Ignore this exception bacuse MSISDN [" + getResourceId(sub) + "] of refernce-type ["
                    + getResourceRef() + "]" + " is already acquired for the Account-BAN[" + sub.getBAN()
                    + "]. The attempt to claim msisdn was redundant", e).log(ctx);
        }
        catch (HomeException e)
        {
            String errorMessage = "An error occured while attpemting to verify/claim MSISDN [" + getResourceId(sub)
                    + "] " + "of refernce-type [" + getResourceRef() + "] for Account BAN[" + sub.getBAN() + "]."
                    + " Error[" + e.getMessage()
                    + "] . However the operation may have been successful ans this error is not necessarily gating.";
            new MinorLogMsg(this, errorMessage, null).log(ctx);
            new DebugLogMsg(this, errorMessage, e).log(ctx);
            // eat this exception and try to associate msisdn. If the msisdn has
            // already been claimed
        }
    }
}
