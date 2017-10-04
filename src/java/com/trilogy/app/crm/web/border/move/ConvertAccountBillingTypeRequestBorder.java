/*
 * This code is a protected work and subject to domestic and international
 * copyright law(s). A complete listing of authors of this work is readily 
 * available. Additionally, source code is, by its very nature, confidential 
 * information and inextricably contains trade secrets and other information 
 * proprietary, valuable and sensitive to Redknee, no unauthorised use, 
 * disclosure, manipulation or otherwise is permitted, and may only be used 
 * in accordance with the terms of the licence agreement entered into with 
 * Redknee Inc. and/or its subsidiaries.
 *
 * Copyright � Redknee Inc. and its subsidiaries. All Rights Reserved. 
 */
package com.trilogy.app.crm.web.border.move;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.webcontrol.RequestServicer;
import com.trilogy.framework.xhome.webcontrol.ViewModeEnum;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.move.MoveRequest;
import com.trilogy.app.crm.move.request.AccountMoveRequest;
import com.trilogy.app.crm.move.request.ConvertAccountBillingTypeRequest;
import com.trilogy.app.crm.move.support.MoveRequestSupport;


/**
 * This border generates an ConvertBillingTypeRequestBorder and puts it in the context.
 * It also sets the read/write mode of any fields that need customization.
 *
 * @author Kumaran Sivasubramaniam
 * @since 8.1
 */
public class ConvertAccountBillingTypeRequestBorder extends AbstractMoveRequestBorder
{

    /**
     * @{inheritDoc}
     */
    @Override
    public void service(Context ctx, HttpServletRequest req, HttpServletResponse res, RequestServicer delegate) throws ServletException, IOException
    {
        Context sCtx = ctx.createSubContext();

        Account account = (Account) sCtx.get(Account.class);
        
        MoveRequest request = MoveRequestSupport.getMoveRequest(sCtx, account, ConvertAccountBillingTypeRequest.class);
        sCtx.put(MoveRequest.class,request);
        if (request instanceof ConvertAccountBillingTypeRequest)
        {   
            setViewModes(sCtx, account, (ConvertAccountBillingTypeRequest)request);
        }
        
        delegate.service(sCtx, req, res);
    }

    
    private void setViewModes(Context ctx, Account account, AccountMoveRequest request)
    {
        Map<String, ViewModeEnum> viewModes = new HashMap<String, ViewModeEnum>();
        setViewModes(ctx, request, viewModes);
    }


}
