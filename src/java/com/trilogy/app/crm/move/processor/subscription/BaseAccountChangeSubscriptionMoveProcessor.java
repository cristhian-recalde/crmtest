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
package com.trilogy.app.crm.move.processor.subscription;

import java.util.HashSet;
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

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountStateEnum;
import com.trilogy.app.crm.bean.BillCycle;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.move.MoveProcessor;
import com.trilogy.app.crm.move.processor.DependencyMoveProcessor;
import com.trilogy.app.crm.move.processor.MoveProcessorProxy;
import com.trilogy.app.crm.move.request.SubscriptionMoveRequest;
import com.trilogy.app.crm.move.request.SubscriptionMoveRequestXInfo;
import com.trilogy.app.crm.move.support.SubscriptionMoveValidationSupport;
import com.trilogy.app.crm.support.PaymentPlanSupportHelper;
import com.trilogy.app.crm.transfer.TransferDisputeHome;
import com.trilogy.app.crm.transfer.TransferDisputeStatusEnum;
import com.trilogy.app.crm.transfer.TransferDisputeXInfo;


/**
 * This processor is responsible for performing all validation that applies
 * to subscription move scenarios involving a change of accounts.  It is
 * also responsible for performing any setup that is common to such
 * subscription move scenario.
 * 
 * It does not implement any subscription move business logic, modify the request,
 * or modify the subscriptions involved.
 *
 * @author aaron.gourley@redknee.com
 * @since 8.2
 */
public class BaseAccountChangeSubscriptionMoveProcessor<SMR extends SubscriptionMoveRequest> extends MoveProcessorProxy<SMR>
{
    public BaseAccountChangeSubscriptionMoveProcessor(SMR request)
    {
        super(new DependencyMoveProcessor<SMR>(request));
    }
    
