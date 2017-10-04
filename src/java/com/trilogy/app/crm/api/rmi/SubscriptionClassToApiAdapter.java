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
package com.trilogy.app.crm.api.rmi;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Adapter;
import com.trilogy.framework.xhome.home.HomeException;

import com.trilogy.app.crm.api.rmi.support.RmiApiSupport;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.bean.account.SubscriptionClass;
import com.trilogy.util.crmapi.wsdl.v2_0.types.PaidType;
import com.trilogy.util.crmapi.wsdl.v2_0.types.SystemType;
import com.trilogy.util.crmapi.wsdl.v2_0.types.SystemTypeEnum;
import com.trilogy.util.crmapi.wsdl.v2_0.types.TechnologyTypeEnum;
import com.trilogy.util.crmapi.wsdl.v2_1.types.subscription.SubscriptionClassReference;

/**
 * Adapts SubscriptionClass object to API objects.
 *
 * @author aaron.gourley@redknee.com
 * @since 8.2
 */
public class SubscriptionClassToApiAdapter implements Adapter
{   
    public Object adapt(final Context ctx, final Object obj) throws HomeException
    {
        SubscriptionClass subClass = (SubscriptionClass) obj;

        final SubscriptionClassReference reference = new SubscriptionClassReference();
        
        reference.setIdentifier(subClass.getId());
        reference.setName(subClass.getName());
        reference.setDescription(subClass.getDescription());
        reference.setSubscriptionType(Long.valueOf(subClass.getSubscriptionType()).intValue());
        
        SubscriberTypeEnum crmPaidType = SubscriberTypeEnum.get(Integer.valueOf(subClass.getSegmentType()).shortValue());
        PaidType apiPaidType = RmiApiSupport.convertCrmSubscriberPaidType2Api(crmPaidType);
        SystemType apiSystemType = SystemTypeEnum.valueOf(apiPaidType.getValue());
        
        reference.setSystemType(apiSystemType);
        
        reference.setTechnologyType(TechnologyTypeEnum.valueOf(subClass.getTechnologyType()));
        
        return reference;
    }

    public Object unAdapt(final Context ctx, final Object obj) throws HomeException
    {
        throw new UnsupportedOperationException();
    }
}
