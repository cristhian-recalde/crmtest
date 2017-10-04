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
package com.trilogy.app.crm.provision.gateway;

import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.provision.CommonProvisionAgentBase;
import com.trilogy.app.crm.provision.service.param.ProvisionEntityType;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;


/**
 * 
 *
 * @author victor.stratan@redknee.com
 * @since 
 */
public class SubscriberServiceProvisionGatewayHome extends HomeProxy
{
    /**
     * For Serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * @param delegate
     */
    public SubscriberServiceProvisionGatewayHome(final Home delegate)
    {
        super(delegate);
    }


    @Override
    public Object store(final Context ctx, final Object obj) throws HomeException
    {
        // here we update spg type service and aux service seperatedly. 
        // it is temporary measure before we complete the provisioning refactoring. 
        // in future, there is not such update, since update should convert to different
        // provisioing commands. However, right now, provision command only support 
        // HLR. update is still use by VM in case msisdn change. 
        
        final Subscriber result = (Subscriber) super.store(ctx, obj);
        Context subCtx = ctx.createSubContext(); 
        subCtx.put(CommonProvisionAgentBase.SPG_PROVISIONING_CUSTOM_PROVISION_ENTITY_TYPE, 
                String.valueOf(ProvisionEntityType.PROVISION_ENTITY_TYPE_SERVICE));

        ServiceProvisioningGatewaySupport.updateServices(subCtx, result);
        
        subCtx = ctx.createSubContext(); 
        subCtx.put(CommonProvisionAgentBase.SPG_PROVISIONING_CUSTOM_PROVISION_ENTITY_TYPE, 
                String.valueOf(ProvisionEntityType.PROVISION_ENTITY_TYPE_AUX_SERVICE));

        ServiceProvisioningGatewaySupport.updateAuxiliaryServices(subCtx, result);
        return result;
    }


}
