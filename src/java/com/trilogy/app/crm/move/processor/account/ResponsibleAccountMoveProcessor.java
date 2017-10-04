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
package com.trilogy.app.crm.move.processor.account;

import java.util.List;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountStateEnum;
import com.trilogy.app.crm.bean.AccountXInfo;
import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.move.MoveException;
import com.trilogy.app.crm.move.MoveProcessor;
import com.trilogy.app.crm.move.processor.MoveProcessorProxy;
import com.trilogy.app.crm.move.request.AccountMoveRequest;
import com.trilogy.app.crm.move.request.AccountMoveRequestXInfo;
import com.trilogy.app.crm.move.support.AccountMoveValidationSupport;
import com.trilogy.app.crm.support.EnumStateSupportHelper;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.app.crm.web.border.AccountIdentificationValidator;
import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;


/**
 * This processor is responsible for updating the account to satisfy the
 * business logic of moving a responsible account.  It performs no Home
 * operations.
 * 
 * It performs any validation necessary to complete its tasks, as well as
 * any validation that is specific to the moving of responsible accounts.
 *
 * @author Aaron Gourley
 * @since 8.1
 */
public class ResponsibleAccountMoveProcessor<AMR extends AccountMoveRequest> extends MoveProcessorProxy<AMR>
{ 
    
    public ResponsibleAccountMoveProcessor(MoveProcessor<AMR> delegate)
    {
        super(delegate);
    }
    

    /**
     * @{inheritDoc}
     */
    @Override
    public Context setUp(Context ctx) throws MoveException
    {
        Context moveCtx = super.setUp(ctx);
        
        AMR request = getRequest();
        
        Account account = request.getNewAccount(moveCtx);
        if (account != null)
        {
            // Update new BAN to match the existing BAN
            account.setBAN(request.getExistingBAN());
            request.setNewBAN(account);
            
            account.setParentBAN(request.getNewParentBAN());
            new DebugLogMsg(this, "Account " + request.getOriginalAccount(moveCtx).getBAN()
                    + " updated with parent BAN " + account.getParentBAN(), null).log(moveCtx);
            
            Account newParentAccount = request.getNewParentAccount(moveCtx);
            if (newParentAccount != null)
            {
                account.setBillCycleID(newParentAccount.getBillCycleID());
                new DebugLogMsg(this, "Account " + request.getOriginalAccount(moveCtx).getBAN()
                        + " updated with bill cycle ID " + account.getBillCycleID(), null).log(moveCtx);
            }
        }
        
        return moveCtx;
    }


    /**
     * @{inheritDoc}
     */
    @Override
    public void validate(Context ctx) throws IllegalStateException
    {
        CompoundIllegalStateException cise = new CompoundIllegalStateException();
        
        AMR request = this.getRequest();
        
        String newParentBAN = request.getNewParentBAN();

        Account account = AccountMoveValidationSupport.validateOldAccountExists(ctx, request, cise);
        if (account != null)
        {
            String parentBAN = account.getParentBAN();
            if (parentBAN == null)
            {
                if (newParentBAN == null || newParentBAN.length() == 0)
                {
                    cise.thrown(new IllegalPropertyArgumentException(
                            AccountMoveRequestXInfo.NEW_PARENT_BAN, 
                            "Responsible account (BAN=" + account.getBAN() + ") is not in an account hierarchy and no parent BAN was specified.  Nothing to do."));
                }
            }
            else if (parentBAN.trim().length() == 0
                    && (newParentBAN == null || parentBAN.trim().equals(newParentBAN)))
            {
                cise.thrown(new IllegalPropertyArgumentException(
                        AccountMoveRequestXInfo.NEW_PARENT_BAN, 
                        "Responsible account (BAN=" + account.getBAN() + ") is not in an account hierarchy and no parent BAN was specified.  Nothing to do."));
            }
        }
        
        Account newParentAccount = AccountMoveValidationSupport.validateNewParentAccountExists(ctx, request, cise);
        if (newParentAccount != null)
        {
            validateResponsibleAccount(ctx, account, request, newParentAccount, cise);
        }
        
        cise.throwAll();

        // Don't bother letting the delegate validate unless this processor has passed.
        super.validate(ctx);
    }

    public static <AMR extends AccountMoveRequest> void validateResponsibleAccount(Context ctx, Account account, AMR request, Account newParentAccount, CompoundIllegalStateException cise)
    {
    	if (EnumStateSupportHelper.get(ctx).stateEquals(newParentAccount, AccountStateEnum.INACTIVE))
    	{
    		cise.thrown(new IllegalPropertyArgumentException(
    				AccountMoveRequestXInfo.NEW_PARENT_BAN, 
    				"New parent account (BAN=" + request.getNewParentBAN() + ") is " + newParentAccount.getState() + "."));
    	}
    	else if ((newParentAccount != null) && newParentAccount.isPooled(ctx) && account.isIndividual(ctx) && !account.isPrepaid())
    	{
    		cise
    		.thrown(new IllegalPropertyArgumentException(
    				AccountMoveRequestXInfo.NEW_PARENT_BAN,
    				"New parent account (BAN="
    						+ request.getNewBAN()
    						+ ") is a Pooled Account. A responsible Subscriber Account cannot be moved under a Pooled Account."));
    	}    



    }

    /**
     * @{inheritDoc}
     */
    @Override
    public void move(Context ctx) throws MoveException
    {        
        super.move(ctx);
    }


    /**
     * @{inheritDoc}
     */
    @Override
    public AMR getRequest()
    {
        return super.getRequest();
    }


    /**
     * @{inheritDoc}
     */
    @Override
    public MoveProcessor<AMR> getDelegate()
    {
        return super.getDelegate();
    }


    /**
     * @{inheritDoc}
     */
    @Override
    public void setDelegate(MoveProcessor<AMR> delegate)
    {
        super.setDelegate(delegate);
    }

}
