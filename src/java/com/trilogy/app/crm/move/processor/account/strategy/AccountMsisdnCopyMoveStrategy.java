/*
 * This code is a protected work and subject to domestic and international
 * copyright law(s). A complete listing of authors of this work is readily 
 * available. Additionally, source code is, by its very nature, confidential 
 * information and inextricably contains trade secrets and other information 
 * proprietary, valuable and sensitive to Redknee, no unauthorised use, 
 * disclosure, manipulation or otherwise is permitted, and may only be used 
 * in accordance with the terms of the licence agreement entered into with 
 * Redknee Inc. and/or its subsidiaries.
 *
 * Copyright � Redknee Inc. and its subsidiaries. All Rights Reserved. 
 */
package com.trilogy.app.crm.move.processor.account.strategy;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.InfoLogMsg;
import com.trilogy.framework.xlog.log.MajorLogMsg;
import com.trilogy.framework.xlog.log.MinorLogMsg;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.core.Msisdn;
import com.trilogy.app.crm.bean.MsisdnHome;
import com.trilogy.app.crm.bean.MsisdnStateEnum;
import com.trilogy.app.crm.bean.PortingTypeEnum;
import com.trilogy.app.crm.home.MsisdnPortHandlingHome;
import com.trilogy.app.crm.move.MoveException;
import com.trilogy.app.crm.move.processor.strategy.CopyMoveStrategy;
import com.trilogy.app.crm.move.processor.strategy.CopyMoveStrategyProxy;
import com.trilogy.app.crm.move.request.AccountMoveRequest;
import com.trilogy.app.crm.move.support.AccountMoveValidationSupport;
import com.trilogy.app.crm.numbermgn.MsisdnAlreadyAcquiredException;
import com.trilogy.app.crm.numbermgn.MsisdnManagement;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.app.crm.support.MsisdnSupport;
import com.trilogy.app.crm.support.SubscriberSupport;


/**
 * Handles MSISDN Management related processing required to move accounts and their dependent subscriptions.
 * 
 * This copy strategy must break the contract to achieve its task.  It must deassociate all MSISDNs from all
 * subscriptions prior to releasing the MSISDNs from the old account.  All of this must in turn be done before
 * acquiring the MSISDN in the new account, so it all happens in the createNewEntity method.
 *
 * @author Aaron Gourley
 * @since 8.1
 */
public class AccountMsisdnCopyMoveStrategy<AMR extends AccountMoveRequest> extends CopyMoveStrategyProxy<AMR>
{
    public AccountMsisdnCopyMoveStrategy(CopyMoveStrategy<AMR> delegate)
    {
        super(delegate);
    }

    /**
     * @{inheritDoc}
     */
    @Override
    public void validate(Context ctx, AMR request) throws IllegalStateException
    {
        CompoundIllegalStateException cise = new CompoundIllegalStateException();

        AccountMoveValidationSupport.validateOldAccountExists(ctx, request, cise);

        AccountMoveValidationSupport.validateNewAccountExists(ctx, request, cise);
        
        cise.throwAll();
        
        super.validate(ctx, request);
    }

    /**
     * @{inheritDoc}
     */
    @Override
    public void createNewEntity(Context ctx, AMR request) throws MoveException
    {
        super.createNewEntity(ctx, request);
        
        Map<Msisdn, Set<String>> deassociatedMsisdns = deassociateMsisdnsFromSubscriptions(ctx, request);
        for (Msisdn msisdnBean : MsisdnSupport.getAcquiredMsisdn(ctx, request.getExistingBAN()))
        {
            releaseMsisdn(ctx, request, msisdnBean.getMsisdn());
            try
            {
                claimMsisdn(ctx, request, msisdnBean.getMsisdn());
            }
            catch (MoveException e)
            {
                new MajorLogMsg(this, "Account " + request.getNewBAN() + " failed to claim MSISDN "
                        + msisdnBean.getMsisdn() + ": " + e.getMessage(), e).log(ctx);
                rollbackMsisdnManagement(ctx, request, msisdnBean, deassociatedMsisdns.get(msisdnBean.getMsisdn()));
                throw e;
            }
        }
    }

    /**
     * @{inheritDoc}
     */
    @Override
    public void removeOldEntity(Context ctx, AMR request) throws MoveException
    {
        // Nothing to do, because we had to release the MSISDN before claiming it in create.
        super.removeOldEntity(ctx, request);
    }

