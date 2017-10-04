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

import com.trilogy.app.crm.bean.FFRmiServiceConfig;
import com.trilogy.app.crm.bean.FFRmiServiceConfigWebControl;
import com.trilogy.framework.application.RemoteApplication;
import com.trilogy.framework.core.platform.Ports;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgent;

/**
 * AppInstall Application Dependency Configuration Agent for ECP.  
 * Configures:
 *  Friends and Family RMI Configuration (FFRmiServiceConfig)
 *  
 * @author ali
 *
 */
public class EcpInstallConfigAgent implements ContextAgent 
{

    public void execute(Context context) throws AgentException 
    {
        RemoteApplication remote = (RemoteApplication)context.get(RemoteApplication.class);

        // Friends and Family RMI Client

        FFRmiServiceConfig config = (FFRmiServiceConfig)context.get(FFRmiServiceConfig.class);

        config.setHostname(remote.getHostname());
        config.setPort(remote.getBasePort() + Ports.RMI_OFFSET);

        // DISPLAY_MODE for Table Web Control
        context.put("MODE", 0);
        // Disable Table Actions
        context.put("ACTIONS", false);
        PrintWriter out = (PrintWriter) context.get(PrintWriter.class);

        FFRmiServiceConfigWebControl ffwebcontrol = new FFRmiServiceConfigWebControl();
        ffwebcontrol.toWeb(context, out, "Updated Friends and Family RMI Config", config);

        // Corba clients
        String[] keys = {"ECP"};
        // Change this to use parameters from Remote Application config
        CorbaClientInstallConfigSupport.execute(context, keys, remote.getHostname());
    }

}
