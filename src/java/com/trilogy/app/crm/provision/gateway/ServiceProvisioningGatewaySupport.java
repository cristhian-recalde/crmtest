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
package com.trilogy.app.crm.provision.gateway;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.trilogy.app.crm.bas.recharge.SuspensionSupport;
import com.trilogy.app.crm.bean.AuxiliaryServiceTypeEnum;
import com.trilogy.app.crm.bean.ChargedItemTypeEnum;
import com.trilogy.app.crm.bean.KeyConfiguration;
import com.trilogy.app.crm.bean.ServiceBase;
import com.trilogy.app.crm.bean.ServiceTypeEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberAuxiliaryService;
import com.trilogy.app.crm.bean.SubscriberServices;
import com.trilogy.app.crm.bean.core.AuxiliaryService;
import com.trilogy.app.crm.bean.core.Service;
import com.trilogy.app.crm.bean.externalapp.ExternalAppEnum;
import com.trilogy.app.crm.bean.service.HistoryEventTypeEnum;
import com.trilogy.app.crm.bean.service.ServiceProvisionActionEnum;
import com.trilogy.app.crm.bean.service.ServiceStateEnum;
import com.trilogy.app.crm.calculator.ValueCalculator;
import com.trilogy.app.crm.extension.auxiliaryservice.core.SPGAuxSvcExtension;
import com.trilogy.app.crm.grr.GrrGeneratorGeneralConfig;
import com.trilogy.app.crm.hlr.HlrSupport;
import com.trilogy.app.crm.provision.CommonProvisionAgentBase;
import com.trilogy.app.crm.provision.ProvisionAgentException;
import com.trilogy.app.crm.provision.service.ParameterTypeEnum;
import com.trilogy.app.crm.provision.service.param.CommandID;
import com.trilogy.app.crm.provision.service.param.ParameterID;
import com.trilogy.app.crm.provision.service.param.ProvisionEntityType;
import com.trilogy.app.crm.subscriber.subscription.history.SubscriberSubscriptionHistorySupport;
import com.trilogy.app.crm.support.BeanLoaderSupport;
import com.trilogy.app.crm.support.BeanLoaderSupportHelper;
import com.trilogy.app.crm.support.DefaultConfigChangeRequestSupport;
import com.trilogy.app.crm.support.ExtensionSupportHelper;
import com.trilogy.app.crm.support.ExternalAppSupportHelper;
import com.trilogy.app.crm.support.FrameworkSupportHelper;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.app.crm.bean.SysFeatureCfg;
import com.trilogy.app.crm.support.Lookup;
import com.trilogy.app.crm.support.SubscriberAuxiliaryServiceSupport;
import com.trilogy.app.crm.support.SubscriberServicesSupport;
import com.trilogy.app.crm.support.UserSupport;
import com.trilogy.app.crm.util.SubscriberServicesUtil;
import com.trilogy.framework.xhome.beans.xi.PropertyInfo;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.webcontrol.HTMLExceptionListener;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MajorLogMsg;
import com.trilogy.framework.xlog.log.MinorLogMsg;
import com.trilogy.util.snippet.log.Logger;


/**
 * 
 *
 * @author victor.stratan@redknee.com
 * @since 
 */
public class ServiceProvisioningGatewaySupport
{
    public static final int KEY = 0;
    public static final int ALL = 1;
    public static Long HLR_SERVICE_SPG_SERVICE_ID = 3L;
    public static long VM_SERVICE_SPG_SERVICE_ID = 1L;
    public static final String PROVISION_ENTITY_TYPE = "PROVISION_ENTITY_TYPE"; 


    private static void serviceToAdd(final Context ctx, final Long spgType, final ServiceBase service)
    {
        SPGServiceProvisionCollector collector = currentCollector(ctx);
        collector.addList.add(service.getID());
        collector.SPGServiceIDList.add(spgType); 
        collector.servicesMap.put(service.getID(), service);
    }
    
    private static void serviceToRemove(final Context ctx, final Long spgType, final ServiceBase service)
    {
        SPGServiceProvisionCollector collector = currentCollector(ctx);
        collector.removeList.add(service.getID());
        collector.SPGServiceIDList.add(spgType); 
        collector.servicesMap.put(service.getID(), service);
    }
    
    private static void serviceToCurrent(final Context ctx, final Long spgType, ServiceBase service)
    {
        SPGServiceProvisionCollector collector = currentCollector(ctx);
        collector.currentList.add(service.getID());
        collector.SPGServiceIDList.add(spgType); 
        collector.servicesMap.put(service.getID(), service);
    }

    private static SPGServiceProvisionCollector currentCollector(final Context ctx)
    {
        return (SPGServiceProvisionCollector) ctx.get(SPGServiceProvisionCollector.class);
    }

    private static void addSPGAuxiliaryToCurrent(final Context ctx, final List<SubscriberAuxiliaryService> auxSrvList)
    {
        for (SubscriberAuxiliaryService subAuxSrv : auxSrvList)
        {
            try
            {
                final AuxiliaryService auxSrv = subAuxSrv.getAuxiliaryService(ctx);
                if (auxSrv != null && AuxiliaryServiceTypeEnum.SERVICE_PROVISIONING_GATEWAY.equals(auxSrv.getType()))
                {
                    SPGAuxSvcExtension spgAuxSvcExtension = ExtensionSupportHelper.get(ctx).getExtension(ctx, auxSrv, SPGAuxSvcExtension.class);
                    if (spgAuxSvcExtension!=null)
                    {
                        serviceToCurrent(ctx, Long.valueOf(spgAuxSvcExtension.getSPGServiceType()), auxSrv);
                    }
                    else 
                    {
                        LogSupport.minor(ctx, ServiceProvisioningGatewaySupport.class,
                                "Unable to find required extension of type '" + SPGAuxSvcExtension.class.getSimpleName()
                                        + "' for auxiliary service " + auxSrv.getIdentifier());
                    }
                }
            }
            catch (HomeException e)
            {
                Logger.minor(ctx, ServiceProvisioningGatewaySupport.class,
                        "Unable to retrieve Auxiliary Service [" + subAuxSrv.getAuxiliaryServiceIdentifier()
                        + "] for subscriber [" + subAuxSrv.getSubscriberIdentifier() + "]", e);
            }
        }
    }

