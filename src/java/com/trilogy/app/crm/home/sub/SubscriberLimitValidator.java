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

package com.trilogy.app.crm.home.sub;

import com.trilogy.framework.xhome.beans.Validator;
import com.trilogy.framework.xhome.context.Context;

import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.extension.account.SubscriberLimitExtension;
import com.trilogy.app.crm.support.SubscriberLimitSupport;


/**
 * Validates the subscriber limit of an account if {@link SubscriberLimitExtension} is
 * installed. Intended to be run against subs during subscriber creation.
 *
 * @author cindy.wong@redknee.com
 * @since 2008-09-08
 */
public class SubscriberLimitValidator implements Validator
{

    /**
     * Create a new instance of <code>SubscriberLimitValidator</code>.
     */
    protected SubscriberLimitValidator()
    {
        // empty
    }


    /**
     * Returns an instance of <code>SubscriberLimitValidator</code>.
     *
     * @return An instance of <code>SubscriberLimitValidator</code>.
     */
    public static SubscriberLimitValidator instance()
    {
        if (instance == null)
        {
            instance = new SubscriberLimitValidator();
        }
        return instance;
    }

    /**
     * Singleton instance.
     */
    private static SubscriberLimitValidator instance;


    /**
     * {@inheritDoc}
     */
    public void validate(final Context context, final Object obj) throws IllegalStateException
    {
        final Subscriber subscriber = (Subscriber) obj;

        if (subscriber == null)
        {
            return;
        }

        final String ban = subscriber.getBAN();
        SubscriberLimitSupport.validateAddSubscriberToAccount(context, ban);

    }
}
