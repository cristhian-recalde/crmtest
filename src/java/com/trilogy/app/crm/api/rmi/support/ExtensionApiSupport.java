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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import com.trilogy.framework.xhome.beans.ComparableComparator;
import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.beans.ExceptionListener;
import com.trilogy.framework.xhome.beans.ReverseComparator;
import com.trilogy.framework.xhome.beans.SafetyUtil;
import com.trilogy.framework.xhome.beans.XBeans;
import com.trilogy.framework.xhome.beans.xi.XInfo;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.home.OrderByHome;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MinorLogMsg;
import com.trilogy.app.crm.account.AccountConstants;
import com.trilogy.app.crm.api.ApiSupport;
import com.trilogy.app.crm.api.rmi.extensions.PPSMSupporteeSubscriberExtensionToApiAdapter;
import com.trilogy.app.crm.api.rmi.extensions.SubscriberExtensionToApiAdapter;
import com.trilogy.app.crm.bean.ServiceProvisionStatusEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.bean.core.BundleFee;
import com.trilogy.app.crm.checking.CrmFix;
import com.trilogy.app.crm.extension.account.AccountExtensionXInfo;
import com.trilogy.app.crm.extension.account.FriendsAndFamilyExtension;
import com.trilogy.app.crm.extension.account.GroupPricePlanExtension;
import com.trilogy.app.crm.extension.account.PoolExtension;
import com.trilogy.app.crm.extension.account.SubscriberLimitExtension;
import com.trilogy.app.crm.extension.account.SubscriberLimitExtensionXInfo;
import com.trilogy.app.crm.extension.account.SubscriptionPoolProperty;
import com.trilogy.app.crm.extension.subscriber.PPSMSupporteeSubExtension;
import com.trilogy.app.crm.extension.subscriber.PPSMSupporterMoveRequest;
import com.trilogy.app.crm.extension.subscriber.PPSMSupporterSubExtension;
import com.trilogy.app.crm.extension.subscriber.PPSMSupporterSubExtensionHome;
import com.trilogy.app.crm.extension.subscriber.SubscriberExtension;
import com.trilogy.app.crm.extension.subscriber.SubscriberExtensionXInfo;
import com.trilogy.app.crm.support.ExtensionSupportHelper;
import com.trilogy.app.crm.web.service.PPSMSupporterMoveSupporteesRequestServicer;
import com.trilogy.util.crmapi.wsdl.v2_0.types.GenericParameter;
import com.trilogy.util.crmapi.wsdl.v2_0.types.SuccessCode;
import com.trilogy.util.crmapi.wsdl.v2_0.types.SuccessCodeEnum;
import com.trilogy.util.crmapi.wsdl.v2_0.types.SystemType;
import com.trilogy.util.crmapi.wsdl.v2_2.types.account.BaseAccountExtensionReference;
import com.trilogy.util.crmapi.wsdl.v2_2.types.subscription.BaseMutableSubscriptionExtension;
import com.trilogy.util.crmapi.wsdl.v3_0.api.CRMExceptionFault;
import com.trilogy.util.crmapi.wsdl.v3_0.types.subscription.BaseReadOnlySubscriptionExtension;
import com.trilogy.util.crmapi.wsdl.v3_0.types.subscription.BaseSubscriptionExtension;
import com.trilogy.util.crmapi.wsdl.v3_0.types.subscription.BaseSubscriptionExtensionReference;
import com.trilogy.util.crmapi.wsdl.v3_0.types.subscription.SubscriptionReference;
import com.trilogy.util.crmapi.wsdl.v3_0.types.subscription.extensions.PPSMSupporteeSubscriptionExtensionReference;


/**
 * Provides utility functions for use with Extension
 * 
 * @author kumaran.sivasubramaniam@redknee.com
 */
public class ExtensionApiSupport
{

    public static SuccessCode removeExtension(final Context ctx, BaseSubscriptionExtensionReference extensionReference,
            Subscriber sub, GenericParameter[] parameters) throws CRMExceptionFault
    {
        final Subscriber subscriber = SubscribersApiSupport.getCrmSubscriber(ctx, extensionReference
                .getSubscriptionRef(), ExtensionApiSupport.class);
        if (subscriber.isInFinalState())
        {
            String msg = "Subscription is in a closed state.  Unable to remove extension.";
            RmiApiErrorHandlingSupport.handleDeleteExceptions(ctx, null, msg, ExtensionApiSupport.class);
        }
        
        SubscriberExtensionToApiAdapter adapter = SubscriberExtensionToApiAdapter.getInstance(extensionReference,
                subscriber);
        if (adapter == null)
        {
            RmiApiErrorHandlingSupport.generalException(ctx, null,
                    "Method remove is not supported for the given extension.", ExtensionApiSupport.class);
        }
        adapter.remove(ctx, extensionReference, parameters);
        return SuccessCodeEnum.SUCCESS.getValue();
    }


    public static SuccessCode updateSubscriptionExtension(final Context ctx,
            BaseSubscriptionExtensionReference extensionReference, BaseMutableSubscriptionExtension extension, GenericParameter[] parameters)
            throws CRMExceptionFault
    {
        RmiApiErrorHandlingSupport.validateMandatoryObject(extensionReference, "extensionReference");
        RmiApiErrorHandlingSupport.validateMandatoryObject(extension, "extension");
        final Subscriber subscriber = SubscribersApiSupport.getCrmSubscriber(ctx, extensionReference
                .getSubscriptionRef(), ExtensionApiSupport.class);
        if (subscriber.isInFinalState())
        {
            String msg = "Subscription is in a closed state.  Unable to update extension.";
            RmiApiErrorHandlingSupport.handleStoreExceptions(ctx, null, msg, ExtensionApiSupport.class);
        }
        
        SubscriberExtensionToApiAdapter adapter = SubscriberExtensionToApiAdapter.getInstance(extensionReference,
                extension, subscriber);
        if (adapter == null)
        {
            RmiApiErrorHandlingSupport.generalException(ctx, null,
                    "Method updateSubscriptionExtension() is not supported for the given extension.",
                    ExtensionApiSupport.class);
        }
        boolean updated = adapter.update(ctx, extensionReference, extension, parameters);
        if (!updated)
        {
            RmiApiErrorHandlingSupport.identificationException(ctx, "Subscriber extension " + subscriber.getId(),
                    ExtensionApiSupport.class);
        }
        return SuccessCodeEnum.SUCCESS.getValue();
    }


