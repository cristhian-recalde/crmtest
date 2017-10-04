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
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.trilogy.framework.auth.AuthMgr;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.True;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.web.border.Border;
import com.trilogy.framework.xhome.web.util.Link;
import com.trilogy.framework.xhome.webcontrol.RequestServicer;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;

import com.trilogy.app.crm.bean.BillingOptionMapping;
import com.trilogy.app.crm.bean.BillingOptionMappingWebControl;
import com.trilogy.app.crm.bean.ErPollerConfig;
import com.trilogy.app.crm.bean.calldetail.AbstractCallDetail;
import com.trilogy.app.crm.bean.calldetail.BillingCategoryEnum;
import com.trilogy.app.crm.bean.calldetail.CallCategorization;
import com.trilogy.app.crm.bean.calldetail.CallDetail;
import com.trilogy.app.crm.bean.calldetail.CallDetailTransientHome;
import com.trilogy.app.crm.bean.calldetail.CallDetailWebControl;
import com.trilogy.app.crm.bean.calldetail.RerateCallDetailHome;
import com.trilogy.app.crm.bean.calldetail.RerateCallDetailTransientHome;
import com.trilogy.app.crm.home.CDRSetSubTypeHome;
import com.trilogy.app.crm.home.calldetail.CallDetailSubAndBANLookupHome;
import com.trilogy.app.crm.home.calldetail.CallDetailZoneHome;
import com.trilogy.app.crm.poller.CallDetailCreator;
import com.trilogy.app.crm.poller.IPCGAdvancedEventCallDetailCreator;
import com.trilogy.app.crm.poller.IPCGCallDetailCreator;
import com.trilogy.app.crm.poller.ProcessorInfo;
import com.trilogy.app.crm.poller.SMSCallDetailCreator;
import com.trilogy.app.crm.poller.URSCallDetailCreator;
import com.trilogy.app.crm.support.DefaultER;
import com.trilogy.app.crm.support.Tracer;
import com.trilogy.app.crm.support.TracerImpl;

/**
 * Call categorization border
 * @author arturo.medina@redknee.com
 *
 */

public class CallCategorizationTestBorder implements Border
{



    /**
	 * Method called by the servlet for displaying the test border
     * @param ctx
     * @param req
     * @param res
     * @param delegate
     * @throws ServletException
     * @throws IOException
	 */
	public void service(final Context ctx, final HttpServletRequest req, final HttpServletResponse res, final RequestServicer delegate)
		throws ServletException, IOException
	{
	    delegate.service(ctx,req,res);

	final AuthMgr amgr = new AuthMgr(ctx);
        if (amgr.check("redknee.admin.testCategory"))
        {
            final PrintWriter out = res.getWriter();


            String cdr = req.getParameter("cc_cdr");
            if (cdr == null)
            {
                cdr = "";
            }

            String action = req.getParameter("cc_action");
            if (action == null)
            {
                action = "";
            }

            out.println("<b>CallCategorization test border</b>");

            final Link link = new Link(ctx);

            out.print("<form action=\"");
            link.write(out);
            out.println("\" method=\"POST\">");

            printCdr(ctx, out, cdr);

            out.println("<input type=\"submit\" name=\"cc_action\" value=\"Test\" \\>");

            out.println("</form>");

            if (action != null && action.trim().length() > 0)
            {
                execute(ctx, out, cdr);
            }
        }
    }


