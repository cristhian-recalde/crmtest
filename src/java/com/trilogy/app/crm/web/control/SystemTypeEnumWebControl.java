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

package com.trilogy.app.crm.web.control;

import javax.servlet.ServletRequest;

import com.trilogy.app.crm.LicenseConstants;
import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountCreationTemplate;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.bean.duplicatedetection.DuplicateAccountDetectionForm;
import com.trilogy.app.crm.move.request.ConvertAccountBillingTypeRequest;
import com.trilogy.app.crm.support.LicensingSupportHelper;
import com.trilogy.framework.license.LicenseMgr;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.webcontrol.AbstractWebControl;
import com.trilogy.framework.xhome.webcontrol.EnumWebControl;
import com.trilogy.framework.xhome.xenum.Enum;
import com.trilogy.framework.xhome.xenum.EnumCollection;

public class SystemTypeEnumWebControl extends EnumWebControl
{
	public SystemTypeEnumWebControl()
	{
		this(true);
	}

	public SystemTypeEnumWebControl(boolean autoPreview)
	{
		super(SubscriberTypeEnum.COLLECTION, autoPreview);
	}

	// Hardcoded cached collections. These must be sorted by index in ascending
	// order in order
	// for EnumCollection.getByIndex() to work properly because it uses binary
	// search.
	private static EnumCollection PREPAID_POSTPAID_COLLECTION =
	    new EnumCollection(new Enum[]
	    {
	        SubscriberTypeEnum.POSTPAID, SubscriberTypeEnum.PREPAID
	    });
	private static EnumCollection POSTPAID_CONVERGED_COLLECTION =
	    new EnumCollection(new Enum[]
	    {
	        SubscriberTypeEnum.POSTPAID, SubscriberTypeEnum.HYBRID
	    });

	private static EnumCollection PREPAID_CONVERGED_COLLECTION =
	    new EnumCollection(new Enum[]
	    {
	        SubscriberTypeEnum.PREPAID, SubscriberTypeEnum.HYBRID
	    });

	private static EnumCollection POSTPAID_ONLY_COLLECTION =
	    new EnumCollection(new Enum[]
	    {
		    SubscriberTypeEnum.POSTPAID
	    });

	private static EnumCollection PREPAID_ONLY_COLLECTION = new EnumCollection(
	    new Enum[]
	    {
		    SubscriberTypeEnum.PREPAID
	    });

	private static EnumCollection HYBRID_ONLY_COLLECTION = new EnumCollection(
	    new Enum[]
	    {
		    SubscriberTypeEnum.HYBRID
	    });

	@Override
	public EnumCollection getEnumCollection(Context ctx)
	{
		if (ctx.get(AbstractWebControl.BEAN) instanceof AccountCreationTemplate)
		{
			AccountCreationTemplate act =
			    (AccountCreationTemplate) ctx.get(AbstractWebControl.BEAN);

			if (act.isIndividual(ctx))
			{
				return PREPAID_POSTPAID_COLLECTION;
			}

			if (act.isPooled(ctx))
			{
				if (LicensingSupportHelper.get(ctx).isLicensed(ctx,
				    LicenseConstants.HYBRID_LICENSE_KEY))
				{
					return SubscriberTypeEnum.COLLECTION;
				}
				else
				{
					return PREPAID_POSTPAID_COLLECTION;
				}

			}

			if (LicensingSupportHelper.get(ctx).isLicensed(ctx,
			    LicenseConstants.HYBRID_LICENSE_KEY))
			{
				return SubscriberTypeEnum.COLLECTION;
			}

			return PREPAID_POSTPAID_COLLECTION;
		}
		else if (ctx.get(AbstractWebControl.BEAN) instanceof ConvertAccountBillingTypeRequest)
		{
			ConvertAccountBillingTypeRequest conversion =
			    (ConvertAccountBillingTypeRequest) ctx
			        .get(AbstractWebControl.BEAN);
			Account account = (Account) ctx.get(Account.class);
			if (account != null)
			{
				if (conversion.isIndividualSubscriberSubAccount())
				{
					if (account.getSystemType() == SubscriberTypeEnum.PREPAID)
					{
						return POSTPAID_ONLY_COLLECTION;
					}
					else if (account.getSystemType() == SubscriberTypeEnum.POSTPAID)
					{
						return PREPAID_ONLY_COLLECTION;
					}
					else
					{
						return PREPAID_POSTPAID_COLLECTION;
					}
				}
				else
				{
					return HYBRID_ONLY_COLLECTION;
				}
			}
			return PREPAID_POSTPAID_COLLECTION;
		}
		else if (ctx.get(AbstractWebControl.BEAN) instanceof DuplicateAccountDetectionForm)
		{
			DuplicateAccountDetectionForm criteria =
			    (DuplicateAccountDetectionForm) ctx
			        .get(AbstractWebControl.BEAN);

			EnumCollection result = SubscriberTypeEnum.COLLECTION;

			final LicenseMgr lMgr = (LicenseMgr) ctx.get(LicenseMgr.class);
			boolean isPrepaidLicensed =
			    lMgr.isLicensed(ctx, LicenseConstants.PREPAID_LICENSE_KEY);
			boolean isHybridLicensed =
			    lMgr.isLicensed(ctx, LicenseConstants.HYBRID_LICENSE_KEY);
			boolean isPostpaidLicensed =
			    lMgr.isLicensed(ctx, LicenseConstants.POSTPAID_LICENSE_KEY);

			if (isPrepaidLicensed && isPostpaidLicensed)
			{
				if (!isHybridLicensed)
				{
					result = PREPAID_POSTPAID_COLLECTION;
				}
			}
			else if (isPrepaidLicensed)
			{
				result = PREPAID_ONLY_COLLECTION;
			}
			else if (isPostpaidLicensed)
			{
				result = POSTPAID_ONLY_COLLECTION;
			}
			return result;
		}
		else
		{
			Account account = (Account) ctx.get(AbstractWebControl.BEAN);

			EnumCollection result = SubscriberTypeEnum.COLLECTION;

            final LicenseMgr lMgr = (LicenseMgr) ctx.get(LicenseMgr.class);
            boolean isHybridLicenced = lMgr.isLicensed(ctx, LicenseConstants.HYBRID_LICENSE_KEY);

            if (account.isPooled(ctx))
            {
                boolean isPrepaidGroupPooledLicensed = lMgr.isLicensed(ctx,
                        LicenseConstants.PREPAID_GROUP_POOLED_LICENSE_KEY);
                if (isPrepaidGroupPooledLicensed)
                {
                    if (!isHybridLicenced)
                    {
                        result = PREPAID_POSTPAID_COLLECTION;
                    }
                }
				else if (isHybridLicenced)
				{
					result = POSTPAID_CONVERGED_COLLECTION;
				}
				else
				{
					result = POSTPAID_ONLY_COLLECTION;
				}
			}
			else if (!isHybridLicenced)
			{
				result = PREPAID_POSTPAID_COLLECTION;
			}
			return result;
		}
	}

	/**
	 * if the selected value is no longer available in the list of
	 * possible values, force the selected value value to be the
	 * first item in the possible values.
	 */
	@Override
	public Object fromWeb(Context ctx, ServletRequest req, String name)
	{
		try
		{
			req.getParameter(name);
			return super.fromWeb(ctx, req, name);
		}
		catch (NullPointerException npEx)
		{
			throw npEx;
		}
		catch (Throwable t)
		{
			return getEnumCollection(ctx).get((short) 0);
		}
	}
	
}
