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
package com.trilogy.app.crm.transfer;

import java.io.IOException;
import java.rmi.RemoteException;

import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;

import com.trilogy.app.crm.home.PipelineFactory;


/**
 * Creates the pipeline necessary for managing private ContractGroup membership.
 *
 * @author gary.anderson@redknee.com
 */
public class ContractGroupMembershipPipelineFactory
    implements PipelineFactory
{
    /**
     * {@inheritDoc}
     */
    public Home createPipeline(final Context context, final Context serverCtx)
        throws RemoteException, HomeException, IOException, AgentException
    {
        final Home home = new ContractGroupMembershipServiceHome();
        context.put(ContractGroupMembershipHome.class, home);

        return home;
    }
}
