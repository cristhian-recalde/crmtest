/* 
 * This code is a protected work and subject to domestic and international
 * copyright law(s). A complete listing of authors of this work is readily 
 * available. Additionally, source code is, by its very nature, confidential 
 * information and inextricably contains trade secrets and other information 
 * proprietary, valuable and sensitive to Redknee, no unauthorised use, 
 * disclosure, manipulation or otherwise is permitted, and may only be used 
 * in accordance with the terms of the licence agreement entered into with 
 * Redknee Inc. and/or its subsidiaries. 
 * 
 * Copyright � Redknee Inc. and its subsidiaries. All Rights Reserved. 
 */
package com.trilogy.app.crm.priceplan;

import java.util.Comparator;

import com.trilogy.app.crm.bean.Service;
import com.trilogy.app.crm.bean.core.ServiceFee2;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAwareSupport;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.LogSupport;

/**
 * @author Marcio Marques
 *
 */
public class ServiceFee2ExecutionOrderComparator extends ContextAwareSupport implements Comparator<ServiceFee2>
{
    public ServiceFee2ExecutionOrderComparator(Context ctx, boolean provisioning)
    {
        context_ = ctx;
        provisioning_ = provisioning;
    }

    public int compare(ServiceFee2 o1, ServiceFee2 o2)
    {
        boolean defaultCases = true;
        
        int result = 0;
        try
        {
            
            if (o1 == null)
            {
                result = -1;
            }
            else if (o2 == null)
            {
                result = 1;
            }
            else if (o1.getServiceId() == o2.getServiceId())
            {
                result = 0;
            }
            else
            {
                Service s1 = o1.getService(getContext());
                Service s2 = o2.getService(getContext());
                
                if (s1 == null)
                {
                    result = -1;
                }
                else if (s2 == null)
                {
                    result = 1;
                }
                else
                {
                    defaultCases = false;
                    if (provisioning_)
                    {
                        result = Service.PROVISIONING_ORDER.compare(s1, s2);
                    }
                    else
                    {
                        result = Service.UNPROVISIONING_ORDER.compare(s1, s2);
                    }
                    
                }
            }
            
        }
        catch (HomeException e)
        {
            LogSupport.minor(getContext(), this, "Unable to retrieve one of the services being compared: Service1 = "
                    + o1.getServiceId() + ", Service2 = " + o2.getServiceId() + "Exception = " + e.getMessage(), e);
        }

        if (!provisioning_ && defaultCases)
        {
            result = -result;
        }
        
        return result;
    }
    
    private boolean provisioning_;

}
