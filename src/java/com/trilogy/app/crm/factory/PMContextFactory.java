/*
 * Created on Apr 14, 2005
 *
 * This code is a protected work and subject to domestic and international
 * copyright law(s).  A complete listing of authors of this work is readily
 * available.  Additionally, source code is, by its very nature, confidential
 * information and inextricably contains trade secrets and other information
 * proprietary, valuable and sensitive to Redknee.  No unauthorized use,
 * disclosure, manipulation or otherwise is permitted, and may only be used
 * in accordance with the terms of the license agreement entered into with
 * Redknee Inc. and/or its subsidiaries.
 *
 * Copyright � Redknee Inc. and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.factory;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextFactory;
import com.trilogy.framework.xhome.context.ContextFactoryProxy;
import com.trilogy.framework.xlog.log.PMLogMsg;

/**
 * This decorator times a ContextAgent.
 * 
 * @author psperneac
 */
public class PMContextFactory
   extends ContextFactoryProxy
{
	protected String name;
	protected String module;

   /** @deprecated use other Constructor instead. **/
	public PMContextFactory(ContextFactory delegate,String module,String name)
	{
      this(module, name, delegate);
	}

	
	public PMContextFactory(String module, String name, ContextFactory delegate)
	{
		super(delegate);
		
		setName(name);
      setModule(module);
	}

   
	/**
	 * @see com.redknee.framework.xhome.context.ContextFactoryProxy#create(com.redknee.framework.xhome.context.Context)
	 */
	public Object create(Context ctx)
	{
		PMLogMsg msg = new PMLogMsg(getModule(), getName());
		
		try
		{
			return super.create(ctx);
		}
		finally
		{
			msg.log(ctx);
		}
	}



	/**
	 * @return Returns the name.
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * @param name The name to set.
	 */
	public void setName(String name)
	{
		this.name = name;
	}



	/**
	 * @return Returns the module.
	 */
	public String getModule()
	{
		return module;
	}



	/**
	 * @param module The module to set.
	 */
	public void setModule(String module)
	{
		this.module = module;
	}

	
}
