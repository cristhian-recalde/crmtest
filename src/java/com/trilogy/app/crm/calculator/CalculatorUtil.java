package com.trilogy.app.crm.calculator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xlog.log.LogSupport;

import com.trilogy.app.crm.bean.ExternalServiceType;
import com.trilogy.app.crm.bean.ExternalServiceTypeXInfo;
import com.trilogy.app.crm.bean.PricePlanVersion;
import com.trilogy.app.crm.bean.ServicePreferenceEnum;
import com.trilogy.app.crm.bean.ServiceTypeEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.core.Service;
import com.trilogy.app.crm.bean.core.ServiceFee2;
import com.trilogy.app.crm.extension.service.ExternalServiceTypeExtension;
import com.trilogy.app.crm.support.ExtensionSupportHelper;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.app.crm.support.PricePlanSupport;
import com.trilogy.app.crm.support.ServiceSupport;
import com.trilogy.app.crm.support.SubscriberSupport;
import com.trilogy.app.crm.util.StringUtil;

public class CalculatorUtil
{
    private static final String LOG_CLASS_NAME_CONSTANT = CalculatorUtil.class.getName();
    
    private static final String SINGLE_SERVICE_DATA_SEPERATOR      = ":";

    private static final String SERVICE_LIST_SEPERATOR             = "\\|";

    private static final String SERVICE_LIST_SEPERATOR_APPENDER    = "|";

    private static String       PRODUCT_OFFERING_START             = new String("<grr:part>" + StringUtil.NEW_LINE
                                                                           + "<grr:name>ProductOffering</grr:name>"
                                                                           + StringUtil.NEW_LINE + "<grr:part>"
                                                                           + StringUtil.NEW_LINE
                                                                           + "<grr:name>name</grr:name>"
                                                                           + StringUtil.NEW_LINE + "<grr:value>");

    private static final String PRODUCT_OFFERING_MID               = new String("</grr:value>" + StringUtil.NEW_LINE
                                                                           + "</grr:part>" + StringUtil.NEW_LINE
                                                                           + "<grr:part>" + StringUtil.NEW_LINE
                                                                           + "<grr:name>id</grr:name>"
                                                                           + StringUtil.NEW_LINE + "<grr:value>");

    private static final String PRODUCT_OFFERING_END               = new String("</grr:value>" + StringUtil.NEW_LINE
                                                                           + "</grr:part>" + StringUtil.NEW_LINE
                                                                           + "</grr:part>" + StringUtil.NEW_LINE);

    private static final String PRODUCT_SPECIFICATION_HEADER_START = new String(
                                                                           "<grr:part>"
                                                                                   + StringUtil.NEW_LINE
                                                                                   + "<grr:name>ProductSpecificationType</grr:name>"
                                                                                   + StringUtil.NEW_LINE
                                                                                   + "<grr:part>"
                                                                                   + StringUtil.NEW_LINE
                                                                                   + "<grr:name>ProductSpecificationValue</grr:name>"
                                                                                   + StringUtil.NEW_LINE + "<grr:part>"
                                                                                   + StringUtil.NEW_LINE
                                                                                   + "<grr:name>describedBy</grr:name>"
                                                                                   + StringUtil.NEW_LINE);

    private static final String PRODUCT_SPECIFICATION_ITEM_START   = new String(
                                                                           "<grr:part>"
                                                                                   + StringUtil.NEW_LINE
                                                                                   + "<grr:name>item</grr:name>"
                                                                                   + StringUtil.NEW_LINE
                                                                                   + "<grr:part>"
                                                                                   + StringUtil.NEW_LINE
                                                                                   + "<grr:name>RK_GRR_XML_PART_ATTRIBUTE</grr:name>"
                                                                                   + StringUtil.NEW_LINE
                                                                                   + "<grr:value>type=\"");

    private static final String PRODUCT_SPECIFICATION_ITEM_MID     = new String("\"</grr:value>" + StringUtil.NEW_LINE
                                                                           + "</grr:part>" +

                                                                           "<grr:part>" + StringUtil.NEW_LINE
                                                                           + "<grr:name>value</grr:name>"
                                                                           + StringUtil.NEW_LINE + "<grr:value>"
                                                                           + "yes" + "</grr:value>"
                                                                           + StringUtil.NEW_LINE + "</grr:part>" +

                                                                           StringUtil.NEW_LINE + "<grr:part>"
                                                                           + StringUtil.NEW_LINE
                                                                           + "<grr:name>features</grr:name>"
                                                                           + StringUtil.NEW_LINE + "<grr:value>");

    private static final String PRODUCT_SPECIFICATION_ITEM_END     = new String("</grr:value>" + StringUtil.NEW_LINE
                                                                           + "</grr:part>" + StringUtil.NEW_LINE
                                                                           + "</grr:part>" + StringUtil.NEW_LINE);

    private static final String PRODUCT_SPECIFICATION_HEADER_END   = new String(StringUtil.NEW_LINE + "</grr:part>"
                                                                           + StringUtil.NEW_LINE + "</grr:part>"
                                                                           + StringUtil.NEW_LINE + "</grr:part>");

