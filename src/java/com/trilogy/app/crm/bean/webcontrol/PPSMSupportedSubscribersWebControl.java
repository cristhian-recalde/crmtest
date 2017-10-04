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
package com.trilogy.app.crm.bean.webcontrol;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.servlet.ServletRequest;

import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.extension.subscriber.PPSMSupporteeSubExtensionTableWebControl;
import com.trilogy.app.crm.extension.subscriber.PPSMSupporterSubExtension;
import com.trilogy.app.crm.support.ExtensionSupportHelper;
import com.trilogy.app.crm.web.action.PPSMSupporteeRemovalAction;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.web.action.SimpleWebAction;
import com.trilogy.framework.xhome.web.agent.WebAgents;
import com.trilogy.framework.xhome.web.renderer.ButtonRenderer;
import com.trilogy.framework.xhome.web.renderer.DefaultButtonRenderer;
import com.trilogy.framework.xhome.webcontrol.AbstractWebControl;
import com.trilogy.framework.xhome.webcontrol.TextFieldWebControl;
import com.trilogy.framework.xhome.webcontrol.WebControl;

/**
 * Web control used to show the ppsm supported subscribers in the PPSM Supporter screen.
 * @author Marcio Marques
 * @since 8.5
 *
 */
public class PPSMSupportedSubscribersWebControl extends AbstractWebControl
{
    private WebControl supportedSubscribers = new com.redknee.framework.xhome.webcontrol.ReadOnlyWebControl(new PPSMSupporteeSubExtensionTableWebControl());
    private WebControl newSupporter = new TextFieldWebControl(15);

    @Override
    public Object fromWeb(Context ctx, ServletRequest req, String name) throws NullPointerException
    {
        return null;
    }

    @Override
    public void fromWeb(Context ctx, Object obj, ServletRequest req, String name)
    {
    }

    @Override
    public void toWeb(Context ctx, PrintWriter out, String name, Object obj)
    {
        String subID = null;
        Subscriber subscriber = (Subscriber) ExtensionSupportHelper.get(ctx).getParentBean(ctx);
        
        if (subscriber==null)
        {
            PPSMSupporterSubExtension extension = (PPSMSupporterSubExtension) ctx.get(AbstractWebControl.BEAN);
            subID = extension.getSubId();
        }
        else
        {
            subID = subscriber.getId();
        }

        ctx = ctx.createSubContext();
        List<SimpleWebAction> list = new ArrayList<SimpleWebAction>();
        SimpleWebAction action = new PPSMSupporteeRemovalAction();
        list.add(action);
        
        ctx.put(com.redknee.framework.xhome.web.action.ActionMgr.ACTION_LIST_KEY, list);
        supportedSubscribers.toWeb(ctx, out, name, obj);
        
        Collection         beans    = (Collection) obj;
        
        if (beans.size()>0 && subID!=null)
        {
            final ButtonRenderer brend = (ButtonRenderer) ctx.get(ButtonRenderer.class, DefaultButtonRenderer.instance());
            
            out.println("<table border=\"0\"><tr><td>");
            out.println("<a href=\"/AppCrm/home?cmd=appCRMPPSMSupporterMoveSupportees&amp;oldSubscriptionId=" + subID + "\">");
            String msg = "Change Supporter";
            
            out.print(" <img  name=\"");
            out.print(WebAgents.rewriteName(ctx, name + DefaultButtonRenderer.BUTTON_KEY));
            out.print("\" id=\"button-");
            out.print(WebAgents.rewriteName(ctx, name + DefaultButtonRenderer.BUTTON_KEY));
            out.print("\" src=\"" + ctx.get(DefaultButtonRenderer.BUTTONRENDERER_SERVICE_KEY, "ButtonRenderServlet")
                    + "?.src=default&amp;.label=");
            out.print(msg);
            out.print("\"");
            out.print(" border=\"0\" align=\"right\"");
            out.print(" alt=\"");
            out.print(msg);
            out.print("\"/> ");            
            out.println("</a>");
            out.println("</td></tr></table>");
        }
    }
        
}
