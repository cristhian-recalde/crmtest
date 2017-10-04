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

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.move.MoveException;
import com.trilogy.app.crm.move.MoveProcessor;
import com.trilogy.app.crm.move.processor.DefaultMoveProcessor;
import com.trilogy.app.crm.move.processor.DependencyMoveProcessor;
import com.trilogy.app.crm.move.processor.ReadOnlyMoveProcessor;
import com.trilogy.app.crm.move.processor.account.AccountAttachmentMoveProcessor;
import com.trilogy.app.crm.move.processor.account.AccountCloningMoveProcessor;
import com.trilogy.app.crm.move.processor.account.AccountResponsibilitySettingMoveProcessor;
import com.trilogy.app.crm.move.processor.account.AccountUpdateMoveProcessor;
import com.trilogy.app.crm.move.processor.account.BaseAccountMoveProcessor;
import com.trilogy.app.crm.move.processor.account.BillCycleDateChangeMoveProcessor;
import com.trilogy.app.crm.move.processor.account.ClosedUserGroupAccountMoveProcessor;
import com.trilogy.app.crm.move.processor.account.NotesAccountMoveProcessor;
import com.trilogy.app.crm.move.processor.account.OMAccountMoveProcessor;
import com.trilogy.app.crm.move.processor.account.ResponsibleAccountMoveProcessor;
import com.trilogy.app.crm.move.processor.account.ResponsibleVPNMemberAccountMoveProcessor;
import com.trilogy.app.crm.move.processor.account.strategy.AccountHomeCopyMoveStrategy;
import com.trilogy.app.crm.move.processor.account.strategy.AccountIdentificationCopyMoveStrategy;
import com.trilogy.app.crm.move.processor.account.strategy.AccountMsisdnCopyMoveStrategy;
import com.trilogy.app.crm.move.processor.account.strategy.AccountSecurityQuestionCopyMoveStrategy;
import com.trilogy.app.crm.move.processor.account.strategy.BMGTVerificationAccountCopyMoveStrategy;
import com.trilogy.app.crm.move.processor.account.strategy.ParentAccountCopyMoveStrategy;
import com.trilogy.app.crm.move.processor.account.strategy.ResponsibleStateChangingCopyMoveStrategy;
import com.trilogy.app.crm.move.processor.account.strategy.ServiceLevelDiscountClassCopyMoveStrategy;
import com.trilogy.app.crm.move.processor.generic.strategy.SupplementaryDataCopyMoveStrategy;
import com.trilogy.app.crm.move.processor.strategy.CopyMoveStrategy;
import com.trilogy.app.crm.move.processor.strategy.LoggingCopyMoveStrategy;
import com.trilogy.app.crm.move.request.AccountMoveRequest;
import com.trilogy.app.crm.move.support.VPNSubscriptionMoveSupport;
import com.trilogy.framework.xhome.beans.SafetyUtil;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.InfoLogMsg;


/**
 * Creates an appropriate instance of an AccountMoveRequest processor.
 *
 * @author Aaron Gourley
 * @since 8.1
 */
