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
package com.trilogy.app.crm.duplicatedetection;

import com.trilogy.framework.xhome.beans.Function;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Adapter;
import com.trilogy.framework.xhome.home.HomeException;

import com.trilogy.app.crm.bean.account.AccountIdentification;
import com.trilogy.app.crm.bean.duplicatedetection.DuplicateAccountDetectionIdentificationResult;

/**
 * Adapts an AccountIdentification object into a
 * DuplicateAccountDetectionIdentificationResult object.
 * 
 * @author cindy.wong@redknee.com
 * @since 2010-09-09
 */
public class IdentificationDetectionResultAdapter implements Adapter, Function
{

	/**
	 * Serial version UID.
	 */
	private static final long serialVersionUID = 1L;

	private static final IdentificationDetectionResultAdapter instance =
	    new IdentificationDetectionResultAdapter();

	public static IdentificationDetectionResultAdapter instance()
	{
		return instance;
	}

	@Override
	public Object f(Context ctx, Object obj)
	{
		AccountIdentification id = (AccountIdentification) obj;
		DuplicateAccountDetectionIdentificationResult result =
		    new DuplicateAccountDetectionIdentificationResult();
		result.setIdType(id.getIdType());
		result.setIdNumber(id.getIdNumber());
		result.setMatched(false);
		return result;
	}

	@Override
	public Object adapt(Context ctx, Object obj) throws HomeException
	{
		return f(ctx, obj);
	}

	@Override
	public Object unAdapt(Context ctx, Object obj) throws HomeException
	{
		throw new UnsupportedOperationException();
	}

}
