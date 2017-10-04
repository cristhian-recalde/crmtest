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
package com.trilogy.app.crm.api.rmi;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Adapter;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.LogSupport;

import com.trilogy.app.crm.api.rmi.support.APIGenericParameterSupport;
import com.trilogy.app.crm.bean.AuxiliaryService;
import com.trilogy.app.crm.bean.PricePlanStateEnum;
import com.trilogy.app.crm.bean.PricePlanSubTypeEnum;
import com.trilogy.app.crm.bean.ServiceSubTypeEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberAuxiliaryService;
import com.trilogy.app.crm.bean.SubscriberServices;
import com.trilogy.app.crm.bean.core.BundleFee;
import com.trilogy.app.crm.bean.core.PricePlan;
import com.trilogy.app.crm.bean.core.PricePlanVersion;
import com.trilogy.app.crm.bean.core.Service;
import com.trilogy.app.crm.bean.core.ServiceFee2;
import com.trilogy.app.crm.bundle.SubscriberBundleSupport;
import com.trilogy.app.crm.subscriber.charge.support.BundleChargingSupport;
import com.trilogy.app.crm.subscriber.charge.support.ServiceChargingSupport;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.app.crm.support.PricePlanSupport;
import com.trilogy.app.crm.support.ServiceSupport;
import com.trilogy.app.crm.support.SubscriberAuxiliaryServiceSupport;
import com.trilogy.app.crm.support.SubscriberServicesSupport;
import com.trilogy.util.crmapi.wsdl.v2_0.types.GenericParameter;
import com.trilogy.util.crmapi.wsdl.v3_0.api.CRMExceptionFault;
import com.trilogy.util.crmapi.wsdl.v3_0.types.serviceandbundle.PricePlanOption;
import com.trilogy.util.crmapi.wsdl.v3_0.types.subscription.SubscriptionPricePlan;


/**
 * Adapts PricePlan object to API objects.
 * 
 * @author victor.stratan@redknee.com
 */
public class SubscriptionPricePlanOptionToApiAdapter implements Adapter
{
	
	public static final String CLASS_NAME = SubscriptionPricePlanOptionToApiAdapter.class.getName();

