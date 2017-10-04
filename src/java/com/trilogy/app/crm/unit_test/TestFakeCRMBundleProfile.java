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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;

import com.trilogy.app.crm.bean.core.BundleProfile;
import com.trilogy.app.crm.bundle.BundleProfileTransientHome;
import com.trilogy.app.crm.bundle.BundleSegmentEnum;
import com.trilogy.app.crm.bundle.GroupChargingTypeEnum;
import com.trilogy.app.crm.bundle.QuotaTypeEnum;
import com.trilogy.app.crm.bundle.exception.BundleAlreadyExistsException;
import com.trilogy.app.crm.bundle.exception.BundleDoesNotExistsException;
import com.trilogy.app.crm.bundle.exception.BundleManagerException;
import com.trilogy.app.crm.bundle.exception.SubscriberProfileDoesNotExistException;
import com.trilogy.app.crm.xhome.home.TransientFieldResettingHome;

/**
 * @author victor.stratan@redknee.com
 */
public class TestFakeCRMBundleProfile implements com.redknee.app.crm.bundle.service.CRMBundleProfile
{
    final Home home;

    public ArrayList<String> switchBundlesMsisdnList_ = new ArrayList<String>();
    public ArrayList switchBundlesOldList_ = new ArrayList();
    public ArrayList switchBundlesNewList_ = new ArrayList();

    public TestFakeCRMBundleProfile(final Context ctx)
    {
        home = new TransientFieldResettingHome(ctx, new BundleProfileTransientHome(ctx));
    }

    public void clearCounters()
    {
        switchBundlesMsisdnList_.clear();
        switchBundlesOldList_.clear();
        switchBundlesNewList_.clear();
    }

    public Set<Long> getBundleIdsByCategoryIds(Context ctx, Set<Long> categoryIds) throws BundleManagerException
    {
        return null;
    }
    
    
    public BundleProfile getBundleProfile(final Context ctx, final int spId, final long bundleId) throws BundleDoesNotExistsException, BundleManagerException
    {
        try
        {
            return (BundleProfile) home.find(ctx, Long.valueOf(bundleId));
        }
        catch (HomeException e)
        {
            throw new BundleManagerException(e);
        }
    }

    public void createBundle(final Context ctx, final BundleProfile profile) throws BundleAlreadyExistsException, BundleManagerException
    {
        try
        {
            home.create(ctx, profile);
        }
        catch (HomeException e)
        {
            throw new BundleManagerException(e);
        }
    }

    public Map<Long, Long> switchBundles(final Context ctx, final String msisdn, final int spid, int subscriptionType, final Collection oldBundles, final Collection newBundles, final Collection options) throws SubscriberProfileDoesNotExistException, BundleManagerException
    {
        switchBundlesMsisdnList_.add(msisdn);
        switchBundlesOldList_.add(oldBundles);
        switchBundlesNewList_.add(newBundles);
        return new HashMap<Long, Long>();
    }

    public void removeBundleProfile(final Context ctx, final int spId, final long bundleId) throws BundleDoesNotExistsException, BundleManagerException
    {
    }

    public void updateBundle(final Context ctx, final BundleProfile profile) throws BundleDoesNotExistsException, BundleManagerException
    {
    	try
        {
            home.store(ctx, profile);
        }
        catch (HomeException e)
        {
            throw new BundleManagerException(e);
        }
    }

    public Home getBundlesPointBundlesByQuotaScheme(final Context ctx, final QuotaTypeEnum quotaScheme) throws BundleManagerException
    {
        return null;
    }

    public Home getBundlesByGroupScheme(final Context ctx, final GroupChargingTypeEnum groupScheme) throws BundleManagerException
    {
        return null;
    }

    public Home getBundlesByGroupScheme(final Context ctx, final GroupChargingTypeEnum groupScheme, final Collection ids, final boolean inclusive) throws BundleManagerException
    {
        return null;
    }

    public Home getInvoiceCategoryBundles(final Context ctx, final int spid, final Set unavailableBundles, final Set categories) throws BundleManagerException
    {
        return null;
    }

    public Home getOneTimeBundles(final Context ctx, final Collection bundleIds) throws BundleDoesNotExistsException, BundleManagerException
    {
        return null;
    }

    public Home getNonOneTimeBundles(final Context ctx, final Collection bundleIds) throws BundleDoesNotExistsException, BundleManagerException
    {
        return null;
    }

    public Collection<BundleProfile> getBundleByCategory(final Context ctx, final long categoryId) throws BundleManagerException
    {
        return null;
    }

    public BundleProfile getBundleByAdjustmentType(final Context ctx, final int adjustmentId) throws  BundleManagerException
    {
        return null;
    }

    public Home getBundlesBySegment(final Context ctx, final BundleSegmentEnum segment, final Collection includedBundles, final boolean onlyAuxiliary) throws BundleDoesNotExistsException, BundleManagerException
    {
        return null;
    }

    public Home getBundlesBySPID(final Context ctx, final int spid) throws BundleManagerException
    {
        return null;
    }

    public Home getAuxiliaryBundlesBySPID(final Context ctx, final int spid) throws BundleManagerException
    {
        return null;
    }
}
