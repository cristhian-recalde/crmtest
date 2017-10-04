/*
 * This code is a protected work and subject to domestic and international copyright
 * law(s). A complete listing of authors of this work is readily available. Additionally,
 * source code is, by its very nature, confidential information and inextricably contains
 * trade secrets and other information proprietary, valuable and sensitive to Redknee, no
 * unauthorised use, disclosure, manipulation or otherwise is permitted, and may only be
 * used in accordance with the terms of the licence agreement entered into with Redknee
 * Inc. and/or its subsidiaries.
 * 
 * Copyright � Redknee Inc. and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.provision;

import java.util.concurrent.locks.ReentrantLock;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.CallingGroupTypeEnum;
import com.trilogy.app.crm.bean.ClosedUserGroup;
import com.trilogy.app.crm.bean.PrivateCug;
import com.trilogy.app.crm.bean.PrivateCugHome;
import com.trilogy.app.crm.bean.Service;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.home.ClosedUserGroupValidator;
import com.trilogy.app.crm.support.ClosedUserGroupSupport;
import com.trilogy.app.crm.support.FriendsAndFamilyExtensionSupport;
import com.trilogy.app.crm.support.SubscriberSupport;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.LogSupport;



/**

 * 
 */
public class CallingGroupUnprovisionAgent extends CommonUnprovisionAgent
{

    static String MODULE = CallingGroupUnprovisionAgent.class.getName();


    public void execute(Context ctx) throws AgentException
    {
        Account account = getAccount(ctx);
        Service service = getService(ctx);
        Subscriber subscriber = getSubscriber(ctx);
        switch ((int) service.getCallingGroupType())
        {
        case CallingGroupTypeEnum.CUG_INDEX:
            try
            {
                if (LogSupport.isDebugEnabled(ctx))
                {
                    LogSupport.debug(ctx, this, "Un-Provisioniong Calling Group Cug Service : " + service.getID()
                            + " from SubscriberID : " + subscriber.getId());
                }
                unProvisionCUG(ctx, account, subscriber.getMsisdn());
            }
            catch (HomeException e)
            {
                throw new AgentException("Exception while removing subscriber from to CUG, subscriber : "
                        + subscriber.getId(), e);
            }
            break;
        }
    }


    private static boolean unProvisionCUG(Context ctx, Account account, String msisdn) throws HomeException
    {
        Account rootAccount = account.getRootAccount(ctx);
        if (LogSupport.isDebugEnabled(ctx))
        {
            LogSupport.debug(ctx, MODULE,
                    "Root account of account : " + account.getBAN() + " is : " + rootAccount.getBAN());
        }
        ClosedUserGroup accountCug = ClosedUserGroupSupport.getCug(ctx, rootAccount.getBAN());
        return unProvisionCUG(ctx, msisdn, rootAccount, accountCug);
    }


