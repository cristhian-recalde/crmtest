/*
 * Created on Apr 1, 2003
 * 
 * Copyright (c) 1999-2003 REDKNEE.com. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of
 * REDKNEE.com. ("Confidential Information"). You shall not disclose such
 * Confidential Information and shall use it only in accordance with the
 * terms of the license agreement you entered into with REDKNEE.com.
 *
 * REDKNEE.COM MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE
 * SUITABILITY OF THE SOFTWARE, EITHCDR EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE IMPLIED WARRANTIES OF MCDRCHANTABILITY, FITNESS FOR
 * A PARTICULAR PURPOSE, OR NON-INFRINGEMENT. REDKNEE.COM SHALL NOT BE
 * LIABLE FOR ANY DAMAGES SUFFCDRED BY LICENSEE AS A RESULT OF USING,
 * MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS DCDRIVATIVES.
 */
package com.trilogy.app.crm.bas.promotion.web;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Iterator;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.trilogy.app.crm.bas.promotion.HandsetPromotionDatesValidator;
import com.trilogy.app.crm.bas.promotion.PromotionAgent;
import com.trilogy.app.crm.bas.promotion.PromotionFactory;
import com.trilogy.app.crm.bas.promotion.processor.ERHandler;
import com.trilogy.app.crm.bas.promotion.processor.PromotionProcessor;
import com.trilogy.app.crm.bas.promotion.summary.SummaryBundle;
import com.trilogy.app.crm.bean.HandsetPromotionGeneration;
import com.trilogy.app.crm.bean.HandsetPromotionGenerationWebControl;
import com.trilogy.app.crm.bean.PromotionGenerationTypeEnum;
import com.trilogy.framework.xhome.beans.ExceptionListener;
import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.language.MessageMgr;
import com.trilogy.framework.xhome.web.renderer.ButtonRenderer;
import com.trilogy.framework.xhome.web.renderer.DefaultButtonRenderer;
import com.trilogy.framework.xhome.web.renderer.DefaultFormRenderer;
import com.trilogy.framework.xhome.web.renderer.FormRenderer;
import com.trilogy.framework.xhome.webcontrol.*;

/**
 * @author kwong
 *
 */
public class PromotionGenerationRequestServicer
    implements RequestServicer
{
    
    final static public String MODULE = "PromotionGenerationRequestServicer";
    
    /**
     * Creates a new PromotionGenerationRequestServicer.
     */
    public PromotionGenerationRequestServicer()
    {
        webControl_ = new HandsetPromotionGenerationWebControl();
    }
    
    
    // INHERIT
    public void service(
        final Context ctx,
        final HttpServletRequest req,
        final HttpServletResponse res)
        throws ServletException, IOException
    {
        final PrintWriter out = res.getWriter();     
        final Context subContext = ctx.createSubContext();
        subContext.put("MODE", OutputWebControl.EDIT_MODE);
        
        MessageMgr            mmgr      = new MessageMgr(subContext, MODULE);
        HTMLExceptionListener hel       = new HTMLExceptionListener(mmgr);
        subContext.put(ExceptionListener.class,  hel);
        
        final HandsetPromotionGeneration form = new HandsetPromotionGeneration();
        
        String msg = null;
        boolean msgIsProblemReport = false;
        
        final ButtonRenderer buttonRenderer =
            (ButtonRenderer)subContext.get(
                ButtonRenderer.class,
                DefaultButtonRenderer.instance());

        if (buttonRenderer.isButton(subContext, "Preview"))
        {
            webControl_.fromWeb(subContext, form, req, "");
            HandsetPromotionDatesValidator.instance().validate(subContext, form);
        }
        else if (buttonRenderer.isButton(subContext, "Generate"))
        {   
            try 
            {
                webControl_.fromWeb(subContext, form, req, "");         
                HandsetPromotionDatesValidator.instance().validate(subContext, form);
                if (hel.hasErrors())
                {
                    hel.toWeb(subContext, out, null, null);
                }
                else
                {            
                    switch (form.getType().getIndex())
                    {
                        case PromotionGenerationTypeEnum.SPID_INDEX:
                        {
                            // get collection of summary bundle from promotion agent searching by spid
                            Collection summaries = new PromotionAgent(
                                    PromotionFactory.generateBySpid(form.getSpid(), 
                                                                    form.getGenerationDate(), false)).summarize();
                            for (Iterator sumIter = summaries.iterator(); sumIter.hasNext();)
                            {
                                new PromotionProcessor(subContext, (SummaryBundle)sumIter.next(), new ERHandler(subContext)).execute();
                            }
     
                            msg = "Usage report process finished for service provider " + form.getSpid() + ".  ";                    
                            break;
                        }
                        case PromotionGenerationTypeEnum.SUBSCRIBER_INDEX:
                        {
                            // get collection of summary bundle from promotion agent searching by subscriber id
                            // TODO: It is for rkadm use only. In the future, a time range should be added to avoid the report
                            //  generated from all calldetails 
                            Collection summaries = new PromotionAgent(
                                        PromotionFactory.generateBySubID(form.getSubscriber(), 
                                                                        form.getGenerationDate())).summarize();
                            for (Iterator sumIter = summaries.iterator(); sumIter.hasNext();)
                            {
                                new PromotionProcessor(subContext, (SummaryBundle)sumIter.next(), new ERHandler(subContext)).execute();
                            }
                        
                            msg = "Usage report process finished for subscriber " + form.getSubscriber() + ".  ";
                            break;
                        }
                        default:
                        {
                            throw new IllegalStateException(
                                "Could not generate handset promotion.  Unknown identifier type selected.");
                        }
                    }    
                }            
            }
            catch (final Exception e)
            {
                msg = e.getMessage();
                msgIsProblemReport = true;
            }
        }

        final FormRenderer formRenderer =
            (FormRenderer)subContext.get(
                FormRenderer.class,
                DefaultFormRenderer.instance());
        
        formRenderer.Form(out, subContext);

        out.print("<table>");
        if ( msg != null )
        {
            if (!msgIsProblemReport)
            {
                out.println("<tr><td align=\"center\"><b style=\"color:green;\">");
            }
            else
            {
                out.println("<tr><td align=\"center\"><b style=\"color:red;\">");
            }

            out.print(msg);
            
            out.println("</b></td></tr>");
        }

        out.print("<tr><td>");
        webControl_.toWeb(subContext, out, "", form);

        out.print("</td></tr><tr><th align=\"right\">");

        buttonRenderer.inputButton(out, subContext, this.getClass(), "Preview", false);
        buttonRenderer.inputButton(out, subContext, this.getClass(), "Generate", false);
        outputHelpLink(subContext, out);

        out.print("</th></tr></table>");

        formRenderer.FormEnd(out);
    }

    /**
     * Calls com.redknee.framework.xhome.webcontrol.BeanWebController.outputHelpLink()
     *
     * @param context the current context
     * @param out the current PrintWriter
     */
    private void outputHelpLink(final Context context, final PrintWriter out)
    {
        final ButtonRenderer buttonRenderer =
            (ButtonRenderer)context.get(
                ButtonRenderer.class,
                DefaultButtonRenderer.instance());

        // in the future we might need to specify the HttpServletRequest and HttpServletResponse
        BeanWebController.outputHelpLink(context, null, null, out, buttonRenderer);
    }
    
    /**
     * The webcontrol used to represent the form.
     */
    protected final HandsetPromotionGenerationWebControl webControl_;
    
    
} // class
