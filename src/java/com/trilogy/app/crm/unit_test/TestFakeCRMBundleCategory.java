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

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.In;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;

import com.trilogy.app.crm.bundle.BundleCategory;
import com.trilogy.app.crm.bundle.BundleCategoryTransientHome;
import com.trilogy.app.crm.bundle.BundleCategoryXInfo;
import com.trilogy.app.crm.bundle.UnitTypeEnum;
import com.trilogy.app.crm.bundle.exception.BundleManagerException;
import com.trilogy.app.crm.bundle.exception.CategoryAlreadyExistException;
import com.trilogy.app.crm.bundle.exception.CategoryNotExistException;
import com.trilogy.app.crm.xhome.home.TransientFieldResettingHome;

/**
 * @author victor.stratan@redknee.com
 */
public class TestFakeCRMBundleCategory implements com.redknee.app.crm.bundle.service.CRMBundleCategory
{
    final Home home;

    public TestFakeCRMBundleCategory(final Context ctx)
    {
        super();
        home = new TransientFieldResettingHome(ctx, new BundleCategoryTransientHome(ctx));
    }

    public Home getCategoriesByUnitType(final Context ctx, final int type) throws BundleManagerException
    {
        final EQ condition = new EQ(BundleCategoryXInfo.UNIT_TYPE, UnitTypeEnum.get((short) type));
        return home.where(ctx, condition);
    }

    public Home getCategoriesByUnitTypeRange(final Context ctx, final Collection type) throws BundleManagerException
    {
        final Set set;
        if (type instanceof Set)
        {
            set = (Set) type;
        }
        else
        {
            set = new HashSet(type.size());
            set.addAll(type);
        }

        final In condition = new In(BundleCategoryXInfo.UNIT_TYPE, set);
        return home.where(ctx, condition);
    }

    public void createCategory(final Context ctx, final BundleCategory category) throws CategoryAlreadyExistException, BundleManagerException
    {
        try
        {
            home.create(ctx, category);
        }
        catch (HomeException e)
        {
            throw new BundleManagerException("Exception during createCategory()", e);
        }
    }

    public void updateCategory(final Context ctx, final BundleCategory category) throws CategoryNotExistException, BundleManagerException
    {
        try
        {
            home.store(ctx, category);
        }
        catch (HomeException e)
        {
            throw new BundleManagerException("Exception during updateCategory()", e);
        }
    }

    public void removeCategory(final Context ctx, final long categoryId) throws CategoryNotExistException, BundleManagerException
    {
        try
        {
            home.remove(ctx, Integer.valueOf((int) categoryId));
        }
        catch (HomeException e)
        {
            throw new BundleManagerException("Exception during removeCategory()", e);
        }
    }

    public BundleCategory getCategory(final Context ctx, final long categoryId) throws CategoryNotExistException, BundleManagerException
    {
        try
        {
            return (BundleCategory) home.find(ctx, Integer.valueOf((int) categoryId));
        }
        catch (HomeException e)
        {
            throw new BundleManagerException("Exception during getCategory()", e);
        }
    }
}
