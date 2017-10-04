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

import java.util.Collections;
import java.util.Map;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.EQIC;
import com.trilogy.framework.xhome.elang.In;
import com.trilogy.framework.xhome.elang.NEQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.visitor.FunctionVisitor;
import com.trilogy.framework.xhome.visitor.HashSetBuildingVisitor;
import com.trilogy.framework.xlog.log.LogSupport;

import com.trilogy.app.crm.bean.AccountHome;
import com.trilogy.app.crm.bean.AccountStateEnum;
import com.trilogy.app.crm.bean.AccountXInfo;
import com.trilogy.app.crm.bean.account.ContactHome;
import com.trilogy.app.crm.bean.account.ContactXInfo;
import com.trilogy.app.crm.bean.duplicatedetection.NameDOBDetectionCriteria;

/**
 * Command for duplicate account detection by account holder's first name, last
 * name, and date of birth.
 * 
 * @author cindy.wong@redknee.com
 * @since 8.5
 */
class NameDOBDuplicateDetectionCommand extends DuplicateAccountDetectionCommand
{

	/**
	 * Constructor for NameDOBDuplicateDetectionCommand.
	 * 
	 * @param criteria
	 */
	NameDOBDuplicateDetectionCommand(final NameDOBDetectionCriteria criteria)
	{
		super(criteria);
	}

	/**
	 * Find all duplicates of the criteria. An account is considered a duplicate
	 * if the first name, last name, and date of birth match exactly,
	 * case-insensitively.
	 * 
	 * @throws HomeException
	 * @throws HomeInternalException
	 * @see com.redknee.app.crm.duplicatedetection.DuplicateAccountDetectionCommand#findDuplicates(com.redknee.framework.xhome.context.Context)
	 */
	@Override
	public Map findDuplicates(final Context context)
	    throws HomeInternalException, HomeException
	{
		final NameDOBDetectionCriteria criteria =
		    (NameDOBDetectionCriteria) criteria_;
		final And accountPredicate = new And();
		accountPredicate.add(new EQIC(AccountXInfo.FIRST_NAME, criteria
		    .getFirstName()));
		accountPredicate.add(new EQIC(AccountXInfo.LAST_NAME, criteria
		    .getLastName()));
		accountPredicate.add(new EQ(AccountXInfo.SPID, criteria_.getSpid()));

		/*
		 * [Cindy Wong] TT#10081706018: Ignore inactive accounts.
		 */
		accountPredicate.add(new NEQ(AccountXInfo.STATE,
		    AccountStateEnum.INACTIVE));

		final Home accountHome = (Home) context.get(AccountHome.class);
		final HashSetBuildingVisitor set = new HashSetBuildingVisitor();
		final FunctionVisitor function =
		    new FunctionVisitor(AccountXInfo.BAN, set);
		accountHome.forEach(context, function, accountPredicate);

		final And and = new And();
		and.add(new EQ(ContactXInfo.DATE_OF_BIRTH, criteria.getDateOfBirth()));
		and.add(new In(ContactXInfo.ACCOUNT, set));

		final Home home = (Home) context.get(ContactHome.class);
		if (home == null)
		{
			LogSupport
			    .minor(context, this, "Contact home not found in context");
			return Collections.EMPTY_MAP;
		}

		ContactResultMapBuildingVisitor visitor =
		    new ContactResultMapBuildingVisitor();
		visitor =
		    (ContactResultMapBuildingVisitor) home.forEach(context, visitor,
		        and);
		if (LogSupport.isDebugEnabled(context))
		{
			LogSupport.debug(context, this,
			    "Matches for [first name: " + criteria.getFirstName()
			        + ", last name: " + criteria.getLastName() + ", DOB: "
			        + criteria.getDateOfBirth() + "]: " + visitor.size()
			        + " accounts");
		}
		return visitor;
	}

}