    public static BaseSubscriptionExtensionReference addExtension(final Context ctx,
            BaseSubscriptionExtension extension, Subscriber sub, GenericParameter[] parameters) throws CRMExceptionFault
    {
        RmiApiErrorHandlingSupport.validateMandatoryObject(extension, "extension");
        try
        {
            if (sub != null && sub.isInFinalState())
            {
                String msg = "Subscription is in a closed state.  Unable to add extension.";
                RmiApiErrorHandlingSupport.handleCreateExceptions(ctx, null, msg, false, SubscriberExtension.class, null, ExtensionApiSupport.class);
            }
            
            if (extension instanceof com.redknee.util.crmapi.wsdl.v3_0.types.subscription.extensions.SubscriptionExtension)
            {
                com.redknee.util.crmapi.wsdl.v3_0.types.subscription.extensions.SubscriptionExtension apiExtension = (com.redknee.util.crmapi.wsdl.v3_0.types.subscription.extensions.SubscriptionExtension) extension;
                
                SubscriberExtensionToApiAdapter adapter = SubscriberExtensionToApiAdapter
                        .getInstance(apiExtension, sub);
                if (adapter == null)
                {
                    RmiApiErrorHandlingSupport.generalException(ctx, null,
                            "Method updateSubscriptionAddExtension() is not supported for the given extension.",
                            ExtensionApiSupport.class);
                    return null;
                }
                SubscriberExtension newExtension = adapter.toCRM(ctx, apiExtension, parameters);
                Home extensionHome = ExtensionSupportHelper.get(ctx).getExtensionHome(ctx, newExtension);
                if (extensionHome != null)
                {
                    extensionHome = ApiSupport.injectAPIUpdateEntityHomeIntoPipeline(ctx, extensionHome);
                   
                    Context sCtx = ctx.createSubContext();
                    CompoundIllegalStateException el = new CompoundIllegalStateException();
                    sCtx.put(ExceptionListener.class, el);
                    newExtension = (SubscriberExtension) extensionHome.create(sCtx, newExtension);
                    if (el.getSize() > 0)
                    {
                        final String msg = "Extension created with errors.  See entries for details.";
                        RmiApiErrorHandlingSupport.handleCreateExceptions(ctx, el, msg, true, newExtension.getClass(), newExtension.ID(), ExtensionApiSupport.class);
                    }
                    return adapter.toAPIReference(ctx, newExtension);
                }
                else
                {
                    StringBuilder msg = new StringBuilder("Subscription Extension type not supported");
                    Class<? extends SubscriberExtension> newExtensionType = null;
                    Object id = null;
                    if (newExtension != null)
                    {
                        newExtensionType = newExtension.getClass();
                        id = newExtension.ID();
                        msg.append(": " + newExtensionType.getName());
                    }
                    RmiApiErrorHandlingSupport.handleCreateExceptions(ctx, null, msg.toString(), false, newExtensionType, id, ExtensionApiSupport.class);
                }
            }
            else
            {
                final String msg = "Subscription Extension type not supported: " + extension.getClass().getSimpleName();
                RmiApiErrorHandlingSupport.handleCreateExceptions(ctx, null, msg, false, SubscriberExtension.class, null, ExtensionApiSupport.class);
            }
        }
        catch (CRMExceptionFault e)
        {
            throw e;
        }
        catch (final Exception e)
        {
            final String msg = "Unable to create Subscription Extension";
            RmiApiErrorHandlingSupport.handleCreateExceptions(ctx, e, msg, false, SubscriberExtension.class, null, ExtensionApiSupport.class);
        }
        return null;
    }


    public static BaseReadOnlySubscriptionExtension[] getListDetailedSubscriptionExtensions(final Context ctx,
            final Subscriber subscriber, Boolean isAscending) throws CRMExceptionFault
    {
        Collection<BaseReadOnlySubscriptionExtension> subscriptionExtension = new ArrayList<BaseReadOnlySubscriptionExtension>();
        try
        {
            final Collection<SubscriberExtension> extensions = getSortedExtensions(ctx, subscriber.getId(),
                    RmiApiSupport.isSortAscending(isAscending));
            for (SubscriberExtension extension : extensions)
            {
                SubscriberExtensionToApiAdapter adapter = SubscriberExtensionToApiAdapter.getInstance(extension,
                        subscriber);
                if (adapter != null)
                {
                    subscriptionExtension.add(adapter.toAPI(ctx, extension));
                }
            }
        }
        catch (final Exception e)
        {
            final String msg = "Unable to retrieve Subscription Extensions";
            RmiApiErrorHandlingSupport.handleQueryExceptions(ctx, e, msg, ExtensionApiSupport.class);
        }
        return subscriptionExtension.toArray(new BaseReadOnlySubscriptionExtension[]
            {});
    }


    public static BaseSubscriptionExtensionReference[] getListSubscriptionExtensionsReference(final Context ctx,
            final Subscriber subscriber, Boolean isAscending) throws CRMExceptionFault
    {
        Collection<BaseSubscriptionExtensionReference> subscriptionExtensionReferences = new ArrayList<BaseSubscriptionExtensionReference>();
        try
        {
            final Collection<SubscriberExtension> extensions = getSortedExtensions(ctx, subscriber.getId(),
                    RmiApiSupport.isSortAscending(isAscending));
            for (SubscriberExtension extension : extensions)
            {
                SubscriberExtensionToApiAdapter adapter = SubscriberExtensionToApiAdapter.getInstance(extension,
                        subscriber);
                if (adapter != null)
                {
                    subscriptionExtensionReferences.add(adapter.toAPIReference(ctx, extension));
                }
            }
        }
        catch (final Exception e)
        {
            final String msg = "Unable to retrieve Subscription Extensions";
            RmiApiErrorHandlingSupport.handleQueryExceptions(ctx, e, msg, ExtensionApiSupport.class);
        }
        return subscriptionExtensionReferences.toArray(new BaseSubscriptionExtensionReference[]
            {});
    }


