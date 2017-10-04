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
package com.trilogy.app.crm.home;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import com.trilogy.app.crm.bean.PPVModificationRequest;
import com.trilogy.app.crm.bean.PPVModificationRequestItems;
import com.trilogy.app.crm.bean.ServiceFee2ID;
import com.trilogy.app.crm.bean.core.ServiceFee2;
import com.trilogy.app.crm.bean.core.BundleCategoryAssociation;
import com.trilogy.app.crm.bean.core.BundleProfile;
import com.trilogy.app.crm.bean.core.PricePlanVersion;
import com.trilogy.app.crm.bean.core.Service;
import com.trilogy.app.crm.bean.core.ServicePackageVersion;
import com.trilogy.app.crm.bean.core.BundleFee;
import com.trilogy.app.crm.bundle.BundleTypeEnum;
import com.trilogy.app.crm.bundle.exception.BundleDoesNotExistsException;
import com.trilogy.framework.xhome.beans.Validator;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.LogSupport;

/**
 * Validates the price plan version bundles against the services
 *
 * @author Marcio Marques
 * @since 9.2
 */

public class PricePlanVersionBundlesValidator implements Validator
{    /**
     * {@inheritDoc}
     */
    public void validate(final Context ctx, final Object obj) throws IllegalStateException
    {
        if (obj instanceof PPVModificationRequest)
        {
            validate(ctx, (PPVModificationRequest) obj);
            
        }
        else if (obj instanceof PricePlanVersion)
        {
            validate(ctx, (PricePlanVersion) obj);
        }
        else
        {
            throw new UnsupportedOperationException("Unsupported bean for this home");
        }
    }

    public void validate(Context ctx, PPVModificationRequest obj) throws IllegalStateException
    {
        PPVModificationRequestItems items = obj.getServicePackageVersion();

        Map<ServiceFee2ID, ServiceFee2> serviceFeesMap = items.getServiceFees(ctx);
        Map<ServiceFee2ID, ServiceFee2> newServiceFeesMap = items.getNewServiceFees(ctx);
        Map<Long, BundleFee> bundleFeesMap = items.getBundleFees(ctx);
        Map<Long, BundleFee> newBundleFeesMap = items.getNewBundleFees(ctx);
        
        if (bundleFeesMap.size() == 0 && newBundleFeesMap.size() == 0)
        {
            return;
        }
        
        String[] existingHandlerArray = populateServiceHandlersArray(ctx, serviceFeesMap);
        String[] newHandlerArray = populateServiceHandlersArray(ctx, newServiceFeesMap);
        
        String[] handlerArray;
        if (newHandlerArray.length==0)
        {
            handlerArray = existingHandlerArray;
        }
        else if (existingHandlerArray.length==0)
        {
            handlerArray = newHandlerArray;
        }
        else
        {
            Set<String> handlers = new TreeSet<String>();
            for (String handler : existingHandlerArray)
            {
                handlers.add(handler);
            }
            for (String handler : newHandlerArray)
            {
                handlers.add(handler);
            }
            handlerArray = handlers.toArray(new String[]{});
        }
        
        validateBundlesAgainstHandlers(ctx, bundleFeesMap, handlerArray);
        validateBundlesAgainstHandlers(ctx, newBundleFeesMap, handlerArray);

    }
    
    /**
     * This method validates the Bundles selected against the Service Types.
     * If the bundles selected in the Price Plan doesn't have corresponding services selected.
     * We throw an exception and will not let the Price plan to save.
     * @param ctx Context Object
     * @param obj ServicePackageVersion Object.
     */
    public void validate(Context ctx, PricePlanVersion obj) throws IllegalStateException
    {
        ServicePackageVersion spver = obj.getServicePackageVersion(ctx);

        Map<ServiceFee2ID, ServiceFee2> serviceFeesMap = spver.getServiceFees(ctx);
        Map<Long, BundleFee> bundleFeesMap = spver.getBundleFees(ctx);
        
        if (bundleFeesMap.size() == 0)
        {
            return;
        }
        
        String[] handlerArray = populateServiceHandlersArray(ctx, serviceFeesMap);
    
        validateBundlesAgainstHandlers(ctx, bundleFeesMap, handlerArray);
        
    }

