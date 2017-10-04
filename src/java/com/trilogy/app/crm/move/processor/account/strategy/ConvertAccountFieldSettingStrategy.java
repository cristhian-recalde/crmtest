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

import java.util.Date;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.move.MoveConstants;
import com.trilogy.app.crm.move.MoveException;
import com.trilogy.app.crm.move.processor.strategy.CopyMoveStrategy;
import com.trilogy.app.crm.move.processor.strategy.CopyMoveStrategyProxy;
import com.trilogy.app.crm.move.request.ConvertAccountGroupTypeRequest;
import com.trilogy.app.crm.move.support.AccountMoveValidationSupport;
import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.LogSupport;


/**
 * @author bdhavalshankh
 * @since 9.5.1
 */
public class ConvertAccountFieldSettingStrategy<CAGTR extends ConvertAccountGroupTypeRequest> extends CopyMoveStrategyProxy<CAGTR>
{
	private static final int SERVICE_LEVEL_DISCOUNT = -2;
    public ConvertAccountFieldSettingStrategy(CopyMoveStrategy<CAGTR> delegate)
    {
        super(delegate);
    }
    
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void validate(Context ctx, CAGTR request) throws IllegalStateException
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
    public void createNewEntity(Context ctx, CAGTR request) throws MoveException
    {
        Account newAccount = request.getNewAccount(ctx);
        
        Account originalAccount = request.getOriginalAccount(ctx);
        int numOfActivePostpaidSub = 1;
        
        Subscriber activeSubscriber = null;
        
        try
        {
            numOfActivePostpaidSub = originalAccount.getNonDeActiveSubscribers(ctx).size();
        }
        catch (HomeException e)
        {
        	String warningMessage = "Could not get total Non deactive subscriptions for account : "+originalAccount.getBAN();
             LogSupport.minor(ctx, this, warningMessage);
             throw new MoveException(request,warningMessage,e );
        }
    
        try
        {
            activeSubscriber = originalAccount.getFirstActiveSubscriber(ctx);
            if(activeSubscriber == null)
            {
                String warningMessage = "First Active subscription returned is null. ";
                LogSupport.major(ctx, this, warningMessage);
                throw new HomeException(warningMessage);
            }
        }
        catch (HomeException e)
        {
            String warningMessage = "Could not get First active subscription for account : "+originalAccount.getBAN();
            LogSupport.minor(ctx, this, warningMessage);
            throw new MoveException(request,warningMessage,e );
        }
        
        newAccount.setCreateAccountReason(MoveConstants.CONVERT_REASON);
        newAccount.setParentBAN(null);
        newAccount.setResponsible(true);
        newAccount.setResponsibleBAN(null);
        newAccount.setOldBAN(originalAccount.getBAN());
        newAccount.setTotalNumOfSubscriptions(numOfActivePostpaidSub);
        newAccount.setLastModified(new Date());
        newAccount.setLastStateChangeDate(new Date());
        newAccount.setCurrentNumPTPTransitions(Account.DEFAULT_CURRENTNUMPTPTRANSITIONS);
        newAccount.setPtpTermsTightened(Account.DEFAULT_PTPTERMSTIGHTENED);
        newAccount.setVpnMSISDN(null);
        newAccount.setOwnerMSISDN(activeSubscriber.getMsisdn());
        super.createNewEntity(ctx, request);
    }

    /**
     * @{inheritDoc}
     */
    @Override
    public void removeOldEntity(Context ctx, CAGTR request) throws MoveException
    {
        Account newAccount = request.getNewAccount(ctx);
        Account oldAccount = request.getOldAccount(ctx);
        oldAccount.setResponsible(false);
        oldAccount.setParentBAN(newAccount.getBAN());
        oldAccount.setResponsibleBAN(newAccount.getBAN());
        if (newAccount.getDiscountClass() == SERVICE_LEVEL_DISCOUNT)
        {
        	oldAccount.setDiscountClass(SERVICE_LEVEL_DISCOUNT);
        }
        else{
        	oldAccount.setDiscountClass(Account.DEFAULT_DISCOUNTCLASS);
        }
        oldAccount.setTEIC(Account.DEFAULT_TEIC);
        oldAccount.setOwnerMSISDN(null);
        
        super.removeOldEntity(ctx, request);
    }
    
}