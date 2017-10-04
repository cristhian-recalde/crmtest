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

import com.trilogy.app.crm.bean.IdFormat;
import com.trilogy.app.crm.bean.IdFormatTableWebControl;
import com.trilogy.framework.xhome.context.Context;

/**
 * @author amedina
 *
 * TableWebControl for auto population of the formats
 */
public class IdFormatInfoTableWebControl extends IdFormatTableWebControl 
{


	public void fromWeb(Context ctx, Object obj, ServletRequest req, String name)
    {
       ctx = ctx.createSubContext();

       ctx.put("MODE", CREATE_MODE);

       super.fromWeb(ctx, obj, req, name);
    }


	

	public void toWeb(Context ctx, PrintWriter out, String name, Object obj)
    {
	    IdFormat format = (IdFormat) obj;
	    ctx = ctx.createSubContext();
		super.toWeb(ctx, out, name, obj);
    }
 
	//List currentIdFormats_ = null;

    private static final int NUMBER_OF_FORMATS = 5;

}
