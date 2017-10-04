/*
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
package com.trilogy.app.crm.api.rmi.support;

import org.jfree.util.Log;
import com.trilogy.app.crm.bean.ServiceFee2ID;
import java.rmi.RemoteException;
import java.security.Principal;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TimeZone;
import com.trilogy.app.crm.subscriber.charge.support.ServiceChargingSupport;
import org.omg.CORBA.LongHolder;
import com.trilogy.app.crm.support.BooleanHolder;
import com.trilogy.app.crm.Common;
import com.trilogy.app.crm.LicenseConstants;
import com.trilogy.app.crm.api.Constants;
import com.trilogy.app.crm.api.rmi.AuxiliaryServiceToPricePlanOptionAdapter;
import com.trilogy.app.crm.api.rmi.GenericParameterParser;
import com.trilogy.app.crm.api.rmi.SubscriberToApiAdapter;
import com.trilogy.app.crm.api.rmi.impl.SubscribersImpl;
import com.trilogy.app.crm.bas.tps.ServiceActivationTemplate;
import com.trilogy.app.crm.bas.tps.ServiceActivationTemplateXInfo;
import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountStateEnum;
import com.trilogy.app.crm.bean.AuxiliaryService;
import com.trilogy.app.crm.bean.AuxiliaryServiceTypeEnum;
import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.ChargedItemTypeEnum;
import com.trilogy.app.crm.bean.FixedIntervalTypeEnum;
import com.trilogy.app.crm.bean.NotificationMethodEnum;
import com.trilogy.app.crm.bean.OcgGenericParameterHolder;
import com.trilogy.app.crm.bean.OneTimeTypeEnum;
import com.trilogy.app.crm.bean.OverdraftBalanceLimit;
import com.trilogy.app.crm.bean.OverdraftBalanceLimitXInfo;
import com.trilogy.app.crm.bean.ServicePeriodEnum;
import com.trilogy.app.crm.bean.ServicePreferenceEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberAuxiliaryService;
import com.trilogy.app.crm.bean.SubscriberHome;
import com.trilogy.app.crm.bean.SubscriberServices;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.bean.SubscriberXInfo;
import com.trilogy.app.crm.bean.SystemNoteSubTypeEnum;
import com.trilogy.app.crm.bean.SystemNoteTypeEnum;
import com.trilogy.app.crm.bean.URCSGenericParameterHolder;
import com.trilogy.app.crm.bean.core.BundleFee;
import com.trilogy.app.crm.bean.core.BundleProfile;
import com.trilogy.app.crm.bean.core.Msisdn;
import com.trilogy.app.crm.bean.core.MsisdnMgmtHistory;
import com.trilogy.app.crm.bean.core.PricePlanVersion;
import com.trilogy.app.crm.bean.core.ServiceFee2;
import com.trilogy.app.crm.bean.service.HistoryEventTypeEnum;
import com.trilogy.app.crm.bean.service.ServiceStateEnum;
import com.trilogy.app.crm.bean.service.SubscriberSubscriptionHistory;
import com.trilogy.app.crm.bean.ui.Service;
import com.trilogy.app.crm.bundle.BundleManagerPipelineConstants;
import com.trilogy.app.crm.bundle.InvalidBundleApiException;
import com.trilogy.app.crm.bundle.SubscriberBundleSupport;
import com.trilogy.app.crm.bundle.exception.BundleDoesNotExistsException;
import com.trilogy.app.crm.client.AppOcgClient;
import com.trilogy.app.crm.client.RemoteServiceException;
import com.trilogy.app.crm.client.urcs.BundleTopupResponse;
import com.trilogy.app.crm.contract.SubscriptionContract;
import com.trilogy.app.crm.contract.SubscriptionContractSupport;
import com.trilogy.app.crm.contract.SubscriptionContractTerm;
import com.trilogy.app.crm.contract.SubscriptionContractTermXInfo;
import com.trilogy.app.crm.contract.SubscriptionContractXInfo;
import com.trilogy.app.crm.defaultvalue.BooleanValue;
import com.trilogy.app.crm.defaultvalue.IntValue;
import com.trilogy.app.crm.defaultvalue.LongValue;
import com.trilogy.app.crm.exception.codemapping.S2100ReturnCodeMsgMapping;
import com.trilogy.app.crm.extension.Extension;
import com.trilogy.app.crm.extension.subscriber.OverdraftBalanceSubExtension;
import com.trilogy.app.crm.extension.subscriber.OverdraftBalanceSubExtensionXInfo;
import com.trilogy.app.crm.factory.SubscriberFactory;
import com.trilogy.app.crm.home.sub.SubscriberNoteSupport;
import com.trilogy.app.crm.log.ERLogger;
import com.trilogy.app.crm.move.MoveConstants;
import com.trilogy.app.crm.move.MoveManager;
import com.trilogy.app.crm.move.MoveRequest;
import com.trilogy.app.crm.util.*;
import com.trilogy.app.crm.move.request.AbstractConvertSubscriptionBillingTypeRequest;
import com.trilogy.app.crm.move.request.ConvertSubscriptionBillingTypeRequest;
import com.trilogy.app.crm.move.support.MoveRequestSupport;
import com.trilogy.app.crm.numbermgn.GenericPackage;
import com.trilogy.app.crm.numbermgn.MsisdnManagement;
import com.trilogy.app.crm.numbermgn.MsisdnMgmtHistoryHome;
import com.trilogy.app.crm.numbermgn.MsisdnMgmtHistoryXInfo;
import com.trilogy.app.crm.secondarybalance.CategoryIdBalanceMapper;
import com.trilogy.app.crm.subscriber.charge.support.BundleChargingSupport;
import com.trilogy.app.crm.subscriber.charge.support.TransactionResultHolder;
import com.trilogy.app.crm.subscriber.provision.TFAAuxServiceSupport;
import com.trilogy.app.crm.subscriber.state.SubscriberStateTransitionSupport;
import com.trilogy.app.crm.subscriber.subscription.history.SubscriberSubscriptionHistorySupport;
import com.trilogy.app.crm.support.AuthSupport;
import com.trilogy.app.crm.support.AuxiliaryServiceSupport;
import com.trilogy.app.crm.support.BundleSupportHelper;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.app.crm.support.ExtensionSupportHelper;
import com.trilogy.app.crm.support.HomeSupport;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.app.crm.support.LicensingSupportHelper;
import com.trilogy.app.crm.support.PricePlanSupport;
import com.trilogy.app.crm.support.SpidSupport;
import com.trilogy.app.crm.support.SubscriberAuxiliaryServiceSupport;
import com.trilogy.app.crm.support.SubscriberServicesSupport;
import com.trilogy.app.crm.support.SubscriberSupport;
import com.trilogy.app.crm.support.SuspendedEntitySupport;
import com.trilogy.app.crm.technology.TechnologyEnum;
import com.trilogy.app.crm.util.SubscriberServicesUtil;
import com.trilogy.app.crm.subscriber.charge.ChargableItemResult;
import com.trilogy.app.crm.xhome.home.*;
//import com.trilogy.framework.core.socket.shell.Cal;
import  com.redknee.app.crm.bean.*;
import com.trilogy.app.crm.support.*;

import com.trilogy.app.crm.support.Lookup;
import com.trilogy.framework.xhome.auth.SimplePermission;
import com.trilogy.framework.xhome.beans.FieldValueTooLongException;
import com.trilogy.framework.xhome.beans.PatternMismatchException;
import com.trilogy.framework.xhome.beans.SafetyUtil;
import com.trilogy.framework.xhome.beans.XBeans;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.NEQ;
import com.trilogy.framework.xhome.elang.Or;
import com.trilogy.framework.xhome.elang.True;
import com.trilogy.framework.xhome.filter.Predicate;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.msp.Spid;
import com.trilogy.framework.xhome.msp.SpidAwareHomePredicate;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.InfoLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MinorLogMsg;
import com.trilogy.product.bundle.manager.provision.common.param.Parameter;
import com.trilogy.product.bundle.manager.provision.common.param.ParameterID;
import com.trilogy.product.bundle.manager.provision.common.param.ParameterValue;
import com.trilogy.product.bundle.manager.provision.v5_0.bucket.error.ErrorCode;
import com.trilogy.util.crmapi.wsdl.v2_0.exception.CRMException;
import com.trilogy.util.crmapi.wsdl.v2_0.exception.ValidationExceptionEntry;
import com.trilogy.util.crmapi.wsdl.v2_0.types.GenericParameter;
import com.trilogy.util.crmapi.wsdl.v2_0.types.NotificationPreferenceTypeEnum;
import com.trilogy.util.crmapi.wsdl.v2_0.types.PaidType;
import com.trilogy.util.crmapi.wsdl.v2_0.types.PaidTypeEnum;
import com.trilogy.util.crmapi.wsdl.v2_0.types.PoolLimitStrategyEnum;
import com.trilogy.util.crmapi.wsdl.v2_0.types.ProvisioningStateType;
import com.trilogy.util.crmapi.wsdl.v2_0.types.ProvisioningStateTypeEnum;
import com.trilogy.util.crmapi.wsdl.v2_0.types.TechnologyTypeEnum;
import com.trilogy.util.crmapi.wsdl.v2_1.exception.ExceptionCode;
import com.trilogy.util.crmapi.wsdl.v2_1.types.cardpackage.CardPackage;
import com.trilogy.util.crmapi.wsdl.v2_1.types.serviceandbundle.PricePlanOptionType;
import com.trilogy.util.crmapi.wsdl.v2_1.types.serviceandbundle.PricePlanOptionTypeEnum;
import com.trilogy.util.crmapi.wsdl.v2_1.types.subscription.BillingOptionTypeEnum;
import com.trilogy.util.crmapi.wsdl.v2_1.types.subscription.MutableSubscriptionBilling;
import com.trilogy.util.crmapi.wsdl.v2_1.types.subscription.ReadOnlySubscriptionBilling;
import com.trilogy.util.crmapi.wsdl.v2_1.types.subscription.ReadOnlySubscriptionStatus;
import com.trilogy.util.crmapi.wsdl.v2_1.types.subscription.SubscriptionBilling;
import com.trilogy.util.crmapi.wsdl.v2_1.types.subscription.SubscriptionState;
import com.trilogy.util.crmapi.wsdl.v2_1.types.subscription.SubscriptionStateEnum;
import com.trilogy.util.crmapi.wsdl.v2_1.types.subscription.SubscriptionStatus;
import com.trilogy.util.crmapi.wsdl.v2_2.types.serviceandbundle.CallingGroupTypeEnum;
import com.trilogy.util.crmapi.wsdl.v2_2.types.serviceandbundle.PricePlanOptionUpdateResult;
import com.trilogy.util.crmapi.wsdl.v2_2.types.serviceandbundle.PricePlanOptionUpdateType;
import com.trilogy.util.crmapi.wsdl.v2_2.types.serviceandbundle.PricePlanOptionUpdateTypeEnum;
import com.trilogy.util.crmapi.wsdl.v2_2.types.subscription.MutableSubscriptionProfile;
import com.trilogy.util.crmapi.wsdl.v2_2.types.subscription.ReadOnlySubscriptionProfile;
import com.trilogy.util.crmapi.wsdl.v2_2.types.subscription.SubscriptionProfile;
import com.trilogy.util.crmapi.wsdl.v3_0.api.CRMExceptionFault;
import com.trilogy.util.crmapi.wsdl.v3_0.exception.CRMExceptionFactory;
import com.trilogy.util.crmapi.wsdl.v3_0.types.serviceandbundle.BundleAdjustment;
import com.trilogy.util.crmapi.wsdl.v3_0.types.serviceandbundle.BundleAdjustmentResult;
import com.trilogy.util.crmapi.wsdl.v3_0.types.serviceandbundle.PricePlan;
import com.trilogy.util.crmapi.wsdl.v3_0.types.serviceandbundle.PricePlanOption;
import com.trilogy.util.crmapi.wsdl.v3_0.types.subscription.ReadOnlySubscriptionRating;
import com.trilogy.util.crmapi.wsdl.v3_0.types.subscription.SubscriptionContractStatus;
import com.trilogy.util.crmapi.wsdl.v3_0.types.subscription.SubscriptionPricePlan;
import com.trilogy.util.crmapi.wsdl.v3_0.types.subscription.SubscriptionRating;
import com.trilogy.util.crmapi.wsdl.v3_0.types.subscription.SubscriptionReference;
import com.trilogy.util.snippet.log.Logger;
import com.trilogy.app.crm.subscriber.charge.ChargingConstants;

/**
 * Support methods for Subscriber objects in API implementation.
 *
 * @author victor.stratan@redknee.com
 */
public final class SubscribersApiSupport
{
    /**
     * Creates a new <code>SubscribersApiSupport</code> instance. This method is made
     * private to prevent instantiation of utility class.
     */
    private SubscribersApiSupport()
    {
        // empty
    }


    /**
     * Extracts a read-only subscriber profile from the CRM subscriber.
     *
     * @param subscriber
     *            Subscriber.
     * @return A read-only subscriber profile representing the provided subscriber.
     */
    public static ReadOnlySubscriptionProfile extractProfile(final Subscriber subscriber)
    {
        final ReadOnlySubscriptionProfile profile = new ReadOnlySubscriptionProfile();

        profile.setPoolLimitStrategy(PoolLimitStrategyEnum.valueOf(subscriber.getQuotaType().getIndex()));
        profile.setPoolLimit(Long.valueOf(subscriber.getQuotaLimit()));

        profile.setIdentifier(subscriber.getId());
        profile.setAccountID(subscriber.getBAN());
        profile.setSpid(Integer.valueOf(subscriber.getSpid()));
        profile.setPaidType(RmiApiSupport.convertCrmSubscriberPaidType2Api(subscriber.getSubscriberType()));
        
        profile.setSubscriptionClass(subscriber.getSubscriptionClass());
        profile.setSubscriptionType((int) subscriber.getSubscriptionType());
        
        profile.setMobileNumber(subscriber.getMSISDN());
        profile.setCardPackageID(subscriber.getPackageId());
        profile.setTechnologyType(TechnologyTypeEnum.valueOf(subscriber.getTechnology().getIndex()));
        profile.setHlrID(Long.valueOf(subscriber.getHlrId()));

        profile.setNotificationPreference(NotificationPreferenceTypeEnum.valueOf(subscriber.getNotificationMethod()));
        
        profile.setCreated(CalendarSupportHelper.get().dateToCalendar(subscriber.getDateCreated()));
        profile.setLastModified(CalendarSupportHelper.get().dateToCalendar(subscriber.getLastModified()));

        return profile;
    }
    
    private static void validateOverdraftBalanceLimit(final Context ctx, final Subscriber subscriber, final long limit, final Object caller) throws HomeException, CRMExceptionFault
    {
        if (LicensingSupportHelper.get(ctx).isLicensed(ctx, LicenseConstants.OVERDRAFT_BALANCE_LIMIT_ENFORCEMENT_LICENSE))
        {
            And filter = new And();
            filter.add(new EQ(OverdraftBalanceLimitXInfo.SPID, Integer.valueOf(subscriber.getSpid())));
            filter.add(new EQ(OverdraftBalanceLimitXInfo.LIMIT, Long.valueOf(limit)));
            filter.add(new EQ(OverdraftBalanceLimitXInfo.ENABLED, Boolean.TRUE));
            if (!HomeSupportHelper.get(ctx).hasBeans(ctx, OverdraftBalanceLimit.class, filter))
            {
                String msg = "Overdraft Balance Limit of " + limit + " not configured on BSS";
                RmiApiErrorHandlingSupport.generalException(ctx, null, msg, ExceptionCode.VALIDATION_EXCEPTION, caller);
            }
            
        }
    }

    /**
     * Creates, updates, or remove a overdraft balance limit extension in a subscriber's extensions list.
     * @param ctx
     * @param subscriber
     * @param limit
     * @param caller
     * @throws HomeException
     * @throws CRMExceptionFault
     */
    public static void handleOverdraftBalanceLimitExtensionInExtensionList(final Context ctx, final Subscriber subscriber, final long limit, final Object caller) throws HomeException, CRMExceptionFault
    {
        if (limit<0)
        {
            SubscribersApiSupport.removeOverdraftBalanceLimitExtensionFromExtensionsList(ctx, subscriber);
        }
        else
        {
            SubscribersApiSupport.addOrUpdateOverdraftBalanceLimitExtensionInExtensionList(ctx, subscriber, limit, caller);
        }
    }

    /**
     * Creates, updates, or remove a overdraft balance limit extension straight in the database.
     * @param ctx
     * @param subscriber
     * @param limit
     * @param caller
     * @throws HomeException
     * @throws CRMExceptionFault
     */
    public static void handleOverdraftBalanceLimitExtension(final Context ctx, final Subscriber subscriber, final long limit, final Object caller) throws HomeException, CRMExceptionFault
    {
        if (limit<0)
        {
            SubscribersApiSupport.removeOverdraftBalanceLimitExtension(ctx, subscriber);
        }
        else
        {
            SubscribersApiSupport.addOrUpdateOverdraftBalanceLimitExtension(ctx, subscriber, limit, caller);
        }
    }

    /**
     * Creates or updates a overdraft balance limit extension straight in the database.
     * @param ctx
     * @param subscriber
     * @param limit
     * @param caller
     * @throws HomeException
     * @throws CRMExceptionFault
     */
    public static void addOrUpdateOverdraftBalanceLimitExtension(final Context ctx, final Subscriber subscriber, final long limit, final Object caller) throws HomeException, CRMExceptionFault
    {
        validateOverdraftBalanceLimit(ctx, subscriber, limit, caller);
        
        OverdraftBalanceSubExtension extension = HomeSupportHelper.get(ctx).findBean(ctx, OverdraftBalanceSubExtension.class, new EQ(OverdraftBalanceSubExtensionXInfo.SUB_ID, subscriber.getId()));

        if (extension!=null)
        {
            extension.setLimit(limit);
            HomeSupportHelper.get(ctx).storeBean(ctx, extension);
        }
        else
        {
            try
            {
                extension = (OverdraftBalanceSubExtension) XBeans.instantiate(OverdraftBalanceSubExtension.class, ctx);
            }
            catch (final Exception e)
            {
                extension = new OverdraftBalanceSubExtension();
                LogSupport.minor(ctx, SubscribersApiSupport.class, "Exception while XBeans.instantiate(OverdraftBalanceSubExtension.class)",
                    e);
            }    
            
            extension.setBAN(subscriber.getBAN());
            extension.setSubId(subscriber.getId());
            extension.setSpid(subscriber.getSpid());
            extension.setLimit(limit);

            HomeSupportHelper.get(ctx).createBean(ctx, extension);
         }
    }

    /**
     * Removes a overdraft balance limit extension straight from the database.
     * @param ctx
     * @param subscriber
     * @throws HomeException
     */
    public static void removeOverdraftBalanceLimitExtension(final Context ctx, final Subscriber subscriber) throws HomeException
    {
        OverdraftBalanceSubExtension extension = HomeSupportHelper.get(ctx).findBean(ctx, OverdraftBalanceSubExtension.class, new EQ(OverdraftBalanceSubExtensionXInfo.SUB_ID, subscriber.getId()));

        if (extension!=null)
        {
            HomeSupportHelper.get(ctx).removeBean(ctx, extension);
        }
        
    }

    
    private static void addOrUpdateOverdraftBalanceLimitExtensionInExtensionList(final Context ctx, final Subscriber subscriber, final long limit, final Object caller) throws HomeException, CRMExceptionFault
    {
        validateOverdraftBalanceLimit(ctx, subscriber, limit, caller);
        subscriber.setOverdraftBalanceLimit(ctx, limit);
    }
    
    private static void removeOverdraftBalanceLimitExtensionFromExtensionsList(final Context ctx, final Subscriber subscriber) throws HomeException
    {
        Collection<Extension> extensions = subscriber.getExtensions();
        Iterator<Extension> iterator = extensions.iterator();
        
        while (iterator.hasNext())
        {
            Extension extension = iterator.next();
            if (extension instanceof OverdraftBalanceSubExtension)
            {
                iterator.remove();
                break;
            }
        }
        
        subscriber.setSubExtensions(ExtensionSupportHelper.get(ctx).wrapExtensions(ctx, extensions));
    }

    /**
     * Fills in a CRM subscriber from API subscriber profile.
     *
     * @param sub
     *            The CRM subscriber to be filled in.
     * @param profile
     *            API subscriber profile.
     * @param sp
     *            Service provider.
     * @param technology
     *            Technology.
     * @param isIndividual
     *            Whether this is an individual subscriber.
     * @throws CRMExceptionFault
     *             Thrown if the provided profile is invalid.
     */
    private static void fillInProfile(final Subscriber sub, final SubscriptionProfile profile, final CRMSpid sp,
        final TechnologyEnum technology, final boolean isIndividual) throws CRMExceptionFault
    {
        if (sp.isAllowToSpecifySubscriberId() && profile.getIdentifier() != null
            && profile.getIdentifier().length() > 0)
        {
            // handle ID
            try
            {
                sub.setId(profile.getIdentifier());
            }
            catch (final FieldValueTooLongException exception)
            {
                RmiApiErrorHandlingSupport.simpleValidation("SubscriptionProfile.identifier",
                    "The provided subscriber identifier has exceeded the maximum allowed length");
            }
            catch (final PatternMismatchException exception)
            {
                RmiApiErrorHandlingSupport.simpleValidation("SubscriptionProfile.identifier",
                    "The provided subscriber identifer does not conform to the allowed pattern");
            }
        }

        /*
         * this is needed because Individual subscriber will take these values from
         * Account. see Account.getSubscriber()
         */
        if (!isIndividual)
        {
            sub.setBAN(profile.getAccountID());
        }
        sub.setSpid(profile.getSpid());
        sub.setSubscriberType(RmiApiSupport.convertApiPaidType2CrmSubscriberType(profile.getPaidType()));

        sub.setSubscriptionType(profile.getSubscriptionType());
        sub.setSubscriptionClass(profile.getSubscriptionClass());

        sub.setMSISDN(profile.getMobileNumber());
        sub.setPackageId(profile.getCardPackageID());

        sub.setTechnology(technology);
        RmiApiSupport.setOptional(sub, SubscriberXInfo.HLR_ID, profile.getHlrID());
        
        fillInMutabileProfile(sub, profile);
    }


    /**
     * Fills in a subscriber from a mutable subscriber profile.
     *
     * @param sub
     *            The CRM subscriber to be filled in.
     * @param profile
     *            Mutable subscriber profile.
     * @throws CRMExceptionFault
     *             Thrown if one or more of the values provided are invalid.
     */
    private static void fillInMutabileProfile(final Subscriber sub, final MutableSubscriptionProfile profile)
        throws CRMExceptionFault
    {
        if (profile.getPoolLimitStrategy() != null)
        {
            sub.setQuotaType(RmiApiSupport.convertApiPoolLimitStrategy2Crm(profile.getPoolLimitStrategy()));
        }
        if (profile.getNotificationPreference() != null)
        {
            NotificationMethodEnum crmNotificationMethod = RmiApiSupport.convertApiNotificationPreference2Crm(profile.getNotificationPreference());
            sub.setNotificationMethod(crmNotificationMethod.getIndex());
        }
        RmiApiSupport.setOptional(sub, SubscriberXInfo.QUOTA_LIMIT, profile.getPoolLimit());
    }
    
    private static void updateLastModifiedHidden(final Subscriber sub, long time)
    {
    	sub.setLastModifiedHidden(new java.util.Date(time));
    }


    /**
     * Extracts the subscriber status.
     *
     * @param subscriber
     *            Subscriber.
     * @return Subscriber status.
     */
    public static ReadOnlySubscriptionStatus extractStatus(final Subscriber subscriber)
    {
        final ReadOnlySubscriptionStatus status = new ReadOnlySubscriptionStatus();

        status.setState(SubscriptionStateEnum.valueOf(subscriber.getStateWithExpired().getIndex()));

        status.setStartDate(CalendarSupportHelper.get().dateToCalendar(subscriber.getStartDate()));
        status.setEndDate(CalendarSupportHelper.get().dateToCalendar(subscriber.getEndDate()));
        status.setExpiryDate(CalendarSupportHelper.get().dateToCalendar(subscriber.getExpiryDate()));

        return status;
    }


