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
package com.trilogy.app.crm.move.processor.account;

import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.beans.SafetyUtil;
import com.trilogy.framework.xhome.beans.XBeans;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.MinorLogMsg;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.BillCycleChangeStatusEnum;
import com.trilogy.app.crm.bean.BillCycleHistory;
import com.trilogy.app.crm.home.UrcsUpdatingBillCycleHistoryHome;
import com.trilogy.app.crm.move.MoveException;
import com.trilogy.app.crm.move.MoveProcessor;
import com.trilogy.app.crm.move.MoveWarningException;
import com.trilogy.app.crm.move.processor.MoveProcessorProxy;
import com.trilogy.app.crm.move.request.AccountMoveRequest;
import com.trilogy.app.crm.move.support.AccountMoveValidationSupport;
import com.trilogy.app.crm.support.BillCycleHistorySupport;
import com.trilogy.app.crm.support.HomeSupportHelper;


/**
 * 
 *
 * @author aaron.gourley@redknee.com
 * @since 9.1
 */
public class BillCycleDateChangeMoveProcessor<AMR extends AccountMoveRequest> extends MoveProcessorProxy<AMR>
{

    public BillCycleDateChangeMoveProcessor(MoveProcessor<AMR> delegate)
    {
        super(delegate);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void validate(Context ctx) throws IllegalStateException
    {
        CompoundIllegalStateException cise = new CompoundIllegalStateException();
        
        AMR request = this.getRequest();

        Account account = AccountMoveValidationSupport.validateOldAccountExists(ctx, request, cise);
        if (account.isRootAccount())
        {
            BillCycleHistory lastEvent = BillCycleHistorySupport.getLastEvent(ctx, account.getBAN());
            if (lastEvent != null && BillCycleChangeStatusEnum.PENDING.equals(lastEvent.getStatus()))
            {
                cise.thrown(new IllegalStateException(
                        "Not allowed to move root account " + request.getExistingBAN()
                        + " with pending bill cycle change into account " + request.getNewParentBAN()));
            }
        }
        
        cise.throwAll();

        super.validate(ctx);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void move(Context ctx) throws MoveException
    {
        super.move(ctx);
        
        AMR request = this.getRequest();
        
        Account originalAccount = request.getOriginalAccount(ctx);
        
        BillCycleHistory lastEvent = BillCycleHistorySupport.getLastEventInHierarchy(ctx, originalAccount);
        if (lastEvent != null 
                && BillCycleChangeStatusEnum.PENDING.equals(lastEvent.getStatus()))
        {
            Account newParent = request.getNewParentAccount(ctx);
            if (newParent == null)
            {
                // Account moving out of any hierarchy.  Take a copy of the pending bill cycle change with it.
                BillCycleHistory newHistory = null;
                try
                {
                    newHistory = (BillCycleHistory) XBeans.instantiate(BillCycleHistory.class, ctx);
                }
                catch (Exception e)
                {
                    newHistory = new BillCycleHistory();
                }
                newHistory.setBAN(request.getNewBAN());
                newHistory.setOldBillCycleID(originalAccount.getBillCycleID());
                newHistory.setNewBillCycleID(lastEvent.getNewBillCycleID());
                newHistory.setStatus(BillCycleChangeStatusEnum.PENDING);
                
                try
                {
                    newHistory = HomeSupportHelper.get(ctx).createBean(ctx, newHistory);
                }
                catch (HomeException e)
                {
                    new MinorLogMsg(this, "Error creating a new copy of bill cycle change request [Source=" + lastEvent.ID() + ", New=" + newHistory.ID()
                            + "] when moving responsible account out of existing hierarchy.  Will attempt to cancel existing pending change in URCS...", e).log(ctx);
                    request.reportWarning(ctx, 
                            new MoveWarningException(request, 
                                    "Unable to retain pending bill cycle change after move for account " + originalAccount.getBAN()
                                    + " to bill cycle day " + lastEvent.getNewBillCycleDay() + " (Bill Cycle ID=" + lastEvent.getNewBillCycleID() + ")", e));
                    cancelPendingBillCycleChange(ctx, request);
                }
            }
            else
            {
                Account oldRootAccount = null;
                Account newRootAccount = null;
                
                try
                {
                    newRootAccount = newParent.getRootAccount(ctx);
                    oldRootAccount = originalAccount.getRootAccount(ctx);
                }
                catch (HomeException e)
                {
                    // NOP - we'll update URCS anyways
                }
                
                if (oldRootAccount == null || newRootAccount == null
                        || !SafetyUtil.safeEquals(oldRootAccount.getBAN(), newRootAccount.getBAN()))
                {
                    // Account moving from one hierarchy to another.
                    // Cancel pending bill cycle change from old account and create pending bill cycle change in new account.
                    cancelPendingBillCycleChange(ctx, request);
                    createPendingBillCycleChange(ctx, request);
                }
            }
        }
    }

    protected void cancelPendingBillCycleChange(Context ctx, AMR request)
    {
        try
        {
            UrcsUpdatingBillCycleHistoryHome.cancelPendingChangeInURCS(ctx, request.getExistingBAN());
        }
        catch (HomeException e)
        {
            request.reportWarning(ctx, 
                    new MoveWarningException(request, "Unable to cancel pending bill cycle change in URCS for account " + request.getExistingBAN(), e));
        }
    }

    protected void createPendingBillCycleChange(Context ctx, AMR request)
    {
        try
        {
            UrcsUpdatingBillCycleHistoryHome.createPendingChangeInURCS(ctx, request.getNewBAN());
        }
        catch (HomeException e)
        {
            request.reportWarning(ctx, 
                    new MoveWarningException(request, "Unable to create pending bill cycle change in URCS for account " + request.getNewBAN(), e));
        }
    }

}
