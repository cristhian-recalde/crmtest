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
package com.trilogy.app.crm.web.agent;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.trilogy.app.crm.bean.payment.PaymentException;
import com.trilogy.app.crm.bean.payment.PaymentExceptionHome;
import com.trilogy.app.crm.bean.payment.PaymentExceptionSearch;
import com.trilogy.app.crm.bean.payment.PaymentExceptionSearchWebControl;
import com.trilogy.app.crm.bean.payment.PaymentExceptionTableWebControl;
import com.trilogy.app.crm.bean.payment.PaymentExceptionWebControl;
import com.trilogy.app.crm.bean.payment.PaymentExceptionXInfo;
import com.trilogy.app.crm.bean.payment.PaymentFailureTypeEnum;
import com.trilogy.app.crm.bean.payment.ReprocessPaymentExceptionService;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.app.crm.support.PaymentExceptionSupport;
import com.trilogy.app.crm.web.action.ReprocessPaymentExceptionAction;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.GTE;
import com.trilogy.framework.xhome.elang.LTE;
import com.trilogy.framework.xhome.elang.Limit;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.language.MessageMgr;
import com.trilogy.framework.xhome.web.action.ActionMgr;
import com.trilogy.framework.xhome.web.agent.ServletBridge;
import com.trilogy.framework.xhome.web.agent.WebAgent;
import com.trilogy.framework.xhome.web.agent.WebAgents;
import com.trilogy.framework.xhome.web.renderer.ButtonRenderer;
import com.trilogy.framework.xhome.web.renderer.DefaultButtonRenderer;
import com.trilogy.framework.xhome.web.renderer.DefaultFormRenderer;
import com.trilogy.framework.xhome.web.renderer.FormRenderer;
import com.trilogy.framework.xhome.webcontrol.HTMLExceptionListener;
import com.trilogy.framework.xhome.webcontrol.OutputWebControl;
import com.trilogy.framework.xhome.webcontrol.WebControl;
import com.trilogy.framework.xhome.webcontrol.XTestIgnoreWebControl;

/** 
 * Web Agent for the "Payment Exceptions" screen.
 * This screen will show the Table View of all existing Payment Exceptions.
 * In the Payment Exception bean's detailed view, the Reprocess option will be
 * available.  This "Reprocess" option will trigger Payment Exception 
 * Reprocessing (attempting the payment again).
 *
 * @author Angie Li
 */
public class ReprocessPaymentExceptionWebAgent extends ServletBridge implements WebAgent 
{

    public ReprocessPaymentExceptionWebAgent()
    {
        tableWebControl_ = new PaymentExceptionTableWebControl();
        detailWebControl_ = new PaymentExceptionWebControl();
        buttonRenderer_ =  DefaultButtonRenderer.instance();
    }
    
    public void execute(Context ctx) throws AgentException 
    {
        buttonRenderer_ = (ButtonRenderer) ctx.get(ButtonRenderer.class,
                DefaultButtonRenderer.instance());
        
        //Retrieve output stream
        PrintWriter out = getWriter(ctx);
        HttpServletRequest req = getRequest(ctx);
        HttpServletResponse res = getResponse(ctx);
        MessageMgr mmgr = new MessageMgr(ctx, ReprocessPaymentExceptionWebAgent.class);
        HTMLExceptionListener hel = new HTMLExceptionListener(mmgr);
        
        Context subCtx = ctx.createSubContext();
        
        subCtx.put("MODE", WebControl.DISPLAY_MODE);
        
        String action = req.getParameter(ACTION);
        
        if ((action != null))
        {
            if (action.equalsIgnoreCase(DETAILS))
            {
                //Bean
                displayDetailedView(subCtx, req, out, mmgr, hel);
            }
        }
        else if (buttonRenderer_.isButton(subCtx, REPROCESS))
        {
            displayReprocessResult(subCtx, req, res, out, mmgr, hel);
        }
        else if (buttonRenderer_.isButton(subCtx, CLEAR))
        {
            boolean search = false;
            displayTableView(subCtx, out, req, search);
        }
        else
        {
            boolean search = true;
            displayTableView(subCtx, out, req, search);
        }
    }
    
