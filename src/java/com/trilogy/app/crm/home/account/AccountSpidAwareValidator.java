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

package com.trilogy.app.crm.home.account;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.trilogy.app.crm.LicenseConstants;
import com.trilogy.app.crm.account.AccountConstants;
import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountXInfo;
import com.trilogy.app.crm.bean.BillCycle;
import com.trilogy.app.crm.bean.CreditCategory;
import com.trilogy.app.crm.bean.DealerCode;
import com.trilogy.app.crm.bean.DealerCodeHome;
import com.trilogy.app.crm.bean.DealerCodeXInfo;
import com.trilogy.app.crm.bean.Identification;
import com.trilogy.app.crm.bean.IdentificationHome;
import com.trilogy.app.crm.bean.IdentificationXInfo;
import com.trilogy.app.crm.bean.Msisdn;
import com.trilogy.app.crm.bean.Province;
import com.trilogy.app.crm.bean.ProvinceHome;
import com.trilogy.app.crm.bean.ProvinceXInfo;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.bean.TaxAuthority;
import com.trilogy.app.crm.bean.TaxAuthorityHome;
import com.trilogy.app.crm.bean.account.AccountIdentification;
import com.trilogy.app.crm.bean.account.AccountIdentificationXInfo;
import com.trilogy.app.crm.bean.payment.AbstractPaymentPlan;
import com.trilogy.app.crm.bean.payment.Contract;
import com.trilogy.app.crm.bean.payment.PaymentPlan;
import com.trilogy.app.crm.bean.payment.PaymentPlanHome;
import com.trilogy.app.crm.extension.account.PoolExtension;
import com.trilogy.app.crm.extension.account.PoolExtensionXInfo;
import com.trilogy.app.crm.support.AccountSupport;
import com.trilogy.app.crm.support.ContractSupport;
import com.trilogy.app.crm.support.CreditCategorySupport;
import com.trilogy.app.crm.support.LicensingSupportHelper;
import com.trilogy.app.crm.support.MsisdnSupport;
import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.beans.SafetyUtil;
import com.trilogy.framework.xhome.beans.Validator;
import com.trilogy.framework.xhome.beans.xi.PropertyInfo;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeOperationEnum;
import com.trilogy.framework.xlog.log.LogSupport;

/**
 * Validates all SPID-aware beans referenced by the account are of the same SPID.
 *
 * @author cindy.wong@redknee.com
 * @since Sep 13, 2007
 */
public class AccountSpidAwareValidator implements Validator
{

    /**
     * Create a new instance of <code>SpidAwareValidator</code>.
     */
    protected AccountSpidAwareValidator()
    {
        // empty
    }


    /**
     * Returns an instance of <code>SpidAwareValidator</code>.
     *
     * @return An instance of <code>SpidAwareValidator</code>.
     */
    public static AccountSpidAwareValidator instance()
    {
        if (instance == null)
        {
            instance = new AccountSpidAwareValidator();
        }
        return instance;
    }


