package com.trilogy.app.crm.home;

import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.beans.Validator;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeOperationEnum;
import com.trilogy.framework.xhome.home.HomeValidator;
import com.trilogy.framework.xhome.home.ValidatingHome;

public class BypassValidationHome extends ValidatingHome 
{
	public static final String FLAG = "SubscriberHome.BypassValidation";

	public BypassValidationHome(HomeValidator validator, Home delegate) 
	{
		super(validator, delegate);
	}

	public BypassValidationHome(Validator createValidator,
			Validator storeValidator, Home delegate) 
	{
		super(createValidator, storeValidator, delegate);
	}

	public BypassValidationHome(Validator validator, Home delegate) 
	{
		super(validator, delegate);
	}

	public BypassValidationHome(Home delegate, Validator validator) 
	{
		super(delegate, validator);
	}

	public BypassValidationHome(Home delegate) 
	{
		super(delegate);
	}

	@Override
	public Object create(Context ctx, Object obj) throws HomeException 
	{
		if(ctx.getBoolean(FLAG, false))
		{
			 return getDelegate(ctx).create(ctx, obj);
		}
		else
		{
			return super.create(ctx, obj); 
		}
	}
	
}
