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
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.trilogy.framework.xhome.beans.DefaultExceptionListener;
import com.trilogy.framework.xhome.beans.ExceptionListener;
import com.trilogy.framework.xhome.beans.SafetyUtil;
import com.trilogy.framework.xhome.beans.XBeans;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgent;
import com.trilogy.framework.xhome.context.ContextAware;
import com.trilogy.framework.xhome.context.ContextLocator;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.In;
import com.trilogy.framework.xhome.elang.True;
import com.trilogy.framework.xhome.filter.Predicate;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.language.MessageMgr;
import com.trilogy.framework.xhome.msp.MSP;
import com.trilogy.framework.xhome.visitor.AbortVisitException;
import com.trilogy.framework.xhome.webcontrol.HTMLExceptionListener;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.app.crm.Common;
import com.trilogy.app.crm.CommonTime;
import com.trilogy.app.crm.api.ApiSupport;
import com.trilogy.app.crm.api.Constants;
import com.trilogy.app.crm.api.queryexecutor.QueryExecutorFactory;
import com.trilogy.app.crm.api.queryexecutor.subscription.SubscriptionQueryExecutors;
import com.trilogy.app.crm.api.rmi.BundleAdjustmentToApiAdapter;
import com.trilogy.app.crm.api.rmi.GenericParameterParser;
import com.trilogy.app.crm.api.rmi.PromotionStatusToApiAdapter;
import com.trilogy.app.crm.api.rmi.SubscriberBucketToApiAdapter;
import com.trilogy.app.crm.api.rmi.SubscriberCreationTemplateToApiAdapter;
import com.trilogy.app.crm.api.rmi.SubscriberToApiAdapter;
import com.trilogy.app.crm.api.rmi.SubscriptionClassToApiAdapter;
import com.trilogy.app.crm.api.rmi.SubscriptionContractTermToApiAdapter;
import com.trilogy.app.crm.api.rmi.SubscriptionLevelToApiAdapter;
import com.trilogy.app.crm.api.rmi.SubscriptionPricePlanOptionToApiAdapter;
import com.trilogy.app.crm.api.rmi.SubscriptionTypeToApiAdapter;
import com.trilogy.app.crm.api.rmi.extensions.SubscriberExtensionToApiAdapter;
import com.trilogy.app.crm.api.rmi.support.APIGenericParameterSupport;
import com.trilogy.app.crm.api.rmi.support.ApiOptionsUpdateResultSupport;
import com.trilogy.app.crm.api.rmi.support.ApiResultHolder;
import com.trilogy.app.crm.api.rmi.support.CardPackageApiSupport;
import com.trilogy.app.crm.api.rmi.support.ExtensionApiSupport;
import com.trilogy.app.crm.api.rmi.support.MobileNumbersApiSupport;
import com.trilogy.app.crm.api.rmi.support.RmiApiErrorHandlingSupport;
import com.trilogy.app.crm.api.rmi.support.RmiApiSupport;
import com.trilogy.app.crm.api.rmi.support.SubscribersApiSupport;
import com.trilogy.app.crm.bas.tps.ServiceActivationTemplate;
import com.trilogy.app.crm.bas.tps.ServiceActivationTemplateXInfo;
import com.trilogy.app.crm.bean.AbstractSubscriber;
import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.Address;
import com.trilogy.app.crm.bean.AddressXInfo;
import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.GroupScreeningTemplate;
import com.trilogy.app.crm.bean.GroupScreeningTemplateHome;
import com.trilogy.app.crm.bean.GroupScreeningTemplateXInfo;
import com.trilogy.app.crm.bean.ManualVourcherRechargingForm;
import com.trilogy.app.crm.bean.MsisdnHome;
import com.trilogy.app.crm.bean.ServiceFee2ID;
import com.trilogy.app.crm.bean.ServicePeriodEnum;
import com.trilogy.app.crm.bean.ServiceSubTypeEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberHome;
import com.trilogy.app.crm.bean.SubscriberServices;
import com.trilogy.app.crm.bean.SubscriberServicesXInfo;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.bean.SubscriberXInfo;
import com.trilogy.app.crm.bean.SupplementaryData;
import com.trilogy.app.crm.bean.SupplementaryDataEntityEnum;
import com.trilogy.app.crm.bean.SupplementaryDataXInfo;
import com.trilogy.app.crm.bean.TDMAPackage;
import com.trilogy.app.crm.bean.core.BundleCategoryAssociation;
import com.trilogy.app.crm.bean.core.BundleProfile;
import com.trilogy.app.crm.bean.core.Msisdn;
import com.trilogy.app.crm.bean.core.PricePlanVersion;
import com.trilogy.app.crm.bean.core.SubscriptionClass;
import com.trilogy.app.crm.bean.core.SubscriptionType;
import com.trilogy.app.crm.bean.priceplan.SubscriptionLevel;
import com.trilogy.app.crm.bean.priceplan.SubscriptionLevelXInfo;
import com.trilogy.app.crm.bean.ui.Service;
import com.trilogy.app.crm.bean.ui.ServiceXInfo;
import com.trilogy.app.crm.bean.usage.BalanceUsage;
import com.trilogy.app.crm.bundle.BundleAdjustmentAgent;
import com.trilogy.app.crm.bundle.BundleManagerPipelineConstants;
import com.trilogy.app.crm.bundle.SubscriberBucket;
import com.trilogy.app.crm.bundle.SubscriberBucketHome;
import com.trilogy.app.crm.bundle.SubscriberBucketXInfo;
import com.trilogy.app.crm.bundle.UnitTypeEnum;
import com.trilogy.app.crm.bundle.exception.BundleManagerException;
import com.trilogy.app.crm.bundle.service.CRMSubscriberBucketProfile;
import com.trilogy.app.crm.client.urcs.PromotionProvisionClient;
import com.trilogy.app.crm.client.urcs.UrcsClientInstall;
import com.trilogy.app.crm.contract.SubscriptionContractSupport;
import com.trilogy.app.crm.contract.SubscriptionContractTerm;
import com.trilogy.app.crm.contract.SubscriptionContractTermXInfo;
import com.trilogy.app.crm.extension.ExtensionHolder;
import com.trilogy.app.crm.extension.subscriber.SubscriberExtension;
import com.trilogy.app.crm.extension.subscriber.SubscriberExtensionHolder;
import com.trilogy.app.crm.home.MsisdnPortHandlingHome;
import com.trilogy.app.crm.home.sub.Claim;
import com.trilogy.app.crm.numbermgn.GenericPackage;
import com.trilogy.app.crm.numbermgn.MsisdnChangeAppendHistoryHome;
import com.trilogy.app.crm.numbermgn.MsisdnManagement;
import com.trilogy.app.crm.priceplan.ScheduledPriceplanChangeExecutor;
import com.trilogy.app.crm.provision.CommonProvisionAgentBase;
import com.trilogy.app.crm.support.BundleSupportHelper;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.app.crm.support.CollectionSupportHelper;
import com.trilogy.app.crm.support.GracefulShutdownSupport;
import com.trilogy.app.crm.support.HomeSupport;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.app.crm.support.Lookup;
import com.trilogy.app.crm.support.PackageSupportHelper;
import com.trilogy.app.crm.support.PricePlanSwitchLimitSupport;
import com.trilogy.app.crm.support.SubscriberServicesSupport;
import com.trilogy.app.crm.support.SubscriberSupport;
import com.trilogy.app.crm.support.SwapPackageSupport;
import com.trilogy.app.crm.technology.TechnologyEnum;
import com.trilogy.app.crm.web.border.SubscriberIdentificationValidator;
import com.trilogy.app.crm.web.service.CSRVoucherRechargeServicer;
import com.trilogy.app.crm.web.service.VoiceMailPINResetServicer;
import com.trilogy.util.crmapi.wsdl.v2_0.exception.CRMException;
import com.trilogy.util.crmapi.wsdl.v2_0.types.BatchResultType;
import com.trilogy.util.crmapi.wsdl.v2_0.types.BatchResultTypeEnum;
import com.trilogy.util.crmapi.wsdl.v2_0.types.GenericParameter;
import com.trilogy.util.crmapi.wsdl.v2_0.types.PaidType;
import com.trilogy.util.crmapi.wsdl.v2_0.types.ResultCodeEnum;
import com.trilogy.util.crmapi.wsdl.v2_0.types.SuccessCode;
import com.trilogy.util.crmapi.wsdl.v2_0.types.SuccessCodeEnum;
import com.trilogy.util.crmapi.wsdl.v2_0.types.TechnologyType;
import com.trilogy.util.crmapi.wsdl.v2_0.types.TechnologyTypeEnum;
import com.trilogy.util.crmapi.wsdl.v2_1.exception.ExceptionCode;
import com.trilogy.util.crmapi.wsdl.v2_1.types.cardpackage.CardPackage;
import com.trilogy.util.crmapi.wsdl.v2_1.types.serviceandbundle.PricePlanOptionType;
import com.trilogy.util.crmapi.wsdl.v2_1.types.subscription.MutableSubscriptionBilling;
import com.trilogy.util.crmapi.wsdl.v2_1.types.subscription.SubscriptionBilling;
import com.trilogy.util.crmapi.wsdl.v2_1.types.subscription.SubscriptionClassReference;
import com.trilogy.util.crmapi.wsdl.v2_1.types.subscription.SubscriptionCreationTemplateReference;
import com.trilogy.util.crmapi.wsdl.v2_1.types.subscription.SubscriptionLevelReference;
import com.trilogy.util.crmapi.wsdl.v2_1.types.subscription.SubscriptionState;
import com.trilogy.util.crmapi.wsdl.v2_1.types.subscription.SubscriptionStatus;
import com.trilogy.util.crmapi.wsdl.v2_1.types.subscription.SubscriptionTypeReference;
import com.trilogy.util.crmapi.wsdl.v2_2.types.serviceandbundle.PricePlanOption;
import com.trilogy.util.crmapi.wsdl.v2_2.types.serviceandbundle.PricePlanOptionUpdateResult;
import com.trilogy.util.crmapi.wsdl.v2_2.types.subscription.BaseMutableSubscriptionExtension;
import com.trilogy.util.crmapi.wsdl.v2_2.types.subscription.MutableSubscriptionProfile;
import com.trilogy.util.crmapi.wsdl.v2_2.types.subscription.PostpaidDetailedSubscriptionBalance;
import com.trilogy.util.crmapi.wsdl.v2_2.types.subscription.PrepaidDetailedSubscriptionBalance;
import com.trilogy.util.crmapi.wsdl.v2_2.types.subscription.PromotionStatus;
import com.trilogy.util.crmapi.wsdl.v2_2.types.subscription.ReadOnlySubscriptionBundle;
import com.trilogy.util.crmapi.wsdl.v2_2.types.subscription.SubscriptionBalance;
import com.trilogy.util.crmapi.wsdl.v2_2.types.subscription.SubscriptionProfile;
import com.trilogy.util.crmapi.wsdl.v3_0.api.CRMExceptionFault;
import com.trilogy.util.crmapi.wsdl.v3_0.api.SubscriptionServiceSkeletonInterface;
import com.trilogy.util.crmapi.wsdl.v3_0.exception.CRMExceptionFactory;
import com.trilogy.util.crmapi.wsdl.v3_0.types.CRMRequestHeader;
import com.trilogy.util.crmapi.wsdl.v3_0.types.ExecuteResult;
import com.trilogy.util.crmapi.wsdl.v3_0.types.serviceandbundle.BundleAdjustment;
import com.trilogy.util.crmapi.wsdl.v3_0.types.serviceandbundle.BundleAdjustmentResponse;
import com.trilogy.util.crmapi.wsdl.v3_0.types.serviceandbundle.BundleAdjustmentResult;
import com.trilogy.util.crmapi.wsdl.v3_0.types.serviceandbundle.CreateBucketHistoryRequest;
import com.trilogy.util.crmapi.wsdl.v3_0.types.serviceandbundle.PricePlan;
import com.trilogy.util.crmapi.wsdl.v3_0.types.subscription.BaseReadOnlySubscriptionExtension;
import com.trilogy.util.crmapi.wsdl.v3_0.types.subscription.BaseSubscriptionExtension;
import com.trilogy.util.crmapi.wsdl.v3_0.types.subscription.BaseSubscriptionExtensionReference;
import com.trilogy.util.crmapi.wsdl.v3_0.types.subscription.BucketHistoryCreateResult;
import com.trilogy.util.crmapi.wsdl.v3_0.types.subscription.BucketHistoryQueryResult;
import com.trilogy.util.crmapi.wsdl.v3_0.types.subscription.DetailedBucketHistoryQueryResult;
import com.trilogy.util.crmapi.wsdl.v3_0.types.subscription.MergedBalanceHistoryResult;
import com.trilogy.util.crmapi.wsdl.v3_0.types.subscription.SubscriptionBundleBalanceSummary;
import com.trilogy.util.crmapi.wsdl.v3_0.types.subscription.SubscriptionContract;
import com.trilogy.util.crmapi.wsdl.v3_0.types.subscription.SubscriptionContractReference;
import com.trilogy.util.crmapi.wsdl.v3_0.types.subscription.SubscriptionContractStatusQueryResult;
import com.trilogy.util.crmapi.wsdl.v3_0.types.subscription.SubscriptionContractUpdateResults;
import com.trilogy.util.crmapi.wsdl.v3_0.types.subscription.SubscriptionPricePlan;
import com.trilogy.util.crmapi.wsdl.v3_0.types.subscription.SubscriptionProfileQueryResults;
import com.trilogy.util.crmapi.wsdl.v3_0.types.subscription.SubscriptionRating;
import com.trilogy.util.crmapi.wsdl.v3_0.types.subscription.SubscriptionReference;
import com.trilogy.util.crmapi.wsdl.v3_0.types.subscription.SubscriptionSecondaryBalanceQueryResult;
import com.trilogy.util.crmapi.wsdl.v3_0.types.subscription.SubscriptionStateTransitionResult;
import com.trilogy.util.crmapi.wsdl.v3_0.types.subscription.SubscriptionUpdateCriteria;
import com.trilogy.util.crmapi.wsdl.v3_0.types.subscription.SubscriptionUpdateFees;
import com.trilogy.util.crmapi.wsdl.v3_0.types.subscription.VoucherBatchResults;
import com.trilogy.util.crmapi.wsdl.v3_0.types.subscription.VoucherOrder;
import com.trilogy.util.crmapi.wsdl.v3_0.types.subscription.extensions.PPSMSupporteeSubscriptionExtensionReference;



/**
 * Implementation of RMI interface methods for Subscription handling.
 *
 * @author victor.stratan@redknee.com
 */
public class SubscribersImpl implements SubscriptionServiceSkeletonInterface, ContextAware
{

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 1L;


    /**
     * Create a new instance of <code>SubscribersImpl</code>.
     *
     * @param ctx
     *            The operating context.
     * @throws RemoteException
     *             Thrown if there are RMI-related errors.
     */
    public SubscribersImpl(final Context ctx) throws RemoteException
    {
        this.context_ = ctx;
        this.subscriptionToApiAdapter_ = new SubscriberToApiAdapter();
        this.subscriptionClassToApiAdapter_ = new SubscriptionClassToApiAdapter();
        this.subscriptionLevelToApiAdapter_ = new SubscriptionLevelToApiAdapter();
        this.subscriptionTypeToApiAdapter_ = new SubscriptionTypeToApiAdapter();
        this.subscriberTemplateToApiAdapter_ = new SubscriberCreationTemplateToApiAdapter();
        this.subBucketToApiAdapter_ = SubscriberBucketToApiAdapter.instance();
        this.subscriptionContractTermToApiAdapter_ = new SubscriptionContractTermToApiAdapter();
        this.bundleAdjustmentToApiAdapter_ = new BundleAdjustmentToApiAdapter();
    }



    /**
     * {@inheritDoc}
     */
    public SubscriptionClassReference[] listSubscriptionClasses(CRMRequestHeader header, Boolean isAscending, GenericParameter[] parameters)
            throws CRMExceptionFault
    {
        final Context ctx = getContext().createSubContext();
        // handle login. Failed logins throw exceptions
        RmiApiSupport.authenticateUser(ctx, header, "listSubscriptionClasses",
                Constants.PERMISSION_SUBSCRIBERS_READ_LISTSUBSCRIPTIONCLASSES);

        SubscriptionClassReference[] subscriptionClassReferences = new SubscriptionClassReference[] {};
        try
        {
            final Object condition = True.instance();
            
            final Collection<SubscriptionClass> collection = HomeSupportHelper.get(ctx).getBeans(
                    ctx, 
                    SubscriptionClass.class, 
                    condition, 
                    RmiApiSupport.isSortAscending(isAscending));
            
            subscriptionClassReferences = CollectionSupportHelper.get(ctx).adaptCollection(
                    ctx, 
                    collection, 
                    this.subscriptionClassToApiAdapter_,
                    subscriptionClassReferences);
        }
        catch (final Exception e)
        {
            final String msg = "Unable to retrieve Subscription Classes. ";
            RmiApiErrorHandlingSupport.handleQueryExceptions(ctx, e, msg, this);
        }
        
        return subscriptionClassReferences;

    }


    /**
     * {@inheritDoc}
     */
    public SubscriptionCreationTemplateReference[] listSubscriptionCreationTemplates(final CRMRequestHeader header,
        final int spid, Boolean isAscending, GenericParameter[] parameters) throws CRMExceptionFault
    {
        final Context ctx = getContext().createSubContext();
        // handle login. Failed logins throw exceptions
        RmiApiSupport.authenticateUser(ctx, header, "listSubscriptionCreationTemplates",
            Constants.PERMISSION_SUBSCRIBERS_READ_LISTSUBSCRIPTIONCREATIONTEMPLATES,
            Constants.PERMISSION_SUBSCRIBERS_READ_LISTSUBSCRIBERCREATIONTEMPLATES);

        RmiApiSupport.getCrmServiceProvider(ctx, spid, this);

        SubscriptionCreationTemplateReference[] creationTemplateReferences = new SubscriptionCreationTemplateReference[] {};
        try
        {
            final Object condition = new EQ(ServiceActivationTemplateXInfo.SPID, spid);
            
            final Collection<ServiceActivationTemplate> collection = HomeSupportHelper.get(ctx).getBeans(
                    ctx, 
                    ServiceActivationTemplate.class, 
                    condition, 
                    RmiApiSupport.isSortAscending(isAscending));
            
            creationTemplateReferences = CollectionSupportHelper.get(ctx).adaptCollection(
                    ctx, 
                    collection, 
                    this.subscriberTemplateToApiAdapter_,
                    creationTemplateReferences);
        }
        catch (final Exception e)
        {
            final String msg = "Unable to retrieve Subscription Creation Templates for Service Provider=" + spid + ". ";
            RmiApiErrorHandlingSupport.handleQueryExceptions(ctx, e, msg, this);
        }

        return creationTemplateReferences;

    }


    /**
     * {@inheritDoc}
     */
    public SubscriptionLevelReference[] listSubscriptionLevels(CRMRequestHeader header, int spid,
            Boolean isAscending, GenericParameter[] parameters) throws CRMExceptionFault
    {
        final Context ctx = getContext().createSubContext();
        // handle login. Failed logins throw exceptions
        RmiApiSupport.authenticateUser(ctx, header, "listSubscriptionLevels",
                Constants.PERMISSION_SUBSCRIBERS_READ_LISTSUBSCRIPTIONLEVELS);

        RmiApiSupport.getCrmServiceProvider(ctx, spid, this);

        SubscriptionLevelReference[] subscriptionLevelReferences = new SubscriptionLevelReference[] {};
        try
        {
            final Object condition = new EQ(SubscriptionLevelXInfo.SPID, spid);
            
            final Collection<SubscriptionLevel> collection = HomeSupportHelper.get(ctx).getBeans(
                    ctx, 
                    SubscriptionLevel.class, 
                    condition, 
                    RmiApiSupport.isSortAscending(isAscending));
            
            subscriptionLevelReferences = CollectionSupportHelper.get(ctx).adaptCollection(
                    ctx, 
                    collection, 
                    this.subscriptionLevelToApiAdapter_,
                    subscriptionLevelReferences);
        }
        catch (final Exception e)
        {
            final String msg = "Unable to retrieve Subscription Levels for Service Provider=" + spid + ". ";
            RmiApiErrorHandlingSupport.handleQueryExceptions(ctx, e, msg, this);
        }

        return subscriptionLevelReferences;

    }
    
    
    public SubscriptionReference[] listSubscriptions(final CRMRequestHeader header, final String accountID,
            final boolean recurse, final Boolean responsible, final Boolean isAscending, GenericParameter[] parameters) throws CRMExceptionFault
    {
        final Context ctx = getContext().createSubContext();
        // handle login. Failed logins throw exceptions
        RmiApiSupport.authenticateUser(ctx, header, "listSubscriptions",
                Constants.PERMISSION_SUBSCRIBERS_READ_LISTSUBSCRIPTIONS,
                Constants.PERMISSION_ACCOUNTS_READ_LISTSUBSCRIBERS);

        RmiApiErrorHandlingSupport.validateMandatoryObject(accountID, "accountID");

        final Account account = AccountsImpl.getCrmAccount(ctx, accountID, this);
        
        final Collection<Account> accounts;
        if (recurse)
        {
            accounts = AccountsImpl.getCrmSubAccounts(ctx, account, recurse, responsible, isAscending, parameters);
        }
        else
        {
            accounts = new ArrayList<Account>(1);
        }
        accounts.add(account);
        
        final Set<String> bans = new HashSet<String>(accounts.size());
        for (Account parent : accounts)
        {
            bans.add(parent.getBAN());
        }

        SubscriptionReference[] subscriptionReferences = new SubscriptionReference[] {};
        try
        {
            final Object condition = new In(SubscriberXInfo.BAN, bans);
            
            final Collection<Subscriber> collection = HomeSupportHelper.get(ctx).getBeans(
                    ctx, 
                    Subscriber.class, 
                    condition, 
                    RmiApiSupport.isSortAscending(isAscending));
            
            subscriptionReferences = CollectionSupportHelper.get(ctx).adaptCollection(
                    ctx, 
                    collection, 
                    this.subscriptionToApiAdapter_, 
                    subscriptionReferences);
            
            populateServiceAddressForSubscription(ctx, account.getBAN(), subscriptionReferences);
        }
        catch (final Exception e)
        {
            final String msg = "Unable to retrieve Subscriptions for Account " + accountID;
            RmiApiErrorHandlingSupport.handleQueryExceptions(ctx, e, msg, this);
        }

        return subscriptionReferences;

    }

