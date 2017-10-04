/*
* This code is a protected work and subject to domestic and international
* copyright law(s). A complete listing of authors of this work is readily
* available. Additionally, source code is, by its very nature, confidential
* information and inextricably contains trade secrets and other information
* proprietary, valuable and sensitive to Redknee, no unauthorised use,
* disclosure, manipulation or otherwise is permitted, and may only be used
* in accordance with the terms of the licence agreement entered into with
* Redknee Inc. and/or its subsidiaries.
*
* Copyright � Redknee Inc. and its subsidiaries. All Rights Reserved.
*/
package com.trilogy.app.crm.home;

import com.trilogy.app.crm.util.SimpleLocks;
import com.trilogy.framework.xhome.beans.XBeans;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xlog.log.LogSupport;

/**
 * Provides synchronized control of home store operations 
 *  
 * @author ltang
 */
public class SynchronizedKeyHome extends HomeProxy
{
    /**
     * Constructs a SynchronizedKeyHome
     * @param delegate
     */
    public SynchronizedKeyHome(Home delegate, String lockerKey)
    {
        super(delegate);
        this.lockerKey = lockerKey; 
        
    }
    
    /**
     * Synchronize store operations on obj by first acquiring lock before executing updates
     * and then releasing lock after completing updates
     * @param ctx
     * @param obj
     */
    public Object store(Context ctx, Object obj)
        throws HomeException
    {        
    	LogSupport.debug(ctx, this, "SubscriberPipeline[store].....");
    	if ( lockerKey != null )
    	{	
    		SimpleLocks locker = (SimpleLocks) ctx.get(lockerKey);
    		Object key = XBeans.getIdentifier(obj);
   	
    		if(locker != null)
    		{
    			locker.lock(key);
    		}
    	
         
    		Object ret = obj;
    		try
    		{
    			ret = getDelegate().store(ctx, obj);            
    		}
    		finally
    		{
    			if (locker != null)
    			{	
    				locker.unlock(key);
    			}	
    		}
    		
    		return ret;
    	
    	}  else {
    	
    		return getDelegate().store(ctx, obj); 
    	
    	}
    }
  
    String lockerKey; 
}
