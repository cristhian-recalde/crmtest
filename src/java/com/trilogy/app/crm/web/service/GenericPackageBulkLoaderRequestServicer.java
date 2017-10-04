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
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.webcontrol.OutputWebControl;
import com.trilogy.framework.xhome.webcontrol.RequestServicer;
import com.trilogy.framework.xhome.webcontrol.WebControl;

import com.trilogy.app.crm.bean.CRMSpidKeyWebControl;
import com.trilogy.app.crm.bean.GenericPackageImportCSVSupport;
import com.trilogy.app.crm.bean.PackageGroupKeyWebControl;
import com.trilogy.app.crm.technology.SetTechnologyProxyWebControl;


/**RequestServicer which handles Package bulk load request.
 * @author deepak.mishra@redknee.com
 */
public class GenericPackageBulkLoaderRequestServicer implements RequestServicer
{

    protected final static WebControl SPID_WC = new CRMSpidKeyWebControl(true);
    protected final static WebControl PACKAGEGROUP_WC = new SetTechnologyProxyWebControl(new PackageGroupKeyWebControl(
            true));
    public final static String DEFAULT_TITLE = "<b>Package Bulk File: </b>";
    protected String title_ = DEFAULT_TITLE;
    protected String buttonString_ = "Process";
    protected String request_ = "upload";
    public final static int DEFAULT_SPID = 0;


    /**Default Constructor.
     */
    public GenericPackageBulkLoaderRequestServicer()
    {
    }


    /**@param title Title to be displayed on Web.
     */
    public GenericPackageBulkLoaderRequestServicer(String title)
    {
        setTitle(title);
    }


    /**Returns Title to be displayed on web.
     * @return Title to be displayed on web.
     */
    public String getTitle()
    {
        return title_;
    }


    /**Sets Title to be displayed on web.
     * @param title Title to be displayed on web.
     */
    public void setTitle(String title)
    {
        title_ = title;
    }


    /**
     * Returns the buttonString_.
     *
     * @return String
     */
    public String getButtonString()
    {
        return buttonString_;
    }


    /**
     * Returns the request_.
     *
     * @return String
     */
    public String getRequest()
    {
        return request_;
    }


    /**
     * Sets the buttonString_.
     *
     * @param buttonString
     *            The buttonString_ to set
     */
    public void setButtonString(String buttonString)
    {
        this.buttonString_ = buttonString;
    }


    /**
     * Sets the request_.
     *
     * @param request
     *            The request_ to set
     */
    public void setRequest(final String request)
    {
        this.request_ = request;
    }


    /** Template method to be overriden by subclasses as required. * */
    public void outputPreForm(Context ctx, HttpServletRequest req, HttpServletResponse res)
    {
        // nop
    }


    /** Template method to be overriden by subclasses as required. * */
    public void outputPostForm(Context ctx, HttpServletRequest req, HttpServletResponse res)
    {
        // nop
    }


    /* Accept the request for Bulk Load.
     * @see com.redknee.framework.xhome.webcontrol.RequestServicer#service(com.redknee.framework.xhome.context.Context, null, null)
     */
    @Override
    public void service(final Context ctx, final HttpServletRequest req, final HttpServletResponse res) throws ServletException,
            IOException
    {
        final PrintWriter out = res.getWriter();
        final String action = req.getParameter(getRequest());
        final String cmd = req.getParameter("cmd");
        final String xmenucmd = req.getParameter("xmenucmd");
        final String url = req.getRequestURI();
        if (action != null && action.trim().length() > 0)
        {
            if (action.equals(getButtonString()))
            {
                // process the bulk load file
                try
                {
                    final String fileLocation = req.getParameter("fileLocation");
                    out.println("<h3>Loading File: " + fileLocation + "</h3>");
                    out.println("<pre>");
                    processFile(ctx, out, fileLocation);
                    out.println("</pre>");
                }
                catch (Exception exc)
                {
                    out.println("<pre>");
                    exc.printStackTrace(out);
                    out.println("</pre>");
                }
            }
        }
        else
        {
            out.print("<FORM action=\"");
            out.print(url);
            out.println("\" method=\"POST\">");
            out.print("<INPUT type=\"hidden\" name=\"cmd\" value=\"");
            out.print(cmd);
            out.println("\">");
            out.print("<INPUT type=\"hidden\" name=\"xmenucmd\" value=\"");
            out.print(xmenucmd);
            out.println("\">");
            outputPreForm(ctx, req, res);
            ctx.put("MODE", OutputWebControl.EDIT_MODE);
            out.println(getTitle());
            out.println(" <INPUT type=\"text\" size=\"30\" name=\"fileLocation\" />");
            out.println(" <INPUT type=\"submit\" name=\"" + getRequest() + "\" value=\"" + getButtonString() + "\" />");
            // output any post form output
            outputPostForm(ctx, req, res);
            out.println("</FORM>");
        }
    }


    /**Send the control to the loader to process the file.
     * @param ctx Context.
     * @param out writer to write on web.
     * @param file Bulk load File.
     */
    public void processFile(final Context ctx, final  PrintWriter out, final String file)
    {
        // check if file exists
        final File inFile = new File(file);
        if (!inFile.exists() || !inFile.isFile())
        {
            out.println("The file " + file + " does not exist!");
            return;
        }
		final GenericPackageBulkLoader genericBulkLoader =
		    new GenericPackageBulkLoader(ctx, file,"");
        genericBulkLoader.start();
        out.println("Please check " + file + ".err for any error.");
    }
}