    public static boolean unProvisionCUG(Context ctx, String msisdn, Account rootAccount, ClosedUserGroup accountCug)
            throws HomeException
    {
        if (accountCug != null)
        {
            ctx.put(ClosedUserGroupValidator.SKIP_CUG_ACCOUNT_VALIDATION,
                    ClosedUserGroupValidator.SKIP_CUG_ACCOUNT_VALIDATION);
            if (LogSupport.isDebugEnabled(ctx))
            {
                LogSupport.debug(ctx, MODULE, "Subscriber is associated with cug : " + accountCug.getID()
                        + ". Removing subscriber : " + msisdn);
            }
            if (accountCug.getSubscribers().get(msisdn) != null)
            {
                String owner = accountCug.getOwnerMSISDN();
                boolean removeCUG = false;
                boolean ownerChange = false;
                String newOwner = null;
                if (owner.equals(msisdn))
                {
                    if (LogSupport.isDebugEnabled(ctx))
                    {
                        LogSupport.debug(ctx, MODULE,"Msisdn : " + msisdn + " is owner of the cug : " + accountCug.getID());
                    }
    	            if( accountCug.getAuxiliaryService(ctx).getAggPPServiceChargesToCUGOwner())
                    {
                    	removeCUG = true;
                    	 if (LogSupport.isDebugEnabled(ctx))
                         {
                             LogSupport.debug(
                                     ctx,
                                     MODULE,
                                     "Flag-AggregatePriceplanServiceCharges to CUG owner is enabled for owner: " + accountCug.getID()
                                             + ". Deactivating owner and deleting FNF extension of account : "
                                             + rootAccount.getBAN());
                         }
                     }
    	            else
    	            {
    	                newOwner = getNewOwner(ctx, accountCug, owner);
    	                ownerChange = true;
    	                if (LogSupport.isDebugEnabled(ctx))
    	                {
    	                    LogSupport.debug(ctx, MODULE, ". Looking up for new owner.");
    	                }
    	            }
    	            
    	            Context appCtx = (Context) ctx.get("app");
    	            if (appCtx.getBoolean("groupPricePlanChange"))
                    {
    	                newOwner = null;
                    }
                    if (newOwner == null)
                    {
                        if (LogSupport.isDebugEnabled(ctx))
                        {
                            LogSupport.debug(
                                    ctx,
                                    MODULE,
                                    "No other msisdn present in cug : " + accountCug.getID()
                                            + ". Removing owner and deleting FNF extension of account : "
                                            + rootAccount.getBAN());
                        }
                        removeCUG = true;
                    }
    	            
                }
                try
                {
                    if (removeCUG)
                    {
                        // remove fnf extension here
                        ReentrantLock lock = FriendsAndFamilyExtensionSupport.getLockOnBan(rootAccount.getBAN());
                        lock.lock();
                        try
                        {
                            FriendsAndFamilyExtensionSupport.removeFnfExtensionFromAccount(ctx, rootAccount,
                                    accountCug.getID());
                        }
                        finally
                        {
                            // thrown exception is caught by the caller
                            lock.unlock();
                            FriendsAndFamilyExtensionSupport.clearLockOnBan(rootAccount.getBAN(), lock);
                        }
                    }
                    else
                    {
                        // remove subscriber
                        ClosedUserGroupSupport.removeSubscriberFromCug(ctx, accountCug.getID(), msisdn);
                        StringBuilder msg = new StringBuilder();
                        msg.append("{");
                        msg.append(msisdn);
                        msg.append("} removed from account CUG {");
                        msg.append(accountCug.getID());
                        msg.append("}");
                        ClosedUserGroupSupport.addAccountCugNote(ctx, rootAccount, msg.toString());
                    }
                }
                catch (HomeException e)
                {
                    if (ownerChange)
                    {
                        // revert owner in case of failure
                        updateOwner(ctx, accountCug.getID(), msisdn);
                    }
                    throw e;
                }
                return true;
            }
            else
            {
                LogSupport.info(ctx, MODULE, "Subscription with msisdn " + msisdn + " does not belong to CUG : "
                        + accountCug.getID() + ". Subscriber not being removed from CUG.");
            }
        }
        else
        {
            LogSupport.minor(ctx, MODULE, "No Fnf extension found for Root Account : " + rootAccount.getBAN()
                    + ". Subscriber not being removed from CUG. Subscriber : " + msisdn);
        }
        return false;
    }


    private static String getNewOwner(Context ctx, ClosedUserGroup cug, String owner) throws HomeException
    {
    	String newOwner = null;
    	
    	for (Object temp : cug.getSubscribers().keySet())
    	{
    		String number = (String) temp;

    		/** TT#14011019024
    		 *  CUG Ownership can be transferred only to postpaid subscription.
    		 */
    		Subscriber sub = null;
			try {
				sub = SubscriberSupport.lookupSubscriberForMSISDN(ctx, number);
			} catch (HomeException e) {
				LogSupport.info(ctx, CallingGroupProvisionAgent.class, e.getMessage(), e);
			}
    		if (sub!=null && sub.isPostpaid() && !number.equals(owner))
    		{
    			newOwner = number;
    			break;
    		}
    	}
    	if (newOwner != null)
    	{
    		updateOwner(ctx, cug.getID(), newOwner);
    	}
    	return newOwner;
    }


    private static void updateOwner(Context ctx, long cugId, String newOwner) throws HomeException
    {
        PrivateCug pcug = new PrivateCug();
        pcug.setID(cugId);
        pcug.setOwnerMSISDN(newOwner);
        Home pHome = (Home) ctx.get(PrivateCugHome.class);
        if (LogSupport.isDebugEnabled(ctx))
        {
            LogSupport.debug(ctx, MODULE, "Changing owner of cug : " + cugId + " to : " + newOwner);
        }
        pHome.store(pcug);
        LogSupport.info(ctx, MODULE, "Changed owner of cug : " + cugId + " to : " + newOwner);
    }
}
