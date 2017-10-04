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
package com.trilogy.app.crm.web.control;

import java.io.PrintWriter;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.language.MessageMgr;
import com.trilogy.framework.xhome.web.action.ActionMgr;
import com.trilogy.framework.xhome.web.action.WebActionSupport;
import com.trilogy.framework.xhome.web.renderer.ButtonRenderer;
import com.trilogy.framework.xhome.web.renderer.DefaultButtonRenderer;
import com.trilogy.framework.xhome.web.renderer.TableRenderer;
import com.trilogy.framework.xhome.web.support.WebSupport;
import com.trilogy.framework.xhome.web.util.Link;
import com.trilogy.framework.xhome.webcontrol.ViewModeEnum;
import com.trilogy.framework.xhome.webcontrol.WebControl;

import com.trilogy.app.crm.bean.duplicatedetection.DuplicateAccountDetectionResult;
import com.trilogy.app.crm.bean.duplicatedetection.DuplicateAccountDetectionResultIdentitySupport;
import com.trilogy.app.crm.bean.duplicatedetection.DuplicateAccountDetectionResultTableWebControl;
import com.trilogy.app.crm.bean.duplicatedetection.DuplicateAccountDetectionResultXInfo;

/**
 * Custom table web control to disable the reordering of duplicate detection
 * result.
 * 
 * @author cindy.wong@redknee.com
 * @since 2010-12-23
 */
