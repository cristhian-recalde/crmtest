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

import com.trilogy.app.crm.bean.ChargingTemplate;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Adapter;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.util.crmapi.wsdl.v2_2.types.generalprovisioning.ChargingTemplateReference;
import com.trilogy.util.crmapi.wsdl.v2_2.types.generalprovisioning.ChargingTemplateStateTypeEnum;

/**
 * Adapts charging template object to API objects.
 *
 * @author Marcio Marques
 * @since 8.5
 * 
 */
public class ChargingTemplateToApiAdapter implements Adapter
{
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public Object adapt(final Context ctx, final Object obj) throws HomeException
    {
        return adaptChargingTemplateToReference((ChargingTemplate) obj);
    }

    public Object unAdapt(final Context ctx, final Object obj) throws HomeException
    {
        throw new UnsupportedOperationException();
    }

    public static ChargingTemplateReference adaptChargingTemplateToReference(final ChargingTemplate chargingTemplate)
    {
        final ChargingTemplateReference reference = new ChargingTemplateReference();
        adaptChargingTemplateToReference(chargingTemplate, reference);

        return reference;
    }

    public static ChargingTemplateReference adaptChargingTemplateToReference(final ChargingTemplate chargingTemplate, final ChargingTemplateReference reference)
    {
        reference.setIdentifier(chargingTemplate.getIdentifier());
        reference.setSpid(chargingTemplate.getSpid());
        reference.setName(chargingTemplate.getName());
        reference.setDescription(chargingTemplate.getDescription());
        if (chargingTemplate.isEnabled())
        {
            reference.setState(ChargingTemplateStateTypeEnum.ACTIVE.getValue());
        }
        else
        {
            reference.setState(ChargingTemplateStateTypeEnum.DEPRECATED.getValue());
        }

        return reference;
    }
}
