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
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.trilogy.app.crm.Common;
import com.trilogy.app.crm.account.AccountConstants;
import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.GroupTypeEnum;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.bean.account.SubscriptionTypeEnum;
import com.trilogy.app.crm.bean.core.SubscriptionType;
import com.trilogy.app.crm.calculation.service.CalculationService;
import com.trilogy.app.crm.calculation.service.CalculationServiceException;
import com.trilogy.app.crm.calculation.support.CalculationServiceSupport;
import com.trilogy.app.crm.extension.Extension;
import com.trilogy.app.crm.extension.account.AccountExtensionHolder;
import com.trilogy.app.crm.extension.account.GroupPricePlanExtension;
import com.trilogy.app.crm.extension.account.PoolExtension;
import com.trilogy.app.crm.extension.account.PoolExtensionXInfo;
import com.trilogy.app.crm.extension.account.SubscriptionPoolProperty;
import com.trilogy.app.crm.home.account.AccountHomePipelineFactory;
import com.trilogy.app.crm.home.account.AccountRequiredFieldValidator;
import com.trilogy.app.crm.move.MoveConstants;
import com.trilogy.app.crm.move.MoveException;
import com.trilogy.app.crm.move.MoveProcessor;
import com.trilogy.app.crm.move.processor.MoveProcessorProxy;
import com.trilogy.app.crm.move.request.AccountMoveRequestXInfo;
import com.trilogy.app.crm.move.request.ConvertAccountGroupTypeRequest;
import com.trilogy.app.crm.move.request.ConvertAccountGroupTypeRequestXInfo;
import com.trilogy.app.crm.move.support.AccountMoveValidationSupport;
import com.trilogy.app.crm.support.AccountSupport;
import com.trilogy.app.crm.support.CurrencyPrecisionSupportHelper;
import com.trilogy.app.crm.support.ExtensionSupportHelper;
import com.trilogy.app.crm.support.SpidSupport;
import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.beans.SafetyUtil;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.util.crmapi.wsdl.v2_1.types.account.AccountStateEnum;


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
public class AccountGroupTypeConversionMoveProcessor<CABTR extends ConvertAccountGroupTypeRequest> extends MoveProcessorProxy<CABTR>
{
    public AccountGroupTypeConversionMoveProcessor(MoveProcessor<CABTR> delegate)
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
            List<Extension> existingExtensions = new ArrayList<Extension>(account.getExtensions());
            for (AccountExtensionHolder holder : ((List<AccountExtensionHolder>) request.getAccountExtensions()))
            {
                Extension extension = holder.getExtension();
                existingExtensions.add(extension);
                newAccount.setAccountExtensions(ExtensionSupportHelper.get(moveCtx).wrapExtensions(moveCtx, existingExtensions));
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

        Account oldAccount = AccountMoveValidationSupport.validateOldAccountExists(ctx, request, cise);
        if (oldAccount != null)
        {
            validateRequest(ctx, request, oldAccount, cise);
        }

        Account newAccount = AccountMoveValidationSupport.validateNewAccountExists(ctx, request, cise);
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


    private void validateRequest(Context ctx, CABTR request, Account oldAccount, CompoundIllegalStateException cise)
    {
        if (SafetyUtil.safeEquals(request.getGroupType(), oldAccount.getGroupType()))
        {
            cise.thrown(new IllegalPropertyArgumentException(
                    ConvertAccountGroupTypeRequestXInfo.GROUP_TYPE, 
                    "Account (BAN=" + request.getExistingBAN() + ") already has the selected group type."));
        }
        else if (GroupTypeEnum.GROUP_POOLED.equals(oldAccount.getGroupType()))
        {
            cise.thrown(new IllegalPropertyArgumentException(
                    AccountMoveRequestXInfo.EXISTING_BAN, 
                    "Account (BAN=" + request.getExistingBAN() + ") is pooled account."));
        }
        else if (isPooledMember(ctx, oldAccount))
        {
            cise.thrown(new IllegalPropertyArgumentException(
                    AccountMoveRequestXInfo.EXISTING_BAN, 
                    "Account (BAN=" + request.getExistingBAN() + ") belongs to a pooled account."));
        }
        else
        {
            try
            {
                if (GroupTypeEnum.SUBSCRIBER.equals(oldAccount.getGroupType()) && oldAccount.getSubscribers(ctx).size()>0)
                {
                    cise.thrown(new IllegalPropertyArgumentException(
                            AccountMoveRequestXInfo.EXISTING_BAN, 
                            "Account (BAN=" + request.getExistingBAN() + ") has subscriptions under it and therefore cannot have it's group converted."));
                }
            }
            catch (HomeException ex)
            {
                cise.thrown(new IllegalPropertyArgumentException(
                        AccountMoveRequestXInfo.EXISTING_BAN, 
                        "Error retrieving subscriptions for Account (BAN=" + request.getExistingBAN() + ")"));
            }
            
            if (GroupTypeEnum.GROUP.equals(oldAccount.getGroupType()) && GroupTypeEnum.SUBSCRIBER.equals(request.getGroupType()))
            {
                try
                {
                    Home childAccountHome = AccountSupport.getImmediateChildrenAccountHome(ctx, oldAccount.getBAN());
                    Collection<Account> subAccounts = childAccountHome.selectAll(ctx);
    
                    if (subAccounts!=null && subAccounts.size()>0)
                    {
                        cise.thrown(new IllegalPropertyArgumentException(
                            AccountMoveRequestXInfo.EXISTING_BAN, 
                            "Account (BAN=" + request.getExistingBAN() + ") has accounts under it and therefore cannot be converted to individual."));
                    }
                }
                catch (HomeException ex)
                {
                    cise.thrown(new IllegalPropertyArgumentException(
                            AccountMoveRequestXInfo.EXISTING_BAN, 
                            "Error retrieving subAccounts for Account (BAN=" + request.getExistingBAN() + ")"));
                }
            }
        
            if (GroupTypeEnum.GROUP_POOLED.equals(request.getGroupType()))
            {
                if (SpidSupport.getPooledSubscriptionLevel(ctx, oldAccount.getSpid()) == CRMSpid.DEFAULT_POOLSUBSCRIPTIONLEVEL)
                {
                    cise.thrown(new IllegalPropertyArgumentException(ConvertAccountGroupTypeRequestXInfo.GROUP_TYPE,
                            "Account of type Pooled cannot be used because Service Provider does not have the "
                                    + "Pooled Group Subscription Level specified"));
                }

                boolean poolExists = false;
                PoolExtension poolExtension = null;
                for (AccountExtensionHolder holder : ((List<AccountExtensionHolder>) request.getAccountExtensions()))
                {
                    Extension extension = holder.getExtension();
                    if (extension instanceof PoolExtension)
                    {
                        poolExtension = (PoolExtension) extension;
                        poolExists = true;
                    }
                    break;
                }
                
                if (!poolExists)
                {
                    cise.thrown(new IllegalPropertyArgumentException(
                            ConvertAccountGroupTypeRequestXInfo.ACCOUNT_EXTENSIONS, 
                            "Pool extension required in order to convert group type to group pooled."));
                }
                else
                {
                    if (poolExtension.getPoolMSISDN().length() == 0)
                    {
                        cise.thrown(new IllegalPropertyArgumentException(PoolExtensionXInfo.POOL_MSISDN,
                                "Pool Extension requires the MSISDN value set."));
                    }

                    final Map<Long, SubscriptionPoolProperty> properties = poolExtension.getSubscriptionPoolProperties();
                    
                    if (properties == null || properties.isEmpty())
                    {
                        cise.thrown(new IllegalPropertyArgumentException(PoolExtensionXInfo.SUBSCRIPTION_POOL_PROPERTIES,
                                "Pool Extension should have at least one Subscription Type property."));
                    }

                    if (SubscriberTypeEnum.POSTPAID.equals(oldAccount.getSystemType()))
                    {
                        for (SubscriptionPoolProperty poolProperty : properties.values())
                        {
                            final SubscriptionType subType = poolProperty.getSubscriptionType(ctx);
                            if (!subType.isOfType(SubscriptionTypeEnum.AIRTIME))
                            {
                                cise.thrown(new IllegalPropertyArgumentException(PoolExtensionXInfo.SUBSCRIPTION_POOL_PROPERTIES,
                                        "Non Airtime Pool in Postpaid account is not supported."));
                            }
                            else
                            {
                                Context myCtx = ctx.createSubContext();
                                String mySessionKey =
                                    CalculationServiceSupport.createNewSession(myCtx);

                                CalculationService service =
                                    (CalculationService) ctx.get(CalculationService.class);
                                try
                                {
                                    long balanceOwing =
                                        service.getAmountOwedByAccount(ctx, mySessionKey,
                                            oldAccount.getBAN(), new Date());

                                    if (balanceOwing>poolProperty.getInitialPoolBalance())
                                    {
                                        cise.thrown(new IllegalPropertyArgumentException(ConvertAccountGroupTypeRequestXInfo.GROUP_TYPE,
                                                "The accumulated balance owing for Account (BAN=" + request.getExistingBAN() + ") in the value of " + 
                                        CurrencyPrecisionSupportHelper.get(ctx).formatDisplayCurrencyValue(ctx, oldAccount.getCurrency(), balanceOwing) + 
                                        " is greater than the Pool Initial Balance/Credit Limit"));
                                    }
                                }
                                catch (CalculationServiceException e)
                                {
                                    cise.thrown(new IllegalPropertyArgumentException(ConvertAccountGroupTypeRequestXInfo.GROUP_TYPE,
                                            "Unable to calculate the accumulated balance owing for Account (BAN=" + request.getExistingBAN() + ")"));
                                }
                                finally
                                {
                                    CalculationServiceSupport.endSession(myCtx,
                                        mySessionKey);
                                }

                            }

                        }
                    }
                    
                    if (GroupTypeEnum.GROUP.equals(oldAccount.getGroupType()))
                    {
                        try
                        {
                            validateNonResponsibleActivePooledAccountsUnder(ctx, request, oldAccount, cise);
                        }
                        catch (HomeException ex)
                        {
                            cise.thrown(new IllegalPropertyArgumentException(
                                    AccountMoveRequestXInfo.EXISTING_BAN, 
                                    "Error retrieving subAccounts for Account (BAN=" + request.getExistingBAN() + ")"));
                        }
                    }

                    
                    
                }
            }

            if (GroupTypeEnum.SUBSCRIBER.equals(request.getGroupType()))
            {
                boolean groupPricePlanExtensionExists = false;
                List<Extension> existingExtensions = new ArrayList<Extension>(oldAccount.getExtensions());
                for (Extension extension : existingExtensions)
                {
                    if (extension instanceof GroupPricePlanExtension)
                    {
                        groupPricePlanExtensionExists = true;
                    }
                    break;
                }
                
                if (groupPricePlanExtensionExists)
                {
                    cise.thrown(new IllegalPropertyArgumentException(
                            ConvertAccountGroupTypeRequestXInfo.ACCOUNT_EXTENSIONS, 
                            "Group Price Plan Extension should be removed before account can be converted to individual."));
                }
    
            }
        }
    }
    
    
    private void validateNonResponsibleActivePooledAccountsUnder(Context ctx, CABTR request, Account account, CompoundIllegalStateException cise) throws HomeException
    {
        Collection<Account> subAccounts = AccountSupport.getNonResponsibleAccounts(ctx, account);
        
        for (Account subAccount : subAccounts)
        {
            if (!AccountStateEnum.INACTIVE.equals(subAccount.getState()) && subAccount.isPooled(ctx))
            {
                cise.thrown(new IllegalPropertyArgumentException(
                        AccountMoveRequestXInfo.EXISTING_BAN, 
                        "Account (BAN=" + request.getExistingBAN() + ") has non responsible group pooled accounts under it and therefore cannot be converted to group pooled."));
                break;
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