    /**
     * Parses the ER generates a fake call detail and the billing option mapping for this
     * particular call detail.
     *
     * @param _ctx
     *            The operating context.
     * @param out
     *            Output writer.
     * @param cdr
     *            Call detail record.
     */
    private void execute(final Context _ctx, final PrintWriter out, final String cdr)
    {
        final Context ctx = _ctx.createSubContext();

        initializeErPollerConfig(ctx, PAST_DAYS, FUTURE_DAYS);
        ctx.put(Tracer.T, True.instance());
        ctx.put(Tracer.class, new TracerImpl()
        {

            /**
             * {@inheritDoc}
             */
            @Override
            public void trace(final Context ctx, final String message, final Throwable th)
            {
                out.println(message + "-" + th.getMessage());
                out.println("<br/>");
            }


            /**
             * {@inheritDoc}
             */
            @Override
            public void trace(final Context ctx, final String message)
            {
                out.println(message);
                out.println("<br/>");
            }
        });

        final DefaultER er = new DefaultER();
        er.parse(ctx, cdr);

        out.println("<p align=\"center\">");


        // Add table so that it doesn't expand to 100%
        out.println("<table>");

        final List<CallDetail> calls = getCallDetail(ctx, er);

        final Iterator<CallDetail> iter = calls.iterator();
        while (iter.hasNext())
        {
            out.println("<tr><td>");
            final CallDetail call = iter.next();
            final BillingOptionMapping cc = getCallCategory(ctx, call);
            if (call != null)
            {
                final CallDetailWebControl cdWc = new CallDetailWebControl();

                cdWc.toWeb(ctx, out, "Call detail sample", call);
            }
            else
            {
                out.println("Unable to create a call detail");
            }
            out.println("</td><td valign=\"top\">");

            if (cc != null)
            {

                final BillingOptionMappingWebControl boWc = new BillingOptionMappingWebControl();

                boWc.toWeb(ctx, out, "Picked Call category", cc);
            }
            else
            {
                out.println("No Call Categorization rule applicable ");
            }
            out.println("</td></tr>");
        }
        out.println("</table>");

        out.println("</p>");
    }


    /**
     * Returns the call category for the fake call detail and fills the call detail with
     * the proper data based on the call category.
     *
     * @param ctx
     *            The operating context.
     * @param cdr
     *            Call detail record.
     * @return Billing option mapping.
     */
    private BillingOptionMapping getCallCategory(final Context ctx, final CallDetail cdr)
    {

        final CallCategorization cc = (CallCategorization) ctx.get(CallCategorization.class);
        final BillingOptionMapping opt = cc.categorizeCall(ctx, cdr);

        if (opt != null)
        {
            cdr.setTaxAuthority2(opt.getTaxAuthority2());

            final short billCat = cdr.getBillingCategory();

            if (billCat == BillingCategoryEnum.ROAMING_TAX_INDEX || billCat == BillingCategoryEnum.ROAMING_INCOMING_INDEX
                    || billCat == BillingCategoryEnum.ROAMING_OUTGOING_INDEX)
            {

                if (LogSupport.isDebugEnabled(ctx))
                {
                    new DebugLogMsg(getClass().getName(), "set Tax Authority: " + opt.getTaxAuthority2() + " for :"
                        + cdr.getChargedMSISDN(), null).log(ctx);
                }
                cdr.setTaxAuthority1(opt.getTaxAuthority2());
            }
            else
            {
                // same as before
                cdr.setTaxAuthority1(opt.getTaxAuthority());
            }

            if (cdr.getBillingCategory() == AbstractCallDetail.DEFAULT_BILLINGCATEGORY)
            {
                cdr.setBillingCategory(opt.getBillingCategory());
            }

            cdr.setUsageType(opt.getUsageType());
        }

        return opt;
    }

    /**
     * Parses and creates the call detail based on the requested ER.
     *
     * @param ctx
     *            The operating context.
     * @param er
     *            The ER received.
     * @return The list of call details.
     */
    private List<CallDetail> getCallDetail(final Context ctx, final DefaultER er)
    {
        CallDetail cd = null;
        final CallDetailCreator creator = getProcessor(ctx, er);
        final List params = new ArrayList();
        final List<CallDetail> list = new ArrayList<CallDetail>();

	setRerateCallDetail(ctx);

        final ProcessorInfo info = new ProcessorInfo(er.getTimestamp(), String.valueOf(er.getId()), er.getCharFields(), START_INDEX);
		try
		{
			if (creator != null)
			{
				final List<CallDetail> result = creator.createCallDetails(ctx, info,params);
				final Iterator<CallDetail> iter = result.iterator();
				while (iter.hasNext())
				{
					cd = iter.next();
					final Home cdHome = getCallDetailHome(ctx);
					cd = (CallDetail) cdHome.create(ctx, cd);
					list.add(cd);
				}
			}
		}
		catch (final ParseException e)
		{
			LogSupport.minor(ctx, this, "ParseException while creating the fake Call detail "  + e.getMessage(), e);
		}
		catch (final HomeException e)
		{
			LogSupport.minor(ctx, this, "HomeException while creating the fake Call detail "  + e.getMessage(), e);
		}
		catch (final AgentException e)
		{
			LogSupport.minor(ctx, this, "AgentException while creating the fake Call detail "  + e.getMessage(), e);
		}
		catch (final Throwable e)
		{
			LogSupport.minor(ctx, this, "Exception while creating the fake Call detail "  + e.getMessage(), e);
		}
		return list;
	}

