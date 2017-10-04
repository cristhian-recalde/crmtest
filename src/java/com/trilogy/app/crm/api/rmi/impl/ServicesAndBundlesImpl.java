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
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import com.trilogy.app.crm.ModelCrmConstants;
import com.trilogy.app.crm.api.ApiSupport;
import com.trilogy.app.crm.api.Constants;
import com.trilogy.app.crm.api.rmi.AuxiliaryServiceToApiAdapter;
import com.trilogy.app.crm.api.rmi.AuxiliaryServiceToApiReferenceAdapter;
import com.trilogy.app.crm.api.rmi.BundleCategoryToApiAdapter;
import com.trilogy.app.crm.api.rmi.BundleCategoryToApiReferenceAdapter;
import com.trilogy.app.crm.api.rmi.BundleFeeToApiAdapter;
import com.trilogy.app.crm.api.rmi.BundleProfileToApiAdapter;
import com.trilogy.app.crm.api.rmi.BundleProfileToApiFeeAdapter;
import com.trilogy.app.crm.api.rmi.BundleProfileToApiReferenceAdapter;
import com.trilogy.app.crm.api.rmi.DependencyGroupToApiAdapter;
import com.trilogy.app.crm.api.rmi.GenericParameterParser;
import com.trilogy.app.crm.api.rmi.PrerequisiteGroupToApiAdapter;
import com.trilogy.app.crm.api.rmi.PricePlanGroupToApiAdapter;
import com.trilogy.app.crm.api.rmi.PricePlanToApiAdapter;
import com.trilogy.app.crm.api.rmi.PricePlanToApiReferenceAdapter;
import com.trilogy.app.crm.api.rmi.PricePlanVersionToApiAdapter;
import com.trilogy.app.crm.api.rmi.RatePlanAssociationToApiAdapter;
import com.trilogy.app.crm.api.rmi.RatePlanAssociationToApiReferenceAdapter;
import com.trilogy.app.crm.api.rmi.ServiceFeeToApiAdapter;
import com.trilogy.app.crm.api.rmi.ServiceToApiAdapter;
import com.trilogy.app.crm.api.rmi.ServiceToApiReferenceAdapter;
import com.trilogy.app.crm.api.rmi.support.RmiApiErrorHandlingSupport;
import com.trilogy.app.crm.api.rmi.support.RmiApiSupport;
import com.trilogy.app.crm.bean.AuxiliaryServiceTypeEnum;
import com.trilogy.app.crm.bean.AuxiliaryServiceXInfo;
import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.CRMSpidXInfo;
import com.trilogy.app.crm.bean.ChargeFailureActionEnum;
import com.trilogy.app.crm.bean.CrmVmPlan;
import com.trilogy.app.crm.bean.CrmVmPlanXInfo;
import com.trilogy.app.crm.bean.DependencyGroup;
import com.trilogy.app.crm.bean.DependencyGroupHome;
import com.trilogy.app.crm.bean.DependencyGroupXInfo;
import com.trilogy.app.crm.bean.ExpiryExtensionModeEnum;
import com.trilogy.app.crm.bean.GLCodeMapping;
import com.trilogy.app.crm.bean.GLCodeMappingXInfo;
import com.trilogy.app.crm.bean.OneTimeTypeEnum;
import com.trilogy.app.crm.bean.PrerequisiteGroup;
import com.trilogy.app.crm.bean.PrerequisiteGroupHome;
import com.trilogy.app.crm.bean.PrerequisiteGroupXInfo;
import com.trilogy.app.crm.bean.PricePlanGroup;
import com.trilogy.app.crm.bean.PricePlanGroupHome;
import com.trilogy.app.crm.bean.PricePlanGroupXInfo;
import com.trilogy.app.crm.bean.PricePlanHome;
import com.trilogy.app.crm.bean.PricePlanVersion;
import com.trilogy.app.crm.bean.PricePlanVersionHome;
import com.trilogy.app.crm.bean.PricePlanVersionXInfo;
import com.trilogy.app.crm.bean.PricePlanXInfo;
import com.trilogy.app.crm.bean.ServicePackageFee;
import com.trilogy.app.crm.bean.ServicePackageVersion;
import com.trilogy.app.crm.bean.ServicePeriodEnum;
import com.trilogy.app.crm.bean.ServiceTypeEnum;
import com.trilogy.app.crm.bean.ServiceXInfo;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.bean.TaxAuthority;
import com.trilogy.app.crm.bean.TaxAuthorityXInfo;
import com.trilogy.app.crm.bean.VMPlan;
import com.trilogy.app.crm.bean.VMPlanXInfo;
import com.trilogy.app.crm.bean.account.SubscriptionType;
import com.trilogy.app.crm.bean.account.SubscriptionTypeXInfo;
import com.trilogy.app.crm.bean.core.BundleFee;
import com.trilogy.app.crm.bean.core.ServiceFee2;
import com.trilogy.app.crm.bean.payment.ContractFeeFrequencyEnum;
import com.trilogy.app.crm.bean.priceplan.RatePlan;
import com.trilogy.app.crm.bean.priceplan.SubscriptionLevel;
import com.trilogy.app.crm.bean.priceplan.SubscriptionLevelXInfo;
import com.trilogy.app.crm.bundle.ActivationTypeEnum;
import com.trilogy.app.crm.bundle.BundleCategoryXInfo;
import com.trilogy.app.crm.bundle.BundleProfile;
import com.trilogy.app.crm.bundle.BundleProfileHome;
import com.trilogy.app.crm.bundle.BundleProfileXInfo;
import com.trilogy.app.crm.bundle.license.BMLicenseConstants;
import com.trilogy.app.crm.bundle.rateplan.RatePlanAssociation;
import com.trilogy.app.crm.bundle.rateplan.RatePlanAssociationHome;
import com.trilogy.app.crm.bundle.rateplan.RatePlanAssociationXInfo;
import com.trilogy.app.crm.bundle.service.CRMBundleProfile;
import com.trilogy.app.crm.client.urcs.PromotionManagementClientV2;
import com.trilogy.app.crm.client.urcs.UrcsClientInstall;
import com.trilogy.app.crm.extension.auxiliaryservice.core.custom.AddMsisdnAuxSvcExtension;
import com.trilogy.app.crm.extension.auxiliaryservice.core.custom.CallingGroupAuxSvcExtension;
import com.trilogy.app.crm.extension.auxiliaryservice.core.custom.SPGAuxSvcExtension;
import com.trilogy.app.crm.extension.auxiliaryservice.core.custom.URCSPromotionAuxSvcExtension;
import com.trilogy.app.crm.extension.auxiliaryservice.core.custom.VPNAuxSvcExtension;
import com.trilogy.app.crm.extension.auxiliaryservice.core.custom.VoicemailAuxSvcExtension;
import com.trilogy.app.crm.provision.gateway.SPGService;
import com.trilogy.app.crm.provision.gateway.SPGServiceXInfo;
import com.trilogy.app.crm.support.CollectionSupportHelper;
import com.trilogy.app.crm.support.ExtensionSupportHelper;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.app.crm.support.PricePlanSupport;
import com.trilogy.app.crm.support.ServicePackageSupportHelper;
import com.trilogy.app.crm.xhome.adapter.BeanAdapter;
import com.trilogy.app.urcs.promotion.v2_0.Promotion;
import com.trilogy.app.urcs.promotion.v2_0.PromotionState;
import com.trilogy.app.urcs.promotion.v2_0.PromotionType;
import com.trilogy.framework.license.LicenseMgr;
import com.trilogy.framework.xhome.beans.ComparableComparator;
import com.trilogy.framework.xhome.beans.MetaBeanException;
import com.trilogy.framework.xhome.beans.ReverseComparator;
import com.trilogy.framework.xhome.beans.XBeans;
import com.trilogy.framework.xhome.beans.xi.PropertyInfo;
import com.trilogy.framework.xhome.beans.xi.XInfo;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAware;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.True;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.xenum.AbstractEnum;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.util.crmapi.wsdl.v2_0.types.GenericParameter;
import com.trilogy.util.crmapi.wsdl.v2_0.types.PaidType;
import com.trilogy.util.crmapi.wsdl.v3_0.api.CRMExceptionFault;
import com.trilogy.util.crmapi.wsdl.v3_0.api.ServicesAndBundlesServiceSkeletonInterface;
import com.trilogy.util.crmapi.wsdl.v3_0.types.CRMRequestHeader;
import com.trilogy.util.crmapi.wsdl.v3_0.types.serviceandbundle.AuxiliaryService;
import com.trilogy.util.crmapi.wsdl.v3_0.types.serviceandbundle.AuxiliaryServiceModificationResult;
import com.trilogy.util.crmapi.wsdl.v3_0.types.serviceandbundle.AuxiliaryServiceReference;
import com.trilogy.util.crmapi.wsdl.v3_0.types.serviceandbundle.Bundle;
import com.trilogy.util.crmapi.wsdl.v3_0.types.serviceandbundle.BundleCategory;
import com.trilogy.util.crmapi.wsdl.v3_0.types.serviceandbundle.BundleCategoryAssociation;
import com.trilogy.util.crmapi.wsdl.v3_0.types.serviceandbundle.BundleCategoryModificationResult;
import com.trilogy.util.crmapi.wsdl.v3_0.types.serviceandbundle.BundleCategoryRatePlanAssociation;
import com.trilogy.util.crmapi.wsdl.v3_0.types.serviceandbundle.BundleCategoryRatePlanAssociationModificationResult;
import com.trilogy.util.crmapi.wsdl.v3_0.types.serviceandbundle.BundleCategoryRatePlanAssociationReference;
import com.trilogy.util.crmapi.wsdl.v3_0.types.serviceandbundle.BundleCategoryReference;
import com.trilogy.util.crmapi.wsdl.v3_0.types.serviceandbundle.BundleFeeReference;
import com.trilogy.util.crmapi.wsdl.v3_0.types.serviceandbundle.BundleModificationResult;
import com.trilogy.util.crmapi.wsdl.v3_0.types.serviceandbundle.BundleReference;
import com.trilogy.util.crmapi.wsdl.v3_0.types.serviceandbundle.PricePlan;
import com.trilogy.util.crmapi.wsdl.v3_0.types.serviceandbundle.PricePlanDependencyGroup;
import com.trilogy.util.crmapi.wsdl.v3_0.types.serviceandbundle.PricePlanDependencyGroupModificationResult;
import com.trilogy.util.crmapi.wsdl.v3_0.types.serviceandbundle.PricePlanModificationResult;
import com.trilogy.util.crmapi.wsdl.v3_0.types.serviceandbundle.PricePlanPrerequisiteGroup;
import com.trilogy.util.crmapi.wsdl.v3_0.types.serviceandbundle.PricePlanPrerequisiteGroupModificationResult;
import com.trilogy.util.crmapi.wsdl.v3_0.types.serviceandbundle.PricePlanReference;
import com.trilogy.util.crmapi.wsdl.v3_0.types.serviceandbundle.PricePlanValidationGroup;
import com.trilogy.util.crmapi.wsdl.v3_0.types.serviceandbundle.PricePlanValidationGroupModificationResult;
import com.trilogy.util.crmapi.wsdl.v3_0.types.serviceandbundle.Service;
import com.trilogy.util.crmapi.wsdl.v3_0.types.serviceandbundle.ServiceFeeReference;
import com.trilogy.util.crmapi.wsdl.v3_0.types.serviceandbundle.ServiceModificationResult;
import com.trilogy.util.crmapi.wsdl.v3_0.types.serviceandbundle.ServiceReference;


/**
 * Implementation of ServicesAndBundles API interface.
 *
 * @author victor.stratan@redknee.com
 */
public class ServicesAndBundlesImpl implements ServicesAndBundlesServiceSkeletonInterface, ContextAware
{

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 1L;


    /**
     * Create a new instance of <code>ServicesAndBundlesImpl</code>.
     *
     * @param ctx
     *            The operating context.
     * @throws RemoteException
     *             Thrown by RMI.
     */
    public ServicesAndBundlesImpl(final Context ctx) throws RemoteException
    {
        this.context_ = ctx;
        this.pricePlanAdapter_ = new PricePlanToApiAdapter();
        this.serviceFeeAdapter_ = new ServiceFeeToApiAdapter();
        this.bundleFeeAdapter_ = new BundleFeeToApiAdapter();
        this.auxiliaryServiceAdapter_ = new AuxiliaryServiceToApiAdapter();
        this.auxiliarBundleReferenceAdapter_ = new BundleProfileToApiReferenceAdapter();
        this.auxiliarBundleAdapter_ = new BundleProfileToApiAdapter();
        this.auxiliarBundleToFeeAdapter_ = new BundleProfileToApiFeeAdapter();
        this.bundleCategoryAdapter_ = new BundleCategoryToApiAdapter();
        this.bundleCategoryReferenceAdapter_ = new BundleCategoryToApiReferenceAdapter();
        this.auxiliaryServiceReferenceAdapter_ = new AuxiliaryServiceToApiReferenceAdapter();
        this.ratePlanAssociationAdapter_ = new RatePlanAssociationToApiAdapter();
        this.ratePlanAssociationReferenceAdapter_ = new RatePlanAssociationToApiReferenceAdapter();
        this.pricePlanReferenceAdapter_ = new PricePlanToApiReferenceAdapter();
        this.dependencyGroupAdapter_ = new DependencyGroupToApiAdapter();
        this.prerequisiteGroupAdapter_ = new PrerequisiteGroupToApiAdapter();
        this.serviceAdapter_ = new ServiceToApiAdapter();
        this.serviceReferenceAdapter_ = new ServiceToApiReferenceAdapter();
        this.priceplangroupAdapter_ = new PricePlanGroupToApiAdapter();
        this.pricePlanVersionAdapter_ = new PricePlanVersionToApiAdapter();
    }


    /**
     * {@inheritDoc}
     */
    public PricePlanReference[] listPricePlans(final CRMRequestHeader header, final int spid, final PaidType paid,
            Integer subscriptionType, Boolean isAscending, GenericParameter[] parameters) throws CRMExceptionFault
    {
        final Context ctx = getContext().createSubContext();
        // handle login. Failed logins throw exceptions
        RmiApiSupport.authenticateUser(ctx, header, "listPricePlans",
              Constants.PERMISSION_SERVICESANDBUNDLES_READ_LISTPRICEPLANS);

        RmiApiErrorHandlingSupport.validateMandatoryObject(paid, "paid type");

        RmiApiSupport.getCrmServiceProvider(ctx, spid, this);

        PricePlanReference[] pricePlanReferences = new PricePlanReference[] {};
        com.redknee.framework.xhome.msp.MSP.setBeanSpid(ctx, spid);

        try
        {
            final SubscriberTypeEnum type = RmiApiSupport.convertApiPaidType2CrmSubscriberType(paid);
            
            final And condition = new And();
            condition.add(new EQ(PricePlanXInfo.SPID, spid));
            condition.add(new EQ(PricePlanXInfo.PRICE_PLAN_TYPE, type));
            if (subscriptionType != null)
            {
                condition.add(new EQ(PricePlanXInfo.SUBSCRIPTION_TYPE, Long.valueOf(subscriptionType)));   
            }
            
            final Collection<com.redknee.app.crm.bean.PricePlan> collection = HomeSupportHelper.get(ctx).getBeans(
                    ctx, 
                    com.redknee.app.crm.bean.PricePlan.class, 
                    condition, 
                    RmiApiSupport.isSortAscending(isAscending));
            
            pricePlanReferences = CollectionSupportHelper.get(ctx).adaptCollection(
                    ctx, 
                    collection, 
                    this.pricePlanReferenceAdapter_, 
                    pricePlanReferences);
        }
        catch (final Exception e)
        {
            final String msg = "Unable to list Price Plans for Service Provider=" + spid + " paid=" + paid;
            RmiApiErrorHandlingSupport.handleQueryExceptions(ctx, e, msg, this);
        }

        return pricePlanReferences;
    }


    /**
     * {@inheritDoc}
     */
    public PricePlan[] listDetailedPricePlans(final CRMRequestHeader header, final int spid, final PaidType paid, Integer subscriptionType, Boolean isAscending, GenericParameter[] parameters)
        throws CRMExceptionFault
    {
        final Context ctx = getContext().createSubContext();

        // handle login. Failed logins throw exceptions
        RmiApiSupport.authenticateUser(ctx, header, "listDetailedPricePlans", 
                Constants.PERMISSION_SERVICESANDBUNDLES_READ_LISTDETAILEDPRICEPLANS, 
                Constants.PERMISSION_SERVICESANDBUNDLES_READ_GETALLPRICEPLANS);

        RmiApiErrorHandlingSupport.validateMandatoryObject(paid, "paid type");

        RmiApiSupport.getCrmServiceProvider(ctx, spid, this);
        
        PricePlan[] pricePlans = new PricePlan[] {};
        try
        {
            final SubscriberTypeEnum type = RmiApiSupport.convertApiPaidType2CrmSubscriberType(paid);
            
            final And condition = new And();
            condition.add(new EQ(PricePlanXInfo.SPID, spid));
            condition.add(new EQ(PricePlanXInfo.PRICE_PLAN_TYPE, type));
            if (subscriptionType != null)
            {
                condition.add(new EQ(PricePlanXInfo.SUBSCRIPTION_TYPE, Long.valueOf(subscriptionType)));   
            }
            
            final Collection<com.redknee.app.crm.bean.PricePlan> collection = HomeSupportHelper.get(ctx).getBeans(
                    ctx, 
                    com.redknee.app.crm.bean.PricePlan.class, 
                    condition, 
                    RmiApiSupport.isSortAscending(isAscending));
            
            pricePlans = new PricePlan[collection.size()];

            int i = 0;
            for (final com.redknee.app.crm.bean.PricePlan pp : collection)
            {
                PricePlanVersion ppv = null;
                try
                {
                    ppv = getCrmPricePlanVersion(ctx, pp.getId(), pp.getCurrentVersion(), this);
                }
                catch (CRMExceptionFault crmEx)
                {
                }
                List<PricePlanVersion> ppvList  = null;
                if (ppv != null)
                {
                    ppvList = new ArrayList<PricePlanVersion>();
                    ppvList.add(ppv);
                }
                pricePlans[i++] = PricePlanToApiAdapter.adaptPricePlanToApi(ctx, pp, ppvList);
            }
        }
        catch (final Exception e)
        {
            final String msg = "Unable to retrieve all Price Plans for Service Provider=" + spid + " paid=" + paid;
            RmiApiErrorHandlingSupport.handleQueryExceptions(ctx, e, msg, this);
        }

        return pricePlans;
    }




    /**
     * {@inheritDoc}
     */
    public ServiceFeeReference[] listServiceFees(final CRMRequestHeader header, final long planID, final long version, Boolean isAscending, GenericParameter[] parameters)
        throws CRMExceptionFault
    {
        final Context ctx = getContext().createSubContext();
        // handle login. Failed logins throw exceptions
        RmiApiSupport.authenticateUser(ctx, header, "listServiceFees",
            Constants.PERMISSION_SERVICESANDBUNDLES_READ_LISTSERVICEFEES);

        final PricePlanVersion ppv = getCrmPricePlanVersion(ctx, planID, version, this);

        ServiceFeeReference[] feeReferences = new ServiceFeeReference[] {};
        try
        {
            // Improve sorting, maybe by source then by ID
            final Map<Long, ServiceFee2> result;
            if (RmiApiSupport.isSortAscending(isAscending))
            {
                result = new TreeMap<Long, ServiceFee2>();
            }
            else
            {
                result = new TreeMap<Long, ServiceFee2>(new ReverseComparator(ComparableComparator.instance()));
            }
            
            final Map<Long, ServiceFee2> serviceFees = ppv.getServicePackageVersion().getServiceFees();
            setPackageSourceToServiceFees(serviceFees.values(), 0);
            result.putAll(serviceFees);

            final Collection<ServicePackageFee> packFees = ppv.getServicePackageVersion().getPackageFees().values();
            final Collection<ServicePackageVersion> packs = convertServicePackageFeesToVersions(ctx, packFees);
            for (final Iterator it = packs.iterator(); it.hasNext();)
            {
                final ServicePackageVersion packVer = (ServicePackageVersion) it.next();
                final Map<Long, ServiceFee2> serviceFeesVer = packVer.getServiceFees();
                setPackageSourceToServiceFees(serviceFeesVer.values(), packVer.getId());
                result.putAll(serviceFeesVer);
            }

            feeReferences = CollectionSupportHelper.get(ctx).adaptCollection(
                    ctx, 
                    result.values(), 
                    this.serviceFeeAdapter_, 
                    feeReferences);
        }
        catch (final Exception e)
        {
            final String msg = "Unable to list Service Fees for Price Plan=" + planID + " version=" + version;
            RmiApiErrorHandlingSupport.handleQueryExceptions(ctx, e, msg, this);
        }

        return feeReferences;
    }


    /**
     * {@inheritDoc}
     */
    public BundleFeeReference[] listBundleFees(final CRMRequestHeader header, final long planID, final long version, Boolean isAscending, GenericParameter[] parameters)
        throws CRMExceptionFault
    {
        final Context ctx = getContext().createSubContext();
        // handle login. Failed logins throw exceptions
        RmiApiSupport.authenticateUser(ctx, header, "listBundleFees",
            Constants.PERMISSION_SERVICESANDBUNDLES_READ_LISTBUNDLEFEES);

        final PricePlanVersion ppv = getCrmPricePlanVersion(ctx, planID, version, this);

        BundleFeeReference[] feeReferences = new BundleFeeReference[] {};
        try
        {
            // Improve sorting, maybe by source then by ID 
            final Map<Long, BundleFee> result;
            if (RmiApiSupport.isSortAscending(isAscending))
            {
                result = new TreeMap<Long, BundleFee>();
            }
            else
            {
                result = new TreeMap<Long, BundleFee>(new ReverseComparator(ComparableComparator.instance()));
            }
            
            final Map<Long, BundleFee> bundleFees = ppv.getServicePackageVersion().getBundleFees();
            setPackageSourceToBundleFees(bundleFees.values(), 0);
            result.putAll(bundleFees);

            final Collection<ServicePackageFee> packFees = ppv.getServicePackageVersion().getPackageFees().values();
            final Collection<ServicePackageVersion> packs = convertServicePackageFeesToVersions(ctx, packFees);
            for (final ServicePackageVersion packVer : packs)
            {
                final Map<Long, BundleFee> bundleFeesVer = packVer.getBundleFees();
                setPackageSourceToBundleFees(bundleFeesVer.values(), packVer.getId());
                result.putAll(bundleFeesVer);
            }

            feeReferences = CollectionSupportHelper.get(ctx).adaptCollection(
                    ctx, 
                    result.values(), 
                    this.bundleFeeAdapter_, 
                    feeReferences);
        }
        catch (final Exception e)
        {
            final String msg = "Unable to list Bundle Fees for Price Plan=" + planID + " version=" + version;
            RmiApiErrorHandlingSupport.handleQueryExceptions(ctx, e, msg, this);
        }

        return feeReferences;
    }


