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

import com.trilogy.app.crm.bean.PrerequisiteGroup;
import com.trilogy.framework.xhome.beans.XBeans;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Adapter;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.MinorLogMsg;
import com.trilogy.util.crmapi.wsdl.v3_0.types.serviceandbundle.PricePlanPrerequisiteGroup;


/**
 * Adapts DependencyGroup object to API objects.
 * 
 * @author bhupendra.pandey@redknee.com
 */
public class PrerequisiteGroupToApiAdapter implements Adapter
{

    public Object adapt(final Context ctx, final Object obj) throws HomeException
    {
        return adaptPrerequisiteGroupToApi(ctx, (PrerequisiteGroup) obj);
    }


    public Object unAdapt(final Context ctx, final Object obj) throws HomeException
    {
        throw new UnsupportedOperationException();
    }


    public static PrerequisiteGroup adaptApiToPrerequisiteGroup(final Context ctx,
            final PricePlanPrerequisiteGroup apiPricePlanPrequisiteGroup)
    {
        PrerequisiteGroup crmPrerequisiteGroup = null;
        try
        {
            crmPrerequisiteGroup = (PrerequisiteGroup) XBeans.instantiate(PrerequisiteGroup.class, ctx);
        }
        catch (Exception e)
        {
            new MinorLogMsg(PrerequisiteGroupToApiAdapter.class,
                    "Error instantiating new PrerequisiteGroup.  Using default constructor.", e).log(ctx);
            crmPrerequisiteGroup = new PrerequisiteGroup();
        }
        adaptApiToPrerequisiteGroup(ctx, apiPricePlanPrequisiteGroup, crmPrerequisiteGroup);
        return crmPrerequisiteGroup;
    }


    public static PrerequisiteGroup adaptApiToPrerequisiteGroup(final Context ctx,
            final PricePlanPrerequisiteGroup apiPricePlanPrequisiteGroup, PrerequisiteGroup crmPrerequisiteGroup)
    {
        if (apiPricePlanPrequisiteGroup.getName() != null)
        {
            crmPrerequisiteGroup.setName(apiPricePlanPrequisiteGroup.getName());
        }
        if (apiPricePlanPrequisiteGroup.getSpid() != null)
        {
            crmPrerequisiteGroup.setSpid(apiPricePlanPrequisiteGroup.getSpid());
        }
        if (apiPricePlanPrequisiteGroup.getDependencyPrerequisiteIDs() != null
                && apiPricePlanPrequisiteGroup.getDependencyPrerequisiteIDs().length != 0)
        {
            if (apiPricePlanPrequisiteGroup.getDependencyPrerequisiteIDs()[0] != null)
            {
                crmPrerequisiteGroup.setDependency_list(apiPricePlanPrequisiteGroup.getDependencyPrerequisiteIDs()[0]);
            }
        }
        if (apiPricePlanPrequisiteGroup.getServicePrerequisiteIDs() != null
                && apiPricePlanPrequisiteGroup.getServicePrerequisiteIDs().length != 0)
        {
            if (apiPricePlanPrequisiteGroup.getServicePrerequisiteIDs()[0] != null)
            {
                crmPrerequisiteGroup.setPrereq_service(apiPricePlanPrequisiteGroup.getServicePrerequisiteIDs()[0]);
            }
        }
        return crmPrerequisiteGroup;
    }


    public static PricePlanPrerequisiteGroup adaptPrerequisiteGroupToApi(final Context ctx,
            final PrerequisiteGroup crmPrerequisiteGroup) throws HomeException
    {
        PricePlanPrerequisiteGroup apiPricePlanPrequisiteGroup = new PricePlanPrerequisiteGroup();
        apiPricePlanPrequisiteGroup.setIdentifier(crmPrerequisiteGroup.getIdentifier());
        apiPricePlanPrequisiteGroup.setName(crmPrerequisiteGroup.getName());
        apiPricePlanPrequisiteGroup.setSpid(crmPrerequisiteGroup.getSpid());
        Long[] apiServiceId = new Long[1];
        apiServiceId[0] = Long.valueOf(crmPrerequisiteGroup.getPrereq_service());
        apiPricePlanPrequisiteGroup.setServicePrerequisiteIDs(apiServiceId);
        Long[] apiDependencyId = new Long[1];
        apiDependencyId[0] = Long.valueOf(crmPrerequisiteGroup.getDependency_list());
        apiPricePlanPrequisiteGroup.setDependencyPrerequisiteIDs(apiDependencyId);
        return apiPricePlanPrequisiteGroup;
    }
}