    private static void addSPGToCurrent(final Context ctx, final Subscriber subscriber)
    {
        try
        {
            final Collection<SubscriberServices> subSrvList = subscriber.computeProvisionServices(ctx, subscriber.getElegibleForProvision(ctx));
            for (SubscriberServices subSrv : subSrvList)
            {
                final Service srv = subSrv.getService(ctx);
                if (srv != null && ServiceTypeEnum.SERVICE_PROVISIONING_GATEWAY.equals(srv.getType()))
                {
                    serviceToCurrent(ctx, Long.valueOf(srv.getSPGServiceType()), srv);
                }
            }
        }
        catch (HomeException e)
        {
            Logger.minor(ctx, ServiceProvisioningGatewaySupport.class,
                    "Unable to retrieve eligible Services for subscriber ["
                    + subscriber.getId() + "]", e);
        }
    }

    private static ServiceProvisionGatewayClient getClient(final Context ctx)
    {
        return (ServiceProvisionGatewayClient) ctx.get(ServiceProvisionGatewayClient.class);
    }

    public static Map<Integer, String> collectParameters(final Context ctx, final Collection<SPGParameter> paramSet,
            final Subscriber sub)
    {
        Context sCtx = ctx.createSubContext();
        if (sub != null)
        {
            // we do this because the passed in subscriber may be the old subscriber
            // which is different from the one under the Subscriber.class key in the context
            sCtx.put(Subscriber.class, sub);
        }

        // Set the bean loader map for the SPG calculators
        BeanLoaderSupport beanLoaderSupport = BeanLoaderSupportHelper.get(sCtx);
        Map<Class, Collection<PropertyInfo>> subMap = beanLoaderSupport.getBeanLoaderMap(sCtx, Subscriber.class);
       
        Map<Class, Collection<PropertyInfo>> spgParamMap = beanLoaderSupport.getBeanLoaderMap(sCtx, SPGParameter.class);
        Map<Class, Collection<PropertyInfo>> beanLoaderMap = new HashMap<Class, Collection<PropertyInfo>>();
        beanLoaderSupport.mergeBeanLoaderMaps(beanLoaderMap, subMap, spgParamMap);
        beanLoaderSupport.setBeanLoaderMap(sCtx, Collections.unmodifiableMap(beanLoaderMap));
        
        final Map<Integer, String> result = new HashMap<Integer, String>();
        for (SPGParameter spgParameter : paramSet)
        {
            sCtx.put(SPGParameter.class, spgParameter);
            
            // Get a value calculator that supports Subscriber and SPGParameter in context as base case
            KeyConfiguration keyConfig = null;
            try
            {
                keyConfig = HomeSupportHelper.get(ctx).findBean(ctx, KeyConfiguration.class, spgParameter.getKeyConfigID());
            }
            catch (HomeException e)
            {
                String msg = "Error retrieving key configuration " + spgParameter.getKeyConfigID();
                if (LogSupport.isDebugEnabled(sCtx))
                {
                    new DebugLogMsg(ServiceProvisioningGatewaySupport.class, msg, e).log(sCtx);
                }
                new MinorLogMsg(ServiceProvisioningGatewaySupport.class, msg, null).log(ctx);
            }

            ValueCalculator calc = null;
            if (keyConfig != null)
            {
                calc = keyConfig.getValueCalculator();
            }
            
            Object value = null;
            if (calc != null)
            {
                value = calc.getValue(sCtx);
            }
            
            if (value instanceof com.redknee.framework.xhome.xenum.AbstractEnum)
            {
                value =  Short.valueOf(((com.redknee.framework.xhome.xenum.AbstractEnum) value).getIndex());
            }
            
            if (value != null)
            {
                result.put(Integer.valueOf(spgParameter.getID()), String.valueOf(value));
            }
        }
        
        // provision type must be provided until the IDL is update to replace id with 
        // id entity, current it is a long, so we can not mix provision of different types. 
        if (ctx.has(CommonProvisionAgentBase.SPG_PROVISIONING_CUSTOM_PROVISION_ENTITY_TYPE))
        {
            //PARAMETERID.PROVISION_ENTITY_TYPE
            result.put(Integer.valueOf(ParameterID.PROVISION_ENTITY_TYPE), (String) ctx.get(CommonProvisionAgentBase.SPG_PROVISIONING_CUSTOM_PROVISION_ENTITY_TYPE)); 
            
        }
        
        result.put(ParameterID.PRICEPLAN_SERVICES, HlrSupport.getPricePlanServiceIdString(ctx, sub));
        
        // For other provisioning call flows these params are not required, hence empty strings for them
        result.put(ParameterID.OSL_ID, (String) ctx.get(ParameterID.OSL_ID," "));
        result.put(ParameterID.NSL_ID, (String) ctx.get(ParameterID.NSL_ID," "));
        SysFeatureCfg sysFeatureCfg = (SysFeatureCfg)ctx.get(SysFeatureCfg.class);
        result.put(ParameterID.VOICEMAIL_RESET_PIN_CMD, (String)sysFeatureCfg.getVoiceMailPinresetCmd());
        result.put(ParameterID.MSISDN_DELETION_CMD, getExtMsisdnDeletionCmd(ctx, sub.getSpid()));

        //populate user-id of the API invoker
        setUserIdInParameterMap(ctx, result);
        
        return result;
    }

