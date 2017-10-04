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
package com.trilogy.app.crm.move.processor.factory;

import com.trilogy.app.crm.Common;
import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.move.MoveProcessor;
import com.trilogy.app.crm.move.processor.CopyMoveProcessor;
import com.trilogy.app.crm.move.processor.DefaultMoveProcessor;
import com.trilogy.app.crm.move.processor.DependencyMoveProcessor;
import com.trilogy.app.crm.move.processor.PostMoveDependencyMoveProcessor;
import com.trilogy.app.crm.move.processor.ReadOnlyMoveProcessor;
import com.trilogy.app.crm.move.processor.account.AccountAttachmentMoveProcessor;
import com.trilogy.app.crm.move.processor.account.AccountCloningMoveProcessor;
import com.trilogy.app.crm.move.processor.account.AccountConversionMoveProcessor;
import com.trilogy.app.crm.move.processor.account.AccountGroupTypeConversionMoveProcessor;
import com.trilogy.app.crm.move.processor.account.BillCycleDateCancellationMoveProcessor;
import com.trilogy.app.crm.move.processor.account.ConvertAccountGroupTypeThreadedMoveProcessor;
import com.trilogy.app.crm.move.processor.account.ERAccountConvertProcessor;
import com.trilogy.app.crm.move.processor.account.NotesAccountMoveProcessor;
import com.trilogy.app.crm.move.processor.account.OMAccountMoveProcessor;
import com.trilogy.app.crm.move.processor.account.strategy.AccountAgedDebtCopyMoveStrategy;
import com.trilogy.app.crm.move.processor.account.strategy.AccountCreditCardTokenMoveStrategy;
import com.trilogy.app.crm.move.processor.account.strategy.AccountHomeCopyMoveStrategy;
import com.trilogy.app.crm.move.processor.account.strategy.AccountIdentificationCopyMoveStrategy;
import com.trilogy.app.crm.move.processor.account.strategy.AccountInvoiceCopyMoveStrategy;
import com.trilogy.app.crm.move.processor.account.strategy.AccountMsisdnCopyMoveStrategy;
import com.trilogy.app.crm.move.processor.account.strategy.AccountPaymentTopUpScheduleMoveStrategy;
import com.trilogy.app.crm.move.processor.account.strategy.AccountSecurityQuestionCopyMoveStrategy;
import com.trilogy.app.crm.move.processor.account.strategy.BMGTVerificationAccountCopyMoveStrategy;
import com.trilogy.app.crm.move.processor.generic.strategy.SupplementaryDataCopyMoveStrategy;
import com.trilogy.app.crm.move.processor.strategy.CopyMoveStrategy;
import com.trilogy.app.crm.move.processor.strategy.LoggingCopyMoveStrategy;
import com.trilogy.app.crm.move.processor.strategy.NullCopyMoveStrategy;
import com.trilogy.app.crm.move.processor.subscription.strategy.DepositCopyMoveStrategy;
import com.trilogy.app.crm.move.request.AccountConversionRequest;
import com.trilogy.app.crm.move.request.AccountMoveRequest;
import com.trilogy.app.crm.move.request.ConvertAccountBillingTypeRequest;
import com.trilogy.app.crm.move.request.ConvertAccountGroupTypeRequest;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xlog.log.InfoLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.app.crm.bean.GroupTypeEnum;
import com.trilogy.app.crm.bean.account.AccountConversionHistoryStateEnum;
import com.trilogy.app.crm.move.processor.account.AccountCloningConvertProcessor;
import com.trilogy.app.crm.move.processor.account.AccountConvertHistoryMoveProcessor;
import com.trilogy.app.crm.move.processor.account.BaseAccountConvertProcessor;
import com.trilogy.app.crm.move.processor.account.strategy.ConvertAccountFieldSettingStrategy;


/**
 * Creates an appropriate instance of a ConvertSubscriptionMoveRequest processor.
 *
 * @author Kumaran sivasubramaniam
 * @since 8.1
 */
class ConvertAccountProcessorFactory
{
	
