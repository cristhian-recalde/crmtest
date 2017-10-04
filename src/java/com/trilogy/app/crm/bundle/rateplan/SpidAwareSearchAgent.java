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

package com.trilogy.app.crm.bundle.rateplan;

import java.security.Principal;

import com.trilogy.framework.xhome.auth.bean.User;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgentProxy;
import com.trilogy.framework.xhome.msp.MSP;

/**
 * Provide SpidAware support for RatePlanAssociationSearchBorder.
 *
 * @author cindy.wong@redknee.com
 */
public class SpidAwareSearchAgent extends ContextAgentProxy
{
    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 2761526583393469493L;

    /**
     * If a principal is found in the context, add its SPID to the context. Theoretically, principal should always
     * exist, since this is only executed by <code>RatePlanAssociationSearchBorder</code>.
     *
     * @param context
     *            The operating context.
     * @throws AgentException
     *             Thrown if there are problems executing the search.
     * @see com.redknee.framework.xhome.context.ContextAgentProxy#execute(com.redknee.framework.xhome.context.Context)
     */
    @Override
    public void execute(final Context context) throws AgentException
    {
        final Context subcontext = context.createSubContext();
        final User principal = (User) context.get(Principal.class);
        if (principal != null)
        {
            MSP.setBeanSpid(subcontext, principal.getSpid());
        }
        delegate(subcontext);
    }
}
