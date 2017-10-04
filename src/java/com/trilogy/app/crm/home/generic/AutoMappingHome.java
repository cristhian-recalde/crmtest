package com.trilogy.app.crm.home.generic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.home.HomeProxy;


/**
 * 1. it support automatic mapping to DB only
 * 2. not auto adapt, the field could be lazy loaded later
 * 3. do support automatic search unadapted fields.  
 * 4. the home should be used as close to xdbhome as possible, to reduce the chance racing condition. 
 * 
 * 
 * @author Larry Xia
 *
 */
public class AutoMappingHome 
extends HomeProxy
{
	   /**
	 * 
	 */
	private static final long serialVersionUID = 1L;


	public AutoMappingHome(Home delegate, Collection <OnewayMapper> mappers)
	   {
	      setDelegate(delegate);
	      mappers_ = mappers; 
	   }

	
	   public AutoMappingHome(Home delegate, OnewayMapper adapter)
	   {
	      setDelegate(delegate);
	      
	      mappers_ = new ArrayList(); 
	      mappers_.add(adapter); 
	   }

	   
	   public Object create(Context ctx, Object obj)
	      throws HomeException, HomeInternalException
	   {
		  
		   for ( Iterator <OnewayMapper> i = mappers_.iterator(); i.hasNext(); )
		   {   
			   obj = ((OnewayMapper)i.next()).create(ctx,obj); 
		   }
		  
	      return getDelegate().create(ctx, obj);
	   }

	   
	   public Object store(Context ctx, Object obj)
	      throws HomeException, HomeInternalException
	   {
		   for ( Iterator <OnewayMapper> i = mappers_.iterator(); i.hasNext(); )
		   {   
			   obj = ((OnewayMapper)i.next()).update(ctx,obj); 
		   }
	      return getDelegate().store(ctx, obj);
	   }

	   
	   public void remove(Context ctx, Object obj)
	      throws HomeException
	   {
		   for ( Iterator <OnewayMapper> i = mappers_.iterator(); i.hasNext(); )
		   {   
			   ((OnewayMapper)i.next()).delete(ctx,obj); 
		   }
	      getDelegate().remove(ctx, obj);  
	   }

	   
	   public void removeAll(Context ctx, Object where)
	      throws HomeException
	   {
		   // didn't find a good way to support this method for now.
		   // but delete objects one by one is too expensive, and 
		   // it can be implemented by using remove() method. otherwise.
		   // developer has to find his own short cut to remove a whole bunch of 
		   // objects. 
		   throw new HomeException("method not supported in " + this.getClass().getName()); 
	   }
	   
	   
	  final private Collection <OnewayMapper> mappers_; 
}
