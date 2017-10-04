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
package com.trilogy.app.crm.api.queryexecutor;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.util.crmapi.wsdl.v3_0.api.CRMExceptionFault;
import com.trilogy.util.crmapi.wsdl.v3_0.types.subscription.SubscriptionReference;


/**
 * 
 * @author Marcio Marques
 * @since 9.1.3
 *
 */
public class SubscriptionReferenceEventRecordGeneratorQueryExecutor<T extends Object> extends AbstractSubscriptionReferenceEventRecordGeneratorQueryExecutor
{
    public SubscriptionReferenceEventRecordGeneratorQueryExecutor()
    {
        super();
    }

    public SubscriptionReferenceEventRecordGeneratorQueryExecutor(int identifierProperty, String method, QueryExecutor<T> delegate, int spidProperty)
    {
        super(identifierProperty, method, delegate, spidProperty);
    }
    
    protected String getIdentifier(Context ctx, Object... parameters) throws CRMExceptionFault
    {
        SubscriptionReference reference = (SubscriptionReference) getParameters(ctx,parameters)[getIdentifierProperty()-1];
        if (reference.getIdentifier()!=null && !reference.getIdentifier().isEmpty())
        {
            return reference.getIdentifier();
        }
        else 
        {
            return reference.getMobileNumber();
        }
    }

    protected int getSpid(Context ctx, Object... parameters) throws CRMExceptionFault
    {
        SubscriptionReference reference = (SubscriptionReference) getParameters(ctx, parameters)[getIdentifierProperty()-1];
        if (reference.getSpid()!=null)
        {
            return reference.getSpid();
        }
        else 
        {
            return -1;
        }
    }

    @Override
    public boolean validateParameterTypes(Class[] parameterTypes)
    {
        boolean result = true;
        result = result && SubscriptionReference.class.isAssignableFrom(parameterTypes[getIdentifierProperty()-1]);
        return result && getDelegate().validateParameterTypes(parameterTypes);
    }
}