	private void populateServiceAddressForSubscription(Context ctx, String accountId,
			SubscriptionReference[] subscriptionReferences) throws CRMExceptionFault {
		if (null != subscriptionReferences) {
			try {
				for (SubscriptionReference subscriptionReference : subscriptionReferences) {
					GenericParameter genericParameter = null;
					String identifier = subscriptionReference.getIdentifier();
					And andCondition = new And();
					andCondition.add(new EQ(SubscriberXInfo.BAN, accountId));
					andCondition.add(new EQ(SubscriberXInfo.ID, identifier));
					HomeSupport homeSupport = HomeSupportHelper.get(ctx);
					Collection<Subscriber> subscribers = homeSupport.getBeans(ctx, Subscriber.class, andCondition);
					if (null != subscribers && 0 < subscribers.size()) {
						genericParameter = new GenericParameter();
						genericParameter.setName(APIGenericParameterSupport.OFFERING_BUSINESS_KEY);
						subscribers.iterator().hasNext();
						genericParameter.setValue(subscribers.iterator().next().getPricePlan());
						subscriptionReference.addParameters(genericParameter);
					}
					andCondition = new And();
					andCondition.add(new EQ(SupplementaryDataXInfo.IDENTIFIER, identifier));
					andCondition
							.add(new EQ(SupplementaryDataXInfo.ENTITY, SupplementaryDataEntityEnum.SUBSCRIPTION_INDEX));
					andCondition.add(new EQ(SupplementaryDataXInfo.KEY, APIGenericParameterSupport.ADDRESS_ID));
					Collection<SupplementaryData> supplementaryDatas = homeSupport.getBeans(ctx,
							SupplementaryData.class, andCondition);
					if (null != supplementaryDatas && 0 < supplementaryDatas.size()) {
						genericParameter = new GenericParameter();
						genericParameter.setName(APIGenericParameterSupport.ADDRESS_ID);
						supplementaryDatas.iterator().hasNext();
						genericParameter.setValue(supplementaryDatas.iterator().next().getValue());
						subscriptionReference.addParameters(genericParameter);
					}
					andCondition = new And();
					andCondition.add(new EQ(SupplementaryDataXInfo.IDENTIFIER, identifier));
					andCondition
							.add(new EQ(SupplementaryDataXInfo.ENTITY, SupplementaryDataEntityEnum.SUBSCRIPTION_INDEX));
					andCondition.add(new EQ(SupplementaryDataXInfo.KEY, APIGenericParameterSupport.ALIAS));
					supplementaryDatas = homeSupport.getBeans(ctx, SupplementaryData.class, andCondition);
					if (null != supplementaryDatas && 0 < supplementaryDatas.size()) {
						genericParameter = new GenericParameter();
						genericParameter.setName(APIGenericParameterSupport.ALIAS);
						supplementaryDatas.iterator().hasNext();
						genericParameter.setValue(supplementaryDatas.iterator().next().getValue());
						subscriptionReference.addParameters(genericParameter);
					}
				}
			} catch (Exception e) {
				final String msg = "Unable to retrieve Address ID/Offering business key/Subscriber alias of Subscriptions for Account "
						+ accountId + ".";
				RmiApiErrorHandlingSupport.handleQueryExceptions(ctx, e, msg, this);
			}
		}
	}

	/**
     * {@inheritDoc}
     */
    public SubscriptionTypeReference[] listSubscriptionTypes(CRMRequestHeader header, Boolean isAscending, GenericParameter[] parameters)
            throws CRMExceptionFault
    {
        final Context ctx = getContext().createSubContext();
        // handle login. Failed logins throw exceptions
        RmiApiSupport.authenticateUser(ctx, header, "listSubscriptionTypes",
                Constants.PERMISSION_SUBSCRIBERS_READ_LISTSUBSCRIPTIONTYPES);

        SubscriptionTypeReference[] subscriptionTypeReferences = new SubscriptionTypeReference[] {};
        try
        {
            final Collection<SubscriptionType> collection = HomeSupportHelper.get(ctx).getBeans(
                    ctx, 
                    SubscriptionType.class, 
                    True.instance(), 
                    RmiApiSupport.isSortAscending(isAscending));
            
            subscriptionTypeReferences = CollectionSupportHelper.get(ctx).adaptCollection(
                    ctx, 
                    collection, 
                    this.subscriptionTypeToApiAdapter_,
                    subscriptionTypeReferences);
        }
        catch (final Exception e)
        {
            final String msg = "Unable to retrieve Subscription Types. ";
            RmiApiErrorHandlingSupport.handleQueryExceptions(ctx, e, msg, this);
        }
        
        return subscriptionTypeReferences;

    }
    
    private void appendDescriptionToInvalidBalances(StringBuilder currentString, String balanceDescription)
    {
        if (currentString.length()>0)
        {
            currentString.append(", ");
        }
        currentString.append(balanceDescription);
    }
    
    private void validateBalanceUsage(Context ctx, BalanceUsage usage, Subscriber subscriber, boolean isPooledSub) throws CRMExceptionFault
    {
        StringBuilder invalidBalances = new StringBuilder();
        boolean invalidBalance = false;
        if (usage.getAdjustmentsSinceLastInvoice() == SubscriberSupport.INVALID_VALUE)
        {
            invalidBalance = true;
            appendDescriptionToInvalidBalances(invalidBalances, "Adjustment Since Last Invoice");
        }
        
        if (usage.getCreditLimit() == SubscriberSupport.INVALID_VALUE)
        {
            invalidBalance = true;
            appendDescriptionToInvalidBalances(invalidBalances, "Credit Limit");
        }

        if (usage.getLastInvoiceAmount() == SubscriberSupport.INVALID_VALUE)
        {
            invalidBalance = true;
            appendDescriptionToInvalidBalances(invalidBalances, "Last Invoice Amount");
        }

        if (usage.getPaymentSinceLastInvoice() == SubscriberSupport.INVALID_VALUE)
        {
            invalidBalance = true;
            appendDescriptionToInvalidBalances(invalidBalances, "Payment Since Last Invoice");
        }

        if (usage.getRealTimeBalance() == SubscriberSupport.INVALID_VALUE)
        {
            invalidBalance = true;
            appendDescriptionToInvalidBalances(invalidBalances, "Real Time Balance");
        }

        if (!isPooledSub)
        {
            if (usage.getMonthlySpendAmount() == SubscriberSupport.INVALID_VALUE)
            {
                invalidBalance = true;
                appendDescriptionToInvalidBalances(invalidBalances, "Monthly Spend Amount");
            }

            if (usage.getMonthlySpendLimit() == SubscriberSupport.INVALID_VALUE)
            {
                invalidBalance = true;
                appendDescriptionToInvalidBalances(invalidBalances, "Monthly Spend Limit");
            }
        }


        if (invalidBalance)
        {
            final String msg =
                "CRM cannot retrieve one or more balances from external application for subscription "
                    + subscriber.getId() + ": " + invalidBalances.toString();
            RmiApiErrorHandlingSupport.handleQueryExceptions(ctx, null,
                msg, this);
        }
    }


    /**
     * {@inheritDoc}
     */
    public SubscriptionBalance getSubscriptionBalance(final CRMRequestHeader header,
        final SubscriptionReference subscriptionRef, final Boolean detailed, GenericParameter[] parameters) throws CRMExceptionFault
    {
        final Context ctx = getContext().createSubContext();
        // handle login. Failed logins throw exceptions
        RmiApiSupport.authenticateUser(ctx, header, "getSubscriptionBalance",
            Constants.PERMISSION_SUBSCRIBERS_READ_GETSUBSCRIPTIONBALANCE,
            Constants.PERMISSION_SUBSCRIBERS_READ_GETSUBSCRIBERBALANCE);

        RmiApiErrorHandlingSupport.validateMandatoryObject(subscriptionRef, "subscriptionRef");

        final Subscriber subscriber = SubscribersApiSupport.getCrmSubscriber(ctx, subscriptionRef, this);
        final SubscriptionBalance result;

        BalanceUsage usage = subscriber.getBalanceUsage(ctx);

        boolean isPooledSub = subscriber.isPooledMemberSubscriber(ctx);

        if (subscriber.getSubscriberType() == SubscriberTypeEnum.POSTPAID)
        {
//        	Modified the condition to fix TT#13050208012
            if (detailed != null && detailed.booleanValue())
            {
                validateBalanceUsage(ctx, usage, subscriber, isPooledSub);

                PostpaidDetailedSubscriptionBalance balance = new PostpaidDetailedSubscriptionBalance();
                balance.setAdjustmentsSinceLastInvoice(Long.valueOf(usage.getAdjustmentsSinceLastInvoice()));
                balance.setCreditLimit(Long.valueOf(usage.getCreditLimit()));
                balance.setLastInvoiceAmount(Long.valueOf(usage.getLastInvoiceAmount()));
                balance.setPaymentsSinceLastInvoice(Long.valueOf(usage.getPaymentSinceLastInvoice()));
                balance.setRealTimeBalance(Long.valueOf(usage.getRealTimeBalance()));
                if (!isPooledSub)
                {
                    balance.setMonthlySpendAmount(Long.valueOf(usage.getMonthlySpendAmount()));
                    balance.setMonthlySpendLimit(Long.valueOf(usage.getMonthlySpendLimit()));
                }
                /*
                 * TODO: support write-off balance
                 */
                // balance.setWrittenOffBalance(usage.getWrittenOffBalance());
                result = balance;
            }
            else
            {
                result = new SubscriptionBalance();
            }
            result.setAmount(Long.valueOf(usage.getAmountOwing()));
            result.setBlockedBalance(Long.valueOf(usage.getBlockedBalance()));
        }
        else
        {
//        	Modified the condition to fix TT#13050208012
            if (detailed != null && detailed.booleanValue())
            {
                PrepaidDetailedSubscriptionBalance balance = new PrepaidDetailedSubscriptionBalance();
                balance.setOverdraftBalance(Long.valueOf(usage.getOverdraftBalance()));
                balance.setOverdraftDate(Long.valueOf(usage.getOverdraftDate()));

                result = balance;
            }
            else
            {
                result = new SubscriptionBalance();
            }
            result.setAmount(Long.valueOf(usage.getBalanceRemaining()));
            result.setBlockedBalance(Long.valueOf(usage.getBlockedBalance()));
            result.setExpiryDate(CalendarSupportHelper.get(ctx).dateToCalendar(subscriber.getExpiryDate()));
        }

        final String mobileNumber;
        if (subscriptionRef.getMobileNumber() != null && subscriptionRef.getMobileNumber().trim().length() > 0)
        {
            mobileNumber = subscriptionRef.getMobileNumber();
        }
        else
        {
            // Main MSISDN of the subscriber is returned in givenMobileNumber
            // field if none was provided.
            mobileNumber = subscriber.getMSISDN();
        }
        result.setGivenMobileNumber(mobileNumber);

        result.setPrimaryMobileNumber(subscriber.getMSISDN());

        result.setCurrency(subscriber.getCurrency(ctx));

        return result;

    }

    /**
     * @{inheritDoc}
     */
    @Override
    public ReadOnlySubscriptionBundle[] getSubscriptionBundleBalances(CRMRequestHeader header,
            SubscriptionReference subscriptionRef, Long bucketID, Long bundleID, Long bundleCategory, GenericParameter[] parameters)
            throws CRMExceptionFault
    {
        final Context ctx = getContext().createSubContext();
        // handle login. Failed logins throw exceptions
        RmiApiSupport.authenticateUser(ctx, header, "getSubscriptionBundleBalances",
                Constants.PERMISSION_SUBSCRIBERS_READ_GETSUBSCRIPTIONBUNDLEBALANCES,
                Constants.PERMISSION_SUBSCRIBERS_READ_GETSUBSCRIBERBUNDLEBALANCES);

        RmiApiErrorHandlingSupport.validateMandatoryObject(subscriptionRef, "subscriptionRef");

        final Subscriber subscriber = SubscribersApiSupport.getCrmSubscriber(ctx, subscriptionRef, this);
        if(subscriber.getState().getIndex() == SubscriberStateEnum.INACTIVE_INDEX)
        {
            RmiApiErrorHandlingSupport.simpleValidation("Subscriber.state", "Can not retrieve balance because subscriber is inactive");
        }
        com.redknee.framework.xhome.msp.MSP.setBeanSpid(ctx, subscriber.getSpid());
        
        ReadOnlySubscriptionBundle[] subscriptionBundles = new ReadOnlySubscriptionBundle[] {};
        try
        {
            Collection<SubscriberBucket> buckets = getBuckets(ctx, subscriber, bucketID, bundleID, bundleCategory, true, parameters);
            
            subscriptionBundles = CollectionSupportHelper.get(ctx).adaptCollection(
                        ctx, 
                        buckets, 
                        subBucketToApiAdapter_, 
                        subscriptionBundles);
        }
        catch (Exception e)
        {
            final String msg = "Error retrieving bundles for subscriber with MSISDN " + subscriber.getMSISDN() + ".";
            RmiApiErrorHandlingSupport.handleQueryExceptions(ctx, e, msg, this);
        }
        
        return subscriptionBundles;

    }
    

    /**
     * @{inheritDoc
     */
    public PromotionStatus[] getSubscriptionPromotionStatus(final CRMRequestHeader header,
            final SubscriptionReference subscriptionRef, GenericParameter[] parameters) throws CRMExceptionFault
    {
        final Context ctx = getContext().createSubContext();
        // handle login. Failed logins throw exceptions
        RmiApiSupport.authenticateUser(ctx, header, "getSubscriptionPromotionStatus",
                Constants.PERMISSION_SUBSCRIBERS_READ_GETSUBSCRIPTIONPROMOTIONSTATUS);
        final Subscriber subscriber = SubscribersApiSupport.getCrmSubscriber(ctx, subscriptionRef, this);
        PromotionStatus[] promotionStatus = null;
        try
        {
            final PromotionProvisionClient client = UrcsClientInstall.getClient(ctx, UrcsClientInstall.PROMOTION_PROVISION_CLIENT_KEY);
            com.redknee.app.urcs.promotion.PromotionStatus[] crmPromotionsStatusArray = client
                    .listSubscriptionPromotionStatus(ctx, subscriber);
            PromotionStatusToApiAdapter adapter = new PromotionStatusToApiAdapter(ctx, subscriber.getSpid(),
                    crmPromotionsStatusArray);
            promotionStatus = new PromotionStatus[crmPromotionsStatusArray.length];
            for (int i = 0; i < crmPromotionsStatusArray.length; i++)
            {
                com.redknee.app.urcs.promotion.PromotionStatus crmPromStatus = crmPromotionsStatusArray[i];
                promotionStatus[i] = (PromotionStatus) adapter.adapt(ctx, crmPromStatus);
            }
        }
        catch (Exception e)
        {
            final String msg = "Error retrieving promotion status for subscriber with MSISDN " + subscriber.getMSISDN()
                    + ".";
            RmiApiErrorHandlingSupport.handleQueryExceptions(ctx, e, msg, this);
        }
        return promotionStatus;

    }

    /**
     * {@inheritDoc}
     */
    public SubscriptionPricePlan getSubscriptionPricePlanOptions(final CRMRequestHeader header,
            final SubscriptionReference subscriptionRef, final Long pricePlanID, GenericParameter[] parameters) throws CRMExceptionFault
    {
        final Context ctx = getContext().createSubContext();
        // handle login. Failed logins throw exceptions
        RmiApiSupport.authenticateUser(ctx, header, "getSubscriptionPricePlanOptions",
                Constants.PERMISSION_SUBSCRIBERS_READ_GETSUBSCRIPTIONPRICEPLANOPTIONS);
        
        Subscriber subscriber = null;
        SubscriptionPricePlan subPricePlan = null;
        
        GenericParameterParser parser = new GenericParameterParser(parameters);
        Boolean sendMRCAndDiscount = parser.getParameter(APIGenericParameterSupport.SEND_MRC_AND_DISCOUNT, Boolean.class, Boolean.FALSE);
        
        if(LogSupport.isDebugEnabled(ctx))
        {
        	LogSupport.debug(ctx, this, "Generic param: "+APIGenericParameterSupport.SEND_MRC_AND_DISCOUNT+" = "+sendMRCAndDiscount.booleanValue());
        }

        if (subscriptionRef != null)
        {
            subscriber = SubscribersApiSupport.getCrmSubscriber(ctx, subscriptionRef, this);
        }
        else
        {
            RmiApiErrorHandlingSupport.validateMandatoryObject(pricePlanID, "subscriptionRef OR pricePlanID");
        }
        
        try
        {
            subPricePlan = SubscriptionPricePlanOptionToApiAdapter.getSubscriptionPricePlanOption(ctx, subscriber, pricePlanID, sendMRCAndDiscount);
        }
        catch (Exception e)
        {
            StringBuilder msg = new StringBuilder("Error retrieving price plan options");
            if (subscriptionRef != null)
            {
                msg.append(" for Subscription with identifier=" + subscriptionRef.getIdentifier());
            }
            if (pricePlanID != null)
            {
                msg.append(" for price plan " + pricePlanID);
            }
            RmiApiErrorHandlingSupport.handleQueryExceptions(ctx, e, msg.toString(), this);
        }
        
        return subPricePlan;

    }
    
