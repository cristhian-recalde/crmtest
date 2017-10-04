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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.trilogy.app.crm.LicenseConstants;
import com.trilogy.app.crm.bean.Msisdn;
import com.trilogy.app.crm.bean.MsisdnBulk;
import com.trilogy.app.crm.bean.MsisdnBulkXInfo;
import com.trilogy.app.crm.bean.MsisdnHome;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.factory.core.MsisdnFactory;
import com.trilogy.app.crm.support.LicensingSupportHelper;
import com.trilogy.app.crm.support.SpidSupport;
import com.trilogy.app.crm.technology.TechnologyEnum;
import com.trilogy.framework.xhome.beans.ExceptionListener;
import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.beans.XBeans;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextFactory;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.language.MessageMgr;
import com.trilogy.framework.xhome.web.renderer.ButtonRenderer;
import com.trilogy.framework.xhome.web.renderer.DefaultButtonRenderer;
import com.trilogy.framework.xhome.web.renderer.DefaultFormRenderer;
import com.trilogy.framework.xhome.web.renderer.FormRenderer;
import com.trilogy.framework.xhome.webcontrol.AbstractWebControl;
import com.trilogy.framework.xhome.webcontrol.BeanWebController;
import com.trilogy.framework.xhome.webcontrol.HTMLExceptionListener;
import com.trilogy.framework.xhome.webcontrol.OutputWebControl;
import com.trilogy.framework.xhome.webcontrol.RequestServicer;
import com.trilogy.framework.xhome.webcontrol.ViewModeEnum;
import com.trilogy.framework.xhome.webcontrol.WebControl;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MajorLogMsg;
import com.trilogy.framework.xlog.log.MinorLogMsg;


/**
 * MSISDN bulk load request servicer.
 *
 * @author larry.xia@redknee.com
 * @author gary.anderson@redknee.com
 * @author karen.lin@redknee.com
 * @author cindy.wong@redknee.com
 */
public class MSISDNBulkLoaderRequestServicer implements RequestServicer
{

    /**
     * Label for load button.
     */
    private static final String LOAD_BUTTON_LABEL = "Load";
    /**
     * Label for preview button.
     */
    private static final String PREVIEW_BUTTON_LABEL = "Preview";
    /**
     * Label for clear button.
     */
    private static final String CLEAR_BUTTON_LABEL = "Clear";
    /**
     * Default form title.
     */
    public static final String DEFAULT_TITLE = "<b>MSISDN Bulk File: </b>";


    /**
     * Create a new instance of <code>MSISDNBulkLoaderRequestServicer</code>.
     */
    public MSISDNBulkLoaderRequestServicer()
    {
        setTitle(DEFAULT_TITLE);
    }


    /**
     * Create a new instance of <code>MSISDNBulkLoaderRequestServicer</code>.
     *
     * @param title
     *            Custom title of the form.
     */
    public MSISDNBulkLoaderRequestServicer(final String title)
    {
        setTitle(title);
    }


    /**
     * Retrieves the value of <code>title</code>.
     *
     * @return The value of <code>title</code>.
     */
    public String getTitle()
    {
        return this.title_;
    }


