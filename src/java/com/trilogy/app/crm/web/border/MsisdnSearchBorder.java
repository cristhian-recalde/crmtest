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
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

import com.trilogy.app.crm.bean.ui.MsisdnHome;
import com.trilogy.app.crm.bean.ui.Msisdn;
import com.trilogy.app.crm.bean.ui.MsisdnXInfo;
import com.trilogy.app.crm.bean.search.MsisdnSearch;
import com.trilogy.app.crm.bean.search.MsisdnSearchWebControl;
import com.trilogy.app.crm.bean.search.MsisdnSearchXInfo;
import com.trilogy.app.crm.support.MultiDbSupportHelper;
import com.trilogy.framework.xhome.beans.ExceptionListener;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.web.renderer.ButtonRenderer;
import com.trilogy.framework.xhome.web.renderer.DefaultButtonRenderer;
import com.trilogy.framework.xhome.web.search.LimitSearchAgent;
import com.trilogy.framework.xhome.web.search.SearchBorder;
import com.trilogy.framework.xhome.web.search.Wildcard2SelectSearchAgent;
import com.trilogy.framework.xhome.web.util.Link;
import com.trilogy.framework.xhome.webcontrol.RequestServicer;
import com.trilogy.framework.xhome.webcontrol.WebControllerConstants;
import com.trilogy.framework.xhome.xdb.SimpleXStatement;
import com.trilogy.framework.xhome.xdb.XDB;
import com.trilogy.framework.xhome.xdb.XStatement;
import com.trilogy.framework.xhome.xdb.visitor.SingleValueXDBVisitor;
import com.trilogy.framework.xlog.log.MinorLogMsg;

/**
 * @author yassir.pakran@redknee.com
 */
public class MsisdnSearchBorder extends SearchBorder
{
    private boolean alphNumericEntered_ = false;

    /**
     * @param context
     */
    public MsisdnSearchBorder(final Context context)
    {
        this(context, MsisdnHome.class);
    }


    public MsisdnSearchBorder(final Context context, final Class homeClass)
    {
        super(context, homeClass, Msisdn.class, new MsisdnSearchWebControl());
        // MSISDN
        addAgent((new Wildcard2SelectSearchAgent(MsisdnXInfo.MSISDN, MsisdnSearchXInfo.MSISDN, true).disableEmptySearch(true)));
        addAgent(new LimitSearchAgent(MsisdnSearchXInfo.LIMIT));
    }

    /**
     * @param msisdn
     * @return
     */
    protected boolean checkMsisdn(final String msisdn)
    {
        int count = 0;
        int alphaCount = 0;
        boolean bool = false;
        String pattern = "[0-9]{1}";
        String alphaPattern = "[a-z]{1}";
        Pattern pat = Pattern.compile(pattern);
        Pattern patAlpha = Pattern.compile(alphaPattern);
        //   char array[] = msisdn.toCharArray();
        for (int i = 0; i <= (msisdn.length() - 1); i++)
        {
            String subString = new String();
            subString = subString + msisdn.charAt(i);
            if (pat.matcher(subString).matches())
            {
                count++;
            }
            else if (patAlpha.matcher(subString).matches())
            {
                alphaCount++;
            }
        }
        if (count > 3 && alphaCount == 0)
        {
            bool = true;
        }
        else if (alphaCount > 0)
        {
            this.alphNumericEntered_ = true;
        }

        return bool;
    }

    @Override
    public void service(final Context ctx, final HttpServletRequest req, final HttpServletResponse res,
            final RequestServicer delegate) throws ServletException, IOException
    {
        Context subCtx = ctx.createSubContext();
        subCtx.put(WebControllerConstants.PAGE_SIZE, 50);
        boolean proceed = true;
        PrintWriter out = res.getWriter();

        final Link link = new Link(ctx);
        final ButtonRenderer brend = (ButtonRenderer) ctx.get(ButtonRenderer.class, DefaultButtonRenderer.instance());
        final Object bean = webcontrol_.fromWeb(
                // This prevents errors from
                ctx.createSubContext().put(ExceptionListener.class, null), new HttpServletRequestWrapper(req)
        {

            // Record criterial parameters so that they can be copied into the
            // Link prototype if
            // required
            @Override
            public String getParameter(String key)
            {
                link.copy((HttpServletRequest) getRequest(), key);
                return super.getParameter(key);
            }
        }, brend.isButton(ctx, "Clear") ? ".notsearch" : ".search");
        MsisdnSearch criteria = (MsisdnSearch) bean;
        if ((criteria != null) && (criteria.getMsisdn() != null && (criteria.getMsisdn().length() != 0)))
        {
            // int length = criteria.getMsisdn().length();
            if (checkMsisdn(criteria.getMsisdn()))
            {
                String wildCardString = modify(criteria.getMsisdn());
                int limit = criteria.getLimit() + 1;
                try
                {
                    XDB xdb = (XDB) ctx.get(XDB.class);

                    final int BAD_RESULT = -1;
                    int count = BAD_RESULT;

                    final String tableName = MultiDbSupportHelper.get(ctx).getTableName(ctx, MsisdnHome.class, "MSISDN");

                    XStatement sql = new SimpleXStatement(
                            "select count(*) from " + tableName + " where msisdn like '" + wildCardString + "'");

                    count = SingleValueXDBVisitor.getInt(ctx, xdb, sql);

                    if (count > (limit - 1))
                    {
                        proceed = false;
                    }
                }
                catch (Exception e)
                {
                    new MinorLogMsg(this, "Exception might have been thrown while connecting to DB.Report to Dev immediately if u see this", e).log(ctx);
                }

                if (!proceed)
                {
                    out.println("Results greater than " + (limit - 1) + " Please narrow your search");
                }
                else
                {
                    super.service(subCtx, req, res, delegate);
                }
            }
            else
            {
                if (this.alphNumericEntered_)
                {
                    out.println("Entered Msisdn <font = 'red'>" + criteria.getMsisdn() + "</font> is not a valid Msisdn." +
                            " Please enter a valid msisdn and try again !");
                    this.alphNumericEntered_ = false;
                }
                else
                {
                    out.println("Number of digits should be more than 4");
                }
            }
        }
        else
        {
            super.service(subCtx, req, res, delegate);
        }
    }

    /**
     * @param searchString
     * @return
     */
    private String modify(String searchString)
    {
        if ((searchString.indexOf("*") == -1) && (searchString.length() > 0))
        {
            searchString = searchString + '%';
        }
        if ((searchString != null) && (searchString.trim().length() > 1))
        {
            searchString = searchString.replaceAll("[/*]", "_");
        }
        return searchString;
    }
}