    /**
     * {@inheritDoc}
     */
    public SuccessCode updateSubscriptionCardPackage(final CRMRequestHeader header, final SubscriptionReference subscriptionRef,
        final String newCardPackageID, final CardPackage cardPackage, GenericParameter[] parameters) throws CRMExceptionFault
    {
        final Context ctx = getContext().createSubContext();
        // handle login. Failed logins throw exceptions
        RmiApiSupport.authenticateUser(ctx, header, "updateSubscriptionCardPackage",
            Constants.PERMISSION_SUBSCRIBERS_WRITE_UPDATESUBSCRIPTIONCARDPACKAGE,
            Constants.PERMISSION_SUBSCRIBERS_WRITE_UPDATESUBSCRIBERCARDPACKAGE);

        SubscribersApiSupport.validateCardPackage(newCardPackageID, cardPackage);
        
        GenericParameterParser parser = new GenericParameterParser(parameters);

        final Subscriber subscriber = SubscribersApiSupport.getCrmSubscriber(ctx, subscriptionRef, this);
        final Home home = updateSubscriberPipeline(ctx,"updateSubscriptionCardPackage");
        String secondaryPackageId = parser.getParameter(APIGenericParameterSupport.SECONDARY_PACKAGE, String.class);
        String msid = parser.getParameter(APIGenericParameterSupport.MSID, String.class);
        
        if (subscriber.isInFinalState())
        {
            RmiApiErrorHandlingSupport.simpleValidation("subscriptionRef",
                "Subscription is in a closed state. Cannot update Card Package of subscriber.");
        }

        if (subscriber.getPackageId().equals(newCardPackageID) && (secondaryPackageId == null || secondaryPackageId.trim().equals("")))
        {
        	if (parser.getParameter(FAIL_CARD_PACKAGE_ALREADY_ASSIGNED, Boolean.class, Boolean.FALSE))
        	{
                RmiApiErrorHandlingSupport.simpleValidation("newCardPackageID",
                        "Card Package with given ID is already assigned to the given subscription.");
        	}
        	else
        	{
	            // nothing to do.
	            return SuccessCodeEnum.SUCCESS.getValue();
        	}
        }

        final CRMSpid sp = RmiApiSupport.getCrmServiceProvider(ctx, Integer.valueOf(subscriber.getSpid()), this);
        SubscribersApiSupport.validateUpdateCardPackage(cardPackage, sp, subscriber);

        try
        {         
            // handle auto create card packages
            if (cardPackage != null)
            {
                CardPackageApiSupport.prepareCardPackage(
                        cardPackage, 
                        subscriber.getSpid(), 
                        TechnologyTypeEnum.valueOf(subscriber.getTechnology().getIndex()));
                
                final GenericPackage cardReference = CardPackageApiSupport.createCrmCardPackage(ctx, cardPackage, this);
                subscriber.setPackageId(cardReference.getPackId());
            }
             else
             {
                String existingPrimaryPackId = subscriber.getPackageId();
                subscriber.setPackageId(newCardPackageID);
                if(parser != null)
                {
                    TechnologyEnum subscriberTech =  subscriber.getTechnology();
                    if(subscriberTech.getIndex() == TechnologyEnum.TDMA_INDEX || subscriberTech.getIndex() == TechnologyEnum.CDMA_INDEX)
                    {
                        TDMAPackage primaryPackage = null;
                        TDMAPackage secondaryPackage = null;
                        TDMAPackage existingPrimaryPackage = null;
                        try
                        {
                            primaryPackage = PackageSupportHelper.get(ctx).getTDMAPackage(ctx, newCardPackageID, subscriber.getSpid());
                            secondaryPackage = PackageSupportHelper.get(ctx).getTDMAPackage(ctx, secondaryPackageId, subscriber.getSpid());
                            existingPrimaryPackage =PackageSupportHelper.get(ctx).getTDMAPackage(ctx, existingPrimaryPackId , subscriber.getSpid());
                            boolean updateExternalMSID = false;
                            if(msid != null && !msid.trim().isEmpty() && primaryPackage != null)
                            {
                                primaryPackage.setExternalMSID(msid);
                                updateExternalMSID = true;
                            }else
                            {	
                            	if(primaryPackage != null)
                            	{
	                            	//IF GENERIC PARAMETER MSID IS EMPLTY THEN GET THE PREVIOUS PACKAGE EXTERNAL MSID AND UPDATE IT WITH NEWLY ASSIGN PACKAGE 
	                            	String tmpExternalMSID = existingPrimaryPackage.getExternalMSID();
	                            	                            	
	                            	if(tmpExternalMSID != null && !tmpExternalMSID.isEmpty())
	                            	{
	                            		msid = tmpExternalMSID;
	                            		primaryPackage.setExternalMSID(msid);
	                            		updateExternalMSID = true;
	                            	}
                            	}
                            }
                            if(secondaryPackageId != null && !secondaryPackageId.trim().isEmpty())
                            {
                                SwapPackageSupport.mergeTDMAPackages(ctx, subscriber, primaryPackage, secondaryPackage, existingPrimaryPackage, secondaryPackageId);
                            }
                            else if(updateExternalMSID)
                            {
                                HomeSupportHelper.get(ctx).storeBean(ctx, primaryPackage);
                            }
                        }
                        catch (final Exception e)
                        {
                            final String msg = "Unable to update Card Package for Subscription with ID/MobileNumber/SubscriptionType = "
                                + subscriptionRef.getIdentifier() + "/" + subscriptionRef.getMobileNumber() + "/" + subscriptionRef.getSubscriptionType()
                                + " and new card package ID=" + newCardPackageID + ". ";
                            RmiApiErrorHandlingSupport.handleStoreExceptions(ctx, e, msg, this);
                        }
                    }
                } 
            }

            final Object resultSub = home.store(ctx, subscriber);
            SubscribersApiSupport.handlePostSubscriptionUpdate(ctx, (Subscriber) resultSub, true);
        }
        catch (final com.redknee.util.crmapi.wsdl.v3_0.api.CRMExceptionFault e)
        {
            throw e;
        }
        catch (final Exception e)
        {
            final String msg = "Unable to update Card Package for Subscription with ID/MobileNumber/SubscriptionType = "
                + subscriptionRef.getIdentifier() + "/" + subscriptionRef.getMobileNumber() + "/" + subscriptionRef.getSubscriptionType()
                + " and new card package ID=" + newCardPackageID + ". ";
            RmiApiErrorHandlingSupport.handleStoreExceptions(ctx, e, msg, this);
        }
        
        return SuccessCodeEnum.SUCCESS.getValue();

    }


    /**
     * {@inheritDoc}
     */
    public SuccessCode updateSubscriptionCreditLimit(final CRMRequestHeader header, final SubscriptionReference subscriptionRef,
        final long creditLimit, GenericParameter[] parameters) throws CRMExceptionFault
    {
        final Context ctx = getContext().createSubContext();
        // handle login. Failed logins throw exceptions
        RmiApiSupport.authenticateUser(ctx, header, "updateSubscriptionCreditLimit",
            Constants.PERMISSION_SUBSCRIBERS_WRITE_UPDATESUBSCRIPTIONCREDITLIMIT,
            Constants.PERMISSION_SUBSCRIBERS_WRITE_UPDATESUBSCRIBERCREDITLIMIT);

        final Subscriber subscriber = SubscribersApiSupport.getCrmSubscriber(ctx, subscriptionRef, this);
        final Home home = updateSubscriberPipeline(ctx,"updateSubscriptionCreditLimit");

        if (subscriber.isInFinalState())
        {
            RmiApiErrorHandlingSupport.simpleValidation("subscriptionRef",
                "Subscription is in a closed state. Cannot update Credit Limit of subscriber.");
        }

        if (subscriber.getCreditLimit(ctx) == creditLimit)
        {
            // nothing to do.
            return SuccessCodeEnum.SUCCESS.getValue();
        }

        try
        {
            subscriber.setCreditLimit(creditLimit);
            Object resultSub = home.store(ctx, subscriber);
            SubscribersApiSupport.handlePostSubscriptionUpdate(ctx, (Subscriber)resultSub,true);
        }
        catch (final Exception e)
        {
            final String msg = "Unable to update Credit Limit for Subscription with ID/MobileNumber/SubscriptionType = " + subscriptionRef.getIdentifier() + "/" + subscriptionRef.getMobileNumber() + "/" + subscriptionRef.getSubscriptionType();
            RmiApiErrorHandlingSupport.handleStoreExceptions(ctx, e, msg, this);
        }
        
        return SuccessCodeEnum.SUCCESS.getValue();

    }


    /**
     * {@inheritDoc}
     */
    public SuccessCode updateSubscriptionResetMonthlySpendAmount(final CRMRequestHeader header, final SubscriptionReference subscriptionRef,
        GenericParameter[] parameters) throws CRMExceptionFault
    {
        final Context ctx = getContext().createSubContext();
        // handle login. Failed logins throw exceptions
        RmiApiSupport.authenticateUser(ctx, header, "updateSubscriptionResetMonthlySpendAmount",
            Constants.PERMISSION_SUBSCRIBERS_WRITE_UPDATESUBSCRIPTIONRESETMONTHLYSPENDAMOUNT,
            Constants.PERMISSION_SUBSCRIBERS_WRITE_UPDATESUBSCRIPTIONRESETMONTHLYSPENDAMOUNT);

        final Subscriber subscriber = SubscribersApiSupport.getCrmSubscriber(ctx, subscriptionRef, this);

        if (subscriber.isInFinalState())
        {
            RmiApiErrorHandlingSupport.simpleValidation("subscriptionRef",
                "Subscription is in a closed state. Cannot reset the monthly spend amount for subscriber.");
        }
        else if (subscriber.isPooled(ctx))
        {
            RmiApiErrorHandlingSupport.simpleValidation("subscriptionRef",
                    "Subscription is a pooled account. Cannot reset the monthly spend amount for subscriber.");
        }
        else if (subscriber.isPrepaid())
        {
            RmiApiErrorHandlingSupport.simpleValidation("subscriptionRef",
                    "Subscription is prepaid. Cannot reset the monthly spend amount for subscriber.");
        }
        
        try
        {
            subscriber.resetMonthlySpendLimit(ctx);
        } 
        catch (HomeException e)
        {
            RmiApiErrorHandlingSupport.handleStoreExceptions(ctx, e, e.toString(), this);
        }

        return SuccessCodeEnum.SUCCESS.getValue();

    }
    
    
    /**
     * {@inheritDoc}
     */
    public SuccessCode updateSubscriptionResetPoolQuotaUsage(final CRMRequestHeader header, final SubscriptionReference subscriptionRef,
        GenericParameter[] parameters) throws CRMExceptionFault
    {
        final Context ctx = getContext().createSubContext();
        // handle login. Failed logins throw exceptions
        RmiApiSupport.authenticateUser(ctx, header, "updateSubscriptionResetPoolQuotaUsage",
            Constants.PERMISSION_SUBSCRIBERS_WRITE_UPDATESUBSCRIPTIONRESETPOOLQUOTAUSAGE,
            Constants.PERMISSION_SUBSCRIBERS_WRITE_UPDATESUBSCRIPTIONRESETPOOLQUOTAUSAGE);

        final Subscriber subscriber = SubscribersApiSupport.getCrmSubscriber(ctx, subscriptionRef, this);

        if (subscriber.isInFinalState())
        {
            RmiApiErrorHandlingSupport.simpleValidation("subscriptionRef",
                "Subscription is in a closed state. Cannot reset the pool quota usage for subscriber.");
        }
        else if (!subscriber.isPooled(ctx))
        {
            RmiApiErrorHandlingSupport.simpleValidation("subscriptionRef",
                    "Subscription is not in a pooled account. Cannot reset the pool quota usage for subscriber.");
        }
        
        try
        {
            subscriber.resetGroupUsage(ctx);
        } 
        catch (HomeException e)
        {
            RmiApiErrorHandlingSupport.handleStoreExceptions(ctx, e, e.toString(), this);
        }

        return SuccessCodeEnum.SUCCESS.getValue();

    }

    /**
     * {@inheritDoc}
     */
    public SubscriptionReference updateSubscriptionMobileNumber(final CRMRequestHeader header,
        final SubscriptionReference subscriptionRef, final String newMobileNumber, GenericParameter[] parameters) throws CRMExceptionFault
    {
   	    String csa =null;
    	final Context ctx = getContext().createSubContext();
        ContextLocator.setThreadContext(ctx);
        // handle login. Failed logins throw exceptions
        RmiApiSupport.authenticateUser(ctx, header, "updateSubscriptionMobileNumber",
            Constants.PERMISSION_SUBSCRIBERS_WRITE_UPDATESUBSCRIPTIONMOBILENUMBER,
            Constants.PERMISSION_SUBSCRIBERS_WRITE_UPDATESUBSCRIBERMOBILENUMBER);

        RmiApiErrorHandlingSupport.validateMandatoryObject(newMobileNumber, "newMobileNumber");
        if (newMobileNumber.equals(""))
        {
            final String msg = "newMobileNumber is empty, newMobileNumber is Mandatory and cannot be empty";

            RmiApiErrorHandlingSupport.simpleValidation("newMobileNumber", msg);
        }

        final Subscriber subscriber = SubscribersApiSupport.getCrmSubscriber(ctx, subscriptionRef, this);
        final Home home = updateSubscriberPipeline(ctx, "updateSubscriptionMobileNumber");

        if (subscriber.isInFinalState())
        {
            RmiApiErrorHandlingSupport.simpleValidation("subscriptionRef",
                "Subscription is in a closed state. Cannot update Mobile Number of subscriber!");
        }
        
        GenericParameterParser parser = null;
        GenericParameterParser subRefParser = null;
        
        
        if (parameters!=null)
        {
            parser = new GenericParameterParser(parameters);
        }
        Boolean portIn =  (null==parser)?Boolean.FALSE:parser.getParameter(APIGenericParameterSupport.PORT_IN,Boolean.class);
        if(Boolean.TRUE.equals(portIn))
        {
            ctx.put(MsisdnPortHandlingHome.MSISDN_PORT_KEY, newMobileNumber);
        }
        
        if(parser != null)
        {
        	if(parser.containsParam(APIGenericParameterSupport.EFFECTIVE_DATE))
            {
                 Date msisdnEffectiveDate = parser.getParameter(APIGenericParameterSupport.EFFECTIVE_DATE, java.util.Date.class);
                 if(msisdnEffectiveDate != null)
                 {
                	 ctx.put(MsisdnChangeAppendHistoryHome.MSISDN_EFFECTIVE_DATE, msisdnEffectiveDate);
                 }
            }
        }

        if (subscriber.getMSISDN().equals(newMobileNumber))
        {
            // nothing to do.
            return SubscriberToApiAdapter.adaptSubscriberToReference(ctx, subscriber);
        }
        
        String oldMsisdnString = subscriber.getMsisdn();

        final CRMSpid sp = RmiApiSupport.getCrmServiceProvider(ctx, subscriber.getSpid(), this);
        Msisdn msisdn = MobileNumbersApiSupport.getCrmMsisdn(ctx, newMobileNumber, this);
        
        SubscribersApiSupport.validateAutocreateMsisdn(msisdn, sp);

        if(LogSupport.isDebugEnabled(ctx))
        {
            Home homeForLogging;
            try
            {
                homeForLogging = HomeSupportHelper.get(ctx).getHome(ctx, com.redknee.app.crm.bean.core.Msisdn.class);
            }
            catch (Exception e)
            {
                homeForLogging = (Home)ctx.get(MsisdnHome.class);
            }
            
            final String msg = MessageFormat.format(
                    "Msisdn with id {0}; Home = {1}; Msisdn-Bean: {2}",
                    new Object[]{newMobileNumber, homeForLogging, msisdn});
            LogSupport.debug(ctx, this, msg); 
        }
        
        // handle non existent MSISDNS
        if (msisdn == null)
        {
            if(LogSupport.isDebugEnabled(ctx))
                LogSupport.debug(ctx, this, "Creating non-existant Msisdn with ID: "+ newMobileNumber);
            
            msisdn = MobileNumbersApiSupport.createMobileNumber(
                    ctx, 
                    newMobileNumber, 
                    Boolean.TRUE.equals(portIn),
                    subscriber.getSubscriberType(), 
                    sp, 
                    subscriber.getTechnology(), 
                    this);
        } else if(Boolean.TRUE.equals(portIn))
        {
            try
            {
                MsisdnManagement.markMsisdnPortedIn(ctx, subscriber.getBAN(), msisdn.getMsisdn());
            }
            catch (HomeException e)
            {                
                RmiApiErrorHandlingSupport.handleStoreExceptions(ctx, e, e.toString(), this);
            }
        }
        Subscriber resultSub = null;
        StringBuilder errMsg = new StringBuilder(
                "Unable to update Mobile Number for Subscriptionwith ID/MobileNumber/SubscriptionType = ");
        errMsg.append(subscriptionRef.getIdentifier());
        errMsg.append("/");
        errMsg.append(subscriptionRef.getMobileNumber());
        errMsg.append("/");
        errMsg.append(subscriptionRef.getSubscriptionType());
        errMsg.append(" and new mobile number=");
        errMsg.append(newMobileNumber);

        try
        {
            boolean sufficientFund = com.redknee.app.crm.home.sub.SubscriberMsisdnValidator
                    .hasSufficientBalance(ctx, msisdn, subscriber, subscriber, false);
            if (!sufficientFund)
            {
                errMsg.append(" . Insufficient funds for charging");
                RmiApiErrorHandlingSupport.inSufficientFundValidation("Subscriber.Msisdn", errMsg.toString());
            }
        }
        catch (HomeException homeEx)
        {
            errMsg.append(" . Mobile group can't be found");
            RmiApiErrorHandlingSupport.generalException(ctx, homeEx, errMsg.toString(), this);
        }
        
        try
        {
        	String msid = null;
        	
            subscriber.setMSISDN(newMobileNumber);
            resultSub = (Subscriber) home.store(ctx, subscriber);
            SubscribersApiSupport.handlePostSubscriptionUpdate(ctx, resultSub,true);
            
            if (LogSupport.isDebugEnabled(ctx)) 
			{
				LogSupport.debug(ctx, this, "newMobileNumber" + newMobileNumber + ":Updating Subscriber:" + resultSub);
			}
            
			if (subscriber.getTechnology() == TechnologyEnum.TDMA || subscriber.getTechnology() == TechnologyEnum.CDMA) {

				if (subscriptionRef.getParameters() != null) 
				{
					subRefParser = new GenericParameterParser(subscriptionRef.getParameters());

					if (subRefParser != null && subRefParser.containsParam(APIGenericParameterSupport.MSID)) 
					{
						msid = subRefParser.getParameter(APIGenericParameterSupport.MSID, String.class);

						if (msid != null && !msid.trim().isEmpty()) 
						{
							
							TDMAPackage tdmaPackage = PackageSupportHelper.get(ctx).getTDMAPackage(ctx,
									subscriber.getPackageId(),
									subscriber.getSpid());

							if (LogSupport.isDebugEnabled(ctx)) 
							{
								LogSupport.debug(ctx, this, "MSID:" + msid + ":TDMAPackage:" + tdmaPackage);
							}
							if(tdmaPackage != null)
							{
								tdmaPackage.setExternalMSID(msid);
								HomeSupportHelper.get(ctx).storeBean(ctx, tdmaPackage);
							}
						}

					}
				}
            }                         
        }
        catch (final Exception e)
        {
            RmiApiErrorHandlingSupport.handleStoreExceptions(ctx, e, errMsg.toString(), this);
        }
        
        if(parser != null)
        {
        	if(parser.containsParam(APIGenericParameterSupport.CLEAN_UP_OLD_MDN))
            {
                 Boolean cleanUpMdn = parser.getParameter(APIGenericParameterSupport.CLEAN_UP_OLD_MDN, Boolean.class);
                 if(cleanUpMdn != null && cleanUpMdn == true)
                 {
                	 
         			try
         			{
         				Msisdn oldMsisdn = HomeSupportHelper.get(ctx).findBean(ctx, Msisdn.class, oldMsisdnString);
         				if(oldMsisdn != null)
         				{
         					HomeSupportHelper.get(ctx).removeBean(ctx, oldMsisdn);
         				}
         			}
         			catch(HomeException he)
         			{
         				LogSupport.major(ctx, this, "Unable to delete MSISDN on UpdateSubscriptionMobileNumber for SubId : "+ subscriber.getId() + " and MSISDN : " + oldMsisdnString);
         			}
                 }
            }
        	
        	//GET THE CSA VALUE
        	 csa = parser.getParameter(APIGenericParameterSupport.CSA, String.class);
        	 
             if(csa !=null)
         	 {
             	try
             	{        
             		//UPDATE THE CSA VALUE IN XCONTACT AGAINST ACCOUNT
	             	Account subAccount = resultSub.getAccount(ctx);
	             	if(subAccount != null)
	             	{
	             		subAccount.setCsa(csa);
	             		HomeSupportHelper.get(ctx).storeBean(ctx, subAccount);
	             	}
             	} catch (HomeException homeEx)
                 {   
                       LogSupport.major(ctx, this, "Unable to update CSA on UpdateSubscriptionMobileNumber for SubId : "+ subscriber.getId() + " and MSISDN : " + oldMsisdnString+":"+homeEx);
                 }
         	}
        }        
        final SubscriptionReference result = SubscriberToApiAdapter.adaptSubscriberToReference(ctx, resultSub);
        return result;
    }
    
    /**
     * {@inheritDoc}
     */
    public SubscriptionReference updateSubscriptionPaidType(final CRMRequestHeader header,
            final SubscriptionReference subscriptionRef, final PaidType paidType, final String newSubscriptionID,
            final long pricePlanID, GenericParameter[] parameters) throws CRMExceptionFault
    {
        final Context ctx = getContext().createSubContext();
        // handle login. Failed logins throw exceptions
        RmiApiSupport.authenticateUser(ctx, header, "updateSubscriptionPaidType",
                Constants.PERMISSION_SUBSCRIBERS_WRITE_UPDATESUBSCRIPTIONPAIDTYPE,
                Constants.PERMISSION_SUBSCRIBERS_WRITE_UPDATESUBSCRIBERPAIDTYPE);
        
        RmiApiErrorHandlingSupport.validateMandatoryObject(paidType, "paidType");
        
        GenericParameterParser parser = new GenericParameterParser(parameters);
        Long subscriptionClassParam = null;
        if(parser != null && parser.containsParam(APIGenericParameterSupport.SUBSCRIPTION_CLASS))
        {
    		subscriptionClassParam =  parser.getParameter(APIGenericParameterSupport.SUBSCRIPTION_CLASS, Long.class);
        }
        
        return SubscribersApiSupport.convertPaidType(ctx, subscriptionRef, (long) 0, (long) 0,
                SubscriberTypeEnum.get((short) paidType.getValue()), subscriptionClassParam, pricePlanID, 0,null);

    }