    /**
     * Extracts billing information from a subscriber.
     *
     * @param subscriber
     *            Subscriber.
     * @return Subscriber billing information.
     */
    public static ReadOnlySubscriptionBilling extractBilling(final Context ctx, final Subscriber subscriber)
    {
        final ReadOnlySubscriptionBilling billing = new ReadOnlySubscriptionBilling();

        billing.setBillingLanguage(subscriber.getBillingLanguage());
        billing.setBillingOption(BillingOptionTypeEnum.valueOf(subscriber.getBillingOption().getIndex()));
        billing.setDiscountClass(Long.valueOf(subscriber.getDiscountClass()));
        billing.setMaximumBalance(Long.valueOf(subscriber.getMaxBalance()));
        billing.setMaximumRecharge(Long.valueOf(subscriber.getMaxRecharge()));
        billing.setReactivationFee(Long.valueOf(subscriber.getReactivationFee()));
        billing.setCategory(Long.valueOf(subscriber.getSubscriberCategory()));

        // Those two fields are not used anymore. For PPSM, check PPSM Supportee Extension.
        billing.setChargeToPostpaid(false);
        billing.setPostpaidSupport("");

        billing.setInitialBalance(Long.valueOf(subscriber.getInitialBalance()));
        billing.setCreditLimit(Long.valueOf(subscriber.getCreditLimit(ctx)));
        billing.setDeposit(Long.valueOf(subscriber.getDeposit(ctx)));

        billing.setDepositDate(CalendarSupportHelper.get(ctx).dateToCalendar(subscriber.getDepositDate()));
        billing.setNextDepositReleaseDate(CalendarSupportHelper.get(ctx).dateToCalendar(subscriber.getNextDepositReleaseDate()));

        return billing;
    }


    /**
     * Fills in billing information of a subscriber.
     *
     * @param sub
     *            Subscriber to be filled in.
     * @param billing
     *            Billing information.
     * @param creationTemplateID
     *            Subscriber creation template identifier.
     * @throws CRMExceptionFault
     *             Thrown if one or more of the values provided are invalid.
     */
    private static void fillInBilling(final Subscriber sub, final SubscriptionBilling billing,
        final Long creationTemplateID) throws CRMExceptionFault
    {
        if (sub.getSubscriberType() == SubscriberTypeEnum.POSTPAID)
        {
            RmiApiSupport.setOptional(sub, SubscriberXInfo.CREDIT_LIMIT, billing.getCreditLimit());
            RmiApiSupport.setOptional(sub, SubscriberXInfo.DEPOSIT, billing.getDeposit());
        }
        else
        {
            if (creationTemplateID == null)
            {
                RmiApiSupport.setOptional(sub, SubscriberXInfo.INITIAL_BALANCE, billing.getInitialBalance());
            }
        }

        fillInMutabileBilling(sub, billing, creationTemplateID);
    }


    /**
     * Fills in billing information of a subscriber.
     *
     * @param sub
     *            Subscriber to be filled in.
     * @param billing
     *            Billing information.
     * @param creationTemplateID
     *            Subscriber creation template identifier.
     * @throws CRMExceptionFault
     *             Thrown if one or more of the values provided are invalid.
     */
    private static void fillInMutabileBilling(final Subscriber sub, final MutableSubscriptionBilling billing,
        final Long creationTemplateID) throws CRMExceptionFault
    {
        if (sub.getSubscriberType() == SubscriberTypeEnum.PREPAID)
        {
            if (creationTemplateID == null)
            {
                RmiApiSupport.setOptional(sub, SubscriberXInfo.MAX_BALANCE, billing.getMaximumBalance());
                RmiApiSupport.setOptional(sub, SubscriberXInfo.MAX_RECHARGE, billing.getMaximumRecharge());
                RmiApiSupport.setOptional(sub, SubscriberXInfo.REACTIVATION_FEE, billing.getReactivationFee());
            }
        }

        if (billing.getBillingLanguage() != null && !billing.getBillingLanguage().equals(""))
        {
            sub.setBillingLanguage(billing.getBillingLanguage());
        }
        if (billing.getBillingOption() != null)
        {
            sub.setBillingOption(RmiApiSupport.convertApiBillingOption2Crm(billing.getBillingOption()));
        }
        RmiApiSupport.setOptional(sub, SubscriberXInfo.DISCOUNT_CLASS, billing.getDiscountClass());

        RmiApiSupport.setOptional(sub, SubscriberXInfo.SUBSCRIBER_CATEGORY, billing.getCategory());
    }


    /**
     * Extracts rating information from a subscriber.
     *
     * @param ctx
     *            The operating context.
     * @param subscriber
     *            Subscriber to be extracted.
     * @return Rating information of the subscriber.
     * @throws CRMException
     *             Thrown if there are problems looking up the rating information from the
     *             subscriber.
     */
    public static ReadOnlySubscriptionRating extractRating(final Context ctx, final Subscriber subscriber)
        throws CRMExceptionFault
    {
        final ReadOnlySubscriptionRating rating = new ReadOnlySubscriptionRating();

        rating.setPrimaryPricePlanID(Long.valueOf(subscriber.getPricePlan()));
        rating.setSecondaryPricePlanID(Long.valueOf(subscriber.getSecondaryPricePlan()));
        rating.setSecondaryPricePlanStart(CalendarSupportHelper.get(ctx).dateToCalendar(subscriber.getSecondaryPricePlanStartDate()));
        rating.setSecondaryPricePlanEnd(CalendarSupportHelper.get(ctx).dateToCalendar(subscriber.getSecondaryPricePlanEndDate()));

        rating.setPricePlanVersion(Long.valueOf(subscriber.getPricePlanVersion()));

        PricePlanVersion ppv;
        final String subId = subscriber.getId();
        try
        {
            ppv = subscriber.getRawPricePlanVersion(ctx);
        }
        catch (final HomeException e)
        {
            final String msg = "Unable to retrieve PPV for Subscription " + subId;
            LogSupport.debug(ctx, SubscribersApiSupport.class, msg, e);
            final CRMException exception = new CRMException();
            exception.setCode(ExceptionCode.GENERAL_EXCEPTION);
            exception.setMessage(msg + " " + e.getMessage());
            throw CRMExceptionFactory.create(exception);
        }
        final Map serviceFees = ppv.getServiceFees(ctx);
        final Collection services = SubscriberServicesSupport.getServicesEligibleForProvisioning(ctx, subId);
        for (final Iterator it = services.iterator(); it.hasNext();)
        {
            final Object id = it.next();
            final ServiceFee2 fee = (ServiceFee2) serviceFees.get(id);
            if (fee == null || fee.getServicePreference() == ServicePreferenceEnum.MANDATORY)
            {
                it.remove();
            }
        }

        final Long[] enabledServices = new Long[services.size()];
        int i = 0;
        for (final Iterator it = services.iterator(); it.hasNext();)
        {
            enabledServices[i++] = ((ServiceFee2ID)it.next()).getServiceId();
        }
        rating.setEnabledOptionalServices(enabledServices);
        rating.setEnabledOptionalPackages(new Long[0]);

        final Map<Long,BundleFee> bundlesFees;
        final Collection<Long> provBundleKeys;
        final Set<Long> mandatoryOrDefault;
        final Set<Long> optionalBundles;
        final Set<Long> auxBundles;
        {
            bundlesFees = SubscriberBundleSupport.getPricePlanBundles(ctx, subscriber);
            provBundleKeys = SubscriberBundleSupport.getProvisionedOnBundleManager(ctx, subscriber.getMsisdn(),
                    (int) subscriber.getSubscriptionType());
            mandatoryOrDefault = new HashSet<Long>();
            optionalBundles = new HashSet<Long>();
            auxBundles = new HashSet<Long>();
            for (long key : provBundleKeys)
            {
                final BundleFee fee = bundlesFees.get(key);
                if (fee == null)
                {
                    auxBundles.add(key);
                }
                else if (fee.getServicePreference() != ServicePreferenceEnum.MANDATORY)
                {
                    optionalBundles.add(key);
                }
                else
                {
                    mandatoryOrDefault.add(key);
                }
            }
        }
        rating.setEnabledOptionalBundles(optionalBundles.toArray(DUMMY_LONG_ARRAY));
        rating.setEnabledAuxiliaryBundles(auxBundles.toArray(DUMMY_LONG_ARRAY));

        final Collection<SubscriberAuxiliaryService> auxServices = subscriber.getAuxiliaryServices(ctx);
        final Long[] enabledAuxService;
        {
            enabledAuxService = new Long[auxServices.size()];
            i = 0;
            for (SubscriberAuxiliaryService auxService : auxServices)
            {
                enabledAuxService[i++] = Long.valueOf(auxService.getAuxiliaryServiceIdentifier());
            }
        }
        rating.setEnabledAuxiliaryServices(enabledAuxService);
        return rating;
    }


    /**
     * Fill in the rating information of a subscriber.
     *
     * @param ctx
     *            The operating context.
     * @param sub
     *            Subscriber to be filled in.
     * @param rating
     *            Rating information to be filled in to the subscriber.
     */
    private static void fillInRating(final Context ctx, final Subscriber sub, final SubscriptionRating rating)
            throws CRMExceptionFault
    {
        if (rating.getPrimaryPricePlanID() != null)
        {
            sub.switchPricePlan(ctx, rating.getPrimaryPricePlanID().longValue());
        }
        if (rating.getSecondaryPricePlanID() != null)
        {
            sub.setSecondaryPricePlan(rating.getSecondaryPricePlanID().longValue());
            RmiApiSupport.setOptional(sub, SubscriberXInfo.SECONDARY_PRICE_PLAN_START_DATE,
                    rating.getSecondaryPricePlanStart());
            RmiApiSupport.setOptional(sub, SubscriberXInfo.SECONDARY_PRICE_PLAN_END_DATE,
                    rating.getSecondaryPricePlanEnd());
        }
        if (rating.getContractID() != null)
        {
            try
            {
                SubscriptionContractTerm term = HomeSupportHelper.get(ctx).findBean(ctx,
                        SubscriptionContractTerm.class,
                        new EQ(SubscriptionContractTermXInfo.ID, rating.getContractID()));
                final CRMSpid sp = RmiApiSupport.getCrmServiceProvider(ctx, Integer.valueOf(sub.getSpid()), null);
                if (sp.getUseContractPricePlan())
                {
                    if (term.getContractPricePlan() != sub.getPricePlan())
                    {
                        RmiApiErrorHandlingSupport.simpleValidation("ContractId", "Contract " + rating.getContractID()
                                + " does not belong priceplan " + sub.getPricePlan());
                    }
                }
                sub.setSubscriptionContract(rating.getContractID().longValue());
            }
            catch (Exception ex)
            {
                RmiApiErrorHandlingSupport.generalException(ctx, ex, " Unable to find the contract ", sub);
            }
        }
    }


    /**
     * Constructs a CRM subscriber from API subscriber parts.
     *
     * @param ctx
     *            The operating context.
     * @param templateID
     *            Subscriber creation template identifier.
     * @param profile
     *            Main subscriber profile.
     * @param status
     *            Subscriber status.
     * @param rating
     *            Subscriber rating information.
     * @param billing
     *            Subscriber billing information.
     * @param identification
     *            Subscriber identification information.
     * @param sp
     *            Service provider.
     * @param technology
     *            Technology.
     * @return The constructed subscriber.
     * @throws RemoteException
     *             Thrown if there are problems constructing the subscriber, such as if
     *             some of the provided values are invalid.
     * @throws HomeException
     *             Thrown if there are home-related problems.
     */
    public static Subscriber constructSubscriber(final Context ctx, final Long templateID,
        final SubscriptionProfile profile, final SubscriptionStatus status, final SubscriptionRating rating,
        final SubscriptionBilling billing, final CRMSpid sp,
        final TechnologyEnum technology, SubscriptionPricePlan options) throws CRMExceptionFault, HomeException
    {
        final Subscriber sub = createSubscriber(ctx);

        fillInIndividualSubscriber(ctx, profile, status, rating, billing, sub, sp, technology, templateID, false, options);
        return sub;
    }


    /**
     * Constructs an individual CRM subscriber from API subscriber parts.
     *
     * @param ctx
     *            The operating context.
     * @param templateID
     *            Subscriber creation template identifier.
     * @param profile
     *            Main subscriber profile.
     * @param status
     *            Subscriber status.
     * @param rating
     *            Subscriber rating information.
     * @param billing
     *            Subscriber billing information.
     * @param sp
     *            Service provider.
     * @param technology
     *            Technology.
     * @return The constructed subscriber.
     * @throws HomeException
     *             Thrown if there are home-related problems.
     * @throws CRMExceptionFault
     *             Thrown if one or more of the provided values are invalid.
     */
    public static Subscriber constructIndividualSubscriber(final Context ctx, final Long templateID,
        final SubscriptionProfile profile, final SubscriptionStatus status, final SubscriptionRating rating,
        final SubscriptionBilling billing, final CRMSpid sp, final TechnologyEnum technology, SubscriptionPricePlan options) throws HomeException,
        CRMExceptionFault
    {
        final Subscriber sub = createSubscriber(ctx);
        sub.setContext(ctx);
        //Setting the start date to null so that it doesn't set startDate to the currentDate
        // which causes automatic activation when the activate/expiry process runs daily
        sub.setStartDate(null);

        fillInIndividualSubscriber(ctx, profile, status, rating, billing, sub, sp, technology, templateID, true, options);

        return sub;
    }


    /**
     * Fills in an individual CRM subscriber from API subscriber parts.
     *
     * @param ctx
     *            The operating context.
     * @param templateID
     *            Subscriber creation template identifier.
     * @param profile
     *            Main subscriber profile.
     * @param status
     *            Subscriber status.
     * @param rating
     *            Subscriber rating information.
     * @param billing
     *            Subscriber billing information.
     * @param sub
     *            Subscriber to be filled in.
     * @param sp
     *            Service provider.
     * @param technology
     *            Technology.
     * @param isIndividual
     *            Whether this is an individual subscriber.
     * @throws HomeException
     *             Thrown if there are home-related problems.
     * @throws CRMExceptionFault
     *             Thrown if one or more of the provided values are invalid.
     */
    private static void fillInIndividualSubscriber(final Context ctx, final SubscriptionProfile profile,
        final SubscriptionStatus status, final SubscriptionRating rating, final SubscriptionBilling billing,
        final Subscriber sub, final CRMSpid sp, final TechnologyEnum technology, final Long templateID,
        final boolean isIndividual,  SubscriptionPricePlan options) throws HomeException, CRMExceptionFault
    {
        fillInProfile(sub, profile, sp, technology, isIndividual);

        sub.setState(RmiApiSupport.convertApiSubscriberState2Crm(status.getState()));
        if (SubscriberTypeEnum.PREPAID.equals(sub.getSubscriberType()) && templateID != null)
        {
            final long satId = templateID.longValue();
            SubscriberSupport.applySubServiceActivationTemplate(ctx, sub, satId);
        }
        else
        {
            fillInRating(ctx, sub, rating);
        }
        if (options != null)
        {
            updatePricePlanOptions(ctx, sub, options, true);
        }

        fillInBilling(sub, billing, templateID);
        fillInUsingSubscriberAccount(ctx, sub);
    }

    /**
     * Fills Subscription using SubscriberAccount data for properties that are same in SubscriberAccoutn and Subscription
     * @param ctx
     * @param sub
     */
    private static void fillInUsingSubscriberAccount(final Context ctx, final Subscriber sub) throws HomeException
    {
        Account account = HomeSupportHelper.get(ctx).findBean(ctx, com.redknee.app.crm.bean.Account.class, sub.getBAN());
    
        if (account != null)
        {
            sub.setDealerCode(account.getDealerCode());
        }
    }

    /**
     * Updates a subscriber.
     *
     * @param ctx
     *            The operating context.
     * @param subscriber
     *            Subscriber to be updated.
     * @param profile
     *            Updated subscriber profile.
     * @param billing
     *            Updated subscriber billing information.
     * @param identification
     *            Updated subscriber identification information.
     * @throws CRMExceptionFault
     *             Thrown if one or more of the provided values are invalid.
     */
    public static void updateSubscriber(final Context ctx, final Subscriber subscriber,
        final MutableSubscriptionProfile profile, final MutableSubscriptionBilling billing) throws CRMExceptionFault
    {
        if (profile != null)
        {
            fillInMutabileProfile(subscriber, profile);
        }
        if (billing != null)
        {
            fillInMutabileBilling(subscriber, billing, null);
        }
        
        long lastModifiedHidden = ctx.getLong(Common.LAST_MODIFIED, -1);
        
        if(lastModifiedHidden != -1)
        {
        	updateLastModifiedHidden(subscriber, lastModifiedHidden);
        }
    }


    /**
     * Creates a subscriber.
     *
     * @param ctx
     *            The operating context.
     * @return The created subscriber.
     */
    private static Subscriber createSubscriber(final Context ctx)
    {
        Subscriber sub = null;
        try
        {
            sub = (Subscriber) XBeans.instantiate(Subscriber.class, ctx);
        }
        catch (final Exception e)
        {
            LogSupport.minor(ctx, SubscribersApiSupport.class, "Exception while XBeans.instantiate(Subscriber.class)",
                e);
        }

        if (sub == null)
        {
            sub = SubscriberFactory.initSubscriber(ctx, new Subscriber());
        }

        return sub;
    }

    
    /**
     * Enables a price plan option.
     * 
     * @param ctx
     *            The operating context.
     * @param subscriber
     *            Subscriber to be updated.
     * @param options
     *            The all the options that are going to be changed.
     * @throws CRMExceptionFault
     *             Thrown if one or more of the provided values are invalid.
     * @throws HomeException
     *             Thrown if there are problems enabling a price plan option.
     * @deprecated Use the other method: {@link #updatePricePlanOptions(Context, Subscriber, SubscriptionPricePlan, boolean, ApiResultHolder)}
     * which allows a result-holder that can be extended/evolved as need arises in future. 
     * @see #updatePricePlanOptions(Context, Subscriber, SubscriptionPricePlan, boolean, ApiResultHolder)
     */
    public static Subscriber updatePricePlanOptions(final Context ctx, final Subscriber subscriber,
            final SubscriptionPricePlan options, boolean onCreate) throws CRMExceptionFault, HomeException
    {
        return updatePricePlanOptions(ctx, subscriber, options, onCreate, 
                new ApiResultHolder());
    }

    
    /**
     * 
     * @param ctx
     * @param subscriber
     * @param options All the options that are going to be changed.
     * @param onCreate
     * @param resHolder A generic holder that would contain info that populated by the 
     * API method, that the caller may need later-on.
     * @return
     * @throws CRMExceptionFault Thrown if one or more of the provided values are invalid.
     * @throws HomeException Thrown if there are problems enabling a price plan option.
     */
    public static Subscriber updatePricePlanOptions(final Context ctx, final Subscriber subscriber,
            final SubscriptionPricePlan options, boolean onCreate, ApiResultHolder resHolder) 
                throws CRMExceptionFault, HomeException
    {
        if (options != null)
        {
            if (options.getIsSelected())
            {
                PricePlan pp = options.getPricePlanDetails();
                PricePlanOption[] subOptions = options.getItems();
                boolean isPPChange = false;
                if (pp != null && pp.getIdentifier() != subscriber.getPricePlan())
                {
                	isPPChange = true;
                	GenericParameter[] optionsParameters = options.getParameters();
                    GenericParameterParser optionsParamsParser = new GenericParameterParser(optionsParameters);
                	final String serviceQuantity = optionsParamsParser.getParameter(APIGenericParameterSupport.SERVICE_QUANTITY, String.class);
                	if(serviceQuantity != null)
                    {
                                ctx.put(APIGenericParameterSupport.SERVICE_QUANTITY, serviceQuantity);
                    }
                    subscriber.switchPricePlan(ctx, pp.getIdentifier());
                }
                if (subOptions != null)
                {
                	long serviceQuantity = 1;
                	String path = SubscriberServicesUtil.DEFAULT_PATH;
                	if(isPPChange){
                		for (int i = 0; i < subOptions.length; i++) {
                			PricePlanOption item = subOptions[i];
                			 if (item == null)  {
                                 continue;
                             }
                			 GenericParameterParser optionsParamsParser = new GenericParameterParser(item.getParameters());
                			 Properties prop = new Properties();
                			 String priceplanParam = optionsParamsParser.getParameter(APIGenericParameterSupport.PRICEPLAN_ID, String.class);
                			 if(priceplanParam != null){
                     			ctx.put(Lookup.PRICEPLAN_CHANGE_REQUEST_FROM_DCRM, true);
                     		}
                			 String pathParam = optionsParamsParser.getParameter(APIGenericParameterSupport.PATH, String.class);
                			 if(null != pathParam){
                     			prop.put(APIGenericParameterSupport.PATH, pathParam);
                     		}
                			 if(null != priceplanParam){
                     			prop.put(APIGenericParameterSupport.PRICEPLAN_ID, priceplanParam);
                     		} 
                		}
                	}
                    for (PricePlanOption option : subOptions)
                    {
                        if (option == null)
                        {
                            continue;
                        }
                        
                        resHolder.putPriceplanOptionUpdateApiResultsHolder(
                                option.getIdentifier(), option.getOptionType(), new ApiOptionResultHolder());
                        
                        /*
                         * Sujeet: TODO refactor to sort out nasty if-then-else running across the methods
                         * We can abstract out common code for checking each option-type (and processing) into 
                         * a PricePlanOption Processor (abstract) and let there be implementors - Enabler, 
                         * Disabler, Repurchase, ...
                         */
                        GenericParameter[] parameters = option.getParameters();
                        GenericParameterParser paramsParser = new GenericParameterParser(parameters);
                        
                        if (option.getIsSelected())
                        {
                        	if(paramsParser.containsParam(APIGenericParameterSupport.SERVICE_QUANTITY))
                            { 
                                serviceQuantity = paramsParser.getParameter(APIGenericParameterSupport.SERVICE_QUANTITY, Long.class); 
                                if (serviceQuantity < 1) {
                					if (LogSupport.isDebugEnabled(ctx)) {
                						LogSupport.debug(ctx, SubscribersApiSupport.class, "Value of serviceQuantity:: " + serviceQuantity);
                					}
                					throw new CRMExceptionFault("SubscriberServices.serviceQuantity [Value less than 1.]");
                				}
                            }
                        	if(paramsParser.containsParam(APIGenericParameterSupport.PATH))
                            { 
                                path = paramsParser.getParameter(APIGenericParameterSupport.PATH, String.class); 
                            }
                            if(isRepurchaseRequest(paramsParser))
                            {
                                repurchasePricePlanOption(ctx, subscriber, onCreate,
                                        option, paramsParser, resHolder);
                            }
                            else 
                            {
								try {
									if (ctx.has(Lookup.PRICEPLAN_CHANGE_REQUEST_FROM_DCRM)){
										final long identifier = option.getIdentifier();
										final PricePlanVersion ppv = subscriber.getRawPricePlanVersion(ctx);
										final Map serviceFees = ppv.getServicePackageVersion(ctx).getServiceFees();
										ServiceFee2ID key = new ServiceFee2ID(identifier, path);
										final ServiceFee2 serviceFee = (ServiceFee2) serviceFees.get(key);						
										if (LogSupport.isDebugEnabled(ctx)) {
											Logger.debug(ctx, SubscribersApiSupport.class,
													" PRICEPLAN_CHANGE_REQUEST_FROM_DCRM is true value :: "+identifier+" PricePlan version:: "+ppv
													+" serviceFees:: "+serviceFees+" ServiceFee2ID:: "+key+" serviceFee:: "+serviceFee
													+"option.getOptionType().getValue():: "+option.getOptionType().getValue());
										}
										if (serviceFee == null && !(option.getOptionType()
												.getValue() == PricePlanOptionTypeEnum.AUXILIARY_SERVICE.ordinal())){
											RmiApiErrorHandlingSupport.simpleValidation(
													"Service is not part of the Price Plan.",
													"Specified service is not part of the subscriber Price Plan.");
										} else if (!ctx.has(Lookup.PRICEPLAN_CHANGE_REQUEST_FROM_DCRM)||
												(option.getOptionType().getValue() == PricePlanOptionTypeEnum.AUXILIARY_SERVICE.ordinal())
												|| (serviceFee.getServicePreference() != ServicePreferenceEnum.MANDATORY)) {
											enablePricePlanOption(ctx, subscriber, onCreate, option, resHolder,
													parameters);
										}

									} else {
										if (LogSupport.isDebugEnabled(ctx)) {
											Logger.debug(ctx, SubscribersApiSupport.class,"Else condition PRICEPLAN_CHANGE_REQUEST_FROM_DCRM");
										}
										enablePricePlanOption(ctx, subscriber, onCreate, option, resHolder, parameters);
									}
								} catch (CRMExceptionFault crmExec) {
									if (LogSupport.isDebugEnabled(ctx)) {
										Logger.debug(ctx, SubscribersApiSupport.class,"Inside CRM Exception Flow and value of decrementLimitExceeded:: "+decrementLimitExceeded);
									}
									if (decrementLimitExceeded.equals(crmExec.getMessage())) {
										String ppId = paramsParser.getParameter(APIGenericParameterSupport.PRICEPLAN_ID,String.class);
										if (ppId != null
												&& (paramsParser.containsParam(APIGenericParameterSupport.PRICEPLAN_ID)
														&& Long.parseLong(ppId) != pp.getIdentifier())
												&& !SubscribersApiSupport.isServiceGettingReplaceInPPChange(ctx,
														subscriber, option.getIdentifier())) {
											 handleServiceRemoveofOldPPInPPChangeRequest(ctx, subscriber, paramsParser,
													pp, option, ppId);
										} else {
											disablePricePlanOption(ctx, subscriber, option, resHolder,
													option.getParameters());
										}
									} else {
										throw crmExec;
									}
								}

                            }
                        }
                        else
                        {
                        	String ppId = paramsParser.getParameter(APIGenericParameterSupport.PRICEPLAN_ID, String.class);
                            if(ppId !=  null && (paramsParser.containsParam(APIGenericParameterSupport.PRICEPLAN_ID) && Long.parseLong(ppId) != pp.getIdentifier()) && !SubscribersApiSupport.isServiceGettingReplaceInPPChange(ctx, subscriber, option.getIdentifier())){
                            	handleServiceRemoveofOldPPInPPChangeRequest(ctx, subscriber, paramsParser, pp, option, ppId);
                            }
                        	else {
                                disablePricePlanOption(ctx, subscriber, option, resHolder,option.getParameters());
                            }                     
                        }
                    }
                }
            }
            else
            {
                final String msg = "Not applying the options because IsSelected is false for Subscription with ID/MobileNumber/SubscriptionType = "
                        + subscriber.getId() + "/" + subscriber.getMSISDN() + "/" + subscriber.getSubscriptionType();
                RmiApiErrorHandlingSupport.handleStoreExceptions(ctx, null, msg, SubscribersApiSupport.class);
            }
        }
        if (LogSupport.isDebugEnabled(ctx)) {
			Logger.debug(ctx, SubscribersApiSupport.class,">>Exiting updatePricePlanOptions method and subscriber:: "+subscriber);
		}
        return subscriber;
    }


