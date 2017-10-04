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
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeSet;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xlog.log.LogSupport;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.Invoice;
import com.trilogy.app.crm.bean.LateFeeEarlyRewardConfiguration;
import com.trilogy.app.crm.extension.creditcategory.CreditCategoryExtension;

/**
 * Processes late fee / early reward. The actual behaviour of the processor can
 * be configured by supplying the correct combination of fetchers/processors.
 * 
 * @author cindy.wong@redknee.com
 * @since 2010-11-04
 */
public class LateFeeEarlyRewardAccountProcessor implements Serializable
{

	/**
	 * Serial version UID.
	 */
	private static final long serialVersionUID = 1L;

	private static final LateFeeEarlyRewardAccountProcessor applicableEarlyRewardAccountProcessor =
	    new LateFeeEarlyRewardAccountProcessor(
	        "ApplicableEarlyRewardAccountProcessor",
	        LatestInvoiceFetcher.instance(),
	        EarlyRewardExtensionFetcher.instance(),
	        EarlyRewardConfigFetcher.instance(),
	        ApplicableEarlyRewardConfigProcessor.instance(),
	        SumsResultsProcessor.instance(),
	        NullAsZeroResultValueFormatter.instance());

	private static final LateFeeEarlyRewardAccountProcessor generateEarlyRewardAccountProcessor =
	    new LateFeeEarlyRewardAccountProcessor(
	        "GenerateEarlyRewardAccountProcessor",
	        LatestInvoiceFetcher.instance(),
	        EarlyRewardExtensionFetcher.instance(),
	        EarlyRewardConfigFetcher.instance(),
	        EarlyRewardConfigProcessor.instance(),
	        GenerateTransactionResultsProcessor.instance(),
	        RemoveNullFromSetResultValueFormatter.instance());

	private static final LateFeeEarlyRewardAccountProcessor applicableLateFeeAccountProcessor =
	    new LateFeeEarlyRewardAccountProcessor(
	        "ApplicableLateFeeAccountProcessor",
	        LatestLateFeeApplicableInvoiceFetcher.instance(),
	        LateFeeExtensionFetcher.instance(),
	        LateFeeConfigFetcher.instance(), LateFeeConfigProcessor.instance(),
	        SumsResultsProcessor.instance(),
	        NullAsZeroResultValueFormatter.instance());

	private static final LateFeeEarlyRewardAccountProcessor generateLateFeeAccountProcessor =
	    new LateFeeEarlyRewardAccountProcessor(
	        "GenerateLateFeeAccountProcessor",
	        LatestPastDueInvoiceFetcher.instance(),
	        LateFeeExtensionFetcher.instance(),
	        LateFeeConfigFetcher.instance(), LateFeeConfigProcessor.instance(),
	        GenerateTransactionResultsProcessor.instance(),
	        RemoveNullFromSetResultValueFormatter.instance());

	public static LateFeeEarlyRewardAccountProcessor
	    getApplicableEarlyRewardInstance()
	{
		return applicableEarlyRewardAccountProcessor;
	}

	public static LateFeeEarlyRewardAccountProcessor
	    getApplicableLateFeeInstance()
	{
		return applicableLateFeeAccountProcessor;
	}

	public static LateFeeEarlyRewardAccountProcessor
	    getGenerateEarlyRewardInstance()
	{
		return generateEarlyRewardAccountProcessor;
	}

	public static LateFeeEarlyRewardAccountProcessor
	    getGenerateLateFeeInstance()
	{
		return generateLateFeeAccountProcessor;
	}

	public LateFeeEarlyRewardAccountProcessor(String name,
	    InvoiceFetcher invoiceFetcher, ExtensionFetcher extensionFetcher,
	    ConfigFetcher configFetcher, ConfigProcessor configProcessor,
	    ResultsProcessor resultsProcessor,
	    ResultValueFormatter resultValueFormatter)
	{
		name_ = name;
		invoiceFetcher_ = invoiceFetcher;
		extensionFetcher_ = extensionFetcher;
		configFetcher_ = configFetcher;
		configProcessor_ = configProcessor;
		resultsProcessor_ = resultsProcessor;
		resultValueFormatter_ = resultValueFormatter;
	}

	public Object processAccount(Context context, Account account, Date date)
	{
		return processAccount(context, account, date, 0);
	}

	public Object processAccount(Context context, Account account, Date date,
	    long additionalPayment)
	{
		return resultValueFormatter_
		    .formatValue(
		        context,
		        unformattedProcessAccount(context, account, date,
		            additionalPayment));
	}

