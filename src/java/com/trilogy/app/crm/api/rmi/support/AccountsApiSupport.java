/*
 * This code is a protected work and subject to domestic and international copyright
 * law(s). A complete listing of authors of this work is readily available. Additionally,
 * source code is, by its very nature, confidential information and inextricably contains
 * trade secrets and other information proprietary, valuable and sensitive to Redknee. No
 * unauthorized use, disclosure, manipulation or otherwise is permitted, and may only be
 * used in accordance with the terms of the license agreement entered into with Redknee
 * Inc. and/or its subsidiaries.
 * 
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.api.rmi.support;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import com.trilogy.app.crm.api.rmi.GenericParameterParser;
import com.trilogy.app.crm.api.rmi.impl.AccountsImpl;
import com.trilogy.app.crm.bean.AbstractAccount;
import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountCategoryXInfo;
import com.trilogy.app.crm.bean.AccountXInfo;
import com.trilogy.app.crm.bean.AccountsDiscount;
import com.trilogy.app.crm.bean.Address;
import com.trilogy.app.crm.bean.BlackList;
import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.CRMSpidXInfo;
import com.trilogy.app.crm.bean.CreditCategory;
import com.trilogy.app.crm.bean.CreditCategoryXInfo;
import com.trilogy.app.crm.bean.DealerCodeXInfo;
import com.trilogy.app.crm.bean.ExternalCreditCheckHome;
import com.trilogy.app.crm.bean.ExternalCreditCheckXDBHome;
import com.trilogy.app.crm.bean.ExternalCreditCheckXInfo;
import com.trilogy.app.crm.bean.GroupTypeEnum;
import com.trilogy.app.crm.bean.Occupation;
import com.trilogy.app.crm.bean.OccupationXInfo;
import com.trilogy.app.crm.bean.Province;
import com.trilogy.app.crm.bean.ProvinceXInfo;
import com.trilogy.app.crm.bean.SpidIdentificationGroups;
import com.trilogy.app.crm.bean.TransactionMethod;
import com.trilogy.app.crm.bean.TransactionMethodXInfo;
import com.trilogy.app.crm.bean.account.AccountIdentificationGroup;
import com.trilogy.app.crm.bean.account.AccountIdentificationXInfo;
import com.trilogy.app.crm.bean.account.AccountRole;
import com.trilogy.app.crm.bean.account.AccountRoleXInfo;
import com.trilogy.app.crm.bean.account.Contact;
import com.trilogy.app.crm.bean.account.ContactXInfo;
import com.trilogy.app.crm.bean.account.SecurityQuestionAnswer;
import com.trilogy.app.crm.bean.bank.Bank;
import com.trilogy.app.crm.bean.bank.BankXInfo;
import com.trilogy.app.crm.bean.core.BundleFee;
import com.trilogy.app.crm.bean.payment.Contract;
import com.trilogy.app.crm.bean.payment.ContractXInfo;
import com.trilogy.app.crm.blacklist.BlackListSupport;
import com.trilogy.app.crm.extension.ExtensionHolder;
import com.trilogy.app.crm.extension.account.AccountExtensionHolder;
import com.trilogy.app.crm.extension.account.FriendsAndFamilyExtension;
import com.trilogy.app.crm.extension.account.GroupPricePlanExtension;
import com.trilogy.app.crm.extension.account.PoolExtension;
import com.trilogy.app.crm.extension.account.PoolExtensionXInfo;
import com.trilogy.app.crm.extension.account.SubscriberLimitExtension;
import com.trilogy.app.crm.extension.account.SubscriptionPoolProperty;
import com.trilogy.app.crm.invoice.delivery.InvoiceDeliveryOption;
import com.trilogy.app.crm.invoice.delivery.InvoiceDeliveryOptionXInfo;
import com.trilogy.app.crm.move.request.ConvertAccountBillingTypeRequest;
import com.trilogy.app.crm.support.AccountIdentificationSupport;
import com.trilogy.app.crm.support.AccountSupport;
import com.trilogy.app.crm.support.AccountTypeSupportHelper;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.app.crm.support.MultiDbSupportHelper;
import com.trilogy.app.crm.support.SpidSupport;
import com.trilogy.app.crm.util.StringUtil;
import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.beans.FieldValueTooLongException;
import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.beans.PatternMismatchException;
import com.trilogy.framework.xhome.beans.XBeans;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.EQIC;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.language.Lang;
import com.trilogy.framework.xhome.language.LangXInfo;
import com.trilogy.framework.xhome.language.MessageMgrSPI;
import com.trilogy.framework.xhome.util.format.ThreadLocalSimpleDateFormat;
import com.trilogy.framework.xhome.webcontrol.DateWebControl;
import com.trilogy.framework.xhome.xdb.SimpleXStatement;
import com.trilogy.framework.xhome.xdb.XDB;
import com.trilogy.framework.xhome.xdb.XPreparedStatement;
import com.trilogy.framework.xhome.xdb.XStatement;
import com.trilogy.framework.xhome.xdb.visitor.SingleValueXDBVisitor;
import com.trilogy.framework.xlog.log.InfoLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MinorLogMsg;
import com.trilogy.util.crmapi.wsdl.v2_0.types.GenericParameter;
import com.trilogy.util.crmapi.wsdl.v2_1.types.account.AccountBankInfo;
import com.trilogy.util.crmapi.wsdl.v2_1.types.account.AccountCompanyContactInfo;
import com.trilogy.util.crmapi.wsdl.v2_1.types.account.AccountContactInfo;
import com.trilogy.util.crmapi.wsdl.v2_1.types.account.AccountProfile;
import com.trilogy.util.crmapi.wsdl.v2_1.types.account.AccountStateEnum;
import com.trilogy.util.crmapi.wsdl.v2_1.types.account.BillingMessagePreferenceEnum;
import com.trilogy.util.crmapi.wsdl.v2_1.types.account.MutableAccountBankInfo;
import com.trilogy.util.crmapi.wsdl.v2_1.types.account.MutableAccountCompanyContactInfo;
import com.trilogy.util.crmapi.wsdl.v2_1.types.account.MutableAccountContactInfo;
import com.trilogy.util.crmapi.wsdl.v2_1.types.account.MutableAccountProfile;
import com.trilogy.util.crmapi.wsdl.v2_1.types.account.ReadOnlyAccountBankInfo;
import com.trilogy.util.crmapi.wsdl.v2_1.types.account.ReadOnlyAccountCompanyContactInfo;
import com.trilogy.util.crmapi.wsdl.v2_1.types.account.ReadOnlyAccountContactInfo;
import com.trilogy.util.crmapi.wsdl.v2_1.types.account.ReadOnlyAccountProfile;
import com.trilogy.util.crmapi.wsdl.v2_1.types.account.SecurityQuestion;
import com.trilogy.util.crmapi.wsdl.v2_2.types.account.MutableAccountDetail;
import com.trilogy.util.crmapi.wsdl.v3_0.api.CRMExceptionFault;
import com.trilogy.util.crmapi.wsdl.v3_0.types.CRMRequestHeader;
import com.trilogy.util.crmapi.wsdl.v3_0.types.account.AccountBilling;
import com.trilogy.util.crmapi.wsdl.v3_0.types.account.AccountDetail;
import com.trilogy.util.crmapi.wsdl.v3_0.types.account.AccountIdentification;
import com.trilogy.util.crmapi.wsdl.v3_0.types.account.AccountPaymentInfo;
import com.trilogy.util.crmapi.wsdl.v3_0.types.account.AccountReference;
import com.trilogy.util.crmapi.wsdl.v3_0.types.account.BaseAccountExtension;
import com.trilogy.util.crmapi.wsdl.v3_0.types.account.BaseAccountExtensionSequence_type0;
import com.trilogy.util.crmapi.wsdl.v3_0.types.account.BaseAccountExtensionSequence_type1;
import com.trilogy.util.crmapi.wsdl.v3_0.types.account.BaseAccountExtensionSequence_type2;
import com.trilogy.util.crmapi.wsdl.v3_0.types.account.BaseAccountExtensionSequence_type3;
import com.trilogy.util.crmapi.wsdl.v3_0.types.account.GroupHierarchyTypeEnum;
import com.trilogy.util.crmapi.wsdl.v3_0.types.account.MutableAccountBilling;
import com.trilogy.util.crmapi.wsdl.v3_0.types.account.MutableAccountIdentification;
import com.trilogy.util.crmapi.wsdl.v3_0.types.account.MutableAccountPaymentInfo;
import com.trilogy.util.crmapi.wsdl.v3_0.types.account.ReadOnlyAccountBilling;
import com.trilogy.util.crmapi.wsdl.v3_0.types.account.ReadOnlyAccountDetail;
import com.trilogy.util.crmapi.wsdl.v3_0.types.account.ReadOnlyAccountIdentification;
import com.trilogy.util.crmapi.wsdl.v3_0.types.account.ReadOnlyAccountPaymentInfo;
import com.trilogy.util.crmapi.wsdl.v3_0.types.account.ServiceAddressOutput;
import com.trilogy.util.crmapi.wsdl.v3_0.types.account.SubscriptionCount;
import com.trilogy.util.crmapi.wsdl.v3_0.types.account.extensions.AccountExtension;
import com.trilogy.util.crmapi.wsdl.v3_0.types.account.extensions.FriendsAndFamilyAccountExtension;
import com.trilogy.util.crmapi.wsdl.v3_0.types.account.extensions.GroupPricePlanAccountExtension;
import com.trilogy.util.crmapi.wsdl.v3_0.types.account.extensions.MutableFriendsAndFamilyAccountExtension;
import com.trilogy.util.crmapi.wsdl.v3_0.types.account.extensions.MutableGroupPricePlanAccountExtension;
import com.trilogy.util.crmapi.wsdl.v3_0.types.account.extensions.MutableSubscriberLimitAccountExtension;
import com.trilogy.util.crmapi.wsdl.v3_0.types.account.extensions.PoolAccountExtension;
import com.trilogy.util.crmapi.wsdl.v3_0.types.account.extensions.SubscriberLimitAccountExtension;
import com.trilogy.util.crmapi.wsdl.v3_0.types.generalprovisioning.BlacklistStatus;
import com.trilogy.util.crmapi.wsdl.v3_0.types.generalprovisioning.BlacklistType;
import com.trilogy.util.crmapi.wsdl.v3_0.types.generalprovisioning.IdentificationEntry;




/**
 * Support methods for Account objects in API implementation.
 * 
 * @author victor.stratan@redknee.com
 */
