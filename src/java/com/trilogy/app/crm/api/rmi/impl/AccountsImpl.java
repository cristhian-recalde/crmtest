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
package com.trilogy.app.crm.api.rmi.impl;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;


import com.trilogy.app.crm.account.state.AccountStateTransitionSupport;
import com.trilogy.app.crm.api.ApiSupport;
import com.trilogy.app.crm.api.Constants;
import com.trilogy.app.crm.api.queryexecutor.QueryExecutorFactory;
import com.trilogy.app.crm.api.rmi.AccountCreationTemplateToApiAdapter;
import com.trilogy.app.crm.api.rmi.AccountExtensionToApiAdapter;
import com.trilogy.app.crm.api.rmi.AccountExtensionToApiReferenceAdapter;
import com.trilogy.app.crm.api.rmi.AccountRoleToApiAdapter;
import com.trilogy.app.crm.api.rmi.AccountToApiAdapter;
import com.trilogy.app.crm.api.rmi.AccountTypeToApiAdapter;
import com.trilogy.app.crm.api.rmi.AgedDebtToApiAdapter;
import com.trilogy.app.crm.api.rmi.GenericParameterParser;
import com.trilogy.app.crm.api.rmi.SubscriberToApiAdapter;
import com.trilogy.app.crm.api.rmi.support.APIGenericParameterSupport;
import com.trilogy.app.crm.api.rmi.support.AccountsApiSupport;
import com.trilogy.app.crm.api.rmi.support.CardPackageApiSupport;
import com.trilogy.app.crm.api.rmi.support.ExtensionApiSupport;
import com.trilogy.app.crm.api.rmi.support.MobileNumbersApiSupport;
import com.trilogy.app.crm.api.rmi.support.RmiApiErrorHandlingSupport;
import com.trilogy.app.crm.api.rmi.support.RmiApiSupport;
import com.trilogy.app.crm.api.rmi.support.SubscribersApiSupport;
import com.trilogy.app.crm.bas.tps.ServiceActivationTemplate;
import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountCategory;
import com.trilogy.app.crm.bean.AccountCategoryXInfo;
import com.trilogy.app.crm.bean.AccountCreationTemplate;
import com.trilogy.app.crm.bean.AccountCreationTemplateXInfo;
import com.trilogy.app.crm.bean.AccountHome;
import com.trilogy.app.crm.bean.AccountStateEnum;
import com.trilogy.app.crm.bean.AccountUsage;
import com.trilogy.app.crm.bean.AccountXInfo;
import com.trilogy.app.crm.bean.AccountsDiscount;
import com.trilogy.app.crm.bean.AccountsDiscountXInfo;
import com.trilogy.app.crm.bean.Address;
import com.trilogy.app.crm.bean.AddressTypeEnum;
import com.trilogy.app.crm.bean.AgedDebt;
import com.trilogy.app.crm.bean.BillCycleChangeStatusEnum;
import com.trilogy.app.crm.bean.BillCycleHistory;
import com.trilogy.app.crm.bean.BillCycleXInfo;
import com.trilogy.app.crm.bean.BlackTypeEnum;
import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.ChargingCycleEnum;
import com.trilogy.app.crm.bean.MsisdnHome;
import com.trilogy.app.crm.bean.ServiceProvisionStatusEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.bean.account.AccountIdentificationXInfo;
import com.trilogy.app.crm.bean.account.AccountRole;
import com.trilogy.app.crm.bean.account.Contact;
import com.trilogy.app.crm.bean.account.ContactXInfo;
import com.trilogy.app.crm.bean.account.SecurityQuestionAnswer;
import com.trilogy.app.crm.bean.core.BundleFee;
import com.trilogy.app.crm.bean.core.Msisdn;
import com.trilogy.app.crm.bean.core.SubscriptionClass;
import com.trilogy.app.crm.bean.core.SubscriptionType;
import com.trilogy.app.crm.blacklist.BlackListSupport;
import com.trilogy.app.crm.extension.account.AccountExtension;
import com.trilogy.app.crm.extension.account.AccountExtensionXInfo;
import com.trilogy.app.crm.extension.account.GroupPricePlanExtension;
import com.trilogy.app.crm.extension.account.PoolExtension;
import com.trilogy.app.crm.extension.account.SubscriptionPoolProperty;
import com.trilogy.app.crm.home.MsisdnPortHandlingHome;
import com.trilogy.app.crm.home.sub.Claim;
import com.trilogy.app.crm.move.MoveManager;
import com.trilogy.app.crm.move.MoveRequest;
import com.trilogy.app.crm.move.request.AccountMoveRequest;
import com.trilogy.app.crm.move.request.PostpaidServiceBasedSubscriberAccountMoveRequest;
import com.trilogy.app.crm.move.request.PrepaidPooledSubscriberAccountMoveRequest;
import com.trilogy.app.crm.move.support.MoveRequestSupport;
import com.trilogy.app.crm.numbermgn.GenericPackage;
import com.trilogy.app.crm.numbermgn.MsisdnAlreadyAcquiredException;
import com.trilogy.app.crm.numbermgn.MsisdnManagement;
import com.trilogy.app.crm.service.ChargingCycleHandler;
import com.trilogy.app.crm.support.AccountSupport;
import com.trilogy.app.crm.support.AccountTypeSupportHelper;
import com.trilogy.app.crm.support.BillCycleHistorySupport;
import com.trilogy.app.crm.support.CalendarSupport;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.app.crm.support.ChargingCycleSupportHelper;
import com.trilogy.app.crm.support.CollectionSupportHelper;
import com.trilogy.app.crm.support.ExtensionSupportHelper;
import com.trilogy.app.crm.support.HomeSupport;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.app.crm.support.MsisdnSupport;
import com.trilogy.app.crm.support.SpidSupport;
import com.trilogy.app.crm.support.SubscriberSupport;
import com.trilogy.app.crm.technology.TechnologyEnum;
import com.trilogy.app.crm.util.StringUtil;
import com.trilogy.framework.xhome.beans.ComparableComparator;
import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.beans.ReverseComparator;
import com.trilogy.framework.xhome.beans.SafetyUtil;
import com.trilogy.framework.xhome.beans.XBeans;
import com.trilogy.framework.xhome.beans.xi.XInfo;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAware;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.EQIC;
import com.trilogy.framework.xhome.elang.True;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.OrderByHome;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.InfoLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MajorLogMsg;
import com.trilogy.framework.xlog.log.MinorLogMsg;
import com.trilogy.util.crmapi.wsdl.v2_0.exception.ValidationExceptionEntry;
import com.trilogy.util.crmapi.wsdl.v2_0.types.GenericParameter;
import com.trilogy.util.crmapi.wsdl.v2_0.types.PaidType;
import com.trilogy.util.crmapi.wsdl.v2_0.types.PaidTypeEnum;
import com.trilogy.util.crmapi.wsdl.v2_0.types.SuccessCode;
import com.trilogy.util.crmapi.wsdl.v2_0.types.SuccessCodeEnum;
import com.trilogy.util.crmapi.wsdl.v2_0.types.SystemType;
import com.trilogy.util.crmapi.wsdl.v2_0.types.TechnologyType;
import com.trilogy.util.crmapi.wsdl.v2_1.exception.ExceptionCode;
import com.trilogy.util.crmapi.wsdl.v2_1.types.account.AccountBankInfo;
import com.trilogy.util.crmapi.wsdl.v2_1.types.account.AccountCompanyContactInfo;
import com.trilogy.util.crmapi.wsdl.v2_1.types.account.AccountContactInfo;
import com.trilogy.util.crmapi.wsdl.v2_1.types.account.AccountProfile;
import com.trilogy.util.crmapi.wsdl.v2_1.types.account.AccountRoleReference;
import com.trilogy.util.crmapi.wsdl.v2_1.types.account.AccountState;
import com.trilogy.util.crmapi.wsdl.v2_1.types.account.MutableAccountBankInfo;
import com.trilogy.util.crmapi.wsdl.v2_1.types.account.MutableAccountCompanyContactInfo;
import com.trilogy.util.crmapi.wsdl.v2_1.types.account.MutableAccountContactInfo;
import com.trilogy.util.crmapi.wsdl.v2_1.types.account.MutableAccountProfile;
import com.trilogy.util.crmapi.wsdl.v2_1.types.cardpackage.CardPackage;
import com.trilogy.util.crmapi.wsdl.v2_1.types.subscription.SubscriptionBilling;
import com.trilogy.util.crmapi.wsdl.v2_1.types.subscription.SubscriptionStatus;
import com.trilogy.util.crmapi.wsdl.v2_2.types.account.AccountBalance;
import com.trilogy.util.crmapi.wsdl.v2_2.types.account.BaseAccountExtensionReference;
import com.trilogy.util.crmapi.wsdl.v2_2.types.account.BaseMutableAccountExtension;
import com.trilogy.util.crmapi.wsdl.v2_2.types.account.MutableAccountDetail;
import com.trilogy.util.crmapi.wsdl.v2_2.types.account.PostpaidAccountBalance;
import com.trilogy.util.crmapi.wsdl.v2_2.types.account.extensions.PoolAccountExtensionReference;
import com.trilogy.util.crmapi.wsdl.v2_2.types.subscription.SubscriptionProfile;
import com.trilogy.util.crmapi.wsdl.v3_0.api.AccountServiceSkeletonInterface;
import com.trilogy.util.crmapi.wsdl.v3_0.api.CRMExceptionFault;
import com.trilogy.util.crmapi.wsdl.v3_0.api.types.account.DoInternalCreditCheckResponse;
import com.trilogy.util.crmapi.wsdl.v3_0.types.CRMRequestHeader;
import com.trilogy.util.crmapi.wsdl.v3_0.types.ExecuteResult;
import com.trilogy.util.crmapi.wsdl.v3_0.types.GenericResponse;
import com.trilogy.util.crmapi.wsdl.v3_0.types.account.AccountAgedDebt;
import com.trilogy.util.crmapi.wsdl.v3_0.types.account.AccountBillCycleActivity;
import com.trilogy.util.crmapi.wsdl.v3_0.types.account.AccountBilling;
import com.trilogy.util.crmapi.wsdl.v3_0.types.account.AccountCreationTemplateReference;
import com.trilogy.util.crmapi.wsdl.v3_0.types.account.AccountCugQueryResults;
import com.trilogy.util.crmapi.wsdl.v3_0.types.account.AccountDetail;
import com.trilogy.util.crmapi.wsdl.v3_0.types.account.AccountFinancialInfo;
import com.trilogy.util.crmapi.wsdl.v3_0.types.account.AccountFinInfo;
import com.trilogy.util.crmapi.wsdl.v3_0.types.account.AccountForIDResults;
import com.trilogy.util.crmapi.wsdl.v3_0.types.account.AccountIdentification;
import com.trilogy.util.crmapi.wsdl.v3_0.types.account.AccountPaymentInfo;
import com.trilogy.util.crmapi.wsdl.v3_0.types.account.AccountProfileQueryResults;
import com.trilogy.util.crmapi.wsdl.v3_0.types.account.AccountProfileQueryResultsV2;
import com.trilogy.util.crmapi.wsdl.v3_0.types.account.AccountProfileWithServiceAddressQueryResults;
import com.trilogy.util.crmapi.wsdl.v3_0.types.account.AccountReference;
import com.trilogy.util.crmapi.wsdl.v3_0.types.account.AccountStateTransitionResult;
import com.trilogy.util.crmapi.wsdl.v3_0.types.account.AccountTypeReference;
import com.trilogy.util.crmapi.wsdl.v3_0.types.account.AgedDebtPeriodDetail;
import com.trilogy.util.crmapi.wsdl.v3_0.types.account.BaseAccountExtension;
import com.trilogy.util.crmapi.wsdl.v3_0.types.account.BaseAccountExtensionSequence_type0;
import com.trilogy.util.crmapi.wsdl.v3_0.types.account.BaseReadOnlyAccountExtension;
import com.trilogy.util.crmapi.wsdl.v3_0.types.account.BaseReadOnlyAccountExtensionSequence_type0;
import com.trilogy.util.crmapi.wsdl.v3_0.types.account.BaseReadOnlyAccountExtensionSequence_type3;
import com.trilogy.util.crmapi.wsdl.v3_0.types.account.ConvertBillingTypeRequest;
import com.trilogy.util.crmapi.wsdl.v3_0.types.account.ConvertBillingTypeResponse;
import com.trilogy.util.crmapi.wsdl.v3_0.types.account.DirectDebitInitiationResponse;
import com.trilogy.util.crmapi.wsdl.v3_0.types.account.DirectDebitInitiationResultTypeEnum;
import com.trilogy.util.crmapi.wsdl.v3_0.types.account.DuplicateAccountInfo;
import com.trilogy.util.crmapi.wsdl.v3_0.types.account.MutableAccountBilling;
import com.trilogy.util.crmapi.wsdl.v3_0.types.account.MutableAccountIdentification;
import com.trilogy.util.crmapi.wsdl.v3_0.types.account.MutableAccountPaymentInfo;
import com.trilogy.util.crmapi.wsdl.v3_0.types.account.ServiceAddressInput;
import com.trilogy.util.crmapi.wsdl.v3_0.types.account.extensions.PoolAccountExtension;
import com.trilogy.util.crmapi.wsdl.v3_0.types.account.extensions.ReadOnlyAccountExtension;
import com.trilogy.util.crmapi.wsdl.v3_0.types.account.extensions.ReadOnlyPoolAccountExtension;
import com.trilogy.util.crmapi.wsdl.v3_0.types.generalprovisioning.BlacklistStatus;
import com.trilogy.util.crmapi.wsdl.v3_0.types.generalprovisioning.IdentificationEntry;
import com.trilogy.util.crmapi.wsdl.v3_0.types.subscription.BaseSubscriptionExtension;
import com.trilogy.util.crmapi.wsdl.v3_0.types.subscription.SubscriptionPricePlan;
import com.trilogy.util.crmapi.wsdl.v3_0.types.subscription.SubscriptionRating;
import com.trilogy.util.crmapi.wsdl.v3_0.types.subscription.SubscriptionReference;

import FinancialAccount;
import FinancialAccountException;
import FinancialAccountSupport;



/**
 * Implementation of Accounts API interface.
 *
 * @author victor.stratan@redknee.com
 */
public class AccountsImpl implements AccountServiceSkeletonInterface, ContextAware
{
	
    /**
     * 
     */
    public static final String PROMISE_TO_PAY_DATE = "PromiseToPayDate";
    
    /**
     * 
     */
    public static final String START_DATE = "StartDate";

    /**
     * 
     */
    public static final String END_DATE = "EndDate";

    /**
     * 
     */
    public static final String COLLECTION_AGENCY_ID = "CollectionAgencyID";

	/**
     * Serial version UID.
     */
    private static final long serialVersionUID = 1L;