    private static Collection<SubscriberExtension> getSortedExtensions(final Context ctx, String subscriberId,
            boolean ascending)
    {
        final Context sCtx = ctx.createSubContext();
        // Set up the registered extension homes so that the results are ordered by
        // primary key
        Set<Class<SubscriberExtension>> extensions = ExtensionSupportHelper.get(ctx).getRegisteredExtensions(sCtx,
                SubscriberExtension.class);
        for (Class<SubscriberExtension> extension : extensions)
        {
            XInfo xinfo = (XInfo) XBeans.getInstanceOf(sCtx, extension, XInfo.class);
            if (xinfo != null)
            {
                Object homeKey = ExtensionSupportHelper.get(ctx).getExtensionHomeKey(sCtx, extension);
                Home home = (Home) sCtx.get(homeKey);
                home = new OrderByHome(sCtx, home);
                ((OrderByHome) home).addOrderBy(xinfo.getID(), ascending);
                sCtx.put(homeKey, home);
            }
        }
        // Sort the extension names
        Map<Class<SubscriberExtension>, List<SubscriberExtension>> temp = ExtensionSupportHelper.get(ctx)
                .getExistingExtensionMap(sCtx, SubscriberExtension.class,
                        new EQ(SubscriberExtensionXInfo.SUB_ID, subscriberId));
        final Map<Class<SubscriberExtension>, List<SubscriberExtension>> extensionMap = temp;
        final Set<Class<SubscriberExtension>> extensionTypeSet = extensionMap.keySet();
        final Map<String, Class<? extends SubscriberExtension>> extensionTypes;
        if (ascending)
        {
            extensionTypes = new TreeMap<String, Class<? extends SubscriberExtension>>();
        }
        else
        {
            extensionTypes = new TreeMap<String, Class<? extends SubscriberExtension>>(new ReverseComparator(
                    ComparableComparator.instance()));
        }
        for (Class<SubscriberExtension> extensionType : extensionTypeSet)
        {
            extensionTypes.put(ExtensionSupportHelper.get(ctx).getExtensionName(ctx, extensionType), extensionType);
        }
        // Add extensions to a collection that is sorted first by extension name, then by
        // ID
        final Collection<SubscriberExtension> result = new ArrayList<SubscriberExtension>();
        for (Class<? extends SubscriberExtension> extensionType : extensionTypes.values())
        {
            result.addAll(extensionMap.get(extensionType));
        }
        return result;
    }


    public static BaseReadOnlySubscriptionExtension getSubscriptionExtension(final Context ctx,
            BaseSubscriptionExtensionReference extensionReference) throws CRMExceptionFault
    {
        RmiApiErrorHandlingSupport.validateMandatoryObject(extensionReference, "extensionReference");
        final Subscriber subscriber = SubscribersApiSupport.getCrmSubscriber(ctx, extensionReference
                .getSubscriptionRef(), ExtensionApiSupport.class);
        SubscriberExtensionToApiAdapter adapter = SubscriberExtensionToApiAdapter.getInstance(extensionReference,
                subscriber);
        if (adapter == null)
        {
            RmiApiErrorHandlingSupport.generalException(ctx, null,
                    "Method getSubscriptionExtension() is not supported for the given extension.",
                    ExtensionApiSupport.class);
        }
        return adapter.find(ctx, extensionReference);
    }


    public static SuccessCode updateSubscriptionsPPSMSupporter(final Context ctx,
            SubscriptionReference currentSupporterRef, SubscriptionReference newSupporterRef) throws CRMExceptionFault
    {
        final Subscriber currentSupporter = SubscribersApiSupport.getCrmSubscriber(ctx, currentSupporterRef,
                ExtensionApiSupport.class);
        PPSMSupporterMoveRequest request = new PPSMSupporterMoveRequest();
        request.setOldSubscriptionId(currentSupporter.getId());
        request.setNewMSISDN(newSupporterRef.getMobileNumber());
        PPSMSupporterMoveSupporteesRequestServicer.validate(ctx, request);
        if (request.hasErrors(ctx))
        {
            CompoundIllegalStateException cise = new CompoundIllegalStateException();
            {
                for (Throwable t : request.getErrors(ctx))
                {
                    cise.thrown(t);
                }
            }
            RmiApiErrorHandlingSupport.generalException(ctx, cise,
                    "Error during PPSM supporter change validation for new MSISDN '"
                            + newSupporterRef.getMobileNumber() + "': " + cise.getMessage(), ExtensionApiSupport.class);
        }
        if (!newSupporterRef.getIdentifier().equals(request.getNewSubscriptionId()))
        {
            RmiApiErrorHandlingSupport.generalException(ctx, null, "New supporter identifier '"
                    + newSupporterRef.getIdentifier() + "' is different from identifier retrieved ("
                    + request.getNewSubscriptionId() + ") for new supporter MSISDN ("
                    + newSupporterRef.getMobileNumber() + ").", ExtensionApiSupport.class);
        }
        try
        {
            ctx.put( PPSMSupporterSubExtensionHome.class, ApiSupport.injectAPIUpdateEntityHomeIntoPipeline(ctx, (Home)ctx.get(PPSMSupporterSubExtensionHome.class)));            
            PPSMSupporterMoveSupporteesRequestServicer.move(ctx, request);
        }
        catch (Exception e)
        {
            RmiApiErrorHandlingSupport.generalException(ctx, e,
                    "Exception occurred during updateSubscriptionsPPSMSupporter() execution when changing supporter from '"
                            + request.getOldSubscriptionId() + "' to '" + request.getNewSubscriptionId() + "': "
                            + e.getMessage(), ExtensionApiSupport.class);
        }
        return SuccessCodeEnum.SUCCESS.getValue();
    }


