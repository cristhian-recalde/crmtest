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
package com.trilogy.app.crm.extension;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.beans.ExceptionListener;
import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.beans.XBeans;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.language.HomeMessageMgrSPI;
import com.trilogy.framework.xhome.language.MessageMgr;
import com.trilogy.framework.xhome.web.action.EditHelpAction;
import com.trilogy.framework.xhome.web.action.WebActionBean;
import com.trilogy.framework.xhome.web.renderer.ButtonRenderer;
import com.trilogy.framework.xhome.web.renderer.DefaultButtonRenderer;
import com.trilogy.framework.xhome.web.renderer.DefaultFormRenderer;
import com.trilogy.framework.xhome.web.renderer.FormRenderer;
import com.trilogy.framework.xhome.web.renderer.TableRenderer;
import com.trilogy.framework.xhome.web.util.Link;
import com.trilogy.framework.xhome.webcontrol.BeanWebController;
import com.trilogy.framework.xhome.webcontrol.HTMLExceptionListener;
import com.trilogy.framework.xhome.webcontrol.OutputWebControl;
import com.trilogy.framework.xhome.webcontrol.WebControl;
import com.trilogy.framework.xlog.log.LogSupport;

/**
* Request servicer responsible to show the advanced features screen.
* @author Marcio Marques
* @since 8.5
*
*/
public class AdvancedFeaturesRequestServicer extends BeanWebController
{
    public AdvancedFeaturesRequestServicer(Context ctx, Class key)
    {
        super(ctx, key, null);
    }

    public Home getHome(Context ctx)
    {
        try
        {
            Class homeClass = Class.forName(((Class) key_).getName() + "Home");
            return (Home) ctx.get(homeClass);
        } 
        catch (ClassNotFoundException e)
        {
            LogSupport.major(ctx, this, "Class " + ((Class) key_).getName() + "Home invalid. Unable to output Advanced Features.");
        }
        return null;
    }
    public void service(Context parentCtx, HttpServletRequest req, HttpServletResponse res) throws ServletException,
    IOException
{
    PrintWriter out = res.getWriter();
    String cmd = req.getParameter("CMD");
    Context ctx = parentCtx.createSubContext();
    Object bean = null; 
    
    
    bean = parentCtx.get(key_);

    if (bean == null)
    {
        out.println("<center><b>Developer Error: No bean with key '" + key_ + "' was found!</b><center>");

        return;
    }

    Link link = (Link) ctx.get(Link.class);
    MessageMgr mmgr = new MessageMgr(ctx, bean);
    WebControl wc = getWebControl();

    if (wc == null)
    {
        wc = (WebControl) XBeans.getInstanceOf(ctx, bean.getClass(), WebControl.class);
    }

    ctx.put(HttpServletRequest.class, req);

    // this table is for formatting only. it prevents the Bean
    // from occupying all the width of the page
    out.println("<table cellpadding=\"0\" cellspacing=\"0\" border=\"0\"><tr><td>");

    wc.fromWeb(ctx, bean, req, "");

    if ((isCmd("Update", req)))
    {
        HTMLExceptionListener hel = new HTMLExceptionListener(mmgr);
        ctx.put(ExceptionListener.class, hel);

        try
        {
            getValidator().validate(ctx, bean);
        }
        catch (IllegalStateException e)
        {
            hel.thrown(e);
        }

        if (hel.hasErrors())
        {
            hel.toWeb(ctx, out, "", null);
        }
        else
        {
            Home home = getHome(ctx);
            try
            {
                home.store(ctx, bean);
            }
            catch (HomeException e)
            {
                hel.thrown(e);
            }

            if (hel.hasErrors())
            {
                HTMLExceptionListener outputHel = new HTMLExceptionListener(mmgr);
                for (Exception e : (List<Exception>) hel.getExceptions())
                {
                    if (e instanceof HomeException && (e.getCause() instanceof IllegalPropertyArgumentException || e.getCause() instanceof CompoundIllegalStateException))
                    {
                        outputHel.thrown(e.getCause());
                    }
                    else
                    {
                        outputHel.thrown(e);
                    }
                }
                outputHel.toWeb(ctx, out, "", null);
            }
            else
            {
                beanUpdated(ctx, key_);
    
                out.println(mmgr.get("AdvancedFeaturesUpdated", "<center><b>Advanced features updated.</b></center>"));
            }
        }
    } // Update or Save

    FormRenderer formRenderer = ((FormRenderer) ctx.get(FormRenderer.class, DefaultFormRenderer.instance()));
    formRenderer.Form(out, ctx);

    //out.print("<table width=\"100%\"><tr><th align=\"right\">");
    out.print("<table width=\"100%\" class=\"center\"><tbody><tr><td align=\"right\">");

   ButtonRenderer br = (ButtonRenderer) ctx.get(ButtonRenderer.class, DefaultButtonRenderer.instance());
//    TableRenderer tr = tableRenderer(ctx,null);

        wc.toWeb(ctx, out, "", bean);
out.print("</td></tr><tr class=\"summary-details\"><td>");
out.println( "<table class=\""+com.redknee.framework.xhome.web.css.CssClassName.SUMMARY_BUTTONS+"\"><tbody><tr>");             
//        tr.Table(ctx,out, "","class=\"summary-buttons\"");
//        tr.TR(ctx, out, null, 0);
        
        if (OutputWebControl.DISPLAY_MODE != ctx.getInt("MODE", OutputWebControl.DISPLAY_MODE))
        {
            if (isPreviewEnabled())
            {
out.println( "<td>");             
//                tr.TD(ctx,out);
                br.inputButton(out, ctx, bean.getClass(), "Preview", false);
//                tr.TDEnd(ctx,out);
out.println( "</td>");             
            }
    
//            tr.TD(ctx,out);
out.println( "<td>");             
            br.inputButton(out, ctx, bean.getClass(), "Update", false);
out.println( "</td>");             
//            tr.TDEnd(ctx,out);
            if (ctx.getBoolean(HomeMessageMgrSPI.ENABLE_XMESSAGE_CAPTURE))
            {
                   WebActionBean action1 = new EditHelpAction();
//                 tr.TD(ctx,out);
out.println( "<td>");             
                   action1.writeLinkDetail(ctx, out, bean, link);
out.println( "</td>");             
//                 tr.TDEnd(ctx,out);
            }
        }

    if (isHelpEnabled())
    {
out.println( "<td>");             
//        tr.TD(ctx,out);
        outputHelpLink(ctx, req, res);
out.println( "</td>");             
//        tr.TDEnd(ctx,out);
    }

out.println( "</tr></tbody></table>");             
//    tr.TREnd(ctx,out);
//    tr.TableEnd(ctx, out, "");

    out.println("</td></tr></tbody></table>");

    formRenderer.FormEnd(out);

    // normal end of formatting table
    out.println("</td></tr></table>");
}}

