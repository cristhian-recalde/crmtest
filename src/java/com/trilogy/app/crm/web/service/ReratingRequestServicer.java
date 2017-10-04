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
package com.trilogy.app.crm.web.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.trilogy.framework.core.cron.TaskEntry;
import com.trilogy.framework.core.cron.TaskHelper;
import com.trilogy.framework.core.cron.TaskStatusEnum;
import com.trilogy.framework.xhome.beans.ExceptionListener;
import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.beans.XBeans;
import com.trilogy.framework.xhome.beans.xi.PropertyInfo;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.In;
import com.trilogy.framework.xhome.elang.LT;
import com.trilogy.framework.xhome.elang.True;
import com.trilogy.framework.xhome.filter.Predicate;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.language.MessageMgr;
import com.trilogy.framework.xhome.txn.DefaultTransaction;
import com.trilogy.framework.xhome.visitor.AbortVisitException;
import com.trilogy.framework.xhome.visitor.Visitor;
import com.trilogy.framework.xhome.web.renderer.ButtonRenderer;
import com.trilogy.framework.xhome.web.renderer.DefaultButtonRenderer;
import com.trilogy.framework.xhome.web.renderer.DefaultFormRenderer;
import com.trilogy.framework.xhome.web.renderer.FormRenderer;
import com.trilogy.framework.xhome.webcontrol.BeanWebController;
import com.trilogy.framework.xhome.webcontrol.HTMLExceptionListener;
import com.trilogy.framework.xhome.webcontrol.OutputWebControl;
import com.trilogy.framework.xhome.webcontrol.RequestServicer;
import com.trilogy.framework.xlog.format.FastERLogMsgFormat;
import com.trilogy.framework.xlog.log.MajorLogMsg;
import com.trilogy.framework.xlog.log.MinorLogMsg;

import com.trilogy.app.crm.agent.CronConstant;
import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountHome;
import com.trilogy.app.crm.bean.AccountXInfo;
import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.CRMSpidHome;
import com.trilogy.app.crm.bean.RerateAlarmConfig;
import com.trilogy.app.crm.bean.RerateCallDetailAlarmTypeEnum;
import com.trilogy.app.crm.bean.RerateConfig;
import com.trilogy.app.crm.bean.RerateForm;
import com.trilogy.app.crm.bean.RerateFormWebControl;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberHome;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.bean.SubscriberXInfo;
import com.trilogy.app.crm.bean.calldetail.CallDetail;
import com.trilogy.app.crm.bean.calldetail.CallDetailHome;
import com.trilogy.app.crm.bean.calldetail.CallDetailXInfo;
import com.trilogy.app.crm.bean.calldetail.CallTypeEnum;
import com.trilogy.app.crm.bean.calldetail.RerateCallDetail;
import com.trilogy.app.crm.bean.calldetail.RerateCallDetailHome;
import com.trilogy.app.crm.bean.calldetail.RerateCallDetailXInfo;
import com.trilogy.app.crm.bean.core.SubscriptionType;
import com.trilogy.app.crm.calculation.support.InvoiceSupport;
import com.trilogy.app.crm.numbermgn.MsisdnMgmtHistory;
import com.trilogy.app.crm.numbermgn.MsisdnMgmtHistoryHome;
import com.trilogy.app.crm.numbermgn.MsisdnMgmtHistoryXInfo;
import com.trilogy.app.crm.rerating.RerateFormDatesValidator;
import com.trilogy.app.crm.support.CallDetailSupportHelper;
import com.trilogy.util.snippet.log.Logger;

/**
 * @author dannyng
 */
public class ReratingRequestServicer implements RequestServicer
{
    static Set sessionSet = new HashSet();

    /**
     * Preview button name
     */
    public static final String PREVIEW_BUTTON_NAME = "Preview";

    /**
     * Extract button name
     */
    public static final String EXTRACT_BUTTON_NAME = "Extract";

    /**
     * Context key for to reference the rerate form
     */
    public static final String RERATE_FORM = "Rerate Form";

    /**
     * Name of file containing extracted CDRs
     */
    public static final String ECP_EXTRACT_FILE_NAME = "CDRExtract.cdr";

    /**
     * Name of file containing extracted CDRs
     */
    public static final String ABM_EXTRACT_FILE_NAME = "CreditExtract";

    /**
     * Name of error file containing extract errors
     */
    public static final String EXTRACT_ERROR_FILE_NAME = "Extract Error";

    /**
     * Back up directory name
     */
    public static final String BACK_UP_DIRECTORY_NAME = "backup";

    /**
     * Default constructor
     */
    public ReratingRequestServicer()
    {
        webControl_ = new RerateFormWebControl();
    }

