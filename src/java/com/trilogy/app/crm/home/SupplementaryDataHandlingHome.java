/*
 * This code is a protected work and subject to domestic and international copyright
 * law(s). A complete listing of authors of this work is readily available. Additionally,
 * source code is, by its very nature, confidential information and inextricably contains
 * trade secrets and other information proprietary, valuable and sensitive to Redknee. No
 * unauthorized use, disclosure, manipulation or otherwise is permitted, and may only be
 * used in accordance with the terms of the license agreement entered into with Redknee
 * Inc. and/or its subsidiaries.
 * 
 * Copyright � Redknee Inc. and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.home;

import java.util.List;

import com.trilogy.app.crm.bean.SupplementaryData;
import com.trilogy.app.crm.bean.SupplementaryDataAware;
import com.trilogy.app.crm.bean.SupplementaryDataEntityEnum;
import com.trilogy.app.crm.support.SupplementaryDataSupportHelper;
import com.trilogy.framework.xhome.beans.xi.PropertyInfo;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xlog.log.LogSupport;

/**
 * 
 * @author Marcio Marques
 *
 * @since 9.1.3
 */
public class SupplementaryDataHandlingHome extends HomeProxy 
{

    /**
     * 
     */
    private static final long serialVersionUID = 1L;


    public SupplementaryDataHandlingHome(Context ctx, Home delegate)
    {
        this(ctx, delegate, null, null, null);
    }

    public SupplementaryDataHandlingHome(Context ctx, Home delegate, PropertyInfo supplementaryDataPropertyInfo, PropertyInfo idPropertyInfo, SupplementaryDataEntityEnum entity)
    {
        super(ctx, delegate);
        supplementaryDataPropertyInfo_ = supplementaryDataPropertyInfo;
        idPropertyInfo_ = idPropertyInfo;
        entity_ = entity;
    }


    public Object create(Context ctx, Object obj)
            throws HomeException, HomeInternalException
    {
        Object result = getDelegate(ctx).create(ctx, obj);
        if (supplementaryDataPropertyInfo_ != null && idPropertyInfo_ != null && entity_ != null)
        {
            Object propObj = supplementaryDataPropertyInfo_.get(obj);
            if (propObj!=null && propObj instanceof List && ((List) propObj).size()>0 && ((List) propObj).iterator().next() instanceof SupplementaryData)
            {
                List<SupplementaryData> list = (List<SupplementaryData>) propObj;
                try
                {
                    String id = String.valueOf(idPropertyInfo_.get(result));

                    for (SupplementaryData newSupplementaryData : list)
                    {
                        newSupplementaryData.setIdentifier(id);
                        newSupplementaryData.setEntity(entity_.getIndex());
                        SupplementaryDataSupportHelper.get(ctx).addOrUpdateSupplementaryData(ctx, newSupplementaryData);
                    }
                }
                catch (HomeException e)
                {
                    LogSupport.minor(ctx, this, "Unable to update supplementary data.", e);
                    // cannot continue with this contact
                }
                
            }
        }
        return result;
    }

    
    public Object store(Context ctx, Object obj)
            throws HomeException, HomeInternalException
    {
        Object result = getDelegate(ctx).store(ctx, obj);
        if (supplementaryDataPropertyInfo_ != null && idPropertyInfo_ != null && entity_ != null)
        {
            Object propObj = supplementaryDataPropertyInfo_.get(obj);
            if (propObj!=null && propObj instanceof List && ((List) propObj).size()>0 && ((List) propObj).iterator().next() instanceof SupplementaryData)
            {
                List<SupplementaryData> list = (List<SupplementaryData>) propObj;
                try
                {
                    String id = String.valueOf(idPropertyInfo_.get(result));

                    for (SupplementaryData newSupplementaryData : list)
                    {
                        newSupplementaryData.setIdentifier(id);
                        newSupplementaryData.setEntity(entity_.getIndex());
                        SupplementaryDataSupportHelper.get(ctx).addOrUpdateSupplementaryData(ctx, newSupplementaryData);
                    }
                }
                catch (HomeException e)
                {
                    LogSupport.minor(ctx, this, "Unable to update supplementary data.", e);
                }
                
            }
        }
        return result;
    }

    @Override
    public void remove(Context ctx, final Object obj)
        throws HomeException, HomeInternalException
    {
        if (obj instanceof SupplementaryDataAware)
        {
            SupplementaryDataAware supplementaryDataAware = (SupplementaryDataAware) obj;
            supplementaryDataAware.removeAllSupplementaryData(ctx);
        }
        super.remove(ctx,obj);
    }    
    
    private PropertyInfo idPropertyInfo_ = null;

    private PropertyInfo supplementaryDataPropertyInfo_ = null;

    private SupplementaryDataEntityEnum entity_;
}
