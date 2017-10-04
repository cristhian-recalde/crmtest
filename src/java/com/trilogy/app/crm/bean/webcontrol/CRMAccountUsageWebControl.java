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
package com.trilogy.app.crm.bean.webcontrol;

import com.trilogy.framework.xhome.webcontrol.WebControl;

import com.trilogy.app.crm.bean.AccountUsageWebControl;
import com.trilogy.app.crm.support.AccountSupport;
import com.trilogy.app.crm.web.control.BalanceDisplayWebControl;


/**
 * 
 *
 * @author aaron.gourley@redknee.com
 * @since 
 */
public class CRMAccountUsageWebControl extends AccountUsageWebControl
{
    /**
     * {@inheritDoc}
     */
    @Override
    public WebControl getAmountDueWebControl()
    {
        return INVALID_VALUE_WC;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public WebControl getPaymentWebControl()
    {
        return INVALID_VALUE_WC;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public WebControl getOtherAdjustmentsWebControl()
    {
        return INVALID_VALUE_WC;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public WebControl getMDUsageWebControl()
    {
        return INVALID_VALUE_WC;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public WebControl getBalanceWebControl()
    {
        return INVALID_VALUE_WC;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public WebControl getGroupUsageQuotaWebControl()
    {
        return com.redknee.app.crm.web.control.LimitCurrencyWebControl.instance();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public WebControl getGroupUsageQuotaAllocatedWebControl()
    {
        return INVALID_VALUE_WC;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public WebControl getGroupUsageWebControl()
    {
        return INVALID_VALUE_WC;
    }

    
    protected static final WebControl INVALID_VALUE_WC = new BalanceDisplayWebControl(true, AccountSupport.INVALID_VALUE);
}