    /**
     * {@inheritDoc}
     */
    public AuxiliaryServiceReference[] listAuxiliaryServices(final CRMRequestHeader header, final int spid, Boolean isAscending, GenericParameter[] parameters)
        throws CRMExceptionFault
    {
        final Context ctx = getContext().createSubContext();
        // handle login. Failed logins throw exceptions
        RmiApiSupport.authenticateUser(ctx, header, "listAuxiliaryServices",
            Constants.PERMISSION_SERVICESANDBUNDLES_READ_LISTAUXILIARYSERVICES);

        RmiApiSupport.getCrmServiceProvider(ctx, spid, this);

        AuxiliaryServiceReference[] auxiliaryServiceReferences = new AuxiliaryServiceReference[] {};
        try
        {
            final EQ condition = new EQ(AuxiliaryServiceXInfo.SPID, spid);
            
            final Collection<com.redknee.app.crm.bean.core.AuxiliaryService> collection = HomeSupportHelper.get(ctx).getBeans(
                    ctx, 
                    com.redknee.app.crm.bean.core.AuxiliaryService.class, 
                    condition, 
                    RmiApiSupport.isSortAscending(isAscending));
            
            auxiliaryServiceReferences = CollectionSupportHelper.get(ctx).adaptCollection(
                    ctx, 
                    collection, 
                    this.auxiliaryServiceReferenceAdapter_, 
                    auxiliaryServiceReferences);
        }
        catch (final Exception e)
        {
            final String msg = "Unable to retrieve Auxiliary Services for Service Provider=" + spid;
            RmiApiErrorHandlingSupport.handleQueryExceptions(ctx, e, msg, this);
        }

        return auxiliaryServiceReferences;
    }


    /**
     * {@inheritDoc}
     */
    public AuxiliaryService getAuxiliaryService(final CRMRequestHeader header, final long auxiliaryServiceID, GenericParameter[] parameters)
        throws CRMExceptionFault
    {
        final Context ctx = getContext().createSubContext();
        // handle login. Failed logins throw exceptions
        RmiApiSupport.authenticateUser(ctx, header, "getAuxiliaryService",
            Constants.PERMISSION_SERVICESANDBUNDLES_READ_GETAUXILIARYSERVICE);

        com.redknee.app.crm.bean.core.AuxiliaryService auxiliaryService = null;
        try
        {
            auxiliaryService = HomeSupportHelper.get(ctx).findBean(
                    ctx, 
                    com.redknee.app.crm.bean.core.AuxiliaryService.class, 
                    auxiliaryServiceID);
        }
        catch (final Exception e)
        {
            final String msg = "Unable to retrieve Auxiliary Service " + auxiliaryServiceID;
            RmiApiErrorHandlingSupport.handleQueryExceptions(ctx, e, msg, this);
        }

        if (auxiliaryService == null)
        {
            final String msg = "Auxiliary Service " + auxiliaryServiceID;
            RmiApiErrorHandlingSupport.identificationException(ctx, msg, this);
        }

        final AuxiliaryService result = AuxiliaryServiceToApiAdapter.adaptAuxiliaryServiceToApi(ctx, auxiliaryService);

        return result;
    }


    /**
     * {@inheritDoc}
     */
    public BundleFeeReference[] listAuxiliaryBundles(final CRMRequestHeader header, final int spid, Boolean isAscending, GenericParameter[] parameters)
        throws CRMExceptionFault
    {
        final Context ctx = getContext().createSubContext();
        // handle login. Failed logins throw exceptions
        RmiApiSupport.authenticateUser(ctx, header, "listAuxiliaryBundles",
            Constants.PERMISSION_SERVICESANDBUNDLES_READ_LISTAUXILIARYBUNDLES);

        RmiApiSupport.getCrmServiceProvider(ctx, spid, this);

        BundleFeeReference[] bundleFeeReferences = new BundleFeeReference[] {};
        try
        {
            CRMBundleProfile service = (CRMBundleProfile) ctx.get(CRMBundleProfile.class);
            if (service == null)
            {
                throw new CRMExceptionFault("System error: Bundle service does not exist");
            }

            // Get the SPID filtered home and put it in the context for HomeSupport
            Home home = service.getAuxiliaryBundlesBySPID(ctx, spid);
            ctx.put(BundleProfileHome.class, home);
            
            final Collection<BundleProfile> collection = HomeSupportHelper.get(ctx).getBeans(
                    ctx, 
                    BundleProfile.class, 
                    True.instance(),
                    RmiApiSupport.isSortAscending(isAscending));
            
            bundleFeeReferences = CollectionSupportHelper.get(ctx).adaptCollection(
                    ctx, 
                    collection, 
                    this.auxiliarBundleToFeeAdapter_, 
                    bundleFeeReferences);
        }
        catch (final Exception e)
        {
            final String msg = "Unable to retrieve Auxiliary Bundles for Service Provider=" + spid;
            RmiApiErrorHandlingSupport.handleQueryExceptions(ctx, e, msg, this);
        }

        return bundleFeeReferences;
    }


    /**
     * Converts a collection service package fees to a collection service package
     * versions.
     *
     * @param ctx
     *            The operating context.
     * @param fees
     *            Collection service package fees.
     * @return Collection service package versions.
     * @throws CRMExceptionFault
     *             Thrown if there are other problems.
     * @throws HomeException
     *             Thrown if there are problems looking up the service package version.
     */
    public Collection<ServicePackageVersion> convertServicePackageFeesToVersions(final Context ctx, final Collection<ServicePackageFee> fees)
        throws CRMExceptionFault, HomeException
    {
        final Collection<ServicePackageVersion> result = new ArrayList<ServicePackageVersion>();

        for (final ServicePackageFee fee : fees)
        {
            final ServicePackageVersion version = ServicePackageSupportHelper.get(ctx).getCurrentVersion(ctx, fee.getPackageId());
            result.add(version);
        }

        return result;
    }


    /**
     * Returns CRM price plan version.
     *
     * @param ctx
     *            The operating context.
     * @param planID
     *            Price plan identifier.
     * @param version
     *            Price plan version.
     * @param caller
     *            Caller of this method.
     * @return CRM price plan version.
     * @throws CRMExceptionFault
     *             Thrown if there are problems looking up the price plan.
     */
    public PricePlanVersion getCrmPricePlanVersion(final Context ctx, final long planID, final long version,
        final Object caller) throws CRMExceptionFault
    {
        PricePlanVersion ppv = null;
        try
        {
            ppv = PricePlanSupport.getVersion(ctx, planID, (int) version);
        }
        catch (final Exception e)
        {
            final String msg = "Unable to find Price Plan " + planID + " version " + version;
            RmiApiErrorHandlingSupport.handleQueryExceptions(ctx, e, msg, caller);
        }

        if (ppv == null)
        {
            final String msg = "Price Plan " + planID + " version " + version;
            RmiApiErrorHandlingSupport.identificationException(ctx, msg, caller);
        }

        return ppv;
    }


    public Collection<PricePlanVersion> getCrmPricePlanVersions(final Context ctx, final long planID,
            final Object caller) throws CRMExceptionFault
    {
        Collection<PricePlanVersion> ppvList = null;
        try
        {
            ppvList = HomeSupportHelper.get(ctx).getBeans(ctx, PricePlanVersion.class,
                    new EQ(PricePlanVersionXInfo.ID, planID));
        }
        catch (final Exception e)
        {
            final String msg = "Unable to find Price Plan " + planID ;
            RmiApiErrorHandlingSupport.handleQueryExceptions(ctx, e, msg, caller);
        }
        if (ppvList == null)
        {
            final String msg = "Price Plan " + planID;
            RmiApiErrorHandlingSupport.identificationException(ctx, msg, caller);
        }
        return ppvList;
    }

    
    /**
     * {@inheritDoc}
     */
    public AuxiliaryServiceModificationResult createAuxiliaryService(CRMRequestHeader header,
            AuxiliaryService auxiliaryService, GenericParameter[] parameters) throws CRMExceptionFault
    {
        final Context ctx = getContext().createSubContext();
        // handle login. Failed logins throw exceptions
        RmiApiSupport.authenticateUser(ctx, header, "createAuxiliaryService",
                Constants.PERMISSION_SERVICESANDBUNDLES_WRITE_CREATEAUXILIARYSERVICE);
        RmiApiErrorHandlingSupport.validateMandatoryObject(auxiliaryService, "auxiliaryService");        
        RmiApiErrorHandlingSupport.validateMandatoryObject(auxiliaryService.getSpid(), "auxiliaryService.spid");
        RmiApiErrorHandlingSupport.validateMandatoryObject(auxiliaryService.getActivationFeeType(), "auxiliaryService.activationFeeType");        
        RmiApiErrorHandlingSupport.validateMandatoryObject(auxiliaryService.getCallingGroupType(), "auxiliaryService.callingGroupType");
        RmiApiErrorHandlingSupport.validateMandatoryObject(auxiliaryService.getPeriod(), "auxiliaryService.period");
        RmiApiErrorHandlingSupport.validateMandatoryObject(auxiliaryService.getType(), "auxiliaryService.type");
        RmiApiErrorHandlingSupport.validateMandatoryObject(auxiliaryService.getTechnology(), "auxiliaryService.technology");        
        RmiApiErrorHandlingSupport.validateMandatoryObject(auxiliaryService.getRecurrence(), "auxiliaryService.recurrence");
        RmiApiErrorHandlingSupport.validateMandatoryObject(auxiliaryService.getRecurrence().getRecurrenceType(), "auxiliaryService.recurrence.recurrenceType");
        RmiApiErrorHandlingSupport.validateMandatoryObject(auxiliaryService.getRecurrence().getPeriodUnitType(), "auxiliaryService.recurrence.periodUnitType");        
        RmiApiErrorHandlingSupport.validateMandatoryObject(auxiliaryService.getPaidType(), "auxiliaryService.paidType");        
        RmiApiErrorHandlingSupport.validateMandatoryObject(auxiliaryService.getParameters(), "auxiliaryService.parameters");
        RmiApiSupport.validateExistanceOfBeanForKey(ctx, CRMSpid.class, new EQ(CRMSpidXInfo.ID, auxiliaryService.getSpid()));
        AuxiliaryServiceModificationResult auxiliaryServiceModification = new AuxiliaryServiceModificationResult();
        try
        {            
            Home home = RmiApiSupport.getCrmHome(ctx, com.redknee.app.crm.bean.AuxiliaryServiceHome.class,
                    ServicesAndBundlesImpl.class);
            home = ApiSupport.injectAPIUpdateEntityHomeIntoPipeline(ctx, home);
            com.redknee.app.crm.bean.core.AuxiliaryService crmAuxServiceClass = (com.redknee.app.crm.bean.core.AuxiliaryService) AuxiliaryServiceToApiAdapter
                    .adaptApiToAuxiliaryService(ctx, auxiliaryService);
            AuxiliaryServiceToApiAdapter.adaptGenericParametersToCreateAuxiliaryService(ctx, 
                    auxiliaryService.getParameters(), crmAuxServiceClass);
            RmiApiSupport.validateExistanceOfBeanForKey(
                    ctx,
                    GLCodeMapping.class,
                    new And().add(new EQ(GLCodeMappingXInfo.SPID, crmAuxServiceClass.getSpid())).add(
                            new EQ(GLCodeMappingXInfo.GL_CODE, crmAuxServiceClass.getGLCode())));
            RmiApiSupport.validateExistanceOfBeanForKey(
                    ctx,
                    TaxAuthority.class,
                    new And().add(new EQ(TaxAuthorityXInfo.SPID, crmAuxServiceClass.getSpid())).add(
                            new EQ(TaxAuthorityXInfo.TAX_ID, crmAuxServiceClass.getTaxAuthority())));
            if (crmAuxServiceClass.getType().getIndex() == AuxiliaryServiceTypeEnum.Voicemail_INDEX)
            {
                String vmPlanId = VoicemailAuxSvcExtension.DEFAULT_VMPLANID;
                VoicemailAuxSvcExtension voicemailAuxSvcExtension = ExtensionSupportHelper.get(ctx).getExtension(ctx, crmAuxServiceClass, VoicemailAuxSvcExtension.class);
                if (voicemailAuxSvcExtension!=null)
                {
                    vmPlanId = voicemailAuxSvcExtension.getVmPlanId();
                }
                else
                {
                    LogSupport.minor(ctx, this,
                            "Unable to find required extension of type '" + VoicemailAuxSvcExtension.class.getSimpleName()
                                    + "' for auxiliary service " + crmAuxServiceClass.getIdentifier());
                }

                RmiApiSupport.validateExistanceOfBeanForKey(ctx, VMPlan.class, new EQ(VMPlanXInfo.VM_PLAN_ID,
                        vmPlanId));
            }     
            
            if (crmAuxServiceClass.getType().getIndex() == AuxiliaryServiceTypeEnum.SERVICE_PROVISIONING_GATEWAY_INDEX)
            {
                SPGAuxSvcExtension spgAuxSvcExtension = ExtensionSupportHelper.get(ctx).getExtension(ctx,crmAuxServiceClass, SPGAuxSvcExtension.class);
                if (spgAuxSvcExtension!=null)
                {
                    RmiApiSupport.validateExistanceOfBeanForKey(ctx, SPGService.class, new EQ(SPGServiceXInfo.ID,
                            spgAuxSvcExtension.getSPGServiceType()));
                }
                else
                {
                    LogSupport.minor(ctx, this,
                            "Unable to find required extension of type '" + SPGAuxSvcExtension.class.getSimpleName()
                                    + "' for auxiliary service " + crmAuxServiceClass.getIdentifier());
                }
            }            
            if (auxiliaryService.getServiceOption() != null
                    && crmAuxServiceClass.getType().getIndex() == AuxiliaryServiceTypeEnum.URCS_Promotion_INDEX)
            {
                long serviceOption = URCSPromotionAuxSvcExtension.DEFAULT_SERVICEOPTION;
                URCSPromotionAuxSvcExtension urcsPromotionAuxSvcExtension = ExtensionSupportHelper.get(ctx).getExtension(ctx, crmAuxServiceClass, URCSPromotionAuxSvcExtension.class);
                if (urcsPromotionAuxSvcExtension!=null)
                {
                    serviceOption = urcsPromotionAuxSvcExtension.getServiceOption();
                }
                else
                {
                    LogSupport.minor(ctx, this,
                            "Unable to find required extension of type '" + URCSPromotionAuxSvcExtension.class.getSimpleName()
                                    + "' for auxiliary service " + crmAuxServiceClass.getIdentifier());
                }
                // service option                
                PromotionManagementClientV2 client = UrcsClientInstall.getClient(ctx, UrcsClientInstall.PROMOTION_MANAGEMENT_CLIENT_V2_KEY);
                Collection<Promotion> list = client.listAllPromotionsForSpid(ctx, crmAuxServiceClass.getSpid(), PromotionType.PRIVATETYPE);
                boolean found = false;
                if(list != null)
                {
                    for (Promotion promotion : list)
                    {
                        if (promotion.promotionId == serviceOption
                                && promotion.state == PromotionState.ACTIVATED)
                        {
                            found = true;
                            break;
                        }
                    }
                    if (!found)
                    {
                        RmiApiErrorHandlingSupport.simpleValidation("auxiliaryService",
                                "No Active Promotion found for Id : " + serviceOption);
                    }
                }
                else
                {
                    RmiApiErrorHandlingSupport.simpleValidation("auxiliaryService", "No Promotion Retrieved from URCS");
                }
            }            
            Object obj = home.create(crmAuxServiceClass);
            auxiliaryServiceModification.setAuxiliaryService((AuxiliaryService) auxiliaryServiceAdapter_
                    .adapt(ctx, obj));
            auxiliaryServiceModification.setParameters(parameters);
        }
        catch (final Exception e)
        {
            final String msg = "Unable to create Auxiliary Service";
            RmiApiErrorHandlingSupport.handleQueryExceptions(ctx, e, msg, this);
        }
        return auxiliaryServiceModification;
    }


    /**
     * {@inheritDoc}
     */
    public BundleModificationResult createBundle(CRMRequestHeader header, Bundle bundle,
            GenericParameter[] parameters) throws CRMExceptionFault
    {
        final Context ctx = getContext().createSubContext();
        // handle login. Failed logins throw exceptions
        RmiApiSupport.authenticateUser(ctx, header, "createBundle",
                Constants.PERMISSION_SERVICESANDBUNDLES_WRITE_CREATEBUNDLE);
        RmiApiErrorHandlingSupport.validateMandatoryObject(bundle, "bundle");        
        RmiApiErrorHandlingSupport.validateMandatoryObject(bundle.getSpid(), "bundle.spid");
        RmiApiErrorHandlingSupport.validateMandatoryObject(bundle.getName(), "bundle.name");
        RmiApiErrorHandlingSupport.validateMandatoryObject(bundle.getActivationFeeType(), "bundle.activationFeeType");
        RmiApiErrorHandlingSupport.validateMandatoryObject(bundle.getPaidType(), "bundle.paidType");
        RmiApiErrorHandlingSupport.validateMandatoryObject(bundle.getAuxiliary(), "bundle.getAuxiliary");
        RmiApiErrorHandlingSupport.validateMandatoryObject(bundle.getCurrency(), "bundle.currency");
        RmiApiErrorHandlingSupport.validateMandatoryObject(bundle.getReprovisionOnActive(), "bundle.reprovisionOnActive");
        RmiApiErrorHandlingSupport.validateMandatoryObject(bundle.getRecurrence(), "bundle.recurrence");
        RmiApiErrorHandlingSupport.validateMandatoryObject(bundle.getRecurrence().getPeriodUnitType(), "bundle.periodUnitType");
        RmiApiErrorHandlingSupport.validateMandatoryObject(bundle.getRecurrence().getRecurrenceType(), "bundle.recurrenceType");
        RmiApiErrorHandlingSupport.validateMandatoryObject(bundle.getExpiryScheme(), "bundle.expiryScheme");        
        RmiApiErrorHandlingSupport.validateMandatoryObject(bundle.getQuotaScheme(), "bundle.qutoaScheme");
        RmiApiErrorHandlingSupport.validateMandatoryObject(bundle.getParameters(), "bundle.parameters");
        if (bundle.getBundleCategoryIDs() == null && bundle.getBundleCategories() == null)
        {
            final String msg = "bundle.bundleCategories is null. bundle.bundleCategories is Mandatory and cannot be null";
            RmiApiErrorHandlingSupport.simpleValidation("bundle.bundleCategories", msg);
        }
        else if (bundle.getBundleCategories() == null || bundle.getBundleCategories().length == 0)
        {
            List<BundleCategoryAssociation> associations = new ArrayList<BundleCategoryAssociation>();
            if (bundle.getBundleCategoryIDs()!=null)
            {
                for (Long id : bundle.getBundleCategoryIDs())
                {
                    BundleCategoryAssociation association = new BundleCategoryAssociation();
                    association.setBundleCategoryID(id);
                    association.setRate((int) com.redknee.app.crm.bundle.BundleCategoryAssociation.DEFAULT_RATE);
                    associations.add(association);
                }
            }
            bundle.setBundleCategories(associations.toArray(new BundleCategoryAssociation[]{}));
            
        }
        
        

        if(bundle.getBundleCategories().length <= 0)
        {
            RmiApiErrorHandlingSupport.simpleValidation("bundle", "Bundle Categories not provied in the request" );
        }
        RmiApiErrorHandlingSupport.validateMandatoryObject(bundle.getBundleCategories()[0].getBundleCategoryID(), "bundle.bundleCategories[0].bundleCategoryId");
        RmiApiErrorHandlingSupport.validateMandatoryObject(bundle.getBundleCategories()[0].getRate(), "bundle.bundleCategories[0].rate");
        RmiApiSupport.validateExistanceOfBeanForKey(ctx, CRMSpid.class, new EQ(CRMSpidXInfo.ID, bundle.getSpid()));        
        BundleModificationResult bundleModification  = new BundleModificationResult();
        try
        {            
            Home home = RmiApiSupport.getCrmHome(ctx, com.redknee.app.crm.bean.ui.BundleProfileHome.class,
                    ServicesAndBundlesImpl.class);  
            home = ApiSupport.injectAPIUpdateEntityHomeIntoPipeline(ctx, home);
            List<com.redknee.app.crm.bean.core.BundleCategoryAssociation> bundleCategoryList = new ArrayList<com.redknee.app.crm.bean.core.BundleCategoryAssociation>();
            if (!bundle.getCurrency() && bundle.getBundleCategories().length == 1)
            {
                com.redknee.app.crm.bundle.BundleCategory bcateg = HomeSupportHelper.get(ctx).findBean(
                        ctx,
                        com.redknee.app.crm.bundle.BundleCategory.class,
                        new And().add(
                                new EQ(BundleCategoryXInfo.CATEGORY_ID, bundle.getBundleCategories()[0].getBundleCategoryID().longValue()))
                                .add(new EQ(BundleCategoryXInfo.SPID, bundle.getSpid())));
                if (bcateg == null)
                {
                    RmiApiErrorHandlingSupport.identificationException(
                            ctx,
                            "Bundle Category Id not present " + bundle.getBundleCategories()[0].getBundleCategoryID() + ", Spid : "
                                    + bundle.getSpid(), this);
                }
                if (!bcateg.isEnabled())
                {
                    RmiApiErrorHandlingSupport.simpleValidation(
                            "bundle",
                            "Bundle Category Id not Enabled " + bundle.getBundleCategories()[0].getBundleCategoryID() + ", Spid : "
                                    + bundle.getSpid());
                }
                com.redknee.app.crm.bean.core.BundleCategoryAssociation association = new com.redknee.app.crm.bean.core.BundleCategoryAssociation();
                association.setCategoryId(bcateg.getCategoryId());
                association.setRate((int) com.redknee.app.crm.bundle.BundleCategoryAssociation.DEFAULT_RATE);
                bundleCategoryList.add(association);
            }
            else
            {
                for (BundleCategoryAssociation category : bundle.getBundleCategories())
                {
                    Long categoryId = category.getBundleCategoryID();
                    com.redknee.app.crm.bundle.BundleCategory bcateg = HomeSupportHelper.get(ctx).findBean(ctx,
                            com.redknee.app.crm.bundle.BundleCategory.class,
                            new EQ(BundleCategoryXInfo.CATEGORY_ID, categoryId.longValue()));
                    if (bcateg == null)
                    {
                        RmiApiErrorHandlingSupport.simpleValidation("bundle", "Bundle Category Id not present "
                                + categoryId + ", Spid : " + bundle.getSpid());
                    }
                    if (!bcateg.isEnabled())
                    {
                        RmiApiErrorHandlingSupport.simpleValidation("bundle", "Bundle Category Id not present "
                                + categoryId + ", Spid : " + bundle.getSpid());
                    }
                    
                    com.redknee.app.crm.bean.core.BundleCategoryAssociation association = new com.redknee.app.crm.bean.core.BundleCategoryAssociation();
                    association.setCategoryId(bcateg.getCategoryId());
                    
                    if (bundle.getCurrency())
                    {
                        association.setRate((int) com.redknee.app.crm.bundle.BundleCategoryAssociation.DEFAULT_RATE);
                    }
                    else
                    {
                        association.setRate(category.getRate());
                    }

                    bundleCategoryList.add(association);
                }
            }
            com.redknee.app.crm.bean.ui.BundleProfile crmBean = (com.redknee.app.crm.bean.ui.BundleProfile) BundleProfileToApiAdapter
                    .adaptApiToBundleProfile(ctx, bundle, bundleCategoryList);
            LicenseMgr lMgr = (LicenseMgr) ctx.get(LicenseMgr.class);
            switch (crmBean.getActivationScheme().getIndex())
            {
            case ActivationTypeEnum.ACTIVATE_ON_PROVISION_INDEX:
                if (lMgr != null
                        && !lMgr.isLicensed(ctx, BMLicenseConstants.ACTIVATION_SCHEME_ACTIVE_ON_PROVISION)
                        && bundle.getActivationScheme() != null
                        && (lMgr.isLicensed(ctx, BMLicenseConstants.ACTIVATION_SCHEME_FIRST_CALL_ACTIVATION) || lMgr
                                .isLicensed(ctx, BMLicenseConstants.ACTIVATION_SCHEME_SCHEDULED_ACTIVATION)))
                {
                    RmiApiErrorHandlingSupport
                            .simpleValidation("bundle",
                                    "License not enabled " + BMLicenseConstants.ACTIVATION_SCHEME_ACTIVE_ON_PROVISION
                                            + ", Spid : " + bundle.getSpid());
                }
                break;
            case ActivationTypeEnum.FIRST_CALL_ACTIVATION_INDEX:
                if (lMgr != null && !lMgr.isLicensed(ctx, BMLicenseConstants.ACTIVATION_SCHEME_FIRST_CALL_ACTIVATION))
                {
                    RmiApiErrorHandlingSupport.simpleValidation("bundle",
                            "License not enabled " + BMLicenseConstants.ACTIVATION_SCHEME_FIRST_CALL_ACTIVATION
                                    + ", Spid : " + bundle.getSpid());
                }
                break;
            case ActivationTypeEnum.SCHEDULED_ACTIVATION_INDEX:
                if (lMgr != null && !lMgr.isLicensed(ctx, BMLicenseConstants.ACTIVATION_SCHEME_SCHEDULED_ACTIVATION))
                {
                    RmiApiErrorHandlingSupport.simpleValidation("bundle",
                            "License not enabled " + BMLicenseConstants.ACTIVATION_SCHEME_SCHEDULED_ACTIVATION
                                    + ", Spid : " + bundle.getSpid());
                }
                break;
            }
            BundleProfileToApiAdapter.adaptGenericParametersToCreateBundleProfile(bundle.getParameters(), crmBean);
            
            /*
             * Validation for Repurchase Bundle
             */
            RmiApiSupport.validateRepurchaseability(crmBean);
            
            RmiApiSupport.validateExistanceOfBeanForKey(
                    ctx,
                    GLCodeMapping.class,
                    new And().add(new EQ(GLCodeMappingXInfo.SPID, crmBean.getSpid())).add(
                            new EQ(GLCodeMappingXInfo.GL_CODE, crmBean.getGLCode())));
            RmiApiSupport.validateExistanceOfBeanForKey(
                    ctx,
                    TaxAuthority.class,
                    new And().add(new EQ(TaxAuthorityXInfo.SPID, crmBean.getSpid())).add(
                            new EQ(TaxAuthorityXInfo.TAX_ID, crmBean.getTaxAuthority())));
            Object obj = home.create(crmBean);            
            bundleModification.setBundle((Bundle) auxiliarBundleAdapter_.adapt(ctx, bundleProfileUiToBundleProfileAdapter_.unAdapt(ctx, obj)));
            bundleModification.setParameters(parameters);
        }
        catch (final Exception e)
        {
            final String msg = "Unable to create Bundle";
            RmiApiErrorHandlingSupport.handleQueryExceptions(ctx, e, msg, this);
        }
        return bundleModification;
    }