    /**
     * Attempts to release the MSISDN from the old account and reset the MSISDN to available state.
     * 
     * @param ctx Move context.
     * @param request Move request.
     * @param msisdn MSISDN that must be released by the old copy of the account.
     */
    private void releaseMsisdn(Context ctx, AMR request, String msisdn)
    {
        Account originalAccount = request.getOriginalAccount(ctx);
        try
        {
            new DebugLogMsg(this, "Account " + originalAccount.getBAN() + " releasing MSISDN " + msisdn + "...", null).log(ctx);
            MsisdnManagement.releaseMsisdn(ctx, msisdn, originalAccount.getBAN(), "");
            new InfoLogMsg(this, "MSISDN " + msisdn + " successfully released by account " + originalAccount.getBAN(), null).log(ctx);
        }
        catch (HomeException e)
        {
            new InfoLogMsg(this, "Error releasing MSISDN " + msisdn + " from account " + originalAccount.getBAN() + ".  "
                    + "Will attempt to proceed to claim MSISDN in new account, which will fail if it's not allowed.", e).log(ctx);
        }

        try
        {
            Home msisdnHome = (Home)ctx.get(MsisdnHome.class);
            Msisdn msisdnObj =(Msisdn) msisdnHome.find(ctx, msisdn);
            if (msisdnObj != null && MsisdnStateEnum.HELD.equals(msisdnObj.getState()))
            {
                // required because we can move ported MSISDNs, and otherwise MSISDN home will not let them become available
                ctx.put(MsisdnPortHandlingHome.MSISDN_PORT_KEY, msisdnObj.getMsisdn());
                new DebugLogMsg(this, "Resetting MSISDN " + msisdn + " to available state...", null).log(ctx);
                msisdnObj.reset();
                msisdnHome.store(ctx, msisdnObj);
                new InfoLogMsg(this, "MSISDN " + msisdn + " successfully reset to available state.", null).log(ctx);
            }
        }
        catch (HomeException e)
        {
            new InfoLogMsg(this, "Error occurred ensuring that MSISDN " + msisdn + " is in available state.  Will attempt to claim anyways.", null).log(ctx);
        }
    }

    /**
     * 
     * @param ctx Move context.
     * @param request Move request.
     * @param msisdnBean MSISDN object containing information about the MSISDN that must be claimed
     * 
     * @throws MoveException Thrown when there is an unrecoverable error when the new account attempts to claim the mobile number
     */
    private void claimMsisdn(Context ctx, AMR request, String msisdn) throws MoveException
    {
        Account newAccount = request.getNewAccount(ctx);        
        try
        {
            new DebugLogMsg(this, "Account " + newAccount.getBAN() + " claiming MSISDN " + msisdn + "...", null).log(ctx);
            Msisdn msisdnBean = (Msisdn) ((Home)ctx.get(MsisdnHome.class)).find(ctx, msisdn);
            MsisdnStateEnum msisdnState = msisdnBean.getState();
            if (MsisdnStateEnum.AVAILABLE != msisdnState)
            {
                if (MsisdnStateEnum.HELD == msisdnState)
                {
                    ctx.put(MsisdnPortHandlingHome.MSISDN_PORT_KEY, msisdnBean.getMsisdn());
                    msisdnBean.setState(MsisdnStateEnum.AVAILABLE);
                    HomeSupportHelper.get(ctx).storeBean(ctx, msisdnBean);
                }
            }
            MsisdnManagement.claimMsisdn(ctx, msisdn, newAccount.getBAN(), msisdnBean.isExternal(), "");
            new InfoLogMsg(this, "MSISDN " + msisdn + " successfully claimed by account " + newAccount.getBAN(), null).log(ctx);
        }
        catch (MsisdnAlreadyAcquiredException e)
        {
            new InfoLogMsg(this, "MSISDN " + msisdn + " already claimed by account " + newAccount.getBAN(), null).log(ctx);
        }
        catch (HomeException e)
        {
            throw new MoveException(request, "Account " + newAccount.getBAN() + " failed to claim mobile number: "
                    + msisdn, e);
        }
    }

