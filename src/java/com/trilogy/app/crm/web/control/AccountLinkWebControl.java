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

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.web.util.Link;

import com.trilogy.app.crm.support.WebControlSupportHelper;

/**
 * @author jchen
 *
 * Web control writes a url link in web page, which will navigate
 * to the account ban specified
 */
public class AccountLinkWebControl
   extends PreviewTextFieldWebControl
{
   
   public final static String MENU_KEY = "SubMenuAccountEdit";

   
	public AccountLinkWebControl(boolean editableAtCreate)
	{
		editableAtCreate_ = editableAtCreate;
	}
	/**
	 * @inheritDoc
	 */
	@Override
    public void toWeb(Context ctx, PrintWriter out, String name, Object obj)
    {
       if (obj != null)
       {
	       	if (WebControlSupportHelper.get(ctx).isCreateMode(ctx) && isEditableAtCreate())
	       	{
	       		super.toWeb(ctx, out, name, obj);
	       	}
	       	else
	       	{
		          String parentBAN = (String)obj;
		          if (parentBAN != null && parentBAN.length() > 0)
		          {
		             Link link = new Link(ctx);
		             link.add("cmd", MENU_KEY);
		             link.add("key", parentBAN);
		             link.writeLink(out, parentBAN);
		          }
	       	}
       }
    }

    @Override
    public Object fromWeb(Context ctx, ServletRequest req, String name)
       throws NullPointerException
    {
    	if (WebControlSupportHelper.get(ctx).isCreateMode(ctx) && isEditableAtCreate())
       	{
       		return super.fromWeb(ctx, req, name);
       	}
        else
        {
            throw new NullPointerException();
        }
    }

    @Override
    public void fromWeb(Context ctx, Object obj, ServletRequest req, String name)
    {
    }
	/**
	 * @return Returns the editableAtCreate_.
	 */
	public boolean isEditableAtCreate() {
		return editableAtCreate_;
	}
	protected boolean editableAtCreate_= false;
}
