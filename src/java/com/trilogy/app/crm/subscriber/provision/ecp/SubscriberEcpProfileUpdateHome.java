package com.trilogy.app.crm.subscriber.provision.ecp;

import com.trilogy.app.crm.Common;
import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.CRMSpidHome;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.client.AppEcpClient;
import com.trilogy.app.crm.client.ProvisioningHomeException;
import com.trilogy.app.crm.client.exception.ECPReturnCodeMsgMapping;
import com.trilogy.app.crm.support.ExternalAppSupport;
import com.trilogy.app.crm.support.Lookup;
import com.trilogy.app.osa.ecp.provision.SubsProfile;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;

public class SubscriberEcpProfileUpdateHome extends
        SubscriberVoiceServiceParameterUpdateHome {
    /**
     * @param ctx
     * @param delegate
     */
    public SubscriberEcpProfileUpdateHome(Context ctx, Home delegate) 
    {
        super(ctx, delegate);
    }

    protected String getOperationMsg() {
        return "Subscription Profile";
    }

    protected void updateServiceParameter(Context ctx, Subscriber oldSub,
            Subscriber newSub) throws HomeException, ProvisioningHomeException {
        final AppEcpClient client = (AppEcpClient)ctx.get(AppEcpClient.class);

        int result = 201;
        SubsProfile ecpSub = null;
        try
        {
    		// UMP-3348: Allow Data service for broadband and voice for wireline subscriptions
            // hack fix to support Multiplay capability, as legacy interfaces did not consider subscription type other than AIRTIME
            // msisdn|subscriptionType will be passed to URCS
    		
    		String paramGetSub = null;
    		if(allowMultiSubPerAccount(ctx, newSub))
    		{
    			paramGetSub = newSub.getMsisdn()+"|"+newSub.getSubscriptionType();
    		}
    		else
    		{
    			paramGetSub = newSub.getMsisdn();
    		}
    		
        	
            ecpSub = client.getSubsProfile(paramGetSub);
        }
        catch (IllegalStateException exception)
        {
            result = ExternalAppSupport.NO_CONNECTION;
        }

          
        if ( ecpSub != null )
        {
            boolean hasChanged = false;
            
            if ( ctx.has(Lookup.ECPPROFILE_GROUPMSISDN))
            {
                final String groupAccount = (String)ctx.get(Lookup.ECPPROFILE_GROUPMSISDN);    

                if ( ! groupAccount.equals(ecpSub.groupAccount))
                {
                    ecpSub.groupAccount = groupAccount;
                    hasChanged = true;
                    if (LogSupport.isDebugEnabled(ctx))
                    {
                        new DebugLogMsg(this, "ECP Update: Update Group msisdn "+ecpSub.groupAccount+" for sub "+newSub.getId(), null).log(
                            ctx);
                        
                    }
                }
            }
        
            if ( ctx.has(Lookup.ECPPROFILE_NEWCOS))
            {
                final int classOfService = ctx.getInt(Lookup.ECPPROFILE_NEWCOS);
                if ( classOfService != ecpSub.classOfService )
                {
                    hasChanged = true;
                    ecpSub.classOfService = classOfService;
                    if (LogSupport.isDebugEnabled(ctx))
                    {
                        new DebugLogMsg(this, "ECP Update: Update class of service "+ecpSub.classOfService+" for sub "+newSub.getId(), null).log(
                            ctx);
                
                    }
                }
            }
        
            /* As of CRM 8.2, Rate Plan information is in Price Plan and it is ignored when pushed to the subscriber 
             * through this interface.  so we simply stop updating the value.
             */
            
            if ( ctx.has(Lookup.ECPPROFILE_NEWSTATE))
            {
                final int state =  ctx.getInt(Lookup.ECPPROFILE_NEWSTATE);
                if ( state != ecpSub.state )
                {
                    ecpSub.state = state;
                    hasChanged = true;
                    if (LogSupport.isDebugEnabled(ctx))
                    {
                        new DebugLogMsg(this, "ECP Update: Update state "+ecpSub.state+" for sub "+newSub.getId(), null).log(
                        ctx);
                    }
                }
            }
            
            if ( ctx.has(Lookup.ECPPROFILE_PACKAGE))
            {
                final String imsi = (String)ctx.get(Lookup.ECPPROFILE_PACKAGE);
                if ( ! imsi.equals(ecpSub.imsi))
                {
                    ecpSub.imsi = imsi;
                    hasChanged = true;      
                    if ( LogSupport.isDebugEnabled(ctx))
                    {
                        new DebugLogMsg(this, "ECP Update: Update imsi "+ecpSub.imsi+" for sub "+newSub.getId(), null).log(
                            ctx);
                        
                    }
                }
            }
            if ( hasChanged )
            {
                result = client.updateSubscriber(ecpSub);
            }
            else
            {
                result = 0;
                new DebugLogMsg(this, " NO ecp update is required, since none of the data changed.",null).log(ctx);
            }
        }
        
        if ( result != 0 )
        {
            throw new ProvisioningHomeException(
                    "Failed to update URCS Voice: " + ECPReturnCodeMsgMapping.getMessage(result),
                    result, Common.OM_ECP_ERROR);
        }
        
        
    }
    
	
	/**
	 * Multiplay capability
	 * @param context
	 * @param subscriberAccount
	 * @return
	 * @throws HomeException
	 */
	private boolean allowMultiSubPerAccount(final Context context, final Subscriber subscriberAccount)
    throws HomeException
	{
	    final int spid = subscriberAccount.getSpid();
	
	    final Home home = (Home)context.get(CRMSpidHome.class);
	    try
	    {
	    	final CRMSpid serviceProvider = (CRMSpid)home.find(context, Integer.valueOf(spid));
	    	if (serviceProvider == null)
		    {
		        throw new HomeException(
		            "Failed to locate service provider profile " + spid + " for account " + subscriberAccount.getBAN());
		    }
	    	return serviceProvider.isAllowMultiSubForOneAccount();
	    }
	    catch(HomeException he)
	    {
	    	throw new HomeException(
		            "Exception while looking for spid " + spid + " for account " + subscriberAccount.getBAN() +" "+ he.getMessage());
	    }
	}
    

    /**
     * Checks if the 2 subscribers have the parameters that are checked for this home equal.
     * this version checks if earlier homes have put their marker in the context. If they did, this home
     * will gather all the changes and apply them in one try. 
     *  
     * @see com.redknee.app.crm.subscriber.provision.SubscriberServiceParameterUpdateHome#parameterEquals(com.redknee.framework.xhome.context.Context, com.redknee.app.crm.bean.Subscriber, com.redknee.app.crm.bean.Subscriber)
     */
    protected boolean parameterEquals(Context ctx, Subscriber oldSub,Subscriber newSub) 
        throws HomeException 
    {
        if(ctx.has(Lookup.ECPPROFILE_GROUPMSISDN) || ctx.has(Lookup.ECPPROFILE_NEWCOS)
               || ctx.has(Lookup.ECPPROFILE_NEWSTATE) 
               || ctx.has(Lookup.ECPPROFILE_PACKAGE)) 
        {
            return false;
        }
                
        return true;
    }

}
