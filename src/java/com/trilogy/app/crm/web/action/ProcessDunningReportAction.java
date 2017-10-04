/*
 * This code is a protected work and subject to domestic and international copyright
 * law(s). A complete listing of authors of this work is readily available. Additionally,
 * source code is, by its very nature, confidential information and inextricably contains
 * trade secrets and other information proprietary, valuable and sensitive to Redknee. No
 * unauthorized use, disclosure, manipulation or otherwise is permitted, and may only be
 * used in accordance with the terms of the license agreement entered into with Redknee
 * Inc. and/or its subsidiaries.
 * 
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.web.action;

import java.io.IOException;
import java.io.PrintWriter;
import java.security.Permission;
import java.util.Date;

import javax.servlet.ServletException;

import com.trilogy.app.crm.agent.CronConstant;
import com.trilogy.app.crm.dunning.DunningProcess;
import com.trilogy.app.crm.dunning.DunningReport;
import com.trilogy.app.crm.dunning.DunningReportHome;
import com.trilogy.app.crm.dunning.DunningReportIdentitySupport;
import com.trilogy.app.crm.dunning.DunningReportStatusEnum;
import com.trilogy.app.crm.dunning.task.DunningReportProcessingLifecycleAgent;
import com.trilogy.app.crm.log.CoreERLogger;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.framework.lifecycle.LifecycleAgentSupport;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.support.IdentitySupport;
import com.trilogy.framework.xhome.web.action.SimpleWebAction;
import com.trilogy.framework.xhome.web.agent.WebAgents;
import com.trilogy.framework.xhome.web.util.Link;
import com.trilogy.framework.xlog.log.MinorLogMsg;

/**
 * Action for processing a dunning report.
 * 
 * @author Marcio Marques
 * @since 9.0
 */

public class ProcessDunningReportAction extends SimpleWebAction
{

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    /**
     * Creates a bean with the specific key and label set.
     * 
     * @param key
     *            Key.
     * @param label
     *            Label.
     */
    public ProcessDunningReportAction()
    {
        super("process", "Process");
    }


    public ProcessDunningReportAction(final Permission permission)
    {
        this();
        setPermission(permission);
    }


    /**
     * Writes a link for the action.
     * @param ctx Context.
     * @param out Output print writer.
     * @param bean Bean.
     * @param link Link.
     * @param action Action being performed.
     */
    public void writeLink(Context ctx, PrintWriter out, Object bean, Link link, String action)
    {
        DunningReport report = (DunningReport) bean;
        link.add("action", getKey());

        out.print("<a href=\"");
        link.write(out);
        out.print("\" onclick=\"try{return confirm('Proceed with the processing of all the scheduled Dunning Reports up to ");
        out.print(CoreERLogger.formatERDateDayOnly(report.getReportDate()));
        out.print(" on SPID ");
        out.print(report.getSpid());
        out.print("');}catch(everything){}\">");
        out.print(getLabel());
        out.print("</a>");
    } 
    
    public boolean isEnabled(Context ctx, Object bean)
    {
        if (bean == null)
        {
            return false;
        }
        else if (bean instanceof DunningReport)
        {
            DunningReport report = (DunningReport) bean;
            return (DunningReportStatusEnum.ACCEPTED_INDEX == report.getStatus() 
                    && !CalendarSupportHelper.get(ctx).getDateWithNoTimeOfDay(report.getReportDate()).
                    after(CalendarSupportHelper.get(ctx).getDateWithNoTimeOfDay(new Date())));
        }
        else
        {
            return false;
        }
    }
    

    
    /**
     * Executes the action
     * @param ctx Context.
     */
    public void execute(Context ctx) throws AgentException
    {
        String stringKey = WebAgents.getParameter(ctx, "key");
        String cmd = WebAgents.getParameter(ctx, "cmd");

        IdentitySupport idSupport = DunningReportIdentitySupport.instance();

        try
        {
            Home home = (Home) ctx
                    .get(DunningReportHome.class);
            DunningReport dunningReport = (DunningReport) home.find(ctx, idSupport
                    .fromStringID(stringKey));
            
            if (dunningReport!=null)
            {
                if (isEnabled(ctx, dunningReport))
                {
                    DunningProcess process = (DunningProcess) ctx.get(DunningProcess.class);
                    process.processSpidReport(ctx, dunningReport.getReportDate(), dunningReport.getSpid(), null);
                }
                else if (DunningReportStatusEnum.PENDING_INDEX == dunningReport.getStatus()
                        || DunningReportStatusEnum.REFRESHING_INDEX == dunningReport.getStatus())
                {
                    String message = "Dunning Report cannot be processed because it's not scheduled.";
                    WebAgents.getWriter(ctx).println("<font color=red>" + message + "</font><br/><br/>");
                }
                else
                {
                    String message = "Dunning Report cannot be processed because it's already being processed or processed.";
                    WebAgents.getWriter(ctx).println("<font color=red>" + message + "</font><br/><br/>");
                }
            }
            
        }
        catch (Throwable t)
        {
            String message = t.getMessage();
            WebAgents.getWriter(ctx).println("<font color=red>" + message + "</font><br/><br/>");
            new MinorLogMsg(this, message, t).log(ctx);       
        }
        
        Link link = new Link(ctx);
        link.add("cmd", cmd);

        try
        {
            WebAgents.service(ctx, link.write(), WebAgents.getWriter(ctx));
        }
        catch (ServletException ex)
        {
            throw new AgentException("Fail to redirect to " + cmd, ex);
        }
        catch (IOException ioEx)
        {
            throw new AgentException("Fail to redirect to " + cmd, ioEx);
        }
    } 
}
