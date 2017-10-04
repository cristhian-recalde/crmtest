package com.trilogy.app.crm.home;

import java.util.Iterator;

import com.trilogy.framework.xhome.elang.True;
import com.trilogy.framework.xhome.filter.Predicate;
import com.trilogy.framework.xhome.beans.TimestampBean;
import com.trilogy.framework.xhome.beans.XBeans;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.visitor.AbortVisitException;
import com.trilogy.framework.xhome.visitor.FindVisitor;
import com.trilogy.framework.xhome.visitor.Visitor;
import com.trilogy.framework.xhome.home.*;
import com.trilogy.framework.xlog.log.PMLogMsg;

/**
 * 
 * An LRUCachingHome which supports find/select cmd based on a predicate.
 * 
 * @author Ameya Bhurke
 *
 */
public class PredicateAwareLRUCachingHome extends LRUCachingHome 
{

	private Object poisonPill = null;

	public PredicateAwareLRUCachingHome(Context ctx, Class clazz, boolean reallySelectAll, Home home) 
	{
		super(ctx, clazz, reallySelectAll, home);
		try
		{
			poisonPill = clazz.newInstance();
		}
		catch(Exception e)
		{
			
		}
	} 
	
	public PredicateAwareLRUCachingHome() {
		super();
		
	}

	public PredicateAwareLRUCachingHome(Context ctx, Object cacheConfigKey,
			boolean reallySelectAll, Home source) {
		super(ctx, cacheConfigKey, reallySelectAll, source);
		
	}

	public PredicateAwareLRUCachingHome(Context ctx, Object cacheConfigKey,
			boolean reallySelectAll, TimestampBean purger, Home source) {
		super(ctx, cacheConfigKey, reallySelectAll, purger, source);
		
	}

	public PredicateAwareLRUCachingHome(Context ctx, Object cacheConfigKey,
			int capacity, boolean reallySelectAll, TimestampBean purger,
			Home source) {
		super(ctx, cacheConfigKey, capacity, reallySelectAll, purger, source);
		
	}

	@Override
	public Visitor forEach(Context ctx, Visitor visitor, Object where)
	throws HomeException
	{
		// TransientHome does not support a XStatement only 'where'.
		// In this scenario 'where' would produce the default True Predicate
		// and of course not be an explicit True Predicate itself.
		
		Predicate p = (Predicate) XBeans.getInstanceOf(ctx, where, Predicate.class);
		if (p != null &&
		    p != where &&
		    p == True.instance())
		{
		    // The where contains something other than the default Predicate
		    throw new UnsupportedWhereHomeException("forEach [" + where + "]: non-Predicate where unsupported");
		}
		
		for (Iterator i = cache_.values().iterator(); i.hasNext(); )
		{
		    try
		    {
		        Object bean = i.next();
		        
		        if(bean == poisonPill)
		        {
		        	continue;
		        }
		
		        if (p == null || p.f(ctx, bean))
		        {
		            visitor.visit(ctx, bean);
		        }
		    }
		    catch (AbortVisitException e)
		    {
		        break;
		    }
		    catch (AgentException e)
		    {
		        // This is so that we preserve the type of the original HomeException
		        if (e.getCause() != null && e.getCause() instanceof HomeException)
		        {
		            throw (HomeException) e.getCause();
		        }
		
		        throw new HomeException(e);
		    }
		}
		
		return visitor;
	}
	
	@Override
	public Object find(Context ctx, Object key)
	        throws HomeException
	{
	
	        Object obj = null;
	        boolean isKey = isKey(ctx, key);
	        PMLogMsg pm = new PMLogMsg(getCacheConfigKey(), "LRUCache-Hit", key.toString());
	
	        try
	        {
	            if (isKey)
	            {
	                obj = cache_.get(key);
	
	                if (obj != null)
	                {
	                    return (obj == poisonPill) ?
	                        null :
	                        clone(ctx, obj);
	                }
	            }
	            else
	            {
	
	                Predicate predicate = (Predicate) XBeans.getInstanceOf(ctx, key, Predicate.class);
	
	                if (predicate != null)
	                {
	                      Object value = ((FindVisitor) forEach(ctx, new FindVisitor(ctx, predicate))).getValue();
	                      if(value != null)
	                      {
	                           return value;
	                      }
	                }
	
	            }
	            pm.setMeasurementName("LRUCache-Miss");
	
	            obj = getDelegate().find(ctx, key);
	
	            // put an if condition to check if this obj is null or not.
		            cache_.put(
		                (isKey) ?
		                    key :
		                    XBeans.getIdentifier(obj), 
		                    obj == null ? poisonPill : obj);
	            
	            return obj;
	        }
	        finally
	        {
	            pm.log(ctx);
	        }
	}

}