    public static PPSMSupporteeSubscriptionExtensionReference[] getListPPSMSupportees(final Context ctx,
            SubscriptionReference supporterRef) throws CRMExceptionFault
    {
        Collection<PPSMSupporteeSubscriptionExtensionReference> supportees = new ArrayList<PPSMSupporteeSubscriptionExtensionReference>();
        try
        {
            PPSMSupporterSubExtension extension = PPSMSupporterSubExtension.getPPSMSupporterSubscriberExtension(ctx,
                    supporterRef.getIdentifier());
            for (PPSMSupporteeSubExtension supporteeExtension : extension.getSupportedSubscribers(ctx))
            {
                Subscriber supporteeSubscriber = supporteeExtension.getSubscriber(ctx);
                PPSMSupporteeSubscriberExtensionToApiAdapter adapter = new PPSMSupporteeSubscriberExtensionToApiAdapter(
                        supporteeSubscriber);
                supportees.add((PPSMSupporteeSubscriptionExtensionReference) adapter.toAPIReference(ctx,
                        supporteeExtension));
            }
        }
        catch (Exception e)
        {
            final String msg = "Unable to retrieve PPSM Subscription Extensions";
            RmiApiErrorHandlingSupport.handleQueryExceptions(ctx, e, msg, ExtensionApiSupport.class);
        }
        return supportees.toArray(new PPSMSupporteeSubscriptionExtensionReference[]
            {});
    }
    
    
	/**
	 * @param extensionReference
	 * @param ctx
	 * @throws CRMExceptionFault
	 */
	public static void removeAccountExtension(
			BaseAccountExtensionReference extensionReference, final Context ctx)
			throws CRMExceptionFault {
		try
        {
            if (extensionReference instanceof com.redknee.util.crmapi.wsdl.v2_2.types.account.extensions.PoolAccountExtensionReference)
            {
				com.redknee.util.crmapi.wsdl.v2_2.types.account.extensions.PoolAccountExtensionReference poolReference = (com.redknee.util.crmapi.wsdl.v2_2.types.account.extensions.PoolAccountExtensionReference) extensionReference;

                RmiApiErrorHandlingSupport.assertExtensionRemoveEnabled(ctx, PoolExtension.class, ExtensionApiSupport.class);
                
                List<PoolExtension> poolExtensions = ExtensionSupportHelper.get(ctx).getExtensions(ctx, PoolExtension.class, new EQ(AccountExtensionXInfo.BAN, extensionReference.getAccountID()));

                for (PoolExtension poolExtension : poolExtensions)
                {
                    String groupMsisdn = poolReference.getGroupMobileNumber();
                    if (groupMsisdn == null || SafetyUtil.safeEquals(groupMsisdn, poolExtension.getPoolMSISDN()))
                    {
                        Home extensionHome = ExtensionSupportHelper.get(ctx).getExtensionHome(ctx, poolExtension);
                        extensionHome = ApiSupport.injectAPIUpdateEntityHomeIntoPipeline(ctx, extensionHome);
                        if (extensionHome != null)
                        {
                            extensionHome.remove(ctx, poolExtension);
                        }
                        else
                        {
                            final String msg = "Account Extension type not supported: " + poolExtension.getClass().getName();
                            RmiApiErrorHandlingSupport.handleDeleteExceptions(ctx, null, msg, ExtensionApiSupport.class);
                        }
                        break;
                    }
                }
            }
            else if(extensionReference instanceof com.redknee.util.crmapi.wsdl.v2_2.types.account.extensions.GroupPricePlanAccountExtensionReference)
            {
            	RmiApiErrorHandlingSupport.assertExtensionRemoveEnabled(ctx, GroupPricePlanExtension.class, ExtensionApiSupport.class);
            	
            	List<GroupPricePlanExtension> groupPricePlanExtensions = ExtensionSupportHelper.get(ctx).getExtensions(ctx, GroupPricePlanExtension.class, new EQ(AccountExtensionXInfo.BAN, extensionReference.getAccountID()));
            	
            	for ( GroupPricePlanExtension groupPricePlanExtension : groupPricePlanExtensions)
            	{
            		Home extensionHome = ExtensionSupportHelper.get(ctx).getExtensionHome(ctx, groupPricePlanExtension);
            		extensionHome = ApiSupport.injectAPIUpdateEntityHomeIntoPipeline(ctx, extensionHome);
            		if(extensionHome != null)
            		{
            			extensionHome.remove(ctx, groupPricePlanExtension);
            		}
            		else 
            		{
            			final String msg = "Account Extension type not supported: " + groupPricePlanExtension.getClass().getName();
                        RmiApiErrorHandlingSupport.handleDeleteExceptions(ctx, null, msg, ExtensionApiSupport.class);
                    }
            	} 
            }
            else if(extensionReference instanceof com.redknee.util.crmapi.wsdl.v2_2.types.account.extensions.SubscriberLimitAccountExtensionReference)
            {
            	RmiApiErrorHandlingSupport.assertExtensionRemoveEnabled(ctx, SubscriberLimitExtension.class, ExtensionApiSupport.class);
            	
            	List<SubscriberLimitExtension> subscriberLimitExtensions = ExtensionSupportHelper.get(ctx).getExtensions(ctx, SubscriberLimitExtension.class, new EQ(AccountExtensionXInfo.BAN, extensionReference.getAccountID()));
            	
            	for (SubscriberLimitExtension subscriberLimitExtension : subscriberLimitExtensions)
            	{
            		Home extensionHome = ExtensionSupportHelper.get(ctx).getExtensionHome(ctx, subscriberLimitExtension);
            		extensionHome = ApiSupport.injectAPIUpdateEntityHomeIntoPipeline(ctx, extensionHome);
            		if(extensionHome != null)
            		{
            			extensionHome.remove(ctx, subscriberLimitExtension);
            		}
            		else 
            		{
            			final String msg = "Account Extension type not supported: " + subscriberLimitExtension.getClass().getName();
                        RmiApiErrorHandlingSupport.handleDeleteExceptions(ctx, null, msg, ExtensionApiSupport.class);
                    }
            	} 
            }
            else if(extensionReference instanceof com.redknee.util.crmapi.wsdl.v2_2.types.account.extensions.FriendsAndFamilyAccountExtensionReference)
            {
            	RmiApiErrorHandlingSupport.assertExtensionRemoveEnabled(ctx, FriendsAndFamilyExtension.class, ExtensionApiSupport.class);
            	
            	List<FriendsAndFamilyExtension> fnfExtensions = ExtensionSupportHelper.get(ctx).getExtensions(ctx, FriendsAndFamilyExtension.class, new EQ(AccountExtensionXInfo.BAN, extensionReference.getAccountID()));
            	
            	for (FriendsAndFamilyExtension fnfExtension : fnfExtensions)
            	{
            		Home extensionHome = ExtensionSupportHelper.get(ctx).getExtensionHome(ctx, fnfExtension);
            		extensionHome = ApiSupport.injectAPIUpdateEntityHomeIntoPipeline(ctx, extensionHome);
            		if(extensionHome != null)
            		{
            			extensionHome.remove(ctx, fnfExtension);
            		}
            		else 
            		{
            			final String msg = "Account Extension type not supported: " + fnfExtension.getClass().getName();
                        RmiApiErrorHandlingSupport.handleDeleteExceptions(ctx, null, msg, ExtensionApiSupport.class);
                    }
            	} 
            }
            else
            {
                final String msg = "Account Extension type not supported: " + extensionReference.getClass().getSimpleName();
                RmiApiErrorHandlingSupport.handleDeleteExceptions(ctx, null, msg, ExtensionApiSupport.class);
            }
        }
        catch (CRMExceptionFault e)
        {
            throw e;
        }
        catch (final Exception e)
        {
            final String msg = "Unable to remove Account Extension";
            RmiApiErrorHandlingSupport.handleDeleteExceptions(ctx, e, msg, ExtensionApiSupport.class);
        }
	}
    

