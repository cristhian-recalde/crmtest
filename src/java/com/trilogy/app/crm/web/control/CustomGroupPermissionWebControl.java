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
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import javax.servlet.ServletRequest;

import com.trilogy.framework.auth.permission.PermissionInfo;
import com.trilogy.framework.auth.permission.PermissionInfoHome;
import com.trilogy.framework.auth.permission.PermissionInfoIdentitySupport;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.webcontrol.MultiSelectWebControl;
import com.trilogy.framework.xhome.webcontrol.OutputWebControl;
import com.trilogy.framework.xhome.webcontrol.WebControl;
import com.trilogy.framework.xlog.log.MajorLogMsg;

import com.trilogy.app.crm.auth.PermissionRowToPermissionInfoAdapter;

/**
 * A customized multi-select web control for group permissions.
 *
 * @author cindy.wong@redknee.com
 * @since 8.5
 */
public class CustomGroupPermissionWebControl extends MultiSelectWebControl
{

    public CustomGroupPermissionWebControl()
    {
        super(PermissionInfoHome.class,
            PermissionInfoIdentitySupport.instance(),
            new OutputWebControl()
            {
                public void toWeb(Context ctx, PrintWriter out, String name, Object obj)
                {
                    PermissionInfo bean = (PermissionInfo) obj;
                    out.print(bean.getName() + " (" + bean.getDescription() + ")");
                }
            });
    }

    public void toWeb(Context ctx, PrintWriter out, String name, Object obj)
    {
        int     mode     = ctx.getInt("MODE", DISPLAY_MODE);
        Home home        = getHome(ctx);
        home = home.where(ctx, getSelectFilter());

        Set set         = new HashSet();
        for (Object o : ((List)obj))
        {
            set.add(PermissionRowToPermissionInfoAdapter.instance().adapt(ctx, o));
        }

        Set selectedSet = new TreeSet(getComparator());

        String hiddenField = "";

        for ( Iterator i = set.iterator() ; i.hasNext() ; )
        {
            Object o = i.next();
            Object key = ((PermissionInfo)o).ID();
            try
            {
                Object bean = home.find(ctx, key);
                if ( bean != null)
                {
                    selectedSet.add(bean);
                }
            }
            catch(Exception e)
            {
                new MajorLogMsg(this, e.getMessage(), e).log(ctx);
            }
        }

        for ( Iterator i = selectedSet.iterator(); i.hasNext();)
        {
            String value = toStringId(ctx, i.next()).replace('[', ' ').replace(']',' ').trim();

            hiddenField = hiddenField +     value + ",";
        }

        out.print("<input type=\"hidden\" name=\"" + name + "_valueofsub" +     "\" value=\"");
        out.print(hiddenField);
        out.print("\"/>");

        switch (mode)
        {
            case EDIT_MODE:
            case CREATE_MODE:
                out.print("<table border=\"0\">");
                out.print("<tr>");

                /** first part of the table,
                select field to display all elements in home
                but without those entries those are selected into
                sublist. **/
                outputWholeList(ctx, out, home, name, selectedSet);

                /** second part of the table **/
                outputButtons(ctx, out,name);

                /** third part of the table,
                will accept selected result from first part     **/
                outputSubList(ctx, out, home, name, selectedSet);

                /** end of the table */
                out.println("<tr>");
                out.println("</table>");
                break;

            case DISPLAY_MODE:
            default:
                String delim = "";
                for (Iterator i = selectedSet.iterator(); i.hasNext(); )
                {
                    out.print(delim);

                    outputBean(ctx, out, i.next());

                    delim = ",";
                }
        } // switch
    }

    public Object fromWeb(Context ctx, ServletRequest req, String name)
    {
        Set set = (Set) super.fromWeb(ctx, req, name);
        List list = new LinkedList();
        for (Object o : set)
        {
            list.add(PermissionRowToPermissionInfoAdapter.instance().unAdapt(ctx, o));
        }
        return list;
    }
}
