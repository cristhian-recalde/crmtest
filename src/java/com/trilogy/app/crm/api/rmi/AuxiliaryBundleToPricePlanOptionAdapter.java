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
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.trilogy.app.crm.api.rmi.support.APIGenericParameterSupport;
import com.trilogy.app.crm.api.rmi.support.RmiApiSupport;
import com.trilogy.app.crm.bean.PricePlan;
import com.trilogy.app.crm.bean.PricePlanVersion;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.core.BundleFee;
import com.trilogy.app.crm.bean.core.BundleProfile;
import com.trilogy.app.crm.bundle.ActivationFeeCalculationEnum;
import com.trilogy.app.crm.bundle.InvalidBundleApiException;
import com.trilogy.app.crm.bundle.SubscriberBundleSupport;
import com.trilogy.app.crm.support.BundleSupportHelper;
import com.trilogy.app.crm.support.SubscriberAuxiliaryServiceSupport;
import com.trilogy.app.crm.support.SuspendedEntitySupport;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Adapter;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.MinorLogMsg;
import com.trilogy.util.crmapi.wsdl.v2_0.types.GenericParameter;
import com.trilogy.util.crmapi.wsdl.v2_0.types.ProvisioningStateTypeEnum;
import com.trilogy.util.crmapi.wsdl.v2_1.types.serviceandbundle.PricePlanOptionType;
import com.trilogy.util.crmapi.wsdl.v2_1.types.serviceandbundle.PricePlanOptionTypeEnum;
import com.trilogy.util.crmapi.wsdl.v2_2.types.serviceandbundle.PricePlanOptionStateTypeEnum;
import com.trilogy.util.crmapi.wsdl.v3_0.types.serviceandbundle.PricePlanOption;


/**
 * Adapts PricePlan object to API objects.
 * 
 * @author victor.stratan@redknee.com
 */
public class AuxiliaryBundleToPricePlanOptionAdapter implements Adapter
{

    AuxiliaryBundleToPricePlanOptionAdapter()
    {
    }


    @Override
    public Object adapt(final Context ctx, final Object obj) throws HomeException
    {
        return convertBundleToPricePlanOption(ctx, (BundleFee) obj, new HashMap(),
                PricePlanOptionTypeEnum.AUXILIARY_BUNDLE.getValue());
    }


    @Override
    public Object unAdapt(final Context ctx, final Object obj) throws HomeException
    {
        throw new UnsupportedOperationException();
    }

    
    /**
     * Returns a list of Auxiliary Bundles that are not available in given PricePlanVersion. Set's 'isSelected' 
     * field to false. 
     *  
     * @param ctx
     * @param pp        PricePlan
     * @param ppv       PricePlanVersion
     * @return
     * @throws HomeException
     */    
    public static PricePlanOption[] getAuxiliaryBundleToPricePlanOption(Context ctx,
            final PricePlan plan, final PricePlanVersion version) throws HomeException
    {
        
        return getSubscriberAuxiliaryBundleToPricePlanOption(ctx, null, plan, version);
    }

    /**
     * Returns a list of Auxiliary Bundles that are not available in given PricePlanVersion. If Subscriber is
     * not null, set's 'isSelected' field to true if Bundle is associated with Subscriber.
     *  
     * @param ctx
     * @param sub       Subscriber
     * @param pp        PricePlan
     * @param ppv       PricePlanVersion
     * @return
     * @throws HomeException
     */    
    public static PricePlanOption[] getSubscriberAuxiliaryBundleToPricePlanOption(Context ctx, final Subscriber sub,
            final PricePlan plan, final PricePlanVersion version) throws HomeException
    {
        final Map<Long, BundleFee> selectedAuxBundles; 
        if (sub != null)
        {
            selectedAuxBundles = sub.getBundles();
        }
        else
        {
            selectedAuxBundles = Collections.EMPTY_MAP;
        }
        final Map<Long, BundleFee> ppBundles = SubscriberBundleSupport.getPricePlanBundles(ctx, plan, version);
        
		// get all auxiliary bundles, even the deprecated ones.
		final Map<Long, BundleFee> allAuxBundles;
		if (sub != null)
		{
		    allAuxBundles = SubscriberBundleSupport.getAvailableAuxiliaryBundlesForSubscriber(
	                ctx, plan.getSpid(), plan.getPricePlanType(), sub);
		}
		else
		{
		    allAuxBundles = SubscriberBundleSupport.getAvailableAuxiliaryBundles(ctx, plan.getSpid(), plan.getPricePlanType());
		}

        //Remove Price Plan bundles
        allAuxBundles.keySet().removeAll(ppBundles.keySet());
        
		List<PricePlanOption> options = new LinkedList<PricePlanOption>();
        int i = 0;
        for (BundleFee fee : allAuxBundles.values())
        {
            PricePlanOption planOption = convertBundleToPricePlanOption(ctx, fee, selectedAuxBundles,
                    PricePlanOptionTypeEnum.AUXILIARY_BUNDLE.getValue());

            if (sub != null)
            {
                boolean isSuspended = false;
                try
                {
                    isSuspended = SuspendedEntitySupport.isSuspendedEntity(ctx, 
                            sub.getId(), 
                            fee.getId(), 
                            SubscriberAuxiliaryServiceSupport.SECONDARY_ID_NOT_USED, 
                            BundleFee.class);
                }
                catch (HomeException e)
                {
                    new MinorLogMsg(AuxiliaryBundleToPricePlanOptionAdapter.class, 
                            "Error looking up suspended state of bundle " + fee.getId()
                            + " for subscription " + sub.getId() + ".  Assuming not suspended...", e).log(ctx);
                }
                
                if (isSuspended)
                {
                    planOption.setProvisioningState(ProvisioningStateTypeEnum.SUSPENDED.getValue());
                }
            }
            
			options.add(planOption);
            i++;
        }
		return options.toArray(new PricePlanOption[] {});
    }