    public static void setUserIdInParameterMap(Context ctx, Map<Integer, String> result)
    {
    	String userId = (String) ctx.get(DefaultConfigChangeRequestSupport.API_SOURCE_USERNAME);
        if(userId != null && !userId.isEmpty())
        {//API is invoked at the CRM's RMI interface.
        	if(Logger.isDebugEnabled(ctx))
        	{
        		Logger.debug(ctx, ServiceProvisioningGatewaySupport.class, "User-Id recevied in " +
        				"context key [" + DefaultConfigChangeRequestSupport.API_SOURCE_USERNAME + "]");
        	}
        }
        else
        {//API is invoked from GUI
        	if(Logger.isDebugEnabled(ctx))
        	{
        		Logger.debug(ctx, ServiceProvisioningGatewaySupport.class, "Trying to populate user-id from the session");
        	}
        	userId = UserSupport.getUserName(ctx);
        }
        if(userId == null || userId.isEmpty())
    	{
        	//This can happen only if this API is invoked from System processes like Dunning and Price Plan Provisioning.
        	if(Logger.isDebugEnabled(ctx))
        	{
        		Logger.debug(ctx, ServiceProvisioningGatewaySupport.class, "User-id not present in the session." +
        				" Setting userId to " + UserSupport.SYSTEM_USERNAME);
        	}
        	userId = UserSupport.SYSTEM_USERNAME;
    	}
    	result.put(ParameterID.USER_ID, userId);
    }
    
    public static Collection<SPGParameter>[] collectParameterDefinitions(final Context ctx,
            final Set<Long> serviceList)
    {
        final Collection<SPGParameter>[] result = new Collection[2];
        Map<Integer, SPGParameter> keyParameterMap = new HashMap<Integer, SPGParameter>();
        Map<Integer, SPGParameter> parameterMap = new HashMap<Integer, SPGParameter>();
        final Home home = (Home) ctx.get(SPGServiceHome.class);
        for (Long srvID : serviceList)
        {
            final EQ condition = new EQ(SPGServiceXInfo.ID, srvID);
            try
            {
                final SPGService service = (SPGService) home.find(ctx, condition);
                if (service != null)
                {
                    for(SPGParameter param : (List<SPGParameter>) service.getParameters())
                    {
                        Integer key = Integer.valueOf(param.getID());
                        if (!parameterMap.containsKey(key))
                        {
                            parameterMap.put(key, param);
                        }
                        if (param.getParameterType() == ParameterTypeEnum.KEY_INDEX
                            && !keyParameterMap.containsKey(key))
                        {
                            keyParameterMap.put(key, param);
                        }
                    }
                }
                else
                {
                    Logger.major(ctx, ServiceProvisioningGatewaySupport.class,
                            "Data integrity Error occurred in collectParameterDefinitions(): "
                            + "missing Provisioning Gateway service ID [" + srvID + "]");
                }
            }
            catch (HomeException e)
            {
                Logger.minor(ctx, ServiceProvisioningGatewaySupport.class,
                        "Error occurred in collectParameterDefinitions(): " + e.getMessage(), e);
            }
        }

        result[KEY] = keyParameterMap.values();
        result[ALL] = parameterMap.values();

        return result;
    }

    private static boolean valuesEquals(final Map<Integer, String> values, final Map<Integer, String> oldValues)
    {
        final Iterator<Map.Entry<Integer, String>> it = values.entrySet().iterator();       
        for ( ; it.hasNext(); )
        {
            final Map.Entry<Integer, String> entry = it.next();
            final Integer key = entry.getKey();
            final String value = entry.getValue();
            if (key !=ParameterID.PRICEPLAN_SERVICES && !value.equals(oldValues.get(key)))
            {
                return false;
            }
        }
        return true;
    }

    public static int provision(final Context ctx, final Subscriber subscriber)
    {
        final SPGServiceProvisionCollector collector = currentCollector(ctx);
        collector.currentList.addAll(collector.addList);
        collector.currentList.addAll(collector.removeList);

        final Collection<SPGParameter>[] params = collectParameterDefinitions(ctx, collector.SPGServiceIDList);
        Map<Integer, String> values = collectParameters(ctx, params[1], subscriber);
        return provision(ctx, collector, values, subscriber);
    }

    protected static int provision(final Context ctx, final SPGServiceProvisionCollector collector,
            final Map<Integer, String> values, Subscriber subscriber)
    {
        int ret = 0; 
        
        if (collector.hasProvisionActions())
        {
            final ServiceProvisionGatewayClient client = getClient(ctx);
            try
            {
                client.provision(ctx, collector.removeList, collector.addList, values, subscriber);
                handleReturnForProvision(ctx,collector, null);
            }
            catch (ServiceProvisionGatewayException e)
            {
                ret = e.getResultCode(); 
                handleReturnForProvision(ctx,collector, e); 
            }
        }
        
        return ret; 
    }


    /**
     * 
     * 
     * @param ctx
     * @param subscriber
     */
    public static int updateSingleService(final Context ctx, final Subscriber subscriber, final ServiceBase service,
            final Long spgServiceId )
    {
        
        final SPGServiceProvisionCollector collector =  new SPGServiceProvisionCollector();
        ctx.put(SPGServiceProvisionCollector.class, collector);
        serviceToCurrent(ctx, spgServiceId, service); 
        collector.newSub = subscriber; 
        
        final Collection<SPGParameter>[] params = collectParameterDefinitions(ctx, collector.SPGServiceIDList);
        Map<Integer, String> values = collectParameters(ctx, params[ALL], subscriber);

        final Subscriber oldSubscriber = (Subscriber) ctx.get(Lookup.OLDSUBSCRIBER);
        Map<Integer, String> oldValues = collectParameters(ctx, params[ALL], oldSubscriber);

        if (!valuesEquals(values, oldValues) || (ctx.has(ctx, SuspensionSupport.SUSPEND_GATEWAY_SERVICE)) || 
                ctx.has(ctx, SuspensionSupport.UNSUSPEND_GATEWAY_SERVICE))
        {
            final Map<Integer, String> keyValues = collectParameters(ctx, params[KEY], oldSubscriber);
            // this will only update service and auxiliary of gateway type. 
            return  update(ctx, collector, keyValues, values, subscriber);
        }
        
        return 0; 
    }
    

