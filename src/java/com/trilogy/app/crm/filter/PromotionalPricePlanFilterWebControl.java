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
package com.trilogy.app.crm.filter;

import java.io.PrintWriter;
import java.util.Collection;
import java.util.Iterator;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.support.IdentitySupport;
import com.trilogy.framework.xhome.web.agent.WebAgents;
import com.trilogy.framework.xhome.webcontrol.AbstractKeyWebControl;

import com.trilogy.app.crm.bean.PricePlan;
import com.trilogy.app.crm.bean.PricePlanMigration;
import com.trilogy.app.crm.bean.PricePlanMigrationHome;
import com.trilogy.app.crm.web.control.CustomizedPricePlanKeyWebControl;


/**
 * @author skushwaha
 */
public class PromotionalPricePlanFilterWebControl extends CustomizedPricePlanKeyWebControl
{
    public PromotionalPricePlanFilterWebControl(AbstractKeyWebControl webControl)
    {
        super(webControl);
    }


    @Override
    public void toWeb(Context ctx, PrintWriter out, String name, Object obj)
    {
        int mode = ctx.getInt("MODE", DISPLAY_MODE);
        Home home = (Home) ctx.get(getHomeKey());
        if (mode == DISPLAY_MODE)
        {
            try
            {
                Object bean = home.find(ctx, obj);
                String str = getDesc(ctx, bean);
                if (str != null && str.length() > 0)
                {
                    out.print(str);
                }
                else
                {
                    out.print("&nbsp;");
                }
            }
            catch (HomeException he)
            {
                outputError(ctx,out, "invalid key '" + obj + "'");
            }
            catch (NullPointerException ne)
            {
                outputError(ctx,out, "invalid key '" + obj + "'");
            }
        }
        else
        {
            try
            {
                out.print("<select name=\"" + name + "\" size=\"" + listSize_ + "\"");
                if (autoPreview_)
                {
                    out.print(" onChange=\"autoPreview('" + WebAgents.getDomain(ctx) + "', event)\"");
                }
                out.print(">");
                if (isOptional_)
                {
                    out.print("<option value=\"\">---</option>");
                }
                Object bean = null;
                try
                {
                    bean = home.find(ctx, obj);
                }
                catch (HomeException he)
                {
                }
                if (bean == null)
                {
                    out.print("<option value=\"" + obj + "\"");
                    out.print(" selected=\"selected\" ");
                    out.print(">");
                    out.print("Illegale price plan " + obj);
                }
                Home pricePlanMigrationHome = (Home) ctx.get(PricePlanMigrationHome.class);
                if (pricePlanMigrationHome == null)
                {
                    throw new HomeException("PricePlanMigrationHome not found in the context");
                }
                IdentitySupport id = getIdentitySupport();
                Collection pricePlans = pricePlanMigrationHome.selectAll();
                if (!pricePlans.isEmpty())
                {
                    for (Iterator j = pricePlans.iterator(); j.hasNext();)
                    {
                        PricePlanMigration pricePlan = (PricePlanMigration) j.next();
                        for (Iterator i = home.selectAll().iterator(); i.hasNext();)
                        {
                            bean = i.next();
                            if (((PricePlan) bean).isEnabled()
                                    && pricePlan.getCurrentPricePlan() != ((PricePlan) bean).getId())
                            {
                                String key = id.toStringID(id.ID(bean));
                                out.print("<option value=\"");
                                out.print(key);
                                out.print("\"");
                                if (key.equals(id.toStringID(obj)))
                                {
                                    out.print(" selected=\"selected\" ");
                                }
                                out.print(">");
                                out.print(getDesc(ctx, bean));
                            }
                        }
                    }
                    out.println("</select>");
                }
                else
                {
                    for (Iterator i = home.selectAll().iterator(); i.hasNext();)
                    {
                        bean = i.next();
                        if (((PricePlan) bean).isEnabled())
                        {
                            String key = id.toStringID(id.ID(bean));
                            out.print("<option value=\"");
                            out.print(key);
                            out.print("\"");
                            if (key.equals(id.toStringID(obj)))
                            {
                                out.print(" selected=\"selected\" ");
                            }
                            out.print(">");
                            out.print(getDesc(ctx, bean));
                        }
                    }
                    out.println("</select>");
                }
            }
            catch (HomeException e)
            {
                outputError(ctx,out, "no home");
            }
        }
    }
}