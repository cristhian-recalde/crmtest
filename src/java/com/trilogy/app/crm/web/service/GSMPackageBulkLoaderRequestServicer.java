/**
 * PackageBulkLoaderRequestServicer
 * 
 * @Author : Lanny Tse Date : Oct 15, 2002
 * 
 *         Copyright (c) Redknee, 2002 - all rights reserved
 **/
package com.trilogy.app.crm.web.service;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.trilogy.app.crm.bean.PackageBulkTask;
import com.trilogy.app.crm.bean.PackageBulkTaskXInfo;
import com.trilogy.app.crm.bean.PackageGroupKeyWebControl;
import com.trilogy.app.crm.home.pipelineFactory.PackageBulkLoaderPipelineFactory;
import com.trilogy.app.crm.technology.Technology;
import com.trilogy.app.crm.technology.TechnologyEnum;
import com.trilogy.app.crm.web.control.GSMPackageLoaderGroupFilterWebControl;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.language.MessageMgr;
import com.trilogy.framework.xhome.web.renderer.ButtonRenderer;
import com.trilogy.framework.xhome.web.renderer.DefaultButtonRenderer;
import com.trilogy.framework.xhome.webcontrol.OutputWebControl;
import com.trilogy.framework.xhome.webcontrol.WebControl;


/**
 * RequestServicer which handles Package bulk load request.
 */
public class GSMPackageBulkLoaderRequestServicer extends AbstractPackageBulkLoaderServicer
{


    public final static String DEFAULT_TITLE = "<b>SIM Package Bulk File: </b>";
    protected String title_ = DEFAULT_TITLE;
    protected String buttonString_ = "Process";
    protected String request_ = "upload";
    public final static int DEFAULT_SPID = 0;


    public GSMPackageBulkLoaderRequestServicer()
    {
    }


    public GSMPackageBulkLoaderRequestServicer(String title)
    {
        setTitle(title);
    }


    public String getTitle()
    {
        return title_;
    }


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
    public void setRequest(String request)
    {
        this.request_ = request;
    }


    /** Template method to be overriden by subclasses as required. **/
    public void outputPreForm(Context ctx, HttpServletRequest req, HttpServletResponse res)
    {
        // nop
    }


    /** Template method to be overriden by subclasses as required. **/
    public void outputPostForm(Context ctx, HttpServletRequest req, HttpServletResponse res)
    {
        // nop
    }


