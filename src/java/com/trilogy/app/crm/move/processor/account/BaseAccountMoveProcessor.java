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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.beans.SafetyUtil;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.In;
import com.trilogy.framework.xhome.elang.Or;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.visitor.CountingVisitor;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MajorLogMsg;

import com.trilogy.app.crm.Common;
import com.trilogy.app.crm.account.AccountConstants;
import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountCategoryHome;
import com.trilogy.app.crm.bean.AccountCategoryXInfo;
import com.trilogy.app.crm.bean.AccountStateEnum;
import com.trilogy.app.crm.bean.AccountXInfo;
import com.trilogy.app.crm.bean.BillCycle;
import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.CreditCategory;
import com.trilogy.app.crm.bean.CustomerTypeEnum;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.bean.core.AccountCategory;
import com.trilogy.app.crm.home.AccountFamilyPlanHome;
import com.trilogy.app.crm.home.account.AccountHomePipelineFactory;
import com.trilogy.app.crm.move.MoveConstants;
import com.trilogy.app.crm.move.MoveException;
import com.trilogy.app.crm.move.MoveProcessor;
import com.trilogy.app.crm.move.processor.DependencyMoveProcessor;
import com.trilogy.app.crm.move.processor.MoveProcessorProxy;
import com.trilogy.app.crm.move.request.AccountMoveRequest;
import com.trilogy.app.crm.move.request.AccountMoveRequestXInfo;
import com.trilogy.app.crm.move.support.AccountMoveValidationSupport;
import com.trilogy.app.crm.support.AccountSupport;
import com.trilogy.app.crm.support.EnumStateSupportHelper;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.app.crm.support.PaymentPlanSupportHelper;
import com.trilogy.app.crm.transfer.TransferDisputeHome;
import com.trilogy.app.crm.transfer.TransferDisputeStatusEnum;
import com.trilogy.app.crm.transfer.TransferDisputeXInfo;
import com.trilogy.app.crm.web.border.AccountIdentificationValidator;
import com.trilogy.app.crm.home.account.AccountHierachyValidator;


/**
 * This processor is responsible for performing all validation that applies
 * to ANY account move scenario.  It is also responsible for performing any
 * setup that is common to ANY account move scenario.  Any validation
 * that is specific to a subset of move scenarios should be implemented in
 * a different processor.
 * 
 * It does not implement any account move business logic, modify the request,
 * or modify the accounts involved.
 *
 * @author Aaron Gourley
 * @since 8.1
 */
public class BaseAccountMoveProcessor<AMR extends AccountMoveRequest> extends MoveProcessorProxy<AMR>
{
    public BaseAccountMoveProcessor(AMR request)
    {
        super(new DependencyMoveProcessor<AMR>(request));
    }
    
    public BaseAccountMoveProcessor(MoveProcessor<AMR> delegate)
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

        AMR request = this.getRequest();

        Home cachedHome = (Home) ctx.get(Common.ACCOUNT_CACHED_HOME);
        try
        {
            final Context serverCtx = moveCtx.createSubContext("RMI Server Context");

            // this gives us access to the RMIHomeServers and the context that is passed by BAS to the homes.
            moveCtx.put("RMI Server Context",serverCtx);

            moveCtx.put(
                    MoveConstants.CUSTOM_ACCOUNT_HOME_CTX_KEY, 
                    AccountHomePipelineFactory.instance().decorateMoveHome(cachedHome, moveCtx, serverCtx));
        }
        catch (Exception e)
        {
            throw new MoveException(request, "Error creating custom move pipeline for account storage operations.", e);
        }

        moveCtx.put(AccountConstants.OLD_ACCOUNT, request.getOldAccount(moveCtx));