public class AccountsApiSupport
{
	public static final Long REMOVE_DISCOUNT = -99L;

    public static ReadOnlyAccountProfile extractProfile(final Account account)
    {
        final ReadOnlyAccountProfile profile = new ReadOnlyAccountProfile();
        profile.setIdentifier(account.getBAN());
        profile.setSpid(account.getSpid());
        profile.setState(AccountStateEnum.valueOf(account.getState().getIndex()));
        profile.setParent(account.getParentBAN());
        profile.setResponsible(account.getResponsible());
        profile.setName(account.getAccountName());
        profile.setCompanyName(account.getCompanyName());
        return profile;
    }


    public static AccountReference adaptAccountToReference(final Account account)
    {
        final AccountReference reference = new AccountReference();
        adaptAccountToReference(account, reference);
        return reference;
    }


    public static AccountReference adaptAccountToReference(final Account account, final AccountReference reference)
    {
        reference.setIdentifier(account.getBAN());
        reference.setSpid(account.getSpid());
        reference.setParent(account.getParentBAN());
        reference.setResponsible(account.getResponsible());
        reference.setRole(account.getRole());
        reference.setState(AccountStateEnum.valueOf(account.getState().getIndex()));
        
        return reference;
    }


    public static void fillInProfile(final Account account, final AccountProfile profile) throws CRMExceptionFault
    {
        fillInMutableProfile(account, profile);
        if (profile.getIdentifier() != null && profile.getIdentifier().length() > 0)
        {
            try
            {
                account.setBAN(profile.getIdentifier());
            }
            catch (final FieldValueTooLongException exception)
            {
                RmiApiErrorHandlingSupport.simpleValidation("AccountProfile.identifier",
                        "The provided account identifier has exceeded the maximum allowed length");
            }
            catch (final PatternMismatchException exception)
            {
                RmiApiErrorHandlingSupport.simpleValidation("AccountProfile.identifier",
                        "The provided account identifer does not conform to the allowed pattern");
            }
        }
        RmiApiSupport.validateExistanceOfBeanForKey(account.getContext(), CRMSpid.class, new EQ(CRMSpidXInfo.ID, profile.getSpid()));
        account.setSpid(profile.getSpid());
        
        account.setState(RmiApiSupport.convertApiAccountState2Crm(profile.getState()));
        if (profile.getParent() != null)
        {
            account.setParentBAN(profile.getParent());
        }
        account.setResponsible(profile.getResponsible());
    }


    public static void fillInMutableProfile(final Account account, final MutableAccountProfile profile)
    {
        if (profile.getName() != null)
        {
            account.setAccountName(profile.getName());
        }
        if (profile.getCompanyName() != null)
        {
            account.setCompanyName(profile.getCompanyName());
        }
    }


    public static ReadOnlyAccountDetail extractDetail(final Account account)
    {
        final ReadOnlyAccountDetail detail = new ReadOnlyAccountDetail();
        detail.setOwnerMobileNumber(account.getOwnerMSISDN());
        detail.setAccountManager(account.getAccountMgr());
        detail.setGreeting(account.getGreeting());        
        detail.setLanguage(account.getLanguage());
        detail.setCurrency(account.getCurrency());
        detail.setIcm(account.getIcm());
        detail.setRole(account.getRole());
        detail.setAccountType(account.getType());
        detail.setSystemType(RmiApiSupport.convertCrmSubscriberPaidType2ApiSystemType(account.getSystemType()));
        detail.setVpnMobileNumber(account.getVpnMSISDN());
        detail.setSuspensionReason(account.getReason());
		detail.setGroupType(GroupHierarchyTypeEnum.valueOf(account.getGroupType().getIndex()));
        //TODO: Implement this (CRM 8.5)
        //detail.setBlockingTemplate(account.getBlockingTemplate());
        return detail;
    }


    public static void fillInDetail(final Account account, final AccountDetail detail) throws CRMExceptionFault
    {
        fillInMutableDetail(account, detail);
        RmiApiSupport.validateExistanceOfBeanForKey(account.getContext(),
                com.redknee.app.crm.bean.account.AccountRole.class, new EQ(AccountRoleXInfo.ID, detail.getRole()));
        account.setRole(detail.getRole());
		RmiApiSupport.validateExistanceOfBeanForKey(account.getContext(),
		    AccountTypeSupportHelper.get(account.getContext())
		        .getAccountTypeClass(account.getContext()), new EQ(
		        AccountCategoryXInfo.IDENTIFIER, detail.getAccountType()));
        account.setType(detail.getAccountType());
        account.setSystemType(RmiApiSupport.convertApiSystemType2CrmSystemType(detail.getSystemType()));
		account.setGroupType(RmiApiSupport
		    .convertApiGroupHierarchyType2CrmGroupType(detail.getGroupType()));

		/*
		 * 2011-05-09: Currency is no longer set on create. See ICD for detail.
		 */
		// if (detail.getCurrency() != null &&
		// !detail.getCurrency().trim().isEmpty())
		// {
		// RmiApiSupport.validateExistanceOfBeanForKey(account.getContext(),
		// Currency.class, new EQ(
		// CurrencyXInfo.CODE, detail.getCurrency()));
		// account.setCurrency(detail.getCurrency());
		// }

        if (detail.getVpnMobileNumber() != null)
        {
            account.setVpnMSISDN(detail.getVpnMobileNumber());
        }
    }


    public static void fillInMutableDetail(final Account account, final MutableAccountDetail detail) throws CRMExceptionFault
    {
        if (detail.getOwnerMobileNumber() != null)
        {
            account.setOwnerMSISDN(detail.getOwnerMobileNumber());
        }
        if (detail.getAccountManager() != null)
        {
            account.setAccountMgr(detail.getAccountManager());
        }
        if (detail.getGreeting() != null)
        {
            account.setGreeting(detail.getGreeting());
        }
        if (detail.getLanguage() != null && !detail.getLanguage().trim().isEmpty())
        {
            RmiApiSupport.validateExistanceOfBeanForKey(account.getContext(), Lang.class, new EQ(LangXInfo.CODE, detail
                    .getLanguage()));
            account.setLanguage(detail.getLanguage());
        }
        if (detail.getIcm() != null)
        {
            account.setIcm(detail.getIcm());
        }
        if (detail.getBlockingTemplate() != null)
        {
            //TODO: Implement this (CRM 8.5)
            //account.setBlockingTemplate(detail.getBlockingTemplate());
        }
        RmiApiSupport.setOptional(account, AccountXInfo.ICM, detail.getIcm());
    }


    public static ReadOnlyAccountBilling extractBilling(final Account account)
    {
        final ReadOnlyAccountBilling billing = new ReadOnlyAccountBilling();
        billing.setPromiseToPayDate(CalendarSupportHelper.get().dateToCalendar(account.getPromiseToPayDate()));
        billing.setCollectionAgentID(account.getDebtCollectionAgencyId());
        billing.setCreditCategory(Long.valueOf(account.getCreditCategory()));
        billing.setDealerCode(account.getDealerCode());
        billing.setDiscountClass(Long.valueOf(account.getDiscountClass()));
        billing.setTaxAuthority(Long.valueOf(account.getTaxAuthority()));
        billing.setTaxExemption(account.getTaxExemption());
        billing.setBillingMsgPreference(BillingMessagePreferenceEnum.valueOf(account.getBillingMsgPreference()
                .getIndex()));        
        billing.setInvoiceDeliveryOptionID(account.getInvoiceDeliveryOption());
        billing.setBillingMessage(account.getBillingMessage());
        billing.setContractID(account.getContract());
        billing.setContractStart(CalendarSupportHelper.get().dateToCalendar(account.getContractStartDate()));
        billing.setContractEnd(CalendarSupportHelper.get().dateToCalendar(account.getContractEndDate()));
        billing.setUseIfNoSubCreditInfo(account.getUseIfNoSubCreditInfo());
        billing.setBillCycle(Long.valueOf(account.getBillCycleID()));
        billing.setInCollectionDate(CalendarSupportHelper.get().dateToCalendar(account.getInCollectionDate()));
        billing.setLastBill(CalendarSupportHelper.get().dateToCalendar(account.getLastBillDate()));
        billing.setSubscriptionCategory(account.getCategory());
        return billing;
    }


    public static void fillInBilling(final Context ctx, final Account account, final AccountBilling billing)
            throws CRMExceptionFault
    {
        fillInMutableBilling(ctx, account, billing);
        
        int billCycleId = (int) billing.getBillCycle();
       
        boolean isAutoAssignBillCycle = false;
        if(account.isPostpaid() && billCycleId ==-1){
        	isAutoAssignBillCycle = true;
        }
        if(!isAutoAssignBillCycle)
        	RmiApiSupport.validateExistanceOfBeanForKey(account.getContext(), com.redknee.app.crm.bean.BillCycle.class, new EQ(
                com.redknee.app.crm.bean.BillCycleXInfo.BILL_CYCLE_ID, billCycleId));
        
        account.setBillCycleID(billCycleId);

        if (billing.getInvoiceDeliveryOptionID() == null
                && !account.isResponsible())
        {
            // For accounts within a hierarchy that are non-responsible, set the Invoice Delivery Option to system configured default or "NONE"
            // Set to default of 3 (None), which is the non-responsible default in the default journal (app-crm-core-data.sch)
            long nonResponsibleDefault = 3L;
            try
            {
                InvoiceDeliveryOption defaultOption = HomeSupportHelper.get(ctx).findBean(ctx, 
                        InvoiceDeliveryOption.class, 
                        new EQ(InvoiceDeliveryOptionXInfo.NON_RESPONSIBLE_DEFAULT, true));
                if (defaultOption != null)
                {
                    nonResponsibleDefault = defaultOption.getId();
                }
            }
            catch (HomeException e)
            {
                //Failed to retrieve the Default delivery option, set with default
                new MinorLogMsg(AccountsApiSupport.class, "Failed to retrieve default non-responsible invoice delivery option.  Using system default value of " + nonResponsibleDefault, e).log(ctx);
            }
            account.setInvoiceDeliveryOption(nonResponsibleDefault);
        }
    }