	public static SubscriptionPricePlan getSubscriptionPricePlanOption(final Context ctx, final Subscriber sub,
            final Long pricePlanId, final Boolean sendMRCAndDiscount) throws HomeException, CRMExceptionFault
    {
        SubscriptionPricePlan subPlanOption;
        subPlanOption = new SubscriptionPricePlan();
        subPlanOption.setIsSelected(false);
        long actualPricePlanId;
        int actualPricePlanVersionId;
		Map<String, Long> ppFees = null;
        
        if (pricePlanId != null)
        {
            actualPricePlanId = pricePlanId.longValue();
            if (sub != null && actualPricePlanId == sub.getPricePlan())
            {
                subPlanOption.setIsSelected(true);
                actualPricePlanVersionId = sub.getPricePlanVersion();
            }
            else
            {
            	PricePlanVersion currentVersion = PricePlanSupport.getCurrentVersion(ctx, actualPricePlanId);
            	if(currentVersion == null)
            	{
            		actualPricePlanVersionId = -1;
            	}
            	else
            	{
            		actualPricePlanVersionId = currentVersion.getVersion();
            	}
            }
        }
        else
        {
            if (sub != null)
            {
                actualPricePlanId = sub.getPricePlan();
                actualPricePlanVersionId = sub.getPricePlanVersion();
            }
            else
            {
                throw new HomeException("Did not provide either subscriber reference or priceplanId");
            }
        }
        
        PricePlan plan = PricePlanSupport.getPlan(ctx, actualPricePlanId);        
        if (plan == null)
        {
            throw new HomeException("Price plan not found with identifier=" + actualPricePlanId);
        }
        com.redknee.framework.xhome.msp.MSP.setBeanSpid(ctx, plan.getSpid());
        
        if (sub != null)
        {
            PricePlanVersion version = PricePlanSupport.getVersion(ctx, plan.getId(), actualPricePlanVersionId);
            List<com.redknee.app.crm.bean.PricePlanVersion> ppvList = new ArrayList<com.redknee.app.crm.bean.PricePlanVersion>();
            ppvList.add(version);
            com.redknee.util.crmapi.wsdl.v3_0.types.serviceandbundle.PricePlan apiPricePlan = PricePlanToApiAdapter
                    .adaptPricePlanToApi(ctx, plan, ppvList);
            PricePlanOption[] servicePlanOptions = PricePlanServiceToPricePlanOptionAdapter
                    .getSubscriberServicesConvertedPricePlanOption(ctx, sub);
            PricePlanOption[] bundlePlanOptions = PricePlanBundleToPricePlanOptionAdapter
                    .getSubscriberBundlesConvertedPricePlanOption(ctx, sub, plan, version);
            PricePlanOption[] auxServiceOptions = AuxiliaryServiceToPricePlanOptionAdapter
                    .getSubscriberAuxiliaryServiceToPricePlanOption(ctx, sub);
            PricePlanOption[] auxiliaryBundleOptions = AuxiliaryBundleToPricePlanOptionAdapter
                    .getSubscriberAuxiliaryBundleToPricePlanOption(ctx, sub, plan, version);
            subPlanOption.setPricePlanDetails(apiPricePlan);
            subPlanOption.setItems(mergeAllOptionsIntoOne(bundlePlanOptions, auxiliaryBundleOptions, auxServiceOptions,
                    servicePlanOptions));
            subPlanOption.setPricePlanDetails(apiPricePlan);
            
            /** 
             *  BSS will send MRC and Discount amount when requested
             *  Scenario: During one time top up from PickNPay page of WSC or one-time top up through IVR 
             *  getSubscriptionPricePlanOptions() API will be called with generic boolean parameter 'SendMRCAndDiscount' as true
             */
            if(sendMRCAndDiscount.equals(Boolean.TRUE))
            {
            	ppFees = new HashMap<String, Long>();
            	ppFees.put(APIGenericParameterSupport.MRC_AMOUNT, new Long(0));
            	ppFees.put(APIGenericParameterSupport.DISCOUNT_AMOUNT, new Long(0));
            	
            	ppFees = calculateMRCFeeAndDiscountAmount(ctx, sub, ppFees);
            	if(ppFees != null)
            	{
            		setMRCAndDiscountGenericParams(ctx, subPlanOption, ppFees);
            	}
            }
        }
        else
        {
        	PricePlanVersion version = null;
        	List<com.redknee.app.crm.bean.PricePlanVersion> ppvList = new ArrayList<com.redknee.app.crm.bean.PricePlanVersion>();
        	if(actualPricePlanVersionId == -1 && PricePlanStateEnum.PENDING_ACTIAVTION.equals(plan.getState()))
        	{
        		//version may exist but still not active. We still need to send this info because ProdcutCatalog (IVP) needs to show such plans.
        		version = PricePlanSupport.findHighestVersion(ctx, plan);
        	}
        	else
        	{
        		version = PricePlanSupport.getVersion(ctx, plan.getId(), plan.getCurrentVersion());
        	}
        	if(version != null)
        	{
        		ppvList.add(version);
	            com.redknee.util.crmapi.wsdl.v3_0.types.serviceandbundle.PricePlan apiPricePlan = PricePlanToApiAdapter
	                    .adaptPricePlanToApi(ctx, plan, ppvList);
	            Collection<ServiceFee2> allServices = version.getServiceFees(ctx).values();
	            PricePlanOption[] servicePlanOptions = PricePlanServiceToPricePlanOptionAdapter
	                    .getServicesConvertedPricePlan(ctx, allServices);
	            Collection<BundleFee> allBundles = SubscriberBundleSupport.getPricePlanBundles(ctx, plan, version).values();
	            PricePlanOption[] bundlePlanOptions = PricePlanBundleToPricePlanOptionAdapter
	                    .getBundlesConvertedPricePlanOption(ctx, allBundles);
	            PricePlanOption[] auxServiceOptions = AuxiliaryServiceToPricePlanOptionAdapter.getAuxiliaryService(ctx,
	                    plan.getSpid());
	            PricePlanOption[] auxiliaryBundleOptions = AuxiliaryBundleToPricePlanOptionAdapter
	                    .getAuxiliaryBundleToPricePlanOption(ctx, plan, version);
	            subPlanOption.setPricePlanDetails(apiPricePlan);
	            subPlanOption.setItems(mergeAllOptionsIntoOne(bundlePlanOptions, auxiliaryBundleOptions, auxServiceOptions,
	                    servicePlanOptions));
        	}
        	else
        	{
        		com.redknee.util.crmapi.wsdl.v3_0.types.serviceandbundle.PricePlan apiPricePlan = PricePlanToApiAdapter
	                    .adaptPricePlanToApi(ctx, plan, ppvList);
        		subPlanOption.setPricePlanDetails(apiPricePlan);
        		subPlanOption.setItems(new PricePlanOption[0]);
        	}
        }
        return subPlanOption;
    }
	
