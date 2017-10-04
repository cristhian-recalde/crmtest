package com.trilogy.app.crm.bulkloader;

import java.io.PrintWriter;

import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgent;
import com.trilogy.framework.xhome.home.Home;

public class SubscriberCreateAgent extends SubscriberCreateVisitor implements
		ContextAgent {

	public SubscriberCreateAgent(Context ctx, Home home, Subscriber template,
			PrintWriter writer, PrintWriter errWriter) 
	{
		super(ctx, home, template, writer, errWriter);
		
	}

	@Override
	public void execute(Context ctx) throws AgentException 
	{
		
		Object obj = ctx.get(BulkLoadSubscriber.class);
		
		visit(ctx, obj);
	}

}