    /**
     * {@inheritDoc}
     */
    public void service(final Context ctx, final HttpServletRequest req, final HttpServletResponse res)
        throws ServletException, IOException
    {
        final HttpSession session = req.getSession();
        final PrintWriter out = res.getWriter();

        final Context subContext = ctx.createSubContext();
        subContext.put("MODE", OutputWebControl.EDIT_MODE);

        String msg = null;
        boolean msgIsProblemReport = false;

        final ButtonRenderer buttonRenderer = (ButtonRenderer) subContext.get(ButtonRenderer.class,
            DefaultButtonRenderer.instance());

        final MessageMgr manager = new MessageMgr(subContext, this);

        final HTMLExceptionListener exceptions = new HTMLExceptionListener(manager);
        subContext.put(ExceptionListener.class, exceptions);

        final RerateForm form = new RerateForm();

        System.currentTimeMillis();
        webControl_.fromWeb(subContext, form, req, "");

        // verify the dates
        RerateFormDatesValidator.instance().validate(subContext, form);

        if (!exceptions.hasErrors())
        {
            try
            {
                synchronized (sessionSet)
                {
                    if (!sessionSet.add(session.getId()))
                    {
                        throw new HomeException("The previous rerate action isn't complete yet.");
                    }
                }
                // Correct the date so the validation and extraction happends on correct date range
                RerateForm form2 = adjustToDate(form);
                if (buttonRenderer.isButton(subContext, PREVIEW_BUTTON_NAME))
                {
                    validate(subContext, form2);
                }
                else if (buttonRenderer.isButton(subContext, EXTRACT_BUTTON_NAME))
                {
                    validate(subContext, form2);
                    extract(subContext, form2);

                    msg = manager
                        .get(
                            "Rerate.successMessage",
                            "Call details extracted.  See configured extraction directory for extracted files.  Check error file for potential errors.");
                }
            }
            catch (final IllegalPropertyArgumentException exception)
            {
                msgIsProblemReport = true;
                msg = "";
                exceptions.thrown(exception);
            }
            catch (final HomeException exception)
            {
                msgIsProblemReport = true;
                exceptions.thrown(exception);
                msg = exception.getMessage();
                new MajorLogMsg(this, "Problem occured during extraction.", exception).log(subContext);
            }
            catch (final Throwable throwable)
            {
                msgIsProblemReport = true;
                exceptions.thrown(throwable);
                msg = throwable.getMessage();
                new MajorLogMsg(this, "Unexpected problem occured during extraction.", throwable).log(subContext);
            }
        }

        if (exceptions.hasErrors())
        {
            exceptions.toWeb(subContext, out, "", form);
        }

        final FormRenderer formRenderer = (FormRenderer) subContext.get(FormRenderer.class, DefaultFormRenderer
            .instance());

        formRenderer.Form(out, subContext);

        out.print("<table>");
        if (msg != null)
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
        buttonRenderer.inputButton(out, subContext, this.getClass(), "Extract", false);
        outputHelpLink(subContext, out, buttonRenderer);

        out.print("</th></tr></table>");

        formRenderer.FormEnd(out);
        synchronized (sessionSet)
        {
            sessionSet.remove(session.getId());
        }

    }