	/**
	 *  Method that sets generic parameters specific to One time top up call flow
	 * @param ctx
	 * @param subPlanOption
	 * @param ppFees
	 */
	
	private static void setMRCAndDiscountGenericParams(Context ctx, SubscriptionPricePlan subPlanOption, Map<String, Long> ppFees)
	{
		GenericParameter mrcParam = new GenericParameter();
        mrcParam.setName(APIGenericParameterSupport.MRC_AMOUNT);
        mrcParam.setValue(ppFees.get(APIGenericParameterSupport.MRC_AMOUNT));
        subPlanOption.addParameters(mrcParam);

        GenericParameter discountParam = new GenericParameter();
        discountParam.setName(APIGenericParameterSupport.DISCOUNT_AMOUNT);
        discountParam.setValue(ppFees.get(APIGenericParameterSupport.DISCOUNT_AMOUNT));
        subPlanOption.addParameters(discountParam);
        
        if(LogSupport.isDebugEnabled(ctx))
        {
		    LogSupport.debug(ctx, CLASS_NAME, "Setting Generic Parameters "+ APIGenericParameterSupport.MRC_AMOUNT + " = " +mrcParam.getValue()+", "
		    		+APIGenericParameterSupport.DISCOUNT_AMOUNT + " = "+discountParam.getValue());
        }
	}