    /**
     * TODO Refactor all the Messages using MessageFormat instead of '+' String-constructor
     * 
     * @param ctx
     * @param subscriber
     * @param onCreate
     * @param option
     * @param paramsParser
     * @param resHolder 
     * @throws CRMExceptionFault
     */
    private static void repurchasePricePlanOption(Context ctx,
            Subscriber subscriber, boolean onCreate, PricePlanOption option,
            GenericParameterParser paramsParser, ApiResultHolder resHolder)
                    throws CRMExceptionFault
    {
        PricePlanOptionType optionType = option.getOptionType();
        ApiOptionResultHolder resH = 
            resHolder.getPriceplanOptionUpdateApiResultsHolder(option.getIdentifier(), option.getOptionType());
        resH.setOptionUpdateType(OptionUpdateType.REPURCHASE);
        
        if(!(
                optionType.getValue() == PricePlanOptionTypeEnum.BUNDLE.getValue().getValue()
            ||  optionType.getValue() == PricePlanOptionTypeEnum.AUXILIARY_BUNDLE.getValue().getValue()
        ))
        {
            final String msg = MessageFormat.format(
                    "Specified PricePlanOption with type {0} is not allowed to be re-purchased.",
                    new Object[]{Long.valueOf(optionType.getValue())});
            RmiApiErrorHandlingSupport.simpleValidation("PricePlanOption",
                        msg);
            return;
        }

        final Long key = Long.valueOf(option.getIdentifier());
        final Map<Long, BundleFee> bundles = SubscriberBundleSupport.getPricePlanBundles(ctx, subscriber);
        BundleFee fee = bundles.get(key);
        
        if (optionType.getValue() == PricePlanOptionTypeEnum.BUNDLE.getValue().getValue())
        {
            if (fee == null)
                RmiApiErrorHandlingSupport.simpleValidation("Bundle is not part of the Price Plan.",
                "Specified bundle is not part of the subscriber Price Plan.");
            
            try
            {
                BundleProfile profile = fee.getBundleProfile(ctx, subscriber.getSpid());
                validateRepurchasableBundleProfile(ctx, subscriber, profile);
                invokeRepurchaseCallFlow(ctx, subscriber, profile, fee, option, paramsParser, resHolder);
                
                /*
                 * We just save this for the sake of updating expiry. 
                 */
                subscriber.getBundles().put(key, fee);
            }
            catch (BundleDoesNotExistsException e)
            {
                RmiApiErrorHandlingSupport.simpleValidation("optionID", "Invalid bundle profile identifier.");
            }
            catch (Exception e)
            {
                RmiApiErrorHandlingSupport.simpleValidation("optionID", "Exception while validating bundle profile identifier.");
            }
        }
        else if(optionType.getValue() == PricePlanOptionTypeEnum.AUXILIARY_BUNDLE.getValue().getValue())
        {
            // The AUX bundles should not be amongst the PricePlan bundles.
            if (fee != null)
                RmiApiErrorHandlingSupport.simpleValidation("optionType",
                "Specified bundle is part of the subscriber Price Plan. Please use BUNDLE optionType.");
            
            fee = (BundleFee) subscriber.getBundles().get(key);
            if (fee == null)
            {
                // ERROR - No such AUX bundle in subscription.
                RmiApiErrorHandlingSupport.simpleValidation("OptionID",
                    "No Aux Bundle exists with ID: "+ key);
            }
            
            try
            {
                BundleProfile profile = fee.getBundleProfile(ctx, subscriber.getSpid());
                
                /*
                 * Bundle Profile can not be null (above method)
                 */
                if (!profile.isAuxiliary())
                    RmiApiErrorHandlingSupport.simpleValidation("optionID", "Specified bundle profile is not auxiliary.");
                
                validateRepurchasableBundleProfile(ctx, subscriber, profile);
                invokeRepurchaseCallFlow(ctx, subscriber, profile, fee, option, paramsParser, resHolder);
                
                /*
                 * We just save this for the sake of updating expiry. 
                 */
                subscriber.getBundles().put(key, fee);
            } 
            catch (BundleDoesNotExistsException e)
            {
                RmiApiErrorHandlingSupport.simpleValidation("optionID", "Invalid bundle profile identifier.");
            }
            catch (Exception e)
            {
                RmiApiErrorHandlingSupport.simpleValidation("optionID", "Exception while validating bundle profile identifier.");
            }
        }
    }


    /**
     * @param subscriber 
     * @param ctx 
     * @param profile
     * @throws CRMExceptionFault
     */
    private static void validateRepurchasableBundleProfile(Context ctx, Subscriber subscriber, BundleProfile profile)
            throws CRMExceptionFault
    {
        if (!profile.isEnabled())
            RmiApiErrorHandlingSupport.simpleValidation("optionID", "Bundle profile [" + profile.getBundleId() + "] is not enabled.");                        
        
        if(!profile.isRepurchasable())
            RmiApiErrorHandlingSupport.simpleValidation("optionID", "Bundle profile [" + profile.getBundleId() + 
                    "] is not marked Re-purchasable. Only one-time bundles marked 'Re-purchasable' can be repurchased.");
        
       
        if(!subscriber.getBundles().containsKey(Long.valueOf(profile.getBundleId())))
            RmiApiErrorHandlingSupport.simpleValidation("optionID", "Bundle profile [" + profile.getBundleId() + 
                "] is not Already Provisioned. Only one-time bundles that are provisioned, and marked 'Re-purchasable' can be repurchased.");
        
        /*
         * No need to verify One-time, as given the bundleprofile is marked 'Repurchasable' it must be one-time (Restriction from the GUI and API)
         */
    }


    /**
     * 
     * @param ctx
     * @param subscriber
     * @param profile The Bundle Profile instance (already fetched). Can be null.
     * @param bundleFee
     * @param option
     * @param paramsParser
     * @param resHolder 
     * @return
     * @throws CRMExceptionFault
     */
    private static int invokeRepurchaseCallFlow(Context ctx,
            Subscriber subscriber, BundleProfile profile, BundleFee bundleFee,
            PricePlanOption option, GenericParameterParser paramsParser, ApiResultHolder resHolder)  throws CRMExceptionFault
    {    
        ApiOptionResultHolder resH = 
            resHolder.getPriceplanOptionUpdateApiResultsHolder(option.getIdentifier(), option.getOptionType());
        try
        {
            if(profile==null)
                profile = bundleFee.getBundleProfile(ctx, subscriber.getSpid());
        } 
        catch (Exception e)
        {
            throw new CRMExceptionFault("Could not obtain bundle profile for bundle id: "+ bundleFee.getId(), e);
        }
        
        String msisdn = subscriber.getMsisdn();
        String msg = MessageFormat.format("Bundle top up request for Bundle-Id {0} for Subscriber {1}",
                new Object[]{Long.valueOf(profile.getBundleId()), msisdn});
        if (LogSupport.isDebugEnabled(ctx))
            LogSupport.debug(ctx, SubscribersApiSupport.class.getName(), msg);
 
        /*
         * First check if AccountOperations service is up
         */
        com.redknee.app.crm.client.urcs.AccountOperationsClientV4 topUpClient = (com.redknee.app.crm.client.urcs.AccountOperationsClientV4) ctx
            .get(com.redknee.app.crm.client.urcs.AccountOperationsClientV4.class);        
        if(!topUpClient.isAlive(ctx))
        {
            msg = MessageFormat.format(
                    "Bundle re-purchase failed for Subscriber {0}. Could not initiate balance updates as the AccountOperations service is down.",
                    new Object[]{msisdn});
            /*
             * We can't throw Soap Fault: we might have other options to process ahead.
             * RmiApiErrorHandlingSupport.generalException(ctx, null, msg, subscriber);
             */
            
            resH.setOverallResultCode(-1);
            resH.setErrorMessage(ErrorMessageConstants.ERROR_BUNDLE_TOPUP_SERVICE_DOWN);
            resH.setUrcsResultCode(-1);
            
            /*
             * TBD Update DDD with the RC for the case.
             */
            return -1;
        }        
        
        /*
         * Make debit transaction (Forward to OCG)
         */
        TransactionResultHolder tranRes = BundleChargingSupport.applyBundleRepurchaseTransaction(ctx, profile, bundleFee, subscriber, false);
        long amount = tranRes.getTransactionAmount();

        msg = MessageFormat.format(
                "Attempting to Debit Subscriber: {0}, with amount: {1}",
                new Object[]{msisdn, Long.valueOf(amount)});
        if (LogSupport.isDebugEnabled(ctx))
            LogSupport.debug(ctx, SubscribersApiSupport.class.getName(), msg);
        
        if(!tranRes.isSuccess())
        {
            msg = MessageFormat.format(
                    "Debit Transaction failed for Subscriber: {0}, for amount: {1}, for bundle repurchase of bundle-id: {2}; Txn result code: {3}",
                    new Object[]{msisdn, Long.valueOf(amount), Long.valueOf(profile.getBundleId()),
                            Integer.valueOf(tranRes.getTransactionResultCode())});
            if (LogSupport.isDebugEnabled(ctx))
                LogSupport.debug(ctx, SubscribersApiSupport.class.getName(), msg);
            
            /*
             * We can't throw SOAP fault - need to proceed with other options if any.
             * RmiApiErrorHandlingSupport.ocgTransactionException(ctx, null, 
             *      tranRes.getTransactionResultCode(), msg, SubscribersImpl.class);
             */
            
            resH.setOverallResultCode(tranRes.getTransactionResultCode());
            resH.setOcgResultCode(tranRes.getOCGResultCode());
            resH.setErrorMessage(ErrorMessageConstants.BUNDLE_TOPUP_CHARGES_DEBIT_FAILED+ tranRes.getOCGErrorMessage());
            
            /*
             * TBD Update DDD with the RC for the case.
             */
            return -1;
        } 
        
        msg = MessageFormat.format(
                "Debit Transaction successful for Subscriber: {0}, for amount: {1}, for bundle repurchase of bundle-id: {2}.",
                new Object[]{msisdn, Long.valueOf(amount), Long.valueOf(profile.getBundleId())});
        if (LogSupport.isDebugEnabled(ctx))
            LogSupport.debug(ctx, SubscribersApiSupport.class.getName(), msg);
        
        /*
         * Top up repurchasable bundle
         */
        BundleTopupResponse response = new BundleTopupResponse(-1, -1, null);
        try
        {
            response = topUpClient.topupBundle(ctx, msisdn, 
                    TimeZone.getTimeZone(subscriber.getTimeZone(ctx)), 
                        subscriber.getCurrency(ctx), profile, "AppCrm-repurchase-" + msisdn);
        }
        catch (RemoteServiceException e)
        {
            msg = MessageFormat.format(
                    "Bundle top up failed for subscriber: {0}, amount: {1}, currency: {2}, bundle: {3}",
                    new Object[]{msisdn, Long.valueOf(amount), 
                            subscriber.getCurrency(ctx), Long.valueOf(profile.getBundleId())});
            LogSupport.minor(ctx, SubscribersApiSupport.class.getName(), msg, e);
            response.setServiceResponseCode(-1);
            
            resH.setOverallResultCode(-1);
            resH.setOcgResultCode(0);
            resH.setUrcsResultCode(-1);
            resH.setErrorMessage(ErrorMessageConstants.BUNDLE_TOPUP_FAILED_URCS_FAILURE+ e.getMessage());
        }        
        
        /*
         * If bundle top up failed then roll-back (credit back subscriber)
         */
        if (response.getServiceResponseCode() != 0)
        {
            handleRollback(ctx, subscriber, profile, bundleFee, msisdn,
                    tranRes, amount, response, resH);
            
            /*
             * We can't throw SOAP fault - need to proceed with other options if any.
             * RmiApiErrorHandlingSupport.generalException(ctx, null, msg, subscriber);
             */
            
            /*
             * TBD Update DDD with the RC for the case.
             */
            return -1;
        }
        
        /*
         * Logging, notes, Charging History updates.
         */
        
        /*
         * Update Bundle expiry (if sent by URCS/CPS)
         */
        msg = MessageFormat.format(
                "Successful Repurchase; New Expiry: {0} for the bundle {1}, for subscriber {2}.",
                new Object[]{
                        response.getNewExpiryDate()!=null ? response.getNewExpiryDate().getTime() : "<no change>", 
                                Long.valueOf(profile.getBundleId()), msisdn
                        });
        if (LogSupport.isDebugEnabled(ctx))
            LogSupport.debug(ctx, SubscribersApiSupport.class.getName(),
                    msg);
        
        if(response.getNewExpiryDate() != null)
        {
            bundleFee.setRepurchaseHappened(true);
            bundleFee.setEndDate(response.getNewExpiryDate().getTime());
        }
        
        SubscriberSubscriptionHistory record = SubscriberSubscriptionHistorySupport.addChargingHistory(ctx, bundleFee, subscriber,
                HistoryEventTypeEnum.CHARGE, 
                    (bundleFee.isAuxiliarySource() ? ChargedItemTypeEnum.AUXBUNDLE : ChargedItemTypeEnum.BUNDLE), 
                        tranRes.getTransactionAmount(), tranRes.getTransactionAmount(), tranRes.getTransaction(), 
                            CalendarSupportHelper.get(ctx).getDateWithLastSecondofDay(
                                    tranRes.getTransaction().getTransDate(), subscriber.getTimeZone(ctx))
                            );
        
        //resHolder.getBundlesToppedUpSuccessfully().add(Long.valueOf(profile.getBundleId()));
        resHolder.addToBundlesToppedUpSuccessfully(Long.valueOf(profile.getBundleId()));
        
        resH.setChargingHistoryRecord(record);
        resH.setOverallResultCode(0);
        resH.setUrcsResultCode(0);
        resH.setOcgResultCode(0);
        
        StringBuilder noteBuff = new StringBuilder();
        noteBuff.append(msg);
        SubscriberNoteSupport.createSubscriberNote(ctx, SubscribersApiSupport.class.getName(), 
            SubscriberNoteSupport.getCsrAgent(ctx, subscriber), 
                subscriber.getId(), SystemNoteTypeEnum.BUNDLE_REPURCHASE, 
                    SystemNoteSubTypeEnum.BUNDLE_REPURCHASE_ROLLBACK, noteBuff, true);
        
        return 0;
    }


    /**
     * Sets the APIResultHolder as well.
     * 
     * @param ctx
     * @param subscriber
     * @param profile
     * @param bundleFee
     * @param msisdn
     * @param tranRes
     * @param amount
     * @param response
     * @param resH 
     * @return
     * @throws CRMExceptionFault
     */
    private static int handleRollback(Context ctx, Subscriber subscriber,
            BundleProfile profile, BundleFee bundleFee, String msisdn,
            TransactionResultHolder tranRes, long amount,
            BundleTopupResponse response, ApiOptionResultHolder resH) throws CRMExceptionFault
    {
        String msg;
        msg = MessageFormat.format(
                "Attempting Roll-Back Transaction. Cause: Bundle top up failed for subscriber: {0}, amount: {1}, bundle: {2}.",
                new Object[]{msisdn, Long.valueOf(amount), 
                        Long.valueOf(profile.getBundleId())});
        if (LogSupport.isDebugEnabled(ctx))
            LogSupport.debug(ctx, SubscribersApiSupport.class.getName(), msg);
        
        TransactionResultHolder tranRollbackRes = BundleChargingSupport.
                applyBundleRepurchaseTransaction(ctx, profile, bundleFee, subscriber, true);
        
        if (!tranRollbackRes.isSuccess())
        {
            msg = MessageFormat.format(
                    "Subsequent RollBack Transaction failed, for subscriber: {0}, amount: {1}, bundle: {2}; Txn result code: {3}",
                    new Object[]{msisdn, Long.valueOf(amount), Long.valueOf(profile.getBundleId()), 
                            Integer.valueOf(tranRollbackRes.getTransactionResultCode())});
            if (LogSupport.isDebugEnabled(ctx))
                LogSupport.debug(ctx, SubscribersApiSupport.class.getName(), msg);
            
            /*
             * We can't throw SOAP Fault here : Need to allow other options to be processed (if any) 
             * 
             * RmiApiErrorHandlingSupport.
                ocgTransactionException(ctx, null, tranRollbackRes.getTransactionResultCode(), 
                        msg, SubscribersImpl.class);
             */
            
            resH.setOverallResultCode(tranRollbackRes.getTransactionResultCode());
            resH.setOcgResultCode(tranRollbackRes.getOCGResultCode());
            resH.setErrorMessage(ErrorMessageConstants.BUNDLE_TOPUP_ROLL_BACK_FAILED+ tranRollbackRes.getOCGErrorMessage());
            
            /*
             * TODO Need to update DDD to return proper result code for this case
             */
            return -1;
        }
        
        /*
         * Logging and notes update.
         */
        msg = MessageFormat.format(
                "Bundle re-purchase attempt failed for Subscriber {0}, bundle-id: {1}. Cause: Bundle top up failed with error-code {2}; Rolled back balance updates succesfully.",
                new Object[]{msisdn, Long.valueOf(profile.getBundleId()), Integer.valueOf(response.getServiceResponseCode())});
        if (LogSupport.isDebugEnabled(ctx))
            LogSupport.debug(ctx, SubscribersApiSupport.class.getName(), msg);
        
        StringBuilder noteBuff = new StringBuilder();
        noteBuff.append(msg);
        SubscriberNoteSupport.createSubscriberNote(ctx, SubscribersApiSupport.class.getName(), 
            SubscriberNoteSupport.getCsrAgent(ctx, subscriber), 
                subscriber.getId(), SystemNoteTypeEnum.BUNDLE_REPURCHASE, 
                    SystemNoteSubTypeEnum.BUNDLE_REPURCHASE_SUCCESS, noteBuff, true);
        
        resH.setOverallResultCode(-1);
        resH.setOcgResultCode(0);
        resH.setUrcsResultCode(response.getServiceResponseCode());
        resH.setErrorMessage(msg);
        
        return 0;
    }


    private static boolean isRepurchaseRequest(GenericParameterParser paramsParser) throws CRMExceptionFault
    {
        return paramsParser.containsParam(APIGenericParameterSupport.REPURCHASE_IN) && 
            paramsParser.getParameter(APIGenericParameterSupport.REPURCHASE_IN, Boolean.class, 
                Boolean.FALSE).booleanValue();
    }


    /**
     * @param ctx
     * @param subscriber
     * @param option
     * @param parameters
     * @throws CRMExceptionFault
     * @throws HomeException
     */
    private static void disablePricePlanOption(final Context ctx,
            final Subscriber subscriber, PricePlanOption option,
            ApiResultHolder resHolder, 
            GenericParameter[] parameters) throws CRMExceptionFault,
            HomeException
    {
        Long secondaryID = null;
        boolean isCUG = false;
        
        ApiOptionResultHolder resH = 
            resHolder.getPriceplanOptionUpdateApiResultsHolder(option.getIdentifier(), option.getOptionType());
        resH.setOptionUpdateType(OptionUpdateType.UNPROVISION);
        
        if (parameters != null)
        {
            for (GenericParameter parameter : parameters)
            {
                if (parameter == null)
                {
                    continue;
                }
                
                if(APIGenericParameterSupport.PATH.equals(parameter.getName())){
                	Logger.debug(ctx, SubscribersApiSupport.class, "Setting the value of PATH in context ["+ parameter.getValue() + "]");
                    if(parameter.getValue() instanceof String)
                        ctx.put(Lookup.PATH, (String)parameter.getValue());	
                }
                
                if (PricePlanOptionTypeEnum.AUXILIARY_SERVICE.getValue().getValue() == option
                        .getOptionType().getValue()
                        && APIGenericParameterSupport.CALLING_GROUP_ID.equals(parameter.getName()))
                {
                    secondaryID = (Long) parameter.getValue();
                }

                if (PricePlanOptionTypeEnum.AUXILIARY_SERVICE.getValue().getValue()== option.getOptionType().getValue()
                        && APIGenericParameterSupport.CALLING_GROUP_TYPE.equals(parameter.getName()))
                {
                    CallingGroupTypeEnum callingGroupType = (CallingGroupTypeEnum) parameter.getValue();
                    
                    if (CallingGroupTypeEnum.CUG.equals(callingGroupType) || CallingGroupTypeEnum.PCUG.equals(callingGroupType))
                    {
                       isCUG = true;
                    }
                }
                
                if (isCUG && secondaryID == null)
                {
                    RmiApiErrorHandlingSupport.handleStoreExceptions(ctx, null, 
                            "CUG/PCUG auxiliary services cannot be removed through the subscription profile unless the secondary identifier is provided.",
                            SubscribersApiSupport.class);                                   
                }
            }
        }

        disablePricePlanOption(ctx, subscriber, option.getOptionType(), option.getIdentifier(), secondaryID);
    }


