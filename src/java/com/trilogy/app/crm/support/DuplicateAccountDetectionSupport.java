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

package com.trilogy.app.crm.support;

import java.security.Principal;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.trilogy.framework.xhome.auth.SimplePermission;
import com.trilogy.framework.xhome.auth.bean.User;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.True;
import com.trilogy.framework.xhome.filter.Predicate;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.SortingHome;
import com.trilogy.framework.xhome.msp.SpidAwareHomePredicate;
import com.trilogy.framework.xhome.msp.SpidAwareXInfo;
import com.trilogy.framework.xhome.visitor.FunctionVisitor;
import com.trilogy.framework.xhome.visitor.SetBuildingVisitor;
import com.trilogy.framework.xlog.log.LogSupport;

import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.CRMSpidHome;
import com.trilogy.app.crm.bean.CRMSpidXInfo;
import com.trilogy.app.crm.bean.DuplicateAccountDetectionActionEnum;
import com.trilogy.app.crm.bean.DuplicateAccountDetectionMethodEnum;
import com.trilogy.app.crm.bean.duplicatedetection.DuplicateAccountDetectionForm;
import com.trilogy.app.crm.duplicatedetection.DuplicateAccountDetectionSearchAgentV2;
import com.trilogy.app.crm.extension.Extension;
import com.trilogy.app.crm.extension.spid.DuplicateAccountDetectionSpidExtension;

/**
 * Support class for duplicate account detection.
 * 
 * @author cindy.wong@redknee.com
 * @since 8.5
 */
public class DuplicateAccountDetectionSupport
{
	private DuplicateAccountDetectionSupport()
	{
		// empty
	}

	public static DuplicateAccountDetectionMethodEnum getSpidDetectionMethod(
	    Context ctx, int spid)
	{
		CRMSpid serviceProvider = null;

		try
		{
			serviceProvider = SpidSupport.getCRMSpid(ctx, spid);
		}
		catch (HomeException exception)
		{
			LogSupport.minor(ctx, DuplicateAccountDetectionSupport.class,
			    "Exception caught: Cannot locate SPID " + spid, exception);
		}

		if (serviceProvider == null)
		{
			LogSupport.info(ctx, DuplicateAccountDetectionSupport.class,
			    "SPID " + spid + " not found");
			return null;
		}

		for (Object obj : serviceProvider.getExtensions())
		{
			Extension extension = (Extension) obj;
			if (extension instanceof DuplicateAccountDetectionSpidExtension)
			{
				DuplicateAccountDetectionSpidExtension ext =
				    (DuplicateAccountDetectionSpidExtension) extension;
				if (!ext.isEnabled())
				{
					return null;
				}
				return ext.getDetectionMethod();
			}
		}

		// no extension found ; skip detection
		return null;
	}

	public static DuplicateAccountDetectionActionEnum getSpidDetectionAction(
	    Context ctx, int spid)
	{
		CRMSpid serviceProvider = null;

		try
		{
			serviceProvider = SpidSupport.getCRMSpid(ctx, spid);
		}
		catch (HomeException exception)
		{
			LogSupport.minor(ctx, DuplicateAccountDetectionSupport.class,
			    "Exception caught: Cannot locate SPID " + spid, exception);
		}

		if (serviceProvider == null)
		{
			LogSupport.info(ctx, DuplicateAccountDetectionSupport.class,
			    "SPID " + spid + " not found");
			return null;
		}

		for (Object obj : serviceProvider.getExtensions())
		{
			Extension extension = (Extension) obj;
			if (extension instanceof DuplicateAccountDetectionSpidExtension)
			{
				DuplicateAccountDetectionSpidExtension ext =
				    (DuplicateAccountDetectionSpidExtension) extension;
				if (!ext.isEnabled())
				{
					return null;
				}
				return ext.getAction();
			}
		}

		// no extension found ; skip detection
		return null;
	}

	public static boolean isDuplicateAccountDetectionEnabled(Context ctx,
	    CRMSpid serviceProvider)
	{
		if (serviceProvider == null)
		{
			LogSupport.info(ctx, DuplicateAccountDetectionSupport.class,
			    "Service Provider was not provided.");
			return false;
		}

		for (Object obj : serviceProvider.getExtensions())
		{
			Extension extension = (Extension) obj;
			if (extension instanceof DuplicateAccountDetectionSpidExtension)
			{
				DuplicateAccountDetectionSpidExtension ext =
				    (DuplicateAccountDetectionSpidExtension) extension;
				return ext.isEnabled();
			}
		}

		// no extension found ; disable
		return false;
	}