    /**
     *  Updates pool extension for an account
     * @param ctx
     * @param poolReference
     * @param poolExtension
     * @param account
     * @throws CRMExceptionFault
     */
    public static void updateAccountPoolExtension(
            final Context ctx,
            final com.redknee.util.crmapi.wsdl.v2_2.types.account.extensions.PoolAccountExtensionReference poolReference,
            final com.redknee.util.crmapi.wsdl.v3_0.types.account.extensions.MutablePoolAccountExtension poolExtension,
            com.redknee.app.crm.bean.Account account) throws CRMExceptionFault
    {
        try
        {
            boolean extensionExists = false;
            RmiApiErrorHandlingSupport
                    .assertExtensionUpdateEnabled(ctx, PoolExtension.class, ExtensionApiSupport.class);
            List<PoolExtension> crmPoolExtensions = ExtensionSupportHelper.get(ctx).getExtensions(ctx,
                    PoolExtension.class, new EQ(AccountExtensionXInfo.BAN, poolReference.getAccountID()));
            for (PoolExtension crmPoolExtension : crmPoolExtensions)
            {
                boolean dirty = false;
                String groupMobileNumber = poolReference.getGroupMobileNumber();
                if (groupMobileNumber == null
                        || SafetyUtil.safeEquals(groupMobileNumber, crmPoolExtension.getPoolMSISDN()))
                {
                    extensionExists = true;
                    Map<Long, SubscriptionPoolProperty> crmPoolProperties = null;
                    if (poolExtension.getSubscriptionPoolProperty() != null)
                    {
                        dirty = true;
                        crmPoolProperties = new HashMap<Long, SubscriptionPoolProperty>();
                        for (com.redknee.util.crmapi.wsdl.v2_2.types.account.extensions.SubscriptionPoolProperty poolProperty : poolExtension
                                .getSubscriptionPoolProperty())
                        {
                            SubscriptionPoolProperty crmPoolProperty;
                            try
                            {
                                crmPoolProperty = (SubscriptionPoolProperty) XBeans.instantiate(
                                        SubscriptionPoolProperty.class, ctx);
                            }
                            catch (Exception e)
                            {
                                new MinorLogMsg(
                                        ExtensionApiSupport.class,
                                        "Error instantiating new subscription pool property.  Using default constructor.",
                                        e).log(ctx);
                                crmPoolProperty = new SubscriptionPoolProperty();
                            }
                            crmPoolProperty.setSubscriptionType(poolProperty.getSubscriptionType());
                            crmPoolProperty.setInitialPoolBalance(poolProperty.getInitialPoolBalance());
                            if (poolProperty.getProvisioned() != null)
                            {
                                crmPoolProperty.setProvisioned((short) poolProperty.getProvisioned().getValue());
                            }
                            else
                            {
                                crmPoolProperty.setProvisioned(ServiceProvisionStatusEnum.PROVISIONED_INDEX);
                            }
                            crmPoolProperties.put(crmPoolProperty.getSubscriptionType(), crmPoolProperty);
                        }
                        crmPoolExtension.setSubscriptionPoolProperties(crmPoolProperties);
                    }
                    else
                    {
                        crmPoolProperties = crmPoolExtension.getSubscriptionPoolProperties();
                    }
                    if (poolExtension.getPoolLimit() != null)
                    {
                        crmPoolExtension.setQuotaLimit(poolExtension.getPoolLimit().intValue());
                        dirty = true;
                    }
                    if (poolExtension.getPoolLimitStrategy() != null)
                    {
                        crmPoolExtension.setQuotaType(RmiApiSupport.convertApiPoolLimitStrategy2Crm(poolExtension
                                .getPoolLimitStrategy()));
                        dirty = true;
                    }
                    Long[] apiBundleIDs = poolExtension.getBundleIDs();
                    final Map<Long, BundleFee> poolBundles;
                    if (null != apiBundleIDs)
                    {
                        if (apiBundleIDs.length > 0)
                        {
                            final CompoundIllegalStateException excl;
                            {
                                excl = new CompoundIllegalStateException();
                                poolBundles = PoolExtension.transformBundles(ctx, excl, apiBundleIDs);
                                if (excl.getSize() > 0)
                                {
                                    excl.throwAll();
                                }
                            }
                        }
                        else
                        {
                            poolBundles = new HashMap<Long, BundleFee>();
                        }
                    }
                    else
                    {
                        poolBundles = new HashMap<Long, BundleFee>();
                    }

                    if (!poolBundles.keySet().equals(crmPoolExtension.getPoolBundles().keySet()))
                    {
                        dirty = true;
                        crmPoolExtension.setPoolBundles(poolBundles);
                    }

                    if (dirty)
                    {
                        Home extensionHome = ExtensionSupportHelper.get(ctx).getExtensionHome(ctx, crmPoolExtension);
                        extensionHome = ApiSupport.injectAPIUpdateEntityHomeIntoPipeline(ctx, extensionHome);
                        if (extensionHome != null)
                        {
                            ctx.put(AccountConstants.OLD_ACCOUNT, account);
                            extensionHome.store(ctx, crmPoolExtension);
                        }
                        else
                        {
                            final String msg = "Account Extension type not supported: "
                                    + crmPoolExtension.getClass().getName();
                            RmiApiErrorHandlingSupport.handleStoreExceptions(ctx, null, msg, ExtensionApiSupport.class);
                        }
                    }
                    if (!extensionExists)
                    {
                        RmiApiErrorHandlingSupport.identificationException(ctx,
                                "Pool extension " + poolReference.getAccountID(), ExtensionApiSupport.class);
                    }
                }
                else
                {
                    RmiApiErrorHandlingSupport.handleStoreExceptions(ctx, null,
                            " Pool extension msisdn=>" + groupMobileNumber + " get doesn't match account pool msisdn=>"
                                    + crmPoolExtension.getPoolMSISDN(), ExtensionApiSupport.class);
                }
        }
        }
        catch (CRMExceptionFault ex)
        {
            throw ex;
        }
        catch (Exception ex)
        {
            final String msg = "Unable to update Account Extension";
            RmiApiErrorHandlingSupport.handleStoreExceptions(ctx, ex, msg, ExtensionApiSupport.class);
        }
    }
    
    
    
