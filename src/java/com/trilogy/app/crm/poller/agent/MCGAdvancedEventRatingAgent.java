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

import com.trilogy.app.crm.poller.MCGAdvancedEventCallDetailCreator;
import com.trilogy.app.crm.poller.event.CRMProcessor;


/**
 * MCG Advanced Event Rating ER (511) processing agent.
 *
 * @author cindy.wong@redknee.com
 * @since 24-Jun-08
 */
public class MCGAdvancedEventRatingAgent extends AbstractAdvancedEventRatingAgent
{

    /**
     * Create a new instance of <code>MCGAdvancedEventRatingAgent</code>.
     *
     * @param processor
     *            ER Processor.
     */
    public MCGAdvancedEventRatingAgent(final CRMProcessor processor)
    {
        super(processor, new MCGAdvancedEventCallDetailCreator());
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