    /**
     * {@inheritDoc}
     */
    public void validate(final Context context, final Object object) throws IllegalStateException
    {
        final Account newAccount = (Account) object;
        final Account oldAccount = (Account) context.get(AccountConstants.OLD_ACCOUNT);
        final CompoundIllegalStateException exceptions = new CompoundIllegalStateException();

        final HomeOperationEnum currHomeOper = (HomeOperationEnum) context.get(HomeOperationEnum.class);
        
        validateBillCycleSpid(context, oldAccount, newAccount, currHomeOper, exceptions);
        validateCreditCategorySpid(context, oldAccount, newAccount, currHomeOper, exceptions);
        validateDealerCodeSpid(context, oldAccount, newAccount, currHomeOper, exceptions);
        validateTaxAuthoritySpid(context, oldAccount, newAccount, currHomeOper, exceptions);
        validatePaymentPlanSpid(context, oldAccount, newAccount, currHomeOper, exceptions);

        validateIdentificationTypeSpid(context, oldAccount, newAccount, currHomeOper, AccountIdentification.DEFAULT_IDTYPE, exceptions);

        validateProvinceSpid(context, newAccount.getSpid(), AccountXInfo.BILLING_PROVINCE, oldAccount, newAccount, currHomeOper, exceptions);
        validateProvinceSpid(context, newAccount.getSpid(), AccountXInfo.COMPANY_PROVINCE, oldAccount, newAccount, currHomeOper, exceptions);
        validateMsisdnSpid(context, AccountXInfo.VPN_MSISDN, oldAccount, newAccount, currHomeOper, exceptions);
        validateMsisdnSpid(context, AccountXInfo.OWNER_MSISDN, oldAccount, newAccount, currHomeOper,
                LicensingSupportHelper.get(context).isLicensed(context,
                        LicenseConstants.ACCOUNT_SMS_NUMBER_SPID_VALIDATION), exceptions);
        validateParentAccountSpid(context, oldAccount, newAccount, currHomeOper, exceptions);
        validateContractSpid(context, oldAccount, newAccount, currHomeOper, exceptions);

        if (newAccount.isPooled(context))
        {
            PoolExtension newExtension = (PoolExtension) newAccount.getFirstAccountExtensionOfType(PoolExtension.class);
            if (newExtension != null)
            {
                String newPoolMsisdn = newExtension.getPoolMSISDN();
                String oldPoolMsisdn = null;
                if (oldAccount != null)
                {
                    PoolExtension oldExtension = (PoolExtension) oldAccount.getFirstAccountExtensionOfType(PoolExtension.class);
                    if (oldExtension != null)
                    {
                        oldPoolMsisdn = oldExtension.getPoolMSISDN();
                    }
                }

                validateMsisdnSpid(context, PoolExtensionXInfo.POOL_MSISDN, oldPoolMsisdn, newPoolMsisdn, newAccount,
                        currHomeOper, exceptions);
            }
        }

        // throw all
        exceptions.throwAll();
    }


    /**
     * Validates the contract selected belongs to the same SPID as the account.
     *
     * @param context
     *            The operating context.
     * @param oldAccount
     *            The old account.
     * @param newAccount
     *            The new account.
     * @param exceptions
     *            Exception listener. Any exceptions which should be thrown by this method
     *            are added to it.
     */
    private void validateContractSpid(final Context context, final Account oldAccount, final Account newAccount, final HomeOperationEnum currHomeOper,
        final CompoundIllegalStateException exceptions)
    {
        boolean validate = false;
        final long newContractId = newAccount.getContract();
        if (newContractId != 0)
        {
            validate = HomeOperationEnum.CREATE == currHomeOper || ( oldAccount != null && oldAccount.getContract() != newContractId);
        }

        if (validate)
        {
            Contract contract = null;
            try
            {
                contract = ContractSupport.findContract(context, newContractId);
            }
            catch (final Exception exception)
            {
                if (LogSupport.isDebugEnabled(context))
                {
                    final StringBuilder sb = new StringBuilder();
                    sb.append(exception.getClass().getSimpleName());
                    sb.append(" caught in ");
                    sb.append(getClass().getSimpleName());
                    sb.append(".validateContractSpid(): ");
                    if (exception.getMessage() != null)
                    {
                        sb.append(exception.getMessage());
                    }
                    LogSupport.debug(context, this, sb.toString(), exception);
                }
                exceptions.thrown(exception);
                return;
            }

            if (contract != null && contract.getSpid() != newAccount.getSpid())
            {
                exceptions.thrown(new IllegalPropertyArgumentException(AccountXInfo.CONTRACT,
                    "Contract does not belong to the same service provider as the account"));
            }
        }
    }