    /**
     * {@inheritDoc}
     */
    public BundleCategoryModificationResult createBundleCategory(CRMRequestHeader header,
            BundleCategory bundleCategory, GenericParameter[] parameters) throws CRMExceptionFault
    {
        final Context ctx = getContext().createSubContext();
        // handle login. Failed logins throw exceptions
        RmiApiSupport.authenticateUser(ctx, header, "createBundleCategory",
                Constants.PERMISSION_SERVICESANDBUNDLES_WRITE_CREATEBUNDLECATEGORY);
        RmiApiErrorHandlingSupport.validateMandatoryObject(bundleCategory, "bundleCategory");
        RmiApiErrorHandlingSupport.validateMandatoryObject(bundleCategory.getSpid(), "bundleCategory.spid");
        RmiApiErrorHandlingSupport.validateMandatoryObject(bundleCategory.getIdentifier(), "bundleCategory.identifier");
        RmiApiErrorHandlingSupport.validateMandatoryObject(bundleCategory.getUnitType(), "bundleCategory.unitType");
        RmiApiSupport.validateExistanceOfBeanForKey(ctx, CRMSpid.class, new EQ(CRMSpidXInfo.ID, bundleCategory.getSpid()));
        BundleCategoryModificationResult bundleCategoryModification  = new BundleCategoryModificationResult();
        try
        {
            Home home = RmiApiSupport.getCrmHome(ctx, com.redknee.app.crm.bundle.BundleCategoryHome.class,
                    ServicesAndBundlesImpl.class);
            home = ApiSupport.injectAPIUpdateEntityHomeIntoPipeline(ctx, home);
            com.redknee.app.crm.bundle.BundleCategory crmBean = (com.redknee.app.crm.bundle.BundleCategory) BundleCategoryToApiAdapter
                    .adaptApiToBundleCategory(ctx, bundleCategory);
            Object obj = home.create(crmBean);
            bundleCategoryModification.setBundleCategory((BundleCategory) bundleCategoryAdapter_.adapt(ctx, obj));
        }
        catch (final Exception e)
        {
            final String msg = "Unable to create Bundle Category";
            RmiApiErrorHandlingSupport.handleQueryExceptions(ctx, e, msg, this);
        }
        return bundleCategoryModification;
    }


    /**
     * {@inheritDoc}
     */
    public PricePlanModificationResult createPricePlan(CRMRequestHeader header, PricePlan pricePlan,
            GenericParameter[] parameters) throws CRMExceptionFault
    {
        final Context ctx = getContext().createSubContext();
        // handle login. Failed logins throw exceptions
        RmiApiSupport.authenticateUser(ctx, header, "createPricePlan",
                Constants.PERMISSION_SERVICESANDBUNDLES_WRITE_CREATEPRICEPLAN);
        RmiApiErrorHandlingSupport.validateMandatoryObject(pricePlan, "pricePlan");
        RmiApiErrorHandlingSupport.validateMandatoryObject(pricePlan.getSpid(), "pricePlan.spid");        
        if(pricePlan.getCriteria()!= null && pricePlan.getCriteria()[0]!= null)
        {            
            RmiApiErrorHandlingSupport.validateMandatoryObject(pricePlan.getCriteria()[0].getPricePlanCriteriaChoice_type0(), "pricePlan.criteria[0].PricePlanCriteriaChoice_type0");
            RmiApiErrorHandlingSupport.validateMandatoryObject(pricePlan.getCriteria()[0].getPricePlanCriteriaChoice_type0().getPricePlanCriteriaSequence_type0(), "pricePlan.criteria[0].pricePlanCriteriaChoice_type0.pricePlanCriteriaSequence_type0");
            RmiApiErrorHandlingSupport.validateMandatoryObject(pricePlan.getCriteria()[0].getPricePlanCriteriaChoice_type0().getPricePlanCriteriaSequence_type0().getContractDuration(), "pricePlan.criteria[0].pricePlanCriteriaChoice_type0.pricePlanCriteriaSequence_type0.contractDuration");
            RmiApiErrorHandlingSupport.validateMandatoryObject(pricePlan.getCriteria()[0].getPricePlanCriteriaChoice_type0().getPricePlanCriteriaSequence_type0().getContractDuration().getDurationFrequency(), "pricePlan.criteria[0].pricePlanCriteriaChoice_type0.pricePlanCriteriaSequence_type0.contractDuration.durationFrequency");            
        }
        RmiApiErrorHandlingSupport.validateMandatoryObject(pricePlan.getPaidtype(), "pricePlan.paidtype");
        RmiApiErrorHandlingSupport.validateMandatoryObject(pricePlan.getTechnology(), "pricePlan.technology");
        RmiApiSupport.validateExistanceOfBeanForKey(ctx, CRMSpid.class, new EQ(CRMSpidXInfo.ID, pricePlan.getSpid()));
        RmiApiSupport.validateExistanceOfBeanForKey(ctx, SubscriptionType.class,
                new EQ(SubscriptionTypeXInfo.ID, Long.valueOf(pricePlan.getSubscriptionType())));
        if (pricePlan.getGroup() != null & pricePlan.getGroup().length != 0)
        {
            And and = new And();
            and.add(new EQ(PricePlanGroupXInfo.SPID, pricePlan.getSpid()));
            and.add(new EQ(PricePlanGroupXInfo.IDENTIFIER, pricePlan.getGroup()[0]));
            RmiApiSupport.validateExistanceOfBeanForKey(ctx, PricePlanGroup.class, and);
        }
        RmiApiSupport.validateExistanceOfBeanForKey(
                ctx,
                SubscriptionLevel.class,
                new And().add(new EQ(SubscriptionLevelXInfo.SPID, pricePlan.getSpid())).add(
                        new EQ(SubscriptionLevelXInfo.ID, pricePlan.getSubscriptionLevel().longValue())));
        try
        {
            if (pricePlan.getSmsRatePlan() != null && pricePlan.getSmsRatePlan().length != 0)
            {
                Home smsRatePlanHome = (Home) ctx.get(ModelCrmConstants.RATE_PLAN_SMS_HOME_KEY);
                RatePlan rtp = (RatePlan) smsRatePlanHome.find(ctx, pricePlan.getSmsRatePlan()[0]);                
                if (rtp == null)
                {
                    RmiApiErrorHandlingSupport.simpleValidation("pricePlan",
                            "Sms Rate Plan not Found " + pricePlan.getSmsRatePlan()[0] + " .");
                }
                if (rtp.getSpid() != pricePlan.getSpid())
                {
                    RmiApiErrorHandlingSupport.simpleValidation("pricePlan", "Invalid Spid of Sms Rate Plan "
                            + pricePlan.getSmsRatePlan()[0] + " spid : " + rtp.getSpid());
                }
            }
            if (pricePlan.getVoiceRatePlan() != null && pricePlan.getVoiceRatePlan().length != 0)
            {
                Home voiceRatePlanHome = (Home) ctx.get(ModelCrmConstants.RATE_PLAN_VOICE_HOME_KEY);
                RatePlan rtp = (RatePlan) voiceRatePlanHome.find(ctx, pricePlan.getVoiceRatePlan()[0]);
                if (rtp == null)
                {
                    RmiApiErrorHandlingSupport.simpleValidation("pricePlan",
                            "Voice Rate Plan not Found " + pricePlan.getVoiceRatePlan()[0] + " .");
                }
                if (rtp.getSpid() != pricePlan.getSpid())
                {
                    RmiApiErrorHandlingSupport.simpleValidation("pricePlan", "Invalid Spid of Voice Rate Plan "
                            + pricePlan.getVoiceRatePlan()[0] + " spid : " + rtp.getSpid());
                }
            }
        }
        catch (Exception e)
        {
            RmiApiErrorHandlingSupport.handleQueryExceptions(ctx, e,
                    "Exception while validating SMS and Data Rate Plans " + pricePlan.getIdentifier() + " .", this);
        }
        PricePlanModificationResult modificationResult = new PricePlanModificationResult();
        com.redknee.app.crm.bean.PricePlan crmPricePlan = null;
        GenericParameterParser parser = null;
        if (pricePlan.getParameters()!=null)
        {
            parser = new GenericParameterParser(pricePlan.getParameters());
        }
        try
        {
            Home home = RmiApiSupport.getCrmHome(ctx, PricePlanHome.class,
                    ServicesAndBundlesImpl.class);
            home = ApiSupport.injectAPIUpdateEntityHomeIntoPipeline(ctx, home);
            
           
            com.redknee.app.crm.bean.PricePlan crmBean = (com.redknee.app.crm.bean.PricePlan) PricePlanToApiAdapter
                    .adaptApiToPricePlan(ctx, pricePlan);
           // IF CURRENT VERSION IS 0 THEN SET IT TO 1
            if(crmBean.getCurrentVersion() == 0) 
            	crmBean.setCurrentVersion(1);
            
            if (parser!=null)
            {
                
                if (parser.containsParam(PricePlanXInfo.EXPIRY_EXTENSION_MODE.getSQLName())) 
                { 
                    short expiryExtensionMode = parser.getParameter(PricePlanXInfo.EXPIRY_EXTENSION_MODE.getSQLName(), Short.class);
                    crmBean.setExpiryExtensionMode(ExpiryExtensionModeEnum.get(expiryExtensionMode)); 
                }
                if (parser.containsParam(PricePlanXInfo.EXPIRY_EXTENTION.getSQLName())) 
                {
                    int expiryExtension = parser.getParameter(PricePlanXInfo.EXPIRY_EXTENTION.getSQLName(), Integer.class);
                    crmBean.setExpiryExtention(expiryExtension);
                }
                if (parser.containsParam(PricePlanXInfo.DESCRIPTION.getSQLName())) 
                {
                    String desc = parser.getParameter(PricePlanXInfo.DESCRIPTION.getSQLName(), String.class);
                    crmBean.setDescription(desc);
                }
                if (parser.containsParam(PricePlanXInfo.SENDSWITCHNOTIFICATION.getSQLName())) 
                {
                    boolean isNotification = parser.getParameter(PricePlanXInfo.SENDSWITCHNOTIFICATION.getSQLName(), Boolean.class);
                    crmBean.setSendswitchnotification(isNotification);
                }
                if (parser.containsParam(PricePlanXInfo.APPLY_CONTRACT_DURATION_CRITERIA.getSQLName())) 
                {
                    boolean contractDurationRest = parser.getParameter(PricePlanXInfo.APPLY_CONTRACT_DURATION_CRITERIA.getSQLName(), Boolean.class);
                    if(contractDurationRest)
                    {
                        long from = parser.getParameter(PricePlanXInfo.MIN_CONTRACT_DURATION.getSQLName(), Long.class);
                        long to = parser.getParameter(PricePlanXInfo.MAX_CONTRACT_DURATION.getSQLName(), Long.class);
                        short contractDurationUnit = parser.getParameter(PricePlanXInfo.CONTRACT_DURATION_UNITS.getSQLName(), Short.class);
                        
                        crmBean.setMinContractDuration(from);
                        crmBean.setMaxContractDuration(to);
                        crmBean.setContractDurationUnits(ContractFeeFrequencyEnum.get(contractDurationUnit));
                    }
                    crmBean.setApplyContractDurationCriteria(contractDurationRest);
                }
            }
            
            Object obj = home.create(crmBean);    
           
            crmPricePlan = (com.redknee.app.crm.bean.PricePlan) obj;
            if(crmPricePlan.getCurrentVersion() == 0){
            	crmPricePlan.setCurrentVersion(1);
            }
            modificationResult.setPricePlan((PricePlan) pricePlanAdapter_.adapt(ctx, obj));
            modificationResult.setParameters(parameters);
        }
        catch (final Exception e)
        {
            final String msg = "Unable to create Price Plan";
            RmiApiErrorHandlingSupport.handleQueryExceptions(ctx, e, msg, this);
        }
        
        if (pricePlan.getVersions() != null)
        {
            com.redknee.util.crmapi.wsdl.v3_0.types.serviceandbundle.PricePlanVersion version = pricePlan.getVersions()[0];
            version.setVersion(pricePlan.getCurrentVersion());
            
            try
            {
                if (version != null && crmPricePlan != null)
                {
                    Home home = RmiApiSupport.getCrmHome(ctx, PricePlanVersionHome.class, ServicesAndBundlesImpl.class);
                    if (version.getServiceFees() != null)
                    {
                        for (ServiceFeeReference ref : version.getServiceFees())
                        {
                            And and = new And();
                            and.add(new EQ(ServiceXInfo.SPID, pricePlan.getSpid()));
                            and.add(new EQ(ServiceXInfo.ID, ref.getIdentifier()));
                            RmiApiSupport.validateExistanceOfBeanForKey(ctx, com.redknee.app.crm.bean.Service.class,
                                    and); 
                            
                        }
                    }
                    if (version.getBundleFees() != null)
                    {
                        for (BundleFeeReference ref : version.getBundleFees())
                        {
                            And and = new And();
                            and.add(new EQ(BundleProfileXInfo.SPID, pricePlan.getSpid()));
                            and.add(new EQ(BundleProfileXInfo.BUNDLE_ID, ref.getIdentifier()));
                            RmiApiSupport.validateExistanceOfBeanForKey(ctx, BundleProfile.class, and);
                        }
                    }
                    PricePlanVersion crmBean = (PricePlanVersion) PricePlanVersionToApiAdapter
                            .adaptApiToPricePlanVersion(ctx, version);
                    crmBean.setId(crmPricePlan.getId());
                    
                    
                    if(version.getParameters() != null)
                    {
                        Map crmServiceFees = crmBean.getServicePackageVersion().getServiceFees();
                        Iterator iter = crmServiceFees.keySet().iterator();
                        parser = new GenericParameterParser(version.getParameters());
                        if(parser != null)
                        {
                            XInfo xInfo = XBeans.getInstanceOf(ctx, com.redknee.app.crm.bean.core.ServiceFee2.class, XInfo.class);
                            
                            while(iter.hasNext())
                            {
                                Object key = iter.next();
                                String value = parser.getParameter(String.valueOf(key), String.class);
                                setServiceFee2Parameter(ctx,xInfo, (ServiceFee2)crmServiceFees.get(key), value);
                            }
                        }
                    }
                    
                    Object obj = home.create(crmBean);
                    List<PricePlanVersion> ppvList = null;
                    if (obj != null)
                    {
                        ppvList = new ArrayList<PricePlanVersion>();
                        ppvList.add((PricePlanVersion) obj);
                    }
                    modificationResult.setPricePlan((PricePlan) PricePlanToApiAdapter.adaptPricePlanToApi(ctx,
                            crmPricePlan, ppvList));
                    modificationResult.setParameters(parameters);
                    
                }
            }
            catch (Exception e)
            {
                final String msg = "Created Price Plan Id : " + crmPricePlan.getId()
                        + ". Failed to create First Price Plan Version ";
                RmiApiErrorHandlingSupport.handleQueryExceptions(ctx, e, msg, this);
             
            }
        }        
        return modificationResult;
    }

    private void setServiceFee2Parameter(Context ctx, XInfo xInfo, ServiceFee2 crmServiceFee , String value) throws MetaBeanException
    {
        if(crmServiceFee != null && value != null)
        {
            String[] keyValArray = value.split(";");
            for(String keyVal : keyValArray)
            {
                String[] keyValue = keyVal.split(":");

                PropertyInfo property = xInfo.getPropertyInfo(ctx, keyValue[0]);
                if(property != null)
                {
                    setProperty(ctx, property, crmServiceFee, keyValue);
                }
            }
        }
    }

    
    private void setProperty(Context ctx, PropertyInfo property, ServiceFee2 serviceFee, String[] keyValue) throws MetaBeanException
    {
        
        if (String.class.isAssignableFrom(property.getType()))
        {
            serviceFee.set(keyValue[0], keyValue[1]);
        }
        else if (Short.class.isAssignableFrom(property.getType()))
        {
            serviceFee.set(keyValue[0], Short.valueOf(keyValue[1]));
        }
        else if (Integer.class.isAssignableFrom(property.getType()))
        {
            serviceFee.set(keyValue[0], Integer.valueOf(keyValue[1]));
        }
        else if (Long.class.isAssignableFrom(property.getType()))
        {
            serviceFee.set(keyValue[0], Long.valueOf(keyValue[1]));
        }
        else if (Boolean.class.isAssignableFrom(property.getType()))
        {
            serviceFee.set(keyValue[0], Boolean.valueOf(keyValue[1]));
        }
        else if (ChargeFailureActionEnum.class.isAssignableFrom(property.getType()))
        {
            serviceFee.set(keyValue[0], ChargeFailureActionEnum.get((Short.valueOf(keyValue[1]))));
        }
    }
    /**
     * {@inheritDoc}
     */
    public PricePlanDependencyGroupModificationResult createPricePlanDependencyGroup(CRMRequestHeader header,
            PricePlanDependencyGroup dependencyGroup, GenericParameter[] parameters) throws CRMExceptionFault
    {
        final Context ctx = getContext().createSubContext();
        // handle login. Failed logins throw exceptions
        RmiApiSupport.authenticateUser(ctx, header, "createPricePlanDependencyGroup",
                Constants.PERMISSION_SERVICESANDBUNDLES_WRITE_CREATEPRICEPLANDEPENDENCYGROUP);
        RmiApiErrorHandlingSupport.validateMandatoryObject(dependencyGroup, "dependencyGroup");
        RmiApiErrorHandlingSupport.validateMandatoryObject(dependencyGroup.getSpid(), "dependencyGroup.spid");
        RmiApiErrorHandlingSupport.validateMandatoryObject(dependencyGroup.getType(), "dependencyGroup.type");
        RmiApiSupport.validateExistanceOfBeanForKey(ctx, CRMSpid.class, new EQ(CRMSpidXInfo.ID, dependencyGroup.getSpid()));
        if (dependencyGroup.getAuxiliaryServiceIDs() != null)
        {
            for (long id : dependencyGroup.getAuxiliaryServiceIDs())
            {
                And and = new And();
                and.add(new EQ(AuxiliaryServiceXInfo.SPID, dependencyGroup.getSpid()));
                and.add(new EQ(AuxiliaryServiceXInfo.IDENTIFIER, id));
                RmiApiSupport.validateExistanceOfBeanForKey(ctx, com.redknee.app.crm.bean.core.AuxiliaryService.class, and);
            }
        }
        if (dependencyGroup.getServiceIDs() != null)
        {
            for (long id : dependencyGroup.getServiceIDs())
            {
                And and = new And();
                and.add(new EQ(ServiceXInfo.SPID, dependencyGroup.getSpid()));
                and.add(new EQ(ServiceXInfo.ID, id));
                RmiApiSupport.validateExistanceOfBeanForKey(ctx, com.redknee.app.crm.bean.Service.class, and);
            }
        }
        if (dependencyGroup.getBundleIDs() != null)
        {
            for (long id : dependencyGroup.getBundleIDs())
            {
                And and = new And();
                and.add(new EQ(BundleProfileXInfo.SPID, dependencyGroup.getSpid()));
                and.add(new EQ(BundleProfileXInfo.BUNDLE_ID, id));
                RmiApiSupport.validateExistanceOfBeanForKey(ctx, com.redknee.app.crm.bundle.BundleProfile.class, and);
            }
        }
        PricePlanDependencyGroupModificationResult modificationResult  = new PricePlanDependencyGroupModificationResult();
        try
        {
            Home home = RmiApiSupport.getCrmHome(ctx, DependencyGroupHome.class,
                    ServicesAndBundlesImpl.class);    
            home = ApiSupport.injectAPIUpdateEntityHomeIntoPipeline(ctx, home);
            DependencyGroup crmBean = (DependencyGroup) DependencyGroupToApiAdapter.adaptApiToDependencyGroup(ctx,
                    dependencyGroup);
            Object obj = home.create(crmBean);
            modificationResult.setDependencyGroup((PricePlanDependencyGroup) dependencyGroupAdapter_.adapt(ctx, obj));
            modificationResult.setParameters(parameters);
        }
        catch (final Exception e)
        {
            final String msg = "Unable to create Price Plan Dependency Group";
            RmiApiErrorHandlingSupport.handleQueryExceptions(ctx, e, msg, this);
        }
        return modificationResult;
    }


