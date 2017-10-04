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

package com.trilogy.app.crm.web.control;

import java.io.PrintWriter;

import javax.servlet.ServletRequest;

import com.trilogy.framework.xhome.beans.SafetyUtil;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.webcontrol.AbstractWebControl;

import com.trilogy.app.crm.bean.AdditionalMsisdnBean;


/**
 * Web control for additional MSISDN selection.
 *
 * @author cindy.wong@redknee.com
 * @since Aug 22, 2007
 */
public class AvailableAdditionalMsisdnWebControl extends AvailMsisdnWebControl
{

    /**
     * Context key for actual display mode of the subscriber.
     */
    protected static final String REAL_MODE_KEY = "REALMODE";


    /**
     * Create a new instance of <code>AvailableAdditionalMsisdnWebControl</code>.
     */
    public AvailableAdditionalMsisdnWebControl()
    {
        super(false);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void toWeb(final Context ctx, final PrintWriter out, final String name, final Object obj)
    {
        final AdditionalMsisdnBean bean = (AdditionalMsisdnBean) ctx.get(AbstractWebControl.BEAN);
        final Context subContext = ctx.createSubContext();
        subContext.setName(AvailableAdditionalMsisdnWebControl.class.getSimpleName());
        final int group = bean.getAMsisdnGroup();
        final String msisdn = bean.getAMsisdn();
        subContext.put(AvailMsisdnWebControl.MSISDN_GROUP_KEY, group);
        subContext.put(AvailMsisdnWebControl.MSISDN_KEY, msisdn);

        /*
         * [Cindy] 2007-10-09: Original MSISDN of the
         */
        subContext.put(AvailMsisdnWebControl.ORIGINAL_MSISDN_KEY, bean.getOriginalMsisdn());
        String displayObject = (String) obj;
        if (displayObject == null || SafetyUtil.safeEquals(displayObject, ""))
        {
            displayObject = bean.getOriginalMsisdn();
        }

        /*
         * TT 7082800048: Use subscriber's real mode for display.
         */
        final Integer modeObj = (Integer) ctx.get(REAL_MODE_KEY);
        int mode = Integer.MIN_VALUE;
        if (modeObj != null)
        {
            mode = modeObj.intValue();
        }
        else
        {
            mode = ctx.getInt(REAL_MODE_KEY, Integer.MIN_VALUE);
        }
        if (mode != Integer.MIN_VALUE)
        {
            subContext.put("MODE", mode);
        }

        super.toWeb(subContext, out, name, displayObject);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized Object fromWeb(final Context ctx, final ServletRequest req, final String name)
    {
        final Context subContext = ctx.createSubContext();
        subContext.setName(AvailableAdditionalMsisdnWebControl.class.getSimpleName());
        final String msisdn = (String) super.fromWeb(subContext, req, name);
        final AdditionalMsisdnBean bean = (AdditionalMsisdnBean) ctx.get(AbstractWebControl.BEAN);
        bean.setAMsisdn((String) subContext.get(AvailMsisdnWebControl.MSISDN_KEY, msisdn));
        bean.setAMsisdnGroup(subContext.getInt(AvailMsisdnWebControl.MSISDN_GROUP_KEY));
        return msisdn;
    }
}
