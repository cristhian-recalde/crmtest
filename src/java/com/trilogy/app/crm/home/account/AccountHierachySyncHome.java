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
package com.trilogy.app.crm.home.account;

import java.util.Collection;
import java.util.Iterator;

import com.trilogy.framework.xhome.beans.XBeans;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xhome.visitor.AbortVisitException;
import com.trilogy.framework.xhome.visitor.Visitor;
import com.trilogy.framework.xhome.visitor.Visitors;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.InfoLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MinorLogMsg;

import com.trilogy.app.crm.Common;
import com.trilogy.app.crm.account.AccountConstants;
import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountHome;
import com.trilogy.app.crm.bean.BillCycleChangeStatusEnum;
import com.trilogy.app.crm.bean.BillCycleHistory;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.client.ProvisioningHomeException;
import com.trilogy.app.crm.hlr.HlrSupport;
import com.trilogy.app.crm.home.sub.HLRConstants;
import com.trilogy.app.crm.provision.CommonProvisionAgentBase;
import com.trilogy.app.crm.provision.ProvisionAgentException;
import com.trilogy.app.crm.subscriber.provision.SubscriberProvisionResultCode;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.app.crm.support.SystemSupport;

/**
 * @author jchen
 *
 * Sync properties with its parent account,
 * So far, properties include:
 *         BillCycle, contract, contractStartDate, contractEndDate
 *         
 */
public class AccountHierachySyncHome extends HomeProxy
{

    public AccountHierachySyncHome(Context ctx, Home delegate)
    {
        super(ctx, delegate);
    }

    /* (non-Javadoc)
     * @see com.redknee.framework.xhome.home.HomeSPI#create(com.redknee.framework.xhome.context.Context, java.lang.Object)
     */
    @Override
    public Object create(Context ctx, Object obj) throws HomeException
    {
        Account account = (Account)obj;
        if (!account.isRootAccount())
        {
            // inherit fields from parent Account
            account = inheritAttributesFromParent(ctx, account);
        }
        return super.create(ctx, account);
    }

    /**
     * Copies inheritable values from parent account to the given account and returns it.
     * @param ctx
     * @param account
     * @return
     * @throws HomeException
     */
    private Account inheritAttributesFromParent(Context ctx, Account account) throws HomeException
    {
        Account parent = account.getParentAccount(ctx);

        if (LogSupport.isDebugEnabled(ctx))
        {
            LogSupport.debug(ctx, AccountHierachySyncHome.class.getName(),
                    "Inherit from parent Account(" + parent.getBAN()+ ") Bill Cycle=" + parent.getBillCycleID() + " and Contract=" + parent.getContract());
        }
        // copy parent account's Role id
        account.setRole(parent.getRole());
        // copy parent account's billcycle id
        account.setBillCycleID(parent.getBillCycleID());
        // copy parent account's contract id, and dates
        account.setContract(parent.getContract());
        account.setContractEndDate(parent.getContractEndDate());
        account.setContractStartDate(parent.getContractStartDate()); 

        if (account.isIndividual(ctx) && parent.isPooled(ctx))
        {
            account.setResponsible(false);
        }

        return account;
    }