    private static final String DEFAULT_PRICE_PLAN_KEY             = "%DefaultRetailPricePlanId%";


    public static String productOfferingAndProductSpecification(Context ctx, long priceplanId)
    {
        if (LogSupport.isDebugEnabled(ctx))
        {
            LogSupport.debug(ctx, LOG_CLASS_NAME_CONSTANT, "productOfferingAndProductSpecification() : 'priceplanId' parameter value - " + priceplanId);
        }
        
        PricePlanVersion ppv;
        try
        {
            ppv = PricePlanSupport.getCurrentVersion(ctx, priceplanId);
        }
        catch (HomeException e)
        {
            String errorMsg = "Error while PricePlan details fetch - " + e.getMessage();
            LogSupport.major(ctx, LOG_CLASS_NAME_CONSTANT, errorMsg, e);
            throw new IllegalStateException(errorMsg);
        }
        Set<Long> serviceIdList = (Set<Long>) ppv.getServicePackageVersion().getServiceFees().keySet();

        return getProductOfferingAndSpecificationDetails(ctx, serviceIdList, true);
    }

    
    
    public static String productOfferingAndProductSpecificationV2(Context ctx, long priceplanId)
    {
        if (LogSupport.isDebugEnabled(ctx))
        {
            LogSupport.debug(ctx, LOG_CLASS_NAME_CONSTANT, "productOfferingAndProductSpecificationV2() : 'priceplanId' parameter value - " + priceplanId);
        }
        
        PricePlanVersion ppv = null;
        try
        {
            ppv = PricePlanSupport.getCurrentVersion(ctx, priceplanId);
        }
        catch (HomeException e)
        {
            String errorMsg = "Error while PricePlan details fetch : " + e.getMessage();
            LogSupport.major(ctx, LOG_CLASS_NAME_CONSTANT, errorMsg, e);
            throw new IllegalStateException(errorMsg);
        }

        try
        {
            return getSuspendResumeServiceList(ctx, ppv, "", false);
        }
        catch(Exception e)
        {
            String errorMsg = "Error while ProductOffering and Specification details fetch : " + e.getMessage();
            LogSupport.major(ctx, LOG_CLASS_NAME_CONSTANT, errorMsg, e);
            throw new IllegalStateException(errorMsg);
        }
    }
    
    /**
     * Considers Default and Mandatory services in PricePlan
     * @param ctx
     * @param priceplanId
     * @return
     */
    public static String productOfferingAndProductSpecificationV3(Context ctx, long priceplanId)
    {
        if (LogSupport.isDebugEnabled(ctx))
        {
            LogSupport.debug(ctx, LOG_CLASS_NAME_CONSTANT, "productOfferingAndProductSpecificationV3() : 'priceplanId' parameter value - " + priceplanId);
        }
        
        PricePlanVersion ppv = null;
        try
        {
            ppv = PricePlanSupport.getCurrentVersion(ctx, priceplanId);
        }
        catch (HomeException e)
        {
            String errorMsg = "Error while PricePlan details fetch : " + e.getMessage();
            LogSupport.major(ctx, LOG_CLASS_NAME_CONSTANT, errorMsg, e);
            throw new IllegalStateException(errorMsg);
        }

        Map serviceFees = ppv.getServicePackageVersion().getServiceFees();
        Set<Long> serviceIdList = (Set<Long>) serviceFees.keySet();
        Set<Long> defaultAndMandatoryServiceIdList = new HashSet<Long>();
        
        for (Long serviceId : serviceIdList)
        {
            ServiceFee2 serviceFee = (ServiceFee2) serviceFees.get(serviceId);
            if (serviceFee.getServicePreference().getIndex() == ServicePreferenceEnum.MANDATORY_INDEX
                    || serviceFee.getServicePreference().getIndex() == ServicePreferenceEnum.DEFAULT_INDEX)
            {
                defaultAndMandatoryServiceIdList.add(serviceId);
            }
        }
        
        return getProductOfferingAndSpecificationDetails(ctx, defaultAndMandatoryServiceIdList, true);
    }
    
    public static String getProductSpecificationForServiceChange(Context ctx, String newServiceList)
    {
        if (LogSupport.isDebugEnabled(ctx))
        {
            LogSupport.debug(ctx, LOG_CLASS_NAME_CONSTANT, " getProductSpecificationForServiceChange() : 'newServiceList' parameter value - " + newServiceList);
        }
        
        String serviceList[] = newServiceList.split(SERVICE_LIST_SEPERATOR);
        Set<Long> serviceIdList = new HashSet<Long>(serviceList.length);

        for (String service : serviceList)
        {
            String[] serviceDetails = service.split(SINGLE_SERVICE_DATA_SEPERATOR);
            serviceIdList.add(Long.parseLong(serviceDetails[0]));
        }
         
            return getProductOfferingAndSpecificationDetails(ctx, serviceIdList, true);
        
    }


