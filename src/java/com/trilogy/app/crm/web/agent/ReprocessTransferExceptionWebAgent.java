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
import com.trilogy.framework.xhome.webcontrol.AbstractWebControl;
import com.trilogy.framework.xhome.webcontrol.HTMLExceptionListener;
import com.trilogy.framework.xhome.webcontrol.OutputWebControl;
import com.trilogy.framework.xhome.webcontrol.ViewModeEnum;
import com.trilogy.framework.xhome.webcontrol.WebControl;
import com.trilogy.framework.xhome.webcontrol.XTestIgnoreWebControl;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.OMLogMsg;

import com.trilogy.app.crm.Common;
import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.transfer.ReprocessTransferExceptionService;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.app.crm.support.TransferExceptionSupport;
import com.trilogy.app.crm.transfer.TransferException;
import com.trilogy.app.crm.transfer.TransferExceptionHome;
import com.trilogy.app.crm.transfer.TransferExceptionSearch;
import com.trilogy.app.crm.transfer.TransferExceptionSearchWebControl;
import com.trilogy.app.crm.transfer.TransferExceptionSearchXInfo;
import com.trilogy.app.crm.transfer.TransferExceptionTableWebControl;
import com.trilogy.app.crm.transfer.TransferExceptionWebControl;
import com.trilogy.app.crm.transfer.TransferExceptionXInfo;
import com.trilogy.app.crm.transfer.TransferFailureStateEnum;
import com.trilogy.app.crm.web.action.ReprocessTransferExceptionAction;

/** 
 * Web Agent for the "Transfer Exceptions" screen.
 * This screen will show the Table View of all existing Transfer Exceptions.
 * In the Transfer Exception bean's detailed view, the Reprocess option will be
 * available.  This "Reprocess" option will trigger Transfer Exception 
 * Reprocessing (attempting the payment again).
 *
 */
public class ReprocessTransferExceptionWebAgent extends ServletBridge implements WebAgent 
{