	public static boolean isDuplicateAccountDetectionEnabled(Context ctx,
	    int spid)
	{
		CRMSpid serviceProvider = null;

		try
		{
			serviceProvider = SpidSupport.getCRMSpid(ctx, spid);
		}
		catch (HomeException exception)
		{
			LogSupport.minor(ctx, DuplicateAccountDetectionSupport.class,
			    "Exception caught: Cannot locate SPID " + spid, exception);
		}

		if (serviceProvider == null)
		{
			LogSupport.info(ctx, DuplicateAccountDetectionSupport.class,
			    "SPID " + spid + " not found");
			return false;
		}

		return isDuplicateAccountDetectionEnabled(ctx, serviceProvider);
	}

	/**
	 * Determines whether duplicate account detection should be performed at all
	 * for the currently logged in user.
	 * 
	 * @param ctx
	 *            Operating context.
	 * @return Whether duplicate account detection should be performed.
	 */
	public static boolean isDuplicateAccountDetectionRequired(Context ctx)
	{
		User principal = (User) ctx.get(Principal.class);
		boolean result = false;
		Set<Integer> spids = new HashSet<Integer>();
		if (principal != null)
		{
			spids.add(Integer.valueOf(principal.getSpid()));
			if (AuthSupport.hasPermission(ctx, new SimplePermission("spid.*")))
			{
				Home home = (Home) ctx.get(CRMSpidHome.class);
				SetBuildingVisitor set = new SetBuildingVisitor();
				FunctionVisitor visitor =
				    new FunctionVisitor(CRMSpidXInfo.ID, set);
				try
				{
					visitor = (FunctionVisitor) home.forEach(ctx, visitor);
				}
				catch (HomeException exception)
				{
					LogSupport
					    .minor(
					        ctx,
					        DuplicateAccountDetectionSupport.class,
					        "Exception caught while looking up SPIDs for current user",
					        exception);
				}
				if (set != null)
				{
					spids.addAll(set);
				}
			}

			for (Integer spid : spids)
			{

				if (isDuplicateAccountDetectionEnabled(ctx, spid.intValue()))
				{
					result = true;
					break;
				}
			}
		}
		return result;
	}

	/**
	 * Return the current SPID used for search. If the SPID is not specified in
	 * the criteria (such as in the case of first loading the Duplicate
	 * Detection screen), then the first available SPID of the currently logged
	 * in user is returned. If the SPID is specified in the criteria, it is
	 * still checked against the currently logged-in user's SPID setting.
	 * 
	 * @param context
	 *            The operating context.
	 * @param criteria
	 *            The criteria used in the search.
	 * @return The current SPID used for the search.
	 */
	public static CRMSpid getCurrentSearchSpid(final Context context,
	    DuplicateAccountDetectionForm criteria)
	{
		if (criteria == null)
		{
			return null;
		}

		Principal user = (Principal) context.get(Principal.class);
		Predicate p = null;
		if (criteria.getSpid() == DuplicateAccountDetectionForm.DEFAULT_SPID)
		{
			if (user == null)
			{
				LogSupport.info(context,
				    DuplicateAccountDetectionSearchAgentV2.class,
				    "There is no user responsible for this search, exiting.");
				return null;
			}
			p = (Predicate) True.instance();
		}
		else
		{
			p = new EQ(SpidAwareXInfo.SPID, criteria.getSpid());
		}
		Collection spids = null;
		try
		{
			spids =
			    new SortingHome(((Home) context.get(CRMSpidHome.class)).where(
			        context, new SpidAwareHomePredicate(user))).select(context,
			        p);
		}
		catch (HomeException exception)
		{
			LogSupport.minor(context,
			    DuplicateAccountDetectionSearchAgentV2.class,
			    "Exception caught", exception);
			return null;
		}

		if (spids == null)
		{
			LogSupport.info(context,
			    DuplicateAccountDetectionSearchAgentV2.class,
			    "No valid SPID found for user " + user.getName()
			        + " for this search, exiting.");
		}
		else if (spids.size() > 1)
		{
			LogSupport.info(context,
			    DuplicateAccountDetectionSearchAgentV2.class,
			    "More than one valid SPID found for user " + user.getName()
			        + " for this search, using the first available one.");
		}
		return (CRMSpid) spids.iterator().next();
	}

	/**
	 * Returns the duplicate check criteria field marker key.
	 * 
	 * @param str
	 *            Original name of the field.
	 * @return The duplicate check criteria field marker key.
	 */
	public static String getCriteriaCheckKey(String str)
	{
		if (str != null)
		{
			StringBuilder sb = new StringBuilder();
			sb.append(".duplicateCheck");
			if (!str.startsWith("."))
			{
				sb.append('.');
			}
			sb.append(str);
			return sb.toString();
		}
		return null;
	}
}