    private void extract(Context ctx, RerateForm form)
    {
        /*
         * Store the form in the sub context so that subscriber visitor has reference to form so it can deteremine the
         * price plan selected in the form
         */
        Context subCtx = ctx.createSubContext();
        subCtx.put(ReratingRequestServicer.RERATE_FORM, form);

        RerateConfig config = (RerateConfig) ctx.get(RerateConfig.class);

        if (config == null)
        {
            throw new IllegalStateException("Extraction directory not configured.");
        }

        PrintStream ecpOutFile = null;
        PrintStream abmOutFile = null;
        PrintStream errorOut = null;

        try
        {
            ecpOutFile = getPrintStream(ctx, config.getEcpExtractionDirectory(),
                ReratingRequestServicer.ECP_EXTRACT_FILE_NAME);
            abmOutFile = getPrintStream(ctx, config.getAbmExtractionDirectory(),
                ReratingRequestServicer.ABM_EXTRACT_FILE_NAME);
            errorOut = getPrintStream(ctx, config.getErrorDirectory(), ReratingRequestServicer.EXTRACT_ERROR_FILE_NAME);
        }
        catch (Exception e)
        {
            IllegalStateException e1 = new IllegalStateException("Error getting print steam to file system.");
            e1.initCause(e);
            throw e1;
        }

        ctx.put(ReratingRequestServicer.ECP_EXTRACT_FILE_NAME, ecpOutFile);
        ctx.put(ReratingRequestServicer.ABM_EXTRACT_FILE_NAME, abmOutFile);
        ctx.put(ReratingRequestServicer.EXTRACT_ERROR_FILE_NAME, errorOut);

        // Only MSISDN is filled in
        if (form.getMSISDN().compareTo(RerateForm.DEFAULT_MSISDN) != 0
            && form.getBAN().compareTo(RerateForm.DEFAULT_BAN) == 0)
        {
            Home subHome = (Home) ctx.get(SubscriberHome.class);

            try
            {
                Home msisdnHistoryHome = getSubscriberIDsFromMsisdnHistory(ctx, form.getMSISDN(), form.getStartDate(),
                    form.getEndDate());

                // Only 1 sub should be returned for that MSISDN in selected date range
                // So just find the first one
                MsisdnMgmtHistory mh = (MsisdnMgmtHistory) msisdnHistoryHome.find(ctx, True.instance());
                if (mh == null)
                {
                    throw new IllegalStateException("Extraction aborted.  Could not find subscriber with MSISDN "
                        + form.getMSISDN() + ". in selected date range or multiple subscribers have MSISDN "
                        + form.getMSISDN() + " in selected date range.");
                }

                subHome.where(subCtx, new EQ(SubscriberXInfo.ID, mh.getSubscriberId())).forEach(subCtx,
                    new SubscriberHomeVisitor());
            }
            catch (Exception e)
            {
                errorOut.print("Unexpected error: ");
                errorOut.print(e.getMessage());
            }
        }
        // Only BAN is filled in
        else if (form.getBAN().compareTo(RerateForm.DEFAULT_BAN) != 0
            && form.getMSISDN().compareTo(RerateForm.DEFAULT_MSISDN) == 0)
        {
            Home acctHome = (Home) ctx.get(AccountHome.class);

            try
            {
                acctHome.where(subCtx, getPredicate(AccountXInfo.BAN, form)).forEach(subCtx, new AccountHomeVisitor());
            }
            catch (Exception e)
            {
                errorOut.print("Unexpected error: ");
                errorOut.print(e.getMessage());
            }
        }
        // MSISDN and BAN filled in
        else if (form.getBAN().compareTo(RerateForm.DEFAULT_BAN) != 0
            && form.getMSISDN().compareTo(RerateForm.DEFAULT_MSISDN) != 0)
        {
            Home subHome = (Home) ctx.get(SubscriberHome.class);

            And subFilterAnd = new And();
            subFilterAnd.add(getPredicate(SubscriberXInfo.BAN, form));
            subFilterAnd.add(getPredicate(SubscriberXInfo.MSISDN, form));
            // Only rerate postpaid in accordance with HLD
            subFilterAnd.add(new EQ(SubscriberXInfo.SUBSCRIBER_TYPE, SubscriberTypeEnum.POSTPAID));

            try
            {
                subHome.where(subCtx, subFilterAnd).forEach(subCtx, new SubscriberHomeVisitor());
            }
            catch (Exception e)
            {
                errorOut.print("Unexpected error: ");
                errorOut.print(e.getMessage());
            }
        }
        // Spid is filled in
        else if (form.getSpid() != RerateForm.DEFAULT_SPID)
        {
            Home acctHome = (Home) ctx.get(AccountHome.class);
            And filterAnd = new And();

            filterAnd.add(getPredicate(AccountXInfo.SPID, form));
            filterAnd.add(getPredicate(AccountXInfo.BILL_CYCLE_ID, form));

            try
            {
                acctHome.where(subCtx, filterAnd).forEach(subCtx, new AccountHomeVisitor());
            }
            catch (Exception e)
            {
                errorOut.print("Unexpected error: ");
                errorOut.print(e.getMessage());
            }
        }
        // Form was blank!
        else
        {
            /*
             * If spid MSISDN, or BAN, or SPID wasn't filled in then Bill cycle and Price plan couldn't have been filled
             * in because they're spid aware
             */
            throw new IllegalStateException("Extraction aborted.  No extraction criteria was selected.");
        }

        ecpOutFile.close();
        abmOutFile.close();
        errorOut.close();

        // Back up files
        try
        {
            backUpFile(config.getEcpExtractionDirectory(), ReratingRequestServicer.ECP_EXTRACT_FILE_NAME);
            backUpFile(config.getAbmExtractionDirectory(), ReratingRequestServicer.ABM_EXTRACT_FILE_NAME);
            backUpFile(config.getErrorDirectory(), ReratingRequestServicer.EXTRACT_ERROR_FILE_NAME);
        }
        catch (Exception e)
        {
            IllegalStateException i = new IllegalStateException(
                "Extraction complete but an unexpected error was encountered creating extract back ups.  RERATING SHOULD PROCEED AS NOMRAL.");
            i.initCause(e);
            throw i;
        }

        // Fetch alarm agent config
        RerateAlarmConfig alarmConfig = (RerateAlarmConfig) ctx.get(RerateAlarmConfig.class);

        Calendar cal = Calendar.getInstance();

        // Get the number of hours or days to wait before check
        if (alarmConfig.getTimeUnit() == RerateCallDetailAlarmTypeEnum.DAYS)
        {
            cal.add(Calendar.DATE, alarmConfig.getNumDays());
        }
        else if (alarmConfig.getTimeUnit() == RerateCallDetailAlarmTypeEnum.HOURS)
        {
            cal.add(Calendar.HOUR_OF_DAY, alarmConfig.getNumHours());
        }

        // Fetch task
        TaskEntry task = TaskHelper.retrieve(ctx, CronConstant.RERATED_CALL_DETAIL_ALARM_NAME);

        if (task != null)
        {
            // Schedule task according to configuration and save
            task.setStatus(TaskStatusEnum.SCHEDULED);
            task.setNextRun(cal.getTime());
            TaskHelper.store(ctx, task);
        }
        else
        {
            // Task not found
            new MinorLogMsg(this, "Unreceived call detail alarm task not found.", new HomeException(
                "Unreceived call detail alarm task not found.")).log(ctx);
        }
    } // extract()