    public static PricePlanOption[] mergeAllOptionsIntoOne(PricePlanOption[] bundlePlanOptions,
            PricePlanOption[] auxiliaryBundleOptions, PricePlanOption[] auxServiceOptions,
            PricePlanOption[] servicePlanOptions)
    {
        PricePlanOption[] options = new PricePlanOption[servicePlanOptions.length + bundlePlanOptions.length
                + auxServiceOptions.length + auxiliaryBundleOptions.length];
        int counter = 0;
        for (int i = 0; i < servicePlanOptions.length; i++)
        {
            options[counter] = servicePlanOptions[i];
            counter++;
        }
        for (int i = 0; i < bundlePlanOptions.length; i++)
        {
            options[counter] = bundlePlanOptions[i];
            counter++;
        }
        
        for (int i = 0; i < auxServiceOptions.length; i++)
        {
            options[counter] = auxServiceOptions[i];
            counter++;
        }
        for (int i = 0; i < auxiliaryBundleOptions.length; i++)
        {
            options[counter] = auxiliaryBundleOptions[i];
            counter++;
        }
        return options;
    }
    
    
    /**
     * Method that calculates MRC fee and discount fee for a subscriber based on recurrence with primary service
     * For PickNPay priceplan, discount is service of subtype discount with negative charge
     * 
     * @param ctx
     * @param subscriber
     * @param ppFees
     * @return MRC amount and discount amount
     */
	private static Map<String, Long> calculateMRCFeeAndDiscountAmount(Context ctx, Subscriber subscriber, Map<String, Long> ppFees)
	{
		long pricePlanFee = 0;
		long discount = 0;
		Date primarySvcRecurrenceDate = null;
		
		Collection<SubscriberServices> servicesToBeCharged = new  HashSet<SubscriberServices>();
		Collection<Long> bundlesToBeCharged= new  HashSet<Long>();
		Collection<SubscriberAuxiliaryService> auxServicesToBeCharged= new  HashSet<SubscriberAuxiliaryService>(); 
		
		Map<com.redknee.app.crm.bean.core.ServiceFee2, SubscriberServices> serviceFees = SubscriberServicesSupport.getSubscribedSubscriberServicesMap(ctx, subscriber);
		servicesToBeCharged.addAll(serviceFees.values());
		
		bundlesToBeCharged.addAll(BundleChargingSupport.getSubscribedBundles(ctx, subscriber).keySet()); 
		auxServicesToBeCharged.addAll(SubscriberAuxiliaryServiceSupport.getSubscriberAuxiliaryServices(ctx, subscriber.getId()));
		
		try
		{
			ServiceFee2 primarySvc = null;
			boolean isPickNPayPricePlan;
			
			for (Iterator<com.redknee.app.crm.bean.core.ServiceFee2> i = serviceFees.keySet().iterator(); i.hasNext();)
			{
				ServiceFee2 fee = i.next();
				if(fee.isPrimary())
				{
					primarySvc = fee;
				}
			}
			
			if(primarySvc == null)
			{
				LogSupport.minor(ctx, CLASS_NAME, "No primary / membership service found for subscriber "+subscriber.getId()
						+". Error in calculating MRC amount and Discount amount. Will not be returning these values.");
				return null;
			}
			
			 PricePlan pricePlan = PricePlanSupport.getPlan(ctx, subscriber.getPricePlan()); 
			 isPickNPayPricePlan = pricePlan.getPricePlanSubType().equals(PricePlanSubTypeEnum.PICKNPAY) ? true:false;
			 
			// else continue
			
			SubscriberServices membershipSubscriberSvc = serviceFees.get(primarySvc);
			Service membershipService = ServiceSupport.getService(ctx, membershipSubscriberSvc.getServiceId());
			
			if(membershipSubscriberSvc.getNextRecurringChargeDate() != null)
			{
				primarySvcRecurrenceDate = membershipSubscriberSvc.getNextRecurringChargeDate();	
			}
			
			if(LogSupport.isDebugEnabled(ctx))
			{
				LogSupport.debug(ctx, CLASS_NAME, "primarySvcRecurrenceDate:" + primarySvcRecurrenceDate+"For membershipService:"+membershipService);
			}	
			
			for (Iterator<com.redknee.app.crm.bean.core.ServiceFee2> i = serviceFees.keySet().iterator(); i.hasNext();)
			{
				ServiceFee2 fee = i.next();
				final SubscriberServices subService = (SubscriberServices) serviceFees.get(fee);
				Date nextRecurringChargeDate = null;
				
				if(subService.getNextRecurringChargeDate() != null)
				{
					nextRecurringChargeDate = CalendarSupportHelper.get(ctx).getDateWithNoTimeOfDay(subService.getNextRecurringChargeDate());
				}
				
			 if(primarySvcRecurrenceDate != null)
			 {
				if (nextRecurringChargeDate != null && nextRecurringChargeDate.equals(primarySvcRecurrenceDate))
				{
					Service service = ServiceSupport.getService(ctx, fee.getServiceId());
					
					if(!service.getServiceSubType().equals(ServiceSubTypeEnum.DISCOUNT))
					{
						if(LogSupport.isDebugEnabled(ctx))
			            {
			                LogSupport.debug(ctx, CLASS_NAME, "Service fees for subscriber : " + subscriber.getId() + " serviceID : " + fee.getServiceId()
			                		+ " with nextRecurDate " + nextRecurringChargeDate + " same as primary service.");
			            }
						pricePlanFee  = pricePlanFee + fee.getFee();
					}
					else
					{
						if(LogSupport.isDebugEnabled(ctx))
			            {
			                LogSupport.debug(ctx, CLASS_NAME, "Discount Service fees for subscriber : " + subscriber.getId() + " serviceID : " + fee.getServiceId()
			                		+ " with nextRecurDate " + nextRecurringChargeDate + " same as primary service.");
			            }
						// discount fee is always negative
						discount = discount + (- fee.getFee());
					}
				}
			 }
			 else 
			 {
					Service service = ServiceSupport.getService(ctx,fee.getServiceId());

					if (membershipService.equals(service)) 
					{
						if (LogSupport.isDebugEnabled(ctx)) 
						{
							LogSupport.debug(ctx,CLASS_NAME,"Subscriber is Not in Active state...Primary Service Next Recurring Charge date is null : "
													+ subscriber.getId()
													+ " serviceID : "
													+ fee.getServiceId());
						}

						if (!service.getServiceSubType().equals(ServiceSubTypeEnum.DISCOUNT)) 
						{

							if (isPickNPayPricePlan) 
							{
								if (fee.isApplyWithinMrcGroup()) 
								{
									pricePlanFee = pricePlanFee + fee.getFee();
								}
							} 
							else 
							{
								pricePlanFee = pricePlanFee + fee.getFee();
							}
						} 
						else 
						{
							if (LogSupport.isDebugEnabled(ctx)) 
							{
								LogSupport.debug(ctx,CLASS_NAME,"Discount Service fees for subscriber : "
												+ subscriber.getId()
												+ " serviceID : "
												+ fee.getServiceId());
							}
							// discount fee is always negative
							discount = discount + (-fee.getFee());
						}
					}

				}
			}

			if(LogSupport.isDebugEnabled(ctx))
			{
			    LogSupport.debug(ctx, CLASS_NAME, "Calculated service fees for subscriber : " + subscriber.getId() + " is : " + pricePlanFee);
			    LogSupport.debug(ctx, CLASS_NAME, "Calculated total DISCOUNT amount for subscriber is : "+ discount);
			}

			long cumulativeBundleFees = 0;
			Collection<BundleFee> bundleFees = BundleChargingSupport.getProvisionedBundles(ctx, SubscriberBundleSupport.getSubscribedBundles(ctx, subscriber).values(), 
					bundlesToBeCharged);
			
			for(BundleFee fee : bundleFees)
			{
				Date nextRecurringChargeDate = null;
				
				if(fee.getNextRecurringChargeDate() != null)
				{
					nextRecurringChargeDate = CalendarSupportHelper.get(ctx).getDateWithNoTimeOfDay(fee.getNextRecurringChargeDate());
				}
				
				if(nextRecurringChargeDate != null && primarySvcRecurrenceDate != null && nextRecurringChargeDate.equals(primarySvcRecurrenceDate))
				{
					if(LogSupport.isDebugEnabled(ctx))
	                {
	                    LogSupport.debug(ctx, CLASS_NAME, "Bundle fees for subscriber : " + subscriber.getId() + " serviceID : " + fee.getId()
	                    		+ " is part of PickNPay MRC group.");
	                }
				    cumulativeBundleFees = cumulativeBundleFees + fee.getFee();
				} 
			}        
			
			pricePlanFee = pricePlanFee + cumulativeBundleFees;

			if(LogSupport.isDebugEnabled(ctx))
            {
			    LogSupport.debug(ctx, CLASS_NAME, "Calculated bundle fees for subscriber : " + subscriber.getId() + " is : " + cumulativeBundleFees);
            }
			
			long cumulativeAuxServiceFees = 0; 
			for (SubscriberAuxiliaryService subAuxService: auxServicesToBeCharged)
			{
			    if(subAuxService == null)
			        continue;
			    
				AuxiliaryService service = subAuxService.getAuxiliaryService(ctx);
				Date nextRecurringChargeDate = null;
				if(nextRecurringChargeDate != null)
				{
					nextRecurringChargeDate = CalendarSupportHelper.get(ctx).getDateWithNoTimeOfDay(subAuxService.getNextRecurringChargeDate());
				}
				
				if (nextRecurringChargeDate != null && primarySvcRecurrenceDate != null && nextRecurringChargeDate.equals(primarySvcRecurrenceDate))
				{
					if(LogSupport.isDebugEnabled(ctx))
	                {
	                    LogSupport.debug(ctx, CLASS_NAME, "Auxiliary Service fees for subscriber : " + subscriber.getId() + " serviceID : " + service.getID()
	                    		+ " with nextRecurDate " + nextRecurringChargeDate + " same as primary service.");
	                }
					
				    cumulativeAuxServiceFees = cumulativeAuxServiceFees + service.getCharge();
				}
			}
			pricePlanFee = pricePlanFee + cumulativeAuxServiceFees;
			
			if(LogSupport.isDebugEnabled(ctx))
            {
			    LogSupport.debug(ctx, CLASS_NAME, "Calculated auxiliary service fees subscriber : " + subscriber.getId() + " is : " + cumulativeAuxServiceFees);
            
			    LogSupport.debug(ctx, CLASS_NAME, "Total charges for subscriber : MRC AMOUNT " + subscriber.getId() + " is : " + pricePlanFee);
            }
			
			ppFees.put(APIGenericParameterSupport.MRC_AMOUNT, pricePlanFee);
			ppFees.put(APIGenericParameterSupport.DISCOUNT_AMOUNT, discount);
            
		}
		catch (Throwable t)
		{
		    LogSupport.major(ctx, CLASS_NAME, "Exception in calculating MRC amount and Discount amount. Will not be returning these values." + t.getMessage(), t);
		    return null;
		}
		
		return ppFees;
	}


    @Override
    public Object adapt(Context arg0, Object arg1) throws HomeException
    {
        // TODO Auto-generated method stub
        return null;
    }


    @Override
    public Object unAdapt(Context arg0, Object arg1) throws HomeException
    {
        // TODO Auto-generated method stub
        return null;
    }
}
