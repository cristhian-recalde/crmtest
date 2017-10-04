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
package com.trilogy.app.crm.bundle;

import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.beans.Validator;
import com.trilogy.framework.xhome.context.Context;

import com.trilogy.app.crm.bean.CRMGroup;
import com.trilogy.app.crm.bean.CRMGroupXInfo;

public class BundleMgrLimitValidator implements Validator
{
    public void validate(final Context ctx, final Object obj)
        throws IllegalStateException
    {
        final CompoundIllegalStateException el = new CompoundIllegalStateException();
        final CRMGroup group = (CRMGroup) obj;

        if (group.getBundleMgrMax() < group.getBundleMgrMin())
        {
            el.thrown(new IllegalPropertyArgumentException(
                    CRMGroupXInfo.BUNDLE_MGR_MAX,
                    "Bundle Manager Upper Limit must be greater than or equal to Bundle Manager Lower Limit."));
        }

        el.throwAll();
    }
}