    /**
     *  Updates Group Price Plan extension for an account
     * @param ctx
     * @param groupPricePlanReference
     * @param groupPricePlanExtension
     * @param account
     * @throws CRMExceptionFault
     */
    public static void updateGroupPricePlanExtension(
            final Context ctx,
            final com.redknee.util.crmapi.wsdl.v2_2.types.account.extensions.GroupPricePlanAccountExtensionReference groupPricePlanReference,
            final com.redknee.util.crmapi.wsdl.v3_0.types.account.extensions.MutableGroupPricePlanAccountExtension groupPricePlanExtension,
            com.redknee.app.crm.bean.Account account) throws CRMExceptionFault
    {
        try
        {
            RmiApiErrorHandlingSupport
                    .assertExtensionUpdateEnabled(ctx, GroupPricePlanExtension.class, ExtensionApiSupport.class);
            //For future reference
            //GenericParameter[] genericParameters=groupPricePlanReference.getParameters();
            List<GroupPricePlanExtension> crmGroupPricePlanExtensions = ExtensionSupportHelper.get(ctx).getExtensions(ctx, GroupPricePlanExtension.class, new EQ(AccountExtensionXInfo.BAN, groupPricePlanReference.getAccountID()));
            boolean alreadyExists = true;
            
            if(crmGroupPricePlanExtensions.isEmpty())
            {
            	alreadyExists = false;
            	try
            	{
            		crmGroupPricePlanExtensions.add((GroupPricePlanExtension)XBeans.instantiate(GroupPricePlanExtension.class, ctx));
            	}
            	catch(Exception e)
            	{
            		new MinorLogMsg(ExtensionApiSupport.class,
                            "Error instantiating new group price plan extension.  Using default constructor.", e).log(ctx);
            		crmGroupPricePlanExtensions.add(new GroupPricePlanExtension());
            	}
            }
            
            for(GroupPricePlanExtension crmGroupPricePlanExtension: crmGroupPricePlanExtensions)
            {
            	apiToCrmGroupPricePlanExtensionMapping(ctx, crmGroupPricePlanExtension, groupPricePlanExtension, account);
            	Home extensionHome = ExtensionSupportHelper.get(ctx).getExtensionHome(ctx, crmGroupPricePlanExtension);
            	extensionHome=ApiSupport.injectAPIUpdateEntityHomeIntoPipeline(ctx, extensionHome);
            	if(extensionHome != null){
            		ctx.put(AccountConstants.OLD_ACCOUNT, account);
            		if(alreadyExists)
            			extensionHome.store(ctx, crmGroupPricePlanExtension);
            		else
            			extensionHome.create(ctx, crmGroupPricePlanExtension);
            	}
            	else
            	{
            	    final String msg = "Account Extension type not supported: "
                            + crmGroupPricePlanExtension.getClass().getName();
            	    if(alreadyExists)
            	    	RmiApiErrorHandlingSupport.handleStoreExceptions(ctx, null, msg, ExtensionApiSupport.class);
            	    else
            	    	RmiApiErrorHandlingSupport.handleCreateExceptions(ctx, null, msg, false, crmGroupPricePlanExtension.getClass(), null, ExtensionApiSupport.class);
            	    
            	}
            }
            
        }
        catch (CRMExceptionFault ex)
        {
            throw ex;
        }
        catch (Exception ex)
        {
            final String msg = "Unable to update Account Extension";
            RmiApiErrorHandlingSupport.handleStoreExceptions(ctx, ex, msg, ExtensionApiSupport.class);
        }
    }
    
