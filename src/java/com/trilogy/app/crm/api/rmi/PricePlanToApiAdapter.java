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
import java.util.List;

import com.trilogy.framework.xhome.beans.XBeans;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Adapter;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.webcontrol.OptionalLongWebControl;
import com.trilogy.framework.xlog.log.MinorLogMsg;

import com.trilogy.app.crm.ModelCrmLicenseConstants;
import com.trilogy.app.crm.api.rmi.support.APIGenericParameterSupport;
import com.trilogy.app.crm.api.rmi.support.RmiApiSupport;
import com.trilogy.app.crm.bean.PricePlan;
import com.trilogy.app.crm.bean.PricePlanVersion;
import com.trilogy.app.crm.support.CollectionSupportHelper;
import com.trilogy.app.crm.technology.TechnologyEnum;
import com.trilogy.util.crmapi.wsdl.v2_0.types.BillingMessageReference;
import com.trilogy.util.crmapi.wsdl.v2_0.types.GenericParameter;
import com.trilogy.util.crmapi.wsdl.v2_2.types.generalprovisioning.ContractFeeFrequencyEnum;
import com.trilogy.util.crmapi.wsdl.v2_3.types.serviceandbundle.ContractDurationPricePlanCriteria;
import com.trilogy.util.crmapi.wsdl.v2_3.types.serviceandbundle.PricePlanCriteria;
import com.trilogy.util.crmapi.wsdl.v2_3.types.serviceandbundle.PricePlanCriteriaChoice_type0;
import com.trilogy.util.crmapi.wsdl.v2_3.types.serviceandbundle.PricePlanCriteriaSequence_type0;
import com.trilogy.util.crmapi.wsdl.v3_0.api.CRMExceptionFault;
import com.trilogy.util.crmapi.wsdl.v3_0.types.serviceandbundle.PricePlanFunctionTypeEnum;


/**
 * Adapts PricePlan object to API objects.
 * 
 * @author victor.stratan@redknee.com
 */
public class PricePlanToApiAdapter implements Adapter
{

    public Object adapt(final Context ctx, final Object obj) throws HomeException
    {
        // This method does not retrieve any priceplanversion
        return adaptPricePlanToApi(ctx, (PricePlan) obj, null);
    }


    public Object unAdapt(final Context ctx, final Object obj) throws HomeException
    {
        throw new UnsupportedOperationException();
    }


    public static PricePlan adaptApiToPricePlan(Context ctx,
            com.redknee.util.crmapi.wsdl.v3_0.types.serviceandbundle.PricePlan apiPricePlan)
    {
        PricePlan crmPricePlan = null;
        try
        {
            crmPricePlan = (PricePlan) XBeans.instantiate(PricePlan.class, ctx);
        }
        catch (Exception e)
        {
            new MinorLogMsg(BundleCategoryToApiAdapter.class,
                    "Error instantiating new PricePlan.  Using default constructor.", e).log(ctx);
            crmPricePlan = new PricePlan();
        }
        adaptApiToPricePlan(ctx, apiPricePlan, crmPricePlan);
        return crmPricePlan;
    }


