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
package com.trilogy.app.crm.bean.ui;


import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

import com.trilogy.app.crm.bean.AdjustmentType;
import com.trilogy.app.crm.bean.AdjustmentTypeEnum;
import com.trilogy.app.crm.bean.AuxiliaryService;
import com.trilogy.app.crm.bean.ChargingTemplateAdjType;
import com.trilogy.app.crm.bean.ChargingTemplateAdjTypeHome;
import com.trilogy.app.crm.bean.ChargingTemplateAdjTypeXInfo;
import com.trilogy.app.crm.bean.Service;
import com.trilogy.app.crm.bean.core.SubscriptionType;
import com.trilogy.app.crm.bundle.BundleProfile;
import com.trilogy.app.crm.support.AdjustmentTypeSupportHelper;
import com.trilogy.app.crm.support.AuxiliaryServiceSupport;
import com.trilogy.app.crm.support.BundleSupportHelper;
import com.trilogy.app.crm.support.ServiceSupport;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.LogSupport;

/**
 * 
 * @author Marcio Marques
 * @since 8.5
 */
public class ChargingTemplate extends AbstractChargingTemplate
{
    private Context context_;
    
    public Context getContext()
    {
        return context_;
    }
    
    public void setContext(Context context)
    {
        context_ = context;
    }
    
    @Override
    public Set<String> getAdjustmentTypes()
    {
        return getAdjustmentTypes(getContext());
    }

    @Override
    public Set<String> getServices()
    {
        return getServices(getContext());
    }
    
    @Override
    public Set<String> getAuxiliaryServices()
    {
        return getAuxiliaryServices(getContext());
    }

    @Override
    public Set<String> getBundles()
    {
        return getBundles(getContext());
    }
    
    public Set<String> getAuxiliaryBundles()
    {
        return getAuxiliaryBundles(getContext());
    }
    
    
    public Set<String> getAdjustmentTypes(Context ctx)
    {
        if (super.getSavedAdjustmentTypes()==null)
        {
            loadAdjustmentTypes(ctx);
        }
        
        Set setObj = super.getAdjustmentTypes();
        Set<String> returnSet = new HashSet<String>();
        for(Object obj : setObj)
        {
        	returnSet.add(obj.toString());	
        }
        return returnSet;
    }
    
    public Set<String> getServices(Context ctx)
    {
        if (super.getSavedServices()==null)
        {
            loadAdjustmentTypes(ctx);
        }
        
        Set setObj = super.getServices();
        Set<String> returnSet = new HashSet<String>();
        for(Object obj : setObj)
        {
        	returnSet.add(obj.toString());	
        }
        return returnSet;
    }

    public Set<String> getAuxiliaryServices(Context ctx)
    {
        if (super.getSavedAuxiliaryServices()==null)
        {
            loadAdjustmentTypes(ctx);
        }
        
        Set setObj = super.getAuxiliaryServices();
        Set<String> returnSet = new HashSet<String>();
        for(Object obj : setObj)
        {
        	returnSet.add(obj.toString());	
        }
        return returnSet;
    }
    
    public Set<String> getBundles(Context ctx)
    {
        if (super.getSavedBundles()==null)
        {
            loadAdjustmentTypes(ctx);
        }
        
        Set setObj = super.getBundles();
        Set<String> returnSet = new HashSet<String>();
        for(Object obj : setObj)
        {
        	returnSet.add(obj.toString());	
        }
        return returnSet;
        
    }

    public Set<String> getAuxiliaryBundles(Context ctx)
    {
        if (super.getSavedAuxiliaryBundles()==null)
        {
            loadAdjustmentTypes(ctx);
        }
        
        Set setObj = super.getAuxiliaryBundles();
        Set<String> returnSet = new HashSet<String>();
        for(Object obj : setObj)
        {
        	returnSet.add(obj.toString());	
        }
        return returnSet;
        
    }
    
    public Set<Service> getAddedServices(Context ctx)
    {
        Set<Service> services = new HashSet<Service>();
        if (super.getSavedServices()!=null)
        {
            Set<String> saved = buildSet(getSavedServices());
            for (Object serviceIdObj : getServices())
            {
            	String serviceId = serviceIdObj.toString();
                if (!saved.contains(serviceId))
                {
                    try
                    {
                        Service service = ServiceSupport.getService(ctx, Long.parseLong(serviceId));
                        if (service!=null)
                        {
                            services.add(service);
                        }
                    }
                    catch (Exception e)
                    {
                        LogSupport.minor(ctx, this, "Invalid service selected for charging template " + getIdentifier() + " -> " + serviceId + ": " + e.getMessage(), e);
                    }
                }
            }
        }
        return services;
    }
    
