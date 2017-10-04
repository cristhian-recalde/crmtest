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

import com.trilogy.app.crm.bean.PricePlanGroup;
import com.trilogy.framework.xhome.beans.XBeans;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Adapter;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.MinorLogMsg;
import com.trilogy.util.crmapi.wsdl.v3_0.types.serviceandbundle.PricePlanValidationGroup;


/**
 * Adapts PricePlanGroup object to API objects.
 * 
 * @author bhupendra.pandey@redknee.com
 */
public class PricePlanGroupToApiAdapter implements Adapter
{

    public Object adapt(final Context ctx, final Object obj) throws HomeException
    {
        return adaptPricePlanGroupToApi(ctx, (PricePlanGroup) obj);
    }


    public Object unAdapt(final Context ctx, final Object obj) throws HomeException
    {
        throw new UnsupportedOperationException();
    }


    public static PricePlanGroup adaptApiToPricePlanGroup(final Context ctx,
            final PricePlanValidationGroup apiPricePlanValidationGroup)
    {
        PricePlanGroup crmPricePlanGroup = null;
        try
        {
            crmPricePlanGroup = (PricePlanGroup) XBeans.instantiate(PricePlanGroup.class, ctx);
        }
        catch (Exception e)
        {
            new MinorLogMsg(PricePlanGroupToApiAdapter.class,
                    "Error instantiating new PricePlanGroup.  Using default constructor.", e).log(ctx);
            crmPricePlanGroup = new PricePlanGroup();
        }
        adaptApiToPricePlanGroup(ctx, apiPricePlanValidationGroup, crmPricePlanGroup);
        return crmPricePlanGroup;
    }


    public static PricePlanGroup adaptApiToPricePlanGroup(final Context ctx,
            final PricePlanValidationGroup apiPricePlanValidationGroup, PricePlanGroup crmPricePlanGroup)
    {
        if (apiPricePlanValidationGroup.getName() != null)
        {
            crmPricePlanGroup.setName(apiPricePlanValidationGroup.getName());
        }
        if (apiPricePlanValidationGroup.getParentID() != null)
        {
            crmPricePlanGroup.setParentPPG(apiPricePlanValidationGroup.getParentID());
        }
        if (apiPricePlanValidationGroup.getSpid() != null)
        {
            crmPricePlanGroup.setSpid(apiPricePlanValidationGroup.getSpid());
        }
        if (apiPricePlanValidationGroup.getDependencyIDs() != null)
        {
            Set crmDepend_List = new LinkedHashSet();
            for (Long id : apiPricePlanValidationGroup.getDependencyIDs())
            {
                crmDepend_List.add(id.toString());
            }
            crmPricePlanGroup.setDepend_list(crmDepend_List);
        }
        if (apiPricePlanValidationGroup.getPrerequisiteIDs() != null)
        {
            Set crmPrereq_List = new LinkedHashSet();
            for (Long id : apiPricePlanValidationGroup.getPrerequisiteIDs())
            {
                crmPrereq_List.add(id.toString());
            }
            crmPricePlanGroup.setPrereq_list(crmPrereq_List);
        }
        return crmPricePlanGroup;
    }


    public static PricePlanValidationGroup adaptPricePlanGroupToApi(final Context ctx,
            final PricePlanGroup crmPricePlanGroup) throws HomeException
    {
        final PricePlanValidationGroup apiPricePlanValidationGroup = new PricePlanValidationGroup();
        apiPricePlanValidationGroup.setIdentifier(crmPricePlanGroup.getIdentifier());
        apiPricePlanValidationGroup.setName(crmPricePlanGroup.getName());
        apiPricePlanValidationGroup.setParentID(crmPricePlanGroup.getParentPPG());
        apiPricePlanValidationGroup.setSpid(crmPricePlanGroup.getSpid());
        Set crmDepend_List = crmPricePlanGroup.getDepend_list();
        Long[] apiDependencyIDs = new Long[crmDepend_List.size()];
        int i = 0;
        for (Object obj : crmDepend_List)
        {
            apiDependencyIDs[i] = Long.valueOf((String) obj);
            i++;
        }
        apiPricePlanValidationGroup.setDependencyIDs(apiDependencyIDs);
        Set crmPrereq_List = crmPricePlanGroup.getPrereq_list();
        Long[] apiPrerequisiteIDs = new Long[crmPrereq_List.size()];
        i = 0;
        for (Object obj : crmPrereq_List)
        {
            apiPrerequisiteIDs[i] = Long.valueOf((String) obj);
            i++;
        }
        apiPricePlanValidationGroup.setPrerequisiteIDs(apiPrerequisiteIDs);
        return apiPricePlanValidationGroup;
    }
}
