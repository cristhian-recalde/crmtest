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

import com.trilogy.app.crm.bas.tps.ServiceActivationTemplate;
import com.trilogy.util.crmapi.wsdl.v2_0.types.PoolLimitStrategyEnum;
import com.trilogy.util.crmapi.wsdl.v2_0.types.TechnologyTypeEnum;
import com.trilogy.util.crmapi.wsdl.v2_1.types.subscription.SubscriptionCreationTemplateReference;

/**
 * Adapts ServiceActivationTemplate object to API objects.
 *
 * @author victor.stratan@redknee.com
 */
public class SubscriberCreationTemplateToApiAdapter implements Adapter
{
    public Object adapt(final Context ctx, final Object obj) throws HomeException
    {
        ServiceActivationTemplate template = (ServiceActivationTemplate) obj;
        
        final SubscriptionCreationTemplateReference reference = new SubscriptionCreationTemplateReference();
        
        reference.setIdentifier(template.getIdentifier());
        reference.setSpid(template.getSpid());
        reference.setName(template.getName());
        reference.setTechnologyType(TechnologyTypeEnum.valueOf(template.getTechnology().getIndex()));
        reference.setSubscriptionClass(template.getSubscriptionClass());
        reference.setPoolLimitStrategy(PoolLimitStrategyEnum.valueOf(template.getQuotaType().getIndex()));
        reference.setPoolLimit(Long.valueOf(template.getQuotaLimit()));

        return reference;
    }

    public Object unAdapt(final Context ctx, final Object obj) throws HomeException
    {
        throw new UnsupportedOperationException();
    }
}
