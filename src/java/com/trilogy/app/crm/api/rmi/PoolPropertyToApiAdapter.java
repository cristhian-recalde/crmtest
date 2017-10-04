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

import com.trilogy.app.crm.bean.ServiceProvisionStatusEnum;
import com.trilogy.app.crm.extension.account.SubscriptionPoolProperty;
import com.trilogy.util.crmapi.wsdl.v2_0.types.ProvisioningStateType;
import com.trilogy.util.crmapi.wsdl.v2_0.types.ProvisioningStateTypeEnum;

/**
 * Adapts SubscriptionPoolProperty object to API objects.
 *
 * @author aaron.gourley@redknee.com
 * @since 8.2
 */
public class PoolPropertyToApiAdapter implements Adapter
{   
    public Object adapt(final Context ctx, final Object obj) throws HomeException
    {
        SubscriptionPoolProperty property = (SubscriptionPoolProperty) obj;

        final com.redknee.util.crmapi.wsdl.v2_2.types.account.extensions.SubscriptionPoolProperty apiProperty = new com.redknee.util.crmapi.wsdl.v2_2.types.account.extensions.SubscriptionPoolProperty();
        
        apiProperty.setInitialPoolBalance(property.getInitialPoolBalance());
        apiProperty.setProvisioned(adaptProvisionState(property.getProvisioned()));
        apiProperty.setSubscriptionType(Long.valueOf(property.getSubscriptionType()).intValue());
        
        return apiProperty;
    }

    private ProvisioningStateType adaptProvisionState(int serviceProvisionStateTypeIdx)
    {
        switch (serviceProvisionStateTypeIdx)
        {
        case ServiceProvisionStatusEnum.PROVISIONED_INDEX:
            return ProvisioningStateTypeEnum.PROVISIONED.getValue();
        case ServiceProvisionStatusEnum.PROVISIONINGFAILED_INDEX:
            return ProvisioningStateTypeEnum.PROVISIONED_WITH_ERRORS.getValue();
        case ServiceProvisionStatusEnum.SUSPENDED_INDEX:
            return ProvisioningStateTypeEnum.SUSPENDED.getValue();
        case ServiceProvisionStatusEnum.SUSPENDEDDUETOCLCT_INDEX:
            return ProvisioningStateTypeEnum.SUSPENDED_CLTC.getValue();
        case ServiceProvisionStatusEnum.SUSPENDEDDUETOCLCTWITHERRORS_INDEX:
            return ProvisioningStateTypeEnum.SUSPENDED_CLTC_WITH_ERRORS.getValue();
        case ServiceProvisionStatusEnum.SUSPENDEDWITHERRORS_INDEX:
            return ProvisioningStateTypeEnum.SUSPENDED_WITH_ERRORS.getValue();
        case ServiceProvisionStatusEnum.UNPROVISIONEDOK_INDEX:
            return ProvisioningStateTypeEnum.UNPROVISIONED.getValue();
        case ServiceProvisionStatusEnum.UNPROVISIONINGFAILED_INDEX:
            return ProvisioningStateTypeEnum.UNPROVISIONED_WITH_ERRORS.getValue();
        }
        return ProvisioningStateTypeEnum.NOT_PROVISIONED.getValue();
    }

    public Object unAdapt(final Context ctx, final Object obj) throws HomeException
    {
        throw new UnsupportedOperationException();
    }
}