    public void service(Context ctx, HttpServletRequest req, HttpServletResponse res) throws ServletException,
            IOException
    {
        final HttpSession session = req.getSession();
        PrintWriter out = res.getWriter();
        String action = req.getParameter(getRequest());
        String cmd = req.getParameter("cmd");
        String xmenucmd = req.getParameter("xmenucmd");
        String url = req.getRequestURI();
        final Object gsmDto = session.getAttribute(GSMDTO);
        GSMPackageLoaderDTO dto = null;
        if (gsmDto != null)
        {
            dto = (GSMPackageLoaderDTO) gsmDto;
        }
        String selectedSpid = req.getParameter(PACKAGE_SPID_WEB_ID);
        String selectedGrp = req.getParameter(PACKAGE_GROUP_WEB_ID);
        if (selectedGrp == null)
        {
            selectedGrp = "";
        }
        
        if (dto != null)
        {
            if (dto.getSpid() != Integer.parseInt(selectedSpid != null ? selectedSpid : "0")
                    || !selectedGrp.equals(dto.getPackageGrp()))
            {
                action = "";
            }
        }
        
        if (action != null && action.trim().length() > 0)
        {
            if (action.equals(getButtonString()))
            {
                PackageBulkTask task = processRequest(ctx, req, res, out);
                if (null != task)
                {
                    out.println("<pre>");
                    task_wc.toWeb(ctx, out, PACKAGE_TASK_LINK_WEB_ID, task.getFileLocation());
                    out.println("</pre>");
                }
            }
        }
        else
        {
            final GSMPackageLoaderDTO gsmDto_obj = new GSMPackageLoaderDTO();
            out.print("<FORM action=\"");
            out.print(url);
            out.println("\" method=\"POST\">");
            out.print("<INPUT type=\"hidden\" name=\"cmd\" value=\"");
            out.print(cmd);
            out.println("\">");
            out.print("<INPUT type=\"hidden\" name=\"xmenucmd\" value=\"");
            out.print(xmenucmd);
            out.println("\">");
            // output any pre form output for additional input
            outputPreForm(ctx, req, res);
            ctx.put("MODE", OutputWebControl.EDIT_MODE);
            out.println("<b>Service Provider ID:</b> &nbsp;&nbsp;");
            final String spid = req.getParameter(PACKAGE_SPID_WEB_ID);
            if (spid != null && spid.length() > 0)
            {
                spid_wc.toWeb(ctx, out, PACKAGE_SPID_WEB_ID, Integer.valueOf(spid)); 
            }
            else
            {
                spid_wc.toWeb(ctx, out, PACKAGE_SPID_WEB_ID, Integer.valueOf(0));
            }
            gsmDto_obj.setSpid(Integer.parseInt(spid != null ? spid : "0"));
            out.println("<p/>");
            Technology.setBeanTechnology(ctx, TechnologyEnum.GSM);
            out.println("<b>SIM Package Group:</b> &nbsp;&nbsp;");
            final String pkgGrp = req.getParameter(PACKAGE_GROUP_WEB_ID);
            gsmDto_obj.setPackageGrp(pkgGrp);
            session.setAttribute(GSMDTO, gsmDto_obj);
            ctx.put(GSMDTO, gsmDto_obj);
            if (pkgGrp != null && pkgGrp.length() > 0)
            {
                packageGroup_wc.toWeb(ctx, out, PACKAGE_GROUP_WEB_ID, pkgGrp);   
            }
            else
            {
                packageGroup_wc.toWeb(ctx, out, PACKAGE_GROUP_WEB_ID, "");
            }
            out.println("<p/>");
            out.println("<b>Batch ID:</b> &nbsp;&nbsp;");
            packageBatchId_wc.toWeb(ctx, out, PACKAGE_BATCH_ID_WEB_ID, "");
            out.println("<p/>");
            out.println("<b>Batch PIN:</b> &nbsp;&nbsp;");
            packageBatchPin_wc.toWeb(ctx, out, PACKAGE_BATCH_PIN_WEB_ID, "");
            out.println("<p/>");
            out.println(getTitle());
            out.println(" <INPUT type=\"text\" size=\"30\" name=\"" + PACKAGE_FILE_LOCATION_WEB_ID + "\" />");
            out.println(" <INPUT type=\"submit\" name=\"" + getRequest() + "\" value=\"" + getButtonString() + "\" />");
            out.println("<p/>");
            final ButtonRenderer buttonRenderer = (ButtonRenderer) ctx.get(ButtonRenderer.class,
                    DefaultButtonRenderer.instance());
            buttonRenderer.inputButton(out, ctx, "Preview");
            // output any post form output
            outputPostForm(ctx, req, res);
            out.println("</FORM>");
        }
    }