    public static void fillInMutableBilling(final Context ctx, final Account account,
            final MutableAccountBilling billing) throws CRMExceptionFault
    {
        RmiApiSupport.setOptional(account, AccountXInfo.PROMISE_TO_PAY_DATE, billing.getPromiseToPayDate());
        if (billing.getCollectionAgentID() != null)
        {
            account.setDebtCollectionAgencyId(billing.getCollectionAgentID());
        }
        if (billing.getCreditCategory() != null)
        {
            RmiApiSupport.validateExistanceOfBeanForKey(account.getContext(), CreditCategory.class, new EQ(
                    CreditCategoryXInfo.CODE, billing.getCreditCategory().intValue()));
            RmiApiSupport.setOptional(account, AccountXInfo.CREDIT_CATEGORY, billing.getCreditCategory() );
        }
        if (billing.getDealerCode() != null && !billing.getDealerCode().equals(""))
        {
            RmiApiSupport.validateExistanceOfBeanForKey(account.getContext(),
                    com.redknee.app.crm.bean.DealerCode.class, new EQ(DealerCodeXInfo.CODE, billing.getDealerCode()));
            account.setDealerCode(billing.getDealerCode());
        }
         
        if (billing.getDiscountClass() != null)
        {	
        	if (billing.getDiscountClass().compareTo(AccountsApiSupport.REMOVE_DISCOUNT)==0) //Fix for BSS-1827 and from now CRM has to send following tag value in XML when they want to delete DISCOUNTCLASS for an account:
        		//<discountClass xmlns = "http://soap.crmapi.util.redknee.com/accounts/xsd/2011/01">-99</discountClass>
            {
                RmiApiSupport.setOptional(account, AccountXInfo.DISCOUNT_CLASS, Account.DEFAULT_DISCOUNTCLASS);
            }
        	else
        	{
        		if(!billing.getDiscountClass().equals(new Long(Account.DEFAULT_DISCOUNTCLASS)))
	            {
	                RmiApiSupport.validateExistanceOfBeanForKey(account.getContext(),
	                    com.redknee.app.crm.bean.DiscountClass.class, new EQ(
	                            com.redknee.app.crm.bean.DiscountClassXInfo.ID, billing.getDiscountClass().intValue()));
	            }
	            RmiApiSupport.setOptional(account, AccountXInfo.DISCOUNT_CLASS, billing.getDiscountClass());
	        }
        }
        
        if (billing.getTaxAuthority() != null)
        {
            RmiApiSupport.validateExistanceOfBeanForKey(account.getContext(),
                    com.redknee.app.crm.bean.TaxAuthority.class, new EQ(
                            com.redknee.app.crm.bean.TaxAuthorityXInfo.TAX_ID, billing.getTaxAuthority().intValue()));
            RmiApiSupport.setOptional(account, AccountXInfo.TAX_AUTHORITY, billing.getTaxAuthority());
        }
        
        RmiApiSupport.setOptional(account, AccountXInfo.TAX_EXEMPTION, billing.getTaxExemption());
        if (billing.getBillingMsgPreference() != null)
        {
            account.setBillingMsgPreference(RmiApiSupport.convertApiBillMsgPref2Crm(billing.getBillingMsgPreference()));
        }
        if (billing.getBillingMessage() != null)
        {
            account.setBillingMessage(billing.getBillingMessage());
        }
        
        if (billing.getContractID() != null)
        {
            RmiApiSupport.validateExistanceOfBeanForKey(account.getContext(), Contract.class, new EQ(ContractXInfo.ID,
                    billing.getContractID()));
            RmiApiSupport.setOptional(account, AccountXInfo.CONTRACT, billing.getContractID());
        }
        if (billing.getContractStart() != null)
        {
            account.setContractStartDate(CalendarSupportHelper.get().calendarToDate(billing.getContractStart()));
        }
        if (billing.getContractEnd() != null)
        {
            account.setContractEndDate(CalendarSupportHelper.get().calendarToDate(billing.getContractEnd()));
        }
        RmiApiSupport.setOptional(account, AccountXInfo.PAYMENT_PLAN, billing.getPaymentPlanID());/**TT#13092408012**/
        RmiApiSupport.setOptional(account, AccountXInfo.USE_IF_NO_SUB_CREDIT_INFO, billing.getUseIfNoSubCreditInfo());
        RmiApiSupport.setOptional(account, AccountXInfo.INVOICE_DELIVERY_OPTION, billing.getInvoiceDeliveryOptionID());
    }


    public static ReadOnlyAccountPaymentInfo extractPaymentInfo(final Context ctx, final Account account)
    {
        final ReadOnlyAccountPaymentInfo detail = new ReadOnlyAccountPaymentInfo();
        detail.setPaymentMethodTypeID(account.getPaymentMethodType());
        detail.setCreditCardNumber(account.getCreditCardNumber());
        Date date = null;
        try
        {
            final SimpleDateFormat formatter = getCreditCardExpiryDateFormater(ctx);
            date = formatter.parse(account.getExpiryDate());            
        }
        catch (final Exception e)
        {
            LogSupport.minor(ctx, AccountsApiSupport.class, "could not parse credit card expiry date " + "\""
                    + account.getExpiryDate() + "\"", e);
        }
        detail.setExpiryDate(CalendarSupportHelper.get(ctx).dateToCalendar(date));
        detail.setHolderName(account.getHolderName());
        detail.setDebitBankTransit(account.getDebitBankTransit());
        detail.setDebitAccountNumber(account.getDebitAccountNumber());
        detail.setPaymentDue(CalendarSupportHelper.get(ctx).dateToCalendar(account.getPaymentDueDate()));
        detail.setBankID(account.getPMethodBankID());
        detail.setMaximumDebitAmount(Long.valueOf(account.getMaxDebitAmount()));
        detail.setCreditCardTypeID(Long.valueOf(account.getPMethodCardTypeId())); 
        return detail;
    }


    public static void fillInPaymentInfo(final Context ctx, final Account account, final AccountPaymentInfo detail)
            throws CRMExceptionFault
    {
        fillInMutablePaymentInfo(ctx, account, detail);
    }


    public static void fillInMutablePaymentInfo(final Context ctx, final Account account,
            final MutableAccountPaymentInfo detail) throws CRMExceptionFault
    {
        if (detail.getPaymentMethodTypeID() != null)
        {
            RmiApiSupport.validateExistanceOfBeanForKey(account.getContext(), TransactionMethod.class, new EQ(
                    TransactionMethodXInfo.IDENTIFIER, detail.getPaymentMethodTypeID()));
            RmiApiSupport.setOptional(account, AccountXInfo.PAYMENT_METHOD_TYPE, detail.getPaymentMethodTypeID());
        }
        
        if (detail.getCreditCardNumber() != null)
        {
            account.setCreditCardNumber(detail.getCreditCardNumber());
        }
        if (detail.getExpiryDate() != null)
        {
            final SimpleDateFormat formatter = getCreditCardExpiryDateFormater(ctx);
            account.setExpiryDate(formatter.format(detail.getExpiryDate().getTime()));
        }
        if (detail.getHolderName() != null)
        {
            account.setHolderName(detail.getHolderName());
        }
        if (detail.getDebitBankTransit() != null)
        {
            account.setDebitBankTransit(detail.getDebitBankTransit());
        }
        if (detail.getDebitAccountNumber() != null)
        {
            account.setDebitAccountNumber(detail.getDebitAccountNumber());
        }
        
        if (detail.getCreditCardTypeID() != null)
        {
            account.setPMethodCardTypeId(detail.getCreditCardTypeID().intValue()); 
        }
        
        if (detail.getMaximumDebitAmount() != null)
        {
            account.setMaxDebitAmount(detail.getMaximumDebitAmount().longValue());
        }
        
        if (detail.getBankID() != null)
        {
            account.setPMethodBankID(detail.getBankID()); 
        }
        
    }


    public static ReadOnlyAccountIdentification extractIdentification(final Context ctx, final Account account)
    {
        final ReadOnlyAccountIdentification detail = new ReadOnlyAccountIdentification();
        Home h = account.identifications(ctx);
        if (null != h)
        {
            try
            {
                Collection<com.redknee.app.crm.bean.account.AccountIdentification> crmEntries = h.selectAll(ctx);
                Collection<IdentificationEntry> apiEntries = new ArrayList<IdentificationEntry>(crmEntries.size());
                for (com.redknee.app.crm.bean.account.AccountIdentification crmEntry : crmEntries)
                {
                    IdentificationEntry entry = new IdentificationEntry();
                    apiEntries.add(entry);
                    entry.setType(Long.valueOf(crmEntry.getIdType()));
                    entry.setValue(crmEntry.getIdNumber());
                    if (crmEntry.getExpiryDate() != null)
                    {
                        Calendar expiry = Calendar.getInstance();
                        expiry.setTime(crmEntry.getExpiryDate());
                        entry.setExpiry(expiry);
                    }
                }
                detail.setIdentification(apiEntries.toArray(new IdentificationEntry[]
                    {}));
            }
            catch (Exception e)
            {
                LogSupport.minor(ctx, AccountsApiSupport.class,
                        "Error looking up account identifications for account [" + account.getBAN() + "]", e);
            }
        }
        else
        {
            LogSupport.minor(ctx, AccountsApiSupport.class,
                    "System error: AccountIdentification Home not available in context.");
        }

        detail.setDateOfBirth(CalendarSupportHelper.get(ctx).dateToCalendar(account.getDateOfBirth()));
        detail.setOccupationID((long) account.getOccupation());
        
        h = account.securityQuestionsAnswers(ctx);
        if (null != h)
        {
            try
            {
                Collection<com.redknee.app.crm.bean.account.SecurityQuestionAnswer> crmEntries = h.selectAll(ctx);
                Collection<SecurityQuestion> apiEntries = new ArrayList<SecurityQuestion>(crmEntries.size());
                for (com.redknee.app.crm.bean.account.SecurityQuestionAnswer crmEntry : crmEntries)
                {
                    SecurityQuestion entry = new SecurityQuestion();
                    apiEntries.add(entry);
                    entry.setQuestion(crmEntry.getQuestion());
                    entry.setAnswer(crmEntry.getAnswer());
                }
                detail.setSecurityQuestion(apiEntries.toArray(new SecurityQuestion[]
                    {}));
            }
            catch (Exception e)
            {
                LogSupport.minor(ctx, AccountsApiSupport.class,
                        "Error looking up security questions and answers for account [" + account.getBAN() + "]", e);
            }
        }
        else
        {
            LogSupport.minor(ctx, AccountsApiSupport.class,
                    "System error: SecurityQuestionAnswer Home not available in context.");
        }
        return detail;
    }


