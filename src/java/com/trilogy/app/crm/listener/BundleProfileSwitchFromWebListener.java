/*
 * This code is a protected work and subject to domestic and international copyright
 * law(s). A complete listing of authors of this work is readily available. Additionally,
 * source code is, by its very nature, confidential information and inextricably contains
 * trade secrets and other information proprietary, valuable and sensitive to Redknee. No
 * unauthorized use, disclosure, manipulation or otherwise is permitted, and may only be
 * used in accordance with the terms of the license agreement entered into with Redknee
 * Inc. and/or its subsidiaries.
 * 
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.listener;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import com.trilogy.app.crm.bean.ServicePeriodEnum;
import com.trilogy.app.crm.bean.ui.BundleProfile;
import com.trilogy.app.crm.bundle.DurationTypeEnum;
import com.trilogy.app.crm.bundle.FlexTypeEnum;
import com.trilogy.app.crm.bundle.QuotaTypeEnum;
import com.trilogy.app.crm.bundle.RecurrenceTypeEnum;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.util.snippet.log.Logger;


public class BundleProfileSwitchFromWebListener implements PropertyChangeListener
{

    public void propertyChange(final PropertyChangeEvent evt)
    {
        final Context ctx = (Context) evt.getSource();
        final BundleProfile oldBundleProfile = (BundleProfile) evt.getOldValue();
        final BundleProfile newBundleProfile = (BundleProfile) evt.getNewValue();
        if (BundleProfile.isFromWebNewOrPreviewOnFlexType(ctx))
        {
            boolean flexTypeChange = oldBundleProfile == null
                    ? true
                    : oldBundleProfile.getFlexType() != newBundleProfile.getFlexType();
            if (newBundleProfile.isFlex() && newBundleProfile.getFlexType() == FlexTypeEnum.SECONDARY)
            {
                try
                {
                    if (flexTypeChange)
                    {
                        // Setting default values for secondary flex
                        // One time, Moving quota, Fixed interval, BCD
                        newBundleProfile.setChargingRecurrenceScheme(ServicePeriodEnum.ONE_TIME);
                        newBundleProfile.setQuotaScheme(QuotaTypeEnum.MOVING_QUOTA);
                        newBundleProfile.setRecurrenceScheme(RecurrenceTypeEnum.ONE_OFF_FIXED_INTERVAL);
                        newBundleProfile.setInterval(DurationTypeEnum.BCD_INDEX);
                    }
                }
                catch (Throwable t)
                {
                    if (Logger.isDebugEnabled())
                    {
                        Logger.debug(ctx, this,
                                "Error setting default BundleProfile values in BundleProfile initialization", t);
                    }
                }
            }
        }
    }
}