    /**
     * Validates the parent account selected belongs to the same SPID as the account.
     *
     * @param context
     *            The operating context.
     * @param oldAccount
     *            The old account.
     * @param newAccount
     *            The new account.
     * @param exceptions
     *            Exception listener. Any exceptions which should be thrown by this method
     *            are added to it.
     */
    private void validateParentAccountSpid(final Context context, final Account oldAccount, final Account newAccount,
            final HomeOperationEnum currHomeOper, final CompoundIllegalStateException exceptions)
    {
        boolean validate = false;
        final String newParentBan = newAccount.getParentBAN();
        if (newParentBan != null && newParentBan.trim().length() > 0)
        {
            validate = HomeOperationEnum.CREATE == currHomeOper || ( oldAccount != null && !SafetyUtil.safeEquals(newParentBan, oldAccount.getParentBAN()));
        }

        if (validate)
        {
            Account parent = null;
            try
            {
                parent = AccountSupport.getAccount(context, newParentBan);
            }
            catch (final Exception exception)
            {
                if (LogSupport.isDebugEnabled(context))
                {
                    final StringBuilder sb = new StringBuilder();
                    sb.append(exception.getClass().getSimpleName());
                    sb.append(" caught in ");
                    sb.append(getClass().getSimpleName());
                    sb.append(".validateParentAccountSpid(): ");
                    if (exception.getMessage() != null)
                    {
                        sb.append(exception.getMessage());
                    }
                    LogSupport.debug(context, this, sb.toString(), exception);
                }
                exceptions.thrown(exception);
                return;
            }

            if (parent == null)
            {
                exceptions.thrown(new IllegalPropertyArgumentException(AccountXInfo.PARENT_BAN, "Parent account \""
                    + newParentBan + "\" does not exist"));
            }
            else if (parent.getSpid() != newAccount.getSpid())
            {
                exceptions.thrown(new IllegalPropertyArgumentException(AccountXInfo.PARENT_BAN,
                    "Parent account does not belong to the same service provider as the account"));
            }
        }
    }


    private void validateMsisdnSpid(final Context context, final PropertyInfo property, final Account oldAccount,
            final Account newAccount, final HomeOperationEnum currHomeOper, final CompoundIllegalStateException exceptions)
    {
        validateMsisdnSpid(context, property, oldAccount, newAccount, currHomeOper, true, exceptions);
    }

    /**
     * Validates the MSISDN selected belongs to the same SPID as the account.
     *
     * @param context
     *            The operating context.
     * @param property
     *            The property being validated.
     * @param oldAccount
     *            The old account.
     * @param newAccount
     *            The new account.
     * @param exceptions
     *            Exception listener. Any exceptions which should be thrown by this method
     *            are added to it.
     */
    private void validateMsisdnSpid(final Context context, final PropertyInfo property, final Account oldAccount,
        final Account newAccount, final HomeOperationEnum currHomeOper, final boolean validateSpid, final CompoundIllegalStateException exceptions)
    {
        final String newValue = (String) property.get(newAccount);
        String oldValue = null;
        if (oldAccount != null)
        {
            oldValue = (String) property.get(oldAccount);
        }

        validateMsisdnSpid(context, property, oldValue, newValue, newAccount, currHomeOper, validateSpid, exceptions);
    }

    private void validateMsisdnSpid(final Context context, final PropertyInfo property, final String oldValue,
            final String newValue, final Account newAccount, final HomeOperationEnum currHomeOper, 
                final CompoundIllegalStateException exceptions)
    {
       validateMsisdnSpid(context, property, oldValue, newValue, newAccount, currHomeOper, true, exceptions);
    }

    /**
     * Validates the MSISDN selected belongs to the same SPID as the account.
     *
     * @param context
     *            The operating context.
     * @param property
     *            The property being validated.
     * @param oldValue
     *            The old value.
     * @param newValue
     *            The new value.
     * @param exceptions
     *            Exception listener. Any exceptions which should be thrown by this method
     *            are added to it.
     */
    private void validateMsisdnSpid(final Context context, final PropertyInfo property, final String oldValue,
        final String newValue, final Account newAccount, final HomeOperationEnum currHomeOper, final boolean validateSpid,
            final CompoundIllegalStateException exceptions)
    {
        boolean validate = false;
        if (newValue != null && newValue.length() > 0)
        {
            validate = HomeOperationEnum.CREATE == currHomeOper
                    || (oldValue != null && !SafetyUtil.safeEquals(newValue, oldValue));
        }

        if (validate)
        {
            Msisdn msisdn = null;
            try
            {
                msisdn = MsisdnSupport.getMsisdn(context, newValue);
            }
            catch (final Exception exception)
            {
                if (LogSupport.isDebugEnabled(context))
                {
                    final StringBuilder sb = new StringBuilder();
                    sb.append(exception.getClass().getSimpleName());
                    sb.append(" caught in ");
                    sb.append(getClass().getSimpleName());
                    sb.append(".validateMsisdnSpid(): ");
                    if (exception.getMessage() != null)
                    {
                        sb.append(exception.getMessage());
                    }
                    LogSupport.debug(context, this, sb.toString(), exception);
                }
                exceptions.thrown(exception);
                return;
            }

            if (msisdn == null)
            {
                exceptions.thrown(new IllegalPropertyArgumentException(property, "MSISDN \"" + newValue
                    + "\" does not exist"));
            }
            else if (validateSpid && msisdn.getSpid() != newAccount.getSpid())
            {
                exceptions.thrown(new IllegalPropertyArgumentException(property, property.getLabel()
                    + " does not belong to the same service provider as the account"));
            }
        }
    }


