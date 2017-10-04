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

import javax.servlet.ServletException;

import com.trilogy.app.crm.dunning.DunningReport;
import com.trilogy.app.crm.dunning.DunningReportHome;
import com.trilogy.app.crm.dunning.DunningReportIdentitySupport;
import com.trilogy.app.crm.dunning.DunningReportStatusEnum;
import com.trilogy.app.crm.dunning.DunningReportXInfo;
import com.trilogy.app.crm.log.CoreERLogger;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.app.crm.support.SystemSupport;
import com.trilogy.framework.xhome.beans.XBeans;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.GT;
import com.trilogy.framework.xhome.elang.Or;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.support.IdentitySupport;
import com.trilogy.framework.xhome.web.action.SimpleWebAction;
import com.trilogy.framework.xhome.web.agent.WebAgents;
import com.trilogy.framework.xhome.web.util.Link;
import com.trilogy.framework.xlog.log.MinorLogMsg;


/**
 * Action for unscheduling a dunning report for processing.
 * 
 * @author Marcio Marques
 * @since 9.0
 */
public class UnacceptDunningReportAction extends SimpleWebAction
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
    public UnacceptDunningReportAction()
    {
        super("unaccept", "Undo accept");
    }


    public UnacceptDunningReportAction(final Permission permission)
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
        out.print("\" onclick=\"try{return confirm('Proceed with the " + action + " of the Dunning Report for ");
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
            return (!SystemSupport.isDunningReportAutoAccept(ctx, report.getSpid()) && DunningReportStatusEnum.ACCEPTED_INDEX == report.getStatus() && !existsFutureReportAccepted(ctx, report));
        }
        else
        {
            return false;
        }
    }
    
    private boolean existsFutureReportAccepted(Context ctx, DunningReport report)
    {
        try
        {
            Home home = (Home) ctx.get(DunningReportHome.class);
            And filter = new And();
            filter.add(new GT(DunningReportXInfo.REPORT_DATE, CalendarSupportHelper.get(ctx).getDateWithLastSecondofDay(report.getReportDate())));
            filter.add(new EQ(DunningReportXInfo.SPID, Integer.valueOf(report.getSpid())));
            filter.add(new Or().add(new EQ(DunningReportXInfo.STATUS, Integer.valueOf(DunningReportStatusEnum.ACCEPTED_INDEX))).
                                add(new EQ(DunningReportXInfo.STATUS, Integer.valueOf(DunningReportStatusEnum.PROCESSING_INDEX))));
            return home.find(ctx, filter) != null;
        }
        catch (HomeException e)
        {
            return true;
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
                    dunningReport.setStatus(DunningReportStatusEnum.PENDING_INDEX);
                    home.store(ctx, dunningReport);
                }
                else if (DunningReportStatusEnum.PENDING_INDEX == dunningReport.getStatus()
                        || DunningReportStatusEnum.REFRESHING_INDEX == dunningReport.getStatus())
                {
                    String message = "Dunning Report is already marked as pending.";
                    WebAgents.getWriter(ctx).println("<font color=red>" + message + "</font><br/><br/>");
                }
                else if (DunningReportStatusEnum.ACCEPTED_INDEX != dunningReport.getStatus())
                {
                    String message = "Dunning Report cannot be moved to pending state because it's already being processed or processed.";
                    WebAgents.getWriter(ctx).println("<font color=red>" + message + "</font><br/><br/>");
                }
                else
                {
                    String message = "Dunning Report cannot be un-accepted while future scheduled reports are not deleted or unscheduled.";
                    WebAgents.getWriter(ctx).println("<font color=red>" + message + "</font><br/><br/>");
                }
            }
            
        }
        catch (Throwable t)
        {
            String message = "Failed to un-accepted processing of Dunning Report: " + t.getMessage();
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
