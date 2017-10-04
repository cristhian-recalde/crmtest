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
package com.trilogy.app.crm.pos;

import java.util.Date;

import com.trilogy.app.crm.invoice.process.ProducerAgent;
import com.trilogy.app.crm.lifecycle.LifecycleAgentScheduledTask;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgent;
import com.trilogy.framework.xlog.log.InfoLogMsg;

/**
 * Methods that start the POS file extractions
 * @author Angie Li
 */
public class PointOfSaleProducer extends ProducerAgent
{
    
    public PointOfSaleProducer(Context ctx, 
            ContextAgent agent,
            String threadName,  
            int threadSize, 
            int queueSize)
    {
        super (ctx, agent, threadName, threadSize, queueSize);
    }
    
    /**
     * Puts the ExternalAgentsProcessor in the Context for the PointOfSaleConsumer to execute.  
     * @param ctx
     * @throws AgentException
     */
    public void createExternalAgentsFile(Context ctx, final LifecycleAgentScheduledTask agent) throws AgentException
    {
        ExternalAgentsProcessor processor = new ExternalAgentsProcessor(ctx, agent);
        
        /*
         * once we have updated the Account Accumulations for POS
         * we place the processor in a thread so the collection can
         * be processed and be written to the file.
         */
        Context subCtx = ctx.createSubContext();
        subCtx.put(PointOfSaleFileWriter.class, processor);

        execute(subCtx);
        
        new InfoLogMsg(this, "POS External Agents Extraction has begun.", null).log(ctx);
    }
    
    /**
     * Puts the CashierProcessor in the Context for the PointOfSaleConsumer to execute.
     * @param ctx
     * @throws AgentException
     */
    public void createCashierFile(Context ctx, final LifecycleAgentScheduledTask agent) throws AgentException
    {
        CashierProcessor processor = new CashierProcessor(ctx, agent);
        
        Context subCtx = ctx.createSubContext();
        subCtx.put(PointOfSaleFileWriter.class, processor);
        
        execute(subCtx);
        
        new InfoLogMsg(this, "POS Cashier Extraction has begun.", null).log(ctx);
    }
    
    /**
     * Puts the ConciliationProcessor in the Context for the PointOfSaleConsumer to execute.
     * @param ctx
     * @throws AgentException
     */
    public void createConciliationFile(Context ctx, final Date currentDate, final LifecycleAgentScheduledTask agent) throws AgentException
    {
        ConciliationProcessor processor = new ConciliationProcessor(ctx, currentDate, agent);
        
        Context subCtx = ctx.createSubContext();
        subCtx.put(PointOfSaleFileWriter.class, processor);
        
        execute(subCtx);
        
        new InfoLogMsg(this, "POS Conciliation Extraction has begun.", null).log(ctx);
    }

    /**
     * Puts the PaymentExceptionProcessor in the Context for the PointOfSaleConsumer to execute.
     * @param ctx
     * @throws AgentException
     */
    public void createPOSPaymentExceptionFile(Context ctx, final Date currentDate, final LifecycleAgentScheduledTask agent) throws AgentException
    {
        PaymentExceptionProcessor processor = new PaymentExceptionProcessor(ctx, currentDate, agent);
        
        Context subCtx = ctx.createSubContext();
        subCtx.put(PointOfSaleFileWriter.class, processor);
        
        execute(subCtx);
        
        new InfoLogMsg(this, "POS Payment Exception Extraction has begun.", null).log(ctx);
    }
    
    /**
     * Puts the POSIVRProcessor in the Context for the PointOfSaleConsumer to execute.
     * @param ctx
     * @throws AgentException
     */
    public void createPOSIVRExtractFile(Context ctx, final LifecycleAgentScheduledTask agent) throws AgentException
    {
        POSIVRProcessor processor = new POSIVRProcessor(ctx, agent);
        
        Context subCtx = ctx.createSubContext();
        subCtx.put(PointOfSaleFileWriter.class, processor);
        
        execute(subCtx);
        
        new InfoLogMsg(this, "POS IVR Extraction has begun.", null).log(ctx);
    }
}
