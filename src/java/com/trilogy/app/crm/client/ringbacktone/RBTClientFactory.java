package com.trilogy.app.crm.client.ringbacktone;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xlog.log.InfoLogMsg;
import com.trilogy.framework.xlog.log.MajorLogMsg;
import com.sun.tools.jxc.gen.config.Config;

public class RBTClientFactory
{
	private RBTClientFactory()
	{
		// Noop
	}
	
	public static void installClient(Context context)
	{
		new InfoLogMsg(RBTClientFactory.class.getName(), "Installing Ring Back Tone Client.", null).log(context);
		
		try
		{
		    Collection<PRBTConfiguration> c = HomeSupportHelper.get(context).getBeans(context, PRBTConfiguration.class); 
		    
		    for (PRBTConfiguration config :c )
		    {    
		    
		        RBTClient clientChain = null;
		        
		        if (config.getDriver() instanceof UCPrbtInfo)
		        {           
		           clientChain = new HTTPPostClient(context, config.getSubscriberNotFoundErrorCode()!=Long.MIN_VALUE?config.getSubscriberNotFoundErrorCode():null);
		        } else 
		        {
		            clientChain = new ProteiPRBTClient(config.getSubscriberNotFoundErrorCode()!=Long.MIN_VALUE?config.getSubscriberNotFoundErrorCode():null);
		        }
		        
		        clientChain = new SwitchProxy(clientChain);
		        clientChain = new DebugLogProxy(clientChain);
		        clientChain = new PMProxy(clientChain);
		        clientChain = new OMProxy(clientChain);
		        clients.put(Long.valueOf(config.getId()), clientChain);    
		            
		    }        
		
			
			
			new InfoLogMsg(RBTClientFactory.class.getName(), "Finished installing Ring Back Tone Client.", null).log(context);
		}
		catch (Throwable t)
		{
			// A little paranoid, but better to be safe
			new MajorLogMsg(RBTClientFactory.class.getName(),
					"Exception thrown while installing Ring Back Tone client.",
					t)
				.log(context);
		}
	}
	
	public static RBTClient locateClient(long id)
	{
		return clients.get(Long.valueOf(id));
	}
	
	
	
	static private Map<Long, RBTClient> clients = new HashMap<Long, RBTClient>(); 
	public static Class<RBTClient> RBT_CLIENT_CONTEXT_KEY = RBTClient.class; 
}
