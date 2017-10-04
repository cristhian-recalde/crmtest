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

import java.util.List;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Adapter;
import com.trilogy.framework.xhome.home.HomeException;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.account.Contact;
import com.trilogy.app.crm.bean.duplicatedetection.DuplicateAccountDetectionResult;
import com.trilogy.app.crm.support.AccountSupport;

/**
 * Adapts an account into a DuplicateAccountDetectionResult object.
 * 
 * @author cindy.wong@redknee.com
 * @since 2010-09-09
 */
public class AccountDetectionResultAdapter implements Adapter
{

	/**
	 * Serial version UID.
	 */
	private static final long serialVersionUID = 1L;

	private static final AccountDetectionResultAdapter instance =
	    new AccountDetectionResultAdapter();

	public static AccountDetectionResultAdapter instance()
	{
		return instance;
	}

	@Override
	public Object adapt(Context ctx, Object obj) throws HomeException
	{
		Account account = (Account) obj;
		Contact contact = AccountSupport.lookupContactOfAccount(ctx, account);

		DuplicateAccountDetectionResult result =
		    new DuplicateAccountDetectionResult();

		result.setBan(account.getBAN());
		result.setAccountState(account.getState());
		result.setAccountType(account.getType());
		result.setAddress(concatAddress(account, contact));
		result.setCity(contact.getCity());
		result.setDateOfBirth(contact.getDateOfBirth());
		result.setFirstName(account.getFirstName());
		result.setLastName(account.getLastName());
		result.setSpid(account.getSpid());
		result
		    .setIdentifications((List) AccountDetectionIdentificationResultListAdapter
		        .instance().adapt(ctx, account));
		result
		    .setSubscriptions((List) AccountDetectionSubscriptionResultListAdapter
		        .instance().adapt(ctx, account));
		return result;
	}

	private String concatAddress(final Account account, final Contact contact)
	{
		final StringBuilder sb = new StringBuilder();
		sb.append(account.getBillingAddress1());
		if (contact.getAddressLineTwo() != null
		    && !contact.getAddressLineTwo().isEmpty())
		{
			sb.append(", ");
			sb.append(contact.getAddressLineTwo());
		}
		if (contact.getAddressLineThree() != null
		    && !contact.getAddressLineThree().isEmpty())
		{
			sb.append(", ");
			sb.append(contact.getAddressLineThree());
		}
		return sb.toString();
	}

	@Override
	public Object unAdapt(Context ctx, Object obj) throws HomeException
	{
		throw new UnsupportedOperationException();
	}

}
