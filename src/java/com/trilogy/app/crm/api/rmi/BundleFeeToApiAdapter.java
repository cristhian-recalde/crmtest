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
import com.trilogy.app.crm.bean.core.BundleFee;
import com.trilogy.app.crm.bean.core.BundleProfile;
import com.trilogy.util.crmapi.wsdl.v2_1.types.serviceandbundle.ServicePreferenceEnum;
import com.trilogy.util.crmapi.wsdl.v3_0.types.serviceandbundle.BundleFeeReference;

/**
 * Adapts BundleFee object to API objects.
 *
 * @author victor.stratan@redknee.com
 */
public class BundleFeeToApiAdapter implements Adapter
{
    public Object adapt(final Context ctx, final Object obj) throws HomeException
    {
        return adaptBundleFeeToReference(ctx, (BundleFee) obj);
    }

    public Object unAdapt(final Context ctx, final Object obj) throws HomeException
    {
        throw new UnsupportedOperationException();
    }

    public static BundleFeeReference adaptBundleFeeToReference(final Context ctx, final BundleFee bundleFee)
    {
        final BundleFeeReference reference = new BundleFeeReference();
        reference.setIdentifier(bundleFee.getId());
        reference.setPreference(ServicePreferenceEnum.valueOf(bundleFee.getServicePreference().getIndex()));
        reference.setPeriod(RmiApiSupport.convertCrmServicePeriodEnum2ApiServicePeriodType(bundleFee.getServicePeriod()));
        reference.setFee(bundleFee.getFee());
        try
        {
            reference.setPackageID(Long.valueOf(bundleFee.getSource()));
        }
        catch (NumberFormatException e)
        {
            reference.setPackageID(0L);
            String msg = "Unable to convert bundle fee source '" + bundleFee.getSource() + "' to package ID. Assuming bundle is not part of a package.";
            LogSupport.info(ctx, BundleFeeToApiAdapter.class.getName(), msg, null);
            if (LogSupport.isDebugEnabled(ctx))
            {
                new DebugLogMsg(BundleFeeToApiAdapter.class, msg, e).log(ctx);
            }
        }
        return reference;
    }
    
    public static BundleFee adaptApiToBundleFee(Context ctx, BundleFeeReference apiBundleFeeRef)
    {
        BundleFee crmBundleFee = null;
        BundleProfile crmBundleProfile = null;
        try
        {
            crmBundleFee = (BundleFee) XBeans.instantiate(BundleFee.class, ctx);
            crmBundleProfile = crmBundleFee.getBundleProfile(ctx);
        }
        catch (Exception e)
        {
            new MinorLogMsg(BundleFeeToApiAdapter.class,
                    "Error instantiating new BundleFee.  Using default constructor.", e).log(ctx);
            crmBundleFee = new BundleFee();
        }
        if (apiBundleFeeRef.getIdentifier() != null)
        {
            crmBundleFee.setId(apiBundleFeeRef.getIdentifier());
        }
        // crmBundleFee.setBundleProfile(null);
        // crmBundleFee.setEndDate(null);
        if (apiBundleFeeRef.getFee() != null)
        {
            crmBundleFee.setFee(apiBundleFeeRef.getFee());
        }
        // crmBundleFee.setPaymentNum(0);
        if (apiBundleFeeRef.getPeriod() != null)
        {
            crmBundleFee.setServicePeriod(RmiApiSupport.convertApiServicePeriodType2CrmServicePeriodEnum(
                    apiBundleFeeRef.getPeriod(), crmBundleProfile.getValidity()));
        }
        if (apiBundleFeeRef.getPreference() != null)
        {
            crmBundleFee.setServicePreference(com.redknee.app.crm.bean.ServicePreferenceEnum
                    .get((short) apiBundleFeeRef.getPreference().getValue()));
        }
        // crmBundleFee.setSource(null);
        // crmBundleFee.setStartDate(null);
        return crmBundleFee;
    }
}
