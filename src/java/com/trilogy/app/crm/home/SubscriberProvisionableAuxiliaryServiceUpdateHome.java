package com.trilogy.app.crm.home;

import com.trilogy.app.crm.bean.Msisdn;
import com.trilogy.app.crm.bean.ProvisionCommand;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberAuxiliaryService;
import com.trilogy.app.crm.bean.core.custom.AuxiliaryService;
import com.trilogy.app.crm.hlr.HlrSupport;
import com.trilogy.app.crm.home.sub.HLRConstants;
import com.trilogy.app.crm.provision.ProvisionAgentException;
import com.trilogy.app.crm.support.Lookup;
import com.trilogy.app.crm.support.SubscriberSupport;
import com.trilogy.framework.xhome.beans.SafetyUtil;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xlog.log.MinorLogMsg;

/**
 * 
 * @author odeshpande
 *
 */
public class SubscriberProvisionableAuxiliaryServiceUpdateHome extends SubscriberProvisionableAuxiliaryServiceHome
{
	/**
     * Serial version UID.
     */
    private static final long serialVersionUID = 1L;
    
    /**
     * Creates a new SubscriberProvisionableAuxiliaryServiceUpdateHome proxy.
     *
     * @param ctx
     *            The operating context.
     * @param delegate
     *            The Home to which we delegate.
     */
    public SubscriberProvisionableAuxiliaryServiceUpdateHome(final Context ctx, final Home delegate)
    {
        super(ctx, delegate);
    }
    
    @Override
    public Object store(final Context ctx, final Object obj) throws HomeException
    {       

        final SubscriberAuxiliaryService association = (SubscriberAuxiliaryService) obj;

        final AuxiliaryService auxService = association.getAuxiliaryService(ctx);        
        
        if (isEnabledMultiSIM(ctx, auxService))
        {
        	SubscriberAuxiliaryService oldAssociation = (SubscriberAuxiliaryService) ctx.get(Lookup.OLD_SUBSCRIBER_AUXILIARY_SERVICE);
        	final Subscriber subscriber = SubscriberSupport.getSubscriberForAuxiliaryService(ctx, association);
            
            final boolean isActive = shouldProvision(ctx, association, subscriber, auxService);
                        
            final boolean isImsiSwapped = !SafetyUtil.safeEquals(association.getMultiSimImsi(), oldAssociation.getMultiSimImsi());
            if(isActive && isImsiSwapped)
            {
            	swapMultiSimSlaveImsi(ctx,association,oldAssociation,subscriber,auxService);
            }
            	
        }
        final Object storedObject = super.store(ctx, association);
        return storedObject;
    }
    
    private void swapMultiSimSlaveImsi(Context ctx,SubscriberAuxiliaryService association,SubscriberAuxiliaryService oldAssociation,
        Subscriber subscriber,AuxiliaryService auxService) throws HomeException
    {
    	String hlrCommand = HLRConstants.PRV_CMD_TYPE_MULTISIM_IMSI_SWAP;
    	ProvisionCommand command = HlrSupport.findCommand(ctx, subscriber, hlrCommand);
    	if(command != null)
    	{
    		Context slaveImsiSwapContext = ctx.createSubContext();
    		//Reterived below key in SPG Key calculator
    		slaveImsiSwapContext.put(OLD_SLAVE_SIM_IMSI, oldAssociation.getMultiSimImsi());
    		
    		slaveImsiSwapContext.put(AuxiliaryService.class, auxService);
    		/*
    		slaveImsiSwapContext.put(SubscriberAuxiliaryService.class, association);
    		slaveImsiSwapContext.put(Subscriber.class, subscriber);
    		*/
    		slaveImsiSwapContext.put(ProvisionCommand.class, command);
    		try{
    			if(HlrSupport.updateHlr(slaveImsiSwapContext, subscriber, command))
    			{
    				association.setProvisioned(true);
    			}else{
    				String msg = "Provisioning update to Provisioning Gateway failed [SubID=" + subscriber.getId() + ",AuxSvcId=" + auxService.getIdentifier() + "].";
                    new MinorLogMsg(this, msg, null).log(ctx);
                    throw new HomeException(msg);
    			}
    		}catch(ProvisionAgentException e)
    		{
    			new MinorLogMsg(this, "Error swapping slave imsi " + association.getMultiSimImsi()
                        + " for subscriber " + association.getSubscriberIdentifier()
                        + " for Multi-SIM auxiliary service " + association.getAuxiliaryServiceIdentifier(), e).log(ctx);
    			throw new HomeException("Error swapping slave Imsi");
    		}
    	}
    
    }
    
    private static final String OLD_SLAVE_SIM_IMSI="OLD_SLAVE_SIM_IMSI";

}
