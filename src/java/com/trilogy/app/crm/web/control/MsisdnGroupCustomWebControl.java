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

import com.trilogy.app.crm.bean.MsisdnGroupHome;
import com.trilogy.app.crm.bean.MsisdnGroupXInfo;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.OrderBy;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.OrderByHome;
import com.trilogy.framework.xhome.webcontrol.ProxyWebControl;
import com.trilogy.framework.xhome.webcontrol.WebControl;

/**
 * Sort the Msisdn groups in alphabetical order. 
 * Sorting Order : 0-n , A-Z, a-z.
 * @author piyush.shirke@redknee.com
 *
 */
public class MsisdnGroupCustomWebControl extends ProxyWebControl {

	public final static int DISPLAY_MODE = 0;

	public MsisdnGroupCustomWebControl(WebControl value) {
		super(value);
	}

	public Context wrapContext(Context ctx)
	{
	        if (ctx.getInt("MODE", CREATE_MODE) != DISPLAY_MODE)
	        {
	            Context subCtx = ctx.createSubContext();
	            Home msisdnGroupHome = (Home) ctx
	                    .get(MsisdnGroupHome.class);
	          
	            msisdnGroupHome = new OrderByHome(subCtx, new OrderBy(MsisdnGroupXInfo.NAME,true), msisdnGroupHome);

	            subCtx.put(MsisdnGroupHome.class, msisdnGroupHome);
	            return subCtx;
	        }

	        return ctx;

	    }
}
