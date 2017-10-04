/*
 * Copyright (c) 1999-2003, REDKNEE. All Rights Reserved.
 * 
 * This software is the confidential and proprietary information of REDKNEE.
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the license
 * agreement you entered into with REDKNEE.
 * 
 * REDKNEE MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF THE
 * SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, OR
 * NON-INFRINGEMENT. REDKNEE SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY
 * LICENSEE AS A RESULT OF USING, MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS
 * DERIVATIVES.
 */
package com.trilogy.app.crm.web.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.trilogy.app.crm.bean.Adjustment;
import com.trilogy.app.crm.bean.Adjustment82;
import com.trilogy.app.crm.bean.Adjustment82CSVSupport;
import com.trilogy.app.crm.bean.Adjustment82TransientHome;
import com.trilogy.app.crm.bean.AdjustmentBulkLoad;
import com.trilogy.app.crm.bean.AdjustmentBulkLoadFormatEnum;
import com.trilogy.app.crm.bean.AdjustmentBulkLoadWebControl;
import com.trilogy.app.crm.bean.AdjustmentCSVHome;
import com.trilogy.app.crm.bean.AdjustmentCSVSupport;
import com.trilogy.app.crm.bean.AdjustmentOld;
import com.trilogy.app.crm.bean.AdjustmentOldCSVHome;
import com.trilogy.app.crm.bean.TransactionHome;
import com.trilogy.app.crm.home.AdjustmentValidator;
import com.trilogy.app.crm.home.TransactionToAdjustmentAdapter;
import com.trilogy.app.crm.home.ValidatingAmountTransactionHome;
import com.trilogy.app.crm.transaction.TransactionAdjustmentTypeLimitValidator;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.csv.GenericCSVHome;
import com.trilogy.framework.xhome.home.AdapterHome;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.ValidatingHome;
import com.trilogy.framework.xhome.language.MessageMgr;
import com.trilogy.framework.xhome.support.StringSeperator;
import com.trilogy.framework.xhome.util.format.ThreadLocalDateFormat;
import com.trilogy.framework.xhome.web.renderer.ButtonRenderer;
import com.trilogy.framework.xhome.web.renderer.DefaultButtonRenderer;
import com.trilogy.framework.xhome.web.renderer.DefaultFormRenderer;
import com.trilogy.framework.xhome.web.renderer.FormRenderer;
import com.trilogy.framework.xhome.web.util.ImageLink;
import com.trilogy.framework.xhome.web.util.Link;
import com.trilogy.framework.xhome.webcontrol.OutputWebControl;
import com.trilogy.framework.xhome.webcontrol.RequestServicer;
import com.trilogy.framework.xhome.webcontrol.WebControl;
import com.trilogy.framework.xlog.log.MajorLogMsg;

/**
 * This class handles the processing of the AdjustmentBulkLoad entity defined in
 * AdjustmentBulkLoad.xml model file.
 * 
 * @author jimmy.ng@redknee.com
 */
public class AdjustmentBulkLoadRequestServicer implements RequestServicer
{
    /**
     * Creates a new AdjustmentBulkLoadRequestServicer.
     */
    public AdjustmentBulkLoadRequestServicer()
    {
        super();
    }

