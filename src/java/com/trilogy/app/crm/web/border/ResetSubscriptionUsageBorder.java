package com.trilogy.app.crm.web.border;

import java.io.IOException;
import java.io.PrintWriter;
import java.security.Permission;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SystemNoteSubTypeEnum;
import com.trilogy.app.crm.bean.SystemNoteTypeEnum;
import com.trilogy.app.crm.client.bm.BalanceManagementSupport;
import com.trilogy.app.crm.client.bm.SubscriberProfileProvisionClient;
import com.trilogy.app.crm.client.bm.SubscriberProfileProvisionException;
import com.trilogy.app.crm.subscriber.provision.SubscriberProvisionResultCode;
import com.trilogy.app.crm.support.NoteSupportHelper;
import com.trilogy.app.crm.support.SubscriberSupport;
import com.trilogy.framework.core.locale.Currency;
import com.trilogy.framework.core.locale.CurrencyHome;
import com.trilogy.framework.xhome.auth.SimplePermission;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.web.border.Border;
import com.trilogy.framework.xhome.webcontrol.RequestServicer;
import com.trilogy.framework.xlog.log.InfoLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MajorLogMsg;


public class ResetSubscriptionUsageBorder implements Border
{

    @Override
    public void service(Context ctx, HttpServletRequest req, HttpServletResponse res, RequestServicer delegate)
            throws ServletException, IOException
    {
        Subscriber sub = (Subscriber) ctx.get(Subscriber.class);
 
        final PrintWriter out = res.getWriter();
        String action = req.getParameter("cc_action");
        if (action != null && sub != null)
        {
            if (action.equals(RESET_Monthly_Spend_Limit))
            {
                executeResetMonthlySpendLimit(ctx, out, sub);
            }
            else if (action.equals(RESET_Group_Usage))
            {
                executeResetGroupUsage(ctx, out, sub);
            }
        }
        delegate.service(ctx, req, res);
    }


    private void executeResetMonthlySpendLimit(Context ctx, PrintWriter out, Subscriber sub)
    {
        new InfoLogMsg(this, "Monthly spend limit reset sub [" +sub.getId() + "]", null).log(ctx);
        try
        {
            sub.resetMonthlySpendLimit(ctx);
        } 
        catch (HomeException e)
        {
            out.println("<td><font color=\"red\">" + e.getMessage() + "</font></td>");
        }
    }


    private void executeResetGroupUsage(Context ctx, PrintWriter out, Subscriber sub)
    {
        new InfoLogMsg(this, "Reset group usage for sub [" +sub.getId() + "]", null).log(ctx);

        try
        {
            sub.resetGroupUsage(ctx);
        }
        catch (final HomeException e)
        {
            out.println("<td><font color=\"red\">" + e.getMessage() + "</font></td>");
        }
    }

    public final static String RESET_Monthly_Spend_Limit = "Reset Monthly Usage";
    public final static String RESET_Group_Usage = "Reset Group Usage";
    
    public final static Permission RESET_Monthly_Spend_Limit_Permission = new SimplePermission("app.crm.usage.reset.monthly");
    public final static Permission RESET_Group_Usage_Permission = new SimplePermission("app.crm.usage.reset.group");
}
