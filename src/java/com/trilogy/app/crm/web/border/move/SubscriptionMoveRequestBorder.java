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
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.move.MoveRequest;
import com.trilogy.app.crm.move.request.AbstractSubscriptionMoveRequest;
import com.trilogy.app.crm.move.request.ServiceBasedSubscriptionMoveRequest;
import com.trilogy.app.crm.move.request.ServiceBasedSubscriptionMoveRequestXInfo;
import com.trilogy.app.crm.move.request.SubscriptionMoveRequest;
import com.trilogy.app.crm.move.request.SubscriptionMoveRequestXInfo;
import com.trilogy.app.crm.move.support.MoveRequestSupport;
import com.trilogy.app.crm.support.SpidSupport;


/**
 * This border generates a SubscriptionMoveRequest and puts it in the context.
 * It also sets the read/write mode of any fields that need customization.
 *
 * @author Aaron Gourley
 * @since 8.1
 */
public class SubscriptionMoveRequestBorder extends AbstractMoveRequestBorder
{
    /**
     * @{inheritDoc}
     */
    @Override
    public void service(Context ctx, HttpServletRequest req, HttpServletResponse res, RequestServicer delegate) throws ServletException, IOException
    {
        Context sCtx = ctx.createSubContext();

        Subscriber subscription = (Subscriber) sCtx.get(Subscriber.class);
        
        MoveRequest request = null;
        if (subscription != null)
        {
            request = MoveRequestSupport.getMoveRequest(sCtx, subscription);
        }
        else
        {
            request = new SubscriptionMoveRequest();
        }
        if (request instanceof SubscriptionMoveRequest)
        {
            setViewModes(sCtx, subscription, (SubscriptionMoveRequest)request);   
        }

        sCtx.put(MoveRequest.class, request);
        delegate.service(sCtx, req, res);
    }


    private void setViewModes(Context ctx, Subscriber subscription, SubscriptionMoveRequest request)
    {
        Map<String, ViewModeEnum> viewModes = new HashMap<String, ViewModeEnum>();

        viewModes.put(
                SubscriptionMoveRequestXInfo.OLD_SUBSCRIPTION_ID.getName(), 
                getOldSubscriptionIDViewMode(ctx, request));

        if (request instanceof ServiceBasedSubscriptionMoveRequest)
        {
            viewModes.put(
                    ServiceBasedSubscriptionMoveRequestXInfo.NEW_DEPOSIT_AMOUNT.getName(),
                    getNewDepositViewMode(ctx, subscription));

            viewModes.put(
                    ServiceBasedSubscriptionMoveRequestXInfo.EXPIRY_EXTENSION.getName(),
                    getExpiryExtensionViewMode(ctx, subscription));
        }

        setViewModes(ctx, request, viewModes);
    }


    private ViewModeEnum getOldSubscriptionIDViewMode(Context ctx, SubscriptionMoveRequest request)
    {
        ViewModeEnum viewMode = ViewModeEnum.READ_ONLY;
        
        if (request != null
                && (request.getOldSubscriptionId() == null
                        || request.getOldSubscriptionId().trim().length() == 0
                        || request.getOldSubscriptionId().trim().equals(AbstractSubscriptionMoveRequest.DEFAULT_OLDSUBSCRIPTIONID)))
        {
            viewMode = ViewModeEnum.READ_WRITE;
        }
        
        return viewMode;
    }


    private ViewModeEnum getNewDepositViewMode(Context ctx, Subscriber subscription)
    {
        ViewModeEnum viewMode = ViewModeEnum.READ_ONLY;
        if (subscription != null)
        {
            try
            {
                CRMSpid spid = SpidSupport.getCRMSpid(ctx, subscription.getSpid());
                if (spid != null
                        && spid.isChangeSubDepositAmt())
                {
                    viewMode = ViewModeEnum.READ_WRITE;
                }
            }
            catch (HomeException e)
            {
                new InfoLogMsg(this, "Error retrieving SPID " + subscription.getSpid() + ".  New deposit amount field will be read-only.", e).log(ctx);
            }
        }
        return viewMode;
    }


    private ViewModeEnum getExpiryExtensionViewMode(Context ctx, Subscriber subscription)
    {
        ViewModeEnum viewMode = ViewModeEnum.NONE;
        if (subscription != null)
        {
            try
            {
                Account account = subscription.getAccount(ctx);
                if (account != null
                        && account.isPrepaid())
                {
                    if (account.isPooled(ctx))
                    {
                        viewMode = ViewModeEnum.READ_WRITE;
                    }
                    else if (account.isIndividual(ctx))
                    {
                        Account parentAccount = account.getParentAccount(ctx);
                        if (parentAccount.isPooled(ctx))
                        {
                            viewMode = ViewModeEnum.READ_WRITE;
                        }
                    }
                }
            }
            catch (HomeException e)
            {
                new InfoLogMsg(this, "Error determining whether or not " + subscription.getBAN() + " is a prepaid pooled member subscriber.  "
                        + "Expiry extension field will be hidden.", e).log(ctx);
            }
        }
        return viewMode;
    }

}
