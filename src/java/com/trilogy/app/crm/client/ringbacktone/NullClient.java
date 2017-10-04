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
package com.trilogy.app.crm.client.ringbacktone;

import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.framework.xhome.context.Context;

/**
 * Not intended for use outside of the RBT package.
 * This client implementation does nothing (useful for stubbing out the client).
 * 
 * @author nick.landry@redknee.com
 */
class NullClient implements RBTClient
{
    public NullClient()
    {
        // Noop
    }

    
    public void createSubscriber(Context context, Subscriber subscriber) throws RBTClientException
    {
    }

    
    public void deleteSubscriber(Context context, String msisdn) throws RBTClientException
    {
    }

    
    public void updateSubscriberMSISDN(Context context, String oldMsisdn, String newMsisdn) throws RBTClientException
    {
    }

    
    public void updateSubscriberReactivate(Context context, String msisdn) throws RBTClientException
    {
    }

    
    public void updateSubscriberSuspend(Context context, String msisdn) throws RBTClientException
    {
    }
    
    public Long getSubscriberNotFoundErrorCode()
    {
        return null;
    }
}