    /**
     * @param ctx
     * @param serviceList
     * @return
     */
    private static String getProductOfferingAndSpecificationDetails(Context ctx, Set<Long> serviceList,
            boolean doRecurrerse)
    {
        if (LogSupport.isDebugEnabled(ctx))
        {
            LogSupport.debug(ctx, LOG_CLASS_NAME_CONSTANT, "getProductOfferingAndSpecificationDetails() : 'serviceList' size - " + serviceList.size() + ", 'doRecurrerse' parameter value - " + doRecurrerse);
        }
        
        ArrayList<Object[]> productOfferingList = new ArrayList<Object[]>();
        ArrayList<Object[]> productSpecificationList = new ArrayList<Object[]>();

        StringBuilder builderbean = new StringBuilder();
        for (Long serviceId : serviceList)
        {
            Service serviceBean = null;
            try
            {
                serviceBean = ServiceSupport.getService(ctx, serviceId);
            }
            catch (Throwable e)
            {
                String errorMsg = "Error while ProductOffering and ProductSpecificationType creation "
                        + e.getMessage();
                LogSupport.major(ctx, LOG_CLASS_NAME_CONSTANT, "getProductOfferingAndSpecificationDetails() : Exception while retrieving ServiceBean for serviceId : " + serviceId, e);
                throw new IllegalStateException(errorMsg);
            }

            if (LogSupport.isDebugEnabled(ctx))
            {
                LogSupport.debug(ctx, LOG_CLASS_NAME_CONSTANT, "getProductOfferingAndSpecificationDetails() : serviceId - " + serviceId + ", service type - " + serviceBean.getType().getDescription());
            }
            
            ExternalServiceTypeExtension externalServiceTypeExtension = ExtensionSupportHelper.get(ctx)
                    .getExtension(ctx, serviceBean, ExternalServiceTypeExtension.class);
            if (externalServiceTypeExtension == null)
            {
                continue;
            }
            
            if (serviceBean.getType().getIndex() == ServiceTypeEnum.EXTERNAL_PRICE_PLAN_INDEX)
            {
                productOfferingList.add(new Object[] { serviceBean, externalServiceTypeExtension });
            }
            else if (serviceBean.getType().getIndex() == ServiceTypeEnum.GENERIC_INDEX)
            {
                productSpecificationList.add(new Object[] { serviceBean, externalServiceTypeExtension });
            }
        }

        if (productOfferingList.isEmpty() && productSpecificationList.isEmpty() && doRecurrerse)
        {
            long defaultPricePlanId = getDefaultPricePlanId(ctx);
            try
            {
                return productOfferingAndProductSpecification(ctx, defaultPricePlanId, false);
            }
            catch (Throwable e)
            {
                LogSupport.major(ctx, LOG_CLASS_NAME_CONSTANT, "Exception during productOffering and productSpecification calculation for defaultPricePlan with ID - " + defaultPricePlanId+ e);
                return "";
            }
        }

        builderbean.append(getProductOfferingString2(ctx, productOfferingList));
        builderbean.append(getProductSpecificationString(ctx, productSpecificationList));
        return builderbean.toString();
    }

    /**
     * @param ctx
     * @return
     */
    private static long getDefaultPricePlanId(Context ctx)
    {
        long defaultPricePlanId;
        InternalKeyValueCalculator calc = new InternalKeyValueCalculator();
        calc.setInternalKey(DEFAULT_PRICE_PLAN_KEY);
        defaultPricePlanId = Long.parseLong((String) calc.getValueAdvanced(ctx));
        if (LogSupport.isDebugEnabled(ctx))
        {
            LogSupport.debug(ctx, LOG_CLASS_NAME_CONSTANT, "getProductOfferingAndSpecificationDetails() : retrieved default price plan ID - " + defaultPricePlanId);
        }
        return defaultPricePlanId;
    }

    private static String productOfferingAndProductSpecification(Context ctx, long pricePlanId,
            boolean doRecurrese)
    {
        if (LogSupport.isDebugEnabled(ctx))
        {
            LogSupport.debug(ctx, LOG_CLASS_NAME_CONSTANT, "productOfferingAndProductSpecification() : 'pricePlanId' - " + pricePlanId + ", 'doRecurrese' - " + doRecurrese);
        }
        
        PricePlanVersion ppv;
        try
        {
            ppv = PricePlanSupport.getCurrentVersion(ctx, pricePlanId);
        }
        catch (HomeException e)
        {
            String errorMsg = "Error while PricePlan details fetch with ID - " + pricePlanId + ", error msg - " + e.getMessage();
            LogSupport.major(ctx, LOG_CLASS_NAME_CONSTANT, errorMsg, e);
            throw new IllegalStateException(errorMsg);
        }
        Set<Long> serviceIdList = (Set<Long>) ppv.getServicePackageVersion().getServiceFees().keySet();

        return getProductOfferingAndSpecificationDetails(ctx, serviceIdList, doRecurrese);

    }

