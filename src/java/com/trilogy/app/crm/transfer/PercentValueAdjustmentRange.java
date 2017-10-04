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
package com.trilogy.app.crm.transfer;

import com.trilogy.framework.xhome.beans.GreaterThanMaxDecimalException;
import com.trilogy.framework.xhome.beans.LessThanMinDecimalException;
import com.trilogy.framework.xhome.beans.xi.PropertyInfo;


/**
 * @author gary.anderson@redknee.com
 */
public class PercentValueAdjustmentRange
    extends AbstractPercentValueAdjustmentRange
{
    /**
     * 
     */
    private static final long serialVersionUID = 1L;


    public String getDisplay()
    {
        return "" + getLowerBound() + ": " + getPercentValue();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void assertPercentValue(final String percentValue)
        throws IllegalArgumentException
    {
        // Allow the default validation to occur first (string not empty).
        super.assertPercentValue(percentValue);

        // Ensure that the value is a valid double/BigDecimal.
        final double value;

        try
        {
            value = new java.math.BigDecimal(percentValue).doubleValue();
        }
        catch (final NumberFormatException nfe)
        {
            // TODO - 2008-07-28 - Use MessageMgr.
            throw new IllegalArgumentException("Not a valid value.", nfe);
        }

        // Ensure that the value is within a valid range.
        final PropertyInfo propertyInfo = PercentValueAdjustmentRangeXInfo.PERCENT_VALUE;
        final double min = Double.parseDouble((String)propertyInfo.getAttributes().get("MIN"));
        final double max = Double.parseDouble((String)propertyInfo.getAttributes().get("MAX"));

        if (value < min)
        {
            throw new LessThanMinDecimalException(propertyInfo, percentValue, min);
        }
        else if (value > max)
        {
            throw new GreaterThanMaxDecimalException(propertyInfo, percentValue, max);
        }

    }


}
