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

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountXInfo;
import com.trilogy.app.crm.bean.BillCycle;
import com.trilogy.app.crm.bean.core.CreditCategory;
import com.trilogy.app.crm.extension.Extension;
import com.trilogy.app.crm.extension.creditcategory.CreditCategoryExtensionHolder;
import com.trilogy.app.crm.support.AccountSupport;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.visitor.AbortVisitException;
import com.trilogy.framework.xhome.visitor.Visitor;
import com.trilogy.framework.xlog.log.LogSupport;

/**
 * Visitor to process a credit category.
 * 
 * @author cindy.wong@redknee.com
 * @since 2010-11-01
 */
public class InvoicePaymentProcessingCreditCategoryVisitor implements Visitor
{

	/**
	 * serial version UID.
	 */
	private static final long serialVersionUID = 1L;

	public InvoicePaymentProcessingCreditCategoryVisitor(BillCycle billCycle,
	    LateFeeEarlyRewardAccountProcessor processor, Date billingDate)
	{
		billCycle_ = billCycle;
		billingDate_ = billingDate;
		processor_ = processor;
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
		CreditCategory cc = (CreditCategory) obj;
		boolean found = false;
		Context subCtx = ctx.createSubContext();

		for (final Object em : cc.getCreditCategoryExtensions())
		{
			CreditCategoryExtensionHolder holder =
			    (CreditCategoryExtensionHolder) em;
			Extension ext = holder.getExtension();
			if (processor_.getExtensionFetcher().isValidExtension(subCtx, ext))
			{
				processor_.getExtensionFetcher().cacheExtension(subCtx, ext);
				found = true;
				break;
			}
		}

		if (!found)
		{
			if (LogSupport.isDebugEnabled(subCtx))
			{
				LogSupport.debug(ctx, this,
				    "Credit Category " + cc.getCode()
				        + " does not have the extension required by "
				        + processor_.getName() + "; skipping");
			}
			return;
		}

		// cache credit category
		subCtx.put(ExtensionFetcher.CREDIT_CATEGORY_KEY, cc);
		try
		{
			Visitor visitor = new InvoicePaymentProcessorAccountVisitor(processor_, billingDate_);

			List <String> lateFeeEligibleBANs = new ArrayList<String>();
			lateFeeEligibleBANs = (List<String>) AccountSupport.getBANsListPerCreditCategory(subCtx, billCycle_.getIdentifier(), cc.getCode(), Boolean.TRUE);

			if(!lateFeeEligibleBANs.isEmpty())
			{
				Iterator<String> bansIterator = lateFeeEligibleBANs.listIterator();
				while(bansIterator.hasNext())
				{
					String ban = bansIterator.next();
					Account account = HomeSupportHelper.get(subCtx).findBean(subCtx, Account.class, new EQ(AccountXInfo.BAN, ban));

					visitor.visit(subCtx, account);
				}
			}
			else
			{
				LogSupport.info(subCtx, this, "No eligible accounts found of credit category : " +cc.getCode() + "to apply Late fee");
			}
		}
		catch (HomeException exception)
		{
			LogSupport
			    .minor(
			        ctx,
			        this,
			        "Exception caught while traversing account home for Payment Promotion processing",
			        exception);
		}
	}

	final Date billingDate_;
	final BillCycle billCycle_;
	final LateFeeEarlyRewardAccountProcessor processor_;
}
