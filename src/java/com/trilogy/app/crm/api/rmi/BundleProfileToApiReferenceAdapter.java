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

import com.trilogy.app.crm.api.rmi.support.RmiApiSupport;
import com.trilogy.app.crm.bundle.BundleProfile;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Adapter;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.util.crmapi.wsdl.v2_0.types.PaidTypeEnum;
import com.trilogy.util.crmapi.wsdl.v3_0.types.serviceandbundle.BundleReference;
import com.trilogy.util.crmapi.wsdl.v3_0.types.serviceandbundle.BundleType;


/**
 * Adapts BundleProfile object to API objects.
 * 
 * @author bhupendra.pandey@redknee.com
 */
public class BundleProfileToApiReferenceAdapter implements Adapter
{

    public Object adapt(final Context ctx, final Object obj) throws HomeException
    {
        return adaptBundleProfileToReference(ctx, (BundleProfile) obj);
    }


    public Object unAdapt(final Context ctx, final Object obj) throws HomeException
    {
        throw new UnsupportedOperationException();
    }


    public static BundleReference adaptBundleProfileToReference(Context ctx, final BundleProfile crmBundleProfile)
    {
        final BundleReference apiBundleRef = new BundleReference();
        adaptBundleProfileToReference(ctx, crmBundleProfile, apiBundleRef);
        return apiBundleRef;
    }


    public static BundleReference adaptBundleProfileToReference(Context ctx, final BundleProfile crmBundleProfile,
            final BundleReference apiBundleRef)
    {
        apiBundleRef.setIdentifier(crmBundleProfile.getBundleId());
        apiBundleRef.setAuxiliary(crmBundleProfile.getAuxiliary());
        apiBundleRef.setName(crmBundleProfile.getName());
        apiBundleRef.setSpid(crmBundleProfile.getSpid());
        apiBundleRef.setPaidType(PaidTypeEnum.valueOf(crmBundleProfile.getSegment().getIndex()));
        apiBundleRef.setRecurrence(RmiApiSupport.getRecurrenceScheme(crmBundleProfile));
        apiBundleRef.setBundleType(BundleType.Factory.fromValue(crmBundleProfile.getType()));
        return apiBundleRef;
    }
}
