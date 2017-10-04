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
package com.trilogy.app.crm.move.processor.factory;

import com.trilogy.app.crm.move.MoveProcessor;
import com.trilogy.app.crm.move.MoveRequest;
import com.trilogy.app.crm.move.request.AccountConversionRequest;
import com.trilogy.app.crm.move.request.AccountExtensionMoveRequest;
import com.trilogy.app.crm.move.request.AccountMoveRequest;
import com.trilogy.app.crm.move.request.ConvertSubscriptionBillingTypeRequest;
import com.trilogy.app.crm.move.request.ReverseActivationMoveRequest;
import com.trilogy.app.crm.move.request.SubscriptionExtensionMoveRequest;
import com.trilogy.app.crm.move.request.SubscriptionMoveRequest;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xlog.log.PMLogMsg;

/**
 * This is the main version of the move processor factory for CRM.
 * 
 * It uses thread-level caching that automatically cleans up after itself
 * when nobody is holding a reference to the dependency manager instance.
 *
 * @author Aaron Gourley
 * @since 8.1
 */
public class CRMMoveProcessorFactory extends DefaultMoveProcessorFactory
{
	public static final String SUBSCRIPTION_MOVE_REQUEST = "subscriptionMoveRequest";
	
    private static MoveProcessorFactory CRMInstance_ = null;
    public static MoveProcessorFactory instance()
    {
        if (CRMInstance_ == null)
        {
            CRMInstance_ = new CRMMoveProcessorFactory();
        }
        return CRMInstance_;
    }
    
    protected CRMMoveProcessorFactory()
    {
    }

    public <MR extends MoveRequest> MoveProcessor<MR> getRequestSpecificInstance(Context ctx, MR request)
    {
        MoveProcessor<MR> processor = null;

        if(request instanceof AccountConversionRequest)
        {
            /*
             * This cast is type safe because we know that the request is a conversion subscription one
             * We therefore know that MR=ConvertAccountBillingTypeRequest (or subclass of it)
             */
            PMLogMsg pm = new PMLogMsg(CRMMoveProcessorFactory.class.getName(), "ConvertAccountProcessorCreation");
            processor =(MoveProcessor<MR>)ConvertAccountProcessorFactory.getNewInstance(ctx, (AccountConversionRequest)request);
            pm.log(ctx);
        }
        else if (request instanceof AccountMoveRequest)
        {
            /*
             * This cast is type safe because we know that the request is an account one
             * We therefore know that MR=AccountMoveRequest (or subclass of it)
             */
            PMLogMsg pm = new PMLogMsg(CRMMoveProcessorFactory.class.getName(), "AccountProcessorCreation");
            processor = (MoveProcessor<MR>)AccountMoveProcessorFactory.getNewInstance(ctx, (AccountMoveRequest)request);
            pm.log(ctx);
        }
        else if (request instanceof AccountExtensionMoveRequest)
        {
            /*
             * This cast is type safe because we know that the request is an account one
             * We therefore know that MR=AccountMoveRequest (or subclass of it)
             */
            PMLogMsg pm = new PMLogMsg(CRMMoveProcessorFactory.class.getName(), "AccountExtensionProcessorCreation");
            processor = (MoveProcessor<MR>)AccountExtensionMoveProcessorFactory.getNewInstance(ctx, (AccountExtensionMoveRequest)request);
            pm.log(ctx);
        }
        else if(request instanceof ConvertSubscriptionBillingTypeRequest)
        {
            /*
             * This cast is type safe because we know that the request is a conversion subscription one
             * We therefore know that MR=ConvertSubscriptionBillingTypeRequest (or subclass of it)
             */
            PMLogMsg pm = new PMLogMsg(CRMMoveProcessorFactory.class.getName(), "ConversionSubscriptionProcessorCreation");
            processor =(MoveProcessor<MR>)ConvertSubscriptionProcessorFactory.getNewInstance(ctx, (ConvertSubscriptionBillingTypeRequest)request);
            pm.log(ctx);
        }
        else if (request instanceof ReverseActivationMoveRequest)
        {
            /*
             * This cast is type safe because we know that the request is a subscription one
             * We therefore know that MR=ReverseActivationMoveRequest (or subclass of it)
             */
            PMLogMsg pm = new PMLogMsg(CRMMoveProcessorFactory.class.getName(), "ReverseActivationMoveProcessorCreation");
            processor = (MoveProcessor<MR>)ReverseActivationMoveProcessorFactory.getNewInstance(ctx, (ReverseActivationMoveRequest)request);
            pm.log(ctx);
        }
        else if (request instanceof SubscriptionMoveRequest)
        {
            /*
             * This cast is type safe because we know that the request is a subscription one
             * We therefore know that MR=SubscriptionMoveRequest (or subclass of it)
             */
        	ctx.put(SUBSCRIPTION_MOVE_REQUEST, true);
            PMLogMsg pm = new PMLogMsg(CRMMoveProcessorFactory.class.getName(), "SubscriptionProcessorCreation");
            processor = (MoveProcessor<MR>)SubscriptionMoveProcessorFactory.getNewInstance(ctx, (SubscriptionMoveRequest)request);
            pm.log(ctx);
        }
        else if (request instanceof SubscriptionExtensionMoveRequest)
        {
            /*
             * This cast is type safe because we know that the request is a subscription one
             * We therefore know that MR=SubscriptionMoveRequest (or subclass of it)
             */
            PMLogMsg pm = new PMLogMsg(CRMMoveProcessorFactory.class.getName(), "SubscriptionExtensionProcessorCreation");
            processor = (MoveProcessor<MR>)SubscriptionExtensionMoveProcessorFactory.getNewInstance(ctx, (SubscriptionExtensionMoveRequest)request);
            pm.log(ctx);
        }
        else
        { 
            PMLogMsg pm = new PMLogMsg(CRMMoveProcessorFactory.class.getName(), "DefaultProcessorCreation");
            processor = super.getRequestSpecificInstance(ctx,request);
            pm.log(ctx);   
        }

        return processor;
    }
}

