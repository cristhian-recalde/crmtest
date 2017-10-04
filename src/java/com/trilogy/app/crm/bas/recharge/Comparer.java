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
package com.trilogy.app.crm.bas.recharge;

import java.util.Comparator;
import java.io.Serializable;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAwareSupport;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.LogSupport;

import com.trilogy.app.crm.bean.Service;
import com.trilogy.app.crm.bean.core.ServiceFee2;
import com.trilogy.app.crm.support.ServiceSupport;

/**
 * @author amit.baid@redknee.com
 * @author victor.stratan@redknee.com
 */
public class Comparer extends ContextAwareSupport implements Comparator, Serializable
{

    public Comparer(Context ctx)
    {
        this.setContext(ctx);
    }

    public int compare(Object obj1, Object obj2)
    {
        Service weeklyService1 = null;
        Service weeklyService2 = null;
        try
        {
            weeklyService1 = ServiceSupport.getService(getContext(), ((ServiceFee2) obj1).getServiceId());
            weeklyService2 = ServiceSupport.getService(getContext(), ((ServiceFee2) obj2).getServiceId());
        }
        catch (HomeException e)
        {
            LogSupport.minor(getContext(), this, "Unable to obtain or cast service ", e);
            return 0;
        }


        if (weeklyService1.getExecutionOrder() > weeklyService2.getExecutionOrder())
        {
            return 1;
        }

        if (weeklyService1.getExecutionOrder() < weeklyService2.getExecutionOrder())
        {
            return -1;
        }

        // Needed as a way to consistently break ties
        return weeklyService1.compareTo(weeklyService2);
    }

    public Context getContext()
    {
        return super.getContext();
    }

    public void setContext(Context arg0)
    {
        super.setContext(arg0);
    }

}