    /**
     * @param ctx
     * @param subscriber
     * @param onCreate
     * @param option
     * @param resHolder A generic structure all interesting updates/results that would
     * be needed at the time of compiling the response.
     * @param parameters
     * @throws CRMExceptionFault
     * @throws HomeException
     */
    private static void enablePricePlanOption(final Context ctx,
            final Subscriber subscriber, boolean onCreate,
            PricePlanOption option, ApiResultHolder resHolder, GenericParameter[] parameters)
            throws CRMExceptionFault, HomeException
    {
        ApiOptionResultHolder resH = 
            resHolder.getPriceplanOptionUpdateApiResultsHolder(option.getIdentifier(), option.getOptionType());
        resH.setOptionUpdateType(OptionUpdateType.PROVISION);
        
        Long personalizedFee=Long.valueOf(-1);
        boolean isPersonalizedFeeSelected=Boolean.FALSE;
        
        String aMsisdn = null;
        if (parameters != null)
        {
            for (GenericParameter parameter : parameters)
            {
                if (parameter == null)
                {
                    continue;
                }
                
                if(APIGenericParameterSupport.SERVICE_QUANTITY.equals(parameter.getName()))
                {
                	long serviceQuantity =(Long)parameter.getValue();
                	if(serviceQuantity != 1 && parameter.getValue() != null)
                	{
                		ctx.put(Lookup.SERIVCE_QUANTITY, serviceQuantity);
                	}
                }
                
				if (APIGenericParameterSupport.PATH.equals(parameter.getName())) {
					if (parameter.getValue() != null) {
						String path = (String) parameter.getValue();
						if (path != null) {
							ctx.put(Lookup.PATH, path);
						}
					}
				}
                
                if (PricePlanOptionTypeEnum.AUXILIARY_SERVICE.getValue().getValue() == option.getOptionType().getValue())
                {
                    if (APIGenericParameterSupport.ADDITIONAL_MOBILE_NUMBER.equals(parameter.getName()))
                    {
                        aMsisdn = (String)parameter.getValue();
                    }
                    
                    else if(APIGenericParameterSupport.PERSONALIZED_FEE.equals(parameter.getName()))
                     {
                        personalizedFee=(Long)parameter.getValue();
                     }

                    else if(APIGenericParameterSupport.IS_PERSONALIZED_FEE_SELECTED.equals(parameter.getName()))
                    {
                                isPersonalizedFeeSelected=(Boolean)parameter.getValue();
                    }
                    else if (parameter.getValue() != null)
                    {
                        final String msg = "Generic Parameter ("
                                + parameter.getValue()
                                + " "
                                + parameter.getValue().getClass()
                                + ") not SUPPORTED for Subscription with ID/MobileNumber/SubscriptionType = "
                                + subscriber.getId() + "/" + subscriber.getMSISDN() + "/"
                                + subscriber.getSubscriptionType();
                        RmiApiErrorHandlingSupport.handleStoreExceptions(ctx, null, msg,
                                SubscribersApiSupport.class);
                    }
                }
                
                else if(APIGenericParameterSupport.PERSONALIZED_FEE.equals(parameter.getName()))
                {
                    personalizedFee=(Long)parameter.getValue();
                }
               else if(APIGenericParameterSupport.IS_PERSONALIZED_FEE_SELECTED.equals(parameter.getName()))
               {
                    isPersonalizedFeeSelected=(Boolean)parameter.getValue();
               }
          }
        }
        
        enablePricePlanOption(ctx, subscriber, option.getOptionType(), option.getIdentifier(), option
                .getStartDate(), option.getEndDate(), option.getNumberOfPayments(),aMsisdn, onCreate, resHolder,isPersonalizedFeeSelected,personalizedFee);
    }

    public static String getAdditionalMsisdn(final Context ctx, GenericParameter [] parameters)
    {
        for (int j = 0; j < parameters.length; j++)
        {
            GenericParameter parameter = parameters[j];
            if (parameter.getName().equals(APIGenericParameterSupport.REQUIRES_ADDITIONAL_MOBILE_NUMBER))
            {
                return (String)parameter.getValue();

            }
        }
        return null;
    }


    public static PricePlanOptionUpdateResult[] diffSubcriptionOptions(Context ctx, String subscriberId, Set newSubAuxSvcs, Set newBundles,
            Set newServices, Set oldSubAuxSvcs, Set oldBundles, Set oldServices, ApiResultHolder apiResultHolder, PricePlanOption[] options, final long pricePlanId, Date date)
            throws HomeException
    {
        /*
         * Sujeet: Refactor the method to make use of the ApiResultHolder, so as to avoid bloating
         * the parameter-space with all sorts of data-structures.
         */
        
        
        //aux service
        Map<String, SubscriberAuxiliaryService> newSubAuxSvcsMap = new HashMap<String, SubscriberAuxiliaryService>();
        for (final Iterator<SubscriberAuxiliaryService> iterator = newSubAuxSvcs.iterator(); iterator.hasNext();)
        {
            SubscriberAuxiliaryService auxService = iterator.next();
            String key = auxService.getIdentifier() + "_" + auxService.getSubscriberIdentifier() + "_"
                    + auxService.getAuxiliaryServiceIdentifier();
            newSubAuxSvcsMap.put(key, auxService);
        }
        //new service
        Map<String, SubscriberServices> newSubSvcsMap = new HashMap<String, SubscriberServices>();
        for (final Iterator<SubscriberServices> iterator = newServices.iterator(); iterator.hasNext();)
        {
            SubscriberServices subService = iterator.next();
            String key = subService.getSubscriberId() + "_" + subService.getServiceId();
            newSubSvcsMap.put(key, subService);
        }
        //old aux
        Map<String, SubscriberAuxiliaryService> oldSubAuxSvcsMap = new HashMap<String, SubscriberAuxiliaryService>();
        for (final Iterator<SubscriberAuxiliaryService> iterator = oldSubAuxSvcs.iterator(); iterator.hasNext();)
        {
            SubscriberAuxiliaryService auxService = iterator.next();
            String key = auxService.getIdentifier() + "_" + auxService.getSubscriberIdentifier() + "_"
                    + auxService.getAuxiliaryServiceIdentifier();
            oldSubAuxSvcsMap.put(key, auxService);
        }
        //old service
        Map<String, SubscriberServices> oldServicesMap = new HashMap<String, SubscriberServices>();
        for (final Iterator<SubscriberServices> iterator = oldServices.iterator(); iterator.hasNext();)
        {
            SubscriberServices subService = iterator.next();
            String key = subService.getSubscriberId() + "_" + subService.getServiceId();
            oldServicesMap.put(key, subService);
        }
        return diffSubcriptionOptions(ctx, subscriberId, newSubAuxSvcsMap, newBundles, newSubSvcsMap, oldSubAuxSvcsMap, oldBundles,
                oldServicesMap, apiResultHolder, options, pricePlanId, date);
    }
    

    public static PricePlanOptionUpdateResult[] diffSubcriptionOptions(Context ctx, String subscriberId, Map newSubAuxSvcs, Set newBundles,
            Map newServices, Map oldSubAuxSvcs, Set oldBundles, Map oldServices, 
            ApiResultHolder apiResultHolder, PricePlanOption[] options, final long pricePlanId, Date date)
            throws HomeException
    {
        // TODO: need to show changes in price plan services and bundles as remove and add
        // even if its for the same service
        
        //Get charging history
        Collection<SubscriberSubscriptionHistory> history = SubscriberSubscriptionHistorySupport.getChargingEventsSince(ctx, subscriberId, date);
        Map<String, SubscriberSubscriptionHistory> chargeHistory = mapChargingHistory(ctx, history);
        
        Map<String, PricePlanOptionUpdateResult> optionResultsSet = new HashMap<String, PricePlanOptionUpdateResult>();
        for (final Iterator iterator = newSubAuxSvcs.values().iterator(); iterator.hasNext();)
        {
            SubscriberAuxiliaryService subAuxService = (SubscriberAuxiliaryService) iterator.next();
            String mapKey = subAuxService.getIdentifier() + "_" + subAuxService.getSubscriberIdentifier() + "_"
                    + subAuxService.getAuxiliaryServiceIdentifier();
            if (!oldSubAuxSvcs.containsKey(mapKey))
            {
                if(LogSupport.isDebugEnabled(ctx))
                    LogSupport.debug(ctx, SubscribersApiSupport.class.getName(), 
                            "For subscriber: "+ subAuxService.getSubscriberIdentifier()+ 
                                " | Added AUX Service: "+ subAuxService.getAuxiliaryServiceIdentifier());
                
                String key = PricePlanOptionTypeEnum.AUXILIARY_SERVICE.getValue().toString() + "_ADD_"
                        + subAuxService.getAuxiliaryServiceIdentifier();
                
                ProvisioningStateTypeEnum auxSvcProvState = ProvisioningStateTypeEnum.PROVISIONED;
                try
                {
                    if (SuspendedEntitySupport.isSuspendedEntity(ctx, 
                            subAuxService.getSubscriberIdentifier(), 
                            subAuxService.getAuxiliaryServiceIdentifier(), 
                            subAuxService.getSecondaryIdentifier(), 
                            com.redknee.app.crm.bean.AuxiliaryService.class))
                    {
                        auxSvcProvState = ProvisioningStateTypeEnum.SUSPENDED;
                    }
                }
                catch (HomeException e)
                {
                    new MinorLogMsg(SubscribersApiSupport.class, 
                            "Error looking up suspended state of auxiliary service " + subAuxService.getIdentifier()
                            + " for subscription " + subAuxService.getSubscriberIdentifier() + ".  Assuming not suspended...", e).log(ctx);
                }
                
                if(LogSupport.isDebugEnabled(ctx))
                    LogSupport.debug(ctx, SubscribersApiSupport.class.getName(), 
                            "Service Id : "+ subAuxService.getAuxiliaryServiceIdentifier()+ " | Added AUX Service ProvState:  "+ auxSvcProvState);
                
                SubscriberSubscriptionHistory charge = chargeHistory.get(key);
                long fee = charge != null ? charge.getChargedAmount() : 0l;
                optionResultsSet.put(key, createPricePlanUpdateOptionResult(PricePlanOptionTypeEnum.AUXILIARY_SERVICE
                        .getValue(), subAuxService.getEndDate(), fee, subAuxService.getStartDate(),
                        PricePlanOptionUpdateTypeEnum.ADD.getValue(), true, auxSvcProvState.getValue(),
                        subAuxService.getAuxiliaryServiceIdentifier()));
            }
        }
        for (final Iterator iterator = oldSubAuxSvcs.values().iterator(); iterator.hasNext();)
        {
            SubscriberAuxiliaryService subAuxService = (SubscriberAuxiliaryService) iterator.next();
            String mapKey = subAuxService.getIdentifier() + "_" + subAuxService.getSubscriberIdentifier() + "_"
                    + subAuxService.getAuxiliaryServiceIdentifier();
            if (!newSubAuxSvcs.containsKey(mapKey))
            {
                
                if(LogSupport.isDebugEnabled(ctx))
                    LogSupport.debug(ctx, SubscribersApiSupport.class.getName(), 
                            "For subscriber: "+ subAuxService.getSubscriberIdentifier()+ 
                                " | Removed AUX Service: "+ subAuxService.getAuxiliaryServiceIdentifier());
                
                
                String key = PricePlanOptionTypeEnum.AUXILIARY_SERVICE.getValue().toString() + "_REMOVE_"
                        + subAuxService.getAuxiliaryServiceIdentifier();
                SubscriberSubscriptionHistory charge = chargeHistory.get(key);
                long fee = charge != null ? charge.getChargedAmount() : 0l;
                optionResultsSet.put(key, createPricePlanUpdateOptionResult(PricePlanOptionTypeEnum.AUXILIARY_SERVICE
                        .getValue(), subAuxService.getEndDate(), fee, subAuxService.getStartDate(),
                        PricePlanOptionUpdateTypeEnum.REMOVE.getValue(), false, ProvisioningStateTypeEnum.UNPROVISIONED
                                .getValue(), subAuxService.getAuxiliaryServiceIdentifier()));
            }
        }
        for (final Iterator iterator = newServices.values().iterator(); iterator.hasNext();)
        {
            SubscriberServices subService = (SubscriberServices) iterator.next();
            String mapKey = subService.getSubscriberId() + "_" + subService.getServiceId();
            if (!oldServices.containsKey(mapKey))
            {
                if(LogSupport.isDebugEnabled(ctx))
                    LogSupport.debug(ctx, SubscribersApiSupport.class.getName(), 
                            "For subscriber: "+ subService.getSubscriberId()+ 
                                " | Added PP Service: "+ subService.getServiceId());
                
                if(LogSupport.isDebugEnabled(ctx))
                    LogSupport.debug(ctx, SubscribersApiSupport.class.getName(), 
                            "Service Id: "+ subService.getServiceId()+ " | Added PP Service ProvState:  "+ subService.getProvisionedState());
                
                String key = PricePlanOptionTypeEnum.SERVICE.getValue().toString() + "_ADD_"
                        + subService.getServiceId();
                SubscriberSubscriptionHistory charge = chargeHistory.get(key);
                long fee = charge != null ? charge.getChargedAmount() : 0l;
                optionResultsSet.put(key, createPricePlanUpdateOptionResult(PricePlanOptionTypeEnum.SERVICE.getValue(),
                        subService.getEndDate(), fee, subService.getStartDate(), PricePlanOptionUpdateTypeEnum.ADD
                                .getValue(), true, ProvisioningStateTypeEnum.valueOf(subService.getProvisionedState()
                                .getIndex()), subService.getServiceId()));
            }
        }
        for (final Iterator iterator = oldServices.values().iterator(); iterator.hasNext();)
        {
            SubscriberServices subService = (SubscriberServices) iterator.next();
            String mapKey = subService.getSubscriberId() + "_" + subService.getServiceId();
            if (!newServices.containsKey(mapKey))
            {
                


                if(LogSupport.isDebugEnabled(ctx))
                    LogSupport.debug(ctx, SubscribersApiSupport.class.getName(), 
                            "For subscriber: "+ subService.getSubscriberId()+ 
                                " | Removed PP Service: "+ subService.getServiceId());
                
                String key = PricePlanOptionTypeEnum.SERVICE.getValue().toString() + "_REMOVE_"
                        + subService.getServiceId();
                SubscriberSubscriptionHistory charge = chargeHistory.get(key);
                long fee = charge != null ? charge.getChargedAmount() : 0l;
                optionResultsSet.put(key, createPricePlanUpdateOptionResult(PricePlanOptionTypeEnum.SERVICE.getValue(),
                        subService.getEndDate(), fee, subService.getStartDate(), PricePlanOptionUpdateTypeEnum.REMOVE
                                .getValue(), false, ProvisioningStateTypeEnum.UNPROVISIONED.getValue(), subService
                                .getServiceId()));
            }
        }
        
        Map bundleIds = null;
        if (newBundles.size() > 0 || oldBundles.size() > 0)
        {
            com.redknee.app.crm.bean.core.PricePlan plan = PricePlanSupport.getPlan(ctx, pricePlanId);
            bundleIds = PricePlanSupport.getBundleIds(ctx, plan, true);
            
        }
        
        for (final Iterator<Long> iterator = newBundles.iterator(); iterator.hasNext();)
        {
            Long bundleId = iterator.next();
            if (!oldBundles.contains(bundleId))
            {
                if(LogSupport.isDebugEnabled(ctx))
                    LogSupport.debug(ctx, SubscribersApiSupport.class.getName(), 
                            "For subscriber: "+ subscriberId+ 
                                " | Added Bundle: "+ bundleId);
                
                BundleProfile bundleProfile = null;
                try
                {
                    bundleProfile = BundleSupportHelper.get(ctx).getBundleProfile(ctx, bundleId.longValue());
                }
                catch (InvalidBundleApiException invalidApi)
                {
                    throw new HomeException("Unable to find bundle profile for " + bundleId);
                }
                PricePlanOptionType optionType = PricePlanOptionTypeEnum.BUNDLE.getValue();
                if (bundleProfile.isAuxiliary())
                {
                    if (bundleIds == null || (!bundleIds.containsKey(Long.valueOf(bundleProfile.getBundleId()))))
                    {
                        optionType = PricePlanOptionTypeEnum.AUXILIARY_BUNDLE.getValue();
                    }
                }
                String key = optionType.toString() + "_ADD_" + bundleId;

                ProvisioningStateTypeEnum bundleProvState = ProvisioningStateTypeEnum.PROVISIONED;
                try
                {
                    if (SuspendedEntitySupport.isSuspendedEntity(ctx, 
                            subscriberId, 
                            bundleId, 
                            SubscriberAuxiliaryServiceSupport.SECONDARY_ID_NOT_USED, 
                            com.redknee.app.crm.bundle.BundleFee.class))
                    {
                        bundleProvState = ProvisioningStateTypeEnum.SUSPENDED;
                    }
                }
                catch (HomeException e)
                {
                    new MinorLogMsg(SubscribersApiSupport.class, 
                            "Error looking up suspended state of bundle " + bundleId
                            + " for subscription " + subscriberId + ".  Assuming not suspended...", e).log(ctx);
                }
                
                
                if(LogSupport.isDebugEnabled(ctx))
                    LogSupport.debug(ctx, SubscribersApiSupport.class.getName(), 
                            "Bundle ID: "+ bundleId+ "| Added Bundle Provisioning State: "+ bundleProvState);
                
                SubscriberSubscriptionHistory charge = chargeHistory.get(key);
                long fee = charge != null ? charge.getChargedAmount() : 0l;
                optionResultsSet.put(key, createPricePlanUpdateOptionResult(optionType, bundleProfile.getEndDate(), fee,
                        bundleProfile.getStartDate(), PricePlanOptionUpdateTypeEnum.ADD.getValue(), true,
                        bundleProvState.getValue(), bundleProfile.getBundleId()));
            }
        }
        for (final Iterator<Long> iterator = oldBundles.iterator(); iterator.hasNext();)
        {
            Long bundleId = iterator.next();
            if (!newBundles.contains(bundleId))
            {
                
                if(LogSupport.isDebugEnabled(ctx))
                    LogSupport.debug(ctx, SubscribersApiSupport.class.getName(), 
                            "For subscriber: "+ subscriberId+ 
                                " | Removed Bundle: "+ bundleId);

                BundleProfile bundleProfile = null;
                try
                {
                    bundleProfile = BundleSupportHelper.get(ctx).getBundleProfile(ctx, bundleId.longValue());
                }
                catch (InvalidBundleApiException invalidApi)
                {
                    throw new HomeException("Unable to find bundle profile for " + bundleId);
                }
                PricePlanOptionType optionType = PricePlanOptionTypeEnum.BUNDLE.getValue();
                if (bundleProfile.isAuxiliary())
                {
                    if (bundleIds == null || (!bundleIds.containsKey(Long.valueOf(bundleProfile.getBundleId()))))
                    {
                        optionType = PricePlanOptionTypeEnum.AUXILIARY_BUNDLE.getValue();
                    }
                }
                String key = optionType.toString() + "_REMOVE_" + bundleId;
                SubscriberSubscriptionHistory charge = chargeHistory.get(key);
                long fee = charge != null ? charge.getChargedAmount() : 0l;
                optionResultsSet.put(key, createPricePlanUpdateOptionResult(optionType, bundleProfile.getEndDate(), fee,
                        bundleProfile.getStartDate(), PricePlanOptionUpdateTypeEnum.REMOVE.getValue(), false,
                        ProvisioningStateTypeEnum.UNPROVISIONED.getValue(), bundleProfile.getBundleId()));
            }
        }
        
        Set<Long> successfullyRepurchasedBundles = apiResultHolder.getBundlesToppedUpSuccessfully();
        
        if (options != null)
        {
            for (int i = 0; i < options.length; i++)
            {
                PricePlanOption option = options[i];
                
                if (option == null)
                {
                    continue;
                }
                ApiOptionResultHolder optionRes = 
                    apiResultHolder.getPriceplanOptionUpdateApiResultsHolder(option.getIdentifier(), option.getOptionType());
                StringBuilder key = new StringBuilder();
                key.append(option.getOptionType().getValue());
                ProvisioningStateType provisionState = option.getProvisioningState(); 
                if (option.getIsSelected())
                {
                    key.append("_ADD_");
                    provisionState = ProvisioningStateTypeEnum.NOT_PROVISIONED.getValue();
                }
                else
                {
                    key.append("_REMOVE_");
                }
                key.append(option.getIdentifier());
                
                if (!optionResultsSet.containsKey(key.toString()))
                {
                    // Only if it was successfully topped-up
                    if(successfullyRepurchasedBundles.contains(Long.valueOf(option.getIdentifier())))
                    {
                        ProvisioningStateTypeEnum bundleProvState = ProvisioningStateTypeEnum.PROVISIONED;
                        try
                        {
                            if (SuspendedEntitySupport.isSuspendedEntity(ctx, 
                                    subscriberId, 
                                    option.getIdentifier(), 
                                    SubscriberAuxiliaryServiceSupport.SECONDARY_ID_NOT_USED, 
                                    com.redknee.app.crm.bundle.BundleFee.class))
                            {
                                bundleProvState = ProvisioningStateTypeEnum.SUSPENDED;
                            }
                        }
                        catch (HomeException e)
                        {
                            new MinorLogMsg(SubscribersApiSupport.class, 
                                    "Error looking up suspended state of bundle " + option.getIdentifier()
                                    + " for subscription " + subscriberId + ".  Assuming not suspended...", e).log(ctx);
                        }
                        
                        if(LogSupport.isDebugEnabled(ctx))
                            LogSupport.debug(ctx, SubscribersApiSupport.class.getName(), 
                                    "For subscriber: "+ subscriberId+ 
                                        " | REPURCHASE | Unchanged Option Id : "+ option.getIdentifier() + 
                                        " | Prov State: "+ bundleProvState.getValue());
                        
                        /*
                         * Bundle Re-purchase.
                         */
                        SubscriberSubscriptionHistory charge = chargeHistory.get(key.toString());
                        long fee = charge != null ? charge.getChargedAmount() : 0l;
                        optionResultsSet.put(key.toString(), 
                                createPricePlanUpdateOptionResult(option.getOptionType(),
                                        option.getEndDate(), fee, option.getStartDate(), 
                                        PricePlanOptionUpdateTypeEnum.ADD.getValue(), option.getIsSelected(), 
                                        bundleProvState.getValue(), 
                                        option.getIdentifier()));
                    }
                    else
                    {
                        /*
                         *  If failed repurchase due to insufficient balance; We should 
                         *  report the exact provisioning state in any case.
                         */
                        if(optionRes.getOptionUpdateType() == OptionUpdateType.REPURCHASE)
                        {
                            provisionState = ProvisioningStateTypeEnum.PROVISIONED.getValue();
                            try
                            {
                                if (SuspendedEntitySupport.isSuspendedEntity(ctx, 
                                        subscriberId, 
                                        option.getIdentifier(), 
                                        SubscriberAuxiliaryServiceSupport.SECONDARY_ID_NOT_USED, 
                                        com.redknee.app.crm.bundle.BundleFee.class))
                                {
                                    provisionState = ProvisioningStateTypeEnum.SUSPENDED.getValue();
                                }
                            }
                            catch (HomeException e)
                            {
                                new MinorLogMsg(SubscribersApiSupport.class, 
                                        "Error looking up suspended state of 'repurchasable' bundle " + option.getIdentifier()
                                        + " for subscription " + subscriberId + ".  Assuming not suspended...", e).log(ctx);
                                provisionState = ProvisioningStateTypeEnum.PROVISIONED.getValue();
                            }
                            if(LogSupport.isDebugEnabled(ctx))
                            {
                                String msg = MessageFormat.format( 
                                        "For subscriber: {0} | {1} - FAILED | Unchanged Option Id : {2} | Option-type: {4} | Prov State: {3}",
                                        new Object[]{subscriberId, optionRes.getOptionUpdateType().name(), 
                                                Long.valueOf(option.getIdentifier()), provisionState, 
                                                    option.getOptionType().getValue()});
                                LogSupport.debug(ctx, SubscribersApiSupport.class.getName(), msg);
                            }
                        }
                        
                        /*
                         * Sujeet: The charge will always be NULL!!! 
                         * --> the hashcode of a StringBuilder/StringBuffer instance 
                         *     will not match against that of a String!
                         * So, effectively the charge will always be 0 (then why do we make an
                         * additional effort to check ChargingHirtory?)
                         */
                        SubscriberSubscriptionHistory charge = chargeHistory.get(key);
                        long fee = charge != null ? charge.getChargedAmount() : 0l;
                        
                        
                       if( ( option.getStartDate() != null && option.getStartDate().after(new Date())) && provisionState.equals(ProvisioningStateTypeEnum.NOT_PROVISIONED.getValue()))
                       {
                    	   optionResultsSet.put(key.toString(), createPricePlanUpdateOptionResult(option.getOptionType(),
                                   option.getEndDate(), fee, option.getStartDate(), PricePlanOptionUpdateTypeEnum.ADD
                                           .getValue(), option.getIsSelected(), provisionState, option
                                           .getIdentifier()));
                       }
                       else
                       {
                    	   optionResultsSet.put(key.toString(), createPricePlanUpdateOptionResult(option.getOptionType(),
                                   option.getEndDate(), fee, option.getStartDate(), PricePlanOptionUpdateTypeEnum.NO_UPDATE
                                           .getValue(), option.getIsSelected(), provisionState, option
                                           .getIdentifier()));
                       }
                        
                    }
                }
                //We may directly check the condition to check with exception code:SERVICE_PROVISIONING_EXCEPTION. Don't want to hit the condition for other priceplanoptions other than TFA.
                //Currently We have added the condition to check with TFAAuxServiceSupport constant. It can be eliminated in future.
                if(optionRes.getOverallResultCode() == TFAAuxServiceSupport.TFA_SAME_TYPE_AUX_SERVICE_EXCEPTION){
                        	
                			provisionState = ProvisioningStateTypeEnum.NOT_PROVISIONED.getValue(); //Set Not Provisioned state.
                        	
                			optionResultsSet.put(key.toString(), createPricePlanUpdateOptionResult(option.getOptionType(),
                                     option.getEndDate(), 0l, option.getStartDate(), PricePlanOptionUpdateTypeEnum.NO_UPDATE
                                             .getValue(), option.getIsSelected(), provisionState, option
                                             .getIdentifier()));
                        	
                			 optionRes.setBssResultCode((int)(ExceptionCode.SERVICE_PROVISIONING_EXCEPTION));//For setting API Error Source as BSS
                        	 optionRes.setOverallResultCode((int)(ExceptionCode.SERVICE_PROVISIONING_EXCEPTION));
                        	
                        	 
                 }
                if(LogSupport.isDebugEnabled(ctx))
                {
                    String msg = MessageFormat.format(
                            "FOR API-RES-HOLDER :: OptionId: {0}, OptionType: {1} OCG-RC: {2}, urcs-RC: {3}, overall-RC: {4} err-mesg: {5}", 
                                new Object[]{Long.valueOf(option.getIdentifier()), 
                                    option.getOptionType(), Integer.valueOf(optionRes.getOcgResultCode()), 
                                        Integer.valueOf(optionRes.getUrcsResultCode()),
                                            Integer.valueOf(optionRes.getOverallResultCode()), 
                                                optionRes.getErrorMessage()});
                    
                    LogSupport.debug(ctx, SubscribersApiSupport.class.getName(), msg);
                }
                
                addErrorCodeReportingForPricePlanUpdateOptionResult(ctx, optionResultsSet, key.toString(), option, optionRes);
            }
        }
        PricePlanOptionUpdateResult[] optionResults = new PricePlanOptionUpdateResult[]
            {};
        return optionResultsSet.values().toArray(optionResults);
    }


