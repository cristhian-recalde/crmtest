/*
 * Created on Feb 3, 2004
 * Copyright (c) 1999-2003, REDKNEE Inc. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of
 * REDKNEE. ("Confidential Information"). You shall not disclose such
 * Confidential Information and shall use it only in accordance with the
 * terms of the license agreement you entered into with REDKNEE.
 *
 * REDKNEE MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE
 * SUITABILITY OF THE SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR
 * A PARTICULAR PURPOSE, OR NON-INFRINGEMENT. REDKNEE SHALL NOT BE
 * LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING,
 * MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS DERIVATIVES.
 */
package com.trilogy.app.crm.bulkloader;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.trilogy.app.crm.agent.BeanInstall;
import com.trilogy.app.crm.bean.Msisdn;
import com.trilogy.app.crm.bean.MsisdnHome;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.webcontrol.OutputWebControl;
import com.trilogy.framework.xhome.webcontrol.RequestServicer;
import com.trilogy.framework.xhome.webcontrol.WebControl;
import com.trilogy.framework.xlog.log.InfoLogMsg;
import com.trilogy.framework.xlog.log.PMLogMsg;

/**
 * This Request Servicer is used to bulk load msisdns.
 * The tool will upload a csv file.
 *
 * The data in the CSV files needs to be valid and follow all validation rules.
 *
 * The data has to be in the Hummingbird approved format. Because of this, the 
 * data is loaded in BulkLoadMsisdn format, and then
 * copied into Msisdn objects.
 *
 * @author psperneac
 */
public class MsisdnBulkLoadRequestServicer implements RequestServicer
{

