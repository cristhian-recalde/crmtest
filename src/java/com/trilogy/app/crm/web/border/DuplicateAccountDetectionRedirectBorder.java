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

package com.trilogy.app.crm.web.border;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

import com.trilogy.framework.xhome.beans.SafetyUtil;
import com.trilogy.framework.xhome.beans.XBeans;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.web.border.Border;
import com.trilogy.framework.xhome.web.xmenu.service.XMenuService;
import com.trilogy.framework.xhome.webcontrol.RequestServicer;
import com.trilogy.framework.xhome.webcontrol.WebControl;
import com.trilogy.framework.xhome.webcontrol.WebController;
import com.trilogy.framework.xlog.log.LogSupport;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.DuplicateAccountDetectionMethodEnum;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.bean.duplicatedetection.DuplicateAccountDetectionForm;
import com.trilogy.app.crm.bean.duplicatedetection.DuplicateAccountDetectionFormWebControl;
import com.trilogy.app.crm.support.DuplicateAccountDetectionSupport;
import com.trilogy.app.crm.web.control.HiddenAccountWebControl;

/**
 * A border to change the behaviour of "New" and "Copy" buttons on the account
 * screen to redirect them to duplicate check screen.
 * 
 * @author cindy.wong@redknee.com
 * @since 8.5
 */
public class DuplicateAccountDetectionRedirectBorder implements Border
{

	public static final String PARAMETER_DETECTION_PERFORMED =
	    "duplicateDetectionPerformed";
	public static final String PARAMETER_IN_DETECTION_MODE =
	    "inDuplicateDetection";
	public static final String ACCOUNT_KEY =
	    "DuplicateAccountDetectionRedirectBorder.Account";
	public static final String HIDDEN_ACCOUNT_NAME = ".hidden";
	public static final String PARAMETER_PREVIOUS_DETECTION_SPID =
	    "duplicateDetectionPreviousSpid";
	public static final String PARAMETER_PREVIOUS_BILLING_TYPE =
	    "duplicateDetectionPreviousBillingType";

	public DuplicateAccountDetectionRedirectBorder(String servicerKey)
	{
		super();
		servicerKey_ = servicerKey;
	}