    /**
     * @param ctx
     * @param obj
     * @param account
     * @return new Account object, if create()/Store() home method already called in order to sync children accounts' billcycle
     * @throws HomeException
     */
    private Account updateBillCycle(Context ctx, Account account) throws HomeException 
    {
        boolean saved = false;
        if (LogSupport.isDebugEnabled(ctx))
        {
            new DebugLogMsg(this, "attempt to update bill cycle for: "+account.getBAN()+" with BC "+account.getBillCycleID(), null).log(ctx);
        }
        final int curBillCycle = account.getBillCycleID();
        final int curBillCycleDay = account.getBillCycleDay(ctx);

        Account oldAccount = (Account) ctx.get(AccountConstants.OLD_ACCOUNT);
        if (oldAccount != null)
        {
            final int oldBillCycle = oldAccount.getBillCycleID();
            final int oldBillCycleDay = oldAccount.getBillCycleDay(ctx);
            if (oldBillCycle != curBillCycle      ||
                    oldAccount.getContract()!= account.getContract() ||
                    !oldAccount.getContractEndDate().equals(account.getContractEndDate())||
                    !oldAccount.getContractStartDate().equals(account.getContractStartDate()))
            {
                if (LogSupport.isDebugEnabled(ctx))
                {
                    if (oldBillCycle != curBillCycle)
                    {
                        new DebugLogMsg(this, "Account(" + account.getBAN() + ") bill cycle changed from "+oldBillCycle+" to "+curBillCycle, null).log(ctx);
                    }
                    if (oldAccount.getContract()!= account.getContract())
                    {
                        new DebugLogMsg(this, "Account(" + account.getBAN() + ") contract changed from "
                                + " Contract=" + oldAccount.getContract() + " to new Contract=" + account.getContract()
                                + " ContractStartDate=" + oldAccount.getContractStartDate() + " to new ContractStartDate=" + account.getContractStartDate()
                                + " ContractEndDate=" + oldAccount.getContractEndDate() + " to new ContractEndDate=" + account.getContractEndDate(), null).log(ctx);
                    }
                }

                // save before recursively traverse into children accounts
                account = (Account)super.store(ctx, account);
                saved = true;

                if (oldBillCycle != curBillCycle
                        && account.isResponsible())
                {
                    // Only update bill cycle history for responsible accounts
                    if (LogSupport.isDebugEnabled(ctx))
                    {
                        new DebugLogMsg(this, "Creating completion bill cycle history record for account " + account.getBAN() 
                                + " following successful bill cycle change from ID " + oldBillCycle + " to " + curBillCycle + "...", null).log(ctx);
                    }
                    
                    BillCycleHistory hist = null;
                    try
                    {
                        hist = (BillCycleHistory) XBeans.instantiate(BillCycleHistory.class, ctx);
                    }
                    catch (Exception e)
                    {
                        hist = new BillCycleHistory();
                    }

                    try
                    {
                        hist.setBillCycleChangeDate(CalendarSupportHelper.get(ctx).getRunningDate(ctx));
                        hist.setBAN(account.getBAN());
                        hist.setStatus(BillCycleChangeStatusEnum.COMPLETE);
                        hist.setOldBillCycleID(oldBillCycle);
                        hist.setNewBillCycleID(curBillCycle);
                        
                        hist = HomeSupportHelper.get(ctx).createBean(ctx, hist);
                        new InfoLogMsg(this, "Created completion bill cycle history record for account " + account.getBAN() 
                                + " following successful bill cycle change from ID " + oldBillCycle + " to " + curBillCycle, null).log(ctx);
                    }
                    catch (HomeException e)
                    {
                        new MinorLogMsg(this, "Error saving bill cycle change history record to indicate successful bill cycle change for account " + hist.getBAN(), e).log(ctx);
                    }
                }
                
                if (LogSupport.isDebugEnabled(ctx))
                {
                    new DebugLogMsg(this, "saved account "+account.getBAN()+" to BC: "+account.getBillCycleID(), null).log(ctx);
                    new DebugLogMsg(this, "saved account "+account.getBAN()+" to Contract: "+account.getContract() + " starting="+ account.getContractStartDate() + " ending=" + account.getContractEndDate(), null).log(ctx);
                }
                sendToHLR(ctx, account, curBillCycle, curBillCycleDay, oldBillCycle, oldBillCycleDay);
                synchronizeDescendentAccounts(ctx, account);
            }
        }
        return saved ? account : null;
    }

