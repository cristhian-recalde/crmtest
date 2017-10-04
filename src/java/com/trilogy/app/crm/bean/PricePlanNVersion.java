/*
 * Created on Jan 27, 2005
 *
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
package com.trilogy.app.crm.bean;

import java.util.Collection;
import java.util.Map;



/**
 * @author psperneac
 */
public class PricePlanNVersion extends AbstractPricePlanNVersion implements Comparable, com.redknee.framework.xhome.beans.Child
{
    @Override
    public Object getParent()
    {
        return Long.valueOf(getId());
    }

    @Override
    public void setParent(Object parent)
    {
        setId(((Long) parent).longValue());
    }

    @Override
    public int compareTo(Object obj)
    {
        PricePlanVersion other = (PricePlanVersion) obj;

        if (getId() != other.getId())
        {
            return (int)(getId() - other.getId());
        }

        return getVersion() - other.getVersion();
    }
    
    
    @Override
    public int getCreditCardTopUpFrequency()
    {
        Map serviceMap = getServicePackageVersion().getServiceFees();
        if(serviceMap != null && !serviceMap.isEmpty())
        {
            Collection<ServiceFee2N> services = serviceMap.values();

            for (ServiceFee2N fee : services)
            {
                if (fee.isPrimary())
                {
                    return CreditCardTopUpFrequencyEnum.PRIMARY_SERVICE_RECURRENCE_INDEX;
                }
            }
        }
        
        return CreditCardTopUpFrequencyEnum.BILL_CYCLE_INDEX;
    }
    
}
