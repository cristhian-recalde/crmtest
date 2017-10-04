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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import com.trilogy.app.crm.Common;
import com.trilogy.app.crm.account.AccountConstants;
import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.core.AccountCategory;
import com.trilogy.app.crm.bean.core.AuxiliaryService;
import com.trilogy.app.crm.bean.AccountCategoryHome;
import com.trilogy.app.crm.bean.AccountCategoryXInfo;
import com.trilogy.app.crm.bean.CallingGroupTypeEnum;
import com.trilogy.app.crm.bean.CustomerTypeEnum;
import com.trilogy.app.crm.bean.PrivateCug;
import com.trilogy.app.crm.bean.PrivateCugHome;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberAuxiliaryService;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.bean.core.SubscriptionType;
import com.trilogy.app.crm.home.AccountFamilyPlanHome;
import com.trilogy.app.crm.home.account.AccountHomePipelineFactory;
import com.trilogy.app.crm.home.account.AccountRequiredFieldValidator;
import com.trilogy.app.crm.move.MoveConstants;
import com.trilogy.app.crm.move.MoveException;
import com.trilogy.app.crm.move.MoveProcessor;
import com.trilogy.app.crm.move.processor.MoveProcessorProxy;
import com.trilogy.app.crm.move.request.AccountMoveRequestXInfo;
import com.trilogy.app.crm.move.request.ConvertAccountBillingTypeRequest;
import com.trilogy.app.crm.move.request.ConvertAccountBillingTypeRequestXInfo;
import com.trilogy.app.crm.move.support.AccountMoveValidationSupport;
import com.trilogy.app.crm.support.AccountSupport;
import com.trilogy.app.crm.support.SubscriberAuxiliaryServiceSupport;
import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.beans.MissingRequireValueException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MinorLogMsg;


/**
 * Processor used to execute paid type conversion specific validation and business logic.
 * 
 * If this class becomes too big, it should be split up into more specific processors.
 * 
 * Originally, it just sets the values of the new account and validates that the request
 * is valid and allowed.
 *
 * @author aaron.gourley@redknee.com
 * @since 8.2 
 */
public class AccountConversionMoveProcessor<CABTR extends ConvertAccountBillingTypeRequest> extends MoveProcessorProxy<CABTR>
{
    public AccountConversionMoveProcessor(MoveProcessor<CABTR> delegate)
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

        CABTR request = this.getRequest();

        moveCtx.put(AccountConstants.OLD_ACCOUNT, request.getOldAccount(moveCtx));
        
        Account account = request.getOriginalAccount(moveCtx);
        if (account != null)
        {
            try
            {
                Collection col = account.getSubscribers(moveCtx);
                Iterator<Subscriber> allSubsItr = col.iterator();
                int totalActiveSubscribers = 0;
                while (allSubsItr.hasNext())
                {
                    Subscriber checkSub = allSubsItr.next();
                    if (checkSub.getState().equals(SubscriberStateEnum.ACTIVE))
                    {
                        totalActiveSubscribers++;
                    }
                }
                if (totalActiveSubscribers == 1)
                {
                    moveCtx.put(Common.BILLING_TYPE_CONVERSION, col.iterator().next());
                }
                else if (totalActiveSubscribers > 1)
                {
                    new MinorLogMsg(this, "Account (BAN=" + account.getBAN() + ") has more than 1 subscription", null).log(moveCtx);
                }
                else
                {
                    new MinorLogMsg(this, "Account (BAN=" + account.getBAN() + ") has no subscriptions", null).log(moveCtx);
                }
            }
            catch (HomeException ex)
            {
                new MinorLogMsg(this, "Error retrieving subscriptions for Account (BAN=" + account.getBAN() + ")", ex).log(moveCtx);
            }
        }

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
            newAccount.setSystemType(request.getSystemType());
            newAccount.setBillCycleID(request.getBillCycleID());