    private void validate(Context ctx, RerateForm form)
    {
        RerateConfig config = (RerateConfig) ctx.get(RerateConfig.class);
        if (config.getEcpExtractionDirectory().length() == 0 || config.getAbmExtractionDirectory().length() == 0
            || config.getErrorDirectory().length() == 0)
        {
            throw new IllegalStateException("Extraction directory was not configured.");
        }

        // Validate start and end dates
        if (form.getStartDate() == null)
        {
            throw new IllegalStateException("Start date cannot be empty.");
        }
        if (form.getEndDate() == null)
        {
            throw new IllegalStateException("End date cannot be empty.");
        }

        // Put form into context for other visitors
        ctx.put(ReratingRequestServicer.RERATE_FORM, form);

        // Only MSISDN is filled in
        if (form.getMSISDN().compareTo(RerateForm.DEFAULT_MSISDN) != 0
            && form.getBAN().compareTo(RerateForm.DEFAULT_BAN) == 0)
        {
            try
            {
                // Have to look for the subsscriber with this MSISDN in the msisdn history table
                Home msisdnHistoryHome = getSubscriberIDsFromMsisdnHistory(ctx, form.getMSISDN(), form.getStartDate(),
                    form.getEndDate());

                // Only 1 sub should be returned for that MSISDN in selected date range
                // So just find the first one
                MsisdnMgmtHistory mh = (MsisdnMgmtHistory) msisdnHistoryHome.find(ctx, True.instance());
                if (mh == null)
                {
                    throw new IllegalStateException("Extraction aborted.  Could not find subscriber with MSISDN "
                        + form.getMSISDN() + ". in selected date range or multiple subscribers have MSISDN "
                        + form.getMSISDN() + " in selected date range.");
                }

                /*
                 * Theoretically I could just parse out the BAN from the subscriber ID within the msisdn history object,
                 * but it's not exactly reliable, so we'll fetch the actual subscriber to get it's BAN
                 */
                Home subHome = (Home) ctx.get(SubscriberHome.class);
                Subscriber sub = (Subscriber) subHome.find(ctx, new EQ(SubscriberXInfo.ID, mh.getSubscriberId()));

                if (sub == null)
                {
                    throw new IllegalStateException("Extraction aborted.  Could not find subscriber with MSISDN "
                        + form.getMSISDN() + ". in database.");
                }

                // Only rerate postpaid in accordance with HLD
                if (!sub.isPostpaid())
                {
                    throw new IllegalStateException(
                        "Extraction aborted.  Selected subscriber is not postpaid.  This tool only extracts postpaid call details.");
                }

                Home acctHome = (Home) ctx.get(AccountHome.class);
                acctHome.where(ctx, new EQ(AccountXInfo.BAN, sub.getBAN())).forEach(ctx,
                    new AccountHomeDateValidatingVisitor());
            }
            catch (Exception e)
            {
                IllegalStateException e1 = new IllegalStateException(e.getMessage());
                e1.initCause(e);
                throw e1;
            }
        }
        // BAN is filled in
        else if (form.getBAN().compareTo(RerateForm.DEFAULT_BAN) != 0)
        {
            try
            {
                // if MSISDN is filled in as well, check if MSISDN entered is prepaid
                if (form.getMSISDN().compareTo(RerateForm.DEFAULT_MSISDN) != 0)
                {
                    // Have to look for the subsscriber with this MSISDN in the msisdn history table
                    Home msisdnHistoryHome = getSubscriberIDsFromMsisdnHistory(ctx, form.getMSISDN(), form
                        .getStartDate(), form.getEndDate());

                    // Only 1 sub should be returned for that MSISDN in selected date range
                    // So just find the first one
                    MsisdnMgmtHistory mh = (MsisdnMgmtHistory) msisdnHistoryHome.find(ctx, True.instance());
                    if (mh == null)
                    {
                        throw new IllegalStateException("Extraction aborted.  Could not find subscriber with MSISDN "
                            + form.getMSISDN() + ". in selected date range or multiple subscribers have MSISDN "
                            + form.getMSISDN() + " in selected date range.");
                    }

                    /*
                     * Theoretically I could just parse out the BAN from the subscriber ID within the msisdn history
                     * object, but it's not exactly reliable, so we'll fetch the actual subscriber to get it's BAN
                     */
                    Home subHome = (Home) ctx.get(SubscriberHome.class);
                    And subAndFilter = new And();
                    
                    subAndFilter.add(new EQ(SubscriberXInfo.ID, mh.getSubscriberId()));
                    subAndFilter.add(new EQ(SubscriberXInfo.BAN, form.getBAN()));
                    Subscriber sub = (Subscriber) subHome.find(ctx, subAndFilter);

                    if (!sub.isPostpaid())
                    {
                        throw new IllegalStateException(
                            "Extraction aborted.  Selected subscriber is not postpaid.  This tool only extracts postpaid call details.");
                    }
                }
                Home acctHome = (Home) ctx.get(AccountHome.class);

                acctHome.where(ctx, getPredicate(AccountXInfo.BAN, form)).forEach(ctx,
                    new AccountHomeDateValidatingVisitor());
            }
            catch (Exception e)
            {
                IllegalStateException e1 = new IllegalStateException(e.getMessage());
                e1.initCause(e);
                throw e1;
            }
        }
        // Spid is filled in but not price plan
        else if (form.getSpid() != RerateForm.DEFAULT_SPID && form.getPricePlan() == RerateForm.DEFAULT_PRICEPLAN)
        {
            Home acctHome = (Home) ctx.get(AccountHome.class);

            And filterAnd = new And();

            filterAnd.add(getPredicate(AccountXInfo.SPID, form));
            filterAnd.add(getPredicate(AccountXInfo.BILL_CYCLE_ID, form));

            try
            {
                acctHome.where(ctx, filterAnd).forEach(ctx, new AccountHomeDateValidatingVisitor());
            }
            catch (Exception e)
            {
                IllegalStateException e1 = new IllegalStateException(e.getMessage());
                e1.initCause(e);
                throw e1;
            }
        }
        // Spid is filled in as well as price plan
        else if (form.getSpid() != RerateForm.DEFAULT_SPID && form.getPricePlan() != RerateForm.DEFAULT_PRICEPLAN)
        {
            Home subHome = (Home) ctx.get(SubscriberHome.class);

            try
            {
                And filterAnd = new And();
                filterAnd.add(getPredicate(SubscriberXInfo.SPID, form));
                filterAnd.add(getPredicate(SubscriberXInfo.PRICE_PLAN, form));
                // Only rerate postpaid in accordance with HLD
                filterAnd.add(new EQ(SubscriberXInfo.SUBSCRIBER_TYPE, SubscriberTypeEnum.POSTPAID));

                subHome.where(ctx, filterAnd).forEach(ctx, new SubscriberHomeDateValidatingVisitor());
            }
            catch (Exception e)
            {
                IllegalStateException e1 = new IllegalStateException(e.getMessage());
                e1.initCause(e);
                throw e1;
            }
        }
        // Form was blank!
        else
        {
            /*
             * If spid MSISDN, or BAN, or SPID wasn't filled in then Bill cycle and Price plan couldn't have been filled
             * in because they're spid aware
             */
            throw new IllegalStateException("Extraction aborted.  No extraction criteria was selected.");
        }
    }