    /**
     * {@inheritDoc}
     */
    public PricePlanPrerequisiteGroupModificationResult createPricePlanPrerequisiteGroup(CRMRequestHeader header,
            PricePlanPrerequisiteGroup prerequisiteGroup, GenericParameter[] parameters) throws CRMExceptionFault
    {
        final Context ctx = getContext().createSubContext();
        // handle login. Failed logins throw exceptions
        RmiApiSupport.authenticateUser(ctx, header, "createPricePlanPrerequisiteGroup",
                Constants.PERMISSION_SERVICESANDBUNDLES_WRITE_CREATEPRICEPLANPREREQUISITEGROUP);
        RmiApiErrorHandlingSupport.validateMandatoryObject(prerequisiteGroup, "prerequisiteGroup");
        RmiApiErrorHandlingSupport.validateMandatoryObject(prerequisiteGroup.getSpid(), "prerequisiteGroup.spid");
        RmiApiSupport.validateExistanceOfBeanForKey(ctx, CRMSpid.class, new EQ(CRMSpidXInfo.ID, prerequisiteGroup.getSpid()));
        if (prerequisiteGroup.getDependencyPrerequisiteIDs() != null
                && prerequisiteGroup.getDependencyPrerequisiteIDs().length != 0)
        {
            if (prerequisiteGroup.getDependencyPrerequisiteIDs()[0] != null)
            {
                And and = new And();
                and.add(new EQ(DependencyGroupXInfo.SPID, prerequisiteGroup.getSpid()));
                and.add(new EQ(DependencyGroupXInfo.IDENTIFIER, prerequisiteGroup.getDependencyPrerequisiteIDs()[0]
                        .longValue()));
                RmiApiSupport.validateExistanceOfBeanForKey(ctx, DependencyGroup.class, and);
            }
        }
        if (prerequisiteGroup.getServicePrerequisiteIDs() != null
                && prerequisiteGroup.getServicePrerequisiteIDs().length != 0)
        {
            if (prerequisiteGroup.getServicePrerequisiteIDs()[0] != null)
            {
                And and = new And();
                and.add(new EQ(DependencyGroupXInfo.SPID, prerequisiteGroup.getSpid()));
                and.add(new EQ(DependencyGroupXInfo.IDENTIFIER, prerequisiteGroup.getServicePrerequisiteIDs()[0]
                        .longValue()));
                RmiApiSupport.validateExistanceOfBeanForKey(ctx, DependencyGroup.class, and);
            }
        }
        PricePlanPrerequisiteGroupModificationResult modificationResult  = new PricePlanPrerequisiteGroupModificationResult();
        try
        {
            Home home = RmiApiSupport.getCrmHome(ctx, PrerequisiteGroupHome.class,
                    ServicesAndBundlesImpl.class);
            home = ApiSupport.injectAPIUpdateEntityHomeIntoPipeline(ctx, home);
            PrerequisiteGroup crmBean = (PrerequisiteGroup) PrerequisiteGroupToApiAdapter.adaptApiToPrerequisiteGroup(
                    ctx, prerequisiteGroup);
            Object obj = home.create(crmBean);
            modificationResult.setPrerequisiteGroup((PricePlanPrerequisiteGroup) prerequisiteGroupAdapter_.adapt(ctx, obj));
        }
        catch (final Exception e)
        {
            final String msg = "Unable to create Price Plan Prerequisite Group";
            RmiApiErrorHandlingSupport.handleQueryExceptions(ctx, e, msg, this);
        }
        return modificationResult;
    }


    /**
     * {@inheritDoc}
     */
    public PricePlanValidationGroupModificationResult createPricePlanValidationGroup(CRMRequestHeader header,
            PricePlanValidationGroup validationGroup, GenericParameter[] parameters) throws CRMExceptionFault
    {
        final Context ctx = getContext().createSubContext();
        // handle login. Failed logins throw exceptions
        RmiApiSupport.authenticateUser(ctx, header, "createPricePlanValidationGroup",
                Constants.PERMISSION_SERVICESANDBUNDLES_WRITE_CREATEPRICEPLANVALIDATIONGROUP);
        RmiApiErrorHandlingSupport.validateMandatoryObject(validationGroup, "validationGroup");
        RmiApiErrorHandlingSupport.validateMandatoryObject(validationGroup.getSpid(), "validationGroup.spid");
        RmiApiSupport.validateExistanceOfBeanForKey(ctx, CRMSpid.class, new EQ(CRMSpidXInfo.ID, validationGroup.getSpid()));
        if (validationGroup.getParentID() != null)
        {
            And and = new And();
            and.add(new EQ(PricePlanGroupXInfo.SPID, validationGroup.getSpid()));
            and.add(new EQ(PricePlanGroupXInfo.IDENTIFIER, validationGroup.getParentID()));
            RmiApiSupport.validateExistanceOfBeanForKey(ctx, PricePlanGroup.class, and);
        }
        if (validationGroup.getDependencyIDs() != null)
        {            
            for (Long id : validationGroup.getDependencyIDs())
            {
                And and = new And();
                and.add(new EQ(DependencyGroupXInfo.SPID, validationGroup.getSpid()));
                and.add(new EQ(DependencyGroupXInfo.IDENTIFIER, id));
                RmiApiSupport.validateExistanceOfBeanForKey(ctx, DependencyGroup.class, and);
            }            
        }
        if (validationGroup.getPrerequisiteIDs() != null)
        {            
            for(Long id : validationGroup.getPrerequisiteIDs())
            {
                And and = new And();
                and.add(new EQ(PrerequisiteGroupXInfo.SPID, validationGroup.getSpid()));
                and.add(new EQ(PrerequisiteGroupXInfo.IDENTIFIER, id));
                RmiApiSupport.validateExistanceOfBeanForKey(ctx, PrerequisiteGroup.class, and);                
            }            
        }
        PricePlanValidationGroupModificationResult modificationResult  = new PricePlanValidationGroupModificationResult();
        try
        {
            Home home = RmiApiSupport.getCrmHome(ctx, PricePlanGroupHome.class,
                    ServicesAndBundlesImpl.class);
            home = ApiSupport.injectAPIUpdateEntityHomeIntoPipeline(ctx, home);
            PricePlanGroup crmBean = (PricePlanGroup) PricePlanGroupToApiAdapter.adaptApiToPricePlanGroup(ctx,
                    validationGroup);
            Object obj = home.create(crmBean);
            modificationResult.setValidationGroup((PricePlanValidationGroup) priceplangroupAdapter_.adapt(ctx, obj));
            modificationResult.setParameters(parameters);
        }
        catch (final Exception e)
        {
            final String msg = "Unable to create Price Plan Validation Group";
            RmiApiErrorHandlingSupport.handleQueryExceptions(ctx, e, msg, this);
        }
        return modificationResult;
    }


    /**
     * {@inheritDoc}
     */
    public PricePlanModificationResult createPricePlanVersion(CRMRequestHeader header, long planID,            
            com.redknee.util.crmapi.wsdl.v3_0.types.serviceandbundle.PricePlanVersion version,
            GenericParameter[] parameters) throws CRMExceptionFault
    {
        final Context ctx = getContext().createSubContext();
        // handle login. Failed logins throw exceptions
        RmiApiSupport.authenticateUser(ctx, header, "createPricePlanVersion",
                Constants.PERMISSION_SERVICESANDBUNDLES_WRITE_CREATEPRICEPLANVERSION);
        RmiApiErrorHandlingSupport.validateMandatoryObject(version, "version");
        PricePlanModificationResult modificationResult  = new PricePlanModificationResult();
        try
        {
            Home home = RmiApiSupport.getCrmHome(ctx, PricePlanVersionHome.class, ServicesAndBundlesImpl.class);
            home = ApiSupport.injectAPIUpdateEntityHomeIntoPipeline(ctx, home);
            com.redknee.app.crm.bean.PricePlan pricePlan = HomeSupportHelper.get(ctx).findBean(ctx,
                    com.redknee.app.crm.bean.PricePlan.class, planID);
            if (pricePlan == null)
            {
                RmiApiErrorHandlingSupport.simpleValidation("pricePlan", "PricePlan does not exist "
                        + planID + " .");
            }
            if (version.getServiceFees() != null)
            {
                for (ServiceFeeReference ref : version.getServiceFees())
                {
                    And and = new And();
                    and.add(new EQ(ServiceXInfo.SPID, pricePlan.getSpid()));
                    and.add(new EQ(ServiceXInfo.ID, ref.getIdentifier()));
                    RmiApiSupport.validateExistanceOfBeanForKey(ctx, com.redknee.app.crm.bean.Service.class, and);
                }
            }
            if (version.getBundleFees() != null)
            {
                for (BundleFeeReference ref : version.getBundleFees())
                {
                    And and = new And();
                    and.add(new EQ(BundleProfileXInfo.SPID, pricePlan.getSpid()));
                    and.add(new EQ(BundleProfileXInfo.BUNDLE_ID, ref.getIdentifier()));
                    RmiApiSupport.validateExistanceOfBeanForKey(ctx, BundleProfile.class, and);
                }
            }
            PricePlanVersion crmBean = (PricePlanVersion) PricePlanVersionToApiAdapter.adaptApiToPricePlanVersion(ctx,
                    version);
            crmBean.setId(planID);
            crmBean.setVersion(pricePlan.getNextVersion());
            Object obj = home.create(crmBean);
            PricePlanVersion ppVersion = (PricePlanVersion) obj; 
            pricePlan.setCurrentVersion(ppVersion.getVersion());
            pricePlan.setNextVersion(ppVersion.getVersion() + 1);//This is already done in PricePlanVersionHomeProxy
            //But since local priceplan object here is not aware of next version and instead of again fetching from db,
            //directly update next version to +1.
            HomeSupportHelper.get(ctx).storeBean(ctx, pricePlan);
            
            List<PricePlanVersion> ppvList  = null;
            if (obj != null)
            {
                ppvList = new ArrayList<PricePlanVersion>();
                ppvList.add(ppVersion);
            }
            modificationResult.setPricePlan(PricePlanToApiAdapter.adaptPricePlanToApi(ctx, pricePlan, ppvList));
            modificationResult.setParameters(parameters);
        }
        catch (final Exception e)
        {
            final String msg = "Unable to create Price Version";
            RmiApiErrorHandlingSupport.handleQueryExceptions(ctx, e, msg, this);
        }
        return modificationResult;
    }


    /**
     * {@inheritDoc}
     */
    public ServiceModificationResult createService(CRMRequestHeader header, Service service,
            GenericParameter[] parameters) throws CRMExceptionFault
    {
        final Context ctx = getContext().createSubContext();
        // handle login. Failed logins throw exceptions
        RmiApiSupport.authenticateUser(ctx, header, "createService",
                Constants.PERMISSION_SERVICESANDBUNDLES_WRITE_CREATESERVICE);
        RmiApiErrorHandlingSupport.validateMandatoryObject(service, "Service");
        RmiApiErrorHandlingSupport.validateMandatoryObject(service.getSpid(), "service.spid");
        RmiApiErrorHandlingSupport.validateMandatoryObject(service.getName(), "service.name");
        RmiApiErrorHandlingSupport.validateMandatoryObject(service.getSubscriptionType(), "service.subscriptionType");
        RmiApiErrorHandlingSupport.validateMandatoryObject(service.getActivationFeeType(), "service.activationFeeType");
        RmiApiErrorHandlingSupport.validateMandatoryObject(service.getRecurrence(), "service.recurrence");
        RmiApiErrorHandlingSupport.validateMandatoryObject(service.getRecurrence().getRecurrenceType(), "service.recurrence.recurrenceType");
        RmiApiErrorHandlingSupport.validateMandatoryObject(service.getRecurrence().getPeriodUnitType(), "service.recurrence.periodUnitType()");
        RmiApiErrorHandlingSupport.validateMandatoryObject(service.getTechnology(), "service.technology");
        RmiApiErrorHandlingSupport.validateMandatoryObject(service.getType(), "service.type");
        RmiApiErrorHandlingSupport.validateMandatoryObject(service.getParameters(), "service.parameters");
        RmiApiSupport.validateExistanceOfBeanForKey(ctx, CRMSpid.class, new EQ(CRMSpidXInfo.ID, service.getSpid()));
        RmiApiSupport.validateExistanceOfBeanForKey(ctx, SubscriptionType.class,
                new EQ(SubscriptionTypeXInfo.ID, Long.valueOf(service.getSubscriptionType())));
        ServiceModificationResult modificationResult = new ServiceModificationResult();
        try
        {
            Home home = RmiApiSupport.getCrmHome(ctx, com.redknee.app.crm.bean.ServiceHome.class,
                    ServicesAndBundlesImpl.class);
            home = ApiSupport.injectAPIUpdateEntityHomeIntoPipeline(ctx, home);
            com.redknee.app.crm.bean.Service crmBean = (com.redknee.app.crm.bean.Service) ServiceToApiAdapter
                    .adaptApiToService(ctx, service);
            ServiceToApiAdapter.adaptGenericParametersToCreateService(service.getParameters(), crmBean);
            RmiApiSupport.validateExistanceOfBeanForKey(
                    ctx,
                    GLCodeMapping.class,
                    new And().add(new EQ(GLCodeMappingXInfo.SPID, crmBean.getSpid())).add(
                            new EQ(GLCodeMappingXInfo.GL_CODE, crmBean.getAdjustmentGLCode())));
            RmiApiSupport.validateExistanceOfBeanForKey(
                    ctx,
                    TaxAuthority.class,
                    new And().add(new EQ(TaxAuthorityXInfo.SPID, crmBean.getSpid())).add(
                            new EQ(TaxAuthorityXInfo.TAX_ID, crmBean.getTaxAuthority())));
            if (crmBean.getChargeScheme().getIndex() == ServicePeriodEnum.MULTIMONTHLY_INDEX
                    && crmBean.getRecurrenceInterval() < 2)
            {
                RmiApiErrorHandlingSupport.simpleValidation("service", "Recurrence Interval less than 2 for Service "
                        + service.getIdentifier() + " .");
            }
            if(crmBean.getType().getIndex() == ServiceTypeEnum.VOICEMAIL_INDEX)
            {                
                RmiApiSupport.validateExistanceOfBeanForKey(ctx, CrmVmPlan.class,
                        new EQ(CrmVmPlanXInfo.ID, Long.valueOf(crmBean.getVmPlanId())));
            }
            if (crmBean.getType().getIndex() == ServiceTypeEnum.SERVICE_PROVISIONING_GATEWAY_INDEX)
            {
                RmiApiSupport.validateExistanceOfBeanForKey(ctx, SPGService.class,
                        new EQ(SPGServiceXInfo.ID, crmBean.getSPGServiceType()));
            }
            Object obj = home.create(crmBean);
            modificationResult.setService((Service) serviceAdapter_.adapt(ctx, obj));
            modificationResult.setParameters(parameters);
        }
        catch (final Exception e)
        {
            final String msg = "Unable to create Service";
            RmiApiErrorHandlingSupport.handleQueryExceptions(ctx, e, msg, this);
        }
        return modificationResult;
    }


    /**
     * {@inheritDoc}
     */
    public Bundle getBundle(CRMRequestHeader header, long bundleID, GenericParameter[] parameters) throws CRMExceptionFault
    {
        final Context ctx = getContext().createSubContext();
        // handle login. Failed logins throw exceptions
        RmiApiSupport.authenticateUser(ctx, header, "getBundle",
                Constants.PERMISSION_SERVICESANDBUNDLES_READ_GETBUNDLE);
        Bundle bundle  = null;
        try
        {
            BundleProfile bundleProfile = HomeSupportHelper.get(ctx).findBean(ctx, BundleProfile.class, bundleID);
            if(bundleProfile == null)
            {
                RmiApiErrorHandlingSupport.identificationException(ctx, "Bundle Profile not found : " + bundleID, this);
            }
            bundle = (Bundle) auxiliarBundleAdapter_.adapt(ctx, bundleProfile);
        }
        catch (final Exception e)
        {
            final String msg = "Unable to retrieve Bundle";
            RmiApiErrorHandlingSupport.handleQueryExceptions(ctx, e, msg, this);
        }
        return bundle;
    }


    /**
     * {@inheritDoc}
     */
    public BundleCategory getBundleCategory(CRMRequestHeader header, long bundleCategoryID, GenericParameter[] parameters) throws CRMExceptionFault
    {
        final Context ctx = getContext().createSubContext();
        // handle login. Failed logins throw exceptions
        RmiApiSupport.authenticateUser(ctx, header, "getBundleCategory",
                Constants.PERMISSION_SERVICESANDBUNDLES_READ_GETBUNDLECATEGORY);
        BundleCategory bundleCategory  = null;
        try
        {
            com.redknee.app.crm.bundle.BundleCategory category = HomeSupportHelper.get(ctx).findBean(ctx,
                    com.redknee.app.crm.bundle.BundleCategory.class, (int) bundleCategoryID);
            if(category == null)
            {
                RmiApiErrorHandlingSupport.identificationException(ctx, "Bundle Category not found : " + bundleCategoryID, this);
            }
            bundleCategory = (BundleCategory) bundleCategoryAdapter_.adapt(ctx, category);
        }
        catch (final Exception e)
        {
            final String msg = "Unable to retrieve Bundle Category";
            RmiApiErrorHandlingSupport.handleQueryExceptions(ctx, e, msg, this);
        }
        return bundleCategory;
    }


    /**
     * {@inheritDoc}
     */
    public BundleCategoryRatePlanAssociation getBundleCategoryRatePlanAssociation(CRMRequestHeader header,
            long associationID, GenericParameter[] parameters) throws CRMExceptionFault
    {
        final Context ctx = getContext().createSubContext();
        // handle login. Failed logins throw exceptions
        RmiApiSupport.authenticateUser(ctx, header, "getBundleCategoryRatePlanAssociation",
                Constants.PERMISSION_SERVICESANDBUNDLES_READ_GETBUNDLECATEGORYRATEPLANASSOCIATION);
        BundleCategoryRatePlanAssociation bundleCategoryRatePlanAssociation  = null;
        try
        {
            RatePlanAssociation rtp = HomeSupportHelper.get(ctx).findBean(ctx, RatePlanAssociation.class, associationID);
            if(rtp == null)
            {
                RmiApiErrorHandlingSupport.identificationException(ctx,
                        "Bundle Category Rate Plan Association not found : " + associationID, this);
            }
            bundleCategoryRatePlanAssociation = (BundleCategoryRatePlanAssociation) ratePlanAssociationAdapter_.adapt(ctx, rtp);
        }
        catch (final Exception e)
        {
            final String msg = "Unable to retrieve Bundle Category Rate Plan Association";
            RmiApiErrorHandlingSupport.handleQueryExceptions(ctx, e, msg, this);
        }
        return bundleCategoryRatePlanAssociation;
    }


    /**
     * {@inheritDoc}
     */
    public PricePlanDependencyGroup[] getPricePlanDependencyGroups(CRMRequestHeader header, Long[] dependencyGroupIDs, GenericParameter[] parameters)
            throws CRMExceptionFault
    {
        final Context ctx = getContext().createSubContext();
        // handle login. Failed logins throw exceptions
        RmiApiSupport.authenticateUser(ctx, header, "getPricePlanDependencyGroups",
                Constants.PERMISSION_SERVICESANDBUNDLES_READ_GETPRICEPLANDEPENDENCYGROUPS);
        RmiApiErrorHandlingSupport.validateMandatoryObject(dependencyGroupIDs, "dependencyGroupIDs");
        PricePlanDependencyGroup[] pricePlanDependencyGroup  = new PricePlanDependencyGroup[] {};
        try
        {
            List<DependencyGroup> dependencyGroupList = new ArrayList<DependencyGroup>();
            for (Long groupId : dependencyGroupIDs)
            {
                DependencyGroup dependencyGroup = HomeSupportHelper.get(ctx).findBean(ctx, DependencyGroup.class,
                        groupId);
                if(dependencyGroup == null)
                {
                    RmiApiErrorHandlingSupport.identificationException(ctx, "Price Plan Dependency Group Does not exist : " + groupId, this);
                }
                dependencyGroupList.add(dependencyGroup);
            }
            pricePlanDependencyGroup = new PricePlanDependencyGroup[dependencyGroupList.size()];
            int i = 0;
            for (DependencyGroup dgroup : dependencyGroupList)
            {
                pricePlanDependencyGroup[i] = (PricePlanDependencyGroup) dependencyGroupAdapter_.adapt(ctx, dgroup);
                i++;
            }
        }
        catch (final Exception e)
        {
            final String msg = "Unable to retrieve Price Plan Dependency Group";
            RmiApiErrorHandlingSupport.handleQueryExceptions(ctx, e, msg, this);
        }
        return pricePlanDependencyGroup;
    }