    /**
     * Sets the Actions for the Table Web Control.
     * @param ctx
     */
    private List setActions(Context ctx)
    {
        List list = new ArrayList();
        list.add(new ReprocessPaymentExceptionAction());
        ActionMgr.setActions(ctx, list);
        return list;
    }
    
    /**
     * Displays the selected Payment Exception in the Detailed View. 
     * @param context
     * @param req
     * @param out
     * @throws AgentException
     */
    private void displayDetailedView(
            Context context,
            HttpServletRequest req, 
            PrintWriter out,
            MessageMgr mmgr,
            HTMLExceptionListener hel)
        throws AgentException
    {
        context.put("MODE", WebControl.EDIT_MODE);
        Long paymentExceptionID = Long.valueOf(req.getParameter(RECORD_ID));
        
        final FormRenderer formRenderer =
            (FormRenderer)context.get(
                FormRenderer.class,
                DefaultFormRenderer.instance());
        formRenderer.Form(out, context);

        {
           out.println(XTestIgnoreWebControl.IGNORE_BEGIN);
           out.println(
              "<input type=\"hidden\" name=\"" +
              WebAgents.rewriteName(context, RECORD_ID) +
              "\" value=\"" +
              paymentExceptionID +
              "\" />");
           out.println(XTestIgnoreWebControl.IGNORE_END);
        }
        
        out.print("<table>");
        out.print("<tr><td>");
        displayBean(context, paymentExceptionID, out, mmgr, hel);
        
        try
        {
            PaymentException paymentException = PaymentExceptionSupport.getPaymentExceptionByID(context, paymentExceptionID);
            if (!paymentException.getType().equals(PaymentFailureTypeEnum.PREPAID))
            {
                // Display Buttons
                displayActionButtons(context, out);
            }
        } catch (HomeException e)
        {
            // Display Buttons
            displayActionButtons(context, out);
        }        
        out.print("</td></tr></table>");

        formRenderer.FormEnd(out);

    }
    
    /**
     * Display the selected Payment Exception Bean
     * @param context
     * @param req
     * @param out
     * @return
     * @throws AgentException
     */
    private void displayBean(
            Context context, 
            Long paymentExceptionID, 
            PrintWriter out,
            MessageMgr mmgr,
            HTMLExceptionListener hel)
    {
        PaymentException record = null;
        try
        {
            record = PaymentExceptionSupport.getPaymentExceptionByID(context, paymentExceptionID);    
        }
        catch (HomeException e)
        {
            String msg = "Failed to find Payment Exception with ID=" + paymentExceptionID;
            AgentException ae = new AgentException(msg);
            ae.initCause(e);
            /* Pass in NULL PaymentException record, since the record should not be updated for errors 
             * that occur due to DISPLAY actions */
            logException(context, null, ae, hel);
            
            out.println(XTestIgnoreWebControl.IGNORE_BEGIN);
            out.println(mmgr.get(
               "UnableToDisplayPaymentExceptionEntry",
               "<font color=\"red\"><b>Unable to Display Payment Exception Entry: ''{0}''</b></font>",
               new Object[] { String.valueOf(paymentExceptionID) }));
            out.println(XTestIgnoreWebControl.IGNORE_END);
        }
        finally
        {
            //Display Errors, if any
            if (hel.hasErrors())
            {
                hel.toWeb(context, out, null, null);
            }
            
            if (record != null)
            {
                // wrap the form in a table so the form doesn't expand to 100%
                out.print("<table><tr><td>");
                detailWebControl_.toWeb(context, out, DETAILED_RECORD, record);
                out.println("</td></tr></table>");
            }
        }
    }
    
    /**
     * Display the action buttons for Payment Exception Reprocessing screen.
     * @param context
     * @param out
     */
    private void displayActionButtons(Context context, PrintWriter out)
    {
        buttonRenderer_.inputButton(out, context, REPROCESS);
    }
    
