/*
 * Copyright (c) 2012, Redknee Inc. and its subsidiaries. All Rights Reserved.
 *
 * This code is a protected work and subject to domestic and international copyright law(s). 
 * A complete listing of authors of this work is readily available. Additionally, source
 * code is, by its very nature, confidential information and inextricably contains trade
 * secrets and other information proprietary, valuable and sensitive to Redknee. No unauthorized
 * use, disclosure, manipulation or otherwise is permitted, and may only be used in accordance
 * with the terms of the license agreement entered into with Redknee Inc. and/or its subsidiaries.
 */
package com.trilogy.app.crm.api.rmi;

import com.trilogy.app.crm.bundle.BundleCategory;
import com.trilogy.app.crm.bundle.BundleCategoryXInfo;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Adapter;
import com.trilogy.framework.xhome.home.HomeException;

import com.trilogy.util.crmapi.wsdl.v3_0.types.calldetail.ChargedBundleInfo;
import com.trilogy.util.crmapi.wsdl.v3_0.types.serviceandbundle.BundleTypeEnum;

/**
 * Adapts ChargedBundleInfo object To API data structure
 * 
 * @author mangaraj.sahoo@redknee.com
 * @since 9.3.2
 */
public class ChargedBundleInfoToApiAdapter implements Adapter
{

    /* (non-Javadoc)
     * @see com.redknee.framework.xhome.home.Adapter#adapt(com.redknee.framework.xhome.context.Context, java.lang.Object)
     */
    @Override
    public Object adapt(Context ctx, Object obj) throws HomeException
    {
        return adaptChargedBundleInfoToApi(ctx, (com.redknee.app.crm.bean.ChargedBundleInfo) obj);
    }


    /* (non-Javadoc)
     * @see com.redknee.framework.xhome.home.Adapter#unAdapt(com.redknee.framework.xhome.context.Context, java.lang.Object)
     */
    @Override
    public Object unAdapt(Context ctx, Object obj) throws HomeException
    {
        throw new UnsupportedOperationException();
    }
    
    
    public static ChargedBundleInfo adaptChargedBundleInfoToApi(Context ctx, com.redknee.app.crm.bean.ChargedBundleInfo crmChargedBundle) throws HomeException
    {
        BundleCategory bundleCategory = HomeSupportHelper.get(ctx).findBean(ctx, BundleCategory.class,
                Integer.valueOf(crmChargedBundle.getBundleCategoryId()));
                
        ChargedBundleInfo apiBundleInfo = new ChargedBundleInfo();
        apiBundleInfo.setBundleId(crmChargedBundle.getBundleId());
        apiBundleInfo.setUnitType(BundleTypeEnum.valueOf(crmChargedBundle.getUnitType().getIndex()));
        apiBundleInfo.setChargedAmount(crmChargedBundle.getChargedAmount());
        apiBundleInfo.setBalance(crmChargedBundle.getBalanceAmount());
        apiBundleInfo.setBundleCategoryId(crmChargedBundle.getBundleCategoryId());
        apiBundleInfo.setBundleCategoryDesc(bundleCategory==null ? "Bundle Category does not exist." : bundleCategory.getName());
        
        return apiBundleInfo;
    }
}