    public ReprocessTransferExceptionWebAgent()
    {
        tableWebControl_ = new TransferExceptionTableWebControl();
        detailWebControl_ = new TransferExceptionWebControl();
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
        MessageMgr mmgr = new MessageMgr(ctx, ReprocessTransferExceptionWebAgent.class);
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
        else if (buttonRenderer_.isButton(subCtx, DELETE))
        {
            displayDeleteResult(subCtx, req, res, out, mmgr, hel);
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
        list.add(new ReprocessTransferExceptionAction());
        ActionMgr.setActions(ctx, list);
        return list;
    }
    
    /**
     * Displays the selected Transfer Exception in the Detailed View. 
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
        Long transferExceptionID = Long.valueOf(req.getParameter(RECORD_ID));
        
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
              transferExceptionID +
              "\" />");
           out.println(XTestIgnoreWebControl.IGNORE_END);
        }
        
        out.print("<table>");
        out.print("<tr><td>");
        displayBean(context, transferExceptionID, out, mmgr, hel);
        
        try
        {
            TransferException transferException = TransferExceptionSupport.getTransferExceptionByID(context, transferExceptionID);

            // Display Buttons
            displayActionButtons(context, out, transferException);
            
        } catch (HomeException e)
        {
        }        
        out.print("</td></tr></table>");

        formRenderer.FormEnd(out);

    }
    
    /**
     * Display the selected Transfer Exception Bean
     * @param context
     * @param req
     * @param out
     * @return
     * @throws AgentException
     */
    private void displayBean(
            Context context, 
            Long transferExceptionID, 
            PrintWriter out,
            MessageMgr mmgr,
            HTMLExceptionListener hel)
    {
        TransferException record = null;
        try
        {
            record = TransferExceptionSupport.getTransferExceptionByID(context, transferExceptionID);    
        }
        catch (HomeException e)
        {
            String msg = "Failed to find Transfer Exception with ID=" + transferExceptionID;
            AgentException ae = new AgentException(msg);
            ae.initCause(e);
            /* Pass in NULL TransferException record, since the record should not be updated for errors 
             * that occur due to DISPLAY actions */
            logException(context, null, ae, hel);
            
            out.println(XTestIgnoreWebControl.IGNORE_BEGIN);
            out.println(mmgr.get(
               "UnableToDisplayTransferExceptionEntry",
               "<font color=\"red\"><b>Unable to Display Transfer Exception Entry: ''{0}''</b></font>",
               new Object[] { String.valueOf(transferExceptionID) }));
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
     * Display the action buttons for Transfer Exception Reprocessing screen.
     * @param context
     * @param out
     */
    private void displayActionButtons(Context context, PrintWriter out, TransferException exception)
    {
        if (exception.getState().equals(TransferFailureStateEnum.FAILED))
        {
            buttonRenderer_.inputButton(out, context, this.getClass(), REPROCESS, false, "try{return confirm('Proceed with Reprocess?');}catch(everything){}");
            buttonRenderer_.inputButton(out, context, this.getClass(), DELETE, false, "try{return confirm('Proceed with Delete?');}catch(everything){}");            
        }
    }
    
    private void displayReprocessResult(
            Context context,
            HttpServletRequest req,
            HttpServletResponse res,
            PrintWriter out,
            MessageMgr mmgr,
            HTMLExceptionListener hel)
    {
        Long transferExceptionID = Long.valueOf(req.getParameter(RECORD_ID));
        
        TransferException recordToProcess = null;
        
        try
        {
            /* Retrieve the record from the DB, since the bean has mostly READ-ONLY fields,
             * and the fromWeb won't return values for those fields. */
            recordToProcess = TransferExceptionSupport.getTransferExceptionByID(context, transferExceptionID);    
            //The following bean has the request from the GUI. Namely SelectedSubcriberID
            TransferException modifiedPE = (TransferException)detailWebControl_.fromWeb(context, req, DETAILED_RECORD);
            
            // Reprocess the Transfer exception Record.
            reprocessTransferException(context, req, hel, recordToProcess);
            
            // Display Success Message
            if (!hel.hasErrors())
            {
                out.println(XTestIgnoreWebControl.IGNORE_BEGIN);
                out.println(mmgr.get(
                        "SuccessfulProcessTransferExceptionEntry",
                        "<font color=\"green\"><b>Successfully processed Transfer Exception Entry: ''{0}''. <br/>" +
                        "This entry has been marked as CORRECTED from the Transfer Exception records.</b></font>",
                        new Object[] { String.valueOf(transferExceptionID) }));
                out.println(XTestIgnoreWebControl.IGNORE_END);
            }
        }
        catch (HomeException e)
        {
            String msg = "Failed to find Transfer Exception with ID=" + transferExceptionID;
            AgentException ae = new AgentException(msg);
            ae.initCause(e);
            /* Pass in NULL TransferException record, since the record should not be updated for errors 
             * that occur due to DISPLAY actions */
            logException(context, null, ae, hel);
        }
        
        // Display Errors, if any
        if (hel.hasErrors())
        {
            out.println(XTestIgnoreWebControl.IGNORE_BEGIN);
            out.println(mmgr.get(
               "UnableToProcessTransferExceptionEntry",
               "<font color=\"red\"><b>Unable to process Transfer Exception Entry: ''{0}''</b></font>",
               new Object[] { String.valueOf(transferExceptionID) }));
            out.println(XTestIgnoreWebControl.IGNORE_END);
            
            displayBean(context, transferExceptionID, out, mmgr, hel);
        }
        
    }
    
    /**
     * Display the all Transfer Exceptions in table view.
     * @param context
     * @param out
     * @throws AgentException
     */
    private void displayTableView(Context context, PrintWriter out, HttpServletRequest req, boolean search)
        throws AgentException
    {
        try
        {
            Home home = (Home) context.get(TransferExceptionHome.class);
            
            TransferExceptionSearchWebControl control = new TransferExceptionSearchWebControl();
                        
            Context subContext = context.createSubContext();
            subContext.put("MODE", OutputWebControl.EDIT_MODE);

            // Creating bean with default limit
            TransferExceptionSearch bean = new TransferExceptionSearch();
            bean.setLimit(DEFAULT_SEARCH_LIMIT);
            
            // Hide BAN and MSISDN search criteria for Account Management menu screen
            String cmd = WebAgents.getParameter(context, "cmd");
            if (cmd.equals("SubMenuAccountTransferException"))
            {
                Account    account  = (Account) context.get(Account.class);
                if (account != null)
                {
                    AbstractWebControl.setMode(context, TransferExceptionSearchXInfo.BAN, ViewModeEnum.NONE);
                    AbstractWebControl.setMode(context, TransferExceptionSearchXInfo.MSISDN, ViewModeEnum.NONE);
                    bean.setBan(account.getBAN());
                }
            }
            
            // Retrieving bean from web when clear was not pressed
            if (search)
            {
                control.fromWeb(context, bean, req, SEARCH_TABLE); 
            }

            // Creating search predicate
            And predicate = createTransferExceptionSearchPredicate(bean);
  
            // Writing the transfer exception search table
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
            AgentException ae = new AgentException("Failed to retrieve all Transfer Exceptions.");
            ae.initCause(e);
            throw ae;
        }
    }
    
    /**
     * Calls the ReprocessTransferExceptionService in order to 
     * attempt to create the Transfer Transaction described in the 
     * Transfer Exception.
     * @param context
     * @param req
     * @param hel
     * @param record - in the case this record has type MULTISUB,
     * the record's selectedSubID will be used to determine to which subscriber the 
     * transaction will be applied
     * @throws AgentException
     */
    private void reprocessTransferException(
            Context context, 
            HttpServletRequest req,
            HTMLExceptionListener hel,
            TransferException record)
    {
        try
        {
            ReprocessTransferExceptionService.instance().service(context, record);    
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
    
    private void displayDeleteResult(
            Context context,
            HttpServletRequest req,
            HttpServletResponse res,
            PrintWriter out,
            MessageMgr mmgr,
            HTMLExceptionListener hel)
    {
        Long transferExceptionID = Long.valueOf(req.getParameter(RECORD_ID));
        
        TransferException recordToProcess = null;
        
        try
        {
            /* Retrieve the record from the DB, since the bean has mostly READ-ONLY fields,
             * and the fromWeb won't return values for those fields. */
            recordToProcess = TransferExceptionSupport.getTransferExceptionByID(context, transferExceptionID);    
            
            // Reprocess the Transfer exception Record.
            deleteTransferException(context, req, hel, recordToProcess);
            
            // Display Success Message
            if (!hel.hasErrors())
            {
                out.println(XTestIgnoreWebControl.IGNORE_BEGIN);
                out.println(mmgr.get(
                        "DeleteProcessTransferExceptionEntry",
                        "<font color=\"green\"><b>Successfully processed Transfer Exception Entry: ''{0}''. <br/>" +
                        "This entry has been marked as DELETED from the Transfer Exception records.</b></font>",
                        new Object[] { String.valueOf(transferExceptionID) }));
                out.println(XTestIgnoreWebControl.IGNORE_END);
            }
        }
        catch (HomeException e)
        {
            String msg = "Failed to find Transfer Exception with ID=" + transferExceptionID;
            AgentException ae = new AgentException(msg);
            ae.initCause(e);
            /* Pass in NULL TransferException record, since the record should not be updated for errors 
             * that occur due to DISPLAY actions */
            logException(context, null, ae, hel);
        }
        
        // Display Errors, if any
        if (hel.hasErrors())
        {
            out.println(XTestIgnoreWebControl.IGNORE_BEGIN);
            out.println(mmgr.get(
               "UnableToProcessTransferExceptionEntry",
               "<font color=\"red\"><b>Unable to process Transfer Exception Entry: ''{0}''</b></font>",
               new Object[] { String.valueOf(transferExceptionID) }));
            out.println(XTestIgnoreWebControl.IGNORE_END);
            
            displayBean(context, transferExceptionID, out, mmgr, hel);
        }
        
    }
    
    
    /**
     * Marks the transfer exception record state as DELETED but does not delete the entry
     * 
     * @param context
     * @param req
     * @param hel
     * @param record
     */
    private void deleteTransferException(
            Context context,
            HttpServletRequest req,
            HTMLExceptionListener hel,
            TransferException record)
    {
        try
        {
            // Update Transfer Exception after successful transaction save
            if (LogSupport.isDebugEnabled(context))
            {
                LogSupport.debug(context, this, "Marking the Transfer Exception record=" + record.getId() + " as DELETED");
            }
            
            TransferExceptionSupport.updateTransferExceptionState(context, record, TransferFailureStateEnum.DELETED);

            if (LogSupport.isDebugEnabled(context))
            {
                LogSupport.debug(context, this, "Successfully marked the Transfer Exception record=" + record.getId() + " as DELETED");
            }
            
            //Log OMs for Transfer Exception Resolution.
            new OMLogMsg(Common.OM_MODULE, Common.OM_TRANSFER_EXCEPTION_UPDATED, 1).log(context);
        }
        catch (HomeException e)
        {
            logException(context, record, e, hel);
        }
    }
    
    /**
     * Updates the Transfer Exception Record attempt counters
     * and logs the given exception to the exception listener (to be 
     * displayed on screen).
     * @param context
     * @param record  if not null, then update the attempt counters.
     * @param exception
     * @param hel
     */
    private void logException(
            Context context, 
            TransferException record, 
            Exception exception, 
            HTMLExceptionListener hel)
    {
        hel.thrown((exception.getCause() != null ? exception.getCause() : exception));
        if (record != null)
        {
            try
            {
                TransferExceptionSupport.updateTransferExceptionCounters(context, record.getId(), (exception.getCause() != null ? exception.getCause() : exception));
            }
            catch(HomeException e)
            {
                hel.thrown(e);
            }
        }
    }
    
    /**
     * Creates a transfer exception search predicate based on the content of the received
     * bean.
     * 
     * @param bean
     *            transfer exception search bean.
     * @return And predicate.
     */
    private And createTransferExceptionSearchPredicate(TransferExceptionSearch bean)
    {
        And predicate = new And();
        if (bean.getLimit() > 0)
        {
            predicate.add(new Limit(Integer.valueOf(bean.getLimit())));
        }
        if (bean.getExceptionStart() != null)
        {
            predicate.add(new GTE(TransferExceptionXInfo.TRANS_DATE, CalendarSupportHelper.get().getDateWithNoTimeOfDay(bean
                    .getExceptionStart())));
        }
        if (bean.getExceptionEnd() != null)
        {
            predicate.add(new LTE(TransferExceptionXInfo.TRANS_DATE, CalendarSupportHelper.get().getDateWithLastSecondofDay(bean
                    .getExceptionEnd())));
        }
        if ((bean.getBan() != null) && (bean.getBan().trim().length() > 0))
        {
            predicate.add(new EQ(TransferExceptionXInfo.BAN, bean.getBan()));
        }
        if ((bean.getMsisdn() != null) && (bean.getMsisdn().trim().length() > 0))
        {
            predicate.add(new EQ(TransferExceptionXInfo.MSISDN, bean.getMsisdn()));
        }
        if(bean.getSubscriptionType() != TransferExceptionSearch.DEFAULT_SUBSCRIPTIONTYPE)
        {
            predicate.add(new EQ(TransferExceptionXInfo.SUBSCRIPTION_TYPE, bean.getSubscriptionType()));
        }
        return predicate;
    }


    public static final String CLEAR = "Clear";
    public static final String SEARCH = "Search";
    public static final String REPROCESS = "Reprocess";
    public static final String DELETE = "Delete";
    public static final String DETAILS = "Details";
    public static final String ACTION = "CMD";
    public static final String RECORD_ID = "RECORD_ID";
    private static final int DEFAULT_SEARCH_LIMIT = 25;
    private static final String DETAILED_RECORD = "";
    private static final String SEARCH_TABLE = "TransferExceptionSearchTable";
    private static final String RESULT_TABLE = "TransferExceptionSearchResultTable";
    private TransferExceptionTableWebControl tableWebControl_;
    private TransferExceptionWebControl detailWebControl_;
    private ButtonRenderer buttonRenderer_;
}
