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
import com.trilogy.framework.xlog.log.MinorLogMsg;

import com.trilogy.app.crm.Common;
import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.move.MoveConstants;
import com.trilogy.app.crm.move.MoveRequest;
import com.trilogy.app.crm.move.request.ConvertAccountBillingTypeRequest;
import com.trilogy.app.crm.move.request.ConvertSubscriptionBillingTypeRequest;
import com.trilogy.app.crm.move.request.ConvertSubscriptionBillingTypeRequestXInfo;
import com.trilogy.app.crm.move.support.MoveRequestSupport;


/**
 * This border generates an ConvertBillingTypeRequestBorder and puts it in the context.
 * It also sets the read/write mode of any fields that need customization.
 *
 * @author Kumaran Sivasubramaniam
 * @since 8.1
 */
public class ConvertSubscriptionBillingTypeRequestBorder extends AbstractMoveRequestBorder
{
    /**
     * @{inheritDoc}
     */
    @Override
    public void service(Context ctx, HttpServletRequest req, HttpServletResponse res, RequestServicer delegate) throws ServletException, IOException
    {
        Context sCtx = ctx.createSubContext();
        sCtx.put(MoveConstants.NO_BILLCYCLE_CHANGE, new Boolean(true));
        Subscriber subscriber = (Subscriber) sCtx.get(Subscriber.class);

        Account account = null;
        try
        {
            account = subscriber.getAccount(sCtx);
        }
        catch (HomeException ex)
        {
            new MinorLogMsg(this," Unable to get all subscriptions", ex).log(ctx);
        }
        
        MoveRequest request = null;
        sCtx.put(Common.BILLING_TYPE_CONVERSION, subscriber);

        request = MoveRequestSupport.getMoveRequest(sCtx, subscriber, ConvertSubscriptionBillingTypeRequest.class);
       
        
        if (request instanceof ConvertSubscriptionBillingTypeRequest)
        {
            ConvertSubscriptionBillingTypeRequest conversion  = (ConvertSubscriptionBillingTypeRequest) request;
         
            if ( subscriber != null )
            {
            	conversion.setSpid(subscriber.getSpid());
                conversion.setSubscriptionClass(subscriber.getSubscriptionClass());
                conversion.setNewBAN(account);
                conversion.setOldSubscriptionId(subscriber.getBAN());      
                conversion.setOldSubscriptionId(subscriber);
                
                if (subscriber.getSubscriberType() == SubscriberTypeEnum.POSTPAID)
                {
                    conversion.setSubscriberType(SubscriberTypeEnum.PREPAID);
                }
                else
                {
                    conversion.setSubscriberType(SubscriberTypeEnum.POSTPAID);
                }
                
            }
            
            setViewModes(sCtx, subscriber, (ConvertSubscriptionBillingTypeRequest)request);   
        }

        sCtx.put(MoveRequest.class, request);
        delegate.service(sCtx, req, res);
    }


    private void setViewModes(Context ctx, Subscriber subscriber, ConvertSubscriptionBillingTypeRequest request)
    {
        Map<String, ViewModeEnum> viewModes = new HashMap<String, ViewModeEnum>();
        viewModes.put(ConvertSubscriptionBillingTypeRequestXInfo.SUBSCRIBER_TYPE.getName(), ViewModeEnum.READ_ONLY);
        viewModes.put(ConvertSubscriptionBillingTypeRequestXInfo.PRICE_PLAN.getName(), ViewModeEnum.READ_WRITE);
        viewModes.put(ConvertSubscriptionBillingTypeRequestXInfo.EXPIRY_EXTENSION.getName(), ViewModeEnum.NONE);
        viewModes.put(ConvertSubscriptionBillingTypeRequestXInfo.NEW_BAN.getName(), ViewModeEnum.NONE);
        viewModes.put(ConvertSubscriptionBillingTypeRequestXInfo.OLD_SUBSCRIPTION_ID.getName(), ViewModeEnum.READ_ONLY);
        if (subscriber.getSubscriberType() == SubscriberTypeEnum.POSTPAID)
        {
            viewModes.put(ConvertSubscriptionBillingTypeRequestXInfo.INITIAL_AMOUNT.getName(), ViewModeEnum.READ_WRITE);
            viewModes.put(ConvertSubscriptionBillingTypeRequestXInfo.CREDIT_LIMIT.getName(), ViewModeEnum.NONE);
            viewModes.put(ConvertSubscriptionBillingTypeRequestXInfo.NEW_DEPOSIT_AMOUNT.getName(), ViewModeEnum.NONE);
        }
        else
        {
            viewModes.put(ConvertSubscriptionBillingTypeRequestXInfo.INITIAL_AMOUNT.getName(), ViewModeEnum.NONE);
            viewModes.put(ConvertSubscriptionBillingTypeRequestXInfo.CREDIT_LIMIT.getName(), ViewModeEnum.READ_WRITE);
            viewModes.put(ConvertSubscriptionBillingTypeRequestXInfo.NEW_DEPOSIT_AMOUNT.getName(),
                    ViewModeEnum.READ_WRITE);
        }
        setViewModes(ctx, request, viewModes);
    }


}
