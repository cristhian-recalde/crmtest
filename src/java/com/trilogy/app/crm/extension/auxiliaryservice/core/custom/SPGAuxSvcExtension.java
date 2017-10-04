package com.trilogy.app.crm.extension.auxiliaryservice.core.custom;

import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberAuxiliaryService;
import com.trilogy.app.crm.extension.AssociableExtension;
import com.trilogy.app.crm.extension.ExtensionAssociationException;
import com.trilogy.app.crm.provision.CommonProvisionAgentBase;
import com.trilogy.app.crm.provision.gateway.ServiceProvisioningGatewaySupport;
import com.trilogy.app.crm.provision.service.param.ProvisionEntityType;
import com.trilogy.app.crm.support.SubscriberSupport;
import com.trilogy.framework.xhome.context.Context;

public class SPGAuxSvcExtension extends
com.redknee.app.crm.extension.auxiliaryservice.core.SPGAuxSvcExtension implements
AssociableExtension<SubscriberAuxiliaryService>
{

    @Override
    public void associate(Context ctx, SubscriberAuxiliaryService associatedBean) throws ExtensionAssociationException
    {

        try
        {
            Context subCtx = ctx.createSubContext(); 
            subCtx.put(CommonProvisionAgentBase.SPG_PROVISIONING_CUSTOM_PROVISION_ENTITY_TYPE, 
                    String.valueOf(ProvisionEntityType.PROVISION_ENTITY_TYPE_AUX_SERVICE));

        //ServiceProvisioningGatewaySupport.serviceToRemove(ctx, Long.valueOf(this.getAuxiliaryService(ctx).getIdentifier()), this.getAuxiliaryService(ctx));
        Subscriber subscriber = SubscriberSupport.getSubscriberForAuxiliaryService(ctx,associatedBean); 
        int resultCode = ServiceProvisioningGatewaySupport.prepareAndSendIndividualServiceToSPG(subCtx, subscriber, 
                associatedBean.getAuxiliaryService(ctx),
                this.getSPGServiceType(), true, this);
        if (resultCode!= 0 )
        {
            throw new Exception("fail to provision the auxiliary service for subscriber"); 
        } 
        associatedBean.setProvisionActionState(true);
        
        } catch (Exception e)
        {
            associatedBean.setProvisionActionState(false);
        }
         
    }

    @Override
    public void updateAssociation(Context ctx, SubscriberAuxiliaryService associatedBean)
            throws ExtensionAssociationException
    {
    }

    @Override
    public void dissociate(Context ctx, SubscriberAuxiliaryService associatedBean) throws ExtensionAssociationException
    {
        
        try
        {
            Context subCtx = ctx.createSubContext(); 
            subCtx.put(CommonProvisionAgentBase.SPG_PROVISIONING_CUSTOM_PROVISION_ENTITY_TYPE, 
                    String.valueOf(ProvisionEntityType.PROVISION_ENTITY_TYPE_AUX_SERVICE));
 
        //ServiceProvisioningGatewaySupport.serviceToRemove(ctx, Long.valueOf(this.getAuxiliaryService(ctx).getIdentifier()), this.getAuxiliaryService(ctx));
        Subscriber subscriber = SubscriberSupport.getSubscriberForAuxiliaryService(ctx,associatedBean); 
        int resultCode = ServiceProvisioningGatewaySupport.prepareAndSendIndividualServiceToSPG(subCtx, subscriber, 
                associatedBean.getAuxiliaryService(ctx),
                this.getSPGServiceType(), false, this);
        if (resultCode!= 0 )
        {
            throw new Exception("fail to unprovision the auxiliary service for subscriber"); 
        }
        associatedBean.setProvisionActionState(true);
        
        } catch (Exception e)
        {
            throw new ExtensionAssociationException(null, "unprovision the auxiliary service for subscriber", 1);
        }
        
    }


}
