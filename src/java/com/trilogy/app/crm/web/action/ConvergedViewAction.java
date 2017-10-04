/*
 * This code is a protected work and subject to domestic and international
 * copyright law(s). A complete listing of authors of this work is readily
 * available. Additionally, source code is, by its very nature, confidential
 * information and inextricably contains trade secrets and other information
 * proprietary, valuable and sensitive to Redknee, no unauthorised use,
 * disclosure, manipulation or otherwise is permitted, and may only be used
 * in accordance with the terms of the licence agreement entered into with
 * Redknee Inc. and/or its subsidiaries.
 *
 * Copyright &copy; Redknee Inc. and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.web.action;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.ConvergedAccountSubscriber;
import com.trilogy.app.crm.bean.SearchTypeEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.framework.xhome.web.action.*;
import com.trilogy.framework.xhome.beans.XBeans;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.web.util.Link;
import java.io.PrintWriter;
import java.security.Permission;
import java.security.Principal;
import java.util.*;


/**
 * Provides a link to the "Move Subscriber" screen.
 *
 * @author gary.andereson@redknee.com
 */
public class ConvergedViewAction
    extends SimpleWebAction
{
    /**
     * Create a new MoveSubscriberAction.
     */
    public ConvergedViewAction()
    {
        super("view", "View");
    }


    /**
     * 
     *
     * @param permission The permission required to use the action.
     */
     // INHERIT
    public void writeLink(
        final Context ctx,
        final PrintWriter out,
        final Object bean,
        final Link link)
    {
        
            final ConvergedAccountSubscriber conAcctSub = (ConvergedAccountSubscriber)bean;

            link.remove("key");
            link.remove("query");
            link.add("mode", "display");
            if( (conAcctSub.getType() == SearchTypeEnum.Account) || ((conAcctSub.getType() == SearchTypeEnum.Both)&&(conAcctSub.getMSISDN().equals(""))))
            {
	            link.addRaw("key",conAcctSub.getBAN());
	            link.addRaw("cmd", "SubMenuAccountEdit");
            }
            /*
            else if(conAcctSub.getType() == SearchTypeEnum.Subscriber)
            {
            	
            	link.addRaw("key",conAcctSub.getSubId());
	            link.addRaw("cmd", "SubMenuSubProfileEdit");
            }
*/
            else if(!(conAcctSub.getMSISDN().equals("")))
            {
            	link.addRaw("key",conAcctSub.getSubId());
	            link.addRaw("cmd", "SubMenuSubProfileEdit");
            }
            
            link.writeLink(out, getLabel());
        
        
    }

} //class
