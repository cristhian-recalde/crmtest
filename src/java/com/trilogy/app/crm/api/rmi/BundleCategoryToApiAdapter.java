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
import com.trilogy.app.crm.bundle.UnitTypeEnum;
import com.trilogy.framework.xhome.beans.XBeans;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Adapter;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.MinorLogMsg;


/**
 * Adapts BundleProfile object to API objects.
 * 
 * @author bhupendra.pandey@redknee.com
 */
public class BundleCategoryToApiAdapter implements Adapter
{

    public Object adapt(final Context ctx, final Object obj) throws HomeException
    {
        return adaptBundleCategoryToApi(ctx, (BundleCategory) obj);
    }


    public Object unAdapt(final Context ctx, final Object obj) throws HomeException
    {
        throw new UnsupportedOperationException();
    }


    public static com.redknee.util.crmapi.wsdl.v3_0.types.serviceandbundle.BundleCategory adaptBundleCategoryToApi(
            Context ctx, final BundleCategory crmBundleCategory)
    {
        final com.redknee.util.crmapi.wsdl.v3_0.types.serviceandbundle.BundleCategory apiBundleCategory = new com.redknee.util.crmapi.wsdl.v3_0.types.serviceandbundle.BundleCategory();
        BundleCategoryToApiReferenceAdapter
                .adaptBundleCategoryToApiReference(ctx, crmBundleCategory, apiBundleCategory);
        return apiBundleCategory;
    }


    public static BundleCategory adaptApiToBundleCategory(Context ctx,
            final com.redknee.util.crmapi.wsdl.v3_0.types.serviceandbundle.BundleCategory apiBundleCategory)
    {
        BundleCategory crmBundleCategory = null;
        try
        {
            crmBundleCategory = (BundleCategory) XBeans.instantiate(BundleCategory.class, ctx);
        }
        catch (Exception e)
        {
            new MinorLogMsg(BundleCategoryToApiAdapter.class,
                    "Error instantiating new BundleCategory.  Using default constructor.", e).log(ctx);
            crmBundleCategory = new BundleCategory();
        }
        adaptApiToBundleCategory(ctx, apiBundleCategory, crmBundleCategory);
        return crmBundleCategory;
    }


    public static BundleCategory adaptApiToBundleCategory(Context ctx,
            final com.redknee.util.crmapi.wsdl.v3_0.types.serviceandbundle.BundleCategory apiBundleCategory,
            BundleCategory crmBundleCategory)
    {
        if (apiBundleCategory.getEnabled() != null)
        {
            crmBundleCategory.setEnabled(apiBundleCategory.getEnabled());
        }
        if (apiBundleCategory.getIdentifier() != null)
        {
            crmBundleCategory.setCategoryId(apiBundleCategory.getIdentifier().intValue());
        }
        if (apiBundleCategory.getName() != null)
        {
            crmBundleCategory.setName(apiBundleCategory.getName());
        }
        if (apiBundleCategory.getSpid() != null)
        {
            crmBundleCategory.setSpid(apiBundleCategory.getSpid());
        }
        if (apiBundleCategory.getUnitType() != null)
        {
            crmBundleCategory.setUnitType(UnitTypeEnum.get((short) apiBundleCategory.getUnitType().getValue()));
        }
        return crmBundleCategory;
    }
}
