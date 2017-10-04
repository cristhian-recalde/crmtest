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

import java.util.Map;
import java.util.TreeMap;

import com.trilogy.framework.xhome.beans.GreaterThanMaxDecimalException;
import com.trilogy.framework.xhome.beans.LessThanMinDecimalException;
import com.trilogy.framework.xhome.beans.xi.PropertyInfo;
import com.trilogy.framework.xhome.context.Context;


public class TransferAgreement
    extends AbstractTransferAgreement
{

    /**
     * 
     */
    private static final long serialVersionUID = 1L;


    private void append(final Context context, final PropertyInfo info, final StringBuffer buffer, final String extra)
    {
        buffer.append(info.getLabel(context));
        buffer.append(": ");
        info.append(this, buffer);
        buffer.append(extra);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void setPercentValueRanges(final Map newPercentValueRanges)
        throws IllegalArgumentException
    {
        final Map convertedMap = new TreeMap();
        convertedMap.putAll(newPercentValueRanges);
        super.setPercentValueRanges(convertedMap);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void setFlatValueRanges(final Map newFlatValueRanges)
        throws IllegalArgumentException
    {
        final Map convertedMap = new TreeMap();
        convertedMap.putAll(newFlatValueRanges);
        super.setFlatValueRanges(convertedMap);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void assertPercentDistribution(final String distribution)
        throws IllegalArgumentException
    {
        // Allow the default validation to occur first (string not empty).
        super.assertPercentDistribution(distribution);

        // Ensure that the value is a valid double/BigDecimal.
        final double value;

        try
        {
            value = new java.math.BigDecimal(distribution).doubleValue();
        }
        catch (final NumberFormatException nfe)
        {
            // TODO - 2008-07-28 - Use MessageMgr.
            throw new IllegalArgumentException("Not a valid value.", nfe);
        }

        // Ensure that the value is within a valid range.
        final PropertyInfo propertyInfo = TransferAgreementXInfo.PERCENT_DISTRIBUTION;
        final double min = Double.parseDouble((String)propertyInfo.getAttributes().get("MIN"));
        final double max = Double.parseDouble((String)propertyInfo.getAttributes().get("MAX"));

        if (value < min)
        {
            throw new LessThanMinDecimalException(propertyInfo, distribution, min);
        }
        else if (value > max)
        {
            throw new GreaterThanMaxDecimalException(propertyInfo, distribution, max);
        }
    }


}
