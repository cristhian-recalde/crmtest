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

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.webcontrol.AbstractWebControl;

import com.trilogy.app.crm.bas.tps.ServiceActivationTemplateKeyWebControl;
import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.SubBulkCreate;

/**
 * This custom web control updates the selected Sat Id on the SubBulkCreate form to
 * the underlying Subscriber object.
 *
 * @author manda.subramanyam@redknee.com
 *
 */
public class SubBulkCreateCustomSatWebControl extends ServiceActivationTemplateKeyWebControl
{

	public SubBulkCreateCustomSatWebControl(boolean val)
	{
		super(val);
	}


	/**
	 * 
	 * @override com.redknee.framework.xhome.webcontrol.AbstractWebControl.toWeb
	 */
    public void toWeb(Context ctx, PrintWriter out, String name, Object obj)
    {
    	Object obj1 = ctx.get(AbstractWebControl.BEAN);
    	Account account = null;
    	SubBulkCreate subBulkCreate = null;
    	if (obj1 instanceof SubBulkCreate)
    	{
    		subBulkCreate = (SubBulkCreate) obj1;
    		account = subBulkCreate.getAccount();
    	}
    	if (account != null && account.getSubscriber() != null)
    	{
    		account.getSubscriber().setSatId(subBulkCreate.getSatId());
    	}

    	super.toWeb(ctx, out, name, obj);
    }
}
