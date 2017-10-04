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

import java.util.HashMap;
import java.util.Map;

import com.trilogy.app.crm.bean.PricePlanVersion;
import com.trilogy.app.crm.bean.ServicePackageVersion;
import com.trilogy.app.crm.bean.core.BundleFee;
import com.trilogy.app.crm.bean.core.ServiceFee2;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.app.crm.support.CollectionSupportHelper;
import com.trilogy.framework.xhome.beans.XBeans;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Adapter;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.MinorLogMsg;
import com.trilogy.util.crmapi.wsdl.v2_0.types.GenericParameter;
import com.trilogy.util.crmapi.wsdl.v3_0.types.serviceandbundle.BundleFeeReference;
import com.trilogy.util.crmapi.wsdl.v3_0.types.serviceandbundle.ServiceFeeReference;


/**
 * Adapts PricePlanVerson object to API objects.
 * 
 * @author bhupendra.pandey@redknee.com
 */
public class PricePlanVersionToApiAdapter implements Adapter
{

    public Object adapt(final Context ctx, final Object obj) throws HomeException
    {
        return adaptPricePlanVersionToApi(ctx, (PricePlanVersion) obj);
    }


    public Object unAdapt(final Context ctx, final Object obj) throws HomeException
    {
        throw new UnsupportedOperationException();
    }


    public static com.redknee.util.crmapi.wsdl.v3_0.types.serviceandbundle.PricePlanVersion adaptPricePlanVersionToApi(
            Context ctx, PricePlanVersion crmPricePlanVersion) throws HomeException
    {
        com.redknee.util.crmapi.wsdl.v3_0.types.serviceandbundle.PricePlanVersion apiPricePlanVersion = new com.redknee.util.crmapi.wsdl.v3_0.types.serviceandbundle.PricePlanVersion();
        apiPricePlanVersion.setActivation(CalendarSupportHelper.get(ctx).dateToCalendar(
                crmPricePlanVersion.getActivation()));
        apiPricePlanVersion.setActivationDate(CalendarSupportHelper.get(ctx).dateToCalendar(
                crmPricePlanVersion.getActivateDate()));
        ServicePackageVersion crmServicePkgVersion = crmPricePlanVersion.getServicePackageVersion();
        if (crmServicePkgVersion != null)
        {
            if (crmServicePkgVersion.getBundleFees() != null)
            {
                BundleFeeReference apiBundleFeeRefs[] = new BundleFeeReference[]
                    {};
                apiBundleFeeRefs = CollectionSupportHelper.get(ctx).adaptCollection(ctx,
                        crmServicePkgVersion.getBundleFees().values(), new BundleFeeToApiAdapter(), apiBundleFeeRefs);
                apiPricePlanVersion.setBundleFees(apiBundleFeeRefs);
            }
            if (crmServicePkgVersion.getServiceFees() != null)
            {
                ServiceFeeReference apiServiceFeeRefs[] = new ServiceFeeReference[]
                    {};
                apiServiceFeeRefs = CollectionSupportHelper.get(ctx)
                        .adaptCollection(ctx, crmServicePkgVersion.getServiceFees().values(),
                                new ServiceFeeToApiAdapter(), apiServiceFeeRefs);
                apiPricePlanVersion.setServiceFees(apiServiceFeeRefs);
            }
        }
        apiPricePlanVersion.setCreatedDate(CalendarSupportHelper.get(ctx).dateToCalendar(
                crmPricePlanVersion.getCreatedDate()));
        apiPricePlanVersion.setDefaultCreditLimit(crmPricePlanVersion.getCreditLimit());
        apiPricePlanVersion.setDefaultDeposit(crmPricePlanVersion.getDeposit());
        apiPricePlanVersion.setDefaultPerMinuteAirRate(crmPricePlanVersion.getDefaultPerMinuteAirRate());
        apiPricePlanVersion.setDescription(crmPricePlanVersion.getDescription());
        apiPricePlanVersion.setOverUsageDataRate(crmPricePlanVersion.getOverusageDataRate());
        apiPricePlanVersion.setOverUsageSMSRate(crmPricePlanVersion.getOverusageSmsRate());
        apiPricePlanVersion.setOverUsageVoiceRate(crmPricePlanVersion.getOverusageVoiceRate());        
        apiPricePlanVersion.setVersion(Long.valueOf(crmPricePlanVersion.getVersion()));
        
        GenericParameter charge = new GenericParameter();
        charge.setName(CHARGE);
        charge.setValue(Long.valueOf(crmPricePlanVersion.getCharge()));
        apiPricePlanVersion.addParameters(charge);

        GenericParameter chargingCycle = new GenericParameter();
        chargingCycle.setName(CHARGE_CYCLE);
        chargingCycle.setValue(Long.valueOf(crmPricePlanVersion.getChargeCycle().getIndex()));
        apiPricePlanVersion.addParameters(chargingCycle);
        return apiPricePlanVersion;
    }


