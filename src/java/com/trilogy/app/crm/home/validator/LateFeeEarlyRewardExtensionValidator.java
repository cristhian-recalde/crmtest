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
package com.trilogy.app.crm.home.validator;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.beans.Validator;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.In;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.LogSupport;

import com.trilogy.app.crm.bean.EarlyRewardConfiguration;
import com.trilogy.app.crm.bean.EarlyRewardConfigurationHome;
import com.trilogy.app.crm.bean.EarlyRewardExtensionProperty;
import com.trilogy.app.crm.bean.EarlyRewardExtensionPropertyXInfo;
import com.trilogy.app.crm.bean.LateFeeConfiguration;
import com.trilogy.app.crm.bean.LateFeeConfigurationHome;
import com.trilogy.app.crm.bean.LateFeeEarlyRewardConfigurationXInfo;
import com.trilogy.app.crm.bean.LateFeeExtensionProperty;
import com.trilogy.app.crm.bean.LateFeeExtensionPropertyXInfo;
import com.trilogy.app.crm.bean.LateFeeTypeEnum;
import com.trilogy.app.crm.bean.core.CreditCategory;
import com.trilogy.app.crm.extension.Extension;
import com.trilogy.app.crm.extension.creditcategory.CreditCategoryExtensionHolder;
import com.trilogy.app.crm.extension.creditcategory.EarlyRewardCreditCategoryExtension;
import com.trilogy.app.crm.extension.creditcategory.LateFeeCreditCategoryExtension;
import com.trilogy.app.crm.extension.creditcategory.LateFeeCreditCategoryExtensionXInfo;

/**
 * @author cindy.wong@redknee.com
 * @since 2010-11-15
 */
public class LateFeeEarlyRewardExtensionValidator implements Validator
{

	private static LateFeeEarlyRewardExtensionValidator instance =
	    new LateFeeEarlyRewardExtensionValidator();

	public static LateFeeEarlyRewardExtensionValidator instance()
	{
		return instance;
	}

	/**
	 * @param ctx
	 * @param obj
	 * @throws IllegalStateException
	 * @see com.redknee.framework.xhome.beans.Validator#validate(com.redknee.framework.xhome.context.Context,
	 *      java.lang.Object)
	 */
	@Override
	public void validate(Context ctx, Object obj) throws IllegalStateException
	{
		CreditCategory creditCategory = (CreditCategory) obj;
		CompoundIllegalStateException el = new CompoundIllegalStateException();
		for (final Object o : creditCategory.getCreditCategoryExtensions())
		{
			CreditCategoryExtensionHolder holder =
			    (CreditCategoryExtensionHolder) o;
			Extension ext = holder.getExtension();

			if (ext instanceof LateFeeCreditCategoryExtension)
			{
				try
				{
					validate(ctx, (LateFeeCreditCategoryExtension) ext);
				}
				catch (Exception e)
				{
					el.thrown(e);
				}
			}
			else if (ext instanceof EarlyRewardCreditCategoryExtension)
			{
				try
				{
					validate(ctx, (EarlyRewardCreditCategoryExtension) ext);
				}
				catch (Exception e)
				{
					el.thrown(e);
				}
			}
		}
		el.throwAll();
	}

	protected void
	    validate(Context context, LateFeeCreditCategoryExtension ext)
	        throws IllegalStateException
	{
		if (ext.getThreshold() < 0)
		{
			throw new IllegalPropertyArgumentException(
			    LateFeeCreditCategoryExtensionXInfo.THRESHOLD,
			    "Threshold cannot be less than 0");
		}

		Set<Long> configIds = new HashSet<Long>();
		for (Object key : ext.getConfigurations().keySet())
		{
			LateFeeExtensionProperty property =
			    (LateFeeExtensionProperty) ext.getConfigurations().get(key);
			long configId = property.getConfiguration();
			configIds.add(Long.valueOf(configId));
		}

		Collection configs =
		    retrieveConfigs(context, LateFeeConfigurationHome.class, configIds);
		validateLateFeeConfigs(context, configs);
	}

	protected void validate(Context context,
	    EarlyRewardCreditCategoryExtension ext) throws IllegalStateException
	{
		Set<Long> configIds = new HashSet<Long>();
		for (Object key : ext.getConfigurations().keySet())
		{
			EarlyRewardExtensionProperty property =
			    (EarlyRewardExtensionProperty) ext.getConfigurations().get(key);
			long configId = property.getConfiguration();
			configIds.add(Long.valueOf(configId));
		}

		Collection configs =
		    retrieveConfigs(context, EarlyRewardConfigurationHome.class,
		        configIds);
		validateEarlyRewardConfigs(context, configs);
	}

	protected Collection retrieveConfigs(Context context, Object homeKey,
	    Set<Long> configIds) throws IllegalStateException
	{
		In predicate =
		    new In(LateFeeEarlyRewardConfigurationXInfo.IDENTIFIER, configIds);
		Home home = (Home) context.get(homeKey);
		Collection configs = null;
		try
		{
			configs = home.select(context, predicate);
		}
		catch (HomeException exception)
		{
			LogSupport
			    .info(
			        context,
			        this,
			        "Exception caught while looking up late fee/early reward configurations",
			        exception);
		}
		return configs;
	}

	protected void validateEarlyRewardConfigs(Context context,
	    Collection configs) throws IllegalStateException
	{
		int amountType = -1;
		for (Object obj : configs)
		{
			EarlyRewardConfiguration config = (EarlyRewardConfiguration) obj;
			if (amountType < 0)
			{
				amountType = config.getAmountType();
			}
			else if (amountType != config.getAmountType())
			{
				throw new IllegalPropertyArgumentException(
				    EarlyRewardExtensionPropertyXInfo.CONFIGURATION,
				    "All configurations must have the same amount type");
			}
		}
	}

	protected void validateLateFeeConfigs(Context context, Collection configs)
	    throws IllegalStateException
	{
		int amountType = -1;
		int baseAmountType = -1;
		for (Object obj : configs)
		{
			LateFeeConfiguration config = (LateFeeConfiguration) obj;
			if (amountType < 0)
			{
				amountType = config.getAmountType();
			}
			else if (amountType != config.getAmountType())
			{
				throw new IllegalPropertyArgumentException(
				    LateFeeExtensionPropertyXInfo.CONFIGURATION,
				    "All configurations must have the same amount type");
			}
			if (amountType == LateFeeTypeEnum.PERCENTAGE_INDEX)
			{
				if (baseAmountType == -1)
				{
					baseAmountType = config.getBaseAmountType();
				}
				else if (baseAmountType != config.getBaseAmountType())
				{
					throw new IllegalPropertyArgumentException(
					    LateFeeExtensionPropertyXInfo.CONFIGURATION,
					    "All configurations must have the same base amount type");
				}
			}
		}

	}
}