public class CustomDuplicateAccountDetectionResultTableWebControl extends
    DuplicateAccountDetectionResultTableWebControl
{

	protected static final WebControl CUSTOM_IDENTIFICATIONS_WC =
	    new CustomDuplicateAccountDetectionIdentificationResultTableWebControl();
	protected static final WebControl CUSTOM_SUBSCRIPTIONS_WC =
	    new CustomDuplicateAccountDetectionSubscriptionResultTableWebControl();

	@Override
	public WebControl getIdentificationsWebControl()
	{
		return CUSTOM_IDENTIFICATIONS_WC;
	}

	@Override
	public WebControl getSubscriptionsWebControl()
	{
		return CUSTOM_SUBSCRIPTIONS_WC;
	}

	@Override
	public void toWeb(Context ctx, PrintWriter out, String name, Object obj)
	{

		Context subCtx = ctx.createSubContext();
		Context secureCtx = subCtx;
		MessageMgr mmgr = new MessageMgr(ctx, this);

		// In table mode so set the TABLE_MODE to true. Used by individual web
		// controls
		subCtx.put("TABLE_MODE", true);

		int mode = ctx.getInt("MODE", DISPLAY_MODE);

		if (mode != DISPLAY_MODE)
		{
			secureCtx = subCtx.createSubContext();
			secureCtx.put("MODE", DISPLAY_MODE);
		}

		HttpServletRequest req =
		    (HttpServletRequest) ctx.get(HttpServletRequest.class);
		int blanks = ctx.getInt(NUM_OF_BLANKS, DEFAULT_BLANKS);
		Collection beans = (Collection) obj;
		TableRenderer renderer = tableRenderer(ctx);
		// get the list of common actions
		List actions = ActionMgr.getActions(ctx);

		// The check for ACTIONS is for legacy support and should be removed at
		// some point
		boolean show_actions =
		    ctx.getBoolean("ACTIONS", true) && ActionMgr.isEnabled(actions);

		// don't propogate ACTIONS to sub-controls
		if (show_actions)
		{
			ActionMgr.disableActions(subCtx);
		}

		if (mode == EDIT_MODE || mode == CREATE_MODE)
		{
			// The Math.max() bit is so that if blanks is set to 0 that you can
			// still add a row
			out.print("<input type=\"hidden\" name=\"" + name + SEPERATOR
			    + "_count\" value=\"" + (beans.size() + Math.max(1, blanks))
			    + "\" />");
			if (ctx
			    .getBoolean(com.redknee.framework.xhome.web.Constants.TABLEWEBCONTROL_REORDER_KEY))
				out.print("<input type=\"hidden\" name=\"" + name + SEPERATOR
				    + "_REORDER_KEY\" value=\"1\" />");
			else
				out.print("<input type=\"hidden\" name=\"" + name + SEPERATOR
				    + "_REORDER_KEY\" value=\"0\" />");
		}

		// WIDHT=722

		renderer.Table(ctx, out, mmgr.get("DuplicateAccountDetectionResult.Label",
		    DuplicateAccountDetectionResultXInfo.Label));

		out.println("<tr>");

		if (mode == EDIT_MODE || mode == CREATE_MODE)
		{

			if (ctx
			    .getBoolean(com.redknee.framework.xhome.web.Constants.TABLEWEBCONTROL_REORDER_KEY))
			{
				// this is for the up/down arrows
				out.print("<th>&nbsp;</th>");
			}

			// this is for the checkbox, only if dynamic table update is not
			// enabled
			if (!ctx.getBoolean(ENABLE_ADDROW_BUTTON))
				out.print("<th>&nbsp;</th>");
		}

		Link link = null;

		// default
		String img_src = "";

		ViewModeEnum ban_mode =
		    getMode(subCtx, "DuplicateAccountDetectionResult.ban");
		ViewModeEnum firstName_mode =
		    getMode(subCtx, "DuplicateAccountDetectionResult.firstName");
		ViewModeEnum lastName_mode =
		    getMode(subCtx, "DuplicateAccountDetectionResult.lastName");
		ViewModeEnum dateOfBirth_mode =
		    getMode(subCtx, "DuplicateAccountDetectionResult.dateOfBirth");
		ViewModeEnum identifications_mode =
		    getMode(subCtx, "DuplicateAccountDetectionResult.identifications");
		ViewModeEnum address_mode =
		    getMode(subCtx, "DuplicateAccountDetectionResult.address");
		ViewModeEnum city_mode =
		    getMode(subCtx, "DuplicateAccountDetectionResult.city");
		ViewModeEnum accountType_mode =
		    getMode(subCtx, "DuplicateAccountDetectionResult.accountType");
		ViewModeEnum accountState_mode =
		    getMode(subCtx, "DuplicateAccountDetectionResult.accountState");
		ViewModeEnum subscriptions_mode =
		    getMode(subCtx, "DuplicateAccountDetectionResult.subscriptions");
		if (ban_mode != ViewModeEnum.NONE)
		{
			out.println("<th TITLE=\""
			    + mmgr.get("DuplicateAccountDetectionResult.ban.Label",
			        DuplicateAccountDetectionResultXInfo.BAN.getLabel(ctx))
			    + "\" >");
			out.println(mmgr.get(
			    "DuplicateAccountDetectionResult.ban.ColumnLabel",
			    DuplicateAccountDetectionResultXInfo.BAN.getColumnLabel(ctx)));

			out.println("</th>");

		}

		if (firstName_mode != ViewModeEnum.NONE)
		{
			out.println("<th TITLE=\""
			    + mmgr
			        .get(
			            "DuplicateAccountDetectionResult.firstName.Label",
			            DuplicateAccountDetectionResultXInfo.FIRST_NAME.getLabel(ctx))
			    + "\" >");
			out.println(
			    mmgr.get(
			        "DuplicateAccountDetectionResult.firstName.ColumnLabel",
			        DuplicateAccountDetectionResultXInfo.FIRST_NAME.getColumnLabel(ctx)));

			out.println("</th>");

		}

		if (lastName_mode != ViewModeEnum.NONE)
		{
			out.println("<th TITLE=\""
			    + mmgr
			        .get(
			            "DuplicateAccountDetectionResult.lastName.Label",
			            DuplicateAccountDetectionResultXInfo.LAST_NAME.getLabel(ctx))
			    + "\" >");
			out.println(mmgr
			    .get(
			        "DuplicateAccountDetectionResult.lastName.ColumnLabel",
			        DuplicateAccountDetectionResultXInfo.LAST_NAME.getColumnLabel(ctx)));

			out.println("</th>");

		}

		if (dateOfBirth_mode != ViewModeEnum.NONE)
		{
			out.println("<th TITLE=\""
			    + mmgr
			        .get(
			            "DuplicateAccountDetectionResult.dateOfBirth.Label",
			            DuplicateAccountDetectionResultXInfo.DATE_OF_BIRTH.getLabel(ctx))
			    + "\" >");
			out.println(
			    mmgr.get(
			        "DuplicateAccountDetectionResult.dateOfBirth.ColumnLabel",
			        DuplicateAccountDetectionResultXInfo.DATE_OF_BIRTH.getColumnLabel(ctx)));

			out.println("</th>");

		}

		if (identifications_mode != ViewModeEnum.NONE)
		{
			out.println("<th TITLE=\""
			    + mmgr
			        .get(
			            "DuplicateAccountDetectionResult.identifications.Label",
			            DuplicateAccountDetectionResultXInfo.IDENTIFICATIONS.getLabel(ctx))
			    + "\" >");
			out.println(mmgr
			    .get(
			        "DuplicateAccountDetectionResult.identifications.ColumnLabel",
			        DuplicateAccountDetectionResultXInfo.IDENTIFICATIONS.getColumnLabel(ctx)));

			out.println("</th>");

		}

		if (address_mode != ViewModeEnum.NONE)
		{
			out.println("<th TITLE=\""
			    + mmgr
			        .get(
			            "DuplicateAccountDetectionResult.address.Label",
			            DuplicateAccountDetectionResultXInfo.ADDRESS.getLabel(ctx))
			    + "\" >");
			out.println(
			    mmgr.get(
			        "DuplicateAccountDetectionResult.address.ColumnLabel",
			        DuplicateAccountDetectionResultXInfo.ADDRESS.getColumnLabel(ctx)));

			out.println("</th>");

		}

		if (city_mode != ViewModeEnum.NONE)
		{
			out.println("<th TITLE=\""
			    + mmgr.get("DuplicateAccountDetectionResult.city.Label",
			        DuplicateAccountDetectionResultXInfo.CITY.getLabel(ctx))
			    + "\" >");
			out.println(mmgr.get(
			    "DuplicateAccountDetectionResult.city.ColumnLabel",
			    DuplicateAccountDetectionResultXInfo.CITY.getColumnLabel(ctx)));

			out.println("</th>");

		}

		if (accountType_mode != ViewModeEnum.NONE)
		{
			out.println("<th TITLE=\""
			    + mmgr
			        .get(
			            "DuplicateAccountDetectionResult.accountType.Label",
			            DuplicateAccountDetectionResultXInfo.ACCOUNT_TYPE.getLabel(ctx))
			    + "\" >");
			out.println(mmgr
			    .get(
			        "DuplicateAccountDetectionResult.accountType.ColumnLabel",
			        DuplicateAccountDetectionResultXInfo.ACCOUNT_TYPE.getColumnLabel(ctx)));

			out.println("</th>");

		}

		if (accountState_mode != ViewModeEnum.NONE)
		{
			out.println("<th TITLE=\""
			    + mmgr
			        .get(
			            "DuplicateAccountDetectionResult.accountState.Label",
			            DuplicateAccountDetectionResultXInfo.ACCOUNT_STATE.getLabel(ctx))
			    + "\" >");
			out.println(mmgr
			    .get(
			        "DuplicateAccountDetectionResult.accountState.ColumnLabel",
			        DuplicateAccountDetectionResultXInfo.ACCOUNT_STATE.getColumnLabel(ctx)));

			out.println("</th>");

		}

		if (subscriptions_mode != ViewModeEnum.NONE)
		{
			out.println("<th TITLE=\""
			    + mmgr
			        .get(
			            "DuplicateAccountDetectionResult.subscriptions.Label",
			            DuplicateAccountDetectionResultXInfo.SUBSCRIPTIONS.getLabel(ctx))
			    + "\" >");
			out.println(mmgr
			    .get(
			        "DuplicateAccountDetectionResult.subscriptions.ColumnLabel",
			        DuplicateAccountDetectionResultXInfo.SUBSCRIPTIONS.getColumnLabel(ctx)));

			out.println("</th>");

		}

		if (show_actions)
		{
			// out.println("<th>Actions</th>");
			out.println("<th>");
			out.println(mmgr.get("SummaryTable.Actions.Label", "Actions"));
			out.println("</th>");
		}

		out.println("</tr>");

		Iterator i = beans.iterator();
		final int count =
		    beans.size()
		        + (((mode == EDIT_MODE || mode == CREATE_MODE) && !ctx
		            .getBoolean(ENABLE_ADDROW_BUTTON)) ? Math.max(0, blanks)
		            : 0);
		int rowStart = 0;

		if ((mode == EDIT_MODE || mode == CREATE_MODE) && !ctx.has(DISABLE_NEW)
		    && ctx.getBoolean(ENABLE_ADDROW_BUTTON))
		{
			out.print("<tr style=\"display:none\">");

			// For the down only arrow-set
			renderer.TD(ctx,out);
			out.println("<table cellpadding=\"0\" cellspacing=\"0\" border=\"0\"><tr><td>");
			out.println("<img src=\"/images/list/up-dark.gif\"></img>");
			out.println("</td></tr><tr><td>");
			out.println("<img onclick=\"swapTableLines(this,-2,-1,'" + name
			    + "','" + WebSupport.fieldToId(ctx, name)
			    + "');\" src=\"/images/list/down.gif\"></img>");
			out.println("</td></tr></table>");
			renderer.TDEnd(ctx,out);

			// For the up arrow-set
			renderer.TD(ctx,out);
			out.println("<table cellpadding=\"0\" cellspacing=\"0\" border=\"0\"><tr><td>");
			out.println("<img onclick=\"swapTableLines(this,-2,-3,'" + name
			    + "','" + WebSupport.fieldToId(ctx, name)
			    + "');\" src=\"/images/list/up.gif\"></img>");
			out.println("</td></tr><tr><td>");
			out.println("<img src=\"/images/list/down-dark.gif\"></img>");
			out.println("</td></tr></table>");
			renderer.TDEnd(ctx,out);

			// For the bi-directional only arrow-set
			renderer.TD(ctx,out);
			out.println("<table cellpadding=\"0\" cellspacing=\"0\" border=\"0\"><tr><td>");
			out.println("<img onclick=\"swapTableLines(this,-2,-3,'" + name
			    + "','" + WebSupport.fieldToId(ctx, name)
			    + "');\" src=\"/images/list/up.gif\"></img>");
			out.println("</td></tr><tr><td>");
			out.println("<img onclick=\"swapTableLines(this,-2,-1,'" + name
			    + "','" + WebSupport.fieldToId(ctx, name)
			    + "');\" src=\"/images/list/down.gif\"></img>");
			out.println("</td></tr></table>");
			renderer.TDEnd(ctx,out);

			// For both black arrows
			renderer.TD(ctx,out);
			out.println("<table cellpadding=\"0\" cellspacing=\"0\" border=\"0\"><tr><td>");
			out.println("<img src=\"/images/list/up-dark.gif\"></img>");
			out.println("</td></tr><tr><td>");
			out.println("<img src=\"/images/list/down-dark.gif\"></img>");
			out.println("</td></tr></table>");
			renderer.TDEnd(ctx,out);

			out.print("</tr>");
			rowStart = -1;
		}

		final int start = rowStart;

		for (int j = start; j < count; j++)
		{
			DuplicateAccountDetectionResult bean;

			boolean b = true;
			if (j > -1 && j < beans.size())
			{
				bean = (DuplicateAccountDetectionResult) i.next();
				b = true;
			}
			else
			{
				bean = new DuplicateAccountDetectionResult();
				b = false;
			}

			/*
			 * if (j < 0)
			 * {
			 * out.print("<tr style=\"display:none\">");
			 * }
			 * else
			 * {
			 */
			renderer.TR(ctx, out, bean, j);
			// }

			// icons for up/down
			if (mode == EDIT_MODE || mode == CREATE_MODE)
			{
				if (ctx
				    .getBoolean(com.redknee.framework.xhome.web.Constants.TABLEWEBCONTROL_REORDER_KEY))
				{
					renderer.TD(ctx,out);
					out.println("<table cellpadding=\"0\" cellspacing=\"0\" border=\"0\"><tr><td>");
					if (ctx.getBoolean(ENABLE_ADDROW_BUTTON)
					    && beans.size() == 1 && j != -1)
					{
						// For both black arrows when 1)Normal: NOWAY...
						// 2)Dynamic -- displaying the only one row in table
						// (not the hidden row)
						out.println("<img src=\"/images/list/up-dark.gif\"></img>");
						out.println("</td></tr><tr><td>");
						out.println("<img src=\"/images/list/down-dark.gif\"></img>");
					}
					else if (j == 0)
					{
						out.println("<img src=\"/images/list/up-dark.gif\"></img>");
						out.println("</td></tr><tr><td>");
						// out.println("<img onclick=\"swapTableLines('"+name +
						// "','" +
						// SEPERATOR+"',"+j+","+(j+1)+",new Array('_enabled','ban','firstName','lastName','dateOfBirth','address','city','accountType','accountState','identifications','subscriptions'));\" src=\"/images/list/down.gif\"></img>");
						out.println("<img onclick=\"swapTableLines(this," + j
						    + "," + (j + 1) + ",'" + name + "','"
						    + WebSupport.fieldToId(ctx, name)
						    + "');\" src=\"/images/list/down.gif\"></img>");
					}
					else if ((j == count - 1) && (j != -1))
					{
						out.println("<img onclick=\"swapTableLines(this," + j
						    + "," + (j - 1) + ",'" + name + "','"
						    + WebSupport.fieldToId(ctx, name)
						    + "');\" src=\"/images/list/up.gif\"></img>");
						out.println("</td></tr><tr><td>");
						out.println("<img src=\"/images/list/down-dark.gif\"></img>");
					}
					else
					{
						out.println("<img onclick=\"swapTableLines(this," + j
						    + "," + (j - 1) + ",'" + name + "','"
						    + WebSupport.fieldToId(ctx, name)
						    + "');\" src=\"/images/list/up.gif\"></img>");
						// out.println("<img onclick=\"swapTableLines('"+name +
						// "','" +
						// SEPERATOR+"',"+j+","+(j-1)+",new Array('_enabled','ban','firstName','lastName','dateOfBirth','address','city','accountType','accountState','identifications','subscriptions'));\" src=\"/images/list/up.gif\"></img>");
						out.println("</td></tr><tr><td>");
						out.println("<img onclick=\"swapTableLines(this," + j
						    + "," + (j + 1) + ",'" + name + "','"
						    + WebSupport.fieldToId(ctx, name)
						    + "');\" src=\"/images/list/down.gif\"></img>");
						// out.println("<img onclick=\"swapTableLines('"+name +
						// "','" +
						// SEPERATOR+"',"+j+","+(j+1)+",new Array('_enabled','ban','firstName','lastName','dateOfBirth','address','city','accountType','accountState','identifications','subscriptions'));\" src=\"/images/list/down.gif\"></img>");
					}
					out.println("</td></tr></table>");
					renderer.TDEnd(ctx,out);
				}
			}

			// checkbox: shwon only in edit/create mode and the "AddRow" button
			// is disable
			if ((mode == EDIT_MODE || mode == CREATE_MODE)
			    && !ctx.getBoolean(ENABLE_ADDROW_BUTTON))
			{
				outputCheckBox(ctx, out, name + SEPERATOR + j, bean, b);
			}

			subCtx.put(BEAN, bean);

			if (ban_mode != ViewModeEnum.NONE)
			{

				renderer.TD(ctx,out);

				// Some nested WebControls may want to know about the particular
				// bean property they are dealing with.
				subCtx.put(PROPERTY, DuplicateAccountDetectionResultXInfo.BAN);
				getBanWebControl().toWeb(
				    (ban_mode == ViewModeEnum.READ_ONLY) ? secureCtx : subCtx,
				    out, name + SEPERATOR + j + SEPERATOR + "ban",
				    bean.getBan());
				renderer.TDEnd(ctx,out);

			}

			if (firstName_mode != ViewModeEnum.NONE)
			{

				renderer.TD(ctx,out);

				// Some nested WebControls may want to know about the particular
				// bean property they are dealing with.
				subCtx.put(PROPERTY,
				    DuplicateAccountDetectionResultXInfo.FIRST_NAME);
				getFirstNameWebControl().toWeb(
				    (firstName_mode == ViewModeEnum.READ_ONLY) ? secureCtx
				        : subCtx, out,
				    name + SEPERATOR + j + SEPERATOR + "firstName",
				    bean.getFirstName());
				renderer.TDEnd(ctx,out);

			}

			if (lastName_mode != ViewModeEnum.NONE)
			{

				renderer.TD(ctx,out);

				// Some nested WebControls may want to know about the particular
				// bean property they are dealing with.
				subCtx.put(PROPERTY,
				    DuplicateAccountDetectionResultXInfo.LAST_NAME);
				getLastNameWebControl().toWeb(
				    (lastName_mode == ViewModeEnum.READ_ONLY) ? secureCtx
				        : subCtx, out,
				    name + SEPERATOR + j + SEPERATOR + "lastName",
				    bean.getLastName());
				renderer.TDEnd(ctx,out);

			}

			if (dateOfBirth_mode != ViewModeEnum.NONE)
			{

				renderer.TD(ctx,out);

				// Some nested WebControls may want to know about the particular
				// bean property they are dealing with.
				subCtx.put(PROPERTY,
				    DuplicateAccountDetectionResultXInfo.DATE_OF_BIRTH);
				getDateOfBirthWebControl().toWeb(
				    (dateOfBirth_mode == ViewModeEnum.READ_ONLY) ? secureCtx
				        : subCtx, out,
				    name + SEPERATOR + j + SEPERATOR + "dateOfBirth",
				    bean.getDateOfBirth());
				renderer.TDEnd(ctx,out);

			}

			if (identifications_mode != ViewModeEnum.NONE)
			{

				renderer.TD(ctx,out);

				subCtx.put(PROPERTY, null);
				getIdentificationsWebControl()
				    .toWeb(
				        (identifications_mode == ViewModeEnum.READ_ONLY) ? secureCtx
				            : subCtx, out,
				        name + SEPERATOR + j + SEPERATOR + "identifications",
				        bean.getIdentifications());
				renderer.TDEnd(ctx,out);

			}

			if (address_mode != ViewModeEnum.NONE)
			{

				renderer.TD(ctx,out);

				// Some nested WebControls may want to know about the particular
				// bean property they are dealing with.
				subCtx.put(PROPERTY,
				    DuplicateAccountDetectionResultXInfo.ADDRESS);
				getAddressWebControl().toWeb(
				    (address_mode == ViewModeEnum.READ_ONLY) ? secureCtx
				        : subCtx, out,
				    name + SEPERATOR + j + SEPERATOR + "address",
				    bean.getAddress());
				renderer.TDEnd(ctx,out);

			}

			if (city_mode != ViewModeEnum.NONE)
			{

				renderer.TD(ctx,out);

				// Some nested WebControls may want to know about the particular
				// bean property they are dealing with.
				subCtx.put(PROPERTY, DuplicateAccountDetectionResultXInfo.CITY);
				getCityWebControl().toWeb(
				    (city_mode == ViewModeEnum.READ_ONLY) ? secureCtx : subCtx,
				    out, name + SEPERATOR + j + SEPERATOR + "city",
				    bean.getCity());
				renderer.TDEnd(ctx,out);

			}

			if (accountType_mode != ViewModeEnum.NONE)
			{

				renderer.TD(ctx,out);

				// Some nested WebControls may want to know about the particular
				// bean property they are dealing with.
				subCtx.put(PROPERTY,
				    DuplicateAccountDetectionResultXInfo.ACCOUNT_TYPE);
				getAccountTypeWebControl().toWeb(
				    (accountType_mode == ViewModeEnum.READ_ONLY) ? secureCtx
				        : subCtx, out,
				    name + SEPERATOR + j + SEPERATOR + "accountType",
				    Long.valueOf(bean.getAccountType()));
				renderer.TDEnd(ctx,out);

			}

			if (accountState_mode != ViewModeEnum.NONE)
			{

				renderer.TD(ctx,out);

				// Some nested WebControls may want to know about the particular
				// bean property they are dealing with.
				subCtx.put(PROPERTY,
				    DuplicateAccountDetectionResultXInfo.ACCOUNT_STATE);
				getAccountStateWebControl().toWeb(
				    (accountState_mode == ViewModeEnum.READ_ONLY) ? secureCtx
				        : subCtx, out,
				    name + SEPERATOR + j + SEPERATOR + "accountState",
				    bean.getAccountState());
				renderer.TDEnd(ctx,out);

			}

			if (subscriptions_mode != ViewModeEnum.NONE)
			{

				renderer.TD(ctx,out);

				subCtx.put(PROPERTY, null);
				getSubscriptionsWebControl().toWeb(
				    (subscriptions_mode == ViewModeEnum.READ_ONLY) ? secureCtx
				        : subCtx, out,
				    name + SEPERATOR + j + SEPERATOR + "subscriptions",
				    bean.getSubscriptions());
				renderer.TDEnd(ctx,out);

			}

			if (show_actions)
			{
				List beanActions = ActionMgr.getActions(ctx, bean);
				if (beanActions == null)
				{
					// use the backup actions from the home level
					beanActions = actions;
				}
				((WebActionSupport) ctx.get(WebActionSupport.class))
				    .writeLinks(subCtx, beanActions, out, bean,
				        DuplicateAccountDetectionResultIdentitySupport
				            .instance());
			}

			if ((mode == EDIT_MODE || mode == CREATE_MODE)
			    && !ctx.getBoolean(DISABLE_NEW)
			    && ctx.getBoolean(ENABLE_ADDROW_BUTTON))
			{
				out.println("<TD>");
				out.println("<img onclick=\"removeRow(this,'"
				    + name
				    + "');\" src=\"ButtonRenderServlet?.src=abc &.label=Delete\"/>");
				// out.println("<input type=\"button\" name=\"Delete\" value=\"Delete\" onclick=\"removeRow(this,'"+name+"');\" />");
				out.println("</TD>");
			}
			renderer.TREnd(ctx, out);
		}
		if ((mode == EDIT_MODE || mode == CREATE_MODE)
		    && ctx.getBoolean(ENABLE_ADDROW_BUTTON)
		    && !ctx.getBoolean(DISABLE_NEW))
		{
			ButtonRenderer br =
			    (ButtonRenderer) ctx.get(ButtonRenderer.class,
			        DefaultButtonRenderer.instance());
			out.println("<tr><td colspan=4>");

			out.println("<table cellpadding=\"0\" cellspacing=\"0\" border=\"0\"><tr><td>");

			out.println("<input type=\"text\" name=\"" + name
			    + ".addRowCount\" size=\"3\" value=\"1\"/></td><td>");
			out.println("<img onclick=\"addRow(this,'" + name + "','"
			    + WebSupport.fieldToId(ctx, name)
			    + "');\" src=\"ButtonRenderServlet?.src=abc &.label=Add\"/>");
			out.println("</td></tr></table>");
			out.println("</td></tr>");
			// br.inputButton(out, ctx, WebController.class, "Help", false,
			// "addRow(this,'"+name+"')");
			// out.println("</td>");
			// out.println("<tr ><td><input type=\"button\" value=\"Add Row\" onclick=\"addRow(this,'"+name+"');\" /></td><td><input type=\"text\" name=\""+name+".addRowCount\" value=\"1\"/></td></tr>");
		}

		renderer.TableEnd(ctx, out, (String) ctx.get(FOOTER));

		outputNewButton(ctx, out, name, beans, blanks, mode);

		// out.println("<center><font color=\"white\">" + beans.size() + " of "
		// + beans.size() + " shown</font></center>");

	}

}
