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


import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AcquiredMsisdnPINManagement;
import com.trilogy.app.crm.bean.AcquiredMsisdnPINManagementHome;
import com.trilogy.app.crm.bean.AcquiredMsisdnPINManagementXInfo;
import com.trilogy.app.crm.bean.PinProvisioningStatusEnum;
import com.trilogy.app.crm.client.ProvisioningHomeException;
import com.trilogy.app.crm.support.AccountSupport;
import com.trilogy.app.crm.support.PinManagerSupport;
import com.trilogy.app.pin.manager.ErrorCode;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgents;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.web.action.SimpleWebAction;
import com.trilogy.framework.xhome.web.agent.WebAgents;
import com.trilogy.framework.xlog.log.InfoLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MinorLogMsg;


/**
 * @author dmishra
 * 
 * Provides Custom action for handling PIN Generation.
 */
public class PINGenerationAction extends SimpleWebAction
{

    private static final long serialVersionUID = -9015110590451406135L;


    public PINGenerationAction()
    {
    }


    public PINGenerationAction(final String action, final String label)
    {
        super(action, label);
    }


    public void execute(Context ctx) throws AgentException
    {
        LogSupport.debug(ctx, this, "PINGenerationAction Invoked");
        String msisdn = WebAgents.getParameter(ctx, "key");
        PrintWriter out = WebAgents.getWriter(ctx);
        String message = "";
        String errorMsg = "";
        short result = -1;
        try
        {
            AcquiredMsisdnPINManagement acqMsisdn = (AcquiredMsisdnPINManagement) ((Home) ctx
                    .get(AcquiredMsisdnPINManagementHome.class)).find(ctx, new EQ(
                    AcquiredMsisdnPINManagementXInfo.MSISDN, msisdn));
            Account acct = AccountSupport.getAccount(ctx, acqMsisdn.getIdIdentifier());
            if (acct != null)
            {
                result = PinManagerSupport.generatePin(ctx, msisdn, acct, PinManagerSupport.ER_REFERENCE);
            }
            else
            {
                message = "<br/>Failed to find the holding account<br/>";
            }
            
        }
        catch (ProvisioningHomeException e)
        {
            LogSupport.debug(ctx, this, "Problem occured in PIN Generation for MSISDN [" + msisdn + " .Return Code "
                    + e.getResultCode(), e);
            result = (short) e.getResultCode();
        }
        catch (HomeException e)
        {
            LogSupport.debug(ctx, this, "Problem occured in PIN Generation for MSISDN [" + msisdn + "]", e);
        }
        if (result == ErrorCode.SUCCESS)
        {
            message += "PIN generation is succesful for MSISDN [" + msisdn + "]. ";
            message = "<table width=\"70%\"><tr><td><center><b style=\"color:green;\">" + message + "</b></center></td></tr></table>";
        }
        else if (result == ErrorCode.ENTRY_ALREADY_EXISTED)
        {
            message += PinManagerSupport.pinManagerResultToMessageMapping(ctx, ErrorCode.ENTRY_ALREADY_EXISTED);
            message += "</br>Unable to Generate New PIN. Please Reset or Delete PIN.";
            message = "<table width=\"70%\"><tr><td><center><b style=\"color:red;\">" + message + "</b></center></td></tr></table>";
        }
        else
        {
            message += "Problem occured in PIN Generation for MSISDN [" + msisdn + "].";
            message = "<table width=\"70%\"><tr><td><center><b style=\"color:red;\">" + message + "</b></center></td></tr></table>";
            errorMsg = PinManagerSupport.pinManagerResultToMessageMapping(ctx, result);
        }
        out.println(message);
        new InfoLogMsg(this,message,null).log(ctx);
        if (!"".equals(errorMsg))
        {
            out.println("<table width=\"70%\"><tr><td><center><b style=\"color:red;\">" + errorMsg + "</b></center></td></tr></table>");
            new MinorLogMsg(this,errorMsg,null).log(ctx);
        }
        out.println("<table width=\"70%\"><tr><td><center>");
        ContextAgents.doReturn(ctx);
        out.println("</center></td></tr></table>");
    }


    public boolean isEnabled(Context ctx, Object bean)
    {
        if (bean != null)
        {
            AcquiredMsisdnPINManagement msisdn = (AcquiredMsisdnPINManagement) bean;
            return (msisdn.getState().getIndex() == PinProvisioningStatusEnum.UNPROVISIONED_INDEX);
        }
        return false;
    }
    /**
     * Template method allowing modification of the Link after action execution.
     */
}
