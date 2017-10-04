/*
 * Created on Sep 26, 2005
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
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.web.agent;

import java.io.PrintWriter;

import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bundle.BundleAdjustment;
import com.trilogy.app.crm.bundle.BundleAdjustmentAgent;
import com.trilogy.app.crm.bundle.BundleAdjustmentWebControl;
import com.trilogy.app.crm.support.SpidSupport;
import com.trilogy.framework.xhome.beans.ExceptionListener;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgent;
import com.trilogy.framework.xhome.language.MessageMgr;
import com.trilogy.framework.xhome.web.agent.ServletBridge;
import com.trilogy.framework.xhome.web.agent.WebAgent;
import com.trilogy.framework.xhome.web.renderer.ButtonRenderer;
import com.trilogy.framework.xhome.web.renderer.DefaultButtonRenderer;
import com.trilogy.framework.xhome.web.renderer.DefaultFormRenderer;
import com.trilogy.framework.xhome.web.renderer.FormRenderer;
import com.trilogy.framework.xhome.webcontrol.AbstractWebControl;
import com.trilogy.framework.xhome.webcontrol.HTMLExceptionListener;
import com.trilogy.framework.xhome.webcontrol.WebControl;
import com.trilogy.framework.xlog.log.MinorLogMsg;

public class BundleAdjustmentWebAgent extends ServletBridge implements WebAgent
{

    protected final WebControl wc_ = new BundleAdjustmentWebControl();


    public BundleAdjustmentWebAgent()
    {
        super();
    }


    public void execute(Context ctx) throws AgentException
    {
        PrintWriter out = getWriter(ctx);
        BundleAdjustment form = new BundleAdjustment();
        boolean displayMessage = false;
        ButtonRenderer br = (ButtonRenderer) ctx.get(ButtonRenderer.class, DefaultButtonRenderer.instance());
        Context subCtx = ctx.createSubContext();
        HTMLExceptionListener exceptions = new HTMLExceptionListener(new MessageMgr(ctx, this));

        subCtx.put(ExceptionListener.class, exceptions);
        subCtx.put("MODE", WebControl.EDIT_MODE);
        subCtx.put(AbstractWebControl.NUM_OF_BLANKS, 5);
        
        Subscriber sub  = (Subscriber) ctx.get(Subscriber.class);
        if (sub != null)
	    {	        	
	        try 
			{
				final CRMSpid crmSpid = SpidSupport.getCRMSpid(ctx, sub.getSpid());
				if (crmSpid != null) 
				{
					ctx.put("useSIunit", !crmSpid.isUseIECunits());
				}
			} 
			catch (Throwable e) 
			{
				new MinorLogMsg(this, "CRMSpid home error", e).log(ctx);
			}
	    }        

        if (br.isButton(subCtx, "Send") || br.isButton(subCtx, "Preview"))
        {
            wc_.fromWeb(subCtx, form, getRequest(ctx), "");
        }

        if (br.isButton(subCtx, "Send"))
        {
            if (! exceptions.hasErrors())
            {
                subCtx.put(BundleAdjustment.class, form);
                ContextAgent adj = (ContextAgent) ctx.get(BundleAdjustmentAgent.class);

                try
                {
                    adj.execute(subCtx);
                }
                catch (AgentException e)
                {
                    exceptions.thrown(e);
                }
            }

            displayMessage = true;
        }

        FormRenderer frend = (FormRenderer) ctx.get(FormRenderer.class, DefaultFormRenderer.instance());
        frend.Form(out, ctx);

        // wrap in table so that the form doesn't expand to 100%
        if (exceptions.hasErrors())
        {
            exceptions.toWeb(ctx, out, "", form);
        }
        else
        {
            if (displayMessage)
            {
                out.print("<pre><center><font size=\"1\" face=\"Verdana\" color=\"green\"><b>Bundle Adjustment successfuly made</b></font></center></pre>");
            }
        }

        out.print("<table><tr><td>");
        wc_.toWeb(subCtx, out, "", form);
        br.inputButton(out, subCtx, "Preview");
        br.inputButton(out, subCtx, "Send");
        br.inputButton(out, subCtx, "Clear");
        out.println("</td></tr></table>");

        frend.FormEnd(out);

        out.println("<br/>");
    }

}
