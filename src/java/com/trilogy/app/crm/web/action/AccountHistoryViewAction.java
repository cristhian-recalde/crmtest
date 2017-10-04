/*
ViewAction

Copyright (c) 2002, Redknee
All rights reserved.

Date        Author          Changes
----        ------          -------
Sept 5 02   Kevin Greer     Created
*/

package com.trilogy.app.crm.web.action;

import java.io.PrintWriter;
import java.security.Permission;

import com.trilogy.app.crm.bean.AccountHistory;
import com.trilogy.app.crm.bean.AccountHistoryTypeEnum;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.web.action.SimpleWebAction;
import com.trilogy.framework.xhome.web.util.Link;
import com.trilogy.framework.xhome.webcontrol.DateWebControl;


/**
**/
public class AccountHistoryViewAction
    extends SimpleWebAction
{
    
    public AccountHistoryViewAction()
    {
        super("view", "View");
    }

    public AccountHistoryViewAction(Permission permission)
    {
        this();
        setPermission(permission);
    }

    public void writeLink(Context ctx, PrintWriter out, Object bean, Link link)
    {
        AccountHistory acctHist = (AccountHistory) bean;
        
        if ( acctHist.getType() == AccountHistoryTypeEnum.TRANSACTION )
        {
            link.add("mode", "display");
            link.writeLink(out, getLabel());
        }
    }
}