    public static PricePlanOption[] getAuxiliaryBundles(Context ctx, Collection<BundleFee> auxBundles)
            throws HomeException
    {
        List<PricePlanOption> options = new ArrayList<PricePlanOption>();
        Iterator<BundleFee> iter = auxBundles.iterator();
        int i = 0;
        while (iter.hasNext())
        {
            BundleFee bundleFee = iter.next();
            if (bundleFee.isAuxiliarySource())
            {
                PricePlanOption planOption = convertBundleToPricePlanOption(ctx, bundleFee, new HashMap(),
                        PricePlanOptionTypeEnum.AUXILIARY_BUNDLE.getValue());
                options.add(planOption);
                i++;
            }
        }
        return options.toArray(new PricePlanOption[]{});
    }


    public static PricePlanOption convertBundleToPricePlanOption(Context ctx, BundleFee fee,
            Map<Long, BundleFee> selectedBundles, PricePlanOptionType optionType) throws HomeException
    {
        final com.redknee.util.crmapi.wsdl.v3_0.types.serviceandbundle.PricePlanOption planOption;
        planOption = new com.redknee.util.crmapi.wsdl.v3_0.types.serviceandbundle.PricePlanOption();
        planOption.setIdentifier(fee.getId());
        planOption.setEndDate(fee.getEndDate());
        planOption.setStartDate(fee.getStartDate());
        planOption.setOptionType(optionType);
        BundleProfile bundle = null;
        try
        {
            bundle = BundleSupportHelper.get(ctx).getBundleProfile(ctx, fee.getId());
        }
        catch (InvalidBundleApiException invalidApi)
        {
            throw new HomeException("Unable to find bundle profile for " + fee.getId());
        }
        planOption.setName(bundle.getName());
        planOption.setAdjustmentTypeID((long) bundle.getAdjustmentType());
        planOption.setProvisioningState(ProvisioningStateTypeEnum.NOT_PROVISIONED.getValue());
        planOption.setIsSelected(false);
        planOption.setProrationEnabled(bundle.getActivationFeeCalculation()
                .equals(ActivationFeeCalculationEnum.PRORATE));
        planOption.setFee(fee.getFee());
        if (!bundle.isEnabled())
        {
            planOption.setOptionState(PricePlanOptionStateTypeEnum.DEPRECATED.getValue());
        }
        else
        {
            planOption.setOptionState(PricePlanOptionStateTypeEnum.ACTIVE.getValue());
        }
        
        final BundleFee selectedBundle = selectedBundles.get(fee.getId());
        
        if (selectedBundle != null)
        {
            // previously picked service
            planOption.setStartDate(selectedBundle.getStartDate());
            planOption.setEndDate(selectedBundle.getEndDate());
            planOption.setIsSelected(true);
            if(planOption.getStartDate().before(new Date()) || planOption.getStartDate().equals(new Date()))
            {
            	planOption.setProvisioningState(ProvisioningStateTypeEnum.PROVISIONED.getValue());
            }
            else 
            {
            	planOption.setProvisioningState(ProvisioningStateTypeEnum.NOT_PROVISIONED.getValue());
            }
            
        }
        planOption.setRecurrence(RmiApiSupport.getRecurrenceScheme(bundle));
        planOption.setParameters(getAllGenericParameter(ctx, bundle, fee, selectedBundle));
        return planOption;
    }


    private static GenericParameter[] getAllGenericParameter(Context ctx, BundleProfile bundleProfile, 
            BundleFee fee, BundleFee selectedBundle)
    {
        Collection<GenericParameter> parametersList = new ArrayList<GenericParameter>();
        parametersList.add(APIGenericParameterSupport.getPricePlanPreferenceTypeParameter(ctx, fee));
        parametersList.add(APIGenericParameterSupport.getBundleCategoryIdParameter(ctx, bundleProfile));
        parametersList.add(APIGenericParameterSupport.getBundleTypeParameter(ctx, bundleProfile));
        parametersList.add(APIGenericParameterSupport.getInitialBalanceLimitParameter(ctx, bundleProfile));
        parametersList.add(APIGenericParameterSupport.getBalanceUnitTypeParameter(ctx, bundleProfile));
        parametersList.add(APIGenericParameterSupport.getExpiryDateParameter(ctx, fee));
        if (selectedBundle != null && selectedBundle.getNextRecurringChargeDate() != null)
        {
            parametersList.add(APIGenericParameterSupport.getNextRecurringChargeDateParam(
                    selectedBundle.getNextRecurringChargeDate()));
        }
        parametersList.add(APIGenericParameterSupport.getRepurchasableParameter(ctx, bundleProfile));
        parametersList.add(APIGenericParameterSupport.getExpirySchemeParameter(ctx, bundleProfile));
       	parametersList.add(APIGenericParameterSupport.getApplyMRCGroupParam(fee.getApplyWithinMrcGroup()));

       	return parametersList.toArray(new GenericParameter[]{});
    }
}