    /**
     * Validates the identification type selected belongs to the same SPID as the account.
     *
     * @param context
     *            The operating context.
     * @param oldAccount
     *            The old account.
     * @param newAccount
     *            The new account.
     * @param exceptions
     *            Exception listener. Any exceptions which should be thrown by this method
     *            are added to it.
     */
    private void validateIdentificationTypeSpid(
        final Context context,
        final Account oldAccount,
        final Account newAccount,
        final HomeOperationEnum currHomeOper,
        final int defaultValue,
        final CompoundIllegalStateException exceptions)
    {
        final Home home = (Home) context.get(IdentificationHome.class);
        if (home == null)
        {
            exceptions.thrown(new IllegalStateException("Cannot find identification home in context"));
            return;
        }

        List accountIdList = newAccount.getIdentificationList();
        if(null != accountIdList)
        {
            // get all the ids defined for the account's SPID
            Collection allowedIds = null;
            try
            {
                allowedIds = home.select(context, new EQ(IdentificationXInfo.SPID, newAccount.getSpid()));
            }
            catch (final Exception exception)
            {
                if (LogSupport.isDebugEnabled(context))
                {
                    final StringBuilder sb = new StringBuilder();
                    sb.append(exception.getClass().getSimpleName());
                    sb.append(" caught in ");
                    sb.append(getClass().getSimpleName());
                    sb.append(".validateIdentificationTypeSpid(): ");
                    if (exception.getMessage() != null)
                    {
                        sb.append(exception.getMessage());
                    }
                    LogSupport.debug(context, this, sb.toString(), exception);
                }
                exceptions.thrown(exception);
                return;
            }

            Iterator accountIdIt = accountIdList.iterator();
            while(accountIdIt.hasNext())
            {
                AccountIdentification ai = (AccountIdentification)accountIdIt.next();
                boolean found = false;

                // search for the id in the list of allowed ids
                Iterator allowedIdsIt = allowedIds.iterator();
                while(allowedIdsIt.hasNext())
                {
                    Identification id = (Identification)allowedIdsIt.next();
                    if(id.getCode() == ai.getIdType())
                    {
                        found = true;
                        break;
                    }
                }

                if(!found)
                {
                    exceptions.thrown(new IllegalPropertyArgumentException(AccountIdentificationXInfo.ID_TYPE, AccountIdentificationXInfo.ID_TYPE.getLabel()
                            + " does not belong to the same service provider as the account"));
                }
            }
        }
    }


    /**
     * Validates the province selected belongs to the same SPID as the account.
     *
     * @param context
     *            The operating context.
     * @param property
     *            The property being validated.
     * @param oldAccount
     *            The old account.
     * @param newAccount
     *            The new account.
     * @param exceptions
     *            Exception listener. Any exceptions which should be thrown by this method
     *            are added to it.
     */
    private void validateProvinceSpid(final Context context, int spid, final PropertyInfo property, final Account oldAccount,
        final Account newAccount, final HomeOperationEnum currHomeOper, final CompoundIllegalStateException exceptions)
    {
        boolean validate = false;
        final Object newType = property.get(newAccount);
        if (newType != null && ((String) newType).length() > 0)
        {
            validate = HomeOperationEnum.CREATE == currHomeOper || ( oldAccount != null &&  !SafetyUtil.safeEquals(newType, property.get(oldAccount)));
        }

        if (validate)
        {
            Province province = null;
            final Home home = (Home) context.get(ProvinceHome.class);
            if (home == null)
            {
                exceptions.thrown(new IllegalStateException("Cannot find province home in context"));
                return;
            }
            try{
                province = (Province) home.find(context, property.get(newAccount));
            }
            catch (final Exception exception)
            {
                if (LogSupport.isDebugEnabled(context))
                {
                    final StringBuilder sb = new StringBuilder();
                    sb.append(exception.getClass().getSimpleName());
                    sb.append(" caught in ");
                    sb.append(getClass().getSimpleName());
                    sb.append(".validateProvinceSpid(): ");
                    if (exception.getMessage() != null)
                    {
                        sb.append(exception.getMessage());
                    }
                    LogSupport.debug(context, this, sb.toString(), exception);
                }
                exceptions.thrown(exception);
                return;
            }

            if (province == null)
            {
                exceptions.thrown(new IllegalPropertyArgumentException(property, "Cannot find " + property.getLabel()
                    + " " + property.get(newAccount)));
            }
            else if (province.getSpid() != newAccount.getSpid())
            {
                exceptions.thrown(new IllegalPropertyArgumentException(property, property.getLabel()
                    + " " + property.get(newAccount) + " does not belong to the same service provider as the account"));
            }
        }
    }


