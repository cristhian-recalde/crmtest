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
package com.trilogy.app.crm.subscriber.validator;

import com.trilogy.framework.xhome.beans.Validator;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;

import com.trilogy.app.crm.bean.DealerCode;
import com.trilogy.app.crm.bean.DealerCodeHome;
import com.trilogy.app.crm.bean.DealerCodeXInfo;
import com.trilogy.app.crm.bean.Subscriber;

/**
 * TODO this dealer code validator is operating on a field that is updated later by SubscriberPackageDealerCodeAdaptor
 * Checks that the dealer code in a subscriber exists in the dealercode home.
 *
 * @author paul.sperneac@redknee.com
 */
public final class DealerCodeValidator implements Validator
{
    private static final DealerCodeValidator INSTANCE = new DealerCodeValidator();

    public static DealerCodeValidator instance()
    {
        return INSTANCE;
    }

    /**
     * Prevents initialization. Use singleton.
     */
    private DealerCodeValidator()
    {
    }

    public void validate(final Context ctx, final Object obj) throws IllegalStateException
    {
        if (obj == null)
        {
            return;
        }

        final Subscriber sub = (Subscriber) obj;

        final Home dlHome = (Home) ctx.get(DealerCodeHome.class);

        if (dlHome == null)
        {
            throw new IllegalStateException("Cannot check dealer code because cannot find DealerCode home in context");
        }

        try
        {
            if (sub.getDealerCode() != null && sub.getDealerCode().trim().length() > 0)
            {
                And and = new And();
                and.add(new EQ(DealerCodeXInfo.CODE, sub.getDealerCode()));
                and.add(new EQ(DealerCodeXInfo.SPID, Integer.valueOf(sub.getSpid())));
                final DealerCode dl = (DealerCode) dlHome.find(ctx, and);

                if (dl == null)
                {
                    throw new IllegalStateException("Cannot find dealer code: " + sub.getDealerCode());
                }
                else if (dl.getSpid() != sub.getSpid())
                {
                    throw new IllegalStateException("Dealer code with wrong spid: " + sub.getDealerCode());
                }
            }
        }
        catch (HomeException e)
        {
            final String msg = "Cannot find dealercode: '" + sub.getDealerCode() + "' for subscriber " + sub.getId();
            if (LogSupport.isDebugEnabled(ctx))
            {
                new DebugLogMsg(this, msg, e).log(ctx);
            }

            throw new IllegalStateException(msg, e);
        }
    }

}