    /**
     * Calls com.redknee.framework.xhome.webcontrol.BeanWebController.outputHelpLink()
     *
     * @param context the current context
     * @param out the current PrintWriter
     */
    private void outputHelpLink(final Context context, final PrintWriter out, final ButtonRenderer buttonRenderer)
    {
    	// in the future we might need to specify the HttpServletRequest and HttpServletResponse
        BeanWebController.outputHelpLink(context, null, null, out, buttonRenderer);
    }

    private Predicate getPredicate(PropertyInfo info, RerateForm form)
    {
        if (info == SubscriberXInfo.MSISDN)
        {
            return (form.getMSISDN().compareTo(RerateForm.DEFAULT_MSISDN) != 0) ? new EQ(SubscriberXInfo.MSISDN, form
                .getMSISDN()) : True.instance();
        }

        if (info == SubscriberXInfo.BAN)
        {
            return (form.getMSISDN().compareTo(RerateForm.DEFAULT_BAN) != 0) ? new EQ(SubscriberXInfo.BAN, form
                .getBAN()) : True.instance();
        }

        if (info == AccountXInfo.BAN)
        {
            return (form.getBAN().compareTo(RerateForm.DEFAULT_BAN) != 0) ? new EQ(AccountXInfo.BAN, form.getBAN())
                : True.instance();
        }

        if (info == SubscriberXInfo.PRICE_PLAN)
        {
            return (form.getPricePlan() != RerateForm.DEFAULT_PRICEPLAN) ? new EQ(SubscriberXInfo.PRICE_PLAN, Long.valueOf(
                form.getPricePlan())) : True.instance();
        }

        if (info == AccountXInfo.BILL_CYCLE_ID)
        {
            return (form.getBillCycleID() != RerateForm.DEFAULT_BILLCYCLEID) ? new EQ(AccountXInfo.BILL_CYCLE_ID,
                Integer.valueOf(form.getBillCycleID())) : True.instance();
        }

        if (info == AccountXInfo.SPID)
        {
            return (form.getSpid() != RerateForm.DEFAULT_SPID) ? new EQ(AccountXInfo.SPID, Integer.valueOf(form.getSpid()))
                : True.instance();
        }

        if (info == SubscriberXInfo.SPID)
        {
            return (form.getSpid() != RerateForm.DEFAULT_SPID) ? new EQ(SubscriberXInfo.SPID, Integer.valueOf(form
                .getSpid())) : True.instance();
        }

        return True.instance();
    }

    private PrintStream getPrintStream(Context ctx, String directory, String file) throws Exception
    {
        File fileDir = new File(directory);
        if (!fileDir.exists())
        {
            if (!fileDir.mkdir())
            {
                throw new Exception("Cannot create direcotry for extraction.");
            }
        }

        return new PrintStream(new FileOutputStream(new File(directory + File.separator + file)));
    }

    private void backUpFile(String directory, String file) throws Exception
    {
        FileChannel source = null;
        FileChannel dest = null;

        try
        {
            // Open the file
            source = (new FileInputStream(new File(directory, file))).getChannel();

            directory = directory + BACK_UP_DIRECTORY_NAME;
            File fileDir = new File(directory);
            if (!fileDir.exists())
            {
                if (!fileDir.mkdir())
                {
                    throw new Exception("Cannot create direcotry for backup.");
                }
            }

            SimpleDateFormat format = (SimpleDateFormat) SimpleDateFormat.getInstance();
            format.applyPattern("yyyyMMdd");
            String destFileName = file + "-" + format.format(new Date());
            dest = (new FileOutputStream(new File(directory, destFileName))).getChannel();

            // transferTo is potentially much more effeicient
            // than a simple loop to read from a source and
            // write to a sink, read the file channel java docs if interesed
            // Copy the file
            long bytesTransfered = source.transferTo(0, source.size(), dest);
            if (bytesTransfered != source.size())
            {
                throw new Exception("An error occured backing up file " + destFileName + ".  Number of bytes copied ("
                    + bytesTransfered + ") to back up file did not match size of original file (" + source.size()
                    + ").");
            }
        }
        finally
        {
            if (source != null)
            {
                source.close();
            }
            if (dest != null)
            {
                dest.close();
            }
        }
    }

