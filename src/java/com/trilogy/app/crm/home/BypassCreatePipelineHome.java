package com.trilogy.app.crm.home;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;

public class BypassCreatePipelineHome extends ConditionalNullHome 
{
	
	

	public BypassCreatePipelineHome() {
		super();
		// TODO Auto-generated constructor stub
	}

	public BypassCreatePipelineHome(Context ctx, Home delegate) {
		super(ctx, delegate);
		// TODO Auto-generated constructor stub
	}

	public static final String BYPASS_SUBSCRIBER_PIPELINE = "BYPASS_SUBSCRIBER_PIPELINE";
	
	@Override
	protected boolean condition(Context ctx) 
	{
		return ctx.getBoolean(BYPASS_SUBSCRIBER_PIPELINE, false);
	}

}