    /**
     * 
     * This method is ambiguous, since spg don't need know the context of updating, 
     * bss should use execute method to give spg exact command to update. otherwise, 
     * we need pass the context information, such as old value to SPG. 
     * 
     * This method on SPG side is supported only by some VM drivers, it is not supported by
     * others. this mehtod should be removed in future. 
     * 
     * @param ctx
     * @param subscriber
     */
    public static int updateServices(final Context ctx, final Subscriber subscriber)
    {
        
        final SPGServiceProvisionCollector collector = new SPGServiceProvisionCollector();
        ctx.put(SPGServiceProvisionCollector.class, collector);
        addSPGToCurrent(ctx, subscriber);
        collector.newSub = subscriber;

        final Collection<SPGParameter>[] params = collectParameterDefinitions(ctx, collector.SPGServiceIDList);
        Map<Integer, String> values = collectParameters(ctx, params[ALL], subscriber);
        

        final Subscriber oldSubscriber = (Subscriber) ctx.get(Lookup.OLDSUBSCRIBER);
        Map<Integer, String> oldValues = collectParameters(ctx, params[ALL], oldSubscriber);

        if (!valuesEquals(values, oldValues) || (ctx.has(ctx, SuspensionSupport.SUSPEND_GATEWAY_SERVICE)) || 
                ctx.has(ctx, SuspensionSupport.UNSUSPEND_GATEWAY_SERVICE))
        {
            final Map<Integer, String> keyValues = collectParameters(ctx, params[KEY], oldSubscriber);
            // this will only update service and auxiliary of gateway type. 
            return  update(ctx, collector, keyValues, values, subscriber);
        }
        
        return 0; 
    }
    
    
    /**
     * 
     * This method is ambiguous, since spg don't need know the context of updating, 
     * bss should use execute method to give spg exact command to update. otherwise, 
     * we need pass the context information, such as old value to SPG. 
     * 
     * This method on SPG side is supported only by some VM drivers, it is not supported by
     * others. this mehtod should be removed in future. 
     * 
     *
     * @param ctx
     * @param subscriber
     */
    public static int updateAuxiliaryServices(final Context ctx, final Subscriber subscriber)
    {
        
        final SPGServiceProvisionCollector collector = new SPGServiceProvisionCollector();
        ctx.put(SPGServiceProvisionCollector.class, collector);
        addSPGAuxiliaryToCurrent(ctx, subscriber.getAuxiliaryServices(ctx));
        collector.newSub = subscriber;

        final Collection<SPGParameter>[] params = collectParameterDefinitions(ctx, collector.SPGServiceIDList);
        Map<Integer, String> values = collectParameters(ctx, params[ALL], subscriber);


        final Subscriber oldSubscriber = (Subscriber) ctx.get(Lookup.OLDSUBSCRIBER);
        Map<Integer, String> oldValues = collectParameters(ctx, params[ALL], oldSubscriber);
       
        if (!valuesEquals(values, oldValues) || (ctx.has(ctx, SuspensionSupport.SUSPEND_GATEWAY_SERVICE)) || 
                ctx.has(ctx, SuspensionSupport.UNSUSPEND_GATEWAY_SERVICE))
        {
            final Map<Integer, String> keyValues = collectParameters(ctx, params[KEY], oldSubscriber);
            // this will only update service and auxiliary of gateway type. 
            return  update(ctx, collector, keyValues, values, subscriber);
        }
        
        return 0; 
    }
    /**
     * 
     * This method is ambiguous, since spg don't need know the context of updating, 
     * bss should use execute method to give spg exact command to update. otherwise, 
     * we need pass the context information, such as old value to SPG. 
     * 
     * This method on SPG side is supported only by some VM drivers, it is not supported by
     * others. this mehtod should be removed in future. 
     * 
     * @deprecated
     * @param ctx
     * @param subscriber
     */
    public static int update(final Context ctx, final Subscriber subscriber)
    {
        
        final SPGServiceProvisionCollector collector = currentCollector(ctx);
        doubleComplement(collector.addList, collector.removeList);
        addSPGToCurrent(ctx, subscriber);
        addSPGAuxiliaryToCurrent(ctx, subscriber.getAuxiliaryServices(ctx));

        final Collection<SPGParameter>[] removeParams = collectParameterDefinitions(ctx, collector.removeList);
        Map<Integer, String> withRemoveValues = collectParameters(ctx, removeParams[KEY], subscriber);

        final Collection<SPGParameter>[] params = collectParameterDefinitions(ctx, collector.currentList);
        Map<Integer, String> values = collectParameters(ctx, params[ALL], subscriber);

        withRemoveValues.putAll(values);
        provision(ctx, collector, withRemoveValues, subscriber);

        final Subscriber oldSubscriber = (Subscriber) ctx.get(Lookup.OLDSUBSCRIBER);
        Map<Integer, String> oldValues = collectParameters(ctx, params[ALL], oldSubscriber);

        if (!valuesEquals(values, oldValues) || (ctx.has(ctx, SuspensionSupport.SUSPEND_GATEWAY_SERVICE)) || 
                ctx.has(ctx, SuspensionSupport.UNSUSPEND_GATEWAY_SERVICE))
        {
            final Map<Integer, String> keyValues = collectParameters(ctx, params[KEY], oldSubscriber);
            // this will only update service and auxiliary of gateway type. 
            return  update(ctx, collector, keyValues, values, subscriber);
        }
        
        return 0; 
    }

    private static int update(final Context ctx, final SPGServiceProvisionCollector collector,
            final Map<Integer, String> keyValues, final Map<Integer, String> values,
            final Subscriber subscriber)
    {
        int ret =0; 
        final ServiceProvisionGatewayClient client = getClient(ctx);
        try
        {
            client.update(ctx, collector.currentList, keyValues, values, subscriber);
            handleReturnForUpdate(ctx, collector, null);
        }
        catch (ServiceProvisionGatewayException e)
        {
            ret = e.getResultCode(); 
            handleReturnForUpdate(ctx, collector, e);
        } 

        return ret; 
    }

    public static long execute(final Context ctx, final int command, final long serviceID,
            final Map<Integer, String> values, Subscriber subscriber)
    {
        Boolean hlrOnly = ctx.getBoolean(SuspensionSupport.HLR_ONLY, false); 
        return execute(ctx, command, serviceID, values, subscriber, hlrOnly);
    }   
        