    public static String getAddAndRemoveServiceList(Context ctx, String newServiceList, String oldServiceList)
    {
        if (LogSupport.isDebugEnabled(ctx))
        {
            LogSupport.debug(ctx, LOG_CLASS_NAME_CONSTANT, "getAddAndRemoveServiceList() : 'newServiceList' - " + newServiceList + ", 'oldServiceList' - " + oldServiceList);
        }
        
        if (newServiceList == null || oldServiceList == null)
        {
            return "";
        }
        
        newServiceList = newServiceList.trim();
        oldServiceList = oldServiceList.trim();
        
        List<Long> newServiceListIds = null;
        Map<Long, String> newServiceListDataMap = null;
        List<Long> oldServiceListIds = null;
        Map<Long, String> oldServiceListDataMap = null;
        
        if (!newServiceList.isEmpty())
        {
            String newServicesDataList[] = newServiceList.split(SERVICE_LIST_SEPERATOR);
            newServiceListIds = new ArrayList<Long>(newServicesDataList.length);
            newServiceListDataMap = new HashMap<Long, String>();
            for (String serviceData : newServicesDataList)
            {
                String[] serviceDetails = serviceData.split(SINGLE_SERVICE_DATA_SEPERATOR);
                long serviceId = Long.parseLong(serviceDetails[0]);
                newServiceListIds.add(serviceId);
                newServiceListDataMap.put(serviceId, serviceData);
            }
        }
        else
        {
            newServiceListIds = new ArrayList<Long>();
            newServiceListDataMap = new HashMap<Long, String>();
        }
        
        if (!oldServiceList.isEmpty())
        {
            String oldServicesDataList[] = oldServiceList.split(SERVICE_LIST_SEPERATOR);
            oldServiceListIds = new ArrayList<Long>(oldServicesDataList.length);
            oldServiceListDataMap = new HashMap<Long, String>();
            for (String serviceData : oldServicesDataList)
            {
                String[] serviceDetails = serviceData.split(SINGLE_SERVICE_DATA_SEPERATOR);
                long serviceId = Long.parseLong(serviceDetails[0]);
                oldServiceListIds.add(serviceId);
                oldServiceListDataMap.put(serviceId, serviceData);
            }
        }
        else
        {
            oldServiceListIds = new ArrayList<Long>();
            oldServiceListDataMap = new HashMap<Long, String>();
        }
        
        List<Long> addServiceListIds = new ArrayList<Long>(newServiceListIds);
        addServiceListIds.removeAll(oldServiceListIds);

        List<Long> deleteServiceListIds = new ArrayList<Long>(oldServiceListIds);
        deleteServiceListIds.removeAll(newServiceListIds);

        String addServiceListKeyValue = getChangedServiceListData(ctx, newServiceListDataMap, addServiceListIds);
        String deleteServiceListKeyValue = getChangedServiceListData(ctx, oldServiceListDataMap, deleteServiceListIds);

        StringBuilder sb = new StringBuilder();

        // if(!addServiceListKeyValue.isEmpty())
        // {
        sb.append("<grr:part><grr:name>rkn_addservicesdelta</grr:name><grr:value>" + addServiceListKeyValue
                + "</grr:value></grr:part>");
        // }

        // if(!deleteServiceListKeyValue.isEmpty())
        // {
        sb.append("<grr:part><grr:name>rkn_deleteservicesdelta</grr:name><grr:value>" + deleteServiceListKeyValue
                + "</grr:value></grr:part>");
        // }
        return sb.toString();
    }