    /**
     * INHERIT
     * 
     * @see com.redknee.framework.xhome.webcontrol.RequestServicer#service(com.redknee.framework.xhome.context.Context,
     *      javax.servlet.http.HttpServletRequest,
     *      javax.servlet.http.HttpServletResponse)
     */
    public void service(Context ctx, HttpServletRequest req,
            HttpServletResponse res) throws ServletException, IOException
    {
        final Context subCtx = ctx.createSubContext();

        // Set up a transaction home specifically for creating adjustment.
        transHome_ = (Home) subCtx.get(TransactionHome.class);
        transHome_ = new ValidatingAmountTransactionHome(subCtx, transHome_);
        transHome_ = new AdapterHome(transHome_,
                new TransactionToAdjustmentAdapter(subCtx));
        transHome_ = new ValidatingHome(transHome_, new AdjustmentValidator(
                subCtx));

        subCtx.put("MODE", OutputWebControl.EDIT_MODE);

        // Get the input from the form.
        final AdjustmentBulkLoad form = new AdjustmentBulkLoad();
        wc_.fromWeb(subCtx, form, req, "");

        final FormRenderer formRenderer = (FormRenderer) subCtx.get(
                FormRenderer.class, DefaultFormRenderer.instance());

        htmlOut_ = res.getWriter();
        formRenderer.Form(htmlOut_, subCtx);

        htmlOut_.print("<table>"); // Begin output to the HTML page

        final ButtonRenderer buttonRenderer = (ButtonRenderer) subCtx.get(
                ButtonRenderer.class, DefaultButtonRenderer.instance());

        if (buttonRenderer.isButton(subCtx, "Run"))
        {
            // Create a new report file.
            final String reportFilePath = form.getReportFilePath();
            reportFilename_ = reportFilePath + File.separator
                    + getReportFilename();

            try
            {
                reportFileOut_ = new PrintStream(new FileOutputStream(new File(
                        reportFilename_)));

                processingBeginTime_ = Calendar.getInstance().getTimeInMillis();
                uploadAdjustments(ctx, form);
                processingEndTime_ = Calendar.getInstance().getTimeInMillis();

                // Print the results to the HTML page and the report file.
                outputResults(numOfAdjustmentsFailed_ <= 0);
            }
            catch (Exception e)
            {
                String formattedMsg = MessageFormat.format(
                        "Problem occured during Adjustment Bulk Load:  {0}",
                        new Object[]
                            { e.getMessage() });
                new MajorLogMsg(this, formattedMsg, e).log(subCtx);

                // Print the results to the HTML page and the report file.
                outputError(formattedMsg);
            }
            finally
            {
                if (reportFileOut_ != null)
                {
                    reportFileOut_.close();
                }
            }
        }

        // Print the form and the buttons to the HTML page.
        htmlOut_.print("<tr><td>");
        wc_.toWeb(subCtx, htmlOut_, "", form);

        htmlOut_.print("</td></tr><tr><th align=\"right\">");

        buttonRenderer.inputButton(htmlOut_, subCtx, this.getClass(), "Run",
                false);
        outputHelpLink(subCtx, htmlOut_);

        htmlOut_.print("</th></tr></table>");

        formRenderer.FormEnd(htmlOut_);
    }

    /**
     * Upload adjustments for the file specified in the Adjustment Bulk Load
     * screen. We will create a new instance of AdjustmentCSVHome for that file,
     * decorate it with the AdapterHome so that we can pull Transaction beans
     * from it directly, then pass those beans to the TransactionHome for
     * permanent storage.
     * 
     * @param form
     *            The form from which we will retrieve the upload file name.
     */
    private void uploadAdjustments(Context ctx, final Object form)
            throws HomeException

