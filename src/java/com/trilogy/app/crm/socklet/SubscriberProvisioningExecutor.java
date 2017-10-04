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
 * Copyright &copy; Redknee Inc. and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.socklet;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAwareSupport;

/**
 * @author amedina
 *
 * Thread that executes the provisioning command
 */
public class SubscriberProvisioningExecutor extends ContextAwareSupport implements Runnable
{

	public SubscriberProvisioningExecutor(Context ctx, SubscriberProvisioningCommand command, String arguments)
	{
		setContext(ctx);
		setCommand(command);
		setArguments(arguments);
	}
	
	/**
	 * @param arguments
	 */
	private void setArguments(String arguments) 
	{
		arguments_ = arguments;
		
	}

	/**
	 * @param command
	 */
	public void setCommand(SubscriberProvisioningCommand command) 
	{
		command_ = command;
	}

	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	public void run() 
	{

		command_.start(getContext(), arguments_);

	}

	private SubscriberProvisioningCommand command_;
	private String arguments_;
}