    private void displayReprocessResult(
            Context context,
            HttpServletRequest req,
            HttpServletResponse res,
            PrintWriter out,
            MessageMgr mmgr,
            HTMLExceptionListener hel)
    {
        Long paymentExceptionID = Long.valueOf(req.getParameter(RECORD_ID));
        
        PaymentException recordToProcess = null;
        
        try
        {
            /* Retrieve the record from the DB, since the bean has mostly READ-ONLY fields,
             * and the fromWeb won't return values for those fields. */
            recordToProcess = PaymentExceptionSupport.getPaymentExceptionByID(context, paymentExceptionID);    
            //The following bean has the request from the GUI. Namely SelectedSubcriberID
            PaymentException modifiedPE = (PaymentException)detailWebControl_.fromWeb(context, req, DETAILED_RECORD);
            
            // Modifying the selected subscriber ID when no selection has been made, and there is only 1 option: TT 8042400035
            if (((modifiedPE.getSelectedSubcriberID()==null) || (modifiedPE.getSelectedSubcriberID().trim().length() == 0)) &&
                    (recordToProcess.getSubscriberIds().indexOf("|")<0))
            {
                    recordToProcess.setSelectedSubcriberID(recordToProcess.getSubscriberIds());
            } else 
            {
                recordToProcess.setSelectedSubcriberID(modifiedPE.getSelectedSubcriberID());
            }
            
            // Reprocess the Payment exception Record.
            reprocessPaymentException(context, req, hel, recordToProcess);
            
            // Display Success Message
            if (!hel.hasErrors())
            {
                out.println(XTestIgnoreWebControl.IGNORE_BEGIN);
                out.println(mmgr.get(
                        "SucessfulProcessPaymentExceptionEntry",
                        "<font color=\"green\"><b>Successfully processed Payment Exception Entry: ''{0}''. <br/>" +
                        "This entry has been deleted from the Payment Exception records.</b></font>",
                        new Object[] { String.valueOf(paymentExceptionID) }));
                out.println(XTestIgnoreWebControl.IGNORE_END);
            }
        }
        catch (HomeException e)
        {
            String msg = "Failed to find Payment Exception with ID=" + paymentExceptionID;
            AgentException ae = new AgentException(msg);
            ae.initCause(e);
            /* Pass in NULL PaymentException record, since the record should not be updated for errors 
             * that occur due to DISPLAY actions */
            logException(context, null, ae, hel);
        }
        
        // Display Errors, if any
        if (hel.hasErrors())
        {
            out.println(XTestIgnoreWebControl.IGNORE_BEGIN);
            out.println(mmgr.get(
               "UnableToProcessPaymentExceptionEntry",
               "<font color=\"red\"><b>Unable to process Payment Exception Entry: ''{0}''</b></font>",
               new Object[] { String.valueOf(paymentExceptionID) }));
            out.println(XTestIgnoreWebControl.IGNORE_END);
            
            displayBean(context, paymentExceptionID, out, mmgr, hel);
        }
        
    }
    
    /**
     * Display the all Payment Exceptions in table view.
     * @param context
     * @param out
     * @throws AgentException
     */
    private void displayTableView(Context context, PrintWriter out, HttpServletRequest req, boolean search)
        throws AgentException
    {
        try
        {
            Home home = (Home) context.get(PaymentExceptionHome.class);
            
            PaymentExceptionSearchWebControl control = new PaymentExceptionSearchWebControl();
            
            Context subContext = context.createSubContext();
            subContext.put("MODE", OutputWebControl.EDIT_MODE);
            
            // Creating bean with default limit
            PaymentExceptionSearch bean = new PaymentExceptionSearch();
            bean.setLimit(DEFAULT_SEARCH_LIMIT);
            
            // Retrieving bean from web when clear was not pressed
            if (search)
            {
                control.fromWeb(context, bean, req, SEARCH_TABLE); 
            }

            // Creating search predicate
            And predicate = createPaymentExceptionSearchPredicate(bean);
  
            // Writing the payment exception search table
            final FormRenderer formRenderer = (FormRenderer) subContext.get(FormRenderer.class, DefaultFormRenderer
                    .instance());

            formRenderer.Form(out, subContext);
            
            out.print("<table><tr><td>");
            control.toWeb(subContext, out, SEARCH_TABLE, bean);
            out.println("</td></tr>");
            out.println("<tr><td><table><tr><td width=100%></td><td>");
            buttonRenderer_.inputButton(out, context, SEARCH);
            out.print("</td><td>");
            buttonRenderer_.inputButton(out, context, CLEAR);
            out.println("</td></tr></table>");
            out.println("</td></tr></table>");
            
            formRenderer.FormEnd(out);

            setActions(context);
            
            // wrap the form in a table so the form doesn't expand to 100%
            out.print("<table><tr><td>");
            tableWebControl_.toWeb(context, out, RESULT_TABLE, home.where(subContext, predicate).selectAll());
            out.println("</td></tr></table>");
        }
        catch(HomeException e)
        {
            AgentException ae = new AgentException("Failed to retrieve all Payment Exceptions.");
            ae.initCause(e);
            throw ae;
        }
    }
    