    {
        final AdjustmentBulkLoadFormatEnum csvFormat = ((AdjustmentBulkLoad) form)
                .getCsvFormat();
        Collection adjustmentColl = null;
        // If the csv format is CRM6.0
        if (AdjustmentBulkLoadFormatEnum.PRE_6_0.equals(csvFormat))
        {
            final String adjustmentFileStr = ((AdjustmentBulkLoad) form)
                    .getAdjustmentFile();
            final Home home = new AdjustmentOldCSVHome(ctx, adjustmentFileStr);
            adjustmentColl = home.selectAll(ctx);
        }
        else if (AdjustmentBulkLoadFormatEnum.V_6_0.equals(csvFormat))
        {
            final String adjustmentFileStr = ((AdjustmentBulkLoad) form)
                    .getAdjustmentFile();
            final Home home = new AdjustmentCSVHome(ctx, adjustmentFileStr);
            adjustmentColl = home.selectAll(ctx);
        }
        else
        {
            final String adjustmentFileStr = ((AdjustmentBulkLoad) form)
                    .getAdjustmentFile();
            final Home home = new GenericCSVHome(new Adjustment82TransientHome(
					ctx), new Adjustment82CSVCustomSupport(), adjustmentFileStr);
            adjustmentColl = home.selectAll(ctx);
        }

        numOfAdjustmentsProcessed_ = 0; // Reset number of adjustments processed
        numOfAdjustmentsFailed_ = 0; // Reset number of adjustments failed

        final Iterator adjustmentItr = adjustmentColl.iterator();
        while (adjustmentItr.hasNext())
        {
            Adjustment82 adjustment = null;
            if (AdjustmentBulkLoadFormatEnum.PRE_6_0.equals(csvFormat))
            {
                adjustment = convertAjustmentOldToAjustment82((AdjustmentOld) adjustmentItr
                        .next());
            }
            else if (AdjustmentBulkLoadFormatEnum.V_6_0.equals(csvFormat))
            {
                adjustment = convertAjustmentOldToAjustment82((Adjustment) adjustmentItr
                        .next());
            }
            else
            {
                adjustment = (Adjustment82) adjustmentItr.next();
            }

			/*
			 * it will set the transation date to be the current date if the one
			 * read from the file is null.
			 */
            if (adjustment.getTransDate() == null)
            {
				SimpleDateFormat format =
				    new SimpleDateFormat(
				        TransactionToAdjustmentAdapter.DATE_FORMAT_STRING);
				adjustment.setTransDate(format.format(new Date()));
            }

			/*
			 * TT#1011254023: set external transaction ID as 0 if it's not
			 * provided
			 */
			if (adjustment.getExtTransactionId() == null
			    || adjustment.getExtTransactionId().isEmpty())
			{
				adjustment.setExtTransactionId("0");
			}

            try
            {

                // TT5123028699, partial fix to prevent the bug happening
                Context subCtx = ctx.createSubContext();
                
                /*
                 * [Cindy Wong] 2010-02-21: Make sure the tranction's limit validated.
                 */
                subCtx.put(TransactionAdjustmentTypeLimitValidator.VALIDATE_KEY, true);
                
                // Store each adjustment as a transaction into the
                // TransactionHome.
                transHome_.create(subCtx, adjustment);
            }
            catch (Exception e)
            {
                if (reportFileOut_ != null)
                {
                    // Write down in the report file, each adjustment that
                    // failed to be
                    // processed and an explanation of why it was not processed.
                    String adjustmentStr = new String(AdjustmentCSVSupport
                            .instance().append(new StringBuffer(), ',',
                                    adjustment));
                    reportFileOut_.println(adjustmentStr);
                    reportFileOut_.println(e.getMessage()); // The explanation
                    reportFileOut_.println();
                }
                numOfAdjustmentsFailed_++;
            }

            numOfAdjustmentsProcessed_++;
        }

    }

    /**
     * Write the results to the HTML page as well as the report file.
     * 
     * @param succeeded
     *            Indicate whether the processing is completed successfully or
     *            not. Different information will be output depending on this
     *            flag.
     */
    private void outputResults(final boolean succeeded)
    {
        if (succeeded)
        {
            htmlOut_
                    .println("<tr><td align=\"center\"><b style=\"color:green;\">");
            htmlOut_.println("Adjustments successfully uploaded.");
            htmlOut_.println("</b></td></tr>");
        }
        else
        {
            htmlOut_
                    .println("<tr><td align=\"center\"><b style=\"color:red;\">");
            htmlOut_.println("Problem occured during Adjustment Bulk Load.");
            htmlOut_.println("</b></td></tr>");
        }

        String formattedMsg = null;

        // Report total number of transaction processed.
        formattedMsg = MessageFormat.format(
                "Number of transaction processed:  {0}", new Object[]
                    { String.valueOf(numOfAdjustmentsProcessed_) });
        htmlOut_.println("<tr><td>");
        htmlOut_.println(formattedMsg);
        htmlOut_.println("</td></tr>");
        if (reportFileOut_ != null)
        {
            reportFileOut_.println(formattedMsg); // Also update the report file
        }

        if (!succeeded)
        {
            // Report total number of transaction failed.
            formattedMsg = MessageFormat.format(
                    "Number of transaction failed:  {0}", new Object[]
                        { String.valueOf(numOfAdjustmentsFailed_) });
            htmlOut_.println("<tr><td>");
            htmlOut_.println(formattedMsg);
            htmlOut_.println("</td></tr>");
            if (reportFileOut_ != null)
            {
                reportFileOut_.println(formattedMsg); // Also update the report
                                                      // file
            }
        }

        // Report total time elapsed.
        final double timeElapsedInSeconds = (processingEndTime_ - processingBeginTime_) / 1000.0;
        formattedMsg = MessageFormat.format("Time elapsed:  {0}s", new Object[]
            { String.valueOf(timeElapsedInSeconds) });
        htmlOut_.println("<tr><td>");
        htmlOut_.println(formattedMsg);
        htmlOut_.println("</td></tr>");
        if (reportFileOut_ != null)
        {
            reportFileOut_.println(formattedMsg); // Also update the report file
        }
    }

