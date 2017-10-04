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

import com.trilogy.app.crm.bundle.BundleCategory;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Adapter;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.util.crmapi.wsdl.v2_2.types.subscription.SubscriptionBundleUnitTypeEnum;


/**
 * Adapts BundleProfile object to API Reference objects.
 * 
 * @author bhupendra.pandey@redknee.com
 */
public class BundleCategoryToApiReferenceAdapter implements Adapter
{

    public Object adapt(final Context ctx, final Object obj) throws HomeException
    {
        return adaptBundleCategoryToApiReference(ctx, (BundleCategory) obj);
    }


    public Object unAdapt(final Context ctx, final Object obj) throws HomeException
    {
        throw new UnsupportedOperationException();
    }


    public static com.redknee.util.crmapi.wsdl.v3_0.types.serviceandbundle.BundleCategoryReference adaptBundleCategoryToApiReference(
            Context ctx, final BundleCategory crmBundleCategory)
    {
        final com.redknee.util.crmapi.wsdl.v3_0.types.serviceandbundle.BundleCategoryReference apiBundleCategoryRef = new com.redknee.util.crmapi.wsdl.v3_0.types.serviceandbundle.BundleCategoryReference();
        adaptBundleCategoryToApiReference(ctx, crmBundleCategory, apiBundleCategoryRef);
        return apiBundleCategoryRef;
    }


    public static com.redknee.util.crmapi.wsdl.v3_0.types.serviceandbundle.BundleCategoryReference adaptBundleCategoryToApiReference(
            Context ctx, final BundleCategory crmBundleCategory,
            com.redknee.util.crmapi.wsdl.v3_0.types.serviceandbundle.BundleCategoryReference apiBundleCategoryRef)
    {
        apiBundleCategoryRef.setEnabled(crmBundleCategory.getEnabled());
        apiBundleCategoryRef.setIdentifier(Long.valueOf(crmBundleCategory.getCategoryId()));
        apiBundleCategoryRef.setName(crmBundleCategory.getName());
        apiBundleCategoryRef.setSpid(crmBundleCategory.getSpid());
        apiBundleCategoryRef.setUnitType(SubscriptionBundleUnitTypeEnum.valueOf(crmBundleCategory.getUnitType()
                .getIndex()));
        return apiBundleCategoryRef;
    }
}