    /**
     * Calls the ReprocessPaymentExceptionService in order to 
     * attempt to create the Payment Transaction described in the 
     * Payment Exception.
     * @param context
     * @param req
     * @param hel
     * @param record - in the case this record has type MULTISUB,
     * the record's selectedSubID will be used to determine to which subscriber the 
     * transaction will be applied
     * @throws AgentException
     */
    private void reprocessPaymentException(
            Context context, 
            HttpServletRequest req,
            HTMLExceptionListener hel,
            PaymentException record)
    {
        try
        {
            ReprocessPaymentExceptionService.instance().service(context, record);    
        }
        catch(ServletException se)
        {
            logException(context, record, se, hel);
        }
        catch(IOException ioe)
        {
            logException(context, record, ioe, hel);
        }
        catch(NullPointerException ne)
        {
            ServletException se = new ServletException("An unspecified error has occurred.  Please see logs.", ne); 
            logException(context, record, se, hel);
        }
    }
    
    /**
     * Updates the Payment Exception Record attempt counters
     * and logs the given exception to the exception listener (to be 
     * displayed on screen).
     * @param context
     * @param record  if not null, then update the attempt counters.
     * @param exception
     * @param hel
     */
    private void logException(
            Context context, 
            PaymentException record, 
            Exception exception, 
            HTMLExceptionListener hel)
    {
        hel.thrown((exception.getCause() != null ? exception.getCause() : exception));
        if (record != null)
        {
            try
            {
                PaymentExceptionSupport.updatePaymentExceptionCounters(context, record);
            }
            catch(HomeException e)
            {
                hel.thrown(e);
            }
        }
    }
    
    /**
     * Creates a payment exception search predicate based on the content of the received
     * bean.
     * 
     * @param bean
     *            Payment exception search bean.
     * @return And predicate.
     */
    private And createPaymentExceptionSearchPredicate(PaymentExceptionSearch bean)
    {
        And predicate = new And();
        if (bean.getLimit() > 0)
        {
            predicate.add(new Limit(Integer.valueOf(bean.getLimit())));
        }
        if (bean.getExceptionStart() != null)
        {
            predicate.add(new GTE(PaymentExceptionXInfo.TRANS_DATE, CalendarSupportHelper.get().getDateWithNoTimeOfDay(bean
                    .getExceptionStart())));
        }
        if (bean.getExceptionEnd() != null)
        {
            predicate.add(new LTE(PaymentExceptionXInfo.TRANS_DATE, CalendarSupportHelper.get().getDateWithLastSecondofDay(bean
                    .getExceptionEnd())));
        }
        if ((bean.getBan() != null) && (bean.getBan().trim().length() > 0))
        {
            predicate.add(new EQ(PaymentExceptionXInfo.BAN, Integer.valueOf(bean.getBan())));
        }
        if ((bean.getMsisdn() != null) && (bean.getMsisdn().trim().length() > 0))
        {
            predicate.add(new EQ(PaymentExceptionXInfo.MSISDN, bean.getMsisdn()));
        }
        return predicate;
    }


    public static final String CLEAR = "Clear";
    public static final String SEARCH = "Search";
    public static final String REPROCESS = "Reprocess";
    public static final String DETAILS = "Details";
    public static final String ACTION = "CMD";
    public static final String RECORD_ID = "RECORD_ID";
    private static final int DEFAULT_SEARCH_LIMIT = 25;
    private static final String DETAILED_RECORD = "";
    private static final String SEARCH_TABLE = "PaymentExceptionSearchTable";
    private static final String RESULT_TABLE = "PaymentExceptionSearchResultTable";
    private PaymentExceptionTableWebControl tableWebControl_;
    private PaymentExceptionWebControl detailWebControl_;
    private ButtonRenderer buttonRenderer_;
}
