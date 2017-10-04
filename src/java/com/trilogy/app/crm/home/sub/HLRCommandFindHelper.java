package com.trilogy.app.crm.home.sub;

/**
 * @author lzou
 * @date   Nov 20, 2003
 *
 * Helper class to look for correct HLR provisioning commands based on 
 * Command ID and subscriber's service provider ID.
 */

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAwareSupport;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;

import com.trilogy.framework.xlog.log.*;

import com.trilogy.app.crm.bean.*;

public class HLRCommandFindHelper
      extends ContextAwareSupport
{
    public HLRCommandFindHelper ( Context ctx_ )  
    {
          setContext(ctx_);
    }
      
	/**
	 *   find HLR provision command by CommandId and Subscriber's Service Provider ID
     *   If no result found from above, then try to search by CommandId and global Service 
     *   Provider ID which is 9999.
	 */
	public ProvisionCommand findCommand(Context ctx,String key, Subscriber sub)
            throws HomeException
    {
          Home    cmdHome  = (Home) ctx.get(ProvisionCommandHome.class);
          if (cmdHome == null)
          {
              throw new HomeException("System Error: ProvisionCommandHome does not exist in context");
          }
          
          ProvisionCommand   cmd   = null;          
          ProvisionCommandID idObj = null;
          
          idObj = new ProvisionCommandID(key, sub.getSpid(), sub.getSubscriberType(), sub.getTechnology());
          
          try
          {
               cmd = (ProvisionCommand) cmdHome.find(ctx,idObj); 
          }
          catch(HomeException e)
          {
                if (LogSupport.isDebugEnabled(ctx))
                {
                     new DebugLogMsg(this, "No HLR provision command [ key=" + key + "] has been found for subscriber [ " + sub + " ]", e).log(ctx);
                }
                cmd = null;
          }
                    
          if ( cmd == null )  // then try to find global provision command
          {                
                idObj = new ProvisionCommandID(key, 9999, sub.getSubscriberType(), sub.getTechnology());  // global SPID
                
                try
                {
                     cmd = ( ProvisionCommand)cmdHome.find(ctx,idObj);
                }
                catch(HomeException e)
                {
                      if (LogSupport.isDebugEnabled(ctx))
                      {
                           new DebugLogMsg(this, "No Global HLR provision command [ key=" + key + "] has been found for subscriber [ " + sub + " ]", e).log(ctx);
                      } 
                }
          }

          return cmd;
    } 
    
}