    /**
     * {@inheritDoc}
     */
    @Override
    public SubscriptionReference updateSubscriptionConvertPaidTypeToPrepaid(CRMRequestHeader header,
            SubscriptionReference subscriptionRef, long initialAmount, SubscriptionPricePlan options, GenericParameter[] parameters)
            throws CRMExceptionFault
    {
        final Context ctx = getContext().createSubContext();
        // handle login. Failed logins throw exceptions
        RmiApiSupport.authenticateUser(ctx, header, "updateSubscriptionConvertPaidTypeToPrepaid",
                Constants.PERMISSION_SUBSCRIBERS_WRITE_UPDATESUBSCRIPTIONCONVERTPAIDTYPETOPREPAID);
        
        RmiApiErrorHandlingSupport.validateMandatoryObject(options, "options");
        com.redknee.util.crmapi.wsdl.v3_0.types.serviceandbundle.PricePlan pp = options.getPricePlanDetails();
        
        RmiApiErrorHandlingSupport.validateMandatoryObject(pp, "pricePlanDetails");
        
        GenericParameterParser parser = new GenericParameterParser(parameters);
        Long subscriptionClassParam = null;
        if(parser != null && parser.containsParam(APIGenericParameterSupport.SUBSCRIPTION_CLASS))
        {
    		subscriptionClassParam =  parser.getParameter(APIGenericParameterSupport.SUBSCRIPTION_CLASS, Long.class);
        }
        
        return SubscribersApiSupport.convertPaidType(ctx, subscriptionRef, (long) 0,(long) 0, SubscriberTypeEnum.PREPAID, subscriptionClassParam,
        		pp.getIdentifier().longValue(), initialAmount,options);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public SubscriptionReference updateSubscriptionConvertPaidTypeToPostpaid(CRMRequestHeader header,
            SubscriptionReference subscriptionRef, Long deposit, Long creditLimit, SubscriptionPricePlan options, GenericParameter[] parameters)
            throws CRMExceptionFault
    {
        final Context ctx = getContext().createSubContext();
        // handle login. Failed logins throw exceptions
        RmiApiSupport.authenticateUser(ctx, header, "updateSubscriptionConvertPaidTypeToPostpaid",
                Constants.PERMISSION_SUBSCRIBERS_WRITE_UPDATESUBSCRIPTIONCONVERTPAIDTYPETOPOSTPAID);
        
        RmiApiErrorHandlingSupport.validateMandatoryObject(options, "options");
        com.redknee.util.crmapi.wsdl.v3_0.types.serviceandbundle.PricePlan pp = options.getPricePlanDetails();
        
        RmiApiErrorHandlingSupport.validateMandatoryObject(pp, "pricePlanDetails");
        
        GenericParameterParser parser = new GenericParameterParser(parameters);
        Long subscriptionClassParam = null;
        if(parser != null && parser.containsParam(APIGenericParameterSupport.SUBSCRIPTION_CLASS))
        {
    		subscriptionClassParam =  parser.getParameter(APIGenericParameterSupport.SUBSCRIPTION_CLASS, Long.class);
        }
        
        return SubscribersApiSupport.convertPaidType(ctx, subscriptionRef, deposit, creditLimit,
                SubscriberTypeEnum.POSTPAID, subscriptionClassParam, pp.getIdentifier().longValue(), 0,options);

    }


    /**
     * {@inheritDoc}
     */
    public SuccessCode updateSubscriptionPrimaryPricePlan(final CRMRequestHeader header,
        final SubscriptionReference subscriptionRef, final long pricePlanID, GenericParameter[] parameters) throws CRMExceptionFault
    {
        final Context ctx = getContext().createSubContext();
        // handle login. Failed logins throw exceptions
        RmiApiSupport.authenticateUser(ctx, header, "updateSubscriptionPrimaryPricePlan",
            Constants.PERMISSION_SUBSCRIBERS_WRITE_UPDATESUBSCRIPTIONPRIMARYPRICEPLAN,
            Constants.PERMISSION_SUBSCRIBERS_WRITE_UPDATESUBSCRIBERPRIMARYPRICEPLAN);

        RmiApiErrorHandlingSupport.validateMandatoryObject(pricePlanID, "pricePlanID");

        final Subscriber subscriber = SubscribersApiSupport.getCrmSubscriber(ctx, subscriptionRef, this);
        final Home home = updateSubscriberPipeline(ctx,"updateSubscriptionPrimaryPricePlan");

        if (subscriber.isInFinalState())
        {
            RmiApiErrorHandlingSupport.simpleValidation("subscriptionRef",
                "Subscription is in a closed state. Cannot update Primary Price Plan of subscriber.");
        }
        else if (SubscriberStateEnum.SUSPENDED.equals(subscriber.getState()))
        {
            RmiApiErrorHandlingSupport.simpleValidation("subscriptionRef",
                "Subscription is in Suspended state. Cannot update Primary Price Plan of subscriber.");
        }

        if (subscriber.getPricePlan() == pricePlanID)
        {
            // nothing to do
            return SuccessCodeEnum.SUCCESS.getValue();
        }

        try
        {
            PricePlanSwitchLimitSupport.validatePricePlanSwitchThreshold(ctx, subscriber);
            ctx.put(Lookup.PRICEPLAN_SWITCH_COUNTER_INCREMENT, true);
            subscriber.switchPricePlan(ctx, pricePlanID);
            Object resultSub = home.store(ctx, subscriber);
            SubscribersApiSupport.handlePostSubscriptionUpdate(ctx, (Subscriber)resultSub,true);
        }
        catch (final Exception e)
        {
            final String msg = "Unable to update Primary Price Plan for Subscription with ID/MobileNumber/SubscriptionType = "
                + subscriptionRef.getIdentifier() + "/" + subscriptionRef.getMobileNumber() + "/" + subscriptionRef.getSubscriptionType()
                + " and new pricePlanID="
                + pricePlanID + ". ";
            RmiApiErrorHandlingSupport.handleStoreExceptions(ctx, e, msg, this);
        }

        return SuccessCodeEnum.SUCCESS.getValue();
    }


    /**
     * {@inheritDoc}
     */
    public SuccessCode updateSubscriptionSecondaryPricePlan(final CRMRequestHeader header,
        final SubscriptionReference subscriptionRef, final long pricePlanID, final Calendar start, final Calendar end, GenericParameter[] parameters)
        throws CRMExceptionFault
    {
        final Context ctx = getContext().createSubContext();
        // handle login. Failed logins throw exceptions
        RmiApiSupport.authenticateUser(ctx, header, "updateSubscriptionSecondaryPricePlan",
            Constants.PERMISSION_SUBSCRIBERS_WRITE_UPDATESUBSCRIPTIONSECONDARYPRICEPLAN,
            Constants.PERMISSION_SUBSCRIBERS_WRITE_UPDATESUBSCRIBERSECONDARYPRICEPLAN);

        RmiApiErrorHandlingSupport.validateMandatoryObject(start, "start");

        final Subscriber subscriber = SubscribersApiSupport.getCrmSubscriber(ctx, subscriptionRef, this);
        final Home home = updateSubscriberPipeline(ctx,"updateSubscriptionSecondaryPricePlan");

        if (subscriber.isInFinalState())
        {
            RmiApiErrorHandlingSupport.simpleValidation("subscriptionRef",
                "Subscription is in a closed state. Cannot update Secondary Price Plan of subscriber.");
        }
        else if (SubscriberStateEnum.SUSPENDED.equals(subscriber.getState()))
        {
            RmiApiErrorHandlingSupport.simpleValidation("subscriptionRef",
                "Subscription is in Suspended state. Cannot update Secondary Price Plan of subscriber.");
        }

        if (subscriber.getSecondaryPricePlan() == pricePlanID
            && SafetyUtil.safeEquals(subscriber.getSecondaryPricePlanStartDate(), start)
            && SafetyUtil.safeEquals(subscriber.getSecondaryPricePlanEndDate(), end))
        {
            // nothing to do.
            return SuccessCodeEnum.SUCCESS.getValue();
        }

        try
        {
            subscriber.setSecondaryPricePlan(pricePlanID);
            subscriber.setSecondaryPricePlanStartDate(CalendarSupportHelper.get(ctx).calendarToDate(start));
            if (end != null)
            {
                subscriber.setSecondaryPricePlanEndDate(CalendarSupportHelper.get(ctx).calendarToDate(end));
            }
            else
            {
                subscriber.setSecondaryPricePlanEndDate(CalendarSupportHelper.get(ctx).findDateYearsAfter(CommonTime.YEARS_IN_FUTURE, CalendarSupportHelper.get(ctx).calendarToDate(start)));
            }
            Object resultSub = home.store(ctx, subscriber);
            SubscribersApiSupport.handlePostSubscriptionUpdate(ctx, (Subscriber)resultSub,true);
        }
        catch (final Exception e)
        {
            final String msg = "Unable to update Secondary Price Plan for Subscription with ID/MobileNumber/SubscriptionType = "
                + subscriptionRef.getIdentifier() + "/" + subscriptionRef.getMobileNumber() + "/" + subscriptionRef.getSubscriptionType()
                + " and new pricePlanID=" + pricePlanID + ". ";
            RmiApiErrorHandlingSupport.handleStoreExceptions(ctx, e, msg, this);
        }

        return SuccessCodeEnum.SUCCESS.getValue();

    }


    /**
     * {@inheritDoc}
     */
    public SuccessCode updateSubscriptionState(final CRMRequestHeader header, final SubscriptionReference subscriptionRef,
        final SubscriptionState newState, GenericParameter[] parameters) throws CRMExceptionFault
    {
        final Context ctx = getContext().createSubContext();

        QueryExecutorFactory executor = QueryExecutorFactory.getInstance();

        return executor.execute(ctx, SubscriptionServiceSkeletonInterface.class.getSimpleName(), "updateSubscriptionState", SuccessCode.class, 
                header, subscriptionRef, newState, parameters);

    }


    /**
     * {@inheritDoc}
     */
    public SuccessCode updateSubscriptionRechargeVoucher(final CRMRequestHeader header, final SubscriptionReference subscriptionRef,
        final String voucher, GenericParameter[] parameters) throws CRMExceptionFault
    {
        final Context ctx = getContext().createSubContext();
        // handle login. Failed logins throw exceptions
        RmiApiSupport.authenticateUser(ctx, header, "updateSubscriptionRechargeVoucher",
            Constants.PERMISSION_SUBSCRIBERS_WRITE_UPDATESUBSCRIPTIONRECHARGEVOUCHER,
            Constants.PERMISSION_SUBSCRIBERS_WRITE_UPDATESUBSCRIBERRECHARGEVOUCHER);
        
        GenericParameterParser parser = new GenericParameterParser(parameters);
                
        boolean validateOnly = parser.getParameter(VOUCHER_VALIDATE_ONLY, Boolean.class, Boolean.FALSE);
        
        Integer category_id = parser.getParameter(APIGenericParameterSupport.CATEGORY_ID, Integer.class, -1);
        Boolean isSecondaryBalance = parser.getParameter(APIGenericParameterSupport.SECONDARY_BALANCE,  Boolean.class, Boolean.FALSE);
        Boolean applyTax = parser.getParameter(APIGenericParameterSupport.APPLY_TAX, Boolean.class,	Boolean.FALSE);
        
        ctx.put(APIGenericParameterSupport.CATEGORY_ID, category_id);
        ctx.put(APIGenericParameterSupport.SECONDARY_BALANCE, isSecondaryBalance);
        ctx.put(APIGenericParameterSupport.APPLY_TAX, applyTax);
        
        final VoucherOrder order = new VoucherOrder();
        order.setSubscriptionRef(subscriptionRef);
        order.setVoucher(voucher);
        final VoucherOrder result = updateSubscriptionRechargeVoucher(ctx, order, null, validateOnly);
        if (result.getException() != null)
        {
            throw CRMExceptionFactory.create(result.getException());
        }

        return SuccessCodeEnum.SUCCESS.getValue();

    }


    /**
     * Burns a voucher.
     *
     * @param ctx
     *            The operating context.
     * @param order
     *            Voucher order.
     * @param label
     *            Label of the order. Use <code>null</code> if order does not have
     *            label.
     * @return The processed order.
     */
    protected VoucherOrder updateSubscriptionRechargeVoucher(final Context ctx, final VoucherOrder order,
        final String label, boolean validateOnly) throws CRMExceptionFault
    {
        final VoucherOrder result = new VoucherOrder();
        result.setOrderResult(ResultCodeEnum.NOT_PROCESSED.getValue());

        final String prefix;
        if (label == null || label.trim().length() == 0)
        {
            prefix = "";
        }
        else
        {
            prefix = label + ".";
        }

        RmiApiErrorHandlingSupport.validateMandatoryObject(order, label);
        
        final String voucher = order.getVoucher();
        SubscriptionReference subscriptionRef = order.getSubscriptionRef();

        RmiApiErrorHandlingSupport.validateMandatoryObject(voucher, prefix + "voucher");
        RmiApiErrorHandlingSupport.validateMandatoryObject(subscriptionRef, prefix + "subscriptionRef");

        final Subscriber subscriber = SubscribersApiSupport.getCrmSubscriber(ctx, subscriptionRef, this);
        /*
         * TT 8022700039: Update the subscriber reference.
         */
        subscriptionRef = SubscriberToApiAdapter.adaptSubscriberToReference(ctx, subscriber, subscriptionRef);
        result.setSubscriptionRef(subscriptionRef);
        result.setVoucher(voucher);

        try
        {

            final StringBuilder errorMsgPrefix = new StringBuilder("Unable to Recharge Voucher for Subscription ");
            errorMsgPrefix.append("with ID/MobileNumber/SubscriptionType = ");
            errorMsgPrefix.append(subscriptionRef.getIdentifier());
            errorMsgPrefix.append("/");
            errorMsgPrefix.append(subscriptionRef.getMobileNumber());
            errorMsgPrefix.append("/");
            errorMsgPrefix.append(subscriptionRef.getSubscriptionType());
            errorMsgPrefix.append(" and new voucher=");
            errorMsgPrefix.append(voucher);

            final MessageMgr manager = new MessageMgr(ctx, CSRVoucherRechargeServicer.class);
            final HTMLExceptionListener exceptions = new HTMLExceptionListener(manager);
            ctx.put(ExceptionListener.class, exceptions);

            try
            {
                final ManualVourcherRechargingForm form = new ManualVourcherRechargingForm();
                form.setMSISDN(subscriber.getMSISDN());
                form.setVoucherNum(voucher);

                CSRVoucherRechargeServicer.processForm(ctx, form, validateOnly);
                result.setOrderResult(ResultCodeEnum.SUCCESS.getValue());
                result.setException(null);
            }
            catch (final Exception e)
            {
                final String msg = errorMsgPrefix.toString() + ". ";
                RmiApiErrorHandlingSupport.handleStoreExceptions(ctx, e, msg, this);
            }

            if (exceptions.hasErrors())
            {
                final Iterator it = exceptions.getExceptions().iterator();
                final Exception e = (Exception) it.next();
                errorMsgPrefix.append('\n');
                RmiApiErrorHandlingSupport.handleStoreExceptions(ctx, e, errorMsgPrefix.toString(), this);
            }
        }
        catch (final com.redknee.util.crmapi.wsdl.v3_0.api.CRMExceptionFault exception)
        {
            result.setOrderResult(ResultCodeEnum.FAILURE.getValue());
            result.setException(exception.getFaultMessage().getCRMException());
            LogSupport.debug(ctx, this, "CRMException caught", exception);
        }
        catch (final Exception exception)
        {
            result.setOrderResult(ResultCodeEnum.FAILURE.getValue());
            final CRMException newException = new CRMException();
            newException.setCode(ExceptionCode.GENERAL_EXCEPTION);
            newException.setMessage("Unable to recharge voucher: " + exception.getMessage());
            result.setException(newException);
        }

        return result;
    }


    /**
     * {@inheritDoc}
     */
    public SuccessCode updateSubscriptionEnablePricePlanOption(final CRMRequestHeader header,
        final SubscriptionReference subscriptionRef, final PricePlanOptionType optionType, final long optionID, final Calendar start,
        final Calendar end, final Integer numberOfPayments, GenericParameter[] parameters) throws CRMExceptionFault
    {
        final Context ctx = getContext().createSubContext();
        // handle login. Failed logins throw exceptions
        RmiApiSupport.authenticateUser(ctx, header, "updateSubscriptionEnablePricePlanOption",
            Constants.PERMISSION_SUBSCRIBERS_WRITE_UPDATESUBSCRIPTIONENABLEPRICEPLANOPTION,
            Constants.PERMISSION_SUBSCRIBERS_WRITE_UPDATESUBSCRIBERENABLEPRICEPLANOPTION);

        RmiApiErrorHandlingSupport.validateMandatoryObject(optionType, "optionType");

        final Subscriber subscriber = SubscribersApiSupport.getCrmSubscriber(ctx, subscriptionRef, this);
        final Home home =updateSubscriberPipeline(ctx,"updateSubscriptionEnablePricePlanOption");

        if (subscriber.isInFinalState())
        {
            RmiApiErrorHandlingSupport.simpleValidation("subscriptionRef",
                "Subscription is in a closed state. Cannot enable Price Plan Option of subscriber.");
        }

        try
        {
            Date startDate = null;
            if (start != null)
            {
                startDate = CalendarSupportHelper.get(ctx).calendarToDate(start);
            }
            Date endDate = null;
            if (end != null)
            {
                endDate = CalendarSupportHelper.get(ctx).calendarToDate(end);
            }
            ApiResultHolder resHolder = new ApiResultHolder() ;
            SubscribersApiSupport.enablePricePlanOption(ctx, subscriber, optionType, 
                    optionID, startDate, endDate, numberOfPayments, 
                        null, false, resHolder,false,Long.valueOf(-1) );
            Object resultSub = home.store(ctx, subscriber);
            SubscribersApiSupport.handlePostSubscriptionUpdate(ctx, (Subscriber)resultSub,true);
        }
        catch (final com.redknee.util.crmapi.wsdl.v3_0.api.CRMExceptionFault e)
        {
            throw e;
        }
        catch (final Exception e)
        {
            final String msg = "Unable to enable price plan options for Subscription with ID/MobileNumber/SubscriptionType = "
                + subscriptionRef.getIdentifier() + "/" + subscriptionRef.getMobileNumber() + "/" + subscriptionRef.getSubscriptionType()
                + " and new optionType="
                + optionType + " optionID=" + optionID + ". ";
            RmiApiErrorHandlingSupport.handleStoreExceptions(ctx, e, msg, this);
        }

        return SuccessCodeEnum.SUCCESS.getValue();

    }


    /**
     * {@inheritDoc}
     */
    public SuccessCode updateSubscriptionDisablePricePlanOption(final CRMRequestHeader header,
            final SubscriptionReference subscriptionRef, final PricePlanOptionType optionType, final long optionID,
            GenericParameter[] parameters) throws CRMExceptionFault
    {
        final Context ctx = getContext().createSubContext();
   // handle login. Failed logins throw exceptions
        RmiApiSupport.authenticateUser(ctx, header, "updateSubscriptionDisablePricePlanOption",
            Constants.PERMISSION_SUBSCRIBERS_WRITE_UPDATESUBSCRIPTIONDISABLEPRICEPLANOPTION,
            Constants.PERMISSION_SUBSCRIBERS_WRITE_UPDATESUBSCRIBERDISABLEPRICEPLANOPTION);

        RmiApiErrorHandlingSupport.validateMandatoryObject(optionType, "optionType");

        final Subscriber subscriber = SubscribersApiSupport.getCrmSubscriber(ctx, subscriptionRef, this);
        final Home home = updateSubscriberPipeline(ctx,"updateSubscriptionDisablePricePlanOption");

        if (subscriber.isInFinalState())
        {
            RmiApiErrorHandlingSupport.simpleValidation("subscriptionRef",
                "Subscription is in a closed state. Cannot disable Price Plan Option of subscriber.");
        }

        try
        {
            GenericParameterParser parser = new GenericParameterParser(parameters);
            Long secondaryID = parser.getParameter(APIGenericParameterSupport.CALLING_GROUP_ID, Long.class);
            
            SubscribersApiSupport.disablePricePlanOption(ctx, subscriber, optionType, optionID, secondaryID);
            Object resultSub = home.store(ctx, subscriber);
            SubscribersApiSupport.handlePostSubscriptionUpdate(ctx, (Subscriber)resultSub,true);
        }
        catch (final com.redknee.util.crmapi.wsdl.v3_0.api.CRMExceptionFault e)
        {
            throw e;
        }
        catch (final Exception e)
        {
            final String msg = "Unable to disable Price Plan Options for Subscription with ID/MobileNumber/SubscriptionType = "
                + subscriptionRef.getIdentifier() + "/" + subscriptionRef.getMobileNumber() + "/" + subscriptionRef.getSubscriptionType()
                + " and new optionType="
                + optionType + " optionID=" + optionID + ". ";
            RmiApiErrorHandlingSupport.handleStoreExceptions(ctx, e, msg, this);
        }

        return SuccessCodeEnum.SUCCESS.getValue();

    }


    /**
     * {@inheritDoc}
     */
    public PricePlanOptionUpdateResult[] updateSubscriptionPricePlanOptions(final CRMRequestHeader header,
            final SubscriptionReference subscriptionRef, final SubscriptionPricePlan options, GenericParameter[] parameters) throws CRMExceptionFault
    {
        final Context ctx = getContext().createSubContext();
        // handle login. Failed logins throw exceptions
        RmiApiSupport.authenticateUser(ctx, header, "updateSubscriptionPricePlanOptions",
                Constants.PERMISSION_SUBSCRIBERS_WRITE_UPDATESUBSCRIPTIONPRICEPLANOPTIONS);
        Subscriber subscriber = SubscribersApiSupport.getCrmSubscriber(ctx, subscriptionRef, this);
        com.redknee.framework.xhome.msp.MSP.setBeanSpid(ctx, subscriber.getSpid());
        PricePlanOptionUpdateResult[] results = new PricePlanOptionUpdateResult[]
            {};
        try
        {
        	if( header.getAgentID() != null && header.getAgentID().equals(""))
        	{
        		ctx.put(Lookup.FUTURE_DATED_PRICEPLAN_CHANGE, true);
        		if(LogSupport.isDebugEnabled(ctx))
        		{
        			LogSupport.debug(ctx, this, "The call is intitated from ScheduledPriceplanChangeExecutor ");
        		}
        	}
        	com.redknee.util.crmapi.wsdl.v3_0.types.serviceandbundle.PricePlanOption[] subOptions = options.getItems();
			if (null != subOptions) {
				for (int i = 0; i < subOptions.length; i++) {
					com.redknee.util.crmapi.wsdl.v3_0.types.serviceandbundle.PricePlanOption item = subOptions[i];
					if (LogSupport.isDebugEnabled(ctx)) {
						LogSupport.debug(ctx, this, "Value of PricePlanOption Object:: " + item);
					}
					if (item == null) {
						continue;
					}
					GenericParameterParser paramsParser = new GenericParameterParser(item.getParameters());
					if (paramsParser.containsParam(APIGenericParameterSupport.CHANGE_INDICATOR)) {
						String changeIndicator = paramsParser.getParameter(APIGenericParameterSupport.CHANGE_INDICATOR,String.class);
						if (LogSupport.isDebugEnabled(ctx)) {
							LogSupport.debug(ctx, this, "Value of changeIndicator:: " + changeIndicator);
						}
						if (changeIndicator != null) {
							if (changeIndicator.equals(APIGenericParameterSupport.CHANGE_INDICATOR_OPTION_MODIFIED)) {
								ctx.put(APIGenericParameterSupport.CHANGE_INDICATOR_APPLICABLE, Boolean.TRUE);
							} else {
								RmiApiErrorHandlingSupport.simpleValidation("changeIndicator","Specified value" + changeIndicator+ " is not a valid value for generic parameter "
												+ APIGenericParameterSupport.CHANGE_INDICATOR);
							}
						}
					}
					if (paramsParser.containsParam(APIGenericParameterSupport.VALID_AFTER)) {
						modifyStartDateWithValidAfter(ctx, subscriber,
								paramsParser.getParameter(APIGenericParameterSupport.VALID_AFTER, Integer.class),
								item.getIdentifier());
					}
				}
			}
            results = updateSubscriptionRating(ctx, subscriber, options,true, "updateSubscriptionPricePlanOptions");
        }
        catch (CRMExceptionFault e)
        {
            throw e;
        }
        catch (final Exception e)
        {
            final String msg = "Unable to update Price Plan Options for Subscription with ID/MobileNumber/SubscriptionType = "
                    + subscriptionRef.getIdentifier()
                    + "/"
                    + subscriptionRef.getMobileNumber()
                    + "/"
                    + subscriptionRef.getSubscriptionType();
            RmiApiErrorHandlingSupport.handleStoreExceptions(ctx, e, msg, this);
        }
        return results;
    }

	private void modifyStartDateWithValidAfter(final Context ctx, Subscriber subscriber, int validAfter, long serviceId)
			throws HomeException {
		int updatedVal = 0;
		PricePlanVersion pricePlanVersion = subscriber.getPricePlan(ctx);
		Set<ServiceFee2ID> serviceIds = pricePlanVersion.getServices(ctx);
		boolean considerValidAfter = false;

		for (ServiceFee2ID serviceFee2ID : serviceIds) {
			if (serviceId == serviceFee2ID.getServiceId()) {
				considerValidAfter = true;
				break;
			}
		}

		if (considerValidAfter) {
			HomeSupport homeSupport = HomeSupportHelper.get(ctx);
			Collection<Service> services = homeSupport.getBeans(ctx, Service.class, new EQ(ServiceXInfo.ID, serviceId));
			if (null != services && 0 < services.size()) {
				services.iterator().hasNext();
				Service service = services.iterator().next();
				if (ServiceSubTypeEnum.DISCOUNT == service.getServiceSubType()) {
					ServicePeriodEnum chargeScheme = service.getChargeScheme();
					if (ServicePeriodEnum.MONTHLY == chargeScheme) {
						updatedVal = validAfter * 30;
					} else if (ServicePeriodEnum.WEEKLY == chargeScheme) {
						updatedVal = validAfter * 7;
					} else if (ServicePeriodEnum.MULTIDAY == chargeScheme) {
						updatedVal = 0;
					} else if (ServicePeriodEnum.MULTIDAY == chargeScheme) {
						updatedVal = (30 / service.getRecurrenceInterval()) * validAfter;
					}
				}
			}
			if (0 < updatedVal) {
				And andCondition = new And();
				andCondition.add(new EQ(SubscriberServicesXInfo.SERVICE_ID, serviceId));
				andCondition.add(new EQ(SubscriberServicesXInfo.SUBSCRIBER_ID, subscriber.getId()));
				Collection<SubscriberServices> subscriberServices = homeSupport.getBeans(ctx, SubscriberServices.class,
						andCondition);
				if (null != subscriberServices && 0 < subscriberServices.size()) {
					subscriberServices.iterator().hasNext();
					SubscriberServices subscriberService = subscriberServices.iterator().next();
					Calendar calendar = Calendar.getInstance();
					calendar.setTime(subscriberService.getStartDate());
					calendar.add(Calendar.DAY_OF_MONTH, updatedVal);
					subscriberService.setStartDate(calendar.getTime());
					homeSupport.storeBean(ctx, subscriberService);
				}
			}
		}
	}

    public PricePlanOptionUpdateResult[] updateSubscriptionRating(final Context ctx, Subscriber subscriber,
            final SubscriptionPricePlan options, boolean diffRequired, final String caller) throws CRMExceptionFault, Exception
    {
        PricePlanOptionUpdateResult[] results = new PricePlanOptionUpdateResult[]
            {};
        final Home home = updateSubscriberPipeline(ctx, caller);
        if (subscriber == null || subscriber.isInFinalState())
        {
            RmiApiErrorHandlingSupport
                    .simpleValidation("subscriptionRef",
                            "Either subscription doesn't exist or subscription is in a closed state. Unable to update price plan options.");
        }
        
        if(subscriber.isPostpaid() && subscriber.getState().equals(SubscriberStateEnum.SUSPENDED)){
          	 RmiApiErrorHandlingSupport
               .simpleValidation("subscriptionRef",
                       "Unable to update option for subscription in Suspended state.");
          	
          }else if(subscriber.isPrepaid() && subscriber.getState().equals(SubscriberStateEnum.LOCKED)){
          	 RmiApiErrorHandlingSupport
               .simpleValidation("subscriptionRef",
                       "Unable to update option for subscription in Barred/Locked state.");
          	
          }
     
        final boolean requiresValidation = SubscribersApiSupport.isSubscriptionPricePlanSwitching(ctx, subscriber,
                options);
        if (requiresValidation)
        {
            PricePlanSwitchLimitSupport.validatePricePlanSwitchThreshold(ctx, subscriber);
        }

        ApiResultHolder resH = new ApiResultHolder();
        resH.initSubscriberState(ctx, subscriber);
        
        if (diffRequired)
        {
			if (null != ctx.get(APIGenericParameterSupport.CHANGE_INDICATOR_APPLICABLE)) {
				if ((Boolean) ctx.get(APIGenericParameterSupport.CHANGE_INDICATOR_APPLICABLE)) {
					if (LogSupport.isDebugEnabled(ctx)) {
						LogSupport.debug(ctx, this, "Inside Change Indicator Applicable Condition ");
					}
					SubscribersApiSupport.disableServiceForChangeIndicator(ctx, subscriber, options, false, resH);
					ApiOptionsUpdateResultSupport.installApiResultSetInContext(ctx, resH);
					subscriber = (Subscriber) home.store(ctx, subscriber);
					ApiOptionsUpdateResultSupport.unInstallApiResultSetFromContext(ctx);
					resH = new ApiResultHolder();
					resH.initSubscriberState(ctx, subscriber);
				}
			}
        	Set oldSubSvcs = (Set) SubscriberServicesSupport.getNonUnProvisionedSubscriberServices(ctx,
                    subscriber.getId());
            Set<Long> oldSubBundles = new HashSet<Long>();
            for (final Iterator<Long> iterator = subscriber.getProvisionedBundles().iterator(); iterator.hasNext();)
            {
                oldSubBundles.add(new Long(iterator.next().longValue()));
            }
            Set oldSubAuxSvcs = (Set) subscriber.getProvisionedAuxiliaryServices(ctx);

            // Updating options
            subscriber = SubscribersApiSupport.updatePricePlanOptions(ctx, subscriber, options, false, resH);
            ctx.put(Lookup.PRICEPLAN_SWITCH_COUNTER_INCREMENT, true);
            
            ApiOptionsUpdateResultSupport.installApiResultSetInContext(ctx, resH);
            
            // Saving subscription with new options
            subscriber = (Subscriber) home.store(ctx, subscriber);
            
            Set newSubSvcs = (Set) SubscriberServicesSupport.getNonUnProvisionedSubscriberServices(ctx,
                    subscriber.getId());
            Set<Long> newSubBundles = subscriber.getProvisionedBundles();
            Set newSubAuxSvcs = (Set) subscriber.getProvisionedAuxiliaryServices(ctx);
            
            Set<Long> repurchasedBundles = resH.getBundlesToppedUpSuccessfully();
            
            /*
             * Sujeet: Refactor the method to make use of the ApiResultHolder, so as to avoid bloating
             * the parameter-space with all sorts of data-structures.
             */
            results = SubscribersApiSupport.diffSubcriptionOptions(ctx, subscriber.getId(), newSubAuxSvcs,
                    newSubBundles, newSubSvcs, oldSubAuxSvcs, oldSubBundles, oldSubSvcs, 
                    resH, options.getItems(), subscriber.getPricePlan(), subscriber.getLastModified());
            
            ApiOptionsUpdateResultSupport.unInstallApiResultSetFromContext(ctx);
        }
        else
        {
            subscriber = SubscribersApiSupport.updatePricePlanOptions(ctx, subscriber, options, false, resH);
            ctx.put(Lookup.PRICEPLAN_SWITCH_COUNTER_INCREMENT, true);
            subscriber = (Subscriber) home.store(ctx, subscriber);           
        }
        SubscribersApiSupport.handlePostSubscriptionUpdate(ctx, subscriber, true);
        return results;
    }

    /**
     * {@inheritDoc}
     */
    public SuccessCode updateSubscriptionProfile(final CRMRequestHeader header, final SubscriptionReference subscriptionRef,
        final MutableSubscriptionProfile profile, final MutableSubscriptionBilling billing, final GenericParameter[] parameters) throws CRMExceptionFault
    {
        final Context ctx = getContext().createSubContext();
        // handle login. Failed logins throw exceptions
        RmiApiSupport.authenticateUser(ctx, header, "updateSubscriptionProfile",
            Constants.PERMISSION_SUBSCRIBERS_WRITE_UPDATESUBSCRIPTIONPROFILE,
            Constants.PERMISSION_SUBSCRIBERS_WRITE_UPDATESUBSCRIBERPROFILE);

        final Subscriber subscriber = SubscribersApiSupport.getCrmSubscriber(ctx, subscriptionRef, this);
        // decorate home for additional validation
        SubscriberIdentificationValidator.decorateHome(ctx);
        final Home home = updateSubscriberPipeline(ctx,"updateSubscriptionProfile");

        if (subscriber.isInFinalState())
        {
            RmiApiErrorHandlingSupport.simpleValidation("subscriptionRef",
                "Subscription is in a closed state. Cannot update subscriber!");
        }

        try
        {

            // TODO: Check the correct parameter name for the monthly spend limit.
            if (parameters!=null)
            {
                GenericParameterParser parser = new GenericParameterParser(parameters);
                
                // TT#12112023031 : Instaed of checking null, now, checking for containsParam.
                // This way if null comes, even that can be set.
                // It is expected that, client will not send anything, if it has to not update the param.

                if (parser.containsParam(MONTHLY_SPEND_LIMIT))
                {
                    subscriber.setMonthlySpendLimit(parser.getParameter(MONTHLY_SPEND_LIMIT, Long.class));
                }
                
                if (parser.containsParam(OVERDRAFT_BALANCE_LIMIT))
                {
                    SubscribersApiSupport.handleOverdraftBalanceLimitExtensionInExtensionList(ctx, subscriber, parser.getParameter(OVERDRAFT_BALANCE_LIMIT, Long.class), this);
                }

                if (parser.containsParam(START_DATE))
                {
                    subscriber.setStartDate(parser.getParameter(START_DATE, Date.class));
                }
                
                if (parser.containsParam(END_DATE))
                {
                	subscriber.setEndDate(parser.getParameter(END_DATE, Date.class));
                }
                
                if (parser.containsParam(APIGenericParameterSupport.DEVICE_TYPE))
                {
                	subscriber.setDeviceTypeId(parser.getParameter(APIGenericParameterSupport.DEVICE_TYPE, Long.class));
                }
                
                if(parser.containsParam(APIGenericParameterSupport.DEVICE_MODEL_NAME))
                {
                	subscriber.setDeviceName(parser.getParameter(APIGenericParameterSupport.DEVICE_MODEL_NAME, String.class));
                }
                
                if(parser.containsParam(APIGenericParameterSupport.IMEI))
                {
                	subscriber.setDeviceImei(parser.getParameter(APIGenericParameterSupport.IMEI, String.class));
                }
                
                if(parser.containsParam(APIGenericParameterSupport.DEVICE_LIST_PRICE))
                {
                	subscriber.setDeviceListPrice(parser.getParameter(APIGenericParameterSupport.DEVICE_LIST_PRICE, Long.class));
                }
                
                if(parser.containsParam(APIGenericParameterSupport.ATU_CREDIT_CARD_TOKEN))
                {
                    subscriber.setAtuCCTokenId(parser.getParameter(APIGenericParameterSupport.ATU_CREDIT_CARD_TOKEN, Long.class));
                }
                
                if(parser.containsParam(Common.SYNC_EXTERNAL_SERVICES))
                {
                    ctx.put(Common.SYNC_EXTERNAL_SERVICES, parser.getParameter(Common.SYNC_EXTERNAL_SERVICES, Boolean.class));
                }
                
                if(parser.containsParam(Common.LAST_MODIFIED))
                {
                    ctx.put(Common.LAST_MODIFIED, parser.getParameter(Common.LAST_MODIFIED, Long.class));
                }  
                
                //UMP-176 - Store a service address for an existing customer
				if (parser.containsParam(APIGenericParameterSupport.ADDRESS_ID)) 
				{
					String addressId = parser.getParameter(APIGenericParameterSupport.ADDRESS_ID, String.class);

					if (addressId != null && !addressId.isEmpty()) 
					{
						validateServiceAddress(ctx, subscriber, addressId);
						addSubscriberSupplementaryData(subscriber, APIGenericParameterSupport.ADDRESS_ID, addressId);
					} 
				}
                
				//UMP-137 - Store an Alias for an existing customer
				if(parser.containsParam(APIGenericParameterSupport.ALIAS)){
					String alias = parser.getParameter(APIGenericParameterSupport.ALIAS, String.class);
					
					if (alias != null) 
					{
						addSubscriberSupplementaryData(subscriber, APIGenericParameterSupport.ALIAS, alias);
						
					}
				}
				
                setBalanceThresholdAndTopupValue(ctx, subscriber, parser);
                setGroupScreeningTemplate(ctx, subscriber, parser);
            }
            
                        
            SubscribersApiSupport.updateSubscriber(ctx, subscriber, profile, billing);

            Object resultSub = home.store(ctx, subscriber);
            SubscribersApiSupport.handlePostSubscriptionUpdate(ctx, (Subscriber)resultSub,true);
            
        }
        catch (final com.redknee.util.crmapi.wsdl.v3_0.api.CRMExceptionFault e)
        {
            throw e;
        }
        catch (final Exception e)
        {
            final String msg = "Unable to update profile for Subscription with ID/MobileNumber/SubscriptionType = "
                + subscriptionRef.getIdentifier() + "/" + subscriptionRef.getMobileNumber() + "/" + subscriptionRef.getSubscriptionType();
            RmiApiErrorHandlingSupport.handleStoreExceptions(ctx, e, msg, this);
        }

        return SuccessCodeEnum.SUCCESS.getValue();

    }


    /**
     * {@inheritDoc}
     */
    @Override
    public SubscriptionReference createSubscription(CRMRequestHeader header, Long templateID,
            SubscriptionProfile profile, SubscriptionStatus status, SubscriptionRating rating,
            SubscriptionBilling billing, CardPackage cardPackage, SubscriptionPricePlan options,
            BaseSubscriptionExtension[] extension, GenericParameter[] parameters) throws CRMExceptionFault
    {
        final Context ctx = getContext().createSubContext();
        final HomeSupport homeSupport = HomeSupportHelper.get(ctx);
        // handle login. Failed logins throw exceptions
        RmiApiSupport.authenticateUser(ctx, header, "createSubscription",
            Constants.PERMISSION_SUBSCRIBERS_WRITE_CREATESUBSCRIPTION,
            Constants.PERMISSION_SUBSCRIBERS_WRITE_CREATESUBSCRIBER);
        Msisdn msisdn = null;
        Msisdn createdMsisdn = null;
        GenericPackage createdCard = null;
        
        Date requestStartTime = new Date();

        boolean subscriberCreated = false;
        Subscriber resultSub = null;
        
        GenericParameterParser parser = null;
        String csa = null;
        if (parameters!=null)
        {
            parser = new GenericParameterParser(parameters);
        }
        
      
        try
        {
            TechnologyEnum technology = null;
            CRMSpid sp = null;
            

            if (profile != null)
            {
                if (profile.getTechnologyType() != null)
                {
                    technology = RmiApiSupport.convertApiTechnology2Crm(profile.getTechnologyType());
                }

                // TODO this should be overriden by account SPID value
                sp = RmiApiSupport.getCrmServiceProvider(ctx, profile.getSpid(), this);

                MSP.setBeanSpid(ctx, sp.getId());
                
                if (profile.getMobileNumber() != null)
                {
                    msisdn = MobileNumbersApiSupport.getCrmMsisdn(ctx, profile.getMobileNumber(), this);
                }
                
                if (!SubscriptionType.isSubscriptionTypeExisting(ctx, profile.getSubscriptionType()))
                {
                    String msg = "Subscription Type " + profile.getSubscriptionType();
                    RmiApiErrorHandlingSupport.identificationException(ctx, msg, this);
                }
                
                if (!SubscriptionClass.isSubscriptionClassExisting(ctx, profile.getSubscriptionClass()))
                {
                    String msg = "Subscription Class " + profile.getSubscriptionClass();
                    RmiApiErrorHandlingSupport.identificationException(ctx, msg, this);
                }
            }

            Boolean portIn =  (null==parser)?Boolean.FALSE:parser.getParameter(APIGenericParameterSupport.PORT_IN,Boolean.class);
            if(Boolean.TRUE.equals(portIn))
            {
                ctx.put(MsisdnPortHandlingHome.MSISDN_PORT_KEY, profile.getMobileNumber());
            }

            SubscribersApiSupport.validateCreateSubscription(ctx, profile, status, rating, billing, msisdn, cardPackage, sp, false, options);

            final Account account = AccountsImpl.getCrmAccount(ctx, profile.getAccountID(), this);
            if (account.getSpid() != sp.getId())
            {
                RmiApiErrorHandlingSupport.simpleValidation("profile.spid",
                "Account and new subscription must be on the same Service Provider.");
            }
            ctx.put(Account.class, account);

            // decorate home for additional validation
            SubscriberIdentificationValidator.decorateHome(ctx);
            final Home home = updateSubscriberPipeline(ctx,"createSubscription");

            final SubscriberTypeEnum type = RmiApiSupport.convertApiPaidType2CrmSubscriberType(profile.getPaidType());

            if (SubscriberTypeEnum.PREPAID.equals(type) && templateID != null)
            {
                // retrieve to validate existence
                final ServiceActivationTemplate sat = SubscribersApiSupport.getCrmCreationTemplate(ctx, templateID);
                if (sat.getSpid() != sp.getId())
                {
                    RmiApiErrorHandlingSupport.simpleValidation("profile.spid",
                        "Subscription creation template and subscription must be on the same Service Provider.");
                }
            }

            // handle non existent MSISDNS
            if (msisdn == null)
            {
               createdMsisdn = msisdn = MobileNumbersApiSupport.createMobileNumber(ctx, profile.getMobileNumber(), Boolean.TRUE.equals(portIn) , type, sp,
                    technology, this);
            }
            else
            {
                if(Boolean.TRUE.equals(portIn))
                {
                    MsisdnManagement.markMsisdnPortedIn(ctx,profile.getAccountID() ,msisdn.getMsisdn());
                }
                Claim.validateMsisdnTypeAndAvailable(ctx, profile.getAccountID(), type, profile.getMobileNumber(), "voiceMsisdn", false);
            }

            // handle auto create card packages
            if (cardPackage != null)
            {
                final TechnologyType tech = profile.getTechnologyType();
                CardPackageApiSupport.prepareCardPackage(cardPackage, profile.getSpid(), tech);
                createdCard = CardPackageApiSupport.createCrmCardPackage(ctx, cardPackage, this);
                profile.setCardPackageID(createdCard.getPackId());
            }

            final Subscriber sub = SubscribersApiSupport.constructSubscriber(ctx, templateID, profile, status, rating,
                billing, sp, technology, options);
            
            // TODO: Check the correct parameter name for the monthly spend limit.
            
            if (parser!=null)
            {
                Long monthlySpendLimit = parser.getParameter(MONTHLY_SPEND_LIMIT, Long.class);
                Long overdraftBalanceLimit = parser.getParameter(OVERDRAFT_BALANCE_LIMIT, Long.class);
                Date startDate = parser.getParameter(START_DATE, Date.class);
                Date endDate = parser.getParameter(END_DATE, Date.class);
                Long deviceType = parser.getParameter(APIGenericParameterSupport.DEVICE_TYPE, Long.class);
                Integer activationReasonCode = parser.getParameter(ACTIVATION_REASON_CODE, Integer.class);
                
                String deviceName = parser.getParameter(APIGenericParameterSupport.DEVICE_MODEL_NAME, String.class);
                String deviceImei = parser.getParameter(APIGenericParameterSupport.IMEI, String.class);
                Long devicePrice = parser.getParameter(APIGenericParameterSupport.DEVICE_LIST_PRICE, Long.class);
                csa = parser.getParameter(APIGenericParameterSupport.CSA, String.class);
                if (monthlySpendLimit!=null)
                {
                    sub.setMonthlySpendLimit(monthlySpendLimit);
                }
                
                if (overdraftBalanceLimit!=null)
                {
                    SubscribersApiSupport.handleOverdraftBalanceLimitExtensionInExtensionList(ctx, sub, overdraftBalanceLimit, this);
                }

                if (startDate != null)
                {
                    sub.setStartDate(startDate);
                }

                if (endDate != null)
                {
                    sub.setEndDate(endDate);
                }
                
                if (deviceType != null)
                {
                    sub.setDeviceTypeId(deviceType);
                }
                
                if (activationReasonCode != null)
                {
                    sub.setReasonCode(activationReasonCode);
                }
                
                if(deviceName != null)
                {
                	sub.setDeviceName(deviceName);
                }
                
                if(deviceImei != null)
                {
                	sub.setDeviceImei(deviceImei);
                }
                
                if(devicePrice != null)
                {
                	sub.setDeviceListPrice(devicePrice);
                }
                
                
                boolean initialBalanceByCreditCard = (null==parser) ? Boolean.FALSE : parser.getParameter(APIGenericParameterSupport.INITIAL_BALANCE_BY_CREDIT_CARD, Boolean.class, Boolean.FALSE);
                if(Boolean.TRUE.equals(initialBalanceByCreditCard))
                {
                    sub.setInitialBalanceByCreditCard(initialBalanceByCreditCard);
                }
                
                setBalanceThresholdAndTopupValue(ctx, sub, parser);
                setGroupScreeningTemplate(ctx, sub, parser);
                
                //UMP-176 - Store a service address for an existing subscriber
				if (parser.containsParam(APIGenericParameterSupport.ADDRESS_ID)) 
				{
					String addressId = parser.getParameter(APIGenericParameterSupport.ADDRESS_ID, String.class);

					if (addressId != null && !addressId.isEmpty()) 
					{
						validateServiceAddress(ctx, sub, addressId);
						addSubscriberSupplementaryData(sub, APIGenericParameterSupport.ADDRESS_ID, addressId);
					} 
				}
				
				//UMP-137 - Store an Alias for an existing subscriber
				if(parser.containsParam(APIGenericParameterSupport.ALIAS))
				{
					String alias = parser.getParameter(APIGenericParameterSupport.ALIAS, String.class);
					
					if (alias != null) 
					{
						addSubscriberSupplementaryData(sub, APIGenericParameterSupport.ALIAS, alias);
					} 
				}
            }
            
            if (extension != null)
            {
                for (int i=0; i<extension.length; i++)
                {
                    if (extension[i] instanceof com.redknee.util.crmapi.wsdl.v3_0.types.subscription.extensions.SubscriptionExtension)
                    {
                        com.redknee.util.crmapi.wsdl.v3_0.types.subscription.extensions.SubscriptionExtension apiExtension = (com.redknee.util.crmapi.wsdl.v3_0.types.subscription.extensions.SubscriptionExtension) extension[i];
                        
                        SubscriberExtensionToApiAdapter adapter = SubscriberExtensionToApiAdapter.getInstance(apiExtension, sub);
                        if (adapter!=null)
                        {
                            SubscriberExtension newExtension = adapter.toCRM(ctx, apiExtension, parameters);

                            ExtensionHolder holder = XBeans.instantiate(SubscriberExtensionHolder.class, ctx);
                            holder.setExtension(newExtension);
                            sub.getSubExtensions(ctx).add(holder);
                        }
                    }
                }
            }
            
            {
            	//fix for TT#12060555063 
	            GenericParameterParser genericParameterParser = new GenericParameterParser(parameters);
	            
	            String tokenValue = genericParameterParser.getParameter("CreditCardTokenValue", String.class);
	            if (tokenValue != null && !tokenValue.isEmpty())
	            {
	            	MsisdnManagement.associateMsisdnWithBAN(getContext(), msisdn.getMsisdn(), account, "voiceMsisdn", sub.getSubscriptionType());
	            }
            }

            ctx.put(CommonProvisionAgentBase.SPG_PROVISIONING_CLIENT_VERSION, "API");
            resultSub = (Subscriber) home.create(ctx, sub);
        
            if(parser != null)
            {
                try
                {
                    TechnologyEnum subscriberTech =  RmiApiSupport.convertApiTechnology2Crm(profile.getTechnologyType());
                    if(subscriberTech.getIndex() == TechnologyEnum.TDMA_INDEX || subscriberTech.getIndex() == TechnologyEnum.CDMA_INDEX)
                    {
                        String secondaryPackageId = null;
                        String msid = null;
                        
                        if(parser.containsParam(APIGenericParameterSupport.SECONDARY_PACKAGE))
                        {
                            secondaryPackageId = parser.getParameter(APIGenericParameterSupport.SECONDARY_PACKAGE, String.class);
                        }
                        if(parser.containsParam(APIGenericParameterSupport.MSID))
                        {
                            msid = parser.getParameter(APIGenericParameterSupport.MSID, String.class);
                        }
                        TDMAPackage primaryPackage =  PackageSupportHelper.get(ctx).getTDMAPackage(ctx, profile.getCardPackageID(), profile.getSpid());
                        if(secondaryPackageId != null && !secondaryPackageId.trim().isEmpty())
                        {
                            TDMAPackage secondaryPackage = PackageSupportHelper.get(ctx).getTDMAPackage(ctx, secondaryPackageId, profile.getSpid());
                            
                            SwapPackageSupport.mergeTDMAPackagesOnCreateSubscription(ctx, resultSub, primaryPackage, secondaryPackage, null, msid, secondaryPackageId);
                        }
                        /**
                         * Adding below else if to make sure MSID gets updated every time it is passed. 
                         * TT#13051750023.
                         **/
                        else if(msid != null && !msid.trim().isEmpty() && primaryPackage != null)
                        {
                            primaryPackage.setExternalMSID(msid);
                            HomeSupportHelper.get(ctx).storeBean(ctx, primaryPackage);
                        }
                    }
                }
                catch (Exception e) 
                {
                    final String msg = "Subscription created . Unable to update card package for subscription with mobile number " + profile.getMobileNumber()
                            + " in account " + profile.getAccountID() + ". ";
                    RmiApiErrorHandlingSupport.handleStoreExceptions(ctx, e, msg, this);
                }
            }        
            
       	 
            if(csa !=null)
        	{
            	try
            	{        
            		//UPDATE THE CSA VALUE IN XCONTACT AGAINST ACCOUNT
	             	Account subAccount = resultSub.getAccount(ctx);
	             	if(subAccount != null)
	             	{
	             		subAccount.setCsa(csa);
	             		HomeSupportHelper.get(ctx).storeBean(ctx, subAccount);
	             	}
            	} catch (HomeException homeEx)
                {   
                      LogSupport.major(ctx, this, "Unable to update CSA on createSubscription for SubId : "+ resultSub.getId() + homeEx);
                }
        	}
            
            SubscribersApiSupport.handlePostSubscriptionUpdate(ctx, resultSub, true);

        }
        catch (final CRMExceptionFault e)
        {
            throw e;
        }
        catch (final Exception e)
        {
            String subId = SubscribersApiSupport.getCreatedSubscriptionID(ctx, profile.getAccountID(), profile.getMobileNumber(), profile.getSubscriptionType(), requestStartTime, this);
            subscriberCreated = SubscribersApiSupport.cleanFailure(ctx, subId, createdMsisdn, createdCard, this);

            final String msg = "Unable to create Subscription with mobile number " + profile.getMobileNumber() + " in account " + profile.getAccountID() + ". ";
            RmiApiErrorHandlingSupport.handleCreateExceptions(ctx, e, msg, subscriberCreated, Subscriber.class, subId, this);
        }

        if (resultSub == null)
        {
            String subId = SubscribersApiSupport.getCreatedSubscriptionID(ctx, profile.getAccountID(), profile.getMobileNumber(), profile.getSubscriptionType(), requestStartTime, this);
            subscriberCreated = SubscribersApiSupport.cleanFailure(ctx, subId, createdMsisdn, createdCard, this);
            
            final String msg = "Subscription create failed Subscription " + profile + ". Reason UNKNOWN.";
            RmiApiErrorHandlingSupport.handleCreateExceptions(ctx, null, msg, subscriberCreated, Subscriber.class, subId, this);
        }
        
        SubscribersApiSupport.validateStateAfterCreateSubscription(ctx, resultSub, profile, status);

        final SubscriptionReference result = SubscriberToApiAdapter.adaptSubscriberToReference(ctx, resultSub);

        return result;

    }
    
	/**
	 * @param ctx
	 * @param subscriber
	 * @param addressId
	 * @return boolean
	 * @throws HomeInternalException
	 * @throws HomeException
	 */
	private void validateServiceAddress(Context ctx, Subscriber subscriber, String addressId)
			throws HomeInternalException, HomeException 
	{
		Address address = HomeSupportHelper.get(ctx).findBean(ctx, Address.class,
				new EQ(AddressXInfo.ADDRESS_ID, addressId));
		
		if (address == null) 
		{
			LogSupport.minor(ctx, this, "Unable to store service address for subscriber: "+subscriber.getId()+", Invalid addressId: "+addressId );
			throw new HomeException("Unable to store service address for subscriber: "+subscriber.getId()+", Invalid addressId: "+addressId );
		}
		
		
		List<SupplementaryData> suppDataList = subscriber.getSupplementaryDataList();
		if (suppDataList != null) 
		{
			Iterator<SupplementaryData> i = suppDataList.listIterator();
			while(i.hasNext())
			{
				SupplementaryData existingData = i.next();
				if(existingData.getKey().equals(APIGenericParameterSupport.ADDRESS_ID))
				{
					LogSupport.minor(ctx, this, "Address already exists as part of supplementary data for subscriber "+subscriber.getId()+" with addressId:"+existingData.getValue());
					throw new HomeException("Address already exists as part of supplementary data for subscriber "+subscriber.getId()+" with addressId:"+existingData.getValue());
				}
			}
		}
	}
    
	/**
	 * Adds subscriber supplementary data with the given key
	 * @param subscriber
	 * @param key
	 * @param value
	 * @throws HomeException 
	 */
	private void addSubscriberSupplementaryData(Subscriber subscriber, String key, String value) throws HomeException 
	{
		SupplementaryData supplementaryData = new SupplementaryData();
		supplementaryData.setEntity(SupplementaryDataEntityEnum.SUBSCRIPTION_INDEX);
		supplementaryData.setIdentifier(subscriber.getId());
		supplementaryData.setKey(key);
		supplementaryData.setValue(value);

		List<SupplementaryData> suppDataList = subscriber.getSupplementaryDataList();
		
		if (suppDataList == null)
		{
			suppDataList = new ArrayList<SupplementaryData>();
		}
		
		suppDataList.add(supplementaryData);

		subscriber.setSupplementaryDataSpid(subscriber.getSpid());
		subscriber.setSupplementaryDataList(suppDataList);
	}


    
    /**
     * @param ctx
     * @param sub
     * @param parser
     * @throws CRMExceptionFault 
     */
    private void setBalanceThresholdAndTopupValue(Context ctx, Subscriber sub, GenericParameterParser parser) throws CRMExceptionFault
    {
        if (parser != null)
        {
            if (parser.containsParam(APIGenericParameterSupport.BALANCE_THRESHOULD_AMOUNT))
            {
                if(!SubscriberSupport.isBalanceThresholdAllowed(ctx, sub))
                {
                    RmiApiErrorHandlingSupport.generalException(ctx, null, "Balance threshold not allowed by Service provider.", ExceptionCode.VALIDATION_EXCEPTION, this);
                }
                long balanceThresholdAmount = parser.getParameter(
                        APIGenericParameterSupport.BALANCE_THRESHOULD_AMOUNT, Long.class);
                sub.setAtuBalanceThreshold(balanceThresholdAmount);
            }

            if (parser.containsParam(APIGenericParameterSupport.AUTO_TOPUP_AMOUNT))
            {
                long autoTopupAmount = parser.getParameter(APIGenericParameterSupport.AUTO_TOPUP_AMOUNT,
                        Long.class);
                sub.setAtuAmount(autoTopupAmount);
            }
        }
    }



    /**
     * @param ctx
     * @param sub
     * @param parser
     * @throws HomeException
     * @throws HomeInternalException
     */
    private void setGroupScreeningTemplate(Context ctx, Subscriber sub, GenericParameterParser parser)
            throws CRMExceptionFault, HomeInternalException, HomeException
    {
        if (parser != null)
        {
            if (parser.containsParam(APIGenericParameterSupport.GROUP_SCREENING_TEMPLATE_ID))
            {
                int groupScreeningTemplateId = parser.getParameter(
                        APIGenericParameterSupport.GROUP_SCREENING_TEMPLATE_ID, Integer.class);

                if(sub.isPooledGroupLeader(ctx))
                {
                    RmiApiErrorHandlingSupport.generalException(ctx, null,
                            "GroupScreeningTemplate cannot be applied to group pooled leader subscription.", this);
                }
                
                if(!sub.isPooledMemberSubscriber(ctx))
                {
                    RmiApiErrorHandlingSupport.generalException(ctx, null,
                            "GroupScreeningTemplate cannot be applied to individual non pooled accounts.", this);
                }
                if(groupScreeningTemplateId == AbstractSubscriber.DEFAULT_GROUPSCREENINGTEMPLATEID)
                {
                	sub.setGroupScreeningTemplateId(groupScreeningTemplateId);
                	return;
                }
                
                Home home = (Home) ctx.get(GroupScreeningTemplateHome.class);
                GroupScreeningTemplate groupScreeningTemplate = (GroupScreeningTemplate) home.find(ctx, new EQ(
                        GroupScreeningTemplateXInfo.IDENTIFIER, groupScreeningTemplateId));
                if (groupScreeningTemplate !=null && groupScreeningTemplate.isActive())
                {
                    sub.setGroupScreeningTemplateId(groupScreeningTemplateId);
                }
                else
                {
                    RmiApiErrorHandlingSupport.generalException(ctx, null,
                            "GroupScreeningTemplate is not ACTIVE. Cannot apply to subscription.", this);
                }
            }
        }
    }

    
    
//    private void updateGroupScreeningTemplate(Context ctx, Subscriber sub, GenericParameterParser parser) throws CRMExceptionFault
//    {
//        setGroupScreeningTemplate(ctx, sub, parser);
//        
//        if (parser != null)
//        {
//            if (parser.containsParam(APIGenericParameterSupport.REMOVE_GROUP_SCREENING_TEMPLATE))
//            {
//                boolean removeGroupScreeningTemplateId = parser.getParameter(
//                        APIGenericParameterSupport.REMOVE_GROUP_SCREENING_TEMPLATE, Boolean.class);
//                if(removeGroupScreeningTemplateId)
//                {
//                    sub.setGroupScreeningTemplateId(AbstractSubscriber.DEFAULT_GROUPSCREENINGTEMPLATEID);
//                }
//            }
//        }
//    }

    /**
     * {@inheritDoc}
     */
    public SubscriptionProfileQueryResults getSubscriptionProfile(final CRMRequestHeader header,
        final SubscriptionReference subscriptionRef, GenericParameter[] parameters) throws CRMExceptionFault
    {
        final Context ctx = getContext().createSubContext();
        // handle login. Failed logins throw exceptions
        RmiApiSupport.authenticateUser(ctx, header, "getSubscriptionProfile",
            Constants.PERMISSION_SUBSCRIBERS_READ_GETSUBSCRIPTIONPROFILE,
            Constants.PERMISSION_SUBSCRIBERS_READ_GETSUBSCRIBERPROFILE);

        final Subscriber subscriber = SubscribersApiSupport.getCrmSubscriber(ctx, subscriptionRef, this);

        final SubscriptionProfileQueryResults subscriberResults = new SubscriptionProfileQueryResults();

        try
        {
            subscriberResults.setProfile(SubscribersApiSupport.extractProfile(subscriber));
            subscriberResults.setStatus(SubscribersApiSupport.extractStatus(subscriber));
            subscriberResults.setBilling(SubscribersApiSupport.extractBilling(ctx, subscriber));
            subscriberResults.setRating(SubscribersApiSupport.extractRating(ctx, subscriber));
        }
        catch (final com.redknee.util.crmapi.wsdl.v3_0.api.CRMExceptionFault e)
        {
            throw e;
        }
        catch (final Exception e)
        {
            final String msg = "Unable retrieve Subscription with ID/MobileNumber/SubscriptionType = "
                + subscriptionRef.getIdentifier() + "/" + subscriptionRef.getMobileNumber() + "/" + subscriptionRef.getSubscriptionType();
            RmiApiErrorHandlingSupport.handleQueryExceptions(ctx, e, msg, this);
        }

        return subscriberResults;

    }


    /**
     * {@inheritDoc}
     */
    public SuccessCode deleteSubscription(final CRMRequestHeader header, final SubscriptionReference subscriptionRef, GenericParameter[] parameters)
        throws CRMExceptionFault
    {
        final Context ctx = getContext().createSubContext();
        // handle login. Failed logins throw exceptions
        RmiApiSupport.authenticateUser(ctx, header, "deleteSubscription",
            Constants.PERMISSION_SUBSCRIBERS_WRITE_DELETESUBSCRIPTION,
            Constants.PERMISSION_SUBSCRIBERS_WRITE_DELETESUBSCRIBER);
        
        GenericParameterParser parser = null;
        
        if (parameters!=null)
        {
            parser = new GenericParameterParser(parameters);
        }
        boolean portOut = false;
        if(parser != null)
        {
            portOut = Boolean.TRUE.equals(parser.getParameter(APIGenericParameterSupport.PORT_OUT_FLAG,Boolean.class));
            ctx.put(SubscriptionQueryExecutors.SubscriptionUpdateWithStateTransitionQueryExecutor.PORTOUT_AND_INACTIVE_STATE, portOut);
        }
        final Subscriber subscriber = SubscribersApiSupport.getCrmSubscriber(ctx, subscriptionRef, this);
        if (subscriber.isInFinalState())
        {
            if(portOut)
            {
                RmiApiErrorHandlingSupport.generalException(ctx, null, "Cannot port-out a Subscription that is in-active", this);
            }
            return SuccessCodeEnum.SUCCESS.getValue();
        }
        
        final Home home = updateSubscriberPipeline(ctx,"deleteSubscription");

        try
        {
            // not deleting the subscriber record. only deactivating the subscriber.
            subscriber.setState(SubscriberStateEnum.INACTIVE);
            Subscriber resultSub = (Subscriber) home.store(ctx, subscriber);
            SubscribersApiSupport.handlePostSubscriptionUpdate(ctx, resultSub, true);
            if(portOut)
            {
                SubscribersApiSupport.handleSubscriptionPortOut(ctx, resultSub);
            }
        }
        catch (final Exception e)
        {
            final String msg = "Unable to delete Subscription with ID/MobileNumber/SubscriptionType = "
                + subscriptionRef.getIdentifier() + "/" + subscriptionRef.getMobileNumber() + "/" + subscriptionRef.getSubscriptionType();
            RmiApiErrorHandlingSupport.handleDeleteExceptions(ctx, e, msg, this);
        }
        
        return SuccessCodeEnum.SUCCESS.getValue();

    }


    /**
     * Gets all of the buckets for a given subscriber that match the search criteria.
     * 
     * @param ctx
     *            Operating Context
     * @param subscriber
     *            Subscriber for which to get buckets
     * @param bucketID
     *            Bucket ID of bucket
     * @param bundleID
     *            Bundle ID of bucket
     * @param bundleCategory
     *            Bundle category of the bucket's bundle profile
     * @return Collection of com.redknee.app.crm.bundle.SubscriberBucket beans
     * @throws HomeException
     *             On unexpected error.
     */
    private Collection<SubscriberBucket> getBuckets(Context ctx, final Subscriber subscriber, final Long bucketID,
            final Long bundleID, final Long bundleCategory, final Boolean isAscending, GenericParameter[] parameters) throws HomeException
    {
        ctx = ctx.createSubContext();
        try
        {
            final CRMSubscriberBucketProfile service = (CRMSubscriberBucketProfile) ctx
                    .get(CRMSubscriberBucketProfile.class);
            Home bucketHome = service.getBuckets(ctx, subscriber.getMSISDN(), (int) subscriber.getSubscriptionType());
            if (bundleCategory != null)
            {
                bucketHome = bucketHome.where(ctx, new Predicate()
                {

                    private static final long serialVersionUID = 1L;

                    public boolean f(Context context, Object obj) throws AbortVisitException
                    {
                        boolean result = false;
                        SubscriberBucket bucket = (SubscriberBucket) obj;
                        
                        if (!UnitTypeEnum.CURRENCY.equals(bucket.getUnitType()))
                        {
                            result = SafetyUtil.safeEquals(bundleCategory.longValue(), bucket.getRegularBal()
                                .getApplicationId());
                        }
                        else
                        {
                            try
                            {
                                BundleProfile profile = BundleSupportHelper.get(context).getBundleProfile(context, bundleID);
                                for (BundleCategoryAssociation assoc : (Collection<BundleCategoryAssociation>) profile.getBundleCategoryIds().values())
                                {
                                    if (bundleCategory.longValue() == assoc.getCategoryId())
                                    {
                                        result = true;
                                        break;
                                    }
                                }
                                
                            }
                            catch (Exception e)
                            {
                                result = false;
                            }
                        }
                        return result;
                    }
                });
            }
            ctx.put(SubscriberBucketHome.class, bucketHome);
        }
        catch (BundleManagerException bme)
        {
            new DebugLogMsg(this, bme.getMessage(), bme).log(ctx);
            throw new HomeException(bme.getMessage(), bme);
        }
        And condition = new And();
        if (bucketID != null)
        {
            condition.add(new EQ(SubscriberBucketXInfo.BUCKET_ID, bucketID.longValue()));
        }
        if (bundleID != null)
        {
            condition.add(new EQ(SubscriberBucketXInfo.BUNDLE_ID, bundleID.longValue()));
        }
        Collection<SubscriberBucket> buckets = HomeSupportHelper.get(ctx).getBeans(ctx, SubscriberBucket.class,
                condition, isAscending, SubscriberBucketXInfo.BUNDLE_ID);
        return buckets;
    }


    /**
     * {@inheritDoc}
     */
    public VoucherBatchResults updateSubscriptionRechargeVoucherBatch(final CRMRequestHeader header,
        final VoucherOrder[] orders, GenericParameter[] parameters) throws CRMExceptionFault
    {
        final Context ctx = getContext().createSubContext();
        // handle login. Failed logins throw exceptions
        RmiApiSupport.authenticateUser(ctx, header, "updateSubscriptionRechargeVoucherBatch",
            Constants.PERMISSION_SUBSCRIBERS_WRITE_UPDATESUBSCRIPTIONRECHARGEVOUCHERBATCH,
            Constants.PERMISSION_SUBSCRIBERS_WRITE_UPDATESUBSCRIBERRECHARGEVOUCHERBATCH);

        RmiApiErrorHandlingSupport.validateMandatoryObject(orders, "orders");
        
        GenericParameterParser parser = new GenericParameterParser(parameters);
        
        boolean validateOnly = parser.getParameter(VOUCHER_VALIDATE_ONLY, Boolean.class, Boolean.FALSE);
        
        Integer category_id = parser.getParameter(APIGenericParameterSupport.CATEGORY_ID, Integer.class, -1);
        Boolean isSecondaryBalance = parser.getParameter(APIGenericParameterSupport.SECONDARY_BALANCE,  Boolean.class, Boolean.FALSE);
        Boolean applyTax = parser.getParameter(APIGenericParameterSupport.APPLY_TAX, Boolean.class,	Boolean.FALSE);
        
        ctx.put(APIGenericParameterSupport.CATEGORY_ID, category_id);
        ctx.put(APIGenericParameterSupport.SECONDARY_BALANCE, isSecondaryBalance);
        ctx.put(APIGenericParameterSupport.APPLY_TAX, applyTax);

        final VoucherBatchResults results = new VoucherBatchResults();
        final VoucherOrder[] resultOrders = new VoucherOrder[orders.length];

        int success = 0;
        boolean shutdown = false;
        for (int i = 0; i < orders.length; i++)
        {
            final VoucherOrder order = orders[i];
            shutdown = shutdown || GracefulShutdownSupport.isShutdown(ctx);

            if (!shutdown)
            {
                final VoucherOrder result = updateSubscriptionRechargeVoucher(ctx, order, "order[" + i + "]", validateOnly);
                if (result.getOrderResult().getValue() == ResultCodeEnum.SUCCESS.getValue().getValue())
                {
                    success++;
                }
                resultOrders[i] = result;
            }
            else
            {
                order.setOrderResult(ResultCodeEnum.NOT_PROCESSED.getValue());
                resultOrders[i] = order;
            }
        }

        // populate batch results
        results.setNumberOfRequests(Integer.valueOf(orders.length));
        results.setNumberOfSuccesses(Integer.valueOf(success));
        results.setOrders(resultOrders);
        BatchResultType batchResult;
        if (success == orders.length)
        {
            batchResult = BatchResultTypeEnum.SUCCESS.getValue();
        }
        else if (success == 0)
        {
            batchResult = BatchResultTypeEnum.OVERALL_FAILURE.getValue();
        }
        else
        {
            batchResult = BatchResultTypeEnum.PARTIAL_SUCCESS.getValue();
        }
        results.setOverallResult(batchResult);

        return results;
    }


    @Override
    public BaseReadOnlySubscriptionExtension getSubscriptionExtension(CRMRequestHeader header,
            BaseSubscriptionExtensionReference extensionReference, GenericParameter[] parameters) throws CRMExceptionFault
    {
        final Context ctx = getContext().createSubContext();
        // handle login. Failed logins throw exceptions
        RmiApiSupport.authenticateUser(ctx, header, "getSubscriptionExtension",
                Constants.PERMISSION_SUBSCRIBERS_READ_GETSUBSCRIPTIONEXTENSION);
        return ExtensionApiSupport.getSubscriptionExtension(ctx, extensionReference);

    }


    @Override
    public BaseReadOnlySubscriptionExtension[] listDetailedSubscriptionExtensions(CRMRequestHeader header,
            SubscriptionReference subscriptionRef, Boolean isAscending, GenericParameter[] parameters) throws CRMExceptionFault
    {
        final Context ctx = getContext().createSubContext();
        // handle login. Failed logins throw exceptions
        RmiApiSupport.authenticateUser(ctx, header, "listDetailedSubscriptionExtensions",
                Constants.PERMISSION_SUBSCRIBERS_READ_LISTDETAILEDSUBSCRIPTIONEXTENSIONS);
        RmiApiErrorHandlingSupport.validateMandatoryObject(subscriptionRef, "subscriptionRef");
        final Subscriber subscriber = SubscribersApiSupport.getCrmSubscriber(ctx, subscriptionRef, this);
        return ExtensionApiSupport.getListDetailedSubscriptionExtensions(ctx, subscriber, isAscending);

    }


    @Override
    public BaseSubscriptionExtensionReference[] listSubscriptionExtensions(CRMRequestHeader header,
            SubscriptionReference subscriptionRef, Boolean isAscending, GenericParameter[] parameters) throws CRMExceptionFault
    {
        final Context ctx = getContext().createSubContext();
        // handle login. Failed logins throw exceptions
        RmiApiSupport.authenticateUser(ctx, header, "listSubscriptionExtensions",
                Constants.PERMISSION_SUBSCRIBERS_READ_LISTSUBSCRIPTIONEXTENSIONS);
        RmiApiErrorHandlingSupport.validateMandatoryObject(subscriptionRef, "subscriptionRef");
        final Subscriber subscriber = SubscribersApiSupport.getCrmSubscriber(ctx, subscriptionRef, this);
        return ExtensionApiSupport.getListSubscriptionExtensionsReference(ctx, subscriber, isAscending);
    }


    @Override
    public BaseSubscriptionExtensionReference updateSubscriptionAddExtension(CRMRequestHeader header,
            SubscriptionReference subscriptionRef, BaseSubscriptionExtension extension, GenericParameter[] parameters) throws CRMExceptionFault
    {
        final Context ctx = getContext().createSubContext();
        // handle login. Failed logins throw exceptions
        RmiApiSupport.authenticateUser(ctx, header, "updateSubscriptionAddExtension",
                Constants.PERMISSION_SUBSCRIBERS_WRITE_UPDATESUBSCRIPTIONADDEXTENSION);
        RmiApiErrorHandlingSupport.validateMandatoryObject(subscriptionRef, "subscriptionRef");
        final Subscriber subscriber = SubscribersApiSupport.getCrmSubscriber(ctx, subscriptionRef, this);
        
        return ExtensionApiSupport.addExtension(ctx, extension, subscriber, parameters);
    }


    @Override
    public SuccessCode updateSubscriptionExtension(CRMRequestHeader header,
            BaseSubscriptionExtensionReference extensionReference, BaseMutableSubscriptionExtension extension,
            GenericParameter[] parameters) throws CRMExceptionFault
    {
        final Context ctx = getContext().createSubContext();
        // handle login. Failed logins throw exceptions
        RmiApiSupport.authenticateUser(ctx, header, "updateSubscriptionExtension",
                Constants.PERMISSION_SUBSCRIBERS_WRITE_UPDATESUBSCRIPTIONEXTENSION);
        return ExtensionApiSupport.updateSubscriptionExtension(ctx, extensionReference, extension, parameters);
    }


    @Override
    public SuccessCode updateSubscriptionRemoveExtension(CRMRequestHeader header,
            BaseSubscriptionExtensionReference extensionReference, GenericParameter[] parameters) throws CRMExceptionFault
    {
        final Context ctx = getContext().createSubContext();
        // handle login. Failed logins throw exceptions
        RmiApiSupport.authenticateUser(ctx, header, "updateSubscriptionRemoveExtension",
                Constants.PERMISSION_SUBSCRIBERS_WRITE_UPDATESUBSCRIPTIONREMOVEEXTENSION);
        RmiApiErrorHandlingSupport.validateMandatoryObject(extensionReference, "extensionReference");
        final Subscriber subscriber = SubscribersApiSupport.getCrmSubscriber(ctx, extensionReference
                .getSubscriptionRef(), this);
        return ExtensionApiSupport.removeExtension(ctx, extensionReference, subscriber, parameters);
    }


    /**
     * {@inheritDoc}
     */
    public SuccessCode resetVoicemailPassword(CRMRequestHeader header, SubscriptionReference subscriptionRef, GenericParameter[] parameters)
            throws CRMExceptionFault
    {
        final Context ctx = getContext().createSubContext();
        // handle login. Failed logins throw exceptions
        RmiApiSupport.authenticateUser(ctx, header, "resetVoicemailPassword",
            Constants.PERMISSION_SUBSCRIBERS_WRITE_RESETVOICEMAILPASSWORD);

        DefaultExceptionListener el = new DefaultExceptionListener();
        VoiceMailPINResetServicer.resetVoicemailPIN(ctx, subscriptionRef.getIdentifier(), el);

        if (el.hasErrors())
        {
            final Exception exeption = (Exception) el.getExceptions().get(0);
            RmiApiErrorHandlingSupport.handleQueryExceptions(ctx, exeption, exeption.getMessage(), this);
        }

        return SuccessCodeEnum.SUCCESS.getValue();
    }
    
    
    /**
     * {@inheritDoc}
     */
    public PPSMSupporteeSubscriptionExtensionReference[] listPPSMSupportees(CRMRequestHeader header,
            SubscriptionReference supporterRef, GenericParameter[] parameters) throws CRMExceptionFault
    {
        final Context ctx = getContext().createSubContext();
        // handle login. Failed logins throw exceptions
        RmiApiSupport.authenticateUser(ctx, header, "listPPSMSupportees",
            Constants.PERMISSION_SUBSCRIBERS_READ_LISTPPSMSUPPORTEES);
        return ExtensionApiSupport.getListPPSMSupportees(ctx, supporterRef);
    }


    /**
     * {@inheritDoc}
     */
    public SuccessCode updateSubscriptionsPPSMSupporter(CRMRequestHeader header,
            SubscriptionReference currentSupporterRef, SubscriptionReference newSupporterRef, GenericParameter[] parameters) throws CRMExceptionFault
    {
        final Context ctx = getContext().createSubContext();
        // handle login. Failed logins throw exceptions
        RmiApiSupport.authenticateUser(ctx, header, "updateSubscriptionsPPSMSupporter",
                Constants.PERMISSION_SUBSCRIBERS_WRITE_UPDATESUBSCRIPTIONSPPSMSUPPORTER);
        return ExtensionApiSupport.updateSubscriptionsPPSMSupporter(ctx, currentSupporterRef, newSupporterRef);

        
    }


    /*
     * (non-Javadoc)
     * 
     * @see com.redknee.util.crmapi.wsdl.v3_0.api.SubscriptionServiceSkeletonInterface#
     * listSubscriptionContracts(com.redknee.util.crmapi.wsdl.v2_0.types.CRMRequestHeader,
     * int, java.lang.Boolean)
     */
    @Override
    public SubscriptionContractReference[] listSubscriptionContracts(CRMRequestHeader header, int spid,
            Boolean isAscending, GenericParameter[] parameters) throws CRMExceptionFault
    {
        final Context ctx = getContext().createSubContext();
        // handle login. Failed logins throw exceptions
        RmiApiSupport.authenticateUser(ctx, header, "listSubscriptionContracts",
                Constants.PERMISSION_SUBSCRIBERS_READ_LISTSUBSCRIPTIONSCONTRACTS);
        RmiApiErrorHandlingSupport.validateMandatoryObject(spid,"spid");

        try
        {
            final Object condition = new EQ(SubscriptionContractTermXInfo.SPID, spid);
            final Collection<SubscriptionContractTerm> collection = HomeSupportHelper.get(ctx).getBeans(ctx,
                    SubscriptionContractTerm.class, condition, RmiApiSupport.isSortAscending(isAscending));
            com.redknee.util.crmapi.wsdl.v3_0.types.subscription.SubscriptionContractReference[] subscriptionContracts = new com.redknee.util.crmapi.wsdl.v3_0.types.subscription.SubscriptionContractReference[collection
                    .size()];
            int i = 0;
            for (SubscriptionContractTerm term : collection)
            {
                subscriptionContracts[i] = SubscriptionContractTermToApiAdapter.toAPIReference(ctx, term);
                i++;
            }
            return subscriptionContracts;
        }
        catch (Exception ex)
        {
            final String msg = "Unable to load list of Contracts for spid " + spid;
            RmiApiErrorHandlingSupport.handleStoreExceptions(ctx, ex, msg, this);
        }
        return null;
    }


    /*
     * (non-Javadoc)
     * 
     * @see com.redknee.util.crmapi.wsdl.v3_0.api.SubscriptionServiceSkeletonInterface#
     * listDetailedSubscriptionContracts
     * (com.redknee.util.crmapi.wsdl.v2_0.types.CRMRequestHeader, int, java.lang.Boolean)
     */
    @Override
    public com.redknee.util.crmapi.wsdl.v3_0.types.subscription.SubscriptionContract[] listDetailedSubscriptionContracts(
            CRMRequestHeader header, int spid, Boolean isAscending, GenericParameter[] parameters) throws CRMExceptionFault
    {
        final Context ctx = getContext().createSubContext();
        RmiApiSupport.authenticateUser(ctx, header, "listDetailedSubscriptionContracts",
                Constants.PERMISSION_SUBSCRIBERS_READ_LISTDETAILSUBSCRIPTIONSCONTRACTS);
        RmiApiErrorHandlingSupport.validateMandatoryObject(spid,"spid");

        try
        {
            final Object condition = new EQ(SubscriptionContractTermXInfo.SPID, spid);
            final Collection<SubscriptionContractTerm> collection = HomeSupportHelper.get(ctx).getBeans(ctx,
                    SubscriptionContractTerm.class, condition, RmiApiSupport.isSortAscending(isAscending));
            com.redknee.util.crmapi.wsdl.v3_0.types.subscription.SubscriptionContract[] subscriptionContracts = new com.redknee.util.crmapi.wsdl.v3_0.types.subscription.SubscriptionContract[]
                {};
            subscriptionContracts = CollectionSupportHelper.get(ctx).adaptCollection(ctx, collection,
                    this.subscriptionContractTermToApiAdapter_, subscriptionContracts);
            return subscriptionContracts;
        }
        catch (Exception ex)
        {
            final String msg = "Unable to load list of Contracts for spid " + spid;
            RmiApiErrorHandlingSupport.handleStoreExceptions(ctx, ex, msg, this);
        }
        return null;
    }


    /**
     * {@inheritDoc}
     */
    public BundleAdjustmentResponse createBundleAdjustments(CRMRequestHeader header,
            SubscriptionReference subscriptionRef, BundleAdjustment[] adjustments, GenericParameter[] parameters)
            throws CRMExceptionFault
    {
        final Context ctx = getContext().createSubContext();
        // handle login. Failed logins throw exceptions
        RmiApiSupport.authenticateUser(ctx, header, "createBundleAdjustments",
                Constants.PERMISSION_SUBSCRIBERS_WRITE_CREATEBUNDLEADJUSTMENTS);
        final Subscriber subscriber = SubscribersApiSupport.getCrmSubscriber(ctx, subscriptionRef, this);
        
        BundleAdjustmentResponse response;
        try
        {
            response = XBeans.instantiate(BundleAdjustmentResponse.class, ctx);
        }
        catch (Throwable t)
        {
            response = new BundleAdjustmentResponse();
        }
        
        List<BundleAdjustmentResult> results = new ArrayList<BundleAdjustmentResult>();
        
        for (BundleAdjustment adjustment : adjustments)
        {
            Context subCtx = ctx.createSubContext();
            subCtx.put(Subscriber.class, subscriber);
            results.add(createBundleAdjustment(subCtx, adjustment));
        }
        
        response.setResults(results.toArray(new BundleAdjustmentResult[results.size()]));
        
        return response;
    }

    
    /**
     * Creates a single bundle adjustment.
     * @param ctx
     * @param adjustment
     * @return
     */
    private BundleAdjustmentResult createBundleAdjustment(final Context ctx, final BundleAdjustment adjustment)
    {
        BundleAdjustmentResult result;
        try
        {
            result = XBeans.instantiate(BundleAdjustmentResult.class, ctx);
        }
        catch (Throwable t)
        {
            result = new BundleAdjustmentResult();
        }
        
        result.setAdjustment(adjustment);
        try
        {
            com.redknee.app.crm.bundle.BundleAdjustment crmAdjustment = (com.redknee.app.crm.bundle.BundleAdjustment) bundleAdjustmentToApiAdapter_.unAdapt(ctx, adjustment);
                    
            ctx.put(com.redknee.app.crm.bundle.BundleAdjustment.class, crmAdjustment);
            
            SubscribersApiSupport.fillInInputGenericParametersInContext(ctx, adjustment);
            
            ctx.put(BundleManagerPipelineConstants.BM_RESULT_CODE, null);
            ContextAgent adjContext = (ContextAgent) ctx.get(BundleAdjustmentAgent.class);
            adjContext.execute(ctx);

            SubscribersApiSupport.verifyBundleCallExecution(ctx,result);
            SubscribersApiSupport.fillInOutputGenericParametersFromContext(ctx,adjustment);
            
        }
        catch (Throwable t)
        {
            LogSupport.minor(ctx, this, "Unable to apply bundle adjustment: " + t.getMessage(), t);
            final CRMException newException = new CRMException();
            newException.setCode(ExceptionCode.GENERAL_EXCEPTION);
            newException.setMessage("Unable to apply bundle adjustment: " + t.getMessage());
            result.setException(newException);
        }    
    
        return result;
    }
    
	/*
     * (non-Javadoc)
     * 
     * @see com.redknee.util.crmapi.wsdl.v3_0.api.SubscriptionServiceSkeletonInterface#
     * updateSubscriptionContractDetails
     * (com.redknee.util.crmapi.wsdl.v2_0.types.CRMRequestHeader,
     * com.redknee.util.crmapi.wsdl.v2_1.types.subscription.SubscriptionReference, long,
     * java.util.Calendar,
     * com.redknee.util.crmapi.wsdl.v3_0.types.subscription.SubscriptionPricePlan,
     * com.redknee.util.crmapi.wsdl.v2_0.types.GenericParameter[])
     */
    @Override
    public SubscriptionContractUpdateResults updateSubscriptionContractDetails(CRMRequestHeader header,
            SubscriptionReference subscriptionRef, long contractID, Calendar contractStartDate,
            SubscriptionPricePlan options, GenericParameter[] parameters) throws CRMExceptionFault
    {
        final Context ctx = getContext().createSubContext();
        RmiApiSupport.authenticateUser(ctx, header, "updateSubscriptionContractDetails",
                Constants.PERMISSION_SUBSCRIBERS_READ_UPDATESUBSCRIPTIONSCONTRACTDETAILS);
        final Subscriber subscriber = SubscribersApiSupport.getCrmSubscriber(ctx, subscriptionRef, this);
        RmiApiErrorHandlingSupport.validateMandatoryObject(contractID, "contractID");
        SubscriptionContractUpdateResults updateResult = new SubscriptionContractUpdateResults();
        try
        {
            
            SubscriptionContractTerm term = HomeSupportHelper.get(ctx).findBean(ctx,
                    SubscriptionContractTerm.class, new EQ(SubscriptionContractTermXInfo.ID, contractID));
                    
            if (contractID != SubscriptionContractSupport.SUBSCRIPTION_CONTRACT_NOT_INTIALIZED && term == null)
            {
                final String msg = "Cannot find contract " + contractID + " or priceplan is empty";
                RmiApiErrorHandlingSupport.handleStoreExceptions(ctx, null, msg, this);
            }
            if ( contractID == subscriber.getSubscriptionContract(ctx))
            {
                final String msg = "Subscriber already has this contract applied on " + subscriber.getSubscriptionContractStartDate();
                RmiApiErrorHandlingSupport.handleStoreExceptions(ctx, null, msg, this);                
            }
            if ( contractID == SubscriptionContractSupport.SUBSCRIPTION_CONTRACT_NOT_INTIALIZED )
            {
                subscriber.setSubscriptionContract(SubscriptionContractSupport.SUBSCRIPITON_CONTRACT_EMPTY_CONTRACT);                    
            }
            else
            {
                subscriber.setSubscriptionContract(contractID);                    
            }
            //SET in context required params in home
            GenericParameterParser parser = new GenericParameterParser(parameters);
            boolean mbgParam = parser.getParameter(APIGenericParameterSupport.NON_STANDARD_RENEWAL_PARAM, Boolean.class) == null
            		?false: parser.getParameter(APIGenericParameterSupport.NON_STANDARD_RENEWAL_PARAM, Boolean.class);
            ctx.put(Constants.NON_STANDARD_RENEWAL, mbgParam);
            
            //write Generic parameter fetching logic here viz - SubsidyAmount(Long), PenaltyFeePerMonth(Long) and DeviceProductId(Long)
            Long subsidyAmount = parser.getParameter(APIGenericParameterSupport.SUBSIDY_AMOUNT_PARAM, Long.class);
            ctx.put(APIGenericParameterSupport.SUBSIDY_AMOUNT_PARAM, subsidyAmount);

            Long penaltyFeePerMonth = parser.getParameter(APIGenericParameterSupport.PENALTY_FEE_PER_MONTH_PARAM, Long.class);
            ctx.put(APIGenericParameterSupport.PENALTY_FEE_PER_MONTH_PARAM, penaltyFeePerMonth);

            Long deviceProductId = parser.getParameter(APIGenericParameterSupport.DEVICE_PRODUCTID_PARAM, Long.class);
            ctx.put(APIGenericParameterSupport.DEVICE_PRODUCTID_PARAM, deviceProductId);
            

            final CRMSpid sp = RmiApiSupport.getCrmServiceProvider(ctx, Integer.valueOf(subscriber.getSpid()), this);
            
            if (!sp.getUseContractPricePlan() && (options == null || (!options.getIsSelected())))
            {
            	//Not using contract priceplan and priceplan not sent in request. Update only contract
                Home subHome = updateSubscriberPipeline(ctx,"updateSubscriptionContractDetails");
                final Subscriber resultSub = (Subscriber) subHome.store(ctx, subscriber);
                updateResult.setEndDate(CalendarSupportHelper.get(ctx).dateToCalendar(
                        resultSub.getSubscriptionContractEndDate()));
                updateResult.setStartDate(CalendarSupportHelper.get(ctx).dateToCalendar(
                        resultSub.getSubscriptionContractStartDate()));
                SubscribersApiSupport.handlePostSubscriptionUpdate(ctx, resultSub, true);
            }
            else
            {
            	//Either use contract priceplan or the received priceplan
                if ((options != null && options.getIsSelected()) && options.getPricePlanDetails() == null)
                {
                    final String msg = "Priceplan cannot be null";
                    RmiApiErrorHandlingSupport.handleStoreExceptions(ctx, null, msg, this);
                }
                
                if ( term != null )
                {
                    PricePlanOptionUpdateResult[] ppResults = new PricePlanOptionUpdateResult[]{};
                    if (sp.getUseContractPricePlan() && (options == null || (!options.getIsSelected())))
                    {
                    	PricePlan pricePlan = new PricePlan();
                    	pricePlan.setIdentifier(term.getContractPricePlan());
                    	options = new SubscriptionPricePlan();
                    	options.setIsSelected(true);
                    	options.setPricePlanDetails(pricePlan);//com.redknee.util.crmapi.wsdl.v3_0.types.serviceandbundle
                    	subscriber.setPricePlan(term.getContractPricePlan());//for a validation in home chain
                    }
                    updateResult.setOldPricePlanID(subscriber.getPricePlan());
                    ppResults = updateSubscriptionRating(ctx, subscriber, options, false,"updateSubscriptionContractDetails");
                    updateResult.setNewPricePlanID(subscriber.getPricePlan());
                    updateResult.setEndDate(CalendarSupportHelper.get(ctx).dateToCalendar(
                            subscriber.getSubscriptionContractEndDate()));
                    updateResult.setStartDate(CalendarSupportHelper.get(ctx).dateToCalendar(
                            subscriber.getSubscriptionContractStartDate()));
                }
                else
                {
                    final String msg = "ContractId " + contractID + " term does not exist";
                    RmiApiErrorHandlingSupport.handleStoreExceptions(ctx, null, msg, this);
                }
            }
        }
        catch (CRMExceptionFault e)
        {
            throw e;
        }
        catch (final Exception e)
        {
            final String msg = "Unable to update contract for Subscription with ID/MobileNumber/SubscriptionType = "
                    + subscriptionRef.getIdentifier() + "/" + subscriptionRef.getMobileNumber() + "/"
                    + subscriptionRef.getSubscriptionType();
            RmiApiErrorHandlingSupport.handleStoreExceptions(ctx, e, msg, this);
        }
        return updateResult;
    }

    @Override
    public ExecuteResult executeSubscriptionBalanceQuery(CRMRequestHeader header,
            SubscriptionReference subscriptionRef, String[] balanceTypes, GenericParameter[] parameters)
            throws CRMExceptionFault
    {
        final Context ctx = getContext().createSubContext();

        QueryExecutorFactory executor = QueryExecutorFactory.getInstance();
        
        return executor.execute(ctx, SubscriptionServiceSkeletonInterface.class.getSimpleName(), "executeSubscriptionBalanceQuery", ExecuteResult.class, 
                header, subscriptionRef, balanceTypes, parameters);
    }



    @Override
    public SubscriptionStateTransitionResult updateSubscriptionWithStateTransition(CRMRequestHeader header,
            SubscriptionReference subscriptionRef, SubscriptionState[] currentState, SubscriptionState newState,
            GenericParameter[] parameters) throws CRMExceptionFault
    {
        final Context ctx = getContext().createSubContext();

        QueryExecutorFactory executor = QueryExecutorFactory.getInstance();
        //SET in context required params in home
        ctx.put(CRMRequestHeader.class, header);
        GenericParameterParser parser = new GenericParameterParser(parameters);
        boolean mbgParam = parser.getParameter(APIGenericParameterSupport.NON_STANDARD_RENEWAL_PARAM, Boolean.class) == null
        		?false: parser.getParameter(APIGenericParameterSupport.NON_STANDARD_RENEWAL_PARAM, Boolean.class);
        ctx.put(Constants.NON_STANDARD_RENEWAL, mbgParam);
        

        return executor.execute(ctx, SubscriptionServiceSkeletonInterface.class.getSimpleName(), "updateSubscriptionWithStateTransition", SubscriptionStateTransitionResult.class, 
                header, subscriptionRef, currentState, newState, parameters);
    }
    
    @Override
    public BucketHistoryCreateResult createSubscriptionBucketHistory(CRMRequestHeader header, 
            SubscriptionReference subscriptionRef, CreateBucketHistoryRequest createBucketHistoryRequest, GenericParameter[] parameters) throws CRMExceptionFault
    {
        final Context ctx = getContext().createSubContext();

        QueryExecutorFactory executor = QueryExecutorFactory.getInstance();

        return executor.execute(ctx, SubscriptionServiceSkeletonInterface.class.getSimpleName(), "createSubscriptionBucketHistory", BucketHistoryCreateResult.class, 
                header, subscriptionRef, createBucketHistoryRequest, parameters);
    } 
    
    public com.redknee.util.crmapi.wsdl.v3_0.types.subscription.BucketHistoryQueryResult listSubscriptionBucketHistory(CRMRequestHeader header,
    		com.redknee.util.crmapi.wsdl.v3_0.types.subscription.SubscriptionReference subscriptionRef,
    		java.util.Calendar startTime,java.util.Calendar endTime,java.lang.Long category,java.lang.String pageKey, int limit,
    		java.lang.Boolean isAscending,com.redknee.util.crmapi.wsdl.v2_0.types.GenericParameter[] parameters)
    	throws CRMExceptionFault {
    	
        final Context ctx = getContext().createSubContext();

        QueryExecutorFactory executor = QueryExecutorFactory.getInstance();

        return executor.execute(ctx, SubscriptionServiceSkeletonInterface.class.getSimpleName(), "listSubscriptionBucketHistory", BucketHistoryQueryResult.class, 
                header, subscriptionRef, startTime, endTime, category, pageKey, limit, isAscending, parameters);
    }

    public com.redknee.util.crmapi.wsdl.v3_0.types.subscription.DetailedBucketHistoryQueryResult listDetailedSubscriptionBucketHistory(CRMRequestHeader header,
    		com.redknee.util.crmapi.wsdl.v3_0.types.subscription.SubscriptionReference subscriptionRef,
    		java.util.Calendar startTime,java.util.Calendar endTime,java.lang.Long category,java.lang.String pageKey, int limit,
    		java.lang.Boolean isAscending,com.redknee.util.crmapi.wsdl.v2_0.types.GenericParameter[] parameters)
    	throws CRMExceptionFault {
    	
        final Context ctx = getContext().createSubContext();

        QueryExecutorFactory executor = QueryExecutorFactory.getInstance();

        return executor.execute(ctx, SubscriptionServiceSkeletonInterface.class.getSimpleName(), "listDetailedSubscriptionBucketHistory", DetailedBucketHistoryQueryResult.class, 
                header, subscriptionRef, startTime, endTime, category, pageKey, limit, isAscending, parameters);
    }
    
    public com.redknee.util.crmapi.wsdl.v3_0.types.subscription.DetailedBucketHistoryQueryResult getBucketHistory(CRMRequestHeader header,
    		long bucketHistoryID,
    		com.redknee.util.crmapi.wsdl.v2_0.types.GenericParameter[] parameters)
    	throws CRMExceptionFault {
    	
        final Context ctx = getContext().createSubContext();

        QueryExecutorFactory executor = QueryExecutorFactory.getInstance();

        return executor.execute(ctx, SubscriptionServiceSkeletonInterface.class.getSimpleName(), "getBucketHistory", DetailedBucketHistoryQueryResult.class, 
                header, bucketHistoryID, parameters);
    }     

    /**
     * {@inheritDoc}
     */
    public SubscriptionBundleBalanceSummary getSubscriptionBundleBalancesWithSummary(CRMRequestHeader header,
            SubscriptionReference subscriptionRef, Long bucketID, Long bundleID, Long bundleCategory, GenericParameter[] parameters)
            throws CRMExceptionFault
    {
                final Context ctx = getContext().createSubContext();

                QueryExecutorFactory executor = QueryExecutorFactory.getInstance();

                return executor.execute(ctx, SubscriptionServiceSkeletonInterface.class.getSimpleName(), "getSubscriptionBundleBalancesWithSummary", SubscriptionBundleBalanceSummary.class, 
                        header, subscriptionRef, bucketID, bundleID, bundleCategory, parameters);
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see com.redknee.util.crmapi.wsdl.v3_0.api.SubscriptionServiceSkeletonInterface#
     * getSubscriptionContract(com.redknee.util.crmapi.wsdl.v2_0.types.CRMRequestHeader,
     * long)
     */
    @Override
    public SubscriptionContract getSubscriptionContract(CRMRequestHeader header, long contractID, GenericParameter[] parameters)
            throws CRMExceptionFault
    {
        final Context ctx = getContext().createSubContext();
        RmiApiSupport.authenticateUser(ctx, header, "getSubscriptionContract",
                Constants.PERMISSION_SUBSCRIBERS_READ_GETSUBSCRIPTIONSCONTRACT);
        RmiApiErrorHandlingSupport.validateMandatoryObject(contractID,"contractID");

        com.redknee.app.crm.contract.SubscriptionContractTerm contract = null;
        try
        {
            contract = HomeSupportHelper.get(ctx).findBean(ctx,
                    com.redknee.app.crm.contract.SubscriptionContractTerm.class,
                    new EQ(SubscriptionContractTermXInfo.ID, contractID));

        }
        catch (Exception ex)
        {
            final String msg = "Unable to find contract Id " + contractID;
            RmiApiErrorHandlingSupport.handleStoreExceptions(ctx, ex, msg, this);
        }
        if (contract == null)
        {
            final String msg = "Subscription Contract [" + contractID + "] cannot be found!";
            RmiApiErrorHandlingSupport.identificationException(ctx, msg, this);
        }
        return SubscriptionContractTermToApiAdapter.adaptToApi(ctx, contract);
    }


    @Override
    public SubscriptionUpdateFees getSubscriptionUpdateFees(CRMRequestHeader header,
            SubscriptionUpdateCriteria[] criteria, GenericParameter[] parameters) throws CRMExceptionFault
    {
        final Context ctx = getContext().createSubContext();

        QueryExecutorFactory executor = QueryExecutorFactory.getInstance();

        return executor.execute(ctx, SubscriptionServiceSkeletonInterface.class.getSimpleName(), "getSubscriptionUpdateFees", SubscriptionUpdateFees.class, 
                header, criteria, parameters);
    }
    
    public SubscriptionContractStatusQueryResult getSubscriptionContractStatus(CRMRequestHeader header,
            SubscriptionReference subscriptionRef, GenericParameter[] parameters) throws CRMExceptionFault
    {

            final Context ctx = getContext().createSubContext();
            RmiApiSupport.authenticateUser(ctx, header, "getSubscriptionContractStatus",
            Constants.PERMISSION_SUBSCRIBERS_READ_GETSUBSCRIPTIONCONTRACTSTATUS);
            
            RmiApiErrorHandlingSupport.validateMandatoryObject(subscriptionRef, "subscriptionRef");
            
            QueryExecutorFactory executor = QueryExecutorFactory.getInstance();
            return executor.execute(ctx, SubscriptionServiceSkeletonInterface.class.getSimpleName(), "getSubscriptionContractStatus", SubscriptionContractStatusQueryResult.class, 
                    header, subscriptionRef, parameters);
    }
    
	@Override
	public Long getSubscriptionDeviceType(CRMRequestHeader header,
			SubscriptionReference subscriptionRef) throws CRMExceptionFault 
	{
        final Context ctx = getContext().createSubContext();
        RmiApiSupport.authenticateUser(ctx, header, "getSubscriptionDeviceType",
                Constants.PERMISSION_SUBSCRIBERS_READ_GETSUBSCRIPTIONDEVICETYPE);
        
        final Subscriber subscriber = SubscribersApiSupport.getCrmSubscriber(ctx, subscriptionRef, this);

        return subscriber.getDeviceTypeId();
	}
	
/*	Commenting this method as it was creating conflict with method being ported from 9.5.4 for PTUB feature.This method was added as fix for TT#13070903034 which was ideally not performing any action.
 *  @Override
    public SubscriptionSecondaryBalanceQueryResult getSubscriptionSecondaryBalance(CRMRequestHeader header65,
            SubscriptionReference subscriptionRef66, Integer[] categoryId, GenericParameter[] parameters67)
            throws CRMExceptionFault
    {
        final Context ctx = getContext().createSubContext();
        SubscriptionSecondaryBalanceQueryResult result = new SubscriptionSecondaryBalanceQueryResult();
        RmiApiErrorHandlingSupport.generalException(ctx, new UnsupportedOperationException("Unsupported Operation"), "Unsupported Operation", this);
        return result;
    }
    */
	 
    private Home updateSubscriberPipeline(final Context ctx, final String caller) throws CRMExceptionFault
    {
        return ApiSupport.injectAPIUpdateEntityHomeIntoPipeline(ctx, getSubscriberHome(ctx), caller);
    }
    /**
     * Returns the subscriber home.
     *
     * @param ctx
     *            The operating context.
     * @return The subscriber home to be used by this object.
     * @throws CRMExceptionFault
     *             Thrown if the home does not exist.
     */
    public static Home getSubscriberHome(final Context ctx) throws CRMExceptionFault
    {
        return RmiApiSupport.getCrmHome(ctx, SubscriberHome.class, SubscribersImpl.class);
    }


    
    /**
     * {@inheritDoc}
     */
    public Context getContext()
    {
        return this.context_;
    }


    /**
     * {@inheritDoc}
     */
    public void setContext(final Context ctx)
    {
        this.context_ = ctx;
    }

    /**
     * Default context.
     */
    private transient Context context_;

    /**
     * CRM subscription to API subscription adapter.
     */
    private final SubscriberToApiAdapter subscriptionToApiAdapter_;

    private final SubscriptionClassToApiAdapter subscriptionClassToApiAdapter_;
    
    private final SubscriptionLevelToApiAdapter subscriptionLevelToApiAdapter_;
    
    private final SubscriptionTypeToApiAdapter subscriptionTypeToApiAdapter_;
    
    private final SubscriptionContractTermToApiAdapter subscriptionContractTermToApiAdapter_;
    /**
     * SCT-to-SubscriberCreationTemplateReference adapter.
     */
    private final SubscriberCreationTemplateToApiAdapter subscriberTemplateToApiAdapter_;

    /**
     * SubscriberBucket-to-ReadOnlySubscriberBundle adapter.
     */
    private final SubscriberBucketToApiAdapter subBucketToApiAdapter_;

    private final BundleAdjustmentToApiAdapter bundleAdjustmentToApiAdapter_;

    public static final String MONTHLY_SPEND_LIMIT = "MonthlySpendLimit";

    public static final String OVERDRAFT_BALANCE_LIMIT = "OverdraftBalanceLimit";
    
    public static final String PRICE_PLAN_MONTHLY_VALUE = "PricePlanMonthlyValue";

    public static final String FAIL_CARD_PACKAGE_ALREADY_ASSIGNED = "FailCardPackageAlreadyAssigned";
    
    public static final String VOUCHER_VALIDATE_ONLY = "ValidateOnly";
    
    
    
    /**
     * 
     */
	public static final String START_DATE = "StartDate";

    /**
     * 
     */
    public static final String END_DATE = "EndDate";
    public static final String ACTIVATION_REASON_CODE = "ActivationReasonCode";
    
  
	@Override
	public SubscriptionSecondaryBalanceQueryResult getSubscriptionSecondaryBalance(CRMRequestHeader header,SubscriptionReference subscriptionRef,Integer[] categoryId,
			GenericParameter[] params) throws CRMExceptionFault
	{
        final Context ctx = getContext().createSubContext();
        RmiApiSupport.authenticateUser(ctx, header, "getSubscriptionSecondaryBalance",
        Constants.PERMISSION_SUBSCRIBERS_READ_GETSUBSCRIPTIONSECONDARYBALANCE);
        
        RmiApiErrorHandlingSupport.validateMandatoryObject(subscriptionRef, "subscriptionRef");
        
        QueryExecutorFactory executor = QueryExecutorFactory.getInstance();
        return executor.execute(ctx, SubscriptionServiceSkeletonInterface.class.getSimpleName(), "getSubscriptionSecondaryBalance", SubscriptionSecondaryBalanceQueryResult.class, 
                header, subscriptionRef, categoryId,params);
	}
	
	
	public MergedBalanceHistoryResult getDetailedBalanceHistory(CRMRequestHeader header,	SubscriptionReference subscriptionRef,	Calendar startTime,	Calendar endTime,	Calendar pageKey, Integer limit,	Boolean isAscending,
			GenericParameter[] parameters) throws CRMExceptionFault
	{
        final Context ctx = getContext().createSubContext();
       /* RmiApiSupport.authenticateUser(ctx, header, "getSubscriptionSecondaryBalance",
        Constants.PERMISSION_SUBSCRIBERS_READ_GETSUBSCRIPTIONSECONDARYBALANCE);
        
        RmiApiErrorHandlingSupport.validateMandatoryObject(subscriptionRef, "subscriptionRef");*/
        
        QueryExecutorFactory executor = QueryExecutorFactory.getInstance();
        return executor.execute(ctx, SubscriptionServiceSkeletonInterface.class.getSimpleName(), "getDetailedBalanceHistory", MergedBalanceHistoryResult.class, 
                header, subscriptionRef, startTime,endTime,pageKey,limit,isAscending, parameters);
	}

//    parser.getParameter(APIGenericParameterSupport.DEVICE_TYPE, Long.class);

}
