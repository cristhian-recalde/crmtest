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
package com.trilogy.app.crm.provision;

import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xlog.log.MajorLogMsg;

import com.trilogy.app.crm.Common;
import com.trilogy.app.crm.bean.core.Service;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.client.aaa.AAAClient;
import com.trilogy.app.crm.client.aaa.AAAClientException;


/**
 * Provides a ContextAgent for unprovisioning AAA services.
 *
 * @author gary.anderson@redknee.com
 */
public
class AAAUnprovisionAgent
    extends AAAAgentBase
{
    /**
     * Unprovisions the Subscriber on the given Service.
     *
     * @param context The operating context.
     * @param client The AAAClient used for provisioning.
     * @param service The service that prompted this provisioning.
     * @param subscriber The subscriber to unprovision.
     *
     * @exception AgentException Thrown if any problems are encounterred durring
     * unprovisioning.
     */
    protected void processSubscriber(
        final Context context,
        final AAAClient client,
        final Service service,
        final Subscriber subscriber)
        throws AgentException
    {
        Common.OM_AAA_UNPROVISION.attempt(context);
        debugLog(context, "Attempting to unprovision AAA", service, subscriber);

        boolean success = false;

        try
        {
            client.deleteProfile(context, subscriber);
            success = true;

            debugLog(context, "Unprovisioned AAA without exception", service, subscriber);
        }
        catch (final AAAClientException exception)
        {
            final String message =
                    debugService(service) + "unprovisioning failed";

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
