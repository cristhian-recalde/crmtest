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
package com.trilogy.app.crm.web.border;

import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.web.agent.WebAgents;
import com.trilogy.framework.xhome.web.renderer.ButtonRenderer;
import com.trilogy.framework.xhome.web.renderer.FormRenderer;
import com.trilogy.framework.xhome.web.search.SearchBorder;
import com.trilogy.framework.xhome.webcontrol.OutputWebControl;
import com.trilogy.framework.xhome.webcontrol.WebControl;

/**
 * This search border allows for more customization in the presentation of the
 * search form.
 * 
 * @author cindy.wong@redknee.com
 * @since 2010-09-27
 */
public class CustomizableSearchBorder extends SearchBorder
{

	public CustomizableSearchBorder(Context ctx, Class beanType,
	    WebControl webcontrol)
	{
		super(ctx, beanType, webcontrol);
	}

	public CustomizableSearchBorder(Context ctx, Object homeKey,
	    Class beanType, WebControl webcontrol)
	{
		super(ctx, homeKey, beanType, webcontrol);
	}

	/**
	 * Outputs the HTML form. This has been copied from the super class, but
	 * made more flexible to allow for easy insertion and modification of
	 * buttons.
	 * 
	 * @param ctx
	 *            Operating context.
	 * @param out
	 *            Output.
	 * @param frend
	 *            Form renderer.
	 * @param brend
	 *            Button renderer.
	 * @param bean
	 *            Bean to be printed.
	 * @param req
	 *            HTTP request.
	 * @see com.redknee.framework.xhome.web.search.SearchBorder#outputForm
	 */
	@Override
	public void outputForm(Context ctx, PrintWriter out, FormRenderer frend,
	    ButtonRenderer brend, Object bean, HttpServletRequest req)
	{
		Context subCtx = ctx.createSubContext();
		subCtx.put("MODE", OutputWebControl.EDIT_MODE);

		printFormHeader(ctx, out, frend);

		printFormBody(ctx, out, bean, subCtx);

		printFormButtons(ctx, out, brend);
		printFormFooter(ctx, out, frend);
	}

	protected void printFormFooter(Context ctx, PrintWriter out,
	    FormRenderer frend)
	{
		out.println("</table></center>");
		out.print("<input name=\"" + WebAgents.rewriteName(ctx, "SearchCMD.x")
		    + "\" type=\"hidden\" value=\"1\"/>");
		frend.FormEnd(out);
	}

	protected String[] getFlushLeftButtons(Context ctx)
	{
		return new String[] {};
	}

	protected void printFormButtons(Context ctx, PrintWriter out,
	    ButtonRenderer brend)
	{
		out.print("<tr><td><table dir=\"" + getDirection(ctx) + "\"><tr>");

		for (String button : getFlushLeftButtons(ctx))
		{
			out.print("<td>");
			brend.inputButton(out, ctx, button);
			out.print("</td>");
		}

		out.print("<td width=\"100%\">&nbsp;</td>");

		for (String button : getFlushRightButtons(ctx))
		{
			out.print("<td>");
			brend.inputButton(out, ctx, button);
			out.print("</td>");
		}

		out.println("</tr></table>");
		out.println("</td></tr>");
	}

	protected String[] getFlushRightButtons(Context ctx)
	{
		return new String[]
		{
		    "Search", "Clear"
		};
	}

	protected void printFormBody(Context ctx, PrintWriter out, Object bean,
	    Context subCtx)
	{
		out.println("<tr><td>");
		webcontrol_.toWeb(subCtx, out, WebAgents.rewriteName(ctx, ".search"),
		    bean);
		out.println("</td></tr>");
	}

	protected void printFormHeader(Context ctx, PrintWriter out,
	    FormRenderer frend)
	{
		frend.Form(out, ctx);
		out.println("<center><table>");
	}
}