    static <AMR extends AccountConversionRequest> MoveProcessor<AMR> getNewInstance(Context ctx, AMR request)
    {
        boolean validationError = false;
        boolean threadingRequired = false;
        ctx.put(DepositCopyMoveStrategy.CONVERSION_REQUEST, Boolean.TRUE);
        
        Account account = request.getOriginalAccount(ctx);
        if (account == null)
        {
            new InfoLogMsg(ConvertAccountProcessorFactory.class, 
                    "Account with BAN " + request.getExistingBAN() + " does not exist.", null).log(ctx);
            validationError = true;
        }
        
        // Create move processor to store the request
        MoveProcessor<AMR> processor = new DefaultMoveProcessor<AMR>(request);

		// Added for Feature Individual to Group Account conversion during 9.5.1
        if (ConvertAccountGroupTypeRequest.class.isAssignableFrom(request.getClass()) 
        		&& ((ConvertAccountGroupTypeRequest) request).getGroupType().equals(GroupTypeEnum.GROUP) 
        		&& ((ConvertAccountGroupTypeRequest) request).getRetainOriginalAccount() 
        		&& ((ConvertAccountGroupTypeRequest) request).getMigrateOnly() == false)
        {

        	ConvertAccountGroupTypeRequest convertRequest = (ConvertAccountGroupTypeRequest) request;

        	// Move account attachments
        	processor = new AccountAttachmentMoveProcessor<AMR>(processor);

        	// Add processor to clone the old account and execute the copy logic
        	processor = new AccountCloningConvertProcessor<AMR>(processor, getConvertIndividualToGroupCopyStrategy(ctx, request));

        	processor = new BillCycleDateCancellationMoveProcessor<AMR>(processor);

        	processor = (MoveProcessor<AMR>) new BaseAccountConvertProcessor<ConvertAccountGroupTypeRequest>((MoveProcessor<ConvertAccountGroupTypeRequest>) processor);

        	// Add processors that perform actual convert logic
        	processor = (MoveProcessor<AMR>) new AccountConvertHistoryMoveProcessor<ConvertAccountGroupTypeRequest>((MoveProcessor<ConvertAccountGroupTypeRequest>) processor, 
        			AccountConversionHistoryStateEnum.CONVERSION_IN_PROGRESS_INDEX, 
        			AccountConversionHistoryStateEnum.CONVERSION_COMPLETE_MIGRATION_PENDING_INDEX, 
        			AccountConversionHistoryStateEnum.CONVERSION_FAILED_INDEX);

        	processor = (MoveProcessor<AMR>) new ERAccountConvertProcessor<ConvertAccountGroupTypeRequest>((MoveProcessor<ConvertAccountGroupTypeRequest>) processor);
        	
        	// Add processor to create appropriate Account notes
            processor = new NotesAccountMoveProcessor<AMR>(processor);
            
        	processor = new PostMoveDependencyMoveProcessor(processor);


        }
        else if (ConvertAccountGroupTypeRequest.class.isAssignableFrom(request.getClass()) 
        		&& ((ConvertAccountGroupTypeRequest) request).getGroupType().equals(GroupTypeEnum.GROUP) 
        		&& ((ConvertAccountGroupTypeRequest) request).getRetainOriginalAccount() 
        		&& ((ConvertAccountGroupTypeRequest) request).getMigrateOnly() == true)
        {
            if(LogSupport.isDebugEnabled(ctx))
            {
                LogSupport.debug(ctx, ConvertAccountProcessorFactory.class, "Creating offline data migration processor pipeline for ConvertAccountGroupTypeRequest.");
            }

            processor = new CopyMoveProcessor<AMR>(processor, getConvertIndividualToGroupDataMirgationCopyStrategy(ctx, request));
            
        	processor = (MoveProcessor<AMR>) new AccountConvertHistoryMoveProcessor<ConvertAccountGroupTypeRequest>((MoveProcessor<ConvertAccountGroupTypeRequest>) processor,
        			AccountConversionHistoryStateEnum.CONVERSION_COMPLETE_MIGRATION_PROGRESS_INDEX, 
        			AccountConversionHistoryStateEnum.CONVERSION_COMPLETE_MIGRATION_COMPLETE_INDEX, 
        			AccountConversionHistoryStateEnum.CONVERSION_COMPLETE_MIGRATION_FAILED_INDEX);
        	
        	processor = (MoveProcessor<AMR>) new ERAccountConvertProcessor<ConvertAccountGroupTypeRequest>((MoveProcessor<ConvertAccountGroupTypeRequest>) processor);
        	
        	// Add processor to create appropriate Account notes
            processor = new NotesAccountMoveProcessor<AMR>(processor);
            
        	threadingRequired = true;
        	
        }
        else
        {

        	// Add processors that perform actual move logic

        	// Add dependent entity (e.g. subscriptions) processor to end of pipeline
        	// See {@link com.redknee.app.crm.move.dependency.AccountMoveDependencyManager}) for dependencies.
        	processor = new DependencyMoveProcessor<AMR>(processor);

        	// Move account attachments
        	processor = new AccountAttachmentMoveProcessor<AMR>(processor);

        	// Add processor to clone the old account and execute the copy logic
        	processor = new AccountCloningMoveProcessor<AMR>(processor, getCopyStrategy(ctx, request));

        	if (ConvertAccountBillingTypeRequest.class.isAssignableFrom(request.getClass()))
        	{
        		processor = (MoveProcessor<AMR>) new AccountConversionMoveProcessor<ConvertAccountBillingTypeRequest>((MoveProcessor<ConvertAccountBillingTypeRequest>) processor);
        	}

        	if (ConvertAccountGroupTypeRequest.class.isAssignableFrom(request.getClass()))
        	{
        		processor = (MoveProcessor<AMR>) new AccountGroupTypeConversionMoveProcessor<ConvertAccountGroupTypeRequest>((MoveProcessor<ConvertAccountGroupTypeRequest>) processor);
        	}
        	
        	// Add processor to create appropriate Account notes
            processor = new NotesAccountMoveProcessor<AMR>(processor);
            
        }
        

        // Add processor to peg Account conversion OMs
        processor = new OMAccountMoveProcessor<AMR>(
                processor, 
                Common.OM_ACCT_CONVERSION_ATTEMPT,
                Common.OM_ACCT_CONVERSION_SUCCESS,
                Common.OM_ACCT_CONVERSION_FAIL);
        
        //This should always be the last wrapper in the chain.
        if(threadingRequired == true)
        {
        	processor = (MoveProcessor<AMR>) new ConvertAccountGroupTypeThreadedMoveProcessor<ConvertAccountGroupTypeRequest>((MoveProcessor<ConvertAccountGroupTypeRequest>) processor);
        }
        
        if (validationError)
        {
            new InfoLogMsg(ConvertAccountProcessorFactory.class, 
                    "Error occurred while creating a move processor for request " + request
                    + ".  Returning a read-only move processor so that validation can be run.", null).log(ctx);
            processor = new ReadOnlyMoveProcessor<AMR>(
                    processor,
                    "Error occurred while creating a move processor for request " + request.toString());
        }
        
        return processor;
    }
    
    
    private static <AMR extends AccountMoveRequest> CopyMoveStrategy<AMR> getCopyStrategy(Context ctx, AMR request)
    {
        // Create copy-style move logic pipeline
        CopyMoveStrategy<AMR> copyStrategy = null;

        // Add copy logic to perform create/store Home operations on new/old copies of accounts
        copyStrategy = new AccountHomeCopyMoveStrategy<AMR>(Boolean.FALSE);

        // Handle MSISDN Management related logic related to the move
        copyStrategy = new AccountMsisdnCopyMoveStrategy<AMR>(copyStrategy);

        // Add logic to copy the account's transient identification fields properly
        copyStrategy = new AccountIdentificationCopyMoveStrategy<AMR>(copyStrategy);

        // Add logic to copy the account's transient security question/answer fields properly
        copyStrategy = new AccountSecurityQuestionCopyMoveStrategy<AMR>(copyStrategy);
        
        // Add logic to copy the account's supplementary data properly
        copyStrategy = new SupplementaryDataCopyMoveStrategy<AMR>(copyStrategy, request);

        copyStrategy = new BMGTVerificationAccountCopyMoveStrategy<AMR>(copyStrategy);

        // Add basic logging for all copy-style move requests
        copyStrategy = new LoggingCopyMoveStrategy<AMR>(copyStrategy);
        
        return copyStrategy;
    }
    
