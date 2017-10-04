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

import java.io.PrintWriter;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.web.renderer.TableRenderer;
import com.trilogy.framework.xhome.web.renderer.TableRendererProxy;
import com.trilogy.framework.xhome.webcontrol.ColourSettings;
import com.trilogy.framework.xlog.log.MinorLogMsg;

import com.trilogy.app.crm.bean.AccountHistory;
import com.trilogy.app.crm.bean.AccountHistoryTableWebControl;
import com.trilogy.app.crm.bean.AccountHistoryTypeEnum;
import com.trilogy.app.crm.support.FrameworkSupportHelper;

/**
 * @author dng
 */
public class AccountHistoryTableBoldWebControl
    extends AccountHistoryTableWebControl
{
    public TableRenderer tableRenderer(Context ctx)
    {
       return new AccountHistoryTableBoldRenderer(ctx, FrameworkSupportHelper.get(ctx).getTableRenderer(ctx));
    }
}

class AccountHistoryTableBoldRenderer
    extends TableRendererProxy
{
    protected Context ctx_;
    public AccountHistoryTableBoldRenderer(Context ctx, TableRenderer delegate)
    {
        super(delegate);
        ctx_ = ctx;
    }
    
    public void TR(PrintWriter out, Object bean, int i)
    {
        try
        {
            AccountHistory acctHist = (AccountHistory) bean;
            
            if (i % 2 == 0)
            {

                out.print("<tr bgcolor=\"");
                ColourSettings colours = ColourSettings.getSettings(ctx_);
                out.print(colours.getTableRowAltBG());

                if ( acctHist.getType() == AccountHistoryTypeEnum.INVOICE )
                {
                    out.print("\" style=\"font-weight: bold\">");
                }
                else
                {
                    out.print("\">");
                }
            }
            else
            {
                if ( acctHist.getType() == AccountHistoryTypeEnum.INVOICE )
                {
                    out.print("<tr style=\"font-weight: bold\">");
                }
                else
                {
                    out.print("<tr>");
                }
            }
            
        }
        catch (Throwable t)
        {
            new MinorLogMsg(this, "fail to render TR correctly", t).log(ctx_);
        }
    }
}
