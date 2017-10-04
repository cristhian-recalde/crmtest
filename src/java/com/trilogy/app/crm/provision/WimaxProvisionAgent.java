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
package com.trilogy.app.crm.provision;

import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;


/**
 * This Provisioning Agent provisions the Subscriber and its services to the 
 * Aptilo AAA Server. There are no HLR commands associated with WiMAX service.
 * 
 * 
 * @author anuradha.malvadkar@redknee.com
 *
 */
public class WimaxProvisionAgent extends GenericProvisionAgent
{


    /**
     * {@inheritDoc}
     *
     * The WiMAX service uses the Aptilo AAA for provisioning
     *
     */
    public void execute(final Context ctx)
        throws AgentException
    {  
    	new GenericProvisionAgent().execute(ctx);
    }
	
	
}
