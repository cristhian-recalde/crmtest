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
import com.trilogy.app.crm.technology.TechnologyEnum;
import com.trilogy.app.crm.web.control.TDMAPackageLoaderTechnologyFilterWebControl;
import com.trilogy.app.crm.xhome.CustomEnumCollection;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.language.MessageMgr;
import com.trilogy.framework.xhome.web.renderer.ButtonRenderer;
import com.trilogy.framework.xhome.web.renderer.DefaultButtonRenderer;
import com.trilogy.framework.xhome.webcontrol.EnumWebControl;
import com.trilogy.framework.xhome.webcontrol.OutputWebControl;
import com.trilogy.framework.xhome.webcontrol.WebControl;


/**
 * RequestServicer which handles Package bulk load request.
 */
public class TDMAPackageBulkLoaderRequestServicer extends AbstractPackageBulkLoaderServicer
{

    
    public final static String DEFAULT_TITLE = "<b>TDMA/CDMA Package Bulk File: </b>";
    protected String title_ = DEFAULT_TITLE;
    protected String buttonString_ = "Process";
    protected String request_ = "upload";
    public final static int DEFAULT_SPID = 0;


    public TDMAPackageBulkLoaderRequestServicer()
    {
    }


    public TDMAPackageBulkLoaderRequestServicer(final String title)
    {
        setTitle(title);
    }


    public String getTitle()
    {
        return title_;
    }


    public void setTitle(final String title)
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
    public void setButtonString(final String buttonString)
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


    /** Template method to be overriden by subclasses as required. **/
    public void outputPreForm(final Context ctx, final HttpServletRequest req, final HttpServletResponse res)
    {
        // nop
    }


    /** Template method to be overriden by subclasses as required. **/
    public void outputPostForm(final Context ctx, final HttpServletRequest req, final HttpServletResponse res)
    {
        // nop
    }


    public void service(final Context ctx, final HttpServletRequest req, final HttpServletResponse res)
            throws ServletException, IOException
    {
        final HttpSession session = req.getSession();
        final PrintWriter out = res.getWriter();
        String action = req.getParameter(getRequest());
        final String cmd = req.getParameter("cmd");
        final String xmenucmd = req.getParameter("xmenucmd");
        final String url = req.getRequestURI();
        final Object dto_obj = session.getAttribute("DTO_OBJ");// ctx.get("DTO_OBJ");
        TDMAPackageLoaderDTO tdmaDto = null;
        if (dto_obj != null)
        {
            tdmaDto = (TDMAPackageLoaderDTO) dto_obj;
        }
        final String selTech = req.getParameter(PACKAGE_TECH_WEB_ID);
        final String selSpid = req.getParameter(PACKAGE_SPID_WEB_ID);
        String selGrp = req.getParameter(PACKAGE_GROUP_WEB_ID);
        if (selGrp == null)
        {
            selGrp = "";
        }
        if (tdmaDto != null)
        {
            if (tdmaDto.getSpid() != Integer.parseInt(selSpid != null ? selSpid : "0")
                    || tdmaDto.getTechnology() != Integer.parseInt(selTech != null ? selTech : String
                            .valueOf(TechnologyEnum.TDMA_INDEX)) || !selGrp.equals(tdmaDto.getPackageGrp()))
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
            final TDMAPackageLoaderDTO dto = new TDMAPackageLoaderDTO();
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
                spid_wc.toWeb(ctx, out, PACKAGE_SPID_WEB_ID, Integer.valueOf(Integer.parseInt(spid)));
            }
            else
            {
                spid_wc.toWeb(ctx, out, PACKAGE_SPID_WEB_ID, Integer.valueOf(0));
            }
            dto.setSpid(Integer.parseInt(spid != null ? spid : "0"));
            out.println("<p/>");
            out.println("<b>Technology:</b> &nbsp;&nbsp;");
            final String tech = req.getParameter(PACKAGE_TECH_WEB_ID);
            if (tech != null && tech.length() > 0)
            {
                final int technology = Integer.parseInt(tech);
                if (technology == TechnologyEnum.TDMA_INDEX)
                {
                    technology_wc.toWeb(ctx, out, "technology", TechnologyEnum.TDMA);
                }
                else if (technology == TechnologyEnum.CDMA_INDEX)
                {
                    technology_wc.toWeb(ctx, out, PACKAGE_TECH_WEB_ID, TechnologyEnum.CDMA);
                }
            }
            else
            {
                technology_wc.toWeb(ctx, out, PACKAGE_TECH_WEB_ID, TechnologyEnum.TDMA);
            }
            out.println("<p/>");
            dto.setTechnology(Integer.parseInt(tech != null ? tech : new String("" + TechnologyEnum.TDMA_INDEX)));
            out.println("<b>Package Group:</b> &nbsp;&nbsp;");
            final String pkgGrp = req.getParameter(PACKAGE_GROUP_WEB_ID);
            dto.setPackageGrp(pkgGrp);
            // Context subCtx = ctx.createSubContext();
            session.setAttribute("DTO_OBJ", dto);
            ctx.put("DTO_OBJ", dto);
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
            final String packageGroup = req.getParameter(PACKAGE_GROUP_WEB_ID);
            final int spid = Integer.parseInt(req.getParameter(PACKAGE_SPID_WEB_ID));
            final TechnologyEnum tech = (TechnologyEnum) technology_wc.fromWeb(ctx, req, PACKAGE_TECH_WEB_ID);
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
                        final File fileProcessed = processFile(ctx, out, inFile, spid, packageGroup, tech, batchId);
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
    public File processFile(final Context ctx, final PrintWriter out, final File file, final int spid,
            final String packageGroup, final TechnologyEnum tech, String batchId)
    {
        final MessageMgr mmgr = new MessageMgr(ctx, this);
        final TDMAPackageBulkLoader bulkLoader = new TDMAPackageBulkLoader(ctx, file.getAbsolutePath(), spid,
                packageGroup, tech, batchId);
        bulkLoader.start();
        out.println(mmgr.get(MMGR_FILE_CHECK_MSG_KEY, "Please check {0}." + ERROR_FILE_EXTENSION + " for any errors.",
                new String[]
                    {file.getAbsolutePath()}));
        return file;
    }
    
    protected final static WebControl packageGroup_wc = new TDMAPackageLoaderTechnologyFilterWebControl(
            new PackageGroupKeyWebControl(1, false, true));
    // SetTechnologyProxyWebControl
    private final static WebControl technology_wc = new EnumWebControl(new CustomEnumCollection(TechnologyEnum.TDMA,
            TechnologyEnum.CDMA), true);
}