    public static void fillInIdentification(final Context ctx, final Account account,
            final AccountIdentification identification) throws CRMExceptionFault
    {
        fillInMutableIdentification(ctx, account, identification);
    }


    public static void fillInMutableIdentification(final Context ctx, final Account account,
            final MutableAccountIdentification identification) throws CRMExceptionFault
    {
    	 /** Added for fix - TT#12080330044. Problem while removing identifications for an account. A boolean flag will be sent
         * in request as generic parameter (AllAccountIdentificationsRemoved) . If flag is true , remove all identifications. 
         */
        Boolean removeIdentifications = (Boolean) ctx.get(AccountsImpl.IS_REMOVE_IDENTIFICATIONS);

    	IdentificationEntry[] accountIdList = identification.getIdentification();
    	if (accountIdList != null && accountIdList.length > 0 || (removeIdentifications != null && removeIdentifications))
    	{
    		SpidIdentificationGroups spidIdGroups = null;
    		List<com.redknee.app.crm.bean.account.AccountIdentificationGroup> groupsList = new ArrayList<com.redknee.app.crm.bean.account.AccountIdentificationGroup>();
    		account.setIdentificationGroupList(groupsList);
    		try
    		{
    			spidIdGroups = SpidSupport.getSpidIdentificationGroups(ctx, account.getSpid());
    		}
    		catch (Exception e)
    		{
    			LogSupport.minor(ctx, AccountsApiSupport.class,
    					"Exception caught trying to find Spid Identification Groups info for SPID ["
    							+ account.getSpid() + "]", e);
    		}
    		if (null == spidIdGroups)
    		{
    			LogSupport.info(ctx, AccountsApiSupport.class,
    					"No SPID Identification Groups configuration defined for SPID [" + account.getSpid() + "]");
    		}
    		else
    		{
    			AccountSupport.createEmptyAccountIdentificationGroupsList(ctx, account, spidIdGroups);
    		}

    		/**
    		 * Added for fix - TT#12080330044 . Just a not null check .
    		 */
    		if(accountIdList != null)
    		{
    			for (IdentificationEntry ai : accountIdList)
    			{
    				if (ai!=null)
    				{
    					com.redknee.app.crm.bean.account.AccountIdentification newAi;
    					try
    					{
    						newAi = (com.redknee.app.crm.bean.account.AccountIdentification) XBeans.instantiate(
    								com.redknee.app.crm.bean.account.AccountIdentification.class, ctx);
    					}
    					catch (Exception e)
    					{
    						new MinorLogMsg(AccountsApiSupport.class,
    								"Error instantiating new account identification bean.  Using default constructor.", e)
    						.log(ctx);
    						newAi = new com.redknee.app.crm.bean.account.AccountIdentification();
    					}

    					if (ai.getType() == null)
    					{
    						RmiApiErrorHandlingSupport.simpleValidation("Identification.type", "Identification type cannot be empty");
    					}
    					else if (ai.getValue() == null)
    					{
    						RmiApiErrorHandlingSupport.simpleValidation("Identification.value", "Identification value cannot be null");
    					}
    					else
    					{
    						newAi.setIdType(Long.valueOf(ai.getType()).intValue());
    						newAi.setIdNumber(ai.getValue());
    						/*
    						 * TT11020215018: check for null expiry date as it is optional.
    						 */
    						if (ai.getExpiry() != null)
    						{
    							newAi.setExpiryDate(ai.getExpiry().getTime());
    						}

    						AccountIdentificationSupport.addAccountIdentification(ctx, newAi, account, spidIdGroups);

    					}
    				}
    			}
    		}
    	}
        RmiApiSupport.setOptional(account, AccountXInfo.DATE_OF_BIRTH, identification.getDateOfBirth());
        if (identification.getOccupationID() != null)
        {
            RmiApiSupport.validateExistanceOfBeanForKey(account.getContext(), Occupation.class, new EQ(
                    OccupationXInfo.ID, identification.getOccupationID().intValue()));
            RmiApiSupport.setOptional(account, AccountXInfo.OCCUPATION, identification.getOccupationID());
        }
        
        SecurityQuestion[] questionList = identification.getSecurityQuestion();
        if (questionList != null && questionList.length > 0 || (removeIdentifications != null && removeIdentifications))
        {
            // TT#11101834007: Always create a new security question list and set the account to it to trigger saving.
            List<SecurityQuestionAnswer> securityQuestionsList = new ArrayList<SecurityQuestionAnswer>();            
            /**
             * Added for fix - TT#12080330044 . Just a not null check before for loop .
             */
            if(questionList != null)
            {
                for (SecurityQuestion s : questionList)
                {
                    SecurityQuestionAnswer newQA;
                    try
                    {
                        newQA = (SecurityQuestionAnswer) XBeans.instantiate(SecurityQuestionAnswer.class, ctx);
                    }
                    catch (Exception e)
                    {
                        new MinorLogMsg(AccountsApiSupport.class,
                                "Error instantiating new security question/answer bean.  Using default constructor.", e)
                                .log(ctx);
                        newQA = new SecurityQuestionAnswer();
                    }
                    if (null != account.getBAN() && account.getBAN().length() > 0)
                    {
                        newQA.setBAN(account.getBAN());
                    }
                    newQA.setId(securityQuestionsList.size());
                    newQA.setQuestion(s.getQuestion());
                    newQA.setAnswer(s.getAnswer());
                    securityQuestionsList.add(newQA);
                }
            }
            account.setSecurityQuestionsAndAnswers(securityQuestionsList);
        }
    }


    public static ReadOnlyAccountCompanyContactInfo extractCompany(final Account account)
    {
        final ReadOnlyAccountCompanyContactInfo companyInfo = new ReadOnlyAccountCompanyContactInfo();
        companyInfo.setAddress1(account.getCompanyAddress1());
        companyInfo.setAddress2(account.getCompanyAddress2());
        companyInfo.setAddress3(account.getCompanyAddress3());
        companyInfo.setCity(account.getCompanyCity());
        companyInfo.setProvince(account.getCompanyProvince());
        companyInfo.setCountry(account.getCompanyCountry());
        companyInfo.setPostalCode(account.getCompanyPostalCode());
        companyInfo.setTradingName(account.getTradingName());
        companyInfo.setRegistrationNumber(account.getRegistrationNumber());
        companyInfo.setCompanyNumber(account.getCompanyTel());
        companyInfo.setCompanyFax(account.getCompanyFax());
        return companyInfo;
    }


    public static void fillInCompany(final Account account, final AccountCompanyContactInfo companyInfo) throws CRMExceptionFault
    {
        fillMutableInCompany(account, companyInfo);
    }


    public static void fillMutableInCompany(final Account account, final MutableAccountCompanyContactInfo companyInfo) throws CRMExceptionFault
    {
        if (companyInfo.getAddress1() != null)
        {
            account.setCompanyAddress1(companyInfo.getAddress1());
        }
        if (companyInfo.getAddress2() != null)
        {
            account.setCompanyAddress2(companyInfo.getAddress2());
        }
        if (companyInfo.getAddress3() != null)
        {
            account.setCompanyAddress3(companyInfo.getAddress3());
        }
        if (companyInfo.getCity() != null)
        {
            account.setCompanyCity(companyInfo.getCity());
        }
        if (companyInfo.getProvince() != null)
        {
            RmiApiSupport.validateExistanceOfBeanForKey(account.getContext(), Province.class, new EQ(
                    ProvinceXInfo.NAME, companyInfo.getProvince()));
            account.setCompanyProvince(companyInfo.getProvince());
        }
        if (companyInfo.getCountry() != null)
        {
            account.setCompanyCountry(companyInfo.getCountry());
        }
        if (companyInfo.getTradingName() != null)
        {
            account.setTradingName(companyInfo.getTradingName());
        }
        if (companyInfo.getRegistrationNumber() != null)
        {
            account.setRegistrationNumber(companyInfo.getRegistrationNumber());
        }
        if (companyInfo.getCompanyNumber() != null)
        {
            account.setCompanyTel(companyInfo.getCompanyNumber());
        }
        if (companyInfo.getCompanyFax() != null)
        {
            account.setCompanyFax(companyInfo.getCompanyFax());
        }
        if (companyInfo.getPostalCode() != null)
        {
            account.setCompanyPostalCode(companyInfo.getPostalCode());
        }
    }


    public static ReadOnlyAccountBankInfo extractBank(final Account account)
    {
        final ReadOnlyAccountBankInfo bankInfo = new ReadOnlyAccountBankInfo();
        bankInfo.setAddress1(account.getBankAddress1());
        bankInfo.setAddress2(account.getBankAddress2());
        bankInfo.setBankID(account.getBankID());
        bankInfo.setBankName(account.getBankName());
        bankInfo.setBankNumber(account.getBankPhone());
        bankInfo.setBankAccountNumber(account.getBankAccountNumber());
        bankInfo.setBankAccountName(account.getBankAccountName());
        return bankInfo;
    }


    public static void fillInBank(final Account account, final AccountBankInfo bankInfo)throws CRMExceptionFault
    {
        fillInMutableBank(account, bankInfo);
    }


    public static void fillInMutableBank(final Account account, final MutableAccountBankInfo bankInfo) throws CRMExceptionFault
    {
        if (bankInfo.getAddress1() != null)
        {
            account.setBankAddress1(bankInfo.getAddress1());
        }
        if (bankInfo.getAddress2() != null)
        {
            account.setBankAddress2(bankInfo.getAddress2());
        }
        
        if (bankInfo.getBankID() != null && !bankInfo.getBankID().trim().isEmpty())
        {
            RmiApiSupport.validateExistanceOfBeanForKey(account.getContext(), Bank.class, new EQ(
                    BankXInfo.BANK_ID, bankInfo.getBankID()));
            RmiApiSupport.setOptional(account, AccountXInfo.BANK_ID, bankInfo.getBankID());
        }
        
        if (bankInfo.getBankName() != null)
        {
            account.setBankName(bankInfo.getBankName());
        }
        if (bankInfo.getBankNumber() != null)
        {
            account.setBankPhone(bankInfo.getBankNumber());
        }
        if (bankInfo.getBankAccountNumber() != null)
        {
            account.setBankAccountNumber(bankInfo.getBankAccountNumber());
        }
        if (bankInfo.getBankAccountName() != null)
        {
            account.setBankAccountName(bankInfo.getBankAccountName());
        }
    }


