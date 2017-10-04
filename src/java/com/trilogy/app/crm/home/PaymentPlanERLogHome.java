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
package com.trilogy.app.crm.home;

import com.trilogy.framework.xhome.beans.xi.PropertyInfo;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;

import com.trilogy.app.crm.bean.payment.PaymentPlan;
import com.trilogy.app.crm.bean.payment.PaymentPlanHome;
import com.trilogy.app.crm.bean.payment.PaymentPlanXInfo;
import com.trilogy.app.crm.xhome.home.SimpleBeanERHome;

public class PaymentPlanERLogHome extends SimpleBeanERHome 
{
    public PaymentPlanERLogHome(final Home delegate)
    {
        super(delegate, IDENTIFIER, CLASS, TITLE, FIELDS);
    }

    private static final int IDENTIFIER = 1119;
    private static final int CLASS = 700;
    private static final String TITLE = "Payment Plan Management";
    
    private static final PropertyInfo[] FIELDS =
    {
        PaymentPlanXInfo.SPID,
        PaymentPlanXInfo.ID,
        PaymentPlanXInfo.NAME,
        PaymentPlanXInfo.DESC,
        PaymentPlanXInfo.NUM_OF_PAYMENTS,
        PaymentPlanXInfo.CREDIT_LIMIT_DECREASE,
        PaymentPlanXInfo.STATE
    };

    protected Object getOriginal(final Context context, final Object object) throws HomeException
    {
        final PaymentPlan newBean = (PaymentPlan)object;

        final Home home = (Home)context.get(PaymentPlanHome.class);

        return home.find(context, Long.valueOf(newBean.getId()));
    }

}
