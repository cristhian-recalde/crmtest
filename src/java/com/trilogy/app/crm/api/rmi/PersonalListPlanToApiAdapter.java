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

import java.util.Date;

import com.trilogy.framework.xhome.beans.XBeans;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Adapter;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.MinorLogMsg;

import com.trilogy.app.crm.api.Constants;
import com.trilogy.app.crm.api.rmi.support.RmiApiErrorHandlingSupport;
import com.trilogy.app.crm.api.rmi.support.RmiApiSupport;
import com.trilogy.app.crm.bean.ActivationFeeModeEnum;
import com.trilogy.app.crm.bean.CallingGroupServiceTypeEnum;
import com.trilogy.app.crm.bean.DiscountTypeEnum;
import com.trilogy.app.crm.bean.PersonalListPlan;
import com.trilogy.util.crmapi.wsdl.v2_0.types.GenericParameter;
import com.trilogy.util.crmapi.wsdl.v3_0.types.callinggroups.CallingGroupDiscountTypeEnum;


/**
 * Adapts PersonalListPlan object to API objects.
 * 
 * @author bhupendra.pandey@redknee.com
 */
public class PersonalListPlanToApiAdapter implements Adapter
{

    public Object adapt(final Context ctx, final Object obj) throws HomeException
    {
        return adaptPersonalListPlanToApi(ctx, (PersonalListPlan) obj);
    }


    public Object unAdapt(final Context ctx, final Object obj) throws HomeException
    {
        throw new UnsupportedOperationException();
    }


    public static com.redknee.util.crmapi.wsdl.v3_0.types.callinggroup.PersonalListPlan adaptPersonalListPlanToApi(
            final Context ctx, final PersonalListPlan crmPlp) throws HomeException
    {
        final com.redknee.util.crmapi.wsdl.v3_0.types.callinggroup.PersonalListPlan apiPlp = new com.redknee.util.crmapi.wsdl.v3_0.types.callinggroup.PersonalListPlan();
        PersonalListPlanToApiReferenceAdapter.adaptPersonalListPlanToApiReference(ctx, crmPlp, apiPlp);
        apiPlp.setMaxSubscriberCount(crmPlp.getMaxSubscriberCount());
        apiPlp.setPlpServiceType(com.redknee.util.crmapi.wsdl.v3_0.types.callinggroups.CallingGroupServiceTypeEnum
                .valueOf(crmPlp.getPlpServiceType().getIndex()));
        if (crmPlp.getPlpServiceType().getIndex() == CallingGroupServiceTypeEnum.ALL_INDEX
                || crmPlp.getPlpServiceType().getIndex() == CallingGroupServiceTypeEnum.SMS_INDEX)
        {
            apiPlp.setSmsDiscountType(CallingGroupDiscountTypeEnum.valueOf(crmPlp.getSmsDiscountType().getIndex()));
            apiPlp.setSmsIncomingValue(crmPlp.getSmsIncomingValue());
            apiPlp.setSmsOutgoingValue(crmPlp.getSmsOutgoingValue());
        }
        if (crmPlp.getPlpServiceType().getIndex() == CallingGroupServiceTypeEnum.ALL_INDEX
                || crmPlp.getPlpServiceType().getIndex() == CallingGroupServiceTypeEnum.VOICE_INDEX)
        {
            apiPlp.setVoiceDiscountType(CallingGroupDiscountTypeEnum.valueOf(crmPlp.getVoiceDiscountType().getIndex()));
            apiPlp.setVoiceIncomingValue(crmPlp.getVoiceIncomingValue());
            apiPlp.setVoiceOutgoingValue(crmPlp.getVoiceOutgoingValue());
        }
        return apiPlp;
    }


    public static PersonalListPlan adaptApiToPersonalListPlan(final Context ctx,
            final com.redknee.util.crmapi.wsdl.v3_0.types.callinggroup.PersonalListPlan apiPlp) throws Exception
    {
        PersonalListPlan crmPlp = null;
        try
        {
            crmPlp = (PersonalListPlan) XBeans.instantiate(PersonalListPlan.class, ctx);
        }
        catch (Exception e)
        {
            new MinorLogMsg(PersonalListPlanToApiAdapter.class,
                    "Error instantiating new PersonalListPlan.  Using default constructor.", e).log(ctx);
            crmPlp = new PersonalListPlan();
        }
        adaptApiToPersonalListPlan(ctx, apiPlp, crmPlp);
        return crmPlp;
    }


