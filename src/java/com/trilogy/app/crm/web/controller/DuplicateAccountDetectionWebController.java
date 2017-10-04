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
package com.trilogy.app.crm.web.controller;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.webcontrol.WebController;

import com.trilogy.app.crm.web.border.DuplicateAccountDetectionWebControllerSummaryRequestServicer;

/**
 * @author cindy.wong@redknee.com
 * @since 2010-12-22
 */
public class DuplicateAccountDetectionWebController extends WebController
{
	/**
	 * Constructor for DuplicateAccountDetectionWebController.
	 * @param ctx
	 * @param beanType
	 */
	public DuplicateAccountDetectionWebController(Context ctx, Class beanType)
	{
		super(ctx, beanType);
		customInit();
	}

	protected void customInit()
	{
		summaryServicer_ =
		    new DuplicateAccountDetectionWebControllerSummaryRequestServicer(
		        this);
	}
}
