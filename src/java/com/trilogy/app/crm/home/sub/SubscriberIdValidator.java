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

import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeOperationEnum;

import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberXInfo;
import com.trilogy.app.crm.support.SpidSupport;
import com.trilogy.app.crm.support.SubscriberSupport;


/**
 * Validates that the subscriber ID is provided if Service Provider configuration
 * AllowToSpecifySubscriberId is true. This check is applied only during create() call.
 *
 * @author victor.stratan@redknee.com
 */
public final class SubscriberIdValidator extends AbstractSubscriberValidator
{

    /**
     * Singleton instance.
     */
    private static SubscriberIdValidator instance;


    /**
     * Prevents initialization
     */
    private SubscriberIdValidator()
    {
        // empty
    }


    /**
     * Returns an instance of <code>SubscriberIdValidator</code>.
     *
     * @return An instance of <code>SubscriberIdValidator</code>.
     */
    public static SubscriberIdValidator instance()
    {
        if (instance == null)
        {
            instance = new SubscriberIdValidator();
        }

        return instance;
    }


    /**
     * {@inheritDoc}
     */
    public void validate(final Context ctx, final Object obj) throws IllegalStateException
    {
        final Object operation = ctx.get(HomeOperationEnum.class);
        if (!HomeOperationEnum.CREATE.equals(operation))
        {
            // validate ID only during create. ID does not change.
            return;
        }

        // TODO 2007-09-18 create exception only if needed
        final CompoundIllegalStateException el = new CompoundIllegalStateException();
        final Subscriber sub = (Subscriber) obj;

        try
        {
            final CRMSpid sp = SpidSupport.getCRMSpid(ctx, sub.getSpid());

            /*
             * TT 7091400011: check is extracted to support class.
             */
            if (!SubscriberSupport.isAutoCreateSubscriberId(sp, sub))
            {
                required(el, sub.getId(), SubscriberXInfo.ID);
            }
        }
        catch (final HomeException e)
        {
            el.thrown(new IllegalStateException("Cannot obtain subscriber Service Provider " + sub.getSpid(), e));
        }
        el.throwAll();
    }

}