    public static PricePlan adaptApiToPricePlan(Context ctx,
            com.redknee.util.crmapi.wsdl.v3_0.types.serviceandbundle.PricePlan apiPricePlan, PricePlan crmPricePlan)
    {
    	//SET THE CURRENT VERSION OF PRICE PLAN
     	if(apiPricePlan.getCurrentVersion() != null){	  
    		crmPricePlan.setCurrentVersion(Integer.parseInt(apiPricePlan.getCurrentVersion().toString()));
    		   //IF IN API GIVES THE  CURRENT VERSION MORE THAN DEFAULT LIKE 2 OR 3 ETC.
    		   // WE NEED TO SET NEXT VERSION AS CURRENT VERSION SO THAT IN NEXT CODE IT WILL TAKE CURRENT VERSION AND INCREASE IT
    		   // LIKE  NEXT VERSION = CURRENT VERSION + 1
     			if(apiPricePlan.getCurrentVersion() > 0)
    			{
    				apiPricePlan.setNextVersion(apiPricePlan.getCurrentVersion());
    			}
     			
    	}
     	
        PricePlanCriteria[] criteriaArray = apiPricePlan.getCriteria();
        if (criteriaArray != null)
        {
            PricePlanCriteria criteria = criteriaArray[0];
            if (criteria != null)
            {
                crmPricePlan.setApplyContractDurationCriteria(true);
                PricePlanCriteriaChoice_type0 choice_type0 = criteria.getPricePlanCriteriaChoice_type0();
                PricePlanCriteriaSequence_type0 seq_type0 = choice_type0.getPricePlanCriteriaSequence_type0();
                ContractDurationPricePlanCriteria duration = seq_type0.getContractDuration();
                if (duration.getDurationFrequency() != null)
                {
                    crmPricePlan.setContractDurationUnits(com.redknee.app.crm.bean.payment.ContractFeeFrequencyEnum
                            .get((short) duration.getDurationFrequency().getValue()));
                }
                if (duration.getMaximumDuration() != null)
                {
                    crmPricePlan.setMaxContractDuration(duration.getMaximumDuration());
                }
                if (duration.getMinimumDuration() != null)
                {
                    crmPricePlan.setMinContractDuration(duration.getMinimumDuration());
                }
            }
        }
        BillingMessageReference apiBillingMsgRefs[] = apiPricePlan.getBillingMessage();
        if (apiBillingMsgRefs != null)
        {
            List billingMsgList = new ArrayList();
            for (BillingMessageReference apiBillingMsgRef : apiBillingMsgRefs)
            {
                billingMsgList.add(BillingMessageToApiAdapter.adaptReferenceToBillingMessage(apiBillingMsgRef));
            }
            crmPricePlan.setBillingMessages(billingMsgList);
        }
        if (apiPricePlan.getDataRatePlan() != null && apiPricePlan.getDataRatePlan().length != 0)
        {
        	crmPricePlan.setDataRatePlan(apiPricePlan.getDataRatePlan()[0]);
        }
        if (apiPricePlan.getEnabled() != null)
        {
            crmPricePlan.setEnabled(apiPricePlan.getEnabled());
        }
        // Id is not required in update call
        final com.redknee.framework.license.LicenseMgr lMgr = (com.redknee.framework.license.LicenseMgr) ctx
                .get(com.redknee.framework.license.LicenseMgr.class);
        if ((lMgr != null && lMgr.isLicensed(ctx, ModelCrmLicenseConstants.PRICEPLAN_CUSTOM_ID_LICENSE_KEY)))
        {
            if (apiPricePlan.getIdentifier() != null)
            {
                crmPricePlan.setIdentifier(apiPricePlan.getIdentifier());
            }
        }
        if (apiPricePlan.getName() != null)
        {
            crmPricePlan.setName(apiPricePlan.getName());
        }
        if (apiPricePlan.getGroup() != null && apiPricePlan.getGroup().length != 0)
        {
            crmPricePlan.setPricePlanGroup(apiPricePlan.getGroup()[0]);
        }
        try
        {
            crmPricePlan
                    .setPricePlanType(RmiApiSupport.convertApiPaidType2CrmSubscriberType(apiPricePlan.getPaidtype()));
        }
        catch (CRMExceptionFault e)
        {
        }
        if (apiPricePlan.getSmsRatePlan() != null && apiPricePlan.getSmsRatePlan().length != 0)
        {
            crmPricePlan.setSMSRatePlan(apiPricePlan.getSmsRatePlan()[0]);
        }
        if (apiPricePlan.getSpid() != null)
        {
            crmPricePlan.setSpid(apiPricePlan.getSpid());
        }
        if (apiPricePlan.getSubscriptionLevel() != null)
        {
            crmPricePlan.setSubscriptionLevel(apiPricePlan.getSubscriptionLevel());
        }
        if (apiPricePlan.getSubscriptionType() != null)
        {
            crmPricePlan.setSubscriptionType(apiPricePlan.getSubscriptionType());
        }
        if (apiPricePlan.getTechnology() != null)
        {
            crmPricePlan.setTechnology(TechnologyEnum.get((short) apiPricePlan.getTechnology().getValue()));
        }
        if (apiPricePlan.getVoiceRatePlan() != null && apiPricePlan.getVoiceRatePlan().length != 0)
        {
            crmPricePlan.setVoiceRatePlan(apiPricePlan.getVoiceRatePlan()[0]);
        }
        if(apiPricePlan.getNextVersion() != null)
        {
        	crmPricePlan.setNextVersion(Integer.parseInt(apiPricePlan.getNextVersion().toString()));
        }
        return crmPricePlan;
    }


