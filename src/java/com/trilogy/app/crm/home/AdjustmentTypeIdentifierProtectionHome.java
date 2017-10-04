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
package com.trilogy.app.crm.home;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;

import com.trilogy.app.crm.bean.core.AdjustmentType;
import com.trilogy.app.crm.bean.AdjustmentTypeEnum;


/**
 * Provides a way to prevent the creation of new AdjustmentTypes in a specific
 * set of ranges.  This home is intended to be used to decorate the actual
 * AdjustmentType home when accessed via the UI.
 *
 * @author gary.anderson@redknee.com
 */
public
class AdjustmentTypeIdentifierProtectionHome
    extends HomeProxy
{
    /**
     * Creates a new AdjustmentTypeIdentifierProtectionHome
     */
    public AdjustmentTypeIdentifierProtectionHome(
        final Context context,
        final Home delegate)
    {
        super(delegate);
        setContext(context);
    }

    // INHERIT
    public Object create(Context ctx,final Object bean)
        throws HomeException
    {
        final AdjustmentType type = (AdjustmentType)bean;

        // The test throws HomeException.
        try
        {
            testValidity(type);
            return super.create(ctx,type);
        }
        catch (final ProtectedIdentifierException exception)
        {
            // NOTE - 2004-08-06 - Since the exception is thrown and caught in
            // the class, I choose not to use exception chaining to simplify the
            // message that appear on-screen.
            throw new HomeException(exception.getMessage());
        }
    }


    /**
     * Tests the validity of the given AdjustmentType, and throws an exception
     * if the test fails.  Validity is dependent on the identifier used -- it
     * must not fall into the protected ranges.
     *
     * @param type The AdjustmentType to validate.
     *
     * @exception ProtectedIdentifierException Thrown if the AdjustementType
     * identifier is in a protected range.
     */
    private void testValidity(final AdjustmentType type)
        throws ProtectedIdentifierException
    {
        final int identifier = type.getCode();

        // TODO - 2004-08-05 - We should make use of the MessageMgr for these
        // exception messages.
        if (type.isInCategory(getContext(), AdjustmentTypeEnum.RecurringCharges))
        {
            throw new ProtectedIdentifierException(
                "The identifer " + identifier + " is reserved for Recurring Charges.");
        }

        if (type.isInCategory(getContext(), AdjustmentTypeEnum.AuxiliaryService))
        {
            throw new ProtectedIdentifierException(
                "The identifer " + identifier + " is reserved for Auxiliary Services.");
        }

        // A subset of OCC charges.
        if (20100 <= identifier && identifier <= 20199)
        {
            throw new ProtectedIdentifierException(
                "The identifier range 20100 to 20199 is reserved for predefined Other Charges.");
        }
    }
} // class