        return moveCtx;
    }

    /**
     * @{inheritDoc}
     */
    @Override
    public void validate(Context ctx) throws IllegalStateException
    {
        CompoundIllegalStateException cise = new CompoundIllegalStateException();

        if (!ctx.has(Common.ACCOUNT_CACHED_HOME))
        {
            cise.thrown(new IllegalStateException(
                    "Cached account home not installed in context (key=" + Common.ACCOUNT_CACHED_HOME + ")."));
        }

        if (!ctx.has(MoveConstants.CUSTOM_ACCOUNT_HOME_CTX_KEY))
        {
            cise.thrown(new IllegalStateException(
                "Custom account home not installed in context."));
        }
        
        // Validate that the account that we are moving exists and is in a valid state.
        validateAccountToMove(ctx, cise);
        
        // Validate that the new parent account exists and that it is a valid candidate for the moving account's new parent account.
        validateNewParentAccount(ctx, cise);

        // Validate that the new parent account exists and that it is a valid candidate for the moving account's new parent account.
        validateNewHierarchy(ctx, cise);

        // Verify that account and new parent account belong to the same service provider and have the same role,
        validateAccountSpid(ctx, cise);
        validateAccountRole(ctx, cise);

        // Verify that new parent account doesn't exceed the maximum number of child account limit set on extensions.
        validateSubAccountLimitInParent(ctx, cise);
        
        // Verify that the moving account's system type is compatible with the new parent account's system type.
        validateSystemType(ctx, cise);
        
        // Verify that the moving account and new parent account have compatible bill cycles.
        validateBillCycleDate(ctx, cise);
        
        // Verify that the moving account has no open transfer disputes.
        validateTransferDisputes(ctx, cise);
        
        //Verify that the account being moved into has not crossed the maximum limit allowed for Subscriptions count.
        validateCreditCategory(ctx, cise);
        
        cise.throwAll();

        // Don't bother letting the delegate validate unless this processor has passed.
        super.validate(ctx);
    }
    
    /**
     * Validate that the account that we are moving exists and is in a valid state.
     * 
     * @param ctx Move context
     * @param cise Container for validation errors
     */
    private void validateAccountToMove(Context ctx, CompoundIllegalStateException cise)
    {
        AMR request = this.getRequest();

        Account account = AccountMoveValidationSupport.validateOldAccountExists(ctx, request, cise);
        if (account != null)
        {
            if (!EnumStateSupportHelper.get(ctx).isOneOfStates(account, 
                    AccountStateEnum.ACTIVE,
                    AccountStateEnum.SUSPENDED,
                    AccountStateEnum.PROMISE_TO_PAY))
            {
                cise.thrown(new IllegalPropertyArgumentException(
                        AccountMoveRequestXInfo.EXISTING_BAN, 
                        "Account (BAN=" + request.getExistingBAN() + ") is in an invalid state (" + account.getState() + ")."));
            }
            else if (PaymentPlanSupportHelper.get(ctx).isEnabled(ctx))
            {
                try
                {
                    Account parentAccount = account.getResponsibleParentAccount(ctx);
                    if (parentAccount != null
                            && PaymentPlanSupportHelper.get(ctx).isValidPaymentPlan(ctx, parentAccount.getPaymentPlan()))
                    {
                        //TODO: Deny only if the given account has the only active postpaid subscriber(s) in the payment plan
                        //      This can only be done if the AmountOwingTransferCopyMoveStrategy accounts for payment plan related
                        //      balance transfers
                        //Hint: Use AccountSupport.getNumberOfActivePostpaidSubscribersInTopology(ctx, parentAccount)
                        cise.thrown(new IllegalPropertyArgumentException(
                                AccountMoveRequestXInfo.EXISTING_BAN,
                                "Account (BAN=" + request.getExistingBAN() + ") can't be moved because it is enrolled in Payment Plan "
                                + "(Payment Plan BAN=" + parentAccount.getBAN() + ")."));
                    }
                }
                catch (HomeException e)
                {
                    cise.thrown(new IllegalPropertyArgumentException(
                            AccountMoveRequestXInfo.NEW_PARENT_BAN,
                            "Account (BAN=" + request.getExistingBAN() + ") can't be moved because an error "
                            + " occurred retrieving its responsible parent account.  Unable to validate Payment Plan restrictions."));
                }
            }
        }
    }

    /**
     * Validate that the new parent account exists and that it is a valid candidate for the moving account's new parent account.
     * 
     * @param ctx Move context
     * @param cise Container for validation errors
     */
    private void validateNewParentAccount(Context ctx, CompoundIllegalStateException cise)
    {
        AMR request = this.getRequest();
        
        Account newParentAccount = request.getNewParentAccount(ctx);
        if (newParentAccount == null)
        {
            Account newAccount = request.getNewAccount(ctx);
            if (newAccount == null || (!newAccount.isResponsible() && !newAccount.isPrepaid()))
            {
                cise.thrown(new IllegalPropertyArgumentException(
                        AccountMoveRequestXInfo.NEW_PARENT_BAN, 
                        "New parent account (BAN=" + request.getNewParentBAN() + ") does not exist."));   
            }
        }
        else if (EnumStateSupportHelper.get(ctx).stateEquals(newParentAccount, AccountStateEnum.INACTIVE))
        {
            cise.thrown(new IllegalPropertyArgumentException(
                    AccountMoveRequestXInfo.NEW_PARENT_BAN, 
                    "New parent account (BAN=" + request.getNewParentBAN() + ") is in an invalid state " + newParentAccount.getState() + "."));
        }
        else if (newParentAccount.isIndividual(ctx))
        {
            cise.thrown(new IllegalPropertyArgumentException(
                    AccountMoveRequestXInfo.NEW_PARENT_BAN, 
                    "New parent account (BAN=" + request.getNewParentBAN() + ") is an individual account."));
        }
    }

    /**
     * Verify that the proposed move will not corrupt the account hierarchy.
     * 
     * @param ctx Move context
     * @param cise Container for validation errors
     */
    private void validateNewHierarchy(Context ctx, CompoundIllegalStateException cise)
    {
        AMR request = this.getRequest();
        
        Account account = request.getOriginalAccount(ctx);        
        if (account != null)
        {
            if (SafetyUtil.safeEquals(account.getBAN(), request.getNewParentBAN()))
            {
                cise.thrown(new IllegalPropertyArgumentException(
                        AccountMoveRequestXInfo.NEW_PARENT_BAN, 
                        "Can't move account (BAN=" + request.getExistingBAN() + ") to itself.  This move would create a cycle in the account hierarchy."));
            }
            
            if (!account.isResponsible()
                    && SafetyUtil.safeEquals(account.getParentBAN(), request.getNewParentBAN()))
            {
                cise.thrown(new IllegalPropertyArgumentException(
                        AccountMoveRequestXInfo.NEW_PARENT_BAN, 
                        "Account (BAN=" + request.getExistingBAN() + ") is already an immediate child of the new parent account (BAN=" + request.getNewParentBAN() + ")."));
            }
            
            try
            {
                Account newParentAccount = request.getNewParentAccount(ctx);
                if (newParentAccount != null
                        && AccountSupport.isDecendentAccount(ctx, account, newParentAccount))
                {
                    cise.thrown(new IllegalPropertyArgumentException(
                            AccountMoveRequestXInfo.NEW_PARENT_BAN, 
                            "New parent account (BAN=" + request.getNewParentBAN() + ") is a descendent of the existing account (BAN=" + request.getExistingBAN() + ").  This move would create a cycle in the account hierarchy."));
                }
            }
            catch (HomeException e)
            {
                String msg = "Error occurred while trying to detect a cycle in the new account hierarchy.";
                new MajorLogMsg(this, msg, e).log(ctx);
                cise.thrown(new IllegalPropertyArgumentException(AccountMoveRequestXInfo.NEW_PARENT_BAN, msg));
            }
            
            try
            {
                final Account parentAccount = request.getNewParentAccount(ctx);
                final Account newAccount = request.getNewAccount(ctx);
                if (parentAccount != null && parentAccount.isPooled(ctx))
                {
                    int numAcct = -1;
                    try
                    {
                        numAcct = AccountSupport.getImmediateChildrenActiveAccountCount(ctx, parentAccount.getBAN());
                    }
                    catch (final HomeException exception)
                    {
                        if (LogSupport.isDebugEnabled(ctx))
                        {
                            LogSupport.debug(ctx, AccountFamilyPlanHome.class, "Not able to find sub accounts",
                                    exception);
                        }
                    }
                    Home home = (Home) ctx.get(AccountCategoryHome.class);
                    AccountCategory accountCategory = (AccountCategory) home.find(ctx, new EQ(
                            AccountCategoryXInfo.IDENTIFIER, parentAccount.getType()));
                    if (numAcct == 0 && accountCategory != null
                            && newAccount.getSystemType().equals(SubscriberTypeEnum.PREPAID)
                            && accountCategory.getCustomerType().equals(CustomerTypeEnum.FAMILY))
                    {
                        int numPostpaidSub = AccountSupport.getNumberOfActivePostpaidSubscribersInTopology(ctx,
                                parentAccount);
                        if (numPostpaidSub <= 1)
                        {
                            cise.thrown(new IllegalPropertyArgumentException(AccountMoveRequestXInfo.NEW_PARENT_BAN,
                                    "Cannot add new account till an active owner is added"));
                        }
                    }
                }
                
            	CRMSpid sp = null;
            	try {

            		sp = HomeSupportHelper.get(ctx).findBean(ctx, CRMSpid.class, account.getSpid());

            	} catch (Exception e) {
            		
            		LogSupport.minor(ctx, ResponsibleAccountMoveProcessor.class.getName(), "Exception Occured while retrieving the SPID. Hence skipping validation.");
            	}

            	if(newAccount !=null && !newAccount.isPrepaid() && newAccount.isResponsible() && !account.isResponsible() && !account.isPrepaid()){
            		Context idCtx = ctx.createSubContext();
            		idCtx.put(AccountIdentificationValidator.FORCE_VALIDATION_CTX_KEY, Boolean.FALSE);
            		
            		try{
            			
            			if(newAccount.getAccountIdentificationLoaded())
            			{
            				AccountIdentificationValidator.instance().validate(idCtx, newAccount);
            			}
            			else
            			{
            				AccountIdentificationValidator.instance().validate(idCtx, account);
            			}
            			
            		}catch(Exception e){
            			cise
    					.thrown(new IllegalPropertyArgumentException(AccountXInfo.IDENTIFICATION_GROUP_LIST, 
    							"Minimum required Ids in identification group are missing for account:"+account.getBAN()));
            		}
            		
            	
            		if (sp != null)
            		{
            			
            			List l = null;
            			
            			if(newAccount.isSecurityQuestionAndAnswerLoaded())
            			{
            				l = newAccount.getSecurityQuestionsAndAnswers();	
            			}else
            			{
            				l = account.getSecurityQuestionsAndAnswers();
            			}
            			
            			
            			int minNumSecurityQuestions = sp.getMinNumSecurityQuestions();
            			if (minNumSecurityQuestions > 0)
            			{	
            				if(l == null
            						|| l.size() < minNumSecurityQuestions)
            				{
            					cise
            					.thrown(new IllegalPropertyArgumentException(AccountXInfo.SECURITY_QUESTIONS_AND_ANSWERS, 
            							"At least " + minNumSecurityQuestions + " security questions must be entered for account:"+account.getBAN()));
            				}
            			}
            		}
            	}
            }
            catch (HomeException ex)
            {
                cise.thrown(new IllegalPropertyArgumentException(AccountMoveRequestXInfo.NEW_PARENT_BAN,
                        "Cannot add new account till an active owner is added"));
            }
        }
    }

    /**
     * Verify that account and new parent account belong to the same service provider.
     * 
     * @param ctx Move context
     * @param cise Container for validation errors
     */
    private void validateAccountSpid(Context ctx, CompoundIllegalStateException cise)
    {
        AMR request = this.getRequest();
        
        Account account = request.getOriginalAccount(ctx);
        Account newParentAccount = request.getNewParentAccount(ctx);
        
        if (account != null && newParentAccount != null)
        {
            if (account.getSpid() != newParentAccount.getSpid())
            {
                cise.thrown(new IllegalPropertyArgumentException(
                        AccountMoveRequestXInfo.NEW_PARENT_BAN, 
                        "Account (BAN=" + request.getExistingBAN() + ") belongs to service provider " + account.getSpid()
                        + " but parent account (BAN=" + request.getNewParentBAN() + ") belongs to service provider " + newParentAccount.getSpid() + ".  "
                        + "Moving accounts across service providers is not supported."));
            }
        }
    }

    /**
     * Verify that account and new parent account have the role.
     * 
     * @param ctx Move context
     * @param cise Container for validation errors
     */
    private void validateAccountRole(Context ctx, CompoundIllegalStateException cise)
    {
        AMR request = this.getRequest();
        
        Account account = request.getOriginalAccount(ctx);
        Account newParentAccount = request.getNewParentAccount(ctx);
        
        if (account != null && newParentAccount != null)
        {
            if (account.getRole() != newParentAccount.getRole())
            {
                
                cise.thrown(new IllegalPropertyArgumentException(
                        AccountMoveRequestXInfo.NEW_PARENT_BAN, 
                        "Account (BAN=" + request.getExistingBAN() + ") has role ID=" + account.getRole()
                        + " but parent account (BAN=" + request.getNewParentBAN() + ") has role ID=" + newParentAccount.getRole() + ".  "
                        + "Moving accounts across roles is not supported."));
            }            
        }
    }

    /**
     * Verify that the moving account's system type is compatible with the new parent account's system type.
     * 
     * @param ctx Move context
     * @param cise Container for validation errors
     */
    private void validateSystemType(Context ctx, CompoundIllegalStateException cise)
    {
        AMR request = this.getRequest();
        
        Account account = request.getOriginalAccount(ctx);
        Account newParentAccount = request.getNewParentAccount(ctx);
        
        if (account != null && newParentAccount != null)
        {
            SubscriberTypeEnum oldType = account.getSystemType();
            SubscriberTypeEnum newParentType = newParentAccount.getSystemType();
            if ((oldType == SubscriberTypeEnum.POSTPAID && newParentType == SubscriberTypeEnum.PREPAID)
                    || (oldType == SubscriberTypeEnum.PREPAID && newParentType == SubscriberTypeEnum.POSTPAID)
                    || (oldType == SubscriberTypeEnum.HYBRID && newParentType != SubscriberTypeEnum.HYBRID))
            {
                cise.thrown(new IllegalPropertyArgumentException(
                        AccountMoveRequestXInfo.NEW_PARENT_BAN,
                        oldType + " (BAN=" + request.getExistingBAN() + ") account cannot move to " + newParentType + " parent account (BAN=" + request.getNewParentBAN() + ")."));
            }
        }
    }

    /**
     * Verify that the moving account and new parent account have compatible bill cycles.
     * 
     * @param ctx Move context
     * @param cise Container for validation errors
     */
    private void validateBillCycleDate(Context ctx, CompoundIllegalStateException cise)
    {
        AMR request = this.getRequest();
        
        Account account = request.getOriginalAccount(ctx);
        Account newParentAccount = request.getNewParentAccount(ctx);
        
        if (account != null && newParentAccount != null)
        {
            try
            {
                BillCycle accountBillCycle = account.getBillCycle(ctx);
                BillCycle newBillCycle = newParentAccount.getBillCycle(ctx);
                if (accountBillCycle != null && newBillCycle != null)
                {
                    int dayOfMonth = accountBillCycle.getDayOfMonth();
                    int newDayOfMonth = newBillCycle.getDayOfMonth();
                    if (dayOfMonth != newDayOfMonth)
                    {
                        cise.thrown(new IllegalStateException(
                                "Bill cycles for accounts " + request.getExistingBAN() + " and " + request.getNewParentBAN()
                                + " are not compatible.  "
                                + "Bill cycle dates differ (" + dayOfMonth + " and " + newDayOfMonth + " respectively)."));
                    }
                }
                else
                {
                    cise.thrown(new IllegalStateException(
                            "Bill cycle not found for account " + request.getExistingBAN() + " or " + request.getNewParentBAN()));
                }
            }
            catch (HomeException e)
            {
                cise.thrown(new IllegalStateException(
                        "Error occurred retrieving bill cycles for accounts " + request.getExistingBAN() + " and " + request.getNewParentBAN(), e));
            }
        }
    }
    
    /**
     * Verify that the moving account has no open transfer disputes.
     * 
     * @param ctx Move context
     * @param cise Container for validation errors
     */
    private void validateTransferDisputes(Context ctx, CompoundIllegalStateException cise)
    {
        AMR request = this.getRequest();
        
        Account oldAccount = request.getOriginalAccount(ctx);
        if (oldAccount != null)
        {
            Set<TransferDisputeStatusEnum> openStates = new HashSet<TransferDisputeStatusEnum>();
            openStates.add(TransferDisputeStatusEnum.INITIATED);
            openStates.add(TransferDisputeStatusEnum.ASSIGNED);
            In isDisputeOpen = new In(TransferDisputeXInfo.STATE, openStates);
            
            Or isAccountInvolved = new Or();
            isAccountInvolved.add(new EQ(TransferDisputeXInfo.CONT_SUB_ACCOUNT, oldAccount.getBAN()));
            isAccountInvolved.add(new EQ(TransferDisputeXInfo.RECP_SUB_ACCOUNT, oldAccount.getBAN()));
            
            And filter = new And();
            filter.add(isDisputeOpen);
            filter.add(isAccountInvolved);
            
            Home disputeHome = (Home) ctx.get(TransferDisputeHome.class);
            disputeHome = disputeHome.where(ctx, filter);
            
            try
            {
                CountingVisitor cv = new CountingVisitor();
                disputeHome.forEach(ctx, cv);
                if (cv.getCount() > 0)
                {
                    cise.thrown(new IllegalPropertyArgumentException(
                            AccountMoveRequestXInfo.EXISTING_BAN,
                            "Unable to move account (BAN=" + request.getExistingBAN() + ") because it has "
                            + cv.getCount() + " open transfer disputes that must be resolved first."));
                }
            }
            catch (HomeException e)
            {
                cise.thrown(new IllegalStateException("Error occurred retrieving open transfer disputes for account (BAN=" + request.getExistingBAN() + ").", e));
            }
        }
    }
    
    /**
     * Verify the maximum number of subscription which can be added to a new account (Group account).
     * 
     * @param ctx Move context
     * @param cise Container for validation errors
     */
    private void validateCreditCategory(Context ctx, CompoundIllegalStateException cise)
    {
        
        AMR request = this.getRequest();
        boolean willBeResponsible = request.getNewResponsible();
        if(!willBeResponsible)
        {
            Account oldAccount = request.getOriginalAccount(ctx);
            Account newParentAccount = request.getNewParentAccount(ctx);
            try
            {
                if(newParentAccount != null && (!newParentAccount.isPooled(ctx)))
                {
                    Account newRootAccount = newParentAccount.getResponsibleParentAccount(ctx);
                    if(newRootAccount != null)
                    {
                        int newRootCreditCat = newRootAccount.getCreditCategory();
                      
                        CreditCategory newRootCc = HomeSupportHelper.get(ctx).findBean(ctx, CreditCategory.class,
                                newRootCreditCat);
                        
                        int totalSubInOldRoot = oldAccount.getNonDeActiveSubscribers(ctx).size();
                        
                        if(totalSubInOldRoot > 0)
                        {
                            if(newRootCc!= null )
                            {
                                if(newRootCc.getMaxSubscriptionsAllowed() > 0)
                                {
                                    int totalSubsAfterMove = totalSubInOldRoot + newRootAccount.getTotalNumOfSubscriptions();
                                    if(totalSubsAfterMove > newRootCc.getMaxSubscriptionsAllowed())
                                    {
                                        cise.thrown(new IllegalPropertyArgumentException(
                                                AccountMoveRequestXInfo.EXISTING_BAN,
                                                "Unable to move account (BAN=" + request.getExistingBAN() + ") because it is exceeding the maximum subscriptions limit " +
                                                "of the credit category with id :  "+newRootCc.getCode()+" associated with the new root account with BAN: "+newRootAccount.getBAN()+" ."));
                                    }
                                }
                            }
                           else
                           {
                               cise.thrown(new IllegalPropertyArgumentException(
                                       AccountMoveRequestXInfo.EXISTING_BAN,
                                       "Credit Category of root account with BAN : "+newRootAccount.getBAN()+" is not found."));
                           }
                        }
                    }
                }
            }
            catch (HomeException e)
            {
                LogSupport.major(ctx, this, "Error occurred during  validating the credit category count for subscriptions ");
                cise.thrown(new IllegalStateException("Error occurred during  retrieving Account's credit category and subscriptions details (BAN=" + newParentAccount.getBAN() + " and "+oldAccount.getBAN()+").", e));
            }
        }
    }
    
    /**
     * Verify the maximum number of sub Accounts which can be added to a new account (Group account).
     * 
     * @param ctx Move context
     * @param cise Container for validation errors
     */
    private void validateSubAccountLimitInParent(Context ctx, CompoundIllegalStateException cise)
    {
        AMR request = this.getRequest();

            Account newAccount = request.getNewAccount(ctx);
            Account newParentAccount = request.getNewParentAccount(ctx);
            if(newAccount != null && newParentAccount != null)
            {
	            try
	            {
	            	AccountHierachyValidator validatorActHier = new AccountHierachyValidator();
                    if (LogSupport.isDebugEnabled(ctx))
                    {
                        LogSupport.debug(ctx, this, "calling AccountHierachyValidator to verify max subscriber limit not hit for parent " + newAccount.getParentBAN());
                    }
	            	validatorActHier.validateAddAccountToParentAccount(ctx, newAccount, cise);
	            }
	            catch (HomeException e)
	            {
	            	String msg=null;
	            	if(newParentAccount != null){
	            		msg = "Error occurred during validating maximum sub Account limit of Parent account : " + newParentAccount.getBAN();
	            	}
	            	else{
	            		msg = "Error occurred during validating maximum sub Account limit of Parent account.";
	            	}
	                LogSupport.major(ctx, this, msg);
	                cise.thrown(new IllegalStateException(msg, e));
	            }
            }
    }
}