    /**
     * 
     * @param ctx
     * @param optionResultsSet
     * @param key
     * @param option
     * @param optionRes
     */
    private static void addErrorCodeReportingForPricePlanUpdateOptionResult(
            Context ctx,
            Map<String, PricePlanOptionUpdateResult> optionResultsSet,
            String key, PricePlanOption option,
            ApiOptionResultHolder optionRes)
    {
        PricePlanOptionUpdateResult res = optionResultsSet.get(key);
        
        if(optionRes.getOverallResultCode()!=0)
        {
            res.addParameters(
                    RmiApiSupport.createGenericParameter(
                            Constants.API_ERROR_CODE, String.valueOf(optionRes.getOverallResultCode())));
            res.addParameters(
                    RmiApiSupport.createGenericParameter(
                            Constants.API_ERROR_MESSAGE, String.valueOf(optionRes.getErrorMessage())));
            res.addParameters(
                    RmiApiSupport.createGenericParameter(
                            Constants.API_ERROR_SOURCE, String.valueOf(
                                    optionRes.getApiErrorSource())));
            res.addParameters(
                    RmiApiSupport.createGenericParameter(
                            Constants.API_INTERNAL_ERROR_CODE, String.valueOf(
                                    optionRes.getApiInternalErrorCode())));
            
           
        }
        
    }


    private static Map<String, SubscriberSubscriptionHistory> mapChargingHistory(Context ctx,
            Collection<SubscriberSubscriptionHistory> history)
    {
        Map<String, SubscriberSubscriptionHistory>  map = new HashMap<String, SubscriberSubscriptionHistory>();
        
        for (SubscriberSubscriptionHistory charge : history)
        {
            PricePlanOptionType type = PricePlanOptionTypeEnum.valueOf(charge.getItemType().getIndex());
            String chargeAction = "ADD";
            if (charge.getEventType() == HistoryEventTypeEnum.REFUND)
            {
                chargeAction = "REMOVE";
            }
            String key = type.toString() + "_" + chargeAction + "_" + charge.getItemIdentifier();
            map.put(key,  charge);
        }
        if (LogSupport.isDebugEnabled(ctx))
        {
            LogSupport.debug(ctx,  SubscribersApiSupport.class.getName(), "Charging history mapped: " + Arrays.toString(map.keySet().toArray()));
        }
        return map;
    }


    private static PricePlanOptionUpdateResult createPricePlanUpdateOptionResult(
            final PricePlanOptionType optionType, final Date endDate, final long fee, final Date startDate,
            final PricePlanOptionUpdateType updateType, final boolean isSelected,
            final ProvisioningStateType state, final long id)
    {
        PricePlanOptionUpdateResult result = new PricePlanOptionUpdateResult();
        result.setIdentifier(id);
        result.setAppliedFee(fee);
        result.setIsSelected(isSelected);
        if (optionType != null)
        {
            result.setOptionType(optionType);
        }
        if (startDate != null)
        {
            result.setStartDate(startDate);
        }
        if (endDate != null)
        {
            result.setEndDate(endDate);
        }
        if (updateType != null)
        {
            result.setUpdateType(updateType);
        }
        if (state != null)
        {
            result.setProvisioningState(state);
        }
        return result;
    }


    /**
     * Enables a price plan option.
     *
     * @param ctx
     *            The operating context.
     * @param subscriber
     *            Subscriber to be updated.
     * @param optionType
     *            The type of price plan option.
     * @param optionID
     *            Price plan option identifier.
     * @param start
     *            Start date.
     * @param end
     *            End date.
     * @param payments
     *            Number of payments.
     * @param resHolder TODO
     * @throws CRMExceptionFault
     *             Thrown if one or more of the provided values are invalid.
     * @throws HomeException
     *             Thrown if there are problems enabling a price plan option.
     */
    public static void enablePricePlanOption(final Context ctx, final Subscriber subscriber, final PricePlanOptionType optionType,
        final long optionID, final Date start, final Date end, final Integer payments, final String aMsisdn, 
            boolean onCreate, ApiResultHolder resHolder,boolean isPersonalizedFeeSelected, Long personalizedFee) throws CRMExceptionFault,
        HomeException
    {
        final Long key = Long.valueOf(optionID);

        RmiApiErrorHandlingSupport.validateMandatoryObject(optionType, "optionType");
        
        String path = SubscriberServicesUtil.DEFAULT_PATH;
        if(ctx.get(Lookup.PATH) != null && !((String)ctx.get(Lookup.PATH)).trim().isEmpty()){
        	path = (String)ctx.get(Lookup.PATH);
        	Logger.debug(ctx, SubscribersApiSupport.class, "Extracted the value of path from context : [" + path + "]");
        }
        
        if (optionType.getValue() == PricePlanOptionTypeEnum.SERVICE.getValue().getValue())
        {
            final PricePlanVersion ppv = subscriber.getRawPricePlanVersion(ctx);
            final Map <ServiceFee2ID, ServiceFee2> serviceFees = ppv.getServicePackageVersion(ctx).getServiceFees();
            
            //Considering that the Map contains the key/value pair where value is default path "-"
            ServiceFee2ID serviceFeeObj = new ServiceFee2ID(key, path);
            
            Logger.debug(ctx, SubscribersApiSupport.class, "Search Key for Service Fee : [" + serviceFeeObj + "]");
            final ServiceFee2 serviceFee = serviceFees.get(serviceFeeObj);
            if (serviceFee == null)
            {
                RmiApiErrorHandlingSupport.simpleValidation("Service is not part of the Price Plan.",
                "Specified service is not part of the subscriber Price Plan.");
            }
            else if (serviceFee.getServicePreference() == ServicePreferenceEnum.MANDATORY)
            {
                RmiApiErrorHandlingSupport.simpleValidation("Service is Mandatory.",
                "Specified service is Mandatory in the subscriber Price Plan it is always enabled.");
                long serviceQuantity = 1;
                if(ctx.has(Lookup.SERIVCE_QUANTITY))
                {
                                serviceQuantity = (Long)ctx.getLong(Lookup.SERIVCE_QUANTITY);
                                if(serviceQuantity > 1)
                                {
                                                updateMandatoryServiceQuantity(ctx, subscriber, optionID);
                                }
                }
            }
            
            enableService(ctx, subscriber, optionID, start, end,
                    onCreate, key, serviceFee,isPersonalizedFeeSelected,personalizedFee, path);
        }
        else if (optionType.getValue() == PricePlanOptionTypeEnum.PACKAGE.getValue().getValue())
        {
            RmiApiErrorHandlingSupport.simpleValidation("Packages currently cannot be enabled or disabled.",
            "updateSubscriptionEnablePricePlanOption() for packages is not supported.");
        }
        else if (optionType.getValue() == PricePlanOptionTypeEnum.BUNDLE.getValue().getValue())
        {
            final Map bundles = SubscriberBundleSupport.getPricePlanBundles(ctx, subscriber);
            BundleFee fee = (BundleFee) bundles.get(key);
            if (fee == null)
            {
                RmiApiErrorHandlingSupport.simpleValidation("Bundle is not part of the Price Plan.",
                "Specified bundle is not part of the subscriber Price Plan.");
            }
            else if (fee.getServicePreference() == ServicePreferenceEnum.MANDATORY)
            {
                RmiApiErrorHandlingSupport.simpleValidation("Bundle is Mandatory.",
                "Specified bundle is Mandatory in the subscriber Price Plan and cannot be disabled.");
            }
            
            try
            {
                BundleProfile profile = fee.getBundleProfile(ctx, subscriber.getSpid());
                if (profile == null)
                {
                    RmiApiErrorHandlingSupport.simpleValidation("Bundle Profile Does Not Exist",
                            " Unable to load bundle profile for key " + key);
                }
                else if (!profile.isEnabled())
                {
                    RmiApiErrorHandlingSupport.simpleValidation("Bundle Profile is Disabled",
                            " Bundle profile [" + key + "] is not enabled.  Hence, it needs to be enabled for it to be provisioned");
                }
            }
            catch (BundleDoesNotExistsException e)
            {
                RmiApiErrorHandlingSupport.simpleValidation("optionID", "Invalid bundle profile identifier.");
            }
            catch (Exception e)
            {
                RmiApiErrorHandlingSupport.simpleValidation("optionID", "Exception while validating bundle profile identifier.");
            }
            
            enableBundle(ctx, subscriber, optionID, start, end,
                        payments, key, fee);
        }
        else if (optionType.getValue() == PricePlanOptionTypeEnum.AUXILIARY_SERVICE.getValue().getValue())
        {
            enableAuxiliaryService(ctx, subscriber, optionID, start, end, payments, aMsisdn, onCreate,isPersonalizedFeeSelected, personalizedFee);
        }
        else if (optionType.getValue() == PricePlanOptionTypeEnum.AUXILIARY_BUNDLE.getValue().getValue())
        {
            final Map bundles = SubscriberBundleSupport.getPricePlanBundles(ctx, subscriber);
            BundleFee fee = (BundleFee) bundles.get(key);
            if (fee != null)
            {
                RmiApiErrorHandlingSupport.simpleValidation("optionType",
                "Specified bundle is part of the subscriber Price Plan. Please use BUNDLE optionType.");
            }
            fee = (BundleFee) subscriber.getBundles().get(key);
            
            if (fee == null)
            {
                try
                {
                    fee = (BundleFee) XBeans.instantiate(BundleFee.class, ctx);
                }
                catch (Exception e)
                {
                    new MinorLogMsg(SubscribersApiSupport.class, "Error instantiating new bundle fee.  Using default constructor.", e).log(ctx);
                    fee = new BundleFee();
                }

                fee.setId(optionID);
            }
            
            long feeAmount = 0L;
            try
            {
                BundleProfile profile = fee.getBundleProfile(ctx, subscriber.getSpid());
                if (profile==null)
                {
                    RmiApiErrorHandlingSupport.simpleValidation("optionID", "Invalid bundle profile identifier.");
                }
                else if (!profile.isAuxiliary())
                {
                    RmiApiErrorHandlingSupport.simpleValidation("optionID", "Informed bundle profile is not auxiliary.");
                }
                else if (!profile.isEnabled())
                {
                    RmiApiErrorHandlingSupport.simpleValidation("optionID", "Bundle profile [" + profile.getBundleId() + "] is not enabled.");                        
                }
                feeAmount = profile.getAuxiliaryServiceCharge();
            } 
            catch (BundleDoesNotExistsException e)
            {
                RmiApiErrorHandlingSupport.simpleValidation("optionID", "Invalid bundle profile identifier.");
            }
            catch (Exception e)
            {
                RmiApiErrorHandlingSupport.simpleValidation("optionID", "Exception while validating bundle profile identifier.");
            }
            
            enableAuxiliaryBundle(ctx, subscriber, optionID, start, end,
                    payments, key, fee);
        }
    }


    /**
     * @param ctx
     * @param subscriber
     * @param optionID
     * @param start
     * @param end
     * @param payments
     * @param key
     * @param fee
     * @throws IllegalArgumentException
     * @throws CRMExceptionFault
     */
    private static void enableAuxiliaryBundle(final Context ctx,
            final Subscriber subscriber, final long optionID, final Date start,
            final Date end, final Integer payments, final Long key,
            BundleFee fee) throws IllegalArgumentException, CRMExceptionFault
    {
        
        if (start != null)
        {
            fee.setStartDate(start);
        }

        if (end != null)
        {
            fee.setEndDate(end);
        }
        
        if (payments != null)
        {
            if (payments <= 0)
            {
                RmiApiErrorHandlingSupport.simpleValidation("numberOfPayments", "Number of payments must be greater than 0.");
            }
            fee.setPaymentNum(payments.intValue());
        }
        subscriber.getBundles().put(key, fee);
    }


    /**
     * @param ctx
     * @param subscriber
     * @param optionID
     * @param start
     * @param end
     * @param payments
     * @param key
     * @param fee
     * @throws CRMExceptionFault
     * @throws IllegalArgumentException
     */
    private static void enableBundle(final Context ctx,
            final Subscriber subscriber, final long optionID, final Date start,
            final Date end, final Integer payments, final Long key,
            BundleFee fee) throws CRMExceptionFault, IllegalArgumentException
    {
        fee = (BundleFee) subscriber.getBundles().get(key);
        if (fee == null)
        {
            try
            {
                fee = (BundleFee) XBeans.instantiate(BundleFee.class, ctx);
            }
            catch (Exception e)
            {
                new MinorLogMsg(SubscribersApiSupport.class, "Error instantiating new bundle fee.  Using default constructor.", e).log(ctx);
                fee = new BundleFee();
            }
            fee.setId(optionID);
            if (start != null)
            {
                fee.setStartDate(start);
            }
        }
        if (end != null)
        {
            fee.setEndDate(end);
        }
        if (payments != null)
        {
            if (payments <= 0)
            {
                RmiApiErrorHandlingSupport.simpleValidation("numberOfPayments", "Number of payments must be greater than 0.");
            }
            fee.setPaymentNum(payments.intValue());
        }
        subscriber.getBundles().put(key, fee);
    }


    /**
     * @param ctx
     * @param subscriber
     * @param optionID
     * @param start
     * @param end
     * @param onCreate
     * @param key
     * @param serviceFee
     * @throws IllegalArgumentException
     */
    private static void enableService(final Context ctx,
            final Subscriber subscriber, final long optionID, final Date start,
            final Date end, boolean onCreate, final Long key,
            final ServiceFee2 serviceFee,boolean isPersonalizedFeeSelected, long personalizedFee, String path) throws CRMExceptionFault,IllegalArgumentException,HomeException
    {
        final Set subscribed = subscriber.getIntentToProvisionServices(ctx);
        long serviceQuantity = 1;
        Service service = null;
        if (ctx.has(Lookup.SERIVCE_QUANTITY)) {
			serviceQuantity = (Long) ctx.getLong(Lookup.SERIVCE_QUANTITY);
			if (onCreate && serviceQuantity > 1 && serviceFee.getServicePreference() == ServicePreferenceEnum.DEFAULT) {
				// calling the MandatoryServiceQuantity method for
				// DefaultServiceQuantity
				updateMandatoryServiceQuantity(ctx, subscriber, optionID);
			}
		}
        if (LogSupport.isDebugEnabled(ctx)){
            LogSupport.debug(ctx, SubscribersApiSupport.class.getName(), "Service optionId : " + optionID
                    + ", serviceQuantity : " + serviceQuantity);
        }
		try {
			service = HomeSupportHelper.get(ctx).findBean(ctx, Service.class, optionID);
		    }
		 catch (HomeException e1) {
			 if (LogSupport.isDebugEnabled(ctx))
             {
                 new DebugLogMsg(SubscribersApiSupport.class, 
                         "Service home is getting null due which error occured while executing updatePricePlan option call", null).log(ctx);
             }
		}
        SubscriberServices bean = null;
        for (final Iterator iterator = subscribed.iterator(); iterator.hasNext();)
        {
            final SubscriberServices subService = (SubscriberServices) iterator.next();
            if (subService.getServiceId() == optionID)
            {
            	if (LogSupport.isDebugEnabled(ctx))
                {
                    LogSupport.debug(ctx, SubscribersApiSupport.class.getName(), "Service optionId : " + optionID
                            + ", serviceQuantity : " + serviceQuantity + ", old quantity : " + subService.getServiceQuantity());
                }
            	//Change in service quantity from DCRM
            	if(subService.getServiceQuantity() > serviceQuantity)
            	{
            		handleRefundOnQuntityDecrease(ctx, subscriber, subService, serviceQuantity);            		
            	}
            	else if(subService.getServiceQuantity() < serviceQuantity)
            	{
            		handleChargeWithIncreasedQuantity(ctx, subscriber, subService, serviceQuantity);
            	}
                bean = subService;
                bean.setProvisionedState(ServiceStateEnum.PROVISIONED);
                bean.setIsfeePersonalizationApplied(isPersonalizedFeeSelected);
                bean.setPath(path);
                
                if(service!=null && service.getFeePersonalizationAllowed() && isPersonalizedFeeSelected)
                {   if(personalizedFee != -1)
                {
                    bean.setPersonalizedFee(personalizedFee);
                }
                else
                {
                	throw new CRMExceptionFault("Send some fee as PersonalizedFee if isPersonalizedFeeSelected flag is true");
                }
                }
                if(serviceQuantity > 1)
                {
                                bean.setServiceQuantity(serviceQuantity);
                }
                break;
            }
        }
        Date startDate = null;
        final Date today = CalendarSupportHelper.get(ctx).getDateWithNoTimeOfDay(new Date());
        if (bean == null)
        {
            try
            {
                bean = (SubscriberServices) XBeans.instantiate(SubscriberServices.class, ctx);
            }
            catch (Exception e)
            {
                new MinorLogMsg(SubscribersApiSupport.class, "Error instantiating new subscriber services object.  Using default constructor.", e).log(ctx);
                bean = new SubscriberServices();
            }
            
            if (!onCreate)
            {
                bean.setSubscriberId(subscriber.getId());
            }
            bean.setServiceId(optionID);
            bean.setMandatory(false);
            bean.setServicePeriod(serviceFee.getServicePeriod());
            bean.setIsfeePersonalizationApplied(isPersonalizedFeeSelected);
            bean.setPath(path);
            
            if(service != null && service.getFeePersonalizationAllowed() && isPersonalizedFeeSelected)
            {
            	if(personalizedFee!=-1)
            	{
                bean.setPersonalizedFee(personalizedFee);
            	}
            	else
            	{
            	  throw new CRMExceptionFault("Send some fee as PersonalizedFee if isPersonalizedFeeSelected flag is true");	
            	}
            }
           
            if (start != null)
            {
                startDate = CalendarSupportHelper.get(ctx).getDateWithNoTimeOfDay(start);
                if (startDate.equals(today)||startDate.before(today))
                {
                    bean.setProvisionedState(ServiceStateEnum.PROVISIONED);
                }
            }
            else
            {
                startDate = today;
                bean.setProvisionedState(ServiceStateEnum.PROVISIONED);
            }
            
            if(serviceQuantity > 1)
            {
                bean.setServiceQuantity(serviceQuantity);
                bean.setChangedServiceQuantity(Math.abs(bean.getServiceQuantity() - serviceQuantity));
            }
            
            bean.setStartDate(startDate);
        }

        if (end != null)
        {
            bean.setEndDate(end);
        }
        else
        {
        	
        	if (bean.getService() != null && ServicePeriodEnum.ONE_TIME.equals(bean.getService().getChargeScheme())){

        		if (bean.getService().getRecurrenceType() == OneTimeTypeEnum.ONE_OFF_FIXED_INTERVAL){


        			if (bean.getService().getFixedInterval() == FixedIntervalTypeEnum.DAYS)
        			{
        				bean.setEndDate(
        						CalendarSupportHelper.get(ctx).findDateDaysAfter(bean.getService().getValidity(),
        								startDate));     
        			}
        			else
        			{
        				bean.setEndDate(
        						CalendarSupportHelper.get(ctx).findDateMonthsAfter(bean.getService().getValidity(),
        								startDate));
        			}
        		}else{
        			bean.setEndDate(bean.getService().getEndDate());
        		}
        	}
        }
        
        if (bean.getEndDate().after(today))
        {
        	ServiceFee2ID sfeeId = new ServiceFee2ID(key, "-");
            subscriber.getServices(ctx).add(sfeeId);
            subscriber.addServiceToIntentToProvisionService(ctx, bean);
        }
    }


    /**
     * Enables an additional mobile number.
     *
     * @param ctx
     *            The operating context.
     * @param subscriber
     *            The subscriber to be updated.
     * @param optionID
     *            Option identifier.
     * @param number
     *            New additional mobile number.
     * @param start
     *            Start date.
     * @param end
     *            End date.
     * @param payments
     *            Number of payments.
     * @throws CRMExceptionFault
     *             Thrown if one or more of the provided options is invalid.
     * @throws HomeException
     *             Thrown if there are problems enabling the additional mobile number.
     */
    public static void enableAdditionalMsisdn(final Context ctx, final Subscriber subscriber, final long optionID,
        final String number, final Date start, final Date end, final int payments) throws CRMExceptionFault,
        HomeException
    {
        enableAuxiliaryService(ctx, subscriber, optionID, start, end, Integer.valueOf(payments), number, false);
    }
    
    public static void enableAuxiliaryService(final Context ctx, final Subscriber subscriber, final long optionID,
            final Date start, final Date end, final Integer payments, final String number, boolean onCreate) throws CRMExceptionFault,
            HomeException
        {
                enableAuxiliaryService(ctx, subscriber,optionID,start,end,payments,number,onCreate);
        }


    /**
     * Enables an auxiliary service.
     *
     * @param ctx
     *            The operating context.
     * @param subscriber
     *            Subscriber to be updated.
     * @param optionID
     *            Option identifier.
     * @param start
     *            Start date.
     * @param end
     *            End date.
     * @param payments
     *            Number of payments.
     * @param number
     *            Mobile number.
     * @throws CRMExceptionFault
     *             Thrown if one or more of the provided options is invalid.
     * @throws HomeException
     *             Thrown if there are problems enabling the auxiliary service.
     */
    public static void enableAuxiliaryService(final Context ctx, final Subscriber subscriber, final long optionID,
        final Date start, final Date end, final Integer payments, final String number, boolean onCreate,boolean isPersonalizedFeeSelected, long personalizedFee) throws CRMExceptionFault,
        HomeException
    {
        final Collection<SubscriberAuxiliaryService> auxServices = subscriber.getAuxiliaryServices(ctx);
        SubscriberAuxiliaryService aux = null;
        
        for (SubscriberAuxiliaryService auxiliaryService : auxServices)
        {
            if (auxiliaryService.getAuxiliaryServiceIdentifier() == optionID)
            {
                aux = auxiliaryService;
                aux.setIsfeePersonalizationApplied(isPersonalizedFeeSelected);
                if(auxiliaryService.getAuxiliaryService(ctx).getFeePersonalizationAllowed() && isPersonalizedFeeSelected)
                {   
                	            if(personalizedFee !=-1)
                	            {
                                 aux.setPersonalizedFee(personalizedFee);
                	            }
                	            else
                	            {
                	            	throw new CRMExceptionFault("Send some fee as PersonalizedFee if isPersonalizedFeeSelected flag is true");	
                	            }
                }
                break;
            }
        }
        Date endDate = end;
        if (aux == null)
        {
            final AuxiliaryService auxSrv = AuxiliaryServiceSupport.getAuxiliaryService(ctx, optionID);
            
            if (auxSrv == null)
            {
                RmiApiErrorHandlingSupport.simpleValidation("optionID", "Invalid auxiliary service identifier.");
            }
            
            try
            {
                aux = (SubscriberAuxiliaryService) XBeans.instantiate(SubscriberAuxiliaryService.class, ctx);
            }
            catch (Exception e)
            {
                new MinorLogMsg(SubscribersApiSupport.class, "Error instantiating new subscriber aux service.  Using default constructor.", e).log(ctx);
                aux = new SubscriberAuxiliaryService();
            }
            aux.setIdentifier(-1);
            aux.setAuxiliaryServiceIdentifier(optionID);
            aux.setType(auxSrv.getType());
            aux.setIsfeePersonalizationApplied(isPersonalizedFeeSelected);
            if(auxSrv.getFeePersonalizationAllowed() && isPersonalizedFeeSelected)
            {   
            	if(personalizedFee != -1)
              {
                aux.setPersonalizedFee(personalizedFee);
              }
               else
               {
            	   throw new CRMExceptionFault("Send some fee as PersonalizedFee if isPersonalizedFeeSelected flag is true");
               }
            
            }
            
            if (!onCreate)
            {
                aux.setSubscriberIdentifier(subscriber.getId());
            }
            auxServices.add(aux);
            endDate=getEndDate(ctx,aux.getStartDate(),end, auxSrv);;
            
            if (start != null)
            {
                aux.setStartDate(start);
            }
        }
        
        if (endDate != null)
        {
            aux.setEndDate(endDate);
        }
        
        if (payments != null)
        {
            if (payments <= 0)
            {
                RmiApiErrorHandlingSupport.simpleValidation("numberOfPayments", "Number of payments must be greater than 0.");
            }
            aux.setPaymentNum(payments.intValue());
        }
        if(aux.getType(ctx) == AuxiliaryServiceTypeEnum.AdditionalMsisdn)
        {
            if (number != null)
            {
                aux.setAMsisdn(number);
            }
            else
            {
                RmiApiErrorHandlingSupport.simpleValidation("parameters", "Must supply the Additional MSISDN to use in (generic) parameters.");
            }
        }
        if (onCreate)
        {
            subscriber.setAuxiliaryServices((List)auxServices);
        }
    }
    
