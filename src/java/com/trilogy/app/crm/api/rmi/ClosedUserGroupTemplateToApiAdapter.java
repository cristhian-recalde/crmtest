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

import com.trilogy.framework.xhome.beans.XBeans;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Adapter;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.MinorLogMsg;

import com.trilogy.app.crm.api.rmi.support.RmiApiErrorHandlingSupport;
import com.trilogy.app.crm.bean.ActivationFeeModeEnum;
import com.trilogy.app.crm.bean.CUGTypeEnum;
import com.trilogy.app.crm.bean.CallingGroupServiceTypeEnum;
import com.trilogy.app.crm.bean.ClosedUserGroupTemplate;
import com.trilogy.app.crm.bean.DiscountTypeEnum;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.util.crmapi.wsdl.v3_0.api.CRMExceptionFault;
import com.trilogy.util.crmapi.wsdl.v3_0.types.callinggroups.CallingGroupDiscountTypeEnum;
import com.trilogy.util.crmapi.wsdl.v3_0.types.callinggroups.ClosedUserGroupTypeEnum;
import com.trilogy.util.crmapi.wsdl.v3_0.types.serviceandbundle.ActivationFeeTypeEnum;


/**
 * Adapts ClosedUserGroupTemplate object to API objects.
 * 
 * @author bhupendra.pandey@redknee.com
 */
public class ClosedUserGroupTemplateToApiAdapter implements Adapter
{

    public Object adapt(final Context ctx, final Object obj) throws HomeException
    {
        return adaptClosedUserGroupTemplateToApi(ctx, (ClosedUserGroupTemplate) obj);
    }


    public Object unAdapt(final Context ctx, final Object obj) throws HomeException
    {
        throw new UnsupportedOperationException();
    }


    public static com.redknee.util.crmapi.wsdl.v3_0.types.callinggroup.ClosedUserGroupTemplate adaptClosedUserGroupTemplateToApi(
            final Context ctx, final ClosedUserGroupTemplate crmCugTemplate) throws HomeException
    {        
        final com.redknee.util.crmapi.wsdl.v3_0.types.callinggroup.ClosedUserGroupTemplate apiCugTemplate = new com.redknee.util.crmapi.wsdl.v3_0.types.callinggroup.ClosedUserGroupTemplate();
        ClosedUserGroupTemplateToApiReferenceAdapter.adaptClosedUserGroupTemplateToReference(ctx, crmCugTemplate, apiCugTemplate);
        apiCugTemplate.setActivationFeeType(ActivationFeeTypeEnum.valueOf(crmCugTemplate.getActivationFee().getIndex()));        
        apiCugTemplate.setCugServiceType(com.redknee.util.crmapi.wsdl.v3_0.types.callinggroups.CallingGroupServiceTypeEnum.valueOf(crmCugTemplate.getCugServiceType().getIndex()));
        apiCugTemplate.setDeprecated(crmCugTemplate.getDeprecated());
        apiCugTemplate.setEndDate(CalendarSupportHelper.get(ctx).dateToCalendar(crmCugTemplate.getEndDate()));
        apiCugTemplate.setGlCode(crmCugTemplate.getGlCode());        
        apiCugTemplate.setPriority(crmCugTemplate.getPriority());
        apiCugTemplate.setServiceCharge(crmCugTemplate.getServiceCharge());
        apiCugTemplate.setCugType(ClosedUserGroupTypeEnum.valueOf(crmCugTemplate.getCugType().getIndex()));
        if (crmCugTemplate.getCugType().getIndex() == CUGTypeEnum.PrivateCUG_INDEX)
        {
            apiCugTemplate.setServiceChargeExternal(crmCugTemplate.getServiceChargeExternal());
            apiCugTemplate.setServiceChargePostpaid(crmCugTemplate.getServiceChargePostpaid());
            apiCugTemplate.setServiceChargePrepaid(crmCugTemplate.getServiceChargePrePaid());
        }
        apiCugTemplate.setShortCodeEnabled(crmCugTemplate.getShortCodeEnable());
        if (crmCugTemplate.getShortCodeEnable())
        {
            apiCugTemplate.setShortCodePattern(crmCugTemplate.getShortCodePattern());
        }
        apiCugTemplate.setSmartSuspensionEnabled(crmCugTemplate.getSmartSuspension());
        if (crmCugTemplate.getCugServiceType().getIndex() == CallingGroupServiceTypeEnum.ALL_INDEX
                || crmCugTemplate.getCugServiceType().getIndex() == CallingGroupServiceTypeEnum.SMS_INDEX)
        {
            apiCugTemplate.setSmsDiscountType(CallingGroupDiscountTypeEnum.valueOf(crmCugTemplate.getSmsDiscountType()
                    .getIndex()));
            apiCugTemplate.setSmsIncomingValue(crmCugTemplate.getSmsIncomingValue());
            apiCugTemplate.setSmsOutgoingValue(crmCugTemplate.getSmsOutgoingValue());
        }
        apiCugTemplate.setStartDate(CalendarSupportHelper.get(ctx).dateToCalendar(crmCugTemplate.getStartDate()));
        apiCugTemplate.setTaxAuthority(crmCugTemplate.getTaxAuthority());
        if (crmCugTemplate.getCugServiceType().getIndex() == CallingGroupServiceTypeEnum.ALL_INDEX
                || crmCugTemplate.getCugServiceType().getIndex() == CallingGroupServiceTypeEnum.VOICE_INDEX)
        {
            apiCugTemplate.setVoiceDiscountType(CallingGroupDiscountTypeEnum.valueOf(crmCugTemplate
                    .getVoiceDiscountType().getIndex()));
            apiCugTemplate.setVoiceIncomingValue(crmCugTemplate.getVoiceIncomingValue());
            apiCugTemplate.setVoiceOutgoingValue(crmCugTemplate.getVoiceOutgoingValue());
        }                
        return apiCugTemplate;
    }
    