    public BaseAccountChangeSubscriptionMoveProcessor(MoveProcessor<SMR> delegate)
    {
        super(delegate);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void validate(Context ctx) throws IllegalStateException
    {
        CompoundIllegalStateException cise = new CompoundIllegalStateException();

        validateSubscriptionToMove(ctx, cise);
        validateSubscriptionAccount(ctx, cise);
        validateNewAccount(ctx, cise);

        validateAccountSpid(ctx, cise);
        validateSystemType(ctx, cise);            
        validateBillCycle(ctx, cise);

        validateTransferDisputes(ctx, cise);
        
        cise.throwAll();

        // Don't bother letting the delegate validate unless this processor has passed.
        super.validate(ctx);
    }


    
    /**
     * Verify that the subscription is allowed to be moved to the new account
     * 
     * @param ctx Move context
     * @param cise Container for validation errors
     */
    private void validateSubscriptionToMove(Context ctx, CompoundIllegalStateException cise)
    {
        SMR request = this.getRequest();

        Subscriber oldSubscription = SubscriptionMoveValidationSupport.validateOldSubscriptionExists(ctx, request, cise);
        if (oldSubscription != null)
        {
            if (SafetyUtil.safeEquals(oldSubscription.getBAN(), request.getNewBAN()))
            {
                cise.thrown(new IllegalPropertyArgumentException(
                        SubscriptionMoveRequestXInfo.NEW_BAN, 
                        "Subscription (ID=" + request.getOldSubscriptionId() + ") already belongs to account (BAN=" + request.getNewBAN() + ")."));
            }
            
            // If/when adding support for standalone subscription move (i.e. without account move),
            // reject if this subscription is the special hidden pooled subscription.  Until then
            // we allow the move for cases where we are moving a non-responsible pooled account.
        }
    }

    /**
     * Verify that the subscription is allowed to leave the current account.
     * 
     * @param ctx Move context
     * @param cise Container for validation errors
     */
    private void validateSubscriptionAccount(Context ctx, CompoundIllegalStateException cise)
    {
        SMR request = this.getRequest();

        Account oldAccount = SubscriptionMoveValidationSupport.validateOldAccountExists(ctx, request, cise);
        if (oldAccount != null)
        {
            // Disallow move when old account inactive.
            if (oldAccount.getState() != AccountStateEnum.ACTIVE)
            {
                cise.thrown(new IllegalPropertyArgumentException(
                        SubscriptionMoveRequestXInfo.OLD_SUBSCRIPTION_ID, 
                        "Subscription's (ID=" + request.getOldSubscriptionId() + ") account (BAN=" + oldAccount.getBAN() + ") is not active."));
            }
            else if (PaymentPlanSupportHelper.get(ctx).isEnabled(ctx))
            {
                // TT5121928403 - Moving the last postpaid subscription within the existing hierarchy is not allowed.
                try
                {
                    Account parentAccount = oldAccount.getResponsibleParentAccount(ctx);
                    if (parentAccount != null
                            && PaymentPlanSupportHelper.get(ctx).isValidPaymentPlan(ctx, parentAccount.getPaymentPlan()))
                    {
                        //TODO: Deny only if the given subscription is the last postpaid subscription in the payment plan
                        //      This can only be done if the AmountOwingTransferCopyMoveStrategy accounts for payment plan related
                        //      balance transfers
                        //Hint: Use AccountSupport.hasMoreThanOnePostpaidSubscriber(ctx, parentAccount)
                        cise.thrown(new IllegalPropertyArgumentException(
                                SubscriptionMoveRequestXInfo.OLD_SUBSCRIPTION_ID, 
                                "Can't move subscription (ID=" + request.getOldSubscriptionId() + ")"
                                + " because it is in an account enrolled in Payment Plan (Payment Plan BAN=" + parentAccount.getBAN() + ")."));
                    }
                }
                catch (HomeException e)
                {
                    cise.thrown(new IllegalPropertyArgumentException(
                            SubscriptionMoveRequestXInfo.OLD_SUBSCRIPTION_ID,
                            "Subscription (ID=" + request.getOldSubscriptionId() + ") can't be moved because an error "
                            + " occurred retrieving its responsible parent account.  Unable to validate Payment Plan restrictions."));
                }
            }
        }
    }
    
    /**
     * Verify that the new account can have subscriptions moved to it.
     * 
     * @param ctx Move context
     * @param cise Container for validation errors
     */
    private void validateNewAccount(Context ctx, CompoundIllegalStateException cise)
    {
        SMR request = this.getRequest();

        Account newAccount = SubscriptionMoveValidationSupport.validateNewAccountExists(ctx, request, cise);
        if (newAccount != null)
        {
            // Disallow move when new account is inactive.
            if (newAccount.getState() != AccountStateEnum.ACTIVE)
            {
                cise.thrown(new IllegalPropertyArgumentException(
                        SubscriptionMoveRequestXInfo.NEW_BAN,
                        "New account (BAN=" + request.getNewBAN() + ") is not active."));
            }
        }
    }

    /**
     * Verify that the subscription and account have compatible paid types.
     * 
     * @param ctx Move context
     * @param cise Container for validation errors
     */
    private void validateSystemType(Context ctx, CompoundIllegalStateException cise)
    {
        SMR request = this.getRequest();
        
        Subscriber oldSubscription = request.getOldSubscription(ctx);
        Account newAccount = request.getNewAccount(ctx);
        
        if ( oldSubscription != null && newAccount != null )
        {
            SubscriberTypeEnum oldType = oldSubscription.getSubscriberType();
            SubscriberTypeEnum newParentType = newAccount.getSystemType();
            if ((oldType == SubscriberTypeEnum.POSTPAID && newParentType == SubscriberTypeEnum.PREPAID)
                    || (oldType == SubscriberTypeEnum.PREPAID && newParentType == SubscriberTypeEnum.POSTPAID))
            {
                cise.thrown(new IllegalPropertyArgumentException(
                        SubscriptionMoveRequestXInfo.NEW_BAN, 
                        oldType + " subscription (ID = " + request.getOldSubscriptionId() + ") "
                        + "can't move to " + newParentType + " account (BAN = " + request.getNewBAN() + ")."));
            }   
        }
    }

    /**
     * Verify that the moving account and new parent account have compatible bill cycles.
     * 
     * @param ctx Move context
     * @param cise Container for validation errors
     */
    private void validateBillCycle(Context ctx, CompoundIllegalStateException cise)
    {
        SMR request = this.getRequest();
        
        Account oldAccount = request.getOldAccount(ctx);
        Account newAccount = request.getNewAccount(ctx);
        if (oldAccount != null && newAccount != null)
        {
            try
            {
                BillCycle oldBillCycle = oldAccount.getBillCycle(ctx);
                BillCycle newBillCycle = newAccount.getBillCycle(ctx);
                if (oldBillCycle != null && newBillCycle != null)
                {
                    int dayOfMonth = oldBillCycle.getDayOfMonth();
                    int newDayOfMonth = newBillCycle.getDayOfMonth();
                    if (dayOfMonth != newDayOfMonth)
                    {
                        cise.thrown(new IllegalStateException(
                                "Bill cycles for accounts " + oldAccount.getBAN() + " and " + request.getNewBAN()
                                + " are not compatible.  "
                                + "Bill cycle dates differ (" + dayOfMonth + " and " + newDayOfMonth + " respectively)."));
                    }
                }
                else
                {
                    cise.thrown(new IllegalStateException(
                            "Bill cycle not found for account " + oldAccount.getBAN() + " or " + request.getNewBAN()));
                }
            }
            catch (HomeException e)
            {
                cise.thrown(new IllegalStateException(
                        "Error occurred retrieving bill cycles for accounts " + oldAccount.getBAN() + " and " + request.getNewBAN(), e));
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
        SMR request = this.getRequest();
        
        Account oldAccount = request.getOldAccount(ctx);
        Account newAccount = request.getNewAccount(ctx);
        
        if (oldAccount != null && newAccount != null)
        {
            if (oldAccount.getSpid() != newAccount.getSpid())
            {
                cise.thrown(new IllegalPropertyArgumentException(
                        SubscriptionMoveRequestXInfo.NEW_BAN,
                        "The service provider of the new account (BAN=" + request.getNewBAN() + ") "
                        + " is required to be the same as of the old account (BAN=" + oldAccount.getBAN() + ")."));
            }
        }
    }
    
    /**
     * Verify that the moving subscription has no open transfer disputes.
     * 
     * @param ctx Move context
     * @param cise Container for validation errors
     */
    private void validateTransferDisputes(Context ctx, CompoundIllegalStateException cise)
    {
        SMR request = this.getRequest();
        
        Subscriber oldSubscription = request.getOriginalSubscription(ctx);
        if (oldSubscription != null)
        {
            Set<TransferDisputeStatusEnum> openStates = new HashSet<TransferDisputeStatusEnum>();
            openStates.add(TransferDisputeStatusEnum.INITIATED);
            openStates.add(TransferDisputeStatusEnum.ASSIGNED);
            In isDisputeOpen = new In(TransferDisputeXInfo.STATE, openStates);
            
            Or isAccountInvolved = new Or();
            isAccountInvolved.add(new EQ(TransferDisputeXInfo.CONT_SUB_ID, oldSubscription.getId()));
            isAccountInvolved.add(new EQ(TransferDisputeXInfo.RECP_SUB_ID, oldSubscription.getId()));
            
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
                            SubscriptionMoveRequestXInfo.OLD_SUBSCRIPTION_ID,
                            "Unable to move subscription (ID=" + request.getOldSubscriptionId() + ") because it has "
                            + cv.getCount() + " open transfer disputes that must be resolved first."));
                }
            }
            catch (HomeException e)
            {
                cise.thrown(new IllegalPropertyArgumentException(
                        SubscriptionMoveRequestXInfo.OLD_SUBSCRIPTION_ID,
                        "Error occurred retrieving open transfer disputes for subscription (ID=" + request.getOldSubscriptionId() + ")."));
            }
        }
    }
}