	@Override
	public void service(Context ctx, HttpServletRequest req,
	    HttpServletResponse res, RequestServicer delegate)
	    throws ServletException, IOException
	{
		Context subCtx =
		    ctx.createSubContext("DuplicateAccountDetectionRedirectBorder-context");
		boolean isNew = WebController.isCmd("New", req);
		boolean isCopy = WebController.isCmd("Copy", req);
		boolean isInDetectionMode =
		    SafetyUtil.safeEquals(
		        req.getParameter(PARAMETER_IN_DETECTION_MODE),
		        Boolean.TRUE.toString());

		boolean isDetectionPerformed = WebController.isCmd("Continue", req);
		boolean redirect = false;

		String prevSpidStr =
		    req.getParameter(PARAMETER_PREVIOUS_DETECTION_SPID);
		int prevSpid = DuplicateAccountDetectionForm.DEFAULT_SPID;
		if (prevSpidStr != null && !prevSpidStr.isEmpty())
		{
			try
			{
				prevSpid = Integer.parseInt(prevSpidStr);
			}
			catch (NumberFormatException exception)
			{
				// noop
			}
		}
		String prevBillingTypeStr =
		    req.getParameter(PARAMETER_PREVIOUS_BILLING_TYPE);
		int prevBillingType = SubscriberTypeEnum.PREPAID_INDEX;
		if (prevBillingTypeStr != null && !prevBillingTypeStr.isEmpty())
		{
			try
			{
				prevBillingType = Integer.parseInt(prevBillingTypeStr);
			}
			catch (NumberFormatException exception)
			{
				// noop
			}
		}
		subCtx.put(PARAMETER_PREVIOUS_DETECTION_SPID, prevSpid);
		subCtx.put(PARAMETER_PREVIOUS_BILLING_TYPE, prevBillingType);
		subCtx.put(PARAMETER_DETECTION_PERFORMED, isDetectionPerformed);
		subCtx.put(PARAMETER_IN_DETECTION_MODE, isInDetectionMode);

		if (isDetectionPerformed)
		{
			redirect = false;
		}
		else if (isInDetectionMode)
		{
			redirect = true;
		}
		else if (isNew || isCopy)
		{
			redirect =
			    DuplicateAccountDetectionSupport
			        .isDuplicateAccountDetectionRequired(ctx);
		}

		if (redirect)
		{
			HttpServletRequest newReq = req;
			Account account;

			try
			{
				account = (Account) XBeans.instantiate(Account.class, subCtx);
			}
			catch (Exception exception)
			{
				LogSupport
				    .minor(subCtx, this,
				        "Failed to instantiate an account using XBeans.  Using account constructor.");
				account = new Account();
			}

			if (isCopy)
			{
				hidden_wc.fromWeb(subCtx, account, req, "");
				subCtx.put(ACCOUNT_KEY, account);
				subCtx
				    .put(PARAMETER_PREVIOUS_DETECTION_SPID, account.getSpid());
				subCtx.put(PARAMETER_PREVIOUS_BILLING_TYPE, ((int) account
				    .getSystemType().getIndex()));

				newReq = new HttpServletRequestWrapper(req)
				{
					@Override
					public String getParameter(String key)
					{
						if (key != null && key.startsWith(".search"))
						{
							String newKey = key.replaceFirst("\\.search", "");
							return super.getParameter(newKey);
						}
						if ("key".equals(key))
						{
							return null;
						}
						return super.getParameter(key);
					}
				};
			}
			else if (isInDetectionMode)
			{
				hidden_wc.fromWeb(subCtx, account, req, ".hidden");
				DuplicateAccountDetectionForm form =
				    (DuplicateAccountDetectionForm) form_wc.fromWeb(subCtx,
				        req, ".search");
				if (form != null)
				{
					copyFormFieldsToAccount(subCtx, form, account);
				}
				subCtx.put(ACCOUNT_KEY, account);
			}

			XMenuService srv = (XMenuService) ctx.get(XMenuService.class);
			RequestServicer search = srv.getServicer(ctx, servicerKey_);
			subCtx.put(HttpServletRequest.class, newReq);
			search.service(subCtx, newReq, res);
		}
		else
		{
			HttpServletRequest newReq = req;
			if (isDetectionPerformed)
			{
				newReq = new HttpServletRequestWrapper(req)
				{
					@Override
					public String getParameter(String key)
					{
						if (key.equals("CMD"))
						{
							return "New";
						}
						else
						{
							String value = super.getParameter(key);
							if (value == null || value.isEmpty())
							{
								value = super.getParameter(".search" + key);
							}
							if (value == null || value.isEmpty())
							{
								return super.getParameter(".hidden" + key);
							}
							return value;
						}
					}
				};
			}
			delegate.service(ctx, newReq, res);
		}
	}

	private void copyFormFieldsToAccount(Context context,
	    DuplicateAccountDetectionForm form, Account account)
	{
		DuplicateAccountDetectionMethodEnum method =
		    DuplicateAccountDetectionSupport.getSpidDetectionMethod(context,
		        form.getSpid());

		if (SafetyUtil.safeEquals(method,
		    DuplicateAccountDetectionMethodEnum.NAME_DOB))
		{
			account.setFirstName(form.getFirstName());
			account.setLastName(form.getLastName());
			account.setDateOfBirth(form.getDateOfBirth());
		}
		else if (SafetyUtil.safeEquals(method,
		    DuplicateAccountDetectionMethodEnum.ID))
		{
			account.setIdentificationGroupList(form
			    .getIdentificationGroupList());
		}
	}

	public static WebControl getHidden_wc()
	{
		return hidden_wc;
	}

	private final String servicerKey_;
	private static final WebControl hidden_wc = new HiddenAccountWebControl();
	private final WebControl form_wc =
	    new DuplicateAccountDetectionFormWebControl();

}