    public static ReadOnlyAccountContactInfo extractContact(final Account account)
    {
        final ReadOnlyAccountContactInfo contactInfo = new ReadOnlyAccountContactInfo();
        contactInfo.setFirstName(account.getFirstName());
        contactInfo.setLastName(account.getLastName());
        contactInfo.setInitials(account.getInitials());
        contactInfo.setEmailAddress(account.getEmailID());
        contactInfo.setContactNumber(account.getContactTel());
        contactInfo.setContactName(account.getContactName());
        contactInfo.setContactFax(account.getContactFax());
        contactInfo.setEmployer(account.getEmployer());
        contactInfo.setEmployerAddress(account.getEmployerAddress());
        contactInfo.setAddress1(account.getBillingAddress1());
        contactInfo.setAddress2(account.getBillingAddress2());
        contactInfo.setAddress3(account.getBillingAddress3());
        contactInfo.setCity(account.getBillingCity());
        contactInfo.setProvince(account.getBillingProvince());
        contactInfo.setCountry(account.getBillingCountry());
        contactInfo.setPostalCode(account.getBillingPostalCode());
        return contactInfo;
    }


    public static void fillInContact(final Account account, final AccountContactInfo contactInfo)throws CRMExceptionFault
    {
        fillInMutableContact(account, contactInfo);
    }


	public static void fillInExtensions(Context ctx, final Account account,
	    BaseAccountExtension[] extensions) throws CRMExceptionFault
    {
        for (BaseAccountExtension extension : extensions)
        {
            fillInExtension(ctx, account, extension);
        }
    }


	public static void fillInExtension(Context ctx, final Account account,
	    BaseAccountExtension extension) throws CRMExceptionFault
    {
            boolean exists = false;
            if(LogSupport.isDebugEnabled(ctx))
            {
            	LogSupport.debug(ctx, AccountsApiSupport.class, "extension :"+ extension);
            }
            if (extension instanceof AccountExtension)
            {
                BaseAccountExtensionSequence_type0 poolWrapper = ((AccountExtension)extension).getBaseAccountExtensionSequence_type0();
                if (poolWrapper != null
                        && poolWrapper.getPool() != null)
                {
                	exists = true;
                	fillInPoolExtension(ctx, account, poolWrapper);
                }
            }
            else if(extension instanceof SubscriberLimitAccountExtension)
            {
            	BaseAccountExtensionSequence_type1 subLimitWrapper = ((SubscriberLimitAccountExtension)extension).getBaseAccountExtensionSequence_type1();
            	if(subLimitWrapper !=  null
            			&& subLimitWrapper.getSubscriberLimit() != null)
            	{
            		exists = true;
                	fillInSubscriberLimitExtension(ctx, account, subLimitWrapper);
            	}
            }
            else if(extension instanceof FriendsAndFamilyAccountExtension)
            {
            	BaseAccountExtensionSequence_type2 fnfWrapper = ((FriendsAndFamilyAccountExtension)extension).getBaseAccountExtensionSequence_type2();
            	if(fnfWrapper != null
            			&& fnfWrapper.getFriendsAndFamily() != null)
            	{
            		exists = true;
            		fillInFriendsAndFamilyExtension(ctx, account, fnfWrapper);
            	}
            }
            else if(extension instanceof GroupPricePlanAccountExtension)
            {
            	BaseAccountExtensionSequence_type3 groupPPWrapper = ((GroupPricePlanAccountExtension)extension).getBaseAccountExtensionSequence_type3();
            	if(groupPPWrapper != null
            			&& groupPPWrapper.getGroupPricePlan() != null)
            	{
            		exists = true;
            		fillInGroupPricePlanExtension(ctx, account, groupPPWrapper);
            	}
            }
            else
            {
                throw new IllegalPropertyArgumentException(AccountXInfo.ACCOUNT_EXTENSIONS,
                "Account extension [" + extension + "] not supported.");
            }
            
            if(exists==false)
            {
                throw new IllegalPropertyArgumentException(AccountXInfo.ACCOUNT_EXTENSIONS,
                "Account extension [" + extension + "] not supported.");
            }   
    }

	public static void fillInPoolExtension(Context ctx, final Account account,
			BaseAccountExtensionSequence_type0 wrapper) throws CRMExceptionFault{
       	
		PoolAccountExtension apiPoolExtension = wrapper.getPool();
        PoolExtension crmPoolExtension;
        try
        {
            crmPoolExtension = (PoolExtension) XBeans.instantiate(PoolExtension.class, ctx);
        }
        catch (Exception e)
        {
            new MinorLogMsg(AccountsApiSupport.class,
                    "Error instantiating new pool extension.  Using default constructor.", e).log(ctx);
            crmPoolExtension = new PoolExtension();
        }
        
        final Long[] apiBudleIDs = apiPoolExtension.getBundleIDs();
        
        // Even though initializing the pool bundles to a new object will make it eligible for garbage collection
        // but it is better approach rather that putting it in else block.
        Map<Long, BundleFee> poolBundles = new HashMap<Long, BundleFee>();
        if(null != apiBudleIDs && apiBudleIDs.length > 0)
        {
            final CompoundIllegalStateException bundleTransformException = new CompoundIllegalStateException(); 
            poolBundles = PoolExtension.transformBundles(ctx, bundleTransformException,apiBudleIDs);
            if(bundleTransformException.getSize() >0 )
            {
                bundleTransformException.throwAll();
            }
        }
      
        crmPoolExtension.setPoolBundles(poolBundles);
        crmPoolExtension.setPoolMSISDN(apiPoolExtension.getGroupMobileNumber());
        crmPoolExtension.setSpid(account.getSpid());
        crmPoolExtension.setBAN(account.getBAN());

        if (apiPoolExtension.getPoolLimit() != null)
        {
            crmPoolExtension.setQuotaLimit(apiPoolExtension.getPoolLimit().intValue());
        }
        crmPoolExtension.setQuotaType(RmiApiSupport.convertApiPoolLimitStrategy2Crm(apiPoolExtension.getPoolLimitStrategy()));
        final Map<Long, SubscriptionPoolProperty> crmPoolProperties = new HashMap<Long, SubscriptionPoolProperty>();
        crmPoolExtension.setSubscriptionPoolProperties(crmPoolProperties);
        if (apiPoolExtension.getSubscriptionPoolProperty() != null)
        {
            for (com.redknee.util.crmapi.wsdl.v2_2.types.account.extensions.SubscriptionPoolProperty apiPoolProperty : apiPoolExtension
                    .getSubscriptionPoolProperty())
            {
                if (apiPoolProperty == null)
                {
                    continue;
                }
                
                RmiApiErrorHandlingSupport.validateMandatoryObject(apiPoolProperty.getSubscriptionType(), "SubscriptionPoolProperty.subscriptionType");
                RmiApiErrorHandlingSupport.validateMandatoryObject(apiPoolProperty.getInitialPoolBalance(), "SubscriptionPoolProperty.initialPoolBalance");
                
                SubscriptionPoolProperty crmProperty;
                try
                {
                    crmProperty = (SubscriptionPoolProperty) XBeans.instantiate(SubscriptionPoolProperty.class, ctx);
                }
                catch (Exception e)
                {
                    new MinorLogMsg(AccountsApiSupport.class,
                            "Error instantiating new pool property.  Using default constructor.", e).log(ctx);
                    crmProperty = new SubscriptionPoolProperty();
                }
                
                crmProperty.setInitialPoolBalance(apiPoolProperty.getInitialPoolBalance());
                crmProperty.setSubscriptionType(apiPoolProperty.getSubscriptionType());
                
                if (crmPoolProperties.containsKey(crmProperty.getSubscriptionType()))
                {
                    throw new IllegalPropertyArgumentException(PoolExtensionXInfo.SUBSCRIPTION_POOL_PROPERTIES,
                            "Duplicate Subscroption Pool properties for Subscription Type: "
                            + crmProperty.getSubscriptionType());
                }
                crmPoolProperties.put(crmProperty.getSubscriptionType(), crmProperty);
            }
        }
        ExtensionHolder holder;
        try
        {
            holder = (AccountExtensionHolder) XBeans.instantiate(AccountExtensionHolder.class, ctx);
        }
        catch (Exception e)
        {
            new MinorLogMsg(AccountsApiSupport.class,
                    "Error instantiating new account extension holder.  Using default constructor.", e).log(ctx);
            holder = new AccountExtensionHolder();
        }
        holder.setExtension(crmPoolExtension);
        List<ExtensionHolder> accountExtensions = new ArrayList<ExtensionHolder>(account.getAccountExtensions());
        accountExtensions.add(holder);
        account.setAccountExtensions(accountExtensions);

	}

	public static void fillInGroupPricePlanExtension(Context ctx, final Account account,
			BaseAccountExtensionSequence_type3 wrapper) throws CRMExceptionFault{
		MutableGroupPricePlanAccountExtension apiGroupPricePlanExtension = wrapper.getGroupPricePlan();
		GroupPricePlanExtension crmGroupPricePlanExtension;
		try
        {
			crmGroupPricePlanExtension=(GroupPricePlanExtension) XBeans.instantiate(GroupPricePlanExtension.class, ctx);
        }
        catch (Exception e)
        {
            new MinorLogMsg(AccountsApiSupport.class,
                    "Error instantiating new group price plan extension.  Using default constructor.", e).log(ctx);
            crmGroupPricePlanExtension = new GroupPricePlanExtension();
        }
		crmGroupPricePlanExtension.setSpid(account.getSpid());
		ExtensionApiSupport.apiToCrmGroupPricePlanExtensionMapping(ctx, crmGroupPricePlanExtension, apiGroupPricePlanExtension, account);
		ExtensionHolder holder;
        try
        {
            holder = (AccountExtensionHolder) XBeans.instantiate(AccountExtensionHolder.class, ctx);
        }
        catch (Exception e)
        {
            new MinorLogMsg(AccountsApiSupport.class,
                    "Error instantiating new account extension holder.  Using default constructor.", e).log(ctx);
            holder = new AccountExtensionHolder();
        }
        holder.setExtension(crmGroupPricePlanExtension);
        List<ExtensionHolder> accountExtensions = new ArrayList<ExtensionHolder>(account.getAccountExtensions());
        accountExtensions.add(holder);
        account.setAccountExtensions(accountExtensions);
	}
	
	
	public static void fillInSubscriberLimitExtension(Context ctx, final Account account,
			BaseAccountExtensionSequence_type1 wrapper) throws CRMExceptionFault
	{
		MutableSubscriberLimitAccountExtension apiSubscriberLimitExtension = wrapper.getSubscriberLimit();
		SubscriberLimitExtension crmSubscriberLimitExtension;
		
		try
		{
			crmSubscriberLimitExtension = XBeans.instantiate(SubscriberLimitExtension.class, ctx);
		}
		catch (Exception e)
        {
            new MinorLogMsg(AccountsApiSupport.class,
                    "Error instantiating new group price plan extension.  Using default constructor.", e).log(ctx);
            crmSubscriberLimitExtension = new SubscriberLimitExtension();
        }
		
		crmSubscriberLimitExtension.setBAN(account.getBAN());
		crmSubscriberLimitExtension.setSpid(account.getSpid());
		
		ExtensionApiSupport.apiToCrmSubscriberLimitExtensionMapping(ctx, crmSubscriberLimitExtension,
				apiSubscriberLimitExtension, account);
		
		ExtensionHolder holder;
        try
        {
            holder = (AccountExtensionHolder) XBeans.instantiate(AccountExtensionHolder.class, ctx);
        }
        catch (Exception e)
        {
            new MinorLogMsg(AccountsApiSupport.class,
                    "Error instantiating new account extension holder.  Using default constructor.", e).log(ctx);
            holder = new AccountExtensionHolder();
        }
        holder.setExtension(crmSubscriberLimitExtension);
        List<ExtensionHolder> accountExtensions = new ArrayList<ExtensionHolder>(account.getAccountExtensions());
        accountExtensions.add(holder);
        account.setAccountExtensions(accountExtensions);
	}
	
