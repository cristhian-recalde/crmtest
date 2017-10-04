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


import com.trilogy.app.crm.Common;
import com.trilogy.app.crm.account.AccountConstants;
import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountStateEnum;
import com.trilogy.app.crm.home.account.AccountHomePipelineFactory;
import com.trilogy.app.crm.move.MoveConstants;
import com.trilogy.app.crm.move.MoveException;
import com.trilogy.app.crm.move.CompoundMoveIllegalSateException;
import com.trilogy.app.crm.move.MoveProcessor;
import com.trilogy.app.crm.move.processor.MoveProcessorProxy;
import com.trilogy.app.crm.move.request.AccountMoveRequestXInfo;
import com.trilogy.app.crm.move.request.ConvertAccountGroupTypeRequest;
import com.trilogy.app.crm.move.support.AccountMoveValidationSupport;
import com.trilogy.app.crm.support.PaymentPlanSupportHelper;
import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.LogSupport;


/**
 * This processor is responsible for performing all validation that applies
 * to ANY account convert scenario. 
 * 
 * It does not implement any account convert business logic, modify the request,
 * or modify the accounts involved.
 *
 * @author bdhavalshankh
 * @since 9.5.1
 */
public class BaseAccountConvertProcessor<CAGTR extends ConvertAccountGroupTypeRequest> extends MoveProcessorProxy<CAGTR>
{
    public BaseAccountConvertProcessor(MoveProcessor<CAGTR> delegate)
    {
        super(delegate);
    }
    
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Context setUp(Context ctx) throws MoveException
    {
        Context moveCtx = super.setUp(ctx);

        CAGTR request = this.getRequest();

        moveCtx.put(AccountConstants.OLD_ACCOUNT, request.getOldAccount(moveCtx));
        
        try
        {
            final Context serverCtx = moveCtx.createSubContext("RMI Server Context");

            // this gives us access to the RMIHomeServers and the context that is passed by BAS to the homes.
            moveCtx.put("RMI Server Context",serverCtx);

            moveCtx.put(
                    MoveConstants.CUSTOM_ACCOUNT_HOME_CTX_KEY, 
                    AccountHomePipelineFactory.instance().decorateMoveHome(
                            (Home) moveCtx.get(Common.ACCOUNT_CACHED_HOME), moveCtx, serverCtx));
        }
        catch (Exception e)
        {
            throw new MoveException(request, "Error creating custom move pipeline for account storage operations.", e);
        }

        Account newAccount = request.getNewAccount(moveCtx);
        if (newAccount != null)
        {
            newAccount.setGroupType(request.getGroupType());
        }

        return moveCtx;
    }

    
    /**
     * @{inheritDoc}
     */
    @Override
    public void validate(Context ctx) throws IllegalStateException
    {
        CompoundMoveIllegalSateException cise = new CompoundMoveIllegalSateException();

        // Validate that the account that we are moving exists and is in a valid state.
        validateAccountToConvert(ctx, cise);
        
        cise.throwAll();

        super.validate(ctx);
    }
    