    /**
     * Attempts to deassociate mobile numbers with subscriptions in the old account.  If deassociation fails, then
     * reassociation is attempted for mobile numbers which were already processed.
     * 
     * @param ctx Move context
     * @param request Move request
     * @return List of MSISDNs that were deassociated.
     * 
     * @throws MoveException Thrown when an unrecoverable error occurs and the move must be aborted.
     */
    private Map<Msisdn,Set<String>> deassociateMsisdnsFromSubscriptions(Context ctx, AMR request) throws MoveException
    {
        Map<Msisdn,Set<String>> msisdnMap =  new HashMap<Msisdn,Set<String>>();
        
        Account account = request.getOriginalAccount(ctx);
        
        Collection<Msisdn> msisdns = MsisdnSupport.getAcquiredMsisdn(ctx, account.getBAN());
        try
        {
            for (Msisdn msisdnObj : msisdns)
            {
                Set<String> subIds = null;

                String msisdn = msisdnObj.getMsisdn();
                try
                {
                    new DebugLogMsg(this, "Retrieving subscription IDs associated with mobile number " + msisdn + "...", null).log(ctx);
                    subIds = SubscriberSupport.getSubscriptionIdsByMSISDN(ctx, msisdn);
                    new DebugLogMsg(this, "Retrieved " + (subIds == null ? "0" : subIds.size()) + " subscription IDs associated with mobile number " + msisdn, null).log(ctx);
                }
                catch (HomeException e)
                {
                    new InfoLogMsg(this, "Error retrieving subscription IDs associated with mobile number " + msisdn + ".  "
                            + "Will attempt to proceed with MSISDN management, which will fail if there are subscriptions "
                            + "associated with mobile number " + msisdn + ".", e).log(ctx);
                }

                if (subIds != null && subIds.size() > 0)
                {
                    msisdnMap.put(msisdnObj, subIds);

                    for (String subId : subIds)
                    {
                        new DebugLogMsg(this, "Subscription " + subId + " deassociating MSISDN " + msisdn + " to facilitate moving account " + account.getBAN() + "...", null).log(ctx);
                        MsisdnManagement.deassociateMsisdnWithSubscription(ctx, msisdn, subId, "");
                        new InfoLogMsg(this, "Successfully deassociated MSISDN " + msisdn + " from subscription " + subId + " to facilitate moving account " + account.getBAN(), null).log(ctx);
                    }
                }   
            }
        }
        catch (HomeException e)
        {
            for (Msisdn msisdnObj : msisdnMap.keySet())
            {
                new MinorLogMsg(this, "Failed to deassociate MSISDNs from subscriptions in account ["
                        + account.getBAN() + "].  Will attempt to reassociate the MSISDNs.", e).log(ctx);
                reassociateMsisdnToSubscriptions(ctx, msisdnObj.getMsisdn(), msisdnMap.get(msisdnObj));   
            }
            throw new MoveException(request, "Failed to deassociate MSISDNs from subscriptions in account ["
                    + account.getBAN() + "]. " + e.getMessage(), e);
        }
        
        return msisdnMap;
    }

    /**
     * Attempts to have the original account reclaim the MSISDN and reassociated all subscriptions with their appropriate MSISDNs.
     * 
     * @param ctx Move context.
     * @param request Move request.
     * @param msisdnBean MSISDN object containing information about the MSISDN that must be claimed & reassociated with subscriptions
     * @param subIds List of subscription IDs that must be reassociated with the given MSISDN
     */
    private void rollbackMsisdnManagement(Context ctx, AMR request, Msisdn msisdnBean, Set<String> subIds)
    {
        Account originalAccount = request.getOriginalAccount(ctx);
        Account newAccount = request.getNewAccount(ctx);
        
        String msisdn = msisdnBean.getMsisdn();
        try
        {
            new DebugLogMsg(this, "Account " + originalAccount.getBAN() + " reclaiming MSISDN " + msisdn + "...", null).log(ctx);
            MsisdnManagement.claimMsisdn(ctx, msisdn, originalAccount.getBAN(), msisdnBean.isExternal(), "");
            new InfoLogMsg(this, "MSISDN " + msisdn + " successfully reclaimed by account " + newAccount.getBAN(), null).log(ctx);
        }
        catch (HomeException e)
        {
            new MajorLogMsg(this, "Account " + newAccount.getBAN() + " failed to reclaim MSISDN " + msisdn + ".  Will attempt to reassociate subscriptions.  Error message: " + e.getMessage(), e).log(ctx);
        }
        catch (MsisdnAlreadyAcquiredException e)
        {
            // NOP
        }
        finally
        {
            reassociateMsisdnToSubscriptions(ctx, msisdn, subIds);   
        }
    }

    /**
     * Attempts to reassociate all subscriptions with their appropriate MSISDNs.
     * 
     * @param ctx Move context
     * @param msisdn MSISDN that must be associated with each subscriber ID in the given set.
     * @param subIds List of subscriber IDs that need to be reassociated with the given MSISDN.
     */
    private void reassociateMsisdnToSubscriptions(Context ctx, String msisdn, Set<String> subIds)
    {
        if (subIds != null)
        {
            for (String subId : subIds)
            {
                try
                {
                    new DebugLogMsg(this, "Attempting to reassociate MSISDN " + msisdn + " with subscription " + subId, null).log(ctx);
                    MsisdnManagement.associateMsisdnWithSubscription(ctx, msisdn, subId, "");
                    new InfoLogMsg(this, "Successfully reassociated MSISDN " + msisdn + " with subscription " + subId, null).log(ctx);
                }
                catch (HomeException e)
                {
                    new MajorLogMsg(this, "Failed to reassociate MSISDN " + msisdn + " with subscription " + subId, e).log(ctx);
                }
            }
        }
    }
}
