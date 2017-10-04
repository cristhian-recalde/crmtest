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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.trilogy.app.crm.api.rmi.support.APIGenericParameterSupport;
import com.trilogy.app.crm.bean.ActivationFeeModeEnum;
import com.trilogy.app.crm.bean.AuxiliaryServiceTypeEnum;
import com.trilogy.app.crm.bean.CallingGroupTypeEnum;
import com.trilogy.app.crm.bean.ClosedUserGroup;
import com.trilogy.app.crm.bean.FixedIntervalTypeEnum;
import com.trilogy.app.crm.bean.OneTimeTypeEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberAuxiliaryService;
import com.trilogy.app.crm.bean.core.custom.AuxiliaryService;
import com.trilogy.app.crm.extension.auxiliaryservice.core.custom.CallingGroupAuxSvcExtension;
import com.trilogy.app.crm.support.AuxiliaryServiceSupport;
import com.trilogy.app.crm.support.ClosedUserGroupSupport;
import com.trilogy.app.crm.support.ExtensionSupportHelper;
import com.trilogy.app.crm.support.SubscriberAuxiliaryServiceSupport;
import com.trilogy.app.crm.support.SuspendedEntitySupport;
import com.trilogy.framework.xhome.beans.SafetyUtil;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Adapter;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MinorLogMsg;
import com.trilogy.util.crmapi.wsdl.v2_0.types.GenericParameter;
import com.trilogy.util.crmapi.wsdl.v2_0.types.ProvisioningStateTypeEnum;
import com.trilogy.util.crmapi.wsdl.v2_1.types.serviceandbundle.PricePlanOptionTypeEnum;
import com.trilogy.util.crmapi.wsdl.v2_2.types.serviceandbundle.PricePlanOptionStateTypeEnum;
import com.trilogy.util.crmapi.wsdl.v2_2.types.serviceandbundle.RecurrenceTypeEnum;
import com.trilogy.util.crmapi.wsdl.v3_0.types.serviceandbundle.PricePlanOption;
import com.trilogy.util.crmapi.wsdl.v3_0.types.serviceandbundle.RecurrenceScheme;
import com.trilogy.util.crmapi.wsdl.v3_0.types.serviceandbundle.ServicePeriodEnum;

/**
 * Adapts PricePlan object to API objects.
 * 
 * @author kumaran.sivasubramaniam@redknee.com
 */
public class AuxiliaryServiceToPricePlanOptionAdapter implements Adapter
{

    AuxiliaryServiceToPricePlanOptionAdapter()
    {
    }


    @Override
    public Object adapt(final Context ctx, final Object obj) throws HomeException
    {
        return convertAuxiliaryServiceToPricePlanOption(ctx, (AuxiliaryService) obj, null);
    }


    @Override
    public Object unAdapt(final Context ctx, final Object obj) throws HomeException
    {
        throw new UnsupportedOperationException();
    }


