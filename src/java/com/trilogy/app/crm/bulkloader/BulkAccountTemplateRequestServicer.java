/*
 * Created on Jun 21, 2005
 *
 * This code is a protected work and subject to domestic and international
 * copyright law(s).  A complete listing of authors of this work is readily
 * available.  Additionally, source code is, by its very nature, confidential
 * information and inextricably contains trade secrets and other information
 * proprietary, valuable and sensitive to Redknee.  No unauthorized use,
 * disclosure, manipulation or otherwise is permitted, and may only be used
 * in accordance with the terms of the license agreement entered into with
 * Redknee Inc. and/or its subsidiaries.
 *
 * Copyright � Redknee Inc. and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.bulkloader;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.trilogy.app.crm.agent.BeanInstall;
import com.trilogy.app.crm.bean.AccountWebControl;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.True;
import com.trilogy.framework.xhome.webcontrol.AbstractWebControl;
import com.trilogy.framework.xhome.webcontrol.BeanWebController;
import com.trilogy.framework.xhome.webcontrol.OutputWebControl;
import com.trilogy.framework.xhome.webcontrol.RequestServicer;
import com.trilogy.framework.xhome.webcontrol.ViewModeEnum;
import com.trilogy.framework.xhome.webcontrol.WebControl;

public class BulkAccountTemplateRequestServicer implements RequestServicer
{
	public void service(Context ctx, HttpServletRequest req, HttpServletResponse res) throws ServletException,
		IOException
	{
		ctx=ctx.createSubContext();
		
		AbstractWebControl.setMode(ctx,"Account.spid",ViewModeEnum.READ_WRITE);
		
		ctx.put("MODE",OutputWebControl.CREATE_MODE);
		
		BeanWebController bb=new BeanWebController(ctx, BeanInstall.BULK_ACCOUNT_TEMPLATE);
		
		bb.setUpdatePredicate(True.instance());
		bb.setWebControl(new AccountWebControl(){

			/**
			 * @see com.redknee.app.crm.bean.AccountWebControl#toWeb(com.redknee.framework.xhome.context.Context, java.io.PrintWriter, java.lang.String, java.lang.Object)
			 */
			public void toWeb(Context ctx, PrintWriter out, String name, Object obj)
			{
				int mode=ctx.getInt("MODE",-1);
				if(mode==WebControl.EDIT_MODE)
				{
					ctx=ctx.createSubContext();
					ctx.put("MODE",WebControl.CREATE_MODE);
				}
				
				super.toWeb(ctx, out, name, obj);
			}

			/**
			 * @see com.redknee.app.crm.bean.AccountWebControl#fromWeb(com.redknee.framework.xhome.context.Context, java.lang.Object, javax.servlet.ServletRequest, java.lang.String)
			 */
			public void fromWeb(Context ctx, Object obj, ServletRequest req, String name)
			{
				int mode=ctx.getInt("MODE",-1);
				if(mode==WebControl.EDIT_MODE)
				{
					ctx=ctx.createSubContext();
					ctx.put("MODE",WebControl.CREATE_MODE);
				}
				
				super.fromWeb(ctx, obj, req, name);
			}

			/**
			 * @see com.redknee.app.crm.bean.AccountWebControl#fromWeb(com.redknee.framework.xhome.context.Context, javax.servlet.ServletRequest, java.lang.String)
			 */
			public Object fromWeb(Context ctx, ServletRequest req, String name)
			{
				int mode=ctx.getInt("MODE",-1);
				if(mode==WebControl.EDIT_MODE)
				{
					ctx=ctx.createSubContext();
					ctx.put("MODE",WebControl.CREATE_MODE);
				}
				
				return super.fromWeb(ctx, req, name);
			}
			
			
			
		});
		
		bb.service(ctx,req,res);
		
	}
}
