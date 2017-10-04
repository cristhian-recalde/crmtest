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

package com.trilogy.app.crm.bas.recharge;

import java.util.Comparator;
import java.io.Serializable;

import com.trilogy.app.crm.bean.core.BundleFee;


/**
 * Comparator used to sort {@link BundleFee}.
 *
 * @author cindy.wong@redknee.com
 */
public class BundleFeeComparator implements Comparator<BundleFee>, Serializable
{

    /**
     * {@inheritDoc}
     */
    public int compare(final BundleFee f1, final BundleFee f2)
    {
        final int result;
        if (f1.getSource().equals(f2.getSource()))
        {
            result = 0;
        }

        else if (f1.getSource().startsWith("Package"))
        {
            result = -1;
        }
        else if (f2.getSource().startsWith("Package"))
        {
            result = 1;
        }

        else if ("Auxiliary".equals(f1.getSource()))
        {
            result = 1;
        }
        else if ("Auxiliary".equals(f2.getSource()))
        {
            result = -1;
        }
        else
        {
            result = f1.getSource().compareTo(f2.getSource());
        }
        return result;
    }
}