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

import com.trilogy.framework.xhome.beans.xi.PropertyInfo;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.webcontrol.AbstractWebControl;

import com.trilogy.app.crm.transfer.OwnerFilteredContractGroupKeyWebControl.OwnerExtractor;


/**
 * Provides an OwnerExtractor that uses a PropertyInfo to extract an owner value
 * from the bean in the Context using AbstractWebControl.BEAN.
 *
 * @author gary.anderson@redknee.com
 */
public class BeanPropertyOwnerExtractor
    implements OwnerExtractor
{

    /**
     * Creates a new extractor for the given property.
     *
     * @param property The property of the bean that holds the "owner" value.
     */
    public BeanPropertyOwnerExtractor(final PropertyInfo property)
    {
        property_ = property;
    }


    /**
     * {@inheritDoc}
     */
    public String getOwner(final Context context)
    {
        final Object bean = context.get(AbstractWebControl.BEAN);
        return (String)property_.get(bean);
    }


    /**
     * The property of the bean that holds the "owner" value.
     */
    private final PropertyInfo property_;
}