	public static PricePlanOption[]
	    getSubscriberAuxiliaryServiceToPricePlanOption(Context ctx,
	        final Subscriber sub) throws HomeException
    {
        Collection<SubscriberAuxiliaryService> subAuxServices = SubscriberAuxiliaryServiceSupport
                .getSubscriberAuxiliaryServices(ctx, sub.getId());
		Map<Long, List<SubscriberAuxiliaryService>> selectedAuxServices =
		    new HashMap<Long, List<SubscriberAuxiliaryService>>();
        Iterator<SubscriberAuxiliaryService> iter = subAuxServices.iterator();
        while (iter.hasNext())
        {
            SubscriberAuxiliaryService subAuxService = iter.next();

            if (AuxiliaryServiceTypeEnum.MultiSIM.equals(subAuxService.getType(ctx))
                    && subAuxService.getSecondaryIdentifier() != SubscriberAuxiliaryService.DEFAULT_SECONDARYIDENTIFIER)
            {
                // Don't return SIM specific auxiliary services in the API
                if (LogSupport.isDebugEnabled(ctx))
                {
                    new DebugLogMsg(AuxiliaryServiceToPricePlanOptionAdapter.class, 
                            "Skipping SIM-specifc Multi-SIM auxiliary service association [AuxSvcId="
                            + subAuxService.getAuxiliaryServiceIdentifier()
                            + ",SecondaryId=" + subAuxService.getSecondaryIdentifier()
                            + ",SubId=" + subAuxService.getSubscriberIdentifier() + "] for API query response.", null).log(ctx);
                }
                continue;
            }
            
			Long key =
			    Long.valueOf(subAuxService.getAuxiliaryServiceIdentifier());
			List<SubscriberAuxiliaryService> subAuxList =
			    selectedAuxServices.get(key);
			if (subAuxList == null)
			{
				subAuxList = new LinkedList<SubscriberAuxiliaryService>();
			}
			subAuxList.add(subAuxService);
			selectedAuxServices.put(key, subAuxList);
        }
        Collection<AuxiliaryService> auxServices = AuxiliaryServiceSupport.getAllAvailableAuxiliaryServices(ctx, sub
                .getSpid());
        Iterator<AuxiliaryService> auxIter = auxServices.iterator();
        
        List<PricePlanOption> options = new ArrayList<PricePlanOption>();
        while (auxIter.hasNext())
        {
            AuxiliaryService auxService = auxIter.next();
            final List<SubscriberAuxiliaryService> auxServicesList = selectedAuxServices
                    .get(auxService.getIdentifier());            

            if (auxServicesList != null)
            {
                for (SubscriberAuxiliaryService subAuxService : auxServicesList)
                {
                    PricePlanOption planOption = convertAuxiliaryServiceToPricePlanOption(ctx, auxService, subAuxService);
                    options.add(planOption);
                }
            }
            // Don't add CUG or PCUG auxiliary services to the price plan options as they
            // can't be selected from the subscription perspective.
            else if (!AuxiliaryServiceTypeEnum.CallingGroup.equals(auxService.getType()) || !isCug(ctx, auxService))
            {
                PricePlanOption planOption = convertAuxiliaryServiceToPricePlanOption(ctx, auxService, null);
                options.add(planOption);
            }
        }
        return options.toArray(new PricePlanOption[]{});
    }


	public static PricePlanOption[] getAuxiliaryService(Context ctx, int spid)
	    throws HomeException
    {
        Collection<AuxiliaryService> auxServices = AuxiliaryServiceSupport.getAllAvailableAuxiliaryServices(ctx, spid);
        Iterator<AuxiliaryService> iter = auxServices.iterator();
        
        List<PricePlanOption> options = new ArrayList<PricePlanOption>();
        while (iter.hasNext())
        {
            AuxiliaryService auxService = iter.next();
            PricePlanOption planOption = convertAuxiliaryServiceToPricePlanOption(ctx, auxService, null);
            options.add(planOption);
        }
        return options.toArray(new PricePlanOption[]{});
    }