    /**
     * From the call detail, returns the appropriate ER791 ECP will need for rerating
     * 
     * @param ctx
     * @param cd
     * @return
     * @throws Exception
     */
    private String getEr791(Context ctx, CallDetail cd)
    {
        if (cd == null)
        {
            return null;
        }

        // Get the delimiter from the ER formatter
        char delimiter = ',';

        FastERLogMsgFormat formatter = (FastERLogMsgFormat) ctx.get(FastERLogMsgFormat.class);
        if (formatter == null)
        {
            formatter = new FastERLogMsgFormat(ctx);
        }

        delimiter = formatter.getDelimiter();

        // Get a date formatter
        SimpleDateFormat dateFormat = (SimpleDateFormat) SimpleDateFormat.getDateInstance();
        dateFormat.applyPattern("yyyyMMdd");
        SimpleDateFormat timeFormat = (SimpleDateFormat) SimpleDateFormat.getTimeInstance();
        timeFormat.applyPattern("HHmmss");

        // Build ER string
        StringBuilder buf = new StringBuilder();

        buf.append(dateFormat.format(cd.getPostedDate()));
        buf.append(delimiter);
        buf.append(timeFormat.format(cd.getPostedDate()));
        buf.append(delimiter);
        buf.append("791");
        buf.append(delimiter);
        buf.append("700");
        buf.append(delimiter);
        buf.append("CDR Rating Feature");
        buf.append(delimiter);
        // SPID here, leave empty
        buf.append(delimiter);
        // MSISDN here, leave empty
        buf.append(delimiter);
        // Connect Parater - Generic Digits here, leave empty
        buf.append(delimiter);
        // SCPID here, leave empty
        buf.append(delimiter);
        buf.append(cd.getOrigMSISDN()); // Originaiting MSISDN
        buf.append(delimiter);
        buf.append(cd.getDestMSISDN()); // Destination MSISDN
        buf.append(delimiter);
        buf.append(cd.getChargedMSISDN()); // Charged MSISDN
        buf.append(delimiter);
        buf.append(cd.getCallType().getIndex()); // Call Type
        buf.append(delimiter);
        buf.append(dateFormat.format(cd.getTranDate()));
        buf.append(delimiter);
        buf.append(timeFormat.format(cd.getTranDate()));
        buf.append(delimiter);
        buf.append(cd.getLocation());
        buf.append(delimiter);
        buf.append(cd.getLocationType());
        buf.append(delimiter);
        buf.append((cd.getDuration().getTime() / 1000L)); // getTime returns the time in miliseconds, divide by 1000L
        // to get back the number of seconds as a long
        buf.append(delimiter);
        buf.append(cd.getRedirectedAddress());
        buf.append(delimiter);
        // Call reference here, leave empty
        buf.append(delimiter);
        buf.append(cd.getOmsc());
        buf.append(delimiter);
        buf.append(cd.getDisconnectReason());
        buf.append(delimiter);
        buf.append(cd.getTeleserviceType().getIndex()); // Tele service type
        buf.append(delimiter);
        buf.append("1");
        buf.append(delimiter);
        
        //Passing Empty string for bearer service.  Once bearer service is added to ER501 
        //and CDR, then we need update this call
        String bearerService = "EMPTY";
        buf.append(bearerService); 
        return buf.toString();
    }

    /**
     * From the call detail, returns the amount that needs to be credited back to ABM
     * 
     * @param ctx
     * @param cd
     * @return
     */
    private String getABMCreditEr(Context ctx, CallDetail cd, Subscriber sub, CRMSpid spid)
    {
        // Get the delimiter from the ER formatter
        char delimiter = ',';

        FastERLogMsgFormat formatter = (FastERLogMsgFormat) ctx.get(FastERLogMsgFormat.class);
        if (formatter == null)
        {
            formatter = new FastERLogMsgFormat(ctx);
        }

        delimiter = formatter.getDelimiter();

        // Get the description of the subscriber type
        String subType = cd.getSubscriberType().getDescription();
        int dashIndex = subType.indexOf("-");
        if (dashIndex >= 0)
        {
            subType = subType.substring(subType.indexOf("-") + 2);
        }

        // Build the ER
        StringBuilder buf = new StringBuilder();

        buf.append("CREDIT");
        buf.append(delimiter);
        buf.append(sub.getMSISDN());
        buf.append(delimiter);
        buf.append(cd.getCharge());
        buf.append(delimiter);
        buf.append(spid.getCurrency());
        buf.append(delimiter);
        // USESERVICE_ID - leave empty
        buf.append(delimiter);
        // SERVICE_ID - leave empty
        buf.append(delimiter);
        // ER_REFERENCE - leave empty
        buf.append(delimiter);
        // SCP_ID - leave empty
        buf.append(delimiter);

        buf.append(subType);
        buf.append(delimiter);
        // Balance flag here - leave empty
        buf.append(delimiter);
        buf.append("N");
        buf.append(delimiter);
        // EXTENSION here - leave empty

        return buf.toString();
    }

