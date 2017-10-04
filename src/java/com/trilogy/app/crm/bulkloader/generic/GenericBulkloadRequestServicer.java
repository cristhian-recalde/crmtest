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
package com.trilogy.app.crm.bulkloader.generic;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.trilogy.framework.lifecycle.LifecycleSupport;
import com.trilogy.framework.xhome.beans.ExceptionListener;
import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.language.MessageMgr;
import com.trilogy.framework.xhome.web.renderer.ButtonRenderer;
import com.trilogy.framework.xhome.web.renderer.DefaultButtonRenderer;
import com.trilogy.framework.xhome.web.renderer.DefaultFormRenderer;
import com.trilogy.framework.xhome.web.renderer.FormRenderer;
import com.trilogy.framework.xhome.webcontrol.BeanWebController;
import com.trilogy.framework.xhome.webcontrol.HTMLExceptionListener;
import com.trilogy.framework.xhome.webcontrol.OutputWebControl;
import com.trilogy.framework.xhome.webcontrol.RequestServicer;
import com.trilogy.framework.xlog.log.MajorLogMsg;
import com.trilogy.framework.xlog.log.MinorLogMsg;
/**
 * The GUI interface used to service the Generic Bean Bulkloader requests. 
 * @author angie.li@redknee.com
 *
 * @since 8.2
 */
public class GenericBulkloadRequestServicer implements RequestServicer
{
    /**
     * Creates a new GenericBulkloadRequestServicer.
     */
    public GenericBulkloadRequestServicer()
    {
        webControl_ = new GenericBeanBulkloaderRequestWebControl();
    }

    public static final String MSG_KEY_BULKLOAD_BUTTON_NAME        = "GenericBulkloadRequest.BulkloadButtonName";
    public static final String MSG_KEY_INCOMPLETE                  = "GenericBulkloadRequest.incomplete";

    public void service(
            final Context ctx,
            final HttpServletRequest req,
            final HttpServletResponse res)
    throws ServletException, IOException
    {
        final PrintWriter out = res.getWriter();
        HttpSession session = req.getSession();

        final Context sCtx = ctx.createSubContext();

        final ButtonRenderer buttonRenderer =
            (ButtonRenderer) sCtx.get(
                    ButtonRenderer.class,
                    DefaultButtonRenderer.instance());

        MessageMgr manager = new MessageMgr(sCtx, this);

        HTMLExceptionListener exceptions = (HTMLExceptionListener) sCtx.get(HTMLExceptionListener.class);
        if (exceptions == null)
        {
            exceptions = new HTMLExceptionListener(manager);
            sCtx.put(HTMLExceptionListener.class, exceptions);
        }
        sCtx.put(ExceptionListener.class, exceptions);

        GenericBeanBulkloaderRequest form = new GenericBeanBulkloaderRequest(); 

        webControl_.fromWeb(sCtx, form, req, "");

        GenericBeanBulkloadManager bulkloadMgr = new GenericBeanBulkloadManager();
        sCtx.put(GenericBeanBulkloadManager.class, bulkloadMgr);

        String bulkloadButtonText = manager.get(MSG_KEY_BULKLOAD_BUTTON_NAME, "Process");

        try
        {   
            /* Since Threading is still not using a sub pool of the FW Threadpool, we open another door to run ourselves
             * to crash (Too many Bulk loaders working at once).  For now, we will not allow more than one Bulk load
             * execution at once.  This also helps us prevent competing race conditions in bulk loading.
             */
            if (isBulkloaderRunning(ctx))
            {
                String engMsg = "The previous bulk load request hasn't been completed yet.  Check the Progress Metrics and Monitors to determine which process is still executing.";
                new MinorLogMsg(this, engMsg, null).log(sCtx);
                throw new HomeException(manager.get(MSG_KEY_INCOMPLETE, engMsg));
            }

            if (buttonRenderer.isButton(sCtx, bulkloadButtonText))
            {
                //Validate form information
                bulkloadMgr.validate(sCtx, form);
                
                //Perform bulkloading
                bulkloadMgr.bulkload(sCtx, form);
            }
        }
        catch (final IllegalPropertyArgumentException e)
        {
            exceptions.thrown(e);
        }
        catch (final IllegalStateException e)
        {
            exceptions.thrown(e);
        }
        catch (final Throwable t)
        {
            exceptions.thrown(t);
            new MajorLogMsg(this, "Unexpected problem occured during Generic Bean Bulkloading.", t).log(sCtx);
        }

        sCtx.put("MODE", OutputWebControl.EDIT_MODE);

        final FormRenderer formRenderer =
            (FormRenderer) sCtx.get(FormRenderer.class, DefaultFormRenderer.instance());

        formRenderer.Form(out, sCtx);

        out.print("<table>");
        if (buttonRenderer.isButton(sCtx, bulkloadButtonText))
        {
            if (!exceptions.hasErrors())
            {
                out.println("<tr><td>&nbsp;</td></tr><tr><td align=\"center\"><b style=\"color:green;\">");
                out.print("The Bulkloading process has begun.<br> To follow the progress of the Bulkloading check the logs in the Report Directory.");
                out.println("</b></td></tr><tr><td>&nbsp;</td></tr>");
            }
            else
            {   
                //Display errors on the Screen
                exceptions.toWeb(sCtx, out, "", form);
                out.print("</td></tr><tr><td>&nbsp;</td></tr>");
            }
            out.print("<tr><td>");
        }

        //Display GenericBeanBulkloadRequest Form
        webControl_.toWeb(sCtx, out, "", form);

        out.print("</td></tr><tr><th align=\"right\">");
        
        //If process button has been pushed do not allow the process button to be displayed again.
        buttonRenderer.inputButton(out, sCtx, this.getClass(), bulkloadButtonText, false);
        
        BeanWebController.outputHelpLink(sCtx, req, res, out, buttonRenderer);

        out.print("</th></tr></table>");

        formRenderer.FormEnd(out);
    }


    /**
     * Returns TRUE if a Bulk Loader instance is running.
     * A way to determine if it is running is to check if the Loggers are still running, since they are the last things shutdown.
     * @param ctx
     * @return
     * @throws HomeException
     */
    private boolean isBulkloaderRunning(Context ctx) 
        throws HomeException
    {
        if (LifecycleSupport.getLifecycleAgentControl(ctx, BulkloadConstants.GENERIC_BULKLOADER_LOG_FILE_LOGGER) != null
                || LifecycleSupport.getLifecycleAgentControl(ctx, BulkloadConstants.GENERIC_BULKLOADER_ERROR_FILE_LOGGER) != null)
        {
            return true;
        }
        return false;
    }

    /**
     * The webcontrol used to represent the form.
     */
    protected final GenericBeanBulkloaderRequestWebControl webControl_;


} // class
