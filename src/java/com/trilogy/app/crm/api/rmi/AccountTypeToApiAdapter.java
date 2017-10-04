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
package com.trilogy.app.crm.api.rmi;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Adapter;
import com.trilogy.framework.xhome.home.HomeException;

import com.trilogy.app.crm.api.rmi.support.RmiApiSupport;
import com.trilogy.app.crm.bean.AccountCategory;
import com.trilogy.util.crmapi.wsdl.v2_0.types.PoolLimitStrategyEnum;
import com.trilogy.util.crmapi.wsdl.v3_0.types.account.AccountCustomerTypeEnum;
import com.trilogy.util.crmapi.wsdl.v3_0.types.account.AccountTypeReference;

/**
 * Adapts AccountType object to API objects.
 *
 * @author victor.stratan@redknee.com
 */
public class AccountTypeToApiAdapter implements Adapter
{
    @Override
    public Object adapt(final Context ctx, final Object obj) throws HomeException
    {
		return adaptAccountTypeToReference(ctx, (AccountCategory) obj);
    }

    @Override
    public Object unAdapt(final Context ctx, final Object obj) throws HomeException
    {
        throw new UnsupportedOperationException();
    }

	public static AccountTypeReference adaptAccountTypeToReference(
	    final Context ctx, final AccountCategory accountType)
	{
		final AccountTypeReference reference = new AccountTypeReference();
		reference.setIdentifier(accountType.getIdentifier());
		// reference.setSpid(accountType.getSpid());
		int spid = 0;
		final Integer id = (Integer) ctx.get(RmiApiSupport.API_SPID);
		if (id != null)
		{
			spid = id.intValue();
		}
		reference.setSpid(spid);
		reference.setName(accountType.getName());
		reference.setDescription(accountType.getDescription());
		reference
		    .setAllowBillCycleChange(accountType.getAllowBillCycleChange());
		reference.setIsLetterOfDemandExempt(accountType.getExemptLOD());
		reference.setPrepaymentDepositRequired(accountType
		    .getPrepaymentDepositRequired());
		reference.setUsageType(AccountCustomerTypeEnum.valueOf(accountType.getCustomerType().getIndex()));

		return reference;
	}
}