    /**
     * Get the subscribers using the msisdn with in the date range
     * 
     * @param ctx
     * @param msisdn
     * @param startDate
     * @param endDate
     * @return
     * @throws UnsupportedOperationException
     * @throws HomeException
     */
    private Home getSubscriberIDsFromMsisdnHistory(Context ctx, String msisdn, Date startDate, Date endDate)
        throws UnsupportedOperationException, HomeException
    {
        Home msisdnHistoryHome = (Home) ctx.get(MsisdnMgmtHistoryHome.class);
        if (msisdnHistoryHome == null)
        {
            throw new HomeException("Could not get MSISDN History home from context.");
        }
        
        SubscriptionType subscriptionType = SubscriptionType.getINSubscriptionType(ctx);
        
        if (subscriptionType == null)
        {
        	throw new HomeException("Unable to retrieve the IN Subscription Type.  Make sure that a SubscriptionType for IN service is defined.");
    	}

        /*
         * Adjust the start date so the MSISDN search works correctly. The start date is given as Nov 12 2005 00:00:00,
         * but if the MSISDN start date is Nov 12 2005 08:00:00, then it would appear the form start date is before the
         * MSISDN start date and thus make it appear the subscriber does not have this MSISDN in the selected date
         * range. To work around this, we'll adjust the form start date to be Nov 12 2005 23:59:59.
         */
        Calendar cal = Calendar.getInstance();
        cal.setTime(startDate);
        cal.add(Calendar.DATE, 1); // advance it by one day
        cal.add(Calendar.SECOND, -1); // move it back by 1 second to acheive the 23:59:59 time we're looking for
        startDate = cal.getTime();

        And filter = new And();
        filter.add(new EQ(MsisdnMgmtHistoryXInfo.TERMINAL_ID, msisdn));
        filter.add(new LT(MsisdnMgmtHistoryXInfo.TIMESTAMP, startDate));
        filter.add(new EQ(MsisdnMgmtHistoryXInfo.SUBSCRIPTION_TYPE, subscriptionType.getId()));
        filter.add(new LT(endDate, MsisdnMgmtHistoryXInfo.END_TIMESTAMP));
        
        return msisdnHistoryHome.where(ctx, filter);
    }

    private RerateForm adjustToDate(RerateForm form) throws CloneNotSupportedException
    {
        RerateForm form2 = (RerateForm) form.clone();
        if (form.getEndDate() != null)
        {
            Calendar cal = Calendar.getInstance();
            cal.setTime(form2.getEndDate());
            /*
             * Add 1 to the end date so that it the extraction criteria is inclusive on the end date
             */
            cal.add(Calendar.DATE, 1);
            form2.setEndDate(cal.getTime());
        }
        return form2;
    }

    /**
     * The webcontrol used to represent the form.
     */
    private final RerateFormWebControl webControl_;

    /**
     * Visitor to go through accounts to fetch all their call details
     * 
     * @author dannyng
     */
    class AccountHomeVisitor implements Visitor
    {
        /**
         * Default Serial id
         */
        private static final long serialVersionUID = 1L;

        public void visit(Context ctx, Object obj)
        {
            Account account = (Account) obj;

            try
            {
                /*
                 * Make sure we filter by price plan as well in case the agent selected a price plan on the form
                 */
                Home subHome = (Home) ctx.get(SubscriberHome.class);
                And filterSub = new And();
                filterSub.add(new EQ(SubscriberXInfo.BAN, account.getBAN()));
                filterSub.add(getPredicate(SubscriberXInfo.PRICE_PLAN, (RerateForm) ctx.get(RERATE_FORM)));
                // Only rerate postpaid in accordance with HLD
                filterSub.add(new EQ(SubscriberXInfo.SUBSCRIBER_TYPE, SubscriberTypeEnum.POSTPAID));

                subHome.where(ctx, filterSub).forEach(ctx, new SubscriberHomeVisitor());

                /*
                 * Go through the sub accounts that this account is responsible for and subsequently, their subs
                 */
                Home acctHome = (Home) ctx.get(AccountHome.class);
                filterSub = new And();
                filterSub.add(new EQ(AccountXInfo.PARENT_BAN, account.getBAN()));
                filterSub.add(new EQ(AccountXInfo.RESPONSIBLE, false));

                acctHome.where(ctx, filterSub).forEach(ctx, new AccountHomeVisitor());
            }
            catch (HomeException e)
            {
                Logger.minor(ctx, this, "Rerating failed for Account " + account.getBAN(), e);
            }
        }
    }

    /**
     * Visitor to go through subscribers to fetch all their call details
     * 
     * @author dannyng
     */
    class SubscriberHomeVisitor implements Visitor
    {
        /**
         * Serial Version id
         */
        private static final long serialVersionUID = 1L;