    /**
     * {@inheritDoc}
     */
    public PricePlanPrerequisiteGroup[] getPricePlanPrerequisiteGroups(CRMRequestHeader header,
            Long[] prerequisiteGroupIDs, GenericParameter[] parameters) throws CRMExceptionFault
    {
        final Context ctx = getContext().createSubContext();
        // handle login. Failed logins throw exceptions
        RmiApiSupport.authenticateUser(ctx, header, "getPricePlanPrerequisiteGroups",
                Constants.PERMISSION_SERVICESANDBUNDLES_READ_GETPRICEPLANPREREQUISITEGROUPS);
        RmiApiErrorHandlingSupport.validateMandatoryObject(prerequisiteGroupIDs, "prerequisiteGroupIDs");
        PricePlanPrerequisiteGroup[] pricePlanPrerequisiteGroup  = new PricePlanPrerequisiteGroup[] {};
        try
        {
            List<PrerequisiteGroup> requisiteList = new ArrayList<PrerequisiteGroup>();
            for (Long groupId : prerequisiteGroupIDs)
            {
                PrerequisiteGroup prerequisiteGroup = HomeSupportHelper.get(ctx).findBean(ctx, PrerequisiteGroup.class,
                        groupId);
                if(prerequisiteGroup == null)
                {
                    RmiApiErrorHandlingSupport.identificationException(ctx, "Price Plan Prequisite Group Does not exist : " + groupId, this);
                }
                requisiteList.add(prerequisiteGroup);
            }
            pricePlanPrerequisiteGroup = new PricePlanPrerequisiteGroup[requisiteList.size()];
            int i = 0;
            for (PrerequisiteGroup dgroup : requisiteList)
            {
                pricePlanPrerequisiteGroup[i] = (PricePlanPrerequisiteGroup) prerequisiteGroupAdapter_.adapt(ctx, dgroup);
                i++;
            }
        }
        catch (final Exception e)
        {
            final String msg = "Unable to retrieve Price Plan Prerequisite Group";
            RmiApiErrorHandlingSupport.handleQueryExceptions(ctx, e, msg, this);
        }
        return pricePlanPrerequisiteGroup;
    }


    /**
     * {@inheritDoc}
     */
    public PricePlanValidationGroup[] getPricePlanValidationGroups(CRMRequestHeader header, Long[] groupIDs,
            Boolean includeAncestors, GenericParameter[] parameters) throws CRMExceptionFault
    {        
        final Context ctx = getContext().createSubContext();
        // handle login. Failed logins throw exceptions
        RmiApiSupport.authenticateUser(ctx, header, "getPricePlanValidationGroups",
                Constants.PERMISSION_SERVICESANDBUNDLES_READ_GETPRICEPLANVALIDATIONGROUPS);
        PricePlanValidationGroup[] pricePlanValidationGroup  = new PricePlanValidationGroup[] {};
        try
        {
            Set<PricePlanGroup> pricePlanGroupList = new LinkedHashSet<PricePlanGroup>();
            for (Long groupId : groupIDs)
            {
                PricePlanGroup validationGroup = HomeSupportHelper.get(ctx).findBean(ctx, PricePlanGroup.class,
                        groupId);
                if(validationGroup == null)
                {
                    RmiApiErrorHandlingSupport.identificationException(ctx, "Price Plan Validation Group Does not exist : " + groupId, this);
                }
                pricePlanGroupList.add(validationGroup);                
            }
            if(includeAncestors)
            {
                Set<PricePlanGroup> parentPricePlanGroupList = new LinkedHashSet<PricePlanGroup>();
                for (PricePlanGroup group : pricePlanGroupList)
                {
                    PricePlanGroup validationGroup = HomeSupportHelper.get(ctx).findBean(ctx, PricePlanGroup.class,
                            group.getParentPPG());
                    if(validationGroup!= null)
                    {
                        parentPricePlanGroupList.add(validationGroup);                        
                    }                    
                }
                pricePlanGroupList.addAll(parentPricePlanGroupList);
            }
            pricePlanValidationGroup = new PricePlanValidationGroup[pricePlanGroupList.size()];
            int i = 0;
            for (PricePlanGroup dgroup : pricePlanGroupList)
            {
                pricePlanValidationGroup[i] = (PricePlanValidationGroup) priceplangroupAdapter_.adapt(ctx, dgroup);
                i++;
            }
        }
        catch (final Exception e)
        {
            final String msg = "Unable to retrieve Price Plan Validation Group";
            RmiApiErrorHandlingSupport.handleQueryExceptions(ctx, e, msg, this);
        }
        return pricePlanValidationGroup;
    }


    /**
     * {@inheritDoc}
     */
    public Service getService(CRMRequestHeader header, long serviceID, GenericParameter[] parameters) throws CRMExceptionFault
    {
        final Context ctx = getContext().createSubContext();
        // handle login. Failed logins throw exceptions
        RmiApiSupport.authenticateUser(ctx, header, "getService",
                Constants.PERMISSION_SERVICESANDBUNDLES_READ_GETSERVICE);
        Service service  = null;
        try
        {
            com.redknee.app.crm.bean.Service srvc = HomeSupportHelper.get(ctx).findBean(ctx,
                    com.redknee.app.crm.bean.Service.class, serviceID);
            if(srvc == null)
            {
                RmiApiErrorHandlingSupport.identificationException(ctx, "Service not found : " + serviceID, this);
            }
            service = (Service) serviceAdapter_.adapt(ctx, srvc);
        }
        catch (final Exception e)
        {
            final String msg = "Unable to retrieve Service";
            RmiApiErrorHandlingSupport.handleQueryExceptions(ctx, e, msg, this);
        }
        return service;
    }


    /**
     * {@inheritDoc}
     */
    public BundleCategoryReference[] listBundleCategories(CRMRequestHeader header, int spid, Boolean isAscending, GenericParameter[] parameters)
            throws CRMExceptionFault
    {
        final Context ctx = getContext().createSubContext();
        // handle login. Failed logins throw exceptions
        RmiApiSupport.authenticateUser(ctx, header, "listBundleCategories",
                Constants.PERMISSION_SERVICESANDBUNDLES_READ_LISTBUNDLECATEGORIES);
        RmiApiSupport.getCrmServiceProvider(ctx, spid, this);
        BundleCategoryReference[] bundleCategoryReference  = new BundleCategoryReference[]{};
        try
        {
            final EQ condition = new EQ(BundleCategoryXInfo.SPID, spid);
            final Collection<com.redknee.app.crm.bundle.BundleCategory> collection = HomeSupportHelper.get(ctx).getBeans(
                    ctx, 
                    com.redknee.app.crm.bundle.BundleCategory.class, 
                    condition, 
                    RmiApiSupport.isSortAscending(isAscending));            
            bundleCategoryReference = CollectionSupportHelper.get(ctx).adaptCollection(
                    ctx, 
                    collection, 
                    this.bundleCategoryReferenceAdapter_, 
                    bundleCategoryReference);
        }
        catch (final Exception e)
        {
            final String msg = "Unable to retrieve Bundle Category Reference";
            RmiApiErrorHandlingSupport.handleQueryExceptions(ctx, e, msg, this);
        }
        return bundleCategoryReference;
    }


    /**
     * {@inheritDoc}
     */
    public BundleCategoryRatePlanAssociationReference[] listBundleCategoryRatePlanAssociations(
            CRMRequestHeader header, int spid, Boolean isAscending, GenericParameter[] parameters) throws CRMExceptionFault
    {
        final Context ctx = getContext().createSubContext();
        // handle login. Failed logins throw exceptions
        RmiApiSupport.authenticateUser(ctx, header, "listBundleCategoryRatePlanAssociations",
                Constants.PERMISSION_SERVICESANDBUNDLES_READ_LISTBUNDLECATEGORYRATEPLANASSOCIATIONS);
        RmiApiSupport.getCrmServiceProvider(ctx, spid, this);
        BundleCategoryRatePlanAssociationReference[] bundleCategoryRatePlanAssociationReference  = new BundleCategoryRatePlanAssociationReference[] {};
        try
        {
            final EQ condition = new EQ(BundleProfileXInfo.SPID, spid);
            final Collection<RatePlanAssociation> collection = HomeSupportHelper.get(ctx).getBeans(
                    ctx, 
                    RatePlanAssociation.class, 
                    condition, 
                    RmiApiSupport.isSortAscending(isAscending));            
            bundleCategoryRatePlanAssociationReference = CollectionSupportHelper.get(ctx).adaptCollection(
                    ctx, 
                    collection, 
                    this.ratePlanAssociationReferenceAdapter_, 
                    bundleCategoryRatePlanAssociationReference);
        }
        catch (final Exception e)
        {
            final String msg = "Unable to retrieve BundleCategory RatePlan Association Reference";
            RmiApiErrorHandlingSupport.handleQueryExceptions(ctx, e, msg, this);
        }
        return bundleCategoryRatePlanAssociationReference;
    }


    /**
     * {@inheritDoc}
     */
    public BundleReference[] listBundles(CRMRequestHeader header, int spid, Boolean isAscending, GenericParameter[] parameters)
            throws CRMExceptionFault
    {
        final Context ctx = getContext().createSubContext();
        // handle login. Failed logins throw exceptions
        RmiApiSupport.authenticateUser(ctx, header, "listBundles",
                Constants.PERMISSION_SERVICESANDBUNDLES_READ_LISTBUNDLES);
        RmiApiSupport.getCrmServiceProvider(ctx, spid, this);
        BundleReference[] bundles  = new BundleReference[] {};
        try
        {
            final EQ condition = new EQ(BundleProfileXInfo.SPID, spid);
            final Collection<com.redknee.app.crm.bean.core.BundleProfile> collection = HomeSupportHelper.get(ctx).getBeans(
                    ctx, 
                    com.redknee.app.crm.bean.core.BundleProfile.class, 
                    condition, 
                    RmiApiSupport.isSortAscending(isAscending));            
            bundles = CollectionSupportHelper.get(ctx).adaptCollection(
                    ctx, 
                    collection, 
                    this.auxiliarBundleReferenceAdapter_, 
                    bundles);        
        }
        catch (final Exception e)
        {
            final String msg = "Unable to retrieve Bundles";
            RmiApiErrorHandlingSupport.handleQueryExceptions(ctx, e, msg, this);
        }
        return bundles;
    }


    /**
     * {@inheritDoc}
     */
    public AuxiliaryService[] listDetailedAuxiliaryServices(CRMRequestHeader header, int spid, Boolean isAscending, GenericParameter[] parameters)
            throws CRMExceptionFault
    {
        final Context ctx = getContext().createSubContext();
        // handle login. Failed logins throw exceptions
        RmiApiSupport.authenticateUser(ctx, header, "listDetailedAuxiliaryServices",
                Constants.PERMISSION_SERVICESANDBUNDLES_READ_LISTDETAILEDAUXILIARYSERVICES);
        RmiApiSupport.getCrmServiceProvider(ctx, spid, this);
        AuxiliaryService[] auxiliaryServices  = new AuxiliaryService[] {};
        try
        {
            final EQ condition = new EQ(AuxiliaryServiceXInfo.SPID, spid);
            final Collection<com.redknee.app.crm.bean.core.AuxiliaryService> collection = HomeSupportHelper.get(ctx).getBeans(
                    ctx, 
                    com.redknee.app.crm.bean.core.AuxiliaryService.class, 
                    condition, 
                    RmiApiSupport.isSortAscending(isAscending));            
            auxiliaryServices = CollectionSupportHelper.get(ctx).adaptCollection(
                    ctx, 
                    collection, 
                    this.auxiliaryServiceAdapter_, 
                    auxiliaryServices);
        }
        catch (final Exception e)
        {
            final String msg = "Unable to retrieve Auxiliary Services";
            RmiApiErrorHandlingSupport.handleQueryExceptions(ctx, e, msg, this);
        }
        return auxiliaryServices;
    }


    /**
     * {@inheritDoc}
     */
    public BundleCategory[] listDetailedBundleCategories(CRMRequestHeader header, int spid, Boolean isAscending, GenericParameter[] parameters)
            throws CRMExceptionFault
    {
        final Context ctx = getContext().createSubContext();
        // handle login. Failed logins throw exceptions
        RmiApiSupport.authenticateUser(ctx, header, "listDetailedBundleCategories",
                Constants.PERMISSION_SERVICESANDBUNDLES_READ_LISTDETAILEDBUNDLECATEGORIES);
        RmiApiSupport.getCrmServiceProvider(ctx, spid, this);
        BundleCategory[] bundleCategories  = new BundleCategory[] {};
        try
        {
            final EQ condition = new EQ(BundleCategoryXInfo.SPID, spid);
            final Collection<com.redknee.app.crm.bundle.BundleCategory> collection = HomeSupportHelper.get(ctx).getBeans(
                    ctx, 
                    com.redknee.app.crm.bundle.BundleCategory.class, 
                    condition, 
                    RmiApiSupport.isSortAscending(isAscending));            
            bundleCategories = CollectionSupportHelper.get(ctx).adaptCollection(
                    ctx, 
                    collection, 
                    this.bundleCategoryAdapter_, 
                    bundleCategories);        
        }
        catch (final Exception e)
        {
            final String msg = "Unable to retrieve Bundle Categories";
            RmiApiErrorHandlingSupport.handleQueryExceptions(ctx, e, msg, this);
        }
        return bundleCategories;
    }


    /**
     * {@inheritDoc}
     */
    public BundleCategoryRatePlanAssociation[] listDetailedBundleCategoryRatePlanAssociations(CRMRequestHeader header,
            int spid, Boolean isAscending, GenericParameter[] parameters) throws CRMExceptionFault
    {
        final Context ctx = getContext().createSubContext();
        // handle login. Failed logins throw exceptions
        RmiApiSupport.authenticateUser(ctx, header, "listDetailedBundleCategoryRatePlanAssociations",
                Constants.PERMISSION_SERVICESANDBUNDLES_READ_LISTDETAILEDBUNDLECATEGORYRATEPLANASSOCIATIONS);
        RmiApiSupport.getCrmServiceProvider(ctx, spid, this);
        BundleCategoryRatePlanAssociation[] bundleCategoryRatePlanAssociations  = new BundleCategoryRatePlanAssociation[] {};
        try
        {
            final EQ condition = new EQ(RatePlanAssociationXInfo.SPID, spid);
            final Collection<RatePlanAssociation> collection = HomeSupportHelper.get(ctx).getBeans(
                    ctx, 
                    RatePlanAssociation.class, 
                    condition, 
                    RmiApiSupport.isSortAscending(isAscending));            
            bundleCategoryRatePlanAssociations = CollectionSupportHelper.get(ctx).adaptCollection(
                    ctx, 
                    collection, 
                    this.ratePlanAssociationAdapter_, 
                    bundleCategoryRatePlanAssociations);        
        }
        catch (final Exception e)
        {
            final String msg = "Unable to retrieve Bundle Category RatePlan Association";
            RmiApiErrorHandlingSupport.handleQueryExceptions(ctx, e, msg, this);
        }
        return bundleCategoryRatePlanAssociations;
    }


    /**
     * {@inheritDoc}
     */
    public Bundle[] listDetailedBundles(CRMRequestHeader header, int spid, Boolean isAscending, GenericParameter[] parameters)
            throws CRMExceptionFault
    {
        final Context ctx = getContext().createSubContext();
        // handle login. Failed logins throw exceptions
        RmiApiSupport.authenticateUser(ctx, header, "listDetailedBundles",
                Constants.PERMISSION_SERVICESANDBUNDLES_READ_LISTDETAILEDBUNDLES);
        RmiApiSupport.getCrmServiceProvider(ctx, spid, this);
        Bundle[] bundles  = new Bundle[] {};
        try
        {
            final EQ condition = new EQ(BundleProfileXInfo.SPID, spid);
            final Collection<BundleProfile> collection = HomeSupportHelper.get(ctx).getBeans(
                    ctx, 
                    BundleProfile.class, 
                    condition, 
                    RmiApiSupport.isSortAscending(isAscending));            
            bundles = CollectionSupportHelper.get(ctx).adaptCollection(
                    ctx, 
                    collection, 
                    this.auxiliarBundleAdapter_, 
                    bundles);
        }
        catch (final Exception e)
        {
            final String msg = "Unable to retrieve Bundles";
            RmiApiErrorHandlingSupport.handleQueryExceptions(ctx, e, msg, this);
        }
        return bundles;
    }


    /**
     * {@inheritDoc}
     */
    public PricePlan[] listDetailedPricePlans(CRMRequestHeader header, int spid, PaidType paid,
            Integer subscriptionType, Boolean provideAllVersions, Boolean isAscending, GenericParameter[] parameters) throws CRMExceptionFault
    {        
        final Context ctx = getContext().createSubContext();
        // handle login. Failed logins throw exceptions
        RmiApiSupport.authenticateUser(ctx, header, "listDetailedPricePlans",
                Constants.PERMISSION_SERVICESANDBUNDLES_READ_LISTDETAILEDPRICEPLANS);
        RmiApiErrorHandlingSupport.validateMandatoryObject(paid, "paid type");
        RmiApiSupport.getCrmServiceProvider(ctx, spid, this);
        PricePlan[] priceplans  = new PricePlan[] {};
        try
        {
            final SubscriberTypeEnum type = RmiApiSupport.convertApiPaidType2CrmSubscriberType(paid);            
            final And condition = new And();
            condition.add(new EQ(PricePlanXInfo.SPID, spid));
            condition.add(new EQ(PricePlanXInfo.PRICE_PLAN_TYPE, type));
            if (subscriptionType != null)
            {
                condition.add(new EQ(PricePlanXInfo.SUBSCRIPTION_TYPE, Long.valueOf(subscriptionType)));   
            }            
            final Collection<com.redknee.app.crm.bean.PricePlan> collection = HomeSupportHelper.get(ctx).getBeans(
                    ctx, 
                    com.redknee.app.crm.bean.PricePlan.class, 
                    condition, 
                    RmiApiSupport.isSortAscending(isAscending));            
            priceplans = CollectionSupportHelper.get(ctx).adaptCollection(
                    ctx, 
                    collection, 
                    this.pricePlanAdapter_, 
                    priceplans);
            if(provideAllVersions)
            {
               int i = 0;
               for (final com.redknee.app.crm.bean.PricePlan pp : collection)
               {
                   Collection<PricePlanVersion> ppvs = null;
                   try
                   {
                       ppvs = getCrmPricePlanVersions(ctx, pp.getId(), this);
                   }
                   catch (CRMExceptionFault crmEx)
                   {
                   }
                   priceplans[i++] = PricePlanToApiAdapter.adaptPricePlanToApi(ctx, pp, ppvs);
               }
            }
            else
            {
                int i = 0;
                for (final com.redknee.app.crm.bean.PricePlan pp : collection)
                {
                    PricePlanVersion ppv = null;
                    try
                    {
                        ppv = getCrmPricePlanVersion(ctx, pp.getId(), pp.getCurrentVersion(), this);
                    }
                    catch (CRMExceptionFault crmEx)
                    {
                    }
                    List<PricePlanVersion> ppvList  = null;
                    if (ppv != null)
                    {
                        ppvList = new ArrayList<PricePlanVersion>();
                        ppvList.add(ppv);
                    }                    
                    priceplans[i++] = PricePlanToApiAdapter.adaptPricePlanToApi(ctx, pp, ppvList);
                }
            }
        }
        catch (final Exception e)
        {
            final String msg = "Unable to retrieve Priceplans";
            RmiApiErrorHandlingSupport.handleQueryExceptions(ctx, e, msg, this);
        }
        return priceplans;
    }


    /**
     * {@inheritDoc}
     */
    public Service[] listDetailedServices(CRMRequestHeader header, int spid, Boolean isAscending, GenericParameter[] parameters)
            throws CRMExceptionFault
    {
        final Context ctx = getContext().createSubContext();
        // handle login. Failed logins throw exceptions
        RmiApiSupport.authenticateUser(ctx, header, "listDetailedServices",
                Constants.PERMISSION_SERVICESANDBUNDLES_READ_LISTDETAILEDSERVICES);
        Service[] services  = new Service[] {};
        try
        {
            final EQ condition = new EQ(ServiceXInfo.SPID, spid);
            final Collection<com.redknee.app.crm.bean.Service> collection = HomeSupportHelper.get(ctx).getBeans(
                    ctx, 
                    com.redknee.app.crm.bean.Service.class, 
                    condition, 
                    RmiApiSupport.isSortAscending(isAscending));            
            services = CollectionSupportHelper.get(ctx).adaptCollection(
                    ctx, 
                    collection, 
                    this.serviceAdapter_, 
                    services);        
        }
        catch (final Exception e)
        {
            final String msg = "Unable to retrieve Services";
            RmiApiErrorHandlingSupport.handleQueryExceptions(ctx, e, msg, this);
        }
        return services;
    }


    /**
     * {@inheritDoc}
     */
    public PricePlanDependencyGroup[] listPricePlanDependencyGroups(CRMRequestHeader header, int spid,
            Boolean isAscending, GenericParameter[] parameters) throws CRMExceptionFault
    {
        final Context ctx = getContext().createSubContext();
        // handle login. Failed logins throw exceptions
        RmiApiSupport.authenticateUser(ctx, header, "listPricePlanDependencyGroups",
                Constants.PERMISSION_SERVICESANDBUNDLES_READ_LISTPRICEPLANDEPENDENCYGROUPS);
        PricePlanDependencyGroup[] priceplanDependencyGroups  = new PricePlanDependencyGroup[] {};
        try
        {
            final EQ condition = new EQ(DependencyGroupXInfo.SPID, spid);
            final Collection<com.redknee.app.crm.bean.DependencyGroup> collection = HomeSupportHelper.get(ctx).getBeans(
                    ctx, 
                    com.redknee.app.crm.bean.DependencyGroup.class, 
                    condition, 
                    RmiApiSupport.isSortAscending(isAscending));            
            priceplanDependencyGroups = CollectionSupportHelper.get(ctx).adaptCollection(
                    ctx, 
                    collection, 
                    this.dependencyGroupAdapter_, 
                    priceplanDependencyGroups);        
        }
        catch (final Exception e)
        {
            final String msg = "Unable to retrieve Priceplan Dependency Groups";
            RmiApiErrorHandlingSupport.handleQueryExceptions(ctx, e, msg, this);
        }
        return priceplanDependencyGroups;
    }


    /**
     * {@inheritDoc}
     */
    public PricePlanPrerequisiteGroup[] listPricePlanPrerequisiteGroups(CRMRequestHeader header, int spid,
            Boolean isAscending, GenericParameter[] parameters) throws CRMExceptionFault
    {
        final Context ctx = getContext().createSubContext();
        // handle login. Failed logins throw exceptions
        RmiApiSupport.authenticateUser(ctx, header, "listPricePlanPrerequisiteGroups",
                Constants.PERMISSION_SERVICESANDBUNDLES_READ_LISTPRICEPLANPREREQUISITEGROUPS);
        PricePlanPrerequisiteGroup[] priceplanPrerequisiteGroups  = new PricePlanPrerequisiteGroup[] {};
        try
        {
            final EQ condition = new EQ(PrerequisiteGroupXInfo.SPID, spid);
            final Collection<com.redknee.app.crm.bean.PrerequisiteGroup> collection = HomeSupportHelper.get(ctx).getBeans(
                    ctx, 
                    com.redknee.app.crm.bean.PrerequisiteGroup.class, 
                    condition, 
                    RmiApiSupport.isSortAscending(isAscending));            
            priceplanPrerequisiteGroups = CollectionSupportHelper.get(ctx).adaptCollection(
                    ctx, 
                    collection, 
                    this.prerequisiteGroupAdapter_, 
                    priceplanPrerequisiteGroups);        
        }
        catch (final Exception e)
        {
            final String msg = "Unable to retrieve Priceplan Prerequisite Groups";
            RmiApiErrorHandlingSupport.handleQueryExceptions(ctx, e, msg, this);
        }
        return priceplanPrerequisiteGroups;
    }


