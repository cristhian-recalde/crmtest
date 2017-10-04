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
package com.trilogy.app.crm.filter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.LicenseCheck;
import com.trilogy.framework.xhome.filter.AndPredicate;
import com.trilogy.framework.xhome.filter.Predicate;
import com.trilogy.framework.xhome.webcontrol.AbstractWebControl;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MinorLogMsg;

import com.trilogy.app.crm.LicenseConstants;
import com.trilogy.app.crm.bean.ServiceTypeEnum;
import com.trilogy.app.crm.bean.ui.Service;
import com.trilogy.app.crm.technology.TechnologyEnum;

/**
 * Provides a predicate for hiding particular ServiceTypeEnum value. 
 * Pass ServiceTypeEnum as an argument to the constructor, that ServiceTypeEnum will get hidden at GUI.
 * 
 * @author vikash.kumar@redknee.com
 */

public class HideParticularServiceTypePredicate implements Predicate {
	private ServiceTypeEnum serviceTypeEnum;

	/**
	 * Constructor
	 * @param ServiceTypeEnum
	 */
	public HideParticularServiceTypePredicate(ServiceTypeEnum serviceTypeEnum) {
		this.serviceTypeEnum = serviceTypeEnum;
	}
	
	/**
	 * Overriden f
	 * @param context
	 * @param object
	 * @return boolean
	 */

	@Override
	public boolean f(final Context context, final Object object) {
		boolean appropriate = true;

		final ServiceTypeEnum serviceType = (ServiceTypeEnum) object;

		if (serviceType.equals(serviceTypeEnum)) {
			appropriate = false;
		}

		return appropriate;

	}

}
