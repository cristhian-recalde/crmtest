/*
 * This code is a protected work and subject to domestic and international
 * copyright law(s). A complete listing of authors of this work is readily
 * available. Additionally, source code is, by its very nature, confidential
 * information and inextricably contains trade secrets and other information
 * proprietary, valuable and sensitive to Redknee, no unauthorised use,
 * disclosure, manipulation or otherwise is permitted, and may only be used
 * in accordance with the terms of the licence agreement entered into with
 * Redknee Inc. and/or its subsidiaries.
 *
 * Copyright &copy; Redknee Inc. and its subsidiaries. All Rights Reserved.
 *
 */
package com.trilogy.app.crm.web.control;

import com.trilogy.framework.xhome.webcontrol.WebControl;

import com.trilogy.app.crm.bean.AccountUsageWebControl;
import com.trilogy.app.crm.calculation.support.AccountSupport;

/**
 * Custom web control to display account usage and balance.
 * 
 * @author cindy.wong@redknee.com
 * @since 8.5
 */
public class CustomAccountUsageWebControl extends AccountUsageWebControl
{
	private final WebControl amountWebControl_ = new BalanceDisplayWebControl(
	    true, AccountSupport.INVALID_VALUE);

	/**
	 * {@inheritDoc}
	 */
	@Override
	public WebControl getAmountDueWebControl()
	{
		return amountWebControl_;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public WebControl getPaymentWebControl()
	{
		return amountWebControl_;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public WebControl getPaymentExcludingOverPaymentWebControl()
	{
		return amountWebControl_;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public WebControl getOtherAdjustmentsWebControl()
	{
		return amountWebControl_;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public WebControl getMDUsageWebControl()
	{
		return amountWebControl_;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public WebControl getBalanceWebControl()
	{
		return amountWebControl_;
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
        return amountWebControl_;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public WebControl getGroupUsageWebControl()
    {
        return amountWebControl_;
    }


}
