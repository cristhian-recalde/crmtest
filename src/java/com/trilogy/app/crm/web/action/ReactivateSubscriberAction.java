/*
 * This code is a protected work and subject to domestic and international copyright
 * law(s). A complete listing of authors of this work is readily available. Additionally,
 * source code is, by its very nature, confidential information and inextricably contains
 * trade secrets and other information proprietary, valuable and sensitive to Redknee, no
 * unauthorised use, disclosure, manipulation or otherwise is permitted, and may only be
 * used in accordance with the terms of the licence agreement entered into with Redknee
 * Inc. and/or its subsidiaries.
 * 
 * Copyright &copy; Redknee Inc. and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.web.action;

import java.io.PrintWriter;
import java.security.Permission;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.web.action.*;
import com.trilogy.framework.xhome.web.util.Link;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;


/**
 * Provides a link to the "Reactivate Subscriber" screen.
 * 
 * @author jimmy.ng@redknee.com
 */
public class ReactivateSubscriberAction extends SimpleWebAction
{

    /**
     * Create a new ReactivateSubscriberAction.
     */
    public ReactivateSubscriberAction()
    {
        super("reactivateSubscriber", "Reactivate");
    }


    /**
     * Create a new ReactivateSubscriberAction with the given permission.
     * 
     * @param permission
     *            The permission required to use the action.
     */
    public ReactivateSubscriberAction(final Permission permission)
    {
        this();
        setPermission(permission);
    }


    // INHERIT
    public void writeLink(final Context ctx, final PrintWriter out, final Object bean, final Link link)
    {
        if (bean instanceof Subscriber)
        {
            final Subscriber subscriber = (Subscriber) bean;
            
            // TT 5072021505: remove reactivate link for postpaid sub and hybrid sub
            if (subscriber.getState() == SubscriberStateEnum.INACTIVE
                    && subscriber.isPrepaid()) 
            {
                link.remove("key");
                link.remove("query");
                link.addRaw(".existingSubscriberIdentifier", subscriber.getId());
                link.addRaw("cmd", "appCRMReactivateSubscriber");
                link.writeLink(out, getLabel());
            }
        }
        else
        {
            return;
        }
    }
} // class
