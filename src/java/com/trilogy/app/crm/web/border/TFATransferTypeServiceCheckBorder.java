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

package com.trilogy.app.crm.web.border;

import java.io.IOException;
import java.security.Principal;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.trilogy.app.crm.transfer.ParticipantTypeEnum;
import com.trilogy.app.crm.transfer.TransferContract;
import com.trilogy.app.crm.transfer.TransferTypeHome;
import com.trilogy.app.crm.transfer.contract.TransferContractException;
import com.trilogy.app.crm.transfer.contract.TransferTypeFacade;
import com.trilogy.framework.xhome.auth.bean.User;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.NullHome;
import com.trilogy.framework.xhome.web.border.Border;
import com.trilogy.framework.xhome.webcontrol.AbstractWebControl;
import com.trilogy.framework.xhome.webcontrol.MessageRequestServicer;
import com.trilogy.framework.xhome.webcontrol.RequestServicer;
import com.trilogy.framework.xlog.log.LogSupport;

public class TFATransferTypeServiceCheckBorder
    implements Border
{
    public void service(Context ctx, HttpServletRequest req, HttpServletResponse res, final RequestServicer delegate)
        throws ServletException, IOException
    {
        Context subCtx = ctx.createSubContext();
        TransferTypeFacade service = (TransferTypeFacade)ctx.get(TransferTypeFacade.class);
        try
        {
            service.retrieveTransferType(ctx, getSpid(ctx));
        }
        catch (final TransferContractException e)
        {
            LogSupport.major(ctx, this, "Unable to retrieve transfer types from TFA.", e);
            String msg = "<pre><center><font size=\"1\" face=\"Verdana\" color=\"red\"><b>Unable to retrieve Transfer types from TFA : " + e.getMessage() + "</b></font></center></pre>";
            MessageRequestServicer msgServicer = new MessageRequestServicer(msg);
            msgServicer.service(subCtx, req, res);
            subCtx.put(TransferTypeHome.class, new NullHome());
        }

        delegate.service(subCtx, req, res);
    }

    private int getSpid(final Context context)
    {
        // TODO: SPID within transfer types makes no sense, particularly because
        // CRM+ may not know the
        final int spid;

        final Object bean = context.get(AbstractWebControl.BEAN);
        if (bean != null && bean instanceof TransferContract)
        {
            final TransferContract contract = (TransferContract)bean;

            if (contract.getParticipantType() == ParticipantTypeEnum.CONTRACT_GROUP)
            {
                spid = contract.getSpid();
            }
            else
            {
                spid = contract.getContributingSpid();
            }
        }
        else
        {
            // Nothing else but to use the SPID of the agent.
            final User user = (User)context.get(Principal.class);
            if (user != null)
            {
                spid = user.getSpid();
            }
            else
            {
                // ???
                spid = 0;
            }
        }

        return spid;
    }
}