    private static Date getEndDate(final Context ctx,final Date startDate, final Date endDate, Object option1 )
    {

        Date resultEndDate = null;
        if (endDate != null)
        {
            resultEndDate = endDate;
        }
        if (com.redknee.app.crm.bean.AuxiliaryService.class.isAssignableFrom(option1.getClass()))
        {
            AuxiliaryService auxService = (AuxiliaryService) option1;
            
            if (auxService.getChargingModeType() == ServicePeriodEnum.ONE_TIME)
            {
                if ( auxService.getRecurrenceType() == OneTimeTypeEnum.ONE_OFF_FIXED_DATE_RANGE)
                {
                    resultEndDate = auxService.getEndDate();
                }
                else if (auxService.getRecurrenceType() == OneTimeTypeEnum.ONE_OFF_FIXED_INTERVAL)
                {
                	//12112629083
            	if( FixedIntervalTypeEnum.DAYS.equals(auxService.getFixedInterval()) )
                {
                    resultEndDate = CalendarSupportHelper.get(ctx).findDateDaysAfter(auxService.getValidity(), startDate);
                
                }
            	else if ( FixedIntervalTypeEnum.MONTHS.equals(auxService.getFixedInterval()))
            	{
            		resultEndDate = CalendarSupportHelper.get(ctx).findDateMonthsAfter(auxService.getValidity(), startDate);
            	}
            }
           }            
        }
        return resultEndDate;
    }

    /**
     * Modify subscriber object to disable a service, bundle or auxiliary service.
     *
     * @param ctx
     *            the operating context
     * @param subscriber
     *            subscriber to modify
     * @param optionType
     *            what kind of option is to be disabled
     * @param optionID
     *            that ID of the option to disable
     * @throws CRMExceptionFault
     *             if values passed in don't make sense
     * @throws HomeException
     *             if thrown by the underlying CRM calls
     */
    public static void disablePricePlanOption(final Context ctx, final Subscriber subscriber, final PricePlanOptionType optionType,
        final long optionID, final Long secondaryID) throws CRMExceptionFault, HomeException
    {
        final Long serviceId = Long.valueOf(optionID);

        String path = SubscriberServicesUtil.DEFAULT_PATH;
        if(ctx.get(Lookup.PATH) != null && !((String)ctx.get(Lookup.PATH)).trim().isEmpty()){
        	path = (String)ctx.get(Lookup.PATH);
        	Logger.debug(ctx, SubscribersApiSupport.class, "Extracted the value of path from context : [" + path + "]");
        }
        LogSupport.debug(ctx, SubscribersApiSupport.class, "The path to be used is [" + path + "]");
        
        ServiceFee2ID serviceFee2IDKey = new ServiceFee2ID(serviceId, path);

        RmiApiErrorHandlingSupport.validateMandatoryObject(optionType, "optionType");
        
        if (optionType.getValue() == PricePlanOptionTypeEnum.SERVICE.getValue().getValue())
        {
            final PricePlanVersion ppv = subscriber.getRawPricePlanVersion(ctx);
            final Map serviceFees = ppv.getServicePackageVersion(ctx).getServiceFees();
            final ServiceFee2 serviceFee = (ServiceFee2) serviceFees.get(serviceFee2IDKey);
            if (serviceFee == null)
            {
                RmiApiErrorHandlingSupport.simpleValidation("optionID",
                "Specified service is not part of the subscriber Price Plan.");
            }
            else if (serviceFee.getServicePreference() == ServicePreferenceEnum.MANDATORY)
            {
                RmiApiErrorHandlingSupport.simpleValidation("optionID",
                "Specified service is Mandatory in the subscriber Price Plan and cannot be disabled.");
            }
            final Set subscribed = subscriber.getIntentToProvisionServices(ctx);
            Object removed = null;
            for (final Iterator iterator = subscribed.iterator(); iterator.hasNext();)
            {
                final SubscriberServices subService = (SubscriberServices) iterator.next();
                if (subService.getServiceId() == optionID)
                {
                    removed = subService;
                    subscriber.getServices(ctx).remove(serviceFee2IDKey);
                    subscriber.removeServiceFromIntentToProvisionServices(ctx, subService);
                    break;
                }
            }
            if (removed == null)
            {
                RmiApiErrorHandlingSupport.simpleValidation("optionID",
                        "Specified service is not Enabled for the given Subscription.");
            }
        }
        else if (optionType.getValue() == PricePlanOptionTypeEnum.PACKAGE.getValue().getValue())
        {
            RmiApiErrorHandlingSupport
            .simpleValidation(
                    "option",
            "Packages currently cannot be enabled or disabled. updateSubscriptionDisablePricePlanOption() for packages is not supported.");
        }
        else if (optionType.getValue() == PricePlanOptionTypeEnum.BUNDLE.getValue().getValue())
        {
            final Map bundles = SubscriberBundleSupport.getPricePlanBundles(ctx, subscriber);
            final BundleFee fee = (BundleFee) bundles.get(serviceId);
            if (fee == null)
            {
                RmiApiErrorHandlingSupport.simpleValidation("optionID",
                "Specified bundle is not part of the subscriber Price Plan.");
            }
            else if (fee.getServicePreference() == ServicePreferenceEnum.MANDATORY)
            {
                RmiApiErrorHandlingSupport.simpleValidation("optionID",
                "Specified bundle is Mandatory in the subscriber Price Plan and cannot be disabled.");
            }
            final Object removed = subscriber.getBundles().remove(serviceId);
            if (removed == null)
            {
                RmiApiErrorHandlingSupport.simpleValidation("optionID",
                "Specified bundle is not Enabled of the subscriber.");
            }
        }
        else if (optionType.getValue() == PricePlanOptionTypeEnum.AUXILIARY_SERVICE.getValue().getValue())
        {
            final Collection auxServices = subscriber.getAuxiliaryServices(ctx);
            Object removed = null;
            for (final Iterator iterator = auxServices.iterator(); iterator.hasNext();)
            {
                final SubscriberAuxiliaryService auxiliaryService = (SubscriberAuxiliaryService) iterator.next();
                if (auxiliaryService.getAuxiliaryServiceIdentifier() == optionID)
                {
                    if (auxiliaryService.getSecondaryIdentifier() != SubscriberAuxiliaryService.DEFAULT_SECONDARYIDENTIFIER
                            && com.redknee.app.crm.bean.AuxiliaryServiceTypeEnum.MultiSIM.equals(auxiliaryService.getType(ctx)))
                    {
                        // This service will be removed by the primary one (i.e. the one without a secondary identifier)
                        // This service is a SIM specific service, which is handled by the multi-SIM subscription extension
                        if (LogSupport.isDebugEnabled(ctx))
                        {
                            new DebugLogMsg(SubscribersApiSupport.class, 
                                    "Skipping API price plan option processing for SIM-specifc Multi-SIM auxiliary service association [AuxSvcId="
                                    + auxiliaryService.getAuxiliaryServiceIdentifier()
                                    + ",SecondaryId=" + auxiliaryService.getSecondaryIdentifier()
                                    + ",SubId=" + auxiliaryService.getSubscriberIdentifier() + "].", null).log(ctx);
                        }
                        continue;
                    }
                    else if (!AuxiliaryServiceToPricePlanOptionAdapter.isCug(ctx, auxiliaryService.getAuxiliaryService(ctx)) || 
                             (secondaryID!=null && auxiliaryService.getSecondaryIdentifier() == secondaryID.longValue()))
                    {
                        removed = auxiliaryService;
                        iterator.remove();
                        break;
                    }
                }
            }
            if (removed == null)
            {
                RmiApiErrorHandlingSupport.simpleValidation("optionID",
                        "Specified Auxiliary service is not Enabled of the subscriber.");
            }

        }
        else if (optionType.getValue() == PricePlanOptionTypeEnum.AUXILIARY_BUNDLE.getValue().getValue())
        {
            final Map bundles = SubscriberBundleSupport.getPricePlanBundles(ctx, subscriber);
            final BundleFee fee = (BundleFee) bundles.get(serviceId);
            if (fee != null)
            {
                RmiApiErrorHandlingSupport.simpleValidation("optionID",
                "Specified bundle is part of the subscriber Price Plan. Please use BUNDLE optionType.");
            }
            final Object removed = subscriber.getBundles().remove(serviceId);
            if (removed == null)
            {
                RmiApiErrorHandlingSupport.simpleValidation("optionID",
                "Specified bundle is not Enabled of the subscriber.");
            }
        }
    }


    /**
     * Gets the Subscription ID of the created subscription using the MSISDN history.
     *
     * @param ctx
     *            The operating context.
     * @param mobileNumber
     *            Mobile number.
     * @param caller
     *            Caller of this method.
     * @return Whether the subscriber has been created.
     * @throws CRMException
     *             Thrown if there are problems with the look up.
     */
    public static String getCreatedSubscriptionID(final Context ctx, final String ban, final String mobileNumber, final long subscriptionType, final Date creationStartTime, final Object caller)
        throws CRMExceptionFault
    {
        com.redknee.app.crm.bean.core.Msisdn msisdn = MobileNumbersApiSupport.getCrmMsisdn(ctx, mobileNumber, caller);
        if (msisdn != null)
        {
            if (SafetyUtil.safeEquals(ban, msisdn.getBAN())
                    && SafetyUtil.safeCompare(creationStartTime, msisdn.getLastModified()) <= 0)
            {
                final String subId = msisdn.getSubscriberID(ctx, subscriptionType);
                if (subId != null && subId.length() > 0)
                {
                    return subId;
                }
            }
        }

        return null;
    }

    
    public static Subscriber getLatestSubscriberUsingMsisdn(final Context ctx, String msisdn, Integer subscriptionType,
            final Object caller) throws CRMExceptionFault
    {
        Home home = (Home) ctx.get(MsisdnMgmtHistoryHome.class);
        String subscriberId = null;
        Subscriber subscriber = null;
        
        try
        {
            Collection<MsisdnMgmtHistory> col = home.select(ctx, new EQ(MsisdnMgmtHistoryXInfo.TERMINAL_ID, msisdn));
            
            //we get the latest subscriberId in list
            for (MsisdnMgmtHistory msisdnMgmtHistory : col)
            {
                subscriberId = msisdnMgmtHistory.getSubscriberId() == null ? subscriberId : msisdnMgmtHistory
                        .getSubscriberId();
            }
            if (subscriberId == null)
            {
                final String msg = "Unable to find SubscriberId with msisdn = " + msisdn;
                RmiApiErrorHandlingSupport.identificationException(ctx, msg, caller);
            }
            subscriber = SubscriberSupport.getSubscriber(ctx, subscriberId);
            if (subscriber == null)
            {
                final String msg = "Unable to find Subscriber with msisdn = " + msisdn;
                RmiApiErrorHandlingSupport.identificationException(ctx, msg, caller);
            }
            
            if (subscriptionType != null)
            {
                if (subscriber.getSubscriptionType() == subscriptionType.intValue())
                {
                    return subscriber;
                }
            }
            else
            {
                CRMSpid spidObj = SpidSupport.getCRMSpid(ctx, subscriber.getSpid());
                if (subscriber.getSubscriptionType() == spidObj.getDefaultSubscriptionType())
                {
                    return subscriber;
                }
            }
        }
        catch (Exception e)
        {
            final String msg = "Unable to find Subscription with msisdn = " + msisdn;
            RmiApiErrorHandlingSupport.handleQueryExceptions(ctx, e, msg, caller);
        }
        
        return subscriber;
    }

    /**
     * Attempts to clean up any failure.
     *
     * @param ctx
     *            The operating context.
     * @param mobileNumber
     *            The mobile number of the subscriber.
     * @param createdMsisdn
     *            Auto-created mobile number.
     * @param createdCard
     *            Auto-created card package.
     * @param caller
     *            Caller of the method.
     * @return Whether the subscriber was created.
     * @throws CRMException
     *             Thrown if there are problems removing the created objects.
     */
    public static boolean cleanFailure(final Context ctx, final String subId, final Msisdn createdMsisdn,
        final GenericPackage createdCard, final Object caller) throws CRMExceptionFault
    {
        boolean subCreated = (subId != null && subId.length() > 0);
        if (!subCreated)
        {
            if (createdMsisdn != null)
            {
                // msisdn was created, removing
                try
                {
                    MobileNumbersApiSupport.removeCrmMsisdn(ctx, createdMsisdn, caller);
                }
                catch (final CRMExceptionFault e)
                {
                    final String detailMsg = "Error occurred while attempting to Remove newly created Mobile Number. "
                        + e.getFaultMessage().getCRMException().getMessage();
                    e.getFaultMessage().getCRMException().setMessage(detailMsg);
                    throw e;
                }
            }
            if (createdCard != null)
            {
                // card package was created, removing
                try
                {
                    CardPackageApiSupport.removeCrmCardPackage(ctx, createdCard, caller);
                }
                catch (final CRMExceptionFault e)
                {
                    CRMException crmExceptionData = e.getFaultMessage().getCRMException();
                    final String detailMsg = "Error occurred while attempting to Remove newly created Card Package. "
                        + crmExceptionData.getMessage();
                    crmExceptionData.setMessage(detailMsg);
                    throw e;
                }
            }
        }

        return subCreated;
    }

    /**
     * Updates subscriber identifier and spid in a subscription reference.  If the reference doesn't refer
     * to a specific subscription then the IN subscription will be used.
     *
     * @param ctx
     *            The operating context.
     * @param reference
     *            Subscription reference.
     * @param caller
     *            Caller of this function; used for logging purposes only.
     * @throws CRMExceptionFault
     *             Thrown if there are problems looking up the subscriber by MSISDN, if
     *             provided.
     */
    public static void updateReferenceId(final Context ctx,
            final SubscriptionReference reference,
            final Object caller) throws CRMExceptionFault
    {
        validateGetSubscription(ctx, reference);
        try
        {
            String identifier = reference.getIdentifier();
            Subscriber sub = null;
            if (identifier == null || identifier.length() == 0)
            {
                final String number = reference.getMobileNumber();
                Date date = CalendarSupportHelper.get(ctx).calendarToDate(reference.getMobileNumberOwnership());
                if (date == null)
                {
                    date = new Date();
                }

                final int subscriptionType;
                if ( reference.getSubscriptionType() != null )
                {
                    subscriptionType = reference.getSubscriptionType();
                }
                else
                {
                    subscriptionType = RmiApiSupport.getDefaultSubscriptionType(ctx, reference.getSpid(), caller);
                }

                sub = SubscriberSupport.lookupSubscriberForMSISDN(ctx, number, subscriptionType, date);

            }
            else
            {
                sub = SubscriberSupport.getSubscriber(ctx, identifier);
            }

            // validate that the subscription exists and handle if it doesn't
            if (sub == null)
            {
                final String msg = "Subscription with ID/MobileNumber/SubscriptionType = " + reference.getIdentifier() + "/" + reference.getMobileNumber() + "/" + reference.getSubscriptionType();
                RmiApiErrorHandlingSupport.identificationException(ctx, msg, caller);
            }

            identifier = sub.getId();
            reference.setIdentifier(identifier);

            reference.setSpid(sub.getSpid());
        }
        catch (final HomeException e)
        {
            final String msg = "Subscription " + reference;
            RmiApiErrorHandlingSupport.identificationException(ctx, msg, caller);
        }
    }


    /**
     * Returns the subscriber referred by the provided subscriber reference.
     *
     * @param ctx
     *            The operating context.
     * @param reference
     *            Subscription reference.
     * @param caller
     *            Caller of this method.
     * @return The subscriber referred by the provided reference.
     * @throws CRMExceptionFault
     *             Thrown if the subscriber is not found, or if there are errors during
     *             the look-up.
     */
    public static Subscriber getCrmSubscriber(final Context ctx, final SubscriptionReference reference,
        final Object caller) throws CRMExceptionFault
    {
        SubscribersApiSupport.updateReferenceId(ctx, reference, caller);

        Subscriber subscriber = null;
        try
        {
            final EQ condition = new EQ(SubscriberXInfo.ID, reference.getIdentifier());
            Predicate subSelectPredicate = condition;

            Predicate spidPredicate = getSpidSelectPredicate(ctx);

            // if the login user does not have access to all the spids, then he
            // can only select subscribers of its spid
            if (spidPredicate != True.instance())
            {
                subSelectPredicate = new And()
                .add(subSelectPredicate)
                .add(spidPredicate);
            }
            subscriber = HomeSupportHelper.get(ctx).findBean(ctx, Subscriber.class, subSelectPredicate);
        }
        catch (final Exception e)
        {
            final String msg = "Unable to find Subscription with identifier=" + reference.getIdentifier() + ". ";
            RmiApiErrorHandlingSupport.handleQueryExceptions(ctx, e, msg, caller);
        }

        if (subscriber == null)
        {
            final String msg = "Subscription " + reference;
            RmiApiErrorHandlingSupport.identificationException(ctx, msg, caller);
        }

        return subscriber;
    }

    
    public static Predicate getSpidSelectPredicate(Context ctx) throws Exception
    {
       Principal p = (Principal) ctx.get(Principal.class);

       if ( p == null
               || AuthSupport.hasPermission(ctx, new SimplePermission("spid.*"))
               || AuthSupport.hasPermission(ctx, new SimplePermission("*")))
       {
           return (Predicate) True.instance();
       }

       // remove the Principal so that we don't get an infinite
       // recursion trying to validate Spid's
       ctx = ctx.createSubContext();
       ctx.put(Principal.class, null);

       Collection<Spid> spids = HomeSupportHelper.get(ctx).getBeans(ctx, Spid.class, new SpidAwareHomePredicate(p));
       if (spids == null || spids.size() == 0)
       {
           return (Predicate) True.instance();
       }
       else
       {
           Or multipleSpidPredicate =  new Or();
           for (Spid spid : spids)
           {
               if (spids.size() == 1)
               {
                   return new EQ(SubscriberXInfo.SPID, spid.getId());
               }

               multipleSpidPredicate.add(new EQ(SubscriberXInfo.SPID, spid.getId()));
           }
           return multipleSpidPredicate;
       }
    }
    

    /**
     * Returns the subscriber creation template.
     *
     * @param ctx
     *            The operating context.
     * @param sctID
     *            Subscriber creation template identifier.
     * @return The subscriber creation template referred by the provided identifier.
     * @throws CRMExceptionFault
     *             Thrown if the subscriber creation template is not found, or if there
     *             are errors during the look-up.
     */
    public static ServiceActivationTemplate getCrmCreationTemplate(final Context ctx, final long sctID) throws CRMExceptionFault
    {
        ServiceActivationTemplate template = null;
        try
        {
            final Object condition = new EQ(ServiceActivationTemplateXInfo.IDENTIFIER, sctID);
            template = HomeSupportHelper.get(ctx).findBean(ctx, ServiceActivationTemplate.class, condition);
        }
        catch (final Exception e)
        {
            final String msg = "Unable to retrieve Subscription Creation Template " + sctID + ". ";
            RmiApiErrorHandlingSupport.handleQueryExceptions(ctx, e, msg, SubscribersApiSupport.class.getName());
        }

        if (template == null)
        {
            final String msg = "Subscription Creation Template " + sctID;
            RmiApiErrorHandlingSupport.identificationException(ctx, msg, SubscribersApiSupport.class.getName());
        }

        return template;
    }

    /**
     * Validates a subscription reference.
     *
     * @param ctx
     *            The operating context.
     * @param reference
     *            Subscriber reference.
     * @throws CRMException
     *             Thrown if there are problems looking up or verifying the subscriber
     *             reference.
     */
    public static void validateGetSubscription(final Context ctx, final SubscriptionReference reference)
        throws CRMExceptionFault
    {
        if (reference == null)
        {
            RmiApiErrorHandlingSupport.validateMandatoryObject(reference, "subscriptionRef");
        }
        else
        {
            final String identifier = reference.getIdentifier();
            final String number = reference.getMobileNumber();

            if (identifier == null || identifier.length() == 0)
            {
                if (number == null || number.length() == 0)
                {
                    final String msg = "Either provide Subscription identifier or Mobile Number in subscription reference.";
                    RmiApiErrorHandlingSupport.simpleValidation("subscriptionRef", msg);
                }
            }
        }
    }


