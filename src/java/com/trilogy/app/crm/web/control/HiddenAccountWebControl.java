/*
 * This code is a protected work and subject to domestic and international
 * copyright law(s). A complete listing of authors of this work is readily
 * available. Additionally, source code is, by its very nature, confidential
 * information and inextricably contains trade secrets and other information
 * proprietary, valuable and sensitive to Redknee, no unauthorised use,
 * disclosure, manipulation or otherwise is permitted, and may only be used
 * in accordance with the terms of the licence agreement entered into with
 * Redknee Inc. and/or its subsidiaries.
 *
 * Copyright &copy; Redknee Inc. and its subsidiaries. All Rights Reserved.
 *
 */
package com.trilogy.app.crm.web.control;

import java.io.PrintWriter;

import javax.servlet.ServletRequest;

import com.trilogy.framework.xhome.beans.xi.PropertyInfo;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.webcontrol.WebControl;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountWebControl;
import com.trilogy.app.crm.bean.AccountXInfo;

/**
 * Adds all the account fields as hidden form input fields. Used for Duplicate
 * Detection.
 * 
 * @author cindy.wong@redknee.com
 * @since 2010-10-12
 */
public class HiddenAccountWebControl extends AccountWebControl
{

	@Override
	public void
	    fromWeb(Context ctx, Object obj, ServletRequest req, String name)
	{
		Context subCtx = ctx.createSubContext();
		subCtx.put("MODE", CREATE_MODE);
		super.fromWeb(subCtx, obj, req, name);
	}

	@Override
	public void toWeb(Context ctx, PrintWriter out, String name, Object obj)
	{
		Account bean = (Account) obj;
		Context subCtx = ctx.createSubContext();

		// Some nested WebControls may want to know about the whole bean rather
		// than just their property so we put it in the Context for them
		subCtx.put(BEAN, bean);

		// Not in table mode so set the TABLE_MODE to false. Used by individual
		// web controls
		subCtx.put("TABLE_MODE", false);

		Context secureCtx = subCtx.createSubContext();
		secureCtx.put("MODE", DISPLAY_MODE);

		for (Object o : AccountXInfo.PROPERTIES)
		{
			PropertyInfo property = (PropertyInfo) o;
			WebControl wc =
			    (WebControl) property.getInstanceOf(subCtx, WebControl.class);
			if (wc != null)
			{
				wc = new HiddenFieldWebControl(wc);
				wc.toWeb(subCtx, out, name + SEPERATOR + property.getName(),
				    property.get(bean));
			}
		}
	}
}