	public static void fillInFriendsAndFamilyExtension(Context ctx, final Account account,
			BaseAccountExtensionSequence_type2 wrapper) throws CRMExceptionFault
	{
		MutableFriendsAndFamilyAccountExtension apiFnFExtension = wrapper.getFriendsAndFamily();
		FriendsAndFamilyExtension crmFnFExtension;
		
		RmiApiErrorHandlingSupport.validateMandatoryObject(apiFnFExtension.getCugTemplateID(), 
    			"FriendsAndFamilyExtension.cugTemplateID");
    	//RmiApiErrorHandlingSupport.validateMandatoryObject(apiFnFExtension.getCugOwnerMSISDN(), 
    			//"FriendsAndFamilyExtension.cugOwnerMSISDN");
    	//RmiApiErrorHandlingSupport.validateMandatoryObject(apiFnFExtension.getSmsNotificationMSISDN(), 
    			//"FriendsAndFamilyExtension.smsNotificationMSISDN");
		
		try
		{
			crmFnFExtension = XBeans.instantiate(FriendsAndFamilyExtension.class, ctx);
		}
		catch (Exception e)
        {
            new MinorLogMsg(AccountsApiSupport.class,
                    "Error instantiating new Friends And Family Extension.  Using default constructor.", e).log(ctx);
            crmFnFExtension = new FriendsAndFamilyExtension();
        }
		
		crmFnFExtension.setBAN(account.getBAN());
		crmFnFExtension.setSpid(account.getSpid());
		
		ExtensionApiSupport.apiToCrmFriendsAndFamilyExtensionMapping(ctx, crmFnFExtension, apiFnFExtension, account);
		
		ExtensionHolder holder;
        try
        {
            holder = (AccountExtensionHolder) XBeans.instantiate(AccountExtensionHolder.class, ctx);
        }
        catch (Exception e)
        {
            new MinorLogMsg(AccountsApiSupport.class,
                    "Error instantiating new account extension holder.  Using default constructor.", e).log(ctx);
            holder = new AccountExtensionHolder();
        }
        holder.setExtension(crmFnFExtension);
        List<ExtensionHolder> accountExtensions = new ArrayList<ExtensionHolder>(account.getAccountExtensions());
        accountExtensions.add(holder);
        account.setAccountExtensions(accountExtensions);
	}

	
    public static void fillInMutableContact(final Account account, final MutableAccountContactInfo contactInfo) throws CRMExceptionFault
    {
        if (contactInfo.getFirstName() != null)
        {
            account.setFirstName(contactInfo.getFirstName());
        }
        if (contactInfo.getLastName() != null)
        {
            account.setLastName(contactInfo.getLastName());
        }
        if (contactInfo.getInitials() != null)
        {
            account.setInitials(contactInfo.getInitials());
        }
        if (contactInfo.getEmailAddress() != null)
        {
            account.setEmailID(contactInfo.getEmailAddress());
        }
        if (contactInfo.getContactNumber() != null)
        {
            account.setContactTel(contactInfo.getContactNumber());
        }
        if (contactInfo.getContactName() != null)
        {
            account.setContactName(contactInfo.getContactName());
        }
        if (contactInfo.getContactFax() != null)
        {
            account.setContactFax(contactInfo.getContactFax());
        }
        if (contactInfo.getEmployer() != null)
        {
            account.setEmployer(contactInfo.getEmployer());
        }
        if (contactInfo.getEmployerAddress() != null)
        {
            account.setEmployerAddress(contactInfo.getEmployerAddress());
        }
        if (contactInfo.getAddress1() != null)
        {
            account.setBillingAddress1(contactInfo.getAddress1());
        }
        if (contactInfo.getAddress2() != null)
        {
            account.setBillingAddress2(contactInfo.getAddress2());
        }
        if (contactInfo.getAddress3() != null)
        {
            account.setBillingAddress3(contactInfo.getAddress3());
        }
        if (contactInfo.getCity() != null)
        {
            account.setBillingCity(contactInfo.getCity());
        }
        if (contactInfo.getProvince() != null)
        {
            RmiApiSupport.validateExistanceOfBeanForKey(account.getContext(), Province.class, new EQ(
                    ProvinceXInfo.NAME, contactInfo.getProvince()));
            account.setBillingProvince(contactInfo.getProvince());
        }
        if (contactInfo.getCountry() != null)
        {
            account.setBillingCountry(contactInfo.getCountry());
        }
        if (contactInfo.getPostalCode() != null) 
        {
            account.setBillingPostalCode(contactInfo.getPostalCode());
        }
    }

    public static void fillInAccountDiscountsHolder(final Account account, final GenericParameterParser parser) throws CRMExceptionFault {
    	
    	Set <Integer>discountClassSet = new HashSet<Integer>();

        String discountClasslist = parser.getParameter(AccountsImpl.DISCOUNT_CLASS_INFO, String.class);
    	StringTokenizer st = new StringTokenizer(discountClasslist,",");
    	
		while (st.hasMoreElements()) {
			try{
				Integer discountclassId = Integer.valueOf(st.nextToken());
				
				discountClassSet.add(discountclassId);
				
	            RmiApiSupport.validateExistanceOfBeanForKey(account.getContext(),
	                    com.redknee.app.crm.bean.DiscountClass.class, new EQ(
	                            com.redknee.app.crm.bean.DiscountClassXInfo.ID, discountclassId.intValue()));
			
			}catch(CRMExceptionFault e){
				throw e;
			}
			catch(Exception e){
				throw new CRMExceptionFault("Expected type for generic parameter discountClass is Integer");
			}
		}
    	account.setDiscountsClassHolder(discountClassSet);
    }
    
    public static Account constructAccount(final Context ctx, final CRMRequestHeader header,
            final AccountProfile profile, final AccountDetail detail, final AccountBilling billing,
            final AccountPaymentInfo paymentInfo, final AccountIdentification identification,
            final AccountCompanyContactInfo company, final AccountBankInfo bank, final AccountContactInfo contact,
            BaseAccountExtension[] extensions) throws CRMExceptionFault
    {
        Account account;
        try
        {
            account = (Account) XBeans.instantiate(Account.class, ctx);
        }
        catch (Exception e)
        {
            new MinorLogMsg(AccountsApiSupport.class, "Error instantiating new account.  Using default constructor.", e)
                    .log(ctx);
            account = new Account();
        }
        account.setContext(ctx);
        if (profile != null)
        {
            fillInProfile(account, profile);
        }
        if (detail != null)
        {
            try
            {
                if (HomeSupportHelper.get().findBean(ctx, AccountRole.class, detail.getRole()) == null)
                {
                    String msg = "Account Role " + detail.getRole();
                    RmiApiErrorHandlingSupport.identificationException(ctx, msg, AccountsApiSupport.class.getName());
                }
            }
            catch (HomeException e)
            {
                String msg = "Account Role " + detail.getRole();
                RmiApiErrorHandlingSupport.identificationException(ctx, msg, AccountsApiSupport.class.getName());
            }
            fillInDetail(account, detail);
        }
        if (billing != null)
        {
            fillInBilling(ctx, account, billing);
        }
        if (paymentInfo != null)
        {
            fillInPaymentInfo(ctx, account, paymentInfo);
        }
        if (identification != null)
        {
            fillInIdentification(ctx, account, identification);
        }
        if (company != null)
        {
            fillInCompany(account, company);
        }
        if (bank != null)
        {
            fillInBank(account, bank);
        }
        if (contact != null)
        {
            fillInContact(account, contact);
        }
        if (extensions != null && extensions.length > 0)
        {
            fillInExtensions(ctx, account, extensions);
        }
        return account;
    }


    public static void updateAccount(final Context ctx, final Account account, final CRMRequestHeader header,
            final MutableAccountProfile profile, final MutableAccountDetail detail,
            final MutableAccountBilling billing, final MutableAccountPaymentInfo paymentInfo,
            final MutableAccountIdentification identification, final MutableAccountCompanyContactInfo company,
            final MutableAccountBankInfo bank, final MutableAccountContactInfo contact) throws CRMExceptionFault
    {
        if (profile != null)
        {
            fillInMutableProfile(account, profile);
        }
        if (detail != null)
        {
            fillInMutableDetail(account, detail);
        }
        if (billing != null)
        {
            fillInMutableBilling(ctx, account, billing);
        }
        if (paymentInfo != null)
        {
            fillInMutablePaymentInfo(ctx, account, paymentInfo);
        }
        if (identification != null)
        {
            fillInMutableIdentification(ctx, account, identification);
        }
        if (company != null)
        {
            fillMutableInCompany(account, company);
        }
        if (bank != null)
        {
            fillInMutableBank(account, bank);
        }
        if (contact != null)
        {
            fillInMutableContact(account, contact);
        }
    }

    public static boolean updateAccountType(final Context ctx , final Account account , final Long accountType)
    {
    	if(accountType == null)
    	{
    		return false;
    	}
    	account.setType(accountType);
    	return true;
    }

