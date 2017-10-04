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
package com.trilogy.app.crm.unit_test;

import com.trilogy.framework.license.LicenseMgr;
import com.trilogy.framework.xhome.context.Context;

/**
 * @author victor.stratan@redknee.com
*/
public class TestFakeLicenseMgr implements LicenseMgr
{

    /**
     * @Override
     */
    public boolean attemptRate(Context arg0, String arg1)
    {
        return true;
    }

    /**
     * @Override
     */
    public void incrQuantity(Context arg0, String arg1, long arg2)
    {

    }

    /**
     * @Override
     */
    public boolean isLicensed(Context arg0, String arg1)
    {
        return true;
    }

    /**
     * @Override
     */
    public long quantityLimit(Context arg0, String arg1)
    {
        return 1000;
    }

    /**
     * @Override
     */
    public void reportQuantity(Context arg0, String arg1, long arg2)
    {
    }

    /**
     * @Override
     */
    public double reportRate(Context arg0, String arg1, long arg2)
    {
        return 1000;
    }
}
