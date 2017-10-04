package com.trilogy.app.crm.bulkloader;

import java.io.PrintWriter;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgent;
import com.trilogy.framework.xhome.home.Home;

public class AccountCreateAgent extends AccountCreateVisitor implements
		ContextAgent {

	public AccountCreateAgent(Context ctx, Home home, Account template,
			PrintWriter writer, PrintWriter errWriter) 
	{
		super(ctx, home, template, writer, errWriter);
	}

	@Override
	public void execute(Context ctx) throws AgentException 
	{
		// This needs to be fixed. It won't make a difference, but looks like, you know!
		Object obj = ctx.get(BulkLoadSubscriber.class);
		visit(ctx, obj);
	}

}
