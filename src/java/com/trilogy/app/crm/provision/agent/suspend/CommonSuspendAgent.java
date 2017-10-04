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
package com.trilogy.app.crm.provision.agent.suspend;

import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.externalapp.ExternalAppEnum;
import com.trilogy.app.crm.provision.CommonProvisionAgentBase;
import com.trilogy.app.crm.provision.ProvisionAgentException;
import com.trilogy.app.crm.subscriber.provision.SubscriberProvisionHlrGatewayHome;
import com.trilogy.app.crm.support.ExternalAppSupportHelper;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;


/**
 * Common class for Generic, Sms and Voice provisioning and suspension agents.
 *
 * @author kumaran.sivasubramaniam
 */
public abstract class CommonSuspendAgent extends CommonProvisionAgentBase
{

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean hasMsisdnJustRemovedFromHlr(final Context ctx, final Subscriber sub)
    {
        return SubscriberProvisionHlrGatewayHome.hasMsisdnRemovedFromHlrGateway(ctx, sub);
    }

    /**
     * Executes the HLR commands.
     *
     * @param ctx
     *            The operating context.
     * @param hlrId
     *            ID of the HLR to use.
     * @param hlrCmds
     *            HLR commands to send.
     * @param subscriber
     *            The subsciber in question.
     * @throws AgentException
     *             Thrown if the execution fails.
     */
    public void callHlr(final Context ctx, final boolean isProvision, final Subscriber subscriber,
            final com.redknee.app.crm.bean.Service service, String aMsisdn, String bearTypeId) throws ProvisionAgentException
    {
        int result = executeHlrCommand(ctx, isProvision, subscriber, service, aMsisdn, bearTypeId);
        if (result != 0)
        {
            throw new ProvisionAgentException(ctx, ExternalAppSupportHelper.get(ctx)
                    .getSuspensionErrorMessage(ctx, ExternalAppEnum.HLR, result, service), result,
                    ExternalAppEnum.HLR);
        }
    }
}
