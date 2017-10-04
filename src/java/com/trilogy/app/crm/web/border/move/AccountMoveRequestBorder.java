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
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.webcontrol.RequestServicer;
import com.trilogy.framework.xhome.webcontrol.ViewModeEnum;
import com.trilogy.framework.xlog.log.InfoLogMsg;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.move.MoveRequest;
import com.trilogy.app.crm.move.request.AbstractAccountMoveRequest;
import com.trilogy.app.crm.move.request.AccountMoveRequest;
import com.trilogy.app.crm.move.request.AccountMoveRequestXInfo;
import com.trilogy.app.crm.move.request.PostpaidServiceBasedSubscriberAccountMoveRequest;
import com.trilogy.app.crm.move.request.PostpaidServiceBasedSubscriberAccountMoveRequestXInfo;
import com.trilogy.app.crm.move.support.MoveRequestSupport;
import com.trilogy.app.crm.support.SpidSupport;


/**
 * This border generates an AccountMoveRequest and puts it in the context.
 * It also sets the read/write mode of any fields that need customization.
 *
 * @author Aaron Gourley
 * @since 8.1
 */
public class AccountMoveRequestBorder extends AbstractMoveRequestBorder
{
    /**
     * @{inheritDoc}
     */
    @Override
    public void service(Context ctx, HttpServletRequest req, HttpServletResponse res, RequestServicer delegate) throws ServletException, IOException
    {
        Context sCtx = ctx.createSubContext();

        Account account = (Account) sCtx.get(Account.class);
        
        MoveRequest request = null;
        if (account != null)
        {
            request = MoveRequestSupport.getMoveRequest(sCtx, account);   
        }
        else
        {
            request = new AccountMoveRequest();
        }
        if (request instanceof AccountMoveRequest)
        {
            setViewModes(sCtx, account, (AccountMoveRequest)request);   
        }

        sCtx.put(MoveRequest.class, request);
        delegate.service(sCtx, req, res);
    }

    
    private void setViewModes(Context ctx, Account account, AccountMoveRequest request)
    {
        Map<String, ViewModeEnum> viewModes = new HashMap<String, ViewModeEnum>();
        
        viewModes.put(
                AccountMoveRequestXInfo.EXISTING_BAN.getName(), 
                getExistingBANViewMode(ctx, request));
        
        if (request instanceof PostpaidServiceBasedSubscriberAccountMoveRequest)
        {
            viewModes.put(
                    PostpaidServiceBasedSubscriberAccountMoveRequestXInfo.NEW_DEPOSIT_AMOUNT.getName(),
                    getNewDepositViewMode(ctx, account));
        }   
        
        setViewModes(ctx, request, viewModes);
    }


    private ViewModeEnum getExistingBANViewMode(Context ctx, AccountMoveRequest request)
    {
        ViewModeEnum viewMode = ViewModeEnum.READ_ONLY;
        
        if (request != null
                && (request.getExistingBAN() == null
                        || request.getExistingBAN().trim().length() == 0
                        || request.getExistingBAN().trim().equals(AbstractAccountMoveRequest.DEFAULT_EXISTINGBAN)))
        {
            viewMode = ViewModeEnum.READ_WRITE;
        }
        
        return viewMode;
    }


    private ViewModeEnum getNewDepositViewMode(Context ctx, Account account)
    {
        ViewModeEnum viewMode = ViewModeEnum.READ_ONLY;
        if (account != null && (!account.isResponsible()))
        {
            try
            {
                CRMSpid spid = SpidSupport.getCRMSpid(ctx, account.getSpid());
                if (spid != null
                        && spid.isChangeSubDepositAmt())
                {
                    viewMode = ViewModeEnum.READ_WRITE;
                }
            }
            catch (HomeException e)
            {
                new InfoLogMsg(this, "Error retrieving SPID " + account.getSpid() + ".  New deposit amount field will be read-only.", e).log(ctx);
            }
        }
        return viewMode;
    }
}