        public void visit(Context ctx, Object obj)
        {
            final Subscriber subscriber = (Subscriber) obj;
            RerateForm rerateForm = (RerateForm) ctx.get(ReratingRequestServicer.RERATE_FORM);

            // Look up last invoice (uncommited)
            Date lastInvoiceDate = InvoiceSupport.getPreviousBillingDateForSubscriber(ctx, subscriber.getId(),
                new Date());

            // Look up 2nd last invoice (commited by the last invoice)
            if (lastInvoiceDate != null)
            {
                lastInvoiceDate = InvoiceSupport.getPreviousBillingDateForSubscriber(ctx, subscriber.getId(),
                    lastInvoiceDate);
            }

            if (lastInvoiceDate == null)
            {
                // Since this person has no commited invoices,
                // set the last invoice date to epoch
                lastInvoiceDate = new Date(0);
            }

            // Use the latest of the 2nd last invoice date and the agent selected start date
            // because we don't want to rerate anything within a commited invoice
            Date startDate = rerateForm.getStartDate();
            startDate = new Date(Math.max(startDate.getTime(), lastInvoiceDate.getTime()));

            try
            {
                final PrintStream ecpOut = (PrintStream) ctx.get(ReratingRequestServicer.ECP_EXTRACT_FILE_NAME);
                final PrintStream abmOut = (PrintStream) ctx.get(ReratingRequestServicer.ABM_EXTRACT_FILE_NAME);
                final PrintStream errorOut = (PrintStream) ctx.get(ReratingRequestServicer.EXTRACT_ERROR_FILE_NAME);

                final Home rerateCDHome = (Home) ctx.get(RerateCallDetailHome.class);
                final Home cdHome = (Home) ctx.get(CallDetailHome.class);
                final RerateConfig config = (RerateConfig) ctx.get(RerateConfig.class);

                final Context txCtx = ctx.createSubContext();

                txCtx.put(com.redknee.framework.xhome.txn.Transaction.class, new DefaultTransaction(txCtx));

                Home spidHome = (Home) ctx.get(CRMSpidHome.class);
                final CRMSpid spid = (CRMSpid) spidHome.find(ctx, Integer.valueOf(subscriber.getSpid()));

                /*
                 * We use new Date() for the last invoice date parameter because this function will grab any call
                 * details with posted date after the last invoice date but were made before the last invoice date (i.e.
                 * this call details sat around a while before the poller picked them up), this ensures that these laggy
                 * call details get picked up by the invoice; however we do not need these laggy call details to be
                 * picked up as we want to rerate only call details specifically within the start and end date range
                 */
                HashSet voice_calltypes = new HashSet();
                voice_calltypes.add(CallTypeEnum.ORIG);
                voice_calltypes.add(CallTypeEnum.TERM);

                CallDetailSupportHelper.get(ctx).getCallDetailsForSubscriberIDHome(ctx, subscriber.getId(), startDate,
                    rerateForm.getEndDate(), new Date()).where(ctx, new In(CallDetailXInfo.CALL_TYPE, voice_calltypes))
                    .forEach(ctx, new Visitor()
                    {
                        private static final long serialVersionUID = 1L;

                        public void visit(Context ctx, Object obj)
                        {
                            CallDetail cd = (CallDetail) obj;

                            String ecpEr = getEr791(ctx, cd);
                            ecpOut.println(ecpEr);
                            if (spid != null)
                            {
                                abmOut.println(getABMCreditEr(ctx, cd, subscriber, spid));
                            }
                            else
                            {
                                errorOut.println("Could not find subscriber service provider.");
                            }

                            /*
                             * Store the extracted call detail into temp table for matching with ER501 returned from ECP
                             */
                            try
                            {
                                RerateCallDetail rcd = new RerateCallDetail();
                                XBeans.copy(ctx, CallDetailXInfo.instance(), cd, RerateCallDetailXInfo.instance(), rcd);
                                rcd.setExtractDate(new Date());

                                rerateCDHome.create(txCtx, rcd);

                                if (config.getReallyExtract())
                                {
                                    cdHome.remove(txCtx, cd);
                                }
                            }
                            catch (Throwable t)
                            {
                                errorOut
                                    .println("Unexpected error writing call detail to temporary rerate table or removing call detail from call detail table for call detail "
                                        + cd.getId() + ".  " + t.getMessage());
                            }
                        }
                    });
                try
                {
                    com.redknee.framework.xhome.txn.Transactions.commit(txCtx);
                }
                catch (Exception e)
                {
                    new MinorLogMsg(this,"Error commiting the transaction for Subscriber: " + subscriber.getId(),e).log(txCtx);
                }
            }
            catch (HomeException e)
            {
                new MinorLogMsg(this,"Error. May be SPID could not  be fetched for Subscriber: " +subscriber.getId() ,e).log(ctx);
            }
        }
    }

    /**
     * Visitor to go through accounts to check their committed invoice date
     * 
     * @author dannyng
     */
    class AccountHomeDateValidatingVisitor implements Visitor
    {
        /**
         * Default Serial id
         */
        private static final long serialVersionUID = 1L;

        public void visit(Context ctx, Object obj) throws AgentException
        {
            Account account = (Account) obj;

            // Look up last invoice (uncommited)
            Date lastInvoiceDate = InvoiceSupport.getPreviousBillingDate(ctx, account.getBAN(), new Date());

            // Look up 2nd last invoice (commited by the last invoice)
            if (lastInvoiceDate != null)
            {
                lastInvoiceDate = InvoiceSupport.getPreviousBillingDate(ctx, account.getBAN(), lastInvoiceDate);

                if (lastInvoiceDate != null)
                {
                    RerateForm rerateForm = (RerateForm) ctx.get(ReratingRequestServicer.RERATE_FORM);
                    Date startDate = rerateForm.getStartDate();

                    if (startDate.getTime() < lastInvoiceDate.getTime())
                    {
                        throw new IllegalStateException("Extraction Aborted.  Account " + account.getBAN()
                            + " has a commited invoice after the selected startdate on " + lastInvoiceDate + ".");
                    }
                }
            }
        }
    }

    /**
     * Visitor to go through and check the account's commited invoice date
     * 
     * @author dannyng
     */
    class SubscriberHomeDateValidatingVisitor implements Visitor
    {
        /**
         * Default Serial id
         */
        private static final long serialVersionUID = 1L;

        public void visit(Context ctx, Object obj) throws AgentException, AbortVisitException
        {
            Subscriber subscriber = (Subscriber) obj;

            Home acctHome = (Home) ctx.get(AccountHome.class);

            try
            {
                RerateForm form = (RerateForm) ctx.get(ReratingRequestServicer.RERATE_FORM);

                And filterAccount = new And();
                filterAccount.add(getPredicate(AccountXInfo.BILL_CYCLE_ID, form));
                filterAccount.add(new EQ(AccountXInfo.BAN, subscriber.getBAN()));

                acctHome.where(ctx, filterAccount).forEach(ctx, new AccountHomeDateValidatingVisitor());
            }
            catch (HomeException e)
            {
                e.printStackTrace();
            }
        }
    }

} // class