    /**
     * Write an error message to the HTML page as well as the report file.
     * 
     * @param msg
     *            The error message to be written.
     */
    private void outputError(final String msg)
    {
        htmlOut_.println("<tr><td align=\"center\"><b style=\"color:red;\">");
        htmlOut_.println(msg);
        htmlOut_.println("</b></td></tr>");

        if (reportFileOut_ != null)
        {
            reportFileOut_.println(msg); // Also update the report file
        }
    }

    /**
     * Copied from com.redknee.framework.xhome.webcontrol.BeanWebController.
     * This functionality is expected to be provided in a more reusable format
     * in an upcoming implementation of the Framework.
     */
    private void outputHelpLink(final Context context, final PrintWriter out)
    {
        final ImageLink image = new ImageLink(context);
        final MessageMgr mmgr = new MessageMgr(context, this);
        final Link link = new Link(context);

        link.add("mode", "help");
        link.add("border", "hide");
        link.add("menu", "hide");

        final String redirectURL = link.write();

        out.print("<a href=\"" + redirectURL + "\" onclick=\"showHelpMenu('"
                + redirectURL + "'); return false\"><img border=\"0\" "
                + image.getSource(mmgr.get("Button.Help", "Help"))
                + " align=\"right\" alt=\"Help\" /></a>");
    }

    /**
     * Return the report file name.
     * 
     * @return String The report file name to be returned.
     */
    private String getReportFilename()
    {
        final SimpleDateFormat formatter = new SimpleDateFormat("ddMMyy_HHmmss");
        return REPORT_FILE_PREFIX + formatter.format(new Date())
                + REPORT_FILE_SUFFIX;
    }

    private Adjustment82 convertAjustmentOldToAjustment82(
            AdjustmentOld adjustmentOld)
    {
        Adjustment82 adjustment = new Adjustment82();
        adjustment.setAcctNum(adjustmentOld.getAcctNum());
        adjustment.setMSISDN(adjustmentOld.getMSISDN());
        adjustment.setAdjustmentType(adjustmentOld.getAdjustmentType());
        adjustment.setAmount(adjustmentOld.getAmount());
        adjustment.setPaymentAgency(adjustmentOld.getPaymentAgency());
        adjustment.setLocationCode(adjustmentOld.getLocationCode());
        adjustment.setExtTransactionId(adjustmentOld.getExtTransactionId());
        adjustment.setPaymentDetails(adjustmentOld.getPaymentDetails());
        adjustment.setTransDate(adjustmentOld.getTransDate());
        adjustment.setCSRInput(adjustmentOld.getCSRInput());
        // adjustment.setReasonCode(adjustmentOld.getReasonCode());
        adjustment.setTransactionMethod(adjustmentOld.getTransactionMethod());
        adjustment.setCreditCardNumber(adjustmentOld.getCreditCardNumber());
        adjustment.setExpDate(adjustmentOld.getExpDate());
        adjustment.setTransDate(adjustmentOld.getTransDate());

        return adjustment;

    }

