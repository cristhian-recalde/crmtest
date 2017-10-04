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
package com.trilogy.app.crm.factory;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextFactory;

import com.trilogy.app.crm.LicenseConstants;
import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.SubscriberTypeRecurringChargeEnum;
import com.trilogy.app.crm.support.LicensingSupportHelper;

public class CRMSpidFactory implements ContextFactory
{
    public CRMSpidFactory()
    {
    }

    /**
     * {@inheritDoc}
     */
    public Object create(final Context context)
    {
        final CRMSpid bean = new CRMSpid();

        final boolean postpaidLicense = LicensingSupportHelper.get(context).isLicensed(context, LicenseConstants.POSTPAID_LICENSE_KEY);
        final boolean prepaidLicense = LicensingSupportHelper.get(context).isLicensed(context, LicenseConstants.PREPAID_LICENSE_KEY);
        if (postpaidLicense && !prepaidLicense)
        {
            bean.setRecurChargeSubscriberType(SubscriberTypeRecurringChargeEnum.POSTPAID);
            bean.setRecurringChargeSubType(SubscriberTypeRecurringChargeEnum.POSTPAID);
        }
        else if (prepaidLicense && !postpaidLicense)
        {
            bean.setRecurChargeSubscriberType(SubscriberTypeRecurringChargeEnum.PREPAID);
            bean.setRecurringChargeSubType(SubscriberTypeRecurringChargeEnum.PREPAID);
        }


        return bean;
    }

} // class
