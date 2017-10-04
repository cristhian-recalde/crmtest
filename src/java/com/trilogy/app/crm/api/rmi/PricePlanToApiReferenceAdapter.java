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
import com.trilogy.app.crm.bean.PricePlan;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Adapter;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.util.crmapi.wsdl.v2_0.types.TechnologyTypeEnum;
import com.trilogy.util.crmapi.wsdl.v3_0.types.serviceandbundle.PricePlanReference;


/**
 * Adapts PricePlan object to API objects.
 * 
 * @author bhupendra.pandey@redknee.com
 */
public class PricePlanToApiReferenceAdapter implements Adapter
{

    public Object adapt(final Context ctx, final Object obj) throws HomeException
    {
        return adaptPricePlanToReference(ctx, (PricePlan) obj);
    }


    public Object unAdapt(final Context ctx, final Object obj) throws HomeException
    {
        throw new UnsupportedOperationException();
    }


    public static PricePlanReference adaptPricePlanToReference(final Context ctx, final PricePlan crmPricePlan)
    {
        PricePlanReference apiPricePlanRef = new PricePlanReference();
        adaptPricePlanToReference(ctx, crmPricePlan, apiPricePlanRef);
        return apiPricePlanRef;
    }
    

    public static PricePlanReference adaptPricePlanToReference(final Context ctx, final PricePlan crmPricePlan,
            PricePlanReference apiPricePlanRef)
    {
        apiPricePlanRef.setCurrentVersion(Long.valueOf(crmPricePlan.getCurrentVersion()));
        apiPricePlanRef.setEnabled(crmPricePlan.getEnabled());
        apiPricePlanRef.setIdentifier(crmPricePlan.getId());
        apiPricePlanRef.setName(crmPricePlan.getName());
        apiPricePlanRef.setPaidtype(RmiApiSupport.convertCrmSubscriberPaidType2Api(crmPricePlan.getPricePlanType()));
        apiPricePlanRef.setSpid(crmPricePlan.getSpid());
        apiPricePlanRef.setSubscriptionLevel(crmPricePlan.getSubscriptionLevel());
        apiPricePlanRef.setSubscriptionType((int) crmPricePlan.getSubscriptionType());
        apiPricePlanRef.setTechnology(TechnologyTypeEnum.valueOf(crmPricePlan.getTechnology().getIndex()));
        return apiPricePlanRef;
    }
}