    public static ClosedUserGroupTemplate adaptApiToClosedUserGroupTemplate(
            final Context ctx, final com.redknee.util.crmapi.wsdl.v3_0.types.callinggroup.ClosedUserGroupTemplate apiCugTemplate) throws Exception
    {        
        ClosedUserGroupTemplate crmCugTemplate = null;
        try
        {
            crmCugTemplate = (ClosedUserGroupTemplate) XBeans.instantiate(ClosedUserGroupTemplate.class, ctx);
        }
        catch (Exception e)
        {
            new MinorLogMsg(ClosedUserGroupTemplateToApiAdapter.class,
                    "Error instantiating new ClosedUserGroupTemplate.  Using default constructor.", e).log(ctx);
            crmCugTemplate = new ClosedUserGroupTemplate();
        }
        adaptApiToClosedUserGroupTemplate(ctx, apiCugTemplate, crmCugTemplate);
        return crmCugTemplate;
    }
    

    public static ClosedUserGroupTemplate adaptApiToClosedUserGroupTemplate(final Context ctx,
            final com.redknee.util.crmapi.wsdl.v3_0.types.callinggroup.ClosedUserGroupTemplate apiCugTemplate,
            ClosedUserGroupTemplate crmCugTemplate) throws Exception
    {
        if(apiCugTemplate.getSpid() != null)
        {
            crmCugTemplate.setSpid(apiCugTemplate.getSpid());            
        }
        if (apiCugTemplate.getCugType() != null)
        {
            // Not required in Update
            crmCugTemplate.setCugType(CUGTypeEnum.get((short) apiCugTemplate.getCugType().getValue()));
        }
        if (apiCugTemplate.getName() != null)
        {
            crmCugTemplate.setName(apiCugTemplate.getName());
        }
        if (apiCugTemplate.getActivationFeeType() != null)
        {
            // Activation Fee Type is not required in Update
            crmCugTemplate.setActivationFee(ActivationFeeModeEnum.get((short) apiCugTemplate.getActivationFeeType()
                    .getValue()));
        }
        if (apiCugTemplate.getCugServiceType() != null)
        {
            crmCugTemplate.setCugServiceType(CallingGroupServiceTypeEnum.get((short) apiCugTemplate.getCugServiceType()
                    .getValue()));
        }
        if (apiCugTemplate.getDeprecated() != null)
        {            
            crmCugTemplate.setDeprecated(apiCugTemplate.getDeprecated());
        }
        if (apiCugTemplate.getEndDate() != null)
        {
            crmCugTemplate.setEndDate(apiCugTemplate.getEndDate().getTime());
        }
        if (apiCugTemplate.getGlCode() != null)
        {
            // Not required in update
            crmCugTemplate.setGlCode(apiCugTemplate.getGlCode());
        }
        if (apiCugTemplate.getPriority() != null)
        {
            crmCugTemplate.setPriority(apiCugTemplate.getPriority());
        }
        if(apiCugTemplate.getServiceCharge() != null)
        {
            // Not required in Update
            crmCugTemplate.setServiceCharge(apiCugTemplate.getServiceCharge());            
        }        
        if (crmCugTemplate.getCugType().getIndex() == CUGTypeEnum.PrivateCUG_INDEX)
        {
            // Required only for Private CUG template
            if (apiCugTemplate.getServiceChargeExternal() != null)
            {
                // Not required in update
                crmCugTemplate.setServiceChargeExternal(apiCugTemplate.getServiceChargeExternal());
            }
            if (apiCugTemplate.getServiceChargePostpaid() != null)
            {
                // Not required in update
                crmCugTemplate.setServiceChargePostpaid(apiCugTemplate.getServiceChargePostpaid());
            }
            if (apiCugTemplate.getServiceChargePrepaid() != null)
            {
                // Not required in update
                crmCugTemplate.setServiceChargePrePaid(apiCugTemplate.getServiceChargePrepaid());
            }
        }
        if (apiCugTemplate.getShortCodeEnabled() != null)
        {
            crmCugTemplate.setShortCodeEnable(apiCugTemplate.getShortCodeEnabled());
        }
        if (crmCugTemplate.getShortCodeEnable() && apiCugTemplate.getShortCodePattern() != null)
        {
            // Required only if shortcode is enabled
            crmCugTemplate.setShortCodePattern(apiCugTemplate.getShortCodePattern());
        }
        if (apiCugTemplate.getSmartSuspensionEnabled() != null)
        {
            crmCugTemplate.setSmartSuspension(apiCugTemplate.getSmartSuspensionEnabled());
        }
        if (crmCugTemplate.getCugServiceType().getIndex() == CallingGroupServiceTypeEnum.ALL_INDEX
                || crmCugTemplate.getCugServiceType().getIndex() == CallingGroupServiceTypeEnum.SMS_INDEX)
        {
            // Required only if service type is sms and All
            RmiApiErrorHandlingSupport.validateMandatoryObject(apiCugTemplate.getSmsDiscountType(),
                    "cugTemplate.smsDiscountType");
            crmCugTemplate.setSmsDiscountType(DiscountTypeEnum.get((short) apiCugTemplate.getSmsDiscountType().getValue()));
            if (apiCugTemplate.getSmsIncomingValue() != null)
            {
                crmCugTemplate.setSmsIncomingValue(apiCugTemplate.getSmsIncomingValue());
            }
            if (apiCugTemplate.getSmsOutgoingValue() != null)
            {
                crmCugTemplate.setSmsOutgoingValue(apiCugTemplate.getSmsOutgoingValue());
            }
        }
        if (apiCugTemplate.getStartDate() != null)
        {
            crmCugTemplate.setStartDate(apiCugTemplate.getStartDate().getTime());
        }
        if (apiCugTemplate.getTaxAuthority() != null)
        {
            crmCugTemplate.setTaxAuthority(apiCugTemplate.getTaxAuthority());
        }
        if (crmCugTemplate.getCugServiceType().getIndex() == CallingGroupServiceTypeEnum.ALL_INDEX
                || crmCugTemplate.getCugServiceType().getIndex() == CallingGroupServiceTypeEnum.VOICE_INDEX)
        {
            // Required only if service type is Voice and All
            RmiApiErrorHandlingSupport.validateMandatoryObject(apiCugTemplate.getVoiceDiscountType(),
            "cugTemplate.voiceDiscountType");   
            crmCugTemplate.setVoiceDiscountType(DiscountTypeEnum.get((short) apiCugTemplate.getVoiceDiscountType()
                    .getValue()));
            if (apiCugTemplate.getVoiceIncomingValue() != null)
            {
                crmCugTemplate.setVoiceIncomingValue(apiCugTemplate.getVoiceIncomingValue());
            }
            if (apiCugTemplate.getVoiceOutgoingValue() != null)
            {
                crmCugTemplate.setVoiceOutgoingValue(apiCugTemplate.getVoiceOutgoingValue());
            }
        }        
        return crmCugTemplate;
    }
}
