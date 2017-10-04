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
package com.trilogy.app.crm.home;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.NEQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xhome.msp.MSP;
import com.trilogy.framework.xhome.support.Command;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.InfoLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MinorLogMsg;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountStateEnum;
import com.trilogy.app.crm.bean.AccountXInfo;
import com.trilogy.app.crm.bean.BillCycleChangeStatusEnum;
import com.trilogy.app.crm.bean.BillCycleHistory;
import com.trilogy.app.crm.client.bm.BalanceManagementSupport;
import com.trilogy.app.crm.client.bm.SubscriberProfileProvisionClient;
import com.trilogy.app.crm.client.bm.SubscriberProfileProvisionCorbaException;
import com.trilogy.app.crm.client.bm.SubscriberProfileProvisionException;
import com.trilogy.app.crm.support.AccountSupport;
import com.trilogy.app.crm.support.BillCycleHistorySupport;
import com.trilogy.app.crm.support.FrameworkSupportHelper;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.product.bundle.manager.provision.profile.error.ErrorCode;
import com.trilogy.product.bundle.manager.provision.v5_0.profile.BCDChangeRequestReturnParam;


/**
 * 
 *
 * @author aaron.gourley@redknee.com
 * @since 9.1
 */
public class UrcsUpdatingBillCycleHistoryHome extends HomeProxy
{
    private static class CancelPendingChangeInURCSCommand implements Command
    {
        private final String ban_;

        public CancelPendingChangeInURCSCommand(String ban)
        {
            ban_ = ban;
        }
        
        public String getBAN()
        {
            return ban_;
        }
    }
    
    private static class CreatePendingChangeInURCSCommand implements Command
    {
        private final BillCycleHistory hist_;

        public CreatePendingChangeInURCSCommand(BillCycleHistory history)
        {
            hist_ = history;
        }
        
        public BillCycleHistory getHistory()
        {
            return hist_;
        }
    }
    
    public UrcsUpdatingBillCycleHistoryHome(Context ctx, Home delegate)
    {
        super(ctx, delegate);
    }
    
    public static void cancelPendingChangeInURCS(Context ctx, String ban) throws HomeException
    {
        BillCycleHistory lastEventInNewHierarchy = BillCycleHistorySupport.getLastEventInHierarchy(ctx, ban, BillCycleChangeStatusEnum.PENDING);
        if (lastEventInNewHierarchy != null)
        {
            Home home = HomeSupportHelper.get(ctx).getHome(ctx, BillCycleHistory.class);
            home.cmd(ctx, new CancelPendingChangeInURCSCommand(ban));
        }
    }
    
