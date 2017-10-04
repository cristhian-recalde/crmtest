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

import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.framework.xhome.context.Context;

/**
 * Executes any subscriber state change at the subscriber level, this interface
 * can call the state change at service level   
 * @author arturo.medina@redknee.com
 * 
 * Ported from CRM 7_4.
 * @since CRM 8.2
 * @author angie.li@redknee.com
 */
public interface SubscriberServiceStateAction
{

    /**
     * Executes the state change for a particular subscriber
     * @param ctx
     * @param subscriber
     * @throws StateChangeException
     */
    public void execute(Context ctx, Subscriber subscriber) throws StateChangeException;
}
