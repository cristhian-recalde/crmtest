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

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.msp.MSP;
import com.trilogy.framework.xhome.msp.Spid;
import com.trilogy.framework.xhome.webcontrol.AbstractWebControl;
import com.trilogy.framework.xhome.webcontrol.KeyWebControlOptionalValue;
import com.trilogy.framework.xhome.webcontrol.LongWebControl;
import com.trilogy.framework.xhome.webcontrol.WebControl;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.MajorLogMsg;

import com.trilogy.app.crm.bean.PricePlan;
import com.trilogy.app.crm.bean.PricePlanVersion;
import com.trilogy.app.crm.bean.ipc.IpcRatePlan;
import com.trilogy.app.crm.bean.ipc.IpcRatePlanID;
import com.trilogy.app.crm.bean.ipc.IpcRatePlanIdentitySupport;
import com.trilogy.app.crm.bean.ipc.IpcRatePlanKeyWebControl;
import com.trilogy.app.crm.filter.SpidPredicate;
import com.trilogy.app.crm.support.IpcgClientSupport;
import com.trilogy.app.crm.support.PricePlanSupport;

public class CustomerizeIPCGRatePlanKeyWebControl implements WebControl
{
    public static int WILDCARD_VALUE = -1;

    // TODO when Framework bug 9011600001 is fixed use IpcRatePlanID class directly
    private static final Object OPTIONAL_KEY = new IdentityIdIpcRatePlanID(-1, -1);
    private static final Object KEY_CONTROL = new KeyWebControlOptionalValue(IpcRatePlan.DEFAULT_DESCRIPTION, OPTIONAL_KEY);

    protected IpcRatePlanKeyWebControl ipcg_delegate_ = new IpcRatePlanKeyWebControl(1, true, KEY_CONTROL);
    protected WebControl crm_delegate_ = new LongWebControl(20);

    /**
     */
    public CustomerizeIPCGRatePlanKeyWebControl()
    {
    }

    // //////////////////////////////////////////////////////////////// Impl
    // WebControl

    public void fromWeb(Context ctx, Object p1, javax.servlet.ServletRequest p2, String p3)
    {
        if (IpcgClientSupport.pricePlanSupportsUrcsDataRatePlan(ctx))
        {
            ipcg_delegate_.fromWeb(ctx, p1, p2, p3);
        }
        else
        {
            crm_delegate_.fromWeb(ctx, p1, p2, p3);
        }
    }

    public Object fromWeb(Context ctx, javax.servlet.ServletRequest p1, String p2)
    {
        if (IpcgClientSupport.pricePlanSupportsUrcsDataRatePlan(ctx))
        {
            return Integer.valueOf(((IpcRatePlanID) ipcg_delegate_.fromWeb(ctx, p1, p2)).getRatePlanId());
        }
        else
        {
            return crm_delegate_.fromWeb(ctx, p1, p2);
        }
    }

    public void toWeb(Context ctx, java.io.PrintWriter p1, String p2, Object p3)
    {
        if (IpcgClientSupport.pricePlanSupportsUrcsDataRatePlan(ctx))
        {
        	Spid spidBean = MSP.getBeanSpid(ctx);
        	int spid = IpcRatePlan.DEFAULT_SPID;
        	if (spidBean != null)
        	{
        		spid = spidBean.getId();
        	}
        	else
        	{
        		new DebugLogMsg(this, "Unable to retrieve SPID from bean.  Will assume it is unset and use -1.", null).log(ctx);
        	}

            // filter the RatePlans by the SPID
            ipcg_delegate_.setSelectFilter(new SpidPredicate(spid));

            final int ratePlan = ((Integer) p3).intValue();
            if (ratePlan == IpcRatePlan.DEFAULT_RATEPLANID)
            {
            	// if the RatePlan hasn't been set we output the Optional Value
            	ipcg_delegate_.toWeb(ctx, p1, p2, OPTIONAL_KEY);
            }
            else
            {
            	ipcg_delegate_.toWeb(ctx, p1, p2, new IpcRatePlanID(ratePlan, spid));
            }

        }
        else
            crm_delegate_.toWeb(ctx, p1, p2, p3);
    }

}

class IdentityIdIpcRatePlanID extends IpcRatePlanID
{
    public IdentityIdIpcRatePlanID(final int ratePlanId, final int spid)
    {
        super(ratePlanId, spid);
    }

    public String toString()
    {
        return IpcRatePlanIdentitySupport.instance().toStringID(this);
    }
}