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
import com.trilogy.app.crm.bean.SubscriberServices;
import com.trilogy.app.crm.support.ServiceSupport;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAwareSupport;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.LogSupport;

/**
 * @author rchen
 *
 */
final class SubscriberServiceComparatorWithExecutionOrder extends ContextAwareSupport implements Comparator<SubscriberServices>
{
    SubscriberServiceComparatorWithExecutionOrder(Context ctx)
    {
        context_ = ctx;
    }

    public int compare(SubscriberServices ss1, SubscriberServices ss2)
    {
        
        int result = 0;
        long svcId1 = ss1.getServiceId();
        long svcId2 = ss2.getServiceId();
        Context ctx = getContext();
        
        try
        {
            Service svc1 = ServiceSupport.getService(ctx, svcId1);
            Service svc2 = ServiceSupport.getService(ctx, svcId2);
            result = Service.PROVISIONING_ORDER.compare(svc1, svc2);
        }
        catch (HomeException e)
        {
            LogSupport.minor(ctx, this, "Exception detected while comaring SubscriberServices.", e);
        }
        
        return result ;
    }

}