    /**
     * @param oldServiceListDataMap
     * @param addServiceListIds
     * @return
     */
    private static String getChangedServiceListData(Context ctx, Map<Long, String> serviceListDataMap, List<Long> serviceListIds)
    {
        if (LogSupport.isDebugEnabled(ctx))
        {
            LogSupport.debug(ctx, LOG_CLASS_NAME_CONSTANT, "getChangedServiceListData() : 'serviceListDataMap' parameter value - " + serviceListDataMap + ", 'serviceListIds' parameter size - " + serviceListIds.size());
        }
        StringBuilder sb = new StringBuilder();
        for (long serviceId : serviceListIds)
        {
//            sb.append(SERVICE_LIST_SEPERATOR_APPENDER + serviceListDataMap.get(serviceId));
//            changing only to log serviceId due to WIDTH limit on DCRM side.
            sb.append(SERVICE_LIST_SEPERATOR_APPENDER + serviceId);
        }

        int length = sb.length();
        sb = sb.replace(0, 1, "");
        return sb.toString();
    }


    
    
    
    /***
     * 
     * @param ctx
     * @param subMdn
     * @param newServiceList
     * @return
     * @throws HomeException
     * 
     * TCB Service ID   TCB Service Name    TCB Service Type    External Price Plan Code    External Service Type
        1   Monthly Service Generic      
        2   URCS Voice  Voice        
        3   URCS Sms    SMS      
        4   URCS Data   Data         
        5   VZ Voice    Wholesale PP    WPXVY   voiceplan
        6   VZ SMS  Wholesale PP    WODKX   smsplan
        7   VZ Data Wholesale PP    WXMD    broadbandplan
        8   VZ Three way calling    Generic XMLEY   voicefeatures
        9   VZ Voice Enabled    Generic TMXYT   blockingfeature
        10  VZ SMS Enabled  Generic TYNSDY  blockingfeature
        11  VZ Data Throttled   Generic GYDYD   dataFeature
        
        
        #case1 : Normal Active Subscriber
        1.    NSL from SPG => (1,2,3,4,5,6,7,8,9,10,11)
        2.    GRR Translation :
        a.    Get blocking services from Retails PP (9,10,11)
        b.    Remove common blocking services from NSL (step #1).                              New NSL (1,2,3,4,5,6,7,8)
        c.    Add missing blocking services to NSL (step #1).                                          New NSL (1,2,3,4,5,6,7,8)
        d.    Translate those services to VZ which has external service code :                    NSL to  VZ (5,6,7,8)
        
        #case2 : Data bundle depleted and 447 is polled by BSS. Suspend Service 11.
        
        1.    NSL from SPG => (1,2,3,4,5,6,7,8,9,10)
        2.    GRR Translation :
        a.    Get blocking services from Retail PP (9,10,11)
        b.    Remove common blocking services from NSL (step #1).                              New NSL (1,2,3,4,5,6,7,8)
        c.    Add missing blocking services to NSL (step #1) if dependency plan found.       New NSL (1,2,3,4,5,6,7,8, 11)
        d.    Translate those services to VZ which has external service code :                    NSL to  VZ (5,6,7,8,11)
        
        
        #case3 : Empty NSL from SPG
        
        1.    NSL from SPG => ()
        2.    GRR detected Empty NSL -> Go to retail PP and add all WSPP services to NSL  (5,6,7) 
        3.    New NSL becomes (5,6,7)                                                                              New NSL (5,6,7)
        4.    GRR Translation :
        a.    Get blocking services from Retail PP (9,10,11)
        b.    Remove common blocking services from NSL (step #3).                              New NSL (5,6,7)
        c.    Add missing blocking services to NSL (step #3) if dependency plan found.       New NSL (5,6,7,9,10,11)
        d.    Translate those services to VZ which has external service code :                    NSL to  VZ (5,6,7,9,10,11)
        
        #case4 : SPG sends something but the translation result is empty NSL.
        
        1.    NSL from SPG => (1,2,3,4)
        2.    GRR Translation :
        a.    Get blocking services from Retails PP (9,10,11)
        b.    Remove common blocking services from NSL (step #1).                              New NSL (1,2,3,4)
        c.    Add missing blocking services to NSL (step #1).                                          New NSL (1,2,3,4)
        d.    Translate those services to VZ which has external service code :                    NSL to  VZ ()
        e.    Translated NSL is empty Go back to Default NSL calculation flow. Go to case#3.

     * 
     */
    
    public static String getSuspendResumeServiceList(Context ctx, String subMdn, String newServiceList)
            throws HomeException
    {
        if (LogSupport.isDebugEnabled(ctx))
        {
            LogSupport.debug(ctx, LOG_CLASS_NAME_CONSTANT, "1. getSuspendResumeServiceList() : 'subMdn' parameter value - " + subMdn + ", 'newServiceList' parameter size - " + newServiceList);
        }
        
        Subscriber subscriber = SubscriberSupport.lookupSubscriberForMSISDN(ctx, subMdn);
        PricePlanVersion ppv = null;
        try
        {
            ppv = PricePlanSupport.getVersion(ctx, subscriber.getPricePlan(), subscriber.getPricePlanVersion());
        }
        catch (Exception e)
        {
            String errorMsg = "Error while PricePlan details fetch : " + e.getMessage();
            LogSupport.major(ctx, LOG_CLASS_NAME_CONSTANT, errorMsg, e);
            throw new IllegalStateException(errorMsg);
        }

        return getSuspendResumeServiceList(ctx, ppv, newServiceList, false);
    }
    
    
    public static boolean getServiceTypeExtensionActionFlag(Context ctx, Long serviceId) 
    		throws HomeException
    {
    	 if (LogSupport.isDebugEnabled(ctx))
         {
             LogSupport.debug(ctx, LOG_CLASS_NAME_CONSTANT, "getServiceTypeExtensionActionFlag() : 'serviceId' parameter value - " + serviceId );
         }
    	 
    	 if (serviceId == null)
         {
    		 String errorMsg = "Error while getService() : ServiceId is null";
    		 LogSupport.major(ctx, LOG_CLASS_NAME_CONSTANT, errorMsg);
    		 throw new IllegalStateException(errorMsg);
         }
    	 
    	 Service serviceBean = null;
         try
         {
             serviceBean = ServiceSupport.getService(ctx, serviceId);
         }
         catch (Throwable e)
         {
             String errorMsg = "Error while getService() : "
                     + e.getMessage();
             LogSupport.major(ctx, LOG_CLASS_NAME_CONSTANT, errorMsg, e);
             throw new IllegalStateException(errorMsg);
         }
         ExternalServiceTypeExtension externalServiceTypeExtension = ExtensionSupportHelper.get(ctx).getExtension(
                 ctx, serviceBean, ExternalServiceTypeExtension.class);
         
         if(externalServiceTypeExtension == null)
         {
             String errorMsg = "Invalid configuration : referred External Service type extension for Service with ID : " + serviceId + " not present.";
             LogSupport.major(ctx, LOG_CLASS_NAME_CONSTANT, errorMsg);
             throw new IllegalStateException(errorMsg);
         }
         ExternalServiceType externalServiceType = getExternalServiceType(ctx,
                 externalServiceTypeExtension.getExternalServiceType());
         
         if(externalServiceType == null)
         {
             String errorMsg = "Invalid configuration : referred External Service type extension for Service with ID : " + serviceId + " not present.";
             LogSupport.major(ctx, LOG_CLASS_NAME_CONSTANT, errorMsg);
             throw new IllegalStateException(errorMsg);
         }
         
         return externalServiceType.getFlipAction();
         
    }
    
