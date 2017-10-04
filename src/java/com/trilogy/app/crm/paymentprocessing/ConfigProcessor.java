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
package com.trilogy.app.crm.paymentprocessing;

import java.io.Serializable;
import java.util.Date;
import java.util.SortedSet;

import com.trilogy.framework.xhome.context.Context;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.Invoice;
import com.trilogy.app.crm.bean.LateFeeEarlyRewardConfiguration;
import com.trilogy.app.crm.extension.creditcategory.CreditCategoryExtension;

/**
 * Processes late fee / early reward configurations.
 * 
 * @author cindy.wong@redknee.com
 * @since 2010-11-04
 */
public interface ConfigProcessor extends Serializable
{
	LateFeeEarlyReward processConfig(Context context, Account account,
	    CreditCategoryExtension extension, Invoice invoice, Date date,
	    int deadline, LateFeeEarlyRewardConfiguration config);

	LateFeeEarlyReward processConfig(Context context, Account account,
	    CreditCategoryExtension extension, Invoice invoice, Date date,
	    int deadline, LateFeeEarlyRewardConfiguration config,
	    long additionalPayment);
}