    public static com.redknee.util.crmapi.wsdl.v3_0.types.serviceandbundle.PricePlan adaptPricePlanToApi(Context ctx,
            final PricePlan crmPricePlan, Collection<PricePlanVersion> crmPricePlanVersions) throws HomeException
    {
        com.redknee.util.crmapi.wsdl.v3_0.types.serviceandbundle.PricePlan apiPricePlan = new com.redknee.util.crmapi.wsdl.v3_0.types.serviceandbundle.PricePlan();
        PricePlanToApiReferenceAdapter.adaptPricePlanToReference(ctx, crmPricePlan, apiPricePlan);
        apiPricePlan.setFunction(PricePlanFunctionTypeEnum.valueOf(crmPricePlan.getPricePlanFunction().getIndex()));
        apiPricePlan.setNextVersion(Long.valueOf(crmPricePlan.getNextVersion()));
        BillingMessageReference[] billingMessages = new BillingMessageReference[]
            {};
        billingMessages = CollectionSupportHelper.get(ctx).adaptCollection(ctx, crmPricePlan.getBillingMessages(),
                new BillingMessageToApiAdapter(), billingMessages);
        apiPricePlan.setBillingMessage(billingMessages);
        apiPricePlan.setSmsRatePlan(new String[]
            {crmPricePlan.getSMSRatePlan()});
        apiPricePlan.setDataRatePlan(new String[]
            {String.valueOf(crmPricePlan.getDataRatePlan())});
        apiPricePlan.setVoiceRatePlan(new String[]
            {crmPricePlan.getVoiceRatePlan()});
        apiPricePlan.setParameters(getGenericParamsForPricePlan(crmPricePlan));
        
        List<PricePlanCriteria> criteria = new ArrayList<PricePlanCriteria>();
        if (crmPricePlan.isApplyContractDurationCriteria())
        {
            ContractDurationPricePlanCriteria duration = new ContractDurationPricePlanCriteria();
            if (crmPricePlan.getMinContractDuration() != OptionalLongWebControl.DEFAULT_VALUE)
            {
                duration.setMinimumDuration(crmPricePlan.getMinContractDuration());
            }
            if (crmPricePlan.getMaxContractDuration() != OptionalLongWebControl.DEFAULT_VALUE)
            {
                duration.setMaximumDuration(crmPricePlan.getMaxContractDuration());
            }
            duration.setDurationFrequency(ContractFeeFrequencyEnum.valueOf(crmPricePlan.getContractDurationUnits()
                    .getIndex()));
            PricePlanCriteriaSequence_type0 contractCriteria = new PricePlanCriteriaSequence_type0();
            contractCriteria.setContractDuration(duration);
            PricePlanCriteriaChoice_type0 contractChoice = new PricePlanCriteriaChoice_type0();
            contractChoice.setPricePlanCriteriaSequence_type0(contractCriteria);
            PricePlanCriteria contractHolder = new PricePlanCriteria();
            contractHolder.setPricePlanCriteriaChoice_type0(contractChoice);
            criteria.add(contractHolder);
        }
        apiPricePlan.setCriteria(criteria.toArray(new PricePlanCriteria[]
            {}));
        apiPricePlan.setGroup(new Long[]
            {crmPricePlan.getPricePlanGroup()});
        if (crmPricePlanVersions != null)
        {
            com.redknee.util.crmapi.wsdl.v3_0.types.serviceandbundle.PricePlanVersion[] apiPricePlanVersions = new com.redknee.util.crmapi.wsdl.v3_0.types.serviceandbundle.PricePlanVersion[]
                {};
            apiPricePlanVersions = CollectionSupportHelper.get(ctx).adaptCollection(ctx, crmPricePlanVersions,
                    new PricePlanVersionToApiAdapter(), apiPricePlanVersions);
            apiPricePlan.setVersions(apiPricePlanVersions);
        }
        return apiPricePlan;
    }
    
    private static GenericParameter[] getGenericParamsForPricePlan(final PricePlan crmPricePlan)
    {
    	GenericParameter[] genericParamsArray = new GenericParameter[3];
    	
    	genericParamsArray[0] = RmiApiSupport.createGenericParameter(APIGenericParameterSupport.PRICE_PLAN_STATE, 
    			RmiApiSupport.convertCrmPricePlanState2Api(crmPricePlan.getState()));
    	genericParamsArray[1] = RmiApiSupport.createGenericParameter(APIGenericParameterSupport.GRANDFATHER_PRICE_PLAN_ID, crmPricePlan.getGrandfatherPPId());
    	if(crmPricePlan.getPricePlanSubType() != null)
    	{
    		genericParamsArray[2] = RmiApiSupport.createGenericParameter(APIGenericParameterSupport.PRICE_PLAN_SUBTYPE, crmPricePlan.getPricePlanSubType().getIndex());
    	}
    	
    	return genericParamsArray;
    }
}
