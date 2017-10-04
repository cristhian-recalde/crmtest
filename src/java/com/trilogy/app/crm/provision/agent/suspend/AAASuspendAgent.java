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

import com.trilogy.app.crm.Common;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.core.Service;
import com.trilogy.app.crm.client.aaa.AAAClient;
import com.trilogy.app.crm.client.aaa.AAAClientException;
import com.trilogy.app.crm.provision.AAAAgentBase;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xlog.log.MajorLogMsg;

/**
 * Provides a ContextAgent for suspend AAA services.
 *
 * @author kumaran.sivasubramaniam@redknee.com
 */
public
class AAASuspendAgent extends AAAAgentBase 
{
    protected void processSubscriber(
            final Context context,
            final AAAClient client,
            final Service service,
            final Subscriber subscriber)
            throws AgentException
        {
            Common.OM_AAA_UNPROVISION.attempt(context);
            debugLog(context, "Attempting to suspend AAA", service, subscriber);

            boolean success = false;

            try
            {
                client.deleteProfile(context, subscriber);
                success = true;

                debugLog(context, "Suspend AAA without exception", service, subscriber);
            }
            catch (final AAAClientException exception)
            {
                final String message =
                    "Failed to suspend "
                    + debugService(service)
                    + " from "
                    + debugSubscriber(subscriber) + ".";

                new MajorLogMsg(this, message, exception).log(context);

                throw new AgentException(message, exception);
            }
            finally
            {
                if (success)
                {
                    Common.OM_AAA_UNPROVISION.success(context);
                }
                else
                {
                    Common.OM_AAA_UNPROVISION.failure(context);
                }
            }
        }



} // class
