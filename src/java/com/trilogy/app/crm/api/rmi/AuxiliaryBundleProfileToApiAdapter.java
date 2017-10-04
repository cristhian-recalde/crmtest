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
package com.trilogy.app.crm.api.rmi;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Adapter;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.MinorLogMsg;

import com.trilogy.app.crm.bean.core.BundleProfile;
import com.trilogy.app.crm.bundle.BundleCategory;
import com.trilogy.app.crm.bundle.UnitTypeEnum;
import com.trilogy.product.bundle.manager.api.RecurrenceTypeEnum;
import com.trilogy.product.bundle.manager.api.v21.ActivationFeeCalculationEnum;
import com.trilogy.util.crmapi.wsdl.v2_0.types.PaidTypeEnum;
import com.trilogy.util.crmapi.wsdl.v2_1.types.bundlequery.AuxiliaryBundle;
import com.trilogy.util.crmapi.wsdl.v2_1.types.bundlequery.BundleType;
import com.trilogy.util.crmapi.wsdl.v2_1.types.bundlequery.FixedDateRange;
import com.trilogy.util.crmapi.wsdl.v2_1.types.bundlequery.FixedEndDate;
import com.trilogy.util.crmapi.wsdl.v2_1.types.bundlequery.FixedInterval;
import com.trilogy.util.crmapi.wsdl.v2_1.types.bundlequery.RecurrenceScheme;
import com.trilogy.util.crmapi.wsdl.v2_1.types.bundlequery.RecurrenceSchemeChoice_type0;
import com.trilogy.util.crmapi.wsdl.v2_1.types.bundlequery.UnitType;
import com.trilogy.util.crmapi.wsdl.v2_1.types.bundlequery.ValidityInterval;


/**
 * 
 *
 * @author aaron.gourley@redknee.com
 * @since 7.4
 */
public class AuxiliaryBundleProfileToApiAdapter implements Adapter
{

    /**
     * {@inheritDoc}
     */
    public Object adapt(Context ctx, Object obj) throws HomeException
    {
        AuxiliaryBundle result = null;

        if (obj instanceof BundleProfile)
        {
            BundleProfile bundle = (BundleProfile) obj;

            result = new AuxiliaryBundle();
            result.setAdjustmentType(Long.valueOf(bundle.getAdjustmentType()));
            result.setBundleCategoryId(Long.valueOf(bundle.getBundleCategoryId()));
            result.setBundleID(bundle.getBundleId());
            result.setBundleName(bundle.getName());
            result.setSpid(bundle.getSpid());
            result.setCharge(bundle.getAuxiliaryServiceCharge());
            result.setDisabled(!bundle.isEnabled());
            result.setProrationEnabled(ActivationFeeCalculationEnum.PRORATE.equals(bundle.getActivationFeeCalculation()));
            result.setSubscriptionType(-1);
            result.setInitialBalanceLimit(Long.valueOf(bundle.getInitialBalanceLimit()));

            try
            {
                result.setBundleType(BundleType.Factory.fromValue(bundle.getType()));
            }
            catch (IllegalArgumentException e)
            {
                new MinorLogMsg(this, "Bundle with ID " + bundle.getBundleId() + " has invalid bundle type: " + bundle.getType(), e).log(ctx);
            }

            try
            {
                result.setRecurrenceScheme(adaptRecurrenceSchemeToApi(bundle));
            }
            catch (IllegalArgumentException e)
            {
                new MinorLogMsg(this, "Bundle with ID " + bundle.getBundleId() + " has invalid recurrence scheme.", e).log(ctx);
            }

            try
            {
                result.setSubscriberType(PaidTypeEnum.valueOf(bundle.getSegment().getIndex()));
            }
            catch (IllegalArgumentException e)
            {
                new MinorLogMsg(this, "Bundle with ID " + bundle.getBundleId() + " has invalid subscriber type: " + bundle.getSegment(), e).log(ctx);
            }

            UnitTypeEnum balanceUnitType = null;
            if (bundle.isSingleService())
            {
                BundleCategory bundleCat = null;
                try
                {
                    bundleCat = bundle.getCategory(ctx);
                    if (bundleCat == null)
                    {
                        new MinorLogMsg(this, "Bundle with ID " + bundle.getBundleId() + " has invalid category: " + bundle.getBundleCategoryId(), null).log(ctx);
                    }
                    else
                    {
                        balanceUnitType = bundleCat.getUnitType();
                    }
                }
                catch (HomeException e)
                {
                    new MinorLogMsg(this, "Bundle with ID " + bundle.getBundleId() + " has invalid category: " + bundle.getBundleCategoryId(), e).log(ctx);
                }
            }
            else if (bundle.isCurrency())
            {
                // Force use of currency balance type.  The bundle category itself will still be of base type (e.g. Volume Seconds).
                // In BM, this is overridden in the bundle category association, but we don't have access to that table.
                balanceUnitType = UnitTypeEnum.CURRENCY;
            }
            else 
            {
                balanceUnitType = UnitTypeEnum.CROSS_UNIT;
            }
            
            if (balanceUnitType != null)
            {
                try
                {
                    result.setBalanceUnitType(UnitType.Factory.fromValue(balanceUnitType.getIndex()));
                }
                catch (IllegalArgumentException e)
                {
                    new MinorLogMsg(this, "Bundle with ID " + bundle.getBundleId() + " has invalid unit type: " + balanceUnitType, e).log(ctx);                        
                }
            }
        }

        return result;
    }

    private RecurrenceScheme adaptRecurrenceSchemeToApi(BundleProfile bundle) throws IllegalArgumentException
    {
        RecurrenceSchemeChoice_type0 scheme = new RecurrenceSchemeChoice_type0();

        FixedEndDate date;
        FixedDateRange dateRange;
        FixedInterval interval;
        switch (bundle.getRecurrenceScheme().getIndex())
        {
        case RecurrenceTypeEnum.RECUR_CYCLE_FIXED_DATETIME_INDEX:
            date = new FixedEndDate();
            date.setEndDate(bundle.getEndDate());
            scheme.setRecurringFixedDate(date);
            break;
        case RecurrenceTypeEnum.RECUR_CYCLE_FIXED_INTERVAL_INDEX:
            interval = new FixedInterval();
            interval.setValidity(bundle.getValidity());
            interval.setValidityInterval(ValidityInterval.Factory.fromValue(bundle.getInterval()));
            scheme.setRecurringFixedInterval(interval );
            break;
        case RecurrenceTypeEnum.ONE_OFF_FIXED_DATE_RANGE_INDEX:
            dateRange = new FixedDateRange();
            dateRange.setStartDate(bundle.getStartDate());
            dateRange.setEndDate(bundle.getEndDate());
            scheme.setOneOffFixedDate(dateRange);
            break;
        case RecurrenceTypeEnum.ONE_OFF_FIXED_INTERVAL_INDEX:
            interval = new FixedInterval();
            interval.setValidity(bundle.getValidity());
            interval.setValidityInterval(ValidityInterval.Factory.fromValue(bundle.getInterval()));
            scheme.setOneOffFixedInterval(interval);
            break;
        }

        RecurrenceScheme schemeHolder = new RecurrenceScheme();
        schemeHolder.setRecurrenceSchemeChoice_type0(scheme);
        return schemeHolder;
    }


    /**
     * {@inheritDoc}
     */
    public Object unAdapt(Context ctx, Object obj) throws HomeException
    {
        throw new UnsupportedOperationException();
    }
}
