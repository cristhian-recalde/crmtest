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
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.trilogy.framework.xhome.auth.SimplePermission;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.False;
import com.trilogy.framework.xhome.elang.True;
import com.trilogy.framework.xhome.web.action.ActionMgr;
import com.trilogy.framework.xhome.web.border.Border;
import com.trilogy.framework.xhome.web.util.Link;
import com.trilogy.framework.xhome.webcontrol.RequestServicer;
import com.trilogy.framework.xhome.webcontrol.WebController;

/**
 * @author lxia
 */
public class SubscriberViewBorder implements Border{

	String mode="edit"; 
	
	public void service(Context ctx, HttpServletRequest req, HttpServletResponse res, RequestServicer delegate)
	throws ServletException, IOException
	{
		  ctx = ctx.createSubContext();
		  Link link = (Link) ctx.get(Link.class); 
		  
		  List list = new ArrayList();
		  mode = (String) link.getMap().get("mode"); 
          WebController ctrl = (WebController) ctx.get(WebController.class);

		  if ( mode == null ||  !mode.equals( "display")) 
		  {
		  	list.add(new com.redknee.framework.xhome.web.action.EditAction(new SimplePermission("")));
	        ctrl.setNewPredicate(True.instance()); 
		  }  
		  else 
		  {
 	        ctrl.setNewPredicate(False.instance()); 
 		  }

		  list.add(new com.redknee.framework.xhome.web.action.ViewAction(new SimplePermission("")));
		  list.add(new com.redknee.app.crm.web.action.TransactionsAction(new SimplePermission("")));
		  list.add(new com.redknee.app.crm.web.action.BucketHistoryAction(new SimplePermission("")));
		  list.add(new com.redknee.app.crm.web.action.CallDetailsAction(new SimplePermission(""))); 
		  list.add(new com.redknee.app.crm.web.action.AccountPaymentAction(new SimplePermission(""))); 
		  list.add(new com.redknee.app.crm.web.action.SubscriberNotesAction(new SimplePermission(""))); 
		  list.add(new com.redknee.app.crm.web.action.MoveSubscriberAction(new SimplePermission("")) ); 

		  ActionMgr.setActions(ctx, list);
		  delegate.service(ctx, req, res);

	}


	
}