    public static long execute(final Context ctx, final int command, final long serviceID,
                final Map<Integer, String> values, Subscriber subscriber, boolean hlrOnly)
    {
        final ServiceProvisionGatewayClient client = getClient(ctx);
        try
        {
            client.execute(ctx, command, serviceID, values, subscriber);
            if(!hlrOnly)
            {
                handleReturnForExecute(ctx, subscriber, command, serviceID, null);
            }
        }
        catch (ServiceProvisionGatewayException e)
        {
            if(!hlrOnly)
            {
                handleReturnForExecute(ctx, subscriber, command, serviceID, e);
            }
            return e.getResultCode(); 
        }
        
        return 0; 
    }

    
    
    public static long resetPIN(final Context ctx, final Subscriber subscriber, final long serviceID)
    {
        final Set<Long> ids = new HashSet<Long>();
        ids.add(Long.valueOf(serviceID));
        
        ctx.put(CommonProvisionAgentBase.SPG_PROVISIONING_CUSTOM_PROVISION_ENTITY_TYPE, 
                String.valueOf(ProvisionEntityType.PROVISION_ENTITY_TYPE_SPG));

        final Collection<SPGParameter>[] params = collectParameterDefinitions(ctx, ids);
        final Map<Integer, String> values = collectParameters(ctx, params[ALL], subscriber);

        final String msisdn = subscriber.getMSISDN();
        final String pin = msisdn.substring(msisdn.length() - 4);
        values.put(ParameterID.VOICEMAIL_PIN, pin);

        long result = execute(ctx, CommandID.VOICEMAIL_PIN_RESET, serviceID, values, subscriber);
        return result;
    }
    
    /**
     * Calculate the compliment for both collections, hence the double.
     *
     * @param a first collection
     * @param b second collection
     */
    protected static void doubleComplement(final Set<Long> a, final Set<Long> b)
    {
        final Iterator<Long> it = a.iterator();
        for ( ; it.hasNext(); )
        {
            final Long id = it.next();
            if (b.remove(id))
            {
                it.remove();
            }
        }
    }
    

    public static int prepareAndSendIndividualServiceToSPG(Context ctx, Subscriber sub, final ServiceBase service,
            final Long spgServiceId, final boolean isProvision, Object caller)
    {
        int result = 0;
        final SPGServiceProvisionCollector collector = new SPGServiceProvisionCollector();
        if (sub != null)
        {
            try
            {
                Subscriber oldSub = (Subscriber) sub.clone();
                collector.oldSub = oldSub;
            }
            catch (CloneNotSupportedException ex)
            {
            }
            collector.newSub = sub;
        }
        ctx.put(SPGServiceProvisionCollector.class, collector);
        if (isProvision)
        {
            serviceToAdd(ctx, spgServiceId, //
                    service);
        }
        else
        {
            serviceToRemove(ctx, spgServiceId,// HLR Service on spg
                    service);
        }
        FrameworkSupportHelper.get(ctx).initExceptionListener(ctx, caller);
        result = provision(ctx, sub);
        checkForExceptionsOnHlr(ctx);
        return result;
    }


