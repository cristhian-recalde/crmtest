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
 * Copyright � Redknee Inc. and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.home.sub;

import com.trilogy.framework.xhome.beans.Validator;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeValidator;


/**
 * Since the subscriber limit check only has to be done on a create, this HomeValidator will return the
 * appropriate create and store validators for use by the ValidatingHome.
 *
 * @author Aaron Gourley
 * @since 7.4.16
 */
public class SubscriberLimitHomeValidator implements HomeValidator
{
    private static HomeValidator instance_ = null;
    public static HomeValidator instance()
    {
        if( instance_ == null )
        {
            instance_ = new SubscriberLimitHomeValidator();
        }
        return instance_;
    }

    private static final Validator NULL_VALIDATOR = new Validator()
    {
        public void validate(final Context ctx, final Object obj) throws IllegalStateException
        {
            // do nothing
        }
    };

    /**
     * {@inheritDoc}
     */
    public Validator getCreateValidator()
    {
        return SubscriberLimitValidator.instance();
    }

    /**
     * {@inheritDoc}
     */
    public Validator getStoreValidator()
    {
        return NULL_VALIDATOR;
    }
}
