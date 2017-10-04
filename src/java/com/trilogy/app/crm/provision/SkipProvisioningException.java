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

/**
 * Exception thrown for legitimately skipping provisioning (or unprovisioning) 
 * action.
 * 
 * Example Use Case:
 *   Have to skip service activation when Subscriber is in Available or Pending State.  
 *   (Only provisioning to "active" subscribers.  Once a subscriber has been activated
 *   once it is provisioned on the RIM Provisioning System). 
 * 
 * @author angie.li
 *
 */
public class SkipProvisioningException extends AgentException 
{
    public SkipProvisioningException(Object src, String subId, String serviceId, String skippingReason)
    {
        super("Skipping provisioning of CRM Service " + serviceId + " for subscriber " + subId + " due to " + skippingReason);
        source_ = src.getClass().getName();
    }
    
    public String getSource()
    {
    	return source_;
    }
    
    final String source_;
}
