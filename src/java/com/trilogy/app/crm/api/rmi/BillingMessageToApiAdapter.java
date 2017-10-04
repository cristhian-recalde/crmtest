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

import com.trilogy.app.crm.bean.BillingMessage;
import com.trilogy.util.crmapi.wsdl.v2_0.types.BillingMessageStateEnum;

/**
 * Adapts Billing Message object to API objects.
 *
 * @author aaron.gourley@redknee.com
 */
public class BillingMessageToApiAdapter implements Adapter
{
    public Object adapt(final Context ctx, final Object obj) throws HomeException
    {
        return adaptBillingMessageToReference((BillingMessage) obj);
    }

    public Object unAdapt(final Context ctx, final Object obj) throws HomeException
    {
        throw new UnsupportedOperationException();
    }

    public static com.redknee.util.crmapi.wsdl.v2_0.types.BillingMessageReference adaptBillingMessageToReference(final BillingMessage billingMessage)
    {
        final com.redknee.util.crmapi.wsdl.v2_0.types.BillingMessageReference reference = new com.redknee.util.crmapi.wsdl.v2_0.types.BillingMessageReference();
        adaptBillingMessageToReference(billingMessage, reference);

        return reference;
    }

    public static com.redknee.util.crmapi.wsdl.v2_0.types.BillingMessageReference adaptBillingMessageToReference(final BillingMessage billingMessage,
            final com.redknee.util.crmapi.wsdl.v2_0.types.BillingMessageReference reference)
    {
        reference.setIdentifier(billingMessage.getIdentifier());
        reference.setLanguage(billingMessage.getLanguage());
        reference.setMessage(billingMessage.getMessage());
        reference.setSpid(billingMessage.getSpid());
        
        if (billingMessage.isActive())
        {
            reference.setState(BillingMessageStateEnum.ACTIVE.getValue());
        }
        else
        {
            reference.setState(BillingMessageStateEnum.INACTIVE.getValue());   
        }

        return reference;
    }
    
    public static BillingMessage adaptReferenceToBillingMessage(com.redknee.util.crmapi.wsdl.v2_0.types.BillingMessageReference apiBillingMessageRef)
    {
        BillingMessage crmBillingMessage = new BillingMessage();
        crmBillingMessage.setIdentifier(apiBillingMessageRef.getIdentifier());
        crmBillingMessage.setLanguage(apiBillingMessageRef.getLanguage());
        crmBillingMessage.setMessage(apiBillingMessageRef.getMessage());
        crmBillingMessage.setSpid(apiBillingMessageRef.getSpid());
        if(apiBillingMessageRef.getState().equals(BillingMessageStateEnum.ACTIVE))
        {
            crmBillingMessage.setActive(true);
        }
        else
        {
            crmBillingMessage.setActive(false);
        }
        return crmBillingMessage;
    }
}