    public static PricePlanVersion adaptApiToPricePlanVersion(Context ctx,
            com.redknee.util.crmapi.wsdl.v3_0.types.serviceandbundle.PricePlanVersion apiPricePlanVersion)
    {
        PricePlanVersion crmPricePlanVersion = null;
        try
        {
            crmPricePlanVersion = (PricePlanVersion) XBeans.instantiate(PricePlanVersion.class, ctx);
        }
        catch (Exception e)
        {
            new MinorLogMsg(PricePlanVersionToApiAdapter.class,
                    "Error instantiating new PricePlanVersion.  Using default constructor.", e).log(ctx);
            crmPricePlanVersion = new PricePlanVersion();
        }
        adaptApiToPricePlanVersion(ctx, apiPricePlanVersion, crmPricePlanVersion);
        return crmPricePlanVersion;
    }


    public static PricePlanVersion adaptApiToPricePlanVersion(Context ctx,
            com.redknee.util.crmapi.wsdl.v3_0.types.serviceandbundle.PricePlanVersion apiPricePlanVersion,
            PricePlanVersion crmPricePlanVersion)
    {
        if (apiPricePlanVersion.getActivationDate() != null)
        {
            crmPricePlanVersion.setActivateDate(apiPricePlanVersion.getActivationDate().getTime());
        }
        if (apiPricePlanVersion.getActivation() != null)
        {
            crmPricePlanVersion.setActivation(apiPricePlanVersion.getActivation().getTime());
        }
        if (apiPricePlanVersion.getDefaultCreditLimit() != null)
        {
            crmPricePlanVersion.setCreditLimit(apiPricePlanVersion.getDefaultCreditLimit());
        }
        if (apiPricePlanVersion.getDefaultPerMinuteAirRate() != null)
        {
            crmPricePlanVersion.setDefaultPerMinuteAirRate(apiPricePlanVersion.getDefaultPerMinuteAirRate());
        }
        if (apiPricePlanVersion.getDefaultDeposit() != null)
        {
            crmPricePlanVersion.setDeposit(apiPricePlanVersion.getDefaultDeposit());
        }
        if (apiPricePlanVersion.getDescription() != null)
        {
            crmPricePlanVersion.setDescription(apiPricePlanVersion.getDescription());
        }
        if (apiPricePlanVersion.getOverUsageDataRate() != null)
        {
            crmPricePlanVersion.setOverusageDataRate(apiPricePlanVersion.getOverUsageDataRate());
        }
        if (apiPricePlanVersion.getOverUsageSMSRate() != null)
        {
            crmPricePlanVersion.setOverusageSmsRate(apiPricePlanVersion.getOverUsageSMSRate());
        }
        if (apiPricePlanVersion.getOverUsageVoiceRate() != null)
        {
            crmPricePlanVersion.setOverusageVoiceRate(apiPricePlanVersion.getOverUsageVoiceRate());
        }
        ServicePackageVersion crmServicePkgVersion = new ServicePackageVersion();
        if (apiPricePlanVersion.getActivationDate() != null)
        {
            crmServicePkgVersion.setActivateDate(apiPricePlanVersion.getActivationDate().getTime());
        }
        if (apiPricePlanVersion.getActivation() != null)
        {
            crmServicePkgVersion.setActivation(apiPricePlanVersion.getActivation().getTime());
        }
        BundleFeeReference[] apiBundleFeeRefs = apiPricePlanVersion.getBundleFees();
        if (apiBundleFeeRefs != null)
        {
            Map crmBundleFees = new HashMap();
            for (BundleFeeReference ref : apiBundleFeeRefs)
            {
                BundleFee crmBundleFee = BundleFeeToApiAdapter.adaptApiToBundleFee(ctx, ref);
                crmBundleFees.put(crmBundleFee.getId(), crmBundleFee);
            }
            crmServicePkgVersion.setBundleFees(crmBundleFees);
        }
        ServiceFeeReference[] apiServiceFeeRefs = apiPricePlanVersion.getServiceFees();
        if (apiServiceFeeRefs != null)
        {
            Map crmServiceFees = new HashMap();
            for (ServiceFeeReference ref : apiServiceFeeRefs)
            {
                ServiceFee2 crmServiceFee = ServiceFeeToApiAdapter.adaptApiToServiceFee2(ctx, ref);
                crmServiceFees.put(crmServiceFee.getServiceId(), crmServiceFee);
            }
            crmServicePkgVersion.setServiceFees(crmServiceFees);
        }
        crmPricePlanVersion.setServicePackageVersion(crmServicePkgVersion);
        return crmPricePlanVersion;
    }
    
    public static String CHARGE = "PricePlanVersionCharge";
    public static String CHARGE_CYCLE = "PricePlanVersionChargeCycle";
}