    public static void createPendingChangeInURCS(Context ctx, String ban) throws HomeException
    {
        BillCycleHistory lastEventInNewHierarchy = BillCycleHistorySupport.getLastEventInHierarchy(ctx, ban, BillCycleChangeStatusEnum.PENDING);
        if (lastEventInNewHierarchy != null)
        {
            try
            {
                lastEventInNewHierarchy = (BillCycleHistory) lastEventInNewHierarchy.clone();
            }
            catch (CloneNotSupportedException e1)
            {
                // NOP
            }
            
            lastEventInNewHierarchy.setBAN(ban);
            
            Home home = HomeSupportHelper.get(ctx).getHome(ctx, BillCycleHistory.class);
            home.cmd(ctx, new CreatePendingChangeInURCSCommand(lastEventInNewHierarchy));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object create(Context ctx, Object obj) throws HomeException, HomeInternalException
    {
        if (obj instanceof BillCycleHistory)
        {
            BillCycleHistory hist = (BillCycleHistory) obj;
            
            if (BillCycleChangeStatusEnum.PENDING.equals(hist.getStatus()))
            {
                BillCycleHistory lastEvent = BillCycleHistorySupport.getLastEvent(ctx, hist.getBAN());
                if (lastEvent == null
                        || (BillCycleChangeStatusEnum.PENDING.equals(lastEvent.getStatus())
                                && lastEvent.getBillCycleChangeDate().before(hist.getBillCycleChangeDate())))
                {
                    // If the latest event was pending, and this one is newer then update URCS
                    updateBillCycleDayInURCS(ctx, hist);
                }
            }
        }
        
        return super.create(ctx, obj);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object store(Context ctx, Object obj) throws HomeException, HomeInternalException
    {
        if (obj instanceof BillCycleHistory)
        {
            BillCycleHistory hist = (BillCycleHistory) obj;
            if (BillCycleChangeStatusEnum.PENDING.equals(hist.getStatus()))
            {
                Date lastEventDate = BillCycleHistorySupport.getLastEventDate(ctx, hist.getBAN());
                if (lastEventDate == null
                        || lastEventDate.equals(hist.getBillCycleChangeDate()))
                {
                    // If this is the latest pending event, update URCS
                    updateBillCycleDayInURCS(ctx, hist);
                }
            }
            else if (BillCycleChangeStatusEnum.CANCELLED.equals(hist.getStatus()))
            {
                Date lastEventDate = BillCycleHistorySupport.getLastEventDate(ctx, hist.getBAN());
                if (lastEventDate == null
                        || lastEventDate.equals(hist.getBillCycleChangeDate()))
                {
                    // If this is the latest pending event, update URCS
                    removePendingBillCycleDayChangeFromURCS(ctx, hist.getBAN());
                }
            }
        }
        
        return super.store(ctx, obj);
    }

    
    /**
     * {@inheritDoc}
     */
    @Override
    public Object cmd(Context ctx, Object arg) throws HomeException, HomeInternalException
    {
        if (arg instanceof CancelPendingChangeInURCSCommand)
        {
            CancelPendingChangeInURCSCommand cmd = ((CancelPendingChangeInURCSCommand) arg);
            removePendingBillCycleDayChangeFromURCS(ctx, cmd.getBAN());
        }
        else if (arg instanceof CreatePendingChangeInURCSCommand)
        {
            CreatePendingChangeInURCSCommand cmd = ((CreatePendingChangeInURCSCommand) arg);
            updateBillCycleDayInURCS(ctx, cmd.getHistory());
        }
        
        return super.cmd(ctx, arg);
    }

    protected void updateBillCycleDayInURCS(Context ctx, BillCycleHistory hist) throws HomeException
    {
        if (hist.getOldBillCycleDay() == hist.getNewBillCycleDay())
        {
            return;
        }

        Account account = AccountSupport.getAccount(ctx, hist.getBAN());
        String[] bans = getSubscriberAccountBANs(ctx, account);
        
        if (bans.length > 0)
        {
            SubscriberProfileProvisionClient urcsClient = BalanceManagementSupport.getSubscriberProfileProvisionClient(ctx);
            try
            {
                List<String> successes = new ArrayList<String>();
                Map<String, Short> failures = new HashMap<String, Short>();
                
                Context subCtx = ctx.createSubContext();                
                MSP.setBeanSpid(subCtx, account.getSpid());
                
                BCDChangeRequestReturnParam[] results = urcsClient.addUpdateBcdChangeRequest(subCtx, bans, hist.getNewBillCycleDay());
                if (results != null)
                {
                    for (BCDChangeRequestReturnParam result : results)
                    {
                        if (result != null)
                        {
                            if (result.resultCode != ErrorCode.SUCCESS
                                    && result.resultCode != ErrorCode.RECORD_ALREADY_EXIST)
                            {
                                String msg = "URCS failed to schedule pending bill cycle change for subscriber account [" + result.subscriberId + "]";
                                new MinorLogMsg(this, msg + " [Result Code=" + result.resultCode + "]", null).log(ctx);
                                failures.put(result.subscriberId, result.resultCode);
                                FrameworkSupportHelper.get(ctx).notifyExceptionListener(ctx, new SubscriberProfileProvisionException(result.resultCode, msg));
                            }
                            else
                            {
                                if (LogSupport.isDebugEnabled(ctx))
                                {
                                    new DebugLogMsg(this, "URCS notified of pending bill cycle change for subscriber account [" + result.subscriberId + "]", null).log(ctx);
                                }
                                successes.add(result.subscriberId);
                            }
                        }
                    }
                }
                
                if (successes.size() > 0)
                {
                    new InfoLogMsg(this, "URCS notified of pending bill cycle change to new date " + hist.getNewBillCycleDay() + " for subscriptions [" + successes + "]", null).log(ctx);
                }

                if (failures.size() > 0)
                {
                    String msg = "URCS failed to schedule pending bill cycle change for subscriber accounts [" + failures.keySet() + "]";
                    new InfoLogMsg(this, msg, null).log(ctx);
                    if (successes.size() == 0)
                    {
                        throw new HomeException(msg);
                    }
                }
            }
            catch (SubscriberProfileProvisionCorbaException e)
            {
                String msg = "Error provisioning bill cycle change to URCS [Error Code=" + e.getErrorCode() + "]";
                new MinorLogMsg(this, msg + ": " + e.getMessage(), e).log(ctx);
                throw new HomeException(msg, e);
            }
        }
    }


    protected void removePendingBillCycleDayChangeFromURCS(Context ctx, String ban) throws HomeException
    {
        Account account = AccountSupport.getAccount(ctx, ban);
        String[] bans = getSubscriberAccountBANs(ctx, account);
        
        if (bans.length > 0)
        {
            SubscriberProfileProvisionClient urcsClient = BalanceManagementSupport.getSubscriberProfileProvisionClient(ctx);

            try
            {
                List<String> successes = new ArrayList<String>();
                Map<String, Short> failures = new HashMap<String, Short>();
                
                Context subCtx = ctx.createSubContext();                
                MSP.setBeanSpid(subCtx, account.getSpid());
                
                BCDChangeRequestReturnParam[] results = urcsClient.removeBcdChangeRequest(subCtx, bans);
                if (results != null)
                {
                    for (BCDChangeRequestReturnParam result : results)
                    {
                        if (result != null)
                        {
                            if (result.resultCode != ErrorCode.SUCCESS
                                    && result.resultCode != ErrorCode.RECORD_NOT_FOUND)
                            {
                                String msg = "URCS failed to cancel bill cycle change for subscriber account [" + result.subscriberId + "]";
                                new MinorLogMsg(this, msg + " [Result Code=" + result.resultCode + "]", null).log(ctx);
                                failures.put(result.subscriberId, result.resultCode);
                                FrameworkSupportHelper.get(ctx).notifyExceptionListener(ctx, new SubscriberProfileProvisionException(result.resultCode, msg));
                            }
                            else
                            {
                                if (LogSupport.isDebugEnabled(ctx))
                                {
                                    new DebugLogMsg(this, "URCS notified of cancelled bill cycle change for subscriber account [" + result.subscriberId + "]", null).log(ctx);
                                }
                                successes.add(result.subscriberId);
                            }
                        }
                    }
                }
                
                if (successes.size() > 0)
                {
                    new InfoLogMsg(this, "URCS notified of cancelled bill cycle change for subscriber accounts [" + successes + "]", null).log(ctx);
                }

                if (failures.size() > 0)
                {
                    String msg = "URCS failed to cancel bill cycle change for subscriber accounts [" + failures.keySet() + "]";
                    new InfoLogMsg(this, msg, null).log(ctx);
                    if (successes.size() == 0)
                    {
                        throw new HomeException(msg);
                    }
                }
            }
            catch (SubscriberProfileProvisionCorbaException e)
            {
                String msg = "Error removing bill cycle change from URCS [Error Code=" + e.getErrorCode() + "]";
                new MinorLogMsg(this, msg + ": " + e.getMessage(), e).log(ctx);
                throw new HomeException(msg, e);
            }
        }
    }

    protected String[] getSubscriberAccountBANs(Context ctx, Account account) throws HomeException
    {
        List<String> banList = new ArrayList<String>();
        
        if (account != null)
        {
            Collection<Account> accounts = AccountSupport.getTopologyEx(ctx, 
                    account, 
                    new NEQ(AccountXInfo.STATE, AccountStateEnum.INACTIVE), 
                    null, true, true, null, false);
            if (accounts != null)
            {
                for (Account child : accounts)
                {
                    if (child != null 
                            && child.isIndividual(ctx))
                    {
                        banList.add(child.getBAN());   
                    }
                }
            }
        }
        
        String[] bans = banList.toArray(new String[]{});
        return bans;
    }
}