            if (!SubscriberTypeEnum.PREPAID.equals(request.getSystemType()))
            {
                newAccount.setLastName(request.getLastName());
                newAccount.setFirstName(request.getFirstName());
                newAccount.setBillingAddress1(request.getBillingAddress1());
                newAccount.setBillingAddress2(request.getBillingAddress2());
                newAccount.setBillingAddress3(request.getBillingAddress3());
                newAccount.setContactName(request.getContactName());
                newAccount.setContactTel(request.getContactTel());
                newAccount.setBillingCity(request.getBillingCity());
                newAccount.setBillingCountry(request.getBillingCountry());
                newAccount.setDateOfBirth(request.getDateOfBirth());
                newAccount.setOccupation(request.getOccupation());
                newAccount.setIdentificationGroupList(request.getIdentificationGroupList());
                newAccount.setSecurityQuestionsAndAnswers(request.getSecurityQuestionsAndAnswers());
            }
        }

        return moveCtx;
    }


    /**
     * @{inheritDoc}
     */
    @Override
    public void validate(Context ctx) throws IllegalStateException
    {
        CompoundIllegalStateException cise = new CompoundIllegalStateException();
        
        CABTR request = this.getRequest();
        boolean isBillingTypeConversion = true;

        Account oldAccount = AccountMoveValidationSupport.validateOldAccountExists(ctx, request, cise);
        Account newAccount = AccountMoveValidationSupport.validateNewAccountExists(ctx, request, cise);
        
        try
        {
            if (oldAccount != null && newAccount != null && oldAccount.getParentAccount(ctx)!=null
                    && (oldAccount.getParentAccount(ctx).equals(newAccount.getParentAccount(ctx)))
                    && oldAccount.getParentAccount(ctx).isPooled(ctx))
            {
                isBillingTypeConversion = false;
            }
        }
        catch (HomeException e)
        {
            cise.thrown(new IllegalPropertyArgumentException(AccountMoveRequestXInfo.NEW_PARENT_BAN,
                    "Not able to find parent account."));
        }
        
        if (oldAccount != null)
        {
            validateConversionRequest(ctx, request, oldAccount.getSpid(), cise);
        
            if (!oldAccount.isResponsible() && isBillingTypeConversion)
            {
                cise.thrown(new IllegalPropertyArgumentException(
                        AccountMoveRequestXInfo.EXISTING_BAN, 
                        "Account (BAN=" + request.getExistingBAN() + ") needs to be responsible account."));
            }
        
            if (!oldAccount.isIndividual(ctx))
            {
                cise.thrown(new IllegalPropertyArgumentException(
                        AccountMoveRequestXInfo.EXISTING_BAN, 
                        "Account (BAN=" + request.getExistingBAN() + ") needs to be an individual account."));
            }
            
            try
            {
                Account parentAccount = oldAccount.getParentAccount(ctx);
                if (parentAccount != null)
                {
                    if (!parentAccount.getSystemType().equals(SubscriberTypeEnum.HYBRID))
                    {
                        cise.thrown(new IllegalPropertyArgumentException(AccountMoveRequestXInfo.NEW_PARENT_BAN,
                                "Parent Account [BAN=" + parentAccount.getBAN() +"]  needs to be hybrid account."));
                    }
                }
            }
            catch (HomeException ex)
            {
                cise.thrown(new IllegalPropertyArgumentException(AccountMoveRequestXInfo.NEW_PARENT_BAN,
                        "Parent Account needs to be hybrid account."));
            }
            
            if ( SubscriberTypeEnum.HYBRID.equals(oldAccount.getSystemType()) )
            {
                cise.thrown(new IllegalPropertyArgumentException(
                        AccountMoveRequestXInfo.EXISTING_BAN, 
                        "Account (BAN=" + request.getExistingBAN() + ") is a hybrid account."));
            }
            
            if (oldAccount.isPooled(ctx))
            {
                cise.thrown(new IllegalPropertyArgumentException(
                        AccountMoveRequestXInfo.EXISTING_BAN, 
                        "Account (BAN=" + request.getExistingBAN() + ") needs to be Subscriber Account with 1 subscription.  " +
                                "You can convert account to a Hybrid Account through Account UI"));
            }
            
            if (isPooledMember(ctx, oldAccount) && isBillingTypeConversion)
            {
                cise.thrown(new IllegalPropertyArgumentException(
                        AccountMoveRequestXInfo.EXISTING_BAN, 
                        "Account (BAN=" + request.getExistingBAN() + ") belongs to a pooled account."));
            }
            
            validateSubscriptionRequirements(ctx, cise);
        }

        if (newAccount != null)
        {
            // Validation entries were copied from the request to the new account in the setUp()
            // We can reuse the normal required field validator to validate those entries.
            new AccountRequiredFieldValidator(true, false, true).validate(ctx, newAccount);
        }
        
        cise.throwAll();

        // Don't bother letting the delegate validate unless this processor has passed.
        super.validate(ctx);
    }


    private void validateSubscriptionRequirements(Context ctx, CompoundIllegalStateException cise)
    {
        CABTR request = this.getRequest();

        Account oldAccount = request.getOriginalAccount(ctx);
        
        try
        {
            Collection<Subscriber> allSubscribers = oldAccount.getSubscribers(ctx);
            Collection<Subscriber> activeSubscribers = new ArrayList<Subscriber>();
            
            for (Subscriber subscriber: allSubscribers)
            {
                if (!subscriber.isInFinalState())
                {
                    activeSubscribers.add(subscriber);
                }
            }
            if (activeSubscribers.size() > 1)
            {
                cise.thrown(new IllegalPropertyArgumentException(
                        AccountMoveRequestXInfo.EXISTING_BAN, 
                        "Account (BAN=" + request.getExistingBAN() + ") has more than 1 active subscription"));
            }
            else if (activeSubscribers.size() > 0)
            {
                Subscriber sub = activeSubscribers.iterator().next();
                if (sub != null)
                {
                    SubscriptionType subscriptionType = sub.getSubscriptionType(ctx);
                    if (subscriptionType != null
                            && !subscriptionType.isMoveSupported(request))
                    {
                        cise.thrown(new IllegalPropertyArgumentException(
                                AccountMoveRequestXInfo.EXISTING_BAN, 
                                "Account (BAN=" + request.getExistingBAN() + ") contains "
                                + subscriptionType.getTypeEnum() + " subscription. Billing type conversion not supported."));
                    }
                    validateSubscriptionCUGs(ctx, sub, cise);
                }
            }
        }
        catch (HomeException ex)
        {
            cise.thrown(new IllegalPropertyArgumentException(
                    AccountMoveRequestXInfo.EXISTING_BAN, 
                    "Error retrieving subscriptions for Account (BAN=" + request.getExistingBAN() + ")"));
        }
    }
    
    private void validateSubscriptionCUGs(Context ctx, Subscriber subscription, CompoundIllegalStateException cise)
    {
        CABTR request = this.getRequest();
        if (subscription.isPostpaid())
        {
            for (final Iterator<SubscriberAuxiliaryService> iterator = subscription.getAuxiliaryServices(ctx).iterator(); iterator.hasNext();)
            {
                final Home home = ((Home) ctx.get(PrivateCugHome.class));
                final SubscriberAuxiliaryService association = iterator.next();
                try
                {
                    final AuxiliaryService service = SubscriberAuxiliaryServiceSupport.getAuxiliaryService(ctx, association);
                    if (service.isPrivateCUG(ctx))
                    {
                        final long cugId = association.getSecondaryIdentifier();
                        try
                        {
                            final PrivateCug cug = (PrivateCug) home.find(ctx, new Long(cugId));
                            
                            if (cug != null && cug.getOwnerMSISDN().equals(subscription.getMSISDN()))
                            {
                                cise.thrown(new IllegalPropertyArgumentException(
                                        AccountMoveRequestXInfo.EXISTING_BAN, 
                                        "Account (BAN=" + request.getExistingBAN() + ") subscription is the CUG owner for private CUG '" + cug.getID() +"'. Billing type conversion not supported."));
                            }
                        }
                        catch (HomeException ex)
                        {
                            new MinorLogMsg(this, "Error retrieving Private CUG for subscriber auxiliary service (SubscriberId=" + 
                                    subscription.getId() + ", SubscriberAuxiliaryServiceId=" + association.getIdentifier() + ", cugId=" + 
                                    association.getSecondaryIdentifier() + ")", ex).log(ctx);
                        }
                        
                    }
                }
                catch (HomeException ex)
                {
                    new MinorLogMsg(this, "Error retrieving auxiliary service for subscriber auxiliary service (SubscriberId=" + 
                            subscription.getId() + ", SubscriberAuxiliaryServiceId=" + association.getIdentifier() + ", AuxiliaryServiceId=" + 
                            association.getAuxiliaryServiceIdentifier() + ")", ex).log(ctx);
                }
            }
            
        }

        
    }


    private void validateConversionRequest(Context ctx, ConvertAccountBillingTypeRequest request, int spid, CompoundIllegalStateException cise)
    {
        if (SubscriberTypeEnum.POSTPAID.equals(request.getSystemType()))
        {
            if (request.getPricePlan() == ConvertAccountBillingTypeRequest.DEFAULT_PRICEPLAN
                    && request.getSubscriptionClass() != ConvertAccountBillingTypeRequest.DEFAULT_SUBSCRIPTIONCLASS)
            {
                cise.thrown(new MissingRequireValueException(ConvertAccountBillingTypeRequestXInfo.PRICE_PLAN));                                        
            }
            
            if ( request.getNewDepositAmount() < 0 )
            {
                cise.thrown(new IllegalPropertyArgumentException(ConvertAccountBillingTypeRequestXInfo.NEW_DEPOSIT_AMOUNT, " Deposit can't be less than 0"));
            }
        }
    }
    
    
    private static boolean isPooledMember(Context ctx, Account account)
    {
        Account parent = null;
        try
        {
            parent = account.getParentAccount(ctx);
        }
        catch (HomeException e)
        {
        }
        return parent != null && parent.isPooled(ctx);
    }
}
