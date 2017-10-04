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
package com.trilogy.app.crm.bundle.home;

import com.trilogy.framework.xhome.beans.xi.XInfo;
import com.trilogy.framework.xhome.beans.xi.XInfoAdapter;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.LogSupport;

import com.trilogy.app.crm.bundle.BundleCategory;
import com.trilogy.app.crm.bundle.UnitTypeEnum;
import com.trilogy.app.crm.support.BundleSupportHelper;
import com.trilogy.product.bundle.manager.api.v21.BundleCategoryApi;

/**
 * Adapts the CRM Bundle category to the RMI Bundle manager category 
 * @author arturo.medina@redknee.com
 *
 */
public class BundleCategoryXInfoAdapter extends XInfoAdapter
{

    /**
     * Default constructor accepts the BundleCategory as input and the BM category as destination
     * @param source
     * @param destination
     */
    public BundleCategoryXInfoAdapter(XInfo source, XInfo destination)
    {
        super(source, destination);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object adapt(Context ctx, Object source) throws HomeException
    {
        BundleCategory category = null;
        try
        {
            category = (BundleCategory)super.adapt(ctx, source);
            BundleCategoryApi bmCategory = (BundleCategoryApi) source;
            
            category.setUnitType( (UnitTypeEnum) 
                    BundleSupportHelper.get(ctx).mapBundleEnums(bmCategory.getUnitType(), category.getUnitType()));
        }
        catch (Throwable e)
        {
           if (LogSupport.isDebugEnabled(ctx))
           {
               LogSupport.debug(ctx, this, "Error when adapting the bundle ", e);
           }
        }
        
        return category;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object unAdapt(Context ctx, Object destination) throws HomeException
    {
        BundleCategoryApi bmCategory = (BundleCategoryApi)super.unAdapt(ctx, destination);
        BundleCategory category = (BundleCategory) destination;
        
        bmCategory.setUnitType( 
                (com.redknee.product.bundle.manager.api.v21.UnitTypeEnum) 
                BundleSupportHelper.get(ctx).mapBundleEnums(category.getUnitType(), bmCategory.getUnitType()));
        
        return bmCategory;
    }
    
    
    /**
     * 
     */
    private static final long serialVersionUID = -6250511395045230441L;

}