    public static String getSuspendResumeServiceList(Context ctx, PricePlanVersion ppv, String newServiceList, boolean isRecurrence)
            throws HomeException
    {
        if (LogSupport.isDebugEnabled(ctx))
        {
            LogSupport.debug(ctx, LOG_CLASS_NAME_CONSTANT, "2. getSuspendResumeServiceList() : 'PricePlanVersion' parameter value ID - " + ppv.getId() + ", 'newServiceList' parameter size - " + newServiceList);
        }
        if (newServiceList == null)
        {
            newServiceList = "";
        }

        String newServicesDataList[] = newServiceList.split(SERVICE_LIST_SEPERATOR);
        Set<Long> newServiceIdsSet = new HashSet<Long>(newServicesDataList.length);
        Map<Long, String> newServiceListDataMap = new HashMap<Long, String>();
        
        for (String serviceData : newServicesDataList)
        {
            if(serviceData.trim().isEmpty())
            {
                continue;
            }
            String[] serviceDetails = serviceData.split(SINGLE_SERVICE_DATA_SEPERATOR);
            long serviceId = Long.parseLong(serviceDetails[0]);
            newServiceIdsSet.add(serviceId);
            newServiceListDataMap.put(serviceId, serviceData);
        }

 
        // Map to hold data of all price plan Service BEANs.
        Map<Long, Service> pricePlanServiceBeans = new HashMap<Long, Service>();
        ArrayList<Long> pricePlanExternalWSPPServiceIds = new ArrayList<Long>();
        Map<Long, ExternalServiceType> pricePlanBlockingExternalServiceTypeBeans = new HashMap<Long, ExternalServiceType>();
        Map<Long, ExternalServiceTypeExtension> pricePlanServiceExternionBeans = new HashMap<Long, ExternalServiceTypeExtension>();
        ArrayList<Long> pricePlanBlockingForExternalServiceTypeIds = new ArrayList<Long>();
        Map<Long, ExternalServiceType> pricePlanExternalServiceTypeBeans = new HashMap<Long, ExternalServiceType>();

        // STEP 1 : Find out all blocking services from Price Plan
        Set<Long> pricePlanServiceIdList = (Set<Long>) ppv.getServicePackageVersion().getServiceFees().keySet();
        for (Long serviceId : pricePlanServiceIdList)
        {
            Service serviceBean = null;
            try
            {
                serviceBean = ServiceSupport.getService(ctx, serviceId);
                pricePlanServiceBeans.put(serviceId, serviceBean);
            }
            catch (Throwable e)
            {
                String errorMsg = "Error while ProductOffering and ProductSpecificationType creation : "
                        + e.getMessage();
                LogSupport.major(ctx, LOG_CLASS_NAME_CONSTANT, errorMsg, e);
                throw new IllegalStateException(errorMsg);
            }

            ExternalServiceTypeExtension externalServiceTypeExtension = ExtensionSupportHelper.get(ctx).getExtension(
                    ctx, serviceBean, ExternalServiceTypeExtension.class);
            if (externalServiceTypeExtension == null)
            {
                // ignore services in PP which would be used for internal handling i.e. MRC, URCS (Voice, SMS, Data etc)
                newServiceIdsSet.remove(serviceId);
                continue;
            }
             
            pricePlanServiceExternionBeans.put(serviceId, externalServiceTypeExtension);
            
            ExternalServiceType externalServiceType = getExternalServiceType(ctx,
                    externalServiceTypeExtension.getExternalServiceType());
            
            if(externalServiceType == null)
            {
                //Invalid configuration : Extension defined for service but the referred extension is not present
                String errorMsg = "Invalid configuration : referred External Service type extension for Service with ID : " + serviceId + " not present.";
                LogSupport.major(ctx, LOG_CLASS_NAME_CONSTANT, errorMsg);
                throw new IllegalStateException(errorMsg);
            }
            
            if (externalServiceType.isBlocking())
            {
                pricePlanBlockingExternalServiceTypeBeans.put(serviceId, externalServiceType);
            }

            pricePlanExternalServiceTypeBeans.put(serviceId, externalServiceType);

            if (serviceBean.getType().getIndex() == ServiceTypeEnum.EXTERNAL_PRICE_PLAN_INDEX)
            {
                pricePlanExternalWSPPServiceIds.add(serviceId);
            }
        }

        // case#3
        if(newServiceIdsSet.isEmpty())
        {
            newServiceIdsSet = new HashSet<Long>(pricePlanExternalWSPPServiceIds);
        }
        
        
        // Step 2 - Remove blocking services which have been received in NSL
        // After this we have removed blocking services which are common from RetailPP and NSL
        // The @blockingServiceDataMap would contain only those blocking services which next would be required to be checked if its DEPENDENT service is present in NSL
        // if dependent service is present in NSL it suggests that the (blocking)service was suspended and thts the reason it did not arrive in NSL. So add the blocking service to NSL 
        Map<Long, ExternalServiceType> blockingServiceDataMap = new HashMap<Long, ExternalServiceType>(pricePlanBlockingExternalServiceTypeBeans);
        for (Long blockingSrvId : pricePlanBlockingExternalServiceTypeBeans.keySet())
        {
            if(newServiceIdsSet.remove(blockingSrvId))
            {
                blockingServiceDataMap.remove(blockingSrvId);
            }
        }

        // Step 3 - Blocking external services refer a ExternalServiceType, now FIND out if the referred
        // ExternalServiceType is present,
        // If ExternalServiceType Service defined by blockingServiceDataMap is present in NSL - DO NOT INCLUDE IN FINAL
        // SET (NSL + BlockingServices)

        
        for(Long serviceId : blockingServiceDataMap.keySet())
        {
            ExternalServiceType externalServiceType = blockingServiceDataMap.get(serviceId);
            pricePlanBlockingForExternalServiceTypeIds.add(externalServiceType.getBlockingForService());
        }
        
        
        //Working code
//        for (Iterator<Long> itr = newServiceListIds.iterator() ; itr.hasNext();  )
//        {
//            long nslServiceId = itr.next();
//            // We are taking ExternalServiceType bean from pricePlan to avoid extra home call, data is already fetched for PricePlan
//            ExternalServiceType externalServiceType = pricePlanExternalServiceTypeBeans.get(nslServiceId);
//
//            // if dependent service is present in NSL it suggests that the (blocking)service was suspended and thts the
//            // reason it did not arrive in NSL. So add the blocking service to NSL
//            // if dependent service is not present in NSL. there is no use sending the blocking for an absent enable service.
//            if (!pricePlanBlockingForExternalServiceTypeIds.contains(externalServiceType.getId()))
//            {
//                blockingServiceDataMap.remove(nslServiceId);
//            }
//        }
        
        Set<Long> blockingAddableSet = new HashSet<Long>();
        for (Iterator<Long> itr = newServiceIdsSet.iterator() ; itr.hasNext();  )
        {
            long nslServiceId = itr.next();
            // We are taking ExternalServiceType bean from pricePlan to avoid extra home call, data is already fetched for PricePlan
            ExternalServiceType externalServiceType = pricePlanExternalServiceTypeBeans.get(nslServiceId);

            // if dependent service is present in NSL it suggests that the (blocking)service was suspended and thts the
            // reason it did not arrive in NSL. So add the blocking service to NSL
            // if dependent service is not present in NSL. there is no use sending the blocking for an absent enable service.
            if (pricePlanBlockingForExternalServiceTypeIds.contains(externalServiceType.getId()))
            {
                for(Long blockingServiceId :blockingServiceDataMap.keySet())
                {
                    if(blockingServiceDataMap.get(blockingServiceId).getBlockingForService() == externalServiceType.getId())
                    {
                        blockingAddableSet.add(blockingServiceId);
                    }
                }
            }
        }
        
        newServiceIdsSet.addAll(blockingAddableSet);

        // Now newNSLlist = NSL + blockingServiceDataMap

//        Set<Long> serviceIdList = new HashSet<Long>(newServiceIdsSet);
//        if (!blockingServiceDataMap.isEmpty())
//        {
//            for (Iterator<Long> itr = blockingServiceDataMap.keySet().iterator(); itr.hasNext();)
//            {
//                serviceIdList.add(itr.next());
//            }
//        }
        
        
        
        /// Step 4 : remove all services from NSL which do not have an external service code
        Set<Long> finalNSL = new HashSet<Long>();
        ArrayList<Object[]> productOfferingList = new ArrayList<Object[]>();
        ArrayList<Object[]> productSpecificationList = new ArrayList<Object[]>();

        for(Long serviceId : newServiceIdsSet)
        {
            if(pricePlanExternalServiceTypeBeans.containsKey(serviceId))
            {
                finalNSL.add(serviceId);
                
                Service serviceBean = pricePlanServiceBeans.get(serviceId);
                        
                if (serviceBean.getType().getIndex() == ServiceTypeEnum.EXTERNAL_PRICE_PLAN_INDEX)
                {
                    productOfferingList.add(new Object[] { serviceBean, pricePlanServiceExternionBeans.get(serviceId) });
                }
                else if (serviceBean.getType().getIndex() == ServiceTypeEnum.GENERIC_INDEX)
                {
                    productSpecificationList.add(new Object[] { serviceBean, pricePlanServiceExternionBeans.get(serviceId) });
                }
            }
        }
        
        // case#4
        if (productOfferingList.isEmpty() && productSpecificationList.isEmpty())
        {
            if(isRecurrence)
            {
                String errorMsg = "ProductOffering and ProductSpecification list empty, please check PricePlan config for presence of ExternalServiceType";
                LogSupport.major(ctx, LOG_CLASS_NAME_CONSTANT, errorMsg);
                throw new IllegalStateException(errorMsg);
            }
            else
            {
                return getSuspendResumeServiceList(ctx, ppv, "", true);
            }
        }
        else
        {
            StringBuilder builderbean = new StringBuilder();
            builderbean.append(getProductOfferingString2(ctx, productOfferingList));
            builderbean.append(getProductSpecificationString(ctx, productSpecificationList));
            return builderbean.toString();
        }
    }