	/**
	 * @see com.redknee.framework.xhome.webcontrol.RequestServicer#service(com.redknee.framework.xhome.context.Context, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	public void service(Context ctx, HttpServletRequest req, HttpServletResponse res)
			throws ServletException, IOException
	{

		PrintWriter out = res.getWriter();
		MsisdnBulkLoad form = new MsisdnBulkLoad();
		Context subCtx = ctx.createSubContext();

		subCtx.put("MODE", OutputWebControl.EDIT_MODE);

		if (req.getParameter("Submit") != null)
		{
			wc_.fromWeb(subCtx, form, req, "");
			loadData(subCtx, form, req, res);
		}

		out.print("<form action=\"");
		out.print(req.getRequestURI());
		out.print("\" method=\"post\">");
		out.print("<input type=\"hidden\" name=\"cmd\" value=\"");
		out.print(req.getParameter("cmd"));
		out.print("\"/><table><tr><td>");

		wc_.toWeb(subCtx, out, "", form);

		out.println("</td></tr></table><input type=\"submit\" value=\"Run\" name=\"Submit\"/>");
		out.println("<input type=\"reset\"/>");
		out.println("</form>");
	}

	private void loadData(Context ctx, Object form, HttpServletRequest req, HttpServletResponse res)
			throws IOException
	{
		//File associations
		String msisdnFileStr = ((MsisdnBulkLoad) form).getMsisdnFile();
		String logPath = ((MsisdnBulkLoad) form).getLogFilePath();

		File logFile=null;
		FileWriter logFileWr=null;
		PrintWriter logPrintWr=null;
		String msg = null;
		try
		{
			//Try to create these log files
			logFile = new File(logPath + File.separator + "accountUpload.log");
			logFile.createNewFile();
			logFileWr = new FileWriter(logFile, true);
			logPrintWr = new PrintWriter(new BufferedWriter(logFileWr));
			logPrintWr.print("\nPackages Load: " + new Date() + "\n\n");
		}
		catch (IOException ioe)
		{
			//Error creating log files
			new InfoLogMsg(this, "IOException creating log files.", ioe).log(ctx);
		}

		//Account State information
		int numMsisdnProcess = 0;
		int numMsisdnSuccess = 0;

		final PMLogMsg createAllPackagesPM = new PMLogMsg(PM_MODULE, "Create All Packages");
		try
		{
			//Create the Account CSV Home
			final PMLogMsg loadPackagesCSVPM = new PMLogMsg(PM_MODULE, "Load All Packages from CSV");
			Home pcsv = new BulkLoadMsisdnCSVHome(ctx, msisdnFileStr);

			loadPackagesCSVPM.log(ctx);

			try
			{
				Msisdn template=(Msisdn) ctx.get(BeanInstall.BULK_MSISDN_TEMPLATE);
				MsisdnCreateVisitor v=new MsisdnCreateVisitor((Home)ctx.get(MsisdnHome.class),template,logPrintWr);
				
				pcsv.forEach(ctx,v);
				
				numMsisdnProcess=v.getProcessed();
				numMsisdnSuccess=v.getSuccess();
			}
			catch (Exception e)
			{
				//An error occured during the selectAll of the CSVHome
				try
				{
					msg = "Failure reading the PackageCSVHome: " + e.getMessage() + "\n";
					logPrintWr.print(msg);
					e.printStackTrace(logPrintWr);
					logPrintWr.println();
				}
				catch (Throwable tt)
				{
					//Can't write to log file
					new InfoLogMsg(this, "Unable to write to Package log file: " + msg, tt)
							.log(ctx);
				}
			}
		}
		catch (Exception e)
		{
			//An error occured during the creation of the AccountCSVHome
			try
			{
				msg = "Failure creating the PackageCSVHome: " + e.getMessage() + "\n";
				logPrintWr.print(msg);
				e.printStackTrace(logPrintWr);
				logPrintWr.println();
			}
			catch (Throwable tt)
			{
				//Can't write to log file
				new InfoLogMsg(this, "Unable to write to Account log file: " + msg, tt).log(ctx);
			}
		}
		finally
		{
			createAllPackagesPM.log(ctx);
		}

		//Output the final stats to the log files and close the files used
		//Account
		try
		{

			logPrintWr.print("\n*********************RESULTS***************************\n");
			logPrintWr.print("Number of packages processed: " + numMsisdnProcess + "\n");
			logPrintWr.print("Successful Transactions: " + numMsisdnSuccess + "\n");
			logPrintWr.print("Failed Transactions: " + (numMsisdnProcess - numMsisdnSuccess) + "\n");
			logPrintWr.print("*********************RESULTS***************************\n");
		}
		catch (Throwable throwable)
		{
			//Error writing results to file
			new InfoLogMsg(this, "Unable to write to transaction count to log file.", throwable)
					.log(ctx);
		}

		try
		{
			logPrintWr.flush();
			logFileWr.flush();
			logPrintWr.close();
			logFileWr.close();
		}
		catch (IOException ioe)
		{
			//Error closing log files
			new InfoLogMsg(this, "Unable to close log files.", ioe).log(ctx);
		}
		
		//Output the results to the screen
		outputResults(ctx, numMsisdnProcess, numMsisdnSuccess, req, res);
	}

	//This method is used to output a result summary to the web
	private void outputResults(Context ctx, int numMsisdnProcess, int numMsisdnSuccess, HttpServletRequest req, HttpServletResponse res) throws IOException
	{
		PrintWriter out = res.getWriter();
		out.print("<H1>Msisdn Bulk Load</H1>\n");
		out.print("<BR><P>");
		out.print("Number of packages processed: " + numMsisdnProcess + "\n");
		out.print("<BR>");
		out.print("Successful Transactions: " + numMsisdnSuccess + "\n");
		out.print("<BR>");
		out.print("Failed Transactions: " + (numMsisdnProcess - numMsisdnSuccess) + "\n");
		out.print("<BR>");
	}

	WebControl wc_=new MsisdnBulkLoadWebControl();

	private static final String	PM_MODULE	= MsisdnBulkLoadRequestServicer.class.getName();
}