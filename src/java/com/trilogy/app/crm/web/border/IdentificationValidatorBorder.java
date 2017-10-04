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
package com.trilogy.app.crm.web.border;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.trilogy.app.crm.bean.IdFormat;
import com.trilogy.app.crm.bean.Identification;
import com.trilogy.app.crm.bean.IdentificationHome;
import com.trilogy.app.crm.support.IdentificationSupport;
import com.trilogy.framework.xhome.beans.Validator;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.ValidatingHome;
import com.trilogy.framework.xhome.web.border.Border;
import com.trilogy.framework.xhome.webcontrol.RequestServicer;

/**
 * @author amedina
 *
 *
 * Decorates the Validation at GUI side for the identification
 * 
 */
public class IdentificationValidatorBorder implements Border, Validator 
{

	/* (non-Javadoc)
	 * @see com.redknee.framework.xhome.web.border.Border#service(com.redknee.framework.xhome.context.Context, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, com.redknee.framework.xhome.webcontrol.RequestServicer)
	 */
	public void service(Context ctx, HttpServletRequest req,
			HttpServletResponse res, RequestServicer delegate)
			throws ServletException, IOException 
			
	{
		Context subCtx = ctx.createSubContext();
		subCtx.put(IdentificationHome.class, new ValidatingHome(this, (Home) subCtx.get(IdentificationHome.class)));
		delegate.service(subCtx,req,res);

	}

	/* (non-Javadoc)
	 * @see com.redknee.framework.xhome.beans.Validator#validate(com.redknee.framework.xhome.context.Context, java.lang.Object)
	 */
	public void validate(Context ctx, Object obj) throws IllegalStateException 
	{
		if (obj instanceof Identification)
		{
			Identification id = (Identification) obj;
			IdFormat format = id.getFormat();
			
			if (format == null)
			{
				throw new IllegalStateException("There must be at least one format on the identification");
			}
			
			if (id.getExample() != format.getRegEx())
			{
				id.setExample(IdentificationSupport.getExample(id));
			}
		}
	}

}
