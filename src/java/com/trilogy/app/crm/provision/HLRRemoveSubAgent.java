package com.trilogy.app.crm.provision;

import com.trilogy.app.crm.bean.ProvisionCommand;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.hlr.CrmHlrServicePipelineImpl;
import com.trilogy.app.crm.hlr.HlrSupport;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;


/**
 * @author lzou
 * @date   Nov 21, 2003
 *
 * ContextAgent to remove specific subscriber from HLR 
 */
public class HLRRemoveSubAgent 
{
	/* (non-Javadoc)
	 * @see com.redknee.framework.xhome.context.ContextAgent#execute(com.redknee.framework.xhome.context.Context)
	 */
	public void execute(Context ctx) 
            throws AgentException
	{
		 Subscriber subscriber;
         ProvisionCommand provCmd;
          
         subscriber = (Subscriber)ctx.get(Subscriber.class);
         
         if ( subscriber == null )
         {
               throw new AgentException("System Error: No Subscriber to Delete from HLR");
         }
                           
         // check ProvisionCommand 
         provCmd = (ProvisionCommand)ctx.get(ProvisionCommand.class);
         
         if ( provCmd == null )  // || provCmd.getHlrCmd().length())
         {
             throw new AgentException("Configuration error: No ProvisionCommand found for deleting Subscriber: " + subscriber);
         }
         
         if ( provCmd.getHlrCmd().length() == 0)
         {
               // no HLR commands means nothing to do
         }
         else
         {

	 		try {
				// execute HLR commands using HLR client
	 			//String request = CommonProvisionAgentBase.replaceHLRCommand(ctx,  provCmd.getHlrCmd(), subscriber, null, null);
                HlrSupport.updateHlr(ctx, subscriber, provCmd); 

			}
			catch (Exception e)
			{
				throw new AgentException(e);
			}
         }
	}
}
