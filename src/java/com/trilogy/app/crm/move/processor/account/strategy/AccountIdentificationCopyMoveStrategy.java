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

import java.util.List;

import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.context.Context;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.account.AbstractAccountIdentification;
import com.trilogy.app.crm.bean.account.AccountIdentification;
import com.trilogy.app.crm.move.MoveException;
import com.trilogy.app.crm.move.processor.strategy.CopyMoveStrategy;
import com.trilogy.app.crm.move.processor.strategy.CopyMoveStrategyProxy;
import com.trilogy.app.crm.move.request.AccountMoveRequest;
import com.trilogy.app.crm.move.support.AccountMoveValidationSupport;


/**
 * Handles moving of the account's identification fields, which are stored in a
 * separate home using a foreign-key relationship.
 *
 * @author Aaron Gourley
 * @since 8.1
 */
public class AccountIdentificationCopyMoveStrategy<AMR extends AccountMoveRequest> extends CopyMoveStrategyProxy<AMR>
{
    public AccountIdentificationCopyMoveStrategy(CopyMoveStrategy<AMR> delegate)
    {
        super(delegate);
    }
    
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void validate(Context ctx, AMR request) throws IllegalStateException
    {
        CompoundIllegalStateException cise = new CompoundIllegalStateException();

        AccountMoveValidationSupport.validateNewAccountExists(ctx, request, cise);
        
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
        Account newAccount = request.getNewAccount(ctx);
        
        List<AccountIdentification> identEntries = newAccount.getIdentificationList();
        for (AccountIdentification identification : identEntries)
        {
            identification.setId(AbstractAccountIdentification.DEFAULT_ID);
            identification.setBAN(AbstractAccountIdentification.DEFAULT_BAN);
        }
        
        super.createNewEntity(ctx, request);
    }

    /**
     * @{inheritDoc}
     */
    @Override
    public void removeOldEntity(Context ctx, AMR request) throws MoveException
    {
        super.removeOldEntity(ctx, request);
    }
}
