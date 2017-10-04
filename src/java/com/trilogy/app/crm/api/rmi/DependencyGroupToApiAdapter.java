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

import java.util.LinkedHashSet;
import java.util.Set;

import com.trilogy.framework.xhome.beans.XBeans;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Adapter;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.MinorLogMsg;

import com.trilogy.app.crm.bean.DependencyGroup;
import com.trilogy.app.crm.bean.DependencyGroupTypeEnum;
import com.trilogy.util.crmapi.wsdl.v3_0.types.serviceandbundle.PricePlanDependencyGroup;
import com.trilogy.util.crmapi.wsdl.v3_0.types.serviceandbundle.PricePlanDependencyGroupTypeEnum;


/**
 * Adapts DependencyGroup object to API objects.
 * 
 * @author bhupendra.pandey@redknee.com
 */
public class DependencyGroupToApiAdapter implements Adapter
{

    public Object adapt(final Context ctx, final Object obj) throws HomeException
    {
        return adaptDependencyGroupToApi(ctx, (DependencyGroup) obj);
    }


    public Object unAdapt(final Context ctx, final Object obj) throws HomeException
    {
        throw new UnsupportedOperationException();
    }


    public static DependencyGroup adaptApiToDependencyGroup(final Context ctx,
            final PricePlanDependencyGroup apiPricePlanDependencyGroup)
    {
        DependencyGroup crmDependencyGroup = null;
        try
        {
            crmDependencyGroup = (DependencyGroup) XBeans.instantiate(DependencyGroup.class, ctx);
        }
        catch (Exception e)
        {
            new MinorLogMsg(DependencyGroupToApiAdapter.class,
                    "Error instantiating new DependencyGroup.  Using default constructor.", e).log(ctx);
            crmDependencyGroup = new DependencyGroup();
        }
        adaptApiToDependencyGroup(ctx, apiPricePlanDependencyGroup, crmDependencyGroup);
        return crmDependencyGroup;
    }


    public static DependencyGroup adaptApiToDependencyGroup(final Context ctx,
            final PricePlanDependencyGroup apiPricePlanDependencyGroup, DependencyGroup crmDependencyGroup)
    {
        if (apiPricePlanDependencyGroup.getName() != null)
        {
            crmDependencyGroup.setName(apiPricePlanDependencyGroup.getName());
        }
        if (apiPricePlanDependencyGroup.getSpid() != null)
        {
            crmDependencyGroup.setSpid(apiPricePlanDependencyGroup.getSpid());
        }
        if (apiPricePlanDependencyGroup.getType() != null)
        {
            crmDependencyGroup.setType(DependencyGroupTypeEnum.get((short) apiPricePlanDependencyGroup.getType()
                    .getValue()));
        }
        // Set Default sets here
        crmDependencyGroup.setAuxiliaryService(new LinkedHashSet());
        crmDependencyGroup.setServicebundle(new LinkedHashSet());
        crmDependencyGroup.setServices(new LinkedHashSet());
        if (apiPricePlanDependencyGroup.getAuxiliaryServiceIDs() != null)
        {
            Set crmAuxServiceSet = new LinkedHashSet();
            for (Long auxServiceId : apiPricePlanDependencyGroup.getAuxiliaryServiceIDs())
            {
                crmAuxServiceSet.add(auxServiceId.toString());
            }
            crmDependencyGroup.setAuxiliaryService(crmAuxServiceSet);
        }        
        if (apiPricePlanDependencyGroup.getBundleIDs() != null)
        {
            Set crmBundleSet = new LinkedHashSet();
            for (Long bundleId : apiPricePlanDependencyGroup.getBundleIDs())
            {
                crmBundleSet.add(bundleId.toString());
            }
            crmDependencyGroup.setServicebundle(crmBundleSet);
        }
        if (apiPricePlanDependencyGroup.getServiceIDs() != null)
        {
            Set crmServiceSet = new LinkedHashSet();
            for (Long serviceId : apiPricePlanDependencyGroup.getServiceIDs())
            {
                crmServiceSet.add(serviceId.toString());
            }
            crmDependencyGroup.setServices(crmServiceSet);
        }
        return crmDependencyGroup;
    }


    public static PricePlanDependencyGroup adaptDependencyGroupToApi(final Context ctx,
            final DependencyGroup crmDependencyGroup) throws HomeException
    {
        final PricePlanDependencyGroup apiPricePlanDependencyGroup = new PricePlanDependencyGroup();
        apiPricePlanDependencyGroup.setIdentifier(crmDependencyGroup.getIdentifier());
        apiPricePlanDependencyGroup.setName(crmDependencyGroup.getName());
        apiPricePlanDependencyGroup.setSpid(crmDependencyGroup.getSpid());
        apiPricePlanDependencyGroup.setType(PricePlanDependencyGroupTypeEnum.valueOf(crmDependencyGroup.getType()
                .getIndex()));
        Set crmAuxiliaryServiceIDs = crmDependencyGroup.getAuxiliaryService();
        Long[] apiAuxiliaryServiceIDs = new Long[crmAuxiliaryServiceIDs.size()];
        int i = 0;
        for (Object obj : crmAuxiliaryServiceIDs)
        {
            apiAuxiliaryServiceIDs[i] = Long.valueOf((String) obj);
            i++;
        }
        apiPricePlanDependencyGroup.setAuxiliaryServiceIDs(apiAuxiliaryServiceIDs);
        Set crmBundleIDs = crmDependencyGroup.getServicebundle();
        Long[] apiBundleIDs = new Long[crmBundleIDs.size()];
        i = 0;
        for (Object obj : crmBundleIDs)
        {
            apiBundleIDs[i] = Long.valueOf((String) obj);
            i++;
        }
        apiPricePlanDependencyGroup.setBundleIDs(apiBundleIDs);
        Set crmServicesIDs = crmDependencyGroup.getServices();
        Long[] apiServicesIDs = new Long[crmServicesIDs.size()];
        i = 0;
        for (Object obj : crmServicesIDs)
        {
            apiServicesIDs[i] = Long.valueOf((String) obj);
            i++;
        }
        apiPricePlanDependencyGroup.setServiceIDs(apiServicesIDs);
        return apiPricePlanDependencyGroup;
    }
}
