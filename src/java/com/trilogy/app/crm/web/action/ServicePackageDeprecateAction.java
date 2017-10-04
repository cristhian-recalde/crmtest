/*
 * Created on Dec 15, 2005 2:59:28 PM
 *
 * This code is a protected work and subject to domestic and international
 * copyright law(s).  A complete listing of authors of this work is readily
 * available.  Additionally, source code is, by its very nature, confidential
 * information and inextricably contains trade secrets and other information
 * proprietary, valuable and sensitive to Redknee.  No unauthorized use,
 * disclosure, manipulation or otherwise is permitted, and may only be used
 * in accordance with the terms of the license agreement entered into with
 * Redknee Inc. and/or its subsidiaries.
 *
 * Copyright � Redknee Inc. and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.web.action;

import com.trilogy.framework.xhome.web.action.SimpleWebAction;
import com.trilogy.framework.xhome.web.util.Link;
import com.trilogy.framework.xhome.web.agent.WebAgents;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.app.crm.bean.ServicePackageHome;
import com.trilogy.app.crm.bean.ServicePackage;
import com.trilogy.app.crm.bean.ServicePackageStateEnum;

import java.io.PrintWriter;

/**
 * @author psperneac
 */
public class ServicePackageDeprecateAction extends SimpleWebAction
{
    public ServicePackageDeprecateAction()
    {
        super("deprecate","Deprecate");
    }

    public ServicePackageDeprecateAction(String key, String label)
    {
        super(key, label);
    }

    public void writeLink(Context ctx, PrintWriter out, Object bean, Link link)
    {
        String key=(String) link.getMap().get("key");
        link.remove("key");
        link.add("deprecate_key",key);

        super.writeLink(ctx, out, bean, link);
    }

    public void execute(Context ctx) throws AgentException
    {
        Home home=(Home) ctx.get(ServicePackageHome.class);

        String strKey=WebAgents.getParameter(ctx,"deprecate_key");

        try
        {
            Integer key = Integer.valueOf(strKey);
            ServicePackage pack=(ServicePackage) home.find(ctx,key);
            if(pack==null)
            {
                throw new AgentException("Cannot find package with key: "+key);
            }

            pack.setState(ServicePackageStateEnum.DEPRECATED_INDEX);
            home.store(ctx,pack);
        }
        catch(NumberFormatException e)
        {
            throw new AgentException("Cannot parse item key. Will not deprecate.");
        }
        catch (HomeException e)
        {
            throw new AgentException(e);
        }

        super.execute(ctx);
    }
}