    private static PricePlanOption convertAuxiliaryServiceToPricePlanOption(Context ctx, AuxiliaryService auxService,
	    SubscriberAuxiliaryService subAuxiliaryService)
	    throws HomeException
    {
		final PricePlanOption planOption;
		planOption = new PricePlanOption();
        planOption.setIdentifier(auxService.getIdentifier());
        planOption.setEndDate(auxService.getEndDate());
        planOption.setStartDate(auxService.getStartDate());
        planOption.setOptionType(PricePlanOptionTypeEnum.AUXILIARY_SERVICE.getValue());
        planOption.setOptionState(PricePlanOptionStateTypeEnum.valueOf(auxService.getState().getIndex()));
        planOption.setName(auxService.getName());
        planOption.setAdjustmentTypeID((long) auxService.getAdjustmentType());
        planOption.setProvisioningState(ProvisioningStateTypeEnum.NOT_PROVISIONED.getValue());
        planOption.setProrationEnabled(auxService.getActivationFee().equals(ActivationFeeModeEnum.PRORATE));
        planOption.setFee(auxService.getCharge());
        RecurrenceScheme scheme = new RecurrenceScheme();

        if (auxService.getChargingModeType().equals(com.redknee.app.crm.bean.ServicePeriodEnum.MONTHLY))
        {
            scheme.setRecurrenceType(RecurrenceTypeEnum.RECURRING.getValue());
            scheme.setPeriod(Long.valueOf(1));
            scheme.setPeriodUnitType(ServicePeriodEnum.MONTHLY.getValue());
        }
        else if (auxService.getChargingModeType().equals(com.redknee.app.crm.bean.ServicePeriodEnum.WEEKLY))
        {
            scheme.setRecurrenceType(RecurrenceTypeEnum.RECURRING.getValue());
            scheme.setPeriod(Long.valueOf(1));
            scheme.setPeriodUnitType(ServicePeriodEnum.WEEKLY.getValue());
        }
        else if (auxService.getChargingModeType().equals(com.redknee.app.crm.bean.ServicePeriodEnum.DAILY))
        {
            scheme.setRecurrenceType(RecurrenceTypeEnum.RECURRING.getValue());
            scheme.setPeriod(Long.valueOf(1));
            scheme.setPeriodUnitType(ServicePeriodEnum.DAILY.getValue());
        }
        else if (auxService.getChargingModeType().equals(com.redknee.app.crm.bean.ServicePeriodEnum.MULTIMONTHLY))
        {
            scheme.setRecurrenceType(RecurrenceTypeEnum.RECURRING.getValue());
            scheme.setPeriod(Long.valueOf(auxService.getRecurrenceInterval()));
            scheme.setPeriodUnitType(ServicePeriodEnum.MONTHLY.getValue());
        }
        else if (auxService.getChargingModeType().equals(com.redknee.app.crm.bean.ServicePeriodEnum.MULTIDAY))
        {
            scheme.setRecurrenceType(RecurrenceTypeEnum.RECURRING.getValue());
            scheme.setPeriod(Long.valueOf(auxService.getRecurrenceInterval()));
            scheme.setPeriodUnitType(ServicePeriodEnum.DAILY.getValue());
        }
        else if (auxService.getChargingModeType().equals(com.redknee.app.crm.bean.ServicePeriodEnum.ONE_TIME))
        {
            scheme.setRecurrenceType(RecurrenceTypeEnum.ONE_TIME.getValue());
            if (auxService.getRecurrenceType().equals(OneTimeTypeEnum.ONE_OFF_FIXED_DATE_RANGE))
            {
                scheme.setEndDate(auxService.getEndDate());
                scheme.setStartDate(auxService.getStartDate());
            }
            else
            {
                scheme.setPeriod(Long.valueOf(auxService.getValidity()));
                if(auxService.getFixedInterval().getIndex() == FixedIntervalTypeEnum.MONTHS_INDEX)
                {
                    scheme.setPeriodUnitType(ServicePeriodEnum.MONTHLY.getValue());                    
                }
                else
                {
                    scheme.setPeriodUnitType(ServicePeriodEnum.DAILY.getValue());                     
                }     
                // see model's help <p>if Multi-monthly is selected this option will appear. It indicates the interval (in months) in which this service recurs.</p>
            }
        }

		/*
		 * TT#11042945053: Properly returning all instances of
		 * SubscriberAuxiliaryService for a single AuxiliaryService into
		 * PricePlanOption.
		 */
		if (subAuxiliaryService != null)
        {
			convertSubscriberAuxiliaryServiceToPricePlanOption(ctx,
			    auxService, subAuxiliaryService, planOption);
        }
        planOption.setRecurrence(scheme);
		GenericParameter[] parameters = getAllGenericParameter(ctx, auxService, subAuxiliaryService);
		for (int i = 0; i < parameters.length; i++)
		{
			planOption.addParameters(parameters[i]);
		}
        return planOption;
    }


