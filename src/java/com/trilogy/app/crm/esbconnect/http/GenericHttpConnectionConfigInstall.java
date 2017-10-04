/*
 * This code is a protected work and subject to domestic and international
 * copyright law(s).  A complete listing of authors of this work is readily
 * available.  Additionally, source code is, by its very nature, confidential
 * information and inextricably contains trade secrets and other information
 * proprietary, valuable and sensitive to Redknee.  No unauthorized use,
 * disclosure, manipulation or otherwise is permitted, and may only be used
 * in accordance with the terms of the license agreement entered into with
 * Redknee Inc. and/or its subsidiaries.
 *
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.esbconnect.http;

import java.io.IOException;
import java.rmi.RemoteException;


import com.trilogy.framework.xhome.context.Context;
import com.trilogy.app.crm.bean.GenericHTTPConnectionConfig;
import com.trilogy.app.crm.support.SystemStatusSupportHelper;
import com.trilogy.app.crm.web.service.SystemStatusRequestServicer;
import com.trilogy.framework.core.platform.CoreSupport;
import com.trilogy.framework.xlog.log.InfoLogMsg;



/**
 * Service Genric Http Connection Config Install logic.
 * 
 * 
 * @author anuradha.malvadkar@redknee.com @9.7.2
 *
 */
public class GenericHttpConnectionConfigInstall 
{

    /**
     * Install the ESB htt Config in CRM.
     * The client serving the requests is controlled by Licensing.
     * @param ctx
     * @throws IOException 
     * @throws HomeException 
     * @throws RemoteException 
     */
    public static void execute(Context ctx) 
    {
    	
    	
    	CoreSupport.bindBean(ctx, GenericHttpConnectionConstants.PRBT_HTTP_CONFIG_KEY, GenericHTTPConnectionConfig.class);
        
        CoreSupport.bindBean(ctx, GenericHttpConnectionConstants.PROTEI_VOICEMAIL_HTTP_CONFIG_KEY, GenericHTTPConnectionConfig.class);
        


        CoreSupport.bindBean(ctx, GenericHttpConnectionConstants.ESB_HTTP_CONFIG_KEY, GenericHTTPConnectionConfig.class);

       

        new InfoLogMsg(GenericHttpConnectionConfigInstall.class, "Generic Http Connection config installed in CRM context.", null).log(ctx);
     }

}
