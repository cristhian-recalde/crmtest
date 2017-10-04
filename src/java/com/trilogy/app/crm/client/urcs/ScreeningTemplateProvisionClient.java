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
package com.trilogy.app.crm.client.urcs;

import com.trilogy.app.crm.client.RemoteServiceException;
import com.trilogy.framework.xhome.context.Context;

/**
 * CRM view of URCS Loyalty Provision interface.
 * 
 * @author asim.mahmood@redknee.com
 * @since 9.1
 */
public interface ScreeningTemplateProvisionClient
{
    String version ();
    
    /**
     * Creates a group screening on URCS. The subscriber account must already exist on URCS.
     * 
     * @param ctx
     * @param profile This consist of all the relevant information required for registration of loyalty profile.
     * @return  copy of loyalty profile
     * @throws RemoteServiceException
     */
    com.redknee.product.bundle.manager.provision.v5_0.screeningTemplate.ScreeningTemplateReturnParam updateScreeningTemplate(Context ctx, int actionIdentifier, com.redknee.product.bundle.manager.provision.v5_0.screeningTemplate.ScreeningTemplate screeningTemplate, com.redknee.product.bundle.manager.provision.v5_0.screeningTemplate.ServiceLevelUsage[] serviceLevelUsageArray) throws RemoteServiceException;

}