	protected static PricePlanOption
	    convertSubscriberAuxiliaryServiceToPricePlanOption(Context ctx,
	        AuxiliaryService auxService,
	        final SubscriberAuxiliaryService subAuxService,
	        final PricePlanOption planOption) throws HomeInternalException,
	        HomeException
	{
		// previously picked service
		planOption.setIsSelected(true);
		planOption.setStartDate(subAuxService.getStartDate());
		planOption.setEndDate(subAuxService.getEndDate());
		planOption.setNumberOfPayments(subAuxService.getPaymentNum());
		if (subAuxService.isProvisioned())
		{
			boolean isSuspended = false;
			try
			{
				isSuspended =
				    SuspendedEntitySupport.isSuspendedEntity(ctx,
				        subAuxService.getSubscriberIdentifier(),
				        subAuxService.getAuxiliaryServiceIdentifier(),
				        subAuxService.getSecondaryIdentifier(),
				        AuxiliaryService.class);
			}
			catch (HomeException e)
			{
				new MinorLogMsg(AuxiliaryServiceToPricePlanOptionAdapter.class,
				    "Error looking up suspended state of auxiliary service "
				        + subAuxService.getIdentifier() + " for subscription "
				        + subAuxService.getSubscriberIdentifier()
				        + ".  Assuming not suspended...", e).log(ctx);
			}

			if (isSuspended)
			{
				planOption
				    .setProvisioningState(ProvisioningStateTypeEnum.SUSPENDED
				        .getValue());
			}
			else
			{
				if(planOption.getStartDate().before(new Date()) || planOption.getStartDate().equals(new Date()))
	            {
	            	planOption.setProvisioningState(ProvisioningStateTypeEnum.PROVISIONED.getValue());
	            }
	            else 
	            {
	            	planOption.setProvisioningState(ProvisioningStateTypeEnum.NOT_PROVISIONED.getValue());
	            }
			}
		}

		if (isCug(ctx, auxService))
		{
            // The identifier is set to the actually CUG instance ID when it's a CUG
            // auxiliary service.
		    ClosedUserGroup cug = ClosedUserGroupSupport.getCUG(ctx,
                    Long.valueOf(subAuxService.getSecondaryIdentifier()), auxService.getSpid());
			planOption.setParameters(getCugOnlyGenericParameters(ctx,
			    auxService, cug));
		}

		return planOption;
	}

	public static boolean isCug(Context ctx, AuxiliaryService auxService)
	{
	    if (AuxiliaryServiceTypeEnum.CallingGroup.equals(auxService.getType()))
	    {
            CallingGroupTypeEnum callingGroupType = CallingGroupAuxSvcExtension.DEFAULT_CALLINGGROUPTYPE;
            CallingGroupAuxSvcExtension callingGroupAuxSvcExtension = ExtensionSupportHelper.get(ctx).getExtension(ctx, auxService, CallingGroupAuxSvcExtension.class);
            if (callingGroupAuxSvcExtension!=null)
            {
                callingGroupType = callingGroupAuxSvcExtension.getCallingGroupType();
            }
            else
            {
                LogSupport.minor(ctx, AuxiliaryServiceToPricePlanOptionAdapter.class,
                        "Unable to find required extension of type '" + CallingGroupAuxSvcExtension.class.getSimpleName()
                                + "' for auxiliary service " + auxService.getIdentifier());
            }
    
            return SafetyUtil.safeEquals(callingGroupType,
    		    CallingGroupTypeEnum.PCUG)
    		    || SafetyUtil.safeEquals(callingGroupType,
    		        CallingGroupTypeEnum.CUG);
	    }
	    else
	    {
	        return false;
	    }
	}

