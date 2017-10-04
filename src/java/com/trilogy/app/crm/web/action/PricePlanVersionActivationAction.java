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
package com.trilogy.app.crm.web.action;

import com.trilogy.framework.xhome.web.action.SimpleWebAction;

/**
 * Activates a price plan version.
 * 
 * @author cindy.wong@redknee.com
 * @since 8.5
 */
public class PricePlanVersionActivationAction extends SimpleWebAction
{

	public static final String DEFAULT_KEY = "activate";

	/**
	 * Serial version UID.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor for PricePlanActivationAction.
	 */
	public PricePlanVersionActivationAction()
	{
		super("activate", "activate");
		defaultHelpText_ = "Activate the price plan version.";
	}

	/**
	 * Constructor for PricePlanActivationAction.
	 * 
	 * @param key
	 *            Key for this web action.
	 * @param label
	 *            Lable for this web action.
	 */
	public PricePlanVersionActivationAction(final String key, final String label)
	{
		super(key, label);
	}

}