	public static final String BLACKLIST_OVERRIDE = "OverrideBlacklist";
	public static final String SECONDARY_EMAIL_ADDRESS = "SecondaryEmailAddress";
	public static final String ACCOUNT_TYPE = "AccountType";
	public static final String CSA = "CSA";
	public static final String IS_REMOVE_IDENTIFICATIONS = "IsRemoveIdentifications";
	public static final String DISCOUNT_CLASS_INFO = "discountClassInfo";
	public static final int SERVICE_LEVEL_DISCOUNT = -2 ;
	public static final String DISCOUNT_GRADE ="discountGrade";
	
		
    /**
     * Create a new instance of <code>AccountsImpl</code>.
     *
     * @param ctx
     *            The operating context.
     * @throws RemoteException
     *             Thrown by RMI.
     */
    public AccountsImpl(final Context ctx)
    {
        this.context_ = ctx;
        this.accountTypeToApiAdapter_ = new AccountTypeToApiAdapter();
        this.accountToApiAdapter_ = new AccountToApiAdapter();
        this.accountCreationTemplateToApiAdapter_ = new AccountCreationTemplateToApiAdapter();
        this.accountRoleToApiAdapter_ = new AccountRoleToApiAdapter();
        this.accountExtensionToApiAdapter_ = new AccountExtensionToApiAdapter();
        this.accountExtensionToApiReferenceAdapter_ = new AccountExtensionToApiReferenceAdapter();
		this.agedDebtToApiAdapter_ = new AgedDebtToApiAdapter();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public AccountCreationTemplateReference[] listAccountCreationTemplates(CRMRequestHeader header, int spid,
            Boolean isAscending, GenericParameter[] parameters) throws CRMExceptionFault
    {
        final Context ctx = getContext().createSubContext();
        // handle login. Failed logins throw exceptions
        RmiApiSupport.authenticateUser(ctx, header, "listAccountCreationTemplates",
            Constants.PERMISSION_ACCOUNTS_READ_LISTACCOUNTCREATIONTEMPLATES);

        RmiApiSupport.getCrmServiceProvider(ctx, spid, this);

        AccountCreationTemplateReference[] actReferences = new AccountCreationTemplateReference[] {};
        try
        {
            final Object condition = new EQ(AccountCreationTemplateXInfo.SPID, spid);
            
            Collection<AccountCreationTemplate> collection = HomeSupportHelper.get(ctx).getBeans(
                    ctx, 
                    AccountCreationTemplate.class, 
                    condition, 
                    RmiApiSupport.isSortAscending(isAscending));
            
            actReferences = CollectionSupportHelper.get(ctx).adaptCollection(
                    ctx, 
                    collection, 
                    this.accountCreationTemplateToApiAdapter_, 
                    actReferences);
        }
        catch (final Exception e)
        {
            final String msg = "Unable to retrieve Account Creation Templates";
            RmiApiErrorHandlingSupport.handleQueryExceptions(ctx, e, msg, this);
        }

        return actReferences;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public AccountTypeReference[] listAccountTypes(final CRMRequestHeader header, final int spid, Boolean isAscending, GenericParameter[] parameters)
        throws CRMExceptionFault
    {
        final Context ctx = getContext().createSubContext();
        // handle login. Failed logins throw exceptions
        RmiApiSupport.authenticateUser(ctx, header, "listAccountTypes",
            Constants.PERMISSION_ACCOUNTS_READ_LISTACCOUNTTYPES);

        RmiApiSupport.getCrmServiceProvider(ctx, spid, this);

        AccountTypeReference[] accountTypeReferences = new AccountTypeReference[] {};
        try
        {
			final Object condition = new EQ(AccountCategoryXInfo.SPID, spid);
            
			Collection<AccountCategory> collection =
			    HomeSupportHelper.get(ctx).getBeans(
                    ctx, 
			        AccountTypeSupportHelper.get(ctx).getAccountTypeClass(ctx),
                    condition, 
                    RmiApiSupport.isSortAscending(isAscending));
            
            ctx.put(RmiApiSupport.API_SPID, spid);
            accountTypeReferences = CollectionSupportHelper.get(ctx).adaptCollection(
                    ctx, 
                    collection, 
                    this.accountTypeToApiAdapter_, 
                    accountTypeReferences);
        }
        catch (final Exception e)
        {
            final String msg = "Unable to retrieve Account Types";
            RmiApiErrorHandlingSupport.handleQueryExceptions(ctx, e, msg, this);
        }

        return accountTypeReferences;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Long getAccountBalance(final CRMRequestHeader header, final String accountID, GenericParameter[] parameters) throws CRMExceptionFault
    {
        final Context ctx = getContext().createSubContext();
        // handle login. Failed logins throw exceptions
        RmiApiSupport.authenticateUser(ctx, header, "getAccountBalance",
            Constants.PERMISSION_ACCOUNTS_READ_GETACCOUNTBALANCE);

        RmiApiErrorHandlingSupport.validateMandatoryObject(accountID, "accountID");
        final Account account = getCrmAccount(ctx, accountID, this);

        long result = 0L;
        try
        {
            final AccountUsage accountUsage = account.getAccountUsage(ctx);
            result = accountUsage.getBalance();
        }
        catch (final Exception e)
        {
            final String msg = "Unable to retrieve Balance for Account " + accountID;
            RmiApiErrorHandlingSupport.handleQueryExceptions(ctx, e, msg, this);
        }

        return Long.valueOf(result);
    }
    

    /**
     * {@inheritDoc}
     */
    @Override
    public AccountBalance getDetailedAccountBalances(final CRMRequestHeader header, final String accountID,
            final Boolean ascendToResponsible, GenericParameter[] parameters) throws CRMExceptionFault
    {
        final Context ctx = getContext().createSubContext();
        // handle login. Failed logins throw exceptions
        RmiApiSupport.authenticateUser(ctx, header, "getDetailedAccountBalances",
            Constants.PERMISSION_ACCOUNTS_READ_GETDETAILEDACCOUNTBALANCES);
        RmiApiErrorHandlingSupport.validateMandatoryObject(accountID, "accountID");
        final Account account = getCrmAccount(ctx, accountID, this);
        
        Account lookupAccount = account;
        
        if (ascendToResponsible != null && ascendToResponsible)
        {
            try
            {
                lookupAccount = account.getResponsibleParentAccount(ctx);
            }
            catch (HomeException exception)
            {
                final String msg = "CRM failed to look up the responsible account for account " + accountID;
                RmiApiErrorHandlingSupport.handleQueryExceptions(ctx, exception, msg, this);
            }
        }
        
        PostpaidAccountBalance balance = new PostpaidAccountBalance();

        if (! lookupAccount.isPrepaid())
        {
			AccountUsage usage = lookupAccount.getAccountUsage(ctx);
			if (usage.getOtherAdjustments() == AccountSupport.INVALID_VALUE
			    || usage.getAmountDue() == AccountSupport.INVALID_VALUE
			    || usage.getPayment() == AccountSupport.INVALID_VALUE)
			{
				final String msg =
				    "CRM cannot retrieve one or more balances from external application for account "
				        + accountID;
				RmiApiErrorHandlingSupport.handleQueryExceptions(ctx, null,
				    msg, this);
			}
            balance.setAdjustmentsSinceLastInvoice(Long.valueOf(usage.getOtherAdjustments()));
            balance.setLastInvoiceAmount(Long.valueOf(usage.getAmountDue()));
            balance.setPaymentsSinceLastInvoice(Long.valueOf(usage.getPayment()));
        }
        else
        {
            final String msg = "getDetailedAccountBalances is not supported for Prepaid accounts";
            RmiApiErrorHandlingSupport.handleQueryExceptions(ctx, null, msg, this);
            
        }

        balance.setWrittenOffBalance(Long.valueOf(0));
        return balance;
    }

    

    /**
     * {@inheritDoc}
     */
    @Override
    public AccountReference[] listSubAccounts(final CRMRequestHeader header, final String accountID,
        final boolean recurse, final boolean responsible, Boolean isAscending, GenericParameter[] parameters) throws CRMExceptionFault
    {
        final Context ctx = getContext().createSubContext();
        // handle login. Failed logins throw exceptions
        RmiApiSupport.authenticateUser(ctx, header, "listSubAccounts",
            Constants.PERMISSION_ACCOUNTS_READ_LISTSUBACCOUNTS);

        RmiApiErrorHandlingSupport.validateMandatoryObject(accountID, "accountID");

        final Account account = getCrmAccount(ctx, accountID, this);

        final Collection collection = getCrmSubAccounts(ctx, account, recurse, responsible, isAscending, parameters);
        
        AccountReference[] subAccountReferences = new AccountReference[]{};
        try
        {
            subAccountReferences = CollectionSupportHelper.get(ctx).adaptCollection(
                    ctx, 
                    collection, 
                    this.accountToApiAdapter_, 
                    subAccountReferences);
        }
        catch (final Exception e)
        {
            final String msg = "Unable to retrieve Sub Accounts for Account " + accountID;
            RmiApiErrorHandlingSupport.handleQueryExceptions(ctx, e, msg, this);
        }

        return subAccountReferences;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public AccountBillCycleActivity updateAccountBillCycle(final CRMRequestHeader header, final String accountID,
            final long billCycleID, GenericParameter[] parameters) throws CRMExceptionFault
    {
        final Context ctx = getContext().createSubContext();
        // handle login. Failed logins throw exceptions
        RmiApiSupport.authenticateUser(ctx, header, "updateAccountBillCycle",
                Constants.PERMISSION_ACCOUNTS_WRITE_UPDATEACCOUNTBILLCYCLE);
        RmiApiErrorHandlingSupport.validateMandatoryObject(accountID, "accountID");
        final Account account = getCrmAccount(ctx, accountID, this);

        com.redknee.app.crm.bean.BillCycle newBillCycle =
            validateUpdateAccountBillCycle(ctx, account, billCycleID);
        com.redknee.app.crm.bean.BillCycle oldBillCycle = null;
        try
        {
            oldBillCycle = account.getBillCycle(ctx);
        }
        catch (final Exception e)
        {
            final String msg =
                "Unable to update Bill Cycle for Account " + accountID;
            RmiApiErrorHandlingSupport.handleStoreExceptions(ctx, e, msg, this);
        }

        AccountBillCycleActivity result = new AccountBillCycleActivity();

        CalendarSupport calendarSupport = CalendarSupportHelper.get(ctx);
        
        BillCycleHistory lastChange =
            BillCycleHistorySupport.getLastEvent(ctx, account.getBAN(),
                    BillCycleChangeStatusEnum.COMPLETE);
        if (lastChange != null)
        {
            result.setPreviousBillCycleChange(calendarSupport
                    .dateToCalendar(lastChange.getBillCycleChangeDate()));
            result
            .setPreviousBillCycleID((long) lastChange.getOldBillCycleID());
        }

        result.setCurrentBillCycleID(Integer.valueOf(account.getBillCycleID()).longValue());

        Date nextAllowedDate =
            BillCycleHistorySupport.getNextAllowedRequestDate(ctx, account);
        if (nextAllowedDate != null)
        {
            result.setEarliestPermittedNewBillCycleChange(calendarSupport.dateToCalendar(nextAllowedDate));
        }

        if (account.getBillCycleID() == billCycleID)
        {
            BillCycleHistory pendingEvent = BillCycleHistorySupport.getLastEvent(ctx, account.getBAN(), BillCycleChangeStatusEnum.PENDING);
            if (pendingEvent != null)
            {
                try
                {
                    pendingEvent = (BillCycleHistory) pendingEvent.clone();
                }
                catch (CloneNotSupportedException e1)
                {
                    // NOP
                }
                
                // API request to change to current bill cycle ID cancels existing pending request
                pendingEvent.setBillCycleChangeDate(CalendarSupportHelper.get(ctx).getRunningDate(ctx));
                pendingEvent.setStatus(BillCycleChangeStatusEnum.CANCELLED);
                try
                {
                    pendingEvent = HomeSupportHelper.get(ctx).createBean(ctx, pendingEvent);
                }
                catch (final Exception e)
                {
                    final String msg = "Unable to cancel pending Bill Cycle change for Account " + accountID;
                    RmiApiErrorHandlingSupport.handleStoreExceptions(ctx, e, msg, this);
                }
            }
            return result;
        }

        BillCycleHistory history = null;
        try
        {
            history = (BillCycleHistory) XBeans.instantiate(BillCycleHistory.class, ctx);
        }
        catch (Exception e)
        {
            history = new BillCycleHistory();
        }

        history.setBillCycleChangeDate(new Date());
        history.setBAN(account.getBAN());
        history.setOldBillCycleID(account.getBillCycleID());
        history.setNewBillCycleID(newBillCycle.getBillCycleID());
        history.setStatus(BillCycleChangeStatusEnum.PENDING);
        try
        {
            history = HomeSupportHelper.get(ctx).createBean(ctx, history);
        }
        catch (final Exception e)
        {
            final String msg = "Unable to update Bill Cycle for Account " + accountID;
            RmiApiErrorHandlingSupport.handleStoreExceptions(ctx, e, msg, this);
        }

        result.setScheduledBillCycleID(billCycleID);

        BillCycleHistory lastEvent = BillCycleHistorySupport.getLastEvent(ctx, account.getBAN());
        if (lastEvent == null
                || !BillCycleChangeStatusEnum.COMPLETE.equals(lastEvent.getStatus()))
        {
            ChargingCycleHandler handler = ChargingCycleSupportHelper.get(ctx).getHandler(ChargingCycleEnum.MONTHLY);
            if (handler != null)
            {
                Date nextMonthDate = calendarSupport.findDateMonthsAfter(1, new Date());
                Date startOfNextOldBillCycle = handler.calculateCycleStartDate(ctx, nextMonthDate, history.getOldBillCycleDay(), account.getSpid());
                if (startOfNextOldBillCycle != null)
                {
                    result.setScheduledBillCycleChange(calendarSupport.dateToCalendar(startOfNextOldBillCycle));
                }
            }
        }
        else
        {
            result.setScheduledBillCycleChange(calendarSupport.dateToCalendar(lastEvent.getBillCycleChangeDate()));
        }

        return result;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public SuccessCode updateAccountParent(final CRMRequestHeader header, final String accountID,
            final String parentID, final Long newDepositAmount, final Boolean responsible,
            final Integer expiryExtention, GenericParameter[] parameters) throws CRMExceptionFault
    {
        /*
         * We don't want to inject wrapper home, because we changes to be propagated back to CRM
         */
        
        final Context ctx = getContext().createSubContext();
        // handle login. Failed logins throw exceptions
        RmiApiSupport.authenticateUser(ctx, header, "updateAccountParent",
            Constants.PERMISSION_ACCOUNTS_WRITE_UPDATEACCOUNTPARENT);
        RmiApiErrorHandlingSupport.validateMandatoryObject(accountID, "accountID");
        RmiApiErrorHandlingSupport.validateMandatoryObject(parentID, "parentID");

        final Account account = getCrmAccount(ctx, accountID, this);
                
        if (account.getParentBAN().equals(parentID))
        {
            // nothing to do
            return SuccessCodeEnum.SUCCESS.getValue();
        }

        if (responsible != null && account.isPrepaid())
        {
            final String msg = "Unable to change responsible flag for prepaid account " + accountID;
            RmiApiErrorHandlingSupport.simpleValidation("responsible", msg);
        }
        
        try
        {
            MoveRequest request = MoveRequestSupport.getMoveRequest(ctx, account);
            if (request instanceof AccountMoveRequest)
            {
                AccountMoveRequest accRequest = (AccountMoveRequest) request;
                accRequest.setNewParentBAN(parentID);
                
                if (request instanceof PostpaidServiceBasedSubscriberAccountMoveRequest)
                {
                    long depositValue = 0;
                    if (newDepositAmount != null)
                    {
                        depositValue = newDepositAmount.longValue();            
                    }

                    PostpaidServiceBasedSubscriberAccountMoveRequest postAccRequest = (PostpaidServiceBasedSubscriberAccountMoveRequest) accRequest;
                    postAccRequest.setNewDepositAmount(depositValue);
                }
                else if (request instanceof PrepaidPooledSubscriberAccountMoveRequest)
                {
                    int expiryExtValue = 0;
                    if (expiryExtention != null)
                    {
                        expiryExtValue = expiryExtention.intValue();            
                    }
                    PrepaidPooledSubscriberAccountMoveRequest preAccRequest = (PrepaidPooledSubscriberAccountMoveRequest) accRequest;
                    preAccRequest.setExpiryExtension(expiryExtValue);
                }
                
                if (responsible != null)
                {
                    accRequest.setNewResponsible(responsible);
                }

                new MoveManager().move(ctx, accRequest);
            }
            
        }
        catch (final Exception e)
        {
            final String msg = "Unable to update Parent for Account " + accountID;
            RmiApiErrorHandlingSupport.handleStoreExceptions(ctx, e, msg, this);
        }
        
        return SuccessCodeEnum.SUCCESS.getValue();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public GenericResponse updateAccountParentV2(final CRMRequestHeader header, final String accountID,
            final String parentID, final Long newDepositAmount, final Boolean responsible,
            final Integer expiryExtention, GenericParameter[] parameters) throws CRMExceptionFault
    {
        final Context ctx = getContext().createSubContext();

        QueryExecutorFactory executor = QueryExecutorFactory.getInstance();

        return executor.execute(ctx, AccountServiceSkeletonInterface.class.getSimpleName(), "updateAccountParentV2", GenericResponse.class, 
                header, accountID, parentID, newDepositAmount, responsible, expiryExtention, parameters);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SuccessCode updateAccountState(final CRMRequestHeader header, final String accountID, final AccountState state, final GenericParameter[] parameters)
        throws CRMExceptionFault
    {
        final Context ctx = getContext().createSubContext();

        QueryExecutorFactory executor = QueryExecutorFactory.getInstance();

        return executor.execute(ctx, AccountServiceSkeletonInterface.class.getSimpleName(), "updateAccountState", SuccessCode.class, 
                header, accountID, state, parameters);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public SuccessCode updateAccountVPNMobileNumber(final CRMRequestHeader header, final String accountID,
        final String mobileNumber, GenericParameter[] parameters) throws CRMExceptionFault
    {
        final Context ctx = getContext().createSubContext();
        // handle login. Failed logins throw exceptions
        RmiApiSupport.authenticateUser(ctx, header, "updateAccountVPNMobileNumber",
            Constants.PERMISSION_ACCOUNTS_WRITE_UPDATEACCOUNTVPNMOBILENUMBER);

        RmiApiErrorHandlingSupport.validateMandatoryObject(accountID, "accountID");
        
        
        final Account account = getCrmAccount(ctx, accountID, this);
        final Home home = updateAccountPipeline(ctx,"updateAccountVPNMobileNumber");

        validateUpdateAccountVpnMSISDN(ctx, account, mobileNumber);

        if (SafetyUtil.safeEquals(account.getVpnMSISDN(), mobileNumber))
        {
            // nothing to do
            return SuccessCodeEnum.SUCCESS.getValue();
        }

        try
        {
            // TODO validation on the pipeline needed
            account.setVpnMSISDN(mobileNumber);
            home.store(ctx, account);
        }
        catch (final Exception e)
        {
            final String msg = "Unable to update VPN MSISDN for Account " + accountID;
            RmiApiErrorHandlingSupport.handleStoreExceptions(ctx, e, msg, this);
        }
        
        return SuccessCodeEnum.SUCCESS.getValue();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public SuccessCode updateAccountProfile(final CRMRequestHeader header, final String accountID,
        final MutableAccountProfile profile, final MutableAccountDetail detail, final MutableAccountBilling billing,
        final MutableAccountPaymentInfo paymentInfo, final MutableAccountIdentification identification,
        final MutableAccountCompanyContactInfo company, final MutableAccountBankInfo bank,
        final MutableAccountContactInfo contact, GenericParameter[] parameters) throws CRMExceptionFault
    {
        final Context ctx = getContext().createSubContext();
        String csa =null;
        // handle login. Failed logins throw exceptions
        RmiApiSupport.authenticateUser(ctx, header, "updateAccountProfile",
            Constants.PERMISSION_ACCOUNTS_WRITE_UPDATEACCOUNTPROFILE);
        
        RmiApiErrorHandlingSupport.validateMandatoryObject(accountID, "accountID");

        Account account = getCrmAccount(ctx, accountID, this);
        
        Long accountType_apiVal = null;

        if (account.getDiscountClass() == SERVICE_LEVEL_DISCOUNT && billing!= null && billing.getDiscountClass()!=null) {
        	account.setDiscountsClassHolder(new HashSet());
        }
        
		if(parameters !=null){
			GenericParameterParser parser = new GenericParameterParser(parameters);
    		 if(parser.containsParam(SECONDARY_EMAIL_ADDRESS))
             {
                  account.setSecondaryEmailAddresses(parser.getParameter(SECONDARY_EMAIL_ADDRESS, String.class));
             }
    		 
    		 if(parser.containsParam(IS_REMOVE_IDENTIFICATIONS))
    		 {
    		     Boolean removeIdentifications = parser.getParameter(IS_REMOVE_IDENTIFICATIONS, Boolean.class);
    		     ctx.put(IS_REMOVE_IDENTIFICATIONS, removeIdentifications);
    		 }
    		 
    		 /**
    		  * Check if new discount class is present in generic parameters
    		  */
    		 if (parser.containsParam(DISCOUNT_CLASS_INFO)){

             	AccountsApiSupport.fillInAccountDiscountsHolder(account, parser);
             	if(account.getDiscountClass() >0){
             		account.setDiscountClass(SERVICE_LEVEL_DISCOUNT);
             }
             }
    		 
    		 if(parser.containsParam(DISCOUNT_GRADE))
             {
             	account.setDiscountGrade(parser.getParameter(DISCOUNT_GRADE, String.class));
             }
			accountType_apiVal = parser.getParameter(ACCOUNT_TYPE, Long.class);
			csa = parser.getParameter(CSA, String.class);
		}
	
        
        if (AccountStateEnum.INACTIVE.equals(account.getState()))
        {
            RmiApiErrorHandlingSupport.generalException(ctx, null, "Account is inactive and cannot be updated", ExceptionCode.INVALID_ACCOUNT_NUMBER, this);

        }
        
        try
        {
            account =  (Account)account.deepClone();
        }
        catch (CloneNotSupportedException e1)
        {
            new MinorLogMsg(this,"Unable to deep-clone Account of type: " + Account.class.getName(),e1).log(ctx);
        }
        final Home home = updateAccountPipeline(ctx,"updateAccountProfile");

        try
        {
            AccountsApiSupport.updateAccount(ctx, account, header, profile, detail, billing, paymentInfo,
                    identification, company, bank, contact);
            AccountsApiSupport.updateAccountType(ctx, account, accountType_apiVal);
        	

        	if(csa !=null)
        	{
        		account.setCsa(csa);
        	}
    			
            home.store(ctx, account);
        }
        catch (final CRMExceptionFault e)
        {
            throw e;
        }
        catch (final Exception e)
        {
            final String msg = "Unable to update profile for Account " + accountID;
            RmiApiErrorHandlingSupport.handleStoreExceptions(ctx, e, msg, this);
        }
        
        return SuccessCodeEnum.SUCCESS.getValue();
    }


    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
	@Override
    public AccountReference createAccount(CRMRequestHeader header, AccountProfile profile, AccountDetail detail,
            AccountBilling billing, AccountPaymentInfo paymentInfo, AccountIdentification identification,
            AccountCompanyContactInfo company, AccountBankInfo bank, AccountContactInfo contact,
            BaseAccountExtension[] extension, GenericParameter[] parameters) throws CRMExceptionFault
    {
        final Context ctx = getContext().createSubContext();
        // handle login. Failed logins throw exceptions
        RmiApiSupport.authenticateUser(ctx, header, "createAccount", Constants.PERMISSION_ACCOUNTS_WRITE_CREATEACCOUNT);
        final Home home = updateAccountPipeline(ctx,"createAccount");
        CRMSpid sp = null;
        if (profile != null)
        {
            sp = RmiApiSupport.getCrmServiceProvider(ctx, profile.getSpid(), this);
        }
        validateCreateAccount(profile, detail, billing, sp);
		BlackTypeEnum blacklistType = null;

		/*
		 * Blacklist override
		 */
		boolean blacklistOverride = false;
	
		String csa = null;
	
        Account returnedAccount = null;
        Account account = null;
        Map<String,String> guidIDMap = null;
        try
        {
        	
            account = AccountsApiSupport.constructAccount(ctx, header, profile, detail, billing,
                paymentInfo, identification, company, bank, contact,  extension);
            

            if(parameters !=null){
                GenericParameterParser parser = new GenericParameterParser(parameters);
                csa = parser.getParameter(CSA, String.class);
                
                if(parser.containsParam(SECONDARY_EMAIL_ADDRESS))
                {
                	account.setSecondaryEmailAddresses(parser.getParameter(SECONDARY_EMAIL_ADDRESS, String.class));
                }
                
                
                if (parser.containsParam(DISCOUNT_CLASS_INFO)){
                	AccountsApiSupport.fillInAccountDiscountsHolder(account, parser);
                }
                }
                
            if(csa !=null)
            {
            	account.setCsa(csa);
            }
            
			blacklistOverride =
			    APIGenericParameterSupport.getParameterBoolean(
			        BLACKLIST_OVERRIDE, parameters);
			
			

			if (blacklistOverride)
			{
				ctx.put(BlackListSupport.BYPASS_BLACKLIST_VALIDATION, true);
			}
			
			
			ctx.put(Account.class, account);
			
				if(account.getSecurityQuestionsAndAnswers().size() > 0){
		 	
						 List<SecurityQuestionAnswer> securityQuestionsList = account.getSecurityQuestionsAndAnswers();
						 List<SecurityQuestionAnswer> newSecurityQuestionsList = new ArrayList<SecurityQuestionAnswer>();
						 guidIDMap = new HashMap<String, String>();
						 for(SecurityQuestionAnswer secQue : securityQuestionsList){
							 if(secQue.getQuestion().contains(":")){
								 String guid_ques[] = secQue.getQuestion().split(":"); 
								 secQue.setQuestion(guid_ques[1]);
								 guidIDMap.put(guid_ques[1],guid_ques[0]);
								 newSecurityQuestionsList.add(secQue);
								 account.setSecurityQuestionsAndAnswers(newSecurityQuestionsList);
			            }
					}
				}
			
            returnedAccount = (Account) home.create(ctx, account);
			blacklistType =
			    BlackListSupport.getAccountBlacklistType(ctx, returnedAccount);
        }
        catch (final CRMExceptionFault e)
        {
            throw e;
        }
        catch (final Exception e)
        {
            String ban = account != null ? account.getBAN() : null;
            final String msg = "Unable to create Account " + (ban == null ? profile.getIdentifier() : ban);
            RmiApiErrorHandlingSupport.handleCreateExceptions(ctx, e, msg, false, Account.class, ban, this);
        }

        if (returnedAccount == null)
        {
            final String msg = "Unable to create Account " + profile.getIdentifier()
                + ". No Account returned from processing.";
            RmiApiErrorHandlingSupport.handleCreateExceptions(ctx, null, msg, false, Account.class, null, this);
        }

        final AccountReference result = AccountsApiSupport.adaptAccountToReference(returnedAccount);
        
        if(billing.getBillCycle() == -1 && account.isPostpaid()) 
        {
            result.addParameters(APIGenericParameterSupport
                    .getAccountBillCycleId(ctx, account));
        }
        
		if (blacklistType != null)
		{
			result.addParameters(APIGenericParameterSupport
			    .getAccountBlacklistStatus(ctx, blacklistType));
		}
		
		if(guidIDMap != null && guidIDMap.size() > 0 )
		{
		 	guidIDMap = com.redknee.app.crm.api.rmi.support.AccountsApiSupport.updateGUIDMap(ctx,returnedAccount,guidIDMap);
		 	result.addParameters(APIGenericParameterSupport.getSecurityQuesGUID(ctx,guidIDMap));
		}
		
        return result;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public SubscriptionReference createIndividualSubscriber(final CRMRequestHeader header, Long templateID,
            AccountProfile accountProfile, AccountDetail detail, AccountBilling accountBilling,
            AccountPaymentInfo paymentInfo, AccountIdentification identification, AccountCompanyContactInfo company,
            AccountBankInfo bank, AccountContactInfo contact, BaseAccountExtension[] extension,
            SubscriptionProfile subProfile, SubscriptionStatus status, SubscriptionRating rating,
            SubscriptionBilling subscriptionBilling, CardPackage cardPackage, SubscriptionPricePlan options,
            BaseSubscriptionExtension[] subscriptionExtension, GenericParameter[] parameters) throws CRMExceptionFault
    {
        final Context ctx = getContext().createSubContext();
        // handle login. Failed logins throw exceptions
        RmiApiSupport.authenticateUser(ctx, header, "createIndividualSubscriber",
                Constants.PERMISSION_ACCOUNTS_WRITE_CREATEINDIVIDUALSUBSCRIBER,
                Constants.PERMISSION_SUBSCRIBERS_WRITE_CREATEINDIVIDUALSUBSCRIBER);  

        final Home acctHome = updateAccountPipeline(ctx,"createIndividualSubscriber");
        com.redknee.app.crm.bean.core.Msisdn createdMsisdn = null;
        GenericPackage createdCard = null;

        Date requestStartTime = new Date();
        
        boolean subscriberCreated = false;
        Account resultAccount = null;
        Subscriber resultSub = null;
        try
        {
            CRMSpid sp = null;
            if (accountProfile != null)
            {
                // account spid overrides subscriber spid
                sp = RmiApiSupport.getCrmServiceProvider(ctx, accountProfile.getSpid(), this);
            }
            
            TechnologyEnum tech = null;
            com.redknee.app.crm.bean.core.Msisdn number = null;

            final PaidType paidType = PaidTypeEnum.valueOf(detail.getSystemType().getValue());
            if (paidType.getValue() >= PaidTypeEnum.UNSPECIFIED.getValue().getValue())
            {
                RmiApiErrorHandlingSupport.simpleValidation(
                        "systemType", 
                        SystemType.class.getName() + " of " + paidType + " is not supported for individual subscriber ");
            }
            
            if (subProfile != null)
            {
                if (subProfile.getTechnologyType() != null)
                {
                    tech = RmiApiSupport.convertApiTechnology2Crm(subProfile.getTechnologyType());
                }
                if (subProfile.getMobileNumber() != null)
                {
                    number = MobileNumbersApiSupport.getCrmMsisdn(ctx, subProfile.getMobileNumber(), this);
                }
                
                if (!SubscriptionType.isSubscriptionTypeExisting(ctx, subProfile.getSubscriptionType()))
                {
                    String msg = "Subscription Type " + subProfile.getSubscriptionType();
                    RmiApiErrorHandlingSupport.identificationException(ctx, msg, SubscribersApiSupport.class.getName());
                }
                
                if (!SubscriptionClass.isSubscriptionClassExisting(ctx, subProfile.getSubscriptionClass()))
                {
                    String msg = "Subscription Class " + subProfile.getSubscriptionClass();
                    RmiApiErrorHandlingSupport.identificationException(ctx, msg, SubscribersApiSupport.class.getName());
                }

                // account type overrides subscriber type
                subProfile.setPaidType(paidType);

                // this is needed so that subscriber template applies in the helper method
                subProfile.setSpid(accountProfile.getSpid());
            }

            validateCreateAccount(accountProfile, detail, accountBilling, sp);

            final Account account = AccountsApiSupport.constructAccount(ctx, header, accountProfile, detail,
                accountBilling, paymentInfo, identification, company, bank, contact, extension);

            if (!account.isIndividual(ctx))
            {
                RmiApiErrorHandlingSupport
                    .simpleValidation(
                        "detail.accountType",
                        "Individual Subscribers can only be associated with an Individual Subscriber Account. Account Type has to be a Subscriber account type.");
            }

            SubscribersApiSupport.validateCreateSubscription(ctx, subProfile, status, rating, subscriptionBilling, number, cardPackage, sp, true, options);
            
            Long monthlySpendLimit = null;
            Long overdraftBalanceLimit = null;
            Date startDate = null;
            Date endDate = null;
            Boolean portIn = null;
            Long deviceType = null;
            Integer activationReasonCode = null;
                    
            String deviceName = null;
            String deviceImei = null;
            Long devicePrice = null;
            
            //Get parameters so that they can be validated before any action is performed.
            if (parameters!=null)
            {
                GenericParameterParser parser = new GenericParameterParser(parameters);
                
                monthlySpendLimit = parser.getParameter(SubscribersImpl.MONTHLY_SPEND_LIMIT, Long.class);
                overdraftBalanceLimit = parser.getParameter(SubscribersImpl.OVERDRAFT_BALANCE_LIMIT, Long.class);
                startDate = parser.getParameter(START_DATE, Date.class);
                endDate = parser.getParameter(END_DATE, Date.class);
                portIn = parser.getParameter(APIGenericParameterSupport.PORT_IN,Boolean.class);
                deviceType = parser.getParameter(APIGenericParameterSupport.DEVICE_TYPE, Long.class);
                activationReasonCode = parser.getParameter(SubscribersImpl.ACTIVATION_REASON_CODE, Integer.class);
                
                deviceName =  parser.getParameter(APIGenericParameterSupport.DEVICE_MODEL_NAME, String.class);
                devicePrice =  parser.getParameter(APIGenericParameterSupport.DEVICE_LIST_PRICE, Long.class);
                deviceImei =  parser.getParameter(APIGenericParameterSupport.IMEI, String.class);
                
                /**
                 * Check if new discount class is present in generic parameters
                 */
                if (parser.containsParam(DISCOUNT_CLASS_INFO)){
                	AccountsApiSupport.fillInAccountDiscountsHolder(account, parser);
            }
            }
            
            final SubscriberTypeEnum type;
            // account type overrides subscriber type
            type = RmiApiSupport.convertApiPaidType2CrmSubscriberType(paidType);

            if (SubscriberTypeEnum.PREPAID.equals(type) && templateID != null)
            {
                // retrieve to validate existence
                final ServiceActivationTemplate sat = SubscribersApiSupport.getCrmCreationTemplate(ctx, templateID.longValue());
                if (sat.getSpid() != sp.getId())
                {
                    RmiApiErrorHandlingSupport.simpleValidation("profile.spid",
                        "CreationTemplate and subscriber should be on the same Service Provider.");
                }
            }
            
            if(Boolean.TRUE.equals(portIn))
            {
                ctx.put(MsisdnPortHandlingHome.MSISDN_PORT_KEY, subProfile.getMobileNumber());
            }
            
            
            // handle non existent MSISDNS
            if (number == null)
            {
                createdMsisdn = MobileNumbersApiSupport.createMobileNumber(ctx, subProfile.getMobileNumber(),  Boolean.TRUE.equals(portIn) ,type, sp, tech,
                    this);
            }
            else
            {
                if(Boolean.TRUE.equals(portIn))
                {
                    MsisdnManagement.markMsisdnPortedIn(ctx,subProfile.getAccountID() ,number.getMsisdn());
                }
                Claim.validateMsisdnTypeAndAvailable(ctx, subProfile.getAccountID(), type, subProfile.getMobileNumber(), "voiceMsisdn", false);
            }

            // handle auto create card packages
            if (cardPackage != null)
            {
                final TechnologyType apiTech = subProfile.getTechnologyType();
                CardPackageApiSupport.prepareCardPackage(cardPackage, accountProfile.getSpid(), apiTech);
                createdCard = CardPackageApiSupport.createCrmCardPackage(ctx, cardPackage, this);
                subProfile.setCardPackageID(createdCard.getPackId());
            }
            
            final Home subHome = SubscribersImpl.getSubscriberHome(ctx);
            final Home accountHome = updateAccountPipeline(ctx, "createIndividualSubscriber");
            resultAccount = (Account) accountHome.create(ctx, account);

            ctx.put(Account.class, resultAccount);
            
            final Subscriber sub = SubscribersApiSupport.constructIndividualSubscriber(ctx, templateID, subProfile,
                    status, rating, subscriptionBilling, sp, tech, options);

            sub.setBAN(resultAccount.getBAN());
            
            // Setting values captured from generic parameters
            if (startDate != null)
            {
                sub.setStartDate(startDate);
            }
            
            if (endDate != null)
            {
                sub.setEndDate(endDate);
            }
            
            if (monthlySpendLimit != null)
            {
                sub.setMonthlySpendLimit(monthlySpendLimit);
            }
            
            if (overdraftBalanceLimit != null)
            {
                SubscribersApiSupport.handleOverdraftBalanceLimitExtensionInExtensionList(ctx, sub, overdraftBalanceLimit, this);
            }
            
            if(deviceType != null)
            {
            	sub.setDeviceTypeId(deviceType);
            }
            
            if(deviceName != null)
            {
                sub.setDeviceName(deviceName);
            }
            
            if(devicePrice != null)
            {
                sub.setDeviceListPrice(devicePrice);
            }
            
            if(deviceImei != null)
            {
                sub.setDeviceImei(deviceImei);
            }
            
            if(activationReasonCode != null)
            {
            	sub.setReasonCode(activationReasonCode);
            }

            try
            {
                
                resultSub = (Subscriber) subHome.create(ctx, sub);
            }
            catch (HomeException e)
            {
                if (SubscriberSupport.getSubscriber(
                        ctx, 
                        sub.getBAN(),
                        sub.getMSISDN(), 
                        sub.getDateCreated()) == null)
                {
                    new MajorLogMsg(this, "Failed to create subscription while creating individual subscriber.  Attempting to delete newly created account " + resultAccount.getBAN(), e).log(ctx);
                    try
                    {

                        acctHome.remove(resultAccount);
                    }
                    catch (final HomeException e2)
                    {
                        new MajorLogMsg(this, "Failed to delete new individual subscriber account: " + resultAccount.getBAN(), e2).log(ctx);
                    }

                    throw e;
                }
            }
        }
        catch (final CRMExceptionFault e)
        {
            throw e;
        }
        catch (final Exception e)
        {
            String subId = null;
            if (resultAccount != null && subProfile != null)
            {
                subId = SubscribersApiSupport.getCreatedSubscriptionID(ctx, resultAccount.getBAN(), subProfile.getMobileNumber(), subProfile.getSubscriptionType(), requestStartTime, this);
            }
            subscriberCreated = SubscribersApiSupport.cleanFailure(ctx, subId, createdMsisdn, createdCard, this);

            final String msg = "Unable to create Individual Subscriber accountProfile.identifier="
                + accountProfile.getIdentifier() + " subProfile.identifier=" + subProfile.getIdentifier() + ". ";
            RmiApiErrorHandlingSupport.handleCreateExceptions(ctx, e, msg, subscriberCreated, Subscriber.class, subId, this);
        }

        if (resultSub == null)
        {
            String subId = null;
            if (resultAccount != null && subProfile != null)
            {
                subId = SubscribersApiSupport.getCreatedSubscriptionID(ctx, resultAccount.getBAN(), subProfile.getMobileNumber(), subProfile.getSubscriptionType(), requestStartTime, this);
            }
            subscriberCreated = SubscribersApiSupport.cleanFailure(ctx, subId, createdMsisdn, createdCard, this);

            final String msg = "Individual Subscriber create failed for accountProfile.identifier="
                + (accountProfile != null ? accountProfile.getIdentifier() : "") + " subProfile.identifier=" + (subProfile != null ? subProfile.getIdentifier() : "")
                + ". Reason UNKNOWN.";
            RmiApiErrorHandlingSupport.handleCreateExceptions(ctx, null, msg, subscriberCreated, Subscriber.class, subId, this);
        }

        SubscribersApiSupport.validateStateAfterCreateSubscription(ctx, resultSub, subProfile, status);

        final SubscriptionReference result = SubscriberToApiAdapter.adaptSubscriberToReference(ctx, resultSub);

        return result;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public AccountProfileQueryResults getAccountProfile(final CRMRequestHeader header, final String accountID, GenericParameter[] parameters)
        throws CRMExceptionFault
    {
        final Context ctx = getContext().createSubContext();
        // handle login. Failed logins throw exceptions
        RmiApiSupport.authenticateUser(ctx, header, "getAccountProfile",
            Constants.PERMISSION_ACCOUNTS_READ_GETACCOUNTPROFILE);

        RmiApiErrorHandlingSupport.validateMandatoryObject(accountID, "accountID");

        final Account account = getCrmAccount(ctx, accountID, this);

        final AccountProfileQueryResults result = new AccountProfileQueryResults();
        try
        {
            result.setProfile(AccountsApiSupport.extractProfile(account));
            result.setDetail(AccountsApiSupport.extractDetail(account));
            result.setBilling(AccountsApiSupport.extractBilling(account));
            result.setPaymentInfo(AccountsApiSupport.extractPaymentInfo(ctx, account));
            result.setIdentification(AccountsApiSupport.extractIdentification(ctx, account));
            result.setCompany(AccountsApiSupport.extractCompany(account));
            result.setBank(AccountsApiSupport.extractBank(account));
            result.setContact(AccountsApiSupport.extractContact(account));
        }
        catch (final Exception e)
        {
            final String msg = "Unable retreive Account " + accountID;
            RmiApiErrorHandlingSupport.handleQueryExceptions(ctx, e, msg, this);
        }

        return result;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public AccountProfileQueryResultsV2 getAccountProfileV2(final CRMRequestHeader header, final String accountID, GenericParameter[] parameters)
        throws CRMExceptionFault
    {
    	    final Context ctx = getContext().createSubContext();
    	    
            QueryExecutorFactory executor = QueryExecutorFactory.getInstance();
            
            return executor.execute(ctx, AccountServiceSkeletonInterface.class.getSimpleName(), "getAccountProfileV2", AccountProfileQueryResultsV2.class, 
                    header, accountID, parameters);
        
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GenericResponse convertIndividualToGroup(final CRMRequestHeader header, final String accountID, Boolean validateOnly, GenericParameter[] parameters)
        throws CRMExceptionFault
    {
    	    final Context ctx = getContext().createSubContext();
    	    
            QueryExecutorFactory executor = QueryExecutorFactory.getInstance();
            
            return executor.execute(ctx, AccountServiceSkeletonInterface.class.getSimpleName(), "convertIndividualToGroup", GenericResponse.class, 
                    header, accountID, validateOnly, parameters);
        
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SuccessCode deleteAccount(final CRMRequestHeader header, final String accountID, GenericParameter[] parameters) throws CRMExceptionFault
    {
        final Context ctx = getContext().createSubContext();
        // handle login. Failed logins throw exceptions
        RmiApiSupport.authenticateUser(ctx, header, "deleteAccount", Constants.PERMISSION_ACCOUNTS_WRITE_DELETEACCOUNT);
 
        RmiApiErrorHandlingSupport.validateMandatoryObject(accountID, "accountID");

        final Account account = getCrmAccount(ctx, accountID, this);
        if (account.getState() == AccountStateEnum.INACTIVE)
        {
            // nothing to do
            return SuccessCodeEnum.SUCCESS.getValue();
        }
        final Home home = updateAccountPipeline(ctx,"deleteAccount");

        try
        {
            // not deleting the account. only deactivating
            account.setState(AccountStateEnum.INACTIVE);
            home.store(ctx, account);
        }
        catch (final Exception e)
        {
            final String msg = "Unable to delete Account " + accountID;
            RmiApiErrorHandlingSupport.handleDeleteExceptions(ctx, e, msg, this);
        }
        
        return SuccessCodeEnum.SUCCESS.getValue();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public SuccessCode acquireMobileNumber(CRMRequestHeader header, String accountID, String mobileNumber, GenericParameter[] parameters)
            throws CRMExceptionFault
    {
        final Context ctx = getContext().createSubContext();

        // handle login. Failed logins throw exceptions
        RmiApiSupport.authenticateUser(ctx, header, "acquireMobileNumber",
            Constants.PERMISSION_ACCOUNTS_WRITE_ACQUIREMOBILENUMBER);
        Home home = ApiSupport.injectAPIUpdateEntityHomeIntoPipeline(ctx, (Home)ctx.get(MsisdnHome.class));
        getCrmAccount(ctx, accountID, this);
        
        try
        {
            Msisdn msisdn = MsisdnSupport.getMsisdn(ctx, mobileNumber);
            boolean isExternalMobileNumber = (msisdn == null || msisdn.isExternal());
            ctx.put(MsisdnHome.class,home);            
            MsisdnManagement.claimMsisdn(ctx, mobileNumber, accountID, isExternalMobileNumber, "");
        }
        catch (MsisdnAlreadyAcquiredException e)
        {
            new InfoLogMsg(ctx, "Mobile number " + mobileNumber + " already acquired by account " + accountID, null).log(ctx);
        }
        catch (Exception e)
        {
            final String msg = "Account " + accountID + " was unable to acquire mobile number " + mobileNumber;
            RmiApiErrorHandlingSupport.handleStoreExceptions(ctx, e, msg, this);
        }
        
        return SuccessCodeEnum.SUCCESS.getValue();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public SuccessCode releaseMobileNumber(CRMRequestHeader header, String accountID, String mobileNumber, GenericParameter[] parameters)
            throws CRMExceptionFault
    {
        final Context ctx = getContext().createSubContext();

        // handle login. Failed logins throw exceptions
        RmiApiSupport.authenticateUser(ctx, header, "releaseMobileNumber",
            Constants.PERMISSION_ACCOUNTS_WRITE_RELEASEMOBILENUMBER);
        ctx.put(MsisdnHome.class, ApiSupport.injectAPIUpdateEntityHomeIntoPipeline(ctx, (Home)ctx.get(MsisdnHome.class)));

        getCrmAccount(ctx, accountID, this);
        
        try
        {
            MsisdnManagement.releaseMsisdn(ctx, mobileNumber, accountID, "");
        }
        catch (Exception e)
        {
            final String msg = "Account " + accountID + " was unable to release mobile number " + mobileNumber;
            RmiApiErrorHandlingSupport.handleStoreExceptions(ctx, e, msg, this);
        }
        
        return SuccessCodeEnum.SUCCESS.getValue();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public AccountRoleReference[] listAccountRoles(CRMRequestHeader header, Boolean isAscending, GenericParameter[] parameters)
            throws CRMExceptionFault
    {
        final Context ctx = getContext().createSubContext();

        // handle login. Failed logins throw exceptions
        RmiApiSupport.authenticateUser(ctx, header, "listAccountRoles",
            Constants.PERMISSION_ACCOUNTS_READ_LISTACCOUNTROLES);

        AccountRoleReference[] accountRoleReferences = new AccountRoleReference[] {};
        try
        {
            final Object condition = True.instance();
            Collection<AccountRole> collection = HomeSupportHelper.get(ctx).getBeans(
                    ctx, 
                    AccountRole.class, 
                    condition, 
                    RmiApiSupport.isSortAscending(isAscending));
            
            accountRoleReferences = CollectionSupportHelper.get(ctx).adaptCollection(
                    ctx, 
                    collection, 
                    this.accountRoleToApiAdapter_, 
                    accountRoleReferences);
        }
        catch (final Exception e)
        {
            final String msg = "Unable to retrieve Account Roles";
            RmiApiErrorHandlingSupport.handleQueryExceptions(ctx, e, msg, this);
        }
        
        return accountRoleReferences;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public BaseAccountExtensionReference updateAccountAddExtension(CRMRequestHeader header, String accountID,
            BaseAccountExtension extension, GenericParameter[] parameters) throws CRMExceptionFault
    {
        final Context ctx = getContext().createSubContext();

        // handle login. Failed logins throw exceptions
        RmiApiSupport.authenticateUser(ctx, header, "updateAccountAddExtension",
            Constants.PERMISSION_ACCOUNTS_WRITE_UPDATEACCOUNTADDEXTENSION);

        RmiApiErrorHandlingSupport.validateMandatoryObject(accountID, "accountID");
        RmiApiErrorHandlingSupport.validateMandatoryObject(extension, "extension");

        Account account = getCrmAccount(ctx, accountID, this);

        AccountExtension newExtension = null;
        try
        {
            if (extension instanceof com.redknee.util.crmapi.wsdl.v3_0.types.account.extensions.AccountExtension)
            {
                com.redknee.util.crmapi.wsdl.v3_0.types.account.extensions.AccountExtension apiExtension = (com.redknee.util.crmapi.wsdl.v3_0.types.account.extensions.AccountExtension) extension;
                
                BaseAccountExtensionSequence_type0 poolWrapper = apiExtension.getBaseAccountExtensionSequence_type0();
                if (poolWrapper != null && poolWrapper.getPool() != null)
                {
                    PoolAccountExtension apiPoolExtension = poolWrapper.getPool();
                    try
                    {
                        newExtension = (PoolExtension) XBeans.instantiate(PoolExtension.class, ctx);
                    }
                    catch (Exception e)
                    {
                        new MinorLogMsg(this, "Error instantiating new pool extension.  Using default constructor.", e).log(ctx);
                        newExtension = new PoolExtension();
                    }
                    
                    PoolExtension poolExtension = (PoolExtension) newExtension;
                    poolExtension.setBAN(accountID);
                    poolExtension.setPoolMSISDN(apiPoolExtension.getGroupMobileNumber());
                    poolExtension.setSpid(account.getSpid());
                    final Long[] apiBudleIDs = apiPoolExtension.getBundleIDs();
                    final Map<Long, BundleFee> poolBundles;
                    if(null != apiBudleIDs && apiBudleIDs.length > 0)
                    {
                        
                        final CompoundIllegalStateException excl; 
                        {
                            excl = new CompoundIllegalStateException();
                            poolBundles = PoolExtension.transformBundles(ctx, excl,apiBudleIDs);
                            if(excl.getSize() >0 )
                            {
                                excl.throwAll();
                            }
                        }
                        
                    }
                    else
                    {
                        poolBundles = new HashMap<Long, BundleFee>();
                    }
                  
                    poolExtension.setPoolBundles(poolBundles);
                    Map<Long, SubscriptionPoolProperty> poolProperties = new HashMap<Long, SubscriptionPoolProperty>();
                    poolExtension.setSubscriptionPoolProperties(poolProperties);

                    for (com.redknee.util.crmapi.wsdl.v2_2.types.account.extensions.SubscriptionPoolProperty poolProperty : apiPoolExtension.getSubscriptionPoolProperty())
                    {
                        if (poolProperty == null)
                        {
                            continue;
                        }
                        
                        RmiApiErrorHandlingSupport.validateMandatoryObject(poolProperty.getSubscriptionType(), "SubscriptionPoolProperty.subscriptionType");
                        RmiApiErrorHandlingSupport.validateMandatoryObject(poolProperty.getInitialPoolBalance(), "SubscriptionPoolProperty.initialPoolBalance");
                        
                        SubscriptionPoolProperty crmProperty;
                        try
                        {
                            crmProperty = (SubscriptionPoolProperty) XBeans.instantiate(SubscriptionPoolProperty.class, ctx);
                        }
                        catch (Exception e)
                        {
                            new MinorLogMsg(this, "Error instantiating new subscription pool property.  Using default constructor.", e).log(ctx);
                            crmProperty = new SubscriptionPoolProperty();
                        }
                        crmProperty.setInitialPoolBalance(poolProperty.getInitialPoolBalance());
                        crmProperty.setSubscriptionType(poolProperty.getSubscriptionType());
                        if (poolProperty.getProvisioned() != null)
                        {
                            crmProperty.setProvisioned((short) poolProperty.getProvisioned().getValue());
                        }
                        else
                        {
                            crmProperty.setProvisioned(ServiceProvisionStatusEnum.PROVISIONED_INDEX);
                        }
                        poolProperties.put(crmProperty.getSubscriptionType(), crmProperty);
                    }
                }
                
                Home extensionHome = ExtensionSupportHelper.get(ctx).getExtensionHome(ctx, newExtension);
                extensionHome = ApiSupport.injectAPIUpdateEntityHomeIntoPipeline(ctx, extensionHome);
                if (extensionHome != null)
                {
                    extensionHome.create(ctx, newExtension);
                }
                else
                {
                    StringBuilder msg = new StringBuilder("Account Extension type not supported");
                    Class<? extends AccountExtension> extensionType = null;
                    if (newExtension != null)
                    {
                        extensionType = newExtension.getClass();
                        msg.append(": " + extensionType.getName());
                    }
                    RmiApiErrorHandlingSupport.handleCreateExceptions(ctx, null, msg.toString(), false, extensionType, null, this);
                }
            }
            else
            {
                Class<? extends AccountExtension> extensionType = null;
                if (newExtension != null)
                {
                    extensionType = newExtension.getClass();
                }
                final String msg = "Account Extension type not supported: " + extension.getClass().getSimpleName();
                RmiApiErrorHandlingSupport.handleCreateExceptions(ctx, null, msg, false, extensionType, null, this);
            }
        }
        catch (final CRMExceptionFault e)
        {
            throw e;
        }
        catch (final Exception e)
        {
            Class<? extends AccountExtension> extensionType = null;
            if (newExtension != null)
            {
                extensionType = newExtension.getClass();
            }
            final String msg = "Unable to create Account Extension";
            RmiApiErrorHandlingSupport.handleCreateExceptions(ctx, e, msg, false, extensionType, null, this);
        }

        return null;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public SuccessCode updateAccountRemoveExtension(CRMRequestHeader header, BaseAccountExtensionReference extensionReference, GenericParameter[] parameters)
            throws CRMExceptionFault
    {
        final Context ctx = getContext().createSubContext();

        // handle login. Failed logins throw exceptions
        RmiApiSupport.authenticateUser(ctx, header, "updateAccountRemoveExtension",
            Constants.PERMISSION_ACCOUNTS_WRITE_UPDATEACCOUNTREMOVEEXTENSION);

        RmiApiErrorHandlingSupport.validateMandatoryObject(extensionReference, "extensionReference");

        getCrmAccount(ctx, extensionReference.getAccountID(), this);

        ExtensionApiSupport.removeAccountExtension(extensionReference, ctx);

        return SuccessCodeEnum.SUCCESS.getValue();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public BaseReadOnlyAccountExtension getAccountExtension(CRMRequestHeader header,
            BaseAccountExtensionReference extensionReference, GenericParameter[] parameters) throws CRMExceptionFault
    {
        final Context ctx = getContext().createSubContext();

        // handle login. Failed logins throw exceptions
        RmiApiSupport.authenticateUser(ctx, header, "getAccountExtension",
            Constants.PERMISSION_ACCOUNTS_READ_GETACCOUNTEXTENSION);

        RmiApiErrorHandlingSupport.validateMandatoryObject(extensionReference, "extensionReference");

        getCrmAccount(ctx, extensionReference.getAccountID(), this);
        
        ReadOnlyAccountExtension accountExtension = new ReadOnlyAccountExtension();
       
        try
        {
            if (extensionReference instanceof PoolAccountExtensionReference)
            {
                PoolAccountExtensionReference poolReference = (PoolAccountExtensionReference) extensionReference;
                if (poolReference != null)
                {   
                    List<PoolExtension> crmPoolExtensions = ExtensionSupportHelper.get(ctx).getExtensions(ctx, PoolExtension.class, new EQ(AccountExtensionXInfo.BAN, extensionReference.getAccountID()));

                    ReadOnlyAccountExtension[] apiExtensions = CollectionSupportHelper.get(ctx).adaptCollection(
                            ctx, 
                            crmPoolExtensions, 
                            this.accountExtensionToApiAdapter_, 
                            new ReadOnlyAccountExtension[]{});
                    for (ReadOnlyAccountExtension apiExtension : apiExtensions)
                    {
                        BaseReadOnlyAccountExtensionSequence_type3 poolWrapper = apiExtension.getBaseReadOnlyAccountExtensionSequence_type3();
                        if (poolWrapper != null)
                        {
                            ReadOnlyPoolAccountExtension poolExtension = poolWrapper.getPool();
                            if (poolExtension != null)
                            {
                                String groupMsisdn = poolReference.getGroupMobileNumber();
                                if (groupMsisdn == null || SafetyUtil.safeEquals(groupMsisdn, poolExtension.getGroupMobileNumber()))
                                {
                                    accountExtension.setBaseReadOnlyAccountExtensionSequence_type3(poolWrapper);
                                    break;
                                }
                            }
                        }
                    }
                }
            }
            else
            {
                final String msg = "Account Extension type not supported: " + extensionReference.getClass().getSimpleName();
                RmiApiErrorHandlingSupport.handleQueryExceptions(ctx, null, msg, this);
            }
        }
        catch (final Exception e)
        {
            final String msg = "Unable to retrieve Account Extension";
            RmiApiErrorHandlingSupport.handleQueryExceptions(ctx, e, msg, this);
        }
        
        return accountExtension;
    }
    

    
    /**
     * {@inheritDoc}
     */
    @Override
    public BaseAccountExtensionReference[] listAccountExtensions(CRMRequestHeader header, String accountID,
            Boolean isAscending, GenericParameter[] parameters) throws CRMExceptionFault
    {
        final Context ctx = getContext().createSubContext();

        // handle login. Failed logins throw exceptions
        RmiApiSupport.authenticateUser(ctx, header, "listAccountExtensions",
            Constants.PERMISSION_ACCOUNTS_READ_LISTACCOUNTEXTENSIONS);

        RmiApiErrorHandlingSupport.validateMandatoryObject(accountID, "accountID");

        getCrmAccount(ctx, accountID, this);
        
        BaseAccountExtensionReference[] accountExtensionReferences = new BaseAccountExtensionReference[] {};
        try
        {
            final Collection<AccountExtension> extensions = getSortedExtensions(ctx, accountID, RmiApiSupport.isSortAscending(isAscending));

            accountExtensionReferences = CollectionSupportHelper.get(ctx).adaptCollection(
                    ctx, 
                    extensions, 
                    this.accountExtensionToApiReferenceAdapter_, 
                    accountExtensionReferences);
        }
        catch (final Exception e)
        {
            final String msg = "Unable to retrieve Account Extensions";
            RmiApiErrorHandlingSupport.handleQueryExceptions(ctx, e, msg, this);
        }

        return accountExtensionReferences;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public BaseReadOnlyAccountExtension[] listDetailedAccountExtensions(CRMRequestHeader header, String accountID,
            Boolean isAscending, GenericParameter[] parameters) throws CRMExceptionFault
    {
        final Context ctx = getContext().createSubContext();

        // handle login. Failed logins throw exceptions
        RmiApiSupport.authenticateUser(ctx, header, "listDetailedAccountExtensions",
                Constants.PERMISSION_ACCOUNTS_READ_LISTDETAILEDACCOUNTEXTENSIONS);

        RmiApiErrorHandlingSupport.validateMandatoryObject(accountID, "accountID");

        getCrmAccount(ctx, accountID, this);
        
        BaseReadOnlyAccountExtension[] accountExtensions = new BaseReadOnlyAccountExtension[] {};
        try
        {
            final Collection<AccountExtension> extensions = getSortedExtensions(ctx, accountID, RmiApiSupport.isSortAscending(isAscending));

            accountExtensions = CollectionSupportHelper.get(ctx).adaptCollection(
                    ctx, 
                    extensions, 
                    this.accountExtensionToApiAdapter_, 
                    accountExtensions);
        }
        catch (final Exception e)
        {
            final String msg = "Unable to retrieve Account Extensions";
            RmiApiErrorHandlingSupport.handleQueryExceptions(ctx, e, msg, this);
        }

        return accountExtensions;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public SuccessCode updateAccountExtension(CRMRequestHeader header, BaseAccountExtensionReference extensionReference,
            BaseMutableAccountExtension extension, GenericParameter[] parameters) throws CRMExceptionFault
    {
        final Context ctx = getContext().createSubContext();

        // handle login. Failed logins throw exceptions
        RmiApiSupport.authenticateUser(ctx, header, "updateAccountExtension",
            Constants.PERMISSION_ACCOUNTS_WRITE_UPDATEACCOUNTEXTENSION);

        RmiApiErrorHandlingSupport.validateMandatoryObject(extensionReference, "extensionReference");
        RmiApiErrorHandlingSupport.validateMandatoryObject(extension, "extension");

        Account account = getCrmAccount(ctx, extensionReference.getAccountID(), this);
        
        try
        {
            if (extensionReference instanceof com.redknee.util.crmapi.wsdl.v2_2.types.account.extensions.PoolAccountExtensionReference
                    && extension instanceof com.redknee.util.crmapi.wsdl.v3_0.types.account.extensions.MutablePoolAccountExtension)
            {
                com.redknee.util.crmapi.wsdl.v2_2.types.account.extensions.PoolAccountExtensionReference poolReference = 
                    (com.redknee.util.crmapi.wsdl.v2_2.types.account.extensions.PoolAccountExtensionReference) extensionReference;
                com.redknee.util.crmapi.wsdl.v3_0.types.account.extensions.MutablePoolAccountExtension poolExtension = 
                    (com.redknee.util.crmapi.wsdl.v3_0.types.account.extensions.MutablePoolAccountExtension) extension;

                ExtensionApiSupport.updateAccountPoolExtension(ctx, poolReference, poolExtension, account);
            }
            else if (extensionReference instanceof com.redknee.util.crmapi.wsdl.v2_2.types.account.extensions.GroupPricePlanAccountExtensionReference
            		&& extension instanceof com.redknee.util.crmapi.wsdl.v3_0.types.account.extensions.MutableGroupPricePlanAccountExtension){
            	
            	com.redknee.util.crmapi.wsdl.v2_2.types.account.extensions.GroupPricePlanAccountExtensionReference groupPricePlanReference=
            		(com.redknee.util.crmapi.wsdl.v2_2.types.account.extensions.GroupPricePlanAccountExtensionReference) extensionReference;
            	com.redknee.util.crmapi.wsdl.v3_0.types.account.extensions.MutableGroupPricePlanAccountExtension groupPricePlanExtension=
            		(com.redknee.util.crmapi.wsdl.v3_0.types.account.extensions.MutableGroupPricePlanAccountExtension) extension;	
            	
            	ExtensionApiSupport.updateGroupPricePlanExtension(ctx, groupPricePlanReference, groupPricePlanExtension, account);
            }
            else if(extensionReference instanceof com.redknee.util.crmapi.wsdl.v2_2.types.account.extensions.SubscriberLimitAccountExtensionReference
            		&& extension instanceof com.redknee.util.crmapi.wsdl.v3_0.types.account.extensions.MutableSubscriberLimitAccountExtension)
            {
            	com.redknee.util.crmapi.wsdl.v2_2.types.account.extensions.SubscriberLimitAccountExtensionReference subscriberLimitReference =
            			(com.redknee.util.crmapi.wsdl.v2_2.types.account.extensions.SubscriberLimitAccountExtensionReference) extensionReference;
            	com.redknee.util.crmapi.wsdl.v3_0.types.account.extensions.MutableSubscriberLimitAccountExtension subscriberLimitExtension =
            			(com.redknee.util.crmapi.wsdl.v3_0.types.account.extensions.MutableSubscriberLimitAccountExtension) extension;
            	
            	ExtensionApiSupport.updateSubscriberLimitExtension(ctx, subscriberLimitReference, subscriberLimitExtension, account);
            }
            else if(extensionReference instanceof com.redknee.util.crmapi.wsdl.v2_2.types.account.extensions.FriendsAndFamilyAccountExtensionReference
            		&& extension instanceof com.redknee.util.crmapi.wsdl.v3_0.types.account.extensions.MutableFriendsAndFamilyAccountExtension)
            {
            	com.redknee.util.crmapi.wsdl.v2_2.types.account.extensions.FriendsAndFamilyAccountExtensionReference fnfAccountReference =
            			(com.redknee.util.crmapi.wsdl.v2_2.types.account.extensions.FriendsAndFamilyAccountExtensionReference) extensionReference;
            	com.redknee.util.crmapi.wsdl.v3_0.types.account.extensions.MutableFriendsAndFamilyAccountExtension fnfExtension =
            			(com.redknee.util.crmapi.wsdl.v3_0.types.account.extensions.MutableFriendsAndFamilyAccountExtension) extension;
            	
            	ExtensionApiSupport.updateFriendsAndFamilyExtension(ctx, fnfAccountReference, fnfExtension, account);
            }
            else
            {
                final String msg = "Account Extension type [" + extension.getClass().getSimpleName() + "] is not compatible with reference type [" + extensionReference.getClass().getSimpleName() + "]";
                RmiApiErrorHandlingSupport.handleStoreExceptions(ctx, null, msg, this);
            }
        }
        catch (final CRMExceptionFault e)
        {
            throw e;
        }
        catch (final Exception e)
        {
            final String msg = "Unable to update Account Extension";
            RmiApiErrorHandlingSupport.handleStoreExceptions(ctx, e, msg, this);
        }
        
        return SuccessCodeEnum.SUCCESS.getValue();
    }


    /**
     * Validates the new bill cycle of an account.
     *
     * @param ctx
     *            The operating context.
     * @param account
     *            Account to be updated.
     * @param billCycleID
     *            Identifier of the new bill cycle.
     * @throws CRMExceptionFault
     *             Thrown if there are problems with the account-billCycleID combination
     *             provided.
     */
	private com.redknee.app.crm.bean.BillCycle validateUpdateAccountBillCycle(
	    final Context ctx, final Account account, final Long billCycleID)
        throws CRMExceptionFault
    {
        RmiApiErrorHandlingSupport.validateMandatoryObject(billCycleID, "billCycleID");

        if (Integer.class.isAssignableFrom(BillCycleXInfo.BILL_CYCLE_ID.getType())
                && (billCycleID < Integer.MIN_VALUE || billCycleID > Integer.MAX_VALUE))
        {
            // This range check is required to prevent small longs from being
            // converted to 0 or big longs from overflowing
            final String msg = "Bill cycle identifier outside of the valid range";
            RmiApiErrorHandlingSupport.simpleValidation("billCycleID", msg);
        }
        
		if (!account.isRootAccount())
		{
			final String msg = "Bill cycle can only be updated on root account";
			RmiApiErrorHandlingSupport.simpleValidation("accountID", msg);
		}
        final com.redknee.app.crm.bean.BillCycle billCycle = RmiApiSupport.getCrmBillCycle(ctx, billCycleID, this);
        if (billCycle == null)
        {
            final String msg = "Cannot find billCycle with billCycleID " + billCycleID;
            RmiApiErrorHandlingSupport.simpleValidation("billCycleID", msg);
        }
        else if (billCycle.getSpid() != account.getSpid())
        {
            // TODO 2007-08-26 move SPID validation to the pipeline
            final String msg = "Cannot assign billCycleID " + billCycleID + " with SPID " + billCycle.getSpid()
            + " to account " + account.getBAN() + " with SPID " + account.getSpid();
            RmiApiErrorHandlingSupport.simpleValidation("billCycleID", msg);
        }
		return billCycle;
    }


    /**
     * Validates the new VPN MSISDN of the account.
     *
     * @param ctx
     *            The operating context.
     * @param account
     *            Account to be updated.
     * @param number
     *            New VPN MSISDN.
     * @throws CRMExceptionFault
     *             Thrown if there are problems with the provided account-MSISDN
     *             combination, such as if the provided MSISDN does not exist.
     */
    private void validateUpdateAccountVpnMSISDN(final Context ctx, final Account account, final String number)
        throws CRMExceptionFault
    {
        // add validations if needed
        RmiApiErrorHandlingSupport.validateMandatoryObject(number, "mobileNumber");

        final com.redknee.app.crm.bean.Msisdn msisdn = MobileNumbersApiSupport.getCrmMsisdn(ctx, number, this);
    
        if (msisdn == null)
        {
            final String msg = "Cannot find Msisdn with \"" + number + "\"";
            RmiApiErrorHandlingSupport.simpleValidation("mobileNumber", msg);
        }
    }


    /**
     * Validates the parameters needed to create an account.
     *
     * @param profile
     *            Account profile.
     * @param detail
     *            Account detail.
     * @param billing
     *            Account billing information.
     * @param sp
     *            Service provider.
     * @throws CRMExceptionFault
     *             Thrown if one or more of the provided parameters are invalid.
     */
    public static void validateCreateAccount(final AccountProfile profile, final AccountDetail detail,
        final AccountBilling billing, final CRMSpid sp) throws CRMExceptionFault
    {
        final List<ValidationExceptionEntry> validations = new ArrayList<ValidationExceptionEntry>();
        if (profile == null)
        {
            RmiApiErrorHandlingSupport
                .addSimpleValidationEntry(
                    validations,
                    "profile",
                    "AccountProfile is a mandatory parameter and cannot be NULL. For Accounts.createAccount() call AccountProfile has to be specified");
        }
        else
        {
            if (profile.getState() == null)
            {
                RmiApiErrorHandlingSupport
                    .addSimpleValidationEntry(
                        validations,
                        "AccountProfile.State",
                        "AccountProfile.State is a mandatory parameter and cannot be NULL. For Accounts.createAccount() call State has to be specified");
            
            }else if(profile.getState() != null){
            	
            	AccountStateEnum acctState = RmiApiSupport.convertApiAccountState2Crm(profile.getState());
            	
            	if(detail !=null && detail.getSystemType() !=null ){
            		SubscriberTypeEnum subType = RmiApiSupport.convertApiSystemType2CrmSystemType(detail.getSystemType());
            		if(subType.equals(SubscriberTypeEnum.PREPAID)){
            			if(!acctState.equals(AccountStateEnum.ACTIVE) && !acctState.equals(AccountStateEnum.INACTIVE) && !acctState.equals(AccountStateEnum.SUSPENDED)){
            				RmiApiErrorHandlingSupport
            				.addSimpleValidationEntry(
            						validations,
            						"AccountProfile.State",
            						"Prepaid Account can only be created in Active/Suspended/InActive states.");
            			}

            		}
            	}
            }
        }
        if (detail == null)
        {
            RmiApiErrorHandlingSupport
                .addSimpleValidationEntry(
                    validations,
                    "detail",
                    "AccountDetail is a mandatory parameter and cannot be NULL. For Accounts.createAccount() call AccountDetail has to be specified");
        }
        else
        {
            if (detail.getSystemType() == null)
            {
                RmiApiErrorHandlingSupport
                    .addSimpleValidationEntry(
                        validations,
                        "AccountDetail.SystemType",
                        "AccountDetail.SystemType is a mandatory parameter and cannot be NULL. For Accounts.createAccount() call SystemType has to be specified");
            }
        }
        if (billing == null)
        {
            RmiApiErrorHandlingSupport
                .addSimpleValidationEntry(
                    validations,
                    "billing",
                    "AccountBilling is a mandatory parameter and cannot be NULL. For Accounts.createAccount() call AccountBilling has to be specified");

        }
        if (sp != null && profile != null)
        {
            if (profile.getIdentifier() != null && profile.getIdentifier().length() > 0 && !sp.getAllowToSpecifyBAN())
            {
                RmiApiErrorHandlingSupport
                    .addSimpleValidationEntry(validations, "AccountProfile.identifier",
                        "Not allowed to specify Account Identifier. Requested Service Provider does not allow to specify Account Identifier");
            }
        }

        if (validations.size() > 0)
        {
            RmiApiErrorHandlingSupport.compoundValidation(validations);
        }
    }


    /**
     * Validates the new account state.
     *
     * @param ctx
     *            The operating context.
     * @param account
     *            Account to be updated.
     * @param state
     *            New state.
     * @throws CRMExceptionFault
     *             Thrown if the provided account-state combination is invalid.
     */
    private void validateUpdateAccountState(final Context ctx, final Account account, final AccountStateEnum state)
        throws CRMExceptionFault
    {
    	if (account.getState() == state)
        {
            RmiApiErrorHandlingSupport.simpleValidation("state",
                    "Account is currently in the given State. Cannot change state to the same State.");
        }
        /*
         * [Cindy Wong] 2008-03-13: Account state manual transition validation.
         */
        if (!AccountStateTransitionSupport.instance().isManualStateTransitionAllowed(ctx, account, state))
        {
            final StringBuilder sb = new StringBuilder();
            sb.append("Account state transition from ");
            sb.append(account.getState().getDescription());
            sb.append(" State to ");
            sb.append(state.getDescription());
            sb.append(" State is not allowed.");

            RmiApiErrorHandlingSupport.simpleValidation("state", sb.toString());
        }
    }


    /**
     * Header must be authenticated. This method does not do authentication. Does not
     * return null. If account not found Exception is thrown.
     *
     * @param ctx
     *            the operating context
     * @param accountID
     *            the ID of the account to retrieve
     * @param caller
     * @return the Account object
     * @throws CRMExceptionFault
     *             any exception is caught and wrapt in a CRMException
     */
    public static Account getCrmAccount(final Context ctx, final String accountID, final Object caller)
        throws CRMExceptionFault
    {
        Account account = null;
        
        try
        {
            account = HomeSupportHelper.get(ctx).findBean(ctx, Account.class, accountID);
        }
        catch (final Exception e)
        {
            final String msg = "Unable to retrieve Account " + accountID;
            RmiApiErrorHandlingSupport.handleQueryExceptions(ctx, e, msg, caller);
        }

        if (account == null)
        {
            final String identifier = "Account " + accountID;
            RmiApiErrorHandlingSupport.identificationException(ctx, identifier, caller);
        }

        return account;
    }


    /**
     * Header must be authenticated. This method does not do authentication. Does not
     * return null. If no subaccounts are found it returns an empty collection. If
     * responsible parameter is true then method will retrieve only child Accounts that
     * are not responsible, that is the accounts that the parent is responsible for. If
     * responsible parameter is false method will retrieve all child accounts.
     *
     * @param ctx
     *            the operating context
     * @param account
     *            the parent account
     * @param recurse
     *            if true method will recursively retrieve child of child accounts
     * @param responsible
     *            if true return only accounts for which parent is responsible
     * @return a collection with account objects
     * @throws CRMExceptionFault
     *             any exception is caught and wrapped in a CRMException
     */
    public static Collection<Account> getCrmSubAccounts(final Context ctx, final Account account, final boolean recurse,
        final Boolean responsible, final Boolean isAscending, GenericParameter[] parameters) throws CRMExceptionFault
    {
        final List<Account> result = new LinkedList<Account>();
        final LinkedList<Account> queue = new LinkedList<Account>();
        queue.addLast(account);
        try
        {
            while (queue.size() > 0)
            {
                final Account current = queue.removeFirst();
                final Object condition = new EQ(AccountXInfo.PARENT_BAN, current.getBAN());
                
                Collection<Account> collection = HomeSupportHelper.get(ctx).getBeans(
                        ctx, 
                        Account.class, 
                        condition, 
                        RmiApiSupport.isSortAscending(isAscending));
                
                for (Account child : collection)
                {
                    if (responsible != null && responsible.booleanValue())
                    {
                        if (!child.isResponsible())
                        {
                            result.add(child);
                            if (recurse)
                            {
                                queue.addLast(child);
                            }
                        }
                    }
                    else
                    {
                        result.add(child);
                        if (recurse)
                        {
                            queue.addLast(child);
                        }
                    }
                }
            }
        }
        catch (final Exception e)
        {
            final String msg = "Unable to retrieve Sub Accounts for Account " + account.getBAN();
            RmiApiErrorHandlingSupport.handleQueryExceptions(ctx, e, msg, AccountsImpl.class.getName());
        }

        return result;
    }




    private Collection<AccountExtension> getSortedExtensions(final Context ctx, String accountID, boolean ascending)
    {
        final Context sCtx = ctx.createSubContext();
        
        // Set up the registered extension homes so that the results are ordered by primary key
        Set<Class<AccountExtension>> extensions = ExtensionSupportHelper.get(ctx).getRegisteredExtensions(sCtx, AccountExtension.class);
        for (Class<AccountExtension> extension : extensions)
        {   
            XInfo xinfo = (XInfo) XBeans.getInstanceOf(sCtx, extension, XInfo.class);
            if (xinfo != null)
            {
                Object homeKey = ExtensionSupportHelper.get(ctx).getExtensionHomeKey(sCtx, extension);
                if (homeKey != null)
                {
                    Home home = (Home) sCtx.get(homeKey);
                    home = new OrderByHome(sCtx, home);
                    ((OrderByHome)home).addOrderBy(xinfo.getID(), ascending);
                    sCtx.put(homeKey, home);
                }
            }
        }
        
        // Sort the extension names
        Map<Class<AccountExtension>, List<AccountExtension>> temp = ExtensionSupportHelper.get(ctx).getExistingExtensionMap(sCtx, AccountExtension.class, new EQ(AccountExtensionXInfo.BAN, accountID));
        final Map<Class<AccountExtension>, List<AccountExtension>> extensionMap = temp;
        final Set<Class<AccountExtension>> extensionTypeSet = extensionMap.keySet();
        final Map<String, Class<? extends AccountExtension>> extensionTypes;
        if (ascending)
        {
            extensionTypes = new TreeMap<String, Class<? extends AccountExtension>>();
        }
        else
        {
            extensionTypes = new TreeMap<String, Class<? extends AccountExtension>>(new ReverseComparator(ComparableComparator.instance()));
        }
        for (Class<AccountExtension> extensionType : extensionTypeSet)
        {
            extensionTypes.put(ExtensionSupportHelper.get(ctx).getExtensionName(ctx, extensionType), extensionType);
        }

        // Add extensions to a collection that is sorted first by extension name, then by ID
        final Collection<AccountExtension> result = new ArrayList<AccountExtension>();
        for (Class<? extends AccountExtension> extensionType : extensionTypes.values())
        {
            result.addAll(extensionMap.get(extensionType));   
        }
        return result;
    }


    /**
     * 
     */
    @Override
    public AccountReference updateAccountConvertSystemTypeHybridToPrepaid(CRMRequestHeader header, String accountID, GenericParameter[] parameters)
            throws CRMExceptionFault
    {
        /*
         * We don't want to inject wrapper home, because we changes to be propagated back to CRM
         */
        
        final Context ctx = getContext().createSubContext();
        // handle login. Failed logins throw exceptions
        RmiApiSupport.authenticateUser(ctx, header, "updateAccountConvertSystemTypeHybridToPrepaid",
                Constants.PERMISSION_ACCOUNTS_WRITE_UPDATEACCOUNTCONVERTSYSTEMTYPEHYBRIDTOPREPAID);
        final Home home = updateAccountPipeline(ctx, "updateAccountConvertSystemTypeHybridToPrepaid");
        Account account = getCrmAccount(ctx, accountID, this);
        if (account.getSystemType().equals(SubscriberTypeEnum.HYBRID))
        {
            try
            {
                account.setSystemType(SubscriberTypeEnum.PREPAID);
                account = (Account) home.store(ctx, account);
            }
            catch (final Exception e)
            {
                final String msg = "Unable to convert Account " + accountID + " into Prepaid account ";
                RmiApiErrorHandlingSupport.handleStoreExceptions(ctx, e, msg, this);
            }
        }
        else if( !(account.getSystemType().equals(SubscriberTypeEnum.PREPAID)))
        {
            RmiApiErrorHandlingSupport.handleStoreExceptions(ctx, null,
                    " Unable to convert to Prepaid Account, due to invaild billingType", this);
        }
        final AccountReference result = AccountsApiSupport.adaptAccountToReference(account);
        return result;
    }


    @Override
    public AccountReference updateAccountConvertSystemTypeHybridToPostpaid(CRMRequestHeader header, String accountID, GenericParameter[] parameters)
    throws CRMExceptionFault
    {
        /*
         * We don't want to inject wrapper home, because we changes to be propagated back to CRM
         */
        
        final Context ctx = getContext().createSubContext();
        // handle login. Failed logins throw exceptions
        RmiApiSupport.authenticateUser(ctx, header, "updateAccountConvertSystemTypeHybridToPostpaid",
                Constants.PERMISSION_ACCOUNTS_WRITE_UPDATEACCOUNTCONVERTSYSTEMTYPEHYBRIDTOPOSTPAID);
        final Home home = updateAccountPipeline(ctx, "updateAccountConvertSystemTypeHybridToPostpaid");
        Account account = getCrmAccount(ctx, accountID, this);
        if (account.getSystemType().equals(SubscriberTypeEnum.HYBRID))
        {
            try
            {
                account.setSystemType(SubscriberTypeEnum.POSTPAID);
                account = (Account) home.store(ctx, account);
            }
            catch (final Exception e)
            {
                final String msg = "Unable to convert Account " + accountID + " into Postpaid account ";
                RmiApiErrorHandlingSupport.handleStoreExceptions(ctx, e, msg, this);
            }
        }
        else if( !(account.getSystemType().equals(SubscriberTypeEnum.POSTPAID)))
        {
            RmiApiErrorHandlingSupport.handleStoreExceptions(ctx, null,
                    " Unable to convert to Postpaid Account, due to invaild billingType", this);
        }
        final AccountReference result = AccountsApiSupport.adaptAccountToReference(account);
        return result;
    }


    @Override
    public AccountReference updateAccountConvertSystemTypeToHybrid(CRMRequestHeader header, String accountID,
            MutableAccountContactInfo contact, MutableAccountIdentification identification,
            GenericParameter[] parameters) throws CRMExceptionFault
    {
        final Context ctx = getContext().createSubContext();
        // handle login. Failed logins throw exceptions
        RmiApiSupport.authenticateUser(ctx, header, "updateAccountConvertSystemTypeToHybrid",
                Constants.PERMISSION_ACCOUNTS_WRITE_UPDATEACCOUNTCONVERTSYSTEMTYPETOHYBRID);        
        final Home home = updateAccountPipeline(ctx,"updateAccountConvertSystemTypeToHybrid");
        Account account = getCrmAccount(ctx, accountID, this);
        if (!account.isHybrid())
        {
            try
            {
                account =  (Account)account.deepClone();
            }
            catch (CloneNotSupportedException e1)
            {
                new MinorLogMsg(this,"Unable to deep-clone Account of type: " + Account.class.getName(),e1).log(ctx);
            }
            try
            {
                if (identification != null)
                {
                    AccountsApiSupport.fillInMutableIdentification(ctx, account, identification);
                }
                if (contact != null)
                {
                    AccountsApiSupport.fillInMutableContact(account, contact);
                }
                account.setSystemType(SubscriberTypeEnum.HYBRID);
                account = (Account) home.store(ctx, account);
            }
            catch (final Exception e)
            {
                final String msg = "Unable to convert Account " + accountID + " into Hybrid account ";
                RmiApiErrorHandlingSupport.handleStoreExceptions(ctx, e, msg, this);
            }
        }
        final AccountReference result = AccountsApiSupport.adaptAccountToReference(account);
        return result;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public AccountAgedDebt getAccountAgedDebt(CRMRequestHeader header, String accountID, GenericParameter[] parameters)
            throws CRMExceptionFault
    {
        final Context ctx = getContext().createSubContext();
        // handle login. Failed logins throw exceptions
		RmiApiSupport.authenticateUser(ctx, header, "getAccountAgedDebt",
		    Constants.PERMISSION_ACCOUNTS_READ_GETACCOUNTAGEDDEBT);

		Account account = getCrmAccount(ctx, accountID, this);
		try
		{
			account = account.getResponsibleParentAccount(ctx);
		}
		catch (HomeException exception)
		{
			final String msg =
			    "CRM failed to look up the responsible account for account "
			        + accountID;
			RmiApiErrorHandlingSupport.handleQueryExceptions(ctx, exception,
			    msg, this);
		}
		// invalidates the cached value
		account.setAccumulatedPayment(AccountSupport.INVALID_VALUE);
        long payments = account.getAccumulatedPayment(ctx, null);
		if (payments == AccountSupport.INVALID_VALUE)
		{
			final String msg =
			    "CRM cannot retrieve the payment since last invoice for account "
			        + accountID;
			RmiApiErrorHandlingSupport.handleQueryExceptions(ctx, null, msg,
			    this);
		}
		AccountAgedDebt apiAgedDebt = new AccountAgedDebt();
		try
		{
			List<AgedDebt> agedDebts = account.getAgedDebt(ctx);
			AgedDebtPeriodDetail[] agedDebtDetails =
			    new AgedDebtPeriodDetail[((agedDebts == null)? 0: agedDebts.size())];
			apiAgedDebt.setPaymentsSinceLastInvoice(payments);
			CollectionSupportHelper.get(ctx).adaptCollection(ctx, agedDebts,
			    this.agedDebtToApiAdapter_, agedDebtDetails);
			apiAgedDebt.setPeriodDetails(agedDebtDetails);
		}
		catch (final Exception e)
		{
			final String msg = "Unable to retrieve Account Aged Debt";
			RmiApiErrorHandlingSupport.handleQueryExceptions(ctx, e, msg, this);
		}
		return apiAgedDebt;
    }


    @Override
    public DirectDebitInitiationResponse initiateDirectDebit(CRMRequestHeader header, java.lang.String accountID,
            GenericParameter[] parameters) throws CRMExceptionFault
    {
    	DirectDebitInitiationResponse ret = new DirectDebitInitiationResponse(); 
		try
		{
			Account acct = AccountSupport.getAccount(getContext(), accountID);
			if (acct != null)
			{
				acct.redoDirectDebit();
				ret.setResultCode(DirectDebitInitiationResultTypeEnum.SUCCESS.getValue());
            }
            else
			{
				ret.setResultCode(DirectDebitInitiationResultTypeEnum.INCOMPLETE_DATA.getValue());
			}
		}
		catch (final Exception e)
		{
			final String msg = "Unable to apply direct debit";
			RmiApiErrorHandlingSupport.handleQueryExceptions(getContext(), e, msg, this);
		}
		return null; 
    }
    

    @Override
    public AccountBillCycleActivity getAccountBillCycleActivity(CRMRequestHeader header, String accountID, GenericParameter[] parameters)
            throws CRMExceptionFault
    {
        final Context ctx = getContext().createSubContext();
        // handle login. Failed logins throw exceptions
        RmiApiSupport.authenticateUser(ctx, header, "updateAccountBillCycle",
            Constants.PERMISSION_ACCOUNTS_WRITE_UPDATEACCOUNTBILLCYCLE);

        RmiApiErrorHandlingSupport.validateMandatoryObject(accountID, "accountID");
        
        final Account account = getCrmAccount(ctx, accountID, this);

		if (!account.isRootAccount())
		{
			final String msg = "Bill cycle can only be updated on root account";
			RmiApiErrorHandlingSupport.simpleValidation("accountID", msg);
		}

        CalendarSupport calendarSupport = CalendarSupportHelper.get(ctx);

		BillCycleHistory history =
		    BillCycleHistorySupport.getLastEvent(ctx, account.getBAN());
		AccountBillCycleActivity result = new AccountBillCycleActivity();
		result.setCurrentBillCycleID((long) account.getBillCycleID());
		
		Date nextAvailable = BillCycleHistorySupport.getNextAllowedRequestDate(ctx, account);
		if (nextAvailable != null)
		{
			result.setEarliestPermittedNewBillCycleChange(calendarSupport.dateToCalendar(nextAvailable));
		}
		
		if (history != null)
        {
			BillCycleHistory before = history;
			if (BillCycleChangeStatusEnum.PENDING.equals(history.getStatus()))
			{
				result.setScheduledBillCycleID((long) history
				    .getNewBillCycleID());
				
	            ChargingCycleHandler handler = ChargingCycleSupportHelper.get(ctx).getHandler(ChargingCycleEnum.MONTHLY);
	            if (handler != null)
	            {
	                Date nextMonthDate = calendarSupport.findDateMonthsAfter(1, new Date());
	                Date startOfNextOldBillCycle = handler.calculateCycleStartDate(ctx, nextMonthDate, history.getOldBillCycleDay(), account.getSpid());
	                if (startOfNextOldBillCycle != null)
	                {
	                    result.setScheduledBillCycleChange(calendarSupport.dateToCalendar(startOfNextOldBillCycle));
	                }
	            }

	            before =
				    BillCycleHistorySupport.getLastEventBefore(ctx,
				        account.getBAN(), null,
				        history.getBillCycleChangeDate());
			}
        	
        	if (before != null)
			{
				result.setPreviousBillCycleID((long) before.getOldBillCycleID());
				result.setPreviousBillCycleChange(calendarSupport.dateToCalendar(before.getBillCycleChangeDate()));
			}
        }
		return result;
    }
    
    @Override
    public ExecuteResult executeAccountBalanceQuery(CRMRequestHeader header,
            String accountID, String[] balanceTypes, GenericParameter[] parameters)
            throws CRMExceptionFault
    {
        final Context ctx = getContext().createSubContext();

        QueryExecutorFactory executor = QueryExecutorFactory.getInstance();

        return executor.execute(ctx, AccountServiceSkeletonInterface.class.getSimpleName(), "executeAccountBalanceQuery", ExecuteResult.class, 
                header, accountID, balanceTypes, parameters);
    }

    @Override
    public AccountStateTransitionResult updateAccountWithStateTransition(CRMRequestHeader header,
            String accountID, AccountState[] currentState, AccountState newState,
            GenericParameter[] parameters) throws CRMExceptionFault
    {
        final Context ctx = getContext().createSubContext();

        QueryExecutorFactory executor = QueryExecutorFactory.getInstance();

        return executor.execute(ctx, AccountServiceSkeletonInterface.class.getSimpleName(), "updateAccountWithStateTransition", AccountStateTransitionResult.class, 
                header, accountID, currentState, newState, parameters);
    }

    @Override
    public AccountForIDResults listAccountsForID(CRMRequestHeader header, IdentificationEntry[] ids,
            AccountState[] states, GenericParameter[] parameters) throws CRMExceptionFault
    {
        final Context ctx = getContext().createSubContext();

        QueryExecutorFactory executor = QueryExecutorFactory.getInstance();

        return executor.execute(ctx, AccountServiceSkeletonInterface.class.getSimpleName(), "listAccountsForID", AccountForIDResults.class, 
                header, ids, states, parameters);
    }

    @Override
    public AccountCugQueryResults getAccountCug(CRMRequestHeader header, String accountId, 
    		GenericParameter[] parameters) throws CRMExceptionFault
    {
    	final Context ctx = getContext().createSubContext();

        QueryExecutorFactory executor = QueryExecutorFactory.getInstance();

        return executor.execute(ctx, AccountServiceSkeletonInterface.class.getSimpleName(), "getAccountCug", AccountCugQueryResults.class, 
                header, accountId, parameters);
    }
    
    @Override
    public ConvertBillingTypeResponse convertBillingType(CRMRequestHeader header, Boolean validateOnly, 
            ConvertBillingTypeRequest convertrequest, GenericParameter[] parameters) throws CRMExceptionFault
    {
        
        final Context ctx = getContext().createSubContext();
        
        RmiApiSupport.authenticateUser(ctx, header, "convertBillingType", Constants.PERMISSION_ACCOUNTS_WRITE_CONVERTBILLINGTYPE);

        QueryExecutorFactory executor = QueryExecutorFactory.getInstance();

        return executor.execute(ctx, AccountServiceSkeletonInterface.class.getSimpleName(), "convertBillingType", ConvertBillingTypeResponse.class, 
                header, validateOnly, convertrequest, parameters);
    }
    
    @Override
	public DuplicateAccountInfo[] duplicateAccountCheck(CRMRequestHeader arg0, Integer spid, String firstname, String lastname,
			Calendar dateofbirth, IdentificationEntry[] ids, GenericParameter[] arg6) throws CRMExceptionFault {
		final Context ctx = getContext().createSubContext();
		LogSupport.info(ctx,this, "Inputs: "+firstname+"..."+lastname+"..."+ids);
		
		 for (IdentificationEntry id : ids)
	        {
			 	LogSupport.info(ctx,this, "TYPE#########: "+id.getType());  	
	         }
		
		
		DuplicateAccountInfo[] subAccountDuplicates = new DuplicateAccountInfo[]{};
		try
        {
			AccountsApiSupport.validateDuplicateAccountCheckGrp(ctx,firstname, lastname, dateofbirth, ids);
			Collection<Contact> duplicateAccount = new HashSet<Contact>(); 
	      
	        //Get duplicate accounts for Group1;
	        duplicateAccount = AccountsApiSupport.getDuplicateAccount(ctx,firstname, lastname, dateofbirth);
	        LogSupport.info(ctx,this, "duplicateAccount::"+duplicateAccount);
	        // New set to store bans from duplicateAccount
	        Set <String> duplicateAccountBan = new HashSet<String>();
	        for (Contact xc : duplicateAccount)
	        {
	        	duplicateAccountBan.add(xc.getAccount()); // get BAN and put in set for Group1
	        }
	        LogSupport.info(ctx,this, "duplicateAccountBan::"+duplicateAccountBan);

	        
	        //get Duplicate accounts for Group2..
	        
	        Set<String> bansToRetrieve = new HashSet<String>();
		    for (IdentificationEntry id : ids)
	        {
	        	// Retrieve all account identifications for given identification
	        	Collection<com.redknee.app.crm.bean.account.AccountIdentification> accountIdentifications = AccountsApiSupport.getDuplicateIdentity(ctx,id); 
	        
	        	for (com.redknee.app.crm.bean.account.AccountIdentification identification : accountIdentifications)
	        		{
	        			String ban = identification.getBAN(); 
	        			LogSupport.info(ctx,this, "ban::::"+ban);
	        			// 	Add ban to the list of accounts that should be retrieved.
	        			bansToRetrieve.add(ban);
	        		}
	         }
		    //Logger.debug(ctx, this,"bansToRetrieve::"+bansToRetrieve );
	               
		    LogSupport.info(ctx,this, "bansToRetrieve::"+bansToRetrieve);
		    LogSupport.info(ctx,this, "duplicateAccountBan::"+duplicateAccountBan);
	      
	        //Merging duplicate bans for Group1 and Group2.. 
	        
	        for(String duplicateAccounts : bansToRetrieve)
	        {
	            if (!duplicateAccountBan.contains(bansToRetrieve))
	            {
	            	LogSupport.info(ctx,this, "duplicateAccounts:"+duplicateAccounts);
	             	duplicateAccountBan.add(duplicateAccounts);
	            }
	        }
	        
	        LogSupport.info(ctx,this, "duplicateAccountBan::"+duplicateAccountBan);

	        List<DuplicateAccountInfo> accDuplicates = new ArrayList<DuplicateAccountInfo>();
	        Iterator<String> itr = duplicateAccountBan.iterator();
	        String bans="";
	        DuplicateAccountInfo accountInfo= null;
	        Set<String>parentBan=new HashSet<String>();
	        
	        while(itr.hasNext())
	        {
	        	bans=AccountsApiSupport.checkParent(ctx, (itr.next()).toString(),spid);
	        	if(bans!=""){
	        		parentBan.add(bans);
	        		LogSupport.info(ctx,this, "Ban Added in parentBan ::"+bans);
	        	}
	        }
	        LogSupport.info(ctx,this, "Bans in parentBan ::"+parentBan);
	        Iterator<String> iter = parentBan.iterator();
	        
	        while(iter.hasNext())
	        {
	        	bans=(iter.next()).toString();
	        	LogSupport.info(ctx,this, "bans in while::"+bans);
	        		accountInfo= new DuplicateAccountInfo();
	        		accountInfo.setACCOUNTID(bans);
	        		Account acct = AccountsApiSupport.getDuplicateBansAccount(ctx,bans);
	        		
	        		if(acct==null){
	        			continue;
	        		}
	        	
	        	AccountStateEnum accState =acct.getState();
	        	if(accState!=null){
	        		accountInfo.setSTATUS(RmiApiSupport.convertCrmAccountState2Api(acct.getState()));
	        		accDuplicates.add(accountInfo);
	        	}
	        	
	        }
	        LogSupport.info(ctx,this, "accDuplicates::"+accDuplicates);
	     
	        Iterator<DuplicateAccountInfo> accitr = accDuplicates.iterator();
	        LogSupport.info(ctx,this, "Removing Duplicates accDuplicates::");
	        while(accitr.hasNext()){
	        	 LogSupport.info(ctx,this, ""+accitr.next());
	        }
	        LogSupport.info(ctx,this, "accDuplicates::"+accDuplicates);
	        subAccountDuplicates=accDuplicates.toArray(new DuplicateAccountInfo[accDuplicates.size()]);
	        LogSupport.info(ctx,this, "subAccountDuplicates::"+subAccountDuplicates);
	        }catch(CRMExceptionFault e){
	        	RmiApiErrorHandlingSupport.generalException(ctx, e, e.getMessage(), this); 
	        }
	        catch (final Exception e)
	        {
	            final String msg = "Unable to retrieve Sub Accounts duplicates for Account ";
	            RmiApiErrorHandlingSupport.handleQueryExceptions(ctx, e, msg, this);
	        }
	        //Logger.debug(ctx, this,"subAccountDuplicates::"+subAccountDuplicates );
	        return subAccountDuplicates;
	}
    
	@Override
	public DoInternalCreditCheckResponse doInternalCreditCheck(CRMRequestHeader cRMRequestHeader, int spid, String accountID,
			IdentificationEntry[] identificationEntry, Boolean subCountRequired, GenericParameter[] genericParameter) throws CRMExceptionFault {
		DoInternalCreditCheckResponse response = new DoInternalCreditCheckResponse();
      	final Context ctx = getContext().createSubContext();
      	try {
      		if (LogSupport.isDebugEnabled(ctx)) {
    			LogSupport.debug(ctx, this, "Values of spid:: "+spid+" accountID:: "+accountID+" subCountRequired:: "+subCountRequired);
    		}
			CRMSpid spidObj = SpidSupport.getCRMSpid(ctx, spid);
			if(spidObj == null){
				final String msg = "Can not find SPID in System";
				RmiApiErrorHandlingSupport.simpleValidation("SPID", msg);
			}else{
				LogSupport.info(ctx,this, "Internal Credit check implementation.>>");
		      	BlacklistStatus blackListStatus = null;
		      	for(IdentificationEntry iEntry : identificationEntry){
		      		blackListStatus = AccountsApiSupport.getBlacklistStatus(ctx,cRMRequestHeader, iEntry, genericParameter);
		      		response.addBlackListStatus(blackListStatus);
		      	 }
		      	if(accountID!=null && !accountID.trim().isEmpty()){
		      		if(subCountRequired == true){
		      			Account account = AccountSupport.getAccount(ctx, accountID);
			      		if(account == null){
							final String msg = "Can not find Account for ban "+accountID;
							RmiApiErrorHandlingSupport.simpleValidation("Account", msg);
						}else{
			      	    LogSupport.info(ctx,this, "Setting External Reevaluation and Subscription Count");
			      	    response.setSubscriptionCount(AccountsApiSupport.getSubscriptionCount(ctx,accountID));
			      	   }
			      	  response.setExternalReevaluationRequired(AccountsApiSupport.getExternalReevaluationRequired(ctx,spid,accountID));
		      		}else{
		      			LogSupport.info(ctx,this, "External Reevaluation and Subscription Count not req.");
		      			response.setExternalReevaluationRequired(AccountsApiSupport.getExternalReevaluationRequired(ctx,spid,accountID));
		      		}
		      	}else{
		      		response.setExternalReevaluationRequired(null);
		      	}
		  }
		} catch (HomeException e) {
			 RmiApiErrorHandlingSupport.simpleValidation("SPID", "Can not find Spid "+spid+" or Account for "+accountID);
		}
      	
  		return response;
		}
		 
	
    public Home updateAccountPipeline(final Context ctx, final String caller) throws CRMExceptionFault
	    {
        return ApiSupport.injectAPIUpdateEntityHomeIntoPipeline(ctx,  getAccountHome(ctx), caller);
	    	}

    /**
     * Returns the account home.
     *
     * @param ctx
     *            The operating context.
     * @return The requested home.
     * @throws CRMExceptionFault
     *             Thrown if there are problems looking up the home.
     */
    public static Home getAccountHome(final Context ctx) throws CRMExceptionFault
	    	{
        return RmiApiSupport.getCrmHome(ctx, AccountHome.class, AccountsImpl.class);
	    	}
	    	
	 
    /**
     * {@inheritDoc}
     */
    @Override
    public Context getContext()
	    {
        return this.context_;
	    	}
	    	
	 
    /**
     * {@inheritDoc}
     */
    @Override
    public void setContext(final Context ctx)
	        {
        this.context_ = ctx;
	            }
	            
    /**
     * The operating context.
     */
    private Context context_;
	            	
    /**
     * CRM account type to API account type adapter.
     */
    private final AccountTypeToApiAdapter accountTypeToApiAdapter_;
	            		
    /**
     * CRM account to API account adapter.
     */
    private final AccountToApiAdapter accountToApiAdapter_;
	            	
    private final AccountCreationTemplateToApiAdapter accountCreationTemplateToApiAdapter_;
		        	
    private final AccountRoleToApiAdapter accountRoleToApiAdapter_;
				 
    private final AccountExtensionToApiAdapter accountExtensionToApiAdapter_;
	    
    private final AccountExtensionToApiReferenceAdapter accountExtensionToApiReferenceAdapter_;

	private final AgedDebtToApiAdapter agedDebtToApiAdapter_;
	
	private void logAndThrowException(Context ctx, String msg) throws CRMExceptionFault {
		LogSupport.minor(ctx, this, msg);
		throw new CRMExceptionFault(msg);
    }

	@Override
	public AccountFinancialInfo[] getAccountFinancialInfo(CRMRequestHeader header, String accountID,
			String pagekey, int limit, GenericParameter[] parameters) throws CRMExceptionFault {
		
		 
		final Context ctx = getContext().createSubContext();
		 
		 if (StringUtil.checkIfStringIsEmpty(accountID)){
			 logAndThrowException(ctx, "Account ID is a mandatory parameter and cannot be NULL."); 
		 }
		 
		 if(limit <= 0){
			 logAndThrowException(ctx, "SubAccounts is a mandatory parameter and cannot be empty.");
		 }
		 
		List<FinancialAccount> accountList = null;
		try {
			LogSupport.debug(ctx, this,"Getting Financial Account Info..!!");
			try {
				accountList = FinancialAccountSupport.getFinancialAccountInfo(ctx,header,accountID,parameters,pagekey,limit);
			} catch (HomeException he) {
				LogSupport.minor(ctx, this, "Home Exception while calling FinancialAccountSupport.getFinancialAccountInfo : "+he);
			}
		} catch (FinancialAccountException e) {
			logAndThrowException(ctx, "Exception while fetching account information for root account "+accountID+" : "+e);
		}


		AccountFinancialInfo[] accountFinancialInfos = new AccountFinancialInfo[accountList.size()];
 
		int num = 0;
		for(FinancialAccount financialAccount : accountList){
			AccountFinancialInfo accountFinancialinfo = new AccountFinancialInfo();
			AccountFinInfo accountFinInfo = new AccountFinInfo();
				accountFinInfo.setAccountName(financialAccount.getAccount().getAccountName());
				accountFinInfo.setFirstName(financialAccount.getAccount().getFirstName());
				accountFinInfo.setBan(financialAccount.getAccount().getBAN());
				accountFinInfo.setLastName(financialAccount.getAccount().getLastName());
				accountFinInfo.setPaymentDueDate(FinancialAccountSupport.toCalendar(financialAccount.getPaymentDueDate()));
				accountFinInfo.setCurrentDebtAge(financialAccount.getDebtAge());
				accountFinInfo.setPaymentPlan(financialAccount.getPaymentPlan());
				accountFinInfo.setPaymentPlanAmount(financialAccount.getPaymentPlanOutstandingAmount());
				accountFinInfo.setTaxClassName(financialAccount.getTax());
				accountFinInfo.setIsDunningExempted(financialAccount.isExemptFromDunning());;
				accountFinInfo.setPapFlag(financialAccount.isPapIndicator());
				accountFinInfo.setInvoiceDeliveryOption(financialAccount.getInvoiceDeliveryMethod());
				accountFinInfo.setParameters(financialAccount.getResponseParameter());
				accountFinInfo.addParameters(FinancialAccountSupport.addGenericParameter(ctx, "PAST_DUE_AMOUNT", financialAccount.getPastDueAmount()));
				accountFinInfo.addParameters(FinancialAccountSupport.addGenericParameter(ctx, "NEXT_DUNNING_LEVEL_DATE", financialAccount.getNextDunningLevelDate()));
				accountFinInfo.setCurrentInvoice(financialAccount.getCurrentInvoice());
			accountFinancialinfo.setAccountFinInfo(accountFinInfo);
			accountFinancialinfo.setPagekey(financialAccount.getAccount().getBAN());
			accountFinancialInfos[num] = accountFinancialinfo;
			num++;
		}
		LogSupport.debug(ctx, this,"Return Financial Account Info");
		return accountFinancialInfos;
	}

    @Override
	public AccountProfileWithServiceAddressQueryResults getAccountProfileWithServiceAddress(CRMRequestHeader header,
			String accountID, GenericParameter[] parameters) throws CRMExceptionFault {
		final Context ctx = getContext().createSubContext();

		QueryExecutorFactory executor = QueryExecutorFactory.getInstance();

		return executor.execute(ctx, AccountServiceSkeletonInterface.class.getSimpleName(),
				"getAccountProfileWithServiceAddress", AccountProfileWithServiceAddressQueryResults.class, header,
				accountID, parameters);
	}

	@Override
	public AccountReference createAccountWithServiceAddress(ServiceAddressInput[] serviceAddress, CRMRequestHeader header,
			AccountProfile profile, AccountDetail detail, AccountBilling billing, AccountPaymentInfo paymentInfo,
			AccountIdentification identification, AccountCompanyContactInfo company, AccountBankInfo bank,
			AccountContactInfo contact, BaseAccountExtension[] extension, GenericParameter[] parameters)
			throws CRMExceptionFault {
		AccountReference accountReference = createAccount(header, profile, detail, billing, paymentInfo, identification,
				company, bank, contact, extension, parameters);
		saveServiceAddressForAccount(serviceAddress, accountReference);
		return accountReference;
	}

	private void saveServiceAddressForAccount(ServiceAddressInput[] serviceAddresses, AccountReference accountReference)
			throws CRMExceptionFault {
		if (null != serviceAddresses) {
			String ban = null != accountReference ? accountReference.getIdentifier() : null;
			Context ctx = getContext().createSubContext();
			HomeSupport homeSupport = HomeSupportHelper.get(ctx);
			for (ServiceAddressInput serviceAddress : serviceAddresses) {
				Address address = new Address();
				if (null != serviceAddress.getAddressType()) {
					address.setAddressType(AddressTypeEnum.get(serviceAddress.getAddressType().shortValue()));
				}
				address.setBan(ban);
				populateAddressLines(serviceAddress, address);
				try {
					homeSupport.createBean(ctx, address);
				} catch (final Exception e) {
					final String msg = "Unable to create Service Address for Account " + ban + ".";
					RmiApiErrorHandlingSupport.handleCreateExceptions(ctx, e, msg, false, Account.class, ban, this);
				}
			}
		}
	}

	private void populateAddressLines(ServiceAddressInput serviceAddress, Address address) {
		String[] serviceAddressLines = serviceAddress.getAddressLine();
		if (null != serviceAddressLines) {
			for (int index = 0; index < serviceAddressLines.length; index++) {
				if (0 == index) {
					address.setAddressLine1(getNonEmptyAddressLine(serviceAddressLines[index]));
				} else if (1 == index) {
					address.setAddressLine2(getNonEmptyAddressLine(serviceAddressLines[index]));
				} else if (2 == index) {
					address.setAddressLine3(getNonEmptyAddressLine(serviceAddressLines[index]));
				} else if (3 == index) {
					address.setAddressLine4(getNonEmptyAddressLine(serviceAddressLines[index]));
				} else if (4 == index) {
					address.setAddressLine5(getNonEmptyAddressLine(serviceAddressLines[index]));
				} else if (5 == index) {
					address.setAddressLine6(getNonEmptyAddressLine(serviceAddressLines[index]));
				} else if (6 == index) {
					address.setAddressLine7(getNonEmptyAddressLine(serviceAddressLines[index]));
				} else if (7 == index) {
					address.setAddressLine8(getNonEmptyAddressLine(serviceAddressLines[index]));
				} else if (8 == index) {
					address.setAddressLine9(getNonEmptyAddressLine(serviceAddressLines[index]));
				} else if (9 == index) {
					address.setAddressLine10(getNonEmptyAddressLine(serviceAddressLines[index]));
				}
			}
		}
	}

	private static String getNonEmptyAddressLine(String addressLine) {
		return (null != addressLine && 0 < addressLine.trim().length()) ? addressLine : null;
	}
}