    /**
     * {@inheritDoc}
     */
    public PricePlanValidationGroup[] listPricePlanValidationGroups(CRMRequestHeader header, int spid,
            Boolean isAscending, GenericParameter[] parameters) throws CRMExceptionFault
    {
        final Context ctx = getContext().createSubContext();
        // handle login. Failed logins throw exceptions
        RmiApiSupport.authenticateUser(ctx, header, "listPricePlanValidationGroups",
                Constants.PERMISSION_SERVICESANDBUNDLES_READ_LISTPRICEPLANVALIDATIONGROUPS);
        PricePlanValidationGroup[] priceplanValidationGroups  = new PricePlanValidationGroup[] {};
        try
        {
            final EQ condition = new EQ(PricePlanGroupXInfo.SPID, spid);
            final Collection<PricePlanGroup> collection = HomeSupportHelper.get(ctx).getBeans(
                    ctx, 
                    PricePlanGroup.class, 
                    condition, 
                    RmiApiSupport.isSortAscending(isAscending));            
            priceplanValidationGroups = CollectionSupportHelper.get(ctx).adaptCollection(
                    ctx, 
                    collection, 
                    this.priceplangroupAdapter_, 
                    priceplanValidationGroups);        
        }
        catch (final Exception e)
        {
            final String msg = "Unable to retrieve Priceplan Validation Groups";
            RmiApiErrorHandlingSupport.handleQueryExceptions(ctx, e, msg, this);
        }
        return priceplanValidationGroups;
    }


    /**
     * {@inheritDoc}
     */
    public ServiceReference[] listServices(CRMRequestHeader header, int spid, Boolean isAscending, GenericParameter[] parameters)
            throws CRMExceptionFault
    {
        final Context ctx = getContext().createSubContext();
        // handle login. Failed logins throw exceptions
        RmiApiSupport.authenticateUser(ctx, header, "listServices",
                Constants.PERMISSION_SERVICESANDBUNDLES_READ_LISTSERVICES);
        ServiceReference[] services = new ServiceReference[]
            {};
        try
        {
            final EQ condition = new EQ(ServiceXInfo.SPID, spid);
            final Collection<com.redknee.app.crm.bean.Service> collection = HomeSupportHelper.get(ctx).getBeans(
                    ctx, 
                    com.redknee.app.crm.bean.Service.class, 
                    condition, 
                    RmiApiSupport.isSortAscending(isAscending));            
            services = CollectionSupportHelper.get(ctx).adaptCollection(
                    ctx, 
                    collection, 
                    this.serviceReferenceAdapter_, 
                    services);        
        }
        catch (final Exception e)
        {
            final String msg = "Unable to retrieve Services";
            RmiApiErrorHandlingSupport.handleQueryExceptions(ctx, e, msg, this);
        }
        return services;
    }


    /**
     * {@inheritDoc}
     */
    public AuxiliaryServiceModificationResult updateAuxiliaryService(CRMRequestHeader header,
            AuxiliaryService auxiliaryService, GenericParameter[] parameters) throws CRMExceptionFault
    {
        final Context ctx = getContext().createSubContext();
        // handle login. Failed logins throw exceptions
        RmiApiSupport.authenticateUser(ctx, header, "updateAuxiliaryService",
                Constants.PERMISSION_SERVICESANDBUNDLES_WRITE_UPDATEAUXILIARYSERVICE);
        RmiApiErrorHandlingSupport.validateMandatoryObject(auxiliaryService, "auxiliaryService");
        AuxiliaryServiceModificationResult auxiliaryServiceModification  = new AuxiliaryServiceModificationResult();
        try
        {
            Home home = RmiApiSupport.getCrmHome(ctx, com.redknee.app.crm.bean.AuxiliaryServiceHome.class,
                    ServicesAndBundlesImpl.class);
            home = ApiSupport.injectAPIUpdateEntityHomeIntoPipeline(ctx, home);
            com.redknee.app.crm.bean.core.AuxiliaryService findBean = HomeSupportHelper.get(ctx).findBean(ctx,
                    com.redknee.app.crm.bean.core.AuxiliaryService.class, auxiliaryService.getIdentifier());
            if (findBean == null)
            {
                RmiApiErrorHandlingSupport.simpleValidation("auxiliaryService", "Auxiliary Service does not exist "
                        + auxiliaryService.getIdentifier() + " .");
            }
            if (auxiliaryService.getSpid() != null && findBean.getSpid() != auxiliaryService.getSpid().intValue())
            {
                RmiApiErrorHandlingSupport.simpleValidation("auxiliaryService",
                        "Spid update is not allowed for Auxiliary Service " + auxiliaryService.getIdentifier()
                                + " to Spid : " + auxiliaryService.getSpid());
            }
            com.redknee.app.crm.bean.core.AuxiliaryService originalBean = (com.redknee.app.crm.bean.core.AuxiliaryService) findBean
                    .clone();
            com.redknee.app.crm.bean.core.AuxiliaryService crmBean = (com.redknee.app.crm.bean.core.AuxiliaryService) AuxiliaryServiceToApiAdapter
                    .adaptApiToAuxiliaryService(ctx, auxiliaryService, findBean);
            if (auxiliaryService.getParameters() != null)
            {
                AuxiliaryServiceToApiAdapter.adaptGenericParametersToUpdateAuxiliaryService(ctx, 
                        auxiliaryService.getParameters(), crmBean);
            }            
            if (originalBean.getTechnology().getIndex() != crmBean.getTechnology().getIndex())
            {
                RmiApiErrorHandlingSupport.simpleValidation("auxiliaryService",
                        "Technology update is not allowed for Auxiliary Service " + auxiliaryService.getIdentifier()
                                + " to Technology : " + auxiliaryService.getTechnology());
            }
            if (originalBean.getType().getIndex() != crmBean.getType().getIndex())
            {
                RmiApiErrorHandlingSupport.simpleValidation("auxiliaryService",
                        "Type update is not allowed for Auxiliary Service " + auxiliaryService.getIdentifier()
                                + " to Type : " + auxiliaryService.getType());
            }
            if (crmBean.getType().getIndex() == AuxiliaryServiceTypeEnum.CallingGroup_INDEX)
            {
                long originalCallingGroupIdentifier = CallingGroupAuxSvcExtension.DEFAULT_CALLINGGROUPIDENTIFIER;
                com.redknee.app.crm.bean.CallingGroupTypeEnum originalCallingGroupType = CallingGroupAuxSvcExtension.DEFAULT_CALLINGGROUPTYPE;
                CallingGroupAuxSvcExtension originalCallingGroupAuxSvcExtension = ExtensionSupportHelper.get(ctx).getExtension(ctx, originalBean, CallingGroupAuxSvcExtension.class);
                if (originalCallingGroupAuxSvcExtension!=null)
                {
                    originalCallingGroupIdentifier = originalCallingGroupAuxSvcExtension.getCallingGroupIdentifier();
                    originalCallingGroupType = originalCallingGroupAuxSvcExtension.getCallingGroupType();
                }
                else
                {
                    LogSupport.minor(ctx, this,
                            "Unable to find required extension of type '" + CallingGroupAuxSvcExtension.class.getSimpleName()
                                    + "' for auxiliary service " + originalBean.getIdentifier());
                }
                
                long crmBeanCallingGroupIdentifier = CallingGroupAuxSvcExtension.DEFAULT_CALLINGGROUPIDENTIFIER;
                com.redknee.app.crm.bean.CallingGroupTypeEnum crmBeanCallingGroupType = CallingGroupAuxSvcExtension.DEFAULT_CALLINGGROUPTYPE;
                CallingGroupAuxSvcExtension crmBeanCallingGroupAuxSvcExtension = ExtensionSupportHelper.get(ctx).getExtension(ctx, crmBean, CallingGroupAuxSvcExtension.class);
                if (crmBeanCallingGroupAuxSvcExtension!=null)
                {
                    crmBeanCallingGroupIdentifier = crmBeanCallingGroupAuxSvcExtension.getCallingGroupIdentifier();
                    crmBeanCallingGroupType = crmBeanCallingGroupAuxSvcExtension.getCallingGroupType();
                }
                else
                {
                    LogSupport.minor(ctx, this,
                            "Unable to find required extension of type '" + AddMsisdnAuxSvcExtension.class.getSimpleName()
                                    + "' for auxiliary service " + crmBean.getIdentifier());
                }

                if (originalCallingGroupType.getIndex() != crmBeanCallingGroupType.getIndex())
                {
                    RmiApiErrorHandlingSupport.simpleValidation(
                            "auxiliaryService",
                            "Calling Group Type update is not allowed for Auxiliary Service "
                                    + auxiliaryService.getIdentifier() + " to CallingGroupType : "
                                    + auxiliaryService.getCallingGroupType());
                }
                if (originalCallingGroupIdentifier != crmBeanCallingGroupIdentifier)
                {
                    RmiApiErrorHandlingSupport.simpleValidation(
                            "auxiliaryService",
                            "Calling Group ID update is not allowed for Auxiliary Service "
                                    + auxiliaryService.getIdentifier() + " to CallinGroupID : "
                                    + auxiliaryService.getCallingGroupID());
                }                
            }            
            if (crmBean.getType().getIndex() == AuxiliaryServiceTypeEnum.AdditionalMsisdn_INDEX)
            {
                String originalBearer = null;
                String crmBearer = null;
                AddMsisdnAuxSvcExtension originalExtension = ExtensionSupportHelper.get(ctx).getExtension(ctx, originalBean, AddMsisdnAuxSvcExtension.class);
                if (originalExtension!=null)
                {
                    originalBearer = originalExtension.getBearerType();
                }
                else
                {
                    LogSupport.minor(ctx, this,
                            "Unable to find required extension of type '" + AddMsisdnAuxSvcExtension.class.getSimpleName()
                                    + "' for auxiliary service " + originalBean.getIdentifier());
                }

                AddMsisdnAuxSvcExtension crmExtension = ExtensionSupportHelper.get(ctx).getExtension(ctx, crmBean, AddMsisdnAuxSvcExtension.class);
                if (crmExtension!=null)
                {
                    crmBearer = originalExtension.getBearerType();
                }
                else
                {
                    LogSupport.minor(ctx, this,
                            "Unable to find required extension of type '" + AddMsisdnAuxSvcExtension.class.getSimpleName()
                                    + "' for auxiliary service " + crmBean.getIdentifier());
                }

                if (originalBearer != crmBearer)
                {
                    RmiApiErrorHandlingSupport.simpleValidation(
                            "auxiliaryService",
                            "BearerType update is not allowed for Auxiliary Service "
                                    + auxiliaryService.getIdentifier() + " to Type : " + auxiliaryService.getType());
                }
            }
            if (crmBean.getType().getIndex() == AuxiliaryServiceTypeEnum.Vpn_INDEX)
            {
                Long originalVPNPricePlan = null;
                Long crmBeanVPNPricePlan = null;
                VPNAuxSvcExtension originalVpnAuxSvcExtension = ExtensionSupportHelper.get(ctx).getExtension(ctx, originalBean, VPNAuxSvcExtension.class);
                if (originalVpnAuxSvcExtension!=null)
                {
                    originalVPNPricePlan = originalVpnAuxSvcExtension.getVpnPricePlan();
                }
                else
                {
                    LogSupport.minor(ctx, this,
                            "Unable to find required extension of type '" + VPNAuxSvcExtension.class.getSimpleName()
                                    + "' for auxiliary service " + originalBean.getIdentifier());
                }
                
                VPNAuxSvcExtension crmBeanVpnAuxSvcExtension = ExtensionSupportHelper.get(ctx).getExtension(ctx, crmBean, VPNAuxSvcExtension.class);
                if (crmBeanVpnAuxSvcExtension!=null)
                {
                    crmBeanVPNPricePlan = crmBeanVpnAuxSvcExtension.getVpnPricePlan();
                }
                else
                {
                    LogSupport.minor(ctx, this,
                            "Unable to find required extension of type '" + VPNAuxSvcExtension.class.getSimpleName()
                                    + "' for auxiliary service " + crmBean.getIdentifier());
                }

                if (originalVPNPricePlan!= crmBeanVPNPricePlan)
                {
                    RmiApiErrorHandlingSupport.simpleValidation(
                            "auxiliaryService",
                            "VpnPricePlan update is not allowed for Auxiliary Service "
                                    + auxiliaryService.getIdentifier() + " to VpnPricePlan : "
                                    + crmBeanVPNPricePlan);
                }
            }
            if (crmBean.getType().getIndex() == AuxiliaryServiceTypeEnum.URCS_Promotion_INDEX)
            {
                long originalServiceOption = URCSPromotionAuxSvcExtension.DEFAULT_SERVICEOPTION;
                URCSPromotionAuxSvcExtension originalUrcsPromotionAuxSvcExtension = ExtensionSupportHelper.get(ctx).getExtension(ctx, originalBean, URCSPromotionAuxSvcExtension.class);
                if (originalUrcsPromotionAuxSvcExtension!=null)
                {
                    originalServiceOption = originalUrcsPromotionAuxSvcExtension.getServiceOption();
                }
                else
                {
                    LogSupport.minor(ctx, this,
                            "Unable to find required extension of type '" + URCSPromotionAuxSvcExtension.class.getSimpleName()
                                    + "' for auxiliary service " + originalBean.getIdentifier());
                }

                long newServiceOption = URCSPromotionAuxSvcExtension.DEFAULT_SERVICEOPTION;
                URCSPromotionAuxSvcExtension newUrcsPromotionAuxSvcExtension = ExtensionSupportHelper.get(ctx).getExtension(ctx, crmBean, URCSPromotionAuxSvcExtension.class);
                if (newUrcsPromotionAuxSvcExtension!=null)
                {
                    newServiceOption = newUrcsPromotionAuxSvcExtension.getServiceOption();
                }
                else
                {
                    LogSupport.minor(ctx, this,
                            "Unable to find required extension of type '" + URCSPromotionAuxSvcExtension.class.getSimpleName()
                                    + "' for auxiliary service " + crmBean.getIdentifier());
                }

                if (originalServiceOption != newServiceOption)
                {
                    RmiApiErrorHandlingSupport.simpleValidation(
                            "auxiliaryService",
                            "ServiceOption update is not allowed for Auxiliary Service "
                                    + auxiliaryService.getIdentifier() + " to ServiceOption : "
                                    + auxiliaryService.getServiceOption());
                }
            }
            if (crmBean.getChargingModeType().getIndex() == ServicePeriodEnum.ONE_TIME_INDEX)
                    
            {
                if (crmBean.getRecurrenceType().getIndex() != originalBean.getRecurrenceType().getIndex())
                {
                    RmiApiErrorHandlingSupport.simpleValidation("auxiliaryService",
                            "Recurrence Type update is not allowed for Service " + auxiliaryService.getIdentifier()
                                    + " .");
                }
                if (crmBean.getRecurrenceType().getIndex() == OneTimeTypeEnum.ONE_OFF_FIXED_DATE_RANGE_INDEX)
                {
                    if (crmBean.getStartDate() != null && originalBean.getStartDate() != null
                            && !crmBean.getStartDate().equals(originalBean.getStartDate()))
                    {
                        RmiApiErrorHandlingSupport.simpleValidation("auxiliaryService",
                                "Start update is not allowed for Aux Service " + auxiliaryService.getIdentifier() + " .");
                    }
                    if (crmBean.getEndDate() != null && originalBean.getEndDate() != null
                            && !crmBean.getEndDate().equals(originalBean.getEndDate()))
                    {
                        RmiApiErrorHandlingSupport.simpleValidation("auxiliaryService", "End update is not allowed for Aux Service "
                                + auxiliaryService.getIdentifier() + " .");
                    }
                }
                if (crmBean.getRecurrenceType().getIndex() == OneTimeTypeEnum.ONE_OFF_FIXED_INTERVAL_INDEX)
                {
                    if (crmBean.getValidity() != originalBean.getValidity())
                    {
                        RmiApiErrorHandlingSupport.simpleValidation(
                                "auxiliaryService",
                                "Fixed Interval update is not allowed for Aux Service "
                                        + auxiliaryService.getIdentifier() + " .");
                    }
                    if (crmBean.getFixedInterval().getIndex() != originalBean.getFixedInterval().getIndex())
                    {
                        RmiApiErrorHandlingSupport.simpleValidation(
                                "auxiliaryService",
                                "Fixed Interval update is not allowed for Aux Service "
                                        + auxiliaryService.getIdentifier() + " .");
                    }
                }
            }
            
            if (crmBean.getChargingModeType().getIndex() == ServicePeriodEnum.MULTIMONTHLY_INDEX
                    && crmBean.getRecurrenceInterval() != originalBean.getRecurrenceInterval())
            {
                RmiApiErrorHandlingSupport.simpleValidation("auxiliaryService",
                        "Recurrence Interval update is not allowed for Aux Service " + auxiliaryService.getIdentifier() + " .");
            } 
            
            if (crmBean.getChargingModeType().getIndex() == ServicePeriodEnum.MULTIDAY_INDEX
                    && crmBean.getRecurrenceInterval() != originalBean.getRecurrenceInterval())
            {
                RmiApiErrorHandlingSupport.simpleValidation("auxiliaryService",
                        "Recurrence Interval update is not allowed for Aux Service " + auxiliaryService.getIdentifier() + " .");
            } 
            
            if (crmBean.getType().getIndex() == AuxiliaryServiceTypeEnum.Voicemail_INDEX)
            {
                String vmPlanId = VoicemailAuxSvcExtension.DEFAULT_VMPLANID;
                VoicemailAuxSvcExtension voicemailAuxSvcExtension = ExtensionSupportHelper.get(ctx).getExtension(ctx, crmBean, VoicemailAuxSvcExtension.class);
                if (voicemailAuxSvcExtension!=null)
                {
                    vmPlanId = voicemailAuxSvcExtension.getVmPlanId();
                }
                else
                {
                    LogSupport.minor(ctx, this,
                            "Unable to find required extension of type '" + VoicemailAuxSvcExtension.class.getSimpleName()
                                    + "' for auxiliary service " + crmBean.getIdentifier());
                }
                RmiApiSupport.validateExistanceOfBeanForKey(ctx, VMPlan.class,
                        new EQ(VMPlanXInfo.VM_PLAN_ID, vmPlanId));
            }
            if (crmBean.getType().getIndex() == AuxiliaryServiceTypeEnum.SERVICE_PROVISIONING_GATEWAY_INDEX)
            {
                SPGAuxSvcExtension spgAuxSvcExtension = ExtensionSupportHelper.get(ctx).getExtension(ctx, crmBean, SPGAuxSvcExtension.class);
                if (spgAuxSvcExtension!=null)
                {
                    RmiApiSupport.validateExistanceOfBeanForKey(ctx, SPGService.class,
                            new EQ(SPGServiceXInfo.ID, spgAuxSvcExtension.getSPGServiceType()));
                }
                else
                {
                    LogSupport.minor(ctx, this,
                            "Unable to find required extension of type '" + SPGAuxSvcExtension.class.getSimpleName()
                                    + "' for auxiliary service " + crmBean.getIdentifier());
                }
            }            
            Object obj = home.store(crmBean);
            auxiliaryServiceModification.setAuxiliaryService((AuxiliaryService) auxiliaryServiceAdapter_.adapt(ctx, obj));
            auxiliaryServiceModification.setParameters(parameters);        
        }
        catch (final Exception e)
        {
            final String msg = "Unable to update Auxiliary Service";
            RmiApiErrorHandlingSupport.handleQueryExceptions(ctx, e, msg, this);
        }
        return auxiliaryServiceModification;
    }


