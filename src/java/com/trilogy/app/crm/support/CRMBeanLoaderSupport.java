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
package com.trilogy.app.crm.support;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.trilogy.framework.core.locale.Currency;
import com.trilogy.framework.xhome.beans.AbstractBean;
import com.trilogy.framework.xhome.beans.xi.PropertyInfo;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.language.Lang;
import com.trilogy.framework.xhome.msp.Spid;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountCreationTemplate;
import com.trilogy.app.crm.bean.AccountCategory;
import com.trilogy.app.crm.bean.AccountXInfo;
import com.trilogy.app.crm.bean.ActivationReasonCode;
import com.trilogy.app.crm.bean.AutoDepositReleaseCriteria;
import com.trilogy.app.crm.bean.AuxiliaryService;
import com.trilogy.app.crm.bean.AuxiliaryServiceXInfo;
import com.trilogy.app.crm.bean.BearerType;
import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.CRMSpidXInfo;
import com.trilogy.app.crm.bean.CreditCategory;
import com.trilogy.app.crm.bean.DealerCode;
import com.trilogy.app.crm.bean.DiscountClass;
import com.trilogy.app.crm.bean.GLCodeMapping;
import com.trilogy.app.crm.bean.Invoice;
import com.trilogy.app.crm.bean.InvoiceXInfo;
import com.trilogy.app.crm.bean.MarketingCampaignBean;
import com.trilogy.app.crm.bean.Occupation;
import com.trilogy.app.crm.bean.PricePlan;
import com.trilogy.app.crm.bean.Province;
import com.trilogy.app.crm.bean.ReasonCode;
import com.trilogy.app.crm.bean.ServiceFee2XInfo;
import com.trilogy.app.crm.bean.ServicePackageXInfo;
import com.trilogy.app.crm.bean.ServiceXInfo;
import com.trilogy.app.crm.bean.SpidLang;
import com.trilogy.app.crm.bean.SubBillingLanguage;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberAuxiliaryService;
import com.trilogy.app.crm.bean.SubscriberAuxiliaryServiceXInfo;
import com.trilogy.app.crm.bean.SubscriberCategory;
import com.trilogy.app.crm.bean.SubscriberServicesXInfo;
import com.trilogy.app.crm.bean.SubscriberXInfo;
import com.trilogy.app.crm.bean.TaxAuthority;
import com.trilogy.app.crm.bean.TransactionMethod;
import com.trilogy.app.crm.bean.TransactionXInfo;
import com.trilogy.app.crm.bean.VMPlan;
import com.trilogy.app.crm.bean.account.AccountRole;
import com.trilogy.app.crm.bean.audi.AudiLoadSubscriber;
import com.trilogy.app.crm.bean.audi.AudiLoadSubscriberXInfo;
import com.trilogy.app.crm.bean.audi.AudiUpdateSubscriber;
import com.trilogy.app.crm.bean.audi.AudiUpdateSubscriberXInfo;
import com.trilogy.app.crm.bean.bank.Bank;
import com.trilogy.app.crm.bean.core.BundleProfile;
import com.trilogy.app.crm.bean.payment.Contract;
import com.trilogy.app.crm.bean.payment.PaymentAgent;
import com.trilogy.app.crm.bean.payment.PaymentPlan;
import com.trilogy.app.crm.bean.service.xml.XMLProvisioningServiceType;
import com.trilogy.app.crm.bundle.BundleFeeXInfo;
import com.trilogy.app.crm.bundle.BundleProfileXInfo;
import com.trilogy.app.crm.calculation.support.CalcBeanLoaderSupport;
import com.trilogy.app.crm.extension.service.AlcatelSSCServiceExtensionXInfo;
import com.trilogy.app.crm.extension.spid.AlcatelSSCSpidExtension;
import com.trilogy.app.crm.extension.spid.AlcatelSSCSpidExtensionXInfo;
import com.trilogy.app.crm.extension.subscriber.AlcatelSSCSubscriberExtension;
import com.trilogy.app.crm.extension.subscriber.AlcatelSSCSubscriberExtensionXInfo;
import com.trilogy.app.crm.invoice.bean.InvoiceSpid;
import com.trilogy.app.crm.invoice.delivery.InvoiceDeliveryOption;
import com.trilogy.app.crm.move.request.AccountExtensionMoveRequest;
import com.trilogy.app.crm.move.request.AccountExtensionMoveRequestXInfo;
import com.trilogy.app.crm.move.request.AccountMoveRequest;
import com.trilogy.app.crm.move.request.AccountMoveRequestXInfo;
import com.trilogy.app.crm.move.request.ConvertAccountBillingTypeRequest;
import com.trilogy.app.crm.move.request.ConvertAccountBillingTypeRequestXInfo;
import com.trilogy.app.crm.move.request.ConvertSubscriptionBillingTypeRequest;
import com.trilogy.app.crm.move.request.ConvertSubscriptionBillingTypeRequestXInfo;
import com.trilogy.app.crm.move.request.SubscriptionMoveRequest;
import com.trilogy.app.crm.move.request.SubscriptionMoveRequestXInfo;
import com.trilogy.app.crm.transfer.TransferDispute;
import com.trilogy.app.crm.transfer.TransferDisputeXInfo;


/**
 * Specialized version of the default bean loader support to add AppCrm specific bean
 * functionality.
 * 
 * As these CRM specific beans are moved into AppCrmCore, so should the special behaviour
 * so that it can be used by other apps.
 *
 * @author aaron.gourley@redknee.com
 * @since 8.3
 */
public class CRMBeanLoaderSupport extends CalcBeanLoaderSupport
{
    protected static BeanLoaderSupport CRM_instance_ = null;
    public static BeanLoaderSupport instance()
    {
        if (CRM_instance_ == null)
        {
            CRM_instance_ = new CRMBeanLoaderSupport();
        }
        return CRM_instance_;
    }