	private static GenericParameter[] getCugOnlyGenericParameters(Context ctx,
	    AuxiliaryService auxService, ClosedUserGroup cug)
	{
		Collection<GenericParameter> parametersList =
		    new ArrayList<GenericParameter>();
        CallingGroupTypeEnum callingGroupType = CallingGroupAuxSvcExtension.DEFAULT_CALLINGGROUPTYPE;
        CallingGroupAuxSvcExtension callingGroupAuxSvcExtension = ExtensionSupportHelper.get(ctx).getExtension(ctx, auxService, CallingGroupAuxSvcExtension.class);
        if (callingGroupAuxSvcExtension!=null)
        {
            callingGroupType = callingGroupAuxSvcExtension.getCallingGroupType();
        }
        else
        {
            LogSupport.minor(ctx, AuxiliaryServiceToPricePlanOptionAdapter.class,
                    "Unable to find required extension of type '" + CallingGroupAuxSvcExtension.class.getSimpleName()
                            + "' for auxiliary service " + auxService.getIdentifier());
        }

		if (SafetyUtil.safeEquals(callingGroupType,
		    CallingGroupTypeEnum.PCUG) && cug != null)
		{
			parametersList.add(APIGenericParameterSupport
			    .getPrivateCUGOwnerParameter(ctx, cug));
			parametersList.add(APIGenericParameterSupport
			    .getCUGShortCodesEnabled(ctx, cug));
			parametersList.add(APIGenericParameterSupport
			    .getCugCallingGroupIdParameter(ctx, cug));
		}
		else if (SafetyUtil.safeEquals(callingGroupType,
		    CallingGroupTypeEnum.CUG) && cug != null)
		{
			parametersList.add(APIGenericParameterSupport
			    .getCUGShortCodesEnabled(ctx, cug));
			parametersList.add(APIGenericParameterSupport
			    .getCugCallingGroupIdParameter(ctx, cug));
		}
		return parametersList.toArray(new GenericParameter[] {});
	}

	private static GenericParameter[] getAllGenericParameter(Context ctx,
	    AuxiliaryService auxService, SubscriberAuxiliaryService subAuxService) throws HomeException
    {
        Collection<GenericParameter> parametersList = new ArrayList<GenericParameter>();
        try
        {
            parametersList.add(APIGenericParameterSupport.getPricePlanPreferenceTypeParameter(ctx, auxService));
            parametersList.add(APIGenericParameterSupport.getAuxiliaryServiceTypeParameter(ctx, auxService));
            if (subAuxService != null && subAuxService.getNextRecurringChargeDate() != null)
            {
                parametersList.add(APIGenericParameterSupport.getNextRecurringChargeDateParam(
                        subAuxService.getNextRecurringChargeDate()));
            }
            
            if (auxService.getType().equals(AuxiliaryServiceTypeEnum.CallingGroup))
            {
                parametersList.add(APIGenericParameterSupport.getCallingGroupTypeParameter(ctx, auxService));
                if (!isCug(ctx, auxService))
                {
                    parametersList.add(APIGenericParameterSupport.getNonCugCallingGroupIdParameter(ctx, auxService));
                }
            }
            else if(auxService.getType().equals(AuxiliaryServiceTypeEnum.AdditionalMsisdn))
            {
                parametersList.add(APIGenericParameterSupport.getAdditionalMobileNumberParameter(ctx, auxService));
            }
            else if((auxService.isHLRProvisionable() || auxService.getType().equals(AuxiliaryServiceTypeEnum.Download_Servers))
                        && !auxService.getType().equals(AuxiliaryServiceTypeEnum.MultiSIM))
            {
                parametersList.add(APIGenericParameterSupport.getProvisionOnSuspendOrDisableParameter(ctx, auxService));
            }
            parametersList.add(APIGenericParameterSupport.getAuxiliaryServiceIsPersonalizedFeeParameter(auxService.getFeePersonalizationAllowed()));
            if(subAuxService!=null)
            {
            	parametersList.add(APIGenericParameterSupport.getAuxiliaryServicePersonalizedFeeParameter(subAuxService.getPersonalizedFee()));
            	parametersList.add(APIGenericParameterSupport.getIsAuxiliaryServicePersonalizedFeeApplied(subAuxService.getIsfeePersonalizationApplied()));
            }
        }
        catch (Exception ex )
        {
            new MinorLogMsg(AuxiliaryServiceToPricePlanOptionAdapter.class, "Unalbe to add generic parameters", ex).log(ctx);
        }
        
        return parametersList.toArray(new GenericParameter[]{});
    }
}