    /**
     * @param string
     * @return
     * @throws HomeException
     * @throws HomeInternalException
     */
    private static ExternalServiceType getExternalServiceType(Context ctx, long externalServiceTypeId)
            throws HomeInternalException, HomeException
    {
        final And condition = new And();
        condition.add(new EQ(ExternalServiceTypeXInfo.ID, externalServiceTypeId));

        return HomeSupportHelper.get(ctx).findBean(ctx, ExternalServiceType.class, condition);
    }


    private static Object getProductOfferingString2(Context ctx, ArrayList<Object[]> productOfferingList)
    {
        /**
         * <ProductOffering> <name><![CDATA[VoicePlan]]></name> <id><![CDATA[WRXV9]]></id> </ProductOffering>
         */
        StringBuilder builderbean = new StringBuilder();
        if (productOfferingList.isEmpty())
        {
            return "";
        }

        ExternalServiceTypeExtension externalServiceTypeExtension = null;
        for (Object[] objArr : productOfferingList)
        {

            Service productOfferingService = (Service) objArr[0];
            externalServiceTypeExtension = (ExternalServiceTypeExtension) objArr[1];

            try
            {
                ExternalServiceType externalServiceType = getExternalServiceType(ctx,
                        externalServiceTypeExtension.getExternalServiceType());
                builderbean
                        .append(PRODUCT_OFFERING_START + externalServiceType.getServiceTypeValue()
                                + PRODUCT_OFFERING_MID + productOfferingService.getExternalServiceCode()
                                + PRODUCT_OFFERING_END);
            }
            catch (Throwable e)
            {
                String errorMsg = "Error while ProductOffering and ProductSpecificationType creation : "
                        + e.getMessage();
                LogSupport.major(ctx, LOG_CLASS_NAME_CONSTANT, errorMsg, e);
                throw new IllegalStateException(errorMsg);
            }
        }

        return builderbean.toString();
    }
    
