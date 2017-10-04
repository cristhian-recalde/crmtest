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
package com.trilogy.app.crm.transfer;

import javax.servlet.http.HttpServletRequest;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.filter.Predicate;
import com.trilogy.framework.xhome.visitor.AbortVisitException;
import com.trilogy.framework.xhome.webcontrol.WebController;

public class TransferDisputeWebActionPredicate
    implements Predicate
{
    public boolean f(Context ctx, Object obj)
        throws AbortVisitException
    {
        try
        {
            TransferDispute dispute = (TransferDispute)obj;

            // TODO: the following hack is used to determine if we are in tablemode or not
            // once FW properly supports hiding web actions sperately from tablebiew and detail view
            // the hack can be removed.
            HttpServletRequest req = (HttpServletRequest)ctx.get(HttpServletRequest.class);
            String cmd = req.getParameter("CMD");
            String key = req.getParameter("key");
            boolean beanview =
                cmd != null           ||
                key != null           ||
                WebController.isCmd("Update",  req) ||
                WebController.isCmd("Preview", req) ||
                WebController.isCmd("Copy",    req) ||
                WebController.isCmd("Save",    req) ||
                WebController.isCmd("Delete",  req) ||
                WebController.isCmd("New",     req);

            return !TransferDisputeStatusEnum.ACCEPTED.equals(dispute.getState()) && !TransferDisputeStatusEnum.REJECTED.equals(dispute.getState()) && beanview;
        }
        catch(Exception e)
        {
            return false;
        }
    }
}