    private static int checkForExceptionsOnHlr(Context ctx)
    {
        int result = 0;
        // print the warnings to the screen if the screen is available
        final HTMLExceptionListener el = (HTMLExceptionListener) ctx.get(HTMLExceptionListener.class);
        if (el != null && el.hasErrors())
        {
            for (Iterator<Throwable> i = el.getExceptions().iterator(); i.hasNext();)
            {
                Throwable t = i.next();
                if (t instanceof ServiceProvisionGatewayException)
                {
                    ServiceProvisionGatewayException gatewayEx = (ServiceProvisionGatewayException) t;
                    result = gatewayEx.getResultCode();
                    new MinorLogMsg(ServiceProvisioningGatewaySupport.class, " Unable to provision to service gateway ", gatewayEx).log(ctx);
                }
                else if (t instanceof ProvisionAgentException)
                {
                	ProvisionAgentException gatewayEx = (ProvisionAgentException) t;
                    result = gatewayEx.getSourceResultCode();
                    new MinorLogMsg(ServiceProvisioningGatewaySupport.class, " Unable to provision to service gateway ", gatewayEx).log(ctx);
                }
            }
        }
        return result;
    }
    
    
    
    
    /**
     * this method is added just as a work around, the original errorhandling can only handle error when service is added or removed. 
     * it can not handle update, which service suspension is called. the main problem is that subscriberservicestate doesn't decouple the action and sattus, 
     * as a result the generic update method has to know what's update action the caller asked for in order to handle the error.     
     * @param ctx
     * @param collector
     * @param spgException
     */
        protected static void handleReturnForProvision(final Context ctx, 
                final SPGServiceProvisionCollector collector,
                ServiceProvisionGatewayException spgException)
        {
            
            if (spgException!=null)
            {
                Logger.minor(ctx, ServiceProvisioningGatewaySupport.class,
                        "Error while running provisoin() for SPG service: " + spgException.getMessage(), spgException);
  
            }
            
            if (spgException != null && spgException.getFailedServices() != null)
            {

                for (Long id : collector.removeList )
                {
                    if (spgException.getFailedServices().contains(id))
                    {
                        if(collector.newSub != null)
                        {
                        ServiceProvisioningGatewaySupport.updateSubscriberEntityState(ctx, collector.newSub,
                                collector.servicesMap.get(id), ServiceProvisionActionEnum.UNPROVISION, false);
                        SubscriberSubscriptionHistorySupport.addProvisioningRecord(ctx, collector.newSub,
                                HistoryEventTypeEnum.UNPROVISION, ChargedItemTypeEnum.SERVICE, 
                                collector.servicesMap.get(id), ServiceStateEnum.UNPROVISIONEDWITHERRORS);
                        }
                        notifyProvisionException(ctx, collector.servicesMap.get(id), true, spgException);
                    }
                    else
                    {
                        if(collector.newSub != null)
                        {
                        ServiceProvisioningGatewaySupport.updateSubscriberEntityState(ctx, collector.newSub,
                                collector.servicesMap.get(id), ServiceProvisionActionEnum.UNPROVISION, true);
                        }
                    }
                }
                
                for (Long id : collector.addList )
                {
                    if (spgException.getFailedServices().contains(id))
                    {
                        if(collector.newSub != null)
                        {
                        ServiceProvisioningGatewaySupport.updateSubscriberEntityState(ctx, collector.newSub,
                                collector.servicesMap.get(id), ServiceProvisionActionEnum.PROVISION, false);
                        SubscriberSubscriptionHistorySupport.addProvisioningRecord(ctx, collector.newSub,
                                HistoryEventTypeEnum.PROVISION, ChargedItemTypeEnum.SERVICE, 
                                collector.servicesMap.get(id), ServiceStateEnum.PROVISIONEDWITHERRORS);
                        }
                        notifyProvisionException(ctx, collector.servicesMap.get(id), true, spgException);
                    }
                    else
                    {
                        if(collector.newSub != null)
                        {
                        ServiceProvisioningGatewaySupport.updateSubscriberEntityState(ctx, collector.newSub,
                                collector.servicesMap.get(id), ServiceProvisionActionEnum.PROVISION, true);
                        }
                    }
                }
                
            }
            else if (spgException != null)
            {
                for (Long id : collector.removeList )
                {

                    if(collector.newSub != null)
                    {
                        ServiceProvisioningGatewaySupport.updateSubscriberEntityState(ctx, collector.newSub,
                                collector.servicesMap.get(id), ServiceProvisionActionEnum.UNPROVISION, false);
                        SubscriberSubscriptionHistorySupport.addProvisioningRecord(ctx, collector.newSub,
                                HistoryEventTypeEnum.UNPROVISION, ChargedItemTypeEnum.SERVICE, 
                                collector.servicesMap.get(id), ServiceStateEnum.UNPROVISIONEDWITHERRORS);
                    }
                        notifyProvisionException(ctx, collector.servicesMap.get(id), true, spgException);
                    
                }
                
                for (Long id : collector.addList )
                {
                    if(collector.newSub != null)
                    {
                        ServiceProvisioningGatewaySupport.updateSubscriberEntityState(ctx, collector.newSub,
                                collector.servicesMap.get(id), ServiceProvisionActionEnum.PROVISION, false);
                        SubscriberSubscriptionHistorySupport.addProvisioningRecord(ctx, collector.newSub,
                                HistoryEventTypeEnum.PROVISION, ChargedItemTypeEnum.SERVICE, 
                                collector.servicesMap.get(id), ServiceStateEnum.PROVISIONEDWITHERRORS);
                    }
                        notifyProvisionException(ctx, collector.servicesMap.get(id), true, spgException);
                    
                }
            } else   
            {    
                for (Long id : collector.removeList )
                {

                    if(collector.newSub != null)
                    {
                        ServiceProvisioningGatewaySupport.updateSubscriberEntityState(ctx, collector.newSub,
                                collector.servicesMap.get(id), ServiceProvisionActionEnum.UNPROVISION, true);
                    }
                }
                
                for (Long id : collector.addList )
                {
                    if(collector.newSub != null)
                    {
                        ServiceProvisioningGatewaySupport.updateSubscriberEntityState(ctx, collector.newSub,
                                collector.servicesMap.get(id), ServiceProvisionActionEnum.PROVISION, true);
                    }
                }
            }

           

        } 
        
        
        
/**
 * this method is added just as a work around, the original errorhandling can only handle error when service is added or removed. 
 * it can not handle update, which service suspension is called. the main problem is that subscriberservicestate doesn't decouple the action and sattus, 
 * as a result the generic update method has to know what's update action the caller asked for in order to handle the error.     
 * @param ctx
 * @param collector
 * @param spgException
 */
    protected static void handleReturnForUpdate(final Context ctx, final SPGServiceProvisionCollector collector,
            ServiceProvisionGatewayException spgException)
    {
        if (spgException!=null)
        {
            Logger.minor(ctx, ServiceProvisioningGatewaySupport.class,
                    "Error while running update() for SPG service: " + spgException.getMessage(), spgException);

        }
        
        ServiceProvisionActionEnum action = ServiceProvisionActionEnum.UPDATE_ATTRIBUTES; 
        
        // gateway type service suspension. 
        if (ctx.has(ctx, SuspensionSupport.SUSPEND_GATEWAY_SERVICE))
        {
            action = ServiceProvisionActionEnum.SUSPEND;
        } else if (      ctx.has(ctx, SuspensionSupport.UNSUSPEND_GATEWAY_SERVICE) )
        {
            action = ServiceProvisionActionEnum.RESUME; 
        }
        

        if (spgException != null && spgException.getFailedServices() != null)
        {

            for (Long id : collector.currentList )
            {
                if (spgException.getFailedServices().contains(id))
                {
                    if(collector.newSub != null)
                    {
                    ServiceProvisioningGatewaySupport.updateSubscriberEntityState(ctx, collector.newSub,
                            collector.servicesMap.get(id), action, false);
                    SubscriberSubscriptionHistorySupport.addProvisioningRecord(ctx, collector.newSub,
                            HistoryEventTypeEnum.PROVISION, ChargedItemTypeEnum.SERVICE, 
                            collector.servicesMap.get(id), ServiceStateEnum.PROVISIONEDWITHERRORS);
                    }
                    notifyProvisionException(ctx, collector.servicesMap.get(id), true, spgException);
                }
                else
                {
                    if(collector.newSub != null)
                    {
                    ServiceProvisioningGatewaySupport.updateSubscriberEntityState(ctx, collector.newSub,
                            collector.servicesMap.get(id), action, true);
                    }
                }
            }
                        
        }
        else if (spgException != null)
        {
            for (Long id : collector.currentList )
            {

                if(collector.newSub != null)
                {
                    ServiceProvisioningGatewaySupport.updateSubscriberEntityState(ctx, collector.newSub,
                            collector.servicesMap.get(id), action, false);
                    SubscriberSubscriptionHistorySupport.addProvisioningRecord(ctx, collector.newSub,
                            HistoryEventTypeEnum.PROVISION, ChargedItemTypeEnum.SERVICE, 
                            collector.servicesMap.get(id), ServiceStateEnum.PROVISIONEDWITHERRORS);
                }
                    notifyProvisionException(ctx, collector.servicesMap.get(id), true, spgException);
                
            }
            
            
        } else  
        {    
            for (Long id : collector.currentList )
            {
                if(collector.newSub != null)
                {

                    ServiceProvisioningGatewaySupport.updateSubscriberEntityState(ctx, collector.newSub,
                            collector.servicesMap.get(id), action, true);
                }
            }
            
           
        }

    } 
    
    
    protected static void handleReturnForExecute(final Context ctx, 
            Subscriber sub, 
            final int command, final long serviceID,
            ServiceProvisionGatewayException spgException)
    {
        
        if (spgException != null)
        {    
            Logger.minor(ctx, ServiceProvisioningGatewaySupport.class,
                "Error while running execute() for SPG service: " + spgException.getMessage(), spgException);
        }
        
        // hlr suspension command and resume command. 
        
        if (ctx.has(CommonProvisionAgentBase.SPG_PROVISIONING_CUSTOM_PROVISION_ENTITY_TYPE) &&
                ctx.get(CommonProvisionAgentBase.SPG_PROVISIONING_CUSTOM_PROVISION_ENTITY_TYPE).equals(String.valueOf(ProvisionEntityType.PROVISION_ENTITY_TYPE_SERVICE)) )
        {
            if (command == CommandID.SERVICE_RESUME)
            {
                if (spgException!=null)
                {
                    ServiceProvisioningGatewaySupport.updateSubscriberServiceState(ctx, sub,
                            serviceID, ServiceProvisionActionEnum.RESUME, false);
                    SubscriberSubscriptionHistorySupport.addProvisioningRecord(ctx, sub,
                            HistoryEventTypeEnum.PROVISION, ChargedItemTypeEnum.SERVICE, 
                            serviceID, ServiceStateEnum.PROVISIONEDWITHERRORS);
              
                }
                else 
                {
                    if(sub != null)
                    {
                        ServiceProvisioningGatewaySupport.updateSubscriberServiceState(ctx, sub,
                                serviceID, ServiceProvisionActionEnum.RESUME, true);
                        
                        SubscriberSubscriptionHistorySupport.addProvisioningRecord(ctx, sub,
                                HistoryEventTypeEnum.PROVISION, ChargedItemTypeEnum.SERVICE, 
                                serviceID, ServiceStateEnum.PROVISIONED);
                    }
  
                }
                
            } 
            else if (command ==CommandID.SERVICE_SUSPEND )
            {
                if (spgException!=null)
                {
                    if(sub != null)
                    {
                        ServiceProvisioningGatewaySupport.updateSubscriberServiceState(ctx, sub,
                                serviceID, ServiceProvisionActionEnum.SUSPEND, false);
                        SubscriberSubscriptionHistorySupport.addProvisioningRecord(ctx, sub,
                                HistoryEventTypeEnum.PROVISION, ChargedItemTypeEnum.SERVICE, 
                                serviceID, ServiceStateEnum.SUSPENDEDWITHERRORS);
                    }
              
                }
                else 
                {
                    if(sub != null)
                    {
                        ServiceProvisioningGatewaySupport.updateSubscriberServiceState(ctx, sub,
                                serviceID, ServiceProvisionActionEnum.SUSPEND, true);
      
                        SubscriberSubscriptionHistorySupport.addProvisioningRecord(ctx, sub,
                                HistoryEventTypeEnum.UNPROVISION, ChargedItemTypeEnum.SERVICE, 
                                serviceID, ServiceStateEnum.SUSPENDED);
                    }
                } 
            }
                
        }
            
                
        
    }   
    /*
    
    static private long[] converToLongArray(Collection<Long> list) 
    {
        long[] result = new long[list.size()];
        Iterator<Long> it = list.iterator();
        for (int i = 0; i < result.length; i++)
        {
            final Long number = it.next();
            result[i] = number.longValue();
        }
        return result;
    }
    
  */
  
