package com.trilogy.app.crm.extension.auxiliaryservice.core.custom;

import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberAuxiliaryService;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.bean.core.custom.AuxiliaryService;
import com.trilogy.app.crm.bean.externalapp.ExternalAppEnum;
import com.trilogy.app.crm.client.ringbacktone.RBTClient;
import com.trilogy.app.crm.client.ringbacktone.RBTClientException;
import com.trilogy.app.crm.client.ringbacktone.RBTClientFactory;
import com.trilogy.app.crm.extension.AssociableExtension;
import com.trilogy.app.crm.extension.ExtensionAssociationException;
import com.trilogy.app.crm.support.ExternalAppSupport;
import com.trilogy.app.crm.support.Lookup;
import com.trilogy.app.crm.support.SubscriberSupport;
import com.trilogy.framework.xhome.beans.SafetyUtil;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.MinorLogMsg;

public class PRBTAuxSvcExtension extends com.redknee.app.crm.extension.auxiliaryservice.core.PRBTAuxSvcExtension
        implements AssociableExtension<SubscriberAuxiliaryService>
{
    @Override
    public void associate(Context ctx, SubscriberAuxiliaryService subAuxSvc) throws ExtensionAssociationException
    {
        final Subscriber subscriber;
        try
        {
            subscriber = SubscriberSupport.getSubscriberForAuxiliaryService(ctx, subAuxSvc);
        }
        catch (HomeException e)
        {
            subAuxSvc.setProvisionActionState(false);
            String msg ="Unable to retrieve subscription "
                    + subAuxSvc.getSubscriberIdentifier() + ": " + e.getMessage();
            new MinorLogMsg(this, msg, e).log(ctx);
            throw new ExtensionAssociationException(ExternalAppEnum.BSS, msg, ExternalAppSupport.BSS_DATABASE_FAILURE_SUBSCRIPTION_RETRIEVAL);
        }

        long rbtId = this.getRbtId();

        RBTClient client = RBTClientFactory.locateClient(rbtId);
        
        if (client == null)
        {
            subAuxSvc.setProvisionActionState(false); 
            throw new ExtensionAssociationException(ExternalAppEnum.RBT, "RBTClient is not available.", ExternalAppSupport.NO_CONNECTION);
        }
        
        Context subCtx   = ctx.createSubContext();
        subCtx.put(AuxiliaryService.class, this.getAuxiliaryService(ctx));
        
        try
        {
            client.createSubscriber(subCtx, subscriber);
            subAuxSvc.setProvisionActionState(true);
        }
        catch (RBTClientException e)
        {
            subAuxSvc.setProvisionActionState(true);
            throw new ExtensionAssociationException(ExternalAppEnum.RBT, "Unable to create subscriber on RBTClient: " + e.getMessage(), e.getResultCode(), e);
        }
    }

    @Override
    public void updateAssociation(Context ctx, SubscriberAuxiliaryService associatedBean)
            throws ExtensionAssociationException
    {
        final Subscriber newSub;
        final Subscriber oldSub = (Subscriber) ctx.get(Lookup.OLDSUBSCRIBER);
        try
        {
            newSub = SubscriberSupport.getSubscriberForAuxiliaryService(ctx, associatedBean);
        }
        catch (HomeException e)
        {
            associatedBean.setProvisionActionState(false);
            throw new ExtensionAssociationException(ExternalAppEnum.BSS, "Unable to retrieve subscription "
                    + associatedBean.getSubscriberIdentifier() + ": " + e.getMessage(), ExternalAppSupport.BSS_DATABASE_FAILURE_SUBSCRIPTION_RETRIEVAL, e);
        }

        if (!SafetyUtil.safeEquals(oldSub.getMSISDN(), newSub.getMSISDN()))
        {        
            long rbtId = this.getRbtId();
    
            RBTClient client = RBTClientFactory.locateClient(rbtId);
            
            if (client == null)
            {
                associatedBean.setProvisionActionState(false);
                throw new ExtensionAssociationException(ExternalAppEnum.RBT, "RBTClient is not available.", ExternalAppSupport.NO_CONNECTION);
            }
            
            Context subCtx   = ctx.createSubContext();
            subCtx.put(AuxiliaryService.class, this.getAuxiliaryService(ctx));
        
            try
            {
                client.updateSubscriberMSISDN(subCtx, oldSub.getMsisdn(), newSub.getMsisdn());         
            }
            catch (RBTClientException e)
            {
                associatedBean.setProvisionActionState(false);
                throw new ExtensionAssociationException(ExternalAppEnum.RBT, "Unable to update subscription's MSISDN on RBTClient: " + e.getMessage(), "Unable to update subscriptions mobile number on Ring Back Tone Server", e.getResultCode(), e);
            }
        }
    }

    @Override
    public void dissociate(Context ctx, SubscriberAuxiliaryService subAuxSvc) throws ExtensionAssociationException
    {
        final Subscriber subscriber;
        try
        {
            subscriber = SubscriberSupport.getSubscriberForAuxiliaryService(ctx, subAuxSvc);
        }
        catch (HomeException e)
        {
            subAuxSvc.setProvisionActionState(false);
            String msg = "Unable to retrieve subscription "
                    + subAuxSvc.getSubscriberIdentifier() + ": " + e.getMessage();
            new MinorLogMsg(this, msg, e).log(ctx);
            throw new ExtensionAssociationException(ExternalAppEnum.BSS, msg, ExternalAppSupport.BSS_DATABASE_FAILURE_SUBSCRIPTION_RETRIEVAL, e);
        }

        long rbtId = this.getRbtId();

        RBTClient client = RBTClientFactory.locateClient(rbtId);
        
        if (client == null)
        {
            subAuxSvc.setProvisionActionState(false);
            throw new ExtensionAssociationException(ExternalAppEnum.RBT, "RBTClient is not available.", ExternalAppSupport.NO_CONNECTION);
        }
        
        Context subCtx   = ctx.createSubContext();
        subCtx.put(AuxiliaryService.class, this.getAuxiliaryService(ctx));
        
        try
        {
            client.deleteSubscriber(subCtx, subscriber.getMSISDN());
            subAuxSvc.setProvisionActionState(true);
        }
        catch (RBTClientException e)
        {
            if (client.getSubscriberNotFoundErrorCode()==null || client.getSubscriberNotFoundErrorCode().longValue() != e.getResultCode())
            {
                subAuxSvc.setProvisionActionState(false);
                throw new ExtensionAssociationException(ExternalAppEnum.RBT, "Unable to delete subscriber from RBTClient: " + e.getMessage(), e.getResultCode(), e, false);
            }
        }
    }

    public int[] deactivationStates =
        {SubscriberStateEnum.SUSPENDED_INDEX, SubscriberStateEnum.LOCKED_INDEX};
    public int[] activationStates =
        {SubscriberStateEnum.ACTIVE_INDEX, SubscriberStateEnum.PROMISE_TO_PAY_INDEX};
    private static final long serialVersionUID = 1L;
}