    protected PackageBulkTask processRequest(Context ctx, HttpServletRequest req, HttpServletResponse res,
            PrintWriter out)
    {
        // process the bulk load file
        PackageBulkTask task = null;
        try
        {
            final MessageMgr mmgr = new MessageMgr(ctx, this);
            String fileLocation = req.getParameter(PACKAGE_FILE_LOCATION_WEB_ID);
            String packageGroup = req.getParameter(PACKAGE_GROUP_WEB_ID);
            int spid = Integer.parseInt(req.getParameter(PACKAGE_SPID_WEB_ID));
            // int packageType =
            // Integer.parseInt(req.getParameter("packageType"));
            String batchId = String.valueOf(packageBatchId_wc.fromWeb(ctx, req, PACKAGE_BATCH_ID_WEB_ID));
            String batchPin = String.valueOf(packageBatchPin_wc.fromWeb(ctx, req, PACKAGE_BATCH_PIN_WEB_ID));
            // Show which files we received
            out.println("<h3>" + mmgr.get(MMGR_TASK_FILE_LOAD_MSG_KEY, "Loading File: ") + fileLocation + "</h3>");
            // out.println("<h3>Loading File: " + fileLocation + "</h3>");
            // check if file exists
            File inFile = new File(fileLocation);
            if (inFile.exists() && inFile.isFile())
            {
                fileLocation = inFile.getAbsolutePath();
                if (null != batchId && !batchId.isEmpty())
                {
                    Home packageBulkTaskXdbHome = (Home) ctx
                            .get(PackageBulkLoaderPipelineFactory.PAKCAGEBULKTASK_DATA_HOME);
                    if ((task = (PackageBulkTask) packageBulkTaskXdbHome.find(ctx, fileLocation)) != null)
                    {
                        out.println("<pre>");
                        out.println(mmgr
                                .get(MMGR_TASK_FILE_EXISTS_MSG_KEY,
                                        "A task with File {0} already exists. Either delete that task entry or load it from another file",
                                        new String[]
                                            {fileLocation}));
                        out.println("</pre>");
                    }
                    else if ((task = (PackageBulkTask) packageBulkTaskXdbHome.find(ctx, new EQ(
                            PackageBulkTaskXInfo.BATCH_ID, batchId))) != null)
                    {
                        out.println("<pre>");
                        out.println(mmgr
                                .get(MMGR_TASK_BATCH_EXISTS_MSG_KEY,
                                        "A task with Batch ID {0} already exists. Either delete that task entry or chose a unique identifier",
                                        new String[]
                                            {batchId}));
                        out.println("</pre>");
                    }
                    else
                    {
                        out.println("<pre>");
                        final File fileProcessed = processFile(ctx, out, inFile, spid, packageGroup, batchId);
                        out.println("</pre>");
                        if (null != fileProcessed)
                        {
                            task = new PackageBulkTask();
                            task.setBatchId(batchId);
                            task.setBatchPin(batchPin);
                            task.setFileLocation(fileProcessed.getAbsolutePath());
                            task.setFileName(fileProcessed.getName());
                            task.setReprotFile(fileProcessed.getAbsolutePath() + "." + ERROR_FILE_EXTENSION);
                            packageBulkTaskXdbHome.create(ctx, task);
                        }
                    }
                }
                else
                {
                    out.println("<pre>");
                    out.println("Not Processed! A unique Batch Identifier is required");
                    out.println("</pre>");
                }
            }
            else
            {
                out.println("<pre>");
                out.println(mmgr.get(MMGR_NO_FILE_MSG_KEY, "Either the file {0} does not exist or is invalid!",
                        new String[]
                            {inFile.getAbsolutePath()}));
                out.println("<pre>");
            }
        }
        catch (Exception exc)
        {
            out.println("<pre>");
            exc.printStackTrace(out);
            out.println("</pre>");
        }
        return task;
    }


    /** Template Method to overide with actual File processing behaviour. **/
    public File processFile(Context ctx, PrintWriter out, File file, int spid, String packageGroup, String batchId)
    {
        final MessageMgr mmgr = new MessageMgr(ctx, this);
        GSMPackageBulkLoader simBulkLoader = new GSMPackageBulkLoader(ctx, file.getAbsolutePath(), spid, packageGroup,
                batchId);
        simBulkLoader.start();
        out.println(mmgr.get(MMGR_FILE_CHECK_MSG_KEY, "Please check {0}." + ERROR_FILE_EXTENSION + " for any errors.",
                new String[]
                    {file.getAbsolutePath()}));
        return file;
    }
    private final static WebControl packageGroup_wc = new GSMPackageLoaderGroupFilterWebControl(
            new PackageGroupKeyWebControl(1, false, true));
    
    public static String GSMDTO  = "GSM_DTO_OBJ";
}