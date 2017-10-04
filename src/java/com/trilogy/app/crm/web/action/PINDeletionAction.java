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

import java.io.PrintWriter;


import com.trilogy.app.crm.bean.AcquiredMsisdnPINManagement;
import com.trilogy.app.crm.bean.PinProvisioningStatusEnum;
import com.trilogy.app.crm.client.ProvisioningHomeException;
import com.trilogy.app.crm.support.PinManagerSupport;
import com.trilogy.app.pin.manager.ErrorCode;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgents;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.language.MessageMgr;
import com.trilogy.framework.xhome.web.action.SimpleWebAction;
import com.trilogy.framework.xhome.web.agent.WebAgents;
import com.trilogy.framework.xhome.web.renderer.ButtonRenderer;
import com.trilogy.framework.xhome.web.renderer.DefaultButtonRenderer;
import com.trilogy.framework.xhome.web.util.Link;
import com.trilogy.framework.xlog.log.LogSupport;


/**
 * @author dmishra Provides Custom action for handling PIN deletion.
 */
public class PINDeletionAction extends SimpleWebAction
{

    private static final long serialVersionUID = 2714255073056859177L;


    public PINDeletionAction()
    {
    }


    public PINDeletionAction(final String action, final String label)
    {
        super(action, label);
    }


    public void execute(Context ctx) throws AgentException
    {
        LogSupport.debug(ctx, this, "PINDeletionAction Invoked");
        String msisdn = WebAgents.getParameter(ctx, "key");
        PrintWriter out = WebAgents.getWriter(ctx);
        String message = "";
        String errorMsg = "";
        short result = -1;
        try
        {
            result = PinManagerSupport.deletePin(ctx, msisdn, PinManagerSupport.ER_REFERENCE);
        }
        catch (ProvisioningHomeException e)
        {
            LogSupport.debug(ctx, this, "Problem occured in PIN Deletion for MSISDN [" + msisdn + "] .Return Code "
                    + e.getResultCode(), e);
            result = (short) e.getResultCode();
        }
        catch (HomeException e)
        {
            LogSupport.debug(ctx, this, "Problem occured in PIN Deletion for MSISDN [" + msisdn + "]", e);
        }
        if (result == ErrorCode.SUCCESS)
        {
            message = "PIN Deletion is succesful for MSISDN [" + msisdn + "]. ";
            message = "<table width=\"70%\"><tr><td><center><b style=\"color:green;\">" + message + "</b></center></td></tr></table>";
        }
        else
        {
            message = "Problem occured in PIN Deletion for MSISDN [" + msisdn + "].<b>";
            message = "<table width=\"70%\"><tr><td><center><b style=\"color:red;\">" + message + "</b></center></td></tr></table>";
            errorMsg = PinManagerSupport.pinManagerResultToMessageMapping(ctx, result);
        }
        out.println(message);
        if (!"".equals(errorMsg))
        {
            out.println("<table width=\"70%\"><tr><td><center><b style=\"color:red;\">" + errorMsg + "</b></center></td></tr></table>");
        }
      
        out.println("<table width=\"70%\"><tr><td><center>");
        ContextAgents.doReturn(ctx);
        out.println("</center></td></tr></table>");
    }


    /*
     * (non-Javadoc)
     * 
     * @see com.redknee.framework.xhome.web.action.WebActionBean#isEnabled(com.redknee.framework.xhome.context.Context,
     *      java.lang.Object) If MSISDN is in Used state then pin delete action would be
     *      available.
     */
    public boolean isEnabled(Context ctx, Object bean)
    {
        if (bean != null)
        {
            AcquiredMsisdnPINManagement acqMsisdn = (AcquiredMsisdnPINManagement) bean;
            return (acqMsisdn.getState().getIndex() == PinProvisioningStatusEnum.PROVISIONED_INDEX);
        }
        return false;
    }
    
    @Override
    public void writeLink(Context ctx, PrintWriter out, Object bean, Link link)
    {
        link = modifyLink(ctx, bean, link);
        MessageMgr mmgr = new MessageMgr(ctx, this);
        AcquiredMsisdnPINManagement acqMsisdn = (AcquiredMsisdnPINManagement) bean;

        String label = mmgr.get("WebAction." + getKey() + ".SummaryLabel", mmgr.get("WebAction." + getKey()
                + ".Label", getLabel()));
        String msg = mmgr.get("WebAction." + getKey() + ".ConfirmMessage", "Confirm deleting the PIN for ");
        
        out.print("<a href=\"");
        link.write(out);
        out.print("\" onclick=\"try{return confirm('" + msg + acqMsisdn.getMsisdn()
                + "');}catch(everything){}\">");
        out.print(label);
        out.print("</a>");
    }

    @Override
    public void writeLinkDetail(Context ctx, PrintWriter out, Object bean, Link link)
    {
        link = modifyLink(ctx, bean, link);
        MessageMgr mmgr = new MessageMgr(ctx, this);
        ButtonRenderer br = (ButtonRenderer) ctx.get(ButtonRenderer.class, DefaultButtonRenderer.instance());
        AcquiredMsisdnPINManagement acqMsisdn = (AcquiredMsisdnPINManagement) bean;

        String label = mmgr.get("WebAction." + getKey() + ".DetailLabel", br.getButton(ctx, getKey(), mmgr.get(
                "WebAction." + getKey() + ".Label", getLabel())));
        String msg = mmgr.get("WebAction." + getKey() + ".ConfirmMessage", "Confirm deleting the PIN for ");
        
        out.print("<a href=\"");
        link.write(out);
        out.print("\" onclick=\"try{return confirm('" + msg + acqMsisdn.getMsisdn()
                + "');}catch(everything){}\">");
        out.print(label);
        out.print("</a>");    
    }
}