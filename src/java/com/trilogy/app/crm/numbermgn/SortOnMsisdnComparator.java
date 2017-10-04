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

package com.trilogy.app.crm.numbermgn;

import java.io.Serializable;
import java.util.Comparator;

import com.trilogy.app.crm.bean.Msisdn;

/**
 * Comparator to be used for sorting of MSISDNs in desired order.
 *
 * @author candy.wong@redknee.com
 */
public class SortOnMsisdnComparator implements Comparator, Serializable
{
    /**
     * Create a MSISDN comparator with a specific order.
     *
     * @param descending true if descending order is desired.
     */
    public SortOnMsisdnComparator(final boolean descending)
    {
        setDescending(descending);
    }

    /**
     * {@inheritDoc}
     */
    public int compare(final Object o1, final Object o2)
    {
        final Msisdn msisdn1 = (Msisdn) o1;
        final Msisdn msisdn2 = (Msisdn) o2;

        if (isDescending())
        {
            return msisdn2.getMsisdn().compareTo(msisdn1.getMsisdn());
        }
        else
        {
            return msisdn1.getMsisdn().compareTo(msisdn2.getMsisdn());
        }
    }

    /**
     * {@inheritDoc}
     */
    public boolean equals(final Object obj)
    {
        if (obj == null)
        {
            return false;
        }

        if (obj instanceof SortOnMsisdnComparator)
        {
            return isDescending() == ((SortOnMsisdnComparator) obj).isDescending();
        }

        return false;
    }

    /**
     * {@inheritDoc}
     */
    public int hashCode()
    {
        return (descending_ ? 1 : 0);
    }

    /**
     * Getter for descending property.
     *
     * @return true if descending order is specified.
     */
    public boolean isDescending()
    {
        return descending_;
    }

    /**
     * Setter for descending property.
     *
     * @param descending true if descending order is desired.
     */
    public void setDescending(final boolean descending)
    {
        descending_ = descending;
    }

    private boolean descending_ = false;
}