    public static String getCreditCardExpiryDateFormat(final Context ctx)
    {
        final MessageMgrSPI mmgr = (MessageMgrSPI) ctx.get(MessageMgrSPI.class);
        final Class lookupClass = Account.class;
        String format = mmgr.get(ctx, DateWebControl.MSG_MGR_KEY, lookupClass, (Lang) ctx.get(Lang.class), null, null);
        if (format == null || format.length() == 0)
        {
            format = AbstractAccount.DEFAULT_CARD_EXPIRY_DATE_FORMAT;
        }
        return format;
    }


    public static SimpleDateFormat getCreditCardExpiryDateFormater(final Context ctx)
    {
        final String formatStr = getCreditCardExpiryDateFormat(ctx);
        ThreadLocalSimpleDateFormat format;
        format = (threadLocalFormatMap.get()).get(formatStr);
        if (format == null)
        {
            format = new ThreadLocalSimpleDateFormat(formatStr);
            (threadLocalFormatMap.get()).put(formatStr, format);
        }
        return (SimpleDateFormat) format.get();
    }

    protected static ThreadLocal<Map<String, ThreadLocalSimpleDateFormat>> threadLocalFormatMap = new ThreadLocal<Map<String, ThreadLocalSimpleDateFormat>>()
    {

        @Override
        public Map<String, ThreadLocalSimpleDateFormat> initialValue()
        {
            return new HashMap<String, ThreadLocalSimpleDateFormat>();
        }
    };
    
    public static void fillInIdentificationForAccountConvert(Context ctx, ConvertAccountBillingTypeRequest convertAcctBillTypeReq , AccountIdentification identification) throws CRMExceptionFault
    {
 	   List <AccountIdentificationGroup> aig =  convertAcctBillTypeReq.getIdentificationGroupList(); //removing dummy object.
 	   
 	   IdentificationEntry[] accountIdList = identification.getIdentification();
 	   SpidIdentificationGroups spidIdGroups = null;
 	   if (accountIdList != null && accountIdList.length > 0 )
 	   {
 		   try
 		   {
 			   spidIdGroups = SpidSupport.getSpidIdentificationGroups(ctx, convertAcctBillTypeReq.getSpid());
 		   }
 		   catch (Exception e)
 		   {
 			   LogSupport.minor(ctx, AccountsApiSupport.class,
 					   "Exception caught trying to find Spid Identification Groups info for SPID ["
 							   +  "]", e);
 		   }
 		   if (null == spidIdGroups)
 		   {
 			   LogSupport.info(ctx, AccountsApiSupport.class,
 					   "No SPID Identification Groups configuration defined for SPID [" + "]");
 		   }
 	   }
 	   if(accountIdList != null)
 	   {
 			for (IdentificationEntry ai : accountIdList)
 			{
 				if (ai!=null)
 				{
 					com.redknee.app.crm.bean.account.AccountIdentification newAi;
 					try
 					{
 						newAi = (com.redknee.app.crm.bean.account.AccountIdentification) XBeans.instantiate(
 								com.redknee.app.crm.bean.account.AccountIdentification.class, ctx);
 					}
 					catch (Exception e)
 					{
 						new MinorLogMsg(AccountsApiSupport.class,
 								"Error instantiating new account identification bean.  Using default constructor.", e)
 						.log(ctx);
 						newAi = new com.redknee.app.crm.bean.account.AccountIdentification();
 					}

 					if (ai.getType() == null)
 					{
 						RmiApiErrorHandlingSupport.simpleValidation("Identification.type", "Identification type cannot be empty");
 					}
 					else if (ai.getValue() == null)
 					{
 						RmiApiErrorHandlingSupport.simpleValidation("Identification.value", "Identification value cannot be null");
 					}
 					else
 					{
 						newAi.setIdType(Long.valueOf(ai.getType()).intValue());
 						newAi.setIdNumber(ai.getValue());

 						if (ai.getExpiry() != null)
 						{
 							newAi.setExpiryDate(ai.getExpiry().getTime());
 						}

 						AccountIdentificationSupport.addAccountIdentification(ctx, newAi, convertAcctBillTypeReq, spidIdGroups , aig);

 					}
 				}
 			}
 		}
 	
 	   
 	   SecurityQuestion[] questionList = identification.getSecurityQuestion();
        if (questionList != null && questionList.length > 0)
        {

            List<SecurityQuestionAnswer> securityQuestionsList = new ArrayList<SecurityQuestionAnswer>();            
  
            if(questionList != null)
            {
                for (SecurityQuestion s : questionList)
                {
                    SecurityQuestionAnswer newQA;
                    try
                    {
                        newQA = (SecurityQuestionAnswer) XBeans.instantiate(SecurityQuestionAnswer.class, ctx);
                    }
                    catch (Exception e)
                    {
                        new MinorLogMsg(AccountsApiSupport.class,
                                "Error instantiating new security question/answer bean.  Using default constructor.", e)
                                .log(ctx);
                        newQA = new SecurityQuestionAnswer();
                    }
                   
                    newQA.setId(securityQuestionsList.size());
                    newQA.setQuestion(s.getQuestion());
                    newQA.setAnswer(s.getAnswer());
                    securityQuestionsList.add(newQA);
                }
            }
            convertAcctBillTypeReq.setSecurityQuestionsAndAnswers(securityQuestionsList);
        }
    }
    
    
	
    public static Map<String, String> updateGUIDMap(Context ctx, Account returnedAccount,
 	
		Map<String, String> guidIDMap) {
 	
		  List<SecurityQuestionAnswer> securityQuestionsList = returnedAccount.getSecurityQuestionsAndAnswers();
 	
		  for(SecurityQuestionAnswer secQue : securityQuestionsList){
 	
			 String guid = guidIDMap.get(secQue.getQuestion());
 	
			  guidIDMap.remove(secQue.getQuestion());
 	
			  guidIDMap.put(guid,String.valueOf(secQue.getId()));
 	
		  }
 	
      return guidIDMap;
 	
	}
    
    public static BlacklistStatus getBlacklistStatus(
    		Context ctx, CRMRequestHeader cRMRequestHeader, IdentificationEntry identification,
    		GenericParameter[] genericParameter) throws CRMExceptionFault{
    	BlacklistStatus ret = new BlacklistStatus();
    	try
    	{
    		try
    		{
    			if(identification.getValue() != null && !identification.getValue().trim().isEmpty()){
    				ret.setIdentificationNumber(identification.getValue());
    				ret.setIdentificationTypeID(identification.getType());
    				BlackList list = BlackListSupport.getIdList(ctx, identification.getType().intValue(), identification.getValue());
    				BlacklistType blacklistType = BlacklistType.Factory.fromValue(-1);
    				if (list != null)
    				{
    					ret.setIdentifier(list.getBlackListID());
    					if (list.getBlackType() != null)
    					{
    						blacklistType = BlacklistType.Factory.fromValue(list.getBlackType().getIndex());
    					}
    					ret.setNote(list.getNote());
    				}
    				ret.setListType(blacklistType);
    			}else{
    				final String msg = "Identification value is null,please fill ID Value.";
    				new MinorLogMsg(MODULE, msg).log(ctx);
    				RmiApiErrorHandlingSupport.generalException(ctx, null, msg, MODULE);
    			}
    		}catch (final Exception e){
    			final String msg = "Unable to retrieve blacklist status";
    			new MinorLogMsg(MODULE, msg, e).log(ctx);
    			RmiApiErrorHandlingSupport.generalException(ctx, e, msg, MODULE);
    		}
    	}catch (com.redknee.util.crmapi.wsdl.v3_0.api.CRMExceptionFault v30Exception){
    		throw v30Exception;
    	}
    	return ret;
      }
    	
      public static boolean getExternalReevaluationRequired(Context ctx,final int spid, final String ban) {
    	 boolean status = false;
    	 try {
    		 final XDB xdb = (XDB) ctx.get(XDB.class);
    		 final String tableName = MultiDbSupportHelper.get(ctx).getTableName(ctx, ExternalCreditCheckHome.class,
    				 ExternalCreditCheckXInfo.DEFAULT_TABLE_NAME);
    		 
    		 final XStatement sql = new SimpleXStatement("select  max(creditCheckDate) from " + tableName + " where (BAN = ? and  SPID = ? )")
             {
                 @Override
                 public void set(final Context ctx, final XPreparedStatement ps) throws SQLException
                 {
                     ps.setString(ban);
                     ps.setInt(spid);
                 }
             };
             Date creditCheckDate = null;
             try{
                 if (xdb != null){
                	 creditCheckDate = SingleValueXDBVisitor.getDate(ctx, xdb, sql);
                 }else{
                     LogSupport.major(ctx, MODULE, "Xdb not found in context ");
               }
             }catch (final HomeException exception){
             	LogSupport.major(ctx, MODULE,"Look-up of the credit Check Date failed for ban: " +ban+" SPID: "+spid);
             }
             if(creditCheckDate!=null){
            	 CreditCategory creditCategory = getCreditCatgory(ctx,spid,ban);
            	 if(creditCategory!=null){
            		 int days = creditCategory.getExternalCreditCheckValidity();
            		Date sysDate = CalendarSupportHelper.get(ctx).getRunningDate(ctx); 
            		Date calulatedDate = addDays(creditCheckDate, days);
            		if(calulatedDate.before(sysDate)){
            			status = true;
            			LogSupport.info(ctx,MODULE, "calculated date "+calulatedDate+" is before system date "+sysDate);
            		}else{
            			status = false;
            			LogSupport.info(ctx,MODULE, "calculated date "+calulatedDate+" after system date "+sysDate);
            		}
            	 }
    		 }else{
    			 status = true;
    			 LogSupport.minor(ctx,MODULE, "CreditCheck date is null in ExternalCreditCheck");
    		 }
             }catch (Exception e) {
    		   LogSupport.minor(ctx, MODULE, "Can not find external credit check configuration for BAN:"+ban +" SPID:"+spid);
    	   }
    	 return status;
    	}


    	public static Date addDays(Date date, int days) {
    		GregorianCalendar cal = new GregorianCalendar();
    		cal.setTime(date);
    		cal.add(Calendar.DATE, days);
    		return cal.getTime();
    	}
    	


