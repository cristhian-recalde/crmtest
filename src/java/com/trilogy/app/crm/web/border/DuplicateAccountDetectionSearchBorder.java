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
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.trilogy.framework.xhome.beans.SafetyUtil;
import com.trilogy.framework.xhome.beans.xi.PropertyInfo;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.web.action.ActionMgr;
import com.trilogy.framework.xhome.web.agent.WebAgents;
import com.trilogy.framework.xhome.web.renderer.FormRenderer;
import com.trilogy.framework.xhome.web.search.SearchBorder;
import com.trilogy.framework.xhome.webcontrol.RequestServicer;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountXInfo;
import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.DuplicateAccountDetectionMethodEnum;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.bean.duplicatedetection.DuplicateAccountDetectionForm;
import com.trilogy.app.crm.bean.duplicatedetection.DuplicateAccountDetectionFormWebControl;
import com.trilogy.app.crm.bean.duplicatedetection.DuplicateAccountDetectionResult;
import com.trilogy.app.crm.duplicatedetection.DuplicateAccountDetectionCriteriaIdentificationGroupSettingAgent;
import com.trilogy.app.crm.duplicatedetection.DuplicateAccountDetectionSearchAgentV2;
import com.trilogy.app.crm.duplicatedetection.HTMLExceptionListenerErrorDisplayAgent;
import com.trilogy.app.crm.duplicatedetection.HTMLExceptionListenerSettingAgent;
import com.trilogy.app.crm.duplicatedetection.IgnoreWebCommandAgent;
import com.trilogy.app.crm.servlet.IgnoreParameterHttpServletRequestWrapper;
import com.trilogy.app.crm.support.DuplicateAccountDetectionSupport;

/**
 * Duplicate account search
 * 
 * @author cindy.wong@redknee.com
 * @since 8.5
 */
