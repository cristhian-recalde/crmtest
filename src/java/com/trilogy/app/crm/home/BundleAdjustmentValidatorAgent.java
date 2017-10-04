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
 */
package com.trilogy.app.crm.home;

import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountHome;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.payment.PaymentAgent;
import com.trilogy.app.crm.bundle.BundleBulkAdjustment;
import com.trilogy.app.crm.report.ReportUtilities;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.app.crm.support.SubscriberSupport;
import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.beans.Validator;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgent;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.pipe.PipelineAgent;
import com.trilogy.framework.xlog.log.MinorLogMsg;


/**
 * This class validates input parameters from BundleAdjustment Bulk Load.
 *
 * @author suyash.gaidhani@redknee.com
 * @since 9.3.2
 */
public class BundleAdjustmentValidatorAgent
extends PipelineAgent
implements Validator
{

	public BundleAdjustmentValidatorAgent(ContextAgent delegate)
    {
        super(delegate);
    }
	
	public void execute(Context ctx) throws AgentException
	{
		try
		{
			validate(ctx, ctx.get(BundleBulkAdjustment.class));
		}
		catch(IllegalStateException e)
		{
			throw new AgentException(e.getMessage(), e);
		}
		
		pass(ctx, this, "The Bundle Adjsutment Request has been validated successfully.");
	}
	/**
	 * INHERIT
	 */
	public void validate(Context ctx,Object obj)
			throws IllegalStateException
			{
		final BundleBulkAdjustment adjustment = (BundleBulkAdjustment) obj;
		Account acct = null;
		Subscriber sub = null;

		final CompoundIllegalStateException exception = 
				new CompoundIllegalStateException();

		final String msisdn = adjustment.getMSISDN().trim();
		if (msisdn.length() == 0)
		{

			String message = "MSISDN must be provided for Bundle Adjustment";
			exception.thrown(new IllegalArgumentException(
					));
			new MinorLogMsg(this, message, null).log(ctx);
		}
		
		
		SimpleDateFormat format = new SimpleDateFormat(BundleAdjustmentBulkAdapterAgent.DATE_FORMAT_STRING);
		Date transDate = null;

		if (adjustment.getTransDate()!=null && !adjustment.getTransDate().trim().isEmpty())
		{
    		try 
    		{
				transDate = format.parse(adjustment.getTransDate());
			} 
    		catch (ParseException e) 
    		{
    			final String formattedMsg = MessageFormat.format(
    					"TransDate of the adjustment is not in the proper format of  \"{0}\"",
    					BundleAdjustmentBulkAdapterAgent.DATE_FORMAT_STRING);
    			new MinorLogMsg(this, formattedMsg, null).log(ctx);
    			exception.thrown(new IllegalArgumentException(formattedMsg));
    		
			}
		}
		else
		{
		    transDate = CalendarSupportHelper.get(ctx).getRunningDate(ctx);
		}
		

		try
		{
			sub=SubscriberSupport.lookupSubscriberForMSISDN(ctx,msisdn, transDate);
			ctx.put(Subscriber.class, sub);

			if (sub == null)
			{
				throw new HomeException("");
			} 
		}
		catch (HomeException e)
		{
			final String formattedMsg = MessageFormat.format(
					"Subscriber could not be found for MSISDN \"{0}\" and active date \"{1}\".",
					msisdn, transDate);
			new MinorLogMsg(this, formattedMsg, null).log(ctx);
			exception.thrown(new IllegalArgumentException(formattedMsg));
		}
		
		if(sub != null)
		{

			try
			{
				acct
				= (Account) ReportUtilities.findByPrimaryKey(
						ctx,
						AccountHome.class,
						sub.getBAN());
				ctx.put(Account.class, acct);

				if (acct == null)
				{
					throw new HomeException("");
				} 

			}
			catch (HomeException e)
			{
				final String formattedMsg = MessageFormat.format(
						"Account could not be found for MSISDN \"{0}\" and activeDate \"{1}\".",
						msisdn, transDate);
				new MinorLogMsg(this, formattedMsg, null).log(ctx);
				exception.thrown(new IllegalArgumentException(formattedMsg));
			}

		}
		// If no payment agency is provided, the string "default" is used.
		String paymentAgency  = null;

		try
		{
			paymentAgency = adjustment.getPaymentAgency().trim();
			if (paymentAgency.length() == 0)
			{
				adjustment.setPaymentAgency("default");
			}
			else
			{
				PaymentAgent agent = HomeSupportHelper.get(ctx).findBean(ctx, PaymentAgent.class, paymentAgency);

				if (agent == null)
				{
					throw new HomeException("");
				}
			}
		}
		catch (HomeException e)
		{
			final String formattedMsg = MessageFormat.format(
					"Payment Agency could not be found with name  \"{0}\"",
					paymentAgency);
			new MinorLogMsg(this, formattedMsg, null).log(ctx);
			exception.thrown(new IllegalArgumentException(formattedMsg));
		}

		long amount  = adjustment.getAmount();
		if (amount < 0)
		{
			String formattedMsg = "Bundle Adjustment Amount cannot be negative value.";
			new MinorLogMsg(this, formattedMsg, null).log(ctx);
			exception.thrown(new IllegalArgumentException(
					formattedMsg));
		}
		
		if (adjustment.getBundleAdjustmentType() == null)
		{
			String formattedMsg = "Bundle Adjustment Type must be Provided. The valid values are 0=Increment, 1=Decrement.";
			new MinorLogMsg(this, formattedMsg, null).log(ctx);
			exception.thrown(new IllegalArgumentException(
					formattedMsg));
		}
		
		
		exception.throwAll();
	}
}
