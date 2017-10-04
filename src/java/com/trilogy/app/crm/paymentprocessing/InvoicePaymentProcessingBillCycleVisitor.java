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
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.visitor.AbortVisitException;
import com.trilogy.framework.xhome.visitor.Visitor;
import com.trilogy.framework.xlog.log.LogSupport;

import com.trilogy.app.crm.bean.BillCycle;
import com.trilogy.app.crm.bean.CreditCategoryHome;
import com.trilogy.app.crm.bean.CreditCategoryXInfo;

/**
 * @author cindy.wong@redknee.com
 * @since 2010-11-01
 */
public class InvoicePaymentProcessingBillCycleVisitor implements Visitor
{
	private static final long serialVersionUID = 1L;

	public InvoicePaymentProcessingBillCycleVisitor(
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
		BillCycle bc = (BillCycle) obj;
		// find all credit categories of the SPID
		final Home creditCategoryHome =
		    (Home) ctx.get(CreditCategoryHome.class);
		if (creditCategoryHome == null)
		{
			throw new AbortVisitException(
			    "Credit Category Home not found in context!");
		}

		Visitor visitor =
		    new InvoicePaymentProcessingCreditCategoryVisitor(bc, processor_,
		        billingDate_);
		try
		{
			creditCategoryHome.forEach(ctx, visitor, new EQ(
			    CreditCategoryXInfo.SPID, bc.getSpid()));
		}
		catch (HomeException exception)
		{
			LogSupport
			    .minor(ctx, this,
			        "Exception caught during invoice payment processing",
			        exception);
			throw new AgentException(exception);
		}
	}

	private final LateFeeEarlyRewardAccountProcessor processor_;
	private final Date billingDate_;
}