    /**
     * {@inheritDoc}
     */
    public BundleModificationResult updateBundle(CRMRequestHeader header, Bundle bundle,
            GenericParameter[] parameters) throws CRMExceptionFault
    {
        final Context ctx = getContext().createSubContext();
        // handle login. Failed logins throw exceptions
        RmiApiSupport.authenticateUser(ctx, header, "updateBundle",
                Constants.PERMISSION_SERVICESANDBUNDLES_WRITE_UPDATEBUNDLE);
        RmiApiErrorHandlingSupport.validateMandatoryObject(bundle, "bundle");
        BundleModificationResult bundleModification  = new BundleModificationResult();
        GenericParameterParser bundleParameterParser = new GenericParameterParser(bundle.getParameters());
        try
        {
            Home home = RmiApiSupport.getCrmHome(ctx, com.redknee.app.crm.bean.ui.BundleProfileHome.class, ServicesAndBundlesImpl.class);
            home = ApiSupport.injectAPIUpdateEntityHomeIntoPipeline(ctx, home);
            com.redknee.app.crm.bean.ui.BundleProfile findBean = HomeSupportHelper.get(ctx).findBean(ctx,
                    com.redknee.app.crm.bean.ui.BundleProfile.class, bundle.getIdentifier());
            if (findBean == null)
            {
                RmiApiErrorHandlingSupport.simpleValidation("bundle", "Bundle does not exist " + bundle.getIdentifier()
                        + " .");
            }
            if (bundle.getSpid() != null && findBean.getSpid() != bundle.getSpid().intValue())
            {
                RmiApiErrorHandlingSupport.simpleValidation("bundle",
                        "Spid update is not allowed for Bundle " + bundle.getIdentifier() + " to spid : " + bundle.getSpid());
            }
            com.redknee.app.crm.bean.ui.BundleProfile originalBean = (com.redknee.app.crm.bean.ui.BundleProfile) findBean.clone();
            com.redknee.app.crm.bean.ui.BundleProfile crmBean = (com.redknee.app.crm.bean.ui.BundleProfile) BundleProfileToApiAdapter
                    .adaptApiToBundleProfile(ctx, bundle, findBean, null);            
            if (originalBean.getSegment().getIndex() != crmBean.getSegment().getIndex())
            {
                RmiApiErrorHandlingSupport.simpleValidation("bundle",
                        "Segment update is not allowed for Bundle " + bundle.getIdentifier() + " .");
            }
            if (originalBean.getAuxiliary() != crmBean.getAuxiliary())
            {
                RmiApiErrorHandlingSupport.simpleValidation("bundle",
                        "Auxiliary update is not allowed for Bundle " + bundle.getIdentifier() + " .");
            }
            if (originalBean.getType() != crmBean.getType())
            {
                RmiApiErrorHandlingSupport.simpleValidation("bundle",
                        "Type update is not allowed for Bundle " + bundle.getIdentifier() + " .");
            }
            if (originalBean.isCurrency() != crmBean.isCurrency())
            {
                RmiApiErrorHandlingSupport.simpleValidation("bundle",
                        "Currency update is not allowed for Bundle " + bundle.getIdentifier() + " .");
            }
            if (originalBean.getQuotaScheme().getIndex() != crmBean.getQuotaScheme().getIndex())
            {
                RmiApiErrorHandlingSupport.simpleValidation("bundle",
                        "QuotaScheme update is not allowed for Bundle " + bundle.getIdentifier() + " .");
            }
            if (originalBean.getActivationScheme().getIndex() != crmBean.getActivationScheme().getIndex())
            {
                RmiApiErrorHandlingSupport.simpleValidation("bundle",
                        "ActivationScheme update is not allowed for Bundle " + bundle.getIdentifier() + " .");
            }
            if (originalBean.getExpiryScheme().getIndex() != crmBean.getExpiryScheme().getIndex())
            {
                RmiApiErrorHandlingSupport.simpleValidation("bundle",
                        "ExpiryScheme update is not allowed for Bundle " + bundle.getIdentifier() + " .");
            }
            if (originalBean.getRecurrenceScheme().getIndex() != crmBean.getRecurrenceScheme().getIndex())
            {
                RmiApiErrorHandlingSupport.simpleValidation("bundle",
                        "RecurrenceScheme update is not allowed for Bundle " + bundle.getIdentifier() + " .");
            }
            if (crmBean.getRecurrenceScheme().getIndex() == com.redknee.app.crm.bundle.RecurrenceTypeEnum.ONE_OFF_FIXED_DATE_RANGE_INDEX)
            {
                if (crmBean.getStartDate() != null && originalBean.getStartDate() != null
                        && !crmBean.getStartDate().equals(originalBean.getStartDate()))
                {
                    RmiApiErrorHandlingSupport.simpleValidation("bundle",
                            "Start Date update is not allowed for Bundle " + bundle.getIdentifier() + " .");
                }
                if (crmBean.getEndDate() != null && originalBean.getEndDate() != null
                        && !crmBean.getEndDate().equals(originalBean.getEndDate()))
                {
                    RmiApiErrorHandlingSupport.simpleValidation("bundle", "End Date update is not allowed for Bundle "
                            + bundle.getIdentifier() + " .");
                }
            }            
            if (crmBean.getRecurrenceScheme().getIndex() == com.redknee.app.crm.bundle.RecurrenceTypeEnum.ONE_OFF_FIXED_INTERVAL_INDEX)
            {
                if (originalBean.getValidity() != crmBean.getValidity())
                {
                    RmiApiErrorHandlingSupport.simpleValidation("bundle", "Validty update is not allowed for Bundle "
                            + bundle.getIdentifier() + " .");
                }
                if(originalBean.getInterval() != crmBean.getInterval())
                {
                    RmiApiErrorHandlingSupport.simpleValidation("bundle", "Interval update is not allowed for Bundle "
                            + bundle.getIdentifier() + " .");
                }
            }
            
            if ((bundle.getBundleCategories() == null || bundle.getBundleCategories().length == 0) && bundle.getBundleCategoryIDs()!=null)
            {
                List<BundleCategoryAssociation> associations = new ArrayList<BundleCategoryAssociation>();
                for (Long id : bundle.getBundleCategoryIDs())
                {
                    BundleCategoryAssociation association = new BundleCategoryAssociation();
                    association.setBundleCategoryID(id);
                    association.setRate((int) com.redknee.app.crm.bundle.BundleCategoryAssociation.DEFAULT_RATE);
                    associations.add(association);
                }
                bundle.setBundleCategories(associations.toArray(new BundleCategoryAssociation[]{}));
                
            }
            
            if (bundle.getBundleCategories() != null)
            {
                Collection<com.redknee.app.crm.bean.core.BundleCategoryAssociation> bundleCategoryAssociations = crmBean.getBundleCategoryIds().values();
                if (bundleCategoryAssociations != null)
                {
                    if (bundle.getBundleCategories().length != bundleCategoryAssociations.size())
                    {
                        RmiApiErrorHandlingSupport.simpleValidation("bundle",
                                "Bundle Category Id update is not allowed for Bundle " + bundle.getIdentifier() + " .");
                    }
                    for (BundleCategoryAssociation bundleCategory : bundle.getBundleCategories())
                    {
                        boolean found = false;
                        for (Object obj : bundleCategoryAssociations)
                        {
                            com.redknee.app.crm.bean.core.BundleCategoryAssociation bundleCategoryAssociation = (com.redknee.app.crm.bean.core.BundleCategoryAssociation) obj;
                            if (bundleCategoryAssociation.getCategoryId() == bundleCategory.getBundleCategoryID().intValue() &&
                                    bundleCategoryAssociation.getRate() == bundleCategory.getRate().longValue())
                            {
                                found = true;
                                break;
                            }
                        }
                        if (!found)
                        {
                            RmiApiErrorHandlingSupport.simpleValidation("bundle",
                                    "Bundle Category Id update is not allowed for Bundle " + bundle.getIdentifier()
                                            + " .");
                        }
                    }
                }
            }
            
            /*
             * 'Repurchasable' updates
             */
            Object paramValue = RmiApiSupport.getGenericParameterValue(Constants.GENERICPARAMETER_REPURCHASABLE, bundle.getParameters());
            if (paramValue != null)
            {
                crmBean.setRepurchasable(((Boolean)paramValue).booleanValue());
            }
            paramValue = RmiApiSupport.getGenericParameterValue(Constants.GENERICPARAMETER_REPURCHASE_EXPIRY_EXTENSION, bundle.getParameters());
            if (paramValue != null)
            {
                crmBean.setExpiryExtensionOnRepurchase(((Integer)paramValue).intValue());
            }
            RmiApiSupport.validateRepurchaseability(crmBean);
            
            /**
             * Set execution order 
             */
            paramValue = RmiApiSupport.getGenericParameterValue(Constants.GENERICPARAMETER_BUNDLE_EXECUTION_ORDER, bundle.getParameters());
            if (paramValue != null)
            {
                if(bundleParameterParser.containsParam(Constants.GENERICPARAMETER_BUNDLE_EXECUTION_ORDER))
                {
                    crmBean.setExecutionOrder(bundleParameterParser.getParameter(Constants.GENERICPARAMETER_BUNDLE_EXECUTION_ORDER, Long.class).intValue());
                }
            }
            Object obj = home.store(crmBean);
            bundleModification.setBundle((Bundle) auxiliarBundleAdapter_.adapt(ctx, bundleProfileUiToBundleProfileAdapter_.unAdapt(ctx, obj)));
            bundleModification.setParameters(parameters);
        }
        catch (final Exception e)
        {
            final String msg = "Unable to update Bundle";
            RmiApiErrorHandlingSupport.handleQueryExceptions(ctx, e, msg, this);
        }
        return bundleModification;
    }


    /**
     * {@inheritDoc}
     */
    public BundleCategoryModificationResult updateBundleCategory(CRMRequestHeader header,
            BundleCategory bundleCategory, GenericParameter[] parameters) throws CRMExceptionFault
    {
        final Context ctx = getContext().createSubContext();
        // handle login. Failed logins throw exceptions
        RmiApiSupport.authenticateUser(ctx, header, "updateBundleCategory",
                Constants.PERMISSION_SERVICESANDBUNDLES_WRITE_UPDATEBUNDLECATEGORY);
        RmiApiErrorHandlingSupport.validateMandatoryObject(bundleCategory, "bundleCategory");
        RmiApiErrorHandlingSupport.validateMandatoryObject(bundleCategory.getSpid(), "bundleCategory.spid");
        RmiApiErrorHandlingSupport.validateMandatoryObject(bundleCategory.getIdentifier(), "bundleCategory.identifier");
        RmiApiSupport.validateExistanceOfBeanForKey(ctx, CRMSpid.class, new EQ(CRMSpidXInfo.ID, bundleCategory.getSpid()));
        BundleCategoryModificationResult bundleCategoryModification  = new BundleCategoryModificationResult();
        try
        {
            Home home = RmiApiSupport.getCrmHome(ctx, com.redknee.app.crm.bundle.BundleCategoryHome.class,
                    ServicesAndBundlesImpl.class);
            home = ApiSupport.injectAPIUpdateEntityHomeIntoPipeline(ctx, home);
            com.redknee.app.crm.bundle.BundleCategory findBean = HomeSupportHelper.get(ctx).findBean(ctx,
                    com.redknee.app.crm.bundle.BundleCategory.class, bundleCategory.getIdentifier().intValue());
            if (findBean == null)
            {
                RmiApiErrorHandlingSupport.simpleValidation("bundleCategory", "Bundle Category does not exist "
                        + bundleCategory.getIdentifier() + " .");
            }
            com.redknee.app.crm.bundle.BundleCategory originalBean = (com.redknee.app.crm.bundle.BundleCategory) findBean.clone();
            if (bundleCategory.getSpid() != findBean.getSpid())
            {
                RmiApiErrorHandlingSupport.simpleValidation("bundleCategory",
                        "Spid update not allowed in Bundle Category " + bundleCategory.getIdentifier() + " .");
            }
            if (bundleCategory.getUnitType().getValue() != (long)findBean.getUnitType().getIndex())
            {
                RmiApiErrorHandlingSupport.simpleValidation("bundleCategory",
                        "Unit Type update not allowed in Bundle Category " + bundleCategory.getIdentifier() + " .");
            }           
            com.redknee.app.crm.bundle.BundleCategory crmBean = (com.redknee.app.crm.bundle.BundleCategory) BundleCategoryToApiAdapter
                    .adaptApiToBundleCategory(ctx, bundleCategory, findBean);
            Object obj = home.store(crmBean);
            bundleCategoryModification.setBundleCategory((BundleCategory) bundleCategoryAdapter_.adapt(ctx, obj));
        }
        catch (final Exception e)
        {
            final String msg = "Unable to update Bundle Category";
            RmiApiErrorHandlingSupport.handleQueryExceptions(ctx, e, msg, this);
        }
        return bundleCategoryModification;
    }


    /**
     * {@inheritDoc}
     */
    public BundleCategoryRatePlanAssociationModificationResult updateBundleCategoryRatePlanAssociation(
            CRMRequestHeader header, BundleCategoryRatePlanAssociation association, GenericParameter[] parameters)
            throws CRMExceptionFault
    {
        final Context ctx = getContext().createSubContext();
        // handle login. Failed logins throw exceptions
        RmiApiSupport.authenticateUser(ctx, header, "updateBundleCategoryRatePlanAssociation",
                Constants.PERMISSION_SERVICESANDBUNDLES_WRITE_UPDATEBUNDLECATEGORYRATEPLANASSOCIATION);
        RmiApiErrorHandlingSupport.validateMandatoryObject(association, "association");
        if (association.getSpid() != null)
        {
            RmiApiSupport.validateExistanceOfBeanForKey(ctx, CRMSpid.class,
                    new EQ(CRMSpidXInfo.ID, association.getSpid()));
        }
        BundleCategoryRatePlanAssociationModificationResult modificationResult  = new BundleCategoryRatePlanAssociationModificationResult();
        try
        {            
            Home home = RmiApiSupport.getCrmHome(ctx, RatePlanAssociationHome.class,
                    ServicesAndBundlesImpl.class);
            home = ApiSupport.injectAPIUpdateEntityHomeIntoPipeline(ctx, home);
            RatePlanAssociation findBean = HomeSupportHelper.get(ctx).findBean(ctx,
                    RatePlanAssociation.class, association.getIdentifier());
            if (findBean == null)
            {
                RmiApiErrorHandlingSupport.simpleValidation("association", "RatePlan Association does not exist "
                        + association.getIdentifier() + " .");
            }            
            RatePlanAssociation crmBean = (RatePlanAssociation) RatePlanAssociationToApiAdapter
                    .adaptApiToRatePlanAssociation(ctx, association, findBean);
            if (association.getSmsRatePlan() != null)
            {
                Home smsRatePlanHome = (Home) ctx.get(ModelCrmConstants.RATE_PLAN_SMS_HOME_KEY);
                RatePlan rtp = (RatePlan) smsRatePlanHome.find(ctx, crmBean.getSmsRatePlan());
                if (rtp == null)
                {
                    RmiApiErrorHandlingSupport.simpleValidation("association",
                            "Sms Rate Plan Not found " + association.getSmsRatePlan() + " .");
                }
                if (rtp.getSpid() != crmBean.getSpid())
                {
                    RmiApiErrorHandlingSupport.simpleValidation("association", "Invalid Spid of Sms Rate Plan"
                            + association.getSmsRatePlan() + " spid : " + rtp.getSpid());
                }
            }
            if (association.getVoiceRatePlan() != null)
            {
                Home voiceRatePlanHome = (Home) ctx.get(ModelCrmConstants.RATE_PLAN_VOICE_HOME_KEY);
                RatePlan rtp = (RatePlan) voiceRatePlanHome.find(ctx, crmBean.getVoiceRatePlan());
                if (rtp == null)
                {
                    RmiApiErrorHandlingSupport.simpleValidation("pricePlan",
                            "Voice Rate Plan Not found " + association.getVoiceRatePlan() + " .");
                }
                if (rtp.getSpid() != crmBean.getSpid())
                {
                    RmiApiErrorHandlingSupport.simpleValidation("pricePlan", "Invalid spid of Voice Rate Plan"
                            + association.getVoiceRatePlan() + " spid : " + rtp.getSpid());
                }
            }
            Object obj = home.store(crmBean);
            modificationResult.setAssociation((BundleCategoryRatePlanAssociation) ratePlanAssociationAdapter_.adapt(ctx, obj));  
        }
        catch (final Exception e)
        {
            final String msg = "Unable to update Bundle Category RatePlan Association";
            RmiApiErrorHandlingSupport.handleQueryExceptions(ctx, e, msg, this);
        }
        return modificationResult;
    }


    /**
     * {@inheritDoc}
     */
    public PricePlanModificationResult updatePricePlan(CRMRequestHeader header, PricePlan pricePlan,
            GenericParameter[] parameters) throws CRMExceptionFault
    {
        final Context ctx = getContext().createSubContext();
        // handle login. Failed logins throw exceptions
        RmiApiSupport.authenticateUser(ctx, header, "updatePricePlan",
                Constants.PERMISSION_SERVICESANDBUNDLES_WRITE_UPDATEPRICEPLAN);
        RmiApiErrorHandlingSupport.validateMandatoryObject(pricePlan, "pricePlan");
        if(pricePlan.getCriteria()!= null && pricePlan.getCriteria()[0]!= null)
        {
            RmiApiErrorHandlingSupport.validateMandatoryObject(pricePlan.getCriteria()[0].getPricePlanCriteriaChoice_type0(), "pricePlan.criteria[0].PricePlanCriteriaChoice_type0");
            RmiApiErrorHandlingSupport.validateMandatoryObject(pricePlan.getCriteria()[0].getPricePlanCriteriaChoice_type0().getPricePlanCriteriaSequence_type0(), "pricePlan.criteria[0].pricePlanCriteriaChoice_type0.pricePlanCriteriaSequence_type0");
            RmiApiErrorHandlingSupport.validateMandatoryObject(pricePlan.getCriteria()[0].getPricePlanCriteriaChoice_type0().getPricePlanCriteriaSequence_type0().getContractDuration(), "pricePlan.criteria[0].pricePlanCriteriaChoice_type0.pricePlanCriteriaSequence_type0.contractDuration");
            RmiApiErrorHandlingSupport.validateMandatoryObject(pricePlan.getCriteria()[0].getPricePlanCriteriaChoice_type0().getPricePlanCriteriaSequence_type0().getContractDuration().getDurationFrequency(), "pricePlan.criteria[0].pricePlanCriteriaChoice_type0.pricePlanCriteriaSequence_type0.contractDuration.durationFrequency");            
        }
        if(pricePlan.getSubscriptionLevel() != null)
        {
            RmiApiSupport.validateExistanceOfBeanForKey(
                    ctx,
                    SubscriptionLevel.class,
                    new And().add(new EQ(SubscriptionLevelXInfo.SPID, pricePlan.getSpid())).add(
                            new EQ(SubscriptionLevelXInfo.ID, pricePlan.getSubscriptionLevel().longValue())));
        }        
        PricePlanModificationResult modificationResult  = new PricePlanModificationResult();
        try
        {
            Home home = RmiApiSupport.getCrmHome(ctx, PricePlanHome.class,
                    ServicesAndBundlesImpl.class);
            home = ApiSupport.injectAPIUpdateEntityHomeIntoPipeline(ctx, home);
            com.redknee.app.crm.bean.PricePlan findBean = HomeSupportHelper.get(ctx).findBean(ctx,
                    com.redknee.app.crm.bean.PricePlan.class, pricePlan.getIdentifier());
            if (findBean == null)
            {
                RmiApiErrorHandlingSupport.simpleValidation("pricePlan", "PricePlan does not exist "
                        + pricePlan.getIdentifier() + " .");
            }
            com.redknee.app.crm.bean.PricePlan originalBean = (com.redknee.app.crm.bean.PricePlan) findBean.clone();
            com.redknee.app.crm.bean.PricePlan crmBean = (com.redknee.app.crm.bean.PricePlan) PricePlanToApiAdapter
                    .adaptApiToPricePlan(ctx, pricePlan, findBean);
            if (originalBean.getSpid() != crmBean.getSpid())
            {
                RmiApiErrorHandlingSupport.simpleValidation("pricePlan", "Spid Update not allowed for Priceplan "
                        + pricePlan.getIdentifier() + " .");
            }
            if (pricePlan.getGroup() != null & pricePlan.getGroup().length != 0)
            {
                And and = new And();
                and.add(new EQ(PricePlanGroupXInfo.SPID, crmBean.getSpid()));
                and.add(new EQ(PricePlanGroupXInfo.IDENTIFIER, pricePlan.getGroup()[0]));
                RmiApiSupport.validateExistanceOfBeanForKey(ctx, PricePlanGroup.class, and);
            }
            if (pricePlan.getSmsRatePlan() != null && pricePlan.getSmsRatePlan().length != 0)
            {
                Home smsRatePlanHome = (Home) ctx.get(ModelCrmConstants.RATE_PLAN_SMS_HOME_KEY);
                RatePlan rtp = (RatePlan) smsRatePlanHome.find(ctx, pricePlan.getSmsRatePlan()[0]);                
                if (rtp == null)
                {
                    RmiApiErrorHandlingSupport.simpleValidation("pricePlan",
                            "Sms Rate Plan not Found " + pricePlan.getSmsRatePlan()[0] + " .");
                }
                if (rtp.getSpid() != pricePlan.getSpid())
                {
                    RmiApiErrorHandlingSupport.simpleValidation("pricePlan", "Invalid Spid of Sms Rate Plan "
                            + pricePlan.getSmsRatePlan()[0] + " spid : " + rtp.getSpid());
                }
            }
            if (pricePlan.getVoiceRatePlan() != null && pricePlan.getVoiceRatePlan().length != 0)
            {
                Home voiceRatePlanHome = (Home) ctx.get(ModelCrmConstants.RATE_PLAN_VOICE_HOME_KEY);
                RatePlan rtp = (RatePlan) voiceRatePlanHome.find(ctx, pricePlan.getVoiceRatePlan()[0]);
                if (rtp == null)
                {
                    RmiApiErrorHandlingSupport.simpleValidation("pricePlan",
                            "Voice Rate Plan not Found " + pricePlan.getVoiceRatePlan()[0] + " .");
                }
                if (rtp.getSpid() != pricePlan.getSpid())
                {
                    RmiApiErrorHandlingSupport.simpleValidation("pricePlan", "Invalid Spid of Voice Rate Plan "
                            + pricePlan.getVoiceRatePlan()[0] + " spid : " + rtp.getSpid());
                }
            }            
            if (originalBean.getSubscriptionType() != crmBean.getSubscriptionType())
            {
                RmiApiErrorHandlingSupport.simpleValidation("pricePlan",
                        "Subscription Type Update not allowed for Priceplan " + pricePlan.getIdentifier() + " .");
            }
            if (originalBean.getTechnology().getIndex() != crmBean.getTechnology().getIndex())
            {
                RmiApiErrorHandlingSupport.simpleValidation("pricePlan", "Technology Update not allowed for Priceplan "
                        + pricePlan.getIdentifier() + " .");
            }
            if (originalBean.getPricePlanType().getIndex() != crmBean.getPricePlanType().getIndex())
            {
                RmiApiErrorHandlingSupport.simpleValidation("pricePlan",
                        "Price Plan Type Update not allowed for Priceplan " + pricePlan.getIdentifier() + " .");
            }
            if (originalBean.getDataRatePlan() != crmBean.getDataRatePlan())
            {
                RmiApiErrorHandlingSupport.simpleValidation("pricePlan",
                        "Data Rate Plan Update not allowed for Priceplan " + pricePlan.getIdentifier() + " .");
            }            
            Object obj = home.store(crmBean);
            modificationResult.setPricePlan((PricePlan) pricePlanAdapter_.adapt(ctx, obj));
            modificationResult.setParameters(parameters);
        }
        catch (final Exception e)
        {
            final String msg = "Unable to update Priceplan";
            RmiApiErrorHandlingSupport.handleQueryExceptions(ctx, e, msg, this);
        }
        return modificationResult;
    }


