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
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.poller.agent;

import java.util.ArrayList;
import java.util.List;

import com.trilogy.app.crm.bean.calldetail.CallDetail;
import com.trilogy.app.crm.bean.calldetail.CallDetailHome;
import com.trilogy.app.crm.home.calldetail.SubscriberNotFoundHomeException;
import com.trilogy.app.crm.poller.Constants;
import com.trilogy.app.crm.poller.FilterOutException;
import com.trilogy.app.crm.poller.ProcessorInfo;
import com.trilogy.app.crm.poller.SMSCallDetailCreator;
import com.trilogy.app.crm.poller.event.CRMProcessor;
import com.trilogy.app.crm.poller.event.CRMProcessorSupport;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgent;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xlog.log.EntryLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.PMLogMsg;


/**
 * @author vcheng
 */
public class SMSBUnifiedBillingAgent implements ContextAgent, Constants
{
    public SMSBUnifiedBillingAgent(Context ctx, CRMProcessor processor)
    {
        super();
        processor_ = processor;

        this.creator_ = new SMSCallDetailCreator(ctx);
    }

    
    /**
     * @see com.redknee.framework.xhome.context.ContextAgent#execute(com.redknee.framework.xhome.context.Context)
     */
    public void execute(Context ctx) throws AgentException
    {
        final List params = new ArrayList();
        final ProcessorInfo info = (ProcessorInfo) ctx.get(ProcessorInfo.class);

        final PMLogMsg pmLogMsg = new PMLogMsg(PM_MODULE, "execute()");

        try
        {
        	try {
        		CRMProcessorSupport.makeArray(ctx, params, info.getRecord(), info.getStartIndex(), ',',info.getErid(), this);
           	} catch ( FilterOutException e){
				return; 
			}

            LogSupport.debug(ctx, this, "Params after processing = " + params , null);

            final Home tHome = (Home) ctx.get(CallDetailHome.class);
            if (tHome == null)
            {
                throw new AgentException("CallDetailHome not found in context");
            }

            final CallDetail t = creator_.createCallDetail(ctx, info, params);

            if (t == null)
            {
                LogSupport.minor(ctx, this, "Could not obtain parsed SMS ER311 !");
                processor_.saveErrorRecord(ctx, info.getRecord());
            }

            tHome.create(ctx, t);
        }
        catch (SubscriberNotFoundHomeException ex)
        {
            new EntryLogMsg(11029, this, "", "", new String[]
                {
            		CRMProcessorSupport.getField(params, creator_.getSMSBChargedMSISDNIndex())
        		}, null).log(ctx);
            processor_.saveErrorRecord(ctx, info.getRecord());
        }
        catch (final Throwable t)
        {
            LogSupport.minor(ctx, this, "Failed to process ER 311 because of Exception " + t.getMessage(), t);
            processor_.saveErrorRecord(ctx, info.getRecord());
        }
        finally
        {
            pmLogMsg.log(ctx);
            CRMProcessor.playNice(ctx, CRMProcessor.HIGH_ER_THROTTLING);
        }
    }

    private CRMProcessor processor_ = null;
    private SMSCallDetailCreator creator_ = null;
    private static final String PM_MODULE = SMSBUnifiedBillingAgent.class.getName();
}
