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

import com.trilogy.framework.xhome.beans.ExceptionListener;
import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.beans.Validator;
import com.trilogy.framework.xhome.beans.xi.PropertyInfo;
import com.trilogy.framework.xhome.context.Context;

/**
 * @author paul.sperneac@redknee.com
 */
public abstract class AbstractSubscriberValidator implements Validator
{
    public boolean isNull(final String val)
    {
        return val == null || val.trim().equals("");
    }

    public void required(final ExceptionListener el, final String val, final PropertyInfo property)
    {
        if (isNull(val))
        {
            el.thrown(new IllegalPropertyArgumentException(property, "Value required."));
        }
    }

    public abstract void validate(Context ctx, Object obj) throws IllegalStateException;
}