    /**
     * Creates a fake RerateCalldetail home so we don;t intervene with this home elsewhere
     *
     * @param ctx
     *            The operating context.
     */
    private void setRerateCallDetail(final Context ctx)
    {
        final Home rerate = new RerateCallDetailTransientHome(ctx);
        ctx.put(RerateCallDetailHome.class, rerate);
    }


    /**
     * Generates a fake Call detail home to fill the most important data for the call
     * category.
     *
     * @param ctx
     *            The operating context.
     * @return Call detail home in the context.
     */
    private Home getCallDetailHome(final Context ctx)
    {
        Home home = new CallDetailTransientHome(ctx);
        home = new CDRSetSubTypeHome(home);
        home = new CallDetailZoneHome(ctx, home);
        home = new CallDetailSubAndBANLookupHome(home);
        return home;
    }


    /**
     * Based on the ER Id it will return the proper Call detail creator.
     *
     * @param ctx
     *            The operating context.
     * @param er
     *            The ER being processed.
     * @return Call detail creator.
     */
    private CallDetailCreator getProcessor(final Context ctx, final DefaultER er)
    {
        switch (er.getId())
        {
            case 501:
                return getER501Creator(ctx, er);
            case 311:
                return new SMSCallDetailCreator(ctx);
            case 511:
                return new IPCGAdvancedEventCallDetailCreator();
        }

        return null;
    }


    /**
     * Returns ER501 creator.
     *
     * @param ctx
     *            The operating context.
     * @param er
     *            ER being processed.
     * @return ER501 call detail creator.
     */
    private CallDetailCreator getER501Creator(final Context ctx, final DefaultER er)
    {
        CallDetailCreator creator = null;

        final String erName = er.getSid().toUpperCase();
        final String subString = IPCGW_NAME.toUpperCase();

        if (erName.startsWith(subString))
        {
            creator = IPCGCallDetailCreator.instance();
        }
        else
        {
            creator = new URSCallDetailCreator();
        }

        return creator;
    }


    /**
     * Prints a call detail record.
     *
     * @param ctx
     *            The operating context.
     * @param out
     *            The write the call detail to.
     * @param cdr
     *            Call detail to write.
     */
    private void printCdr(final Context ctx, final PrintWriter out, final String cdr)
    {
        out.print("<p><TEXTAREA cols=\"80\" rows=\"4\" name=\"cc_cdr\">");
        out.print(cdr);
        out.println("</TEXTAREA></p>");
    }

    /**
     * Initializes an ErPollerConfig object, because parsing of the ER data
     * performs a date validation.
     *
     * @param context The operating context.
     * @param pastDays The number of days into the past beyond which an ER will
     *        be skipped for being too old.
     * @param futureDays The number of days into the future beyond which an ER
     *        will be skipped for being too premature.
     */
    private void initializeErPollerConfig(
        final Context context,
        final int pastDays,
        final int futureDays)

    {
        final ErPollerConfig config = new ErPollerConfig();
        config.setPastValidDays(pastDays);
        config.setFutureValidDays(futureDays);

        context.put(ErPollerConfig.class, config);
    }

    /**
     * IPCG ER name.
     */
    private static final String IPCGW_NAME = "Synaxis-5600";
    private static final int FUTURE_DAYS = 365;
    private static final int PAST_DAYS = 365;
    private static final int START_INDEX = 20;

}
