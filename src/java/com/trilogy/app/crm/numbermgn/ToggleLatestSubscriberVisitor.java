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
package com.trilogy.app.crm.numbermgn;

import java.util.Date;

import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;

/**
 * Sets the end date and the latest to the set one
 * without checking the subscriber ID.
 * @author arturo.medina@redknee.com
 *
 */
public class ToggleLatestSubscriberVisitor extends ToggleLatestVisitor
{

    /**
     * Same constructor as ToggleLatestVisitor.
     * @param latest specifies if it's the ;atest or not
     * @param subscriberId the subscirber id to verify
     * @param endTimestamp the end timestamp to set
     * @param home the home to store in the DB
     */
    public ToggleLatestSubscriberVisitor(final boolean latest,
            final String subscriberId,
            final Date endTimestamp,
            final Home home)
    {
        super(latest, subscriberId, endTimestamp, home);
    }

    /**
     * {@inheritDoc}
     */
    public void visit(final Context ctx, final Object obj)
        throws AgentException
    {
        final SubscriberNumberMgmtHistory history = (SubscriberNumberMgmtHistory)obj;

        toggleHistory(ctx, history);
    }


    /**
     * the serial version iud
     */
    private static final long serialVersionUID = 8512609964022614613L;

}