    /**
     *  Maps API to CRM Group Price Plan extension for an account
     * @param ctx
     * @param crmGroupPricePlanExtension
     * @param groupPricePlanExtension
     * @param account
     * @throws CRMExceptionFault
     */
    public static void apiToCrmGroupPricePlanExtensionMapping(
            final Context ctx, GroupPricePlanExtension crmGroupPricePlanExtension,
            com.redknee.util.crmapi.wsdl.v3_0.types.account.extensions.MutableGroupPricePlanAccountExtension groupPricePlanExtension,
            com.redknee.app.crm.bean.Account account) throws CRMExceptionFault
    {  
	    if(account.getSystemType()==SubscriberTypeEnum.HYBRID )
	    {
			RmiApiErrorHandlingSupport.validateMandatoryObject(groupPricePlanExtension.getPostpaidPricePlanID(), 
					"GroupPricePlanAccountExtension.postpaidPricePlanID");
			RmiApiErrorHandlingSupport.validateMandatoryObject(groupPricePlanExtension.getPrepaidPricePlanID(), 
					"GroupPricePlanAccountExtension.prepaidPricePlanID");
	    	RmiApiErrorHandlingSupport.validatePricePlanWithBillingType(ctx, SubscriberTypeEnum.POSTPAID, groupPricePlanExtension.getPostpaidPricePlanID(), 
	    			ExtensionApiSupport.class);
	    	RmiApiErrorHandlingSupport.validatePricePlanWithBillingType(ctx, SubscriberTypeEnum.PREPAID, groupPricePlanExtension.getPrepaidPricePlanID(), 
	    			ExtensionApiSupport.class);
			crmGroupPricePlanExtension.setPostpaidPricePlanID(groupPricePlanExtension.getPostpaidPricePlanID());
			crmGroupPricePlanExtension.setPrepaidPricePlanID(groupPricePlanExtension.getPrepaidPricePlanID());
	    }
	    else if (account.getSystemType()==SubscriberTypeEnum.PREPAID)
	    {
	    	RmiApiErrorHandlingSupport.validateMandatoryObject(groupPricePlanExtension.getPrepaidPricePlanID(), 
	    			"GroupPricePlanAccountExtension.prepaidPricePlanID");
	    	RmiApiErrorHandlingSupport.validatePricePlanWithBillingType(ctx, SubscriberTypeEnum.PREPAID, groupPricePlanExtension.getPrepaidPricePlanID(), 
	    			ExtensionApiSupport.class);
	    	crmGroupPricePlanExtension.setPrepaidPricePlanID(groupPricePlanExtension.getPrepaidPricePlanID());
	    }
	    else if (account.getSystemType()==SubscriberTypeEnum.POSTPAID)
	    {
	    	RmiApiErrorHandlingSupport.validateMandatoryObject(groupPricePlanExtension.getPostpaidPricePlanID(), 
	    			"GroupPricePlanAccountExtension.postpaidPricePlanID");
	    	RmiApiErrorHandlingSupport.validatePricePlanWithBillingType(ctx, SubscriberTypeEnum.POSTPAID, groupPricePlanExtension.getPostpaidPricePlanID(), 
	    			ExtensionApiSupport.class);
			crmGroupPricePlanExtension.setPostpaidPricePlanID(groupPricePlanExtension.getPostpaidPricePlanID());
	    }
	    else
	    {
	    	RmiApiErrorHandlingSupport.simpleValidation("systemType", "Unsupported " + SystemType.class.getName() + " "
	                + account.getSystemType());
	    }
	    
	    crmGroupPricePlanExtension.setBAN(account.getBAN());
	    crmGroupPricePlanExtension.setSpid(account.getSpid());
    }
    
    /**
     * Updates Subscriber Limit Extension for an account
     * @param ctx
     * @param subscriberLimitReference
     * @param subscriberLimitExtension
     * @param account
     * @throws CRMExceptionFault
     */
    public static void updateSubscriberLimitExtension(final Context ctx,
    		final com.redknee.util.crmapi.wsdl.v2_2.types.account.extensions.SubscriberLimitAccountExtensionReference subscriberLimitReference,
    		final com.redknee.util.crmapi.wsdl.v3_0.types.account.extensions.MutableSubscriberLimitAccountExtension subscriberLimitExtension,
    		com.redknee.app.crm.bean.Account account) throws CRMExceptionFault
    {
    	try
    	{
    		RmiApiErrorHandlingSupport.assertExtensionUpdateEnabled(ctx, SubscriberLimitExtension.class, ExtensionApiSupport.class);

    		List<SubscriberLimitExtension> crmSubscriberLimitExtensions = ExtensionSupportHelper.get(ctx).getExtensions(ctx, SubscriberLimitExtension.class, new EQ(AccountExtensionXInfo.BAN, subscriberLimitReference.getAccountID()));
    		boolean alreadyExists = true;
    		
    		if(crmSubscriberLimitExtensions.isEmpty())
    		{
    			alreadyExists = false;
    			try
    			{
    				crmSubscriberLimitExtensions.add(XBeans.instantiate(SubscriberLimitExtension.class, ctx));
    			}
    			catch(Exception e)
    			{
    				LogSupport.minor(ctx, ExtensionApiSupport.class,
                        "Error instantiating new Subscriber Limit extension.  Using default constructor.");
    				crmSubscriberLimitExtensions.add(new SubscriberLimitExtension());
    			}
    		}
    		
    		for(SubscriberLimitExtension crmSubscriberLimitExtension : crmSubscriberLimitExtensions)
    		{
    			apiToCrmSubscriberLimitExtensionMapping(ctx, crmSubscriberLimitExtension, subscriberLimitExtension, account);
    			
    			Home extensionHome = ExtensionSupportHelper.get(ctx).getExtensionHome(ctx, crmSubscriberLimitExtension);
            	extensionHome = ApiSupport.injectAPIUpdateEntityHomeIntoPipeline(ctx, extensionHome);
            	
            	if(extensionHome != null)
            	{
            		ctx.put(AccountConstants.OLD_ACCOUNT, account);
            		if(alreadyExists)
            		{
            			extensionHome.store(ctx, crmSubscriberLimitExtension);
            		}
            		else
            		{
            			extensionHome.create(ctx, crmSubscriberLimitExtension);
            		}
            	}
            	else
            	{
            	    final String msg = "Account Extension type not supported: "
                            + crmSubscriberLimitExtension.getClass().getName();
            	    
            	    if(alreadyExists)
            	    	RmiApiErrorHandlingSupport.handleStoreExceptions(ctx, null, msg, ExtensionApiSupport.class);
            	    else
            	    	RmiApiErrorHandlingSupport.handleCreateExceptions(ctx, null, msg, false, crmSubscriberLimitExtension.getClass(), null, ExtensionApiSupport.class);
            	    
            	}
    		}
    	}
    	catch(CRMExceptionFault crmEx)
    	{
    		throw crmEx;
		} 
    	catch (Exception e) 
    	{
            final String msg = "Unable to update Account Extension";
            RmiApiErrorHandlingSupport.handleStoreExceptions(ctx, e, msg, ExtensionApiSupport.class);
		}
    }
    