   private static void notifyProvisionException(Context ctx, ServiceBase service, boolean provision, ServiceProvisionGatewayException e)
   {
       HTMLExceptionListener exps = (HTMLExceptionListener)ctx.get(HTMLExceptionListener.class);
       String message;
       
       
       if (provision)
       {
           message = ExternalAppSupportHelper.get(ctx).getProvisionErrorMessage(ctx, ExternalAppEnum.SPG, e.getResultCode(), service);
       }
       else
       {
           message = ExternalAppSupportHelper.get(ctx).getUnprovisionErrorMessage(ctx, ExternalAppEnum.SPG, e.getResultCode(), service);
       }

       ProvisionAgentException exception = new ProvisionAgentException(
               ctx, message, e.getResultCode(), ExternalAppEnum.SPG, e);

       
       
       if (exps!=null)
       {
           exps.thrown(exception);
       }
       else
       {
           FrameworkSupportHelper.get(ctx).notifyExceptionListener(
               ctx,
               exception);
       }
   }
   
    
   private static void updateSubscriberEntityState(final Context ctx, 
            Subscriber sub,
            ServiceBase service,  
            final ServiceProvisionActionEnum action,
            final boolean provisionResult)
   {
       if (service instanceof AuxiliaryService)
       {
           updateForSusbsscriberAuxiliaryService(ctx, sub, service, action, provisionResult);
       } else 
       {
           updateSubscriberServiceState(ctx, sub, service.getID(), action, provisionResult); 
       }
       
       
       
   }
   
   
    public static void updateForSusbsscriberAuxiliaryService(final Context ctx, 
            Subscriber sub,
            ServiceBase service,  
            final ServiceProvisionActionEnum action,
            final boolean provisionResult)
    {
        // A hack to prevent update on subauxserv update on provision and unprovision call.
        // This method is adding unnecessary entry in provisioning record and unnecessarily updating subscriber aux serv table for provision and unprovisoin call.
        // other updates should also be examined to find out if this update is requird for those. If not then this update should be removed.
        if(action == ServiceProvisionActionEnum.PROVISION || action == ServiceProvisionActionEnum.UNPROVISION)
        {
            return;
        }
             // gateway doesn't support secondary id for now, so secondory id should by 0.
            // it is major limitation. update value in subscriber and database. 
            SubscriberAuxiliaryService subAuxSvc = SubscriberAuxiliaryServiceSupport.getSelectedSubscriberAuxiliaryService(
                    SubscriberAuxiliaryServiceSupport.getSubscriberAuxiliaryServices(ctx, sub.getId()),
                service.getID(), -1); 
        
            if (subAuxSvc != null)
            {               
                subAuxSvc.setProvisionActionState(provisionResult);
                // auxiliary action is should be already update it is called. there is not unified 
                // mapping, for example, suspension could be mapped to update or unprovision. depends on 
                // auxiliary service type. 
                // subAuxSvc.setProvisionAction(action);
                
                subAuxSvc = SubscriberAuxiliaryServiceSupport.updateSubscriberAuxiliaryServiceOnXDBHomeDirectly(ctx, subAuxSvc); 
                // the cached subscriber auxiliary service in the subscriber is not update? do we need or not? 
                          
                
                SubscriberSubscriptionHistorySupport.addProvisioningRecord(ctx, sub,
                        HistoryEventTypeEnum.PROVISION, ChargedItemTypeEnum.AUXSERVICE, service, subAuxSvc.getProvisionedState());
            } else 
            {
                new MinorLogMsg(ServiceProvisioningGatewaySupport.class, " Unable to update subscriber axilairy service state for sub " +
                    sub.getId() + " service id = " +
                    service.getID(), null).log(ctx);
            }
    }
    
 
    
