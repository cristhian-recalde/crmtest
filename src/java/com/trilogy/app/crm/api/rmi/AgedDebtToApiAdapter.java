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
package com.trilogy.app.crm.api.rmi;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Adapter;
import com.trilogy.framework.xhome.home.HomeException;

import com.trilogy.util.crmapi.wsdl.v3_0.types.account.AgedDebtPeriodDetail;

import com.trilogy.app.crm.bean.AgedDebt;
import com.trilogy.app.crm.support.CalendarSupportHelper;

/**
 * Adapts AgedDebt object to API objects.
 * 
 * @author cindy.wong@redknee.com
 * @since 9.0
 */
public class AgedDebtToApiAdapter implements Adapter
{

	@Override
	public Object adapt(Context ctx, Object obj) throws HomeException
	{
		AgedDebtPeriodDetail apiAgedDebt = new AgedDebtPeriodDetail();
		AgedDebt agedDebt = (AgedDebt) obj;
		apiAgedDebt.setPeriodInvoiceDate(CalendarSupportHelper.get(ctx)
		    .dateToCalendar(agedDebt.getDebtDate()));
		apiAgedDebt.setDebt(Long.valueOf(agedDebt.getCurrentDebt()));
		return apiAgedDebt;
	}

	@Override
	public Object unAdapt(Context ctx, Object obj) throws HomeException
	{
		throw new UnsupportedOperationException();
	}

}
