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
import com.trilogy.util.crmapi.wsdl.v3_0.types.generalprovisioning.CollectionAgencyReference;
import com.trilogy.util.crmapi.wsdl.v3_0.types.generalprovisioning.SupplementaryData;

/**
 * 
 * @author Marcio Marques
 * @since 9.1.3
 *
 */
public class SupplementaryDataToApiAdapter implements Adapter
{

    /**
     * {@inheritDoc}
     */
    public Object adapt(Context ctx, Object obj) throws HomeException
    {
        SupplementaryData result = null;
        if (obj instanceof com.redknee.app.crm.bean.SupplementaryData)
        {
            com.redknee.app.crm.bean.SupplementaryData supplementaryData = (com.redknee.app.crm.bean.SupplementaryData) obj;
            
            result = new SupplementaryData();
            result.setName(supplementaryData.getKey());
            result.setValue(supplementaryData.getValue());
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