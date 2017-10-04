/*
 * Created on Jan 27, 2004
 *
 * Copyright (c) 1999-2003 REDKNEE.com. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of
 * REDKNEE.com. ("Confidential Information"). You shall not disclose such
 * Confidential Information and shall use it only in accordance with the
 * terms of the license agreement you entered into with REDKNEE.com.
 *
 * REDKNEE.COM MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE
 * SUITABILITY OF THE SOFTWARE, EITHCDR EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE IMPLIED WARRANTIES OF MCDRCHANTABILITY, FITNESS FOR
 * A PARTICULAR PURPOSE, OR NON-INFRINGEMENT. REDKNEE.COM SHALL NOT BE
 * LIABLE FOR ANY DAMAGES SUFFCDRED BY LICENSEE AS A RESULT OF USING,
 * MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS DERIVATIVES.  */
package com.trilogy.app.crm.provision.corba;

import java.util.Date;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.trilogy.app.crm.provision.SupportingTestConstants;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAware;
import com.trilogy.framework.xlog.log.MinorLogMsg;
import com.trilogy.service.corba.CorbaClientProperty;

/**
 * @author psperneac
 */
public class TestECareServiceClient extends TestCase implements SupportingTestConstants, ContextAware
{
	protected Context context;

	/**
	 * @param ctx
	 * @param name
	 */
	public TestECareServiceClient(Context ctx, String name)
	{
	}

	public void testConnection()
	{
		CorbaClientProperty prop = new CorbaClientProperty();
		prop.setNameServiceHost(CORBA_NAMESERVICE_HOST);
		prop.setNameServicePort(CORBA_NAMESERVICE_PORT);
		prop.setNameServiceName("name");
		prop.setNameServiceContextName("Redknee/App/Crm/ecareServiceFactory");
		prop.setUsername("rkadm");
		prop.setPassword("rkadm");

		Context ctx = getContext().createSubContext();
		ctx.put(CorbaClientProperty.class, prop);

		ECareServiceClient client = new ECareServiceClient(ctx);

		assertTrue(client.isConnected());

		ECareService service = client.getService();
		try
		{
			service.acctAdjust("03", "1230045098", 0, -123, new Date().toString(), "nope");
		}
		catch (Throwable th)
		{
			new MinorLogMsg(this, th.getMessage(), th).log(ctx);

			assertTrue(false);
		}

	}

	/**
	 * @param ctx
	 * @return
	 */
	public static Test suite(Context ctx)
	{
		TestSuite suite = new TestSuite();

		suite.addTest(new TestECareServiceClient(ctx, "testConnection"));

		return suite;
	}

	/**
	 * @return Returns the context.
	 */
	public Context getContext()
	{
		return context;
	}

	/**
	 * @param context The context to set.
	 */
	public void setContext(Context context)
	{
		this.context = context;
	}
}
