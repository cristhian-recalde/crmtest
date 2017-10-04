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
package com.trilogy.app.crm.home.sub;

import com.trilogy.framework.xhome.beans.ExceptionListener;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xlog.log.MinorLogMsg;

import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.ff.FFEcareException;
import com.trilogy.app.crm.ff.PersonalListPlanSupport;

/**
 * Provides a mechanism to update the subscriber's list of MSISDNs in their PersonalListPlan.
 *
 * @author gary.anderson@redknee.com
 */
public class PersonalListPlanUpdateHome extends HomeProxy
{
    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Creates a new PersonalListPlanUpdateHome.
     *
     * @param delegate
     *            The Home to which we delegate.
     */
    public PersonalListPlanUpdateHome(final Home delegate)
    {
        super(delegate);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object store(final Context ctx, final Object obj) throws HomeException
    {
        try
        {
            PersonalListPlanSupport.updatePersonalListPlanMSISDNs(ctx, (Subscriber) obj);
        }
        catch (FFEcareException e)
        {
            final ExceptionListener el = (ExceptionListener) ctx.get(ExceptionListener.class);
            if (el != null)
            {
                el.thrown(new Exception(e.getMessage()));
            }
            new MinorLogMsg(this, e.getMessage(), e).log(ctx);
        }
        return super.store(ctx, obj);
    }

} // class
