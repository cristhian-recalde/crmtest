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

import java.util.Date;

import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.visitor.AbortVisitException;
import com.trilogy.framework.xhome.visitor.Visitor;

import com.trilogy.app.crm.bean.Account;

/**
 * @author cindy.wong@redknee.com
 * @since 2010-11-01
 */
public class InvoicePaymentProcessorAccountVisitor implements Visitor
{
	private static final long serialVersionUID = 1L;

	public InvoicePaymentProcessorAccountVisitor(
	    LateFeeEarlyRewardAccountProcessor processor, Date billingDate)
	{
		processor_ = processor;
		billingDate_ = billingDate;
	}

	/**
	 * @param ctx
	 * @param obj
	 * @throws AgentException
	 * @throws AbortVisitException
	 * @see com.redknee.framework.xhome.visitor.Visitor#visit(com.redknee.framework.xhome.context.Context,
	 *      java.lang.Object)
	 */
	@Override
	public void visit(Context ctx, Object obj) throws AgentException,
	    AbortVisitException
	{
		Account account = (Account) obj;
		processor_.processAccount(ctx, account, billingDate_);
	}

	protected final Date billingDate_;
	protected final LateFeeEarlyRewardAccountProcessor processor_;

}