    private static <AMR extends AccountMoveRequest> CopyMoveStrategy<AMR> getConvertIndividualToGroupCopyStrategy(Context ctx, AMR request)
    {
        // Create copy-style move logic pipeline
        CopyMoveStrategy<AMR> copyStrategy = null;

        // Add copy logic to perform create/store Home operations on new/old copies of accounts
        copyStrategy = new AccountHomeCopyMoveStrategy<AMR>(Boolean.TRUE);
        
        copyStrategy = (CopyMoveStrategy<AMR>) new ConvertAccountFieldSettingStrategy<ConvertAccountGroupTypeRequest>((CopyMoveStrategy<ConvertAccountGroupTypeRequest>) copyStrategy);

        // Add logic to copy the account's transient identification fields properly
        copyStrategy = new AccountIdentificationCopyMoveStrategy<AMR>(copyStrategy);

        // Add logic to copy the account's transient security question/answer fields properly
        copyStrategy = new AccountSecurityQuestionCopyMoveStrategy<AMR>(copyStrategy);
        
        // Add logic to copy the account's supplementary data properly
        copyStrategy = new SupplementaryDataCopyMoveStrategy<AMR>(copyStrategy, request);

        return copyStrategy;
    }
    
    private static <AMR extends AccountMoveRequest> CopyMoveStrategy<AMR> getConvertIndividualToGroupDataMirgationCopyStrategy(Context ctx, AMR request)
    {
        // Create copy-style move logic pipeline
        CopyMoveStrategy<AMR> copyStrategy = NullCopyMoveStrategy.instance();
        copyStrategy = new AccountInvoiceCopyMoveStrategy<AMR>(copyStrategy);
        copyStrategy = new AccountAgedDebtCopyMoveStrategy<AMR>(copyStrategy);
        copyStrategy = new AccountCreditCardTokenMoveStrategy<AMR>(copyStrategy);
        copyStrategy = new AccountPaymentTopUpScheduleMoveStrategy<AMR>(copyStrategy);

        return copyStrategy;
    }
    
}
