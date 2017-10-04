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

import com.trilogy.app.crm.bean.CreditCategory;
import com.trilogy.app.crm.bean.IdentifierEnum;
import com.trilogy.app.crm.support.IdentifierSequenceSupportHelper;


/**
 * Sets the code of CreditCategory to a unique code pulled from the
 * pool of available codes.
 *
 * @author victor.stratan@redknee.com
 */
public class CreditCategoryCodeSettingHome
    extends HomeProxy
{
    /**
     * Creates a new CreditCategoryCodeSettingHome.
     *
     * @param ctx The operation context.
     * @param delegate The Home to which we delegate.
     *
     * @exception HomeException Thrown if there are problems accessing Home data
     * in the context used to set-up this home.
     */
    public CreditCategoryCodeSettingHome(
        final Context ctx,
        final Home delegate)
        throws HomeException
    {
        super(delegate);

        IdentifierSequenceSupportHelper.get(ctx).ensureSequenceExists(
            ctx,
            IdentifierEnum.CREDIT_CATEGORY_CODE,
            100,
            Integer.MAX_VALUE);
    }

    //INHERIT
    public Object create(final Context ctx, final Object obj)
        throws HomeException
    {
        final CreditCategory creditCategory = (CreditCategory)obj;

        // Throws HomeException.
        if (creditCategory.getCode() < 0)
        {
            final int code = (int)getNextIdentifier(ctx);

            creditCategory.setCode(code);
        }

        return super.create(ctx, creditCategory);
    }


    /**
     * Gets the next available identifier.
     *
     * @return The next available identifier.
     */
    private long getNextIdentifier(final Context ctx)
        throws HomeException
    {
        return IdentifierSequenceSupportHelper.get(ctx).getNextIdentifier(
            ctx,
            IdentifierEnum.CREDIT_CATEGORY_CODE,
            null);
    }
} // class
