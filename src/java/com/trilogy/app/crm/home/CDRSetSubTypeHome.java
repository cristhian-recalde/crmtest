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

import java.util.Date;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;

import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.bean.calldetail.CallDetail;
import com.trilogy.app.crm.support.CallDetailSupportHelper;

/**
 * This Home decorator sets the SubscriberType property in the CallDetail bean.
 *
 * @author daniel.zhang@redknee.com
 */
public class CDRSetSubTypeHome extends SetSubTypeHome
{
    /**
     * Constructor.
     * @param delegate the home decorator to delegate to
     */
    public CDRSetSubTypeHome(final Home delegate)
    {
        super(delegate);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setSubType(final Context ctx, final Object obj)
    {
        if (obj != null)
        {
            if (obj instanceof CallDetail)
            {
                final CallDetail cdr = (CallDetail) obj;
                final String ban = cdr.getBAN();
                final String msisdn = cdr.getChargedMSISDN();
                final Date date = cdr.getTranDate();
                final SubscriberTypeEnum st = getSubscriberType(ctx, ban, msisdn, date);
                if (st != null)
                {
                    cdr.setSubscriberType(st);
                    CallDetailSupportHelper.get(ctx).debugMsg(CDRSetSubTypeHome.class, cdr, " Setting Subscriber type " + st, ctx);
                }
                
            }
        }
    }
}