    /**
     * Validates the parameters provided for subscriber creation.
     *
     * @param profile
     *            Subscriber profile.
     * @param status
     *            Subscriber status.
     * @param rating
     *            Rating information.
     * @param billing
     *            Billing information.
     * @param msisdn
     *            Mobile number.
     * @param cardPackage
     *            Card package.
     * @param sp
     *            Service provider.
     * @param isIndividual
     *            Whether this is an individual subscriber.
     * @throws CRMExceptionFault
     *             Thrown if any of the provided parameters are invalid.
     */
    public static void validateCreateSubscription(final Context ctx, final SubscriptionProfile profile, final SubscriptionStatus status,
        final SubscriptionRating rating, final SubscriptionBilling billing, final Msisdn msisdn,
        final CardPackage cardPackage, final CRMSpid sp, boolean isIndividual, SubscriptionPricePlan options) throws CRMExceptionFault
    {
        final List<ValidationExceptionEntry> validations = new ArrayList<ValidationExceptionEntry>();
        if (profile == null)
        {
            RmiApiErrorHandlingSupport
                .addSimpleValidationEntry(
                    validations,
                    "profile",
                    "SubscriptionProfile is a mandatory parameter and cannot be NULL. For Subscription.createSubscription() call SubscriptionProfile has to be specified");
        }
        else
        {
            if (profile.getAccountID() == null && !isIndividual)
            {
                RmiApiErrorHandlingSupport
                    .addSimpleValidationEntry(
                        validations,
                        "SubscriptionProfile.AccountID",
                        "SubscriptionProfile.AccountID is a mandatory parameter and cannot be NULL. For Subscription.createSubscription() call AccountID has to be specified");
            }
            if (profile.getPaidType() == null && !isIndividual)
            {
                RmiApiErrorHandlingSupport
                    .addSimpleValidationEntry(
                        validations,
                        "SubscriptionProfile.PaidType",
                        "SubscriptionProfile.PaidType is a mandatory parameter and cannot be NULL. For Subscription.createSubscription() call PaidType has to be specified");
            }
            if (profile.getMobileNumber() == null)
            {
                RmiApiErrorHandlingSupport
                    .addSimpleValidationEntry(
                        validations,
                        "SubscriptionProfile.MobileNumber",
                        "SubscriptionProfile.MobileNumber is a mandatory parameter and cannot be NULL. For Subscription.createSubscription() call MobileNumber has to be specified");
            }
            if (profile.getTechnologyType() == null)
            {
                RmiApiErrorHandlingSupport
                    .addSimpleValidationEntry(
                        validations,
                        "SubscriptionProfile.TechnologyType",
                        "SubscriptionProfile.TechnologyType is a mandatory parameter and cannot be NULL. For Subscription.createSubscription() call TechnologyType has to be specified");
            }
            
            TechnologyEnum technology =  RmiApiSupport.convertApiTechnology2Crm(profile.getTechnologyType());
            if (technology.isPackageAware())
            {
                validateCardPackage(profile.getCardPackageID(), cardPackage, validations);   
            }
        }
        if (status == null)
        {
            RmiApiErrorHandlingSupport
                .addSimpleValidationEntry(
                    validations,
                    "status",
                    "SubscriptionStatus is a mandatory parameter and cannot be NULL. For Subscription.createSubscription() call SubscriptionStatus has to be specified");
        }
        else
        {
            // Allow create Subscriptions only for "Available" and "Active" states.
            final SubscriberStateEnum newState = RmiApiSupport.convertApiSubscriberState2Crm(status.getState());
            final SubscriberTypeEnum subType = RmiApiSupport.convertApiPaidType2CrmSubscriberType(profile.getPaidType());
            if (newState.equals(SubscriberStateEnum.AVAILABLE) && subType.equals(SubscriberTypeEnum.POSTPAID))
            {
                RmiApiErrorHandlingSupport
                    .addSimpleValidationEntry(validations, "status",
                        "Can only create Prepaid Subscriptions in the 'Available' State.  Either change the Paid Type or the Profile Status.");
            }
            else if (!newState.equals(SubscriberStateEnum.ACTIVE) && !newState.equals(SubscriberStateEnum.AVAILABLE))
            {
                RmiApiErrorHandlingSupport.addSimpleValidationEntry(validations, "status",
                    "Only 'Available' and 'Active' states are supported for the Creating Subscriptions.");
            }
        }
        if (rating == null)
        {
            if (options == null)
            {
                RmiApiErrorHandlingSupport.addSimpleValidationEntry(validations, "rating",
                        "SubscriptionRating  is required if SubscriberPricePlan options is not provided.");
            }
            else
            {
                com.redknee.util.crmapi.wsdl.v3_0.types.serviceandbundle.PricePlan priceplan = options
                        .getPricePlanDetails();
                if (priceplan == null || priceplan.getIdentifier() == null)
                {
                    RmiApiErrorHandlingSupport.addSimpleValidationEntry(validations, "rating",
                            "SubscriptionRating  is required if SubscriberPricePlan options is not provided.");
                }
            }
        }
        else
        {
            if (options != null)
            {
                Long ratingPricePlanId = rating.getPrimaryPricePlanID();
                com.redknee.util.crmapi.wsdl.v3_0.types.serviceandbundle.PricePlan priceplan = options
                        .getPricePlanDetails();
                if (priceplan != null && priceplan.getIdentifier() != null && ratingPricePlanId != null)
                {
                    if (ratingPricePlanId.longValue() != priceplan.getIdentifier().longValue())
                    {
                        RmiApiErrorHandlingSupport
                                .addSimpleValidationEntry(validations, "rating",
                                        "SubscriptionRating has different priceplanId than the pricePlanId given in the SubscriberPricePlan options.");
                    }
                }
                
                //Validate contract startDate
                //Validate contract Id
            }
        }
        
        if (billing == null)
        {
            RmiApiErrorHandlingSupport
                .addSimpleValidationEntry(
                    validations,
                    "billing",
                    "SubscriptionBilling is a mandatory parameter and cannot be NULL. For Subscription.createSubscription() call SubscriptionBilling has to be specified");
        }
        if (sp != null && profile != null)
        {
            if (profile.getIdentifier() != null && profile.getIdentifier().length() > 0
                && !sp.getAllowToSpecifySubscriberId())
            {
                RmiApiErrorHandlingSupport
                    .addSimpleValidationEntry(
                        validations,
                        "identifier",
                        "Not allowed to specify Subscription Identifier. Requested Service Provider does not allow to specify Subscription Identifier");
            }
            else if (sp.getAllowToSpecifySubscriberId()
                && (profile.getIdentifier() == null || profile.getIdentifier().length() == 0))
            {
                RmiApiErrorHandlingSupport
                    .addSimpleValidationEntry(validations, "identifier",
                        "Subscription Identifier is mandatory. Requested Service Provider requires to specify Subscription Identifier");
            }
        }
        validateAutocreateMsisdn(msisdn, sp, validations);

        validateUpdateCardPackage(cardPackage, sp,(short) profile.getTechnologyType().getValue(), validations);

        if (validations.size() > 0)
        {
            RmiApiErrorHandlingSupport.compoundValidation(validations);
        }
    }


    /**
     * Validates a card package.
     *
     * @param cardID
     *            Card package ID.
     * @param cardPackage
     *            Card package.
     * @throws CRMExceptionFault
     *             Thrown if the card package is invalid.
     */
    public static void validateCardPackage(final String cardID, final CardPackage cardPackage) throws CRMExceptionFault
    {
        final List<ValidationExceptionEntry> validations = new ArrayList<ValidationExceptionEntry>();

        validateCardPackage(cardID, cardPackage, validations);

        if (validations.size() > 0)
        {
            RmiApiErrorHandlingSupport.compoundValidation(validations);
        }
    }


    /**
     * Validates a card package. If the card package is invalid, a
     * {@link ValidationExceptionEntry} is added to <code>validations</code>.
     *
     * @param cardID
     *            Card package ID.
     * @param cardPackage
     *            Card package.
     * @param validations
     *            A list to store any validation failures.
     */
    public static void validateCardPackage(final String cardID, final CardPackage cardPackage,
        final List<ValidationExceptionEntry> validations)
    {
        if (cardID == null || cardID.length() == 0)
        {
            if (cardPackage == null)
            {
                RmiApiErrorHandlingSupport.addSimpleValidationEntry(validations, "newCardPackageID",
                    "Either provide Card Package identifier or CardPackage object.");
            }
        }
    }


    /**
     * Validates a card package for update.
     *
     * @param cardPackage
     *            Card package.
     * @param sp
     *            Service provider.
     * @throws CRMExceptionFault
     *             Thrown if the card package is invalid.
     */
    public static void validateUpdateCardPackage(final CardPackage cardPackage, final CRMSpid sp, final Subscriber sub) throws CRMExceptionFault
    {
        final List<ValidationExceptionEntry> validations = new ArrayList<ValidationExceptionEntry>();

        validateUpdateCardPackage(cardPackage, sp, sub.getTechnology().getIndex(), validations);

        if (validations.size() > 0)
        {
            RmiApiErrorHandlingSupport.compoundValidation(validations);
        }
    }


    /**
     * Validates a card package for update. If the card package is invalid, a
     * {@link ValidationExceptionEntry} is added to <code>validations</code>.
     *
     * @param cardPackage
     *            Card package.
     * @param sp
     *            Service provider.
     * @param validations
     *            A list to store any validation failures.
     */
    public static void validateUpdateCardPackage(final CardPackage cardPackage, final CRMSpid sp, short subTechnologyId,
        final List<ValidationExceptionEntry> validations)
    {
        if (sp != null && cardPackage != null)
        {
            if (!sp.getAutoCreateCardPackage())
            {
                RmiApiErrorHandlingSupport
                    .addSimpleValidationEntry(validations, "cardPackage",
                        "Not allowed to auto create Card Package. Requested Service Provider does not allow to auto create Card Package");
            }
            
            if (subTechnologyId != (short) cardPackage.getTechnology().getValue())
            {
                RmiApiErrorHandlingSupport.addSimpleValidationEntry(validations, "cardTechnology",
                        "Not allowed to create Card Package where Subscription Technology [" + subTechnologyId
                                + "] is incompatiable with Card Technology [" + cardPackage.getTechnology() + "]");
            }
        }
    }


    /**
     * Validates a mobile number for auto-creation.
     *
     * @param msisdn
     *            Mobile number.
     * @param sp
     *            Service provider.
     * @throws CRMExceptionFault
     *             Thrown if the mobile number is invalid.
     */
    public static void validateAutocreateMsisdn(final Msisdn msisdn, final CRMSpid sp) throws CRMExceptionFault
    {
        final List<ValidationExceptionEntry> validations = new ArrayList<ValidationExceptionEntry>();

        validateAutocreateMsisdn(msisdn, sp, validations);

        if (validations.size() > 0)
        {
            RmiApiErrorHandlingSupport.compoundValidation(validations);
        }
    }


    /**
     * Validates a mobile number for auto-creation. If the mobile number is invalid, a
     * {@link ValidationExceptionEntry} is added to <code>validations</code>.
     *
     * @param msisdn
     *            Mobile number.
     * @param sp
     *            Service provider.
     * @param validations
     *            A list to store any validation failures.
     */
    private static void validateAutocreateMsisdn(final Msisdn msisdn, final CRMSpid sp,
        final List<ValidationExceptionEntry> validations)
    {
        if (sp != null)
        {
            if (msisdn == null && !sp.getAutoCreateMSISDN())
            {
                RmiApiErrorHandlingSupport
                    .addSimpleValidationEntry(validations, "newMobileNumber",
                        "Not allowed to auto create Mobile Numbers. Requested Service Provider does not allow to auto create Mobile Numbers.");
            }
        }
    }


    /**
     * TT 8020100034: API needs to allow creation of Active Prepaid Subscriptions.
     * DestinationStateByTypeCreateHome in the Subscriber Pipeline forces Prepaid
     * Subscriptions to be created in Available State. This method will verify if the
     * Subscription has to be Activated and performs the activation.
     *
     * @param sub
     *            Subscriber that is persistent in the system.
     * @param profile
     *            Subscription Profile in the API request
     * @param status
     *            Subscription state in the API request
     * @throws CRMExceptionFault
     */
    public static void validateStateAfterCreateSubscription(Context ctx, final Subscriber sub,
        final SubscriptionProfile profile, final SubscriptionStatus status) throws CRMExceptionFault
    {
        /* The SubscriberPipeLineContextPrepareHome.cleanupContext() method does nothing.
         * Since implementing cleanup of Lookup.OLDSUBSCRIBER impacts too many other
         * features, we will instead make sure to pass in a "clean" sub-Context.*/
        ctx = ctx.createSubContext();
        Subscriber resultSub = sub;
        /* TT 8031100028: Change State is not being propagated to External applications.
         * The SubscriberServiceParameterUpdateHome.store method prevents updates to the External Application
         * profile if the external profile has "just been provisioned".  Discussions with other developers
         * have turned up a few Use Cases that require multiple passes through the Subscriber pipeline.
         * Without this "just provisioned" check, any update after the first external profile update might
         * fail.  Also, I suppose it could be considered an "optimization", but only if everyone is aware that
         * subsequent saves with the same Subscriber reference ignore updates to external profiles.
         * To accurately update the parameters to provisioned Services, one must reset the
         * Subscriber.transientProvisionedServices. */
        resultSub.resetTransientProvisionedServices();
        try
        {
            PaidType paidType = profile.getPaidType();
            SubscriptionState state = status.getState();
            
            if (paidType != null && state != null 
                    && paidType.getValue() == PaidTypeEnum.PREPAID.getValue().getValue()
                    && state.getValue() == SubscriptionStateEnum.ACTIVE.getValue().getValue())
            {
                // Activate Subscriber
                resultSub = updateSubscriptionState(
                        ctx, 
                        resultSub, 
                        RmiApiSupport.convertApiSubscriberState2Crm(state));
            }
        }
        catch (final CRMExceptionFault e)
        {
            throw e;
        }
        catch (final Exception e)
        {
            final String msg = "Failed to create Subscription in the Active state. Subscription " + profile + ". ";
            RmiApiErrorHandlingSupport.handleCreateExceptions(ctx, e, msg, true, Subscriber.class, resultSub.getId(), SubscribersApiSupport.class.getName());
        }
    }

    
    /**
     * Returns true if request is for Price Plan switch and generic parameter in 'options' 
     * 'DisableSwitchThreshold' is  not set to true. Otherwise, return false.
     *
     */
    public static boolean isSubscriptionPricePlanSwitching(final Context ctx, final Subscriber subscriber,
            final SubscriptionPricePlan options)
    {
        //nothing to validate
        if (subscriber == null || options == null)
        {
            return false;
        }        
        
        final boolean skipThresholdCheck = APIGenericParameterSupport.getParameterBoolean( 
                APIGenericParameterSupport.DISABLE_SWITCH_THRESHOLD, options.getParameters());
        
        if (options.getIsSelected() && !skipThresholdCheck)
        {
            final PricePlan pp = options.getPricePlanDetails();
            if (pp != null && pp.getIdentifier() != subscriber.getPricePlan())
            {
                return true;
            }
        }
        
        return false;
    }


    /**
     * Update the subscriber's state.
     *
     * @param ctx
     *            The operating context.
     * @param subscriber
     *            Subscription to be updated.
     * @param state
     *            New state of the subscriber.
     * @return The updated subscriber.
     * @throws Exception
     *             THrown if there are problems updating the subscriber.
     */
    public static Subscriber updateSubscriptionState(final Context ctx, final Subscriber subscriber,
        final SubscriberStateEnum state) throws Exception
    {
        final Home home = RmiApiSupport.getCrmHome(ctx, SubscriberHome.class, SubscribersImpl.class);

        if (subscriber.isInFinalState())
        {
            RmiApiErrorHandlingSupport.simpleValidation("subscriptionRef",
                "Subscription is in a closed state. Cannot update subscriber.");
        }

        if (subscriber.getStateWithExpired() == state)
        {
            RmiApiErrorHandlingSupport.simpleValidation("newState",
                "Subscription is currently in the given State. Cannot change state to the same State.");
        }

        /*
         * [Cindy Wong] TT#8030300020 2008-03-13: validate state transition is allowed for
         * the provided subscriber.
         */
        if (!SubscriberStateTransitionSupport.instance(ctx, subscriber).isManualStateTransitionAllowed(ctx, subscriber,
            state))
        {
            final StringBuilder sb = new StringBuilder();
            sb.append("Subscription transition from ");
            sb.append(subscriber.getStateWithExpired().getDescription());
            sb.append(" State to ");
            sb.append(state.getDescription());
            sb.append(" State is not allowed");
            RmiApiErrorHandlingSupport.simpleValidation("newState", sb.toString());
        }
        subscriber.setState(state);
        return (Subscriber) home.store(ctx, subscriber);
    }
    
    public static SubscriptionReference convertPaidType(Context ctx, SubscriptionReference subscriptionRef, Long deposit,
            Long creditLimit, SubscriberTypeEnum subType, Long subscriptionClass, long pricePlanId, long initialAmount, SubscriptionPricePlan options)
            throws CRMExceptionFault
    {
        if (subType.equals(SubscriberTypeEnum.POSTPAID))
        {
            if (creditLimit != null)
            {
                if (creditLimit < 0)
                {
                    RmiApiErrorHandlingSupport.handleStoreExceptions(ctx, null, "Invalid credit limit amount "
                            + creditLimit, SubscribersApiSupport.class);
                }
            }
            else
            {
                creditLimit = AbstractConvertSubscriptionBillingTypeRequest.DEFAULT_CREDITLIMIT;
            }
            if (deposit != null)
            {
                if (deposit < 0)
                {
                    RmiApiErrorHandlingSupport.handleStoreExceptions(ctx, null, "Invalid deposit amount " + deposit,
                            SubscribersApiSupport.class);
                }
            }
            else 
            {
                deposit = AbstractConvertSubscriptionBillingTypeRequest.DEFAULT_NEWDEPOSITAMOUNT;
            }
            initialAmount = AbstractConvertSubscriptionBillingTypeRequest.DEFAULT_INITIALAMOUNT;
        }
        else if (subType.equals(SubscriberTypeEnum.PREPAID))
        {
            if (initialAmount < 0)
            {
                RmiApiErrorHandlingSupport.handleStoreExceptions(ctx, null, "Invalid initial amount " + creditLimit,
                        SubscribersApiSupport.class);
            }
            creditLimit = AbstractConvertSubscriptionBillingTypeRequest.DEFAULT_CREDITLIMIT;
            deposit = AbstractConvertSubscriptionBillingTypeRequest.DEFAULT_NEWDEPOSITAMOUNT;
        }
        Subscriber subscriber = SubscribersApiSupport.getCrmSubscriber(ctx, subscriptionRef, SubscribersApiSupport.class);
        try
        {
            Account account = subscriber.getAccount(ctx);
            ctx.put(MoveConstants.NO_BILLCYCLE_CHANGE, new Boolean(true));
            MoveRequest request = MoveRequestSupport.getMoveRequest(ctx, subscriber,
                    ConvertSubscriptionBillingTypeRequest.class);
            if (request instanceof ConvertSubscriptionBillingTypeRequest)
            {
                ConvertSubscriptionBillingTypeRequest subRequest = (ConvertSubscriptionBillingTypeRequest) request;
                subRequest.setOldSubscriptionId(subscriber);
                subRequest.setSubscriberType(subType);
                subRequest.setNewBAN(account);
                subRequest.setPricePlan(pricePlanId);
                subRequest.setNewDepositAmount(deposit);
                subRequest.setCreditLimit(creditLimit);
                subRequest.setInitialAmount(initialAmount);
                // change subscription class of subscriber if sent by API through optional generic parameter
                if(subscriptionClass != null)
                {
                	subRequest.setSubscriptionClass(subscriptionClass);
                }
                
                new MoveManager().move(ctx, subRequest);
                subscriber = subRequest.getNewSubscription(ctx);
            }
            
            if (options != null)
            {
                PricePlanOption[] pricePlanOptions = options.getItems();
                if (pricePlanOptions != null && pricePlanOptions.length > 0)
                {
                    SubscribersApiSupport.updatePricePlanOptions(ctx, subscriber, options, false);    
                    final Home home = SubscribersImpl.getSubscriberHome(ctx);
                    Subscriber resultSub = (Subscriber)home.store(ctx, subscriber);
                    handlePostSubscriptionUpdate(ctx,resultSub, true);
                }
            }
        }
        catch (final Exception e)
        {
            final String msg = "Unable to convert subscription " + subscriptionRef.getIdentifier() + "/"
                    + subscriptionRef.getMobileNumber() + "/" + subscriptionRef.getSubscriptionType()
                    + " to " + subType + " paidtype ";
            RmiApiErrorHandlingSupport.handleStoreExceptions(ctx, e, msg, SubscribersApiSupport.class);
        }
        SubscriptionReference result = null;
        if (subscriber != null)
        {
            result = SubscriberToApiAdapter.adaptSubscriberToReference(ctx, subscriber);
        }
        else
        {
            final String msg = "Subscriber is NULL when returning paidtype ";
            RmiApiErrorHandlingSupport.handleStoreExceptions(ctx, null, msg, SubscribersApiSupport.class);
        }
        return result;
    }
    

    /**
     * Any post update handling should be done here for any subscription update
     * @param ctx
     * @param sub
     * @param isCreated
     * @throws CRMExceptionFault
     */
    public static void handlePostSubscriptionUpdate(final Context ctx, final Subscriber sub, boolean isCreated)
            throws CRMExceptionFault
    {
        // Catching any HLR provisionign command failure
        if (sub != null && sub.getLastExp() != null)
        {
            final String msg = "Unable to update Subscription with mobile number " + sub.getMSISDN() + " in account "
                    + sub.getBAN() + ". ";
            RmiApiErrorHandlingSupport.handleCreateExceptions(ctx, sub.getLastExp(), msg, isCreated, Subscriber.class,
                    sub.getId(), SubscribersImpl.class);
        }
    }
    
    public static void fillInInputGenericParametersInContext(Context ctx,
			BundleAdjustment adjustment) 
    {
    	final Map<String, Object> genericParameterMap = APIGenericParameterSupport.createGenericParameterMap(adjustment.getParameters());
    	final String originatingApplication = (String)genericParameterMap.get(APIGenericParameterSupport.ORIGINATING_APPLICATION);
    	if(APIGenericParameterSupport.MVNE.equals(originatingApplication))
    	{
    		URCSGenericParameterHolder paramHolder = new URCSGenericParameterHolder();
    		ctx.put(URCSGenericParameterHolder.class, paramHolder);
    		
            final String tigoInformation = (String)genericParameterMap.get(APIGenericParameterSupport.TIGO_INFORMATION);

            if(tigoInformation != null)
    		{
    			Parameter infoParam = new Parameter();

    			infoParam.parameterID = ParameterID.IN_TIGO_INFORMATION;
    			ParameterValue paramValue = new ParameterValue();
    			paramValue.stringValue(tigoInformation);
    			infoParam.value = paramValue;
    			paramHolder.addInputParameter(infoParam);
    		}
    		
    		final short extensionDays = (Short)genericParameterMap.get(APIGenericParameterSupport.EXTENSION_DAYS);
    		if(extensionDays > 0)
    		{
    			Parameter extensionDaysParam = new Parameter();

    			extensionDaysParam.parameterID = ParameterID.IN_EXPIRY_EXTENSION;
    			ParameterValue paramValue = new ParameterValue();
    			paramValue.shortValue(extensionDays);
    			extensionDaysParam.value = paramValue;
    			paramHolder.addInputParameter(extensionDaysParam);
    		}
    		
    	}
    }
    
    public static void verifyBundleCallExecution(Context ctx, BundleAdjustmentResult result) 
    {
    	long crmExceptionFaultCode;
		Object rc = ctx.get(BundleManagerPipelineConstants.BM_RESULT_CODE);
    	if(rc != null && rc instanceof Integer)
    	{
    		int bMErrorCode = (Integer) rc;

    		switch(bMErrorCode)
    		{
    		case ErrorCode.SUCCESS :
    			crmExceptionFaultCode = -1;
    			break;
    			
    		case ErrorCode.OCG_TRANSACTION_TIMEDOUT :
    			crmExceptionFaultCode = (int)ExceptionCode.TRANSACTION_TIMED_OUT;
    			break;

    		case ErrorCode.OCG_NOT_ENOUGH_BAL :
    			crmExceptionFaultCode = (int)ExceptionCode.INSUFFICIENT_BALANCE;
    			break;

    		default :
    			crmExceptionFaultCode = ExceptionCode.GENERAL_EXCEPTION;
    			break;
    		}
    	}
    	else
    	{
    		crmExceptionFaultCode = ExceptionCode.GENERAL_EXCEPTION;
    	}

    	if(crmExceptionFaultCode != -1)
    	{
    		final CRMException newException = new CRMException();
    		newException.setCode(crmExceptionFaultCode);
    		newException.setMessage("Unable to apply bundle adjustment: ");
    		result.setException(newException); 
    	}
    }
    
    public static  void fillInOutputGenericParametersFromContext(Context ctx,
    		BundleAdjustment adjustment) 
    {
    	URCSGenericParameterHolder paramHolder = (URCSGenericParameterHolder) ctx.get(URCSGenericParameterHolder.class);
    	if(paramHolder != null)
    	{
    		Parameter authParam = paramHolder.getOutputParameter(ParameterID.OUT_TIGO_AUTHORIZATION);
    		
    		if(authParam != null)
    		{
    			String authorizationCode = authParam.value.stringValue();
    			adjustment.addParameters(APIGenericParameterSupport
    					.getTransactionRNTigoAuthorizationCode(ctx, authorizationCode));
    		}
    		
    		Parameter tigoTransactionId = paramHolder.getOutputParameter(ParameterID.OUT_TIGO_TRANSACTION_ID);
    		if(tigoTransactionId != null)
    		{
    			String tigoTxnIdValue = tigoTransactionId.value.stringValue();
    			adjustment.addParameters(APIGenericParameterSupport
    					.getTransactionRNTigoTransactionId(ctx, tigoTxnIdValue));
    		}
    	}
    }
    
    /**
     * Ported out MSISDN must be held indefinitely. State (In-Held), PortingType (Port-In)
     * @param ctx
     * @param subscription
     * @throws HomeException
     * @throws AgentException
     */
    public static void handleSubscriptionPortOut(Context ctx, Subscriber subscription) throws HomeException, AgentException
    {
        final String ban = subscription.getBAN();
        final String msisdn = subscription.getMsisdn();
        MsisdnManagement.portOutMsisdn(ctx, msisdn, ban);
        ERLogger.logPortOutER(ctx, subscription);
        final HomeSupport homeSupport = HomeSupportHelper.get(ctx);
        String logMessage = "Deactivation of Account [" + ban + "] with ported Subscription with MSISDN [" + msisdn
                + "]";
        if (homeSupport.hasBeans(
                ctx,
                Subscriber.class,
                new And().add(new EQ(SubscriberXInfo.BAN, ban)).add(
                        new NEQ(SubscriberXInfo.STATE, SubscriberStateEnum.INACTIVE))))
        {
            new InfoLogMsg(SubscribersApiSupport.class, logMessage
                    + " has been skipped because there are non-deactivated Subscriptions in Account", null)
                    .log(ctx);
        }
        else
        {
            try
            {
                Account acct = homeSupport.findBean(ctx, Account.class, subscription.getBAN());
                acct.setState(AccountStateEnum.INACTIVE);
                homeSupport.storeBean(ctx, acct);
            }
            catch (Throwable t)
            {
                new MinorLogMsg(SubscribersApiSupport.class, logMessage + " has failed. Operation however will continue.", t).log(ctx);
            }
        }
    }
    
