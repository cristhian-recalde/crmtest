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
package com.trilogy.app.crm.web.action;

import java.io.PrintWriter;
import java.security.Permission;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.visitor.AbortVisitException;
import com.trilogy.framework.xhome.web.action.SimpleWebAction;
import com.trilogy.framework.xhome.web.util.Link;
import com.trilogy.framework.xhome.web.util.PopupLink;

import com.trilogy.app.crm.bean.TestInvoice;
import com.trilogy.app.crm.web.border.TestInvoiceAccountNoteWebBorder;

/**
 * @author amedina
 * 
 * Executes the View Action and adds a note when viewing such invoice
 */
public class TestInvoiceViewAction extends SimpleWebAction 
{
	public TestInvoiceViewAction()
	{
		super("view", "View");
		
	}
	
    public TestInvoiceViewAction(final String action, final String label)
    {
        super(action, label);
    }

	  
    public TestInvoiceViewAction(Permission permission)
	{
	   this();
	   setPermission(permission);
	}


	@Override
    public void writeLink(Context ctx, PrintWriter out, Object bean, Link link)
	{
	   if (isPopup())
	   {
	      link = new PopupLink(link);
	      // while we're at it, hide the menu too so they can't continue to use the popup window
	      link.add("menu", "hide");
	   }
	   link.add("mode", "display");
	   link.add(TestInvoiceAccountNoteWebBorder.MESSAGE, " - View - ");
	   link.writeLink(out, getLabel());
	   
	}
	
    @Override
    public boolean isEnabled(Context context, Object bean) throws AbortVisitException
    {
        if (bean instanceof TestInvoice)
        {
            if (((TestInvoice)bean).isGhost())
            {
                return false;
            }
        }     
        
        return true;
    }
}