    /**
     * Sets the value of <code>title</code>.
     *
     * @param title
     *            The value of <code>title</code> to set.
     */
    public void setTitle(final String title)
    {
        this.title_ = title;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void service(final Context context, final HttpServletRequest req, final HttpServletResponse res)
        throws ServletException, IOException
    {
        final PrintWriter out = res.getWriter();

        final boolean isPrepaid = LicensingSupportHelper.get(context).isLicensed(context, LicenseConstants.PREPAID_LICENSE_KEY);
        final boolean isPostpaid = LicensingSupportHelper.get(context).isLicensed(context, LicenseConstants.POSTPAID_LICENSE_KEY);

        /*
         * if prepaid and postpaid licenses are not turned on at the same time, then hide
         * the prepaid-postpaid drop down
         */
        if (!(isPrepaid && isPostpaid))
        {

            AbstractWebControl.setMode(context, MsisdnBulkXInfo.SUBSCRIBER_TYPE, ViewModeEnum.NONE);

        }
        final Context ctx = context.createSubContext();
        ctx.put("MODE", OutputWebControl.EDIT_MODE);

        final MsisdnBulk form = instantiateForm(ctx, isPrepaid, isPostpaid);

        final MessageMgr manager = new MessageMgr(ctx, this);

        final HTMLExceptionListener exceptions = new HTMLExceptionListener(manager);
        ctx.put(ExceptionListener.class, exceptions);

        final ButtonRenderer buttonRenderer = (ButtonRenderer) ctx.get(ButtonRenderer.class,
                DefaultButtonRenderer.instance());

        if (!buttonRenderer.isButton(ctx, CLEAR_BUTTON_LABEL))
        {
            this.wc_.fromWeb(ctx, form, req, "");

            if (!exceptions.hasErrors())
            {
                try
                {
                	if ( SpidSupport.isGroupPooledMsisdnGroup(ctx, form.getSpid(), form.getGroup()) )
                	{
                		// set the appropriate values for the Pooled MSISDN bulk provisioning
                		form.setVanityGroup(MsisdnBulk.DEFAULT_VANITYGROUP);
                		form.setSubscriberType(SubscriberTypeEnum.HYBRID);
                		form.setTechnology(TechnologyEnum.ANY);                		
                	}
                	
                    if (buttonRenderer.isButton(ctx, PREVIEW_BUTTON_LABEL))
                    {
                        validateInput(ctx, form);
                    }
                    else if (buttonRenderer.isButton(ctx, LOAD_BUTTON_LABEL))
                    {
                        // [Cindy]: we still need to validate before loading
                        validateInput(ctx, form);
                        loadBulk(ctx, out, form);
                    }
                }
                catch (final Exception exc)
                {
                    exceptions.thrown(exc);
                }
            }
        }

        if (exceptions.hasErrors())
        {
            exceptions.toWeb(ctx, out, "", form);
        }

        final FormRenderer frend = (FormRenderer) ctx.get(FormRenderer.class, DefaultFormRenderer.instance());

        frend.Form(out, ctx);
        out.print("<table><tr><td>");
        this.wc_.toWeb(ctx, out, "", form);

        out.print("</td></tr><tr><th align=\"right\">");

        buttonRenderer.inputButton(out, ctx, this.getClass(), CLEAR_BUTTON_LABEL, false);
        buttonRenderer.inputButton(out, ctx, this.getClass(), PREVIEW_BUTTON_LABEL, false);
        buttonRenderer.inputButton(out, ctx, this.getClass(), LOAD_BUTTON_LABEL, false);
        outputHelpLink(ctx, out, buttonRenderer);

        out.println("</th></tr></table>");

        frend.FormEnd(out);
        out.println("<br/>");
    }


    /**
     * Instantiate the form bean.
     *
     * @param ctx
     *            The operating context.
     * @param isPrepaid
     *            Whether prepaid license is enabled.
     * @param isPostpaid
     *            Whether postpaid license is enabled.
     * @return The instantiated form.
     * @throws IllegalArgumentException
     *             Thrown if there are problems with instantiation.
     */
    private MsisdnBulk instantiateForm(final Context ctx, final boolean isPrepaid, final boolean isPostpaid)
        throws IllegalArgumentException
    {
        MsisdnBulk form = new MsisdnBulk();
        try
        {
            form = (MsisdnBulk) XBeans.instantiate(MsisdnBulk.class, ctx);

            if (isPostpaid && !isPrepaid)
            {
                /*
                 * if the license is postpaid enabled, then set the subscriber type to
                 * postpaid
                 */
                form.setSubscriberType(SubscriberTypeEnum.POSTPAID);
            }
		    else if(isPrepaid && !isPostpaid)
            {
                form.setSubscriberType(SubscriberTypeEnum.PREPAID);
            }

        }
        catch (final Exception e)
        {
            new MinorLogMsg(this, "Cannot instantiate MsisdnBulk from template: " + e.getMessage(), e).log(ctx);
        }

        return form;
    }


    /**
     * Validates the form input.
     *
     * @param ctx
     *            The operating context.
     * @param input
     *            The form input.
     * @throws IllegalStateException
     *             Thrown if the input is invalid.
     */
    private void validateInput(final Context ctx, final MsisdnBulk input) throws IllegalStateException
    {
        final long start = getAsNumber(input.getStart());
        final long end = getAsNumber(input.getEnd());
        if (start >= end)
        {
            throw new IllegalStateException("The starting MSISDN must smaller than the ending MSISDN.");
        }
        else if (input.getStart().trim().length() < input.getEnd().trim().length())
        {
            throw new IllegalStateException("The ending MSISDN must not be shorter than the starting MSISDN.");
        }
        
        if(input.getGroup() == MsisdnBulk.DEFAULT_GROUP ){
        	
        	throw new IllegalPropertyArgumentException(
                    "Regular Group Options", "Regular Mobile Number Group is not selected. Please select Mobile Number Group.");
        }
        if (input.getVanityGroup() != MsisdnBulk.DEFAULT_VANITYGROUP
                && !areVanityOptionsSelected(input))
        {
            throw new IllegalPropertyArgumentException(
                    "Vanity Options", "Vanity Group was selected.  You must select at least one Vanity Option.");
        }
    }
    
    /**
     * Returns true if any Vanity Options are enabled.  Otherwise, returns False.
     * @param form
     * @return
     */
    private boolean areVanityOptionsSelected(final MsisdnBulk form)
    {
        boolean result = false;
        if (form.getThreepairs() ||
                form.getTwopairs() || 
                form.getFullhouse() ||
                form.getStraight7() ||
                form.getStraight6() ||
                form.getStraight5() ||
                form.getStraight4() ||
                form.getSame7() ||
                form.getSame6() ||
                form.getSame5() ||
                form.getSame4() ||
                form.getSame3() ||
                form.getFourDigit2Pairs() ||
                form.getFourDigitAaabPattern() ||
                form.getFourDigitAbabPattern() ||
                form.getFourDigitAbbaPattern() ||
                form.getFourDigitSame3() ||
                form.getFourDigitSame4())
        {
            result = true;
        }
        return result;
    }


    /**
     * Returns the MSISDN to be generated as a number. This value is used for the
     * numerical part of the MSISDN generation.
     *
     * @param input
     *            The input.
     * @return The MSISDN as a long.
     * @throws IllegalStateException
     *             Thrown if the MSISDN is invalid.
     */
    private long getAsNumber(final String input) throws IllegalStateException
    {
        if (input == null || input.trim().length() == 0)
        {
            throw new IllegalStateException("The MSISDN must not be empty");
        }
        long start;
        try
        {
            start = Long.parseLong(input.trim());
        }
        catch (final NumberFormatException exception)
        {
            throw new IllegalStateException("The MSISDN must be a number", exception);
        }
        return start;
    }


    /**
     * Bulk-loads the MSISDNs according to the parameters specified.
     *
     * @param ctx
     *            The operating context.
     * @param out
     *            The on-screen output writer.
     * @param mk
     *            MSISDn bulk-load parameter form.
     */
    public void loadBulk(final Context ctx, final PrintWriter out, final MsisdnBulk mk)
    {
        PrintStream errorOut = null;
        final ContextFactory factory = MsisdnFactory.instance();

        final boolean isVanity = isVanityOptionsSelected(mk);

        final long startNum = getAsNumber(mk.getStart());
        final long endNum = getAsNumber(mk.getEnd());
        final StringBuilder pattern = new StringBuilder();
        for (int i = 0; i < mk.getStart().length(); i++)
        {
            pattern.append('0');
        }
        final DecimalFormat df = new DecimalFormat(pattern.toString());

        // for (long i = mk.getStart(); i <= mk.getEnd(); ++i)
        for (long i = startNum; i <= endNum; i++)
        {
            final String number = df.format(i);
            try
            {
                final Home home = (Home) ctx.get(MsisdnHome.class);
                final Msisdn msisdn = (Msisdn) factory.create(ctx);
                msisdn.setMsisdn(number);
                msisdn.setSpid(mk.getSpid());
                msisdn.setState(mk.getState());
                msisdn.setLnpRequired(mk.getLnpRequired());
                msisdn.setSubscriberType(mk.getSubscriberType());
                msisdn.setTechnology(mk.getTechnology());

				/*
				 * [Cindy Wong] TT#10082326025: startTimestamp of MSISDN is
				 * supposed to be the creation time of the MSISDN. It should be
				 * properly set to now, not 0L.
				 */
				msisdn.setStartTimestamp(new Date());

                /*
                 * TT 8012900032: 
                 * Vanity numbers are created into the Vanity MSISDN Group.  All non-vanity 
                 * numbers are created into the "Regular" MSISDN Group.
                 * Both Vanity and Non-Vanity numbers are created on the same Bulk Load pass.
                 * Previously, either Vanity or Non-Vanity numbers were created per Load pass.   
                 */
                if (isVanity && checkVanity(ctx, mk, number))
                {
                    msisdn.setGroup(mk.getVanityGroup());
                }
                else
                {
                    msisdn.setGroup(mk.getGroup());
                }
                home.create(msisdn);
            }
            catch (final HomeException e)
            {
                new MajorLogMsg(this, e.getMessage(), e).log(ctx);
                String msg = "Unexpected Error occurred while Bulk Loading Mobile Numbers. View the logs for details. Error: " + e.getMessage();
                throw new IllegalStateException(msg, e); 
            }
            catch (final Exception e)
            {
                if (LogSupport.isDebugEnabled(ctx))
                {
                    new DebugLogMsg(this, "Failed to load Mobile Number " + i, e).log(ctx);
                }
                try
                {
                    if (errorOut == null)
                    {
                        errorOut = getErrorPrintStream(ctx, mk.getStart(), mk.getEnd(), mk.getErrfile());
                    }

                    errorOut.println(number);
                }
                catch (final Exception ie)
                {
                    new MinorLogMsg(this, e.getMessage(), ie).log(ctx);
                }
            }
        }

        if (errorOut != null)
        {
            out.println("<H3>The msisdns are loaded with errors, please check error file.</H3>");
            errorOut.close();
        }
        else
        {
            out.println("<H3>The msisdns are loaded successfully.</H3>");
        }
    }


    /**
     * Returns the print stream for error logging.
     *
     * @param ctx
     *            The operating context.
     * @param start
     *            Starting MSISDN.
     * @param end
     *            Ending MSISDN.
     * @param errFilePath
     *            Error file directory name.
     * @return The print stream of this bulkloading.
     * @throws Exception
     *             Thrown if there are error creating the print stream.
     */
    private PrintStream getErrorPrintStream(final Context ctx, final String start, final String end,
        final String errFilePath) throws Exception
    {
        final File errDir = new File(errFilePath);
        if (!errDir.exists())
        {
            if (!errDir.mkdir())
            {
                throw new IllegalStateException("Cannot create find and create eror file directory when Bulk Load Mobile Number");
            }
        }

        return new PrintStream(new FileOutputStream(new File(errFilePath + File.separator + "msisdnerro" + start + "-"
            + end)));
    }


    /**
     * Whether any vanity option is selected.
     *
     * @param mk
     *            Parameter form.
     * @return Returns <code>true</code> if any vanity option is selected.
     */
    private boolean isVanityOptionsSelected(final MsisdnBulk mk)
    {
        boolean result = false;

        if (mk.getVanityGroup() != MsisdnBulk.DEFAULT_VANITYGROUP
                && areVanityOptionsSelected(mk))
        {
            result = true;
        }

        return result;
    }


    /**
     * Checks whether the MSISDN is a vanity MSISDN.  
     * Pattern matching according to shortest suffix match.
     *
     * @param mk
     *            Bulkloading form.
     * @param msisdn
     *            The MSISDN generated.
     * @return Whether the MSISDn is a vanity MSISDN.
     */
    public boolean checkVanity(final Context context, final MsisdnBulk mk, final String msisdn)
    {
        boolean result = false;
        
        /* Since all of the Vanity options are being entered into the same Vanity Group,
         * we will optimize the following sequence of validation using the shortest 
         * match. (Supposing all vanity options were checked off, the shortest pattern 
         * match would validate minimum base cases). 
         */
        // Base cases
        if ((mk.getTwopairs() || mk.getFourDigit2Pairs()) && VanityChecker.isStraightPairs(msisdn, 2))
        {
            result = true;
            logVanityMatch(context, msisdn, "Two Pairs");
        }
        else if (mk.getFullhouse() && VanityChecker.isFullhouse(msisdn))
        {
            result = true;
            logVanityMatch(context, msisdn, "Full House");
        }
        else if (mk.getStraight4() && VanityChecker.isStraight(msisdn, 4))
        {
            result = true;
            logVanityMatch(context, msisdn, "4-digit Straight");
        }
        else if ((mk.getSame3() || mk.getFourDigitSame3()) && VanityChecker.isSame(msisdn, 3))
        {
            result = true;
            logVanityMatch(context, msisdn, "3-of-a-kind");
        }
        // Special Cases
        else if (mk.getThreepairs() && VanityChecker.isStraightPairs(msisdn, 3))
        {
            result = true;
            logVanityMatch(context, msisdn, "Three Pairs");
        }
        else if (mk.getStraight5() && VanityChecker.isStraight(msisdn, 5))
        {
            result = true;
            logVanityMatch(context, msisdn, "5-digit Straight");
        }
        else if (mk.getStraight6() && VanityChecker.isStraight(msisdn, 6))
        {
            result = true;
            logVanityMatch(context, msisdn, "6-digit Straight");
        }
        else if (mk.getStraight7() && VanityChecker.isStraight(msisdn, 7))
        {
            result = true;
            logVanityMatch(context, msisdn, "7-digit Straight");
        }
        else if ((mk.getSame4() || mk.getFourDigitSame4() ) && VanityChecker.isSame(msisdn, 4))
        {
            result = true;
            logVanityMatch(context, msisdn, "4-of-a-kind");
        }
        else if (mk.getSame5() && VanityChecker.isSame(msisdn, 5))
        {
            result = true;
            logVanityMatch(context, msisdn, "5-of-a-kind");
        }
        else if (mk.getSame6() && VanityChecker.isSame(msisdn, 6))
        {
            result = true;
            logVanityMatch(context, msisdn, "6-of-a-kind");
        }
        else if (mk.getSame7() && VanityChecker.isSame(msisdn, 7))
        {
            result = true;
            logVanityMatch(context, msisdn, "7-of-a-kind");
        }
        else if (mk.getFourDigitAaabPattern() && VanityChecker.isValidFourDigitPattern(msisdn, VanityChecker.FOUR_DIGIT_PATTERN_AAAB))
        {
            result = true;
            logVanityMatch(context, msisdn, "Aaab");
        }
        else if (mk.getFourDigitAbabPattern() && VanityChecker.isValidFourDigitPattern(msisdn, VanityChecker.FOUR_DIGIT_PATTERN_ABAB))
        {
            result = true;
            logVanityMatch(context, msisdn, "Abab");
        }
        else if (mk.getFourDigitAbbaPattern() && VanityChecker.isValidFourDigitPattern(msisdn, VanityChecker.FOUR_DIGIT_PATTERN_ABBA))
        {
            result = true;
            logVanityMatch(context, msisdn, "Abba");            
        }
        return result;
    }
    
    /**
     * Log the Vanity pattern matching result.
     * @param context
     * @param msisdn Mobile number that was checked 
     * @param pattern  matching pattern
     */
    private void logVanityMatch(Context context, String msisdn, String pattern)
    {
        if (LogSupport.isDebugEnabled(context))
        {
            new DebugLogMsg(this, "Msisdn=" + msisdn + " matched the Vanity Pattern " + pattern, null).log(context);
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

    /**
     * Web control for MSISDN bulk loader.
     */
    protected WebControl wc_ = new com.redknee.app.crm.technology.SetTechnologyProxyWebControl(
            new com.redknee.app.crm.bean.webcontrol.CRMMsisdnBulkWebControl()
            );

    /**
     * Form title.
     */
    protected String title_ = DEFAULT_TITLE;
}