    public static List<SubscriptionContractStatus> getSubscriptionContracts(Context ctx, SubscriptionReference subRef, GenericParameter[] parameters) throws CRMExceptionFault
    {
        List<SubscriptionContractStatus> results = new ArrayList<SubscriptionContractStatus>();
        Subscriber subscriber = SubscribersApiSupport.getCrmSubscriber(ctx, subRef, SubscribersApiSupport.class);
        try
        {
            Collection<SubscriptionContract> contracts = HomeSupportHelper.get(ctx).getBeans(ctx, SubscriptionContract.class, new EQ(SubscriptionContractXInfo.SUBSCRIPTION_ID, subscriber.getId()));
            for (SubscriptionContract subscriptionContract : contracts)
            {
                
                if(SubscriptionContractSupport.isDummyContract(ctx, subscriptionContract.getContractId()))
                {
                   continue; 
                }
                SubscriptionContractStatus scs = new SubscriptionContractStatus();
                scs.setContractId(subscriptionContract.getContractId());
                scs.setStartDate(subscriptionContract.getContractStartDate());
                scs.setEndDate(subscriptionContract.getContractEndDate());
                scs.setCancellationFee(SubscriptionContractSupport.getCurrentPenaltyFee(ctx, subscriber, subscriptionContract, CalendarSupportHelper.get(ctx).getDateWithNoTimeOfDay(new Date()), subscriptionContract.getContractTerm(ctx).isProrateCancellationFees()));
                
                if(subscriptionContract.getPenaltyFeePerMonth() > 0l)
                {
                	scs.setPenaltyFeePerMonth(subscriptionContract.getPenaltyFeePerMonth());
                }
                else
                {
                	scs.setPenaltyFeePerMonth(subscriptionContract.getContractTerm(ctx).getPenaltyFeePerMonth());
                }
                
                scs.setCancellationIsProrated(subscriptionContract.getContractTerm(ctx).isProrateCancellationFees());
                
                if(subscriptionContract.getSubsidyAmount() > 0l)
                {
                	scs.setSubsidy(subscriptionContract.getSubsidyAmount());
                }
                else
                {
                	scs.setSubsidy(subscriptionContract.getContractTerm(ctx).getSubsidyAmount());
                }
                
                GenericParameter[] outGenericParameters = {};
                List<GenericParameter> genericParamList = new ArrayList<GenericParameter>();
                
                if(subscriptionContract.getContractEndDate() != null)
                {
                    String durationArray[] = SubscriptionContract.remainingContractTermDuration(ctx, new Date(), subscriptionContract.getContractStartDate(), subscriptionContract.getContractEndDate(), subscriptionContract.getContractTerm(ctx).isProrateCancellationFees());
                    if(durationArray.length > 0)
                    {
                    	
                    	GenericParameter[] genericParameters = subRef.getParameters();
                    	
                    	
                    	boolean isContractInDaysRequired = isContractInDaysRequired(new GenericParameterParser(genericParameters));
                    	
                    	genericParamList.add(RmiApiSupport.createGenericParameter(APIGenericParameterSupport.CONTRACT_REMAINING_YEARS,
                    			isContractInDaysRequired?0:Integer.parseInt(durationArray[0])));
                        
                    	genericParamList.add(RmiApiSupport.createGenericParameter(APIGenericParameterSupport.CONTRACT_REMAINING_MONTHS,
                    			isContractInDaysRequired?0:Integer.parseInt(durationArray[1])));
                        
                    	//Calculate Contract period in Days
                    	int contractRemainingDays = isContractInDaysRequired?
                    			(int) CalendarSupportHelper.get(ctx).getNumberOfDaysBetween(new Date(),subscriptionContract.getContractEndDate()):Integer.parseInt(durationArray[2]);
                        
                    	genericParamList.add(RmiApiSupport.createGenericParameter(APIGenericParameterSupport.CONTRACT_REMAINING_DAYS, contractRemainingDays));
                    
                    	
                    }
                    
                   genericParamList.add(RmiApiSupport.createGenericParameter(APIGenericParameterSupport.SUBSCRIBER_SUSPEND_PERIOD,
            		subscriptionContract.getSuspensionPeriod()));
                }
                if(subscriptionContract.getDeviceProductID() > 0l)
                {
                	genericParamList.add(RmiApiSupport.createGenericParameter(APIGenericParameterSupport.DEVICE_PRODUCT_ID, 
                			subscriptionContract.getDeviceProductID()));
                    
                }
                
                if(genericParamList.size() > 0)
                {
                    outGenericParameters = genericParamList.toArray(new GenericParameter[genericParamList.size()]);
                    scs.setParameter(outGenericParameters);
                }
                
                results.add(scs);
            }
        }
        catch (final Exception e)
        {
            final String msg = "Unable to process subscription contracts for subscriber " + subscriber.getId() + ". ";
            RmiApiErrorHandlingSupport.handleQueryExceptions(ctx, e, msg, SubscribersApiSupport.class);
        }
        
        return results;
    }
    
    /**
     * 
     * Retrieve balance for every Secondary Balance bundle which is provisioned to this subscription.
     * 
     * @param ctx
     * @param subscriber
     * @return
     * @throws OcgTransactionException 
     */
    public static List<CategoryIdBalanceMapper> getSubscriptionSecondaryBalanceList(Context ctx, Subscriber subscriber)
    	throws Exception
    {
    	Map<Long, BundleFee> bundleFeeMap = subscriber.getBundles();    	
    	Collection<BundleFee> bundleFeeCollection = bundleFeeMap.values();
    	
    	List<Integer> categoryIdList = new ArrayList<Integer>();
    	int spid = subscriber.getSpid();
    	
    	for(BundleFee bundleFee : bundleFeeCollection)
    	{
    		BundleProfile bundleProfile = bundleFee.getBundleProfile(ctx, spid);	
    		
    		if(bundleProfile.isSecondaryBalance(ctx))
    		{
    			categoryIdList.add(bundleProfile.getBundleCategoryId());
    		}
    	}
    	
    	return getSubscriptionSecondaryBalanceList(ctx, subscriber, categoryIdList);
    }
    
    /**
     * 
     * Retrieve balance for every Secondary Balance bundle belonging to categoryId list passed. This is done by recursively sending requestBalance
     * calls to OCG for every categoryId.
     * 
     * @param ctx
     * @param subscriber
     * @param categoryId
     * @return
     * @throws OcgTransactionException
     */
    public static List<CategoryIdBalanceMapper> getSubscriptionSecondaryBalanceList(Context ctx, Subscriber subscriber, List<Integer> categoryIdList)
    
    	throws OcgTransactionException
    {
    	List<CategoryIdBalanceMapper> secondaryBalanceList = new ArrayList<CategoryIdBalanceMapper>();
    	
    	if( !(categoryIdList == null || categoryIdList.size() ==0) )
    	{
    		
    		OcgGenericParameterHolder ocgParamHolder = (OcgGenericParameterHolder)ctx.get(OcgGenericParameterHolder.class);
    		
            if(ocgParamHolder == null)
            {
            	ocgParamHolder =  new OcgGenericParameterHolder();
            	ctx.put(OcgGenericParameterHolder.class,ocgParamHolder);
            }
            
            AppOcgClient client = (AppOcgClient)ctx.get(AppOcgClient.class);
            
            if(client == null)
            {
            	OcgTransactionException exception = new OcgTransactionException("AppOcgClient is not initialized in AppCrm", com.redknee.product.s2100.ErrorCode.INTERNAL_ERROR);
            	LogSupport.debug(ctx, SubscribersApiSupport.class.getName(), "requestBalance Error", exception);
            	throw exception;
            }
            
            String msisdn = subscriber.getMsisdn();
            SubscriberTypeEnum subType = subscriber.getSubscriberType();
            String currencyType = subscriber.getCurrency(ctx);
            long subscriptionType = subscriber.getSubscriptionType();
            
            StringBuilder erReferenceBuilder = new StringBuilder();
            erReferenceBuilder.append(SECONDARY_BALANCE_ER_REFERENCE);
            erReferenceBuilder.append(msisdn); 
            
            for(int category : categoryIdList)
            {
            	com.redknee.product.s2100.oasis.param.Parameter inParam = new com.redknee.product.s2100.oasis.param.Parameter();
	            inParam.parameterID = com.redknee.product.s2100.oasis.param.ParameterID.SUBSCRIPTION_TYPE;
	            inParam.value = new com.redknee.product.s2100.oasis.param.ParameterValue();
	            inParam.value.intValue(category);
	            
	            ocgParamHolder.addInputParameter(inParam);
	            
	            LongHolder balance = new LongHolder();
	            
	            int result = IntValue.ZERO;
	            
	            result = client.requestBalance(msisdn, subType, currencyType, BooleanValue.FALSE, 
	            			erReferenceBuilder.toString(), subscriptionType, balance, new LongHolder(), new LongHolder());
	            
	            if (result != com.redknee.product.s2100.ErrorCode.NO_ERROR)
	            {

                    StringBuilder msg = new StringBuilder("Failed to query Balance for msisdn:" + msisdn + " for category ids:" + category);

                    msg.append(S2100ReturnCodeMsgMapping.getMessage(result));
                    final OcgTransactionException exception = new OcgTransactionException(msg.toString(), result);
                    if (LogSupport.isDebugEnabled(ctx))
                    {
                        LogSupport.debug(ctx, SubscribersApiSupport.class.getName(), "requestBalance Error", exception);
                    }
                    
                    CategoryIdBalanceMapper secondaryBalanceMapper = new CategoryIdBalanceMapper(category,
                    		LongValue.ARBITRARY_NEGATIVE_LONG, msisdn, exception);
                    
                    secondaryBalanceList.add(secondaryBalanceMapper);
	            }	
	            else
	            {
                    CategoryIdBalanceMapper secondaryBalanceMapper = new CategoryIdBalanceMapper(category,
                    		balance.value, msisdn, null);
                    secondaryBalanceList.add(secondaryBalanceMapper);
	            }
            }
    	}
    	
    	return secondaryBalanceList;
    }
    
    
    private static boolean isContractInDaysRequired(GenericParameterParser paramsParser) throws CRMExceptionFault
    {
        return paramsParser.containsParam(APIGenericParameterSupport.CONTRACT_IN_DAYS) && 
        		paramsParser.getParameter(APIGenericParameterSupport.CONTRACT_IN_DAYS, Boolean.class, 
                Boolean.FALSE).booleanValue();
    }
    
    /**
     * Refund the services.
     *
     * @param ctx
     *            The operating context.
     * @param sub
     *            The subscriber to be refund.
     * @param services
     *            The services the subscriber is refund for.
     */
    private static void handleRefundOnQuntityDecrease(final Context ctx, final Subscriber sub, SubscriberServices subService, long serviceQuantity) throws HomeException
    {
        ctx.put(IS_SERVICE_QUANTITY_CHANGED, new BooleanHolder(true));
    	subService.setChangedServiceQuantity(subService.getServiceQuantity() - serviceQuantity);
    	subService.setServiceQuantity(serviceQuantity);
    	// During charging loop, we call SPG with provisionAction flag as 1 = UNIFIED_ONLY (for BSS and unified charged services)
    	// which is done by this charger through ServiceChargingSupport
    	int action = ChargingConstants.ACTION_UNPROVISIONING_REFUND;
        handleChargeOnQuantityChange(ctx, sub, subService, action);    
    }
    public static void handleRefundOnQuntityDecrease(final Context ctx, final Subscriber sub, SubscriberServices subService) throws HomeException
    {
    	// During charging loop, we call SPG with provisionAction flag as 1 = UNIFIED_ONLY (for BSS and unified charged services)
    	// which is done by this charger through ServiceChargingSupport
    	int action = ChargingConstants.ACTION_UNPROVISIONING_REFUND;
        handleChargeOnQuantityChange(ctx, sub, subService, action);    
    }
    
    
    
    /**
     * 
     * @param ctx
     * @param sub
     * @param subService
     * @param serviceQuantity
     */
    private static void handleChargeWithIncreasedQuantity(final Context ctx, final Subscriber sub, SubscriberServices subService, long serviceQuantity) throws HomeException
    {
    	ctx.put(IS_SERVICE_QUANTITY_CHANGED, new BooleanHolder(true));
    	subService.setChangedServiceQuantity(serviceQuantity - subService.getServiceQuantity());
    	subService.setServiceQuantity(serviceQuantity);
    	// During charging loop, we call SPG with provisionAction flag as 1 = UNIFIED_ONLY (for BSS and unified charged services)
    	// which is done by this charger through ServiceChargingSupport
    	
    	// use default handler
    	int action = ChargingConstants.ACTION_PROVISIONING_CHARGE;
    	handleChargeOnQuantityChange(ctx, sub, subService, action);
    	    
    }
    public static void handleChargeWithIncreasedQuantity(final Context ctx, final Subscriber sub, SubscriberServices subService) throws HomeException
    {
    	// During charging loop, we call SPG with provisionAction flag as 1 = UNIFIED_ONLY (for BSS and unified charged services)
    	// which is done by this charger through ServiceChargingSupport
    	// use default handler
    	int action = ChargingConstants.ACTION_PROVISIONING_CHARGE;
    	handleChargeOnQuantityChange(ctx, sub, subService, action);
    	    
    }
    
    private static void handleChargeOnQuantityChange(final Context ctx, final Subscriber sub, SubscriberServices subService,int action) throws HomeException
    {
        ctx.put(ServiceChargingSupport.SKIP_UNIFIED_PROVISIONING_AND_CHARGING, Boolean.TRUE);
        ChargableItemResult ret = null;
        Collection subServices = new HashSet();
        subServices.add(subService);
        Map services = ServiceChargingSupport.getProvisionedServices(ctx,
                PricePlanSupport.getVersion(ctx, sub.getPricePlan(), sub.getPricePlanVersion()).getServiceFees(ctx)
                        .values(), subServices);
        List<ServiceFee2> serviceFees = new ArrayList(services.keySet());
        ServiceFee2 fee = null;
        for (ServiceFee2 servicefee : serviceFees)
        {
            if (servicefee.getServiceId() == subService.getServiceId())
            {
                fee = servicefee;
                break;
            }
        }
        ret = new ChargableItemResult(action, ChargingConstants.CHARGABLE_ITEM_SERVICE, fee, fee.getServiceId(), sub,
                sub);
        ret.setChargableObjectRef(subService.getService());
        ret.setAction(action);
        ret.isActivation = false;
        ret.setSkipValidation(true);
        ret.setChargeResult(ServiceChargingSupport.getEventType(ctx, sub, fee, subService.getService(), action));
        ServiceChargingSupport.chargeOrRefund(ctx, sub, sub, action, ChargingConstants.RUNNING_SUCCESS, fee,
                subService, subService.getService(), ret);
    }
    
    
    /**
     * If serviceQauntity in generic param is greater than 1 then this method will update the subscriberService bean with given quantity.
     * 
     * @param ctx
     * @param subscriber
     * @param optionID
     * @throws IllegalArgumentException
     * @throws HomeException
     * @throws CRMExceptionFault
     */
    private static void updateMandatoryServiceQuantity(final Context ctx,
            final Subscriber subscriber, final long optionID) throws IllegalArgumentException, HomeException,CRMExceptionFault
    {
                long serviceQuantity = 1;
                if (ctx.has(Lookup.SERIVCE_QUANTITY))
                {
                                serviceQuantity = (Long)ctx.get(Lookup.SERIVCE_QUANTITY);
                }
                if(serviceQuantity >1)
                {
                                final Set subscribed = subscriber.getIntentToProvisionServices(ctx);
            SubscriberServices bean = null;
            for (final Iterator iterator = subscribed.iterator(); iterator.hasNext();)
            {
                final SubscriberServices subService = (SubscriberServices) iterator.next();
                if (subService.getServiceId() == optionID)
                {
                    bean = subService;
                    bean.setServiceQuantity(serviceQuantity);
                    bean.setChangedServiceQuantity(Math.abs(bean.getServiceQuantity() - serviceQuantity));
                    break;
                }
            }
            
            if(bean==null)
            {
                RmiApiErrorHandlingSupport.simpleValidation("Service is Mandatory.","Mandatory Services can not be newly added like this.");
            }  
                }
    }
    
    public static Subscriber disableServiceForChangeIndicator(final Context ctx, final Subscriber subscriber, final SubscriptionPricePlan options, boolean onCreate, ApiResultHolder resHolder) 
    	    throws CRMExceptionFault, HomeException
	{
    	if (LogSupport.isDebugEnabled(ctx)) {
			Logger.debug(ctx, SubscribersApiSupport.class,">>Entering disableServiceForChangeIndicator method and subscriber:: "+subscriber
					+"SubscriptionPricePlan:: "+options+" onCreate:: "+onCreate+" ApiResultHolder:: "+resHolder);
		}
		if (options != null) {
			if (options.getIsSelected()) {
				PricePlan pp = options.getPricePlanDetails();
				PricePlanOption[] subOptions = options.getItems();

				boolean isPPChange = false;

				if (subOptions != null) {
					String changeIndicator = null;
					for (PricePlanOption option : subOptions) {
						if (option == null) {
							continue;
						}

						resHolder.putPriceplanOptionUpdateApiResultsHolder(option.getIdentifier(),
								option.getOptionType(), new ApiOptionResultHolder());

						GenericParameter[] parameters = option.getParameters();

						GenericParameterParser paramsParser = new GenericParameterParser(parameters);
						if (option.getIsSelected()) {
							if (paramsParser.containsParam(APIGenericParameterSupport.CHANGE_INDICATOR)) {
								changeIndicator = paramsParser.getParameter(APIGenericParameterSupport.CHANGE_INDICATOR,
										String.class);
							}

							// If change indicator is 'MODIFIED', then
							// unprovision the service first and then
							// reprovision again

							if (changeIndicator != null) {
								if (changeIndicator
										.equals(APIGenericParameterSupport.CHANGE_INDICATOR_OPTION_MODIFIED)) {
									String ppId = paramsParser.getParameter(APIGenericParameterSupport.PRICEPLAN_ID,
											String.class);
									if (ppId != null
											&& (paramsParser.containsParam(APIGenericParameterSupport.PRICEPLAN_ID)
													&& Long.parseLong(ppId) != pp.getIdentifier())
											&& !SubscribersApiSupport.isServiceGettingReplaceInPPChange(ctx, subscriber,
													option.getIdentifier())) {
										handleServiceRemoveofOldPPInPPChangeRequest(ctx, subscriber, paramsParser, pp,
												option, ppId);
									} else {

										disablePricePlanOption(ctx, subscriber, option, resHolder,
												option.getParameters());
									}

								} else {
									RmiApiErrorHandlingSupport.simpleValidation("changeIndicator",
											"Specified value" + changeIndicator
													+ " is not a valid value for generic parameter "
													+ APIGenericParameterSupport.CHANGE_INDICATOR);
								}

								// adding the change indicator value to a map in
								// the context against the option identifier
								// (subscriber service id)
								addChangeIndicatorToCtxMap(ctx, option.getIdentifier(), changeIndicator
										.equals(APIGenericParameterSupport.CHANGE_INDICATOR_OPTION_MODIFIED));
							} else {
								// adding the change indicator value as false to
								// a map in
								// the context against the option identifier
								// (subscriber service id)
								addChangeIndicatorToCtxMap(ctx, option.getIdentifier(), false);
							}
						}
					}
				}
			} else {
				final String msg = "Not applying the options because IsSelected is false for Subscription with ID/MobileNumber/SubscriptionType = "
						+ subscriber.getId() + "/" + subscriber.getMSISDN() + "/" + subscriber.getSubscriptionType();
				RmiApiErrorHandlingSupport.handleStoreExceptions(ctx, null, msg, SubscribersApiSupport.class);
			}
		}

		if (LogSupport.isDebugEnabled(ctx)) {
			Logger.debug(ctx, SubscribersApiSupport.class,">>Existing disableServiceForChangeIndicator method and subscriber:: "+subscriber
					+"SubscriptionPricePlan:: "+options+" onCreate:: "+onCreate+" ApiResultHolder:: "+resHolder);
		}
		return subscriber;
	}
    	    
	private static void addChangeIndicatorToCtxMap(Context ctx, long optionIdentifier, boolean changeIndicatorState) {
		Map<Long, Boolean> changeIndicatorCtxMap = (Map<Long, Boolean>) ctx.get(CHANGE_INDICATOR_MAP);
		if (changeIndicatorCtxMap == null) {
			changeIndicatorCtxMap = new HashMap<Long, Boolean>();
		}
		changeIndicatorCtxMap.put(optionIdentifier, changeIndicatorState);

		ctx.put(CHANGE_INDICATOR_MAP, changeIndicatorCtxMap);
	}
    	    
	private static Map<Long, Boolean> getChangeIndicatorMapFromCtx(Context ctx) {
		Map<Long, Boolean> changeIndicatorCtxMap = (Map<Long, Boolean>) ctx.get(CHANGE_INDICATOR_MAP);
		if (changeIndicatorCtxMap == null) {
			changeIndicatorCtxMap = new HashMap<Long, Boolean>();
		}
		return changeIndicatorCtxMap;
	}
    	    
    
	private static void handleServiceRemoveofOldPPInPPChangeRequest(Context ctx, Subscriber sub, GenericParameterParser paramsParser, 
    		PricePlan pp, PricePlanOption option ,String ppId) throws CRMExceptionFault, HomeException
    
	{
		Properties prop = new Properties();
		long removedServiceId = option.getIdentifier();
		String priceplanParam = paramsParser.getParameter(APIGenericParameterSupport.PRICEPLAN_ID, String.class);
		if(LogSupport.isDebugEnabled(ctx)){
			LogSupport.debug(ctx, SubscribersApiSupport.class.getName(), "Value of priceplanParam:: "+priceplanParam+" removedServiceId:: "+removedServiceId);
		}
		if (null != priceplanParam) {
			prop.put(APIGenericParameterSupport.PRICEPLAN_ID, priceplanParam);
		}

		try {
			sub.addToOldPPServiceRemovePropOnPPChange(removedServiceId, prop);
		} catch (ClassCastException ex) {
			Log.debug(APIGenericParameterSupport.PRICEPLAN_ID + " is passed by DCRM is not Long.", ex);
		}
	}
	
	/**
     *  This method is used while doing PP change whether the service replacing or not from new PP 
     *   under New PP Change Enhancement  
     */
    
    
    public static boolean isServiceGettingReplaceInPPChange(Context ctx, Subscriber newSubscriber, long serviceId){
    	boolean retVal = false;
    	boolean isPPChangeFromDCRM = false;
    	if(ctx.has(Lookup.PRICEPLAN_CHANGE_REQUEST_FROM_DCRM)){
    		isPPChangeFromDCRM = (Boolean)ctx.get(Lookup.PRICEPLAN_CHANGE_REQUEST_FROM_DCRM);
    	}
    	if(isPPChangeFromDCRM){
    		return newSubscriber.getReplacePropForPPChange(serviceId) != null ? true : false;
    	}
    	if(LogSupport.isDebugEnabled(ctx)){
			LogSupport.debug(ctx, SubscribersApiSupport.class.getName(), "Value of isServiceGettingReplaceInPPChange:: "+retVal);
		}
    	return retVal;
    }
    
    public final static String SECONDARY_BALANCE_ER_REFERENCE = "AppCrm-getSubscriptionSecondaryBalance-msisdn:";
  
    public final static Long[] DUMMY_LONG_ARRAY = new Long[0];
    
    public static final String IS_SERVICE_QUANTITY_CHANGED = "isServiceQuantityChanged";
    
    public static final String CHANGE_INDICATOR_MAP = "__CHANGE_INDICATOR_MAP__";
    
    public static String decrementLimitExceeded = "Aux SERVICE QTY Decrement QTY 0";
    
    public static final String Quantity_Support_For_Full_Charge = "QuantitySupportForFullCharge";
}