    /**
     * {@inheritDoc}
     */
    public PricePlanDependencyGroupModificationResult updatePricePlanDependencyGroup(CRMRequestHeader header,
            PricePlanDependencyGroup dependencyGroup, GenericParameter[] parameters) throws CRMExceptionFault
    {
        final Context ctx = getContext().createSubContext();
        // handle login. Failed logins throw exceptions
        RmiApiSupport.authenticateUser(ctx, header, "updatePricePlanDependencyGroup",
                Constants.PERMISSION_SERVICESANDBUNDLES_WRITE_UPDATEPRICEPLANDEPENDENCYGROUP);
        RmiApiErrorHandlingSupport.validateMandatoryObject(dependencyGroup, "dependencyGroup");
        RmiApiErrorHandlingSupport.validateMandatoryObject(dependencyGroup.getSpid(), "dependencyGroup.spid");
        RmiApiErrorHandlingSupport.validateMandatoryObject(dependencyGroup.getType(), "dependencyGroup.type");
        RmiApiSupport.validateExistanceOfBeanForKey(ctx, CRMSpid.class, new EQ(CRMSpidXInfo.ID, dependencyGroup.getSpid()));
        PricePlanDependencyGroupModificationResult modificationResult  = new PricePlanDependencyGroupModificationResult();
        if (dependencyGroup.getAuxiliaryServiceIDs() != null)
        {
            for (long id : dependencyGroup.getAuxiliaryServiceIDs())
            {
                And and = new And();
                and.add(new EQ(AuxiliaryServiceXInfo.SPID, dependencyGroup.getSpid()));
                and.add(new EQ(AuxiliaryServiceXInfo.IDENTIFIER, id));
                RmiApiSupport.validateExistanceOfBeanForKey(ctx, com.redknee.app.crm.bean.core.AuxiliaryService.class, and);
            }
        }
        if (dependencyGroup.getServiceIDs() != null)
        {
            for (long id : dependencyGroup.getServiceIDs())
            {
                And and = new And();
                and.add(new EQ(ServiceXInfo.SPID, dependencyGroup.getSpid()));
                and.add(new EQ(ServiceXInfo.ID, id));
                RmiApiSupport.validateExistanceOfBeanForKey(ctx, com.redknee.app.crm.bean.Service.class, and);
            }
        }
        if (dependencyGroup.getBundleIDs() != null)
        {
            for (long id : dependencyGroup.getBundleIDs())
            {
                And and = new And();
                and.add(new EQ(BundleProfileXInfo.SPID, dependencyGroup.getSpid()));
                and.add(new EQ(BundleProfileXInfo.BUNDLE_ID, id));
                RmiApiSupport.validateExistanceOfBeanForKey(ctx, com.redknee.app.crm.bundle.BundleProfile.class, and);
            }
        }
        try
        {
            Home home = RmiApiSupport.getCrmHome(ctx, DependencyGroupHome.class,
                    ServicesAndBundlesImpl.class);
            home = ApiSupport.injectAPIUpdateEntityHomeIntoPipeline(ctx, home);
            DependencyGroup findBean = HomeSupportHelper.get(ctx).findBean(ctx,
                    DependencyGroup.class, dependencyGroup.getIdentifier());
            if (findBean == null)
            {
                RmiApiErrorHandlingSupport.simpleValidation("dependencyGroup", "Dependency Group does not exist "
                        + dependencyGroup.getIdentifier() + " .");
            }
            DependencyGroup crmBean = (DependencyGroup) DependencyGroupToApiAdapter.adaptApiToDependencyGroup(ctx,
                    dependencyGroup, findBean);
            Object obj = home.store(crmBean);
            modificationResult.setDependencyGroup((PricePlanDependencyGroup) dependencyGroupAdapter_.adapt(ctx, obj));
            modificationResult.setParameters(parameters);
        }
        catch (final Exception e)
        {
            final String msg = "Unable to update Priceplan Dependency Group";
            RmiApiErrorHandlingSupport.handleQueryExceptions(ctx, e, msg, this);
        }
        return modificationResult;
    }


    /**
     * {@inheritDoc}
     */
    public PricePlanPrerequisiteGroupModificationResult updatePricePlanPrerequisiteGroup(CRMRequestHeader header,
            PricePlanPrerequisiteGroup prerequisiteGroup, GenericParameter[] parameters) throws CRMExceptionFault
    {
        final Context ctx = getContext().createSubContext();
        // handle login. Failed logins throw exceptions
        RmiApiSupport.authenticateUser(ctx, header, "updatePricePlanPrerequisiteGroup",
                Constants.PERMISSION_SERVICESANDBUNDLES_WRITE_UPDATEPRICEPLANPREREQUISITEGROUP);
        RmiApiErrorHandlingSupport.validateMandatoryObject(prerequisiteGroup, "prerequisiteGroup");
        RmiApiErrorHandlingSupport.validateMandatoryObject(prerequisiteGroup.getSpid(), "prerequisiteGroup.spid");
        RmiApiSupport.validateExistanceOfBeanForKey(ctx, CRMSpid.class, new EQ(CRMSpidXInfo.ID, prerequisiteGroup.getSpid()));
        if (prerequisiteGroup.getDependencyPrerequisiteIDs() != null
                && prerequisiteGroup.getDependencyPrerequisiteIDs().length != 0)
        {
            if (prerequisiteGroup.getDependencyPrerequisiteIDs()[0] != null)
            {
                And and = new And();
                and.add(new EQ(DependencyGroupXInfo.SPID, prerequisiteGroup.getSpid()));
                and.add(new EQ(DependencyGroupXInfo.IDENTIFIER, prerequisiteGroup.getDependencyPrerequisiteIDs()[0]
                        .longValue()));
                RmiApiSupport.validateExistanceOfBeanForKey(ctx, DependencyGroup.class, and);
            }
        }
        if (prerequisiteGroup.getServicePrerequisiteIDs() != null
                && prerequisiteGroup.getServicePrerequisiteIDs().length != 0)
        {
            if (prerequisiteGroup.getServicePrerequisiteIDs()[0] != null)
            {
                And and = new And();
                and.add(new EQ(DependencyGroupXInfo.SPID, prerequisiteGroup.getSpid()));
                and.add(new EQ(DependencyGroupXInfo.IDENTIFIER, prerequisiteGroup.getServicePrerequisiteIDs()[0]
                        .longValue()));
                RmiApiSupport.validateExistanceOfBeanForKey(ctx, DependencyGroup.class, and);
            }
        }
        PricePlanPrerequisiteGroupModificationResult modificationResult  = new PricePlanPrerequisiteGroupModificationResult();
        try
        {
            Home home = RmiApiSupport.getCrmHome(ctx, PrerequisiteGroupHome.class,
                    ServicesAndBundlesImpl.class);
            home = ApiSupport.injectAPIUpdateEntityHomeIntoPipeline(ctx, home);
            PrerequisiteGroup findBean = HomeSupportHelper.get(ctx).findBean(ctx,
                    PrerequisiteGroup.class, prerequisiteGroup.getIdentifier());
            if (findBean == null)
            {
                RmiApiErrorHandlingSupport.simpleValidation("prerequisiteGroup", "Prerequisite Group does not exist "
                        + prerequisiteGroup.getIdentifier() + " .");
            }
            PrerequisiteGroup crmBean = (PrerequisiteGroup) PrerequisiteGroupToApiAdapter.adaptApiToPrerequisiteGroup(
                    ctx, prerequisiteGroup, findBean);
            Object obj = home.store(crmBean);
            modificationResult.setPrerequisiteGroup((PricePlanPrerequisiteGroup) prerequisiteGroupAdapter_.adapt(ctx, obj));
        }
        catch (final Exception e)
        {
            final String msg = "Unable to update Priceplan Prerequisite Group";
            RmiApiErrorHandlingSupport.handleQueryExceptions(ctx, e, msg, this);
        }
        return modificationResult;
    }


    /**
     * {@inheritDoc}
     */
    public PricePlanValidationGroupModificationResult updatePricePlanValidationGroup(CRMRequestHeader header,
            PricePlanValidationGroup validationGroup, GenericParameter[] parameters) throws CRMExceptionFault
    {
        final Context ctx = getContext().createSubContext();
        // handle login. Failed logins throw exceptions
        RmiApiSupport.authenticateUser(ctx, header, "updatePricePlanValidationGroup",
                Constants.PERMISSION_SERVICESANDBUNDLES_WRITE_UPDATEPRICEPLANVALIDATIONGROUP);
        RmiApiErrorHandlingSupport.validateMandatoryObject(validationGroup, "validationGroup");
        if (validationGroup.getSpid() != null)
        {
            RmiApiSupport.validateExistanceOfBeanForKey(ctx, CRMSpid.class,
                    new EQ(CRMSpidXInfo.ID, validationGroup.getSpid()));
        }          
        if (validationGroup.getDependencyIDs() != null)
        {            
            for (Long id : validationGroup.getDependencyIDs())
            {
                And and = new And();
                and.add(new EQ(DependencyGroupXInfo.SPID, validationGroup.getSpid()));
                and.add(new EQ(DependencyGroupXInfo.IDENTIFIER, id));
                RmiApiSupport.validateExistanceOfBeanForKey(ctx, DependencyGroup.class, and);
            }            
        }
        if (validationGroup.getPrerequisiteIDs() != null)
        {            
            for(Long id : validationGroup.getPrerequisiteIDs())
            {
                And and = new And();
                and.add(new EQ(PrerequisiteGroupXInfo.SPID, validationGroup.getSpid()));
                and.add(new EQ(PrerequisiteGroupXInfo.IDENTIFIER, id));
                RmiApiSupport.validateExistanceOfBeanForKey(ctx, PrerequisiteGroup.class, and);                
            }            
        }
        PricePlanValidationGroupModificationResult modificationResult  = new PricePlanValidationGroupModificationResult();
        try
        {
            Home home = RmiApiSupport.getCrmHome(ctx, PricePlanGroupHome.class,
                    ServicesAndBundlesImpl.class);
            home = ApiSupport.injectAPIUpdateEntityHomeIntoPipeline(ctx, home);
            PricePlanGroup findBean = HomeSupportHelper.get(ctx).findBean(ctx,
                    PricePlanGroup.class, validationGroup.getIdentifier());
            if (findBean == null)
            {
                RmiApiErrorHandlingSupport.simpleValidation("validationGroup", "Validation Group does not exist "
                        + validationGroup.getIdentifier() + " .");
            }            
            PricePlanGroup crmBean = (PricePlanGroup) PricePlanGroupToApiAdapter.adaptApiToPricePlanGroup(ctx,
                    validationGroup, findBean);
            if (validationGroup.getParentID() != null)
            {
                And and = new And();
                and.add(new EQ(PricePlanGroupXInfo.SPID, crmBean.getSpid()));
                and.add(new EQ(PricePlanGroupXInfo.IDENTIFIER, crmBean.getParentPPG()));
                RmiApiSupport.validateExistanceOfBeanForKey(ctx, PricePlanGroup.class, and);            
            }
            Object obj = home.store(crmBean);
            modificationResult.setValidationGroup((PricePlanValidationGroup) priceplangroupAdapter_.adapt(ctx, obj));
            modificationResult.setParameters(parameters);
        }
        catch (final Exception e)
        {
            final String msg = "Unable to update Priceplan Validation Group";
            RmiApiErrorHandlingSupport.handleQueryExceptions(ctx, e, msg, this);
        }
        return modificationResult;
    }


    /**
     * {@inheritDoc}
     */
    public ServiceModificationResult updateService(CRMRequestHeader header, Service service,
            GenericParameter[] parameters) throws CRMExceptionFault
    {
        final Context ctx = getContext().createSubContext();
        // handle login. Failed logins throw exceptions
        RmiApiSupport.authenticateUser(ctx, header, "updateService",
                Constants.PERMISSION_SERVICESANDBUNDLES_WRITE_UPDATESERVICE);
        RmiApiErrorHandlingSupport.validateMandatoryObject(service, "service");
        ServiceModificationResult modificationResult  = new ServiceModificationResult();
        try
        {
            Home home = RmiApiSupport.getCrmHome(ctx, com.redknee.app.crm.bean.ServiceHome.class,
                    ServicesAndBundlesImpl.class);
            home = ApiSupport.injectAPIUpdateEntityHomeIntoPipeline(ctx, home);
            com.redknee.app.crm.bean.Service findBean = HomeSupportHelper.get(ctx).findBean(ctx,
                    com.redknee.app.crm.bean.Service.class, service.getIdentifier());            
            if (findBean == null)
            {
                RmiApiErrorHandlingSupport.simpleValidation("service", "Service does not exist "
                        + service.getIdentifier() + " .");
            }            
            com.redknee.app.crm.bean.Service originalBean = (com.redknee.app.crm.bean.Service) findBean.clone();
            com.redknee.app.crm.bean.Service crmBean = (com.redknee.app.crm.bean.Service) ServiceToApiAdapter
                    .adaptApiToService(ctx, service, findBean);
            if (service.getParameters() != null)
            {
                ServiceToApiAdapter.adaptGenericParametersToUpdateService(service.getParameters(), crmBean);
            }
            if (originalBean.getSpid() != crmBean.getSpid())
            {
                RmiApiSupport.validateExistanceOfBeanForKey(
                        ctx,
                        GLCodeMapping.class,
                        new And().add(new EQ(GLCodeMappingXInfo.SPID, crmBean.getSpid())).add(
                                new EQ(GLCodeMappingXInfo.GL_CODE, crmBean.getAdjustmentGLCode())));
                RmiApiSupport.validateExistanceOfBeanForKey(
                        ctx,
                        TaxAuthority.class,
                        new And().add(new EQ(TaxAuthorityXInfo.SPID, crmBean.getSpid())).add(
                                new EQ(TaxAuthorityXInfo.TAX_ID, crmBean.getTaxAuthority())));
            }
            if (originalBean.getSubscriptionType() != crmBean.getSubscriptionType())
            {
                RmiApiErrorHandlingSupport.simpleValidation("service",
                        "SubscriptionType update is not allowed Service " + service.getIdentifier() + " .");
            }            
            if (originalBean.getTechnology().getIndex() != crmBean.getTechnology().getIndex())
            {
                RmiApiErrorHandlingSupport.simpleValidation("service",
                        "Technology update is not allowed for Service " + service.getIdentifier()
                                + " .");
            }
            if (originalBean.getChargeScheme().getIndex() != crmBean.getChargeScheme().getIndex())
            {
                RmiApiErrorHandlingSupport.simpleValidation(
                        "service",
                        "ChargeScheme update is not allowed for Service "
                                + service.getIdentifier() + " .");
            }
            if (originalBean.getType().getIndex() != crmBean.getType().getIndex())
            {
                RmiApiErrorHandlingSupport.simpleValidation("service",
                        "Type update is not allowed for Service " + service.getIdentifier() + " .");
            }
            if (crmBean.getChargeScheme().getIndex() == ServicePeriodEnum.ONE_TIME_INDEX)
            {
                if (crmBean.getRecurrenceType().getIndex() != originalBean.getRecurrenceType().getIndex())
                {
                    RmiApiErrorHandlingSupport.simpleValidation("service",
                            "Recurrence Type update is not allowed for Service " + service.getIdentifier() + " .");
                }
                if (crmBean.getRecurrenceType().getIndex() == OneTimeTypeEnum.ONE_OFF_FIXED_DATE_RANGE_INDEX)
                {
                    if (crmBean.getStartDate() != null && originalBean.getStartDate() != null
                            && !crmBean.getStartDate().equals(originalBean.getStartDate()))
                    {
                        RmiApiErrorHandlingSupport.simpleValidation("service",
                                "Start update is not allowed for Service " + service.getIdentifier() + " .");
                    }
                    if (crmBean.getEndDate() != null && originalBean.getEndDate() != null
                            && !crmBean.getEndDate().equals(originalBean.getEndDate()))
                    {
                        RmiApiErrorHandlingSupport.simpleValidation("service", "End update is not allowed for Service "
                                + service.getIdentifier() + " .");
                    }
                }
                if (crmBean.getRecurrenceType().getIndex() == OneTimeTypeEnum.ONE_OFF_FIXED_INTERVAL_INDEX)
                {
                    if (crmBean.getFixedInterval().getIndex() != originalBean.getFixedInterval().getIndex())
                    {
                        RmiApiErrorHandlingSupport.simpleValidation("service",
                                "Fixed Interval update is not allowed for Service " + service.getIdentifier() + " .");
                    }
                    if (crmBean.getValidity() != originalBean.getValidity())
                    {
                        RmiApiErrorHandlingSupport.simpleValidation("service",
                                "Validty update is not allowed for Service " + service.getIdentifier() + " .");
                    }
                }
            }
            
            if (crmBean.getChargeScheme().getIndex() == ServicePeriodEnum.MULTIMONTHLY_INDEX
                    && crmBean.getRecurrenceInterval() != originalBean.getRecurrenceInterval())
            {
                RmiApiErrorHandlingSupport.simpleValidation("service",
                        "Recurrence Interval update is not allowed for Service " + service.getIdentifier() + " .");
            }
            
            if (crmBean.getChargeScheme().getIndex() == ServicePeriodEnum.MULTIDAY_INDEX
                    && crmBean.getRecurrenceInterval() != originalBean.getRecurrenceInterval())
            {
                RmiApiErrorHandlingSupport.simpleValidation("service",
                        "Recurrence Interval update is not allowed for Service " + service.getIdentifier() + " .");
            }
            
            if(crmBean.getType().getIndex() == ServiceTypeEnum.VOICEMAIL_INDEX)
            {
                RmiApiSupport.validateExistanceOfBeanForKey(ctx, CrmVmPlan.class,
                        new EQ(CrmVmPlanXInfo.ID, Long.valueOf(crmBean.getVmPlanId())));
            }
            if (crmBean.getType().getIndex() == ServiceTypeEnum.SERVICE_PROVISIONING_GATEWAY_INDEX)
            {
                RmiApiSupport.validateExistanceOfBeanForKey(ctx, SPGService.class,
                        new EQ(SPGServiceXInfo.ID, crmBean.getSPGServiceType()));
            }
            Object obj = home.store(crmBean);
            modificationResult.setService((Service) serviceAdapter_.adapt(ctx, obj));
            modificationResult.setParameters(parameters);
        }
        catch (final Exception e)
        {
            final String msg = "Unable to update Service";
            RmiApiErrorHandlingSupport.handleQueryExceptions(ctx, e, msg, this);
        }
        return modificationResult;
    }
    
    /**
     * {@inheritDoc}
     */
    public PricePlan getPricePlan(final CRMRequestHeader header, final long planID, final Long version, GenericParameter[] parameters)
        throws CRMExceptionFault
    {
        // throw new CRMExceptionFault("Method PricePlanToApiAdapter.adaptPricePlanToApi needs to set values to new fields in PricePlan data type");
        // in the PricePlanToApiAdapter should be reimplemented to set the new values
        // introduced in the PricePlan data structure.

        
        final Context ctx = getContext().createSubContext();
        // handle login. Failed logins throw exceptions
        RmiApiSupport.authenticateUser(ctx, header, "getPricePlan",
            Constants.PERMISSION_SERVICESANDBUNDLES_READ_GETPRICEPLAN);

        com.redknee.app.crm.bean.PricePlan pricePlan = null;
        try
        {
            pricePlan = HomeSupportHelper.get(ctx).findBean(ctx, com.redknee.app.crm.bean.PricePlan.class, planID);
        }
        catch (final Exception e)
        {
            final String msg = "Unable to retrieve Price Plan " + planID + " version=" + version;
            RmiApiErrorHandlingSupport.handleQueryExceptions(ctx, e, msg, this);
        }

        if (pricePlan == null)
        {
            final String identifier = "Price Plan " + planID;
            RmiApiErrorHandlingSupport.identificationException(ctx, identifier, this);
        }

        List<PricePlanVersion> ppvList  = null;
        if (version!=null)
        {
            final PricePlanVersion ppv = getCrmPricePlanVersion(ctx, planID, version.longValue(), this);

            if (ppv != null)
            {
                ppvList = new ArrayList<PricePlanVersion>();
                ppvList.add(ppv);
            }
            else
            {
                final String identifier = "Price Plan " + planID + " Price Plan Version : " + version;
                RmiApiErrorHandlingSupport.identificationException(ctx, identifier, this);
            }
        }
        else
        {
            ppvList = new ArrayList<PricePlanVersion>(getCrmPricePlanVersions(ctx, planID, this));
        }
        

        PricePlan result = null;
        try
        {
            result = PricePlanToApiAdapter.adaptPricePlanToApi(ctx, pricePlan, ppvList);
        }
        catch (Exception e)
        {
            final String msg = "Unable to retrieve Price Plan " + planID + " version=" + version;
            RmiApiErrorHandlingSupport.handleQueryExceptions(ctx, e, msg, this);
        }

        return result;
        
    }


    /**
     * Sets the source of each of the service package fee in a collection.
     *
     * @param collection
     *            Collection of service package fees.
     * @param id
     *            Source of the service package fee.
     */
    private void setPackageSourceToServiceFees(final Collection<ServiceFee2> fees, final long id)
    {
        for (final ServiceFee2 fee : fees)
        {
            fee.setSource(String.valueOf(id));
        }
    }


    /**
     * Sets the source of each of the bundle fee in a collection.
     *
     * @param collection
     *            Collection of bundle fees.
     * @param id
     *            Source of the bundle fee.
     */
    private void setPackageSourceToBundleFees(final Collection<BundleFee> fees, final long id)
    {
        for (final BundleFee fee : fees)
        {
            fee.setSource(String.valueOf(id));
        }
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
     * Operating context.
     */
    private Context context_;

    /**
     * CRM price plan to API price plan adapter.
     */
    private final PricePlanToApiAdapter pricePlanAdapter_;

    /**
     * CRM service fee to API service fee adapter.
     */
    private final ServiceFeeToApiAdapter serviceFeeAdapter_;

    /**
     * CRM bundle fee to API bundle fee adapter.
     */
    private final BundleFeeToApiAdapter bundleFeeAdapter_;

    /**
     * CRM auxiliary service to API auxiliary service adapter.
     */
    private final AuxiliaryServiceToApiAdapter auxiliaryServiceAdapter_;
    
    /**
     * CRM bundle profile to API bundle profile adapter.
     */
    private final BundleProfileToApiReferenceAdapter auxiliarBundleReferenceAdapter_;

    private final AuxiliaryServiceToApiReferenceAdapter auxiliaryServiceReferenceAdapter_;
    
    private final BundleProfileToApiAdapter auxiliarBundleAdapter_;
    
    private final BundleProfileToApiFeeAdapter auxiliarBundleToFeeAdapter_;

    private final BundleCategoryToApiAdapter bundleCategoryAdapter_;
    
    private final BundleCategoryToApiReferenceAdapter bundleCategoryReferenceAdapter_;
    
    private final RatePlanAssociationToApiAdapter ratePlanAssociationAdapter_;
    
    private final RatePlanAssociationToApiReferenceAdapter ratePlanAssociationReferenceAdapter_;

    private final PricePlanToApiReferenceAdapter pricePlanReferenceAdapter_;
    
    private final DependencyGroupToApiAdapter dependencyGroupAdapter_;
    
    private final PrerequisiteGroupToApiAdapter prerequisiteGroupAdapter_;
    
    private final ServiceToApiAdapter serviceAdapter_;
    
    private final ServiceToApiReferenceAdapter serviceReferenceAdapter_;
    
    private final PricePlanGroupToApiAdapter priceplangroupAdapter_;
    
    private final PricePlanVersionToApiAdapter pricePlanVersionAdapter_;
    
    private final BeanAdapter<com.redknee.app.crm.bean.core.BundleProfile, com.redknee.app.crm.bean.ui.BundleProfile> bundleProfileUiToBundleProfileAdapter_ = new BeanAdapter<com.redknee.app.crm.bean.core.BundleProfile, com.redknee.app.crm.bean.ui.BundleProfile>(
            com.redknee.app.crm.bean.core.BundleProfile.class, com.redknee.app.crm.bean.ui.BundleProfile.class);
}