    /**
     * This method compares the bundles with the services
     * @param bArray java.lang.String array holding the bundle types
     * @param hArray java.lang.String array holding the service handlers
     */
    public void compareBundlesWithServices(Context ctx, String[] bArray, String[] hArray, String bundleName) throws IllegalStateException
    {       
        boolean matchFound = false;
        String services = "";
        if (bArray != null && bArray.length > 0 && hArray != null && hArray.length > 0)
        {
            for (int ind = 0; ind < bArray.length; ind++)
            {
                services += bArray[ind] + ", ";
                matchFound = false;
                for(int indy = 0; indy < hArray.length; indy++)
                {
                    if (hArray[indy].toUpperCase().equals(bArray[ind]))
                    {
                        matchFound = true;
                        
                        break;
                    }
                }
                if (matchFound)
                {
                    break;
                }
            }
        }

        if (matchFound == false)
        {
            if (services.length()>0)
            {
                services = services.substring(0, services.length()-2);
            }
            String msg = "None of the services supported by bundle '" + bundleName + "' (" + services + ") are selected.";
            if (LogSupport.isDebugEnabled(ctx))
            {
                LogSupport.debug(ctx, this, msg + " Service array " + Arrays.toString(hArray)
                        + " Bundle array " + Arrays.toString(bArray));
            }
            throw new IllegalStateException(msg);
        }

    }


   /**
    * This method maps the bundle type to crm service type
    * @param bTypeArray int array representing  bundle types
    * @return java.lang.String array holding the mapped service handler types
    */ 
   private static String[] mapBMTypeToCRMServiceType(int[] bTypeArray)
   {
       Set<String> possibleHandlers = new TreeSet<String>();
       
        for(int i = 0; i < bTypeArray.length; i++)
        {
            switch (bTypeArray[i])
            {
                case BundleTypeEnum.VOICE_INDEX:
                    possibleHandlers.add("VOICE");
                    break;
                case BundleTypeEnum.SMS_INDEX:
                    possibleHandlers.add("SMS");
                    break;
                case BundleTypeEnum.DATA_INDEX:
                    possibleHandlers.add("IPC");
                    possibleHandlers.add("EVDO");
                    break;
                case BundleTypeEnum.MONETARY_INDEX:
                    possibleHandlers.add("VOICE");
                    possibleHandlers.add("SMS");
                    possibleHandlers.add("IPC");
                    possibleHandlers.add("EVDO");
                    break;
                case BundleTypeEnum.EVENT_INDEX:
                    possibleHandlers.add("IPC");
                    possibleHandlers.add("EVDO");
                    break;
                case BundleTypeEnum.CROSS_SERVICE_INDEX:
                    possibleHandlers.add("VOICE");
                    possibleHandlers.add("SMS");
                    possibleHandlers.add("IPC");
                    possibleHandlers.add("EVDO");
                    break;
                default:
                    break;
            }
        }
            return possibleHandlers.toArray(new String[]{});
    }
   
    public String[] populateServiceHandlersArray(Context ctx, Map<ServiceFee2ID, ServiceFee2> map)
    {
        Set<String> handlers = new TreeSet<String>();
        for(Iterator<ServiceFee2ID> iter = map.keySet().iterator();iter.hasNext();)
        {
        	ServiceFee2ID key = iter.next();
            ServiceFee2 serviceFee = map.get(key);
            Service service = null;
            try 
            {
                service = (Service) serviceFee.getService(ctx);
                service.setContext(ctx);
            }
            catch (HomeException e) 
            {
                LogSupport.minor(ctx, this, "Unable to get the Service object for service Id = " + serviceFee.getServiceId() + " : " + e.getMessage(), e);
            }
            if (service != null)
            {
                handlers.add(service.getHandler());
            }
        } 
        
        return handlers.toArray(new String[]{});
    }
    
    public void validateBundlesAgainstHandlers(Context ctx, Map<Long, BundleFee> map, String[] handlerArray)
    {
        BundleFee bundleFee = null;
        BundleProfile bundle = null;

        for(Iterator<Long> iter = map.keySet().iterator(); iter.hasNext();)
        {
            Long key = iter.next();
            bundleFee = map.get(key);
            long bundleId = bundleFee.getId();
            try 
            {
                bundle = bundleFee.getBundleProfile(ctx);
            } 
            catch (BundleDoesNotExistsException e) 
            {
                LogSupport.minor(ctx, this, "Bundle " + bundleId + " does not exist: " + e.getMessage(), e);
            }
            catch (Exception e)
            {
                LogSupport.minor(ctx, this, "Exception while looking for bundle " + bundleId + ": " + e.getMessage(), e);
            }
            
            if (bundle != null)
            {
                int[] btypeArray = new int[bundle.getBundleCategoryIds().size()];
                int index = 0;
                for(Iterator<Map.Entry<?, BundleCategoryAssociation>> catIter = bundle.getBundleCategoryIds().entrySet().iterator();catIter.hasNext();)
                {
                    BundleCategoryAssociation association = catIter.next().getValue();
                    int bundleType = association.getType();
                    btypeArray[index] = bundleType;
                    index++;
                }
                String[] bundleTypeArray = mapBMTypeToCRMServiceType(btypeArray);
                compareBundlesWithServices(ctx, bundleTypeArray, handlerArray, bundle.getName());
            }
        }
    }
}



