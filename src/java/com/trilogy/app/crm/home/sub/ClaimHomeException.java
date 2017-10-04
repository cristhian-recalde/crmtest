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
package com.trilogy.app.crm.home.sub;

import com.trilogy.framework.xhome.home.HomeException;

/**
 * TODO rename this class
 * Claim exceptions for Msisdns voice/fax/data, and Package
 *
 * @author joe.chen@redknee.com
 */
public class ClaimHomeException extends HomeException
{

	/**
	 * @param arg0
	 */
	public ClaimHomeException(String arg0) {
		super(arg0);
	}
	/**
	 * @param arg0
	 * @param arg1
	 */
	public ClaimHomeException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}
	/**
	 * @param arg0
	 */
	public ClaimHomeException(Throwable arg0) {
		super(arg0);
	}
}
