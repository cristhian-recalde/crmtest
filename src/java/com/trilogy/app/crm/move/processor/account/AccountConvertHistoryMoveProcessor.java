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

import java.util.Collection;
import java.util.Date;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.account.AccountConversionHistory;
import com.trilogy.app.crm.bean.account.AccountConversionHistoryHome;
import com.trilogy.app.crm.bean.account.AccountConversionHistoryXInfo;
import com.trilogy.app.crm.bean.account.AccountConversionHistoryStateEnum;
import com.trilogy.app.crm.move.MoveException;
import com.trilogy.app.crm.move.CompoundMoveIllegalSateException;
import com.trilogy.app.crm.move.MoveProcessor;
import com.trilogy.app.crm.move.processor.MoveProcessorProxy;
import com.trilogy.app.crm.move.request.AccountMoveRequestXInfo;
import com.trilogy.app.crm.move.request.ConvertAccountGroupTypeRequest;
import com.trilogy.app.crm.move.support.AccountMoveValidationSupport;
import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.beans.XBeans;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.Limit;
import com.trilogy.framework.xhome.elang.Order;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.LogSupport;


/**
 *
 * @author bdhavalshankh
 * @since 9.5.1
 */
public class AccountConvertHistoryMoveProcessor<CAGTR extends ConvertAccountGroupTypeRequest> extends MoveProcessorProxy<CAGTR>
{
    private int inProgress_;
    private int complete_;
    private int failed_;
    
    public AccountConvertHistoryMoveProcessor(MoveProcessor<CAGTR> delegate, int inProgress, int complete, int failed)
    {
        super(delegate);
        this.inProgress_ = inProgress;
        this.complete_ = complete;
        this.failed_ = failed;
                
    }
    
    @Override
    public Context setUp(Context ctx) throws MoveException
    {
        
        CAGTR request = this.getRequest();
        if(request.getInitiatedOn() == null)
        {
            request.setInitiatedOn(new Date());
        }
        return super.setUp(ctx);
    } 
    
    @Override
    public void validate(Context ctx) throws IllegalStateException
    {
        
        CompoundMoveIllegalSateException cise = new CompoundMoveIllegalSateException();

        CAGTR request = this.getRequest();
        
        AccountMoveValidationSupport.validateOldAccountExists(ctx, request, cise);
        
        validateDuplicateRequest(ctx, request, cise);
        
        cise.throwAll();

        super.validate(ctx);
    }
    
    /**
     * @{inheritDoc}
     */
    @Override
    public void move(Context ctx) throws MoveException
    {
        CAGTR request = this.getRequest();
        try
        {
            createOrStoreHistory(ctx, request, inProgress_);
            
            super.move(ctx);
            
            if(request.getMigrateOnly())
            {
            	if(request.getWarnings(ctx).size() > 0)
            	{
            		createOrStoreHistory(ctx, request, failed_);
            	}
            	else
            	{
            		createOrStoreHistory(ctx, request, complete_);            		
            	}
            	
            }
            else
            {
            	createOrStoreHistory(ctx, request, complete_);
            }
        }
        catch (MoveException me)
        {
            createOrStoreHistory(ctx, request, failed_);
            throw me;
        }  
        
    }

    private void createOrStoreHistory(Context ctx, CAGTR request, int status) throws MoveException 
    {

        Account newAccount = request.getNewAccount(ctx);
        Account oldAccount = request.getOldAccount(ctx);

        Date initiatedOn =  request.getInitiatedOn();

        Home home = (Home) ctx.get(AccountConversionHistoryHome.class);

        try 
        {
            AccountConversionHistory history = (AccountConversionHistory) home.find(ctx, new EQ(AccountConversionHistoryXInfo.INITIATED_ON, initiatedOn));
            if(history == null)
            {
                //This will not be null.
                history = (AccountConversionHistory) XBeans.instantiate(AccountConversionHistory.class, ctx);

                history.setInitiatedOn(initiatedOn);
                history.setNewGroupBan(newAccount.getBAN());
                history.setOriginalBan(oldAccount.getBAN());
                history.setOriginalGroupType(oldAccount.getGroupType().getIndex());
                history.setOriginalSubscriberType(oldAccount.getSubscriberType().getIndex());
                history.setResponsiblebefore(oldAccount.getResponsible());
                history.setSpid(oldAccount.getSpid());
                history.setConversionStatus(status);
                
                home.create(ctx, history);
            }
            else
            {
                history.setConversionStatus(status);
                history.setNewGroupBan(newAccount.getBAN());
                history.setNewgrouptype(newAccount.getGroupType().getIndex());
                history.setNewSubscriberType(newAccount.getSubscriberType().getIndex());
                history.setResponsibleafter(oldAccount.getResponsible());
                
                home.store(ctx, history);
            }
        }
        catch (Exception e) 
        {
            String warningMessage = "Unable to create/update AccountConversionHistory record.";
            LogSupport.minor(ctx, this, warningMessage, e);
            throw new MoveException(request, warningMessage,e);
        }
    }


    private void validateDuplicateRequest(Context ctx, CAGTR request, CompoundMoveIllegalSateException cise) 
    {
        Home home = (Home) ctx.get(AccountConversionHistoryHome.class);
        And and = new And();
        and.add(new EQ(AccountConversionHistoryXInfo.ORIGINAL_BAN, request.getOldBAN()));
        and.add(new Order().add(AccountConversionHistoryXInfo.INITIATED_ON, false));
        and.add(new Limit(1));
        Collection historyRecords = null;
        try
        {
            historyRecords = home.where(ctx, and).selectAll();
        }
        catch (HomeException e)
        {
            cise.thrown(new IllegalStateException("HomeException encountered while trying to fetch AccountConversionHistory recoeds for BAN :" 
                    + request.getExistingBAN() 
                    + ". Unable to validate request.", e));
        }
        
        if(historyRecords  != null && historyRecords.size() > 0)
        {
            //Ideally there should be only one record.
            for (Object object : historyRecords)
            {
                AccountConversionHistory history  = (AccountConversionHistory) object;
                if(history != null)
                {
                    if(history.getConversionStatus() == AccountConversionHistoryStateEnum.CONVERSION_IN_PROGRESS_INDEX)
                    {
                        String msg = "Can not initiate request. One request is already processing";
                        cise.thrown(CompoundMoveIllegalSateException.REQUEST_ALREADY_PROCESSING, msg, new IllegalPropertyArgumentException(AccountMoveRequestXInfo.EXISTING_BAN, msg));
                    }
                }
            }
        }
    }
}
