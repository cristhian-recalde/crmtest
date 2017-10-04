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
package com.trilogy.app.crm.poller.agent;

import java.text.MessageFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;

import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgent;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MajorLogMsg;
import com.trilogy.framework.xlog.log.MinorLogMsg;
import com.trilogy.framework.xlog.log.PMLogMsg;

import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberAuxiliaryService;
import com.trilogy.app.crm.bean.SubscriberHome;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.client.AppEcpClient;
import com.trilogy.app.crm.poller.Constants;
import com.trilogy.app.crm.poller.FilterOutException;
import com.trilogy.app.crm.poller.ProcessorInfo;
import com.trilogy.app.crm.poller.event.CRMProcessor;
import com.trilogy.app.crm.poller.event.CRMProcessorSupport;
import com.trilogy.app.crm.poller.event.URSProcessor;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.app.crm.support.SubscriberSupport;

/**
 * @author v.cheng@redknee.com
 */
public class URSFirstCallActivationAgent extends AbstractActivationAgent implements ContextAgent, Constants
{
    public URSFirstCallActivationAgent(final CRMProcessor processor)
    {
        super();
        processor_ = processor;
    }

    /* (non-Javadoc)
    * @see com.redknee.framework.xhome.context.ContextAgent#execute(com.redknee.framework.xhome.context.Context)
    */
    public void execute(final Context ctx) throws AgentException
    {
        final List params = new ArrayList();
        final ProcessorInfo info = (ProcessorInfo) ctx.get(ProcessorInfo.class);

        final PMLogMsg pmLogMsg = new PMLogMsg(PM_MODULE, "execute()");

        try
        {
            try
            {
                CRMProcessorSupport.makeArray(ctx, params, info.getRecord(), info.getStartIndex(), ',', info.getErid(),
                        this);
            }
            catch (FilterOutException e)
            {
                return;
            }

            if (LogSupport.isDebugEnabled(ctx))
            {
                new DebugLogMsg(this, URSProcessor.getDebugParams(params), null).log(ctx);
            }

            // Previous Subscriber State specified in the ER.
            final int previousState = CRMProcessorSupport.getInt(ctx, CRMProcessorSupport.getField(params,
                    INDEX_PREVIOUS_SUBSCRIBER_STATE), -1);

            // Next Subscriber State specified in the ER.
            final int nextState = CRMProcessorSupport.getInt(ctx, CRMProcessorSupport.getField(params,
                    INDEX_NEW_SUBSCRIBER_STATE), -1);

            // MSISDN specified in the ER.
            final String msisdnStr = CRMProcessorSupport.getField(params, INDEX_MSISDN);
            String msisdn = "";
            try
            {
                msisdn = CRMProcessorSupport.getMsisdn(msisdnStr);
            }
            catch (ParseException e)
            {
                final String formattedMsg = MessageFormat.format("Could not parse Msisdn \"{0}\". Caused by {1}.",
                        new Object[]{msisdnStr, e.getMessage()});

                throw new HomeException(formattedMsg, e);
            }

            // Subscriber with the MSISDN.
            Subscriber subscriber = null;
            
            // use the time of the ER to represent the activation time since that was when the activation event occured.
            Date activationTime = new Date(info.getDate());
            
            try
            {
                subscriber = SubscriberSupport.lookupSubscriberForMSISDN(ctx, msisdn, activationTime);
            }
            catch (HomeException e)
            {
                final String formattedMsg = MessageFormat.format(
                        "Failed to look-up subscriber for MSISDN \"{0}\". Caused by {1}.",
                        new Object[]{msisdnStr, e.getMessage()});

                throw new HomeException(formattedMsg, e);
            }

            

            // Handle the case that the previous subscriber state and the next
            // subscriber state are Available and Active respectively.
            if (previousState == AppEcpClient.AVAILABLE
                    && nextState == AppEcpClient.ACTIVE)
            {
                activateSubscriber(ctx, subscriber, activationTime, null);
            }
        }
        catch (final Throwable t)
        {
            new MinorLogMsg(this, "Failed to process ER 909 because of Exception " + t.getMessage(), t).log(ctx);
            processor_.saveErrorRecord(ctx, info.getRecord());
        }
        finally
        {
            pmLogMsg.log(ctx);
        }
    }

    private CRMProcessor processor_;
    protected static final int INDEX_MSISDN = 3;
    protected static final int INDEX_PREVIOUS_SUBSCRIBER_STATE = 4;
    protected static final int INDEX_NEW_SUBSCRIBER_STATE = 5;
    protected static final String PM_MODULE = URSFirstCallActivationAgent.class.getName();
    
}