    /**
     * Validates the payment plan selected belongs to the same SPID as the account.
     *
     * @param context
     *            The operating context.
     * @param oldAccount
     *            The old account.
     * @param newAccount
     *            The new account.
     * @param exceptions
     *            Exception listener. Any exceptions which should be thrown by this method
     *            are added to it.
     */
    private void validatePaymentPlanSpid(final Context context, final Account oldAccount, final Account newAccount,
            final HomeOperationEnum currHomeOper, final CompoundIllegalStateException exceptions)
    {
        boolean validate = false;
        if (newAccount.getPaymentPlan() != AbstractPaymentPlan.DEFAULT_ID)
        {
            validate = HomeOperationEnum.CREATE == currHomeOper || 
                ( oldAccount != null &&  oldAccount.getPaymentPlan() != newAccount.getPaymentPlan());
        }

        if (validate)
        {
            PaymentPlan paymentPlan = null;
            final Home home = (Home) context.get(PaymentPlanHome.class);
            if (home == null)
            {
                exceptions.thrown(new IllegalStateException("Cannot find payment plan home in context"));
                return;
            }
            try
            {
                paymentPlan = (PaymentPlan) home.find(context, Long.valueOf(newAccount.getPaymentPlan()));
            }
            catch (final HomeException exception)
            {
                if (LogSupport.isDebugEnabled(context))
                {
                    final StringBuilder sb = new StringBuilder();
                    sb.append(exception.getClass().getSimpleName());
                    sb.append(" caught in ");
                    sb.append(getClass().getSimpleName());
                    sb.append(".validatePaymentPlanSpid(): ");
                    if (exception.getMessage() != null)
                    {
                        sb.append(exception.getMessage());
                    }
                    LogSupport.debug(context, this, sb.toString(), exception);
                }
                exceptions.thrown(exception);
                return;
            }

            if (paymentPlan == null)
            {
                exceptions.thrown(new IllegalPropertyArgumentException(AccountXInfo.PAYMENT_PLAN,
                    "Cannot find payment plan " + newAccount.getPaymentPlan()));
            }
            else if (paymentPlan.getSpid() != newAccount.getSpid())
            {
                exceptions.thrown(new IllegalPropertyArgumentException(AccountXInfo.PAYMENT_PLAN,
                    "Payment plan does not belong to the same service provider as the account"));
            }
        }
    }


