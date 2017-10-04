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

import com.trilogy.util.crmapi.wsdl.v3_0.types.generalprovisioning.IdentificationReference;

import com.trilogy.app.crm.bean.Identification;


/**
 * Adapter to convert CRM identifications to API identification references.
 *
 * @author aaron.gourley@redknee.com
 * @since 8.6
 */
public class IdentificationToApiReferenceAdapter implements Adapter
{

    /**
     * {@inheritDoc}
     */
    @Override
    public Object adapt(Context ctx, Object obj) throws HomeException
    {
        IdentificationReference result = null;
        if (obj instanceof Identification)
        {
            Identification crmId = (Identification) obj;
            
            result = new IdentificationReference();
            result.setIdentifier((long) crmId.getCode());
            result.setName(crmId.getDesc());
            result.setSpid(crmId.getSpid());
			result.setMandatoryExpiryDate(crmId.isMandatoryExpiryDate());
        }
        return result;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Object unAdapt(Context ctx, Object obj) throws HomeException
    {
        throw new UnsupportedOperationException();
    }

}