    public Set<Service> getRemovedServices(Context ctx)
    {
        Set<Service> services = new HashSet<Service>();
        if (super.getSavedServices()!=null)
        {
            Set<String> saved = buildSet(getSavedServices());
            for (Object serviceIdObj : saved)
            {
            	String serviceId  = serviceIdObj.toString();
                if (!getServices().contains(serviceId))
                {
                    try
                    {
                        Service service = ServiceSupport.getService(ctx, Long.parseLong(serviceId));
                        if (service!=null)
                        {
                            services.add(service);
                        }
                    }
                    catch (Exception e)
                    {
                        LogSupport.minor(ctx, this, "Invalid service selected for charging template " + getIdentifier() + " -> " + serviceId + ": " + e.getMessage(), e);
                    }
                }
            }
        }
        return services;
    }
    
    public Set<AuxiliaryService> getAddedAuxiliaryServices(Context ctx)
    {
        Set<AuxiliaryService> auxServices = new HashSet<AuxiliaryService>();
        if (super.getSavedAuxiliaryServices()!=null)
        {
            Set<String> saved = buildSet(getSavedAuxiliaryServices());
            Set auxSet = getAuxiliaryServices();
            for (Object auxServiceIdObj : auxSet)
            {
            	String auxServiceId = auxServiceIdObj.toString();
                if (!saved.contains(auxServiceId))
                {
                    try
                    {
                        AuxiliaryService auxService = AuxiliaryServiceSupport.getAuxiliaryService(ctx, Long.parseLong(auxServiceId));
                        if (auxService!=null)
                        {
                            auxServices.add(auxService);
                        }
                    }
                    catch (Exception e)
                    {
                        LogSupport.minor(ctx, this, "Invalid auxiliary service selected for charging template " + getIdentifier() + " -> " + auxServiceId + ": " + e.getMessage(), e);
                    }
                }
            }
        }
        return auxServices;
    }
    
    public Set<AuxiliaryService> getRemovedAuxiliaryServices(Context ctx)
    {
        Set<AuxiliaryService> auxServices = new HashSet<AuxiliaryService>();
        if (super.getSavedAuxiliaryServices()!=null)
        {
            Set<String> saved = buildSet(getSavedAuxiliaryServices());
           for (Object auxServiceIdObj : saved)
            {
        	   String auxServiceId = auxServiceIdObj.toString();
                if (!getAuxiliaryServices().contains(auxServiceId))
                {
                    try
                    {
                        AuxiliaryService auxService = AuxiliaryServiceSupport.getAuxiliaryService(ctx, Long.parseLong(auxServiceId));
                        if (auxService!=null)
                        {
                            auxServices.add(auxService);
                        }
                    }
                    catch (Exception e)
                    {
                        LogSupport.minor(ctx, this, "Invalid auxiliary service selected for charging template " + getIdentifier() + " -> " + auxServiceId + ": " + e.getMessage(), e);
                    }
                }
            }
        }
        return auxServices;
    }
    
    public Set<BundleProfile> getAddedBundles(Context ctx)
    {
        Set<BundleProfile> bundles = new HashSet<BundleProfile>();
        if (super.getSavedBundles()!=null)
        {
            Set<String> saved = buildSet(getSavedBundles());
            for (Object bundleIdObj : getBundles())
            {
            	String bundleId  = bundleIdObj.toString();
                if (!saved.contains(bundleId))
                {
                    try
                    {
                        BundleProfile bundle = BundleSupportHelper.get(ctx).getBundleProfile(ctx, Long.parseLong(bundleId));
                        if (bundle!=null)
                        {
                            bundles.add(bundle);
                        }
                    }
                    catch (Exception e)
                    {
                        LogSupport.minor(ctx, this, "Invalid bundle selected for charging template " + getIdentifier() + " -> " + bundleId + ": " + e.getMessage(), e);
                    }
                }
            }
        }
        return bundles;
    }
    
    public Set<BundleProfile> getRemovedBundles(Context ctx)
    {
        Set<BundleProfile> bundles = new HashSet<BundleProfile>();
        if (super.getSavedBundles()!=null)
        {
            Set<String> saved = buildSet(getSavedBundles());
            for (String bundleId : saved)
            {
                if (!getBundles().contains(bundleId))
                {
                    try
                    {
                        BundleProfile bundle = BundleSupportHelper.get(ctx).getBundleProfile(ctx, Long.parseLong(bundleId));
                        if (bundle!=null)
                        {
                            bundles.add(bundle);
                        }
                    }
                    catch (Exception e)
                    {
                        LogSupport.minor(ctx, this, "Invalid bundle selected for charging template " + getIdentifier() + " -> " + bundleId + ": " + e.getMessage(), e);
                    }
                }
            }
        }
        return bundles;

    }

