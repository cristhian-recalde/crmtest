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

import java.security.Permission;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.visitor.AbortVisitException;
import com.trilogy.framework.xhome.web.action.DeleteAction;
import com.trilogy.framework.xhome.web.util.Link;

import com.trilogy.app.crm.bean.TestInvoice;
import com.trilogy.app.crm.web.border.TestInvoiceAccountNoteWebBorder;

/**
 * @author amedina
 */
public class DeleteTestInvoiceAction extends DeleteAction 
{
    public DeleteTestInvoiceAction()
    {
    }


    public DeleteTestInvoiceAction(Permission permission)
    {
        this();
        setPermission(permission);
    }


    @Override
    public Link modifyLink(Context ctx, Object bean, Link link)
    {
        link = super.modifyLink(ctx, bean, link);
        link.add(TestInvoiceAccountNoteWebBorder.MESSAGE, " - Delete - ");
        return link;
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