    /**
     * Validate that the account that we are moving exists and is in a valid state.
     * 
     * @param ctx Move context
     * @param cise Container for validation errors
     */
    private void validateAccountToConvert(Context ctx, CompoundMoveIllegalSateException cise)
    {
        CAGTR request = this.getRequest();

        Account account = AccountMoveValidationSupport.validateOldAccountExists(ctx, request, cise);
        if (account != null)
        {
                if(account.getState() !=  AccountStateEnum.ACTIVE)
                {
                    String msg = "Account (BAN=" + request.getExistingBAN() + ") is not Active. Convert not allowed. ";
                    cise.thrown(CompoundMoveIllegalSateException.ACCOUNT_NOT_ACTIVE, msg,  new IllegalPropertyArgumentException(
                            AccountMoveRequestXInfo.EXISTING_BAN, msg 
                            ));
                }
                
                if(!account.isIndividual(ctx))
                {
                    String msg = "Account (BAN=" + request.getExistingBAN() + ") is not an individual account. Convert not allowed. ";
                    cise.thrown(CompoundMoveIllegalSateException.ACCOUNT_NOT_INDIVIDUAL, msg,  new IllegalPropertyArgumentException(
                            AccountMoveRequestXInfo.EXISTING_BAN, msg 
                            ));
                }
                
                if(!account.isPostpaid())
                {
                    String msg = "Account (BAN=" + request.getExistingBAN() + ") is not postpaid. Convert not allowed. ";
                    cise.thrown(CompoundMoveIllegalSateException.ACCOUNT_NOT_POSTPAID, msg, new IllegalPropertyArgumentException(
                            AccountMoveRequestXInfo.EXISTING_BAN, msg 
                            ));
                }
                
                if(!account.isResponsible())
                {
                    String msg = "Account (BAN=" + request.getExistingBAN() + ") is not responsible. Convert not allowed. ";
                    cise.thrown(CompoundMoveIllegalSateException.ACCOUNT_NOT_RESPONSIBLE, msg, new IllegalPropertyArgumentException(
                                    AccountMoveRequestXInfo.EXISTING_BAN, msg
                                    ));
                }
                
                if(account.getParentBAN() != null && !account.getParentBAN().trim().equals(""))
                {
                    if(account.getParentBAN() != account.getBAN())
                    {
                        {
                           String msg = "Account (BAN=" + request.getExistingBAN() + ") exists in a hierarchy . Convert not allowed. Move out this account first.";  
                            cise.thrown(CompoundMoveIllegalSateException.ACCOUNT_UNDER_HIERARCHY, msg, new IllegalPropertyArgumentException(
                                    AccountMoveRequestXInfo.EXISTING_BAN, msg
                                   ));
                        }
                    }
                }
                
                int numOfActivePostpaidSub = 0;
				try 
				{
					numOfActivePostpaidSub = account.getActiveSubscribers(ctx).size();
				} 
				catch (HomeException e1) 
				{
					LogSupport.minor(ctx, this, "Home exceptions encountred while trying to calculate Number of Active Subscribers for BAN :" + account.getBAN(), e1);
				}
                
                if (numOfActivePostpaidSub <= 0)
                {
                    String msg ="Account (BAN=" + request.getExistingBAN() + ") does not have an active postpaid subscription ."; 
                    cise.thrown(CompoundMoveIllegalSateException.NO_ACTIVE_SUBSCRIPTION, msg, new IllegalPropertyArgumentException(
                            AccountMoveRequestXInfo.EXISTING_BAN, msg
                            ));
                }
                
                if (PaymentPlanSupportHelper.get(ctx).isEnabled(ctx))
                {
                    try
                    {
                        Account parentAccount = account.getResponsibleParentAccount(ctx);
                        if (parentAccount != null
                                && PaymentPlanSupportHelper.get(ctx).isValidPaymentPlan(ctx, parentAccount.getPaymentPlan()))
                        {
                            String msg = "Account (BAN=" + request.getExistingBAN() + ") can't be converted because it is enrolled in Payment Plan "
                                    + "(Payment Plan BAN=" + parentAccount.getBAN() + ")." ;
                            cise.thrown(CompoundMoveIllegalSateException.PAYMENT_PLAN_ENROLLED, msg, new IllegalPropertyArgumentException(
                                    AccountMoveRequestXInfo.EXISTING_BAN, msg
                                    ));
                        }
                    }
                    catch (HomeException e)
                    {
                        cise.thrown(new IllegalPropertyArgumentException(
                                AccountMoveRequestXInfo.EXISTING_BAN,
                                "Account (BAN=" + request.getExistingBAN() + ") can't be converted because an error "
                                + " occurred retrieving its responsible parent account.  Unable to validate Payment Plan restrictions."));
                    }
                }
            }
        }

}
