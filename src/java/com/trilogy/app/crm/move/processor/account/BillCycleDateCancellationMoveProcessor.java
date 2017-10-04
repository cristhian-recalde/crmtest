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

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.BillCycleChangeStatusEnum;
import com.trilogy.app.crm.bean.BillCycleHistory;
import com.trilogy.app.crm.home.UrcsUpdatingBillCycleHistoryHome;
import com.trilogy.app.crm.move.CompoundMoveIllegalSateException;
import com.trilogy.app.crm.move.MoveException;
import com.trilogy.app.crm.move.MoveProcessor;
import com.trilogy.app.crm.move.MoveWarningException;
import com.trilogy.app.crm.move.processor.MoveProcessorProxy;
import com.trilogy.app.crm.move.request.AccountConversionRequest;
import com.trilogy.app.crm.move.support.AccountMoveValidationSupport;
import com.trilogy.app.crm.support.BillCycleHistorySupport;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xlog.log.LogSupport;

/**
 * This class will take care cancelling BC History records in pending state.
 *
 * @author suyash.gaidhani@redknee.com
 * @since 9.5.1
 */
public class BillCycleDateCancellationMoveProcessor<ACR extends AccountConversionRequest> extends MoveProcessorProxy<ACR>
{

    public BillCycleDateCancellationMoveProcessor(MoveProcessor<ACR> delegate)
    {
        super(delegate);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void validate(Context ctx) throws IllegalStateException
    {
    	CompoundMoveIllegalSateException cise = new CompoundMoveIllegalSateException();
        
        ACR request = this.getRequest();

        Account account = AccountMoveValidationSupport.validateOldAccountExists(ctx, request, cise);
        
        if(!account.isResponsible())
        {
        	String errorMessage = "Not allowed to convet non-responsible account " + request.getExistingBAN();
        	cise.thrown(CompoundMoveIllegalSateException.ACCOUNT_NOT_RESPONSIBLE, errorMessage, new IllegalStateException(
                    errorMessage));
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
        ACR request = this.getRequest();
        
        Account originalAccount = request.getOriginalAccount(ctx);
        
        BillCycleHistory lastEvent = BillCycleHistorySupport.getLastEvent(ctx, originalAccount.getBAN());
        if (lastEvent != null 
                && BillCycleChangeStatusEnum.PENDING.equals(lastEvent.getStatus()))
        {
                    cancelPendingBillCycleChange(ctx, request);
                    
                    cancelDatabaseRecords(ctx, originalAccount, lastEvent, request);
        }
        else
        {
        	request.reportStatusMessages(ctx,"BillCycleChangeRecordCancellation" + " "+ "Nothing to Cancel.  ");
        }
        
        
        super.move(ctx);
    }

	private void cancelDatabaseRecords(Context ctx, Account originalAccount,
			BillCycleHistory lastEvent, ACR request) throws MoveException{
		lastEvent.setStatus(BillCycleChangeStatusEnum.CANCELLED);
		lastEvent.setFailureMessage("Account " + originalAccount.getBAN() + " was converted to Group Account and triggered cancellation of pending bill cycle change for account " + lastEvent.getBAN());
		try 
		{
			lastEvent = HomeSupportHelper.get(ctx).storeBean(ctx, lastEvent);
			request.reportStatusMessages(ctx,"BillCycleChangeRecordCancellation" + " "+ "Passed. ");
		
		} 
		catch (HomeException e) 
		{

        	String warningMesssage = "Unable to cancel pending bill cycle change in DB. " + request.getExistingBAN();
        	
        	LogSupport.minor(ctx, this, warningMesssage, e);
        	
            request.reportStatusMessages(ctx,"BillCycleChangeRecordCancellation" + " "+ "Failed. ");
            throw new MoveException(request, warningMesssage, e);
        
			
		}
	}

    protected void cancelPendingBillCycleChange(Context ctx, ACR request) throws MoveException
    {
        try
        {
            UrcsUpdatingBillCycleHistoryHome.cancelPendingChangeInURCS(ctx, request.getExistingBAN());
            
        }
        catch (HomeException e)
        {
        	String warningMesssage = "Unable to cancel pending bill cycle change in URCS for account " + request.getExistingBAN();
        	
        	LogSupport.minor(ctx, this, warningMesssage, e);
        	
            request.reportStatusMessages(ctx,"BillCycleChangeRecordCancellation" + " "+ "Failed. ");
            throw new MoveException(request, warningMesssage, e);
        }
    }
}
