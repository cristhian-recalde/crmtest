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

import java.util.Date;
import java.util.Iterator;
import java.util.Map;

import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.beans.Validator;
import com.trilogy.framework.xhome.context.Context;

import com.trilogy.app.crm.bean.PricePlanVersion;
import com.trilogy.app.crm.bean.ServicePackageVersionXInfo;
import com.trilogy.app.crm.bean.ServicePreferenceEnum;
import com.trilogy.app.crm.bean.core.BundleFee;
import com.trilogy.app.crm.bean.core.BundleProfile;
import com.trilogy.app.crm.bundle.GroupChargingTypeEnum;
import com.trilogy.app.crm.bundle.RecurrenceTypeEnum;
import com.trilogy.app.crm.exception.RethrowExceptionListener;
import com.trilogy.app.crm.support.BundleSupportHelper;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.util.snippet.log.Logger;

/**
 * If the price plan comes with a one time bundle, it make sure the service preference cannot be made "Mandatory".
 * Also, expired one time bunldes are prohibited in new price plan versions.
 *
 * @author victor.stratan@redknee.com
 */

public class PricePlanVersionValidator implements Validator
{
    /**
     * {@inheritDoc}
     */
    public void validate(final Context ctx, final Object obj)
    {
        final PricePlanVersion ppv = (PricePlanVersion) obj;
        final Map bundlefees = ppv.getServicePackageVersion().getBundleFees();

        if (bundlefees.size() == 0)
        {
            // shortcut for Price Plan Versions with no bundles
            return;
        }

        final Iterator it = bundlefees.entrySet().iterator();
        final RethrowExceptionListener exceptions = new RethrowExceptionListener();
        final Date today = CalendarSupportHelper.get(ctx).getRunningDate(ctx);

        while (it.hasNext())
        {
            final Map.Entry entry = (Map.Entry) it.next();
            final Long bundleId = (Long) entry.getKey();
            final BundleFee bundlefee = (BundleFee) entry.getValue();

            final BundleProfile profile;
            try
            {
                profile = BundleSupportHelper.get(ctx).getBundleProfile(ctx, bundleId.longValue());
            }
            catch (Exception e)
            {
                final String msg = "Unable to validate presence of bundle " + bundleId + " in price plan "
                        + ppv.getId() + " while attempting to create new version.";
                final IllegalPropertyArgumentException ex = new IllegalPropertyArgumentException(
                        ServicePackageVersionXInfo.BUNDLE_FEES, msg);
                ex.initCause(e);
                Logger.minor(ctx, this, msg, e);
                exceptions.thrown(ex);
                continue;
            }

            if (bundlefee.getServicePreference().equals(ServicePreferenceEnum.MANDATORY))
            {

                if (GroupChargingTypeEnum.GROUP_BUNDLE.equals(profile.getGroupChargingScheme()))
                {
                    final String msg = profile.getGroupChargingScheme().getDescription()
                            + " " + bundleId + " cannot be made mandatory!";
                    final IllegalPropertyArgumentException ex = new IllegalPropertyArgumentException(
                            ServicePackageVersionXInfo.BUNDLE_FEES, msg);
                    exceptions.thrown(ex);
                }
            }

            Date endDate = profile.getEndDate();
            if (profile.getRecurrenceScheme().equals(RecurrenceTypeEnum.ONE_OFF_FIXED_DATE_RANGE)
                    && (endDate != null && endDate.before(today)))
            {
                final String msg = "Cannot assign expired bundle " + bundleId + " to price plan";
                final IllegalPropertyArgumentException ex = new IllegalPropertyArgumentException(
                        ServicePackageVersionXInfo.BUNDLE_FEES, msg);
                exceptions.thrown(ex);
            }
        }

        exceptions.throwAllAsCompoundException();
    }
}



