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

import java.util.Set;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Adapter;
import com.trilogy.framework.xhome.home.HomeException;

import com.trilogy.util.crmapi.wsdl.v2_2.types.generalprovisioning.IdentificationGroup;


/**
 * Adapter to convert CRM identification groups to API identification groups.
 *
 * @author aaron.gourley@redknee.com
 * @since 8.6
 */
public class IdentificationGroupToApiAdapter implements Adapter
{

    /**
     * {@inheritDoc}
     */
    public Object adapt(Context ctx, Object obj) throws HomeException
    {
        IdentificationGroup result = null;
        if (obj instanceof com.redknee.app.crm.bean.IdentificationGroup)
        {
            com.redknee.app.crm.bean.IdentificationGroup crmGroup = (com.redknee.app.crm.bean.IdentificationGroup) obj;
            result = new IdentificationGroup();
            result.setIdentifier((long) crmGroup.getIdGroup());
            result.setName(crmGroup.getName());
            result.setAcceptAny(crmGroup.isAcceptAny());
            result.setRequiredNumber(crmGroup.getRequiredNumber());
            
            Set<String> identifications = crmGroup.getIdentificationList();
            if (identifications != null)
            {
                Long[] apiIdentifications = new Long[identifications.size()];
                int i=0;
                for (String identification : identifications)
                {
                    apiIdentifications[i++] = Long.valueOf(identification);
                }
                result.setIdentificationTypeID(apiIdentifications);
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
