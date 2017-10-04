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

import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.beans.SafetyUtil;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextSupport;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.webcontrol.HTMLExceptionListener;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.InfoLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.move.MoveConstants;
import com.trilogy.app.crm.move.MoveException;
import com.trilogy.app.crm.move.MoveProcessor;
import com.trilogy.app.crm.move.processor.MoveProcessorProxy;
import com.trilogy.app.crm.move.processor.account.strategy.AccountGroupMsisdnCopyMoveStrategy;
import com.trilogy.app.crm.move.request.AccountMoveRequest;
import com.trilogy.app.crm.move.request.AccountMoveRequestXInfo;
import com.trilogy.app.crm.move.support.AccountMoveValidationSupport;
import com.trilogy.app.crm.move.support.MoveProcessorSupport;


/**
 * This processor is responsible for setting the new BAN field = existing BAN
 * during the setup.  During the move it is responsible for updating the account
 * via Home operations.  It does not modify the account.
 * 
 * It only performs validation required to perform its duty.  No business use case
 * validation is performed here.
 *
 * @author Aaron Gourley
 * @since 8.1
 */
public class AccountUpdateMoveProcessor<AMR extends AccountMoveRequest> extends MoveProcessorProxy<AMR>
{
    public AccountUpdateMoveProcessor(AMR request)
    {
        this(new BaseAccountMoveProcessor<AMR>(request));
    }
    
    
    public AccountUpdateMoveProcessor(MoveProcessor<AMR> delegate)
    {
        super(delegate);
    }


    /**
     * @{inheritDoc}
     */
    @Override
    public void validate(Context ctx) throws IllegalStateException
    {
        CompoundIllegalStateException cise = new CompoundIllegalStateException();
        
        AMR request = this.getRequest();

        if (!ctx.has(MoveConstants.CUSTOM_ACCOUNT_HOME_CTX_KEY))
        {
            cise.thrown(new IllegalStateException(
                "Custom account home not installed in context."));
        }

        AccountMoveValidationSupport.validateNewAccountExists(ctx, request, cise);
        
        if (!SafetyUtil.safeEquals(request.getExistingBAN(), request.getNewBAN()))
        {
            cise.thrown(new IllegalPropertyArgumentException(
                    AccountMoveRequestXInfo.NEW_BAN, 
                    "Old & new account numbers do not match.  They must match when moving this type of account."));
        }
        
        cise.throwAll();

        // Don't bother letting the delegate validate unless this processor has passed.
        super.validate(ctx);
    }


    /**
     * @{inheritDoc}
     */
    @Override
    public void move(Context ctx) throws MoveException
    {
        AMR request = getRequest();
        Context subCtx = ctx.createSubContext();
        subCtx.put(HTMLExceptionListener.class, null);

        Account account = request.getNewAccount(ctx);
        account.setOldBAN(request.getOldBAN());
        account.setCreateAccountReason("MOVE");
               
        try
        {
            try
            {
                Home accountHome = (Home) subCtx.get(MoveConstants.CUSTOM_ACCOUNT_HOME_CTX_KEY);
    
                new DebugLogMsg(this, "Updating account (BAN=" + account.getBAN() + ") in account home.", null).log(subCtx);
                Account newAccount = (Account) accountHome.store(subCtx, account);
                if (newAccount != null)
                {
                    new InfoLogMsg(this, "Account (BAN=" + newAccount.getBAN() + ") updated in account home successfully.", null).log(subCtx);
                    request.setNewBAN(newAccount);  
                    
                    //This method is use to update the Group Id in CPS when Prepaid group Account is move to group pooled account
                    if(request.getNewParentAccount(subCtx) != null && newAccount.isPrepaid() && request.getNewParentAccount(subCtx).isPooled(subCtx) )
                    {
                    	AccountGroupMsisdnCopyMoveStrategy.updateGroupIDInBMGT(subCtx,request.getNewParentAccount(ctx),newAccount);
                    }
                }
            }
            catch(HomeException he)
            {
                throw new MoveException(request, "Error occurred while updating account with BAN " + request.getExistingBAN(), he);
            }
            
            super.move(subCtx);
        }
        finally
        {
            MoveProcessorSupport.copyHTMLExceptionListenerExceptions(subCtx, ctx);
        }
    }
}
