/*
 * Copyright (c) 1999-2008 REDKNEE.com. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of
 * REDKNEE.com. ("Confidential Information"). You shall not disclose such
 * Confidential Information and shall use it only in accordance with the
 * terms of the license agreement you entered into with REDKNEE.com.
 *
 * REDKNEE.COM MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE
 * SUITABILITY OF THE SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR
 * A PARTICULAR PURPOSE, OR NON-INFRINGEMENT. REDKNEE.COM SHALL NOT BE
 * LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING,
 * MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS DERIVATIVES.
 */
package com.trilogy.app.crm.install;

import java.io.PrintWriter;
import java.util.Collection;

import com.trilogy.framework.xhome.beans.xi.PropertyInfo;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.ContainsIC;
import com.trilogy.framework.xhome.elang.Or;
import com.trilogy.framework.xhome.filter.Predicate;
import com.trilogy.framework.xhome.home.Home;

import com.trilogy.app.crm.support.WebControlSupportHelper;
import com.trilogy.service.corba.CorbaClientProperty;
import com.trilogy.service.corba.CorbaClientPropertyHome;
import com.trilogy.service.corba.CorbaClientPropertyTableWebControl;
import com.trilogy.service.corba.CorbaClientPropertyXInfo;

/**
 * Support class used to update CORBA Client configurations using Application Dependency Agents
 * @author ali
 *
 */
public class CorbaClientInstallConfigSupport 
{

	private CorbaClientInstallConfigSupport() {}

	public static void execute(Context context, String clientKey, String hostname)
	throws AgentException
	{
		String[] keys = {clientKey};
		execute(context, keys, hostname);
	}
	
	public static void execute(Context context, String[] clientKeys, String hostname)
	throws AgentException
	{
		try
		{
			Home corbaHome = (Home)context.get(CorbaClientPropertyHome.class);

			Predicate predicate = null;
			if (clientKeys.length > 1)
			{
				predicate = new Or();
				for(int i=0; i < clientKeys.length; i++)
				{
					((Or) predicate).add(new ContainsIC(CorbaClientPropertyXInfo.KEY, clientKeys[i]));        	
				}
			}
			else
			{
				predicate = new ContainsIC(CorbaClientPropertyXInfo.KEY, clientKeys[0]);
			}

			Collection<CorbaClientProperty> configurations = 
				corbaHome.where(context, predicate).selectAll();

			for (CorbaClientProperty p : configurations)
			{
				p.setNameServiceHost(hostname);
				corbaHome.store(context,p);
			}

			// DISPLAY_MODE for Table Web Control
			context.put("MODE", 0);
			// Disable Table Actions
			context.put("ACTIONS", false);

			PrintWriter out = (PrintWriter) context.get(PrintWriter.class);

			// Only print important fields, hide all others.
			CorbaClientPropertyTableWebControl webcontrol = new CorbaClientPropertyTableWebControl();
			PropertyInfo[] properties = {CorbaClientPropertyXInfo.NAME_SERVICE_CONTEXT_NAME, 
					CorbaClientPropertyXInfo.NAME_SERVICE_PORT, 
					CorbaClientPropertyXInfo.NAME_SERVICE_NAME, 
					CorbaClientPropertyXInfo.SUPPORT_INS, 
					CorbaClientPropertyXInfo.KEEP_ALIVE_INTERVAL, 
					CorbaClientPropertyXInfo.CONNECTION_TIMEOUT, 
					CorbaClientPropertyXInfo.VERSION};
			WebControlSupportHelper.get(context).hideProperties(context, properties);
			webcontrol.toWeb(context, out, "Updated Corba Configurations", configurations);
		}
		catch (Throwable t)
		{
			t.printStackTrace();
			throw new AgentException(t);
		}
	}
}
