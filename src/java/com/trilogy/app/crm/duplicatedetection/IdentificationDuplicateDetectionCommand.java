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
import com.trilogy.framework.xhome.elang.Or;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xlog.log.LogSupport;

import com.trilogy.app.crm.bean.account.AccountIdentificationHome;
import com.trilogy.app.crm.bean.account.AccountIdentificationXInfo;
import com.trilogy.app.crm.bean.duplicatedetection.IdentificationDetectionCriteria;
import com.trilogy.app.crm.bean.duplicatedetection.IdentificationDetectionCriteriaProperty;

/**
 * Command for duplicate account detection by account identification.
 * 
 * @author cindy.wong@redknee.com
 * @since 8.5
 */
class IdentificationDuplicateDetectionCommand extends
    DuplicateAccountDetectionCommand
{

	/**
	 * Constructor for IdentificationDuplicateDetectionCommand.
	 * 
	 * @param criteria
	 */
	IdentificationDuplicateDetectionCommand(
	    final IdentificationDetectionCriteria criteria)
	{
		super(criteria);
	}

	/**
	 * Finds all duplicates of the criteria. An account is considered a
	 * duplicate if at least one of the account identifications match exactly.
	 * 
	 * @throws HomeException
	 * @throws HomeInternalException
	 * @see com.redknee.app.crm.duplicatedetection.DuplicateAccountDetectionCommand#findDuplicates(com.redknee.framework.xhome.context.Context)
	 */
	@Override
	public Map findDuplicates(final Context context)
	    throws HomeInternalException, HomeException
	{
		Home home = (Home) context.get(AccountIdentificationHome.class);
		if (home == null)
		{
			LogSupport.minor(context, this,
			    "AccountIdentification home not found in context");
			return Collections.EMPTY_MAP;
		}

		Or predicate = new Or();
		StringBuilder logMessage = new StringBuilder();
		if (LogSupport.isDebugEnabled(context))
		{
			logMessage.append("Matches for ID ");
		}
		for (final Object obj : ((IdentificationDetectionCriteria) criteria_)
		    .getIdentifications())
		{
			final IdentificationDetectionCriteriaProperty property =
			    (IdentificationDetectionCriteriaProperty) obj;
			final And and = new And();
			and.add(new EQ(AccountIdentificationXInfo.ID_TYPE, property
			    .getIdType()));
			and.add(new EQ(AccountIdentificationXInfo.ID_NUMBER, property
			    .getIdNumber()));
			predicate.add(and);
			if (LogSupport.isDebugEnabled(context))
			{
				logMessage.append("[type: ");
				logMessage.append(property.getIdType());
				logMessage.append(", number: ");
				logMessage.append(property.getIdNumber());
				logMessage.append("] ");
			}
		}

		IdentificationResultMapBuildingVisitor visitor =
		    new IdentificationResultMapBuildingVisitor();
		visitor =
		    (IdentificationResultMapBuildingVisitor) home.forEach(context,
		        visitor, predicate);

		if (LogSupport.isDebugEnabled(context))
		{
			logMessage.append(": ");
			logMessage.append(visitor.size());
			logMessage.append(" accounts");
			LogSupport.debug(context, this, logMessage.toString());
		}

		return visitor;
	}

}
