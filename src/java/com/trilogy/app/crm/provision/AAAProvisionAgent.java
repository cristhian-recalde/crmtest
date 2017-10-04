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
import com.trilogy.framework.xlog.log.EntryLogMsg;
import com.trilogy.framework.xlog.log.MajorLogMsg;

import com.trilogy.app.crm.Common;
import com.trilogy.app.crm.bean.core.Service;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.client.aaa.AAAClient;
import com.trilogy.app.crm.client.aaa.AAAClientException;


/**
 * Provides a ContextAgent for provisioning AAA services.
 *
 * @author gary.anderson@redknee.com
 */
public
class AAAProvisionAgent
    extends AAAAgentBase
{
    /**
     * Provisions the Subscriber on the given Service.
     *
     * @param context The operating context.
     * @param client The AAAClient used for provisioning.
     * @param service The service that prompted this provisioning.
     * @param subscriber The subscriber to provision.
     *
     * @exception AgentException Thrown if any problems are encounterred durring
     * provisioning.
     */
    protected void processSubscriber(
        final Context context,
        final AAAClient client,
        final Service service,
        final Subscriber subscriber)
        throws AgentException
    {
        // It is unfortunate that only one line of this method is doing any
        // work, and that the rest of the code is OAM related.  Hopefully a
        // future refactorring of the provisioning code will allow us to
        // easily decorate agents for OAM overhead (with reuse).
        Common.OM_AAA_PROVISION.attempt(context);
        debugLog(context, "Attempting to provision AAA", service, subscriber);

        boolean success = false;
        Exception caughtException = null;

        try
        {
            client.createProfile(context, subscriber);
            success = true;

            debugLog(context, "Provisioned AAA without exception", service, subscriber);
        }
        catch (final AAAClientException exception)
        {
            caughtException = exception;

            final String message =
                "Failed to provision "
                + debugService(service)
                + " to "
                + debugSubscriber(subscriber) + ".";

            new MajorLogMsg(this, message, exception).log(context);

            throw new AgentException(message, exception);
        }
        catch (final RuntimeException exception)
        {
            caughtException = exception;

            final String message =
                debugService(service) + "provisioning failed";

            new MajorLogMsg(this, message, exception).log(context);

            throw new AgentException(message, exception);
        }
        finally
        {
            if (success)
            {
                Common.OM_AAA_PROVISION.success(context);
            }
            else
            {
                Common.OM_AAA_PROVISION.failure(context);

                final String[] parameters =
                {
                    subscriber.getId(),
                    subscriber.getMSISDN(),
                    Long.toString(service.getID()),
                    service.getName()
                };

                new EntryLogMsg(12901, this, "", "", parameters, caughtException).log(context);
            }
        }
    }


} // class