class AccountMoveProcessorFactory
{
    static <AMR extends AccountMoveRequest> MoveProcessor<AMR> getNewInstance(Context ctx, AMR request)
    {
        boolean validationError = false;
        
        Account account = request.getOriginalAccount(ctx);
        if (account == null)
        {
            new InfoLogMsg(AccountMoveProcessorFactory.class, 
                    "Account with BAN " + request.getExistingBAN() + " does not exist.", null).log(ctx);
            validationError = true;
        }

        // Create move processor to store the request
        MoveProcessor<AMR> processor = new DefaultMoveProcessor<AMR>(request);
        
        // Only execute account move logic if the parent account is changing.
            // Add processors that perform actual move logic
            try
            {
                boolean isResponsible = isResponsible(ctx, request, account);
                boolean willBeResponsible = request.getNewResponsible();
                boolean responsibleStateChange = isResponsible != willBeResponsible;
                if (account == null || responsibleStateChange
                        || !SafetyUtil.safeEquals(account.getParentBAN(), request.getNewParentBAN()) || !SafetyUtil.safeEquals(account.getParentBAN(), request.getNewParentBAN()))
                {
                    
                    if (!isResponsible || !willBeResponsible)
                    {
                    	// Add dependent entity (e.g. subscriptions) processor to end of pipeline
                        // See {@link com.redknee.app.crm.move.dependency.AccountMoveDependencyManager}) for dependencies.
                        processor = new DependencyMoveProcessor<AMR>(processor);
                        
                        // Move account attachments
                        processor = new AccountAttachmentMoveProcessor<AMR>(processor);
                        
                     // Add logic to copy the account's transient discount class fields properly
                        processor = new ServiceLevelDiscountClassCopyMoveStrategy<AMR>(processor);
                        
                        // Add processor to clone the old account and execute the copy logic
                        processor = new AccountCloningMoveProcessor<AMR>(processor, getCopyStrategy(ctx, request));
                        
                        if (!request.getNewResponsible() && (
                                (request.getNewAccount(ctx) != null && !request.getNewAccount(ctx).isPrepaid()) || 
                                (request.getNewAccount(ctx) == null && !request.getOriginalAccount(ctx).isPrepaid())
                                )
                           )  
                        {
                            //Normal move requests need a parent BAN specified for non-responsible or VPN account moves
                            Account newParentAccount = request.getNewParentAccount(ctx);
                            if (newParentAccount == null
                                    || !newParentAccount.isBANSet())
                            {
                                new InfoLogMsg(AccountMoveProcessorFactory.class, 
                                        "New parent BAN not set properly.  This move request can be validated but not executed.", null).log(ctx);
                                validationError = true;
                            }
                        }
                    }
                    else
                    {
                        // Add processor to store the account to the Home
                        processor = new AccountUpdateMoveProcessor<AMR>(processor);
                        
                        final VPNSubscriptionMoveSupport<AMR> vpnMoveSupport = new VPNSubscriptionMoveSupport<AMR>(ctx, request, true);
                        if (vpnMoveSupport.getOldRootAccount() == null
                                || vpnMoveSupport.getNewRootAccount() == null
                                || vpnMoveSupport.isVPNAccountChange())
                        {
                            // Add processor to subscriptions into, out of, or between VPN accounts
                            processor = new ResponsibleVPNMemberAccountMoveProcessor<AMR>(processor);
                        }
                        
                        // Add processor to update account fields to reference new account
                        // (also to perform responsible account specific validation)
                        processor = new ResponsibleAccountMoveProcessor<AMR>(processor);
                        
                     // Add logic to copy the account's transient discount class fields properly
                        processor = new ServiceLevelDiscountClassCopyMoveStrategy<AMR>(processor);
                        
                        // ADD/Remove cug for account
                        processor = new ClosedUserGroupAccountMoveProcessor<AMR>(processor);
                        
                        // Add processor to clone the old account.  This is to ensure that old and new state is available
                        // to downstream processors.
                        processor = new AccountCloningMoveProcessor<AMR>(processor);
                        
                    }
                }
            }
            catch (MoveException e)
            {
                new InfoLogMsg(AccountMoveProcessorFactory.class, e.getMessage(), e).log(ctx);
                validationError = true;
            }
            
            // Propogate/cancel bill cycle changes as necessary
            processor = new BillCycleDateChangeMoveProcessor<AMR>(processor);

            // Add processor to create appropriate Account notes
            processor = new NotesAccountMoveProcessor<AMR>(processor);

            // Add processor to peg Account move OMs
            processor = new OMAccountMoveProcessor<AMR>(processor); 

            // Add processor to perform common business logic validation
            processor = new BaseAccountMoveProcessor<AMR>(processor);
            
            // Add processor to perform logic for setting responsible flag for account while performing account move
            processor = new AccountResponsibilitySettingMoveProcessor<AMR>(processor);
        
        if (validationError)
        {
            new InfoLogMsg(AccountMoveProcessorFactory.class, 
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

        copyStrategy = new ResponsibleStateChangingCopyMoveStrategy<AMR>(copyStrategy);
        
        // Add copy logic to update fields of the old & new accounts
        // (also to perform non-responsible account specific validation)
        copyStrategy = new ParentAccountCopyMoveStrategy<AMR>(copyStrategy);  
        
        copyStrategy = new BMGTVerificationAccountCopyMoveStrategy<AMR>(copyStrategy);

        // Add basic logging for all copy-style move requests
        copyStrategy = new LoggingCopyMoveStrategy<AMR>(copyStrategy);
        
        return copyStrategy;
    }

    private static <AMR extends AccountMoveRequest> boolean isResponsible(Context ctx, AMR request, Account account) throws MoveException
    {
        boolean result = false;

        if (account != null
                && account.isResponsible())
        {
            result = true;
        }
        else
        {
            Account newParentAccount = request.getNewParentAccount(ctx);
            if (newParentAccount != null && account != null)
            {
                try
                {
                    Account responsibleParentAccount = account.getResponsibleParentAccount(ctx);
                    if (responsibleParentAccount != null)
                    {

                        Account newResponsibleParentAccount = newParentAccount.getResponsibleParentAccount(ctx);
                        if (newResponsibleParentAccount != null)
                        {
                            if (SafetyUtil.safeEquals(responsibleParentAccount.getBAN(), newResponsibleParentAccount.getBAN()))
                            {
                                result = true;
                            }
                        }
                        else
                        {
                            throw new MoveException(request, "Responsible parent account not found for account " + newParentAccount.getBAN());
                        }
                    }
                    else
                    {
                        throw new MoveException(request, "Responsible parent account not found for account " + account.getBAN());
                    }
                }
                catch (HomeException e)
                {
                    throw new MoveException(request, "Error retrieving responsible parent accounts for account " + account.getBAN() + " or " + newParentAccount.getBAN(), e);
                } 
            }
        }

        return result;
    }
}
