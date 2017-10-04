package com.trilogy.app.crm.extension.auxiliaryservice.core.custom;

import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberAuxiliaryService;
import com.trilogy.app.crm.bean.externalapp.ExternalAppEnum;
import com.trilogy.app.crm.client.bm.BalanceManagementSupport;
import com.trilogy.app.crm.client.bm.SubscriberProfileProvisionClient;
import com.trilogy.app.crm.client.bm.SubscriberProfileProvisionException;
import com.trilogy.app.crm.extension.AssociableExtension;
import com.trilogy.app.crm.extension.ExtensionAssociationException;
import com.trilogy.app.crm.subscriber.provision.SubscriberProvisionResultCode;
import com.trilogy.app.crm.support.ExternalAppSupport;
import com.trilogy.app.crm.support.SubscriberSupport;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.MinorLogMsg;

public class PromOptOutAuxSvcExtension extends
        com.redknee.app.crm.extension.auxiliaryservice.core.PromOptOutAuxSvcExtension 
        implements AssociableExtension<SubscriberAuxiliaryService>
{

    @Override
    public void associate(Context ctx, SubscriberAuxiliaryService subAuxSvc) throws ExtensionAssociationException
    {
        provisionPromotion(ctx, subAuxSvc, true);

    }

    @Override
    public void updateAssociation(Context ctx, SubscriberAuxiliaryService subAuxSvc) throws ExtensionAssociationException
    {
    }

    @Override
    public void dissociate(Context ctx, SubscriberAuxiliaryService subAuxSvc) throws ExtensionAssociationException
    {
        provisionPromotion(ctx, subAuxSvc, false);
    }
    
    public void provisionPromotion(final Context ctx, final SubscriberAuxiliaryService subAuxSvc,
            final boolean status) throws ExtensionAssociationException
    {
        final String subscriberId = subAuxSvc.getSubscriberIdentifier();
        final Subscriber subscriber;
        try
        {
            subscriber = SubscriberSupport.getSubscriberForAuxiliaryService(ctx, subAuxSvc);
        }
        catch (HomeException e)
        {
            subAuxSvc.setProvisionActionState(false);
            throw new ExtensionAssociationException(ExternalAppEnum.URCS, "Unable to provision URCS Promotion for auxiliary service " 
                    + getAuxiliaryServiceId()
                    + " to subscriber " + subscriberId + ": " + e.getMessage(), ExternalAppSupport.REMOTE_EXCEPTION, e, false);
        }

       
        try
        {
            final SubscriberProfileProvisionClient client;
            client = BalanceManagementSupport.getSubscriberProfileProvisionClient(ctx);
            client.updatePromOptOut(ctx, subscriber, status);
            subAuxSvc.setProvisionActionState(true);
        }
        catch (final SubscriberProfileProvisionException exception)
        {
            final short resultBM = exception.getErrorCode();
            SubscriberProvisionResultCode.setProvisionUpsErrorCode(ctx, resultBM);
            final String err = "Failed to update promotional sms opt out status for subscription " + subscriber.getId() + "  due to error on URCS (" + resultBM + ")";
            new MinorLogMsg(this, err, exception).log(ctx);
            subAuxSvc.setProvisionActionState(false);
            new ExtensionAssociationException(ExternalAppEnum.URCS, "Unable to provision URCS Promotion for auxiliary service " 
                    + getAuxiliaryServiceId()
                    + " to subscriber " + subscriberId + ": " + exception.getMessage(), ExternalAppSupport.REMOTE_EXCEPTION, exception, false);
            
        }
    }

}