    /**
     * Validates the tax authority selected belongs to the same SPID as the account.
     *
     * @param context
     *            The operating context.
     * @param oldAccount
     *            The old account.
     * @param newAccount
     *            The new account.
     * @param exceptions
     *            Exception listener. Any exceptions which should be thrown by this method
     *            are added to it.
     */
    private void validateTaxAuthoritySpid(final Context context, final Account oldAccount, final Account newAccount,
            final HomeOperationEnum currHomeOper, final CompoundIllegalStateException exceptions)
    {
        boolean validate = false;
        if (newAccount.getSystemType() != SubscriberTypeEnum.PREPAID)
        {
            validate = HomeOperationEnum.CREATE == currHomeOper || ( oldAccount != null && oldAccount.getTaxAuthority() != newAccount.getTaxAuthority());
        }

        if (validate)
        {
            TaxAuthority taxAuthority = null;
            final Home home = (Home) context.get(TaxAuthorityHome.class);
            if (home == null)
            {
                exceptions.thrown(new IllegalStateException("Cannot find tax authority home in context"));
                return;
            }
            try
            {
                taxAuthority = (TaxAuthority) home.find(context, Integer.valueOf(newAccount.getTaxAuthority()));
            }
            catch (final HomeException exception)
            {
                if (LogSupport.isDebugEnabled(context))
                {
                    final StringBuilder sb = new StringBuilder();
                    sb.append(exception.getClass().getSimpleName());
                    sb.append(" caught in ");
                    sb.append(getClass().getSimpleName());
                    sb.append(".validateTaxAuthoritySpid(): ");
                    if (exception.getMessage() != null)
                    {
                        sb.append(exception.getMessage());
                    }
                    LogSupport.debug(context, this, sb.toString(), exception);
                }
                exceptions.thrown(exception);
                return;
            }

            if (taxAuthority == null)
            {
                exceptions.thrown(new IllegalPropertyArgumentException(AccountXInfo.TAX_AUTHORITY,
                    "Cannot find tax authority " + newAccount.getTaxAuthority()));
            }
            else if (taxAuthority.getSpid() != newAccount.getSpid())
            {
                exceptions.thrown(new IllegalPropertyArgumentException(AccountXInfo.TAX_AUTHORITY,
                    "Tax authority does not belong to the same service provider as the account"));
            }
        }
    }


    /**
     * Validates the dealer code selected belongs to the same SPID as the account.
     *
     * @param context
     *            The operating context.
     * @param oldAccount
     *            The old account.
     * @param newAccount
     *            The new account.
     * @param exceptions
     *            Exception listener. Any exceptions which should be thrown by this method
     *            are added to it.
     */
    private void validateDealerCodeSpid(final Context context, final Account oldAccount, final Account newAccount,
            final HomeOperationEnum currHomeOper, final CompoundIllegalStateException exceptions)
    {
        boolean validate = false;

        if (HomeOperationEnum.CREATE == currHomeOper || 
                ( oldAccount != null &&  !SafetyUtil.safeEquals(oldAccount.getDealerCode(), newAccount.getDealerCode())))
        {
            validate = newAccount.getDealerCode() != null && newAccount.getDealerCode().trim().length() > 0;
        }

        if (validate)
        {
            DealerCode dealerCode = null;
            final Home home = (Home) context.get(DealerCodeHome.class);
            if (home == null)
            {
                exceptions.thrown(new IllegalStateException("Cannot find dealer code home in context"));
                return;
            }
            try
            {
                And and = new And();
                and.add(new EQ(DealerCodeXInfo.CODE, newAccount.getDealerCode()));
                and.add(new EQ(DealerCodeXInfo.SPID, Integer.valueOf(newAccount.getSpid())));
                dealerCode = (DealerCode) home.find(context, and);
            }
            catch (final HomeException exception)
            {
                if (LogSupport.isDebugEnabled(context))
                {
                    final StringBuilder sb = new StringBuilder();
                    sb.append(exception.getClass().getSimpleName());
                    sb.append(" caught in ");
                    sb.append(getClass().getSimpleName());
                    sb.append(".validateDealerCodeSpid(): ");
                    if (exception.getMessage() != null)
                    {
                        sb.append(exception.getMessage());
                    }
                    LogSupport.debug(context, this, sb.toString(), exception);
                }
                exceptions.thrown(exception);
                return;
            }

            if (dealerCode == null)
            {
                exceptions.thrown(new IllegalPropertyArgumentException(AccountXInfo.DEALER_CODE,
                    "Cannot find dealer code " + newAccount.getDealerCode()));
            }
            else if (dealerCode.getSpid() != newAccount.getSpid())
            {
                exceptions.thrown(new IllegalPropertyArgumentException(AccountXInfo.DEALER_CODE,
                    "Dealer code does not belong to the same service provider as the account"));
            }
        }
    }


