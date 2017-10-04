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

import com.trilogy.framework.application.RemoteApplication;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgent;


/**
 * AppInstall Application Dependency Configuration Agent for 
 * Invoice Server's BasServerService and 
 * BAS Node Configuration.
 * @author ali
 *
 */
public class BasNodeInstallConfigAgent implements ContextAgent 
{

    public void execute(Context context) throws AgentException 
    {
        try
          {
             RemoteApplication remote = (RemoteApplication)context.get(RemoteApplication.class);

             RemoteApplication config = (RemoteApplication)context.get("AppCrmBas");
             
             config.setName(remote.getName());
             config.setVersion(remote.getVersion());
             config.setHostname(remote.getHostname());
             config.setBasePort(remote.getBasePort());
          }
          catch (Throwable t)
          {
             t.printStackTrace();
             throw new AgentException(t);
          }
    }

}
