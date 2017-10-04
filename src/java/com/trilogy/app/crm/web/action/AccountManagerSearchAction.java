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
import com.trilogy.framework.xhome.web.action.SimpleWebAction;
import com.trilogy.framework.xhome.web.util.Link;

import com.trilogy.app.crm.bean.account.AccountManager;


/**
 * Link to the Accounts search border from Account Manager screen
 * 
 * @author ltang
 */

public class AccountManagerSearchAction extends SimpleWebAction
{

    public AccountManagerSearchAction()
    {
        super("search", "Accounts");
    }


    public AccountManagerSearchAction(Permission permission)
    {
        this();
        setPermission(permission);
    }


    /**
     * Output the link on the Table View
     */
    @Override
    public void writeLink(Context ctx, PrintWriter out, Object bean, Link link)
    {
        link.add("redirect", "true");
        link.add(".search.accountMgr", ((AccountManager) bean).getAccountMgrId());
        link.remove("key");
        link.remove("cmd");
        link.add("cmd", "SubMenuAccountEdit");

        out.print("<a href=\"");
        link.write(out);
        out.print("\" >");
        out.print(getLabel());
        out.print("</a>");
    }

}
