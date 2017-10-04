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

import java.util.ArrayList;
import java.util.List;

import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgent;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xlog.log.MinorLogMsg;
import com.trilogy.framework.xlog.log.PMLogMsg;

import com.trilogy.app.crm.bean.calldetail.CallDetail;
import com.trilogy.app.crm.bean.calldetail.CallDetailHome;
import com.trilogy.app.crm.poller.CallDetailCreator;
import com.trilogy.app.crm.poller.ProcessorInfo;
import com.trilogy.app.crm.poller.event.CRMProcessor;


/**
 * Generic ER 511 processing agent.
 *
 * @author cindy.wong@redknee.com
 * @since 24-Jun-08
 */
public abstract class AbstractAdvancedEventRatingAgent implements ContextAgent
{

    /**
     * Create a new instance of <code>AbstractAdvancedEventRatingAgent</code>.
     *
     * @param processor
     *            ER processor.
     * @param creator
     *            Call detail creator.
     */
    public AbstractAdvancedEventRatingAgent(final CRMProcessor processor, final CallDetailCreator creator)
    {
        this.processor_ = processor;
        this.creator_ = creator;
    }


    /**
     * {@inheritDoc}
     */
    public void execute(final Context ctx) throws AgentException
    {
        final List<String> params = new ArrayList<String>();
        final ProcessorInfo info = (ProcessorInfo) ctx.get(ProcessorInfo.class);

        final PMLogMsg pmLogMsg = new PMLogMsg(getPmModuleName(), "execute()");

        try
        {
            final List<CallDetail> callDetails = getCreator().createCallDetails(ctx, info, params);
            if (callDetails != null)
            {
                final Home home = (Home) ctx.get(CallDetailHome.class);
                for (final CallDetail cd : callDetails)
                {
                    home.create(ctx, cd);
                }
            }
        }
        catch (final Throwable t)
        {
            new MinorLogMsg(this, "Failed to process ER 511 because of Exception " + t.getMessage(), t).log(ctx);
            getProcessor().saveErrorRecord(ctx, info.getRecord());
        }
        finally
        {
            pmLogMsg.log(ctx);
            CRMProcessor.playNice(ctx, CRMProcessor.HIGH_ER_THROTTLING);
        }
    }


    /**
     * Returns the PM module name.
     *
     * @return PM module name.
     */
    protected abstract String getPmModuleName();


    /**
     * Returns the CRM processor.
     *
     * @return CRM processor.
     */
    protected CRMProcessor getProcessor()
    {
        return this.processor_;
    }


    /**
     * Returns the Call detail creator.
     *
     * @return Call detail creator.
     */
    protected CallDetailCreator getCreator()
    {
        return this.creator_;
    }

    /**
     * ER processor.
     */
    private final CRMProcessor processor_;
    /**
     * Call detail generator.
     */
    private final CallDetailCreator creator_;

}