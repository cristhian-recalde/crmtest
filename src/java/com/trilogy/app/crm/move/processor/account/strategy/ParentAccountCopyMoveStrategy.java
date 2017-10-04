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

import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.beans.SafetyUtil;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.InfoLogMsg;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountStateEnum;
import com.trilogy.app.crm.move.MoveException;
import com.trilogy.app.crm.move.processor.strategy.CopyMoveStrategy;
import com.trilogy.app.crm.move.processor.strategy.CopyMoveStrategyProxy;
import com.trilogy.app.crm.move.request.AccountMoveRequest;
import com.trilogy.app.crm.move.request.AccountMoveRequestXInfo;
import com.trilogy.app.crm.move.request.PostpaidServiceBasedSubscriberAccountMoveRequest;
import com.trilogy.app.crm.move.support.AccountMoveValidationSupport;
import com.trilogy.app.crm.support.EnumStateSupportHelper;


/**
 * 
 *
 * @author Aaron Gourley
 * @since 
 */
public class ParentAccountCopyMoveStrategy<AMR extends AccountMoveRequest> extends CopyMoveStrategyProxy<AMR>
{
    public ParentAccountCopyMoveStrategy(CopyMoveStrategy<AMR> delegate)
    {
        super(delegate);
    }

    /**
     * @{inheritDoc}
     */
    @Override
    public void initialize(Context ctx, AMR request)
    {
        Account oldAccount = request.getOriginalAccount(ctx);
        Account account = request.getNewAccount(ctx);

        if (account != null && oldAccount != null)
        {
            account.setParentBAN(request.getNewParentBAN());
            new DebugLogMsg(this, "New copy of account " + oldAccount.getBAN()
                    + " initialized with parent BAN " + account.getParentBAN(), null).log(ctx);

            Account newParentAccount = request.getNewParentAccount(ctx);
            
            if (newParentAccount != null)
            {
                account.setBillCycleID((newParentAccount.getBillCycleID()));
                new DebugLogMsg(this, "New copy of account " + oldAccount.getBAN()
                        + " initialized with bill cycle ID " + account.getBillCycleID(), null).log(ctx);   
            }
            
            
        }
        
        super.initialize(ctx, request);
    }

    /**
     * @{inheritDoc}
     */
    @Override
    public void validate(Context ctx, AMR request) throws IllegalStateException
    {
        CompoundIllegalStateException cise = new CompoundIllegalStateException();

        Account account = AccountMoveValidationSupport.validateOldAccountExists(ctx, request, cise);

        AccountMoveValidationSupport.validateNewAccountExists(ctx, request, cise);

        
        if (SafetyUtil.safeEquals(request.getExistingBAN(), request.getNewBAN()))
        {
            cise.thrown(new IllegalPropertyArgumentException(
                    AccountMoveRequestXInfo.NEW_BAN, 
                    "Old & new account numbers are the same.  They must differ when moving this type of account."));
        }
        
        // The following validation is business logic validation which does not apply to billing type conversion
        AccountMoveValidationSupport.validateNewParentAccountExists(ctx, request, cise);
    
        cise.throwAll();

        // Don't bother letting the delegate validate unless this processor has passed.
        super.validate(ctx, request);
    }
    
    /**
     * @{inheritDoc}
     */
    @Override
    public void createNewEntity(Context ctx, AMR request) throws MoveException
    {   
        super.createNewEntity(ctx, request);
        new InfoLogMsg(this, "New copy of account " + request.getOriginalAccount(ctx).getBAN()
                + " successfully created (New BAN " + request.getNewBAN() + ").", null).log(ctx);
    }

    /**
     * @{inheritDoc}
     */
    @Override
    public void removeOldEntity(Context ctx, AMR request) throws MoveException
    {
        Account oldAccount = request.getOldAccount(ctx);
        oldAccount.setState(AccountStateEnum.INACTIVE);

        new DebugLogMsg(this, "Deactivating account " + request.getExistingBAN() + "...", null).log(ctx);
        super.removeOldEntity(ctx, request);
        new DebugLogMsg(this, "Account deactivated successfully.", null).log(ctx);
    }
}
