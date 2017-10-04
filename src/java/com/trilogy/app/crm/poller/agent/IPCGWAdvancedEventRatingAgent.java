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

import com.trilogy.app.crm.poller.IPCGAdvancedEventCallDetailCreator;
import com.trilogy.app.crm.poller.event.CRMProcessor;


/**
 * IPCG ER 511 processing agent.
 *
 * @author vincci.cheng@redknee.com
 */
public class IPCGWAdvancedEventRatingAgent extends AbstractAdvancedEventRatingAgent
{

    /**
     * Create a new instance of <code>IPCGWAdvancedEventRatingAgent</code>.
     *
     * @param processor
     *            Processor of the ER.
     */
    public IPCGWAdvancedEventRatingAgent(final CRMProcessor processor)
    {
        super(processor, new IPCGAdvancedEventCallDetailCreator());
    }


    /**
     * {@inheritDoc}
     */
    @Override
    protected String getPmModuleName()
    {
        return getClass().getCanonicalName();
    }
}