    /**
     * Maps API to CRM Subscriber Limit Extension for an account
     * @param ctx
     * @param crmSubscriberLimitExtension
     * @param subscriberLimitExtension
     * @param account
     * @throws CRMExceptionFault
     */
    
    public static void apiToCrmSubscriberLimitExtensionMapping(
    		final Context ctx, SubscriberLimitExtension crmSubscriberLimitExtension,
    		com.redknee.util.crmapi.wsdl.v3_0.types.account.extensions.MutableSubscriberLimitAccountExtension subscriberLimitExtension,
    		com.redknee.app.crm.bean.Account account) throws CRMExceptionFault
    {
    	RmiApiErrorHandlingSupport.validateMandatoryObject(subscriberLimitExtension.getMaxSubscribers(), 
    			"SubscriberLimitExtension.maxSubscribers");
    	
    	int maxSubscribers = subscriberLimitExtension.getMaxSubscribers();
    	
    	RmiApiErrorHandlingSupport.validateLimitInput(ctx, maxSubscribers, 9999);
    	
    	crmSubscriberLimitExtension.setMaxSubscribers(maxSubscribers);
    	crmSubscriberLimitExtension.setSpid(account.getSpid());
    	crmSubscriberLimitExtension.setBAN(account.getBAN());    	
    }

    /**
     * Updates or adds Friends and Family Extension to an account
     * @param ctx
     * @param fnfAccountReference
     * @param fnfExtension
     * @param account
     * @throws CRMExceptionFault
     */
    public static void updateFriendsAndFamilyExtension(final Context ctx,
    		final com.redknee.util.crmapi.wsdl.v2_2.types.account.extensions.FriendsAndFamilyAccountExtensionReference fnfAccountReference,
    		final com.redknee.util.crmapi.wsdl.v3_0.types.account.extensions.MutableFriendsAndFamilyAccountExtension fnfExtension,
    		com.redknee.app.crm.bean.Account account) throws CRMExceptionFault
    {
    	try
    	{
    		RmiApiErrorHandlingSupport.assertExtensionUpdateEnabled(ctx, FriendsAndFamilyExtension.class, ExtensionApiSupport.class);

    		List<FriendsAndFamilyExtension> crmFnFExtensions = ExtensionSupportHelper.get(ctx).getExtensions(ctx, FriendsAndFamilyExtension.class, new EQ(AccountExtensionXInfo.BAN, fnfAccountReference.getAccountID()));
    		boolean alreadyExists = true;
    		
    		if(crmFnFExtensions.isEmpty())
    		{
    			alreadyExists = false;
    			try
    			{
    				crmFnFExtensions.add(XBeans.instantiate(FriendsAndFamilyExtension.class, ctx));
    			}
    			catch(Exception e)
    			{
    				LogSupport.minor(ctx, ExtensionApiSupport.class,
                        "Error instantiating new Friends And Family account extension.  Using default constructor.");
    				crmFnFExtensions.add(new FriendsAndFamilyExtension());
    			}
    		}
    		
    		for(FriendsAndFamilyExtension crmFnFExtension : crmFnFExtensions)
    		{
    			apiToCrmFriendsAndFamilyExtensionMapping(ctx, crmFnFExtension, fnfExtension, account);
    			
    			Home extensionHome = ExtensionSupportHelper.get(ctx).getExtensionHome(ctx, crmFnFExtension);
            	extensionHome = ApiSupport.injectAPIUpdateEntityHomeIntoPipeline(ctx, extensionHome);
            	
            	if(extensionHome != null)
            	{
            		ctx.put(AccountConstants.OLD_ACCOUNT, account);
            		if(alreadyExists)
            		{
            			extensionHome.store(ctx, crmFnFExtension);
            		}
            		else
            		{
            			extensionHome.create(ctx, crmFnFExtension);
            		}
            	}
            	else
            	{
            	    final String msg = "Account Extension type not supported: "
                            + crmFnFExtension.getClass().getName();
            	    
            	    if(alreadyExists)
            	    	RmiApiErrorHandlingSupport.handleStoreExceptions(ctx, null, msg, ExtensionApiSupport.class);
            	    else
            	    	RmiApiErrorHandlingSupport.handleCreateExceptions(ctx, null, msg, false, crmFnFExtension.getClass(), null, ExtensionApiSupport.class);
            	}
    		}
    	}
    	catch(CRMExceptionFault crmEx)
    	{
    		throw crmEx;
		} 
    	catch (Exception e) 
    	{
            final String msg = "Unable to update Account Extension";
            RmiApiErrorHandlingSupport.handleStoreExceptions(ctx, e, msg, ExtensionApiSupport.class);
		}
    }
    
    /**
     * Maps API to CRM Friends and Family Extension for an account
     * @param ctx
     * @param crmFnFExtension
     * @param fnfExtension
     * @param account
     * @throws CRMExceptionFault
     */
    public static void apiToCrmFriendsAndFamilyExtensionMapping(
    		final Context ctx, FriendsAndFamilyExtension crmFnFExtension,
    		com.redknee.util.crmapi.wsdl.v3_0.types.account.extensions.MutableFriendsAndFamilyAccountExtension fnfExtension,
    		com.redknee.app.crm.bean.Account account) throws CRMExceptionFault
    {
    	crmFnFExtension.setBAN(account.getBAN());
    	crmFnFExtension.setSpid(account.getSpid());
    	
    	if(fnfExtension.getCugOwnerMSISDN()!=null && !fnfExtension.getCugOwnerMSISDN().isEmpty())
    	{
    		crmFnFExtension.setCugOwnerMsisdn(fnfExtension.getCugOwnerMSISDN());
    	}
    	if(fnfExtension.getCugTemplateID()!=null)
    	{
    		crmFnFExtension.setCugTemplateID(fnfExtension.getCugTemplateID());
    	}
    	if(fnfExtension.getSmsNotificationMSISDN()!=null && !fnfExtension.getSmsNotificationMSISDN().isEmpty())
    	{
    		crmFnFExtension.setSmsNotificationMSISDN(fnfExtension.getSmsNotificationMSISDN());
    	}
    }

}