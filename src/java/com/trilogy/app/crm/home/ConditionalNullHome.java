package com.trilogy.app.crm.home;

import java.util.ArrayList;
import java.util.Collection;

import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.framework.xhome.beans.XBeans;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.AbstractHome;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xhome.home.NullHome;
import com.trilogy.framework.xhome.visitor.Visitor;


/**
 * 
 * Conditionally a Null Home. The boolean condition can be 
 * realized by implementing the method
 * 
 *  {@link ConditionalNullHome#condition(Context)}
 * 
 * @author Ameya Bhurke.
 *
 */
public abstract class ConditionalNullHome extends HomeProxy 
{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;


	public ConditionalNullHome() 
	{
	}

	public ConditionalNullHome(Context ctx, Home delegate) 
	{
		super(ctx, delegate);		
	}
	
	
	/**
	 * 
	 * Method to decide whether home will behave as a
	 * NullHome or a HomeProxy.
	 * 
	 * @param ctx
	 * @return
	 */
	protected abstract boolean condition(Context ctx);
	

	@Override
	public Object create(Context ctx, Object obj) throws HomeException,
			HomeInternalException 
	{
		if(condition(ctx))
		{
			return obj;
		}
		else
		{
			return super.create(ctx, obj);
		}
		
	}

	
	@Override
	public Object find(Context ctx, Object obj) throws HomeException,
			HomeInternalException 
	{
		if(condition(ctx))
		{
			Subscriber bean = XBeans.instantiate(Subscriber.class, ctx);
			
			if(obj instanceof String)
			{
				bean.setId((String)obj);
			}
			
			return bean;
		}
		else
		{
			return super.find(ctx, obj);
		}
	}

	@Override
	public Visitor forEach(Context arg0, Visitor arg1, Object arg2)
			throws HomeException, HomeInternalException 
	{
		// TODO Auto-generated method stub
		return super.forEach(arg0, arg1, arg2);
	}

	@Override
	public void remove(Context ctx, Object obj) throws HomeException,
			HomeInternalException, UnsupportedOperationException 
	{
		if(!condition(ctx))
		{
			super.remove(ctx, obj);
		}
	}

	@Override
	public void removeAll(Context ctx, Object obj) throws HomeException,
			HomeInternalException, UnsupportedOperationException 
	{
		if(!condition(ctx))
		{
			super.removeAll(ctx, obj);
		}
	}

	@Override
	public Collection select(Context ctx, Object obj) throws HomeException,
			HomeInternalException, UnsupportedOperationException 
	{
		if(condition(ctx))
		{
			return new ArrayList();
		}
		else
		{
			return super.select(ctx, obj);
		}
	}
	

	@Override
	public Object store(Context ctx, Object obj) throws HomeException,
			HomeInternalException 
	{
		if(condition(ctx))
		{
			return obj;
		}
		else
		{
			return super.store(ctx, obj);
		}
	}
	
	

}