    public Set<BundleProfile> getAddedAuxiliaryBundles(Context ctx)
    {
        Set<BundleProfile> bundles = new HashSet<BundleProfile>();
        if (super.getSavedAuxiliaryBundles()!=null)
        {
            Set<String> saved = buildSet(getSavedAuxiliaryBundles());
            for (String bundleId : getAuxiliaryBundles())
            {
                if (!saved.contains(bundleId))
                {
                    try
                    {
                        BundleProfile bundle = BundleSupportHelper.get(ctx).getBundleProfile(ctx, Long.parseLong(bundleId));
                        if (bundle!=null)
                        {
                            bundles.add(bundle);
                        }
                    }
                    catch (Exception e)
                    {
                        LogSupport.minor(ctx, this, "Invalid bundle selected for charging template " + getIdentifier() + " -> " + bundleId + ": " + e.getMessage(), e);
                    }
                }
            }
        }
        return bundles;
    }
    
    public Set<BundleProfile> getRemovedAuxiliaryBundles(Context ctx)
    {
        Set<BundleProfile> bundles = new HashSet<BundleProfile>();
        if (super.getSavedAuxiliaryBundles()!=null)
        {
            Set<String> saved = buildSet(getSavedAuxiliaryBundles());
            for (String bundleId : saved)
            {
                if (!getAuxiliaryBundles().contains(bundleId))
                {
                    try
                    {
                        BundleProfile bundle = BundleSupportHelper.get(ctx).getBundleProfile(ctx, Long.parseLong(bundleId));
                        if (bundle!=null)
                        {
                            bundles.add(bundle);
                        }
                    }
                    catch (Exception e)
                    {
                        LogSupport.minor(ctx, this, "Invalid auxiliary bundle selected for charging template " + getIdentifier() + " -> " + bundleId + ": " + e.getMessage(), e);
                    }
                }
            }
        }
        return bundles;

    }
    
    public Set<AdjustmentType> getAddedAdjustmentTypes(Context ctx)
    {
        Set<AdjustmentType> adjTypes = new HashSet<AdjustmentType>();
        if (super.getSavedAdjustmentTypes()!=null)
        {
            Set<String> saved = buildSet(getSavedAdjustmentTypes());
            for (String adjTypeId : getAdjustmentTypes())
            if (!saved.contains(adjTypeId))
            {
                try
                {
                    AdjustmentType adjustmentType = AdjustmentTypeSupportHelper.get(ctx).getAdjustmentType(ctx, Integer.parseInt(adjTypeId));
                    if (adjustmentType!=null)
                    {
                        adjTypes.add(adjustmentType);
                    }
                }
                catch (Exception e)
                {
                    LogSupport.minor(ctx, this, "Invalid adjustment type selected for charging template " + getIdentifier() + " -> " + adjTypeId + ": " + e.getMessage(), e);
                }
            }
        
        }
        return adjTypes;
    }
    
    public Set<AdjustmentType> getRemovedAdjustmentTypes(Context ctx)
    {
        Set<AdjustmentType> adjTypes = new HashSet<AdjustmentType>();
        if (super.getSavedAdjustmentTypes()!=null)
        {
            Set<String> saved = buildSet(getSavedAdjustmentTypes());
            for (String adjTypeId : saved)
            if (!getAdjustmentTypes().contains(adjTypeId))
            {
                try
                {
                    AdjustmentType adjustmentType = AdjustmentTypeSupportHelper.get(ctx).getAdjustmentType(ctx, Integer.parseInt(adjTypeId));
                    if (adjustmentType!=null)
                    {
                        adjTypes.add(adjustmentType);
                    }
                }
                catch (Exception e)
                {
                    LogSupport.minor(ctx, this, "Invalid adjustment type selected for charging template " + getIdentifier() + " -> " + adjTypeId + ": " + e.getMessage(), e);
                }
            }
        }
        return adjTypes;
    }    

    public SubscriptionType getSubscriptionType(final Context ctx)
    {
        return SubscriptionType.getSubscriptionType(ctx, getSubscriptionType());
    }