    private static String getProductSpecificationString(Context ctx, ArrayList<Object[]> productSpecificationList)
    {
        /**
         * <ProductSpecificationType> <ProductSpecificationValue> <describedBy> <item type="VoiceFeatures">
         * <value><![CDATA[yes]]></value> <features><![CDATA[WRXMH]]></features> </item> <item type="VoiceFeatures">
         * <value><![CDATA[yes]]></value> <features><![CDATA[WRXMG]]></features> </item> </describedBy>
         * </ProductSpecificationValue> </ProductSpecificationType>
         */

        StringBuilder builderbean = new StringBuilder();
        if (productSpecificationList.isEmpty())
        {
            return "";
        }

        builderbean.append(PRODUCT_SPECIFICATION_HEADER_START);
        Service ss = null;
        ExternalServiceTypeExtension externalServiceTypeExtension = null;
        for (Object[] objArr : productSpecificationList)
        {

            ss = (Service) objArr[0];
            externalServiceTypeExtension = (ExternalServiceTypeExtension) objArr[1];
            
            try
            {
                ExternalServiceType externalServiceType = getExternalServiceType(ctx,
                        externalServiceTypeExtension.getExternalServiceType());
                builderbean
                        .append(PRODUCT_SPECIFICATION_ITEM_START + externalServiceType.getServiceTypeValue()
                                + PRODUCT_SPECIFICATION_ITEM_MID + ss.getExternalServiceCode()
                                + PRODUCT_SPECIFICATION_ITEM_END);
            }
            catch (Throwable e)
            {
                String errorMsg = "Error while ProductOffering and ProductSpecificationType creation : "
                        + e.getMessage();
                LogSupport.major(ctx, LOG_CLASS_NAME_CONSTANT, errorMsg, e);
                throw new IllegalStateException(errorMsg);
            }
        }

        builderbean.append(PRODUCT_SPECIFICATION_HEADER_END);
        return builderbean.toString();
    }

    
    // public static void main(String[] args)
    // {
    //
    // String newServicesDataList[] = "".split(SERVICE_LIST_SEPERATOR);
    // Set<Long> newServiceIdsSet = new HashSet<Long>(newServicesDataList.length);
    // Map<Long, String> newServiceListDataMap = new HashMap<Long, String>();
    // System.out.println(newServicesDataList.length);
    // for (String serviceData : newServicesDataList)
    // {
    // String[] serviceDetails = serviceData.split(SINGLE_SERVICE_DATA_SEPERATOR);
    // long serviceId = Long.parseLong(serviceDetails[0]);
    // newServiceIdsSet.add(serviceId);
    // newServiceListDataMap.put(serviceId, serviceData);
    // }
    //
    // System.out.println(getAddAndRemoveServiceList(null, "", ""));
    // String sff = "fsfdgfd";
    // String newServicesDataList[] = sff.split(SERVICE_LIST_SEPERATOR);
    // System.out.println(newServicesDataList[0]);
    // }
}
