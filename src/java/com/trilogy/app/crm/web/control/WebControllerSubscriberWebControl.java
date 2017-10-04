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

import javax.servlet.http.HttpServletRequest;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.framework.xhome.beans.XBeans;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.web.agent.WebAgents;
import com.trilogy.framework.xhome.web.util.Link;
import com.trilogy.framework.xhome.webcontrol.WebControl;
import com.trilogy.framework.xhome.webcontrol.WebController;

/**
 * @author lxia
 */
public class WebControllerSubscriberWebControl extends WebControllerWebControl57 
	{
		public WebControllerSubscriberWebControl(Class beanType) {
			super(beanType); 
		}
	
		  public WebControllerSubscriberWebControl(Class beanType, String homeIdentifier)
		   {
		  		super(beanType, homeIdentifier);
		   }
		/** Have the Subscriber take its SPID from its parent Account. **/
		public Object modifyChild(Context ctx, Object child)
		{
			Subscriber sub     = (Subscriber) child;
			Account    account = (Account) getParent(ctx);
			
			sub.setSpid(account.getSpid());
			
			return sub;
		}

		
		public String createSQLClause(Object key)
		{
			return "BAN = '" + key + "'";
		}

		protected boolean added_ = false;			
		public void setUpWebControlFunctionButton(Context ctx, WebController ctrl, Class beanType_)
		{
			if ( ! added_ )
			{
				added_ = true;
				addBorder(new com.redknee.app.crm.web.border.SubscriberSearchBorder(ctx));
			}
			
			ctrl.setSummaryBorder(null);
			//ctrl.setWebControl(new ConvergeSubscriberWebControl());
			ctrl.setWebControl( (WebControl)(XBeans.getInstanceOf(ctx,Subscriber.class,WebControl.class)));
			
			String className  =  beanType_.getName();
			
			if ( ctx.get(className + ".delete") != null )
			{
				Boolean deleteEnabled = (Boolean)ctx.get(className + ".delete");
				ctrl.setDeleteEnabled(deleteEnabled.booleanValue());
			}
		}
		
             
     }