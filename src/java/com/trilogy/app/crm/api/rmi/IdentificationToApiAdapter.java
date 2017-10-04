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

import com.trilogy.app.crm.bean.IdFormat;
import com.trilogy.util.crmapi.wsdl.v2_2.types.generalprovisioning.IdentificationFormat;
import com.trilogy.util.crmapi.wsdl.v3_0.types.generalprovisioning.Identification;


/**
 * Adapter to convert CRM identifications to API identifications.
 *
 * @author aaron.gourley@redknee.com
 * @since 8.6
 */
public class IdentificationToApiAdapter implements Adapter
{

    /**
     * {@inheritDoc}
     */
    public Object adapt(Context ctx, Object obj) throws HomeException
    {
        Identification result = null;
        if (obj instanceof com.redknee.app.crm.bean.Identification)
        {
            com.redknee.app.crm.bean.Identification crmId = (com.redknee.app.crm.bean.Identification) obj;
            
            result = new Identification();
            result.setIdentifier((long) crmId.getCode());
            result.setName(crmId.getDesc());
            result.setSpid(crmId.getSpid());
            result.setExample(crmId.getExample());
            result.setMandatoryExpiryDate(Boolean.valueOf(crmId.getMandatoryExpiryDate()));

            IdFormat crmFormat = crmId.getFormat();
            if (crmFormat != null)
            {
                IdentificationFormat format = new IdentificationFormat();
                format.setErrorMessage(crmFormat.getErrorMsg());
                format.setRegex(crmFormat.getRegEx());
                result.setFormat(format);
            }
        }
        return result;
    }


    /**
     * {@inheritDoc}
     */
    public Object unAdapt(Context ctx, Object obj) throws HomeException
    {
        throw new UnsupportedOperationException();
    }

}
