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
package com.trilogy.app.crm.extension.auxiliaryservice.core.custom;

import java.util.HashSet;
import java.util.Set;

import com.trilogy.app.crm.bean.NgrcOptinTypeEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberAuxiliaryService;
import com.trilogy.app.crm.bean.externalapp.ExternalAppEnum;
import com.trilogy.app.crm.client.ClientException;
import com.trilogy.app.crm.client.ngrc.AppNGRCClient;
import com.trilogy.app.crm.extension.AssociableExtension;
import com.trilogy.app.crm.extension.ExtensionAssociationException;
import com.trilogy.app.crm.support.ExternalAppSupport;
import com.trilogy.app.crm.support.SubscriberSupport;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.InfoLogMsg;
import com.trilogy.framework.xlog.log.MinorLogMsg;

/**
 * @author Marcio Marques
 * @since 9.1.2
 *
 */
public class NGRCOptInAuxSvcExtension extends
com.redknee.app.crm.extension.auxiliaryservice.core.NGRCOptInAuxSvcExtension implements AssociableExtension<SubscriberAuxiliaryService>
{

    @Override
    public void associate(Context ctx, SubscriberAuxiliaryService service) throws ExtensionAssociationException
    {
        Subscriber subscriber = getSubscriber(ctx, service);
        
        try
        {

            AppNGRCClient client = getClient(ctx);
            if (this.getDataOptInType() == NgrcOptinTypeEnum.BASE)
            {
                client.addOptIn(ctx, subscriber.getMsisdn(), subscriber.getSpid(), Integer.parseInt(subscriber.getDataPricePlan(ctx)),
                        (int) this.getDataOptInID(), -1, -1, "", false);
            }
            else
            {
                client.addOptIn(ctx, subscriber.getMsisdn(), subscriber.getSpid(), -1, -1, -1,
                        (int) this.getDataOptInID(), "", false);
            }
            service.setProvisionActionState(true);
        }
        catch (ClientException e)
        {
            final String msg = String.format("Unable to provision Data OptIn Auxiliary Service (id=%d) due to: %s",
                    service.getAuxiliaryServiceIdentifier(), e.getMessage());
            new MinorLogMsg(this, msg, e).log(ctx);
            service.setProvisionActionState(false);
            throw new ExtensionAssociationException(ExternalAppEnum.DATA_OPTIN, msg, e.getResultCode(), e, false);
        }
    }


    @Override
    public void updateAssociation(Context ctx, SubscriberAuxiliaryService service) throws ExtensionAssociationException
    {
    }

    @Override
    public void dissociate(Context ctx, SubscriberAuxiliaryService service) throws ExtensionAssociationException
    {
        Subscriber subscriber = getSubscriber(ctx, service);

        try
        {
            AppNGRCClient client = getClient(ctx);
            if (this.getDataOptInType() == NgrcOptinTypeEnum.BASE)
            {
                client.deleteOptIn(ctx, subscriber.getMsisdn(), subscriber.getSpid(), Integer.parseInt(subscriber.getDataPricePlan(ctx)),
                        (int) this.getDataOptInID(), -1, -1, -1);
            }
            else
            {
                client.deleteOptIn(ctx, subscriber.getMsisdn(), subscriber.getSpid(), -1, -1, -1,
                        (int) this.getDataOptInID(), -1);
            }
            service.setProvisionActionState(true);
        }
        catch (ClientException ce)
        {
            service.setProvisionActionState(false);
            if (successfulUnprov.contains(ce.getResultCode()))
            {
                final String msg = String
                        .format("Received (rc=%d) for unprovisioning of Data OptIn Auxiliary Service (id=%d). Treating as a successful unprov.",
                                ce.getResultCode(), service.getAuxiliaryServiceIdentifier());
                new InfoLogMsg(this, msg, ce).log(ctx);
            }
            else
            {
                final String msg = String.format(
                        "Unable to unprovision Data OptIn Auxiliary Service (id=%d) due to: %s",
                        service.getAuxiliaryServiceIdentifier(), ce.getMessage());
                new MinorLogMsg(this, msg, ce).log(ctx);

                throw new ExtensionAssociationException(ExternalAppEnum.DATA_OPTIN, msg, ce.getResultCode(), ce, false);
            }
        }
        catch (Exception e)
        {
            service.setProvisionActionState(false);
            final String msg = String.format("Unable to unprovision Data OptIn Auxiliary Service (id=%d) due to: %s",
                    service.getAuxiliaryServiceIdentifier(), e.getMessage());
            new MinorLogMsg(this, msg, e).log(ctx);

            throw new ExtensionAssociationException(ExternalAppEnum.DATA_OPTIN, msg, ExternalAppSupport.REMOTE_EXCEPTION);
        }
    }
    


    private Subscriber getSubscriber(Context ctx, SubscriberAuxiliaryService service) throws ExtensionAssociationException
    {
        Subscriber subscriber = (Subscriber)ctx.get(Subscriber.class);
        if (subscriber == null)
        {
            try
            {
                subscriber = SubscriberSupport.getSubscriber(ctx, service.getSubscriberIdentifier());
            }
            catch (HomeException e)
            {
                String msg = "Unable to retrieve subscription "
                        + service.getSubscriberIdentifier() + ": " + e.getMessage();
                new MinorLogMsg(this, msg, e).log(ctx);
                throw new ExtensionAssociationException(ExternalAppEnum.BSS, msg, ExternalAppSupport.BSS_DATABASE_FAILURE_SUBSCRIPTION_RETRIEVAL);
            }
        }
        return subscriber;
    }

    public AppNGRCClient getClient(Context ctx)
    {
        return (AppNGRCClient)ctx.get(AppNGRCClient.class);
    }
    
    private final static Set<Short> successfulUnprov = new HashSet<Short>();
    static 
    {
        successfulUnprov.add((short) 24); // Subscriber is not provisioned with requested rate plan.
    }
}