    private Adjustment82 convertAjustmentToAjustment82(
            Adjustment adjustmentOld)
    {
        Adjustment82 adjustment = new Adjustment82();
        adjustment.setAcctNum(adjustmentOld.getAcctNum());
        adjustment.setMSISDN(adjustmentOld.getMSISDN());
        adjustment.setAdjustmentType(adjustmentOld.getAdjustmentType());
        adjustment.setAmount(adjustmentOld.getAmount());
        adjustment.setPaymentAgency(adjustmentOld.getPaymentAgency());
        adjustment.setLocationCode(adjustmentOld.getLocationCode());
        adjustment.setExtTransactionId(adjustmentOld.getExtTransactionId());
        adjustment.setPaymentDetails(adjustmentOld.getPaymentDetails());
        adjustment.setTransDate(adjustmentOld.getTransDate());
        adjustment.setCSRInput(adjustmentOld.getCSRInput());
        adjustment.setReasonCode(adjustmentOld.getReasonCode());
        adjustment.setTransactionMethod(adjustmentOld.getTransactionMethod());
        adjustment.setCreditCardNumber(adjustmentOld.getCreditCardNumber());
        adjustment.setExpDate(adjustmentOld.getExpDate());
        adjustment.setTransDate(adjustmentOld.getTransDate());

        return adjustment;

    }
    private final WebControl wc_ = new AdjustmentBulkLoadWebControl();

    private Home transHome_ = null;
    private String reportFilename_ = null;
    private PrintStream reportFileOut_ = null;
    private PrintWriter htmlOut_ = null;
    private int numOfAdjustmentsProcessed_ = 0;
    private int numOfAdjustmentsFailed_ = 0;
    private long processingBeginTime_ = 0;
    private long processingEndTime_ = 0;

    private static final String REPORT_FILE_PREFIX = "AdjBulkLoad.";
    private static final String REPORT_FILE_SUFFIX = ".txt";

    

    
    private static class Adjustment82CSVCustomSupport extends Adjustment82CSVSupport
    {

		@Override
		public Object parse(StringSeperator seperator) 
			{
		      Adjustment82 bean = new Adjustment82();

		      
		      try { bean.setBAN(parseString(seperator.next())); } catch (Throwable t) { }
		      
		      try { bean.setMSISDN(parseString(seperator.next())); } catch (Throwable t) { }
		      
		      try { bean.setAdjustmentType(Integer.parseInt(seperator.next())); } catch (Throwable t) { }
		      
		      try { bean.setAmount(Long.parseLong(seperator.next())); } catch (Throwable t) { }
		      
		      try { bean.setPaymentAgency(parseString(seperator.next())); } catch (Throwable t) { }
		      
		      try { bean.setLocationCode(parseString(seperator.next())); } catch (Throwable t) { }
		      
		      try { bean.setExtTransactionId(parseString(seperator.next())); } catch (Throwable t) { }
		      
		      try { bean.setPaymentDetails(parseString(seperator.next())); } catch (Throwable t) { }
		      
		      try { bean.setTransDate(seperator.next()); } catch (Throwable t) { }
		      
		      try { bean.setCSRInput(parseString(seperator.next())); } catch (Throwable t) { }
		      
		      try { bean.setTransactionMethod(bean.parseCSVForLong(seperator.next(),-1)); } catch (Throwable t) { }
		      
		      try { bean.setCreditCardNumber(parseString(seperator.next())); } catch (Throwable t) { }
		      
		      try { bean.setExpDate(parseString(seperator.next())); } catch (Throwable t) { }
		      
		      try { bean.setReasonCode(bean.parseCSVForLong(seperator.next(), Long.MAX_VALUE)); } catch (Throwable t) { }
		      
		      try { bean.setExpiryDaysExt(Short.parseShort(seperator.next())); } catch (Throwable t) { }
		      
		      
		      return bean;
		   }
    	
		private final static ThreadLocalDateFormat DATE_FORMAT = new ThreadLocalDateFormat()
	    {
	        public Object initialValue()
	        {
	            return new SimpleDateFormat(DATE_FORMAT_PATTERN);
	        }
	    };
	    public final static String DATE_FORMAT_PATTERN = "MM/dd/yy HH:mm:ss";
    }
    
    
}
