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

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.language.Lang;
import com.trilogy.framework.xhome.language.MessageMgr;
import com.trilogy.framework.xhome.web.renderer.ButtonRenderer;
import com.trilogy.framework.xhome.web.renderer.DefaultButtonRenderer;
import com.trilogy.framework.xhome.web.renderer.DefaultFormRenderer;
import com.trilogy.framework.xhome.web.renderer.FormRenderer;
import com.trilogy.framework.xhome.web.renderer.TableRenderer;
import com.trilogy.framework.xhome.web.util.ImageLink;
import com.trilogy.framework.xhome.web.util.Link;
import com.trilogy.framework.xhome.webcontrol.WebController;
import com.trilogy.framework.xhome.webcontrol.WebControllerSummaryRequestServicer;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;

/**
 * @author cindy.wong@redknee.com
 * @since 2010-12-22
 */
public class DuplicateAccountDetectionWebControllerSummaryRequestServicer
    extends WebControllerSummaryRequestServicer
{

	/**
	 * Constructor for
	 * DuplicateAccountDetectionWebControllerSummaryRequestServicer.
	 * 
	 * @param ctrl
	 */
	public DuplicateAccountDetectionWebControllerSummaryRequestServicer(
	    WebController ctrl)
	{
		super(ctrl);
	}

	@Override
	public void service(Context ctx, HttpServletRequest req,
	    HttpServletResponse res) throws ServletException, IOException
	{
		PrintWriter out = res.getWriter();
		String size = null;
		// String format of entry number
		int asked = 0;
		// user input
		int count = 0;
		// entry number in collection
		int current_row = 0;
		// row number displayed in current page
		Collection c = null;
		Context resCtx = null;
		int num_pages = 0;
		// page number for displaying all the entries
		int page = 0;
		int pageSize = ctx.getInt(PAGE_SIZE, DEFAULT_PAGE_SIZE);
		int pageNum = ctx.getInt(MAX_PAGE_LINKS, DEFAULT_MAX_PAGE_LINKS);
		String extra_query = null;
		MessageMgr mmgr = new MessageMgr(ctx, ctrl_.getModule());
		ButtonRenderer br =
		    (ButtonRenderer) ctx.get(ButtonRenderer.class,
		        DefaultButtonRenderer.instance());
		Lang lang = (Lang) ctx.get(Lang.class);
		String display_lang =
		    (lang == null) ? Lang.DEFAULT.getCode() : lang.getCode();

		Context subCtx = ctx.createSubContext();

		try
		{
			page = Integer.parseInt(req.getParameter("page"));
		}
		catch (NumberFormatException ne)
		{
		}

		// following 2 lines have been moved from SimpleSearchBorder
		// since GUI has been broken if setting NullBorder as summaryBorder
		out.println("<center><table><tbody><tr>");
		out.print("<td colspan=\"3\">");

		try
		{
			try
			{
				c = ctrl_.getHome(ctx).selectAll();

				// subCtx.put("MODE", new Integer(WebControl.DISPLAY_MODE));
				subCtx.put("ACTIONS", Boolean.TRUE);
				count = c.size();
				// Collection size
				size = String.valueOf(count);
				num_pages = (count + pageSize - 1) / pageSize;
				// total page number

				// /////////////////////////////////// display data in whole
				// Collection
				// Copy out the page of data that we're interested unless there
				// is
				// less than one page worth of data in the first place
				if (pageSize < c.size())
				{
					List list = new ArrayList(c);
					List subList = new ArrayList();

					for (int i = page * pageSize; i < Math.min((page + 1)
					    * pageSize, c.size()); i++)
					{
						subList.add(list.get(i));
						current_row++;
					}

					c = subList;
				}

				// go to print out table with defined size
				ctrl_.getTableWebControl().toWeb(subCtx, out, "", c);
			}
			catch (NullPointerException e)
			{
				out.println(mmgr
				    .get("DevNoHomeErr",
				        "<b>INTERNAL ERROR: Check that Home supplied in Context!</b>"));
				out.println("<!--");
				e.printStackTrace(out);
				out.println("\n-->");
			}
			catch (UnsupportedOperationException e)
			{
				out.println(mmgr.get("NoTableView",
				    "<b>Please Enter Search Criteria</b>"));
			}

			out.print("</td></tr><tr><td></td></tr><tr><td>");
			out.print("<font color=\"#003366\"><b>");

			// out.print(current_row + " of " + size + " entries shown");
			if (size != null)
			{
				out.print(mmgr.get("NumOfEntries", "Total {0} entries",
				    new Object[]
				{
					size
				}));
				// out.print(size + " " + mmgr.get("entries", "entries") );
			}

			if (num_pages > 1)
			{
				out.print("&nbsp;|&nbsp;");
				out.print(mmgr.get("DisplayingFirst",
				    "Displaying first {0} entries", new Object[]
				    {
					    Integer.valueOf(pageSize)
				    }));
			}
			out.print("</b></font>");
			// if ( num_pages > 1 )
			// {
			((FormRenderer) ctx.get(FormRenderer.class,
			    DefaultFormRenderer.instance())).FormEnd(out);
			// }

			out.print("</td><th align=\"right\" valign=\"top\">");

			// /////////////////////////////////////// display "New" button
			Link link2 = new Link(ctx);

			TableRenderer buttonTr = tableRenderer(ctx, "dupulicateClass");
			buttonTr.Table(ctx, out, "");
			buttonTr.TR(ctx, out, null, 0);

			if (ctrl_.getNewPredicate().f(ctx, ""))
			{
				ImageLink image = new ImageLink(ctx);

				link2.add("CMD", "New");

				buttonTr.TD(ctx, out);
				br.linkButton(out, ctx, "New", "New", link2);
				buttonTr.TDEnd(ctx, out);
			}

			if (ctrl_.getHelpPredicate().f(ctx, ""))
			{
				buttonTr.TD(ctx, out);
				ctrl_.outputHelpLink(ctx, req, res);
				buttonTr.TDEnd(ctx, out);
			}

			buttonTr.TREnd(ctx, out);
			buttonTr.TableEnd(ctx, out, "");

			out.print("</th></tr>");

		}
		catch (HomeException e)
		{
			out.println("<font color=\"red\"><b>Error: " + e.getMessage()
			    + "</b></font><br/>");
			out.println("<!--");
			e.printStackTrace(out);
			out.println("\n-->");

			if (LogSupport.isDebugEnabled(ctx))
			{
				new DebugLogMsg(this, e.getMessage(), e).log(ctx);
			}

			throw new IOException(e.getMessage());
		}
		finally
		{
			out.println("</td></tr></tbody></table></center>");
		}
	}
}
