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
 * Copyright � Redknee Inc. and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.home.sub;

import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.beans.Validator;
import com.trilogy.framework.xhome.context.Context;

import com.trilogy.app.crm.bean.Subscriber;

public final class FirstLastNameValidator implements Validator
{
    private static final FirstLastNameValidator INSTANCE = new FirstLastNameValidator();

    private FirstLastNameValidator()
    {
    }

    public static FirstLastNameValidator instance()
    {
        return INSTANCE;
    }

    public void validate(final Context ctx, final Object obj) throws IllegalStateException
    {
        if (obj == null)
        {
            return;
        }

        final Subscriber sub = (Subscriber) obj;

        if (sub.isPostpaid() && !sub.isAccountIndividual())
        {
            final CompoundIllegalStateException ce = new CompoundIllegalStateException();

        // TODO 2008-08-21 name no longer part of subscriber
        // class to be removed
//            if (sub.getFirstName() == null || sub.getFirstName().trim().length() == 0)
//            {
                // TODO change to IllegalPropertyArgumentException
//                ce.thrown(new IllegalStateException("FirstName cannot be null in non-individual postpaid subscribers"));
//            }

//            if (sub.getLastName() == null || sub.getLastName().trim().length() == 0)
//            {
                // TODO change to IllegalPropertyArgumentException
//                ce.thrown(new IllegalStateException("LastName cannot be null in non-individual postpaid subscribers"));
//            }

            if (ce.getSize() > 0)
            {
                ce.throwAll();
            }
        }
    }

}
