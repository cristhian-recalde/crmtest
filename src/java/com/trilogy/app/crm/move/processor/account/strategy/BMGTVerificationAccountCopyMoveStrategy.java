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
package com.trilogy.app.crm.move.processor.account.strategy;

import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.InfoLogMsg;
import com.trilogy.framework.xlog.log.MajorLogMsg;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountStateEnum;
import com.trilogy.app.crm.client.bm.BalanceManagementSupport;
import com.trilogy.app.crm.client.bm.Parameters;
import com.trilogy.app.crm.client.bm.SubscriberProfileProvisionClient;
import com.trilogy.app.crm.client.bm.SubscriberProfileProvisionException;
import com.trilogy.app.crm.move.MoveException;
import com.trilogy.app.crm.move.processor.strategy.CopyMoveStrategy;
import com.trilogy.app.crm.move.processor.strategy.CopyMoveStrategyProxy;
import com.trilogy.app.crm.move.request.AccountMoveRequest;
import com.trilogy.app.crm.move.support.AccountMoveValidationSupport;


/**
 * This copy strategy verifies that the balance management profile was moved successfully.
 * 
 * @author aaron.gourley@redknee.com
 * @since 8.2
 */
public class BMGTVerificationAccountCopyMoveStrategy<AMR extends AccountMoveRequest> extends CopyMoveStrategyProxy<AMR>
{

    public BMGTVerificationAccountCopyMoveStrategy(CopyMoveStrategy<AMR> delegate)
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

        AccountMoveValidationSupport.validateOldAccountExists(ctx, request, cise);

        AccountMoveValidationSupport.validateNewAccountExists(ctx, request, cise);

        cise.throwAll();

        // Don't bother letting the delegate validate unless this processor has passed.
        super.validate(ctx, request);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void createNewEntity(Context ctx, AMR request) throws MoveException
    {
        super.createNewEntity(ctx, request);

        Account account = request.getNewAccount(ctx);

        new DebugLogMsg(this, "Verifying that BM profile exists for new account (BAN=" + request.getNewBAN() + ")...",
                null).log(ctx);
        Parameters bmProfile = getBalanceManagementProfile(ctx, request, account);
        if (bmProfile == null)
        {
            throw new MoveException(request, "Balance Management account profile " + account.getBAN()
                    + " was not created.");
        }
        else
        {
            new InfoLogMsg(this, "BM profile exists for new account (BAN=" + request.getNewBAN() + ").", null).log(ctx);
        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void removeOldEntity(Context ctx, AMR request) throws MoveException
    {
        Account oldAccount = request.getOldAccount(ctx);
        oldAccount.setState(AccountStateEnum.INACTIVE);
        super.removeOldEntity(ctx, request);

        new DebugLogMsg(this, "Verifying that BM profile no longer exists for old account (BAN="
                + request.getExistingBAN() + ")...", null).log(ctx);

        Parameters bmProfile = getBalanceManagementProfile(ctx, request, oldAccount);
        if (bmProfile != null)
        {
            new InfoLogMsg(this, "BM profile exists for old account (BAN=" + request.getExistingBAN() + ").", null)
                    .log(ctx);

            //throw new MoveException(request, "Balance Management account profile " + request.getExistingBAN()
            //+ " was not removed.");
        }
        else
        {
            new InfoLogMsg(this, "BM profile no longer exists for old account (BAN=" + request.getExistingBAN() + ").",
                    null).log(ctx);
        }
    }


    private Parameters getBalanceManagementProfile(Context ctx, AMR request, Account account) throws MoveException
    {
        final SubscriberProfileProvisionClient client = BalanceManagementSupport
                .getSubscriberProfileProvisionClient(ctx);

        Parameters profile = null;
        try
        {
            profile = client.querySubscriberAccountProfile(ctx, account);
        }
        catch (final SubscriberProfileProvisionException exception)
        {
            new MajorLogMsg(this, "Failed to communicate with BM to ensure profile exists for account "
                    + account.getBAN(), exception).log(ctx);
        }
        catch (final HomeException exception)
        {
            new MajorLogMsg(this, "Failed to ensure that BM profile exists for account " + account.getBAN(), exception)
                    .log(ctx);
        }

        return profile;
    }
}
