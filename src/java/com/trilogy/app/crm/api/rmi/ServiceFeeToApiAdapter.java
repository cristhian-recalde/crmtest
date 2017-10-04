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
package com.trilogy.app.crm.api.rmi;

import com.trilogy.framework.xhome.beans.XBeans;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Adapter;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MinorLogMsg;

import com.trilogy.app.crm.api.rmi.support.RmiApiSupport;
import com.trilogy.app.crm.bean.Service;
import com.trilogy.app.crm.bean.core.ServiceFee2;
import com.trilogy.app.crm.support.ServiceSupport;
import com.trilogy.util.crmapi.wsdl.v2_1.types.serviceandbundle.ServicePreferenceEnum;
import com.trilogy.util.crmapi.wsdl.v3_0.types.serviceandbundle.ServiceFeeReference;
import com.trilogy.util.crmapi.wsdl.v3_0.types.serviceandbundle.ServiceTypeEnum;

/**
 * Adapts ServiceFee2 object to API objects.
 *
 * @author victor.stratan@redknee.com
 */
public class ServiceFeeToApiAdapter implements Adapter
{
    public Object adapt(final Context ctx, final Object obj) throws HomeException
    {
        return adaptServiceFeeToReference(ctx, (ServiceFee2) obj);
    }

    public Object unAdapt(final Context ctx, final Object obj) throws HomeException
    {
        throw new UnsupportedOperationException();
    }
    public static ServiceFeeReference adaptServiceFeeToReference(final Context ctx, final ServiceFee2 serviceFee)
    {
        final ServiceFeeReference reference = new ServiceFeeReference();
        reference.setIdentifier(serviceFee.getServiceId());
        reference.setPreference(ServicePreferenceEnum.valueOf(serviceFee.getServicePreference().getIndex()));
        reference.setPeriod(RmiApiSupport.convertCrmServicePeriodEnum2ApiServicePeriodType(serviceFee.getServicePeriod()));
        reference.setFee(serviceFee.getFee());

        try
        {
            reference.setPackageID(Long.valueOf(serviceFee.getSource()));
        }
        catch (NumberFormatException e)
        {
            String msg = "Unable to convert service fee source '" + serviceFee.getSource() + "' to package ID. Assuming service is not part of a package.";
            LogSupport.info(ctx, ServiceFeeToApiAdapter.class.getName(), msg, null);
            if (LogSupport.isDebugEnabled(ctx))
            {
                new DebugLogMsg(ServiceFeeToApiAdapter.class, msg, e).log(ctx);
            }
            reference.setPackageID(0L);
        }
        
        try
        {
            final Service service = ServiceSupport.getService(ctx, serviceFee.getServiceId());
            reference.setType(ServiceTypeEnum.valueOf(service.getType().getIndex()));
        }
        catch (Throwable e)
        {
            LogSupport.minor(ctx, ServiceFeeToApiAdapter.class.getName(), "Cannot retreive service " + serviceFee.getServiceId()
                    + " Data Integrity problem", e);
        }

        return reference;
    }
    

    public static ServiceFee2 adaptApiToServiceFee2(Context ctx, ServiceFeeReference apiServiceRef)
    {
        ServiceFee2 crmServiceFee2 = null;
        try
        {
            crmServiceFee2 = (ServiceFee2) XBeans.instantiate(ServiceFee2.class, ctx);
        }
        catch (Exception e)
        {
            new MinorLogMsg(ServiceFeeToApiAdapter.class,
                    "Error instantiating new BundleFee.  Using default constructor.", e).log(ctx);
            crmServiceFee2 = new ServiceFee2();
        }
        // crmServiceFee2.setChecked(false);
        // crmServiceFee2.setCltcDisabled(false);
        // crmServiceFee2.setDispCLTC(false);
        // crmServiceFee2.setEnabled(false);
        if (apiServiceRef.getFee() != null)
        {
            crmServiceFee2.setFee(apiServiceRef.getFee());
        }
        if (apiServiceRef.getRecurrenceInterval() != null)
        {
            crmServiceFee2.setRecurrenceInterval(apiServiceRef.getRecurrenceInterval());
        }
        // crmServiceFee2.setService(null);
        if (apiServiceRef.getIdentifier() != null)
        {
            crmServiceFee2.setServiceId(apiServiceRef.getIdentifier());
        }
        if (apiServiceRef.getPeriod() != null)
        {
            crmServiceFee2.setServicePeriod(RmiApiSupport.convertApiServicePeriodType2CrmServicePeriodEnum(
                    apiServiceRef.getPeriod(), apiServiceRef.getRecurrenceInterval()));
        }
        // crmServiceFee2.setSource(null);
        if (apiServiceRef.getPreference() != null)
        {
            crmServiceFee2.setServicePreference(com.redknee.app.crm.bean.ServicePreferenceEnum
                    .get((short) apiServiceRef.getPreference().getValue()));
        }
        return crmServiceFee2;
    }
}
