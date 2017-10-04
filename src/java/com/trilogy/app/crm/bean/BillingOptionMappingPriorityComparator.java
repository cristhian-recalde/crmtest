/*
 * Created on Apr 20, 2005
 *
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
package com.trilogy.app.crm.bean;

import java.io.Serializable;
import java.util.Comparator;

/**
 * @author psperneac
 * @since Apr 20, 2005
 */
public class BillingOptionMappingPriorityComparator implements Comparator, Serializable
{
    public int compare(Object o1, Object o2)
    {
        BillingOptionMapping b1=(BillingOptionMapping) o1;
        BillingOptionMapping b2=(BillingOptionMapping) o2;

        if (b1.getPriority()==b2.getPriority())
        {
            return Long.valueOf(b1.getIdentifier()).compareTo(Long.valueOf(b2.getIdentifier()));
        }

        return b1.getPriority().getIndex() - b2.getPriority().getIndex();
    }
}