    private void loadAdjustmentTypes(Context ctx)
    {
        if (ctx!=null)
        {
            try
            {
                Home home = (Home) ctx.get(ChargingTemplateAdjTypeHome.class);
                Collection<ChargingTemplateAdjType> adjTypesMapping = (Collection<ChargingTemplateAdjType>) home
                        .select(new EQ(ChargingTemplateAdjTypeXInfo.IDENTIFIER, this.getIdentifier()));
                Set<String> adjustmentTypeSet = new HashSet<String>();
                Set<String> servicesSet = new HashSet<String>();
                Set<String> auxServicesSet = new HashSet<String>();
                Set<String> bundlesSet = new HashSet<String>();
                Set<String>  auxBundlesSet = new HashSet<String>();
                int svcIndex =  Integer.valueOf(AdjustmentTypeEnum.Services_INDEX);
                int auxSvcIndex = Integer.valueOf(AdjustmentTypeEnum.AuxiliaryServices_INDEX);
                int bundleIndex = Integer.valueOf(AdjustmentTypeEnum.Bundles_INDEX);
                int auxBundleIndex = Integer.valueOf(AdjustmentTypeEnum.AuxiliaryBundles_INDEX);
                for (ChargingTemplateAdjType adjTypeMapping : adjTypesMapping)
                {
                    boolean added = false;
                    if(adjTypeMapping != null) 
                    {
                        AdjustmentType adjType = AdjustmentTypeSupportHelper.get(ctx).getAdjustmentType(ctx, adjTypeMapping.getAdjustmentTypeId());
                        if(adjType != null) 
                        {
                            int parent = adjType.getParentCode();
                          
                            if (parent == svcIndex)
                            {
                                Service service  = ServiceSupport.getServiceByAdjustment(ctx, adjType.getCode());
                                if (service!=null)
                                {
                                    servicesSet.add(String.valueOf(service.getID()));
                                    added = true;
                                }
                            }
                            else if (parent == auxSvcIndex)
                            {
                                AuxiliaryService auxService  = ServiceSupport.getAuxServiceByAdjustment(ctx, adjType.getCode());
                                if (auxService!=null)
                                {
                                    auxServicesSet.add(String.valueOf(auxService.getIdentifier()));
                                    added = true;
                                }
                            }
                            else if (parent == bundleIndex)
                            {
                                try
                                {
                                    BundleProfile bundle = BundleSupportHelper.get(ctx).getBundleByAdjustmentType(ctx, adjType.getCode());
                                    if (bundle!=null && !bundle.isAuxiliary())
                                    {
                                        bundlesSet.add(String.valueOf(bundle.getBundleId()));
                                        added = true;
                                    }
                                }
                                catch (Exception e)
                                {
                                    LogSupport.minor(ctx, this, "Unable to retrieve bundle profile for adjustment type " + adjType.getCode() + ": " + e.getMessage(), e);
                                }
                                
                            }
                            else if (parent == auxBundleIndex)
                            {
                                try
                                {
                                    BundleProfile bundle = BundleSupportHelper.get(ctx).getBundleByAdjustmentType(ctx, adjType.getCode());
                                    if (bundle!=null && bundle.isAuxiliary())
                                    {
                                        auxBundlesSet.add(String.valueOf(bundle.getBundleId()));
                                        added = true;
                                    }
                                }
                                catch (Exception e)
                                {
                                    LogSupport.minor(ctx, this, "Unable to retrieve auxiliary bundle profile for adjustment type " + adjType.getCode() + ": " + e.getMessage(), e);
                                }
                                
                            }                
                            if (!added)
                            {
                                adjustmentTypeSet.add(String.valueOf(adjType.getCode()));
                            }
                        }
                    }
                }
                
                setAdjustmentTypes(adjustmentTypeSet);
                
                setServices(servicesSet);
                
                setAuxiliaryServices(auxServicesSet);
    
                setBundles(bundlesSet);
                
                setAuxiliaryBundles(auxBundlesSet);
    
                setSavedAdjustmentTypes(buildString(adjustmentTypeSet));
                
                setSavedServices(buildString(servicesSet));
                
                setSavedAuxiliaryServices(buildString(auxServicesSet));
    
                setSavedBundles(buildString(bundlesSet));
                
                setSavedAuxiliaryBundles(buildString(auxBundlesSet));
            }
            catch (HomeException e)
            {
                LogSupport.minor(ctx, this, "Unable to load charging template adjustment types: " + e.getMessage(), e);
            }
        }
    }

    public void resetSavedValues(Context ctx)
    {
        setSavedAdjustmentTypes(buildString(getAdjustmentTypes(ctx)));
        
        setSavedServices(buildString(getServices(ctx)));
        
        setSavedAuxiliaryServices(buildString(getAuxiliaryServices(ctx)));

        setSavedBundles(buildString(getBundles(ctx)));
        
        setSavedAuxiliaryBundles(buildString(getAuxiliaryBundles(ctx)));
    }
    
    private Set<String> buildSet(String str)
    {
        Set<String> set = new HashSet<String>();
        StringTokenizer st = new StringTokenizer(str,",");
        while (st.hasMoreTokens()) {
            set.add(st.nextToken());
        }
        return set;
    }

    
    private String buildString(Set set)
    {
        Object[] arr = set.toArray();
        StringBuilder buff = new StringBuilder();
        
        for (int x=0; x<arr.length; x++)
        {
            if (x!=0) buff.append(",");
            buff.append(arr[x]);
        }

        return buff.toString();
    }
    
}