	protected Object unformattedProcessAccount(Context context,
	    Account account, Date date, long additionalPayment)
	{
		if (account == null)
		{
			LogSupport.minor(context, this, "No account provided.");
			return null;
		}
		if (date == null)
		{
			LogSupport.minor(context, this, "No date provided.");
			return null;
		}

		Invoice invoice = invoiceFetcher_.getInvoice(context, account, date);
		if (invoice == null)
		{
			if (LogSupport.isDebugEnabled(context))
			{
				LogSupport.debug(context, this, "No invoice found for account="
				    + account.getBAN() + ", date=" + date);
			}
			return null;
		}

		if (invoice.getTotalAmount() <= 0)
		{
			if (LogSupport.isDebugEnabled(context))
			{
				LogSupport.debug(context, this,
				    "invoice " + invoice.getInvoiceId()
				        + " has no amount due; skipping");
			}
			return null;
		}

		if (LogSupport.isDebugEnabled(context))
		{
			LogSupport.debug(context, this,
			    "Processing invoice " + invoice.getInvoiceId()
			        + " [invoiceDate=" + invoice.getInvoiceDate()
			        + ", dueDate=" + invoice.getDueDate() + ", totalAmount="
			        + invoice.getTotalAmount() + "]");
		}

		CreditCategoryExtension extension =
		    extensionFetcher_.getExtension(context, account);
		if (extension == null)
		{
			if (LogSupport.isDebugEnabled(context))
			{
				LogSupport
				    .debug(
				        context,
				        this,
				        "Credit category "
				            + account.getCreditCategory()
				            + " of account "
				            + account.getBAN()
				            + " does not have the required extension for this operation; skipping");
			}
			return null;
		}

		if (LogSupport.isDebugEnabled(context))
		{
			LogSupport.debug(
			    context,
			    this,
			    extension.getClass().getSimpleName()
			        + " found for credit category "
			        + extension.getCreditCategory());
		}

		SortedMap<Integer, LateFeeEarlyRewardConfiguration> configs =
		    configFetcher_.getConfigurations(context, account, extension,
		        invoice, date);

		if (LogSupport.isDebugEnabled(context))
		{
			StringBuilder sb = new StringBuilder();
			sb.append("Based on invoice due date and deadlines, ");
			if (configs.isEmpty())
			{
				sb.append("no configs are applicable.");
			}
			else
			{
				sb.append("the following config(s) may be applicable:\n");
				for (Integer deadline : configs.keySet())
				{
					sb.append("\tdeadline = ");
					sb.append(deadline);
					sb.append(", configId = ");
					sb.append(configs.get(deadline).getIdentifier());
					sb.append("\n");
				}
			}
			LogSupport.debug(context, this, sb.toString());
		}

		SortedSet<LateFeeEarlyReward> results =
		    new TreeSet<LateFeeEarlyReward>();
		for (Integer deadline : configs.keySet())
		{
			LateFeeEarlyRewardConfiguration config = configs.get(deadline);
			if (LogSupport.isDebugEnabled(context))
			{
				LogSupport.debug(context, this, "Processing config deadline="
				    + deadline + ", configId=" + config.getIdentifier());
			}
			LateFeeEarlyReward result =
			    configProcessor_.processConfig(context, account, extension,
			        invoice, date, deadline, config, additionalPayment);
			if (result != null)
			{
				if (LogSupport.isDebugEnabled(context))
				{
					LogSupport.debug(context, this,
					    "Result of processing deadline=" + deadline
					        + ", configId=" + config.getIdentifier() + " is "
					        + result.toString());
				}
				results.add(result);
			}
			else
			{
				if (LogSupport.isDebugEnabled(context))
				{
					LogSupport.debug(context, this,
					    "The config is not applicable.");
				}
			}
		}

		return resultsProcessor_.processResults(context, account, results);
	}

	/**
	 * Returns the value of extensionFetcher.
	 * 
	 * @return the extensionFetcher
	 */
	public ExtensionFetcher getExtensionFetcher()
	{
		return extensionFetcher_;
	}

	/**
	 * Returns the value of invoiceFetcher.
	 * 
	 * @return the invoiceFetcher
	 */
	public InvoiceFetcher getInvoiceFetcher()
	{
		return invoiceFetcher_;
	}

	/**
	 * Returns the value of configFetcher.
	 * 
	 * @return the configFetcher
	 */
	public ConfigFetcher getConfigFetcher()
	{
		return configFetcher_;
	}

	/**
	 * Returns the value of configProcessor.
	 * 
	 * @return the configProcessor
	 */
	public ConfigProcessor getConfigProcessor()
	{
		return configProcessor_;
	}

	/**
	 * Returns the value of resultsProcessor.
	 * 
	 * @return the resultsProcessor
	 */
	public ResultsProcessor getResultsProcessor()
	{
		return resultsProcessor_;
	}

	public String getName()
	{
		return name_;
	}

	private final ExtensionFetcher extensionFetcher_;
	private final InvoiceFetcher invoiceFetcher_;
	private final ConfigFetcher configFetcher_;
	private final ConfigProcessor configProcessor_;
	private final ResultsProcessor resultsProcessor_;
	private final ResultValueFormatter resultValueFormatter_;
	private final String name_;
}