    /**
     * Validates the credit category selected belongs to the same SPID as the account.
     *
     * @param context
     *            The operating context.
     * @param oldAccount
     *            The old account.
     * @param newAccount
     *            The new account.
     * @param exceptions
     *            Exception listener. Any exceptions which should be thrown by this method
     *            are added to it.
     */
    private void validateCreditCategorySpid(final Context context, final Account oldAccount, final Account newAccount,
            final HomeOperationEnum currHomeOper, final CompoundIllegalStateException exceptions)
    {
        if (HomeOperationEnum.CREATE == currHomeOper || 
                ( oldAccount != null && oldAccount.getCreditCategory() != newAccount.getCreditCategory()))
        {
            CreditCategory creditCategory = null;
            try
            {
                creditCategory = CreditCategorySupport.findCreditCategory(context, newAccount.getCreditCategory());
            }
            catch (final HomeException exception)
            {
                if (LogSupport.isDebugEnabled(context))
                {
                    final StringBuilder sb = new StringBuilder();
                    sb.append(exception.getClass().getSimpleName());
                    sb.append(" caught in ");
                    sb.append(getClass().getSimpleName());
                    sb.append(".validateCreditCategorySpid(): ");
                    if (exception.getMessage() != null)
                    {
                        sb.append(exception.getMessage());
                    }
                    LogSupport.debug(context, this, sb.toString(), exception);
                }
                exceptions.thrown(exception);
                return;
            }

            if (creditCategory == null)
            {
                exceptions.thrown(new IllegalPropertyArgumentException(AccountXInfo.CREDIT_CATEGORY,
                    "Cannot find credit category " + newAccount.getCreditCategory()));
            }
            else if (creditCategory.getSpid() != newAccount.getSpid())
            {
                exceptions.thrown(new IllegalPropertyArgumentException(AccountXInfo.CREDIT_CATEGORY,
                    "Credit category does not belong to the same service provider as the account"));
            }
        }
    }


    /**
     * Validates the bill cycle selected belongs to the same SPID as the account.
     *
     * @param context
     *            The operating context.
     * @param oldAccount
     *            The old account.
     * @param newAccount
     *            The new account.
     * @param exceptions
     *            Exception listener. Any exceptions which should be thrown by this method
     *            are added to it.
     */
    private void validateBillCycleSpid(final Context context, final Account oldAccount, final Account newAccount,
            final HomeOperationEnum currHomeOper, final CompoundIllegalStateException exceptions)
    {
        if (newAccount.getParentBAN() != null && newAccount.getParentBAN().length() > 0)
        {
            // if parent BAN is set the following validations are wrong.
            // billCycle is the same as the parent account, but is set later in AccountHierachySyncHome
            return;
        }

        if (HomeOperationEnum.CREATE == currHomeOper || ( oldAccount != null &&  oldAccount.getBillCycleID() != newAccount.getBillCycleID()))
        {
            BillCycle billCycle = null;
            try
            {
                billCycle = newAccount.getBillCycle(context);
            }
            catch (final HomeException exception)
            {
                if (LogSupport.isDebugEnabled(context))
                {
                    final StringBuilder sb = new StringBuilder();
                    sb.append(exception.getClass().getSimpleName());
                    sb.append(" caught in ");
                    sb.append(getClass().getSimpleName());
                    sb.append(".validateBillCycleSpid(): ");
                    if (exception.getMessage() != null)
                    {
                        sb.append(exception.getMessage());
                    }
                    LogSupport.debug(context, this, sb.toString(), exception);
                }
                exceptions.thrown(exception);
                return;
            }

            if (billCycle == null)
            {
                               
                if (newAccount.getBillCycleID() == -1)
	            	{
                		exceptions.thrown(new IllegalPropertyArgumentException(AccountXInfo.BILL_CYCLE_ID,
                		"Auto assignable Bill Cycle does not exist"));
	            	}
                else
                  {
                	 	exceptions.thrown(new IllegalPropertyArgumentException(AccountXInfo.BILL_CYCLE_ID,
                		"Cannot find bill cycle " + newAccount.getBillCycleID()));
                  }
            }
            else if (billCycle.getSpid() != newAccount.getSpid())
            {
                if (newAccount.getParentBAN()==null || newAccount.getParentBAN().trim().length()==0)
                {
                    exceptions.thrown(new IllegalPropertyArgumentException(AccountXInfo.BILL_CYCLE_ID,
                        "Bill cycle does not belong to the same service provider as the account"));
                }
            }
        }
    }

    /**
     * Singleton instance.
     */
    private static AccountSpidAwareValidator instance;

}
