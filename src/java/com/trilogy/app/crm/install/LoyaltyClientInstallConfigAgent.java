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

/**
 * AppInstall Application Dependency Configuration Agent for AppLoyalty 
 * Configures:
 *  AppLoyalty corba clients
 *  
 * @author ali
 *
 */
import com.trilogy.framework.application.RemoteApplication;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgent;

public class LoyaltyClientInstallConfigAgent implements ContextAgent 
{

	public void execute(Context context) throws AgentException 
    {
		RemoteApplication remote = (RemoteApplication)context.get(RemoteApplication.class);

		// Corba clients
		CorbaClientInstallConfigSupport.execute(context, "Loyalty", remote.getHostname());
    }

}