    /**
     * Create or overwrite the Subscriber Service record for the given subscriber and service identifier.
     * 
     * @param ctx
     * @param subscriber
     * @param serviceId
     * @param state
     */
    public static void updateSubscriberServiceState(final Context ctx,
            final Subscriber subscriber,
            final long serviceId,           
            final ServiceProvisionActionEnum action,
            final boolean provisionResult) 
    {
        SubscriberServices service = SubscriberServicesSupport.getSubscriberServiceRecord(ctx,
                subscriber.getId(),
                Long.valueOf(serviceId), SubscriberServicesUtil.DEFAULT_PATH);
        
        final ServiceStateEnum state = getServiceState(action, provisionResult);  
        
        if (service != null )
        {    
            service.setProvisionAction( action);
            service.setProvisionedState(state);
            service.setProvisionActionState(provisionResult); 

            try 
            {
                SubscriberServicesSupport.updateSubscriberServiceRecord(ctx, service);
            } catch (Exception e)
            {
                Logger.minor(ctx, ServiceProvisioningGatewaySupport.class, 
                        "fail to upadte subscriber service " + e.getMessage(), e);
            }
        }        else
        {
            //Only create if the Service is allowed by the Price Plan
            service = SubscriberServicesSupport.getSubscriberServiceFromList(ctx, subscriber, serviceId);
            if (service != null)
            {
                service.setSubscriberId(subscriber.getId());
                service.setProvisionedState(state);
                service.setProvisionAction( action);
                service.setProvisionActionState(provisionResult);
                try 
                {
                    SubscriberServicesSupport.createSubscriberServiceRecord(ctx, subscriber.getId(), service);
                } catch (Exception e)
                {
                    Logger.minor(ctx, ServiceProvisioningGatewaySupport.class, 
                            "fail to upadte subscriber service " + e.getMessage(), e);
                }
            }
        }    
    }
    
    
    private static ServiceStateEnum getServiceState(final ServiceProvisionActionEnum action,
            final boolean provisionResult)
    {
        switch (action.getIndex())
        {
        case ServiceProvisionActionEnum.PROVISION_INDEX:
        case ServiceProvisionActionEnum.UPDATE_ATTRIBUTES_INDEX:
        case ServiceProvisionActionEnum.RESUME_INDEX:    
            if (provisionResult)
            {
                return ServiceStateEnum.PROVISIONED;
            }else
             {
             return ServiceStateEnum.PROVISIONEDWITHERRORS;   
            }
        case ServiceProvisionActionEnum.UNPROVISION_INDEX:
            if (provisionResult)
            {
                return ServiceStateEnum.UNPROVISIONED;
            }else
             {
             return ServiceStateEnum.UNPROVISIONEDWITHERRORS;   
            }    
        case ServiceProvisionActionEnum.SUSPEND_INDEX:
            if (provisionResult)
            {
                return ServiceStateEnum.SUSPENDED;
            }else
             {
             return ServiceStateEnum.SUSPENDEDWITHERRORS;   
            }   
        }
        
        
        return ServiceStateEnum.PENDING;
    }
    
    private static String getExtMsisdnDeletionCmd(Context ctx, int spid) 
    {
    	GrrGeneratorGeneralConfig grrGeneratorGeneralConfig = null;
    	String msisdnDeletionCmd = "";

    	try
    	{
    		grrGeneratorGeneralConfig= HomeSupportHelper.get(ctx).findBean(ctx, GrrGeneratorGeneralConfig.class, spid);
    		
    		if(grrGeneratorGeneralConfig !=null)
    		{
    			msisdnDeletionCmd = grrGeneratorGeneralConfig.getExtMsisdnDeletionCmd();
    		}
    		else
    		{
    			String msg = "Error retrieving Grr General configuration for spid" + spid;
    			new MajorLogMsg(ServiceProvisioningGatewaySupport.class, msg, null).log(ctx);
    		}
    	}
    	catch (HomeException e)
    	{
    		String msg = "Error retrieving Grr General configuration for spid" + spid;
    		new MajorLogMsg(ServiceProvisioningGatewaySupport.class, msg, null).log(ctx);
    	}
    	
    	return msisdnDeletionCmd;
    }
    
    
}
