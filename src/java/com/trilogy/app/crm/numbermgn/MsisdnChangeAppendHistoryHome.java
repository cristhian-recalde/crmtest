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

package com.trilogy.app.crm.numbermgn;

import java.util.Date;

import com.trilogy.framework.xhome.beans.SafetyUtil;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;

import com.trilogy.app.crm.bean.Msisdn;
import com.trilogy.app.crm.bean.MsisdnStateEnum;


/**
 * This home decorator creates history items of MSISDN changes.
 *
 * @author candy.wong@redknee.com
 * @author paul.sperneac@redknee.com
 */
public class MsisdnChangeAppendHistoryHome extends AppendNumberMgmtHistoryHome
{

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 1L;


    /**
     * Create a new instance of <code>MsisdnChangeAppendHistoryHome</code>.
     *
     * @param ctx
     *            The operating context.
     * @param delegate
     *            Delegate of this home.
     */
    public MsisdnChangeAppendHistoryHome(final Context ctx, final Home delegate)
    {
        super(ctx, delegate, MsisdnMgmtHistoryHome.class);
    }


    /**
     * Create a new instance of <code>MsisdnChangeAppendHistoryHome</code>.
     *
     * @param delegate
     *            Delegate of this home.
     */
    public MsisdnChangeAppendHistoryHome(final Home delegate)
    {
        super(delegate, MsisdnMgmtHistoryHome.class);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Object store(final Context ctx, final Object obj) throws HomeException, HomeInternalException
    {
        final Msisdn oldBean = (Msisdn) find(ctx, obj);
        final Msisdn newBean = (Msisdn) obj;

        if (oldBean != null && newBean != null)
        {
            if (!SafetyUtil.safeEquals(oldBean.getState(), newBean.getState()))
            {
                if (MsisdnStateEnum.IN_USE.equals(newBean.getState()))
                {
                    // need to set the startTimestamp because this indicates the effective
                    // date
                	Date effectiveDate = (Date) ctx.get(MSISDN_EFFECTIVE_DATE,null);
                	if(effectiveDate == null || effectiveDate.after(new Date()))
                	{
                		newBean.setStartTimestamp(newBean.getLastModified());
                	}
                	else
                	{
                		newBean.setStartTimestamp(effectiveDate);
                	}
                }
            }
        }
        final Object result = super.store(ctx, newBean);

        return result;
    }
    
    public static final String MSISDN_EFFECTIVE_DATE = "MsisdnEffectiveDate";
}
