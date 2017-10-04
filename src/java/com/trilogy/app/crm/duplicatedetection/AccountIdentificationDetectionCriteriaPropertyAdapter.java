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

package com.trilogy.app.crm.duplicatedetection;

import com.trilogy.app.crm.bean.account.AccountIdentification;
import com.trilogy.app.crm.bean.duplicatedetection.IdentificationDetectionCriteriaProperty;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Adapter;
import com.trilogy.framework.xhome.home.HomeException;

/**
 * Adapts AccountIdentification to IdentificationDetectionCriteriaProperty
 * 
 * @author cindy.wong@redknee.com
 * @since 8.5
 */
public class AccountIdentificationDetectionCriteriaPropertyAdapter implements Adapter
{

    private static final long serialVersionUID = 1L;
    private static final AccountIdentificationDetectionCriteriaPropertyAdapter instance_ = new AccountIdentificationDetectionCriteriaPropertyAdapter();

    public static AccountIdentificationDetectionCriteriaPropertyAdapter instance()
    {
        return instance_;
    }

    /**
     * Constructor for AccountIdentificationDetectionCriteriaPropertyAdapter.
     */
    private AccountIdentificationDetectionCriteriaPropertyAdapter()
    {
        // empty
    }

    /**
     * @see com.redknee.framework.xhome.home.Adapter#adapt(com.redknee.framework.xhome.context.Context,
     *      java.lang.Object)
     */
    @Override
    public Object adapt(Context ctx, Object obj)
    {
        AccountIdentification id = (AccountIdentification) obj;
        IdentificationDetectionCriteriaProperty property = new IdentificationDetectionCriteriaProperty();
        property.setIdNumber(id.getIdNumber());
        property.setIdType(id.getIdType());
        return property;
    }

    /**
     * @see com.redknee.framework.xhome.home.Adapter#unAdapt(com.redknee.framework.xhome.context.Context,
     *      java.lang.Object)
     */
    @Override
    public Object unAdapt(Context ctx, Object obj) throws HomeException
    {
        // not supported
        return null;
    }

}