    protected CRMBeanLoaderSupport()
    {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T extends AbstractBean> Map<Class, Collection<PropertyInfo>> getBeanLoaderMap(Context ctx, Class<T> baseType)
    {
        Map<Class, Collection<PropertyInfo>> map = new HashMap<Class, Collection<PropertyInfo>>();
        if (CRMSpid.class.isAssignableFrom(baseType))
        {
            map = getSpidBasedMap();
        }
        else if (Account.class.isAssignableFrom(baseType))
        {
            map = getAccountBasedMap();
        }
        else if (Invoice.class.isAssignableFrom(baseType))
        {
            map = getInvoiceBasedMap();
        }
        else if (Subscriber.class.isAssignableFrom(baseType))
        {
            map = getSubscriberBasedMap();
        }
        else if (AlcatelSSCSpidExtension.class.isAssignableFrom(baseType))
        {
            map = getAlcatelSSCSpidBasedMap();
        }
        else if (AlcatelSSCSubscriberExtension.class.isAssignableFrom(baseType))
        {
            map = getAlcatelSSCSubscriptionBasedMap();
        }
        else if (TransferDispute.class.isAssignableFrom(baseType))
        {
            map = getTransferDisputeBasedMap();
        }
        else if (AudiLoadSubscriber.class.isAssignableFrom(baseType))
        {
            map = getAudiLoadSubscriberBasedMap();
        }
        else if (AudiUpdateSubscriber.class.isAssignableFrom(baseType))
        {
            map = getAudiUpdateSubscriberBasedMap();
        }
        else if (ConvertAccountBillingTypeRequest.class.isAssignableFrom(baseType))
        {
            map = getConvertAccountBillingTypeRequestBasedMap();
        }
        else if (ConvertSubscriptionBillingTypeRequest.class.isAssignableFrom(baseType))
        {
            map = getConvertSubscriptionBillingTypeRequestBasedMap();
        }
        else if (AccountMoveRequest.class.isAssignableFrom(baseType))
        {
            map = getAccountMoveRequestBasedMap();
        }
        else if (AccountExtensionMoveRequest.class.isAssignableFrom(baseType))
        {
            map = getAccountExtensionMoveRequestBasedMap();
        }
        else if (SubscriptionMoveRequest.class.isAssignableFrom(baseType))
        {
            map = getSubscriptionMoveRequestBasedMap();
        }
        else if (AuxiliaryService.class.isAssignableFrom(baseType))
        {
            map = getAuxiliaryServiceBasedMap();
        }
        else if (SubscriberAuxiliaryService.class.isAssignableFrom(baseType))
        {
            map = getSubscriberAuxiliaryServiceBasedMap();
        }
        else
        {
            map = super.getBeanLoaderMap(ctx, baseType);
        }
        return map;
    }

    @Override
    public Map<Class, Collection<PropertyInfo>> getSpidBasedMap()
    {
        synchronized(SPID_LOCK)
        {
            if (SPID_BASED_MAP == null)
            {
                // Add SPID related lookup hierarchy
                SPID_BASED_MAP = new HashMap<Class, Collection<PropertyInfo>>();
                addBeanLoaderMapEntry(SPID_BASED_MAP, Lang.class, CRMSpidXInfo.LANGUAGE);
                addBeanLoaderMapEntry(SPID_BASED_MAP, SpidLang.class, CRMSpidXInfo.LANGUAGE);
                addBeanLoaderMapEntry(SPID_BASED_MAP, Currency.class, CRMSpidXInfo.CURRENCY);
                addBeanLoaderMapEntry(SPID_BASED_MAP, AutoDepositReleaseCriteria.class, CRMSpidXInfo.AUTO_DEPOSIT_RELEASE_CRITERIA);
                addBeanLoaderMapEntry(SPID_BASED_MAP, TaxAuthority.class, CRMSpidXInfo.TAX_AUTHORITY);
                addBeanLoaderMapEntry(SPID_BASED_MAP, DealerCode.class, CRMSpidXInfo.DEALER);
                addBeanLoaderMapEntry(SPID_BASED_MAP, com.redknee.app.crm.bean.BillCycle.class, CRMSpidXInfo.BILL_CYCLE);
                addBeanLoaderMapEntry(SPID_BASED_MAP, com.redknee.app.crm.bean.core.BillCycle.class, CRMSpidXInfo.BILL_CYCLE);
                addBeanLoaderMapEntry(SPID_BASED_MAP, com.redknee.app.crm.bean.CreditCategory.class, CRMSpidXInfo.DEFAULT_CREDIT_CATEGORY);
                addBeanLoaderMapEntry(SPID_BASED_MAP, com.redknee.app.crm.bean.core.CreditCategory.class, CRMSpidXInfo.DEFAULT_CREDIT_CATEGORY);
                //addBeanLoaderMapEntry(SPID_BASED_MAP, HLRClientMap.class, CRMSpidXInfo.DEFAULT_HLR_ID);
                addBeanLoaderMapEntry(SPID_BASED_MAP, com.redknee.app.crm.bean.account.SubscriptionType.class, CRMSpidXInfo.DEFAULT_SUBSCRIPTION_TYPE);
                addBeanLoaderMapEntry(SPID_BASED_MAP, com.redknee.app.crm.bean.core.SubscriptionType.class, CRMSpidXInfo.DEFAULT_SUBSCRIPTION_TYPE);
                SPID_BASED_MAP = getUnmodifiableBeanLoaderMap(SPID_BASED_MAP);
            }
        }
        return SPID_BASED_MAP;
    }

    @Override
    public Map<Class, Collection<PropertyInfo>> getAccountBasedMap()
    {
        synchronized(ACCOUNT_LOCK)
        {
            if (ACCOUNT_BASED_MAP == null)
            {
                // Add account related lookup hierarchy
                Map<Class, Collection<PropertyInfo>> accountMap = new HashMap<Class, Collection<PropertyInfo>>();
                addBeanLoaderMapEntry(accountMap, CRMSpid.class, AccountXInfo.SPID);
                addBeanLoaderMapEntry(accountMap, InvoiceSpid.class, AccountXInfo.SPID);
                addBeanLoaderMapEntry(accountMap, Spid.class, AccountXInfo.SPID);
                addBeanLoaderMapEntry(accountMap, com.redknee.app.crm.bean.BillCycle.class, AccountXInfo.BILL_CYCLE_ID);
                addBeanLoaderMapEntry(accountMap, com.redknee.app.crm.bean.core.BillCycle.class, AccountXInfo.BILL_CYCLE_ID);
                addBeanLoaderMapEntry(accountMap, Bank.class, AccountXInfo.BANK_ID);
                addBeanLoaderMapEntry(accountMap, AccountCreationTemplate.class, AccountXInfo.ACT_ID);
                addBeanLoaderMapEntry(accountMap, Province.class, AccountXInfo.BILLING_PROVINCE);
                addBeanLoaderMapEntry(accountMap, AccountRole.class, AccountXInfo.ROLE);
                addBeanLoaderMapEntry(accountMap, AccountCategory.class, AccountXInfo.TYPE);
                addBeanLoaderMapEntry(accountMap, com.redknee.app.crm.bean.CreditCategory.class, AccountXInfo.CREDIT_CATEGORY);
                addBeanLoaderMapEntry(accountMap, com.redknee.app.crm.bean.core.CreditCategory.class, AccountXInfo.CREDIT_CATEGORY);
                addBeanLoaderMapEntry(accountMap, DiscountClass.class, AccountXInfo.DISCOUNT_CLASS);
                addBeanLoaderMapEntry(accountMap, DealerCode.class, AccountXInfo.DEALER_CODE);
                addBeanLoaderMapEntry(accountMap, TaxAuthority.class, AccountXInfo.TAX_AUTHORITY);
                addBeanLoaderMapEntry(accountMap, SubBillingLanguage.class, AccountXInfo.LANGUAGE);
                addBeanLoaderMapEntry(accountMap, Lang.class, AccountXInfo.LANGUAGE);
                addBeanLoaderMapEntry(accountMap, Currency.class, AccountXInfo.CURRENCY);
                addBeanLoaderMapEntry(accountMap, PaymentPlan.class, AccountXInfo.PAYMENT_PLAN);
                addBeanLoaderMapEntry(accountMap, TransactionMethod.class, AccountXInfo.PAYMENT_METHOD_TYPE);
                addBeanLoaderMapEntry(accountMap, InvoiceDeliveryOption.class, AccountXInfo.INVOICE_DELIVERY_OPTION);
                addBeanLoaderMapEntry(accountMap, Occupation.class, AccountXInfo.OCCUPATION);
                addBeanLoaderMapEntry(accountMap, Contract.class, AccountXInfo.CONTRACT);
                mergeBeanLoaderMaps(accountMap, getSpidBasedMap());
                ACCOUNT_BASED_MAP = getUnmodifiableBeanLoaderMap(accountMap);
            }
        }
        return ACCOUNT_BASED_MAP;
    }

    @Override
    public Map<Class, Collection<PropertyInfo>> getInvoiceBasedMap()
    {
        synchronized(INVOICE_LOCK)
        {
            if (INVOICE_BASED_MAP == null)
            {
                // Add invoice related lookup info
                Map<Class, Collection<PropertyInfo>> invoiceMap = new HashMap<Class, Collection<PropertyInfo>>();
                addBeanLoaderMapEntry(invoiceMap, Account.class, InvoiceXInfo.BAN);
                addBeanLoaderMapEntry(invoiceMap, com.redknee.app.crm.bean.BillCycle.class, InvoiceXInfo.BILL_CYCLE_ID);
                addBeanLoaderMapEntry(invoiceMap, com.redknee.app.crm.bean.core.BillCycle.class, InvoiceXInfo.BILL_CYCLE_ID);
                addBeanLoaderMapEntry(invoiceMap, CRMSpid.class, InvoiceXInfo.SPID);
                addBeanLoaderMapEntry(invoiceMap, InvoiceSpid.class, InvoiceXInfo.SPID);
                addBeanLoaderMapEntry(invoiceMap, Spid.class, InvoiceXInfo.SPID);
                mergeBeanLoaderMaps(invoiceMap, getAccountBasedMap());
                mergeBeanLoaderMaps(invoiceMap, getSpidBasedMap());
                INVOICE_BASED_MAP = getUnmodifiableBeanLoaderMap(invoiceMap);
            }
        }
        return INVOICE_BASED_MAP;
    }

    public Map<Class, Collection<PropertyInfo>> getTransferDisputeBasedMap()
    {
        synchronized(TRANSFER_DISPUTE_LOCK)
        {
            if (TRANSFER_DISPUTE_BASED_MAP == null)
            {
                // Add invoice related lookup info
                Map<Class, Collection<PropertyInfo>> transferDisputeMap = new HashMap<Class, Collection<PropertyInfo>>();
                addBeanLoaderMapEntry(transferDisputeMap, Subscriber.class, TransferDisputeXInfo.RECP_SUB_ID);
                mergeBeanLoaderMaps(transferDisputeMap, getSubscriberBasedMap());
                mergeBeanLoaderMaps(transferDisputeMap, getAccountBasedMap());
                mergeBeanLoaderMaps(transferDisputeMap, getSpidBasedMap());
                TRANSFER_DISPUTE_BASED_MAP = getUnmodifiableBeanLoaderMap(transferDisputeMap);
            }
        }
        return TRANSFER_DISPUTE_BASED_MAP;
    }

    @Override
    public Map<Class, Collection<PropertyInfo>> getTransactionBasedMap()
    {
        synchronized(TRANSACTION_LOCK)
        {
            if (TRANSACTION_BASED_MAP == null)
            {
                TRANSACTION_BASED_MAP = new HashMap<Class, Collection<PropertyInfo>>();
                addBeanLoaderMapEntry(TRANSACTION_BASED_MAP, Subscriber.class, TransactionXInfo.SUBSCRIBER_ID);
                addBeanLoaderMapEntry(TRANSACTION_BASED_MAP, CRMSpid.class, TransactionXInfo.SPID);
                addBeanLoaderMapEntry(TRANSACTION_BASED_MAP, InvoiceSpid.class, TransactionXInfo.SPID);
                addBeanLoaderMapEntry(TRANSACTION_BASED_MAP, Spid.class, TransactionXInfo.SPID);
                addBeanLoaderMapEntry(TRANSACTION_BASED_MAP, com.redknee.app.crm.bean.AdjustmentType.class, TransactionXInfo.ADJUSTMENT_TYPE);
                addBeanLoaderMapEntry(TRANSACTION_BASED_MAP, com.redknee.app.crm.bean.core.AdjustmentType.class, TransactionXInfo.ADJUSTMENT_TYPE);
                addBeanLoaderMapEntry(TRANSACTION_BASED_MAP, GLCodeMapping.class, TransactionXInfo.GLCODE);
                addBeanLoaderMapEntry(TRANSACTION_BASED_MAP, com.redknee.app.crm.bean.Msisdn.class, TransactionXInfo.MSISDN);
                addBeanLoaderMapEntry(TRANSACTION_BASED_MAP, com.redknee.app.crm.bean.core.Msisdn.class, TransactionXInfo.MSISDN);
                addBeanLoaderMapEntry(TRANSACTION_BASED_MAP, com.redknee.app.crm.bean.custom.Msisdn.class, TransactionXInfo.MSISDN);
                addBeanLoaderMapEntry(TRANSACTION_BASED_MAP, com.redknee.app.crm.bean.ui.Msisdn.class, TransactionXInfo.MSISDN);
                addBeanLoaderMapEntry(TRANSACTION_BASED_MAP, PaymentAgent.class, TransactionXInfo.PAYMENT_AGENCY);
                addBeanLoaderMapEntry(TRANSACTION_BASED_MAP, ReasonCode.class, TransactionXInfo.REASON_CODE);
                addBeanLoaderMapEntry(TRANSACTION_BASED_MAP, com.redknee.app.crm.bean.account.SubscriptionType.class, TransactionXInfo.SUBSCRIPTION_TYPE_ID);
                addBeanLoaderMapEntry(TRANSACTION_BASED_MAP, com.redknee.app.crm.bean.core.SubscriptionType.class, TransactionXInfo.SUBSCRIPTION_TYPE_ID);
                addBeanLoaderMapEntry(TRANSACTION_BASED_MAP, TransactionMethod.class, TransactionXInfo.TRANSACTION_METHOD);
                mergeBeanLoaderMaps(TRANSACTION_BASED_MAP, getSubscriberBasedMap());
                mergeBeanLoaderMaps(TRANSACTION_BASED_MAP, getAccountBasedMap());
                mergeBeanLoaderMaps(TRANSACTION_BASED_MAP, getSpidBasedMap());
                TRANSACTION_BASED_MAP = getUnmodifiableBeanLoaderMap(TRANSACTION_BASED_MAP);
            }
        }
        return TRANSACTION_BASED_MAP;
    }

    @Override
    public Map<Class, Collection<PropertyInfo>> getSubscriberBasedMap()
    {
        synchronized(SUBSCRIPTION_LOCK)
        {
            if (SUBSCRIPTION_BASED_MAP == null)
            {
                // Add subscription related lookup hierarchy
                SUBSCRIPTION_BASED_MAP = new HashMap<Class, Collection<PropertyInfo>>();
                addBeanLoaderMapEntry(SUBSCRIPTION_BASED_MAP, Account.class, SubscriberXInfo.BAN);
                addBeanLoaderMapEntry(SUBSCRIPTION_BASED_MAP, CRMSpid.class, SubscriberXInfo.SPID);
                addBeanLoaderMapEntry(SUBSCRIPTION_BASED_MAP, InvoiceSpid.class, SubscriberXInfo.SPID);
                addBeanLoaderMapEntry(SUBSCRIPTION_BASED_MAP, Spid.class, SubscriberXInfo.SPID);
                addBeanLoaderMapEntry(SUBSCRIPTION_BASED_MAP, com.redknee.app.crm.bean.account.SubscriptionClass.class, SubscriberXInfo.SUBSCRIPTION_CLASS);
                addBeanLoaderMapEntry(SUBSCRIPTION_BASED_MAP, com.redknee.app.crm.bean.core.SubscriptionClass.class, SubscriberXInfo.SUBSCRIPTION_CLASS);
                addBeanLoaderMapEntry(SUBSCRIPTION_BASED_MAP, com.redknee.app.crm.bean.account.SubscriptionType.class, SubscriberXInfo.SUBSCRIPTION_TYPE);
                addBeanLoaderMapEntry(SUBSCRIPTION_BASED_MAP, com.redknee.app.crm.bean.core.SubscriptionType.class, SubscriberXInfo.SUBSCRIPTION_TYPE);
                addBeanLoaderMapEntry(SUBSCRIPTION_BASED_MAP, ActivationReasonCode.class, SubscriberXInfo.REASON_CODE);
                //addBeanLoaderMapEntry(SUBSCRIPTION_BASED_MAP, HLRClientMap.class, SubscriberXInfo.HLR_ID);
                addBeanLoaderMapEntry(SUBSCRIPTION_BASED_MAP, SubBillingLanguage.class, SubscriberXInfo.BILLING_LANGUAGE);
                addBeanLoaderMapEntry(SUBSCRIPTION_BASED_MAP, PricePlan.class, SubscriberXInfo.PRICE_PLAN);
                addBeanLoaderMapEntry(SUBSCRIPTION_BASED_MAP, SubscriberCategory.class, SubscriberXInfo.SUBSCRIBER_CATEGORY);
                addBeanLoaderMapEntry(SUBSCRIPTION_BASED_MAP, MarketingCampaignBean.class, SubscriberXInfo.MARKETING_CAMPAIGN_BEAN);

                // TODO: Connect the dots in the dependency chain for price plan related maps
                // i.e. Service, Aux Service, Bundle, Service Package
                mergeBeanLoaderMaps(SUBSCRIPTION_BASED_MAP, getServiceBasedMap());
                mergeBeanLoaderMaps(SUBSCRIPTION_BASED_MAP, getSubscriberAuxiliaryServiceBasedMap());
                mergeBeanLoaderMaps(SUBSCRIPTION_BASED_MAP, getBundleBasedMap());
                mergeBeanLoaderMaps(SUBSCRIPTION_BASED_MAP, getServicePackageBasedMap());

                mergeBeanLoaderMaps(SUBSCRIPTION_BASED_MAP, getAccountBasedMap());
                mergeBeanLoaderMaps(SUBSCRIPTION_BASED_MAP, getSpidBasedMap());
                SUBSCRIPTION_BASED_MAP = getUnmodifiableBeanLoaderMap(SUBSCRIPTION_BASED_MAP);
            }
        }
        return SUBSCRIPTION_BASED_MAP;
    }

    @Override
    public Map<Class, Collection<PropertyInfo>> getServiceBasedMap()
    {
        synchronized(SERVICE_LOCK)
        {
            if (SERVICE_BASED_MAP == null)
            {
                // Add service related lookup hierarchy
                SERVICE_BASED_MAP = new HashMap<Class, Collection<PropertyInfo>>();
                addBeanLoaderMapEntry(SERVICE_BASED_MAP, com.redknee.app.crm.bean.Service.class, SubscriberServicesXInfo.SERVICE_ID, ServiceFee2XInfo.SERVICE_ID);
                addBeanLoaderMapEntry(SERVICE_BASED_MAP, com.redknee.app.crm.bean.core.Service.class, SubscriberServicesXInfo.SERVICE_ID, ServiceFee2XInfo.SERVICE_ID);
                addBeanLoaderMapEntry(SERVICE_BASED_MAP, com.redknee.app.crm.bean.ui.Service.class, SubscriberServicesXInfo.SERVICE_ID, ServiceFee2XInfo.SERVICE_ID);
                addBeanLoaderMapEntry(SERVICE_BASED_MAP, Subscriber.class, SubscriberServicesXInfo.SUBSCRIBER_ID);
                addBeanLoaderMapEntry(SERVICE_BASED_MAP, CRMSpid.class, ServiceXInfo.SPID);
                addBeanLoaderMapEntry(SERVICE_BASED_MAP, InvoiceSpid.class, ServiceXInfo.SPID);
                addBeanLoaderMapEntry(SERVICE_BASED_MAP, Spid.class, ServiceXInfo.SPID);
                addBeanLoaderMapEntry(SERVICE_BASED_MAP, com.redknee.app.crm.bean.account.SubscriptionType.class, ServiceXInfo.SUBSCRIPTION_TYPE);
                addBeanLoaderMapEntry(SERVICE_BASED_MAP, com.redknee.app.crm.bean.core.SubscriptionType.class, ServiceXInfo.SUBSCRIPTION_TYPE);
                addBeanLoaderMapEntry(SERVICE_BASED_MAP, VMPlan.class, ServiceXInfo.VM_PLAN_ID);
                addBeanLoaderMapEntry(SERVICE_BASED_MAP, XMLProvisioningServiceType.class, ServiceXInfo.XML_PROV_SVC_TYPE);
                addBeanLoaderMapEntry(SERVICE_BASED_MAP, com.redknee.app.crm.bean.AdjustmentType.class, ServiceXInfo.ADJUSTMENT_TYPE);
                addBeanLoaderMapEntry(SERVICE_BASED_MAP, com.redknee.app.crm.bean.core.AdjustmentType.class, ServiceXInfo.ADJUSTMENT_TYPE);
                mergeBeanLoaderMaps(SERVICE_BASED_MAP, getSpidBasedMap());
                SERVICE_BASED_MAP = getUnmodifiableBeanLoaderMap(SERVICE_BASED_MAP);
            }
        }
        return SERVICE_BASED_MAP;
    }

    public Map<Class, Collection<PropertyInfo>> getAlcatelSSCSpidBasedMap()
    {
        synchronized(ALCATEL_SSC_EXTENSION_SPID_LOCK)
        {
            if (ALCATEL_SSC_EXTENSION_SPID_BASED_MAP == null)
            {
                // Add Alcatel SSC SPID related lookup hierarchy
                ALCATEL_SSC_EXTENSION_SPID_BASED_MAP = new HashMap<Class, Collection<PropertyInfo>>();
                addBeanLoaderMapEntry(ALCATEL_SSC_EXTENSION_SPID_BASED_MAP, CRMSpid.class, AlcatelSSCSpidExtensionXInfo.SPID);
                addBeanLoaderMapEntry(ALCATEL_SSC_EXTENSION_SPID_BASED_MAP, InvoiceSpid.class, AlcatelSSCSpidExtensionXInfo.SPID);
                addBeanLoaderMapEntry(ALCATEL_SSC_EXTENSION_SPID_BASED_MAP, Spid.class, AlcatelSSCSpidExtensionXInfo.SPID);
                mergeBeanLoaderMaps(ALCATEL_SSC_EXTENSION_SPID_BASED_MAP, getSpidBasedMap());
                ALCATEL_SSC_EXTENSION_SPID_BASED_MAP = getUnmodifiableBeanLoaderMap(ALCATEL_SSC_EXTENSION_SPID_BASED_MAP);
            }
        }
        return ALCATEL_SSC_EXTENSION_SPID_BASED_MAP;
    }

    @Override
    public Map<Class, Collection<PropertyInfo>> getAlcatelSSCServiceBasedMap()
    {
        synchronized(ALCATEL_SSC_EXTENSION_SERVICE_LOCK)
        {
            if (ALCATEL_SSC_EXTENSION_SERVICE_BASED_MAP == null)
            {
                // Add Alcatel SSC Service related lookup hierarchy
                ALCATEL_SSC_EXTENSION_SERVICE_BASED_MAP = new HashMap<Class, Collection<PropertyInfo>>();
                addBeanLoaderMapEntry(ALCATEL_SSC_EXTENSION_SERVICE_BASED_MAP, CRMSpid.class, AlcatelSSCServiceExtensionXInfo.SPID);
                addBeanLoaderMapEntry(ALCATEL_SSC_EXTENSION_SERVICE_BASED_MAP, InvoiceSpid.class, AlcatelSSCServiceExtensionXInfo.SPID);
                addBeanLoaderMapEntry(ALCATEL_SSC_EXTENSION_SERVICE_BASED_MAP, Spid.class, AlcatelSSCServiceExtensionXInfo.SPID);
                addBeanLoaderMapEntry(ALCATEL_SSC_EXTENSION_SERVICE_BASED_MAP, com.redknee.app.crm.bean.Service.class, AlcatelSSCServiceExtensionXInfo.SERVICE_ID);
                addBeanLoaderMapEntry(ALCATEL_SSC_EXTENSION_SERVICE_BASED_MAP, com.redknee.app.crm.bean.core.Service.class, AlcatelSSCServiceExtensionXInfo.SERVICE_ID);
                addBeanLoaderMapEntry(ALCATEL_SSC_EXTENSION_SERVICE_BASED_MAP, com.redknee.app.crm.bean.ui.Service.class, AlcatelSSCServiceExtensionXInfo.SERVICE_ID);
                mergeBeanLoaderMaps(ALCATEL_SSC_EXTENSION_SERVICE_BASED_MAP, getServiceBasedMap());
                mergeBeanLoaderMaps(ALCATEL_SSC_EXTENSION_SERVICE_BASED_MAP, getSpidBasedMap());
                ALCATEL_SSC_EXTENSION_SERVICE_BASED_MAP = getUnmodifiableBeanLoaderMap(ALCATEL_SSC_EXTENSION_SERVICE_BASED_MAP);
            }
        }
        return ALCATEL_SSC_EXTENSION_SERVICE_BASED_MAP;
    }

    public Map<Class, Collection<PropertyInfo>> getAlcatelSSCSubscriptionBasedMap()
    {
        synchronized(ALCATEL_SSC_EXTENSION_SUBSCRIBER_LOCK)
        {
            if (ALCATEL_SSC_EXTENSION_SUBSCRIBER_BASED_MAP == null)
            {
                // Add Alcatel SSC Subscriber related lookup hierarchy
                ALCATEL_SSC_EXTENSION_SUBSCRIBER_BASED_MAP = new HashMap<Class, Collection<PropertyInfo>>();
                addBeanLoaderMapEntry(ALCATEL_SSC_EXTENSION_SUBSCRIBER_BASED_MAP, CRMSpid.class, AlcatelSSCSubscriberExtensionXInfo.SPID);
                addBeanLoaderMapEntry(ALCATEL_SSC_EXTENSION_SUBSCRIBER_BASED_MAP, InvoiceSpid.class, AlcatelSSCSubscriberExtensionXInfo.SPID);
                addBeanLoaderMapEntry(ALCATEL_SSC_EXTENSION_SUBSCRIBER_BASED_MAP, Spid.class, AlcatelSSCSubscriberExtensionXInfo.SPID);
                addBeanLoaderMapEntry(ALCATEL_SSC_EXTENSION_SUBSCRIBER_BASED_MAP, Subscriber.class, AlcatelSSCSubscriberExtensionXInfo.SUB_ID);
                mergeBeanLoaderMaps(ALCATEL_SSC_EXTENSION_SUBSCRIBER_BASED_MAP, getSubscriberBasedMap());
                mergeBeanLoaderMaps(ALCATEL_SSC_EXTENSION_SUBSCRIBER_BASED_MAP, getSpidBasedMap());
                ALCATEL_SSC_EXTENSION_SUBSCRIBER_BASED_MAP = getUnmodifiableBeanLoaderMap(ALCATEL_SSC_EXTENSION_SUBSCRIBER_BASED_MAP);
            }
        }
        return ALCATEL_SSC_EXTENSION_SUBSCRIBER_BASED_MAP;
    }

    @Override
    public Map<Class, Collection<PropertyInfo>> getSubscriberAuxiliaryServiceBasedMap()
    {
        synchronized(SUBSCRIBER_AUXILIARY_SERVICE_LOCK)
        {
            if (SUBSCRIBER_AUXILIARY_SERVICE_BASED_MAP == null)
            {
                // Add SubscriberAuxiliaryService related lookup hierarchy
                SUBSCRIBER_AUXILIARY_SERVICE_BASED_MAP = new HashMap<Class, Collection<PropertyInfo>>();
                addBeanLoaderMapEntry(SUBSCRIBER_AUXILIARY_SERVICE_BASED_MAP, AuxiliaryService.class, SubscriberAuxiliaryServiceXInfo.AUXILIARY_SERVICE_IDENTIFIER);
                addBeanLoaderMapEntry(SUBSCRIBER_AUXILIARY_SERVICE_BASED_MAP, com.redknee.app.crm.bean.ui.AuxiliaryService.class, SubscriberAuxiliaryServiceXInfo.AUXILIARY_SERVICE_IDENTIFIER);
                addBeanLoaderMapEntry(SUBSCRIBER_AUXILIARY_SERVICE_BASED_MAP, com.redknee.app.crm.bean.Msisdn.class, SubscriberAuxiliaryServiceXInfo.AMSISDN);
                addBeanLoaderMapEntry(SUBSCRIBER_AUXILIARY_SERVICE_BASED_MAP, com.redknee.app.crm.bean.core.Msisdn.class, SubscriberAuxiliaryServiceXInfo.AMSISDN);
                addBeanLoaderMapEntry(SUBSCRIBER_AUXILIARY_SERVICE_BASED_MAP, com.redknee.app.crm.bean.custom.Msisdn.class, SubscriberAuxiliaryServiceXInfo.AMSISDN);
                addBeanLoaderMapEntry(SUBSCRIBER_AUXILIARY_SERVICE_BASED_MAP, com.redknee.app.crm.bean.ui.Msisdn.class, SubscriberAuxiliaryServiceXInfo.AMSISDN);
                addBeanLoaderMapEntry(SUBSCRIBER_AUXILIARY_SERVICE_BASED_MAP, Subscriber.class, SubscriberAuxiliaryServiceXInfo.SUBSCRIBER_IDENTIFIER);
                addBeanLoaderMapEntry(SUBSCRIBER_AUXILIARY_SERVICE_BASED_MAP, BearerType.class, SubscriberAuxiliaryServiceXInfo.BEARER_TYPE);

                SUBSCRIBER_AUXILIARY_SERVICE_BASED_MAP = new HashMap<Class, Collection<PropertyInfo>>();
                mergeBeanLoaderMaps(SUBSCRIBER_AUXILIARY_SERVICE_BASED_MAP, getSubscriberBasedMap());
                mergeBeanLoaderMaps(SUBSCRIBER_AUXILIARY_SERVICE_BASED_MAP, getAuxiliaryServiceBasedMap());
                mergeBeanLoaderMaps(SUBSCRIBER_AUXILIARY_SERVICE_BASED_MAP, getSpidBasedMap());
                SUBSCRIBER_AUXILIARY_SERVICE_BASED_MAP = getUnmodifiableBeanLoaderMap(SUBSCRIBER_AUXILIARY_SERVICE_BASED_MAP);
            }
        }
        return SUBSCRIBER_AUXILIARY_SERVICE_BASED_MAP;
    }

    @Override
    public Map<Class, Collection<PropertyInfo>> getAuxiliaryServiceBasedMap()
    {
        synchronized(AUXILIARY_SERVICE_LOCK)
        {
            if (AUXILIARY_SERVICE_BASED_MAP == null)
            {
                // Add service related lookup hierarchy
                Map<Class, Collection<PropertyInfo>> SUBSCRIBER_AUXILIARY_SERVICE_BASED_MAP = new HashMap<Class, Collection<PropertyInfo>>();
                addBeanLoaderMapEntry(SUBSCRIBER_AUXILIARY_SERVICE_BASED_MAP, CRMSpid.class, AuxiliaryServiceXInfo.SPID);
                addBeanLoaderMapEntry(SUBSCRIBER_AUXILIARY_SERVICE_BASED_MAP, InvoiceSpid.class, AuxiliaryServiceXInfo.SPID);
                addBeanLoaderMapEntry(SUBSCRIBER_AUXILIARY_SERVICE_BASED_MAP, Spid.class, AuxiliaryServiceXInfo.SPID);
                addBeanLoaderMapEntry(SUBSCRIBER_AUXILIARY_SERVICE_BASED_MAP, com.redknee.app.crm.bean.account.SubscriptionType.class, AuxiliaryServiceXInfo.SUBSCRIBER_TYPE);
                addBeanLoaderMapEntry(SUBSCRIBER_AUXILIARY_SERVICE_BASED_MAP, com.redknee.app.crm.bean.core.SubscriptionType.class, AuxiliaryServiceXInfo.SUBSCRIBER_TYPE);
                addBeanLoaderMapEntry(SUBSCRIBER_AUXILIARY_SERVICE_BASED_MAP, com.redknee.app.crm.bean.AdjustmentType.class, AuxiliaryServiceXInfo.ADJUSTMENT_TYPE);
                addBeanLoaderMapEntry(SUBSCRIBER_AUXILIARY_SERVICE_BASED_MAP, com.redknee.app.crm.bean.core.AdjustmentType.class, AuxiliaryServiceXInfo.ADJUSTMENT_TYPE);
                mergeBeanLoaderMaps(SUBSCRIBER_AUXILIARY_SERVICE_BASED_MAP, getSpidBasedMap());
                AUXILIARY_SERVICE_BASED_MAP = getUnmodifiableBeanLoaderMap(SUBSCRIBER_AUXILIARY_SERVICE_BASED_MAP);
            }
        }
        return AUXILIARY_SERVICE_BASED_MAP;
    }

    @Override
    public Map<Class, Collection<PropertyInfo>> getBundleBasedMap()
    {
        synchronized(BUNDLE_LOCK)
        {
            if (BUNDLE_BASED_MAP == null)
            {
                // Add service related lookup hierarchy
                BUNDLE_BASED_MAP = new HashMap<Class, Collection<PropertyInfo>>();
                addBeanLoaderMapEntry(BUNDLE_BASED_MAP, BundleProfile.class, BundleFeeXInfo.ID);
                addBeanLoaderMapEntry(BUNDLE_BASED_MAP, com.redknee.app.crm.bean.AdjustmentType.class, BundleProfileXInfo.ADJUSTMENT_TYPE);
                addBeanLoaderMapEntry(BUNDLE_BASED_MAP, com.redknee.app.crm.bean.core.AdjustmentType.class, BundleProfileXInfo.ADJUSTMENT_TYPE);
                addBeanLoaderMapEntry(BUNDLE_BASED_MAP, CRMSpid.class, BundleProfileXInfo.SPID);
                addBeanLoaderMapEntry(BUNDLE_BASED_MAP, InvoiceSpid.class, BundleProfileXInfo.SPID);
                addBeanLoaderMapEntry(BUNDLE_BASED_MAP, Spid.class, BundleProfileXInfo.SPID);
                mergeBeanLoaderMaps(BUNDLE_BASED_MAP, getSpidBasedMap());
                BUNDLE_BASED_MAP = getUnmodifiableBeanLoaderMap(BUNDLE_BASED_MAP);
            }
        }
        return BUNDLE_BASED_MAP;
    }

    @Override
    public Map<Class, Collection<PropertyInfo>> getServicePackageBasedMap()
    {
        synchronized(SERVICE_PACKAGE_LOCK)
        {
            if (SERVICE_PACKAGE_BASED_MAP == null)
            {
                // Add service related lookup hierarchy
                SERVICE_PACKAGE_BASED_MAP = new HashMap<Class, Collection<PropertyInfo>>();
                addBeanLoaderMapEntry(SERVICE_PACKAGE_BASED_MAP, com.redknee.app.crm.bean.ServicePackage.class, com.redknee.app.crm.bean.ServicePackageFeeXInfo.PACKAGE_ID);
                addBeanLoaderMapEntry(SERVICE_PACKAGE_BASED_MAP, com.redknee.app.crm.bean.AdjustmentType.class, ServicePackageXInfo.ADJUSTMENT_CODE);
                addBeanLoaderMapEntry(SERVICE_PACKAGE_BASED_MAP, com.redknee.app.crm.bean.core.AdjustmentType.class, ServicePackageXInfo.ADJUSTMENT_CODE);
                addBeanLoaderMapEntry(SERVICE_PACKAGE_BASED_MAP, CRMSpid.class, ServicePackageXInfo.SPID);
                addBeanLoaderMapEntry(SERVICE_PACKAGE_BASED_MAP, InvoiceSpid.class, ServicePackageXInfo.SPID);
                addBeanLoaderMapEntry(SERVICE_PACKAGE_BASED_MAP, Spid.class, ServicePackageXInfo.SPID);
                mergeBeanLoaderMaps(SERVICE_PACKAGE_BASED_MAP, getSpidBasedMap());
                SERVICE_PACKAGE_BASED_MAP = getUnmodifiableBeanLoaderMap(SERVICE_PACKAGE_BASED_MAP);
            }
        }
        return SERVICE_PACKAGE_BASED_MAP;
    }

    public Map<Class, Collection<PropertyInfo>> getAudiLoadSubscriberBasedMap()
    {
        synchronized(AUDI_LOAD_SUBSCRIBER_LOCK)
        {
            if (AUDI_LOAD_SUBSCRIBER_BASED_MAP == null)
            {
                // Add audi load subscriber related lookup hierarchy
                AUDI_LOAD_SUBSCRIBER_BASED_MAP = new HashMap<Class, Collection<PropertyInfo>>();
                addBeanLoaderMapEntry(AUDI_LOAD_SUBSCRIBER_BASED_MAP, AccountRole.class, AudiLoadSubscriberXInfo.ACCOUNT_ROLE);
                addBeanLoaderMapEntry(AUDI_LOAD_SUBSCRIBER_BASED_MAP, AccountCategory.class, AudiLoadSubscriberXInfo.ACCOUNT_TYPE);
                addBeanLoaderMapEntry(AUDI_LOAD_SUBSCRIBER_BASED_MAP, Account.class, AudiLoadSubscriberXInfo.BAN);
                addBeanLoaderMapEntry(AUDI_LOAD_SUBSCRIBER_BASED_MAP, CreditCategory.class, AudiLoadSubscriberXInfo.CREDIT_CATEGORY);
                addBeanLoaderMapEntry(AUDI_LOAD_SUBSCRIBER_BASED_MAP, com.redknee.app.crm.bean.core.CreditCategory.class, AudiLoadSubscriberXInfo.CREDIT_CATEGORY);
                addBeanLoaderMapEntry(AUDI_LOAD_SUBSCRIBER_BASED_MAP, com.redknee.app.crm.bean.ui.CreditCategory.class, AudiLoadSubscriberXInfo.CREDIT_CATEGORY);
                addBeanLoaderMapEntry(AUDI_LOAD_SUBSCRIBER_BASED_MAP, DealerCode.class, AudiLoadSubscriberXInfo.DEALER_CODE);
                addBeanLoaderMapEntry(AUDI_LOAD_SUBSCRIBER_BASED_MAP, DiscountClass.class, AudiLoadSubscriberXInfo.DISCOUNT_CLASS);
                addBeanLoaderMapEntry(AUDI_LOAD_SUBSCRIBER_BASED_MAP, com.redknee.app.crm.bean.Msisdn.class, AudiLoadSubscriberXInfo.MSISDN);
                addBeanLoaderMapEntry(AUDI_LOAD_SUBSCRIBER_BASED_MAP, com.redknee.app.crm.bean.core.Msisdn.class, AudiLoadSubscriberXInfo.MSISDN);
                addBeanLoaderMapEntry(AUDI_LOAD_SUBSCRIBER_BASED_MAP, com.redknee.app.crm.bean.custom.Msisdn.class, AudiLoadSubscriberXInfo.MSISDN);
                addBeanLoaderMapEntry(AUDI_LOAD_SUBSCRIBER_BASED_MAP, com.redknee.app.crm.bean.ui.Msisdn.class, AudiLoadSubscriberXInfo.MSISDN);
                addBeanLoaderMapEntry(AUDI_LOAD_SUBSCRIBER_BASED_MAP, PricePlan.class, AudiLoadSubscriberXInfo.PRICE_PLAN);
                addBeanLoaderMapEntry(AUDI_LOAD_SUBSCRIBER_BASED_MAP, Province.class, AudiLoadSubscriberXInfo.PROVINCE);
                addBeanLoaderMapEntry(AUDI_LOAD_SUBSCRIBER_BASED_MAP, Spid.class, AudiLoadSubscriberXInfo.SPID);
                addBeanLoaderMapEntry(AUDI_LOAD_SUBSCRIBER_BASED_MAP, CRMSpid.class, AudiLoadSubscriberXInfo.SPID);
                addBeanLoaderMapEntry(AUDI_LOAD_SUBSCRIBER_BASED_MAP, com.redknee.app.crm.bean.account.SubscriptionClass.class, AudiLoadSubscriberXInfo.SUBSCRIPTION_CLASS);
                addBeanLoaderMapEntry(AUDI_LOAD_SUBSCRIBER_BASED_MAP, com.redknee.app.crm.bean.core.SubscriptionClass.class, AudiLoadSubscriberXInfo.SUBSCRIPTION_CLASS);
                addBeanLoaderMapEntry(AUDI_LOAD_SUBSCRIBER_BASED_MAP, com.redknee.app.crm.bean.account.SubscriptionType.class, AudiLoadSubscriberXInfo.SUBSCRIPTION_TYPE);
                addBeanLoaderMapEntry(AUDI_LOAD_SUBSCRIBER_BASED_MAP, com.redknee.app.crm.bean.core.SubscriptionType.class, AudiLoadSubscriberXInfo.SUBSCRIPTION_TYPE);
                mergeBeanLoaderMaps(AUDI_LOAD_SUBSCRIBER_BASED_MAP, getAccountBasedMap());
                mergeBeanLoaderMaps(AUDI_LOAD_SUBSCRIBER_BASED_MAP, getSpidBasedMap());
                mergeBeanLoaderMaps(AUDI_LOAD_SUBSCRIBER_BASED_MAP, getSubscriberBasedMap());
                AUDI_LOAD_SUBSCRIBER_BASED_MAP = getUnmodifiableBeanLoaderMap(AUDI_LOAD_SUBSCRIBER_BASED_MAP);
            }
        }
        return AUDI_LOAD_SUBSCRIBER_BASED_MAP;
    }

    public Map<Class, Collection<PropertyInfo>> getAudiUpdateSubscriberBasedMap()
    {
        synchronized(AUDI_UPDATE_SUBSCRIBER_LOCK)
        {
            if (AUDI_UPDATE_SUBSCRIBER_BASED_MAP == null)
            {
                // Add audi update subscriber related lookup hierarchy
                AUDI_UPDATE_SUBSCRIBER_BASED_MAP = new HashMap<Class, Collection<PropertyInfo>>();
                addBeanLoaderMapEntry(AUDI_UPDATE_SUBSCRIBER_BASED_MAP, CreditCategory.class, AudiUpdateSubscriberXInfo.CREDIT_CATEGORY);
                addBeanLoaderMapEntry(AUDI_UPDATE_SUBSCRIBER_BASED_MAP, DealerCode.class, AudiUpdateSubscriberXInfo.DEALER_CODE);
                addBeanLoaderMapEntry(AUDI_UPDATE_SUBSCRIBER_BASED_MAP, DiscountClass.class, AudiUpdateSubscriberXInfo.DISCOUNT_CLASS);
                addBeanLoaderMapEntry(AUDI_UPDATE_SUBSCRIBER_BASED_MAP, MarketingCampaignBean.class, AudiUpdateSubscriberXInfo.MARKETING_CAMPAIGN);
                addBeanLoaderMapEntry(AUDI_UPDATE_SUBSCRIBER_BASED_MAP, com.redknee.app.crm.bean.Msisdn.class, AudiUpdateSubscriberXInfo.MSISDN);
                addBeanLoaderMapEntry(AUDI_UPDATE_SUBSCRIBER_BASED_MAP, com.redknee.app.crm.bean.core.Msisdn.class, AudiUpdateSubscriberXInfo.MSISDN);
                addBeanLoaderMapEntry(AUDI_UPDATE_SUBSCRIBER_BASED_MAP, com.redknee.app.crm.bean.custom.Msisdn.class, AudiUpdateSubscriberXInfo.MSISDN);
                addBeanLoaderMapEntry(AUDI_UPDATE_SUBSCRIBER_BASED_MAP, com.redknee.app.crm.bean.ui.Msisdn.class, AudiUpdateSubscriberXInfo.MSISDN);
                addBeanLoaderMapEntry(AUDI_UPDATE_SUBSCRIBER_BASED_MAP, PricePlan.class, AudiUpdateSubscriberXInfo.PRICE_PLAN);
                addBeanLoaderMapEntry(AUDI_UPDATE_SUBSCRIBER_BASED_MAP, Province.class, AudiUpdateSubscriberXInfo.PROVINCE);
                AUDI_UPDATE_SUBSCRIBER_BASED_MAP = getUnmodifiableBeanLoaderMap(AUDI_UPDATE_SUBSCRIBER_BASED_MAP);
            }
        }
        return AUDI_UPDATE_SUBSCRIBER_BASED_MAP;
    }

    public Map<Class, Collection<PropertyInfo>> getConvertAccountBillingTypeRequestBasedMap()
    {
        synchronized(CONVERT_ACCOUNT_BILLING_TYPE_REQUEST_LOCK)
        {
            if (CONVERT_ACCOUNT_BILLING_TYPE_REQUEST_BASED_MAP == null)
            {
                // Add account conversion related lookup hierarchy
                CONVERT_ACCOUNT_BILLING_TYPE_REQUEST_BASED_MAP = new HashMap<Class, Collection<PropertyInfo>>();
                addBeanLoaderMapEntry(CONVERT_ACCOUNT_BILLING_TYPE_REQUEST_BASED_MAP, com.redknee.app.crm.bean.BillCycle.class, ConvertAccountBillingTypeRequestXInfo.BILL_CYCLE_ID);
                addBeanLoaderMapEntry(CONVERT_ACCOUNT_BILLING_TYPE_REQUEST_BASED_MAP, com.redknee.app.crm.bean.core.BillCycle.class, ConvertAccountBillingTypeRequestXInfo.BILL_CYCLE_ID);
                addBeanLoaderMapEntry(CONVERT_ACCOUNT_BILLING_TYPE_REQUEST_BASED_MAP, Account.class, ConvertAccountBillingTypeRequestXInfo.EXISTING_BAN);
                addBeanLoaderMapEntry(CONVERT_ACCOUNT_BILLING_TYPE_REQUEST_BASED_MAP, Occupation.class, ConvertAccountBillingTypeRequestXInfo.OCCUPATION);
                addBeanLoaderMapEntry(CONVERT_ACCOUNT_BILLING_TYPE_REQUEST_BASED_MAP, PricePlan.class, ConvertAccountBillingTypeRequestXInfo.PRICE_PLAN);
                addBeanLoaderMapEntry(CONVERT_ACCOUNT_BILLING_TYPE_REQUEST_BASED_MAP, com.redknee.app.crm.bean.account.SubscriptionClass.class, ConvertAccountBillingTypeRequestXInfo.SUBSCRIPTION_CLASS);
                addBeanLoaderMapEntry(CONVERT_ACCOUNT_BILLING_TYPE_REQUEST_BASED_MAP, com.redknee.app.crm.bean.core.SubscriptionClass.class, ConvertAccountBillingTypeRequestXInfo.SUBSCRIPTION_CLASS);
                mergeBeanLoaderMaps(CONVERT_ACCOUNT_BILLING_TYPE_REQUEST_BASED_MAP, getAccountBasedMap());
                CONVERT_ACCOUNT_BILLING_TYPE_REQUEST_BASED_MAP = getUnmodifiableBeanLoaderMap(CONVERT_ACCOUNT_BILLING_TYPE_REQUEST_BASED_MAP);
            }
        }
        return CONVERT_ACCOUNT_BILLING_TYPE_REQUEST_BASED_MAP;
    }

    public Map<Class, Collection<PropertyInfo>> getConvertSubscriptionBillingTypeRequestBasedMap()
    {
        synchronized(CONVERT_SUBSCRIPTION_BILLING_TYPE_REQUEST_LOCK)
        {
            if (CONVERT_SUBSCRIPTION_BILLING_TYPE_REQUEST_BASED_MAP == null)
            {
                // Add subscription conversion related lookup hierarchy
                CONVERT_SUBSCRIPTION_BILLING_TYPE_REQUEST_BASED_MAP = new HashMap<Class, Collection<PropertyInfo>>();
                addBeanLoaderMapEntry(CONVERT_SUBSCRIPTION_BILLING_TYPE_REQUEST_BASED_MAP, Account.class, ConvertSubscriptionBillingTypeRequestXInfo.NEW_BAN);
                addBeanLoaderMapEntry(CONVERT_SUBSCRIPTION_BILLING_TYPE_REQUEST_BASED_MAP, Subscriber.class, ConvertSubscriptionBillingTypeRequestXInfo.OLD_SUBSCRIPTION_ID);
                addBeanLoaderMapEntry(CONVERT_SUBSCRIPTION_BILLING_TYPE_REQUEST_BASED_MAP, PricePlan.class, ConvertSubscriptionBillingTypeRequestXInfo.PRICE_PLAN);
                addBeanLoaderMapEntry(CONVERT_SUBSCRIPTION_BILLING_TYPE_REQUEST_BASED_MAP, com.redknee.app.crm.bean.account.SubscriptionClass.class, ConvertSubscriptionBillingTypeRequestXInfo.SUBSCRIPTION_CLASS);
                addBeanLoaderMapEntry(CONVERT_SUBSCRIPTION_BILLING_TYPE_REQUEST_BASED_MAP, com.redknee.app.crm.bean.core.SubscriptionClass.class, ConvertSubscriptionBillingTypeRequestXInfo.SUBSCRIPTION_CLASS);
                mergeBeanLoaderMaps(CONVERT_SUBSCRIPTION_BILLING_TYPE_REQUEST_BASED_MAP, getAccountBasedMap());
                mergeBeanLoaderMaps(CONVERT_SUBSCRIPTION_BILLING_TYPE_REQUEST_BASED_MAP, getSubscriberBasedMap());
                CONVERT_SUBSCRIPTION_BILLING_TYPE_REQUEST_BASED_MAP = getUnmodifiableBeanLoaderMap(CONVERT_SUBSCRIPTION_BILLING_TYPE_REQUEST_BASED_MAP);
            }
        }
        return CONVERT_SUBSCRIPTION_BILLING_TYPE_REQUEST_BASED_MAP;
    }

    public Map<Class, Collection<PropertyInfo>> getAccountMoveRequestBasedMap()
    {
        synchronized(ACCOUNT_MOVE_REQUEST_LOCK)
        {
            if (ACCOUNT_MOVE_REQUEST_BASED_MAP == null)
            {
                // Add account move related lookup hierarchy
                ACCOUNT_MOVE_REQUEST_BASED_MAP = new HashMap<Class, Collection<PropertyInfo>>();
                addBeanLoaderMapEntry(ACCOUNT_MOVE_REQUEST_BASED_MAP, Account.class, AccountMoveRequestXInfo.EXISTING_BAN);
                mergeBeanLoaderMaps(ACCOUNT_MOVE_REQUEST_BASED_MAP, getAccountBasedMap());
                ACCOUNT_MOVE_REQUEST_BASED_MAP = getUnmodifiableBeanLoaderMap(ACCOUNT_MOVE_REQUEST_BASED_MAP);
            }
        }
        return ACCOUNT_MOVE_REQUEST_BASED_MAP;
    }

    public Map<Class, Collection<PropertyInfo>> getAccountExtensionMoveRequestBasedMap()
    {
        synchronized(ACCOUNT_EXTENSION_MOVE_REQUEST_LOCK)
        {
            if (ACCOUNT_EXTENSION_MOVE_REQUEST_BASED_MAP == null)
            {
                // Add account extension move related lookup hierarchy
                ACCOUNT_EXTENSION_MOVE_REQUEST_BASED_MAP = new HashMap<Class, Collection<PropertyInfo>>();
                addBeanLoaderMapEntry(ACCOUNT_EXTENSION_MOVE_REQUEST_BASED_MAP, Account.class, AccountExtensionMoveRequestXInfo.NEW_BAN);
                mergeBeanLoaderMaps(ACCOUNT_EXTENSION_MOVE_REQUEST_BASED_MAP, getAccountBasedMap());
                ACCOUNT_EXTENSION_MOVE_REQUEST_BASED_MAP = getUnmodifiableBeanLoaderMap(ACCOUNT_EXTENSION_MOVE_REQUEST_BASED_MAP);
            }
        }
        return ACCOUNT_EXTENSION_MOVE_REQUEST_BASED_MAP;
    }

    public Map<Class, Collection<PropertyInfo>> getSubscriptionMoveRequestBasedMap()
    {
        synchronized(SUBSCRIPTION_MOVE_REQUEST_LOCK)
        {
            if (SUBSCRIPTION_MOVE_REQUEST_BASED_MAP == null)
            {
                // Add subscription move related lookup hierarchy
                SUBSCRIPTION_MOVE_REQUEST_BASED_MAP = new HashMap<Class, Collection<PropertyInfo>>();
                addBeanLoaderMapEntry(SUBSCRIPTION_MOVE_REQUEST_BASED_MAP, Account.class, SubscriptionMoveRequestXInfo.NEW_BAN);
                addBeanLoaderMapEntry(SUBSCRIPTION_MOVE_REQUEST_BASED_MAP, Subscriber.class, SubscriptionMoveRequestXInfo.OLD_SUBSCRIPTION_ID);
                mergeBeanLoaderMaps(SUBSCRIPTION_MOVE_REQUEST_BASED_MAP, getAccountBasedMap());
                mergeBeanLoaderMaps(SUBSCRIPTION_MOVE_REQUEST_BASED_MAP, getSubscriberBasedMap());
                SUBSCRIPTION_MOVE_REQUEST_BASED_MAP = getUnmodifiableBeanLoaderMap(SUBSCRIPTION_MOVE_REQUEST_BASED_MAP);
            }
        }
        return SUBSCRIPTION_MOVE_REQUEST_BASED_MAP;
    }

    // Pre-defined bean loader maps
    private static Object SPID_LOCK = new Object();
    private static Map<Class, Collection<PropertyInfo>> SPID_BASED_MAP = null;

    private static Object ACCOUNT_LOCK = new Object();
    private static Map<Class, Collection<PropertyInfo>> ACCOUNT_BASED_MAP = null;

    private static Object INVOICE_LOCK = new Object();
    private static Map<Class, Collection<PropertyInfo>> INVOICE_BASED_MAP = null;

    private static Object SUBSCRIPTION_LOCK = new Object();
    private static Map<Class, Collection<PropertyInfo>> SUBSCRIPTION_BASED_MAP = null;

    private static Object SERVICE_LOCK = new Object();
    private static Map<Class, Collection<PropertyInfo>> SERVICE_BASED_MAP = null;

    private static Object AUXILIARY_SERVICE_LOCK = new Object();
    private static Map<Class, Collection<PropertyInfo>> AUXILIARY_SERVICE_BASED_MAP = null;

    private static Object SUBSCRIBER_AUXILIARY_SERVICE_LOCK = new Object();
    private static Map<Class, Collection<PropertyInfo>> SUBSCRIBER_AUXILIARY_SERVICE_BASED_MAP = null;

    private static Object BUNDLE_LOCK = new Object();
    private static Map<Class, Collection<PropertyInfo>> BUNDLE_BASED_MAP = null;

    private static Object SERVICE_PACKAGE_LOCK = new Object();
    private static Map<Class, Collection<PropertyInfo>> SERVICE_PACKAGE_BASED_MAP = null;

    private static Object TRANSFER_DISPUTE_LOCK = new Object();
    private static Map<Class, Collection<PropertyInfo>> TRANSFER_DISPUTE_BASED_MAP = null;

    private static Object TRANSACTION_LOCK = new Object();
    private static Map<Class, Collection<PropertyInfo>> TRANSACTION_BASED_MAP = null;

    private static Object ALCATEL_SSC_EXTENSION_SPID_LOCK = new Object();
    private static Map<Class, Collection<PropertyInfo>> ALCATEL_SSC_EXTENSION_SPID_BASED_MAP = null;

    private static Object ALCATEL_SSC_EXTENSION_SUBSCRIBER_LOCK = new Object();
    private static Map<Class, Collection<PropertyInfo>> ALCATEL_SSC_EXTENSION_SUBSCRIBER_BASED_MAP = null;

    private static Object ALCATEL_SSC_EXTENSION_SERVICE_LOCK = new Object();
    private static Map<Class, Collection<PropertyInfo>> ALCATEL_SSC_EXTENSION_SERVICE_BASED_MAP = null;

    private static Object AUDI_LOAD_SUBSCRIBER_LOCK = new Object();
    private static Map<Class, Collection<PropertyInfo>> AUDI_LOAD_SUBSCRIBER_BASED_MAP = null;

    private static Object AUDI_UPDATE_SUBSCRIBER_LOCK = new Object();
    private static Map<Class, Collection<PropertyInfo>> AUDI_UPDATE_SUBSCRIBER_BASED_MAP = null;

    private static Object CONVERT_ACCOUNT_BILLING_TYPE_REQUEST_LOCK = new Object();
    private static Map<Class, Collection<PropertyInfo>> CONVERT_ACCOUNT_BILLING_TYPE_REQUEST_BASED_MAP = null;

    private static Object CONVERT_SUBSCRIPTION_BILLING_TYPE_REQUEST_LOCK = new Object();
    private static Map<Class, Collection<PropertyInfo>> CONVERT_SUBSCRIPTION_BILLING_TYPE_REQUEST_BASED_MAP = null;

    private static Object ACCOUNT_MOVE_REQUEST_LOCK = new Object();
    private static Map<Class, Collection<PropertyInfo>> ACCOUNT_MOVE_REQUEST_BASED_MAP = null;

    private static Object ACCOUNT_EXTENSION_MOVE_REQUEST_LOCK = new Object();
    private static Map<Class, Collection<PropertyInfo>> ACCOUNT_EXTENSION_MOVE_REQUEST_BASED_MAP = null;

    private static Object SUBSCRIPTION_MOVE_REQUEST_LOCK = new Object();
    private static Map<Class, Collection<PropertyInfo>> SUBSCRIPTION_MOVE_REQUEST_BASED_MAP = null;
}
