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
package com.trilogy.app.crm.subscriber.subscription.history;

import com.trilogy.app.crm.bean.service.SubscriberSubscriptionHistory;
import com.trilogy.framework.xhome.beans.Validator;
import com.trilogy.framework.xhome.context.Context;

public class SubscriberSubscriptionHistoryValidator implements Validator 
{

    public void validate(Context context, Object obj)
            throws IllegalStateException 
    {
        SubscriberSubscriptionHistory record = (SubscriberSubscriptionHistory) obj; 
        if (record.getItemIdentifier() == 0L)
        {
            throw new IllegalStateException("Failed to identify the item identifier.");
        }

    }

}