    	private static CreditCategory getCreditCatgory(Context ctx, int spid, String ban) {
    		CreditCategory creditCategory = null;
    			try {
    				Account account = AccountSupport.getAccount(ctx, ban);
    				if(account!=null){
    					And filter = new And();
    					filter.add(new EQ(CreditCategoryXInfo.SPID,spid));
    					filter.add(new EQ(CreditCategoryXInfo.CODE,account.getCreditCategory()));
    					creditCategory = (CreditCategory) HomeSupportHelper.get(ctx).findBean(ctx, CreditCategory.class, filter);
    				}else{
    					LogSupport.minor(ctx, MODULE, "Cannot find account for BAN:" +ban);
    				}
    			} catch (Exception e) {
    				LogSupport.minor(ctx, MODULE, "Cannot find credit catgory " + e);
    			}
    			return creditCategory;
         }

    	public static SubscriptionCount getSubscriptionCount(Context ctx, String ban) {
    		SubscriptionCount subscriptionCount = new SubscriptionCount();
    		Account account = null;
    		try {
    			account = AccountSupport.getAccount(ctx, ban);
    		}catch (Exception e) {
    			LogSupport.minor(ctx, MODULE, "Cannot find Account " + ban);
    		}
    		long resCount = 0, nonResCount = 0;
    		if(account!=null){
    			GroupTypeEnum groupType_ = account.getGroupType();
    			if(groupType_.equals(GroupTypeEnum.GROUP)){
    				List<Account> accountList = AccountSupport.getParentAccounts(ctx,ban);
    				for(Account singleAccount : accountList){
    					if(singleAccount.isResponsible()){
    						resCount = resCount + AccountSupport.getSubscriptionCount(ctx,singleAccount.getBAN());
    					}else{
    						nonResCount = nonResCount + AccountSupport.getSubscriptionCount(ctx,singleAccount.getBAN());
    					}
    				}
    			}else{
    				long count = AccountSupport.getSubscriptionCount(ctx,ban);
    				resCount = resCount + count;
    			}
    			LogSupport.info(ctx, MODULE, "Subscription Count for account " +ban+" responsible count: "+resCount+" non-responsible count:"+nonResCount);
    		}else{
    			LogSupport.minor(ctx, MODULE, "Cannot find account:" +ban);
    		}
    		subscriptionCount.setResponsibleCount((int) (long) resCount);
    		subscriptionCount.setNonResponsibleCount((int) (long) nonResCount);
    		return subscriptionCount;
    	}
    	
        public static void validateDuplicateAccountCheckGrp(final Context ctx,final String firstname, final String lastname,
                final Calendar dateofbirth, IdentificationEntry[] ids) throws CRMExceptionFault
        
            {
        	LogSupport.info(ctx,AccountsApiSupport.class, "IN validateDuplicateAccountCheckGrp.."+firstname+".."+lastname+".."+dateofbirth+".."+ids);
        	LogSupport.info(ctx,AccountsApiSupport.class, "IN validateDuplicateAccountCheckGrp.."+firstname.length()+".."+lastname.length()+".."+dateofbirth+".."+ids.length);
                if (firstname == null || "".equals(firstname))
                {
                	logAndThrowException(ctx, "First Name is a mandatory parameter and cannot be NULL to check duplicate accounts."); 
                }
                else if (lastname == null || "".equals(lastname))
                {
                	logAndThrowException(ctx, "Last Name is a mandatory parameter and cannot be NULL to check duplicate accounts.");
                }
                else if (dateofbirth == null)
                {
                	logAndThrowException(ctx, "Date of birth is a mandatory parameter and cannot be NULL to check duplicate accounts.");
                }
                
                for (IdentificationEntry id : ids)
    	        {
                	
                	if(id.getType()==-1){
                		
                		logAndThrowException(ctx, "Identification Type is a mandatory parameter and cannot be NULL to check duplicate accounts.");
                	}
                	
                	if(id.getValue() != null && (id.getValue().toString() == null || "".equals(id.getValue().toString()))){
                		logAndThrowException(ctx, "Identification Value is a mandatory parameter and cannot be NULL to check duplicate accounts.");
                	}
    	        	
    	         }
    			 
    	}
        
    	// Retrive duplicates considering Group1
    	 public static Collection<Contact> getDuplicateAccount(final Context ctx,String firstname, String lastname, Calendar dateofbirth) {
    		// TODO Auto-generated method stub
       	try
       	{
       		And filter = new And();
    	    	filter.add(new EQIC(ContactXInfo.FIRST_NAME, firstname));
    	    	filter.add(new EQIC(ContactXInfo.LAST_NAME, lastname));
    	    	filter.add(new EQ(ContactXInfo.DATE_OF_BIRTH, dateofbirth.getTime()));
    	      	return HomeSupportHelper.get(ctx).getBeans(ctx, Contact.class, filter);
       	}
       	catch (HomeException e)
       	{
       		try {
    				RmiApiErrorHandlingSupport.generalException(ctx, e, "Unable to retrieve account identification", AccountsApiSupport.class);
    			} catch (CRMExceptionFault e1) {
    				// TODO Auto-generated catch block
    				e1.printStackTrace();
    			}
       	}
       	
       	return null;
    	}
    	 
    		// Retrive duplicates considering Group2
    		
    		public static Collection<com.redknee.app.crm.bean.account.AccountIdentification> getDuplicateIdentity(Context ctx, IdentificationEntry id) throws CRMExceptionFault
    	    {
    	    	try
    	    	{
    	    		LogSupport.info(ctx,AccountsApiSupport.class, "In getDuplicateIdentity: "+Integer.valueOf((int) id.getType().longValue())+"..."+id.getValue());
    		    	And filter = new And();
    		    	filter.add(new EQ(AccountIdentificationXInfo.ID_TYPE, Integer.valueOf((int) id.getType().longValue())));
    		    	filter.add(new EQ(AccountIdentificationXInfo.ID_NUMBER, id.getValue()));
    		    	LogSupport.info(ctx,AccountsApiSupport.class, "Out getDuplicateIdentity..");
    		    	return HomeSupportHelper.get(ctx).getBeans(ctx, com.redknee.app.crm.bean.account.AccountIdentification.class, filter);
    	    	}
    	    	catch (Exception e)
    	    	{
    	    		RmiApiErrorHandlingSupport.generalException(ctx, e, "Unable to retrieve account identification", AccountsApiSupport.class);
    	    	}
    	    	
    	    	return null;
    	   }
    		
    		public static String checkParent(final Context ctx,String ban,int spid) {
    		  LogSupport.info(ctx,AccountsApiSupport.class, "In checkParent Check: "+ban+"Spid::"+spid);
    		  try {
    				 Account acct = getDuplicateBansAccount(ctx,ban);
    				 if(acct!=null){
    					boolean responsible = acct.getResponsible();
    					LogSupport.info(ctx,AccountsApiSupport.class, "responsible::"+responsible);
    					if((responsible)&&(acct.getSpid()==spid)){
    						LogSupport.info(ctx,AccountsApiSupport.class, "Spid Match for ban..");
    						return ban;
    						}
    						else if(acct.getSpid()==spid){
    							LogSupport.info(ctx,AccountsApiSupport.class, "In else Parent :::::::::::: "+acct.getParentBAN());
    							return acct.getParentBAN();
    						 }
    						}
    					} catch (CRMExceptionFault e) {
    						e.printStackTrace();
    					}
    					
    			return "";
    		}
    		
    		public static Account getDuplicateBansAccount(Context ctx, String ban) throws CRMExceptionFault
    	    {
    	    	try
    	    	{
    	    		LogSupport.info(ctx,AccountsApiSupport.class, "In getDuplicateBansAccount.."+ban);
    		    	And filter = new And();
    		    	filter.add(new EQ(com.redknee.app.crm.bean.AccountXInfo.BAN, ban));
    		    	Account account = HomeSupportHelper.get(ctx).findBean(ctx, Account.class, filter);
    	    		return account;
    	    	}
    	    	catch (Exception e)
    	    	{
    	    		try {
    					RmiApiErrorHandlingSupport.generalException(ctx, e, "Unable to retrieve account identification", AccountsApiSupport.class);
    				} catch (CRMExceptionFault e1) {
    					// TODO Auto-generated catch block
    					e1.printStackTrace();
    				}
    	    	}
    	    	
    	    	return null;	    	
    	    }
    		
    		private static void logAndThrowException(Context ctx, String msg) throws CRMExceptionFault {
    			LogSupport.minor(ctx, AccountsApiSupport.class, msg);
    			throw new CRMExceptionFault(msg);
    	    }
    	
    	private static final String MODULE = AccountsApiSupport.class.getName();
    	
	public static List<ServiceAddressOutput> getServiceAddresses(Collection<Address> addresses) {
		List<ServiceAddressOutput> serviceAddresses = null;
		if (null != addresses && !addresses.isEmpty()) {
			serviceAddresses = new ArrayList(addresses.size());
			for (Address address : addresses) {
				ServiceAddressOutput serviceAddress = new ServiceAddressOutput();
				serviceAddress.setAddressId(address.getAddressId());
				serviceAddress.setAddressType(0L + address.getAddressType().SERVICE_ADDRESS_INDEX);
				populateServiceAddressLines(serviceAddress, address);
				serviceAddresses.add(serviceAddress);
			}
		}
		return serviceAddresses;
	}

	private static void populateServiceAddressLines(ServiceAddressOutput serviceAddress, Address address) {
		serviceAddress.addAddressLine(getNonEmptyAddressLine(address.getAddressLine1()));
		serviceAddress.addAddressLine(getNonEmptyAddressLine(address.getAddressLine2()));
		serviceAddress.addAddressLine(getNonEmptyAddressLine(address.getAddressLine3()));
		serviceAddress.addAddressLine(getNonEmptyAddressLine(address.getAddressLine4()));
		serviceAddress.addAddressLine(getNonEmptyAddressLine(address.getAddressLine5()));
		serviceAddress.addAddressLine(getNonEmptyAddressLine(address.getAddressLine6()));
		serviceAddress.addAddressLine(getNonEmptyAddressLine(address.getAddressLine7()));
		serviceAddress.addAddressLine(getNonEmptyAddressLine(address.getAddressLine8()));
		serviceAddress.addAddressLine(getNonEmptyAddressLine(address.getAddressLine9()));
		serviceAddress.addAddressLine(getNonEmptyAddressLine(address.getAddressLine10()));
	}

	private static String getNonEmptyAddressLine(String addressLine) {
		return (null != addressLine && 0 < addressLine.trim().length()) ? addressLine : null;
	}
}
