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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.trilogy.app.crm.bean.BunleAdjustmentBulkLoad;
import com.trilogy.app.crm.bean.BunleAdjustmentBulkLoadWebControl;
import com.trilogy.app.crm.bundle.BundleBulkAdjustment;
import com.trilogy.app.crm.bundle.BundleBulkAdjustmentAgent;
import com.trilogy.app.crm.bundle.BundleBulkAdjustmentCSVHome;
import com.trilogy.app.crm.bundle.BundleBulkAdjustmentCSVSupport;
import com.trilogy.app.crm.home.BundleAdjustmentBulkAdapterAgent;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgent;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.language.MessageMgr;
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
 * This class handles the processing of the BundleAdjustmentBulkLoad entity defined in
 * BundleAdjustmentBulkLoad.xml model file.
 * 
 * @author suyash.gaidhani@redknee.com
 */
public class BundleAdjustmentBulkLoadRequestServicer implements RequestServicer
{
	/**
	 * Creates a new BundleAdjustmentBulkLoadRequestServicer.
	 */
	public BundleAdjustmentBulkLoadRequestServicer()
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
		bundleAdjustmentAgent_ = (ContextAgent) ctx.get(BundleBulkAdjustmentAgent.class);
				

		subCtx.put("MODE", OutputWebControl.EDIT_MODE);

		// Get the input from the form.
		final BunleAdjustmentBulkLoad form = new BunleAdjustmentBulkLoad();
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
				uploadAdjustments(subCtx, form);
				processingEndTime_ = Calendar.getInstance().getTimeInMillis();

				// Print the results to the HTML page and the report file.
				outputResults(numOfBundleAdjustmentsFailed_ <= 0);
			}
			catch (Exception e)
			{
				String formattedMsg = MessageFormat.format(
						"Problem occured during Bundle Adjustment Bulk Load:  {0}",
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
		Collection adjustmentColl = null;

		final String adjustmentFileStr = ((BunleAdjustmentBulkLoad) form)
				.getAdjustmentFile();
		final Home home = new BundleBulkAdjustmentCSVHome(ctx, adjustmentFileStr);
		adjustmentColl = home.selectAll(ctx);

		numOfAdjustmentsProcessed_ = 0; // Reset number of adjustments processed
		numOfBundleAdjustmentsFailed_ = 0; // Reset number of adjustments failed

		final Iterator adjustmentItr = adjustmentColl.iterator();
		while (adjustmentItr.hasNext())
		{
			BundleBulkAdjustment adjustment = (BundleBulkAdjustment) adjustmentItr.next();


			try
			{
				/*
				 * it will set the bundle adjustment date to be the current date if the one
				 * read from the file is null.
				 */
				SimpleDateFormat format =
						new SimpleDateFormat(
								BundleAdjustmentBulkAdapterAgent.DATE_FORMAT_STRING);
				if (adjustment.getTransDate() == null || adjustment.getTransDate().length() == 0 )
				{
					adjustment.setTransDate(format.format(new Date()));
				}
				else
				{
					try
					{
						format.parse(adjustment.getTransDate());
					}
					catch (ParseException e)
					{
						throw new HomeException(
								"TransDate of the adjustment is not in the proper format of "
										+ BundleAdjustmentBulkAdapterAgent.DATE_FORMAT_STRING, e);
					}
				}

				Context subCtx = ctx.createSubContext();
				subCtx.put(BundleBulkAdjustment.class, adjustment);

				// Perform bundle adjustment.
				bundleAdjustmentAgent_.execute(subCtx);
			}
			catch (Exception e)
			{
				if (reportFileOut_ != null)
				{
					// Write down in the report file, each adjustment that
					// failed to be
					// processed and an explanation of why it was not processed.
					String adjustmentStr = new String(BundleBulkAdjustmentCSVSupport
							.instance().append(new StringBuffer(), ',',
									adjustment));
					reportFileOut_.println(adjustmentStr);
					reportFileOut_.println(e.getMessage()); // The explanation
					reportFileOut_.println();
				}
				numOfBundleAdjustmentsFailed_++;
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
			htmlOut_.println("Bundle Adjustments successfully uploaded.");
			htmlOut_.println("</b></td></tr>");
		}
		else
		{
			htmlOut_
			.println("<tr><td align=\"center\"><b style=\"color:red;\">");
			htmlOut_.println("Problem occured during Bundle Adjustment Bulk Load.");
			htmlOut_.println("</b></td></tr>");
		}

		String formattedMsg = null;

		// Report total number of transaction processed.
		formattedMsg = MessageFormat.format(
				"Number of Bundle Adjustments processed:  {0}", new Object[]
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
			// Report total number of bundle adjustments failed.
			formattedMsg = MessageFormat.format(
					"Number of bundle adjustments failed:  {0}", new Object[]
							{ String.valueOf(numOfBundleAdjustmentsFailed_) });
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


	private final WebControl wc_ = new BunleAdjustmentBulkLoadWebControl();

	private ContextAgent bundleAdjustmentAgent_ = null;
	private String reportFilename_ = null;
	private PrintStream reportFileOut_ = null;
	private PrintWriter htmlOut_ = null;
	private int numOfAdjustmentsProcessed_ = 0;
	private int numOfBundleAdjustmentsFailed_ = 0;
	private long processingBeginTime_ = 0;
	private long processingEndTime_ = 0;

	private static final String REPORT_FILE_PREFIX = "BundleAdjBulkLoad.";
	private static final String REPORT_FILE_SUFFIX = ".txt";
    
	private final static ThreadLocalDateFormat DATE_FORMAT = new ThreadLocalDateFormat()
    {
        public Object initialValue()
        {
            return new SimpleDateFormat(DATE_FORMAT_PATTERN);
        }
    };


	public final static String DATE_FORMAT_PATTERN = "MM/dd/yy HH:mm:ss";
}