    /**
     * @param ctx
     * @param account
     * @param oldBillCycleDay 
     * @param oldBillCycle 
     * @param curBillCycleDay 
     * @param curBillCycle 
     * @throws HomeException
     */
    public void sendToHLR(Context ctx, Account account, int curBillCycle, int curBillCycleDay, int oldBillCycle, int oldBillCycleDay)
            throws HomeException
    {
        ctx = ctx.createSubContext();
        ctx.put(HLRConstants.ACCOUNT_BILLCYCLE_CHANGE, Boolean.TRUE);
        ctx.put(CommonProvisionAgentBase.SPG_PROVISIONING_CUSTOM_OLD_BILLCYCLEDAY, Integer.valueOf(oldBillCycleDay));
        ctx.put(CommonProvisionAgentBase.SPG_PROVISIONING_CUSTOM_OLD_BILLCYCLEID, Integer.valueOf(oldBillCycle));
        ctx.put(CommonProvisionAgentBase.SPG_PROVISIONING_CUSTOM_BILLCYCLEDAY, Integer.valueOf(curBillCycleDay));
        ctx.put(CommonProvisionAgentBase.SPG_PROVISIONING_CUSTOM_BILLCYCLEID, Integer.valueOf(curBillCycle));
        
        Collection collection = account.getSubscribers(ctx);
        Iterator<Subscriber> allSubsItr = collection.iterator();
        while (allSubsItr.hasNext())
        {
            Subscriber subscriber = allSubsItr.next();
            if( !subscriber.getState().equals(SubscriberStateEnum.INACTIVE) 
            		&& ctx.getBoolean(Common.DURING_BILL_CYCLE_CHANGE, Boolean.FALSE)== Boolean.TRUE) 
            {
            	// If call is coming from Bill Cycle Change task, don't send HLR call for Deactivated subscriber 
            	// since this subscriber doesnt exist there anymore, it exists only in BSS. Ref: TT# 13112219009
	            try
	            {
	                if (SystemSupport.needsHlr(ctx))
	                {
	                    HlrSupport.updateHlr(ctx, subscriber, HLRConstants.PRV_CMD_TYPE_BILLCYCLE_CHANGE);
	                }                                    
	            }
	            catch (ProvisionAgentException e)
	            {
	                SubscriberProvisionResultCode.setProvisionHlrResultCode(ctx, -1);
	                new MinorLogMsg(this, "Error propogating bill cycle provision: " + e.getMessage() + " (Command = " + HLRConstants.PRV_CMD_TYPE_BILLCYCLE_CHANGE + ")", e).log(ctx);
	            }
            }
            else
            {
            	LogSupport.info(ctx, this, "Subscription with ID "+subscriber.getId()+ 
            			" is in Deactivated state, hence not sending HLR call for Bill Cycle change.");
            }
        }
    }
    
    /**
     * Updates billcycle and contract for all descendent accounts recusively
     * @param ctx
     * @param account
     * @throws HomeException
     * @throws AgentException
     */
    protected void synchronizeDescendentAccounts(Context ctx, final Account account) throws HomeException
    {
        try 
        {
            final int newBillCycle = account.getBillCycleID();
            Collection subAccounts = account.getImmediateChildrenAccounts(ctx);
            // TODO 2007-05-24 reimplement as a Home Visitor to avoid loading all beans into memory
            final Home accountHome = (Home)ctx.get(AccountHome.class);
            Visitors.forEach(ctx, subAccounts, new Visitor()
            {
                public void visit(Context ctx1, Object obj)
                throws AgentException, AbortVisitException
                {

                    Account childAccount = (Account)obj;
                    int childBillCycle = childAccount.getBillCycleID();
                    if (    childBillCycle != newBillCycle ||
                            childAccount.getContract()!= account.getContract() ||
                            !childAccount.getContractEndDate().equals(account.getContractEndDate())||
                            !childAccount.getContractStartDate().equals(account.getContractStartDate()))
                    {
                        if (LogSupport.isDebugEnabled(ctx1))
                        {
                            new DebugLogMsg(this, "Propogating bill cycle and contract information from account " + account.getBAN() + " to child account " + childAccount.getBAN(), null).log(ctx1);
                        }
                        childAccount.setBillCycleID(newBillCycle);
                        childAccount.setContract(account.getContract());
                        childAccount.setContractEndDate(account.getContractEndDate());
                        childAccount.setContractStartDate(account.getContractStartDate()); 
                        try 
                        {
                            accountHome.store(ctx1, childAccount);
                            new InfoLogMsg(this, "Propogated bill cycle and contract information from account " + account.getBAN() + " to child account " + childAccount.getBAN(), null).log(ctx1);
                        }
                        catch(HomeException he)
                        {
                            new MinorLogMsg(this, "Error propogating bill cycle and contract information from account " + account.getBAN() + " to child account " + childAccount.getBAN() + ".  Aborting propogation to remaining child accounts...", he).log(ctx1);
                            throw new AbortVisitException("Failed to update account bill cycle and/or contract information, account " + childAccount.getBAN(), he);
                        }
                    }
                }
            });
        }
        catch(AgentException e)
        {
            throw new HomeException("Failed to update billcycle for account" + account.getBAN(), e);
        }
    }
    
    /* (non-Javadoc)
     * @see com.redknee.framework.xhome.home.HomeSPI#store(com.redknee.framework.xhome.context.Context, java.lang.Object)
     */
    @Override
    public Object store(Context ctx, Object obj) throws HomeException
    {
        Account account = (Account)obj;
        Account newAccount = updateBillCycle(ctx, account);
        return newAccount != null ? newAccount : super.store(ctx, obj);
    }

}