    public static PersonalListPlan adaptApiToPersonalListPlan(final Context ctx,
            final com.redknee.util.crmapi.wsdl.v3_0.types.callinggroup.PersonalListPlan apiPlp, PersonalListPlan crmPlp)
            throws Exception
    {
        if (apiPlp.getMaxSubscriberCount() != null)
        {
            crmPlp.setMaxSubscriberCount(apiPlp.getMaxSubscriberCount());
        }
        if (apiPlp.getName() != null)
        {
            crmPlp.setName(apiPlp.getName());
        }
        if (apiPlp.getPlpServiceType() != null)
        {
            crmPlp.setPlpServiceType(CallingGroupServiceTypeEnum.get((short) apiPlp.getPlpServiceType().getValue()));
        }
        if (crmPlp.getPlpServiceType().getIndex() == CallingGroupServiceTypeEnum.ALL_INDEX
                || crmPlp.getPlpServiceType().getIndex() == CallingGroupServiceTypeEnum.SMS_INDEX)
        {
            RmiApiErrorHandlingSupport.validateMandatoryObject(apiPlp.getSmsDiscountType(), "plp.smsDiscountType");
            crmPlp.setSmsDiscountType(DiscountTypeEnum.get((short) apiPlp.getSmsDiscountType().getValue()));
            if (apiPlp.getSmsIncomingValue() != null)
            {
                crmPlp.setSmsIncomingValue(apiPlp.getSmsIncomingValue());
            }
            if (apiPlp.getSmsOutgoingValue() != null)
            {
                crmPlp.setSmsOutgoingValue(apiPlp.getSmsOutgoingValue());
            }
        }
        if (apiPlp.getSpid() != null)
        {
            crmPlp.setSpid(apiPlp.getSpid());
        }        
        if (crmPlp.getPlpServiceType().getIndex() == CallingGroupServiceTypeEnum.ALL_INDEX
                || crmPlp.getPlpServiceType().getIndex() == CallingGroupServiceTypeEnum.VOICE_INDEX)
        {
            RmiApiErrorHandlingSupport.validateMandatoryObject(apiPlp.getVoiceDiscountType(), "plp.voiceDiscountType");
            crmPlp.setVoiceDiscountType(DiscountTypeEnum.get((short) apiPlp.getVoiceDiscountType().getValue()));
            if (apiPlp.getVoiceIncomingValue() != null)
            {
                crmPlp.setVoiceIncomingValue(apiPlp.getVoiceIncomingValue());
            }
            if (apiPlp.getVoiceOutgoingValue() != null)
            {
                crmPlp.setVoiceOutgoingValue(apiPlp.getVoiceOutgoingValue());
            }
        }        
        return crmPlp;
    }


    public static PersonalListPlan adaptGenericParametersToCreatePersonalListPlan(final Context ctx,
            final GenericParameter[] apiGenericParameter, PersonalListPlan crmPlp) throws Exception
    {
        Object obj = RmiApiSupport.getGenericParameterValue(Constants.GENERICPARAMETER_ACTIVATIONFEETYPE,
                apiGenericParameter);
        RmiApiErrorHandlingSupport.validateMandatoryObject(obj, "plp.ActivationFeeType");
        if (obj != null)
        {
            crmPlp.setActivationFee(ActivationFeeModeEnum.get(((Long) obj).shortValue()));
        }           
        obj = RmiApiSupport.getGenericParameterValue(Constants.GENERICPARAMETER_GLCODE, apiGenericParameter);
        if (obj != null)
        {
            crmPlp.setAdjustmentGLCode((String) obj);
        }
        obj = RmiApiSupport.getGenericParameterValue(Constants.GENERICPARAMETER_ENDDATE, apiGenericParameter);
        if (obj != null)
        {
            crmPlp.setEndDate((Date) obj);
        }
        obj = RmiApiSupport.getGenericParameterValue(Constants.GENERICPARAMETER_SERVICECHARGE, apiGenericParameter);
        if (obj != null)
        {
            crmPlp.setMonthlyCharge((Long) obj);
        }
        obj = RmiApiSupport.getGenericParameterValue(Constants.GENERICPARAMETER_SMARTSUSPENSIONENABLED,
                apiGenericParameter);
        if (obj != null)
        {
            crmPlp.setSmartSuspension((Boolean) obj);
        }
        obj = RmiApiSupport.getGenericParameterValue(Constants.GENERICPARAMETER_STARTDATE, apiGenericParameter);
        if (obj != null)
        {
            crmPlp.setStartDate((Date) obj);
        }
        obj = RmiApiSupport.getGenericParameterValue(Constants.GENERICPARAMETER_TAXAUTHORITY, apiGenericParameter);
        if (obj != null)
        {
            crmPlp.setTaxAuthority(((Long) obj).intValue());
        }
        return crmPlp;
    }
}
