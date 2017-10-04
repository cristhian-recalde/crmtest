/* 
 * This code is a protected work and subject to domestic and international
 * copyright law(s). A complete listing of authors of this work is readily 
 * available. Additionally, source code is, by its very nature, confidential 
 * information and inextricably contains trade secrets and other information 
 * proprietary, valuable and sensitive to Redknee, no unauthorised use, 
 * disclosure, manipulation or otherwise is permitted, and may only be used 
 * in accordance with the terms of the licence agreement entered into with 
 * Redknee Inc. and/or its subsidiaries. 
 * 
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.state;

import com.trilogy.app.crm.bean.Service;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;

/**
 * Executes an update job when the subscriber state change at service level
 * @author arturo.medina@redknee.com
 * 
 * Ported from CRM 7_4, part of the Subscriber State Service Update provisioning module.
 * @since 8.2
 */
public interface ServiceStateUpdateAgent
{
    /**
     * Updates an external service state.  
     * @param ctx
     * @param subscriber 
     * @param service
     * @throws HomeException
     */
    public void update(Context ctx, Subscriber subscriber, Service service) throws HomeException;
}