public class DuplicateAccountDetectionSearchBorder extends
    CustomizableSearchBorder
{
	/**
	 * Creates a new DuplicateAccountDetectionSearchBorder.
	 * 
	 * @param context
	 *            The operating context.
	 */
	public DuplicateAccountDetectionSearchBorder(final Context context)
	{
		super(context, DuplicateAccountDetectionResult.class,
		    new DuplicateAccountDetectionFormWebControl());

		/*
		 * Execution order:
		 * 1. Exception listener setting agent.
		 * 2. Search agent.
		 * 3. ID group setting agent.
		 * 4. Exception display agent.
		 * 5. New and copy command ignoring agent.
		 */
		addAgent(new IgnoreWebCommandAgent("New", "Copy"));
		addAgent(new HTMLExceptionListenerErrorDisplayAgent(
		    DuplicateAccountDetectionSearchAgentV2.DISPLAY_ERROR, true));
		addAgent(new DuplicateAccountDetectionCriteriaIdentificationGroupSettingAgent());
		addAgent(new DuplicateAccountDetectionSearchAgentV2());
		addAgent(new HTMLExceptionListenerSettingAgent(getClass()));
	}

	@Override
	public void service(Context ctx, HttpServletRequest req,
	    HttpServletResponse res, final RequestServicer delegate)
	    throws ServletException, IOException
	{
		// Ignores copy actions
		HttpServletRequest newReq =
		    new IgnoreParameterHttpServletRequestWrapper(req, "CMD", "New",
		        "Copy");
		Context subCtx = ctx.createSubContext();
		subCtx.put(HttpServletRequest.class, newReq);

		// Don't display actions for search results.
		ActionMgr.disableActions(subCtx);

		/*
		 * Use SearchBorder to display the form and result in the correct order.
		 */
		super.service(subCtx, newReq, res, delegate);
	}

	@Override
	protected void printFormBody(Context ctx, PrintWriter out, Object bean,
	    Context subCtx)
	{
		out.println("<tr><td>");
		webcontrol_.toWeb(subCtx, out, WebAgents.rewriteName(ctx, ".search"),
		    bean);
		out.println("</td></tr>");
	}

	@Override
	protected String[] getFlushLeftButtons(Context ctx)
	{
		return super.getFlushRightButtons(ctx);
	}

	@Override
	protected String[] getFlushRightButtons(Context ctx)
	{
		// Depends on DuplicateAccountDetectionSearchAgent to set this flag.
		if (ctx.getBoolean(
		    DuplicateAccountDetectionSearchAgentV2.DISPLAY_CONTINUE_BUTTON,
		    false))
		{
			return new String[]
			{
				"Continue"
			};
		}
		return super.getFlushLeftButtons(ctx);
	}

	@Override
	protected void printFormFooter(Context ctx, PrintWriter out,
	    FormRenderer frend)
	{
		/*
		 * Depends on DuplicateAccountDetectionSearchAgent to set this flag.
		 * Sets the hidden parameters.
		 */
		if (ctx.getBoolean(
		    DuplicateAccountDetectionSearchAgentV2.DISPLAY_CONTINUE_BUTTON,
		    false))
		{
			printHiddenInput(
			    out,
			    DuplicateAccountDetectionRedirectBorder.PARAMETER_DETECTION_PERFORMED,
			    Boolean.TRUE);
		}
		printHiddenInput(
		    out,
		    DuplicateAccountDetectionRedirectBorder.PARAMETER_IN_DETECTION_MODE,
		    Boolean.TRUE.toString());

		printCurrentValuesHiddenInput(ctx, out);
		printCopiedFieldsHiddenInput(ctx, out);
		printSearchCriteriaHiddenInput(ctx, out);
		super.printFormFooter(ctx, out, frend);
	}

	private void printSearchCriteriaHiddenInput(Context ctx, PrintWriter out)
	{
		DuplicateAccountDetectionForm criteria =
		    (DuplicateAccountDetectionForm) SearchBorder.getCriteria(ctx);
		if (criteria != null
		    && criteria.getSpid() != DuplicateAccountDetectionForm.DEFAULT_SPID)
		{
			CRMSpid spid =
			    DuplicateAccountDetectionSupport.getCurrentSearchSpid(ctx,
			        criteria);
			if (spid != null)
			{
				PropertyInfo[] properties = new PropertyInfo[0];
				DuplicateAccountDetectionMethodEnum method =
				    DuplicateAccountDetectionSupport.getSpidDetectionMethod(
				        ctx, spid.getSpid());
				if (SafetyUtil.safeEquals(criteria.getSystemType(),
				    SubscriberTypeEnum.PREPAID))
				{
					properties = new PropertyInfo[]
					{
					    AccountXInfo.SPID, AccountXInfo.SYSTEM_TYPE
					};
				}
				else if (SafetyUtil.safeEquals(method,
				    DuplicateAccountDetectionMethodEnum.ID))
				{
					properties =
					    new PropertyInfo[]
					    {
					        AccountXInfo.SPID, AccountXInfo.SYSTEM_TYPE,
					        AccountXInfo.IDENTIFICATION_GROUP_LIST
					    };
				}
				else if (SafetyUtil.safeEquals(method,
				    DuplicateAccountDetectionMethodEnum.NAME_DOB))
				{
					properties =
					    new PropertyInfo[]
					    {
					        AccountXInfo.SPID, AccountXInfo.SYSTEM_TYPE,
					        AccountXInfo.FIRST_NAME, AccountXInfo.LAST_NAME,
					        AccountXInfo.DATE_OF_BIRTH
					    };
				}

				for (PropertyInfo property : properties)
				{
					printHiddenInput(out,
					    DuplicateAccountDetectionSupport
					        .getCriteriaCheckKey(property.getName()),
					    Boolean.TRUE.toString());
				}
			}
		}
	}

	private void printCurrentValuesHiddenInput(Context ctx, PrintWriter out)
	{
		DuplicateAccountDetectionForm criteria =
		    (DuplicateAccountDetectionForm) SearchBorder.getCriteria(ctx);

		if (criteria != null
		    && criteria.getSpid() != DuplicateAccountDetectionForm.DEFAULT_SPID)
		{
			printHiddenInput(
			    out,
			    DuplicateAccountDetectionRedirectBorder.PARAMETER_PREVIOUS_DETECTION_SPID,
			    String.valueOf(criteria.getSpid()));
		}

		if (criteria != null && criteria.getSystemType() != null)
		{
			printHiddenInput(
			    out,
			    DuplicateAccountDetectionRedirectBorder.PARAMETER_PREVIOUS_BILLING_TYPE,
			    criteria.getSystemType().getIndex());
		}

	}

	private void printCopiedFieldsHiddenInput(Context ctx, PrintWriter out)
	{
		Account account =
		    (Account) ctx
		        .get(DuplicateAccountDetectionRedirectBorder.ACCOUNT_KEY);
		if (account != null)
		{
			DuplicateAccountDetectionRedirectBorder.getHidden_wc().toWeb(ctx,
			    out,
			    DuplicateAccountDetectionRedirectBorder.HIDDEN_ACCOUNT_NAME,
			    account);
		}
	}

	private void printHiddenInput(PrintWriter out, String name, Object value)
	{
		if (value != null)
		{
			out.print("<input name=\"");
			out.print(name);
			out.print("\" type=\"hidden\" value=\"");
			out.print(value);
			out.println("\"/>");
			out.flush();
		}
	}

}
