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

import com.trilogy.util.crmapi.wsdl.v2_2.types.generalprovisioning.DiscountClassReference;


/**
 * 
 *
 * @author aaron.gourley@redknee.com
 * @since 8.6
 */
public class DiscountClassToApiReferenceAdapter implements Adapter
{

    /**
     * {@inheritDoc}
     */
    public Object adapt(Context ctx, Object obj) throws HomeException
    {
        DiscountClassReference apiDiscountClassRef = null;
        if (obj instanceof com.redknee.app.crm.bean.DiscountClass)
        {
            com.redknee.app.crm.bean.DiscountClass crmDiscountClass = (com.redknee.app.crm.bean.DiscountClass) obj;
            apiDiscountClassRef = new DiscountClassReference();
            adaptDiscountClassToReference(ctx, crmDiscountClass, apiDiscountClassRef);
        }
        return apiDiscountClassRef;
    }


    public static DiscountClassReference adaptDiscountClassToReference(Context ctx,
            final com.redknee.app.crm.bean.DiscountClass crmDiscountClass,
            final DiscountClassReference apiDiscountClassRef)
    {
        apiDiscountClassRef.setIdentifier(crmDiscountClass.getIdentifier());
        apiDiscountClassRef.setName(crmDiscountClass.getName());
        apiDiscountClassRef.setSpid(crmDiscountClass.getSpid());
        return apiDiscountClassRef;
    }

    /**
     * {@inheritDoc}
     */
    public Object unAdapt(Context ctx, Object obj) throws HomeException
    {
        throw new UnsupportedOperationException();
    }
}
